package service.strategy;

/**
 * Descuento por porcentaje sobre el subtotal.
 * Ejemplo: new DescuentoPorcentual(0.10) = 10% de descuento.
 */
public class DescuentoPorcentual implements DescuentoStrategy {

    private final double porcentaje; // Entre 0.0 y 1.0

    /**
     * @param porcentaje valor entre 0.0 (0%) y 1.0 (100%)
     * @throws IllegalArgumentException si el porcentaje está fuera de rango
     */
    public DescuentoPorcentual(double porcentaje) {
        if (porcentaje < 0.0 || porcentaje > 1.0) {
            throw new IllegalArgumentException(
                "El porcentaje debe estar entre 0.0 y 1.0. Recibido: " + porcentaje
            );
        }
        this.porcentaje = porcentaje;
    }

    @Override
    public double aplicar(double subtotal) {
        return subtotal * (1.0 - porcentaje);
    }

    @Override
    public String getDescripcion() {
        return String.format("Descuento %.0f%%", porcentaje * 100);
    }
}
