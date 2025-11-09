package repository;

import model.Reserva;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de repositorio de Reservas en memoria
 */
public class ReservaRepository implements IReservaRepository {
    private final Map<String, Reserva> reservas = new HashMap<>();

    @Override
    public void guardar(Reserva reserva) {
        reservas.put(reserva.getNumeroReserva(), reserva);
    }

    @Override
    public Optional<Reserva> buscarPorNumeroReserva(String numeroReserva) {
        return Optional.ofNullable(reservas.get(numeroReserva));
    }

    @Override
    public List<Reserva> listarTodos() {
        return new ArrayList<>(reservas.values());
    }

    @Override
    public List<Reserva> buscarPorPasajero(String dniPasajero) {
        return reservas.values().stream()
                .filter(r -> r.getPasajero() != null 
                          && r.getPasajero().getDni().equals(dniPasajero))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> buscarPorVuelo(String numeroVuelo) {
        return reservas.values().stream()
                .filter(r -> r.getVuelo() != null 
                          && r.getVuelo().getNumeroVuelo().equals(numeroVuelo))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> buscarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            return Collections.emptyList();
        }
        return reservas.values().stream()
                .filter(r -> r.getFechaReserva() != null
                          && r.getFechaReserva().toLocalDate().equals(fecha))
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminar(String numeroReserva) {
        return reservas.remove(numeroReserva) != null;
    }

    @Override
    public boolean existe(String numeroReserva) {
        return reservas.containsKey(numeroReserva);
    }
}

