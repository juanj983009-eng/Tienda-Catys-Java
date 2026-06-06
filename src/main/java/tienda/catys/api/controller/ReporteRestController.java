package tienda.catys.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.catys.api.dto.ReporteFinancieroDTO;
import tienda.catys.api.service.ReporteService;

import java.util.Map;

@RestController
@RequestMapping("/api/financiero")
public class ReporteRestController {

    private final ReporteService reporteService;

    public ReporteRestController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/reporte")
    public ResponseEntity<?> obtenerReporte() {
        try {
            ReporteFinancieroDTO reporte = reporteService.obtenerReporteFinanciero();
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "error", "No se pudo recuperar el reporte financiero: " + e.getMessage()
                    ));
        }
    }
}
