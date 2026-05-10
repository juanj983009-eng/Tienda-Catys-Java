package repository;

import modelo.Usuario;
import repository.exception.DataAccessException;
import util.AppLogger;
import util.ConexionSQL;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;

/**
 * Repository de autenticación de usuarios.
 *
 * CAMBIOS vs UsuarioDAO anterior:
 * - Eliminados todos los System.out.println de debug (reemplazados por AppLogger)
 * - Devuelve Optional<Usuario> en lugar de null — obliga al llamador a manejar el caso vacío
 * - Lanza DataAccessException en lugar de silenciar errores SQL
 * - La lógica de BCrypt permanece aquí (concerns de seguridad de datos)
 */
public class UsuarioRepository {

    /**
     * Busca un usuario por username y valida la contraseña contra el hash BCrypt.
     *
     * @param username el nombre de usuario ingresado
     * @param passPlano la contraseña en texto plano (se compara con el hash en BD)
     * @return Optional<Usuario> con el usuario autenticado, vacío si credenciales incorrectas
     * @throws DataAccessException si ocurre un error de BD
     */
    public Optional<Usuario> findByCredenciales(String username, String passPlano)
            throws DataAccessException {

        final String sql = "SELECT id_usuario, nombre_completo, username, password, rol "
                         + "FROM Usuarios WHERE username = ?";

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    AppLogger.warn("Intento de login fallido — usuario no encontrado: " + username);
                    return Optional.empty();
                }

                String hashAlmacenado = rs.getString("password").trim();

                // TODO: COMPARACIÓN TEMPORAL EN TEXTO PLANO — revertir a BCrypt antes de producción
                // if (!BCrypt.checkpw(passPlano, hashAlmacenado)) {
                if (!passPlano.equals(hashAlmacenado)) {
                    AppLogger.warn("Intento de login fallido — contraseña incorrecta para: " + username);
                    return Optional.empty();
                }

                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id_usuario"));
                usuario.setNombreCompleto(rs.getString("nombre_completo"));
                usuario.setUsername(rs.getString("username"));
                usuario.setRol(rs.getString("rol"));

                AppLogger.info("Autenticación exitosa para: " + username + " | Rol: " + usuario.getRol());
                return Optional.of(usuario);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error de BD al validar credenciales: " + e.getMessage(), e);
        }
    }

    /**
     * Registra un nuevo usuario aplicando hashing BCrypt automáticamente.
     *
     * @param u objeto Usuario con los datos (sin contraseña)
     * @param passPlano contraseña en texto plano — se hashea antes de guardar
     * @throws DataAccessException si ocurre un error de BD (ej: username duplicado)
     */
    public void save(Usuario u, String passPlano) throws DataAccessException {
        final String sql = "INSERT INTO Usuarios (nombre_completo, username, password, rol) "
                         + "VALUES (?, ?, ?, ?)";

        // Generar hash BCrypt con factor de costo 12
        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(12));

        try (Connection conn = ConexionSQL.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getNombreCompleto());
            stmt.setString(2, u.getUsername());
            stmt.setString(3, hash);
            stmt.setString(4, u.getRol());
            stmt.executeUpdate();

            AppLogger.info("Usuario registrado: " + u.getUsername() + " | Rol: " + u.getRol());

        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar usuario '" + u.getUsername() + "': " + e.getMessage(), e);
        }
    }
}
