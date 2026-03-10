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

        // ✅ FIX: ya no busca por id (que llega null)
        // Busca por dni, si no existe lo crea automáticamente
        String dniRecibido = facturaRequest.getCliente().getDni();

        Cliente cliente = clienteRepo.findByDni(dniRecibido)
            .orElseGet(() -> {
                Cliente nuevo = new Cliente();
                nuevo.setDni(dniRecibido);
                nuevo.setNombre(facturaRequest.getCliente().getNombre());
                nuevo.setEmail(facturaRequest.getCliente().getEmail());
                return clienteRepo.save(nuevo);
            });

        facturaRequest.setCliente(cliente);
        double subtotalGlobal = 0;

        for (DetalleFactura detalle : facturaRequest.getDetalles()) {
            // ✅ FIX: los productos vienen del microservicio externo (Grupo 4)
            // No buscamos en nuestra BD local, usamos precio/cantidad del request
            Long productoId = detalle.getProducto().getId();

            // Intentamos buscar en BD local, si no existe creamos uno temporal
            Producto producto = productoRepo.findById(productoId)
                .orElseGet(() -> {
                    Producto temp = new Producto();
                    temp.setId(productoId);
                    temp.setNombre("Producto-" + productoId);
                    temp.setPrecio(detalle.getPrecioUnitario());
                    temp.setStock(9999); // stock alto porque el real está en Grupo 4
                    return productoRepo.save(temp);
                });

            detalle.setFactura(facturaRequest);
            detalle.setPrecioUnitario(detalle.getPrecioUnitario()); // usa el precio que viene del frontend
            detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad());
            subtotalGlobal += detalle.getSubtotal();
        }

        // Calcular IVA via tax-service
        String url = "http://tax-contenedor:8081/api/tax/calcular?subtotal=" + subtotalGlobal;
        try {
            System.out.println("Solicitando IVA para subtotal: " + subtotalGlobal);
            TaxResponse taxResponse = restTemplate.getForObject(url, TaxResponse.class);
            if (taxResponse != null) {
                facturaRequest.setTotal(taxResponse.getTotal());
                System.out.println("IVA: " + taxResponse.getIva() + " | Total: " + taxResponse.getTotal());
            } else {
                double iva = subtotalGlobal * 0.15;
                facturaRequest.setTotal(subtotalGlobal + iva);
            }
        } catch (Exception e) {
            System.err.println("Tax-Service no disponible: " + e.getMessage());
            double iva = subtotalGlobal * 0.15;
            facturaRequest.setTotal(subtotalGlobal + iva); // fallback manual 15%
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
            // fallback: calcular manualmente si tax-service no responde
            double iva = subtotal * 0.15;
            return new TaxResponse(subtotal, iva, subtotal + iva);
        }
    }
}