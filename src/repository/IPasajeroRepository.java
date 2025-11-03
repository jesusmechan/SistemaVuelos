package repository;

import model.Pasajero;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Pasajeros
 * Aplica ISP - interfaz específica para pasajeros
 */
public interface IPasajeroRepository {
    void guardar(Pasajero pasajero);
    Optional<Pasajero> buscarPorDni(String dni);
    List<Pasajero> listarTodos();
    boolean eliminar(String dni);
    boolean existe(String dni);
}

