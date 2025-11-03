package model;

import java.time.LocalDateTime;

/**
 * Clase que representa una Reserva de vuelo
 * Aplica composición con Pasajero y Vuelo
 */
public class Reserva {
    private String numeroReserva;
    private Pasajero pasajero;
    private Vuelo vuelo;
    private LocalDateTime fechaReserva;
    private EstadoReserva estado;
    private int numeroAsiento;

    public Reserva(String numeroReserva, Pasajero pasajero, Vuelo vuelo, int numeroAsiento) {
        this.numeroReserva = numeroReserva;
        this.pasajero = pasajero;
        this.vuelo = vuelo;
        this.fechaReserva = LocalDateTime.now();
        this.estado = EstadoReserva.CONFIRMADA;
        this.numeroAsiento = numeroAsiento;
    }

    // Getters y Setters
    public String getNumeroReserva() {
        return numeroReserva;
    }

    public void setNumeroReserva(String numeroReserva) {
        this.numeroReserva = numeroReserva;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public void setPasajero(Pasajero pasajero) {
        this.pasajero = pasajero;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public int getNumeroAsiento() {
        return numeroAsiento;
    }

    public void setNumeroAsiento(int numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
    }

    public double calcularTotal() {
        return vuelo != null ? vuelo.getPrecio() : 0.0;
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "numeroReserva='" + numeroReserva + '\'' +
                ", pasajero=" + (pasajero != null ? pasajero.getNombreCompleto() : "N/A") +
                ", vuelo=" + (vuelo != null ? vuelo.getNumeroVuelo() : "N/A") +
                ", fechaReserva=" + fechaReserva +
                ", estado=" + estado +
                ", numeroAsiento=" + numeroAsiento +
                ", total=" + calcularTotal() +
                '}';
    }
}

