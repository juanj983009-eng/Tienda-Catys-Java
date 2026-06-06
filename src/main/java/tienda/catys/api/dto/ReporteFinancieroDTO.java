package tienda.catys.api.dto;

import java.util.List;

public record ReporteFinancieroDTO(
    KpisFinancierosDTO kpis,
    List<MetodoPagoDistribucionDTO> metodosPago,
    List<ProgresoMensualDTO> progressionMensual
) {
    public record KpisFinancierosDTO(double dia, double semana, double ano) {}
    public record MetodoPagoDistribucionDTO(String name, double monto, int porcentaje) {}
    public record ProgresoMensualDTO(String mes, double monto, int porcentaje) {}
}
