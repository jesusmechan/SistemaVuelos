package ui;

import model.*;
import service.*;
import repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Servicio de interfaz de usuario (menú)
 * Aplica SRP - única responsabilidad: interfaz de usuario
 */
public class MenuService {
    private final Scanner scanner;
    private final IEmpleadoService empleadoService;
    private final IUsuarioService usuarioService;
    private final IPasajeroService pasajeroService;
    private final IAvionService avionService;
    private final IVueloService vueloService;
    private final IReservaService reservaService;
    private Usuario usuarioActual;

    public MenuService() {
        this.scanner = new Scanner(System.in);
        
        // Inicializar repositorios
        IEmpleadoRepository empleadoRepo = new EmpleadoRepository();
        IUsuarioRepository usuarioRepo = new UsuarioRepository();
        IPasajeroRepository pasajeroRepo = new PasajeroRepository();
        IAvionRepository avionRepo = new AvionRepository();
        IVueloRepository vueloRepo = new VueloRepository();
        IReservaRepository reservaRepo = new ReservaRepository();
        
        // Inicializar servicios
        this.empleadoService = new EmpleadoService(empleadoRepo);
        this.usuarioService = new UsuarioService(usuarioRepo);
        this.pasajeroService = new PasajeroService(pasajeroRepo);
        this.avionService = new AvionService(avionRepo);
        this.vueloService = new VueloService(vueloRepo);
        this.reservaService = new ReservaService(reservaRepo);
        
        // Crear datos iniciales
        inicializarDatos();
    }

