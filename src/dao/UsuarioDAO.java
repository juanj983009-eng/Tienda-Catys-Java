package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import modelo.Usuario;
import util.ConexionSQL;

public class UsuarioDAO {

    public Usuario validarLogin(String user, String pass) {
        Usuario usuario = null;
        String sql = "SELECT * FROM Usuarios WHERE username = ? AND password = ?";

        try (Connection conn = ConexionSQL.getConexion();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, pass);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setId(rs.getInt("id_usuario"));
                usuario.setNombreCompleto(rs.getString("nombre_completo"));
                usuario.setUsername(rs.getString("username"));
                usuario.setRol(rs.getString("rol"));
            }

        } catch (Exception e) {
            System.out.println("Error en Login: " + e.getMessage());
        }

        return usuario; // Si retorna null, es que el login falló
    }
}