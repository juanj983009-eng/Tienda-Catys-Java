package repository;

import modelo.Producto;
import repository.exception.DataAccessException;
import util.AppLogger;
import util.ConexionSQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operaciones CRUD de Producto en la BD.
 *
 * CAMBIOS CLAVE vs ProductoDAO anterior:
 * - Devuelve List<Producto> en lugar de DefaultTableModel (sin acoplamiento a Swing)
 * - Todos los errores se lanzan como DataAccessException (sin catches vacíos)
 * - Usa try-with-resources en TODOS los métodos para garantizar cierre de recursos
 */
public class ProductoRepository {

    /**
     * Obtiene todos los productos de una categoría específica.
     *
     * @param categoria nombre exacto de la categoría (ej: "CRIOLLO", "BEBIDAS")
     * @return lista de Producto (puede ser vacía, nunca null)
     * @throws DataAccessException si ocurre un error de BD
     */
    public List<Producto> findByCategoria(String categoria) throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos WHERE categoria = ? ORDER BY nombre";
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar productos por categoría '" + categoria + "': " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Obtiene todos los productos de la BD, ordenados por categoría y nombre.
     *
     * @return lista completa de Producto
     * @throws DataAccessException si ocurre un error de BD
     */
    public List<Producto> findAll() throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos ORDER BY categoria, nombre";
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToProducto(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar todos los productos: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Busca productos por nombre o categoría (búsqueda parcial, case-insensitive).
     *
     * @param texto texto a buscar
     * @return lista de Producto que coinciden
     * @throws DataAccessException si ocurre un error de BD
     */
    public List<Producto> findByTexto(String texto) throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos WHERE nombre LIKE ? OR categoria LIKE ? "
                         + "ORDER BY categoria, nombre";
        List<Producto> lista = new ArrayList<>();
        String patron = "%" + texto + "%";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patron);
            stmt.setString(2, patron);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar productos con texto '" + texto + "': " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id el identificador del producto
     * @return Optional<Producto> — vacío si no existe
     * @throws DataAccessException si ocurre un error de BD
     */
    public Optional<Producto> findById(int id) throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos WHERE id_producto = ?";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar producto con ID " + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Inserta un nuevo producto en la BD.
     *
     * @param p producto a insertar (sin ID — se genera en BD)
     * @return true si la inserción fue exitosa
     * @throws DataAccessException si ocurre un error de BD
     */
    public boolean save(Producto p) throws DataAccessException {
        final String sql = "INSERT INTO Productos (nombre, precio, stock, categoria, imagen) "
                         + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setDouble(2, p.getPrecio());
            stmt.setInt(3, p.getStock());
            stmt.setString(4, p.getCategoria());
            stmt.setString(5, p.getImagen());
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) AppLogger.info("Producto registrado: " + p.getNombre());
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar producto '" + p.getNombre() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un producto existente en la BD.
     *
     * @param p producto con los datos actualizados (debe tener ID válido)
     * @return true si la actualización fue exitosa
     * @throws DataAccessException si ocurre un error de BD
     */
    public boolean update(Producto p) throws DataAccessException {
        final String sql = "UPDATE Productos SET nombre=?, precio=?, stock=?, categoria=?, imagen=? "
                         + "WHERE id_producto=?";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setDouble(2, p.getPrecio());
            stmt.setInt(3, p.getStock());
            stmt.setString(4, p.getCategoria());
            stmt.setString(5, p.getImagen());
            stmt.setInt(6, p.getId());
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) AppLogger.info("Producto actualizado: " + p.getNombre() + " (ID: " + p.getId() + ")");
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar producto ID " + p.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un producto de la BD por su ID.
     *
     * @param id identificador del producto a eliminar
     * @return true si fue eliminado
     * @throws DataAccessException si ocurre un error de BD
     */
    public boolean deleteById(int id) throws DataAccessException {
        final String sql = "DELETE FROM Productos WHERE id_producto = ?";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) AppLogger.info("Producto eliminado (ID: " + id + ")");
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al eliminar producto ID " + id + ": " + e.getMessage(), e);
        }
    }

    // --- Método privado de mapeo ---

    private Producto mapRowToProducto(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("id_producto"),
            rs.getString("nombre"),
            rs.getDouble("precio"),
            rs.getString("imagen"),
            rs.getString("categoria"),
            rs.getInt("stock")
        );
    }
}
