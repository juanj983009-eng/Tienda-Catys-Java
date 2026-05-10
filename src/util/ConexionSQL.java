package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory de conexiones SQL Server.
 *
 * CAMBIO ARQUITECTÓNICO: Ya NO es un Singleton con conexión estática compartida.
 * Cada llamada a getConnection() devuelve una conexión fresca e independiente.
 * Esto elimina el bug donde setAutoCommit(false) de una transacción de venta
 * podía contaminar otras queries concurrentes.
 *
 * CONFIGURACIÓN (config.properties):
 *   db.host     = localhost          (obligatorio)
 *   db.port     = 1433               (usar si NO hay instancia con nombre)
 *   db.instance = SQLEXPRESS         (usar en vez de db.port, para instancias con nombre)
 *   db.name     = CatysDB            (obligatorio)
 *   db.user     = sa                 (obligatorio)
 *   db.pass     = tu_contraseña      (obligatorio)
 *
 * El cierre de la conexión es responsabilidad del llamador:
 *   try (Connection conn = ConexionSQL.getConnection()) { ... }
 */
public final class ConexionSQL {

    // Config cacheada al primer uso — inmutable después de inicializar
    private static volatile String jdbcUrl;
    private static volatile String dbUser;
    private static volatile String dbPass;
    private static volatile boolean configLoaded = false;

