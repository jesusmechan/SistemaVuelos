package service;

import model.Vuelo;
import java.util.List;
import java.util.Optional;

/**
 * Interface para servicio de Vuelos
 */
public interface IVueloService {
    boolean registrarVuelo(Vuelo vuelo);
    Optional<Vuelo> buscarVueloPorNumero(String numeroVuelo);
    List<Vuelo> listarTodosLosVuelos();
    List<Vuelo> buscarVuelosPorOrigen(String origen);
    List<Vuelo> buscarVuelosPorDestino(String destino);
    List<Vuelo> buscarVuelosPorRuta(String origen, String destino);
    boolean eliminarVuelo(String numeroVuelo);
}

