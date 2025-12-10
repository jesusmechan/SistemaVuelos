-- =====================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS - SISTEMA DE VUELOS
-- =====================================================
-- Base de datos: sistema_vuelos
-- Motor: SQL Server 2016+
-- =====================================================

-- Crear base de datos
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'sistema_vuelos')
    DROP DATABASE sistema_vuelos;
GO

CREATE DATABASE sistema_vuelos;
GO

USE sistema_vuelos;
GO

-- =====================================================
-- TIPOS DE DATOS PERSONALIZADOS (Para ENUMs)
-- =====================================================

-- Tipo para estado de avión
IF NOT EXISTS (SELECT * FROM sys.types WHERE name = 'EstadoAvion')
    CREATE TYPE EstadoAvion AS VARCHAR(20);
GO

-- Tipo para estado de vuelo
IF NOT EXISTS (SELECT * FROM sys.types WHERE name = 'EstadoVuelo')
    CREATE TYPE EstadoVuelo AS VARCHAR(20);
GO

-- Tipo para estado de reserva
IF NOT EXISTS (SELECT * FROM sys.types WHERE name = 'EstadoReserva')
    CREATE TYPE EstadoReserva AS VARCHAR(20);
GO

-- Tipo para tipo de persona
IF NOT EXISTS (SELECT * FROM sys.types WHERE name = 'TipoPersona')
    CREATE TYPE TipoPersona AS VARCHAR(20);
GO

-- Tipo para rol de usuario
IF NOT EXISTS (SELECT * FROM sys.types WHERE name = 'RolUsuario')
    CREATE TYPE RolUsuario AS VARCHAR(20);
GO

-- =====================================================
-- TABLAS PRINCIPALES
-- =====================================================

-- Tabla: aviones
CREATE TABLE aviones (
    numero_serie VARCHAR(50) PRIMARY KEY,
    modelo VARCHAR(100) NOT NULL,
    fabricante VARCHAR(100) NOT NULL,
    capacidad_pasajeros INT NOT NULL CHECK (capacidad_pasajeros > 0),
    capacidad_carga INT NOT NULL CHECK (capacidad_carga >= 0),
    estado EstadoAvion NOT NULL DEFAULT 'DISPONIBLE',
    fecha_creacion DATETIME2 DEFAULT GETDATE(),
    fecha_actualizacion DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT CHK_EstadoAvion CHECK (estado IN ('DISPONIBLE', 'EN_MANTENIMIENTO', 'EN_VUELO', 'FUERA_SERVICIO'))
);
GO

CREATE INDEX idx_estado ON aviones(estado);
CREATE INDEX idx_modelo ON aviones(modelo);
GO

-- Tabla: personas (tabla base para pasajeros y empleados)
CREATE TABLE personas (
    dni VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    tipo TipoPersona NOT NULL,
    fecha_creacion DATETIME2 DEFAULT GETDATE(),
    fecha_actualizacion DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT CHK_TipoPersona CHECK (tipo IN ('PASAJERO', 'EMPLEADO'))
);
GO

CREATE INDEX idx_email ON personas(email);
CREATE INDEX idx_tipo ON personas(tipo);
CREATE INDEX idx_nombre_apellido ON personas(nombre, apellido);
GO

-- Tabla: pasajeros (extiende personas)
CREATE TABLE pasajeros (
    dni VARCHAR(20) PRIMARY KEY,
    fecha_nacimiento DATE NOT NULL,
    nacionalidad VARCHAR(100) NOT NULL,
    numero_pasaporte VARCHAR(50) UNIQUE,
    FOREIGN KEY (dni) REFERENCES personas(dni) ON DELETE CASCADE
);
GO

CREATE INDEX idx_pasaporte ON pasajeros(numero_pasaporte);
CREATE INDEX idx_nacionalidad ON pasajeros(nacionalidad);
GO

-- Tabla: empleados (extiende personas)
CREATE TABLE empleados (
    dni VARCHAR(20) PRIMARY KEY,
    numero_empleado VARCHAR(50) NOT NULL UNIQUE,
    cargo VARCHAR(100) NOT NULL,
    fecha_contratacion DATE NOT NULL,
    salario DECIMAL(10, 2) NOT NULL CHECK (salario >= 0),
    activo BIT DEFAULT 1,
    FOREIGN KEY (dni) REFERENCES personas(dni) ON DELETE CASCADE
);
GO

CREATE INDEX idx_numero_empleado ON empleados(numero_empleado);
CREATE INDEX idx_cargo ON empleados(cargo);
CREATE INDEX idx_activo ON empleados(activo);
GO

-- Tabla: usuarios
CREATE TABLE usuarios (
    nombre_usuario VARCHAR(50) PRIMARY KEY,
    contrasena VARCHAR(255) NOT NULL,
    rol RolUsuario NOT NULL,
    dni_empleado VARCHAR(20) NOT NULL,
    activo BIT DEFAULT 1,
    fecha_creacion DATETIME2 DEFAULT GETDATE(),
    ultimo_acceso DATETIME2 NULL,
    FOREIGN KEY (dni_empleado) REFERENCES empleados(dni) ON DELETE NO ACTION,
    CONSTRAINT CHK_RolUsuario CHECK (rol IN ('ADMINISTRADOR', 'OPERADOR', 'VENDEDOR'))
);
GO

CREATE INDEX idx_rol ON usuarios(rol);
CREATE INDEX idx_activo ON usuarios(activo);
GO

-- Tabla: vuelos
CREATE TABLE vuelos (
    numero_vuelo VARCHAR(50) PRIMARY KEY,
    origen VARCHAR(100) NOT NULL,
    destino VARCHAR(100) NOT NULL,
    fecha_hora_salida DATETIME2 NOT NULL,
    fecha_hora_llegada DATETIME2 NOT NULL,
    numero_serie_avion VARCHAR(50) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio >= 0),
    asientos_disponibles INT NOT NULL CHECK (asientos_disponibles >= 0),
    estado EstadoVuelo NOT NULL DEFAULT 'PROGRAMADO',
    fecha_creacion DATETIME2 DEFAULT GETDATE(),
    fecha_actualizacion DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (numero_serie_avion) REFERENCES aviones(numero_serie) ON DELETE NO ACTION,
    CONSTRAINT CHK_EstadoVuelo CHECK (estado IN ('PROGRAMADO', 'EN_ABORDAR', 'EN_VUELO', 'COMPLETADO', 'CANCELADO')),
    CONSTRAINT CHK_FechaValida CHECK (fecha_hora_llegada > fecha_hora_salida)
);
GO

