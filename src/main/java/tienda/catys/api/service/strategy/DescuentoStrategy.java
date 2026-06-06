package tienda.catys.api.service.strategy;

public interface DescuentoStrategy {
    double aplicar(double subtotal);
    String getDescripcion();
}
