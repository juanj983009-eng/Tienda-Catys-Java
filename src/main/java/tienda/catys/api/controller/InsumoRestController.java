package tienda.catys.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.catys.api.dto.InsumoResponseDTO;
import tienda.catys.api.dto.KardexAjusteRequestDTO;
import tienda.catys.api.service.InsumoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insumos")
public class InsumoRestController {

    private final InsumoService insumoService;

    public InsumoRestController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @GetMapping
    public ResponseEntity<List<InsumoResponseDTO>> listarYFiltrar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(insumoService.listarYFiltrar(buscar, categoria));
    }

    @PostMapping("/kardex")
    public ResponseEntity<?> registrarMovimiento(@RequestBody KardexAjusteRequestDTO request) {
        insumoService.registrarAjuste(request);
        return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "mensaje", "Kardex actualizado correctamente"
        ));
    }
}
