package com.tienda.facturacion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/proxy")
@CrossOrigin(origins = "*")
public class ProxyController {

    @Value("${microservicio.clientes.url}")
    private String clientesUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/clientes")
    public Object obtenerClientes() {
        return restTemplate.getForObject(clientesUrl + "/api/clientes", Object.class);
    }
    @Value("${microservicio.productos.url}")
private String productosUrl;

@PutMapping("/productos/reducir-stock")
public Object reducirStock(@RequestBody Object body) {
    return restTemplate.postForObject(
        productosUrl + "/api/productos/reducir-stock", 
        body, 
        Object.class
    );
}
}