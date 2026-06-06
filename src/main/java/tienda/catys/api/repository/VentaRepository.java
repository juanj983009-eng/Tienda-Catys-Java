package tienda.catys.api.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import tienda.catys.api.dto.ItemVentaDTO;
import tienda.catys.api.dto.ResumenVentasDTO;
import tienda.catys.api.dto.VentaDTO;
import tienda.catys.api.modelo.MetodoPago;
import tienda.catys.api.repository.exception.DataAccessException;

@Repository
public class VentaRepository {

    private static final Logger log = LoggerFactory.getLogger(VentaRepository.class);
    private final DataSource dataSource;

    public VentaRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(VentaDTO venta, List<ItemVentaDTO> items) throws DataAccessException {
        final String sqlVenta = "INSERT INTO ventas (cliente_nombre, metodo_pago, total, fecha) VALUES (?, ?, ?, GETDATE())";
        final String sqlDetalle = "INSERT INTO venta_detalles (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        final String sqlStock = "UPDATE Productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idVenta = -1;
                try (PreparedStatement stmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    stmtVenta.setString(1, venta.clienteNombre());
                    stmtVenta.setString(2, venta.metodoPago().name());
                    stmtVenta.setDouble(3, venta.total());
                    stmtVenta.executeUpdate();

                    try (ResultSet generatedKeys = stmtVenta.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idVenta = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("No se pudo obtener el ID generado para la venta.");
                        }
                    }
                }

                try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                    for (ItemVentaDTO item : items) {
                        stmtDetalle.setInt(1, idVenta);
                        stmtDetalle.setInt(2, item.idProducto());
                        stmtDetalle.setInt(3, item.cantidad());
                        stmtDetalle.setDouble(4, item.precioUnitario());
                        stmtDetalle.addBatch();
                    }
                    stmtDetalle.executeBatch();
                }

                try (PreparedStatement stmtStock = conn.prepareStatement(sqlStock)) {
                    for (ItemVentaDTO item : items) {
                        stmtStock.setInt(1, item.cantidad());
                        stmtStock.setInt(2, item.idProducto());
                        stmtStock.setInt(3, item.cantidad());
                        int filasActualizadas = stmtStock.executeUpdate();
                        if (filasActualizadas == 0) {
                            throw new DataAccessException(
                                "Stock insuficiente para el producto ID " + item.idProducto()
                                + " (" + item.nombre() + "). Transacción cancelada."
                            );
                        }
                    }
                }

                conn.commit();
                log.info("Venta registrada exitosamente para cliente: {} | ID Venta: {} | Total: S/ {}",
                        venta.clienteNombre(), idVenta, String.format("%.2f", venta.total()));

            } catch (DataAccessException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Error SQL al registrar la venta: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error de conexión con la BD: " + e.getMessage(), e);
        }
    }

    public List<VentaDTO> findAll() throws DataAccessException {
        final String sql = "SELECT id_venta, fecha, cliente_nombre, metodo_pago, total, detalle_compra "
                         + "FROM ventas ORDER BY fecha DESC";
        List<VentaDTO> ventas = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ventas.add(mapRowToVentaDTO(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al obtener historial de ventas: " + e.getMessage(), e);
        }
        return ventas;
    }

    public ResumenVentasDTO getResumen() throws DataAccessException {
        final String sqlResumen = "SELECT SUM(total) AS total_ingresos, COUNT(*) AS total_transacciones FROM ventas";
        final String sqlPorMetodo = "SELECT metodo_pago, SUM(total) AS total_vendido FROM ventas GROUP BY metodo_pago";

        double totalIngresos = 0.0;
        int totalTransacciones = 0;
        Map<String, Double> ventasPorMetodo = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlResumen);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalIngresos = rs.getDouble("total_ingresos");
                    totalTransacciones = rs.getInt("total_transacciones");
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlPorMetodo);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String metodo = resolveMetodoPagoDisplayName(rs.getString("metodo_pago"));
                    ventasPorMetodo.put(metodo, rs.getDouble("total_vendido"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al calcular resumen de ventas: " + e.getMessage(), e);
        }

        return new ResumenVentasDTO(totalIngresos, totalTransacciones, ventasPorMetodo);
    }

    private VentaDTO mapRowToVentaDTO(ResultSet rs) throws SQLException {
        MetodoPago metodo;
        try {
            metodo = MetodoPago.valueOf(rs.getString("metodo_pago"));
        } catch (IllegalArgumentException e) {
            metodo = MetodoPago.fromDisplayName(rs.getString("metodo_pago"));
        }

        LocalDateTime fecha = rs.getTimestamp("fecha") != null
            ? rs.getTimestamp("fecha").toLocalDateTime()
            : LocalDateTime.now();

        return new VentaDTO(
            rs.getInt("id_venta"),
            fecha,
            rs.getString("cliente_nombre"),
            metodo,
            rs.getDouble("total"),
            rs.getString("detalle_compra")
        );
    }

    private String resolveMetodoPagoDisplayName(String rawValue) {
        try {
            return MetodoPago.valueOf(rawValue).getDisplayName();
        } catch (IllegalArgumentException e) {
            return rawValue;
        }
    }
}
