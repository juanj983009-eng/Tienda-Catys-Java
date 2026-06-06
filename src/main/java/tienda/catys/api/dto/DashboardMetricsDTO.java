package tienda.catys.api.dto;

import java.util.List;

public record DashboardMetricsDTO(
    KpisDTO kpis,
    CocinaMetricasDTO cocina,
    MesasMetricasDTO mesas,
    List<AlertaStockDTO> alertasStock
) {
    public record KpisDTO(double ventasHoy, int ordenesHoy, int clientesVip) {}
    public record CocinaMetricasDTO(int enCola, int enPreparacion) {}
    public record MesasMetricasDTO(int ocupadas, int disponibles) {}
    public record AlertaStockDTO(Integer id, String insumo, String actual, String minimo) {}
}
