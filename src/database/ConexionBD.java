package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para gestionar la conexión a la base de datos
 * Implementa patrón Singleton para una única instancia de conexión
 */
public class ConexionBD {
    // Configuración de la base de datos SQL Server
//    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=sistema_vuelos;encrypt=true;trustServerCertificate=true";
//    private static final String URL ="jdbc:sqlserver://JESUSMECHAN\\SQLEXPRESS:1433;databaseName=sistema_vuelos;encrypt=true;trustServerCertificate=true;";
    private static final String URL =
            "jdbc:sqlserver://sistemavuelos.mssql.somee.com:1433;"
                    + "databaseName=sistemavuelos;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    private static final String USUARIO = "jesusmechan_SQLLogin_1";
    private static final String CONTRASENA = "vxiaarfrbc";
    
    private static ConexionBD instancia;
    private Connection conexion;
    
    /**
     * Constructor privado para implementar Singleton
     */
    private ConexionBD() {
        try {
            // Cargar el driver de SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de SQL Server: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene la instancia única de ConexionBD (Singleton)
     * @return Instancia de ConexionBD
     */
    public static ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }
    
    /**
     * Obtiene una conexión a la base de datos
     * @return Objeto Connection
     * @throws SQLException Si hay error al conectar
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
            // Configurar autocommit a false para manejar transacciones
            conexion.setAutoCommit(false);
        }
        return conexion;
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    /**
     * Confirma los cambios en la base de datos (commit)
     */
    public void commit() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            conexion.commit();
        }
    }
    
    /**
     * Revierte los cambios en la base de datos (rollback)
     */
    public void rollback() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            conexion.rollback();
        }
    }
    
    /**
     * Verifica si la conexión está activa
     * @return true si la conexión está activa, false en caso contrario
     */
    public boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

