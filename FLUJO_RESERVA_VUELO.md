# Flujo de Reserva de Vuelo

## Descripción General

Este documento describe el proceso completo de reserva de vuelos en el sistema **SisVuelos_Grupo1**, incluyendo la creación, consulta, cancelación y gestión de reservas.

---

## 1. Proceso de Creación de Reserva

### 1.1 Pasos del Flujo

El proceso de creación de una reserva sigue los siguientes pasos:

#### **Paso 1: Iniciar Reserva**
El usuario accede al menú de reservas y selecciona la opción "Crear Reserva".

#### **Paso 2: Captura de Datos**
El sistema solicita la siguiente información:
- **Número de Reserva**: Identificador único de la reserva
- **DNI del Pasajero**: Documento de identidad del pasajero
- **Número de Vuelo**: Identificador del vuelo a reservar
- **Número de Asiento**: Asiento específico que el pasajero desea reservar

#### **Paso 3: Validación del Pasajero**
- El sistema busca al pasajero por su DNI en el repositorio de pasajeros
- **Si el pasajero NO existe:**
  - Se muestra el mensaje: *"Pasajero no encontrado. Debe registrar el pasajero primero."*
  - El proceso termina aquí

#### **Paso 4: Validación del Vuelo**
- Si el pasajero existe, el sistema busca el vuelo por su número
- **Si el vuelo NO existe:**
  - Se muestra el mensaje: *"Vuelo no encontrado."*
  - El proceso termina aquí

#### **Paso 5: Validación de Disponibilidad**
- Si el vuelo existe, el sistema verifica:
  - Que el vuelo tenga asientos disponibles (`tieneAsientosDisponibles()`)
- **Si NO hay asientos disponibles:**
  - Se muestra el mensaje: *"Error al crear reserva. Verifique que el vuelo tenga asientos disponibles."*
  - El proceso termina aquí

#### **Paso 6: Creación de la Reserva**
Si todas las validaciones son exitosas:
1. Se crea un nuevo objeto `Reserva` con:
   - Número de reserva
   - Pasajero (objeto completo)
   - Vuelo (objeto completo)
   - Número de asiento
   - Fecha y hora de reserva (se establece automáticamente con `LocalDateTime.now()`)
   - Estado inicial: **CONFIRMADA**

2. Se reserva el asiento en el vuelo:
   - Se decrementa el contador de `asientosDisponibles` del vuelo
   - Se invoca `vuelo.reservarAsiento()`

3. Se guarda la reserva en el repositorio

#### **Paso 7: Confirmación**
- Si todo es exitoso, se muestra: *"Reserva creada exitosamente."*

### 1.2 Validaciones Aplicadas

En el servicio `ReservaService.crearReserva()`:

- ✅ La reserva no debe ser `null`
- ✅ El número de reserva no debe ser `null` o vacío
- ✅ El vuelo asociado no debe ser `null`
- ✅ El pasajero asociado no debe ser `null`
- ✅ El vuelo debe tener asientos disponibles

### 1.3 Diagrama de Flujo

```
[Inicio] → [Solicitar datos de reserva]
           ↓
[Buscar Pasajero por DNI]
           ↓
     ¿Pasajero existe?
           ↓
      NO → [Error: Pasajero no encontrado] → [Fin]
      SI ↓
[Buscar Vuelo por número]
           ↓
     ¿Vuelo existe?
           ↓
      NO → [Error: Vuelo no encontrado] → [Fin]
      SI ↓
[Verificar asientos disponibles]
           ↓
     ¿Hay asientos?
           ↓
      NO → [Error: Sin asientos disponibles] → [Fin]
      SI ↓
[Crear objeto Reserva]
           ↓
[Reservar asiento en vuelo]
           ↓
[Guardar reserva en repositorio]
           ↓
[Mostrar: Reserva creada exitosamente]
           ↓
        [Fin]
```

---

## 2. Consulta de Reservas

El sistema permite consultar reservas de diferentes maneras:

### 2.1 Buscar por Número de Reserva
- **Entrada**: Número de reserva
- **Proceso**: Búsqueda en el repositorio por número de reserva
- **Salida**: 
  - Si existe: Muestra los detalles de la reserva
  - Si no existe: *"Reserva no encontrada."*

### 2.2 Listar Todas las Reservas
- **Entrada**: Ninguna
- **Proceso**: Obtiene todas las reservas del repositorio
- **Salida**: 
  - Si hay reservas: Lista completa con todos los detalles
  - Si no hay reservas: *"No hay reservas registradas."*

