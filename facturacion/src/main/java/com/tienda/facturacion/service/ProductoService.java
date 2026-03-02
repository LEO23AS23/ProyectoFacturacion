package com.tienda.facturacion.service;

import com.tienda.facturacion.model.Producto;
import com.tienda.facturacion.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired 
    private ProductoRepository productoRepo;

    public List<Producto> listarTodos() {
        return productoRepo.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepo.findById(id).orElse(null);
    }

    public Producto guardar(Producto producto) {
        if (producto.getPrecio() < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }
        return productoRepo.save(producto);
    }

    public void eliminar(Long id) {
        productoRepo.deleteById(id);
    }
}