package util;

import org.mindrot.jbcrypt.BCrypt;

public class Encriptador {
    
    // Genera un hash seguro para guardar en la BD
    public static String encriptarPassword(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(12));
    }

    // Compara la clave que ingresa el usuario con el hash de la BD
    public static boolean verificarPassword(String passwordPlano, String passwordHaseado) {
        try {
            return BCrypt.checkpw(passwordPlano, passwordHaseado);
        } catch (Exception e) {
            return false;
        }
    }
}