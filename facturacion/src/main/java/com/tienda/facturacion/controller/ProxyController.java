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

    // Clientes (Grupo 1) - obtener lista
    @GetMapping("/clientes")
    public Object obtenerClientes() {
        return restTemplate.getForObject(clientesUrl + "/api/clientes", Object.class);
    }

    // Clientes (Grupo 1) - registrar nuevo cliente
    @PostMapping("/clientes")
    public Object registrarCliente(@RequestBody Object body) {
        return restTemplate.postForObject(clientesUrl + "/api/clientes", body, Object.class);
    }

    // Productos (Grupo 4) - reducir stock
    // Grupo 4 usa @RequestBody List<StockUpdateRequest> con campo idProducto y cantidad
    @PutMapping("/productos/reducir-stock")
    public Object reducirStock(@RequestBody Object body) {
        return restTemplate.exchange(
            productosUrl + "/api/productos/reducir-stock",
            HttpMethod.PUT,
            new HttpEntity<>(body),
            Object.class
        ).getBody();
    }
}