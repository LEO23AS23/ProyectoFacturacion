package com.tienda.tax.dto;

public class TaxResponse {
    private Double subtotal;
    private Double iva;
    private Double total;

    public TaxResponse(Double subtotal, Double iva, Double total) {
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public Double getSubtotal() { return subtotal; }
    public Double getIva() { return iva; }
    public Double getTotal() { return total; }
}