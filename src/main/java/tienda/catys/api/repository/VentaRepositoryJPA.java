package tienda.catys.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepositoryJPA extends JpaRepository<Venta, Integer> {

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha >= :startDate AND v.fecha <= :endDate")
    Optional<Double> sumTotalByFechaBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COALESCE(SUM(total), 0) FROM Ventas WHERE CAST(fecha AS DATE) = CAST(GETDATE() AS DATE)", nativeQuery = true)
    BigDecimal obtenerVentasDeHoy();

    @Query(value = "SELECT COUNT(*) FROM Ventas WHERE CAST(fecha AS DATE) = CAST(GETDATE() AS DATE)", nativeQuery = true)
    long obtenerTotalPedidosDeHoy();

    @Query(value = "SELECT COALESCE(SUM(total), 0) FROM Ventas WHERE fecha >= DATEADD(day, -7, GETDATE())", nativeQuery = true)
    BigDecimal obtenerVentasDeLaSemana();

    @Query(value = "SELECT COALESCE(SUM(total), 0) FROM Ventas WHERE YEAR(fecha) = YEAR(GETDATE())", nativeQuery = true)
    BigDecimal obtenerVentasDelAno();

    @Query(value = "SELECT metodo_pago, COALESCE(SUM(total), 0) FROM Ventas GROUP BY metodo_pago", nativeQuery = true)
    List<Object[]> obtenerDistribucionPagos();

    @Query(value = "SELECT MONTH(fecha) AS mes, COALESCE(SUM(total), 0) AS total FROM Ventas WHERE YEAR(fecha) = 2026 GROUP BY MONTH(fecha) ORDER BY MONTH(fecha)", nativeQuery = true)
    List<Object[]> obtenerProgresionMensual();
}
