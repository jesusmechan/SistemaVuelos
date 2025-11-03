package repository;

import model.Empleado;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de repositorio de Empleados en memoria
 * Aplica OCP (Open/Closed Principle) - abierto a extensión, cerrado a modificación
 */
public class EmpleadoRepository implements IEmpleadoRepository {
    private final Map<String, Empleado> empleados = new HashMap<>();

    @Override
    public void guardar(Empleado empleado) {
        empleados.put(empleado.getDni(), empleado);
    }

    @Override
    public Optional<Empleado> buscarPorDni(String dni) {
        return Optional.ofNullable(empleados.get(dni));
    }

    @Override
    public Optional<Empleado> buscarPorNumeroEmpleado(String numeroEmpleado) {
        return empleados.values().stream()
                .filter(e -> e.getNumeroEmpleado().equals(numeroEmpleado))
                .findFirst();
    }

    @Override
    public List<Empleado> listarTodos() {
        return new ArrayList<>(empleados.values());
    }

    @Override
    public List<Empleado> buscarPorCargo(String cargo) {
        return empleados.values().stream()
                .filter(e -> e.getCargo().equalsIgnoreCase(cargo))
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminar(String dni) {
        return empleados.remove(dni) != null;
    }

    @Override
    public boolean existe(String dni) {
        return empleados.containsKey(dni);
    }
}