CREATE INDEX idx_origen ON vuelos(origen);
CREATE INDEX idx_destino ON vuelos(destino);
CREATE INDEX idx_fecha_salida ON vuelos(fecha_hora_salida);
CREATE INDEX idx_estado ON vuelos(estado);
CREATE INDEX idx_origen_destino ON vuelos(origen, destino);
GO

-- Tabla: reservas
CREATE TABLE reservas (
    numero_reserva VARCHAR(50) PRIMARY KEY,
    dni_pasajero VARCHAR(20) NOT NULL,
    numero_vuelo VARCHAR(50) NOT NULL,
    fecha_reserva DATETIME2 NOT NULL DEFAULT GETDATE(),
    estado EstadoReserva NOT NULL DEFAULT 'CONFIRMADA',
    numero_asiento INT NOT NULL CHECK (numero_asiento > 0),
    precio_pagado DECIMAL(10, 2) NOT NULL CHECK (precio_pagado >= 0),
    fecha_creacion DATETIME2 DEFAULT GETDATE(),
    fecha_actualizacion DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (dni_pasajero) REFERENCES pasajeros(dni) ON DELETE NO ACTION,
    FOREIGN KEY (numero_vuelo) REFERENCES vuelos(numero_vuelo) ON DELETE NO ACTION,
    CONSTRAINT CHK_EstadoReserva CHECK (estado IN ('CONFIRMADA', 'PENDIENTE', 'CANCELADA', 'COMPLETADA')),
    CONSTRAINT UK_VueloAsiento UNIQUE (numero_vuelo, numero_asiento)
);
GO

CREATE INDEX idx_pasajero ON reservas(dni_pasajero);
CREATE INDEX idx_vuelo ON reservas(numero_vuelo);
CREATE INDEX idx_fecha_reserva ON reservas(fecha_reserva);
CREATE INDEX idx_estado ON reservas(estado);
GO

-- Trigger para actualizar fecha_actualizacion automáticamente
CREATE TRIGGER trg_aviones_update ON aviones
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE aviones
    SET fecha_actualizacion = GETDATE()
    FROM aviones a
    INNER JOIN inserted i ON a.numero_serie = i.numero_serie;
END;
GO

CREATE TRIGGER trg_personas_update ON personas
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE personas
    SET fecha_actualizacion = GETDATE()
    FROM personas p
    INNER JOIN inserted i ON p.dni = i.dni;
END;
GO

CREATE TRIGGER trg_vuelos_update ON vuelos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE vuelos
    SET fecha_actualizacion = GETDATE()
    FROM vuelos v
    INNER JOIN inserted i ON v.numero_vuelo = i.numero_vuelo;
END;
GO

CREATE TRIGGER trg_reservas_update ON reservas
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE reservas
    SET fecha_actualizacion = GETDATE()
    FROM reservas r
    INNER JOIN inserted i ON r.numero_reserva = i.numero_reserva;
END;
GO

-- =====================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================

-- Procedimiento: Autenticar usuario
IF OBJECT_ID('sp_autenticar_usuario', 'P') IS NOT NULL
    DROP PROCEDURE sp_autenticar_usuario;
GO

CREATE PROCEDURE sp_autenticar_usuario
    @p_nombre_usuario VARCHAR(50),
    @p_contrasena VARCHAR(255),
    @p_resultado BIT OUTPUT,
    @p_rol VARCHAR(20) OUTPUT,
    @p_dni_empleado VARCHAR(20) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_activo BIT;
    
    SELECT @v_activo = u.activo, 
           @p_rol = u.rol, 
           @p_dni_empleado = u.dni_empleado
    FROM usuarios u
    WHERE u.nombre_usuario = @p_nombre_usuario 
      AND u.contrasena = @p_contrasena;
    
    IF @v_activo IS NOT NULL AND @v_activo = 1
    BEGIN
        SET @p_resultado = 1;
        -- Actualizar último acceso
        UPDATE usuarios 
        SET ultimo_acceso = GETDATE()
        WHERE nombre_usuario = @p_nombre_usuario;
    END
    ELSE
    BEGIN
        SET @p_resultado = 0;
        SET @p_rol = NULL;
        SET @p_dni_empleado = NULL;
    END
END;
GO

-- Procedimiento: Crear reserva
IF OBJECT_ID('sp_crear_reserva', 'P') IS NOT NULL
    DROP PROCEDURE sp_crear_reserva;
GO

CREATE PROCEDURE sp_crear_reserva
    @p_numero_reserva VARCHAR(50),
    @p_dni_pasajero VARCHAR(20),
    @p_numero_vuelo VARCHAR(50),
    @p_numero_asiento INT,
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_asientos_disponibles INT;
    DECLARE @v_precio DECIMAL(10, 2);
    DECLARE @v_estado_vuelo VARCHAR(20);
    DECLARE @v_existe_reserva INT;
    
    -- Verificar que el vuelo existe y está disponible
    SELECT @v_asientos_disponibles = asientos_disponibles, 
           @v_precio = precio, 
           @v_estado_vuelo = estado
    FROM vuelos 
    WHERE numero_vuelo = @p_numero_vuelo;
    
    IF @v_asientos_disponibles IS NULL
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El vuelo no existe';
        RETURN;
    END
    
    IF @v_estado_vuelo != 'PROGRAMADO' AND @v_estado_vuelo != 'EN_ABORDAR'
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El vuelo no está disponible. Estado: ' + @v_estado_vuelo;
        RETURN;
    END
    
    IF @v_asientos_disponibles <= 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No hay asientos disponibles';
        RETURN;
    END
    
    -- Verificar que el asiento no esté ocupado
    SELECT @v_existe_reserva = COUNT(*)
    FROM reservas
    WHERE numero_vuelo = @p_numero_vuelo 
      AND numero_asiento = @p_numero_asiento
      AND estado != 'CANCELADA';
    
    IF @v_existe_reserva > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El asiento ya está ocupado';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Crear la reserva
        INSERT INTO reservas (
            numero_reserva, dni_pasajero, numero_vuelo, 
            numero_asiento, precio_pagado, estado
        ) VALUES (
            @p_numero_reserva, @p_dni_pasajero, @p_numero_vuelo,
            @p_numero_asiento, @v_precio, 'CONFIRMADA'
        );
        
        -- Actualizar asientos disponibles
        UPDATE vuelos 
        SET asientos_disponibles = asientos_disponibles - 1
        WHERE numero_vuelo = @p_numero_vuelo;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Reserva creada exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Cancelar reserva
IF OBJECT_ID('sp_cancelar_reserva', 'P') IS NOT NULL
    DROP PROCEDURE sp_cancelar_reserva;
GO

