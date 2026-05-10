package service.strategy;

/**
 * Implementación nula del patrón Strategy — representa el caso sin descuento.
 * Es el default en VentaService cuando no se configura otro descuento.
 * Implementa el patrón Null Object para evitar null-checks en VentaService.
 */
public class SinDescuento implements DescuentoStrategy {

    @Override
    public double aplicar(double subtotal) {
        return subtotal; // Sin modificación
    }

    @Override
    public String getDescripcion() {
        return "Sin descuento";
    }
}
