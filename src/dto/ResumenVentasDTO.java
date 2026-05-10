package dto;

import java.util.Map;

/**
 * DTO que agrupa el resumen ejecutivo de ventas para el panel de Reportes.
 * Eliminó la necesidad de calcularTotalVentas() parseando Strings de la tabla Swing.
 */
public record ResumenVentasDTO(
    double totalIngresos,
    int totalTransacciones,
    Map<String, Double> ventasPorMetodo
) {
    /**
     * Formatea el total de ingresos como texto para mostrar en la UI.
     */
    public String getTotalIngresosFormateado() {
        return String.format("S/ %.2f", totalIngresos);
    }
}
