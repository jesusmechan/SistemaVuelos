package repository;

import database.ConexionBD;
import model.Usuario;
import model.Rol;
import model.Empleado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de repositorio de Usuarios con SQL Server
 */
public class UsuarioRepository implements IUsuarioRepository {
    private final ConexionBD conexionBD;

    public UsuarioRepository() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "{CALL sp_crear_usuario(?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, usuario.getNombreUsuario());
            cstmt.setString(2, usuario.getContrasena());
            cstmt.setString(3, usuario.getRol().toString());
            cstmt.setString(4, usuario.getEmpleado().getDni());
            cstmt.registerOutParameter(5, Types.BIT);
            cstmt.registerOutParameter(6, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(5);
            String mensaje = cstmt.getString(6);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
                throw new RuntimeException("Error al guardar usuario: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT u.*, e.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM usuarios u " +
                     "INNER JOIN empleados e ON u.dni_empleado = e.dni " +
                     "INNER JOIN personas per ON e.dni = per.dni " +
                     "WHERE u.nombre_usuario = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreUsuario);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT u.*, e.*, per.nombre, per.apellido, per.email, per.telefono " +
                     "FROM usuarios u " +
                     "INNER JOIN empleados e ON u.dni_empleado = e.dni " +
                     "INNER JOIN personas per ON e.dni = per.dni " +
                     "ORDER BY u.nombre_usuario";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = conexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }
        
        return usuarios;
    }

    @Override
    public boolean eliminar(String nombreUsuario) {
        String sql = "{CALL sp_actualizar_estado_usuario(?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, nombreUsuario);
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
                throw new RuntimeException("Error al eliminar usuario: " + mensaje);
            }
        } catch (SQLException e) {
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback", ex);
            }
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String nombreUsuario) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ?";
        
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreUsuario);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de usuario: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Autentica un usuario usando el procedimiento almacenado
     */
    public Optional<Usuario> autenticar(String nombreUsuario, String contrasena) {
        String sql = "{CALL sp_autenticar_usuario(?, ?, ?, ?, ?)}";
        
        try (Connection conn = conexionBD.getConexion();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, nombreUsuario);
            cstmt.setString(2, contrasena);
            cstmt.registerOutParameter(3, Types.BIT);
            cstmt.registerOutParameter(4, Types.VARCHAR);
            cstmt.registerOutParameter(5, Types.VARCHAR);
            
            cstmt.execute();
            
            boolean resultado = cstmt.getBoolean(3);
            if (resultado) {
                // Si la autenticación fue exitosa, obtener el usuario completo
                return buscarPorNombreUsuario(nombreUsuario);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al autenticar usuario: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    /**
     * Mapea un ResultSet a un objeto Usuario
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        // Crear empleado
        Date fechaContratacion = rs.getDate("fecha_contratacion");
        java.time.LocalDate fechaCont = fechaContratacion != null ? fechaContratacion.toLocalDate() : null;
        
        Empleado empleado = new Empleado(
            rs.getString("dni_empleado"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("telefono"),
            rs.getString("numero_empleado"),
            rs.getString("cargo"),
            fechaCont,
            rs.getDouble("salario")
        );
        
        // Crear usuario
        String rolStr = rs.getString("rol");
        Rol rol;
        try {
            rol = Rol.valueOf(rolStr);
        } catch (IllegalArgumentException e) {
            rol = Rol.VENDEDOR; // Valor por defecto
        }
        
        Usuario usuario = new Usuario(
            rs.getString("nombre_usuario"),
            rs.getString("contrasena"),
            rol,
            empleado
        );
        
        return usuario;
    }
}
