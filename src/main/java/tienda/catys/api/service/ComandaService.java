package tienda.catys.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.catys.api.dto.ComandaItemDTO;
import tienda.catys.api.dto.ComandaResponseDTO;
import tienda.catys.api.modelo.Comanda;
import tienda.catys.api.repository.ComandaRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ComandaService(ComandaRepository comandaRepository) {
        this.comandaRepository = comandaRepository;
    }

    @Transactional(readOnly = true)
    public List<ComandaResponseDTO> obtenerComandasActivas() {
        // Obtenemos todas las comandas cuyo estado no sea "entregado"
        List<Comanda> comandas = comandaRepository.findByEstadoNot("entregado");
        return comandas.stream()
                .map(this::mapearAComandaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComandaResponseDTO actualizarEstado(Integer id, String nuevoEstado) {
        if (id == null || nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la comanda y el nuevo estado no pueden ser nulos o vacíos.");
        }

        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comanda no encontrada con ID: " + id));

        // Normalizamos el estado a minúsculas para coincidir con la lógica del frontend (en_cola, preparacion, listo, entregado)
        comanda.setEstado(nuevoEstado.trim().toLowerCase());
        Comanda comandaActualizada = comandaRepository.save(comanda);

        return mapearAComandaResponseDTO(comandaActualizada);
    }

    private ComandaResponseDTO mapearAComandaResponseDTO(Comanda comanda) {
        String horaFormateada = "";
        long haceMinutos = 0;

        if (comanda.getFechaHora() != null) {
            horaFormateada = comanda.getFechaHora().format(TIME_FORMATTER);
            haceMinutos = Duration.between(comanda.getFechaHora(), LocalDateTime.now()).toMinutes();
            // Evitar valores negativos si hay discrepancias de reloj menores
            if (haceMinutos < 0) {
                haceMinutos = 0;
            }
        }

        List<ComandaItemDTO> itemsDTO = comanda.getItems().stream()
                .map(item -> new ComandaItemDTO(
                        item.getCantidad(),
                        item.getProducto() != null ? item.getProducto().getNombre() : "Producto Desconocido"
                ))
                .collect(Collectors.toList());

        return new ComandaResponseDTO(
                comanda.getId(),
                comanda.getClienteNombre(),
                horaFormateada,
                haceMinutos,
                itemsDTO,
                comanda.getEstado()
        );
    }
}
