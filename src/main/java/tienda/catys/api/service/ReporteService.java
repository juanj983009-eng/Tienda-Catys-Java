package tienda.catys.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.catys.api.dto.ReporteFinancieroDTO;
import tienda.catys.api.dto.ReporteFinancieroDTO.KpisFinancierosDTO;
import tienda.catys.api.dto.ReporteFinancieroDTO.MetodoPagoDistribucionDTO;
import tienda.catys.api.dto.ReporteFinancieroDTO.ProgresoMensualDTO;
import tienda.catys.api.repository.VentaRepositoryJPA;
import tienda.catys.api.repository.exception.DataAccessException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteService {

    private final VentaRepositoryJPA ventaRepositoryJPA;

    public ReporteService(VentaRepositoryJPA ventaRepositoryJPA) {
        this.ventaRepositoryJPA = ventaRepositoryJPA;
    }

    @Transactional(readOnly = true)
    public ReporteFinancieroDTO obtenerReporteFinanciero() throws DataAccessException {
        try {
            // 1. KPIs
            double dia = ventaRepositoryJPA.obtenerVentasDeHoy().doubleValue();
            double semana = ventaRepositoryJPA.obtenerVentasDeLaSemana().doubleValue();
            double ano = ventaRepositoryJPA.obtenerVentasDelAno().doubleValue();
            KpisFinancierosDTO kpis = new KpisFinancierosDTO(dia, semana, ano);

            // 2. Distribución de pagos
            List<Object[]> rowsPagos = ventaRepositoryJPA.obtenerDistribucionPagos();
            double totalPagos = rowsPagos.stream().mapToDouble(row -> ((Number) row[1]).doubleValue()).sum();
            List<MetodoPagoDistribucionDTO> metodosPago = new ArrayList<>();
            for (Object[] row : rowsPagos) {
                String rawMetodo = (String) row[0];
                double monto = ((Number) row[1]).doubleValue();
                int porcentaje = totalPagos > 0 ? (int) Math.round((monto / totalPagos) * 100) : 0;

                String name = rawMetodo;
                if ("EFECTIVO".equalsIgnoreCase(rawMetodo)) name = "Efectivo";
                else if ("TARJETA_CREDITO".equalsIgnoreCase(rawMetodo)) name = "Tarjeta";
                else if ("YAPE_PLIN".equalsIgnoreCase(rawMetodo)) name = "Yape / Plin";

                metodosPago.add(new MetodoPagoDistribucionDTO(name, monto, porcentaje));
            }

            // 3. Progresión mensual
            List<Object[]> rowsMeses = ventaRepositoryJPA.obtenerProgresionMensual();
            double maxMonto = rowsMeses.stream().mapToDouble(row -> ((Number) row[1]).doubleValue()).max().orElse(1.0);
            if (maxMonto == 0) maxMonto = 1.0;

            List<ProgresoMensualDTO> progressionMensual = new ArrayList<>();
            String[] nombresMeses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

            for (Object[] row : rowsMeses) {
                int mesNum = ((Number) row[0]).intValue();
                double monto = ((Number) row[1]).doubleValue();
                int porcentaje = (int) Math.round((monto / maxMonto) * 100);

                String mesNombre = (mesNum >= 1 && mesNum <= 12) ? nombresMeses[mesNum] : String.valueOf(mesNum);
                progressionMensual.add(new ProgresoMensualDTO(mesNombre, monto, porcentaje));
            }

            return new ReporteFinancieroDTO(kpis, metodosPago, progressionMensual);
        } catch (Exception e) {
            throw new DataAccessException("Error al calcular el reporte financiero: " + e.getMessage(), e);
        }
    }
}
