package tienda.catys.api.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tienda.catys.api.modelo.Producto;
import tienda.catys.api.repository.ProductoRepository;
import tienda.catys.api.repository.exception.DataAccessException;
import tienda.catys.api.service.exception.ValidacionException;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Map<String, List<Producto>> getProductosPorCategoria() throws DataAccessException {
        List<Producto> todos = productoRepository.findAll();
        return todos.stream().collect(Collectors.groupingBy(Producto::getCategoria));
    }

    public List<Producto> getByCategoria(String categoria) throws DataAccessException {
        return productoRepository.findByCategoria(categoria);
    }

    public List<Producto> buscar(String texto) throws DataAccessException {
        if (texto == null || texto.trim().isEmpty()) {
            return productoRepository.findAll();
        }
        return productoRepository.findByTexto(texto.trim());
    }

    public void registrar(Producto p) throws ValidacionException, DataAccessException {
        validar(p);
        boolean exito = productoRepository.save(p);
        if (!exito) {
            log.warn("El registro del producto no produjo cambios en BD: {}", p.getNombre());
        }
    }

    public void actualizar(Producto p) throws ValidacionException, DataAccessException {
        if (p.getId() <= 0) {
            throw new ValidacionException("id", "Se requiere un ID de producto válido para actualizar.");
        }
        validar(p);
        productoRepository.update(p);
    }

    public void eliminar(int id) throws DataAccessException {
        Optional<Producto> existente = productoRepository.findById(id);
        if (existente.isEmpty()) {
            throw new DataAccessException("No se encontró el producto con ID " + id + " para eliminar.");
        }
        productoRepository.deleteById(id);
    }

    public List<Producto> getTodos() throws DataAccessException {
        return productoRepository.findAll();
    }

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