CREATE PROCEDURE sp_cancelar_reserva
    @p_numero_reserva VARCHAR(50),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_estado VARCHAR(20);
    DECLARE @v_numero_vuelo VARCHAR(50);
    
    SELECT @v_estado = estado, 
           @v_numero_vuelo = numero_vuelo
    FROM reservas
    WHERE numero_reserva = @p_numero_reserva;
    
    IF @v_estado IS NULL
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'La reserva no existe';
        RETURN;
    END
    
    IF @v_estado = 'CANCELADA'
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'La reserva ya está cancelada';
        RETURN;
    END
    
    IF @v_estado = 'COMPLETADA'
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No se puede cancelar una reserva completada';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Cancelar la reserva
        UPDATE reservas 
        SET estado = 'CANCELADA',
            fecha_actualizacion = GETDATE()
        WHERE numero_reserva = @p_numero_reserva;
        
        -- Liberar el asiento
        UPDATE vuelos 
        SET asientos_disponibles = asientos_disponibles + 1
        WHERE numero_vuelo = @v_numero_vuelo;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Reserva cancelada exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Buscar vuelos disponibles
IF OBJECT_ID('sp_buscar_vuelos_disponibles', 'P') IS NOT NULL
    DROP PROCEDURE sp_buscar_vuelos_disponibles;
GO

CREATE PROCEDURE sp_buscar_vuelos_disponibles
    @p_origen VARCHAR(100) = NULL,
    @p_destino VARCHAR(100) = NULL,
    @p_fecha DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        v.numero_vuelo,
        v.origen,
        v.destino,
        v.fecha_hora_salida,
        v.fecha_hora_llegada,
        v.precio,
        v.asientos_disponibles,
        v.estado,
        a.modelo,
        a.fabricante,
        a.capacidad_pasajeros
    FROM vuelos v
    INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie
    WHERE (@p_origen IS NULL OR v.origen = @p_origen)
      AND (@p_destino IS NULL OR v.destino = @p_destino)
      AND (@p_fecha IS NULL OR CAST(v.fecha_hora_salida AS DATE) = @p_fecha)
      AND v.estado IN ('PROGRAMADO', 'EN_ABORDAR')
      AND v.asientos_disponibles > 0
    ORDER BY v.fecha_hora_salida ASC;
END;
GO

-- Procedimiento: Actualizar estado de vuelo
IF OBJECT_ID('sp_actualizar_estado_vuelo', 'P') IS NOT NULL
    DROP PROCEDURE sp_actualizar_estado_vuelo;
GO

CREATE PROCEDURE sp_actualizar_estado_vuelo
    @p_numero_vuelo VARCHAR(50),
    @p_nuevo_estado VARCHAR(20),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_estado_actual VARCHAR(20);
    
    SELECT @v_estado_actual = estado
    FROM vuelos
    WHERE numero_vuelo = @p_numero_vuelo;
    
    IF @v_estado_actual IS NULL
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El vuelo no existe';
        RETURN;
    END
    
    IF @v_estado_actual = @p_nuevo_estado
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El vuelo ya está en ese estado';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        UPDATE vuelos 
        SET estado = @p_nuevo_estado,
            fecha_actualizacion = GETDATE()
        WHERE numero_vuelo = @p_numero_vuelo;
        
        -- Si el vuelo pasa a EN_VUELO, actualizar estado del avión
        IF @p_nuevo_estado = 'EN_VUELO'
        BEGIN
            UPDATE aviones
            SET estado = 'EN_VUELO'
            FROM aviones a
            INNER JOIN vuelos v ON a.numero_serie = v.numero_serie_avion
            WHERE v.numero_vuelo = @p_numero_vuelo;
        END
        
        -- Si el vuelo se completa o cancela, liberar el avión
        IF @p_nuevo_estado IN ('COMPLETADO', 'CANCELADO')
        BEGIN
            UPDATE aviones
            SET estado = 'DISPONIBLE'
            FROM aviones a
            INNER JOIN vuelos v ON a.numero_serie = v.numero_serie_avion
            WHERE v.numero_vuelo = @p_numero_vuelo;
        END
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Estado actualizado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Obtener reservas por pasajero
IF OBJECT_ID('sp_obtener_reservas_pasajero', 'P') IS NOT NULL
    DROP PROCEDURE sp_obtener_reservas_pasajero;
GO

CREATE PROCEDURE sp_obtener_reservas_pasajero
    @p_dni_pasajero VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        r.numero_reserva,
        r.fecha_reserva,
        r.estado,
        r.numero_asiento,
        r.precio_pagado,
        v.numero_vuelo,
        v.origen,
        v.destino,
        v.fecha_hora_salida,
        v.fecha_hora_llegada,
        p.nombre,
        p.apellido
    FROM reservas r
    INNER JOIN vuelos v ON r.numero_vuelo = v.numero_vuelo
    INNER JOIN pasajeros pas ON r.dni_pasajero = pas.dni
    INNER JOIN personas p ON pas.dni = p.dni
    WHERE r.dni_pasajero = @p_dni_pasajero
    ORDER BY r.fecha_reserva DESC;
END;
GO

-- Procedimiento: Obtener estadísticas del sistema
IF OBJECT_ID('sp_obtener_estadisticas', 'P') IS NOT NULL
    DROP PROCEDURE sp_obtener_estadisticas;
GO

CREATE PROCEDURE sp_obtener_estadisticas
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        (SELECT COUNT(*) FROM aviones WHERE estado = 'DISPONIBLE') AS aviones_disponibles,
        (SELECT COUNT(*) FROM vuelos WHERE estado = 'PROGRAMADO') AS vuelos_programados,
        (SELECT COUNT(*) FROM vuelos WHERE estado = 'EN_VUELO') AS vuelos_en_vuelo,
        (SELECT COUNT(*) FROM reservas WHERE estado = 'CONFIRMADA') AS reservas_confirmadas,
        (SELECT COUNT(*) FROM pasajeros) AS total_pasajeros,
        (SELECT COUNT(*) FROM empleados WHERE activo = 1) AS empleados_activos,
        (SELECT ISNULL(SUM(precio_pagado), 0) FROM reservas WHERE estado = 'CONFIRMADA') AS ingresos_totales,
        (SELECT COUNT(*) FROM vuelos WHERE CAST(fecha_hora_salida AS DATE) = CAST(GETDATE() AS DATE)) AS vuelos_hoy;
END;
GO

-- Procedimiento: Obtener asientos disponibles de un vuelo
IF OBJECT_ID('sp_obtener_asientos_disponibles', 'P') IS NOT NULL
    DROP PROCEDURE sp_obtener_asientos_disponibles;
GO

