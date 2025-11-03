package service;

import model.Usuario;
import java.util.Optional;

/**
 * Interface para servicio de Usuarios
 */
public interface IUsuarioService {
    boolean registrarUsuario(Usuario usuario);
    Optional<Usuario> autenticar(String nombreUsuario, String contrasena);
    Optional<Usuario> buscarUsuario(String nombreUsuario);
    boolean eliminarUsuario(String nombreUsuario);
}

