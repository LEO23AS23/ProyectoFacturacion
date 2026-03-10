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

    // Cuando el Grupo 2 te dé su IP → actualiza application.properties
    // microservicio.envios.url=http://IP_GRUPO2:PUERTO
    @Value("${microservicio.envios.url:PENDIENTE}")
    private String enviosUrl;

    public List<Factura> listarTodas() {
        return facturaRepo.findAll();
    }

    @Transactional
    public Factura procesarVentaECommerce(Factura facturaRequest) {

        // ── PASO 1: Cliente ─────────────────────────────────────────────
        // Busca por dni en tu BD local, si no existe lo crea
        // (el cliente real vive en Grupo 1, aquí solo guardamos referencia)
        String dniRecibido = facturaRequest.getCliente().getDni();
        if (dniRecibido == null || dniRecibido.isEmpty()) {
            throw new RuntimeException("DNI del cliente es requerido.");
        }

        Cliente cliente = clienteRepo.findByDni(dniRecibido)
            .orElseGet(() -> {
                Cliente nuevo = new Cliente();
                nuevo.setDni(dniRecibido);
                nuevo.setNombre(facturaRequest.getCliente().getNombre());
                nuevo.setEmail(facturaRequest.getCliente().getEmail());
                nuevo.setDireccion(facturaRequest.getCliente().getDireccion());
                System.out.println("Cliente guardado localmente: " + dniRecibido);
                return clienteRepo.save(nuevo);
            });

        facturaRequest.setCliente(cliente);

        // ── PASO 2: Detalles ────────────────────────────────────────────
        // NO buscamos productos en BD local — vienen del frontend (Grupo 4)
        double subtotalGlobal = 0;

        for (DetalleFactura detalle : facturaRequest.getDetalles()) {
            if (detalle.getPrecioUnitario() == null || detalle.getCantidad() == null) {
                throw new RuntimeException("Precio o cantidad inválidos.");
            }
            detalle.setFactura(facturaRequest);
            detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad());
            subtotalGlobal += detalle.getSubtotal();
        }

        // ── PASO 3: IVA via tax-service ─────────────────────────────────
        String taxUrl = "http://tax-service:8081/api/tax/calcular?subtotal=" + subtotalGlobal;
        try {
            TaxResponse tax = restTemplate.getForObject(taxUrl, TaxResponse.class);
            if (tax != null) {
                facturaRequest.setTotal(tax.getTotal());
                System.out.println("IVA: " + tax.getIva() + " | Total: " + tax.getTotal());
            } else {
                facturaRequest.setTotal(subtotalGlobal * 1.15);
            }
        } catch (Exception e) {
            System.err.println("Tax-Service no disponible, calculando manualmente.");
            facturaRequest.setTotal(subtotalGlobal * 1.15);
        }

        // ── PASO 4: Guardar factura ─────────────────────────────────────
        Factura guardada = facturaRepo.save(facturaRequest);

        Factura facturaCompleta = facturaRepo.findById(guardada.getId())
                .orElseThrow(() -> new RuntimeException("Error recuperando factura."));

        // ── PASO 5: Email ───────────────────────────────────────────────
        try {
            emailService.enviarFacturaPdf(facturaCompleta);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }

        // ── PASO 6: Orden de envío (Grupo 2) ────────────────────────────
        // Se activa solo cuando tengas la IP — no bloquea la factura
        if (!enviosUrl.equals("PENDIENTE")) {
            try {
                Map<String, Object> envio = new HashMap<>();
                envio.put("facturaId",     facturaCompleta.getId());
                envio.put("clienteDni",    facturaCompleta.getCliente().getDni());
                envio.put("clienteNombre", facturaCompleta.getCliente().getNombre());
                envio.put("direccion",     facturaCompleta.getCliente().getDireccion());
                envio.put("productos",     facturaCompleta.getDetalles());
                restTemplate.postForObject(enviosUrl + "/api/envios", envio, Object.class);
                System.out.println("Orden de envío creada ✅");
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