package com.tienda.facturacion.dto;

public class TaxResponse {
    private Double subtotal;
    private Double iva;
    private Double total;

    public TaxResponse() {
    }

    public TaxResponse(Double subtotal, Double iva, Double total) {
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getIva() { return iva; }
    public void setIva(Double iva) { this.iva = iva; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}