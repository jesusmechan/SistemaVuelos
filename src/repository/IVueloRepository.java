package repository;

import model.Vuelo;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Vuelos
 * Aplica ISP - interfaz específica para vuelos
 */
public interface IVueloRepository {
    void guardar(Vuelo vuelo);
    Optional<Vuelo> buscarPorNumeroVuelo(String numeroVuelo);
    List<Vuelo> listarTodos();
    List<Vuelo> buscarPorOrigen(String origen);
    List<Vuelo> buscarPorDestino(String destino);
    List<Vuelo> buscarPorOrigenYDestino(String origen, String destino);
    boolean eliminar(String numeroVuelo);
    boolean existe(String numeroVuelo);
}

