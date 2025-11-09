package service;

import model.Reserva;

import java.time.LocalDate;
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
    List<Reserva> buscarReservasPorFecha(LocalDate fecha);
    boolean cancelarReserva(String numeroReserva);
}

