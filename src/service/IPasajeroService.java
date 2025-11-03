package service;

import model.Pasajero;
import java.util.List;
import java.util.Optional;

/**
 * Interface para servicio de Pasajeros
 */
public interface IPasajeroService {
    boolean registrarPasajero(Pasajero pasajero);
    Optional<Pasajero> buscarPasajeroPorDni(String dni);
    List<Pasajero> listarTodosLosPasajeros();
    boolean eliminarPasajero(String dni);
}

