package repository;

import database.ConexionBD;
import model.Vuelo;
import model.Avion;
import model.EstadoVuelo;
import model.EstadoAvion;
import repository.AvionRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de repositorio de Vuelos con SQL Server
 */
public class VueloRepository implements IVueloRepository {
    private final ConexionBD conexionBD;
    private final AvionRepository avionRepository;

    public VueloRepository() {
        this.conexionBD = ConexionBD.getInstancia();
        this.avionRepository = new AvionRepository();
    }

    @Override
    public void guardar(Vuelo vuelo) {
        String sql = "{CALL sp_crear_vuelo(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, vuelo.getNumeroVuelo());
            cstmt.setString(2, vuelo.getOrigen());
            cstmt.setString(3, vuelo.getDestino());
            cstmt.setTimestamp(4, Timestamp.valueOf(vuelo.getFechaHoraSalida()));
            cstmt.setTimestamp(5, Timestamp.valueOf(vuelo.getFechaHoraLlegada()));
            cstmt.setString(6, vuelo.getAvion().getNumeroSerie());
            cstmt.setDouble(7, vuelo.getPrecio());
            cstmt.setString(8, vuelo.getEstado().toString());
            cstmt.registerOutParameter(9, Types.BIT);
            cstmt.registerOutParameter(10, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(9);
            String mensaje = cstmt.getString(10);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al guardar vuelo: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al guardar vuelo: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Vuelo> buscarPorNumeroVuelo(String numeroVuelo) {
        String sql = "{CALL sp_buscar_vuelo_por_numero(?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, numeroVuelo);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVueloCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelo: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Vuelo> listarTodos() {
        String sql = "{CALL sp_listar_vuelos}";
        List<Vuelo> vuelos = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql);
             ResultSet rs = cstmt.executeQuery()) {
            
            while (rs.next()) {
                vuelos.add(mapResultSetToVueloCompleto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar vuelos: " + e.getMessage(), e);
        }
        
        return vuelos;
    }

    @Override
    public List<Vuelo> buscarPorOrigen(String origen) {
        String sql = "{CALL sp_buscar_vuelos_por_origen(?)}";
        List<Vuelo> vuelos = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, origen);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    vuelos.add(mapResultSetToVueloCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos por origen: " + e.getMessage(), e);
        }
        
        return vuelos;
    }

    @Override
    public List<Vuelo> buscarPorDestino(String destino) {
        String sql = "{CALL sp_buscar_vuelos_por_destino(?)}";
        List<Vuelo> vuelos = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, destino);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    vuelos.add(mapResultSetToVueloCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos por destino: " + e.getMessage(), e);
        }
        
        return vuelos;
    }

    @Override
    public List<Vuelo> buscarPorOrigenYDestino(String origen, String destino) {
        String sql = "{CALL sp_buscar_vuelos_por_ruta(?, ?)}";
        List<Vuelo> vuelos = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, origen);
            cstmt.setString(2, destino);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    vuelos.add(mapResultSetToVueloCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos por origen y destino: " + e.getMessage(), e);
        }
        
