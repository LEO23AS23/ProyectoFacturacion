package com.tienda.facturacion.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cedula;    
    private String nombre;
    private String apellido;
    private String correo;    
    private String telefono;
    private String direccion;
    private String estado;    
}