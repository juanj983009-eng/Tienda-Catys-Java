package tienda.catys.api.service.exception;

public class ValidacionException extends Exception {

    private final String campo;

    public ValidacionException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
