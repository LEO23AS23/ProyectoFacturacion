package com.tienda.facturacion.controller;

import org.springframework.beans.factory.annotation.Value;
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

    // ── Productos stock (Grupo 4) ────────────────────────────────────────
    // Su endpoint usa @RequestParam, no @RequestBody
    // Llamada: PUT /api/proxy/productos/reducir-stock?idProducto=23&cantidad=1
    @PutMapping("/productos/reducir-stock")
    public Object reducirStock(@RequestParam Integer idProducto,
                               @RequestParam Integer cantidad) {
        String url = productosUrl + "/api/productos/reducir-stock"
                   + "?idProducto=" + idProducto
                   + "&cantidad=" + cantidad;
        return restTemplate.postForObject(url, null, Object.class);
    }
}