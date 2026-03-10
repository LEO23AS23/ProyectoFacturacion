package com.tienda.facturacion.repository;

import com.tienda.facturacion.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // ✅ FIX: buscar por dni en vez de id
    Optional<Cliente> findByDni(String dni);
}