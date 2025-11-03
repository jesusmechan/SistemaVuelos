package repository;

import model.Pasajero;
import java.util.*;

/**
 * Implementación de repositorio de Pasajeros en memoria
 */
public class PasajeroRepository implements IPasajeroRepository {
    private final Map<String, Pasajero> pasajeros = new HashMap<>();

    @Override
    public void guardar(Pasajero pasajero) {
        pasajeros.put(pasajero.getDni(), pasajero);
    }

    @Override
    public Optional<Pasajero> buscarPorDni(String dni) {
        return Optional.ofNullable(pasajeros.get(dni));
    }

    @Override
    public List<Pasajero> listarTodos() {
        return new ArrayList<>(pasajeros.values());
    }

    @Override
    public boolean eliminar(String dni) {
        return pasajeros.remove(dni) != null;
    }

    @Override
    public boolean existe(String dni) {
        return pasajeros.containsKey(dni);
    }
}

