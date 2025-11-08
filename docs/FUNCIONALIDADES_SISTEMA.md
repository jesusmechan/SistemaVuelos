## Funcionalidades Principales · SisVuelos_Grupo1

Resumen de los casos de uso cubiertos por la aplicación de consola (`MenuService`) que opera sobre los modelos, servicios y repositorios definidos en `src/`.

---

### Autenticación y gestión de sesiones
- **Iniciar sesión** (`MenuService.iniciarSesion`): valida credenciales del usuario (`UsuarioService.autenticar`) y determina el menú disponible según su `Rol`.
- **Roles soportados**:
  - `ADMINISTRADOR`: acceso total a la gestión de recursos.
  - `OPERADOR`: acceso a operaciones de pasajeros, vuelos y reservas.
  - `VENDEDOR` u otros roles: acceso de consulta (vuelos y reservas).
- **Cerrar sesión**: disponible en cada menú para finalizar la sesión activa.

---

### Gestión de empleados
- **Registrar empleado**: captura datos personales, número de empleado, cargo, salario y fecha de contratación (`EmpleadoService.registrarEmpleado` → `EmpleadoRepository.guardar`).
- **Buscar por DNI**: obtiene un empleado específico (`EmpleadoService.buscarEmpleadoPorDni`).
- **Listar todos**: muestra el staff registrado (`EmpleadoService.listarTodosLosEmpleados`).
- **Filtrar por cargo**: permite consultas específicas (`EmpleadoService.buscarEmpleadosPorCargo`).
- **Eliminar empleado**: remueve empleados existentes (`EmpleadoService.eliminarEmpleado`).

---

### Gestión de usuarios del sistema
- **Registrar usuario**: asocia credenciales (`nombreUsuario`, `contraseña`) y rol a un empleado existente (`UsuarioService.registrarUsuario`).
- **Buscar usuario**: consulta por `nombreUsuario`.
- **Listar usuarios**: reporta usuarios con su rol y datos de empleado.
- **Eliminar usuario**: remueve el acceso a un empleado determinado.

---

### Gestión de pasajeros
- **Registrar pasajero**: almacena datos personales y de viaje (`PasajeroService.registrarPasajero`).
- **Buscar por DNI**: localiza un pasajero concreto.
- **Listar todos**: muestra el registro completo de pasajeros.
- **Eliminar pasajero**: elimina registros existentes (opción exclusiva de administradores).

---

### Gestión de aviones
- **Registrar avión**: ingresa datos técnicos (modelo, fabricante, capacidades) y su estado inicial (`AvionService.registrarAvion`).
- **Buscar por número de serie**.
- **Listar todos los aviones**.
- **Listar aviones disponibles**: filtra por `EstadoAvion.DISPONIBLE`.
- **Eliminar avión**: remueve aeronaves del inventario.

---

### Gestión de vuelos
- **Registrar vuelo**: define ruta, horarios, avión asociado y precio (`VueloService.registrarVuelo` con verificación de avión existente).
- **Buscar por número de vuelo**.
- **Listar todos los vuelos registrados**.
- **Filtrar por origen, destino o ruta** (`VueloService.buscarVuelosPorOrigen/Destino/Ruta`).
- **Eliminar vuelo**: borra registros cuando ya no son necesarios.
- **Gestión de disponibilidad**: cada vuelo calcula y actualiza asientos disponibles (`Vuelo.reservarAsiento`, `Vuelo.liberarAsiento`).

---

### Gestión de reservas
- **Crear reserva**: vincula pasajero y vuelo, asigna asiento y estado inicial (`ReservaService.crearReserva` con verificación de disponibilidad).
- **Buscar por número de reserva**.
- **Listar reservas**: muestra todas las reservas almacenadas.
- **Filtrar por pasajero o vuelo** (`ReservaService.buscarReservasPorPasajero/Vuelo`).
- **Cancelar reserva**: actualiza estado y libera asiento (`ReservaService.cancelarReserva`).
- **Calcular total**: cada reserva expone el precio asociado mediante `Reserva.calcularTotal`.

---

### Funcionalidades de consulta (roles restringidos)
- Usuarios con permisos limitados (por ejemplo `VENDEDOR`) acceden a:
  - Consulta de vuelos disponibles (`VueloService.listarTodosLosVuelos`).
  - Consulta de reservas registradas (`ReservaService.listarTodasLasReservas`).

---

### Datos iniciales de demostración
- Durante la inicialización (`MenuService.inicializarDatos`) se crean:
  - Dos empleados y sus usuarios (`jesus.mechan` como administrador y `juana.rivera` como operador).
  - Ejemplos adicionales pueden añadirse extendiendo este método o cargando datos persistentes.

---

> **Sugerencias futuras**: integrar persistencia externa, añadir reportes personalizados, incorporar validaciones de negocio adicionales (gestión de estados de vuelo/reserva), y ofrecer una interfaz gráfica o API para ampliar el alcance del sistema.
*** End Patch

