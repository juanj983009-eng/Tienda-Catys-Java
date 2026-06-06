package tienda.catys.api.modelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MetodoPago {

    EFECTIVO("💵 Efectivo"),
    TARJETA_CREDITO("💳 Tarjeta de Crédito"),
    YAPE_PLIN("📱 Yape / Plin");

    private static final Logger log = LoggerFactory.getLogger(MetodoPago.class);
    private final String displayName;

    MetodoPago(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MetodoPago fromDisplayName(String name) {
        if (name == null) return EFECTIVO;
        for (MetodoPago mp : values()) {
            if (mp.displayName.equals(name)) return mp;
        }
        log.warn("MetodoPago desconocido recibido: '{}'. Se usará EFECTIVO como default.", name);
        return EFECTIVO;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
