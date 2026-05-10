package service.strategy;

/**
 * Interfaz Strategy para aplicar descuentos a una venta.
 * Permite agregar nuevos tipos de descuento (cupones, fidelidad, promociones)
 * sin modificar el código existente — cumple el principio Open/Closed (OCP).
 *
 * Ejemplo de uso en VentaService:
 *   DescuentoStrategy descuento = new DescuentoPorcentual(0.10); // 10%
 *   double totalFinal = descuento.aplicar(subtotal);
 */
public interface DescuentoStrategy {

    /**
     * Aplica el descuento al subtotal dado.
     * @param subtotal el total antes del descuento (siempre >= 0)
     * @return el total después de aplicar el descuento
     */
    double aplicar(double subtotal);

    /**
     * Descripción legible del descuento para mostrar en el ticket.
     * Ejemplo: "Sin descuento", "Descuento 10%", "Cupón BIENVENIDA"
     */
    String getDescripcion();
}
