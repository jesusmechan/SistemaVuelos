package service;

import model.Vuelo;
import repository.IVueloRepository;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Vuelos
 * Aplica SRP - única responsabilidad: gestión de vuelos
 */
public class VueloService implements IVueloService {
    private final IVueloRepository vueloRepository;

    public VueloService(IVueloRepository vueloRepository) {
        this.vueloRepository = vueloRepository;
    }

    @Override
    public boolean registrarVuelo(Vuelo vuelo) {
        if (vuelo == null) {
            return false;
        }
        
        if (vuelo.getNumeroVuelo() == null || vuelo.getNumeroVuelo().isEmpty()) {
            return false;
        }
        
        if (vuelo.getAvion() == null) {
            return false; // Un vuelo debe tener un avión asignado
        }
        
        if (vueloRepository.existe(vuelo.getNumeroVuelo())) {
            return false; // Ya existe un vuelo con ese número
        }
        
        vueloRepository.guardar(vuelo);
        return true;
    }

    @Override
    public Optional<Vuelo> buscarVueloPorNumero(String numeroVuelo) {
        return vueloRepository.buscarPorNumeroVuelo(numeroVuelo);
    }

    @Override
    public List<Vuelo> listarTodosLosVuelos() {
        return vueloRepository.listarTodos();
    }

    @Override
    public List<Vuelo> buscarVuelosPorOrigen(String origen) {
        return vueloRepository.buscarPorOrigen(origen);
    }

    @Override
    public List<Vuelo> buscarVuelosPorDestino(String destino) {
        return vueloRepository.buscarPorDestino(destino);
    }

    @Override
    public List<Vuelo> buscarVuelosPorRuta(String origen, String destino) {
        return vueloRepository.buscarPorOrigenYDestino(origen, destino);
    }

    @Override
    public boolean eliminarVuelo(String numeroVuelo) {
        return vueloRepository.eliminar(numeroVuelo);
    }
}

