package service;

import exception.OperacionNoPermitidaException;
import exception.RecursoNoEncontradoException;
import exception.ValidacionException;
import model.Avion;
import model.EstadoAvion;
import repository.IAvionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Aviones
 * Aplica SRP - única responsabilidad: gestión de aviones
 */
public class AvionService implements IAvionService {
    private final IAvionRepository avionRepository;

    public AvionService(IAvionRepository avionRepository) {
        this.avionRepository = avionRepository;
    }

    @Override
    public boolean registrarAvion(Avion avion) {
        if (avion == null) {
            throw new ValidacionException("El avión no puede ser nulo.");
        }

        if (avion.getNumeroSerie() == null || avion.getNumeroSerie().isBlank()) {
            throw new ValidacionException("El número de serie del avión es obligatorio.");
        }

        if (avionRepository.existe(avion.getNumeroSerie())) {
            throw new OperacionNoPermitidaException("Ya existe un avión con el número de serie " + avion.getNumeroSerie() + ".");
        }

        avionRepository.guardar(avion);
        return true;
    }

    @Override
    public Optional<Avion> buscarAvionPorNumeroSerie(String numeroSerie) {
        return avionRepository.buscarPorNumeroSerie(numeroSerie);
    }

    @Override
    public List<Avion> listarTodosLosAviones() {
        return avionRepository.listarTodos();
    }

    @Override
    public List<Avion> buscarAvionesDisponibles() {
        return avionRepository.listarTodos().stream()
                .filter(a -> a.getEstado() == EstadoAvion.DISPONIBLE)
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminarAvion(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            throw new ValidacionException("El número de serie es obligatorio para eliminar un avión.");
        }
        if (!avionRepository.existe(numeroSerie)) {
            throw new RecursoNoEncontradoException("No se encontró un avión con número de serie " + numeroSerie + ".");
        }
        return avionRepository.eliminar(numeroSerie);
    }
}

