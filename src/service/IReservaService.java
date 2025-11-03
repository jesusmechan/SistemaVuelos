package service;

import model.Reserva;
import java.util.List;
import java.util.Optional;

/**
 * Interface para servicio de Reservas
 */
public interface IReservaService {
    boolean crearReserva(Reserva reserva);
    Optional<Reserva> buscarReservaPorNumero(String numeroReserva);
    List<Reserva> listarTodasLasReservas();
    List<Reserva> buscarReservasPorPasajero(String dniPasajero);
    List<Reserva> buscarReservasPorVuelo(String numeroVuelo);
    boolean cancelarReserva(String numeroReserva);
}

