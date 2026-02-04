package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionSQL {

    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;" +
            "databaseName=CatysDB;" +
            "encrypt=true;" +
            "trustServerCertificate=true;" +
            "sendStringParametersAsUnicode=true;";

    public static Connection getConexion() {
        Properties props = new Properties();
        try {
            // Cargamos el archivo de configuración
            props.load(new FileInputStream("config.properties"));
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.pass");

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(CONNECTION_URL, user, pass);

        } catch (IOException e) {
            System.out.println("Error: No se encontró el archivo config.properties");
            return null;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error de Conexión: " + e.getMessage());
            return null;
        }
    }
}