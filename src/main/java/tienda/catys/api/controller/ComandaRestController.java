package tienda.catys.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.catys.api.dto.ComandaEstadoRequestDTO;
import tienda.catys.api.dto.ComandaResponseDTO;
import tienda.catys.api.service.ComandaService;

import java.util.List;

@RestController
@RequestMapping("/api/comandas")
public class ComandaRestController {

    private final ComandaService comandaService;

    public ComandaRestController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping("/activas")
    public ResponseEntity<List<ComandaResponseDTO>> obtenerActivas() {
        return ResponseEntity.ok(comandaService.obtenerComandasActivas());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<ComandaResponseDTO> avanzarEstado(
            @PathVariable Integer id,
            @RequestBody ComandaEstadoRequestDTO request) {
        return ResponseEntity.ok(comandaService.actualizarEstado(id, request.estado()));
    }
}
