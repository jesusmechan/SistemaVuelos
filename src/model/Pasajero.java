package model;

import java.time.LocalDate;

/**
 * Clase que representa un Pasajero
 * Extiende de Persona (Herencia)
 * Aplica LSP (Liskov Substitution Principle)
 */
public class Pasajero extends Persona {
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String numeroPasaporte;

    public Pasajero(String dni, String nombre, String apellido, String email, 
                   String telefono, LocalDate fechaNacimiento, String nacionalidad, 
                   String numeroPasaporte) {
        super(dni, nombre, apellido, email, telefono);
        this.fechaNacimiento = fechaNacimiento;
        this.nacionalidad = nacionalidad;
        this.numeroPasaporte = numeroPasaporte;
    }

    // Getters y Setters
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getNumeroPasaporte() {
        return numeroPasaporte;
    }

    public void setNumeroPasaporte(String numeroPasaporte) {
        this.numeroPasaporte = numeroPasaporte;
    }

    @Override
    public String toString() {
        return "Pasajero{" +
                "fechaNacimiento=" + fechaNacimiento +
                ", nacionalidad='" + nacionalidad + '\'' +
                ", numeroPasaporte='" + numeroPasaporte + '\'' +
                ", " + super.toString() +
                '}';
    }
}

