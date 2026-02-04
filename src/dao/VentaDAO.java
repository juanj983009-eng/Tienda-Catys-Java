package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import util.ConexionSQL;
import modelo.Carrito;
import modelo.Producto;

public class VentaDAO {

    // MÉTODO ACTUALIZADO: AHORA RECIBE EL CARRITO PARA DESCONTAR STOCK
    public boolean registrarVenta(String cliente, String metodoPago, String detalle, double total, Carrito carrito) {
        String sqlVenta = "INSERT INTO Ventas (cliente_nombre, metodo_pago, detalle_compra, total, fecha) VALUES (?, ?, ?, ?, GETDATE())";
        
        Connection conn = null;

        try {
            conn = ConexionSQL.getConexion();
            if (conn == null) return false;

            // Desactivamos el auto-guardado para hacer una TRANSACCIÓN (Todo o nada)
            conn.setAutoCommit(false); 

            // 1. Guardar la Venta
            PreparedStatement pstmt = conn.prepareStatement(sqlVenta);
            pstmt.setString(1, cliente);
            pstmt.setString(2, metodoPago);
            pstmt.setString(3, detalle);
            pstmt.setDouble(4, total);
            pstmt.executeUpdate();
            pstmt.close();

            // 2. Descontar Stock de cada producto
            String sqlStock = "UPDATE Productos SET stock = stock - 1 WHERE id_producto = ?";
            PreparedStatement pstmtStock = conn.prepareStatement(sqlStock);

            for (Producto p : carrito.getLista()) {
                pstmtStock.setInt(1, p.getId());
                pstmtStock.executeUpdate();
            }
            pstmtStock.close();

            // Si todo salió bien, confirmamos los cambios
            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("❌ Error en transacción: " + e.getMessage());
            try {
                if (conn != null) conn.rollback(); // Si falla, deshacemos todo
            } catch (Exception ex) {}
            return false;
        } finally {
            try { if(conn != null) conn.setAutoCommit(true); if(conn != null) conn.close(); } catch(Exception e){}
        }
    }
    
    // Método para reporte (se mantiene igual)
    public javax.swing.table.DefaultTableModel obtenerHistorialVentas() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel();
        modelo.addColumn("ID"); modelo.addColumn("Fecha"); modelo.addColumn("Cliente");
        modelo.addColumn("Pago"); modelo.addColumn("Total"); modelo.addColumn("Detalle");

        String sql = "SELECT * FROM Ventas ORDER BY fecha DESC";
        try (Connection conn = ConexionSQL.getConexion();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Object[] fila = new Object[6];
                fila[0] = rs.getInt("id_venta");
                fila[1] = rs.getString("fecha");
                fila[2] = rs.getString("cliente_nombre");
                fila[3] = rs.getString("metodo_pago");
                fila[4] = "S/ " + rs.getDouble("total");
                fila[5] = rs.getString("detalle_compra");
                modelo.addRow(fila);
            }
        } catch (Exception e) {}
        return modelo;
    }
// MÉTODO PARA EL GRÁFICO (Suma total por método de pago)
    public java.util.Map<String, Double> obtenerVentasPorMetodo() {
        java.util.Map<String, Double> datos = new java.util.HashMap<>();
        String sql = "SELECT metodo_pago, SUM(total) as total_vendido FROM Ventas GROUP BY metodo_pago";
        
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next()) {
                datos.put(rs.getString("metodo_pago"), rs.getDouble("total_vendido"));
            }
        } catch(Exception e) {
            System.out.println("Error grafico: " + e.getMessage());
        }
        return datos;
    }
}