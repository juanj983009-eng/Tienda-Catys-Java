package tienda.catys.api.dto;

import java.time.LocalDateTime;
import tienda.catys.api.modelo.MetodoPago;

public record VentaDTO(
    int id,
    LocalDateTime fecha,
    String clienteNombre,
    MetodoPago metodoPago,
    double total,
    String detalle
) {}
