package repository;

import model.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Reservas
 * Aplica ISP - interfaz específica para reservas
 */
public interface IReservaRepository {
    void guardar(Reserva reserva);
    Optional<Reserva> buscarPorNumeroReserva(String numeroReserva);
    List<Reserva> listarTodos();
    List<Reserva> buscarPorPasajero(String dniPasajero);
    List<Reserva> buscarPorVuelo(String numeroVuelo);
    List<Reserva> buscarPorFecha(LocalDate fecha);
    boolean eliminar(String numeroReserva);
    boolean existe(String numeroReserva);
}

