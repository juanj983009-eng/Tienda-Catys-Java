package util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Logger centralizado para toda la aplicación Catys ERP.
 * Reemplaza todos los System.out.println y catch(e) {} vacíos.
 * Escribe en consola Y en un archivo de log rotativo.
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("CatysERP");
    private static volatile boolean initialized = false;

    // Constructor privado — clase utilitaria, no instanciable
    private AppLogger() {}

    /**
     * Inicializa el logger con FileHandler rotativo.
     * Llamar UNA vez al inicio del main().
     */
    public static synchronized void init() {
        if (initialized) return;
        try {
            // Formato legible: fecha | nivel | mensaje
            Formatter formatter = new SimpleFormatter() {
                private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
                @Override
                public String format(LogRecord record) {
                    return String.format(FORMAT,
                        new java.util.Date(record.getMillis()),
                        record.getLevel().getName(),
                        record.getMessage()
                    );
                }
            };

            // Handler de consola
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(formatter);
            consoleHandler.setLevel(Level.ALL);

            // Handler de archivo — rota cada 5MB, máximo 3 archivos
            FileHandler fileHandler = new FileHandler("catys-app.log", 5 * 1024 * 1024, 3, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.INFO);

            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

            initialized = true;
        } catch (IOException e) {
            // Fallback: solo consola si no se puede crear el archivo
            LOGGER.warning("No se pudo inicializar el FileHandler de logs: " + e.getMessage());
        }
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void warn(String msg) {
        LOGGER.warning(msg);
    }

    /**
     * Registra un error con su excepción completa (stack trace incluido en el archivo).
     */
    public static void error(String msg, Throwable t) {
        LOGGER.log(Level.SEVERE, msg, t);
    }

    public static void error(String msg) {
        LOGGER.severe(msg);
    }

    public static void debug(String msg) {
        LOGGER.fine(msg);
    }
}
