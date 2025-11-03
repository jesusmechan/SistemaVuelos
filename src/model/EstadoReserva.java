package model;

/**
 * Enum que representa el estado de una reserva
 */
public enum EstadoReserva {
    CONFIRMADA("Confirmada"),
    PENDIENTE("Pendiente"),
    CANCELADA("Cancelada"),
    COMPLETADA("Completada");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}

