package repository;

import database.ConexionBD;
import model.Pasajero;
import model.Persona;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de repositorio de Pasajeros con SQL Server
 */
public class PasajeroRepository implements IPasajeroRepository {
    private final ConexionBD conexionBD;

    public PasajeroRepository() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    @Override
    public void guardar(Pasajero pasajero) {
        String sql = "{CALL sp_crear_pasajero(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, pasajero.getDni());
            cstmt.setString(2, pasajero.getNombre());
            cstmt.setString(3, pasajero.getApellido());
            cstmt.setString(4, pasajero.getEmail());
            cstmt.setString(5, pasajero.getTelefono());
            cstmt.setDate(6, Date.valueOf(pasajero.getFechaNacimiento()));
            cstmt.setString(7, pasajero.getNacionalidad());
            cstmt.setString(8, pasajero.getNumeroPasaporte());
            cstmt.registerOutParameter(9, Types.BIT);
            cstmt.registerOutParameter(10, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(9);
            String mensaje = cstmt.getString(10);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al guardar pasajero: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al guardar pasajero: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Pasajero> buscarPorDni(String dni) {
        String sql = "SELECT p.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM pasajeros p " +
                     "INNER JOIN personas per ON p.dni = per.dni " +
                     "WHERE p.dni = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPasajero(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pasajero: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Pasajero> listarTodos() {
        String sql = "SELECT p.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM pasajeros p " +
                     "INNER JOIN personas per ON p.dni = per.dni " +
                     "ORDER BY per.apellido, per.nombre";
        List<Pasajero> pasajeros = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                pasajeros.add(mapResultSetToPasajero(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pasajeros: " + e.getMessage(), e);
        }
        
        return pasajeros;
    }

    @Override
    public boolean eliminar(String dni) {
        // Usar procedimiento almacenado para eliminar pasajero
        String sql = "{CALL sp_eliminar_pasajero(?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, dni);
            cstmt.registerOutParameter(2, Types.BIT);
            cstmt.registerOutParameter(3, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(2);
            String mensaje = cstmt.getString(3);
            
            if (resultado) {
                conexionBD.commit();
                return true;
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al eliminar pasajero: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al eliminar pasajero: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String dni) {
        String sql = "SELECT COUNT(*) FROM pasajeros WHERE dni = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de pasajero: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Mapea un ResultSet a un objeto Pasajero
     */
    private Pasajero mapResultSetToPasajero(ResultSet rs) throws SQLException {
        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        LocalDate fechaNac = fechaNacimiento != null ? fechaNacimiento.toLocalDate() : null;

        String dni = rs.getString("dni");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String email = rs.getString("email");
        String telefono = rs.getString("telefono");
        String nacionalidad = rs.getString("nacionalidad");
        String numero_pasaporte = rs.getString("numero_pasaporte");

        return new Pasajero(
            dni,
            nombre,
            apellido,
            email,
            telefono,
            fechaNac,
            nacionalidad,
            numero_pasaporte
        );
    }
}
