package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import util.ConexionSQL;

public class ClienteDAO {

    // --- 1. MÉTODO PARA GUARDAR UN NUEVO CLIENTE (INSERT) ---
    public boolean registrarCliente(Cliente c) {
        // La consulta SQL
        String sql = "INSERT INTO Clientes (dni, nombre, telefono) VALUES (?, ?, ?)";
        
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Asignamos los valores del objeto Cliente a la consulta SQL
            pstmt.setString(1, c.getDni());
            pstmt.setString(2, c.getNombre());
            pstmt.setString(3, c.getTelefono());
            
            pstmt.executeUpdate(); // Ejecuta la inserción
            return true; // Si llegó aquí, todo salió bien
            
        } catch (Exception e) {
            System.out.println("Error al registrar cliente: " + e.getMessage());
            return false; // Algo falló (ej: DNI repetido)
        }
    }

    // --- 2. MÉTODO PARA LEER TODOS LOS CLIENTES (SELECT) ---
    // Retorna un 'DefaultTableModel' listo para ponerlo en un JTable
    public DefaultTableModel listarClientes() {
        // Configuramos las columnas que se verán en la ventana
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("DNI");
        modelo.addColumn("Nombre Completo");
        modelo.addColumn("Teléfono");

        // Ordenamos por ID descendente para ver los nuevos primero
        String sql = "SELECT * FROM Clientes ORDER BY id_cliente DESC";

        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Recorremos fila por fila lo que nos trajo SQL Server
            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getInt("id_cliente");
                fila[1] = rs.getString("dni");
                fila[2] = rs.getString("nombre");
                fila[3] = rs.getString("telefono");
                
                modelo.addRow(fila); // Agregamos la fila a la tabla visual
            }
        } catch (Exception e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
        }
        return modelo;
    }
}