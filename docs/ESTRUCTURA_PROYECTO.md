## Estructura del Proyecto · SisVuelos_Grupo1

Visión general de los directorios principales y su propósito dentro de la aplicación.

---

### `src/`
Código fuente Java del sistema.

- `Main.java`: Punto de entrada; instancia `MenuService` y lanza la interfaz de consola.
- `model/`: Entidades del dominio (POJO). Incluye clases como `Persona`, `Empleado`, `Vuelo`, `Reserva` y enumeraciones de estados/roles.
- `exception/`: Jerarquía de excepciones personalizadas (`DominioException`, `ValidacionException`, etc.) para validar datos y controlar reglas de negocio.
- `repository/`: Interfaces y repositorios en memoria para CRUD de cada entidad (`IEmpleadoRepository`, `ReservaRepository`, etc.).
- `service/`: Lógica de negocio y validaciones. Los servicios dependen de interfaces de repositorio e internamente manejan excepciones de dominio.
- `ui/`: Capa de presentación por consola (`MenuService`) que coordina menús, lectura de datos y salida formateada.
- `exception/`: Jerarquía de excepciones personalizadas (`DominioException`, `ValidacionException`, etc.) para manejo estructurado de errores.

---

### `docs/`
Documentación complementaria en Markdown:

- `SECCION_5_MODELADO.md`: Diagramas UML (clases, relaciones, jerarquías, casos de uso) y análisis conceptual.
- `FUNCIONALIDADES_SISTEMA.md`: Inventario de funcionalidades/casos de uso disponibles.
- `ESTRUCTURA_PROYECTO.md`: Este documento, descripción de carpetas.

---

### `out/`
Salida de compilación (clases `.class`) generada por el IDE. No se modifica manualmente.

---

### Otros archivos
- `README.md`: Descripción general del proyecto y pasos básicos.
- `SisVuelos_Grupo1.iml`: Configuración del proyecto para IntelliJ IDEA.

---

> **Nota:** Se recomienda mantener la lógica dentro de `src/` estructurada por capas (modelo → repositorio → servicio → UI) y usar `docs/` para toda documentación funcional o técnica adicional.

