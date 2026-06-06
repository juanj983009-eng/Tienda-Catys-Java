package tienda.catys.api.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tienda.catys.api.dto.DashboardMetricsDTO;
import tienda.catys.api.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;

    public DashboardRestController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen() {
        try {
            DashboardMetricsDTO metricas = dashboardService.obtenerMetricas();
            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "error", "No se pudieron recuperar las métricas del dashboard: " + e.getMessage()
                    ));
        }
    }
}
