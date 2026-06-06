package tienda.catys.api.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.catys.api.modelo.Producto;
import tienda.catys.api.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoService productoService;

    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping({"", "/buscar"})
    public ResponseEntity<List<Producto>> buscar(
            @RequestParam(value = "filtro", required = false) String filtro,
            @RequestParam(value = "categoria", required = false) String categoria) {
        try {
            List<Producto> productos;
            
            if (categoria != null && !categoria.trim().isEmpty()) {
                productos = productoService.getByCategoria(categoria.trim());
                if (filtro != null && !filtro.trim().isEmpty()) {
                    String cleanFiltro = filtro.trim().toLowerCase();
                    productos = productos.stream()
                            .filter(p -> p.getNombre().toLowerCase().contains(cleanFiltro))
                            .collect(Collectors.toList());
                }
            } else if (filtro != null && !filtro.trim().isEmpty()) {
                productos = productoService.buscar(filtro.trim());
            } else {
                productos = productoService.getTodos();
            }
            
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
