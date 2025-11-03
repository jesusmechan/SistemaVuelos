package service;

import model.Empleado;
import java.util.List;
import java.util.Optional;

/**
 * Interface para servicio de Empleados
 * Aplica DIP (Dependency Inversion Principle)
 */
public interface IEmpleadoService {
    boolean registrarEmpleado(Empleado empleado);
    Optional<Empleado> buscarEmpleadoPorDni(String dni);
    Optional<Empleado> buscarEmpleadoPorNumero(String numeroEmpleado);
    List<Empleado> listarTodosLosEmpleados();
    List<Empleado> buscarEmpleadosPorCargo(String cargo);
    boolean eliminarEmpleado(String dni);
}

