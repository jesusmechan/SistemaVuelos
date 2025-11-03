package service;

import model.Empleado;
import repository.IEmpleadoRepository;
import repository.EmpleadoRepository;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Empleados
 * Aplica SRP (Single Responsibility Principle) - única responsabilidad: gestión de empleados
 * Aplica DIP - depende de IEmpleadoRepository (abstracción)
 */
public class EmpleadoService implements IEmpleadoService {
    private final IEmpleadoRepository empleadoRepository;

    public EmpleadoService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public boolean registrarEmpleado(Empleado empleado) {
        if (empleado == null) {
            return false;
        }
        
        if (empleado.getDni() == null || empleado.getDni().isEmpty()) {
            return false;
        }
        
        if (empleadoRepository.existe(empleado.getDni())) {
            return false; // Ya existe un empleado con ese DNI
        }
        
        empleadoRepository.guardar(empleado);
        return true;
    }

    @Override
    public Optional<Empleado> buscarEmpleadoPorDni(String dni) {
        return empleadoRepository.buscarPorDni(dni);
    }

    @Override
    public Optional<Empleado> buscarEmpleadoPorNumero(String numeroEmpleado) {
        return empleadoRepository.buscarPorNumeroEmpleado(numeroEmpleado);
    }

    @Override
    public List<Empleado> listarTodosLosEmpleados() {
        return empleadoRepository.listarTodos();
    }

    @Override
    public List<Empleado> buscarEmpleadosPorCargo(String cargo) {
        return empleadoRepository.buscarPorCargo(cargo);
    }

    @Override
    public boolean eliminarEmpleado(String dni) {
        return empleadoRepository.eliminar(dni);
    }
}

