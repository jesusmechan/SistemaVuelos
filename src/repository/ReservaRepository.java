package repository;

import database.ConexionBD;
import model.Reserva;
import model.Pasajero;
import model.Vuelo;
import model.Avion;
import model.EstadoReserva;
import model.EstadoVuelo;
import model.EstadoAvion;
import repository.PasajeroRepository;
import repository.VueloRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de repositorio de Reservas con SQL Server
 */
public class ReservaRepository implements IReservaRepository {
    private final ConexionBD conexionBD;
    private final PasajeroRepository pasajeroRepository;
    private final VueloRepository vueloRepository;

    public ReservaRepository() {
        this.conexionBD = ConexionBD.getInstancia();
        this.pasajeroRepository = new PasajeroRepository();
        this.vueloRepository = new VueloRepository();
    }

    @Override
    public void guardar(Reserva reserva) {
        // Usar el procedimiento almacenado para crear reserva
        String sql = "{CALL sp_crear_reserva(?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, reserva.getNumeroReserva());
            cstmt.setString(2, reserva.getPasajero().getDni());
            cstmt.setString(3, reserva.getVuelo().getNumeroVuelo());
            cstmt.setInt(4, reserva.getNumeroAsiento());
            cstmt.registerOutParameter(5, Types.BIT);
            cstmt.registerOutParameter(6, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(5);
            String mensaje = cstmt.getString(6);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al crear reserva: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al guardar reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Reserva> buscarPorNumeroReserva(String numeroReserva) {
        String sql = "{CALL sp_buscar_reserva_por_numero(?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, numeroReserva);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservaCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reserva: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Reserva> listarTodos() {
        String sql = "{CALL sp_listar_reservas}";
        List<Reserva> reservas = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql);
             ResultSet rs = cstmt.executeQuery()) {
            
            while (rs.next()) {
                reservas.add(mapResultSetToReservaCompleto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar reservas: " + e.getMessage(), e);
        }
        
        return reservas;
    }

    @Override
    public List<Reserva> buscarPorPasajero(String dniPasajero) {
        // Usar el procedimiento almacenado
        String sql = "{CALL sp_obtener_reservas_pasajero(?)}";
        List<Reserva> reservas = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, dniPasajero);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    // Mapear desde el procedimiento almacenado
                    reservas.add(mapResultSetToReservaFromProcedure(rs, dniPasajero));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reservas por pasajero: " + e.getMessage(), e);
        }
        
        return reservas;
    }

