package tienda.catys.api.dto;

import java.math.BigDecimal;

public record InsumoResponseDTO(
    Integer id,
    String nombre,
    BigDecimal stockActual,
    BigDecimal stockMinimo,
    String unidad,
    String categoria
) {}
