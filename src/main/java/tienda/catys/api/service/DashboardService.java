package tienda.catys.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.catys.api.dto.DashboardMetricsDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.AlertaStockDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.CocinaMetricasDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.KpisDTO;
import tienda.catys.api.dto.DashboardMetricsDTO.MesasMetricasDTO;
import tienda.catys.api.modelo.Insumo;
import tienda.catys.api.repository.*;
import tienda.catys.api.repository.exception.DataAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final VentaRepositoryJPA ventaRepositoryJPA;
    private final ComandaRepository comandaRepository;
    private final ClienteRepository clienteRepository;
    private final MesaRepository mesaRepository;
    private final InsumoRepository insumoRepository;

    public DashboardService(VentaRepositoryJPA ventaRepositoryJPA,
                            ComandaRepository comandaRepository,
                            ClienteRepository clienteRepository,
                            MesaRepository mesaRepository,
                            InsumoRepository insumoRepository) {
        this.ventaRepositoryJPA = ventaRepositoryJPA;
        this.comandaRepository = comandaRepository;
        this.clienteRepository = clienteRepository;
        this.mesaRepository = mesaRepository;
        this.insumoRepository = insumoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDTO obtenerMetricas() throws DataAccessException {
        try {
            LocalDateTime start = LocalDate.now().atStartOfDay();
            LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

            // 1. KPI Ventas Hoy
            double ventasHoy = ventaRepositoryJPA.obtenerVentasDeHoy().doubleValue();

            // 2. KPI Órdenes Hoy
            int ordenesHoy = (int) ventaRepositoryJPA.obtenerTotalPedidosDeHoy();

            // 3. KPI Clientes VIP totales
            int clientesVip = (int) clienteRepository.count();

            KpisDTO kpis = new KpisDTO(ventasHoy, ordenesHoy, clientesVip);

            // 4. Cocina métricas
            int enCola = (int) comandaRepository.countByEstado("en_cola");
            int enPreparacion = (int) comandaRepository.countByEstado("preparacion");
            CocinaMetricasDTO cocina = new CocinaMetricasDTO(enCola, enPreparacion);

            // 5. Mesas métricas (ocupadas = ocupada + cuenta)
            int ocupadas = (int) (mesaRepository.countByEstado("ocupada") + mesaRepository.countByEstado("cuenta"));
            int disponibles = (int) mesaRepository.countByEstado("disponible");
            MesasMetricasDTO mesas = new MesasMetricasDTO(ocupadas, disponibles);

            // 6. Alertas de stock crítico
            List<Insumo> todosInsumos = insumoRepository.findAll();
            List<AlertaStockDTO> alertasStock = todosInsumos.stream()
                    .filter(ins -> ins.getStockActual() != null && ins.getStockMinimo() != null 
                            && ins.getStockActual().compareTo(ins.getStockMinimo()) < 0)
                    .map(ins -> new AlertaStockDTO(
                            ins.getId(),
                            ins.getNombre(),
                            String.format("%.2f %s", ins.getStockActual(), ins.getUnidad()),
                            String.format("%.2f %s", ins.getStockMinimo(), ins.getUnidad())
                    ))
                    .collect(Collectors.toList());

            return new DashboardMetricsDTO(kpis, cocina, mesas, alertasStock);
        } catch (Exception e) {
            throw new DataAccessException("Error al calcular las métricas del dashboard mediante JPA: " + e.getMessage(), e);
        }
    }
}
