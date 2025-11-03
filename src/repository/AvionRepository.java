package repository;

import model.Avion;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de repositorio de Aviones en memoria
 */
public class AvionRepository implements IAvionRepository {
    private final Map<String, Avion> aviones = new HashMap<>();

    @Override
    public void guardar(Avion avion) {
        aviones.put(avion.getNumeroSerie(), avion);
    }

    @Override
    public Optional<Avion> buscarPorNumeroSerie(String numeroSerie) {
        return Optional.ofNullable(aviones.get(numeroSerie));
    }

    @Override
    public List<Avion> listarTodos() {
        return new ArrayList<>(aviones.values());
    }

    @Override
    public List<Avion> buscarPorEstado(String estado) {
        return aviones.values().stream()
                .filter(a -> a.getEstado().toString().equalsIgnoreCase(estado))
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminar(String numeroSerie) {
        return aviones.remove(numeroSerie) != null;
    }

    @Override
    public boolean existe(String numeroSerie) {
        return aviones.containsKey(numeroSerie);
    }
}

