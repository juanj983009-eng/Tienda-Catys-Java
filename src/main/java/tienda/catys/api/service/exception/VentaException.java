package tienda.catys.api.service.exception;

public class VentaException extends Exception {

    public VentaException(String message) {
        super(message);
    }

    public VentaException(String message, Throwable cause) {
        super(message, cause);
    }
}