CREATE PROCEDURE sp_obtener_asientos_disponibles
    @p_numero_vuelo VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_capacidad_total INT;
    DECLARE @v_asientos_disponibles INT;
    
    SELECT @v_capacidad_total = a.capacidad_pasajeros, 
           @v_asientos_disponibles = v.asientos_disponibles
    FROM vuelos v
    INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie
    WHERE v.numero_vuelo = @p_numero_vuelo;
    
    IF @v_capacidad_total IS NULL
    BEGIN
        SELECT 'El vuelo no existe' AS mensaje;
    END
    ELSE
    BEGIN
        -- Obtener asientos ocupados
        SELECT 
            numero_asiento,
            'OCUPADO' AS estado
        FROM reservas
        WHERE numero_vuelo = @p_numero_vuelo
          AND estado != 'CANCELADA'
        ORDER BY numero_asiento;
    END
END;
GO

-- Procedimiento: Listar todas las reservas con información completa
IF OBJECT_ID('sp_listar_reservas', 'P') IS NOT NULL
    DROP PROCEDURE sp_listar_reservas;
GO

CREATE PROCEDURE sp_listar_reservas
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        r.numero_reserva, 
        r.fecha_reserva, 
        r.estado AS estado_reserva, 
        r.numero_asiento, 
        r.precio_pagado, 
        p.dni AS dni_pasajero, 
        p.nombre AS nombre_pasajero, 
        p.apellido AS apellido_pasajero, 
        p.email AS email_pasajero, 
        p.telefono AS telefono_pasajero, 
        pas.fecha_nacimiento, 
        pas.nacionalidad, 
        pas.numero_pasaporte, 
        v.numero_vuelo, 
        v.origen, 
        v.destino, 
        v.fecha_hora_salida, 
        v.fecha_hora_llegada, 
        v.precio, 
        v.asientos_disponibles, 
        v.estado AS estado_vuelo, 
        a.numero_serie, 
        a.modelo, 
        a.fabricante, 
        a.capacidad_pasajeros, 
        a.capacidad_carga, 
        a.estado AS estado_avion 
    FROM reservas r 
    INNER JOIN pasajeros pas ON r.dni_pasajero = pas.dni 
    INNER JOIN personas p ON pas.dni = p.dni 
    INNER JOIN vuelos v ON r.numero_vuelo = v.numero_vuelo 
    INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie 
    ORDER BY r.fecha_reserva DESC;
END;
GO

-- Procedimiento: Buscar reserva por número de reserva
IF OBJECT_ID('sp_buscar_reserva_por_numero', 'P') IS NOT NULL
    DROP PROCEDURE sp_buscar_reserva_por_numero;
GO

CREATE PROCEDURE sp_buscar_reserva_por_numero
    @p_numero_reserva VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        r.numero_reserva, 
        r.fecha_reserva, 
        r.estado AS estado_reserva, 
        r.numero_asiento, 
        r.precio_pagado, 
        p.dni AS dni_pasajero, 
        p.nombre AS nombre_pasajero, 
        p.apellido AS apellido_pasajero, 
        p.email AS email_pasajero, 
        p.telefono AS telefono_pasajero, 
        pas.fecha_nacimiento, 
        pas.nacionalidad, 
        pas.numero_pasaporte, 
        v.numero_vuelo, 
        v.origen, 
        v.destino, 
        v.fecha_hora_salida, 
        v.fecha_hora_llegada, 
        v.precio, 
        v.asientos_disponibles, 
        v.estado AS estado_vuelo, 
        a.numero_serie, 
        a.modelo, 
        a.fabricante, 
        a.capacidad_pasajeros, 
        a.capacidad_carga, 
        a.estado AS estado_avion 
    FROM reservas r 
    INNER JOIN pasajeros pas ON r.dni_pasajero = pas.dni 
    INNER JOIN personas p ON pas.dni = p.dni 
    INNER JOIN vuelos v ON r.numero_vuelo = v.numero_vuelo 
    INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie 
    WHERE r.numero_reserva = @p_numero_reserva;
END;
GO

-- Procedimiento: Buscar reservas por número de vuelo
IF OBJECT_ID('sp_buscar_reservas_por_vuelo', 'P') IS NOT NULL
    DROP PROCEDURE sp_buscar_reservas_por_vuelo;
GO

CREATE PROCEDURE sp_buscar_reservas_por_vuelo
    @p_numero_vuelo VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        r.numero_reserva, 
        r.fecha_reserva, 
        r.estado AS estado_reserva, 
        r.numero_asiento, 
        r.precio_pagado, 
        p.dni AS dni_pasajero, 
        p.nombre AS nombre_pasajero, 
        p.apellido AS apellido_pasajero, 
        p.email AS email_pasajero, 
        p.telefono AS telefono_pasajero, 
        pas.fecha_nacimiento, 
        pas.nacionalidad, 
        pas.numero_pasaporte, 
        v.numero_vuelo, 
        v.origen, 
        v.destino, 
        v.fecha_hora_salida, 
        v.fecha_hora_llegada, 
        v.precio, 
        v.asientos_disponibles, 
        v.estado AS estado_vuelo, 
        a.numero_serie, 
        a.modelo, 
        a.fabricante, 
        a.capacidad_pasajeros, 
        a.capacidad_carga, 
        a.estado AS estado_avion 
    FROM reservas r 
    INNER JOIN pasajeros pas ON r.dni_pasajero = pas.dni 
    INNER JOIN personas p ON pas.dni = p.dni 
    INNER JOIN vuelos v ON r.numero_vuelo = v.numero_vuelo 
    INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie 
    WHERE r.numero_vuelo = @p_numero_vuelo 
    ORDER BY r.numero_asiento;
END;
GO

-- Procedimiento: Buscar reservas por fecha
IF OBJECT_ID('sp_buscar_reservas_por_fecha', 'P') IS NOT NULL
    DROP PROCEDURE sp_buscar_reservas_por_fecha;
GO