### 2.3 Buscar por Pasajero (DNI)
- **Entrada**: DNI del pasajero
- **Proceso**: Búsqueda de todas las reservas asociadas al DNI
- **Salida**: 
  - Si hay reservas: Lista de reservas del pasajero
  - Si no hay: *"No se encontraron reservas para ese pasajero."*

### 2.4 Buscar por Vuelo
- **Entrada**: Número de vuelo
- **Proceso**: Búsqueda de todas las reservas asociadas al vuelo
- **Salida**: 
  - Si hay reservas: Lista de reservas del vuelo
  - Si no hay: *"No se encontraron reservas para ese vuelo."*

---

## 3. Cancelación de Reserva

### 3.1 Proceso de Cancelación

#### **Paso 1: Solicitud de Cancelación**
- El usuario ingresa el número de reserva a cancelar

#### **Paso 2: Búsqueda de la Reserva**
- El sistema busca la reserva por su número en el repositorio

#### **Paso 3: Validación**
- **Si la reserva NO existe:**
  - Se muestra: *"Error al cancelar reserva."*
  - El proceso termina

#### **Paso 4: Cancelación**
Si la reserva existe:
1. Se cambia el estado de la reserva a **CANCELADA**
2. Se libera el asiento en el vuelo:
   - Se incrementa el contador de `asientosDisponibles`
   - Se invoca `vuelo.liberarAsiento()`
3. Se actualiza la reserva en el repositorio

#### **Paso 5: Confirmación**
- Se muestra: *"Reserva cancelada exitosamente."*

### 3.2 Diagrama de Flujo de Cancelación

```
[Inicio] → [Solicitar número de reserva]
           ↓
[Buscar Reserva por número]
           ↓
     ¿Reserva existe?
           ↓
      NO → [Error al cancelar reserva] → [Fin]
      SI ↓
[Cambiar estado a CANCELADA]
           ↓
[Liberar asiento en vuelo]
           ↓
[Actualizar reserva en repositorio]
           ↓
[Mostrar: Reserva cancelada exitosamente]
           ↓
        [Fin]
```

---

## 4. Estados de Reserva

El sistema maneja los siguientes estados de reserva (`EstadoReserva`):

| Estado | Descripción |
|--------|-------------|
| **CONFIRMADA** | Estado inicial al crear una reserva. La reserva está activa y válida. |
| **PENDIENTE** | Reserva que está en proceso de confirmación. |
| **CANCELADA** | Reserva que ha sido cancelada. El asiento queda liberado. |
| **COMPLETADA** | Reserva cuyo vuelo ya se completó. |

### 4.1 Transiciones de Estado

- **Nueva Reserva**: `null` → **CONFIRMADA**
- **Cancelación**: Cualquier estado → **CANCELADA**
- **Completación**: CONFIRMADA → **COMPLETADA** (cuando el vuelo se completa)

---

## 5. Información de la Reserva

Cada reserva contiene la siguiente información:

### 5.1 Atributos
- **numeroReserva** (String): Identificador único de la reserva
- **pasajero** (Pasajero): Objeto completo del pasajero
- **vuelo** (Vuelo): Objeto completo del vuelo
- **fechaReserva** (LocalDateTime): Fecha y hora en que se realizó la reserva
- **estado** (EstadoReserva): Estado actual de la reserva
- **numeroAsiento** (int): Número del asiento asignado

### 5.2 Métodos Importantes

- **calcularTotal()**: Calcula el total de la reserva basándose en el precio del vuelo
- **toString()**: Retorna una representación en texto de la reserva con todos sus detalles

---

## 6. Integración con Otros Componentes

### 6.1 Dependencias

La reserva está relacionada con:

1. **Pasajero** (Composición)
   - Debe existir previamente en el sistema
   - Se requiere el DNI para buscar al pasajero

2. **Vuelo** (Composición)
   - Debe existir previamente en el sistema
   - Debe tener asientos disponibles
   - Se actualiza el contador de asientos al crear/cancelar reservas

3. **Avión** (a través del Vuelo)
   - El vuelo tiene un avión asignado
   - La capacidad del avión determina los asientos disponibles iniciales

### 6.2 Servicios Involucrados

- **ReservaService**: Lógica de negocio para gestión de reservas
- **PasajeroService**: Validación y búsqueda de pasajeros
- **VueloService**: Validación y búsqueda de vuelos

### 6.3 Repositorios

- **ReservaRepository**: Persistencia y consultas de reservas
- **PasajeroRepository**: Acceso a datos de pasajeros
- **VueloRepository**: Acceso a datos de vuelos

---

## 7. Casos de Uso Específicos

