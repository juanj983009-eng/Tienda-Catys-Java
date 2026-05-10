package dto;

/**
 * DTO que representa un ítem individual dentro de una venta.
 * Reemplaza el String plano que antes se guardaba en la BD como "detalle_compra".
 *
 * Usando 'record' de Java 16+. Si tu JDK es anterior, ver comentario al pie.
 * Con JDK 8-15: reemplazar 'record' por clase convencional con constructor, getters y equals/hashCode.
 */
public record ItemVentaDTO(
    int idProducto,
    String nombre,
    int cantidad,
    double precioUnitario
) {
    /**
     * Calcula el subtotal del ítem (cantidad * precio unitario).
     */
    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    /**
     * Genera la línea de texto para el ticket/recibo.
     * Ejemplo: "- Lomo Saltado          x2    S/  30.00"
     */
    public String toLineaTicket() {
        String nombreCorto = nombre.length() > 20 ? nombre.substring(0, 18) + ".." : nombre;
        return String.format("- %-20s x%-2d   S/ %6.2f", nombreCorto, cantidad, getSubtotal());
    }
}

/*
 * COMPATIBILIDAD JDK 8-15 — Si 'record' no compila, usa esta versión:
 *
 * public final class ItemVentaDTO {
 *     private final int idProducto;
 *     private final String nombre;
 *     private final int cantidad;
 *     private final double precioUnitario;
 *
 *     public ItemVentaDTO(int idProducto, String nombre, int cantidad, double precioUnitario) {
 *         this.idProducto = idProducto;
 *         this.nombre = nombre;
 *         this.cantidad = cantidad;
 *         this.precioUnitario = precioUnitario;
 *     }
 *
 *     public int idProducto() { return idProducto; }
 *     public String nombre() { return nombre; }
 *     public int cantidad() { return cantidad; }
 *     public double precioUnitario() { return precioUnitario; }
 *     public double getSubtotal() { return cantidad * precioUnitario; }
 *     public String toLineaTicket() { ... }
 * }
 */
