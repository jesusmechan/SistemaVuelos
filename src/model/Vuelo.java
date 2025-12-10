package model;

import java.time.LocalDateTime;

/**
 * Clase que representa un Vuelo
 * Aplica composición con Avion y encapsulación
 */
public class Vuelo {
    private String numeroVuelo;
    private String origen;
    private String destino;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Avion avion;
    private double precio;
    private int asientosDisponibles;
    private EstadoVuelo estado;

    public Vuelo(){}
    public Vuelo(String numeroVuelo, String origen, String destino, 
                LocalDateTime fechaHoraSalida, LocalDateTime fechaHoraLlegada,
                Avion avion, double precio) {
        this.numeroVuelo = numeroVuelo;
        this.origen = origen;
        this.destino = destino;
        this.fechaHoraSalida = fechaHoraSalida;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.avion = avion;
        this.precio = precio;
        this.asientosDisponibles = avion != null ? avion.getCapacidadPasajeros() : 0;
        this.estado = EstadoVuelo.PROGRAMADO;
    }

    // Getters y Setters
    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public LocalDateTime getFechaHoraLlegada() {
        return fechaHoraLlegada;
    }

    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) {
        this.fechaHoraLlegada = fechaHoraLlegada;
    }

    public Avion getAvion() {
        return avion;
    }

    public void setAvion(Avion avion) {
        this.avion = avion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(int asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public EstadoVuelo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVuelo estado) {
        this.estado = estado;
    }

    public boolean tieneAsientosDisponibles() {
        return asientosDisponibles > 0;
    }

    public void reservarAsiento() {
        if (asientosDisponibles > 0) {
            asientosDisponibles--;
        }
    }

    public void liberarAsiento() {
        if (avion != null && asientosDisponibles < avion.getCapacidadPasajeros()) {
            asientosDisponibles++;
        }
    }

    @Override
    public String toString() {
        return "Vuelo{" +
                "numeroVuelo='" + numeroVuelo + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", fechaHoraSalida=" + fechaHoraSalida +
                ", fechaHoraLlegada=" + fechaHoraLlegada +
                ", avion=" + (avion != null ? avion.getModelo() : "N/A") +
                ", precio=" + precio +
                ", asientosDisponibles=" + asientosDisponibles +
                ", estado=" + estado +
                '}';
    }
}

