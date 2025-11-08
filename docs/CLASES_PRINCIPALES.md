## Clases Principales · SisVuelos_Grupo1

Selección de las clases más representativas del proyecto, agrupadas por capa.

---

### Aplicación y capa UI
- `Main` (`src/Main.java`): Punto de entrada; inicia la aplicación creando una instancia de `MenuService`.
- `MenuService` (`src/ui/MenuService.java`): Controla toda la interfaz de consola. Gestiona menús por rol, lectura de datos, invocación de servicios y manejo de excepciones de dominio. Es la fachada entre el usuario y las capas inferiores.

---

### Modelo de dominio (`src/model/`)
- `Persona`: Base con atributos y comportamiento compartido (DNI, datos de contacto). Superclase de `Empleado` y `Pasajero`.
- `Empleado`: Especializa a `Persona` con información laboral (número empleado, cargo, salario). Se asocia a `Usuario`.
- `Pasajero`: Especializa a `Persona` con datos de viaje (nacionalidad, pasaporte, fecha de nacimiento).
- `Avion`: Describe aeronaves disponibles (modelo, capacidades, estado). Compuesto en los vuelos.
- `Vuelo`: Contiene la información operativa del vuelo (ruta, horarios, avión asignado, estado, disponibilidad de asientos) y métodos para reservar/liberar asientos.
- `Reserva`: Vincula `Pasajero` y `Vuelo`, registrando asiento, fecha y estado de la reserva. Calcula el total con base en el precio del vuelo.
- `Usuario`: Representa credenciales del sistema asociadas a un `Empleado` y rol (`Rol` enum).

---

### Servicios (`src/service/`)
- `EmpleadoService`, `PasajeroService`, `AvionService`, `VueloService`, `ReservaService`, `UsuarioService`: Encapsulan la lógica de negocio y validaciones. Cada servicio:
  - Depende de la interfaz de repositorio equivalente.
  - Lanza excepciones personalizadas (`ValidacionException`, `OperacionNoPermitidaException`, `RecursoNoEncontradoException`) al detectar datos inválidos o estados inconsistentes.
  - Sirve como punto de coordinación entre UI y repositorios.

---

### Repositorios en memoria (`src/repository/`)
- `EmpleadoRepository`, `PasajeroRepository`, `AvionRepository`, `VueloRepository`, `ReservaRepository`, `UsuarioRepository`: Implementaciones concretas de las interfaces `I*Repository`. Usan estructuras `Map` o colecciones simples para persistencia temporal durante la ejecución.

---

### Excepciones de Dominio (`src/exception/`)
- `DominioException`: Raíz de la jerarquía de errores controlados.
- `ValidacionException`, `OperacionNoPermitidaException`, `RecursoNoEncontradoException`: Tipos específicos que los servicios utilizan para comunicar fallos a la UI.

---

> Estas clases conforman el “esqueleto” del sistema: los modelos describen el problema, los servicios aplican reglas, los repositorios persisten en memoria y `MenuService` orquesta la interacción con el usuario.

