package com.tienda.facturacion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    // ── Clientes (Grupo 1) ───────────────────────────────────────────────
    @GetMapping("/clientes")
    public Object obtenerClientes() {
        return restTemplate.getForObject(clientesUrl + "/api/clientes", Object.class);
    }

    // ── Productos (Grupo 4) ──────────────────────────────────────────────
    @PutMapping("/productos/reducir-stock")
    public Object reducirStock(@RequestBody Object body) {
        return restTemplate.postForObject(
            productosUrl + "/api/productos/reducir-stock",
            body,
            Object.class
        );
    }
}