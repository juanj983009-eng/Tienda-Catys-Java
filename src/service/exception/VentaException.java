package service.exception;

/**
 * Excepción de negocio para errores en el proceso de venta.
 * Ejemplos: carrito vacío, stock insuficiente, datos de cliente inválidos.
 */
public class VentaException extends Exception {

    public VentaException(String message) {
        super(message);
    }

    public VentaException(String message, Throwable cause) {
        super(message, cause);
    }
}
