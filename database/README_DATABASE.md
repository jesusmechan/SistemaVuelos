# Base de Datos - Sistema de Vuelos

## Descripción

Este documento describe la estructura de la base de datos y los procedimientos almacenados del Sistema de Vuelos.

## Requisitos

- SQL Server 2016 o superior
- SQL Server Management Studio (SSMS) o Azure Data Studio
- Usuario con permisos de CREATE DATABASE, CREATE TABLE, CREATE PROCEDURE

## Instalación

1. Abrir SQL Server Management Studio (SSMS)
2. Conectarse al servidor SQL Server
3. Abrir el archivo `script_bd.sql`
4. Ejecutar el script completo (F5 o botón Execute)

O desde la línea de comandos:
```bash
sqlcmd -S localhost -U sa -P TuContrasena123 -i script_bd.sql
```

## Estructura de la Base de Datos

### Tablas Principales

1. **aviones**: Almacena información de los aviones
   - `numero_serie` (PK): Identificador único del avión
   - `modelo`, `fabricante`: Información del avión
   - `capacidad_pasajeros`, `capacidad_carga`: Capacidades
   - `estado`: DISPONIBLE, EN_MANTENIMIENTO, EN_VUELO, FUERA_SERVICIO

2. **personas**: Tabla base para pasajeros y empleados
   - `dni` (PK): Documento de identidad
   - `nombre`, `apellido`, `email`, `telefono`: Datos personales
   - `tipo`: PASAJERO o EMPLEADO

3. **pasajeros**: Extiende personas con datos específicos
   - `dni` (PK, FK a personas)
   - `fecha_nacimiento`, `nacionalidad`, `numero_pasaporte`

4. **empleados**: Extiende personas con datos laborales
   - `dni` (PK, FK a personas)
   - `numero_empleado`, `cargo`, `fecha_contratacion`, `salario`

5. **usuarios**: Credenciales de acceso al sistema
   - `nombre_usuario` (PK)
   - `contrasena`, `rol`: ADMINISTRADOR, OPERADOR, VENDEDOR
   - `dni_empleado` (FK a empleados)

6. **vuelos**: Información de los vuelos
   - `numero_vuelo` (PK)
   - `origen`, `destino`: Ciudades
   - `fecha_hora_salida`, `fecha_hora_llegada`: Fechas y horas
   - `numero_serie_avion` (FK a aviones)
   - `precio`, `asientos_disponibles`
   - `estado`: PROGRAMADO, EN_ABORDAR, EN_VUELO, COMPLETADO, CANCELADO

7. **reservas**: Reservas de vuelos
   - `numero_reserva` (PK)
   - `dni_pasajero` (FK a pasajeros)
   - `numero_vuelo` (FK a vuelos)
   - `numero_asiento`, `precio_pagado`
   - `estado`: CONFIRMADA, PENDIENTE, CANCELADA, COMPLETADA

## Procedimientos Almacenados

### 1. `sp_autenticar_usuario`
Autentica un usuario en el sistema.

**Parámetros:**
- `p_nombre_usuario` (IN): Nombre de usuario
- `p_contrasena` (IN): Contraseña
- `p_resultado` (OUT): TRUE si la autenticación fue exitosa
- `p_rol` (OUT): Rol del usuario autenticado
- `p_dni_empleado` (OUT): DNI del empleado asociado

**Ejemplo:**
```sql
DECLARE @resultado BIT, @rol VARCHAR(20), @dni VARCHAR(20);
EXEC sp_autenticar_usuario 'admin', 'admin123', @resultado OUTPUT, @rol OUTPUT, @dni OUTPUT;
SELECT @resultado, @rol, @dni;
```

### 2. `sp_crear_reserva`
Crea una nueva reserva validando disponibilidad.

**Parámetros:**
- `p_numero_reserva` (IN): Número único de reserva
- `p_dni_pasajero` (IN): DNI del pasajero
- `p_numero_vuelo` (IN): Número del vuelo
- `p_numero_asiento` (IN): Número de asiento
- `p_resultado` (OUT): TRUE si se creó exitosamente
- `p_mensaje` (OUT): Mensaje descriptivo del resultado

**Ejemplo:**
```sql
DECLARE @resultado BIT, @mensaje VARCHAR(255);
EXEC sp_crear_reserva 'RES001', '87654321', 'VUELO001', 1, @resultado OUTPUT, @mensaje OUTPUT;
SELECT @resultado, @mensaje;
```

### 3. `sp_cancelar_reserva`
Cancela una reserva existente y libera el asiento.

**Parámetros:**
- `p_numero_reserva` (IN): Número de reserva a cancelar
- `p_resultado` (OUT): TRUE si se canceló exitosamente
- `p_mensaje` (OUT): Mensaje descriptivo

