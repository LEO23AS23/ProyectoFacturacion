package com.tienda.facturacion.controller;

import com.tienda.facturacion.service.ProductoService;
import com.tienda.facturacion.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Usamos @Controller para devolver HTML, no JSON
public class DashboardController {

    @Autowired private ProductoService productoService;
    @Autowired private ClienteService clienteService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("clientes", clienteService.listarTodos());
        return "index";
    }
}