package model;

/**
 * Enum que representa los roles del sistema
 * Aplica principio de encapsulación
 */
public enum Rol {
    ADMINISTRADOR("Administrador"),
    OPERADOR("Operador");
    //VENDEDOR("Vendedor");

    private final String descripcion;

    Rol(String descripcion) {
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

