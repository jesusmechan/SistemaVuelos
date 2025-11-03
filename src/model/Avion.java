package model;

/**
 * Clase que representa un Avión
 * Aplica encapsulación y SRP (Single Responsibility Principle)
 */
public class Avion {
    private String numeroSerie;
    private String modelo;
    private String fabricante;
    private int capacidadPasajeros;
    private int capacidadCarga; // en kg
    private EstadoAvion estado;

    public Avion(String numeroSerie, String modelo, String fabricante, 
                int capacidadPasajeros, int capacidadCarga) {
        this.numeroSerie = numeroSerie;
        this.modelo = modelo;
        this.fabricante = fabricante;
        this.capacidadPasajeros = capacidadPasajeros;
        this.capacidadCarga = capacidadCarga;
        this.estado = EstadoAvion.DISPONIBLE;
    }

    // Getters y Setters
    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public int getCapacidadPasajeros() {
        return capacidadPasajeros;
    }

    public void setCapacidadPasajeros(int capacidadPasajeros) {
        this.capacidadPasajeros = capacidadPasajeros;
    }

    public int getCapacidadCarga() {
        return capacidadCarga;
    }

    public void setCapacidadCarga(int capacidadCarga) {
        this.capacidadCarga = capacidadCarga;
    }

    public EstadoAvion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAvion estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Avion{" +
                "numeroSerie='" + numeroSerie + '\'' +
                ", modelo='" + modelo + '\'' +
                ", fabricante='" + fabricante + '\'' +
                ", capacidadPasajeros=" + capacidadPasajeros +
                ", capacidadCarga=" + capacidadCarga +
                ", estado=" + estado +
                '}';
    }
}

