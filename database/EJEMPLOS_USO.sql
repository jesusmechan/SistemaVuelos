-- =====================================================
-- EJEMPLOS DE USO DE PROCEDIMIENTOS ALMACENADOS
-- Sistema de Vuelos - SQL Server
-- =====================================================

USE sistema_vuelos;
GO

-- =====================================================
-- 1. AUTENTICACIÓN DE USUARIOS
-- =====================================================

-- Ejemplo: Autenticar un usuario
DECLARE @resultado BIT;
DECLARE @rol VARCHAR(20);
DECLARE @dni_empleado VARCHAR(20);

EXEC sp_autenticar_usuario 
    @p_nombre_usuario = 'admin',
    @p_contrasena = 'admin123',
    @p_resultado = @resultado OUTPUT,
    @p_rol = @rol OUTPUT,
    @p_dni_empleado = @dni_empleado OUTPUT;

SELECT @resultado AS autenticado, @rol AS rol, @dni_empleado AS dni_empleado;
GO

-- =====================================================
-- 2. CREAR RESERVAS
-- =====================================================

-- Primero, crear un pasajero de ejemplo
INSERT INTO personas (dni, nombre, apellido, email, telefono, tipo) VALUES
('87654321', 'Juan', 'Pérez', 'juan.perez@email.com', '987654321', 'PASAJERO');
GO

INSERT INTO pasajeros (dni, fecha_nacimiento, nacionalidad, numero_pasaporte) VALUES
('87654321', '1990-05-15', 'Peruana', 'PP123456');
GO

-- Crear una reserva
DECLARE @resultado_reserva BIT;
DECLARE @mensaje_reserva VARCHAR(255);

EXEC sp_crear_reserva 
    @p_numero_reserva = 'RES001',
    @p_dni_pasajero = '87654321',
    @p_numero_vuelo = 'VUELO001',
    @p_numero_asiento = 1,
    @p_resultado = @resultado_reserva OUTPUT,
    @p_mensaje = @mensaje_reserva OUTPUT;

SELECT @resultado_reserva AS exito, @mensaje_reserva AS mensaje;
GO

-- =====================================================
-- 3. BUSCAR VUELOS DISPONIBLES
-- =====================================================

-- Buscar todos los vuelos disponibles
EXEC sp_buscar_vuelos_disponibles;
GO

-- Buscar vuelos por origen
EXEC sp_buscar_vuelos_disponibles @p_origen = 'Lima';
GO

-- Buscar vuelos por origen y destino
EXEC sp_buscar_vuelos_disponibles 
    @p_origen = 'Lima',
    @p_destino = 'Cusco';
GO

-- Buscar vuelos por fecha específica
EXEC sp_buscar_vuelos_disponibles 
    @p_fecha = '2024-12-25';
GO

-- Buscar vuelos con todos los filtros
EXEC sp_buscar_vuelos_disponibles 
    @p_origen = 'Lima',
    @p_destino = 'Cusco',
    @p_fecha = '2024-12-25';
GO

-- =====================================================
-- 4. CANCELAR RESERVAS
-- =====================================================

DECLARE @resultado_cancelar BIT;
DECLARE @mensaje_cancelar VARCHAR(255);

EXEC sp_cancelar_reserva 
    @p_numero_reserva = 'RES001',
    @p_resultado = @resultado_cancelar OUTPUT,
    @p_mensaje = @mensaje_cancelar OUTPUT;

SELECT @resultado_cancelar AS exito, @mensaje_cancelar AS mensaje;
GO

-- =====================================================
-- 5. ACTUALIZAR ESTADO DE VUELOS
-- =====================================================

DECLARE @resultado_estado BIT;
DECLARE @mensaje_estado VARCHAR(255);

-- Cambiar estado a EN_ABORDAR
EXEC sp_actualizar_estado_vuelo 
    @p_numero_vuelo = 'VUELO001',
    @p_nuevo_estado = 'EN_ABORDAR',
    @p_resultado = @resultado_estado OUTPUT,
    @p_mensaje = @mensaje_estado OUTPUT;

SELECT @resultado_estado AS exito, @mensaje_estado AS mensaje;
GO

-- Cambiar estado a EN_VUELO
DECLARE @resultado_estado2 BIT;
DECLARE @mensaje_estado2 VARCHAR(255);

EXEC sp_actualizar_estado_vuelo 
    @p_numero_vuelo = 'VUELO001',
    @p_nuevo_estado = 'EN_VUELO',
    @p_resultado = @resultado_estado2 OUTPUT,
    @p_mensaje = @mensaje_estado2 OUTPUT;

SELECT @resultado_estado2 AS exito, @mensaje_estado2 AS mensaje;
GO

-- Cambiar estado a COMPLETADO
DECLARE @resultado_estado3 BIT;
DECLARE @mensaje_estado3 VARCHAR(255);

EXEC sp_actualizar_estado_vuelo 
    @p_numero_vuelo = 'VUELO001',
    @p_nuevo_estado = 'COMPLETADO',
    @p_resultado = @resultado_estado3 OUTPUT,
    @p_mensaje = @mensaje_estado3 OUTPUT;

SELECT @resultado_estado3 AS exito, @mensaje_estado3 AS mensaje;
GO

-- =====================================================
-- 6. OBTENER RESERVAS DE UN PASAJERO
-- =====================================================

EXEC sp_obtener_reservas_pasajero @p_dni_pasajero = '87654321';
GO

-- =====================================================
-- 7. OBTENER ESTADÍSTICAS DEL SISTEMA
-- =====================================================

EXEC sp_obtener_estadisticas;
GO

-- =====================================================
-- 8. OBTENER ASIENTOS DISPONIBLES
-- =====================================================

EXEC sp_obtener_asientos_disponibles @p_numero_vuelo = 'VUELO001';
GO

-- =====================================================
-- CONSULTAS ÚTILES ADICIONALES
-- =====================================================

-- Ver todos los vuelos completos
SELECT * FROM vw_vuelos_completos;
GO

-- Ver todas las reservas completas
SELECT * FROM vw_reservas_completas;
GO

-- Ver empleados con usuarios
SELECT * FROM vw_empleados_usuarios;
GO

-- Consultar vuelos por rango de fechas
SELECT * FROM vw_vuelos_completos
WHERE fecha_hora_salida BETWEEN '2024-12-01' AND '2024-12-31'
ORDER BY fecha_hora_salida;
GO

-- Consultar reservas confirmadas de hoy
SELECT * FROM vw_reservas_completas
WHERE CAST(fecha_reserva AS DATE) = CAST(GETDATE() AS DATE)
  AND estado = 'CONFIRMADA';
GO

-- Consultar pasajeros frecuentes (más de 3 reservas)
SELECT 
    dni_pasajero,
    nombre_pasajero,
    COUNT(*) AS total_reservas
FROM vw_reservas_completas
WHERE estado != 'CANCELADA'
GROUP BY dni_pasajero, nombre_pasajero
HAVING COUNT(*) > 3
ORDER BY total_reservas DESC;
GO

-- Consultar vuelos con mayor ocupación
SELECT 
    numero_vuelo,
    origen,
    destino,
    capacidad_pasajeros,
    asientos_disponibles,
    (capacidad_pasajeros - asientos_disponibles) AS asientos_ocupados,
    CAST(((capacidad_pasajeros - asientos_disponibles) * 100.0 / capacidad_pasajeros) AS DECIMAL(5,2)) AS porcentaje_ocupacion
FROM vw_vuelos_completos
WHERE estado IN ('PROGRAMADO', 'EN_ABORDAR')
ORDER BY porcentaje_ocupacion DESC;
GO
