package service;

import model.Avion;
import java.util.List;
import java.util.Optional;

/**
 * Interface para servicio de Aviones
 */
public interface IAvionService {
    boolean registrarAvion(Avion avion);
    Optional<Avion> buscarAvionPorNumeroSerie(String numeroSerie);
    List<Avion> listarTodosLosAviones();
    List<Avion> buscarAvionesDisponibles();
    boolean eliminarAvion(String numeroSerie);
}

