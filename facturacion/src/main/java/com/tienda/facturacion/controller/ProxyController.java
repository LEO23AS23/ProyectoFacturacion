package com.tienda.facturacion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/proxy")
@CrossOrigin(origins = "*")
public class ProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${microservicio.clientes.url}")
    private String clientesUrl;

    @Value("${microservicio.productos.url}")
    private String productosUrl;

    // ── Clientes (Grupo 1) - obtener lista ──────────────────────────────
    @GetMapping("/clientes")
    public Object obtenerClientes() {
        return restTemplate.getForObject(clientesUrl + "/api/clientes", Object.class);
    }

    // ── Clientes (Grupo 1) - registrar nuevo ────────────────────────────
    // ✅ FIX: agrega Content-Type: application/json explícitamente
    @PostMapping("/clientes")
    public ResponseEntity<Object> registrarCliente(@RequestBody Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                clientesUrl + "/api/clientes",
                HttpMethod.POST,
                entity,
                Object.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al registrar cliente: " + e.getMessage());
        }
    }

    // ── Productos (Grupo 4) - reducir stock ─────────────────────────────
    @PutMapping("/productos/reducir-stock")
    public Object reducirStock(@RequestBody Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(
            productosUrl + "/api/productos/reducir-stock",
            HttpMethod.PUT,
            entity,
            Object.class
        ).getBody();
    }
}