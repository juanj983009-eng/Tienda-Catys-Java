package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import modelo.Producto;
import java.util.Map;
import java.util.HashMap;

public class GeneradorPDF {

    public static void crearTicket(String cliente, String metodoPago, ArrayList<Producto> listaProductos, double total) {
        try {
            // 1. Nombre del archivo único (ej: Ticket_20231127_183055.pdf)
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "Ticket_" + timeStamp + ".pdf";

            // 2. Configurar tamaño de página (Tipo Ticket Térmico - 80mm ancho)
            Rectangle pageSize = new Rectangle(226, 400); // Ancho x Alto (el alto puede ser variable en versiones avanzadas)
            Document documento = new Document(pageSize, 10, 10, 10, 10); // Márgenes pequeños

            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            // --- ESTILOS DE FUENTE ---
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font fontPequena = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

            // --- CABECERA ---
            Paragraph titulo = new Paragraph("RESTAURANTE CATYS", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph subtitulo = new Paragraph("RUC: 10123456789\nAv. Siempre Viva 123", fontPequena);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(subtitulo);

            documento.add(new Paragraph("----------------------------------"));

            // --- DATOS DEL CLIENTE ---
            documento.add(new Paragraph("Cliente: " + cliente, fontNormal));
            documento.add(new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), fontNormal));
            documento.add(new Paragraph("Pago: " + metodoPago, fontNormal));

            documento.add(new Paragraph("----------------------------------"));

            // --- TABLA DE PRODUCTOS ---
            PdfPTable tabla = new PdfPTable(3); // 3 Columnas: Descripcion, Cant, Total
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{5, 1, 2}); // Anchos relativos

            // Agrupamos productos (Lógica x10)
            Map<String, Integer> conteo = new HashMap<>();
            Map<String, Double> precios = new HashMap<>();
            for (Producto p : listaProductos) {
                conteo.put(p.getNombre(), conteo.getOrDefault(p.getNombre(), 0) + 1);
                precios.put(p.getNombre(), p.getPrecio());
            }

            for (String nombre : conteo.keySet()) {
                int cant = conteo.get(nombre);
                double precioU = precios.get(nombre);
                double subtotal = cant * precioU;

                // Nombre corto para que quepa
                String nombreCorto = nombre.length() > 15 ? nombre.substring(0, 15) : nombre;

                PdfPCell c1 = new PdfPCell(new Phrase(nombreCorto, fontNormal));
                c1.setBorder(Rectangle.NO_BORDER);
                tabla.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Phrase("x" + cant, fontNormal));
                c2.setBorder(Rectangle.NO_BORDER);
                tabla.addCell(c2);

                PdfPCell c3 = new PdfPCell(new Phrase("S/" + String.format("%.2f", subtotal), fontNormal));
                c3.setBorder(Rectangle.NO_BORDER);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(c3);
            }

            documento.add(tabla);

            documento.add(new Paragraph("----------------------------------"));

            // --- TOTAL ---
            Paragraph pTotal = new Paragraph("TOTAL: S/ " + String.format("%.2f", total), fontTitulo);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            documento.add(pTotal);

            // --- PIE DE PÁGINA ---
            Paragraph pie = new Paragraph("\n¡Gracias por su preferencia!\nGuarde su voucher.", fontPequena);
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.add(pie);

            documento.close();
            
            // Abrir el archivo automáticamente (Opcional, funciona en Windows)
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + nombreArchivo);
            } catch (Exception e) {}

            System.out.println("PDF Generado: " + nombreArchivo);

        } catch (Exception e) {
            System.out.println("Error al crear PDF: " + e.getMessage());
        }
    }
}