CREATE PROCEDURE sp_buscar_reservas_por_fecha
    @p_fecha DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        r.numero_reserva, 
        r.fecha_reserva, 
        r.estado AS estado_reserva, 
        r.numero_asiento, 
        r.precio_pagado, 
        p.dni AS dni_pasajero, 
        p.nombre AS nombre_pasajero, 
        p.apellido AS apellido_pasajero, 
        p.email AS email_pasajero, 
        p.telefono AS telefono_pasajero, 
        pas.fecha_nacimiento, 
        pas.nacionalidad, 
        pas.numero_pasaporte, 
        v.numero_vuelo, 
        v.origen, 
        v.destino, 
        v.fecha_hora_salida, 
        v.fecha_hora_llegada, 
        v.precio, 
        v.asientos_disponibles, 
        v.estado AS estado_vuelo, 
        a.numero_serie, 
        a.modelo, 
        a.fabricante, 
        a.capacidad_pasajeros, 
        a.capacidad_carga, 
        a.estado AS estado_avion 
    FROM reservas r 
    INNER JOIN pasajeros pas ON r.dni_pasajero = pas.dni 
    INNER JOIN personas p ON pas.dni = p.dni 
    INNER JOIN vuelos v ON r.numero_vuelo = v.numero_vuelo 
    INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie 
    WHERE CAST(r.fecha_reserva AS DATE) = @p_fecha 
    ORDER BY r.fecha_reserva DESC;
END;
GO

-- =====================================================
-- PROCEDIMIENTOS ALMACENADOS PARA INSERT Y UPDATE
-- =====================================================

-- Procedimiento: Crear usuario
IF OBJECT_ID('sp_crear_usuario', 'P') IS NOT NULL
    DROP PROCEDURE sp_crear_usuario;
GO

CREATE PROCEDURE sp_crear_usuario
    @p_nombre_usuario VARCHAR(50),
    @p_contrasena VARCHAR(255),
    @p_rol VARCHAR(20),
    @p_dni_empleado VARCHAR(20),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe_usuario INT;
    DECLARE @v_existe_empleado INT;
    
    -- Verificar que el usuario no exista
    SELECT @v_existe_usuario = COUNT(*)
    FROM usuarios
    WHERE nombre_usuario = @p_nombre_usuario;
    
    IF @v_existe_usuario > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El nombre de usuario ya existe';
        RETURN;
    END
    
    -- Verificar que el empleado exista
    SELECT @v_existe_empleado = COUNT(*)
    FROM empleados
    WHERE dni = @p_dni_empleado AND activo = 1;
    
    IF @v_existe_empleado = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El empleado no existe o está inactivo';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        INSERT INTO usuarios (nombre_usuario, contrasena, rol, dni_empleado, activo)
        VALUES (@p_nombre_usuario, @p_contrasena, @p_rol, @p_dni_empleado, 1);
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Usuario creado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Actualizar estado de usuario (desactivar)
IF OBJECT_ID('sp_actualizar_estado_usuario', 'P') IS NOT NULL
    DROP PROCEDURE sp_actualizar_estado_usuario;
GO

CREATE PROCEDURE sp_actualizar_estado_usuario
    @p_nombre_usuario VARCHAR(50),
    @p_activo BIT,
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    
    SELECT @v_existe = COUNT(*)
    FROM usuarios
    WHERE nombre_usuario = @p_nombre_usuario;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El usuario no existe';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        UPDATE usuarios
        SET activo = @p_activo
        WHERE nombre_usuario = @p_nombre_usuario;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Estado de usuario actualizado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Crear empleado
IF OBJECT_ID('sp_crear_empleado', 'P') IS NOT NULL
    DROP PROCEDURE sp_crear_empleado;
GO

CREATE PROCEDURE sp_crear_empleado
    @p_dni VARCHAR(20),
    @p_nombre VARCHAR(100),
    @p_apellido VARCHAR(100),
    @p_email VARCHAR(150),
    @p_telefono VARCHAR(20),
    @p_numero_empleado VARCHAR(50),
    @p_cargo VARCHAR(100),
    @p_fecha_contratacion DATE,
    @p_salario DECIMAL(10, 2),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe_persona INT;
    DECLARE @v_existe_empleado INT;
    DECLARE @v_existe_email INT;
    DECLARE @v_existe_numero_empleado INT;
    
    -- Verificar que la persona no exista
    SELECT @v_existe_persona = COUNT(*)
    FROM personas
    WHERE dni = @p_dni;
    
    IF @v_existe_persona > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'Ya existe una persona con ese DNI';
        RETURN;
    END
    
    -- Verificar que el email no esté en uso
    SELECT @v_existe_email = COUNT(*)
    FROM personas
    WHERE email = @p_email;
    
    IF @v_existe_email > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El email ya está en uso';
        RETURN;
    END
    
    -- Verificar que el número de empleado no esté en uso
    SELECT @v_existe_numero_empleado = COUNT(*)
    FROM empleados
    WHERE numero_empleado = @p_numero_empleado;
    
    IF @v_existe_numero_empleado > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El número de empleado ya está en uso';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Insertar en tabla personas
        INSERT INTO personas (dni, nombre, apellido, email, telefono, tipo)
        VALUES (@p_dni, @p_nombre, @p_apellido, @p_email, @p_telefono, 'EMPLEADO');
        
        -- Insertar en tabla empleados
        INSERT INTO empleados (dni, numero_empleado, cargo, fecha_contratacion, salario, activo)
        VALUES (@p_dni, @p_numero_empleado, @p_cargo, @p_fecha_contratacion, @p_salario, 1);
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Empleado creado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Actualizar estado de empleado (desactivar)
IF OBJECT_ID('sp_actualizar_estado_empleado', 'P') IS NOT NULL
    DROP PROCEDURE sp_actualizar_estado_empleado;
GO

CREATE PROCEDURE sp_actualizar_estado_empleado
    @p_dni VARCHAR(20),
    @p_activo BIT,
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    
    SELECT @v_existe = COUNT(*)
    FROM empleados
    WHERE dni = @p_dni;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El empleado no existe';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        UPDATE empleados
        SET activo = @p_activo
        WHERE dni = @p_dni;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Estado de empleado actualizado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Crear pasajero
IF OBJECT_ID('sp_crear_pasajero', 'P') IS NOT NULL
    DROP PROCEDURE sp_crear_pasajero;
GO

