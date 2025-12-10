package repository;

import database.ConexionBD;
import model.Avion;
import model.EstadoAvion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de repositorio de Aviones con SQL Server
 */
public class AvionRepository implements IAvionRepository {
    private final ConexionBD conexionBD;

    public AvionRepository() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    @Override
    public void guardar(Avion avion) {
        String sql = "{CALL sp_crear_avion(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, avion.getNumeroSerie());
            cstmt.setString(2, avion.getModelo());
            cstmt.setString(3, avion.getFabricante());
            cstmt.setInt(4, avion.getCapacidadPasajeros());
            cstmt.setInt(5, avion.getCapacidadCarga());
            cstmt.setString(6, avion.getEstado().toString());
            cstmt.registerOutParameter(7, Types.BIT);
            cstmt.registerOutParameter(8, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(7);
            String mensaje = cstmt.getString(8);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al guardar avión: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al guardar avión: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Avion> buscarPorNumeroSerie(String numeroSerie) {
        String sql = "SELECT * FROM aviones WHERE numero_serie = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroSerie);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAvion(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar avión: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Avion> listarTodos() {
        String sql = "SELECT * FROM aviones ORDER BY numero_serie";
        List<Avion> aviones = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                aviones.add(mapResultSetToAvion(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar aviones: " + e.getMessage(), e);
        }
        
        return aviones;
    }

    @Override
    public List<Avion> buscarPorEstado(String estado) {
        String sql = "SELECT * FROM aviones WHERE estado = ? ORDER BY numero_serie";
        List<Avion> aviones = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado.toUpperCase());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    aviones.add(mapResultSetToAvion(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar aviones por estado: " + e.getMessage(), e);
        }
        
        return aviones;
    }

    @Override
    public boolean eliminar(String numeroSerie) {
        String sql = "{CALL sp_eliminar_avion(?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, numeroSerie);
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
                throw new RuntimeException("Error al eliminar avión: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al eliminar avión: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String numeroSerie) {
        String sql = "SELECT COUNT(*) FROM aviones WHERE numero_serie = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroSerie);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de avión: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Mapea un ResultSet a un objeto Avion
     */
    private Avion mapResultSetToAvion(ResultSet rs) throws SQLException {
        Avion avion = new Avion(
            rs.getString("numero_serie"),
            rs.getString("modelo"),
            rs.getString("fabricante"),
            rs.getInt("capacidad_pasajeros"),
            rs.getInt("capacidad_carga")
        );
        
        // Establecer estado
        String estadoStr = rs.getString("estado");
        try {
            avion.setEstado(EstadoAvion.valueOf(estadoStr));
        } catch (IllegalArgumentException e) {
            avion.setEstado(EstadoAvion.DISPONIBLE);
        }
        
        return avion;
    }
}
