package dto;

import java.time.LocalDateTime;
import modelo.MetodoPago;

/**
 * DTO para representar una Venta completa (devuelta por el Repository).
 * La capa de UI lee este objeto — nunca accede directamente a la BD.
 */
public record VentaDTO(
    int id,
    LocalDateTime fecha,
    String clienteNombre,
    MetodoPago metodoPago,
    double total,
    String detalle
) {}
