package service;

import exception.OperacionNoPermitidaException;
import exception.RecursoNoEncontradoException;
import exception.ValidacionException;
import model.EstadoReserva;
import model.Reserva;
import model.Vuelo;
import repository.IReservaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Reservas
 * Aplica SRP - única responsabilidad: gestión de reservas
 */
public class ReservaService implements IReservaService {
    private final IReservaRepository reservaRepository;

    public ReservaService(IReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    public boolean crearReserva(Reserva reserva) {
        if (reserva == null) {
            throw new ValidacionException("La reserva no puede ser nula.");
        }

        if (reserva.getNumeroReserva() == null || reserva.getNumeroReserva().isBlank()) {
            throw new ValidacionException("El número de reserva es obligatorio.");
        }

        if (reservaRepository.existe(reserva.getNumeroReserva())) {
            throw new OperacionNoPermitidaException("Ya existe una reserva con el número " + reserva.getNumeroReserva() + ".");
        }

        if (reserva.getVuelo() == null || reserva.getPasajero() == null) {
            throw new ValidacionException("La reserva debe tener pasajero y vuelo asociados.");
        }

        // Validar que el vuelo tenga asientos disponibles
        Vuelo vuelo = reserva.getVuelo();
        if (!vuelo.tieneAsientosDisponibles()) {
            throw new OperacionNoPermitidaException("El vuelo " + vuelo.getNumeroVuelo() + " no tiene asientos disponibles.");
        }

        // Reservar el asiento en el vuelo
        vuelo.reservarAsiento();

        reservaRepository.guardar(reserva);
        return true;
    }

    @Override
    public Optional<Reserva> buscarReservaPorNumero(String numeroReserva) {
        return reservaRepository.buscarPorNumeroReserva(numeroReserva);
    }

    @Override
    public List<Reserva> listarTodasLasReservas() {
        return reservaRepository.listarTodos();
    }

    @Override
    public List<Reserva> buscarReservasPorPasajero(String dniPasajero) {
        return reservaRepository.buscarPorPasajero(dniPasajero);
    }

    @Override
    public List<Reserva> buscarReservasPorVuelo(String numeroVuelo) {
        return reservaRepository.buscarPorVuelo(numeroVuelo);
    }

    @Override
    public List<Reserva> buscarReservasPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ValidacionException("La fecha es obligatoria para la búsqueda.");
        }
        return reservaRepository.buscarPorFecha(fecha);
    }

    @Override
    public boolean cancelarReserva(String numeroReserva) {
        if (numeroReserva == null || numeroReserva.isBlank()) {
            throw new ValidacionException("El número de reserva es obligatorio para cancelar.");
        }

        Optional<Reserva> reservaOpt = reservaRepository.buscarPorNumeroReserva(numeroReserva);

        if (reservaOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontró la reserva " + numeroReserva + ".");
        }

        Reserva reserva = reservaOpt.get();
        reserva.setEstado(EstadoReserva.CANCELADA);

        // Liberar el asiento en el vuelo
        if (reserva.getVuelo() != null) {
            reserva.getVuelo().liberarAsiento();
        }

        reservaRepository.guardar(reserva);
        return true;
    }
}

