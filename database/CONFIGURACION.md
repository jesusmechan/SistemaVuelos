# Configuración de Base de Datos

## Requisitos Previos

1. **SQL Server instalado**
   - SQL Server 2016 o superior
   - SQL Server Express (gratuito) también funciona
   - Servidor corriendo en el puerto 1433 (por defecto)

2. **Driver JDBC de SQL Server**
   - Descargar desde: https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
   - O usar Maven/Gradle para gestionar dependencias

## Instalación

### Paso 1: Crear la Base de Datos

Ejecutar el script SQL principal:

**Desde SQL Server Management Studio (SSMS):**
1. Abrir SSMS
2. Conectarse al servidor SQL Server
3. File > Open > File
4. Seleccionar `script_bd.sql`
5. Ejecutar el script (F5 o botón Execute)

**Desde la línea de comandos:**
```bash
sqlcmd -S localhost -U sa -P TuContrasena123 -i script_bd.sql
```

**Desde Azure Data Studio:**
1. Abrir Azure Data Studio
2. Conectarse al servidor
3. Abrir el archivo `script_bd.sql`
4. Ejecutar el script

### Paso 2: Configurar la Conexión en Java

#### Opción A: Usando Maven

Agregar la dependencia en `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
        <version>12.4.2.jre8</version>
    </dependency>
</dependencies>
```

#### Opción B: Descargar JAR manualmente

1. Descargar `mssql-jdbc-12.4.2.jre8.jar` desde el sitio oficial de Microsoft
2. Agregar el JAR al classpath del proyecto

### Paso 3: Configurar Credenciales

Editar el archivo `src/database/ConexionBD.java` y actualizar:

```java
private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=sistema_vuelos;encrypt=true;trustServerCertificate=true";
private static final String USUARIO = "sa";  // Cambiar aquí
private static final String CONTRASENA = "TuContrasena123";  // Cambiar aquí
```

**Nota sobre la URL:**
- `encrypt=true`: Habilita el cifrado
- `trustServerCertificate=true`: Confía en el certificado del servidor (útil para desarrollo)
- Para producción, configurar certificados SSL apropiados

## Estructura de Archivos

```
SistemaVuelos/
├── database/
│   ├── script_bd.sql          # Script principal de creación
│   ├── EJEMPLOS_USO.sql       # Ejemplos de uso de procedimientos
│   ├── README_DATABASE.md     # Documentación completa
│   └── CONFIGURACION.md       # Este archivo
└── src/
    └── database/
        └── ConexionBD.java    # Clase de conexión
```

## Ejemplo de Uso

### Ejemplo 1: Conexión Básica

```java
import database.ConexionBD;
import java.sql.Connection;
import java.sql.Statement;

public class EjemploConexion {
    public static void main(String[] args) {
        ConexionBD conexionBD = ConexionBD.getInstancia();
        
        try {
            Connection conn = conexionBD.getConexion();
            Statement stmt = conn.createStatement();
            
            // Ejecutar consulta
            String sql = "SELECT * FROM aviones";
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                System.out.println("Avión: " + rs.getString("modelo"));
            }
            
            conexionBD.commit();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            conexionBD.cerrarConexion();
        }
    }
}
```

### Ejemplo 2: Usar Procedimiento Almacenado

```java
import database.ConexionBD;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class EjemploProcedimiento {
    public static void main(String[] args) {
        ConexionBD conexionBD = ConexionBD.getInstancia();
        
        try {
            Connection conn = conexionBD.getConexion();
            
            // Llamar procedimiento almacenado
            String sql = "{CALL sp_buscar_vuelos_disponibles(?, ?, ?)}";
            CallableStatement cstmt = conn.prepareCall(sql);
            
            cstmt.setString(1, "Lima");      // origen
            cstmt.setString(2, "Cusco");     // destino
            cstmt.setDate(3, null);          // fecha (null = todas)
            
            ResultSet rs = cstmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("Vuelo: " + rs.getString("numero_vuelo"));
                System.out.println("Precio: " + rs.getDouble("precio"));
            }
            
            conexionBD.commit();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            conexionBD.cerrarConexion();
        }
    }
}
```

### Ejemplo 3: Procedimiento con Parámetros OUT

