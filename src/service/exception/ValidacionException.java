package service.exception;

/**
 * Excepción de validación para datos incorrectos en la capa de servicio.
 * Ejemplos: nombre vacío, precio negativo, stock inválido.
 */
public class ValidacionException extends Exception {

    private final String campo;

    public ValidacionException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    /**
     * @return el nombre del campo que falló la validación (útil para resaltar en la UI)
     */
    public String getCampo() {
        return campo;
    }
}
