package repository;

import database.ConexionBD;
import model.Empleado;
import model.Persona;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de repositorio de Empleados con SQL Server
 */
public class EmpleadoRepository implements IEmpleadoRepository {
    private final ConexionBD conexionBD;

    public EmpleadoRepository() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    @Override
    public void guardar(Empleado empleado) {
        String sql = "{CALL sp_crear_empleado(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, empleado.getDni());
            cstmt.setString(2, empleado.getNombre());
            cstmt.setString(3, empleado.getApellido());
            cstmt.setString(4, empleado.getEmail());
            cstmt.setString(5, empleado.getTelefono());
            cstmt.setString(6, empleado.getNumeroEmpleado());
            cstmt.setString(7, empleado.getCargo());
            cstmt.setDate(8, Date.valueOf(empleado.getFechaContratacion()));
            cstmt.setDouble(9, empleado.getSalario());
            cstmt.registerOutParameter(10, Types.BIT);
            cstmt.registerOutParameter(11, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(10);
            String mensaje = cstmt.getString(11);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al guardar empleado: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al guardar empleado: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Empleado> buscarPorDni(String dni) {
        String sql = "SELECT e.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM empleados e " +
                     "INNER JOIN personas per ON e.dni = per.dni " +
                     "WHERE e.dni = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empleado: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<Empleado> buscarPorNumeroEmpleado(String numeroEmpleado) {
        String sql = "SELECT e.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM empleados e " +
                     "INNER JOIN personas per ON e.dni = per.dni " +
                     "WHERE e.numero_empleado = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroEmpleado);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empleado por número: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Empleado> listarTodos() {
        String sql = "SELECT e.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM empleados e " +
                     "INNER JOIN personas per ON e.dni = per.dni " +
                     "ORDER BY per.apellido, per.nombre";
        List<Empleado> empleados = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                empleados.add(mapResultSetToEmpleado(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar empleados: " + e.getMessage(), e);
        }
        
        return empleados;
    }

    @Override
    public List<Empleado> buscarPorCargo(String cargo) {
        String sql = "SELECT e.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM empleados e " +
                     "INNER JOIN personas per ON e.dni = per.dni " +
                     "WHERE e.cargo = ? " +
                     "ORDER BY per.apellido, per.nombre";
        List<Empleado> empleados = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cargo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    empleados.add(mapResultSetToEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empleados por cargo: " + e.getMessage(), e);
        }
        
        return empleados;
    }

    @Override
    public boolean eliminar(String dni) {
        // Actualizar como inactivo en lugar de eliminar usando procedimiento almacenado
        String sql = "{CALL sp_actualizar_estado_empleado(?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, dni);
            cstmt.setBoolean(2, false);
            cstmt.registerOutParameter(3, Types.BIT);
            cstmt.registerOutParameter(4, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(3);
            String mensaje = cstmt.getString(4);
            
            if (resultado) {
                conexionBD.commit();
                return true;
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al eliminar empleado: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al eliminar empleado: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String dni) {
        String sql = "SELECT COUNT(*) FROM empleados WHERE dni = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de empleado: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Mapea un ResultSet a un objeto Empleado
     */
    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        Date fechaContratacion = rs.getDate("fecha_contratacion");
        LocalDate fechaCont = fechaContratacion != null ? fechaContratacion.toLocalDate() : null;
        
        return new Empleado(
            rs.getString("dni"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("telefono"),
            rs.getString("numero_empleado"),
            rs.getString("cargo"),
            fechaCont,
            rs.getDouble("salario")
        );
    }
}
