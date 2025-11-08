package service;

import exception.OperacionNoPermitidaException;
import exception.RecursoNoEncontradoException;
import exception.ValidacionException;
import model.Empleado;
import repository.IEmpleadoRepository;

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
            throw new ValidacionException("El empleado no puede ser nulo.");
        }

        if (empleado.getDni() == null || empleado.getDni().isBlank()) {
            throw new ValidacionException("El DNI del empleado es obligatorio.");
        }

        if (empleadoRepository.existe(empleado.getDni())) {
            throw new OperacionNoPermitidaException("Ya existe un empleado con el DNI " + empleado.getDni() + ".");
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
        if (cargo == null || cargo.isBlank()) {
            throw new ValidacionException("El cargo no puede estar vacío.");
        }
        return empleadoRepository.buscarPorCargo(cargo);
    }

    @Override
    public boolean eliminarEmpleado(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new ValidacionException("El DNI es obligatorio para eliminar un empleado.");
        }
        if (!empleadoRepository.existe(dni)) {
            throw new RecursoNoEncontradoException("No se encontró un empleado con DNI " + dni + ".");
        }
        return empleadoRepository.eliminar(dni);
    }
}

