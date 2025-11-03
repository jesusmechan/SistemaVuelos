package model;

/**
 * Clase que representa un Usuario del sistema
 * Puede ser un empleado con credenciales de acceso
 * Aplica composición con Empleado
 */
public class Usuario {
    private String nombreUsuario;
    private String contrasena;
    private Rol rol;
    private Empleado empleado;

    public Usuario(String nombreUsuario, String contrasena, Rol rol, Empleado empleado) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.empleado = empleado;
    }

    // Getters y Setters
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public String getNombre() {
        return empleado != null ? empleado.getNombreCompleto() : nombreUsuario;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", rol=" + rol +
                ", empleado=" + (empleado != null ? empleado.getNombreCompleto() : "N/A") +
                '}';
    }
}

