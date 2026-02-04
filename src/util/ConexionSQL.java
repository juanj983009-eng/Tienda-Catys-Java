package util;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionSQL {
    // 1. La instancia única (estática)
    private static Connection connection = null;

    // 2. Constructor privado (evita que alguien haga 'new ConexionSQL()')
    private ConexionSQL() {}

    // 3. Método para obtener la conexión única
    public static Connection getConexion() {
        try {
            // Solo creamos la conexión si no existe o si se cerró
            if (connection == null || connection.isClosed()) {
                Properties props = new Properties();
                // Cargamos tus credenciales desde el archivo que ya configuramos
                props.load(new FileInputStream("config.properties"));

                String url = "jdbc:sqlserver://localhost:1433;" +
                             "databaseName=CatysDB;" +
                             "encrypt=true;" +
                             "trustServerCertificate=true;";

                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.pass");

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("✅ Conexión establecida exitosamente.");
            }
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
        return connection;
    }

    // 4. Método para cerrar la conexión (opcional, para limpieza)
    public static void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Conexión cerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}