    public void mostrarMenuPrincipal() {
        while (true) {
            System.out.println("\n=== SISTEMA DE GESTIÓN DE VUELOS ===");
            System.out.println("1. Iniciar Sesión");
            System.out.println("2. Salir");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    iniciarSesion();
                    break;
                case 2:
                    System.out.println("¡Hasta luego!");
                    return;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    private void iniciarSesion() {
        System.out.println("\n=== INICIAR SESIÓN ===");
        System.out.print("Usuario: ");
        String nombreUsuario = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine();
        
        usuarioActual = usuarioService.autenticar(nombreUsuario, contrasena).orElse(null);
        
        if (usuarioActual != null) {
            System.out.println("\n¡Bienvenido, " + usuarioActual.getNombre() + "!");
            mostrarMenuUsuario();
        } else {
            System.out.println("Usuario o contraseña incorrectos.");
        }
    }

    private void mostrarMenuUsuario() {
        if (usuarioActual == null) {
            return;
        }
        
        Rol rolUsuario = usuarioActual.getRol();
        
        // Mostrar menú según el rol del usuario
        if (rolUsuario == Rol.ADMINISTRADOR) {
            mostrarMenuAdministrador();
        } else if (rolUsuario == Rol.OPERADOR) {
            mostrarMenuOperador();
        } else {
            // Para otros roles (VENDEDOR, etc.) mostrar menú básico
            mostrarMenuBasico();
        }
    }

    private void mostrarMenuAdministrador() {
        while (usuarioActual != null) {
            System.out.println("\n=== MENÚ PRINCIPAL - ADMINISTRADOR ===");
            System.out.println("1. Gestión de Empleados");
            System.out.println("2. Gestión de Usuarios");
            System.out.println("3. Gestión de Pasajeros");
            System.out.println("4. Gestión de Aviones");
            System.out.println("5. Gestión de Vuelos");
            System.out.println("6. Gestión de Reservas");
            System.out.println("7. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    menuEmpleados();
                    break;
                case 2:
                    menuUsuarios();
                    break;
                case 3:
                    menuPasajeros();
                    break;
                case 4:
                    menuAviones();
                    break;
                case 5:
                    menuVuelos();
                    break;
                case 6:
                    menuReservas();
                    break;
                case 7:
                    usuarioActual = null;
                    System.out.println("Sesión cerrada.");
                    return;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    private void mostrarMenuOperador() {
        while (usuarioActual != null) {
            System.out.println("\n=== MENÚ PRINCIPAL - OPERADOR ===");
            System.out.println("1. Gestión de Pasajeros");
            System.out.println("2. Gestión de Vuelos");
            System.out.println("3. Gestión de Reservas");
            System.out.println("4. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    menuPasajeros();
                    break;
                case 2:
                    menuVuelos();
                    break;
                case 3:
                    menuReservas();
                    break;
                case 4:
                    usuarioActual = null;
                    System.out.println("Sesión cerrada.");
                    return;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    private void mostrarMenuBasico() {
        while (usuarioActual != null) {
            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Consultar Vuelos");
            System.out.println("2. Consultar Reservas");
            System.out.println("3. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    listarVuelos();
                    break;
                case 2:
                    listarReservas();
                    break;
                case 3:
                    usuarioActual = null;
                    System.out.println("Sesión cerrada.");
                    return;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    private void menuEmpleados() {
        while (true) {
            System.out.println("\n=== GESTIÓN DE EMPLEADOS ===");
            System.out.println("1. Registrar Empleado");
            System.out.println("2. Buscar Empleado por DNI");
            System.out.println("3. Listar Todos los Empleados");
            System.out.println("4. Buscar Empleados por Cargo");
            System.out.println("5. Eliminar Empleado");
            System.out.println("6. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    registrarEmpleado();
                    break;
                case 2:
                    buscarEmpleadoPorDni();
                    break;
                case 3:
                    listarEmpleados();
                    break;
                case 4:
                    buscarEmpleadosPorCargo();
                    break;
                case 5:
                    eliminarEmpleado();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void registrarEmpleado() {
        System.out.println("\n=== REGISTRAR EMPLEADO ===");
        System.out.print("DNI: ");
        String dni = scanner.nextLine();
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine();
        System.out.print("Número de Empleado: ");
        String numeroEmpleado = scanner.nextLine();
        System.out.print("Cargo: ");
        String cargo = scanner.nextLine();
        System.out.print("Salario: ");
        double salario = leerDouble();
        
        LocalDate fechaContratacion = LocalDate.now();
        
        Empleado empleado = new Empleado(dni, nombre, apellido, email, telefono,
                numeroEmpleado, cargo, fechaContratacion, salario);
        
        if (empleadoService.registrarEmpleado(empleado)) {
            System.out.println("Empleado registrado exitosamente.");
        } else {
            System.out.println("Error al registrar empleado. El DNI puede ya estar registrado.");
        }
    }

    private void buscarEmpleadoPorDni() {
        System.out.print("\nIngrese el DNI: ");
        String dni = scanner.nextLine();
        empleadoService.buscarEmpleadoPorDni(dni).ifPresentOrElse(
            empleado -> System.out.println("Empleado encontrado: " + empleado),
            () -> System.out.println("Empleado no encontrado.")
        );
    }

    private void listarEmpleados() {
        List<Empleado> empleados = empleadoService.listarTodosLosEmpleados();
        if (empleados.isEmpty()) {
            System.out.println("No hay empleados registrados.");
        } else {
            System.out.println("\n=== LISTA DE EMPLEADOS ===");
            empleados.forEach(System.out::println);
        }
    }

    private void buscarEmpleadosPorCargo() {
        System.out.print("\nIngrese el cargo: ");
        String cargo = scanner.nextLine();
        List<Empleado> empleados = empleadoService.buscarEmpleadosPorCargo(cargo);
        if (empleados.isEmpty()) {
            System.out.println("No se encontraron empleados con ese cargo.");
        } else {
            empleados.forEach(System.out::println);
        }
    }

    private void eliminarEmpleado() {
        System.out.print("\nIngrese el DNI del empleado a eliminar: ");
        String dni = scanner.nextLine();
        if (empleadoService.eliminarEmpleado(dni)) {
            System.out.println("Empleado eliminado exitosamente.");
        } else {
            System.out.println("Error al eliminar empleado.");
        }
    }

    private void menuUsuarios() {
        while (true) {
            System.out.println("\n=== GESTIÓN DE USUARIOS ===");
            System.out.println("1. Registrar Usuario");
            System.out.println("2. Buscar Usuario");
            System.out.println("3. Listar Todos los Usuarios");
            System.out.println("4. Eliminar Usuario");
            System.out.println("5. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    registrarUsuario();
                    break;
                case 2:
                    buscarUsuario();
                    break;
                case 3:
                    listarUsuarios();
                    break;
                case 4:
                    eliminarUsuario();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void registrarUsuario() {
        System.out.println("\n=== REGISTRAR USUARIO ===");
        System.out.print("Nombre de Usuario: ");
        String nombreUsuario = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine();
        System.out.print("Rol (ADMINISTRADOR, OPERADOR, VENDEDOR): ");
        String rolStr = scanner.nextLine().toUpperCase();
        
        Rol rol;
        try {
            rol = Rol.valueOf(rolStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Rol inválido. Usando VENDEDOR por defecto.");
            rol = Rol.VENDEDOR;
        }
        
        final Rol rolFinal = rol; // Variable final para usar en la lambda
        
        System.out.print("DNI del Empleado asociado: ");
        String dniEmpleado = scanner.nextLine();
        
        empleadoService.buscarEmpleadoPorDni(dniEmpleado).ifPresentOrElse(
            empleado -> {
                Usuario usuario = new Usuario(nombreUsuario, contrasena, rolFinal, empleado);
                if (usuarioService.registrarUsuario(usuario)) {
                    System.out.println("Usuario registrado exitosamente.");
                } else {
                    System.out.println("Error al registrar usuario. El nombre de usuario puede ya estar en uso.");
                }
            },
            () -> System.out.println("Empleado no encontrado. Debe registrar el empleado primero.")
        );
    }

    private void buscarUsuario() {
        System.out.print("\nIngrese el nombre de usuario: ");
        String nombreUsuario = scanner.nextLine();
        usuarioService.buscarUsuario(nombreUsuario).ifPresentOrElse(
            usuario -> System.out.println("Usuario encontrado: " + usuario),
            () -> System.out.println("Usuario no encontrado.")
        );
    }

    private void listarUsuarios() {
        System.out.println("\n=== LISTA DE USUARIOS ===");
        List<Usuario> usuarios = usuarioService.listarTodosLosUsuarios();
        
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
        } else {
            System.out.printf("%-20s %-20s %-20s %-30s%n", 
                "Nombre de Usuario", "Rol", "Empleado", "Email");
            System.out.println("--------------------------------------------------------------------------------");
            
            for (Usuario usuario : usuarios) {
                String nombreUsuario = usuario.getNombreUsuario();
                String rol = usuario.getRol() != null ? usuario.getRol().toString() : "N/A";
                String empleadoNombre = usuario.getEmpleado() != null 
                    ? usuario.getEmpleado().getNombreCompleto() 
                    : "N/A";
                String email = usuario.getEmpleado() != null 
                    ? usuario.getEmpleado().getEmail() 
                    : "N/A";
                
                System.out.printf("%-20s %-20s %-20s %-30s%n", 
                    nombreUsuario, rol, empleadoNombre, email);
            }
            System.out.println("\nTotal de usuarios: " + usuarios.size());
        }
    }

    private void eliminarUsuario() {
        System.out.print("\nIngrese el nombre de usuario a eliminar: ");
        String nombreUsuario = scanner.nextLine();
        if (usuarioService.eliminarUsuario(nombreUsuario)) {
            System.out.println("Usuario eliminado exitosamente.");
        } else {
            System.out.println("Error al eliminar usuario.");
        }
    }

    private void menuPasajeros() {
        // Determinar si es administrador para mostrar todas las opciones
        boolean esAdministrador = usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR;
        
        while (true) {
            System.out.println("\n=== GESTIÓN DE PASAJEROS ===");
            System.out.println("1. Registrar Pasajero");
            
            if (esAdministrador) {
                System.out.println("2. Buscar Pasajero por DNI");
                System.out.println("3. Listar Todos los Pasajeros");
                System.out.println("4. Eliminar Pasajero");
                System.out.println("5. Volver al Menú Principal");
            } else {
                System.out.println("2. Volver al Menú Principal");
            }
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            if (esAdministrador) {
                switch (opcion) {
                    case 1:
                        registrarPasajero();
                        break;
                    case 2:
                        buscarPasajeroPorDni();
                        break;
                    case 3:
                        listarPasajeros();
                        break;
                    case 4:
                        eliminarPasajero();
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            } else {
                switch (opcion) {
                    case 1:
                        registrarPasajero();
                        break;
                    case 2:
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        }
    }

    private void registrarPasajero() {
        System.out.println("\n=== REGISTRAR PASAJERO ===");
        System.out.print("DNI: ");
        String dni = scanner.nextLine();
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine();
        System.out.print("Fecha de Nacimiento (YYYY-MM-DD): ");
        LocalDate fechaNacimiento = leerFecha();
        System.out.print("Nacionalidad: ");
        String nacionalidad = scanner.nextLine();
        System.out.print("Número de Pasaporte: ");
        String numeroPasaporte = scanner.nextLine();
        
        Pasajero pasajero = new Pasajero(dni, nombre, apellido, email, telefono,
                fechaNacimiento, nacionalidad, numeroPasaporte);
        
        if (pasajeroService.registrarPasajero(pasajero)) {
            System.out.println("Pasajero registrado exitosamente.");
        } else {
            System.out.println("Error al registrar pasajero. El DNI puede ya estar registrado.");
        }
    }

    private void buscarPasajeroPorDni() {
        System.out.print("\nIngrese el DNI: ");
        String dni = scanner.nextLine();
        pasajeroService.buscarPasajeroPorDni(dni).ifPresentOrElse(
            pasajero -> System.out.println("Pasajero encontrado: " + pasajero),
            () -> System.out.println("Pasajero no encontrado.")
        );
    }

    private void listarPasajeros() {
        List<Pasajero> pasajeros = pasajeroService.listarTodosLosPasajeros();
        if (pasajeros.isEmpty()) {
            System.out.println("No hay pasajeros registrados.");
        } else {
            System.out.println("\n=== LISTA DE PASAJEROS ===");
            pasajeros.forEach(System.out::println);
        }
    }

    private void eliminarPasajero() {
        System.out.print("\nIngrese el DNI del pasajero a eliminar: ");
        String dni = scanner.nextLine();
        if (pasajeroService.eliminarPasajero(dni)) {
            System.out.println("Pasajero eliminado exitosamente.");
        } else {
            System.out.println("Error al eliminar pasajero.");
        }
    }

    private void menuAviones() {
        while (true) {
            System.out.println("\n=== GESTIÓN DE AVIONES ===");
            System.out.println("1. Registrar Avión");
            System.out.println("2. Buscar Avión por Número de Serie");
            System.out.println("3. Listar Todos los Aviones");
            System.out.println("4. Buscar Aviones Disponibles");
            System.out.println("5. Eliminar Avión");
            System.out.println("6. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            switch (opcion) {
                case 1:
                    registrarAvion();
                    break;
                case 2:
                    buscarAvionPorNumeroSerie();
                    break;
                case 3:
                    listarAviones();
                    break;
                case 4:
                    listarAvionesDisponibles();
                    break;
                case 5:
                    eliminarAvion();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void registrarAvion() {
        System.out.println("\n=== REGISTRAR AVION ===");
        System.out.print("Número de Serie: ");
        String numeroSerie = scanner.nextLine();
        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();
        System.out.print("Fabricante: ");
        String fabricante = scanner.nextLine();
        System.out.print("Capacidad de Pasajeros: ");
        int capacidadPasajeros = leerEntero();
        System.out.print("Capacidad de Carga (kg): ");
        int capacidadCarga = leerEntero();
        
        Avion avion = new Avion(numeroSerie, modelo, fabricante, capacidadPasajeros, capacidadCarga);
        
        if (avionService.registrarAvion(avion)) {
            System.out.println("Avión registrado exitosamente.");
        } else {
            System.out.println("Error al registrar avión. El número de serie puede ya estar registrado.");
        }
    }

    private void buscarAvionPorNumeroSerie() {
        System.out.print("\nIngrese el número de serie: ");
        String numeroSerie = scanner.nextLine();
        avionService.buscarAvionPorNumeroSerie(numeroSerie).ifPresentOrElse(
            avion -> System.out.println("Avión encontrado: " + avion),
            () -> System.out.println("Avión no encontrado.")
        );
    }

    private void listarAviones() {
        List<Avion> aviones = avionService.listarTodosLosAviones();
        if (aviones.isEmpty()) {
            System.out.println("No hay aviones registrados.");
        } else {
            System.out.println("\n=== LISTA DE AVIONES ===");
            aviones.forEach(System.out::println);
        }
    }

    private void listarAvionesDisponibles() {
        List<Avion> aviones = avionService.buscarAvionesDisponibles();
        if (aviones.isEmpty()) {
            System.out.println("No hay aviones disponibles.");
        } else {
            System.out.println("\n=== AVIONES DISPONIBLES ===");
            aviones.forEach(System.out::println);
        }
    }

    private void eliminarAvion() {
        System.out.print("\nIngrese el número de serie del avión a eliminar: ");
        String numeroSerie = scanner.nextLine();
        if (avionService.eliminarAvion(numeroSerie)) {
            System.out.println("Avión eliminado exitosamente.");
        } else {
            System.out.println("Error al eliminar avión.");
        }
    }

    private void menuVuelos() {
        // Determinar si es administrador para mostrar todas las opciones
        boolean esAdministrador = usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR;
        
        while (true) {
            System.out.println("\n=== GESTIÓN DE VUELOS ===");
            System.out.println("1. Registrar Vuelo");
            
            if (esAdministrador) {
                System.out.println("2. Buscar Vuelo por Número");
                System.out.println("3. Listar Todos los Vuelos");
                System.out.println("4. Buscar Vuelos por Origen");
                System.out.println("5. Buscar Vuelos por Destino");
                System.out.println("6. Buscar Vuelos por Ruta");
                System.out.println("7. Eliminar Vuelo");
                System.out.println("8. Volver al Menú Principal");
            } else {
                System.out.println("2. Volver al Menú Principal");
            }
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            if (esAdministrador) {
                switch (opcion) {
                    case 1:
                        registrarVuelo();
                        break;
                    case 2:
                        buscarVueloPorNumero();
                        break;
                    case 3:
                        listarVuelos();
                        break;
                    case 4:
                        buscarVuelosPorOrigen();
                        break;
                    case 5:
                        buscarVuelosPorDestino();
                        break;
                    case 6:
                        buscarVuelosPorRuta();
                        break;
                    case 7:
                        eliminarVuelo();
                        break;
                    case 8:
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            } else {
                switch (opcion) {
                    case 1:
                        registrarVuelo();
                        break;
                    case 2:
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        }
    }

    private void registrarVuelo() {
        System.out.println("\n=== REGISTRAR VUELO ===");
        System.out.print("Número de Vuelo: ");
        String numeroVuelo = scanner.nextLine();
        System.out.print("Origen: ");
        String origen = scanner.nextLine();
        System.out.print("Destino: ");
        String destino = scanner.nextLine();
        System.out.print("Fecha y Hora de Salida (YYYY-MM-DD HH:MM): ");
        LocalDateTime fechaHoraSalida = leerFechaHora();
        System.out.print("Fecha y Hora de Llegada (YYYY-MM-DD HH:MM): ");
        LocalDateTime fechaHoraLlegada = leerFechaHora();
        System.out.print("Número de Serie del Avión: ");
        String numeroSerie = scanner.nextLine();
        System.out.print("Precio: ");
        double precio = leerDouble();
        
        avionService.buscarAvionPorNumeroSerie(numeroSerie).ifPresentOrElse(
            avion -> {
                Vuelo vuelo = new Vuelo(numeroVuelo, origen, destino, fechaHoraSalida,
                        fechaHoraLlegada, avion, precio);
                if (vueloService.registrarVuelo(vuelo)) {
                    System.out.println("Vuelo registrado exitosamente.");
                } else {
                    System.out.println("Error al registrar vuelo. El número de vuelo puede ya estar registrado.");
                }
            },
            () -> System.out.println("Avión no encontrado. Debe registrar el avión primero.")
        );
    }

    private void buscarVueloPorNumero() {
        System.out.print("\nIngrese el número de vuelo: ");
        String numeroVuelo = scanner.nextLine();
        vueloService.buscarVueloPorNumero(numeroVuelo).ifPresentOrElse(
            vuelo -> System.out.println("Vuelo encontrado: " + vuelo),
            () -> System.out.println("Vuelo no encontrado.")
        );
    }

    private void listarVuelos() {
        List<Vuelo> vuelos = vueloService.listarTodosLosVuelos();
        if (vuelos.isEmpty()) {
            System.out.println("No hay vuelos registrados.");
        } else {
            System.out.println("\n=== LISTA DE VUELOS ===");
            vuelos.forEach(System.out::println);
        }
    }

    private void buscarVuelosPorOrigen() {
        System.out.print("\nIngrese el origen: ");
        String origen = scanner.nextLine();
        List<Vuelo> vuelos = vueloService.buscarVuelosPorOrigen(origen);
        if (vuelos.isEmpty()) {
            System.out.println("No se encontraron vuelos con ese origen.");
        } else {
            vuelos.forEach(System.out::println);
        }
    }

    private void buscarVuelosPorDestino() {
        System.out.print("\nIngrese el destino: ");
        String destino = scanner.nextLine();
        List<Vuelo> vuelos = vueloService.buscarVuelosPorDestino(destino);
        if (vuelos.isEmpty()) {
            System.out.println("No se encontraron vuelos con ese destino.");
        } else {
            vuelos.forEach(System.out::println);
        }
    }

    private void buscarVuelosPorRuta() {
        System.out.print("\nIngrese el origen: ");
        String origen = scanner.nextLine();
        System.out.print("Ingrese el destino: ");
        String destino = scanner.nextLine();
        List<Vuelo> vuelos = vueloService.buscarVuelosPorRuta(origen, destino);
        if (vuelos.isEmpty()) {
            System.out.println("No se encontraron vuelos en esa ruta.");
        } else {
            vuelos.forEach(System.out::println);
        }
    }

    private void eliminarVuelo() {
        System.out.print("\nIngrese el número de vuelo a eliminar: ");
        String numeroVuelo = scanner.nextLine();
        if (vueloService.eliminarVuelo(numeroVuelo)) {
            System.out.println("Vuelo eliminado exitosamente.");
        } else {
            System.out.println("Error al eliminar vuelo.");
        }
    }

    private void menuReservas() {
        // Determinar si es administrador para mostrar todas las opciones
        boolean esAdministrador = usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR;
        
        while (true) {
            System.out.println("\n=== GESTIÓN DE RESERVAS ===");
            System.out.println("1. Crear Reserva");
            
            if (esAdministrador) {
                System.out.println("2. Buscar Reserva por Número");
                System.out.println("3. Listar Todas las Reservas");
                System.out.println("4. Buscar Reservas por Pasajero");
                System.out.println("5. Buscar Reservas por Vuelo");
                System.out.println("6. Cancelar Reserva");
                System.out.println("7. Volver al Menú Principal");
            } else {
                System.out.println("2. Volver al Menú Principal");
            }
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero();
            
            if (esAdministrador) {
                switch (opcion) {
                    case 1:
                        crearReserva();
                        break;
                    case 2:
                        buscarReservaPorNumero();
                        break;
                    case 3:
                        listarReservas();
                        break;
                    case 4:
                        buscarReservasPorPasajero();
                        break;
                    case 5:
                        buscarReservasPorVuelo();
                        break;
                    case 6:
                        cancelarReserva();
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            } else {
                switch (opcion) {
                    case 1:
                        crearReserva();
                        break;
                    case 2:
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        }
    }

    private void crearReserva() {
        System.out.println("\n=== CREAR RESERVA ===");
        System.out.print("Número de Reserva: ");
        String numeroReserva = scanner.nextLine();
        System.out.print("DNI del Pasajero: ");
        String dniPasajero = scanner.nextLine();
        System.out.print("Número de Vuelo: ");
        String numeroVuelo = scanner.nextLine();
        System.out.print("Número de Asiento: ");
        int numeroAsiento = leerEntero();
        
        pasajeroService.buscarPasajeroPorDni(dniPasajero).ifPresentOrElse(
            pasajero -> {
                vueloService.buscarVueloPorNumero(numeroVuelo).ifPresentOrElse(
                    vuelo -> {
                        Reserva reserva = new Reserva(numeroReserva, pasajero, vuelo, numeroAsiento);
                        if (reservaService.crearReserva(reserva)) {
                            System.out.println("Reserva creada exitosamente.");
                        } else {
                            System.out.println("Error al crear reserva. Verifique que el vuelo tenga asientos disponibles.");
                        }
                    },
                    () -> System.out.println("Vuelo no encontrado.")
                );
            },
            () -> System.out.println("Pasajero no encontrado. Debe registrar el pasajero primero.")
        );
    }

    private void buscarReservaPorNumero() {
        System.out.print("\nIngrese el número de reserva: ");
        String numeroReserva = scanner.nextLine();
        reservaService.buscarReservaPorNumero(numeroReserva).ifPresentOrElse(
            reserva -> System.out.println("Reserva encontrada: " + reserva),
            () -> System.out.println("Reserva no encontrada.")
        );
    }

    private void listarReservas() {
        List<Reserva> reservas = reservaService.listarTodasLasReservas();
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas registradas.");
        } else {
            System.out.println("\n=== LISTA DE RESERVAS ===");
            reservas.forEach(System.out::println);
        }
    }

    private void buscarReservasPorPasajero() {
        System.out.print("\nIngrese el DNI del pasajero: ");
        String dniPasajero = scanner.nextLine();
        List<Reserva> reservas = reservaService.buscarReservasPorPasajero(dniPasajero);
        if (reservas.isEmpty()) {
            System.out.println("No se encontraron reservas para ese pasajero.");
        } else {
            reservas.forEach(System.out::println);
        }
    }

    private void buscarReservasPorVuelo() {
        System.out.print("\nIngrese el número de vuelo: ");
        String numeroVuelo = scanner.nextLine();
        List<Reserva> reservas = reservaService.buscarReservasPorVuelo(numeroVuelo);
        if (reservas.isEmpty()) {
            System.out.println("No se encontraron reservas para ese vuelo.");
        } else {
            reservas.forEach(System.out::println);
        }
    }

    private void cancelarReserva() {
        System.out.print("\nIngrese el número de reserva a cancelar: ");
        String numeroReserva = scanner.nextLine();
        if (reservaService.cancelarReserva(numeroReserva)) {
            System.out.println("Reserva cancelada exitosamente.");
        } else {
            System.out.println("Error al cancelar reserva.");
        }
    }

    // Métodos auxiliares
    private int leerEntero() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double leerDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private LocalDate leerFecha() {
        while (true) {
            try {
                String fechaStr = scanner.nextLine();
                return LocalDate.parse(fechaStr);
            } catch (DateTimeParseException e) {
                System.out.print("Fecha inválida. Use formato YYYY-MM-DD. Intente nuevamente: ");
            }
        }
    }

    private LocalDateTime leerFechaHora() {
        while (true) {
            try {
                String fechaHoraStr = scanner.nextLine();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(fechaHoraStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.print("Fecha y hora inválidas. Use formato YYYY-MM-DD HH:MM. Intente nuevamente: ");
            }
        }
    }

    private void inicializarDatos() {
        // Crear empleado de ejemplo
        Empleado empleadoAdmin = new Empleado( "12345678", "Admin", "Sistema", "admin@sistema.com",
            "999999999", "EMP001", "Administrador", LocalDate.now(), 5000.0
        );
        empleadoService.registrarEmpleado(empleadoAdmin);


        Empleado empleadoAdmin2 = new Empleado( "12345679", "Admin", "Sistema", "juana@sistema.com",
                "88888888", "EMP002", "Operadora", LocalDate.now(), 5000.0
        );
        empleadoService.registrarEmpleado(empleadoAdmin2);
        
        // Crear usuario de ejemplo
        Usuario usuarioAdmin = new Usuario("jesus.mechan", "1234", Rol.ADMINISTRADOR, empleadoAdmin);
        usuarioService.registrarUsuario(usuarioAdmin);

        Usuario usuarioAdmin2 = new Usuario("juana.rivera", "1234", Rol.OPERADOR, empleadoAdmin2);
        usuarioService.registrarUsuario(usuarioAdmin2);

    }
}

