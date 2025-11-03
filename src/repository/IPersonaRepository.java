package repository;

import model.Persona;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Personas
 * Aplica ISP (Interface Segregation Principle) - interfaz específica
 * Aplica DIP (Dependency Inversion Principle) - dependencia de abstracción
 */
public interface IPersonaRepository {
    void guardar(Persona persona);
    Optional<Persona> buscarPorDni(String dni);
    List<Persona> listarTodos();
    boolean eliminar(String dni);
    boolean existe(String dni);
}

