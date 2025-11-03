package repository;

import model.Usuario;
import java.util.*;

/**
 * Implementación de repositorio de Usuarios en memoria
 */
public class UsuarioRepository implements IUsuarioRepository {
    private final Map<String, Usuario> usuarios = new HashMap<>();

    @Override
    public void guardar(Usuario usuario) {
        usuarios.put(usuario.getNombreUsuario(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return Optional.ofNullable(usuarios.get(nombreUsuario));
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }

    @Override
    public boolean eliminar(String nombreUsuario) {
        return usuarios.remove(nombreUsuario) != null;
    }

    @Override
    public boolean existe(String nombreUsuario) {
        return usuarios.containsKey(nombreUsuario);
    }
}

