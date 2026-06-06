package tienda.catys.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.KardexMovimiento;

@Repository
public interface KardexMovimientoRepository extends JpaRepository<KardexMovimiento, Integer> {
}
