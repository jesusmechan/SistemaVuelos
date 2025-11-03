package repository;

import model.Vuelo;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de repositorio de Vuelos en memoria
 */
public class VueloRepository implements IVueloRepository {
    private final Map<String, Vuelo> vuelos = new HashMap<>();

    @Override
    public void guardar(Vuelo vuelo) {
        vuelos.put(vuelo.getNumeroVuelo(), vuelo);
    }

    @Override
    public Optional<Vuelo> buscarPorNumeroVuelo(String numeroVuelo) {
        return Optional.ofNullable(vuelos.get(numeroVuelo));
    }

    @Override
    public List<Vuelo> listarTodos() {
        return new ArrayList<>(vuelos.values());
    }

    @Override
    public List<Vuelo> buscarPorOrigen(String origen) {
        return vuelos.values().stream()
                .filter(v -> v.getOrigen().equalsIgnoreCase(origen))
                .collect(Collectors.toList());
    }

    @Override
    public List<Vuelo> buscarPorDestino(String destino) {
        return vuelos.values().stream()
                .filter(v -> v.getDestino().equalsIgnoreCase(destino))
                .collect(Collectors.toList());
    }

    @Override
    public List<Vuelo> buscarPorOrigenYDestino(String origen, String destino) {
        return vuelos.values().stream()
                .filter(v -> v.getOrigen().equalsIgnoreCase(origen) 
                          && v.getDestino().equalsIgnoreCase(destino))
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminar(String numeroVuelo) {
        return vuelos.remove(numeroVuelo) != null;
    }

    @Override
    public boolean existe(String numeroVuelo) {
        return vuelos.containsKey(numeroVuelo);
    }
}

