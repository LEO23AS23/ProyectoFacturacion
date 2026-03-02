package com.tienda.facturacion.controller;

import com.tienda.facturacion.model.Cliente;
import com.tienda.facturacion.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired private ClienteService clienteService;

    @GetMapping public List<Cliente> listar() { return clienteService.listarTodos(); }
    @PostMapping public Cliente crear(@RequestBody Cliente cliente) { return clienteService.guardar(cliente); }
    
    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        cliente.setId(id);
        return clienteService.guardar(cliente);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { clienteService.eliminar(id); }
}