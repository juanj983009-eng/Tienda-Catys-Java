package repository;

import modelo.Cliente;
import repository.exception.DataAccessException;
import util.AppLogger;
import util.ConexionSQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operaciones CRUD de Cliente en la BD.
 */
public class ClienteRepository {

    /**
     * Obtiene todos los clientes ordenados por ID descendente (más recientes primero).
     *
     * @return lista de Cliente (puede ser vacía, nunca null)
     * @throws DataAccessException si ocurre un error de BD
     */
    public List<Cliente> findAll() throws DataAccessException {
        final String sql = "SELECT id_cliente, dni, nombre, telefono FROM Clientes ORDER BY id_cliente DESC";
        List<Cliente> lista = new ArrayList<>();

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToCliente(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Busca un cliente por su DNI.
     *
     * @param dni el DNI a buscar
     * @return Optional<Cliente> — vacío si no existe
     * @throws DataAccessException si ocurre un error de BD
     */
    public Optional<Cliente> findByDni(String dni) throws DataAccessException {
        final String sql = "SELECT id_cliente, dni, nombre, telefono FROM Clientes WHERE dni = ?";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCliente(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar cliente por DNI '" + dni + "': " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Registra un nuevo cliente en la BD.
     *
     * @param c cliente a registrar
     * @return true si fue guardado exitosamente
     * @throws DataAccessException si ocurre un error de BD (ej: DNI duplicado)
     */
    public boolean save(Cliente c) throws DataAccessException {
        final String sql = "INSERT INTO Clientes (dni, nombre, telefono) VALUES (?, ?, ?)";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getDni());
            stmt.setString(2, c.getNombre());
            stmt.setString(3, c.getTelefono());
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) AppLogger.info("Cliente registrado: " + c.getNombre() + " (DNI: " + c.getDni() + ")");
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar cliente '" + c.getNombre() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un cliente permanentemente por su ID.
     *
     * @param id identificador del cliente
     * @return true si fue eliminado
     * @throws DataAccessException si ocurre un error de BD
     */
    public boolean deleteById(int id) throws DataAccessException {
        final String sql = "DELETE FROM Clientes WHERE id_cliente = ?";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) AppLogger.info("Cliente eliminado (ID: " + id + ")");
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al eliminar cliente ID " + id + ": " + e.getMessage(), e);
        }
    }

    private Cliente mapRowToCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id_cliente"));
        c.setDni(rs.getString("dni"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        return c;
    }
}
