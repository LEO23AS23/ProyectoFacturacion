package com.tienda.tax.service;

import com.tienda.tax.dto.TaxResponse;
import org.springframework.stereotype.Service;

@Service
public class TaxService {
    
    private final Double PORCENTAJE_IVA = 0.15; 

    public TaxResponse calcularImpuestos(Double subtotal) {
        double valorIva = subtotal * PORCENTAJE_IVA;
        double totalConIva = subtotal + valorIva;
        return new TaxResponse(subtotal, valorIva, totalConIva);
    }
}