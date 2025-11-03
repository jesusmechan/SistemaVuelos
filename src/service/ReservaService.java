package service;

import model.EstadoReserva;
import model.Reserva;
import model.Vuelo;
import repository.IReservaRepository;
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
            return false;
        }
        
        if (reserva.getNumeroReserva() == null || reserva.getNumeroReserva().isEmpty()) {
            return false;
        }
        
        if (reserva.getVuelo() == null || reserva.getPasajero() == null) {
            return false;
        }
        
        // Validar que el vuelo tenga asientos disponibles
        Vuelo vuelo = reserva.getVuelo();
        if (!vuelo.tieneAsientosDisponibles()) {
            return false; // No hay asientos disponibles
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
    public boolean cancelarReserva(String numeroReserva) {
        Optional<Reserva> reservaOpt = reservaRepository.buscarPorNumeroReserva(numeroReserva);
        
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setEstado(EstadoReserva.CANCELADA);
            
            // Liberar el asiento en el vuelo
            if (reserva.getVuelo() != null) {
                reserva.getVuelo().liberarAsiento();
            }
            
            reservaRepository.guardar(reserva);
            return true;
        }
        
        return false;
    }
}

