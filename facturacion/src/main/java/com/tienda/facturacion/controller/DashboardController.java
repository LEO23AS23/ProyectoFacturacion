package com.tienda.facturacion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // Lee las URLs desde application.properties
    @Value("${microservicio.productos.url}")
    private String productosUrl;

    @Value("${microservicio.clientes.url}")
    private String clientesUrl;

    @Value("${microservicio.envios.url}")
    private String enviosUrl;

    @Value("${microservicio.pedidos.url}")
    private String pedidosUrl;

    @Value("${microservicio.payment.url}")
    private String paymentUrl;

    @GetMapping("/")
    public String index(Model model) {
        // Pasa las URLs al frontend — así el HTML no tiene IPs hardcodeadas
        model.addAttribute("productosUrl",  productosUrl);
        model.addAttribute("clientesUrl",   clientesUrl);
        model.addAttribute("enviosUrl",     enviosUrl);
        model.addAttribute("pedidosUrl",    pedidosUrl);
        model.addAttribute("paymentUrl",    paymentUrl);
        return "index";
    }
}