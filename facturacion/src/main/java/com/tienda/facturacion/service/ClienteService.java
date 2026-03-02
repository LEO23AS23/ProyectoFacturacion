package com.tienda.facturacion.service;

import com.tienda.facturacion.model.Cliente;
import com.tienda.facturacion.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClienteService {
    @Autowired private ClienteRepository clienteRepo;

    public List<Cliente> listarTodos() { return clienteRepo.findAll(); }
    public Cliente guardar(Cliente cliente) { return clienteRepo.save(cliente); }
    public Cliente buscarPorId(Long id) { return clienteRepo.findById(id).orElse(null); }
    public void eliminar(Long id) { clienteRepo.deleteById(id); }
}