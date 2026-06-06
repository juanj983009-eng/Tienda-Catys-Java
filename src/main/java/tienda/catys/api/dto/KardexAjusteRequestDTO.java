package tienda.catys.api.dto;

import java.math.BigDecimal;

public record KardexAjusteRequestDTO(
    Integer idInsumo,
    BigDecimal cantidad,
    String tipoMovimiento,
    String motivo
) {}