```java
import database.ConexionBD;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class EjemploProcedimientoOUT {
    public static void main(String[] args) {
        ConexionBD conexionBD = ConexionBD.getInstancia();
        
        try {
            Connection conn = conexionBD.getConexion();
            
            // Llamar procedimiento con parámetros OUT
            String sql = "{CALL sp_crear_reserva(?, ?, ?, ?, ?, ?)}";
            CallableStatement cstmt = conn.prepareCall(sql);
            
            // Parámetros IN
            cstmt.setString(1, "RES001");
            cstmt.setString(2, "87654321");
            cstmt.setString(3, "VUELO001");
            cstmt.setInt(4, 1);
            
            // Registrar parámetros OUT (BIT en SQL Server se mapea a Types.BIT)
            cstmt.registerOutParameter(5, Types.BIT);
            cstmt.registerOutParameter(6, Types.VARCHAR);
            
            cstmt.execute();
            
            // Obtener resultados
            boolean resultado = cstmt.getBoolean(5);
            String mensaje = cstmt.getString(6);
            
            System.out.println("Resultado: " + resultado);
            System.out.println("Mensaje: " + mensaje);
            
            if (resultado) {
                conexionBD.commit();
            } else {
                conexionBD.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            try {
                conexionBD.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            conexionBD.cerrarConexion();
        }
    }
}
```

## Solución de Problemas

### Error: "ClassNotFoundException: com.microsoft.sqlserver.jdbc.SQLServerDriver"

**Solución**: El driver JDBC no está en el classpath. Agregar el JAR de SQL Server JDBC Driver.

### Error: "Login failed for user"

**Solución**: 
- Verificar que las credenciales en `ConexionBD.java` sean correctas
- Verificar que SQL Server esté configurado para autenticación SQL (no solo Windows)
- En SQL Server Configuration Manager, habilitar "SQL Server and Windows Authentication mode"

### Error: "Cannot open database 'sistema_vuelos'"

**Solución**: Ejecutar primero el script `script_bd.sql` para crear la base de datos.

### Error: "The TCP/IP connection to the host has failed"

**Solución**: 
- Verificar que SQL Server esté corriendo
- Verificar que el puerto 1433 esté abierto
- En SQL Server Configuration Manager, habilitar TCP/IP en SQL Server Network Configuration
- Verificar que SQL Server Browser esté corriendo

### Error: "Encryption not supported"

**Solución**: Si estás usando una versión antigua de SQL Server, cambiar la URL a:
```java
private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=sistema_vuelos;encrypt=false";
```

## Configuración Avanzada

### Usar Pool de Conexiones (Recomendado para Producción)

Para aplicaciones en producción, se recomienda usar un pool de conexiones como HikariCP o C3P0.

Ejemplo con HikariCP:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
```

### Variables de Entorno

Para mayor seguridad, usar variables de entorno o archivo de configuración:

```java
private static final String URL = System.getenv("DB_URL");
private static final String USUARIO = System.getenv("DB_USER");
private static final String CONTRASENA = System.getenv("DB_PASSWORD");
```

### Configuración de SQL Server para Desarrollo

Si es la primera vez que usas SQL Server:

1. **Instalar SQL Server Express** (gratuito): https://www.microsoft.com/en-us/sql-server/sql-server-downloads

2. **Habilitar autenticación SQL**:
   - Abrir SQL Server Management Studio
   - Conectarse con autenticación de Windows
   - Click derecho en el servidor > Properties > Security
   - Seleccionar "SQL Server and Windows Authentication mode"
   - Reiniciar el servicio SQL Server

3. **Habilitar TCP/IP**:
   - Abrir SQL Server Configuration Manager
   - SQL Server Network Configuration > Protocols for [INSTANCE]
   - Habilitar TCP/IP
   - Reiniciar el servicio SQL Server

4. **Crear usuario SA** (si no existe):
   - En SSMS: Security > Logins > New Login
   - Login name: sa
   - Seleccionar "SQL Server authentication"
   - Establecer contraseña
   - Habilitar la cuenta

## Próximos Pasos

1. Integrar la clase `ConexionBD` en los repositorios existentes
2. Reemplazar las estructuras en memoria por consultas a la base de datos
3. Implementar transacciones para operaciones complejas
4. Agregar manejo de excepciones específicas de SQL