        return vuelos;
    }

    @Override
    public List<Vuelo> buscarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            return new ArrayList<>();
        }
        
        String sql = "{CALL sp_buscar_vuelos_por_fecha(?)}";
        List<Vuelo> vuelos = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setDate(1, Date.valueOf(fecha));
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    vuelos.add(mapResultSetToVueloCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos por fecha: " + e.getMessage(), e);
        }
        
        return vuelos;
    }

    @Override
    public boolean eliminar(String numeroVuelo) {
        String sql = "{CALL sp_eliminar_vuelo(?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, numeroVuelo);
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
                throw new RuntimeException("Error al eliminar vuelo: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al eliminar vuelo: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String numeroVuelo) {
        String sql = "SELECT COUNT(*) FROM vuelos WHERE numero_vuelo = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroVuelo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de vuelo: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Busca vuelos disponibles usando el procedimiento almacenado
     */
    public List<Vuelo> buscarVuelosDisponibles(String origen, String destino, LocalDate fecha) {
        String sql = "{CALL sp_buscar_vuelos_disponibles(?, ?, ?)}";
        List<Vuelo> vuelos = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            if (origen != null && !origen.isEmpty()) {
                cstmt.setString(1, origen);
            } else {
                cstmt.setNull(1, Types.VARCHAR);
            }
            
            if (destino != null && !destino.isEmpty()) {
                cstmt.setString(2, destino);
            } else {
                cstmt.setNull(2, Types.VARCHAR);
            }
            
            if (fecha != null) {
                cstmt.setDate(3, Date.valueOf(fecha));
            } else {
                cstmt.setNull(3, Types.DATE);
            }
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    vuelos.add(mapResultSetToVueloCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos disponibles: " + e.getMessage(), e);
        }
        
        return vuelos;
    }

    /**
     * Mapea un ResultSet completo (con JOINs) a un objeto Vuelo sin hacer consultas adicionales
     */
    private Vuelo mapResultSetToVueloCompleto(ResultSet rs) throws SQLException {
        // Datos del vuelo
        Timestamp fechaSalida = rs.getTimestamp("fecha_hora_salida");
        Timestamp fechaLlegada = rs.getTimestamp("fecha_hora_llegada");
        LocalDateTime fechaHoraSalida = fechaSalida != null ? fechaSalida.toLocalDateTime() : null;
        LocalDateTime fechaHoraLlegada = fechaLlegada != null ? fechaLlegada.toLocalDateTime() : null;
        
        String numero_vuelo = rs.getString("numero_vuelo");
        String origen = rs.getString("origen");
        String destino = rs.getString("destino");
        Double precio = rs.getDouble("precio");
        int asientos_disponibles = rs.getInt("asientos_disponibles");
        String estado_vuelo = rs.getString("estado_vuelo");
        
        // Crear Avión desde los datos del JOIN
        String numeroSerie = rs.getString("numero_serie");
        String modelo = rs.getString("modelo");
        String fabricante = rs.getString("fabricante");
        int capacidadPasajeros = rs.getInt("capacidad_pasajeros");
        int capacidadCarga = rs.getInt("capacidad_carga");
        String estadoAvionStr = rs.getString("estado_avion");
        
        Avion avion = new Avion(numeroSerie, modelo, fabricante, capacidadPasajeros, capacidadCarga);
        try {
            avion.setEstado(EstadoAvion.valueOf(estadoAvionStr));
        } catch (IllegalArgumentException e) {
            avion.setEstado(EstadoAvion.DISPONIBLE);
        }
        
        // Crear Vuelo
        Vuelo vuelo = new Vuelo(
            numero_vuelo,
            origen,
            destino,
            fechaHoraSalida,
            fechaHoraLlegada,
            avion,
            precio
        );
        vuelo.setAsientosDisponibles(asientos_disponibles);
        
        // Establecer estado
        try {
            vuelo.setEstado(EstadoVuelo.valueOf(estado_vuelo));
        } catch (IllegalArgumentException e) {
            vuelo.setEstado(EstadoVuelo.PROGRAMADO);
        }
        
        return vuelo;
    }

    /**
     * Mapea un ResultSet a un objeto Vuelo (método original que hace consultas adicionales)
     */
    private Vuelo mapResultSetToVuelo(ResultSet rs) throws SQLException {
        Timestamp fechaSalida = rs.getTimestamp("fecha_hora_salida");
        Timestamp fechaLlegada = rs.getTimestamp("fecha_hora_llegada");
        
        LocalDateTime fechaHoraSalida = fechaSalida != null ? fechaSalida.toLocalDateTime() : null;
        LocalDateTime fechaHoraLlegada = fechaLlegada != null ? fechaLlegada.toLocalDateTime() : null;
        String numero_vuelo = rs.getString("numero_vuelo");
        String origen = rs.getString("origen");
        String destino = rs.getString("destino");
        Double precio = rs.getDouble("precio");
        int asientos_disponibles = rs.getInt("asientos_disponibles");
        String estado = rs.getString("estado");


        // Obtener el avión
        String numeroSerieAvion = rs.getString("numero_serie_avion");
        Avion avion = avionRepository.buscarPorNumeroSerie(numeroSerieAvion)
                .orElseThrow(() -> new SQLException("Avión no encontrado: " + numeroSerieAvion));
        
        Vuelo vuelo = new Vuelo(
            numero_vuelo,
            origen,
            destino,
            fechaHoraSalida,
            fechaHoraLlegada,
            avion,
            precio
        );
        
        vuelo.setAsientosDisponibles(asientos_disponibles);
        
        // Establecer estado
        String estadoStr = estado;
        try {
            vuelo.setEstado(EstadoVuelo.valueOf(estadoStr));
        } catch (IllegalArgumentException e) {
            vuelo.setEstado(EstadoVuelo.PROGRAMADO);
        }
        
        return vuelo;
    }

}
