package tienda.catys.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tienda.catys.api.dto.ItemVentaDTO;
import tienda.catys.api.dto.ResumenVentasDTO;
import tienda.catys.api.dto.VentaDTO;
import tienda.catys.api.modelo.Cliente;
import tienda.catys.api.modelo.Comanda;
import tienda.catys.api.modelo.ComandaItem;
import tienda.catys.api.modelo.Mesa;
import tienda.catys.api.modelo.MetodoPago;
import tienda.catys.api.modelo.Producto;
import tienda.catys.api.repository.ClienteRepository;
import tienda.catys.api.repository.ComandaRepository;
import tienda.catys.api.repository.MesaRepository;
import tienda.catys.api.repository.ProductoRepository;
import tienda.catys.api.repository.VentaRepository;
import tienda.catys.api.repository.exception.DataAccessException;
import tienda.catys.api.service.exception.VentaException;
import tienda.catys.api.service.strategy.DescuentoStrategy;
import tienda.catys.api.service.strategy.SinDescuento;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final MesaRepository mesaRepository;
    private final ComandaRepository comandaRepository;
    private DescuentoStrategy descuento;

    @PersistenceContext
    private EntityManager entityManager;

    public VentaService(VentaRepository ventaRepository,
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository,
                        MesaRepository mesaRepository,
                        ComandaRepository comandaRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.mesaRepository = mesaRepository;
        this.comandaRepository = comandaRepository;
        this.descuento = new SinDescuento();
    }

    public void setDescuento(DescuentoStrategy descuento) {
        this.descuento = descuento != null ? descuento : new SinDescuento();
    }

    @Transactional
    public void procesarVenta(String clienteNombre, Integer clienteId, Integer idMesa, MetodoPago metodoPago, List<ItemVentaDTO> items)
            throws VentaException, DataAccessException {

        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            throw new VentaException("El nombre del cliente no puede estar vacío.");
        }
        if (items == null || items.isEmpty()) {
            throw new VentaException("El carrito está vacío. Agrega al menos un producto.");
        }

        double subtotal = items.stream().mapToDouble(ItemVentaDTO::getSubtotal).sum();
        double totalConDescuento = descuento.aplicar(subtotal);

        log.info("Procesando venta — Cliente: {} (ID: {}) | Mesa: {} | Ítems: {} | Subtotal: S/{} | {} | Total: S/{}",
            clienteNombre, clienteId, idMesa, items.size(), String.format("%.2f", subtotal), descuento.getDescripcion(), String.format("%.2f", totalConDescuento));

        VentaDTO ventaDTO = new VentaDTO(0, LocalDateTime.now(), clienteNombre.trim(),
                                         metodoPago, totalConDescuento, "");

        // 1. Guardar la venta y sus detalles en base de datos mediante JDBC (lógica preexistente)
        try {
            ventaRepository.save(ventaDTO, items);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Stock insuficiente")) {
                throw new VentaException(e.getMessage(), e);
            }
            throw e;
        }

        // 2. Si clienteId no es nulo e id > 0, incrementar contador de visitas
        Cliente cliente = null;
        if (clienteId != null && clienteId > 0) {
            cliente = clienteRepository.findById(clienteId).orElse(null);
            if (cliente != null) {
                cliente.setVisitas(cliente.getVisitas() + 1);
                clienteRepository.save(cliente);
                log.info("Incrementadas visitas del cliente VIP ID: {}. Visitas totales: {}", clienteId, cliente.getVisitas());
            }
        }

        // 3. Buscar Mesa si aplica
        Mesa mesa = null;
        if (idMesa != null && idMesa > 0) {
            mesa = mesaRepository.findById(idMesa).orElse(null);
        }

        // 4. Instanciar y guardar Comanda para cocina en estado 'en_cola'
        Comanda comanda = new Comanda();
        comanda.setClienteNombre(clienteNombre.trim());
        comanda.setEstado("en_cola");
        comanda.setFechaHora(LocalDateTime.now());
        comanda.setCliente(cliente);
        comanda.setMesa(mesa);

        for (ItemVentaDTO item : items) {
            Producto producto = entityManager.find(Producto.class, item.idProducto());
            if (producto != null) {
                ComandaItem comandaItem = new ComandaItem();
                comandaItem.setProducto(producto);
                comandaItem.setCantidad(item.cantidad());
                comanda.addItem(comandaItem);
            } else {
                log.warn("No se encontró el producto con ID {} para vincularlo a la comanda.", item.idProducto());
            }
        }

        comandaRepository.save(comanda);
        log.info("Comanda generada automáticamente para cocina en estado 'en_cola' con ID comanda: {}", comanda.getId());
    }

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

    public double calcularTotal(List<ItemVentaDTO> items) {
        double subtotal = items.stream().mapToDouble(ItemVentaDTO::getSubtotal).sum();
        return descuento.aplicar(subtotal);
    }

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

    public ResumenVentasDTO obtenerResumen() throws DataAccessException {
        return ventaRepository.getResumen();
    }

    public List<VentaDTO> obtenerHistorial() throws DataAccessException {
        return ventaRepository.findAll();
    }
}
