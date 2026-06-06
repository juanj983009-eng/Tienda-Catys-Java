package tienda.catys.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.catys.api.dto.MesaResponseDTO;
import tienda.catys.api.modelo.Comanda;
import tienda.catys.api.modelo.ComandaItem;
import tienda.catys.api.modelo.Mesa;
import tienda.catys.api.repository.ComandaRepository;
import tienda.catys.api.repository.MesaRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final ComandaRepository comandaRepository;

    public MesaService(MesaRepository mesaRepository, ComandaRepository comandaRepository) {
        this.mesaRepository = mesaRepository;
        this.comandaRepository = comandaRepository;
    }

    @Transactional(readOnly = true)
    public List<MesaResponseDTO> obtenerMesas(String zona) {
        List<Mesa> mesas;
        if (zona != null && !zona.trim().isEmpty() && !"TODAS".equalsIgnoreCase(zona.trim())) {
            mesas = mesaRepository.findByZonaIgnoreCase(zona.trim());
        } else {
            mesas = mesaRepository.findAll();
        }

        // Recuperar comandas activas para asociar consumo y tiempos
        List<Comanda> comandasActivas = comandaRepository.findByEstadoNot("entregado");

        return mesas.stream()
                .map(m -> mapearAMesaResponseDTO(m, comandasActivas))
                .collect(Collectors.toList());
    }

    @Transactional
    public MesaResponseDTO cambiarEstado(Integer id, String nuevoEstado) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        mesa.setEstado(nuevoEstado.trim().toLowerCase());
        Mesa guardada = mesaRepository.save(mesa);

        List<Comanda> comandasActivas = comandaRepository.findByEstadoNot("entregado");
        return mapearAMesaResponseDTO(guardada, comandasActivas);
    }

    private MesaResponseDTO mapearAMesaResponseDTO(Mesa mesa, List<Comanda> comandasActivas) {
        // Buscar si hay una comanda activa para esta mesa
        Comanda comandaActiva = comandasActivas.stream()
                .filter(c -> c.getMesa() != null && c.getMesa().getId().equals(mesa.getId()))
                .findFirst()
                .orElse(null);

        Double consumo = 0.0;
        String tiempo = null;

        if (comandaActiva != null) {
            consumo = comandaActiva.getItems().stream()
                    .mapToDouble(item -> {
                        double precio = item.getProducto() != null ? item.getProducto().getPrecio() : 0.0;
                        return item.getCantidad() * precio;
                    })
                    .sum();

            if (comandaActiva.getFechaHora() != null) {
                long minutos = Duration.between(comandaActiva.getFechaHora(), LocalDateTime.now()).toMinutes();
                if (minutos < 0) minutos = 0;
                tiempo = minutos + " min";
            }
        }

        return new MesaResponseDTO(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getEstado(),
                mesa.getZona(),
                consumo > 0 ? consumo : null, // Muestra consumo nulo si es 0
                tiempo
        );
    }
}
