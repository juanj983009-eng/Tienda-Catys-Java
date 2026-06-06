package tienda.catys.api.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.Producto;
import tienda.catys.api.repository.exception.DataAccessException;

@Repository
public class ProductoRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductoRepository.class);
    private final DataSource dataSource;

    public ProductoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Producto> findByCategoria(String categoria) throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos WHERE categoria = ? ORDER BY nombre";
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
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

    public List<Producto> findAll() throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos ORDER BY categoria, nombre";
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
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

    public List<Producto> findByTexto(String texto) throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos WHERE nombre LIKE ? OR categoria LIKE ? "
                         + "ORDER BY categoria, nombre";
        List<Producto> lista = new ArrayList<>();
        String patron = "%" + texto + "%";

        try (Connection conn = dataSource.getConnection();
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

    public Optional<Producto> findById(int id) throws DataAccessException {
        final String sql = "SELECT id_producto, nombre, precio, imagen, categoria, stock "
                         + "FROM Productos WHERE id_producto = ?";

        try (Connection conn = dataSource.getConnection();
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

    public boolean save(Producto p) throws DataAccessException {
        final String sql = "INSERT INTO Productos (nombre, precio, stock, categoria, imagen) "
                         + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setDouble(2, p.getPrecio());
            stmt.setInt(3, p.getStock());
            stmt.setString(4, p.getCategoria());
            stmt.setString(5, p.getImagen());
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) log.info("Producto registrado: {}", p.getNombre());
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar producto '" + p.getNombre() + "': " + e.getMessage(), e);
        }
    }

    public boolean update(Producto p) throws DataAccessException {
        final String sql = "UPDATE Productos SET nombre=?, precio=?, stock=?, categoria=?, imagen=? "
                         + "WHERE id_producto=?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setDouble(2, p.getPrecio());
            stmt.setInt(3, p.getStock());
            stmt.setString(4, p.getCategoria());
            stmt.setString(5, p.getImagen());
            stmt.setInt(6, p.getId());
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) log.info("Producto actualizado: {} (ID: {})", p.getNombre(), p.getId());
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar producto ID " + p.getId() + ": " + e.getMessage(), e);
        }
    }

    public boolean deleteById(int id) throws DataAccessException {
        final String sql = "DELETE FROM Productos WHERE id_producto = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) log.info("Producto eliminado (ID: {})", id);
            return exito;

        } catch (SQLException e) {
            throw new DataAccessException("Error al eliminar producto ID " + id + ": " + e.getMessage(), e);
        }
    }

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
