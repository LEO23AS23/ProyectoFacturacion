package com.tienda.facturacion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detalles_factura")
@Data
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factura_id")
    @JsonIgnore
    private Factura factura;

    // ✅ Solo el ID externo del producto (Grupo 4)
    // No hay JOIN a tabla local de productos
    @Column(name = "producto_id_externo")
    private Long productoId;

    // Snapshot del nombre al momento de la venta
    private String productoNombre;

    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}