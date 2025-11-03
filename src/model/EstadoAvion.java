package model;

/**
 * Enum que representa el estado de un avión
 */
public enum EstadoAvion {
    DISPONIBLE("Disponible"),
    EN_MANTENIMIENTO("En Mantenimiento"),
    EN_VUELO("En Vuelo"),
    FUERA_SERVICIO("Fuera de Servicio");

    private final String descripcion;

    EstadoAvion(String descripcion) {
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

