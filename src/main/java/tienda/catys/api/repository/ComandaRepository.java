package tienda.catys.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.Comanda;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComandaRepository extends JpaRepository<Comanda, Integer> {
    List<Comanda> findByEstadoNot(String estado);
    long countByFechaHoraBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByEstado(String estado);
}
