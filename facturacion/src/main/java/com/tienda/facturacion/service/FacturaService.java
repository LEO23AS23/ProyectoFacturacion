package com.tienda.facturacion.service;

import com.tienda.facturacion.model.*;
import com.tienda.facturacion.repository.*;
import com.tienda.facturacion.dto.TaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacturaService {

    @Autowired private FacturaRepository facturaRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private EmailService emailService;
    @Autowired private RestTemplate restTemplate;

    @Value("${microservicio.envios.url:PENDIENTE}")
    private String enviosUrl;

    public List<Factura> listarTodas() {
        return facturaRepo.findAll();
    }

    @Transactional
    public Factura procesarVentaECommerce(Factura facturaRequest) {

        // PASO 1: Buscar o crear cliente por cedula
        String cedulaRecibida = facturaRequest.getCliente().getCedula();
        if (cedulaRecibida == null || cedulaRecibida.isEmpty()) {
            throw new RuntimeException("Cedula del cliente es requerida.");
        }

        Cliente cliente = clienteRepo.findByCedula(cedulaRecibida)
            .orElseGet(() -> {
                Cliente nuevo = new Cliente();
                nuevo.setCedula(cedulaRecibida);
                nuevo.setNombre(facturaRequest.getCliente().getNombre());
                nuevo.setApellido(facturaRequest.getCliente().getApellido());
                nuevo.setCorreo(facturaRequest.getCliente().getCorreo());
                nuevo.setTelefono(facturaRequest.getCliente().getTelefono());
                nuevo.setDireccion(facturaRequest.getCliente().getDireccion());
                nuevo.setEstado("ACTIVO");
                return clienteRepo.save(nuevo);
            });

        facturaRequest.setCliente(cliente);

        // PASO 2: Procesar detalles
        double subtotalGlobal = 0;
        for (DetalleFactura detalle : facturaRequest.getDetalles()) {
            if (detalle.getPrecioUnitario() == null || detalle.getCantidad() == null) {
                throw new RuntimeException("Precio o cantidad invalidos.");
            }
            detalle.setFactura(facturaRequest);
            detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad());
            subtotalGlobal += detalle.getSubtotal();
        }

        // PASO 3: IVA via tax-service
        String taxUrl = "http://tax-service:8081/api/tax/calcular?subtotal=" + subtotalGlobal;
        try {
            TaxResponse tax = restTemplate.getForObject(taxUrl, TaxResponse.class);
            facturaRequest.setTotal(tax != null ? tax.getTotal() : subtotalGlobal * 1.15);
        } catch (Exception e) {
            facturaRequest.setTotal(subtotalGlobal * 1.15);
        }

        // PASO 4: Guardar factura
        Factura guardada = facturaRepo.save(facturaRequest);
        Factura facturaCompleta = facturaRepo.findById(guardada.getId())
                .orElseThrow(() -> new RuntimeException("Error recuperando factura."));

        // PASO 5: Enviar email con PDF
        try {
            emailService.enviarFacturaPdf(facturaCompleta);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }

        // PASO 6: Crear orden de envio (Grupo 2) cuando haya IP
        if (!enviosUrl.equals("PENDIENTE")) {
            try {
                Map<String, Object> envio = new HashMap<>();
                envio.put("facturaId",     facturaCompleta.getId());
                envio.put("clienteCedula", facturaCompleta.getCliente().getCedula());
                envio.put("clienteNombre", facturaCompleta.getCliente().getNombre());
                envio.put("direccion",     facturaCompleta.getCliente().getDireccion());
                envio.put("productos",     facturaCompleta.getDetalles());
                restTemplate.postForObject(enviosUrl + "/api/envios", envio, Object.class);
            } catch (Exception e) {
                System.err.println("Grupo 2 no disponible: " + e.getMessage());
            }
        }

        return facturaCompleta;
    }

    public TaxResponse simularImpuesto(Double subtotal) {
        String url = "http://tax-service:8081/api/tax/calcular?subtotal=" + subtotal;
        try {
            return restTemplate.getForObject(url, TaxResponse.class);
        } catch (Exception e) {
            double iva = subtotal * 0.15;
            return new TaxResponse(subtotal, iva, subtotal + iva);
        }
    }
}