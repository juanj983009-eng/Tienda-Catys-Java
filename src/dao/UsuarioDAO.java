package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;
import util.ConexionSQL;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase DAO para la gestión de usuarios y autenticación.
 * Implementa seguridad mediante hashing de contraseñas con BCrypt.
 */
public class UsuarioDAO {

    /**
     * Valida las credenciales de un usuario.
     * @param user Nombre de usuario ingresado.
     * @param passPlano Contraseña en texto plano ingresada en la interfaz.
     * @return Objeto Usuario si las credenciales son correctas, null en caso contrario.
     */
    public Usuario validarLogin(String user, String passPlano) {
        Usuario usuario = null;
        
        // Buscamos al usuario solo por su nombre de usuario.
        // No filtramos por password en el SQL porque necesitamos traer el hash para compararlo en Java.
        String sql = "SELECT id_usuario, nombre_completo, username, password, rol FROM Usuarios WHERE username = ?";

        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Extraemos el hash almacenado en la base de datos.
                    String hashAlmacenado = rs.getString("password");

                    // Comparamos la contraseña ingresada con el hash usando BCrypt.
                    if (BCrypt.checkpw(passPlano, hashAlmacenado)) {
                        usuario = new Usuario();
                        usuario.setId(rs.getInt("id_usuario"));
                        usuario.setNombreCompleto(rs.getString("nombre_completo"));
                        usuario.setUsername(rs.getString("username"));
                        usuario.setRol(rs.getString("rol"));

                        System.out.println("Autenticación exitosa para: " + user);
                    } else {
                        System.out.println("Contraseña incorrecta para el usuario: " + user);
                    }
                } else {
                    System.out.println("El usuario no existe: " + user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error crítico en UsuarioDAO: " + e.getMessage());
        }

        return usuario;
    }

    /**
     * Registra un nuevo usuario haseando su contraseña antes de guardarla.
     */
    public boolean registrarUsuario(Usuario u, String passPlano) {
        String sql = "INSERT INTO Usuarios (nombre_completo, username, password, rol) VALUES (?, ?, ?, ?)";

        // Generamos el hash seguro de la contraseña.
        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(12));

        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, u.getNombreCompleto());
            pstmt.setString(2, u.getUsername());
            pstmt.setString(3, hash); // Guardamos el HASH, nunca el texto plano.
            pstmt.setString(4, u.getRol());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }
}