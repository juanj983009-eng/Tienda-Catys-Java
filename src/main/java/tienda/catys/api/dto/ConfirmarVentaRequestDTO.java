package tienda.catys.api.dto;

import java.util.List;
import tienda.catys.api.modelo.MetodoPago;

public record ConfirmarVentaRequestDTO(
    String cliente,
    Integer clienteId,
    Integer idMesa,
    MetodoPago metodoPago,
    List<ItemVentaDTO> items
) {}
