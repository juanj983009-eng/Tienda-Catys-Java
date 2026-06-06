package tienda.catys.api.dto;

public record ItemVentaDTO(
    int idProducto,
    String nombre,
    int cantidad,
    double precioUnitario
) {
    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    public String toLineaTicket() {
        String nombreCorto = nombre.length() > 20 ? nombre.substring(0, 18) + ".." : nombre;
        return String.format("- %-20s x%-2d   S/ %6.2f", nombreCorto, cantidad, getSubtotal());
    }
}
