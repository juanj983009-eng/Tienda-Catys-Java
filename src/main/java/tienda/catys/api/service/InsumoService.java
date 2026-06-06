package tienda.catys.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.catys.api.dto.InsumoResponseDTO;
import tienda.catys.api.dto.KardexAjusteRequestDTO;
import tienda.catys.api.modelo.Insumo;
import tienda.catys.api.modelo.KardexMovimiento;
import tienda.catys.api.repository.InsumoRepository;
import tienda.catys.api.repository.KardexMovimientoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final KardexMovimientoRepository kardexMovimientoRepository;

    public InsumoService(InsumoRepository insumoRepository, KardexMovimientoRepository kardexMovimientoRepository) {
        this.insumoRepository = insumoRepository;
        this.kardexMovimientoRepository = kardexMovimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listarYFiltrar(String buscar, String categoria) {
        List<Insumo> insumos;
        if (buscar != null && !buscar.trim().isEmpty()) {
            insumos = insumoRepository.findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(buscar.trim(), buscar.trim());
        } else {
            insumos = insumoRepository.findAll();
        }

        return insumos.stream()
                .filter(ins -> categoria == null || "TODOS".equalsIgnoreCase(categoria.trim()) || ins.getCategoria().equalsIgnoreCase(categoria.trim()))
                .map(ins -> new InsumoResponseDTO(
                        ins.getId(),
                        ins.getNombre(),
                        ins.getStockActual(),
                        ins.getStockMinimo(),
                        ins.getUnidad(),
                        ins.getCategoria()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void registrarAjuste(KardexAjusteRequestDTO dto) {
        if (dto == null || dto.idInsumo() == null) {
            throw new IllegalArgumentException("El DTO de ajuste o el ID del insumo no pueden ser nulos.");
        }

        Insumo insumo = insumoRepository.findById(dto.idInsumo())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + dto.idInsumo()));

        BigDecimal stockActual = insumo.getStockActual();
        if (stockActual == null) {
            stockActual = BigDecimal.ZERO;
        }

        BigDecimal nuevaCantidad = dto.cantidad();
        if (nuevaCantidad == null || nuevaCantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a ajustar debe ser mayor que cero.");
        }

        String tipo = dto.tipoMovimiento();
        if ("INGRESO".equalsIgnoreCase(tipo)) {
            insumo.setStockActual(stockActual.add(nuevaCantidad));
        } else if ("MERMA".equalsIgnoreCase(tipo)) {
            if (stockActual.compareTo(nuevaCantidad) < 0) {
                throw new IllegalArgumentException("El stock actual (" + stockActual + ") es menor que la cantidad de merma (" + nuevaCantidad + ").");
            }
            insumo.setStockActual(stockActual.subtract(nuevaCantidad));
        } else {
            throw new IllegalArgumentException("Tipo de movimiento no soportado: " + tipo + ". Debe ser INGRESO o MERMA.");
        }

        insumoRepository.save(insumo);

        KardexMovimiento movimiento = new KardexMovimiento();
        movimiento.setInsumo(insumo);
        movimiento.setTipoMovimiento(tipo.toUpperCase());
        movimiento.setCantidad(nuevaCantidad);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setMotivo(dto.motivo());

        kardexMovimientoRepository.save(movimiento);
    }
}
