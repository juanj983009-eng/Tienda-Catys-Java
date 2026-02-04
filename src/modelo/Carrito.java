package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Carrito {

    private ArrayList<Producto> lista = new ArrayList<>();

    public void agregarProducto(Producto p) {
        lista.add(p);
    }

    public void vaciar() {
        lista.clear();
    }

    public boolean estaVacio() {
        return lista.isEmpty();
    }

    public ArrayList<Producto> getLista() {
        return lista;
    }

    // Método vital para guardar el total exacto en SQL Server
    public double obtenerTotalNumerico() {
        double total = 0;
        for (Producto p : lista) {
            total += p.getPrecio();
        }
        return total;
    }

    // Método para generar el texto bonito del ticket (Agrupado xCantidad)
    public String generarResumen() {
        if (lista.isEmpty()) {
            return "El carrito está vacío.";
        }

        Map<String, Integer> conteo = new HashMap<>();
        Map<String, Double> precios = new HashMap<>();

        // Contamos frecuencias
        for (Producto p : lista) {
            conteo.put(p.getNombre(), conteo.getOrDefault(p.getNombre(), 0) + 1);
            precios.put(p.getNombre(), p.getPrecio());
        }

        StringBuilder sb = new StringBuilder();
        double total = 0;

        for (String nombre : conteo.keySet()) {
            int cantidad = conteo.get(nombre);
            double precioUnitario = precios.get(nombre);
            double subtotal = cantidad * precioUnitario;

            // Formato: Nombre (corto) ..... xCant ..... Subtotal
            String nombreCorto = nombre.length() > 20 ? nombre.substring(0, 18) + ".." : nombre;
            sb.append(String.format("- %-20s x%-2d   S/ %6.2f\n", nombreCorto, cantidad, subtotal));

            total += subtotal;
        }

        sb.append("------------------------------------\n");
        sb.append(String.format("TOTAL A PAGAR:          S/ %6.2f", total));

        return sb.toString();
    }
}