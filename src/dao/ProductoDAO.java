package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import modelo.Producto;
import util.ConexionSQL;

public class ProductoDAO {

    // 1. LISTAR POR CATEGORÍA (Para la Tienda)
    public ArrayList<Producto> listarPorCategoria(String categoria) {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE categoria = ?";
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    rs.getDouble("precio"), rs.getString("imagen"),
                    rs.getString("categoria"), rs.getInt("stock")
                );
                lista.add(p);
            }
        } catch (Exception e) {}
        return lista;
    }

    // 2. LISTAR TODOS (Para la Gestión)
    public DefaultTableModel listarTodos() {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID"); modelo.addColumn("Nombre"); modelo.addColumn("Precio");
        modelo.addColumn("Stock"); modelo.addColumn("Categoría"); modelo.addColumn("Imagen");
        
        String sql = "SELECT * FROM Productos ORDER BY categoria, nombre";
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                Object[] fila = {
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    rs.getDouble("precio"), rs.getInt("stock"),
                    rs.getString("categoria"), rs.getString("imagen")
                };
                modelo.addRow(fila);
            }
        } catch(Exception e) {}
        return modelo;
    }

    // 3. REGISTRAR NUEVO
    public boolean registrar(Producto p) {
        String sql = "INSERT INTO Productos (nombre, precio, stock, categoria, imagen) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNombre());
            pstmt.setDouble(2, p.getPrecio());
            pstmt.setInt(3, p.getStock());
            pstmt.setString(4, p.getCategoria());
            pstmt.setString(5, p.getImagen());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // 4. ACTUALIZAR
    public boolean actualizar(Producto p) {
        String sql = "UPDATE Productos SET nombre=?, precio=?, stock=?, categoria=?, imagen=? WHERE id_producto=?";
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNombre());
            pstmt.setDouble(2, p.getPrecio());
            pstmt.setInt(3, p.getStock());
            pstmt.setString(4, p.getCategoria());
            pstmt.setString(5, p.getImagen());
            pstmt.setInt(6, p.getId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // 5. ELIMINAR
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Productos WHERE id_producto = ?";
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
    // MÉTODO NUEVO: BUSCAR PRODUCTOS (FILTRO)
    public javax.swing.table.DefaultTableModel filtrar(String texto) {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel();
        modelo.addColumn("ID"); modelo.addColumn("Nombre"); modelo.addColumn("Precio");
        modelo.addColumn("Stock"); modelo.addColumn("Categoría"); modelo.addColumn("Imagen");
        
        // El símbolo % sirve para buscar coincidencias parciales
        String sql = "SELECT * FROM Productos WHERE nombre LIKE ? OR categoria LIKE ? ORDER BY categoria, nombre";
        
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + texto + "%");
            pstmt.setString(2, "%" + texto + "%");
            
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Object[] fila = {
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    rs.getDouble("precio"), rs.getInt("stock"),
                    rs.getString("categoria"), rs.getString("imagen")
                };
                modelo.addRow(fila);
            }
        } catch(Exception e) {}
        return modelo;
    }
}