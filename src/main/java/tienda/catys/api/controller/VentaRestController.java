package tienda.catys.api.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.catys.api.dto.ConfirmarVentaRequestDTO;
import tienda.catys.api.modelo.MetodoPago;
import tienda.catys.api.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    private final VentaService ventaService;

    public VentaRestController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Map<String, String>> confirmar(@RequestBody ConfirmarVentaRequestDTO request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El cuerpo de la solicitud no puede estar vacío."));
            }

            MetodoPago metodo = request.metodoPago();
            if (metodo == null) {
                metodo = MetodoPago.EFECTIVO;
            }

            ventaService.procesarVenta(request.cliente(), request.clienteId(), request.idMesa(), metodo, request.items());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "mensaje", "Venta procesada exitosamente."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", "error",
                            "error", e.getMessage()
                    ));
        }
    }
}