CREATE PROCEDURE sp_crear_pasajero
    @p_dni VARCHAR(20),
    @p_nombre VARCHAR(100),
    @p_apellido VARCHAR(100),
    @p_email VARCHAR(150),
    @p_telefono VARCHAR(20),
    @p_fecha_nacimiento DATE,
    @p_nacionalidad VARCHAR(100),
    @p_numero_pasaporte VARCHAR(50),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe_persona INT;
    DECLARE @v_existe_email INT;
    DECLARE @v_existe_pasaporte INT;
    
    -- Verificar que la persona no exista
    SELECT @v_existe_persona = COUNT(*)
    FROM personas
    WHERE dni = @p_dni;
    
    IF @v_existe_persona > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'Ya existe una persona con ese DNI';
        RETURN;
    END
    
    -- Verificar que el email no esté en uso
    SELECT @v_existe_email = COUNT(*)
    FROM personas
    WHERE email = @p_email;
    
    IF @v_existe_email > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El email ya está en uso';
        RETURN;
    END
    
    -- Verificar que el pasaporte no esté en uso (si se proporciona)
    IF @p_numero_pasaporte IS NOT NULL AND @p_numero_pasaporte != ''
    BEGIN
        SELECT @v_existe_pasaporte = COUNT(*)
        FROM pasajeros
        WHERE numero_pasaporte = @p_numero_pasaporte;
        
        IF @v_existe_pasaporte > 0
        BEGIN
            SET @p_resultado = 0;
            SET @p_mensaje = 'El número de pasaporte ya está en uso';
            RETURN;
        END
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Insertar en tabla personas
        INSERT INTO personas (dni, nombre, apellido, email, telefono, tipo)
        VALUES (@p_dni, @p_nombre, @p_apellido, @p_email, @p_telefono, 'PASAJERO');
        
        -- Insertar en tabla pasajeros
        INSERT INTO pasajeros (dni, fecha_nacimiento, nacionalidad, numero_pasaporte)
        VALUES (@p_dni, @p_fecha_nacimiento, @p_nacionalidad, @p_numero_pasaporte);
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Pasajero creado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Eliminar pasajero
IF OBJECT_ID('sp_eliminar_pasajero', 'P') IS NOT NULL
    DROP PROCEDURE sp_eliminar_pasajero;
GO

CREATE PROCEDURE sp_eliminar_pasajero
    @p_dni VARCHAR(20),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    DECLARE @v_tiene_reservas INT;
    
    -- Verificar que el pasajero exista
    SELECT @v_existe = COUNT(*)
    FROM pasajeros
    WHERE dni = @p_dni;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El pasajero no existe';
        RETURN;
    END
    
    -- Verificar que no tenga reservas activas
    SELECT @v_tiene_reservas = COUNT(*)
    FROM reservas
    WHERE dni_pasajero = @p_dni AND estado != 'CANCELADA';
    
    IF @v_tiene_reservas > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No se puede eliminar un pasajero con reservas activas';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Eliminar de personas (se elimina automáticamente de pasajeros por CASCADE)
        DELETE FROM personas
        WHERE dni = @p_dni AND tipo = 'PASAJERO';
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Pasajero eliminado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Crear avión
IF OBJECT_ID('sp_crear_avion', 'P') IS NOT NULL
    DROP PROCEDURE sp_crear_avion;
GO

CREATE PROCEDURE sp_crear_avion
    @p_numero_serie VARCHAR(50),
    @p_modelo VARCHAR(100),
    @p_fabricante VARCHAR(100),
    @p_capacidad_pasajeros INT,
    @p_capacidad_carga INT,
    @p_estado VARCHAR(20),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    
    -- Verificar que el avión no exista
    SELECT @v_existe = COUNT(*)
    FROM aviones
    WHERE numero_serie = @p_numero_serie;
    
    IF @v_existe > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'Ya existe un avión con ese número de serie';
        RETURN;
    END
    
    -- Validar capacidad
    IF @p_capacidad_pasajeros <= 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'La capacidad de pasajeros debe ser mayor a 0';
        RETURN;
    END
    
    IF @p_capacidad_carga < 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'La capacidad de carga no puede ser negativa';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        INSERT INTO aviones (numero_serie, modelo, fabricante, capacidad_pasajeros, capacidad_carga, estado)
        VALUES (@p_numero_serie, @p_modelo, @p_fabricante, @p_capacidad_pasajeros, @p_capacidad_carga, @p_estado);
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Avión creado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Actualizar avión
IF OBJECT_ID('sp_actualizar_avion', 'P') IS NOT NULL
    DROP PROCEDURE sp_actualizar_avion;
GO

CREATE PROCEDURE sp_actualizar_avion
    @p_numero_serie VARCHAR(50),
    @p_modelo VARCHAR(100) = NULL,
    @p_fabricante VARCHAR(100) = NULL,
    @p_capacidad_pasajeros INT = NULL,
    @p_capacidad_carga INT = NULL,
    @p_estado VARCHAR(20) = NULL,
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    DECLARE @v_en_uso INT;
    
    -- Verificar que el avión exista
    SELECT @v_existe = COUNT(*)
    FROM aviones
    WHERE numero_serie = @p_numero_serie;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El avión no existe';
        RETURN;
    END
    
    -- Si se intenta cambiar el estado a FUERA_SERVICIO, verificar que no esté en vuelo
    IF @p_estado = 'FUERA_SERVICIO'
    BEGIN
        SELECT @v_en_uso = COUNT(*)
        FROM vuelos
        WHERE numero_serie_avion = @p_numero_serie 
          AND estado IN ('PROGRAMADO', 'EN_ABORDAR', 'EN_VUELO');
        
        IF @v_en_uso > 0
        BEGIN
            SET @p_resultado = 0;
            SET @p_mensaje = 'No se puede poner fuera de servicio un avión con vuelos activos';
            RETURN;
        END
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        UPDATE aviones
        SET modelo = ISNULL(@p_modelo, modelo),
            fabricante = ISNULL(@p_fabricante, fabricante),
            capacidad_pasajeros = ISNULL(@p_capacidad_pasajeros, capacidad_pasajeros),
            capacidad_carga = ISNULL(@p_capacidad_carga, capacidad_carga),
            estado = ISNULL(@p_estado, estado)
        WHERE numero_serie = @p_numero_serie;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Avión actualizado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Eliminar avión
IF OBJECT_ID('sp_eliminar_avion', 'P') IS NOT NULL
    DROP PROCEDURE sp_eliminar_avion;
GO

CREATE PROCEDURE sp_eliminar_avion
    @p_numero_serie VARCHAR(50),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    DECLARE @v_en_uso INT;
    
    -- Verificar que el avión exista
    SELECT @v_existe = COUNT(*)
    FROM aviones
    WHERE numero_serie = @p_numero_serie;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El avión no existe';
        RETURN;
    END
    
    -- Verificar que no esté en uso en vuelos
    SELECT @v_en_uso = COUNT(*)
    FROM vuelos
    WHERE numero_serie_avion = @p_numero_serie;
    
    IF @v_en_uso > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No se puede eliminar un avión que tiene vuelos asociados';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        DELETE FROM aviones
        WHERE numero_serie = @p_numero_serie;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Avión eliminado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Crear vuelo
IF OBJECT_ID('sp_crear_vuelo', 'P') IS NOT NULL
    DROP PROCEDURE sp_crear_vuelo;
GO

