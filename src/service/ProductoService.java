package service;

import modelo.Producto;
import repository.ProductoRepository;
import repository.exception.DataAccessException;
import service.exception.ValidacionException;
import util.AppLogger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para el módulo de Productos.
 *
 * Centraliza la lógica de validación que antes estaba dispersa en la UI.
 * La UI (TiendaGUI, GestionProductosFrame) solo llama métodos de este servicio.
 */
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Carga todos los productos agrupados por categoría.
     * La UI recibe un Map listo para renderizar — sin lógica de agrupación en los frames.
     *
     * @return Map<categoría, List<Producto>>
     * @throws DataAccessException si hay error de BD
     */
    public Map<String, List<Producto>> getProductosPorCategoria() throws DataAccessException {
        List<Producto> todos = productoRepository.findAll();
        return todos.stream().collect(Collectors.groupingBy(Producto::getCategoria));
    }

    /**
     * Carga los productos de una categoría específica.
     *
     * @param categoria nombre exacto de la categoría
     * @return lista de Producto disponibles en esa categoría
     * @throws DataAccessException si hay error de BD
     */
    public List<Producto> getByCategoria(String categoria) throws DataAccessException {
        return productoRepository.findByCategoria(categoria);
    }

    /**
     * Busca productos por texto (nombre o categoría).
     *
     * @param texto texto de búsqueda
     * @return lista de Producto que coinciden
     * @throws DataAccessException si hay error de BD
     */
    public List<Producto> buscar(String texto) throws DataAccessException {
        if (texto == null || texto.trim().isEmpty()) {
            return productoRepository.findAll();
        }
        return productoRepository.findByTexto(texto.trim());
    }

    /**
     * Registra un nuevo producto con validación completa de datos.
     *
     * @param p producto a registrar
     * @throws ValidacionException si los datos son inválidos
     * @throws DataAccessException si hay error de BD
     */
    public void registrar(Producto p) throws ValidacionException, DataAccessException {
        validar(p);
        boolean exito = productoRepository.save(p);
        if (!exito) {
            AppLogger.warn("El registro del producto no produjo cambios en BD: " + p.getNombre());
        }
    }

    /**
     * Actualiza un producto existente con validación de datos.
     *
     * @param p producto con los datos actualizados (debe tener ID válido)
     * @throws ValidacionException si los datos son inválidos
     * @throws DataAccessException si hay error de BD
     */
    public void actualizar(Producto p) throws ValidacionException, DataAccessException {
        if (p.getId() <= 0) {
            throw new ValidacionException("id", "Se requiere un ID de producto válido para actualizar.");
        }
        validar(p);
        productoRepository.update(p);
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto a eliminar
     * @throws DataAccessException si hay error de BD o el producto no existe
     */
    public void eliminar(int id) throws DataAccessException {
        Optional<Producto> existente = productoRepository.findById(id);
        if (existente.isEmpty()) {
            throw new DataAccessException("No se encontró el producto con ID " + id + " para eliminar.");
        }
        productoRepository.deleteById(id);
    }

    /**
     * Obtiene todos los productos para la pantalla de gestión.
     *
     * @return lista completa de Producto
     * @throws DataAccessException si hay error de BD
     */
    public List<Producto> getTodos() throws DataAccessException {
        return productoRepository.findAll();
    }

    // --- Validaciones de negocio ---

    private void validar(Producto p) throws ValidacionException {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre del producto no puede estar vacío.");
        }
        if (p.getNombre().length() > 100) {
            throw new ValidacionException("nombre", "El nombre del producto no puede superar 100 caracteres.");
        }
        if (p.getPrecio() <= 0) {
            throw new ValidacionException("precio", "El precio debe ser mayor a 0.");
        }
        if (p.getStock() < 0) {
            throw new ValidacionException("stock", "El stock no puede ser negativo.");
        }
        if (p.getCategoria() == null || p.getCategoria().trim().isEmpty()) {
            throw new ValidacionException("categoria", "Debe seleccionar una categoría.");
        }
    }
}
