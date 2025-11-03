package service;

import model.Usuario;
import repository.IUsuarioRepository;
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
            return false;
        }
        
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isEmpty()) {
            return false;
        }
        
        if (usuarioRepository.existe(usuario.getNombreUsuario())) {
            return false; // El usuario ya existe
        }
        
        usuarioRepository.guardar(usuario);
        return true;
    }

    @Override
    public Optional<Usuario> autenticar(String nombreUsuario, String contrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorNombreUsuario(nombreUsuario);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getContrasena().equals(contrasena)) {
                return Optional.of(usuario);
            }
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarUsuario(String nombreUsuario) {
        return usuarioRepository.buscarPorNombreUsuario(nombreUsuario);
    }

    @Override
    public boolean eliminarUsuario(String nombreUsuario) {
        return usuarioRepository.eliminar(nombreUsuario);
    }
}

