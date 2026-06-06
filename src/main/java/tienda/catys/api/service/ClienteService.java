package tienda.catys.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.catys.api.dto.ClienteRequestDTO;
import tienda.catys.api.dto.ClienteResponseDTO;
import tienda.catys.api.modelo.Cliente;
import tienda.catys.api.repository.ClienteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarYBuscar(String buscar) {
        List<Cliente> clientes;
        if (buscar != null && !buscar.trim().isEmpty()) {
            clientes = clienteRepository.findByNombreContainingIgnoreCaseOrDniContaining(buscar.trim(), buscar.trim());
        } else {
            clientes = clienteRepository.findAll();
        }
        return clientes.stream()
                .map(this::mapearAClienteResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO request) {
        Cliente cliente = new Cliente(request.dni(), request.nombre(), request.telefono());
        Cliente guardado = clienteRepository.save(cliente);
        return mapearAClienteResponseDTO(guardado);
    }

    @Transactional
    public ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        cliente.setDni(request.dni());
        cliente.setNombre(request.nombre());
        cliente.setTelefono(request.telefono());
        Cliente guardado = clienteRepository.save(cliente);
        return mapearAClienteResponseDTO(guardado);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteResponseDTO mapearAClienteResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getVisitas()
        );
    }
}
