package tienda.catys.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.Insumo;
import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Integer> {
    List<Insumo> findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String nombre, String categoria);
}
