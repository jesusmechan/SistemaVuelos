package repository;

import model.Empleado;
import java.util.List;
import java.util.Optional;

/**
 * Interface para repositorio de Empleados
 * Aplica ISP - interfaz específica para empleados
 */
public interface IEmpleadoRepository {
    void guardar(Empleado empleado);
    Optional<Empleado> buscarPorDni(String dni);
    Optional<Empleado> buscarPorNumeroEmpleado(String numeroEmpleado);
    List<Empleado> listarTodos();
    List<Empleado> buscarPorCargo(String cargo);
    boolean eliminar(String dni);
    boolean existe(String dni);
}

