package tienda.catys.api.dto;

import java.util.List;

public record ComandaResponseDTO(
    Integer id,
    String cliente,
    String hora,
    Long haceMinutos,
    List<ComandaItemDTO> items,
    String estado
) {}