### 7.1 Caso de Uso: Reserva Exitosa
```
1. Usuario: Selecciona "Crear Reserva"
2. Sistema: Solicita datos
3. Usuario: Ingresa datos válidos
4. Sistema: Valida pasajero → Existe
5. Sistema: Valida vuelo → Existe
6. Sistema: Verifica asientos → Disponibles
7. Sistema: Crea reserva con estado CONFIRMADA
8. Sistema: Reserva asiento en vuelo
9. Sistema: Guarda reserva
10. Sistema: Muestra mensaje de éxito
```

### 7.2 Caso de Uso: Reserva Fallida (Sin Asientos)
```
1. Usuario: Selecciona "Crear Reserva"
2. Sistema: Solicita datos
3. Usuario: Ingresa datos válidos
4. Sistema: Valida pasajero → Existe
5. Sistema: Valida vuelo → Existe
6. Sistema: Verifica asientos → NO disponibles
7. Sistema: Muestra error
8. Usuario: No puede completar la reserva
```

### 7.3 Caso de Uso: Cancelación Exitosa
```
1. Usuario: Selecciona "Cancelar Reserva"
2. Sistema: Solicita número de reserva
3. Usuario: Ingresa número válido
4. Sistema: Busca reserva → Existe
5. Sistema: Cambia estado a CANCELADA
6. Sistema: Libera asiento en vuelo
7. Sistema: Actualiza reserva
8. Sistema: Muestra mensaje de éxito
```

---

## 8. Consideraciones Técnicas

### 8.1 Patrones de Diseño Aplicados

- **Repository Pattern**: Separación de lógica de acceso a datos
- **Service Layer**: Separación de lógica de negocio
- **Composición**: Reserva compuesta por Pasajero y Vuelo
- **Encapsulación**: Atributos privados con getters/setters

### 8.2 Validaciones de Negocio

- Un pasajero puede tener múltiples reservas
- Un vuelo puede tener múltiples reservas
- Un asiento solo puede estar reservado una vez por vuelo (implícito por disponibilidad)
- La capacidad del avión limita el número de reservas por vuelo

### 8.3 Manejo de Asientos

- Al crear reserva: `asientosDisponibles--`
- Al cancelar reserva: `asientosDisponibles++`
- La verificación previa evita reservas en vuelos completos

---

## 9. Ejemplos de Interacción

### 9.1 Crear una Reserva
```
=== CREAR RESERVA ===
Número de Reserva: RES-001
DNI del Pasajero: 12345678
Número de Vuelo: VU-2024-001
Número de Asiento: 15

Reserva creada exitosamente.
```

### 9.2 Consultar Reserva
```
Ingrese el número de reserva: RES-001
Reserva encontrada: Reserva{numeroReserva='RES-001', pasajero=Juan Pérez, vuelo=VU-2024-001, fechaReserva=2024-01-15T10:30:00, estado=CONFIRMADA, numeroAsiento=15, total=350.0}
```

### 9.3 Cancelar Reserva
```
Ingrese el número de reserva a cancelar: RES-001
Reserva cancelada exitosamente.
```

---

## 10. Flujos Alternativos y Manejo de Errores

### 10.1 Errores Comunes

| Error | Causa | Mensaje al Usuario |
|-------|-------|-------------------|
| Pasajero no encontrado | DNI no existe en el sistema | "Pasajero no encontrado. Debe registrar el pasajero primero." |
| Vuelo no encontrado | Número de vuelo inválido | "Vuelo no encontrado." |
| Sin asientos disponibles | Vuelo completo | "Error al crear reserva. Verifique que el vuelo tenga asientos disponibles." |
| Reserva no encontrada | Número de reserva inválido | "Reserva no encontrada." |
| Error al cancelar | Reserva no existe o ya cancelada | "Error al cancelar reserva." |

### 10.2 Validaciones Adicionales

El sistema también valida:
- Que el número de reserva no sea `null` o vacío
- Que los objetos relacionados (pasajero, vuelo) no sean `null`
- Que la reserva exista antes de cancelarla

---

## Conclusión

El flujo de reserva de vuelo en **SisVuelos_Grupo1** está diseñado para garantizar:
- ✅ Validación exhaustiva de datos
- ✅ Integridad de información (pasajero y vuelo deben existir)
- ✅ Control de disponibilidad de asientos
- ✅ Actualización automática de disponibilidad
- ✅ Gestión de estados de reserva
- ✅ Manejo apropiado de errores

Este flujo asegura que solo se creen reservas válidas y que el sistema mantenga la consistencia de los datos en todo momento.