    @Override
    public List<Reserva> buscarPorVuelo(String numeroVuelo) {
        String sql = "{CALL sp_buscar_reservas_por_vuelo(?)}";
        List<Reserva> reservas = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, numeroVuelo);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapResultSetToReservaCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reservas por vuelo: " + e.getMessage(), e);
        }
        
        return reservas;
    }

    @Override
    public List<Reserva> buscarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            return new ArrayList<>();
        }
        
        String sql = "{CALL sp_buscar_reservas_por_fecha(?)}";
        List<Reserva> reservas = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setDate(1, Date.valueOf(fecha));
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapResultSetToReservaCompleto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reservas por fecha: " + e.getMessage(), e);
        }
        
        return reservas;
    }

    @Override
    public boolean eliminar(String numeroReserva) {
        // Usar el procedimiento almacenado para cancelar reserva
        String sql = "{CALL sp_cancelar_reserva(?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, numeroReserva);
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
                throw new RuntimeException("Error al cancelar reserva: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al eliminar reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String numeroReserva) {
        String sql = "SELECT COUNT(*) FROM reservas WHERE numero_reserva = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroReserva);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de reserva: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Mapea un ResultSet completo (con JOINs) a un objeto Reserva sin hacer consultas adicionales
     */
    private Reserva mapResultSetToReservaCompleto(ResultSet rs) throws SQLException {
        // Datos de la reserva
        Timestamp fechaReserva = rs.getTimestamp("fecha_reserva");
        LocalDateTime fechaRes = fechaReserva != null ? fechaReserva.toLocalDateTime() : LocalDateTime.now();
        String numero_reserva = rs.getString("numero_reserva");
        int numero_asiento = rs.getInt("numero_asiento");
        String estado_reserva = rs.getString("estado_reserva");
        
        // Crear Pasajero desde los datos del JOIN
        String dniPasajero = rs.getString("dni_pasajero");
        String nombrePasajero = rs.getString("nombre_pasajero");
        String apellidoPasajero = rs.getString("apellido_pasajero");
        String emailPasajero = rs.getString("email_pasajero");
        String telefonoPasajero = rs.getString("telefono_pasajero");
        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        LocalDate fechaNac = fechaNacimiento != null ? fechaNacimiento.toLocalDate() : null;
        String nacionalidad = rs.getString("nacionalidad");
        String numeroPasaporte = rs.getString("numero_pasaporte");
        
        Pasajero pasajero = new Pasajero(
            dniPasajero,
            nombrePasajero,
            apellidoPasajero,
            emailPasajero,
            telefonoPasajero,
            fechaNac,
            nacionalidad,
            numeroPasaporte
        );
        
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
        
        // Crear Vuelo desde los datos del JOIN
        String numeroVuelo = rs.getString("numero_vuelo");
        String origen = rs.getString("origen");
        String destino = rs.getString("destino");
        Timestamp fechaSalida = rs.getTimestamp("fecha_hora_salida");
        Timestamp fechaLlegada = rs.getTimestamp("fecha_hora_llegada");
        LocalDateTime fechaHoraSalida = fechaSalida != null ? fechaSalida.toLocalDateTime() : null;
        LocalDateTime fechaHoraLlegada = fechaLlegada != null ? fechaLlegada.toLocalDateTime() : null;
        Double precio = rs.getDouble("precio");
        int asientosDisponibles = rs.getInt("asientos_disponibles");
        String estadoVueloStr = rs.getString("estado_vuelo");
        
        Vuelo vuelo = new Vuelo(
            numeroVuelo,
            origen,
            destino,
            fechaHoraSalida,
            fechaHoraLlegada,
            avion,
            precio
        );
        vuelo.setAsientosDisponibles(asientosDisponibles);
        try {
            vuelo.setEstado(EstadoVuelo.valueOf(estadoVueloStr));
        } catch (IllegalArgumentException e) {
            vuelo.setEstado(EstadoVuelo.PROGRAMADO);
        }
        
        // Crear Reserva
        Reserva reserva = new Reserva(
            numero_reserva,
            pasajero,
            vuelo,
            numero_asiento
        );
        reserva.setFechaReserva(fechaRes);
        
        // Establecer estado
        try {
            reserva.setEstado(EstadoReserva.valueOf(estado_reserva));
        } catch (IllegalArgumentException e) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }
        
        return reserva;
    }

    /**
     * Mapea un ResultSet a un objeto Reserva (método original que hace consultas adicionales)
     */
    private Reserva mapResultSetToReserva(ResultSet rs) throws SQLException {
        Timestamp fechaReserva = rs.getTimestamp("fecha_reserva");
        LocalDateTime fechaRes = fechaReserva != null ? fechaReserva.toLocalDateTime() : LocalDateTime.now();
        String numeroVuelo = rs.getString("numero_vuelo");
        String numero_reserva = rs.getString("numero_reserva");
        int numero_asiento = rs.getInt("numero_asiento");
        String estado = rs.getString("estado");

        // Obtener pasajero
        String dniPasajero = rs.getString("dni_pasajero");
        Pasajero pasajero = pasajeroRepository.buscarPorDni(dniPasajero)
                .orElseThrow(() -> new SQLException("Pasajero no encontrado: " + dniPasajero));
        
        // Obtener vuelo

        Vuelo vuelo = vueloRepository.buscarPorNumeroVuelo(numeroVuelo)
                .orElseThrow(() -> new SQLException("Vuelo no encontrado: " + numeroVuelo));
        
        Reserva reserva = new Reserva(
            numero_reserva,
            pasajero,
            vuelo,
                numero_asiento
        );
        reserva.setFechaReserva(fechaRes);
        // Establecer estado
        String estadoStr = estado;
        try {
            reserva.setEstado(EstadoReserva.valueOf(estadoStr));
        } catch (IllegalArgumentException e) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }
        
        return reserva;
    }

    /**
     * Mapea un ResultSet del procedimiento almacenado a un objeto Reserva
     */
    private Reserva mapResultSetToReservaFromProcedure(ResultSet rs, String dniPasajero) throws SQLException {
        Timestamp fechaReserva = rs.getTimestamp("fecha_reserva");
        LocalDateTime fechaRes = fechaReserva != null ? fechaReserva.toLocalDateTime() : LocalDateTime.now();
        
        // Obtener pasajero
        Pasajero pasajero = pasajeroRepository.buscarPorDni(dniPasajero)
                .orElseThrow(() -> new SQLException("Pasajero no encontrado: " + dniPasajero));
        
        // Crear vuelo básico desde el procedimiento
        Timestamp fechaSalida = rs.getTimestamp("fecha_hora_salida");
        Timestamp fechaLlegada = rs.getTimestamp("fecha_hora_llegada");
        
        LocalDateTime fechaHoraSalida = fechaSalida != null ? fechaSalida.toLocalDateTime() : null;
        LocalDateTime fechaHoraLlegada = fechaLlegada != null ? fechaLlegada.toLocalDateTime() : null;
        
        // Necesitamos obtener el vuelo completo
        String numeroVuelo = rs.getString("numero_vuelo");
        Vuelo vuelo = vueloRepository.buscarPorNumeroVuelo(numeroVuelo)
                .orElseThrow(() -> new SQLException("Vuelo no encontrado: " + numeroVuelo));
        
        Reserva reserva = new Reserva(
            rs.getString("numero_reserva"),
            pasajero,
            vuelo,
            rs.getInt("numero_asiento")
        );
        
        reserva.setFechaReserva(fechaRes);
        
        // Establecer estado
        String estadoStr = rs.getString("estado");
        try {
            reserva.setEstado(EstadoReserva.valueOf(estadoStr));
        } catch (IllegalArgumentException e) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }
        
        return reserva;
    }
}
