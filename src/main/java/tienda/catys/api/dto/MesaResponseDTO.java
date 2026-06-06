package tienda.catys.api.dto;

public record MesaResponseDTO(
    Integer id,
    String numero,
    Integer capacidad,
    String estado,
    String zona,
    Double consumo,
    String tiempo
) {}
