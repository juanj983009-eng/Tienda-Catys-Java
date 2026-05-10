package modelo;

import util.AppLogger;

/**
 * Enum tipado para métodos de pago.
 * Reemplaza el String raw "💳 Tarjeta de Crédito" que se usaba en TiendaGUI.
 * Ahora el compilador detecta métodos de pago inválidos en tiempo de compilación.
 */
public enum MetodoPago {

    EFECTIVO("💵 Efectivo"),
    TARJETA_CREDITO("💳 Tarjeta de Crédito"),
    YAPE_PLIN("📱 Yape / Plin");

    private final String displayName;

    MetodoPago(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convierte el displayName (el String del JComboBox) de vuelta al Enum.
     * Seguro ante nulls — retorna EFECTIVO como default.
     */
    public static MetodoPago fromDisplayName(String name) {
        if (name == null) return EFECTIVO;
        for (MetodoPago mp : values()) {
            if (mp.displayName.equals(name)) return mp;
        }
        AppLogger.warn("MetodoPago desconocido recibido: '" + name + "'. Se usará EFECTIVO como default.");
        return EFECTIVO;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