CREATE PROCEDURE sp_crear_vuelo
    @p_numero_vuelo VARCHAR(50),
    @p_origen VARCHAR(100),
    @p_destino VARCHAR(100),
    @p_fecha_hora_salida DATETIME2,
    @p_fecha_hora_llegada DATETIME2,
    @p_numero_serie_avion VARCHAR(50),
    @p_precio DECIMAL(10, 2),
    @p_estado VARCHAR(20),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe_vuelo INT;
    DECLARE @v_existe_avion INT;
    DECLARE @v_estado_avion VARCHAR(20);
    DECLARE @v_capacidad_pasajeros INT;
    
    -- Verificar que el vuelo no exista
    SELECT @v_existe_vuelo = COUNT(*)
    FROM vuelos
    WHERE numero_vuelo = @p_numero_vuelo;
    
    IF @v_existe_vuelo > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'Ya existe un vuelo con ese número';
        RETURN;
    END
    
    -- Verificar que el avión exista
    SELECT @v_existe_avion = COUNT(*),
           @v_estado_avion = estado,
           @v_capacidad_pasajeros = capacidad_pasajeros
    FROM aviones
    WHERE numero_serie = @p_numero_serie_avion;
    
    IF @v_existe_avion = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El avión no existe';
        RETURN;
    END
    
    -- Verificar que el avión esté disponible
    IF @v_estado_avion != 'DISPONIBLE'
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El avión no está disponible. Estado: ' + @v_estado_avion;
        RETURN;
    END
    
    -- Validar fechas
    IF @p_fecha_hora_llegada <= @p_fecha_hora_salida
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'La fecha de llegada debe ser posterior a la fecha de salida';
        RETURN;
    END
    
    -- Validar precio
    IF @p_precio < 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El precio no puede ser negativo';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        INSERT INTO vuelos (
            numero_vuelo, origen, destino, fecha_hora_salida, fecha_hora_llegada,
            numero_serie_avion, precio, asientos_disponibles, estado
        )
        VALUES (
            @p_numero_vuelo, @p_origen, @p_destino, @p_fecha_hora_salida, @p_fecha_hora_llegada,
            @p_numero_serie_avion, @p_precio, @v_capacidad_pasajeros, @p_estado
        );
        
        -- Actualizar estado del avión si el vuelo está programado
        IF @p_estado = 'PROGRAMADO'
        BEGIN
            UPDATE aviones
            SET estado = 'EN_MANTENIMIENTO'
            WHERE numero_serie = @p_numero_serie_avion;
        END
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Vuelo creado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Actualizar vuelo
IF OBJECT_ID('sp_actualizar_vuelo', 'P') IS NOT NULL
    DROP PROCEDURE sp_actualizar_vuelo;
GO

CREATE PROCEDURE sp_actualizar_vuelo
    @p_numero_vuelo VARCHAR(50),
    @p_origen VARCHAR(100) = NULL,
    @p_destino VARCHAR(100) = NULL,
    @p_fecha_hora_salida DATETIME2 = NULL,
    @p_fecha_hora_llegada DATETIME2 = NULL,
    @p_precio DECIMAL(10, 2) = NULL,
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    DECLARE @v_estado VARCHAR(20);
    
    -- Verificar que el vuelo exista
    SELECT @v_existe = COUNT(*),
           @v_estado = estado
    FROM vuelos
    WHERE numero_vuelo = @p_numero_vuelo;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El vuelo no existe';
        RETURN;
    END
    
    -- No permitir actualizar vuelos completados o cancelados
    IF @v_estado IN ('COMPLETADO', 'CANCELADO')
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No se puede actualizar un vuelo completado o cancelado';
        RETURN;
    END
    
    -- Validar fechas si se proporcionan ambas
    IF @p_fecha_hora_salida IS NOT NULL AND @p_fecha_hora_llegada IS NOT NULL
    BEGIN
        IF @p_fecha_hora_llegada <= @p_fecha_hora_salida
        BEGIN
            SET @p_resultado = 0;
            SET @p_mensaje = 'La fecha de llegada debe ser posterior a la fecha de salida';
            RETURN;
        END
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        UPDATE vuelos
        SET origen = ISNULL(@p_origen, origen),
            destino = ISNULL(@p_destino, destino),
            fecha_hora_salida = ISNULL(@p_fecha_hora_salida, fecha_hora_salida),
            fecha_hora_llegada = ISNULL(@p_fecha_hora_llegada, fecha_hora_llegada),
            precio = ISNULL(@p_precio, precio)
        WHERE numero_vuelo = @p_numero_vuelo;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Vuelo actualizado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedimiento: Eliminar vuelo
IF OBJECT_ID('sp_eliminar_vuelo', 'P') IS NOT NULL
    DROP PROCEDURE sp_eliminar_vuelo;
GO

CREATE PROCEDURE sp_eliminar_vuelo
    @p_numero_vuelo VARCHAR(50),
    @p_resultado BIT OUTPUT,
    @p_mensaje VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe INT;
    DECLARE @v_estado VARCHAR(20);
    DECLARE @v_tiene_reservas INT;
    
    -- Verificar que el vuelo exista
    SELECT @v_existe = COUNT(*),
           @v_estado = estado
    FROM vuelos
    WHERE numero_vuelo = @p_numero_vuelo;
    
    IF @v_existe = 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'El vuelo no existe';
        RETURN;
    END
    
    -- No permitir eliminar vuelos en vuelo o completados
    IF @v_estado IN ('EN_VUELO', 'COMPLETADO')
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No se puede eliminar un vuelo en vuelo o completado';
        RETURN;
    END
    
    -- Verificar que no tenga reservas activas
    SELECT @v_tiene_reservas = COUNT(*)
    FROM reservas
    WHERE numero_vuelo = @p_numero_vuelo AND estado != 'CANCELADA';
    
    IF @v_tiene_reservas > 0
    BEGIN
        SET @p_resultado = 0;
        SET @p_mensaje = 'No se puede eliminar un vuelo con reservas activas';
        RETURN;
    END
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Obtener el avión antes de eliminar
        DECLARE @v_numero_serie_avion VARCHAR(50);
        SELECT @v_numero_serie_avion = numero_serie_avion
        FROM vuelos
        WHERE numero_vuelo = @p_numero_vuelo;
        
        -- Eliminar el vuelo
        DELETE FROM vuelos
        WHERE numero_vuelo = @p_numero_vuelo;
        
        -- Liberar el avión
        UPDATE aviones
        SET estado = 'DISPONIBLE'
        WHERE numero_serie = @v_numero_serie_avion;
        
        COMMIT TRANSACTION;
        
        SET @p_resultado = 1;
        SET @p_mensaje = 'Vuelo eliminado exitosamente';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @p_resultado = 0;
        SET @p_mensaje = ERROR_MESSAGE();
    END CATCH
