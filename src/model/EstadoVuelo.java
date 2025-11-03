package model;

/**
 * Enum que representa el estado de un vuelo
 */
public enum EstadoVuelo {
    PROGRAMADO("Programado"),
    EN_ABORDAR("En Abordar"),
    EN_VUELO("En Vuelo"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoVuelo(String descripcion) {
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

