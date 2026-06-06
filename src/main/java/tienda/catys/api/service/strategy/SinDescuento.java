package tienda.catys.api.service.strategy;

public class SinDescuento implements DescuentoStrategy {

    @Override
    public double aplicar(double subtotal) {
        return subtotal;
    }

    @Override
    public String getDescripcion() {
        return "Sin descuento";
    }
}
