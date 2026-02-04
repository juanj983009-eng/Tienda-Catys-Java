package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;
import util.ConexionSQL;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase DAO para la gestión de usuarios y autenticación segura.
 * Implementa el estándar de la industria BCrypt para protección de contraseñas.
 */
public class UsuarioDAO {

    /**
     * Valida las credenciales de acceso al sistema.
     * @param user Nombre de usuario ingresado.
     * @param passPlano Contraseña sin encriptar ingresada.
     * @return Un objeto Usuario si los datos son correctos; de lo contrario, null.
     */
    public Usuario validarLogin(String user, String passPlano) {
        Usuario usuario = null;
        
        // Buscamos al usuario por su username
        String sql = "SELECT id_usuario, nombre_completo, username, password, rol FROM Usuarios WHERE username = ?";

        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Obtenemos el hash y aplicamos .trim() para eliminar espacios de SQL Server
                    String hashAlmacenado = rs.getString("password").trim();

                    // --- BLOQUE DE DEPURACIÓN (Revisa esto en tu consola de VS Code) ---
                    System.out.println("--- DEBUG LOGIN ---");
                    System.out.println("Usuario buscado: [" + user + "]");
                    System.out.println("Largo del hash en BD: " + hashAlmacenado.length());
                    System.out.println("Hash recuperado: [" + hashAlmacenado + "]");
                    System.out.println("-------------------");

                    try {
                        // Verificamos si la clave coincide con el hash
                        if (BCrypt.checkpw(passPlano, hashAlmacenado)) {
                            usuario = new Usuario();
                            usuario.setId(rs.getInt("id_usuario"));
                            usuario.setNombreCompleto(rs.getString("nombre_completo"));
                            usuario.setUsername(rs.getString("username"));
                            usuario.setRol(rs.getString("rol"));
                            
                            System.out.println("✅ Autenticación exitosa para: " + user);
                        } else {
                            System.out.println("⚠️ Contraseña incorrecta para: " + user);
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("❌ Error: El hash en la BD no tiene formato BCrypt.");
                    }
                } else {
                    System.out.println("⚠️ Usuario no encontrado: " + user);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error de SQL en UsuarioDAO: " + e.getMessage());
        }

        return usuario; 
    }

    /**
     * Registra un nuevo usuario aplicando Hashing automáticamente.
     */
    public boolean registrarUsuario(Usuario u, String passPlano) {
        String sql = "INSERT INTO Usuarios (nombre_completo, username, password, rol) VALUES (?, ?, ?, ?)";
        
        // Generamos el hash seguro de 60 caracteres
        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(12));

        try (Connection conn = ConexionSQL.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, u.getNombreCompleto());
            pstmt.setString(2, u.getUsername());
            pstmt.setString(3, hash); 
            pstmt.setString(4, u.getRol());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }
}