    // Solo carga el driver una vez al arrancar la JVM
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            AppLogger.error("Driver JDBC de SQL Server no encontrado en lib/. "
                          + "Añade mssql-jdbc-*.jar a las dependencias del proyecto.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConexionSQL() {}

    // ------------------------------------------------------------------
    //  Carga de configuración
    // ------------------------------------------------------------------

    /**
     * Lee config.properties y construye la JDBC URL de forma dinámica.
     * Soporta dos modos:
     *   - Puerto explícito:    jdbc:sqlserver://host:puerto;databaseName=...
     *   - Instancia con nombre: jdbc:sqlserver://host\INSTANCIA;databaseName=...
     *
     * Operación idempotente y thread-safe.
     */
    private static synchronized void loadConfig() {
        if (configLoaded) return;

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(fis);

            dbUser = props.getProperty("db.user", "").trim();
            dbPass = props.getProperty("db.pass", "").trim();

            String host     = props.getProperty("db.host",     "localhost").trim();
            String port     = props.getProperty("db.port",     "").trim();
            String instance = props.getProperty("db.instance", "").trim();
            String dbName   = props.getProperty("db.name",     "CatysDB").trim();

            // Validaciones mínimas
            if (dbUser.isEmpty())
                throw new RuntimeException("config.properties: db.user no puede estar vacío.");
            if (dbName.isEmpty())
                throw new RuntimeException("config.properties: db.name no puede estar vacío.");

            // Construir la parte del servidor de la URL
            String serverPart;
            if (!instance.isEmpty()) {
                // Modo instancia con nombre → host\INSTANCIA  (sin puerto)
                serverPart = host + "\\\\" + instance;
                AppLogger.info("Modo conexión: instancia con nombre → " + host + "\\" + instance
                             + " | BD: " + dbName);
            } else {
                // Modo puerto explícito → host:puerto
                String p = port.isEmpty() ? "1433" : port;
                serverPart = host + ":" + p;
                AppLogger.info("Modo conexión: puerto explícito → " + host + ":" + p
                             + " | BD: " + dbName);
            }

            jdbcUrl = "jdbc:sqlserver://" + serverPart + ";"
                    + "databaseName=" + dbName + ";"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
                    + "loginTimeout=10;";   // 10 s máx. de espera — evita cuelgues silenciosos

            configLoaded = true;
            AppLogger.info("JDBC URL construida: " + jdbcUrl.replaceAll("(?i)(password=)[^;]+", "$1***"));

        } catch (IOException e) {
            String msg = "No se pudo leer 'config.properties'. "
                       + "Asegúrate de que el archivo esté en la raíz del proyecto (junto a src/).";
            e.printStackTrace();
            AppLogger.error(msg, e);
            System.err.println("[CATYS-BD] " + msg);
            System.err.println("[CATYS-BD] Causa: " + e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }

    // ------------------------------------------------------------------
    //  API pública
    // ------------------------------------------------------------------

    /**
     * Obtiene una conexión activa e independiente a CatysDB.
     *
     * USO OBLIGATORIO con try-with-resources:
     * <pre>
     *   try (Connection conn = ConexionSQL.getConnection()) {
     *       // usar conn...
     *   } // conn.close() se llama automáticamente
     * </pre>
     *
     * @return una nueva Connection a SQL Server
     * @throws SQLException si no se puede establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        // TODO: HARDCODE TEMPORAL — revertir antes de producción
        String url  = "jdbc:sqlserver://localhost:1433;databaseName=CatysDB;encrypt=true;trustServerCertificate=true;";
        String user = "juan_admin";
        String pass = "123";
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Verifica que la BD es alcanzable e imprime un diagnóstico detallado.
     * Distingue entre: error de driver, error de red/puerto, error de login
     * y error de base de datos no encontrada.
     *
     * @return true si la conexión de prueba es exitosa
     */
    public static boolean testConnection() {
        AppLogger.info("Iniciando test de conexión a CatysDB...");
        try (Connection conn = getConnection()) {
            boolean ok = conn != null && !conn.isClosed();
            if (ok) {
                AppLogger.info("✔ Conexión exitosa con CatysDB.");
                System.out.println("[CATYS-BD] ✔ Conexión exitosa con CatysDB.");
            }
            return ok;
        } catch (SQLException e) {
            diagnosticarError(e);
            return false;
        }
    }

    /**
     * Analiza el SQLState y el error code de SQL Server para imprimir
     * un mensaje de diagnóstico útil en consola y log.
     *
     * Códigos de error MSSQL relevantes:
     *   18456 — Login failed (usuario/contraseña incorrectos)
     *   4060  — Cannot open database (BD no existe o sin permisos)
     *   18452 — Login failed, not associated with trusted connection
     *   08001 / 08S01 — No se pudo conectar al servidor (puerto/host)
     */
    private static void diagnosticarError(SQLException e) {
        int    errorCode = e.getErrorCode();
        String sqlState  = e.getSQLState() != null ? e.getSQLState() : "N/A";
        String mensaje   = e.getMessage()  != null ? e.getMessage()  : "(sin mensaje)";

        String tipo;
        String sugerencia;

        if (sqlState.startsWith("08") || mensaje.toLowerCase().contains("connect")) {
            tipo       = "ERROR DE RED / PUERTO";
            sugerencia = "Verifica que SQL Server esté activo (Servicios de Windows) "
                       + "y que el puerto/instancia en config.properties sea correcto. "
                       + "Para instancias con nombre (ej: SQLEXPRESS) activa el servicio "
                       + "'SQL Server Browser' y usa db.instance=SQLEXPRESS en lugar de db.port.";
        } else if (errorCode == 18456 || errorCode == 18452) {
            tipo       = "ERROR DE LOGIN (credenciales)";
            sugerencia = "El usuario o la contraseña son incorrectos. "
                       + "Revisa db.user y db.pass en config.properties. "
                       + "Si usas autenticación Windows (sin usuario/pass), "
                       + "deja db.user y db.pass vacíos y añade integratedSecurity=true a la URL.";
        } else if (errorCode == 4060) {
            tipo       = "BASE DE DATOS NO ENCONTRADA";
            sugerencia = "La base de datos 'CatysDB' no existe en el servidor. "
                       + "Ejecuta el script CatysDB_Setup.sql en SSMS para crearla. "
                       + "También verifica que db.name=CatysDB en config.properties.";
        } else {
            tipo       = "ERROR SQL DESCONOCIDO";
            sugerencia = "Revisa el mensaje completo a continuación.";
        }

        // Salida a consola (siempre visible aunque el logger falle)
        System.err.println("╔══════════════════════════════════════════════════╗");
        System.err.println("║   CATYS — Error de conexión con la base de datos ║");
        System.err.println("╠══════════════════════════════════════════════════╣");
        System.err.println("  Tipo      : " + tipo);
        System.err.println("  SQLState  : " + sqlState);
        System.err.println("  ErrorCode : " + errorCode);
        System.err.println("  Mensaje   : " + mensaje);
        System.err.println("  Sugerencia: " + sugerencia);
        System.err.println("  JDBC URL  : " + jdbcUrl);
        System.err.println("╚══════════════════════════════════════════════════╝");
        e.printStackTrace();

        // También al log de archivo
        AppLogger.error("[" + tipo + "] SQLState=" + sqlState
                      + " | ErrorCode=" + errorCode + " | " + mensaje, e);
    }
}