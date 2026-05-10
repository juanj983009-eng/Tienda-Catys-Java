package repository.exception;

/**
 * Excepción checked para errores de acceso a datos.
 * Reemplaza los cientos de catch(Exception e){} vacíos — ahora la capa Service
 * DEBE manejar este error explícitamente, haciéndolo visible al desarrollador.
 */
public class DataAccessException extends Exception {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
