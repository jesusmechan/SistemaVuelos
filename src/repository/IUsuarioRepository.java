package repository;

import model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Usuarios
 * Aplica ISP - interfaz específica para usuarios
 */
public interface IUsuarioRepository {
    void guardar(Usuario usuario);
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
    List<Usuario> listarTodos();
    boolean eliminar(String nombreUsuario);
    boolean existe(String nombreUsuario);
}

