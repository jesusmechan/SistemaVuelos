package service;

import exception.OperacionNoPermitidaException;
import exception.RecursoNoEncontradoException;
import exception.ValidacionException;
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
            throw new ValidacionException("El vuelo no puede ser nulo.");
        }

        if (vuelo.getNumeroVuelo() == null || vuelo.getNumeroVuelo().isBlank()) {
            throw new ValidacionException("El número de vuelo es obligatorio.");
        }

        if (vuelo.getAvion() == null) {
            throw new ValidacionException("El vuelo debe tener un avión asignado.");
        }

        if (vueloRepository.existe(vuelo.getNumeroVuelo())) {
            throw new OperacionNoPermitidaException("Ya existe un vuelo con el número " + vuelo.getNumeroVuelo() + ".");
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
        if (numeroVuelo == null || numeroVuelo.isBlank()) {
            throw new ValidacionException("El número de vuelo es obligatorio para eliminar.");
        }
        if (!vueloRepository.existe(numeroVuelo)) {
            throw new RecursoNoEncontradoException("No se encontró un vuelo con número " + numeroVuelo + ".");
        }
        return vueloRepository.eliminar(numeroVuelo);
    }
}

