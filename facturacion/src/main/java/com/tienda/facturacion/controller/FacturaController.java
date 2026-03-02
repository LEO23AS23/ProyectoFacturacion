package com.tienda.facturacion.controller;

import com.tienda.facturacion.model.Factura;
import com.tienda.facturacion.service.FacturaService;
import com.tienda.facturacion.dto.TaxResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired 
    private FacturaService facturaService;

    @GetMapping
    public List<Factura> listarFacturas() {
        return facturaService.listarTodas();
    }

    @GetMapping("/calcular-previa")
    public ResponseEntity<TaxResponse> calcularPrevia(@RequestParam Double subtotal) {
        TaxResponse respuesta = facturaService.simularImpuesto(subtotal);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    public ResponseEntity<?> crearVenta(@RequestBody Factura factura) {
        try {
            Factura nuevaFactura = facturaService.procesarVentaECommerce(factura);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaFactura);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}