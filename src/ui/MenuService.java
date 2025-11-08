package ui;

import exception.DominioException;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

        usuarioActual = null;
        
        try {
            usuarioActual = usuarioService.autenticar(nombreUsuario, contrasena).orElse(null);
            
            if (usuarioActual != null) {
                System.out.println("\n¡Bienvenido, " + usuarioActual.getNombre() + "!");
                mostrarMenuUsuario();
            }
        } catch (DominioException e) {
            System.out.println("Error: " + e.getMessage());
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

        ejecutarAccion(() -> {
            empleadoService.registrarEmpleado(empleado);
            System.out.println("Empleado registrado exitosamente.");
        });
    }

    private void buscarEmpleadoPorDni() {
        System.out.print("\nIngrese el DNI: ");
        String dni = scanner.nextLine();
        empleadoService.buscarEmpleadoPorDni(dni).ifPresentOrElse(
            empleado -> {
                System.out.println("\n=== EMPLEADO ENCONTRADO ===");
                imprimirTablaEmpleados(List.of(empleado));
                System.out.println("Total encontrados: 1");
            },
            () -> System.out.println("Empleado no encontrado.")
        );
    }

    private void listarEmpleados() {
        List<Empleado> empleados = empleadoService.listarTodosLosEmpleados();
        if (empleados.isEmpty()) {
            System.out.println("No hay empleados registrados.");
        } else {
            System.out.println("\n=== LISTA DE EMPLEADOS ===");
            imprimirTablaEmpleados(empleados);
            System.out.println("Total de empleados: " + empleados.size());
        }
    }

    private void buscarEmpleadosPorCargo() {
        System.out.print("\nIngrese el cargo: ");
        String cargo = scanner.nextLine();
        ejecutarAccion(() -> {
            List<Empleado> empleados = empleadoService.buscarEmpleadosPorCargo(cargo);
            if (empleados.isEmpty()) {
                System.out.println("No se encontraron empleados con ese cargo.");
            } else {
                System.out.println("\n=== EMPLEADOS CON CARGO: " + cargo.toUpperCase() + " ===");
                imprimirTablaEmpleados(empleados);
                System.out.println("Total encontrados: " + empleados.size());
            }
        });
    }

    private void imprimirTablaEmpleados(List<Empleado> empleados) {
        String headerFormat = "%-12s %-25s %-30s %-15s %-12s %-18s %-12s %-10s%n";
        String rowFormat = "%-12s %-25s %-30s %-15s %-12s %-18s %-12s %-10.2f%n";
        String separator = "---------------------------------------------------------------------------------------------------------------";

        System.out.printf(headerFormat,
                "DNI",
                "Nombre completo",
                "Email",
                "Teléfono",
                "N° Empleado",
                "Cargo",
                "F. Contrato",
                "Salario");
        System.out.println(separator);

        empleados.forEach(empleado -> System.out.printf(rowFormat,
                empleado.getDni(),
                empleado.getNombreCompleto(),
                empleado.getEmail(),
                empleado.getTelefono(),
                empleado.getNumeroEmpleado(),
                empleado.getCargo(),
                empleado.getFechaContratacion(),
                empleado.getSalario()));

        System.out.println(separator);
    }

    private void imprimirTablaUsuarios(List<Usuario> usuarios) {
        String headerFormat = "%-20s %-15s %-25s %-30s%n";
        String rowFormat = "%-20s %-15s %-25s %-30s%n";
        String separator = "-------------------------------------------------------------------------------------------";

        System.out.printf(headerFormat, "Usuario", "Rol", "Empleado", "Email");
        System.out.println(separator);

        usuarios.forEach(usuario -> {
            String rol = usuario.getRol() != null ? usuario.getRol().toString() : "N/A";
            String empleadoNombre = usuario.getEmpleado() != null ? usuario.getEmpleado().getNombreCompleto() : "N/A";
            String email = usuario.getEmpleado() != null ? usuario.getEmpleado().getEmail() : "N/A";

            System.out.printf(rowFormat,
                    usuario.getNombreUsuario(),
                    rol,
                    empleadoNombre,
                    email);
        });

        System.out.println(separator);
    }

    private void imprimirTablaPasajeros(List<Pasajero> pasajeros) {
        String headerFormat = "%-12s %-25s %-30s %-15s %-12s %-15s %-18s%n";
        String rowFormat = "%-12s %-25s %-30s %-15s %-12s %-15s %-18s%n";
        String separator = "---------------------------------------------------------------------------------------------------------------";

        System.out.printf(headerFormat,
                "DNI",
                "Nombre completo",
                "Email",
                "Teléfono",
                "F. Nac.",
                "Nacionalidad",
                "Pasaporte");
        System.out.println(separator);

        pasajeros.forEach(pasajero -> System.out.printf(rowFormat,
                pasajero.getDni(),
                pasajero.getNombreCompleto(),
                pasajero.getEmail(),
                pasajero.getTelefono(),
                pasajero.getFechaNacimiento() != null ? pasajero.getFechaNacimiento() : "N/A",
                pasajero.getNacionalidad() != null ? pasajero.getNacionalidad() : "N/A",
                pasajero.getNumeroPasaporte() != null ? pasajero.getNumeroPasaporte() : "N/A"));

        System.out.println(separator);
    }

    private void imprimirTablaAviones(List<Avion> aviones) {
        String headerFormat = "%-15s %-20s %-20s %-15s %-15s %-15s%n";
        String rowFormat = "%-15s %-20s %-20s %-15d %-15d %-15s%n";
        String separator = "-----------------------------------------------------------------------------------------------";

        System.out.printf(headerFormat,
                "N° Serie",
                "Modelo",
                "Fabricante",
                "Cap. Pasajeros",
                "Cap. Carga",
                "Estado");
        System.out.println(separator);

        aviones.forEach(avion -> System.out.printf(rowFormat,
                avion.getNumeroSerie(),
                avion.getModelo(),
                avion.getFabricante(),
                avion.getCapacidadPasajeros(),
                avion.getCapacidadCarga(),
                avion.getEstado() != null ? avion.getEstado() : "N/A"));

        System.out.println(separator);
    }

    private void imprimirTablaVuelos(List<Vuelo> vuelos) {
        String headerFormat = "%-10s %-15s %-15s %-18s %-18s %-25s %-10s %-12s %-10s%n";
        String rowFormat = "%-10s %-15s %-15s %-18s %-18s %-25s %-10.2f %-12d %-10s%n";
        String separator = "----------------------------------------------------------------------------------------------------------------------------";

        System.out.printf(headerFormat,
                "N° Vuelo",
                "Origen",
                "Destino",
                "Salida",
                "Llegada",
                "Avión",
                "Precio",
                "Asientos",
                "Estado");
        System.out.println(separator);

        vuelos.forEach(vuelo -> {
            String salida = formatearFechaHora(vuelo.getFechaHoraSalida());
            String llegada = formatearFechaHora(vuelo.getFechaHoraLlegada());
            String avionInfo = vuelo.getAvion() != null
                    ? vuelo.getAvion().getModelo() + " (" + vuelo.getAvion().getNumeroSerie() + ")"
                    : "N/A";
            System.out.printf(rowFormat,
                    vuelo.getNumeroVuelo(),
                    vuelo.getOrigen(),
                    vuelo.getDestino(),
                    salida,
                    llegada,
                    avionInfo,
                    vuelo.getPrecio(),
                    vuelo.getAsientosDisponibles(),
                    vuelo.getEstado() != null ? vuelo.getEstado() : "N/A");
        });

        System.out.println(separator);
    }

    private void imprimirTablaReservas(List<Reserva> reservas) {
        String headerFormat = "%-12s %-25s %-12s %-20s %-12s %-10s %-10s%n";
        String rowFormat = "%-12s %-25s %-12s %-20s %-12s %-10d %-10.2f%n";
        String separator = "------------------------------------------------------------------------------------------------------";

        System.out.printf(headerFormat,
                "N° Reserva",
                "Pasajero",
                "DNI",
                "Vuelo",
                "Fecha",
                "Asiento",
                "Total");
        System.out.println(separator);

        reservas.forEach(reserva -> {
            Pasajero pasajero = reserva.getPasajero();
            Vuelo vuelo = reserva.getVuelo();
            String pasajeroNombre = pasajero != null ? pasajero.getNombreCompleto() : "N/A";
            String pasajeroDni = pasajero != null ? pasajero.getDni() : "N/A";
            String vueloInfo = vuelo != null ? vuelo.getNumeroVuelo() : "N/A";
            String fecha = reserva.getFechaReserva() != null ? formatearFechaHora(reserva.getFechaReserva()) : "N/A";

            System.out.printf(rowFormat,
                    reserva.getNumeroReserva(),
                    pasajeroNombre,
                    pasajeroDni,
                    vueloInfo,
                    fecha,
                    reserva.getNumeroAsiento(),
                    reserva.calcularTotal());
        });

        System.out.println(separator);
    }

    private String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(DATE_TIME_FORMATTER) : "N/A";
    }

    private void ejecutarAccion(Runnable accion) {
        try {
            accion.run();
        } catch (DominioException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }

    private void eliminarEmpleado() {
        System.out.print("\nIngrese el DNI del empleado a eliminar: ");
        String dni = scanner.nextLine();
        ejecutarAccion(() -> {
            empleadoService.eliminarEmpleado(dni);
            System.out.println("Empleado eliminado exitosamente.");
        });
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
            empleado -> ejecutarAccion(() -> {
                Usuario usuario = new Usuario(nombreUsuario, contrasena, rolFinal, empleado);
                usuarioService.registrarUsuario(usuario);
                System.out.println("Usuario registrado exitosamente.");
            }),
            () -> System.out.println("Empleado no encontrado. Debe registrar el empleado primero.")
        );
    }

    private void buscarUsuario() {
        System.out.print("\nIngrese el nombre de usuario: ");
        String nombreUsuario = scanner.nextLine();
        usuarioService.buscarUsuario(nombreUsuario).ifPresentOrElse(
            usuario -> {
                System.out.println("\n=== USUARIO ENCONTRADO ===");
                imprimirTablaUsuarios(List.of(usuario));
                System.out.println("Total encontrados: 1");
            },
            () -> System.out.println("Usuario no encontrado.")
        );
    }

    private void listarUsuarios() {
        System.out.println("\n=== LISTA DE USUARIOS ===");
        List<Usuario> usuarios = usuarioService.listarTodosLosUsuarios();
        
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
        } else {
            imprimirTablaUsuarios(usuarios);
            System.out.println("\nTotal de usuarios: " + usuarios.size());
        }
    }

    private void eliminarUsuario() {
        System.out.print("\nIngrese el nombre de usuario a eliminar: ");
        String nombreUsuario = scanner.nextLine();
        ejecutarAccion(() -> {
            usuarioService.eliminarUsuario(nombreUsuario);
            System.out.println("Usuario eliminado exitosamente.");
        });
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
        
        ejecutarAccion(() -> {
            pasajeroService.registrarPasajero(pasajero);
            System.out.println("Pasajero registrado exitosamente.");
        });
    }

    private void buscarPasajeroPorDni() {
        System.out.print("\nIngrese el DNI: ");
        String dni = scanner.nextLine();
        pasajeroService.buscarPasajeroPorDni(dni).ifPresentOrElse(
            pasajero -> {
                System.out.println("\n=== PASAJERO ENCONTRADO ===");
                imprimirTablaPasajeros(List.of(pasajero));
                System.out.println("Total encontrados: 1");
            },
            () -> System.out.println("Pasajero no encontrado.")
        );
    }

    private void listarPasajeros() {
        List<Pasajero> pasajeros = pasajeroService.listarTodosLosPasajeros();
        if (pasajeros.isEmpty()) {
            System.out.println("No hay pasajeros registrados.");
        } else {
            System.out.println("\n=== LISTA DE PASAJEROS ===");
            imprimirTablaPasajeros(pasajeros);
            System.out.println("Total de pasajeros: " + pasajeros.size());
        }
    }

    private void eliminarPasajero() {
        System.out.print("\nIngrese el DNI del pasajero a eliminar: ");
        String dni = scanner.nextLine();
        ejecutarAccion(() -> {
            pasajeroService.eliminarPasajero(dni);
            System.out.println("Pasajero eliminado exitosamente.");
        });
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
        
        ejecutarAccion(() -> {
            avionService.registrarAvion(avion);
            System.out.println("Avión registrado exitosamente.");
        });
    }

    private void buscarAvionPorNumeroSerie() {
        System.out.print("\nIngrese el número de serie: ");
        String numeroSerie = scanner.nextLine();
        ejecutarAccion(() -> avionService.buscarAvionPorNumeroSerie(numeroSerie).ifPresentOrElse(
                avion -> {
                    System.out.println("\n=== AVIÓN ENCONTRADO ===");
                    imprimirTablaAviones(List.of(avion));
                    System.out.println("Total encontrados: 1");
                },
                () -> System.out.println("Avión no encontrado.")
        ));
    }

    private void listarAviones() {
        List<Avion> aviones = avionService.listarTodosLosAviones();
        if (aviones.isEmpty()) {
            System.out.println("No hay aviones registrados.");
        } else {
            System.out.println("\n=== LISTA DE AVIONES ===");
            imprimirTablaAviones(aviones);
            System.out.println("Total de aviones: " + aviones.size());
        }
    }

    private void listarAvionesDisponibles() {
        ejecutarAccion(() -> {
            List<Avion> aviones = avionService.buscarAvionesDisponibles();
            if (aviones.isEmpty()) {
                System.out.println("No hay aviones disponibles.");
            } else {
                System.out.println("\n=== AVIONES DISPONIBLES ===");
                imprimirTablaAviones(aviones);
                System.out.println("Total disponibles: " + aviones.size());
            }
        });
    }

    private void eliminarAvion() {
        System.out.print("\nIngrese el número de serie del avión a eliminar: ");
        String numeroSerie = scanner.nextLine();
        ejecutarAccion(() -> {
            avionService.eliminarAvion(numeroSerie);
            System.out.println("Avión eliminado exitosamente.");
        });
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
        
        ejecutarAccion(() -> avionService.buscarAvionPorNumeroSerie(numeroSerie).ifPresentOrElse(
                avion -> {
                    Vuelo vuelo = new Vuelo(numeroVuelo, origen, destino, fechaHoraSalida,
                            fechaHoraLlegada, avion, precio);
                    vueloService.registrarVuelo(vuelo);
                    System.out.println("Vuelo registrado exitosamente.");
                },
                () -> System.out.println("Avión no encontrado. Debe registrar el avión primero.")
        ));
    }

    private void buscarVueloPorNumero() {
        System.out.print("\nIngrese el número de vuelo: ");
        String numeroVuelo = scanner.nextLine();
        ejecutarAccion(() -> vueloService.buscarVueloPorNumero(numeroVuelo).ifPresentOrElse(
                vuelo -> {
                    System.out.println("\n=== VUELO ENCONTRADO ===");
                    imprimirTablaVuelos(List.of(vuelo));
                    System.out.println("Total encontrados: 1");
                },
                () -> System.out.println("Vuelo no encontrado.")
        ));
    }

    private void listarVuelos() {
        List<Vuelo> vuelos = vueloService.listarTodosLosVuelos();
        if (vuelos.isEmpty()) {
            System.out.println("No hay vuelos registrados.");
        } else {
            System.out.println("\n=== LISTA DE VUELOS ===");
            imprimirTablaVuelos(vuelos);
            System.out.println("Total de vuelos: " + vuelos.size());
        }
    }

    private void buscarVuelosPorOrigen() {
        System.out.print("\nIngrese el origen: ");
        String origen = scanner.nextLine();
        ejecutarAccion(() -> {
            List<Vuelo> vuelos = vueloService.buscarVuelosPorOrigen(origen);
            if (vuelos.isEmpty()) {
                System.out.println("No se encontraron vuelos con ese origen.");
            } else {
                System.out.println("\n=== VUELOS DESDE " + origen.toUpperCase() + " ===");
                imprimirTablaVuelos(vuelos);
                System.out.println("Total encontrados: " + vuelos.size());
            }
        });
    }

    private void buscarVuelosPorDestino() {
        System.out.print("\nIngrese el destino: ");
        String destino = scanner.nextLine();
        ejecutarAccion(() -> {
            List<Vuelo> vuelos = vueloService.buscarVuelosPorDestino(destino);
            if (vuelos.isEmpty()) {
                System.out.println("No se encontraron vuelos con ese destino.");
            } else {
                System.out.println("\n=== VUELOS HACIA " + destino.toUpperCase() + " ===");
                imprimirTablaVuelos(vuelos);
                System.out.println("Total encontrados: " + vuelos.size());
            }
        });
    }

    private void buscarVuelosPorRuta() {
        System.out.print("\nIngrese el origen: ");
        String origen = scanner.nextLine();
        System.out.print("Ingrese el destino: ");
        String destino = scanner.nextLine();
        ejecutarAccion(() -> {
            List<Vuelo> vuelos = vueloService.buscarVuelosPorRuta(origen, destino);
            if (vuelos.isEmpty()) {
                System.out.println("No se encontraron vuelos en esa ruta.");
            } else {
                System.out.println("\n=== VUELOS " + origen.toUpperCase() + " ➜ " + destino.toUpperCase() + " ===");
                imprimirTablaVuelos(vuelos);
                System.out.println("Total encontrados: " + vuelos.size());
            }
        });
    }

    private void eliminarVuelo() {
        System.out.print("\nIngrese el número de vuelo a eliminar: ");
        String numeroVuelo = scanner.nextLine();
        ejecutarAccion(() -> {
            vueloService.eliminarVuelo(numeroVuelo);
            System.out.println("Vuelo eliminado exitosamente.");
        });
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

        ejecutarAccion(() -> pasajeroService.buscarPasajeroPorDni(dniPasajero).ifPresentOrElse(
                pasajero -> vueloService.buscarVueloPorNumero(numeroVuelo).ifPresentOrElse(
                        vuelo -> {
                            Reserva reserva = new Reserva(numeroReserva, pasajero, vuelo, numeroAsiento);
                            reservaService.crearReserva(reserva);
                            System.out.println("Reserva creada exitosamente.");
                        },
                        () -> System.out.println("Vuelo no encontrado.")
                ),
                () -> System.out.println("Pasajero no encontrado. Debe registrar el pasajero primero.")
        ));
    }

    private void buscarReservaPorNumero() {
        System.out.print("\nIngrese el número de reserva: ");
        String numeroReserva = scanner.nextLine();
        ejecutarAccion(() -> reservaService.buscarReservaPorNumero(numeroReserva).ifPresentOrElse(
                reserva -> {
                    System.out.println("\n=== RESERVA ENCONTRADA ===");
                    imprimirTablaReservas(List.of(reserva));
                    System.out.println("Total encontrados: 1");
                },
                () -> System.out.println("Reserva no encontrada.")
        ));
    }

    private void listarReservas() {
        List<Reserva> reservas = reservaService.listarTodasLasReservas();
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas registradas.");
        } else {
            System.out.println("\n=== LISTA DE RESERVAS ===");
            imprimirTablaReservas(reservas);
            System.out.println("Total de reservas: " + reservas.size());
        }
    }

    private void buscarReservasPorPasajero() {
        System.out.print("\nIngrese el DNI del pasajero: ");
        String dniPasajero = scanner.nextLine();
        ejecutarAccion(() -> {
            List<Reserva> reservas = reservaService.buscarReservasPorPasajero(dniPasajero);
            if (reservas.isEmpty()) {
                System.out.println("No se encontraron reservas para ese pasajero.");
            } else {
                System.out.println("\n=== RESERVAS DEL PASAJERO " + dniPasajero + " ===");
                imprimirTablaReservas(reservas);
                System.out.println("Total encontradas: " + reservas.size());
            }
        });
    }

    private void buscarReservasPorVuelo() {
        System.out.print("\nIngrese el número de vuelo: ");
        String numeroVuelo = scanner.nextLine();
        ejecutarAccion(() -> {
            List<Reserva> reservas = reservaService.buscarReservasPorVuelo(numeroVuelo);
            if (reservas.isEmpty()) {
                System.out.println("No se encontraron reservas para ese vuelo.");
            } else {
                System.out.println("\n=== RESERVAS DEL VUELO " + numeroVuelo + " ===");
                imprimirTablaReservas(reservas);
                System.out.println("Total encontradas: " + reservas.size());
            }
        });
    }

    private void cancelarReserva() {
        System.out.print("\nIngrese el número de reserva a cancelar: ");
        String numeroReserva = scanner.nextLine();
        ejecutarAccion(() -> {
            reservaService.cancelarReserva(numeroReserva);
            System.out.println("Reserva cancelada exitosamente.");
        });
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
                return LocalDateTime.parse(fechaHoraStr, DATE_TIME_FORMATTER);
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

