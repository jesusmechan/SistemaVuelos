# Cambios Realizados para Integración con SQL Server

## Resumen

Se han actualizado todos los repositorios del proyecto para que utilicen SQL Server en lugar de estructuras en memoria. Los cambios mantienen la misma interfaz, por lo que los servicios no requieren modificaciones adicionales.

## Archivos Modificados

### Repositorios Actualizados

1. **AvionRepository.java**
   - Implementa todas las operaciones CRUD usando SQL Server
   - Mapea objetos `Avion` desde/hacia la base de datos
   - Maneja estados de avión (ENUM)

2. **PasajeroRepository.java**
   - Maneja la inserción en dos tablas: `personas` y `pasajeros`
   - Usa transacciones para garantizar integridad
   - Mapea correctamente las relaciones entre tablas

3. **EmpleadoRepository.java**
   - Maneja la inserción en dos tablas: `personas` y `empleados`
   - Implementa soft delete (marca como inactivo en lugar de eliminar)
   - Soporta búsqueda por número de empleado

4. **UsuarioRepository.java**
   - Implementa autenticación usando el procedimiento almacenado `sp_autenticar_usuario`
   - Maneja relaciones con empleados
   - Mapea roles correctamente

5. **VueloRepository.java**
   - Implementa todas las operaciones CRUD
   - Incluye método `buscarVuelosDisponibles()` que usa el procedimiento almacenado
   - Mapea correctamente las relaciones con aviones

6. **ReservaRepository.java**
   - Usa procedimientos almacenados para crear y cancelar reservas
   - `guardar()` usa `sp_crear_reserva` que valida y actualiza asientos automáticamente
   - `eliminar()` usa `sp_cancelar_reserva` que cancela y libera asientos
   - `buscarPorPasajero()` usa `sp_obtener_reservas_pasajero`

### Servicios Actualizados

1. **ReservaService.java**
   - Eliminada la lógica manual de reserva/liberación de asientos
   - Ahora confía en los procedimientos almacenados para manejar esto automáticamente
   - `cancelarReserva()` ahora usa el método `eliminar()` del repositorio

## Características Implementadas

### Manejo de Transacciones
- Todos los repositorios usan `ConexionBD` para gestionar conexiones
- Las operaciones que modifican múltiples tablas usan transacciones
- Rollback automático en caso de error

### Procedimientos Almacenados Utilizados
- `sp_autenticar_usuario`: Autenticación de usuarios
- `sp_crear_reserva`: Creación de reservas con validaciones
- `sp_cancelar_reserva`: Cancelación de reservas y liberación de asientos
- `sp_buscar_vuelos_disponibles`: Búsqueda de vuelos disponibles
- `sp_obtener_reservas_pasajero`: Obtener reservas de un pasajero

### Mapeo de Datos
- Conversión automática entre tipos Java y SQL Server
- Manejo de fechas (`LocalDate`, `LocalDateTime` ↔ `DATE`, `DATETIME2`)
- Manejo de ENUMs (conversión a String y viceversa)
- Manejo de relaciones entre entidades

## Configuración Requerida

### 1. Base de Datos
- Ejecutar el script `database/script_bd.sql` en SQL Server
- Verificar que la base de datos `sistema_vuelos` esté creada

### 2. Credenciales
- Actualizar `ConexionBD.java` con las credenciales correctas:
  ```java
  private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=sistema_vuelos;encrypt=true;trustServerCertificate=true";
  private static final String USUARIO = "sa";
  private static final String CONTRASENA = "TuContrasena123";
  ```

### 3. Driver JDBC
- Agregar el driver de SQL Server al proyecto:
  - Maven: `com.microsoft.sqlserver:mssql-jdbc`
  - O descargar el JAR manualmente

## Comportamiento de los Métodos

### guardar()
- **AvionRepository**: INSERT en tabla `aviones`
- **PasajeroRepository**: INSERT en `personas` y `pasajeros` (transacción)
- **EmpleadoRepository**: INSERT en `personas` y `empleados` (transacción)
- **UsuarioRepository**: INSERT en tabla `usuarios`
- **VueloRepository**: INSERT en tabla `vuelos`
- **ReservaRepository**: Usa `sp_crear_reserva` (valida y actualiza asientos)

### eliminar()
- **AvionRepository**: DELETE físico
- **PasajeroRepository**: DELETE en `personas` (CASCADE elimina de `pasajeros`)
- **EmpleadoRepository**: UPDATE `activo = 0` (soft delete)
- **UsuarioRepository**: UPDATE `activo = 0` (soft delete)
- **VueloRepository**: DELETE físico
- **ReservaRepository**: Usa `sp_cancelar_reserva` (cancela y libera asientos)

## Manejo de Errores

Todos los repositorios lanzan `RuntimeException` con mensajes descriptivos en caso de error. Los servicios existentes capturan estas excepciones y las convierten a excepciones de dominio apropiadas.

## Próximos Pasos Recomendados

1. **Probar la conexión**: Verificar que la aplicación se conecta correctamente a SQL Server
2. **Probar operaciones CRUD**: Verificar que todas las operaciones funcionan correctamente
3. **Agregar métodos de actualización**: Si es necesario, agregar métodos `actualizar()` a los repositorios
4. **Optimizar consultas**: Revisar y optimizar consultas según el uso real
5. **Agregar logging**: Considerar agregar logging para operaciones de base de datos

## Notas Importantes

- Los repositorios mantienen la misma interfaz, por lo que los servicios no requieren cambios
- Las validaciones de negocio se mantienen en los servicios
- Los procedimientos almacenados manejan validaciones a nivel de base de datos
- Se recomienda usar un pool de conexiones en producción (HikariCP, C3P0, etc.)


