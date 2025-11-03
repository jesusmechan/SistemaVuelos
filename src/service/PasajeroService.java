package service;

import model.Pasajero;
import repository.IPasajeroRepository;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Pasajeros
 * Aplica SRP - única responsabilidad: gestión de pasajeros
 */
public class PasajeroService implements IPasajeroService {
    private final IPasajeroRepository pasajeroRepository;

    public PasajeroService(IPasajeroRepository pasajeroRepository) {
        this.pasajeroRepository = pasajeroRepository;
    }

    @Override
    public boolean registrarPasajero(Pasajero pasajero) {
        if (pasajero == null) {
            return false;
        }
        
        if (pasajero.getDni() == null || pasajero.getDni().isEmpty()) {
            return false;
        }
        
        if (pasajeroRepository.existe(pasajero.getDni())) {
            return false; // Ya existe un pasajero con ese DNI
        }
        
        pasajeroRepository.guardar(pasajero);
        return true;
    }

    @Override
    public Optional<Pasajero> buscarPasajeroPorDni(String dni) {
        return pasajeroRepository.buscarPorDni(dni);
    }

    @Override
    public List<Pasajero> listarTodosLosPasajeros() {
        return pasajeroRepository.listarTodos();
    }

    @Override
    public boolean eliminarPasajero(String dni) {
        return pasajeroRepository.eliminar(dni);
    }
}

