package service;

import dto.ItemVentaDTO;
import dto.ResumenVentasDTO;
import dto.VentaDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.MetodoPago;
import modelo.Producto;
import repository.ProductoRepository;
import repository.VentaRepository;
import repository.exception.DataAccessException;
import service.exception.VentaException;
import service.strategy.DescuentoStrategy;
import service.strategy.SinDescuento;
import util.AppLogger;

/**
 * Servicio de negocio para el módulo de Ventas.
 *
 * Responsabilidades:
 *   - Orquestar la transacción de venta (validar → persistir → notificar)
 *   - Aplicar descuentos mediante el patrón Strategy
 *   - Transformar el Carrito (modelo de UI) en ItemVentaDTO (modelo de datos)
 *   - Proporcionar datos de reportes a la UI sin que esta toque el Repository
 *
 * La UI NUNCA llama al Repository directamente — siempre pasa por este Service.
 */
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private DescuentoStrategy descuento;

    /**
     * Constructor con Inyección de Dependencias.
     * Permite sustituir las implementaciones en tests sin modificar el código.
     */
    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.descuento = new SinDescuento(); // Default: sin descuento
    }

    /**
     * Configura la estrategia de descuento a aplicar en la siguiente venta.
     * Ejemplo: ventaService.setDescuento(new DescuentoPorcentual(0.10));
     */
    public void setDescuento(DescuentoStrategy descuento) {
        this.descuento = descuento != null ? descuento : new SinDescuento();
    }

    /**
     * Procesa una venta completa de forma atómica.
     *
     * Flujo:
     *   1. Validar que el nombre del cliente no esté vacío
     *   2. Validar que la lista de ítems no esté vacía
     *   3. Validar stock disponible para cada ítem (consulta optimista — la BD lo garantiza en la transacción)
     *   4. Calcular el total con el descuento configurado
     *   5. Persistir via VentaRepository (transacción atómica)
     *
     * @param clienteNombre nombre del cliente
     * @param metodoPago    método de pago tipado
     * @param items         lista de ítems agrupados con cantidades
     * @throws VentaException      si hay errores de negocio (carrito vacío, stock insuficiente)
     * @throws DataAccessException si hay errores de persistencia
     */
    public void procesarVenta(String clienteNombre, MetodoPago metodoPago, List<ItemVentaDTO> items)
            throws VentaException, DataAccessException {

        // --- Validaciones de negocio ---
        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            throw new VentaException("El nombre del cliente no puede estar vacío.");
        }
        if (items == null || items.isEmpty()) {
            throw new VentaException("El carrito está vacío. Agrega al menos un producto.");
        }

        // Calcular subtotal
        double subtotal = items.stream().mapToDouble(ItemVentaDTO::getSubtotal).sum();
        double totalConDescuento = descuento.aplicar(subtotal);

        AppLogger.info(String.format("Procesando venta — Cliente: %s | Ítems: %d | Subtotal: S/%.2f | %s | Total: S/%.2f",
            clienteNombre, items.size(), subtotal, descuento.getDescripcion(), totalConDescuento));

        // Construir el DTO de cabecera
        VentaDTO ventaDTO = new VentaDTO(0, LocalDateTime.now(), clienteNombre.trim(),
                                         metodoPago, totalConDescuento, "");

        // Delegar persistencia al Repository
        try {
            ventaRepository.save(ventaDTO, items);
        } catch (DataAccessException e) {
            // Si el error es de stock insuficiente, lo convertimos a VentaException (error de negocio)
            if (e.getMessage().contains("Stock insuficiente")) {
                throw new VentaException(e.getMessage(), e);
            }
            throw e; // Otros errores de BD se propagan como DataAccessException
        }
    }

    /**
     * Convierte una lista plana de Producto (del Carrito) en ItemVentaDTOs agrupados.
     * Los duplicados se agrupan: [Lomo, Lomo, Papa] → [Lomo x2, Papa x1]
     *
     * @param productosCarrito lista de Produto del carrito (con duplicados)
     * @return lista de ItemVentaDTO agrupados, lista ordenada por nombre
     */
    public List<ItemVentaDTO> agruparItems(List<Producto> productosCarrito) {
        Map<Integer, ItemVentaDTO> agrupados = new HashMap<>();

        for (Producto p : productosCarrito) {
            if (agrupados.containsKey(p.getId())) {
                ItemVentaDTO existente = agrupados.get(p.getId());
                agrupados.put(p.getId(), new ItemVentaDTO(
                    p.getId(), p.getNombre(),
                    existente.cantidad() + 1,
                    p.getPrecio()
                ));
            } else {
                agrupados.put(p.getId(), new ItemVentaDTO(p.getId(), p.getNombre(), 1, p.getPrecio()));
            }
        }
        return new ArrayList<>(agrupados.values());
    }

    /**
     * Calcula el total de un carrito con el descuento configurado (para mostrar en UI).
     *
     * @param items lista de ítems del carrito
     * @return total con descuento aplicado
     */
    public double calcularTotal(List<ItemVentaDTO> items) {
        double subtotal = items.stream().mapToDouble(ItemVentaDTO::getSubtotal).sum();
        return descuento.aplicar(subtotal);
    }

    /**
     * Genera el texto de resumen del carrito para mostrar al usuario.
     */
    public String generarResumenCarrito(List<ItemVentaDTO> items) {
        if (items.isEmpty()) return "El carrito está vacío.";
        StringBuilder sb = new StringBuilder();
        double subtotal = 0;
        for (ItemVentaDTO item : items) {
            sb.append(item.toLineaTicket()).append("\n");
            subtotal += item.getSubtotal();
        }
        double total = descuento.aplicar(subtotal);
        sb.append("------------------------------------\n");
        if (!(descuento instanceof SinDescuento)) {
            sb.append(String.format("  %-20s        S/ %6.2f\n", descuento.getDescripcion(), subtotal - total));
        }
        sb.append(String.format("TOTAL A PAGAR:          S/ %6.2f", total));
        return sb.toString();
    }

    /**
     * Obtiene el resumen ejecutivo de ventas para el panel de reportes.
     *
     * @return ResumenVentasDTO con totales pre-calculados en BD
     * @throws DataAccessException si hay error de persistencia
     */
    public ResumenVentasDTO obtenerResumen() throws DataAccessException {
        return ventaRepository.getResumen();
    }

    /**
     * Obtiene el historial completo de ventas para la tabla de reportes.
     * La UI convierte este List a DefaultTableModel — no el Repository.
     *
     * @return lista de VentaDTO
     * @throws DataAccessException si hay error de persistencia
     */
    public List<VentaDTO> obtenerHistorial() throws DataAccessException {
        return ventaRepository.findAll();
    }
}
