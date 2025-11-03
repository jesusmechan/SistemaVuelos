package model;

import java.time.LocalDate;

/**
 * Clase que representa un Empleado
 * Extiende de Persona (Herencia)
 * Aplica LSP (Liskov Substitution Principle)
 */
public class Empleado extends Persona {
    private String numeroEmpleado;
    private String cargo;
    private LocalDate fechaContratacion;
    private double salario;

    public Empleado(String dni, String nombre, String apellido, String email, 
                   String telefono, String numeroEmpleado, String cargo, 
                   LocalDate fechaContratacion, double salario) {
        super(dni, nombre, apellido, email, telefono);
        this.numeroEmpleado = numeroEmpleado;
        this.cargo = cargo;
        this.fechaContratacion = fechaContratacion;
        this.salario = salario;
    }

    // Getters y Setters
    public String getNumeroEmpleado() {
        return numeroEmpleado;
    }

    public void setNumeroEmpleado(String numeroEmpleado) {
        this.numeroEmpleado = numeroEmpleado;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "numeroEmpleado='" + numeroEmpleado + '\'' +
                ", cargo='" + cargo + '\'' +
                ", fechaContratacion=" + fechaContratacion +
                ", salario=" + salario +
                ", " + super.toString() +
                '}';
    }
}

