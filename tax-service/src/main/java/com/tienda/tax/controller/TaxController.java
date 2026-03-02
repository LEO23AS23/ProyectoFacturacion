package com.tienda.tax.controller;

import com.tienda.tax.dto.TaxResponse;
import com.tienda.tax.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tax")
public class TaxController {

    @Autowired
    private TaxService taxService;

    @GetMapping("/calcular")
    public TaxResponse obtenerIva(@RequestParam Double subtotal) {
        return taxService.calcularImpuestos(subtotal);
    }
}