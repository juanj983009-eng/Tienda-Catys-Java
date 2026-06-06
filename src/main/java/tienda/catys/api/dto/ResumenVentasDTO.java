package tienda.catys.api.dto;

import java.util.Map;

public record ResumenVentasDTO(
    double totalIngresos,
    int totalTransacciones,
    Map<String, Double> ventasPorMetodo
) {
    public String getTotalIngresosFormateado() {
        return String.format("S/ %.2f", totalIngresos);
    }
}