END;
GO

-- =====================================================
-- TRIGGERS DE VALIDACIÓN
-- =====================================================

-- Trigger: Validar que no se puedan crear reservas en vuelos cancelados
IF OBJECT_ID('trg_validar_reserva_vuelo', 'TR') IS NOT NULL
    DROP TRIGGER trg_validar_reserva_vuelo;
GO

CREATE TRIGGER trg_validar_reserva_vuelo
ON reservas
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_estado_vuelo VARCHAR(20);
    DECLARE @v_asientos_disponibles INT;
    DECLARE @v_numero_vuelo VARCHAR(50);
    
    SELECT @v_numero_vuelo = numero_vuelo FROM inserted;
    
    SELECT @v_estado_vuelo = estado, 
           @v_asientos_disponibles = asientos_disponibles
    FROM vuelos
    WHERE numero_vuelo = @v_numero_vuelo;
    
    IF @v_estado_vuelo = 'CANCELADO' OR @v_estado_vuelo = 'COMPLETADO'
    BEGIN
        THROW 50000, 'No se puede crear una reserva en un vuelo cancelado o completado', 1;
        RETURN;
    END
    
    IF @v_asientos_disponibles <= 0
    BEGIN
        THROW 50001, 'No hay asientos disponibles en este vuelo', 1;
        RETURN;
    END
    
    -- Si pasa las validaciones, insertar normalmente
    INSERT INTO reservas
    SELECT * FROM inserted;
END;
GO

-- Trigger: Actualizar asientos disponibles al crear reserva
IF OBJECT_ID('trg_actualizar_asientos_crear', 'TR') IS NOT NULL
    DROP TRIGGER trg_actualizar_asientos_crear;
GO

CREATE TRIGGER trg_actualizar_asientos_crear
ON reservas
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE vuelos 
    SET asientos_disponibles = asientos_disponibles - 1
    FROM vuelos v
    INNER JOIN inserted i ON v.numero_vuelo = i.numero_vuelo
    WHERE i.estado = 'CONFIRMADA';
END;
GO

-- Trigger: Actualizar asientos disponibles al cancelar reserva
IF OBJECT_ID('trg_actualizar_asientos_cancelar', 'TR') IS NOT NULL
    DROP TRIGGER trg_actualizar_asientos_cancelar;
GO

CREATE TRIGGER trg_actualizar_asientos_cancelar
ON reservas
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Si se cancela una reserva confirmada, liberar el asiento
    UPDATE vuelos 
    SET asientos_disponibles = asientos_disponibles + 1
    FROM vuelos v
    INNER JOIN inserted i ON v.numero_vuelo = i.numero_vuelo
    INNER JOIN deleted d ON i.numero_reserva = d.numero_reserva
    WHERE d.estado = 'CONFIRMADA' AND i.estado = 'CANCELADA';
END;
GO

-- =====================================================
-- VISTAS ÚTILES
-- =====================================================

-- Vista: Vuelos con información completa
IF OBJECT_ID('vw_vuelos_completos', 'V') IS NOT NULL
    DROP VIEW vw_vuelos_completos;
GO

CREATE VIEW vw_vuelos_completos AS
SELECT 
    v.numero_vuelo,
    v.origen,
    v.destino,
    v.fecha_hora_salida,
    v.fecha_hora_llegada,
    v.precio,
    v.asientos_disponibles,
    v.estado AS estado_vuelo,
    a.numero_serie,
    a.modelo,
    a.fabricante,
    a.capacidad_pasajeros,
    a.estado AS estado_avion,
    (SELECT COUNT(*) FROM reservas r WHERE r.numero_vuelo = v.numero_vuelo AND r.estado != 'CANCELADA') AS reservas_activas
FROM vuelos v
INNER JOIN aviones a ON v.numero_serie_avion = a.numero_serie;
GO

-- Vista: Reservas con información completa
IF OBJECT_ID('vw_reservas_completas', 'V') IS NOT NULL
    DROP VIEW vw_reservas_completas;
GO

CREATE VIEW vw_reservas_completas AS
SELECT 
    r.numero_reserva,
    r.fecha_reserva,
    r.estado,
    r.numero_asiento,
    r.precio_pagado,
    p.dni AS dni_pasajero,
    p.nombre + ' ' + p.apellido AS nombre_pasajero,
    p.email AS email_pasajero,
    v.numero_vuelo,
    v.origen,
    v.destino,
    v.fecha_hora_salida,
    v.fecha_hora_llegada
FROM reservas r
INNER JOIN pasajeros pas ON r.dni_pasajero = pas.dni
INNER JOIN personas p ON pas.dni = p.dni
INNER JOIN vuelos v ON r.numero_vuelo = v.numero_vuelo;
GO

-- Vista: Empleados con información de usuario
IF OBJECT_ID('vw_empleados_usuarios', 'V') IS NOT NULL
    DROP VIEW vw_empleados_usuarios;
GO

CREATE VIEW vw_empleados_usuarios AS
SELECT 
    e.dni,
    p.nombre + ' ' + p.apellido AS nombre_completo,
    p.email,
    p.telefono,
    e.numero_empleado,
    e.cargo,
    e.fecha_contratacion,
    e.salario,
    e.activo,
    u.nombre_usuario,
    u.rol,
    u.ultimo_acceso
FROM empleados e
INNER JOIN personas p ON e.dni = p.dni
LEFT JOIN usuarios u ON e.dni = u.dni_empleado;
GO

-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- Insertar aviones de ejemplo
INSERT INTO aviones (numero_serie, modelo, fabricante, capacidad_pasajeros, capacidad_carga, estado) VALUES
('AV001', 'Boeing 737', 'Boeing', 180, 20000, 'DISPONIBLE'),
('AV002', 'Airbus A320', 'Airbus', 150, 18000, 'DISPONIBLE'),
('AV003', 'Boeing 787', 'Boeing', 250, 35000, 'DISPONIBLE');
GO

-- Insertar empleado y usuario de ejemplo
INSERT INTO personas (dni, nombre, apellido, email, telefono, tipo) VALUES
('12345678', 'Admin', 'Sistema', 'admin@sistema.com', '999999999', 'EMPLEADO');
GO

INSERT INTO empleados (dni, numero_empleado, cargo, fecha_contratacion, salario, activo) VALUES
('12345678', 'EMP001', 'Administrador', CAST(GETDATE() AS DATE), 5000.00, 1);
GO

INSERT INTO usuarios (nombre_usuario, contrasena, rol, dni_empleado, activo) VALUES
('admin', 'admin123', 'ADMINISTRADOR', '12345678', 1);
GO

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
