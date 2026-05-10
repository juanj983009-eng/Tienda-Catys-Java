package repository;

import dto.ItemVentaDTO;
import dto.VentaDTO;
import dto.ResumenVentasDTO;
import modelo.MetodoPago;
import repository.exception.DataAccessException;
import util.AppLogger;
import util.ConexionSQL;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository para operaciones de Venta en la BD.
 *
 * REGLAS DE ORO de esta clase:
 * 1. NUNCA importa ni devuelve nada de javax.swing.*
 * 2. NUNCA instancia objetos de la capa UI
 * 3. NUNCA contiene lógica de negocio — solo SQL y mapeo
 * 4. SIEMPRE usa try-with-resources para garantizar cierre de conexiones
 * 5. SIEMPRE lanza DataAccessException en caso de error (nunca lo silencia)
 */
public class VentaRepository {

    /**
     * Guarda una venta y sus ítems de forma ATÓMICA en una sola transacción.
     * Si cualquier paso falla, se hace rollback de todo.
     *
     * @param venta   datos de la cabecera de la venta
     * @param items   lista de productos comprados con cantidades
     * @throws DataAccessException si ocurre cualquier error de BD
     */
    public void save(VentaDTO venta, List<ItemVentaDTO> items) throws DataAccessException {
        final String sqlVenta = "INSERT INTO Ventas (cliente_nombre, metodo_pago, detalle_compra, total, fecha) "
                              + "VALUES (?, ?, ?, ?, GETDATE())";
        final String sqlStock = "UPDATE Productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

        // Construir el detalle como texto (compatible con la BD actual sin migración)
        String detalle = construirDetalleTexto(items);

        try (Connection conn = ConexionSQL.getConnection()) {
            // Transacción: Todo o nada
            conn.setAutoCommit(false);
            try {
                // 1. Insertar cabecera de venta
                try (PreparedStatement stmtVenta = conn.prepareStatement(sqlVenta)) {
                    stmtVenta.setString(1, venta.clienteNombre());
                    stmtVenta.setString(2, venta.metodoPago().name());
                    stmtVenta.setString(3, detalle);
                    stmtVenta.setDouble(4, venta.total());
                    stmtVenta.executeUpdate();
                }

                // 2. Descontar stock de cada ítem (agrupados por producto)
                try (PreparedStatement stmtStock = conn.prepareStatement(sqlStock)) {
                    for (ItemVentaDTO item : items) {
                        stmtStock.setInt(1, item.cantidad());
                        stmtStock.setInt(2, item.idProducto());
                        stmtStock.setInt(3, item.cantidad()); // Condición: stock suficiente
                        int filasActualizadas = stmtStock.executeUpdate();
                        if (filasActualizadas == 0) {
                            // Rollback automático en el catch
                            throw new DataAccessException(
                                "Stock insuficiente para el producto ID " + item.idProducto()
                                + " (" + item.nombre() + "). Transacción cancelada."
                            );
                        }
                    }
                }

                conn.commit();
                AppLogger.info("Venta registrada exitosamente para cliente: " + venta.clienteNombre()
                             + " | Total: S/ " + String.format("%.2f", venta.total()));

            } catch (DataAccessException e) {
                conn.rollback();
                throw e; // Re-lanzar para que el Service lo maneje
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Error SQL al registrar la venta: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al obtener conexión con la BD: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el historial completo de ventas ordenado por fecha descendente.
     * Devuelve una List<VentaDTO> — nunca un DefaultTableModel.
     *
     * @return lista de VentaDTO (puede estar vacía, nunca null)
     * @throws DataAccessException si ocurre un error de BD
     */
    public List<VentaDTO> findAll() throws DataAccessException {
        final String sql = "SELECT id_venta, fecha, cliente_nombre, metodo_pago, total, detalle_compra "
                         + "FROM Ventas ORDER BY fecha DESC";
        List<VentaDTO> ventas = new ArrayList<>();

        try (Connection conn = ConexionSQL.getConnection();
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

    /**
     * Calcula el resumen ejecutivo de ventas directamente en SQL (eficiente).
     * La agregación se hace en la BD, no en Java parseando Strings.
     *
     * @return ResumenVentasDTO con totales pre-calculados
     * @throws DataAccessException si ocurre un error de BD
     */
    public ResumenVentasDTO getResumen() throws DataAccessException {
        final String sqlResumen = "SELECT SUM(total) AS total_ingresos, COUNT(*) AS total_transacciones FROM Ventas";
        final String sqlPorMetodo = "SELECT metodo_pago, SUM(total) AS total_vendido FROM Ventas GROUP BY metodo_pago";

        double totalIngresos = 0.0;
        int totalTransacciones = 0;
        Map<String, Double> ventasPorMetodo = new HashMap<>();

        try (Connection conn = ConexionSQL.getConnection()) {
            // Query 1: Totales globales
            try (PreparedStatement stmt = conn.prepareStatement(sqlResumen);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalIngresos = rs.getDouble("total_ingresos");
                    totalTransacciones = rs.getInt("total_transacciones");
                }
            }
            // Query 2: Desglose por método de pago
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

    // --- Métodos privados de mapeo ---

    private VentaDTO mapRowToVentaDTO(ResultSet rs) throws SQLException {
        MetodoPago metodo;
        try {
            metodo = MetodoPago.valueOf(rs.getString("metodo_pago"));
        } catch (IllegalArgumentException e) {
            // Datos legacy en BD con el displayName como String — fallback seguro
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

    private String construirDetalleTexto(List<ItemVentaDTO> items) {
        StringBuilder sb = new StringBuilder();
        for (ItemVentaDTO item : items) {
            sb.append(item.toLineaTicket()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Compatibilidad con datos legacy: la BD puede tener metodo_pago como
     * el enum.name() (ej: "EFECTIVO") o el displayName (ej: "💵 Efectivo").
     */
    private String resolveMetodoPagoDisplayName(String rawValue) {
        try {
            return MetodoPago.valueOf(rawValue).getDisplayName();
        } catch (IllegalArgumentException e) {
            return rawValue; // Ya es un displayName del encoding viejo
        }
    }
}
