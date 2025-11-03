package repository;

import model.Avion;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Aviones
 * Aplica ISP - interfaz específica para aviones
 */
public interface IAvionRepository {
    void guardar(Avion avion);
    Optional<Avion> buscarPorNumeroSerie(String numeroSerie);
    List<Avion> listarTodos();
    List<Avion> buscarPorEstado(String estado);
    boolean eliminar(String numeroSerie);
    boolean existe(String numeroSerie);
}

