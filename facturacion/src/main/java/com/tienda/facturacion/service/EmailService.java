package com.tienda.facturacion.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.tienda.facturacion.model.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void enviarFacturaPdf(Factura factura) {
        if (factura == null || factura.getCliente() == null || factura.getCliente().getEmail() == null) {
            throw new RuntimeException("No se puede enviar el correo: Datos del cliente o email faltantes.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            String nombreCli = factura.getCliente().getNombre() != null ? factura.getCliente().getNombre() : "Cliente General";
            String idFactura = factura.getId() != null ? String.valueOf(factura.getId()) : "S/N";

            document.add(new Paragraph("TIENDA DE COMPONENTES PC - FACTURA OFICIAL").setBold().setFontSize(20));
            document.add(new Paragraph("Factura N°: " + idFactura));
            document.add(new Paragraph("Cliente: " + nombreCli));
            document.add(new Paragraph("Email: " + factura.getCliente().getEmail()));
            document.add(new Paragraph("\nDetalle de compra:"));

            float[] columnWidths = {100, 300, 100, 100};
            Table table = new Table(columnWidths);
            table.addCell("Cant.");
            table.addCell("Producto");
            table.addCell("Precio U.");
            table.addCell("Subtotal");

            if (factura.getDetalles() != null) {
                for (DetalleFactura detalle : factura.getDetalles()) {
                    String nombreProd = (detalle.getProducto() != null && detalle.getProducto().getNombre() != null) 
                                        ? detalle.getProducto().getNombre() 
                                        : "Producto sin nombre";

                    table.addCell(String.valueOf(detalle.getCantidad() != null ? detalle.getCantidad() : 0));
                    table.addCell(nombreProd);
                    table.addCell("$" + (detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : 0.0));
                    table.addCell("$" + (detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0));
                }
            }
            
            document.add(table);
            document.add(new Paragraph("\nTOTAL A PAGAR: $" + (factura.getTotal() != null ? factura.getTotal() : 0.0)).setBold());
            document.close();

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String emailDestino = factura.getCliente().getEmail();
            helper.setTo(emailDestino);
            helper.setSubject("Tu Factura de Compra #" + idFactura);
            helper.setText("Hola " + nombreCli + ", adjuntamos tu factura legal.");
            helper.addAttachment("Factura_PC.pdf", new ByteArrayResource(outputStream.toByteArray()));

            emailSender.send(message);

        } catch (Exception e) {
            System.err.println("======= ERROR REAL ENCONTRADO =======");
            e.printStackTrace(); 
            throw new RuntimeException("Error en PDF o Email: " + e.getMessage());
        }
    }
}