package service;

import exception.OperacionNoPermitidaException;
import exception.RecursoNoEncontradoException;
import exception.ValidacionException;
import model.Usuario;
import repository.IUsuarioRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Usuarios y autenticación
 * Aplica SRP - única responsabilidad: gestión de usuarios y autenticación
 */
public class UsuarioService implements IUsuarioService {
    private final IUsuarioRepository usuarioRepository;

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new ValidacionException("El usuario no puede ser nulo.");
        }

        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isBlank()) {
            throw new ValidacionException("El nombre de usuario es obligatorio.");
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            throw new ValidacionException("La contraseña es obligatoria.");
        }

        if (usuarioRepository.existe(usuario.getNombreUsuario())) {
            throw new OperacionNoPermitidaException("Ya existe un usuario con el nombre " + usuario.getNombreUsuario() + ".");
        }

        usuarioRepository.guardar(usuario);
        return true;
    }

    @Override
    public Optional<Usuario> autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ValidacionException("El nombre de usuario es obligatorio para autenticarse.");
        }
        if (contrasena == null || contrasena.isBlank()) {
            throw new ValidacionException("La contraseña es obligatoria para autenticarse.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorNombreUsuario(nombreUsuario);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getContrasena().equals(contrasena)) {
            return usuarioOpt;
        }

        throw new RecursoNoEncontradoException("Credenciales inválidas.");
    }

    @Override
    public Optional<Usuario> buscarUsuario(String nombreUsuario) {
        return usuarioRepository.buscarPorNombreUsuario(nombreUsuario);
    }

    @Override
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.listarTodos();
    }

    @Override
    public boolean eliminarUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ValidacionException("El nombre de usuario es obligatorio para eliminar un usuario.");
        }
        if (!usuarioRepository.existe(nombreUsuario)) {
            throw new RecursoNoEncontradoException("No se encontró un usuario con nombre " + nombreUsuario + ".");
        }
        return usuarioRepository.eliminar(nombreUsuario);
    }
}

