package tienda.catys.api.dto;

public record ClienteResponseDTO(
    Integer id,
    String dni,
    String nombre,
    String telefono,
    Integer visitas
) {}
