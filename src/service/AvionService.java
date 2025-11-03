package service;

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
            return false;
        }
        
        if (avion.getNumeroSerie() == null || avion.getNumeroSerie().isEmpty()) {
            return false;
        }
        
        if (avionRepository.existe(avion.getNumeroSerie())) {
            return false; // Ya existe un avión con ese número de serie
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
        return avionRepository.eliminar(numeroSerie);
    }
}

