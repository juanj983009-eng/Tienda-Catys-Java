package service;

import modelo.Cliente;
import repository.ClienteRepository;
import repository.exception.DataAccessException;
import service.exception.ValidacionException;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para el módulo de Clientes.
 */
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Obtiene todos los clientes registrados.
     *
     * @return lista de Cliente (puede ser vacía, nunca null)
     * @throws DataAccessException si hay error de BD
     */
    public List<Cliente> getTodos() throws DataAccessException {
        return clienteRepository.findAll();
    }

    /**
     * Busca un cliente por DNI.
     *
     * @param dni el DNI a buscar
     * @return Optional<Cliente>
     * @throws DataAccessException si hay error de BD
     */
    public Optional<Cliente> buscarPorDni(String dni) throws DataAccessException {
        if (dni == null || dni.trim().isEmpty()) return Optional.empty();
        return clienteRepository.findByDni(dni.trim());
    }

    /**
     * Registra un nuevo cliente con validación.
     *
     * @param c cliente a registrar
     * @throws ValidacionException si los datos son inválidos
     * @throws DataAccessException si hay error de BD o DNI duplicado
     */
    public void registrar(Cliente c) throws ValidacionException, DataAccessException {
        validar(c);
        // Verificar si el DNI ya existe
        Optional<Cliente> existente = clienteRepository.findByDni(c.getDni());
        if (existente.isPresent()) {
            throw new ValidacionException("dni", "Ya existe un cliente registrado con el DNI: " + c.getDni());
        }
        clienteRepository.save(c);
    }

    /**
     * Elimina un cliente por su ID.
     *
     * @param id ID del cliente
     * @throws DataAccessException si hay error de BD
     */
    public void eliminar(int id) throws DataAccessException {
        clienteRepository.deleteById(id);
    }

    private void validar(Cliente c) throws ValidacionException {
        if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
            throw new ValidacionException("nombre", "El nombre del cliente no puede estar vacío.");
        }
        if (c.getDni() == null || c.getDni().trim().isEmpty()) {
            throw new ValidacionException("dni", "El DNI no puede estar vacío.");
        }
        if (!c.getDni().matches("\\d{8}")) {
            throw new ValidacionException("dni", "El DNI debe contener exactamente 8 dígitos numéricos.");
        }
    }
}
