package service;

import modelo.Usuario;
import repository.UsuarioRepository;
import repository.exception.DataAccessException;
import service.exception.ValidacionException;
import util.AppLogger;

import java.util.Optional;

/**
 * Servicio de autenticación y gestión de usuarios del sistema (backoffice).
 *
 * Responsabilidades:
 *   - Validar las credenciales y autenticar el acceso al sistema
 *   - Validar los datos antes de crear un nuevo usuario
 *   - La UI nunca llama al UsuarioRepository directamente
 */
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Autentica un usuario con username y contraseña.
     *
     * @param username nombre de usuario
     * @param passPlano contraseña en texto plano
     * @return Optional<Usuario> — vacío si las credenciales son incorrectas
     * @throws DataAccessException si hay un error de BD
     */
    public Optional<Usuario> autenticar(String username, String passPlano) throws DataAccessException {
        if (username == null || username.trim().isEmpty()) {
            AppLogger.warn("Intento de login con username vacío.");
            return Optional.empty();
        }
        if (passPlano == null || passPlano.isEmpty()) {
            AppLogger.warn("Intento de login con contraseña vacía para: " + username);
            return Optional.empty();
        }
        return usuarioRepository.findByCredenciales(username.trim(), passPlano);
    }

    /**
     * Registra un nuevo usuario del sistema con validaciones.
     *
     * @param u datos del usuario a registrar
     * @param passPlano contraseña en texto plano (se hashea internamente)
     * @throws ValidacionException si los datos son inválidos
     * @throws DataAccessException si hay error de BD
     */
    public void registrar(Usuario u, String passPlano) throws ValidacionException, DataAccessException {
        validar(u, passPlano);
        usuarioRepository.save(u, passPlano);
    }

    private void validar(Usuario u, String passPlano) throws ValidacionException {
        if (u.getUsername() == null || u.getUsername().trim().isEmpty()) {
            throw new ValidacionException("username", "El nombre de usuario no puede estar vacío.");
        }
        if (u.getUsername().length() < 3 || u.getUsername().length() > 50) {
            throw new ValidacionException("username", "El usuario debe tener entre 3 y 50 caracteres.");
        }
        if (u.getNombreCompleto() == null || u.getNombreCompleto().trim().isEmpty()) {
            throw new ValidacionException("nombreCompleto", "El nombre completo no puede estar vacío.");
        }
        if (passPlano == null || passPlano.length() < 6) {
            throw new ValidacionException("password", "La contraseña debe tener al menos 6 caracteres.");
        }
        if (u.getRol() == null || u.getRol().trim().isEmpty()) {
            throw new ValidacionException("rol", "El rol del usuario no puede estar vacío.");
        }
    }
}
