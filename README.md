# Documentación de Relaciones del Sistema de Gestión de Vuelos

## 📋 Índice
1. [Diagrama de Relaciones](#diagrama-de-relaciones)
2. [Relaciones Entre Entidades](#relaciones-entre-entidades)
3. [Jerarquía de Herencia](#jerarquía-de-herencia)
4. [Sistema de Roles y Permisos](#sistema-de-roles-y-permisos)
5. [Estructura del Sistema](#estructura-del-sistema)
6. [Principios SOLID Aplicados](#principios-solid-aplicados)

---

## 📊 Diagrama de Relaciones

```
                    ┌─────────────┐
                    │   Persona   │
                    │  (Clase Base)│
                    └──────┬──────┘
                           │
            ┌──────────────┴──────────────┐
            │                              │
    ┌───────▼──────┐              ┌────────▼────────┐
    │   Empleado   │              │    Pasajero    │
    └──────┬───────┘              └────────────────┘
           │
           │ (Composición)
           │
    ┌──────▼──────┐
    │   Usuario   │──────────┐
    └──────┬──────┘          │
           │                  │
           │                  │
    ┌──────▼──────┐   ┌───────▼────────┐
    │     Rol     │   │   Empleado     │
    │   (Enum)    │   │  (Referencia)  │
    └─────────────┘   └────────────────┘
```

---

## 🔗 Relaciones Entre Entidades

### 1. **Persona → Empleado / Pasajero (Herencia)**

**Tipo de Relación:** Herencia (`extends`)

**Descripción:**
- `Persona` es la clase base abstracta que contiene información común a todas las personas del sistema
- `Empleado` y `Pasajero` heredan de `Persona`, lo que les permite reutilizar los atributos y métodos definidos en la clase base

**Atributos de Persona (heredados):**
```java
- String dni
- String nombre
- String apellido
- String email
- String telefono
```

**Beneficios:**
- ✅ Evita duplicación de código
- ✅ Facilita el mantenimiento
- ✅ Permite aplicar el principio LSP (Liskov Substitution Principle)

**Ejemplo:**
```java
Persona persona = new Empleado(...);  // ✅ Válido - LSP
Persona persona = new Pasajero(...); // ✅ Válido - LSP
```

---

### 2. **Usuario ← Empleado (Composición)**

**Tipo de Relación:** Composición (`has-a`)

**Descripción:**
- Un `Usuario` tiene una relación de composición con `Empleado`
- Un usuario siempre está asociado a un empleado (no existe un usuario sin empleado)
- Esto permite que solo los empleados puedan tener credenciales de acceso al sistema

**Atributos de Usuario:**
```java
- String nombreUsuario
- String contrasena
- Rol rol
- Empleado empleado  // ← Composición
```

**Cardinalidad:**
- **Empleado → Usuario:** 1 a 0..1 (Un empleado puede tener 0 o 1 usuario)
- **Usuario → Empleado:** 1 a 1 (Un usuario siempre tiene un empleado)

**Ejemplo de uso:**
```java
Empleado empleado = new Empleado(...);
Usuario usuario = new Usuario("admin", "pass123", Rol.ADMINISTRADOR, empleado);
```

**Reglas de negocio:**
- Para crear un usuario, primero debe existir un empleado
- El usuario hereda información del empleado asociado (nombre, email, etc.)

---

### 3. **Usuario → Rol (Asociación con Enum)**

**Tipo de Relación:** Asociación (`uses`)

**Descripción:**
- `Usuario` utiliza un `Rol` para determinar sus permisos en el sistema
- `Rol` es un Enum que define los tipos de roles disponibles

**Valores posibles de Rol:**
```java
enum Rol {
    ADMINISTRADOR,
    OPERADOR,
    VENDEDOR
}
```

**Cardinalidad:**
- **Usuario → Rol:** Muchos a 1 (Muchos usuarios pueden tener el mismo rol)

**Propósito:**
- Control de acceso basado en roles (RBAC - Role-Based Access Control)
- Determina qué opciones del menú puede ver cada usuario

---

## 📈 Jerarquía de Herencia

### Árbol de Herencia

```
                    Persona
                      │
        ┌─────────────┴─────────────┐
        │                           │
    Empleado                    Pasajero
        │
    Usuario (usa Empleado)
```

### Detalles de las Clases

#### **Persona (Clase Base)**
```java
public class Persona {
    protected String dni;
    protected String nombre;
    protected String apellido;
    protected String email;
    protected String telefono;
}
```

**Responsabilidad:** Almacenar información básica común a todas las personas

#### **Empleado (Extiende Persona)**
```java
public class Empleado extends Persona {
    private String numeroEmpleado;
    private String cargo;
    private LocalDate fechaContratacion;
    private double salario;
}
```

**Responsabilidad:** Representar a un empleado de la empresa con información laboral

#### **Pasajero (Extiende Persona)**
```java
public class Pasajero extends Persona {
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String numeroPasaporte;
}
```

**Responsabilidad:** Representar a un pasajero que puede realizar reservas

---

## 🔐 Sistema de Roles y Permisos

### Tabla de Permisos por Rol

| Funcionalidad | ADMINISTRADOR | OPERADOR | VENDEDOR |
|--------------|---------------|----------|----------|
| **Gestión de Empleados** | ✅ Completo | ❌ | ❌ |
| **Gestión de Usuarios** | ✅ Completo | ❌ | ❌ |
| **Gestión de Pasajeros** | ✅ Completo | ✅ Solo Registrar | ❌ |
| **Gestión de Aviones** | ✅ Completo | ❌ | ❌ |
| **Gestión de Vuelos** | ✅ Completo | ✅ Solo Registrar | 👁️ Solo Consultar |
| **Gestión de Reservas** | ✅ Completo | ✅ Solo Crear | 👁️ Solo Consultar |

**Leyenda:**
- ✅ = Acceso completo (crear, leer, actualizar, eliminar)
- 👁️ = Solo lectura (consultar)
- ❌ = Sin acceso

### Flujo de Autenticación

```
1. Usuario ingresa credenciales (nombreUsuario, contrasena)
2. Sistema busca Usuario por nombreUsuario
3. Sistema verifica contraseña
4. Si es válido, obtiene el Rol del Usuario
5. Sistema muestra menú según el Rol
```

### Ejemplo de Implementación

```java
// Autenticación
Usuario usuario = usuarioService.autenticar("admin", "admin123");

// Verificación de rol
if (usuario.getRol() == Rol.ADMINISTRADOR) {
    mostrarMenuAdministrador();
} else if (usuario.getRol() == Rol.OPERADOR) {
    mostrarMenuOperador();
}
```

---

## 🏗️ Estructura del Sistema

### Capas de la Arquitectura

```
┌─────────────────────────────────────┐
│         Capa de Presentación         │
│         (MenuService - UI)           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Capa de Servicios            │
│  (EmpleadoService, UsuarioService,   │
│   PasajeroService, etc.)             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Capa de Repositorios        │
│  (EmpleadoRepository, UsuarioRepo,  │
│   etc.) - Implementa Interfaces     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Capa de Modelos             │
│  (Persona, Empleado, Usuario, etc.) │
└─────────────────────────────────────┘
```

### Relaciones entre Capas

1. **UI → Services:** La interfaz depende de los servicios
2. **Services → Repositories:** Los servicios dependen de interfaces de repositorios (DIP)
3. **Repositories → Models:** Los repositorios trabajan con las entidades del modelo

---

## 🎯 Principios SOLID Aplicados

### 1. **Single Responsibility Principle (SRP)**

**Cada clase tiene una única responsabilidad:**

- **Persona:** Almacenar datos básicos de una persona
- **Empleado:** Gestionar información laboral
- **Usuario:** Manejar autenticación y autorización
- **EmpleadoService:** Lógica de negocio de empleados
- **MenuService:** Interfaz de usuario

### 2. **Open/Closed Principle (OCP)**

**Las clases están abiertas a extensión pero cerradas a modificación:**

- Se puede agregar un nuevo tipo de Persona (ej: `Cliente`) sin modificar `Persona`
- Se puede agregar un nuevo Rol sin modificar `Usuario`
- Los repositorios pueden extenderse para usar bases de datos sin modificar servicios

**Ejemplo:**
```java
// Se puede extender sin modificar Persona
public class Cliente extends Persona {
    // Nuevo tipo sin cambiar Persona
}
```

### 3. **Liskov Substitution Principle (LSP)**

**Las clases derivadas pueden sustituir a sus clases base:**

- Un `Empleado` puede usarse donde se espera una `Persona`
- Un `Pasajero` puede usarse donde se espera una `Persona`

**Ejemplo:**
```java
// ✅ Válido - LSP
List<Persona> personas = new ArrayList<>();
personas.add(new Empleado(...));
personas.add(new Pasajero(...));

// Todas son Personas
for (Persona p : personas) {
    System.out.println(p.getNombreCompleto());
}
```

### 4. **Interface Segregation Principle (ISP)**

**Las interfaces son específicas, no generales:**

- `IEmpleadoRepository` tiene métodos específicos para empleados
- `IUsuarioRepository` tiene métodos específicos para usuarios
- No hay una interfaz genérica con métodos innecesarios

**Ejemplo:**
```java
// ✅ Interfaz específica
interface IEmpleadoRepository {
    void guardar(Empleado empleado);
    Optional<Empleado> buscarPorNumeroEmpleado(String numero);
}

// ❌ No hay una interfaz genérica con métodos no usados
```

### 5. **Dependency Inversion Principle (DIP)**

**Dependencia de abstracciones, no de implementaciones concretas:**

- Los servicios dependen de interfaces de repositorios
- Los repositorios implementan interfaces
- Facilita el cambio de implementación (memoria → base de datos)

**Ejemplo:**
```java
// Servicio depende de la interfaz, no de la implementación
public class EmpleadoService {
    private final IEmpleadoRepository empleadoRepository; // ← Interfaz
    
    public EmpleadoService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }
}

// Se puede inyectar cualquier implementación
EmpleadoService service = new EmpleadoService(
    new EmpleadoRepository() // o new EmpleadoDBRepository()
);
```

---

## 📝 Resumen de Relaciones

### Matriz de Relaciones

| Clase A | Relación | Clase B | Tipo | Cardinalidad |
|---------|----------|---------|------|--------------|
| Empleado | extiende | Persona | Herencia | 1:1 |
| Pasajero | extiende | Persona | Herencia | 1:1 |
| Usuario | compone | Empleado | Composición | 1:1 |
| Usuario | usa | Rol | Asociación | N:1 |
| Vuelo | usa | Avion | Asociación | N:1 |
| Reserva | usa | Pasajero | Asociación | N:1 |
| Reserva | usa | Vuelo | Asociación | N:1 |

---

## 🔄 Flujo de Creación de Usuario

```
1. Crear Empleado
   └─> Empleado empleado = new Empleado(...)

2. Asignar Rol
   └─> Rol rol = Rol.ADMINISTRADOR

3. Crear Usuario (asociado al Empleado)
   └─> Usuario usuario = new Usuario("admin", "pass", rol, empleado)

4. Registrar Usuario
   └─> usuarioService.registrarUsuario(usuario)
```

---

## 📌 Puntos Importantes

### ✅ Buenas Prácticas Implementadas

1. **Separación de responsabilidades:** Cada clase tiene un propósito único
2. **Composición sobre herencia:** Usuario compone Empleado en lugar de heredar
3. **Inversión de dependencias:** Servicios dependen de interfaces
4. **Control de acceso basado en roles:** Sistema RBAC implementado
5. **Validaciones de negocio:** Reglas de negocio en los servicios

### ⚠️ Consideraciones

1. **Un empleado puede no tener usuario:** No todos los empleados necesitan acceso al sistema
2. **Un usuario siempre tiene empleado:** No hay usuarios sin empleado asociado
3. **Los roles determinan permisos:** El menú se adapta según el rol del usuario
4. **Los pasajeros no tienen usuarios:** Los pasajeros no acceden al sistema directamente

---

## 📚 Referencias de Código

### Ubicación de Clases Principales

```
src/
├── model/
│   ├── Persona.java          # Clase base
│   ├── Empleado.java         # Extiende Persona
│   ├── Pasajero.java         # Extiende Persona
│   ├── Usuario.java          # Compone Empleado
│   └── Rol.java              # Enum de roles
├── repository/
│   ├── IEmpleadoRepository.java
│   ├── IUsuarioRepository.java
│   └── [Implementaciones]
├── service/
│   ├── IEmpleadoService.java
│   ├── IUsuarioService.java
│   └── [Implementaciones]
└── ui/
    └── MenuService.java       # Interfaz de usuario
```

---

**Documento generado para el Sistema de Gestión de Vuelos**
*Aplicando principios SOLID y POO*

