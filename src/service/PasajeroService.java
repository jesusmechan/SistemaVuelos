package service;

import exception.OperacionNoPermitidaException;
import exception.RecursoNoEncontradoException;
import exception.ValidacionException;
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
            throw new ValidacionException("El pasajero no puede ser nulo.");
        }

        if (pasajero.getDni() == null || pasajero.getDni().isBlank()) {
            throw new ValidacionException("El DNI del pasajero es obligatorio.");
        }

        if (pasajeroRepository.existe(pasajero.getDni())) {
            throw new OperacionNoPermitidaException("Ya existe un pasajero con el DNI " + pasajero.getDni() + ".");
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
        if (dni == null || dni.isBlank()) {
            throw new ValidacionException("El DNI es obligatorio para eliminar un pasajero.");
        }
        if (!pasajeroRepository.existe(dni)) {
            throw new RecursoNoEncontradoException("No se encontró un pasajero con DNI " + dni + ".");
        }
        return pasajeroRepository.eliminar(dni);
    }
}

