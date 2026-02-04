package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQL {

    // DATOS DE CONEXIÓN
    // Hemos agregado 'sendStringParametersAsUnicode=true' para arreglar las tildes
    private static final String CONNECTION_URL = 
            "jdbc:sqlserver://localhost:1433;" +
            "databaseName=CatysDB;" +
            "encrypt=true;" +
            "trustServerCertificate=true;" +
            "sendStringParametersAsUnicode=true;"; 
    
    // --- TUS CREDENCIALES ---
    private static final String USER = "sa";
    private static final String PASS = "123456"; // Asegúrate que esta sea tu clave real
    // ------------------------

    public static Connection getConexion() {
        try {
            // Cargamos el driver manualmente para asegurar que Java lo encuentre
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            return DriverManager.getConnection(CONNECTION_URL, USER, PASS);
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error CRÍTICO: Falta el archivo .jar de SQL Server en la carpeta lib.");
            return null;
        } catch (SQLException e) {
            System.out.println("❌ Error de Conexión SQL: " + e.getMessage());
            return null;
        }
    }
}