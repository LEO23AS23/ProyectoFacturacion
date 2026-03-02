package com.tienda.facturacion.service;

import com.tienda.facturacion.model.*;
import com.tienda.facturacion.repository.*;
import com.tienda.facturacion.dto.TaxResponse; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; 

import java.util.List;

@Service
public class FacturaService {

    @Autowired private FacturaRepository facturaRepo;
    @Autowired private ProductoRepository productoRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private EmailService emailService;
    @Autowired private RestTemplate restTemplate; 

    public List<Factura> listarTodas() {
        return facturaRepo.findAll();
    }

    @Transactional
    public Factura procesarVentaECommerce(Factura facturaRequest) {
        Cliente cliente = clienteRepo.findById(facturaRequest.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe."));

        facturaRequest.setCliente(cliente);
        double subtotalGlobal = 0; 

        for (DetalleFactura detalle : facturaRequest.getDetalles()) {
            Producto producto = productoRepo.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

            if (producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepo.save(producto);

            detalle.setFactura(facturaRequest);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * detalle.getCantidad());
            subtotalGlobal += detalle.getSubtotal();
        }

        String url = "http://tax-contenedor:8081/api/tax/calcular?subtotal=" + subtotalGlobal;
        
        try {
            System.out.println("Solicitando cálculo de IVA para subtotal: " + subtotalGlobal);
            TaxResponse taxResponse = restTemplate.getForObject(url, TaxResponse.class);
            
            if (taxResponse != null) {
                facturaRequest.setTotal(taxResponse.getTotal());
                System.out.println("IVA recibido: " + taxResponse.getIva() + " | Total final: " + taxResponse.getTotal());
            }
        } catch (Exception e) {
            System.err.println("Error al contactar Tax-Service: " + e.getMessage());
            facturaRequest.setTotal(subtotalGlobal); 
        }

        Factura facturaGuardada = facturaRepo.save(facturaRequest);
        
        Factura facturaParaEmail = facturaRepo.findById(facturaGuardada.getId())
                .orElseThrow(() -> new RuntimeException("Error al recuperar factura para email"));

        emailService.enviarFacturaPdf(facturaParaEmail);

        return facturaParaEmail;
    }

    public TaxResponse simularImpuesto(Double subtotal) {
        String url = "http://tax-contenedor:8081/api/tax/calcular?subtotal=" + subtotal;
        try {
            return restTemplate.getForObject(url, TaxResponse.class);
        } catch (Exception e) {
            return new TaxResponse(subtotal, 0.0, subtotal);
        }
    }
}