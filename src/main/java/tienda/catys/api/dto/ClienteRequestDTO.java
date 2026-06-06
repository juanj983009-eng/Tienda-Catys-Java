package tienda.catys.api.dto;

public record ClienteRequestDTO(
    String dni,
    String nombre,
    String telefono
) {}
