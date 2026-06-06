package tienda.catys.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.Mesa;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {
    List<Mesa> findByZonaIgnoreCase(String zona);
    long countByEstado(String estado);
}
