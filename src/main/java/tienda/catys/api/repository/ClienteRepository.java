package tienda.catys.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.catys.api.modelo.Cliente;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findByNombreContainingIgnoreCaseOrDniContaining(String nombre, String dni);
}
