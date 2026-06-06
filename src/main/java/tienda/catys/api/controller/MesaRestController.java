package tienda.catys.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.catys.api.dto.MesaEstadoRequestDTO;
import tienda.catys.api.dto.MesaResponseDTO;
import tienda.catys.api.service.MesaService;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaRestController {

    private final MesaService mesaService;

    public MesaRestController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listar(@RequestParam(required = false) String zona) {
        return ResponseEntity.ok(mesaService.obtenerMesas(zona));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<MesaResponseDTO> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody MesaEstadoRequestDTO request) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, request.estado()));
    }
}
