package tienda.catys.api.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import tienda.catys.api.dto.DashboardMetricsDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.AlertaStockDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.CocinaMetricasDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.KpisDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.MesasMetricasDTO;
import tienda.catys.api.repository.exception.DataAccessException;

@Repository
public class DashboardDAOImpl implements DashboardDAO {

    private static final Logger log = LoggerFactory.getLogger(DashboardDAOImpl.class);
    private final DataSource dataSource;

    public DashboardDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DashboardMetricsDTO getMetrics() throws DataAccessException {
        String sqlKpis = "SELECT "
                       + "  (SELECT COALESCE(SUM(total), 0) FROM Ventas WHERE CAST(fecha AS DATE) = CAST(GETDATE() AS DATE)) AS ventas_hoy, "
                       + "  (SELECT COUNT(*) FROM Ventas WHERE CAST(fecha AS DATE) = CAST(GETDATE() AS DATE)) AS ordenes_hoy, "
                       + "  (SELECT COUNT(*) FROM Clientes) AS clientes_vip";

        String sqlCocina = "SELECT "
                         + "  (SELECT COUNT(*) FROM Comandas WHERE estado = 'en_cola') AS en_cola, "
                         + "  (SELECT COUNT(*) FROM Comandas WHERE estado = 'preparacion') AS en_prep";

        String sqlMesas = "SELECT "
                        + "  (SELECT COUNT(*) FROM Mesas WHERE estado = 'ocupada') AS ocupadas, "
                        + "  (SELECT COUNT(*) FROM Mesas WHERE estado = 'disponible') AS disponibles";

        String sqlAlertas = "SELECT id_insumo, nombre, stock_actual, stock_minimo, unidad FROM Insumos WHERE stock_actual < stock_minimo";

        try (Connection conn = dataSource.getConnection()) {
            
            // 1. Obtener KPIs
            KpisDTO kpis;
            try (PreparedStatement stmt = conn.prepareStatement(sqlKpis);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    kpis = new KpisDTO(
                        rs.getDouble("ventas_hoy"),
                        rs.getInt("ordenes_hoy"),
                        rs.getInt("clientes_vip")
                    );
                } else {
                    kpis = new KpisDTO(0.0, 0, 0);
                }
            }

            // 2. Obtener Cocina
            CocinaMetricasDTO cocina;
            try (PreparedStatement stmt = conn.prepareStatement(sqlCocina);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cocina = new CocinaMetricasDTO(
                        rs.getInt("en_cola"),
                        rs.getInt("en_prep")
                    );
                } else {
                    cocina = new CocinaMetricasDTO(0, 0);
                }
            }

            // 3. Obtener Mesas
            MesasMetricasDTO mesas;
            try (PreparedStatement stmt = conn.prepareStatement(sqlMesas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mesas = new MesasMetricasDTO(
                        rs.getInt("ocupadas"),
                        rs.getInt("disponibles")
                    );
                } else {
                    mesas = new MesasMetricasDTO(0, 0);
                }
            }

            // 4. Obtener Alertas de stock crítico
            List<AlertaStockDTO> alertasStock = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlAlertas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idInsumo = rs.getInt("id_insumo");
                    String nombreInsumo = rs.getString("nombre");
                    double stockActual = rs.getDouble("stock_actual");
                    double stockMinimo = rs.getDouble("stock_minimo");
                    String unidad = rs.getString("unidad");

                    String actualStr = String.format("%.0f %s", stockActual, unidad);
                    String minimoStr = String.format("%.0f %s", stockMinimo, unidad);

                    alertasStock.add(new AlertaStockDTO(idInsumo, nombreInsumo, actualStr, minimoStr));
                }
            }

            return new DashboardMetricsDTO(kpis, cocina, mesas, alertasStock);

        } catch (SQLException e) {
            log.error("Error al ejecutar consulta de métricas del Dashboard", e);
            throw new DataAccessException("Error SQL al obtener métricas del Dashboard: " + e.getMessage(), e);
        }
    }
}
