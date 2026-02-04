package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import util.ConexionSQL;

/**
 * Clase DAO para la gestión de Clientes en SQL Server.
 * Aplica el patrón Data Access Object para separar la persistencia de la interfaz.
 */
public class ClienteDAO {

    /**
     * Obtiene todos los clientes de la base de datos.
     * @return Una lista de objetos Cliente.
     */
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, dni, nombre, telefono FROM Clientes ORDER BY id_cliente DESC";

        // Uso de try-with-resources para asegurar el cierre de la conexión Singleton
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id_cliente"));
                c.setDni(rs.getString("dni"));
                c.setNombre(rs.getString("nombre"));
                c.setTelefono(rs.getString("telefono"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un nuevo cliente en la base de datos.
     * @param c Objeto cliente con los datos a insertar.
     * @return true si la inserción fue exitosa.
     */
    public boolean registrarCliente(Cliente c) {
        String sql = "INSERT INTO Clientes (dni, nombre, telefono) VALUES (?, ?, ?)";
        
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, c.getDni());
            pstmt.setString(2, c.getNombre());
            pstmt.setString(3, c.getTelefono());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un cliente permanentemente por su ID.
     * @param id Identificador único del cliente.
     * @return true si se eliminó correctamente.
     */
    public boolean eliminarCliente(int id) {
        String sql = "DELETE FROM Clientes WHERE id_cliente = ?";
        
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un cliente específico por su número de DNI.
     * @param dni El DNI a buscar.
     * @return Un objeto Cliente si existe, de lo contrario null.
     */
    public Cliente buscarPorDni(String dni) {
        String sql = "SELECT * FROM Clientes WHERE dni = ?";
        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id_cliente"));
                    c.setDni(rs.getString("dni"));
                    c.setNombre(rs.getString("nombre"));
                    c.setTelefono(rs.getString("telefono"));
                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar cliente por DNI: " + e.getMessage());
        }
        return null;
    }
}