**Ejemplo:**
```sql
DECLARE @resultado BIT, @mensaje VARCHAR(255);
EXEC sp_cancelar_reserva 'RES001', @resultado OUTPUT, @mensaje OUTPUT;
SELECT @resultado, @mensaje;
```

### 4. `sp_buscar_vuelos_disponibles`
Busca vuelos disponibles con filtros opcionales.

**Parámetros:**
- `p_origen` (IN): Ciudad de origen (NULL para todos)
- `p_destino` (IN): Ciudad de destino (NULL para todos)
- `p_fecha` (IN): Fecha de salida (NULL para todas las fechas)

**Ejemplo:**
```sql
-- Todos los vuelos disponibles
EXEC sp_buscar_vuelos_disponibles;

-- Vuelos Lima a Cusco
EXEC sp_buscar_vuelos_disponibles @p_origen = 'Lima', @p_destino = 'Cusco';
```

### 5. `sp_actualizar_estado_vuelo`
Actualiza el estado de un vuelo y del avión asociado.

**Parámetros:**
- `p_numero_vuelo` (IN): Número del vuelo
- `p_nuevo_estado` (IN): Nuevo estado
- `p_resultado` (OUT): TRUE si se actualizó exitosamente
- `p_mensaje` (OUT): Mensaje descriptivo

**Ejemplo:**
```sql
DECLARE @resultado BIT, @mensaje VARCHAR(255);
EXEC sp_actualizar_estado_vuelo 'VUELO001', 'EN_VUELO', @resultado OUTPUT, @mensaje OUTPUT;
SELECT @resultado, @mensaje;
```

### 6. `sp_obtener_reservas_pasajero`
Obtiene todas las reservas de un pasajero.

**Parámetros:**
- `p_dni_pasajero` (IN): DNI del pasajero

**Ejemplo:**
```sql
EXEC sp_obtener_reservas_pasajero @p_dni_pasajero = '87654321';
```

### 7. `sp_obtener_estadisticas`
Obtiene estadísticas generales del sistema.

**Ejemplo:**
```sql
EXEC sp_obtener_estadisticas;
```

### 8. `sp_obtener_asientos_disponibles`
Obtiene los asientos ocupados de un vuelo.

**Parámetros:**
- `p_numero_vuelo` (IN): Número del vuelo

**Ejemplo:**
```sql
EXEC sp_obtener_asientos_disponibles @p_numero_vuelo = 'VUELO001';
```

## Vistas

### `vw_vuelos_completos`
Vista que muestra vuelos con información completa del avión y número de reservas activas.

### `vw_reservas_completas`
Vista que muestra reservas con información completa del pasajero y vuelo.

### `vw_empleados_usuarios`
Vista que muestra empleados con su información de usuario asociada.

## Triggers

1. **trg_validar_reserva_vuelo**: Valida que no se puedan crear reservas en vuelos cancelados o completados.
2. **trg_actualizar_asientos_crear**: Actualiza automáticamente los asientos disponibles al crear una reserva.
3. **trg_actualizar_asientos_cancelar**: Libera asientos al cancelar una reserva.

## Índices

La base de datos incluye índices en:
- Campos de búsqueda frecuente (email, nombre, apellido)
- Claves foráneas
- Campos de estado
- Combinaciones de campos usados en búsquedas (origen-destino)

## Datos de Prueba

El script incluye datos iniciales:
- 3 aviones de ejemplo
- 1 usuario administrador (admin/admin123)

## Notas Importantes

1. **Seguridad**: Las contraseñas se almacenan en texto plano en este script. En producción, usar hash (SHA-256, bcrypt, etc.).

2. **Integridad Referencial**: Las relaciones están configuradas con ON DELETE RESTRICT para evitar eliminaciones accidentales.

3. **Validaciones**: Los triggers y procedimientos almacenados incluyen validaciones de negocio.

4. **Rendimiento**: Los índices están optimizados para las consultas más frecuentes.

## Conexión desde Java

Para conectar desde Java, usar JDBC:

```java
String url = "jdbc:sqlserver://localhost:1433;databaseName=sistema_vuelos;encrypt=true;trustServerCertificate=true";
String user = "sa";
String password = "tu_contraseña";
Connection conn = DriverManager.getConnection(url, user, password);
```

## Mantenimiento

- Realizar backups regulares de la base de datos
- Monitorear el rendimiento de las consultas
- Revisar y optimizar índices según el uso real
- Actualizar estadísticas de tablas periódicamente

## Soporte

Para más información sobre el uso de los procedimientos almacenados, consultar el archivo `EJEMPLOS_USO.sql`.

