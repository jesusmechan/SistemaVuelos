import ui.MenuService;

/**
 * Clase principal del Sistema de Gestión de Vuelos
 * Aplica principios SOLID:
 * - SRP: Cada clase tiene una única responsabilidad
 * - OCP: Abierto a extensión, cerrado a modificación
 * - LSP: Las clases derivadas pueden sustituir a sus bases
 * - ISP: Interfaces específicas y no generales
 * - DIP: Dependencia de abstracciones, no de implementaciones concretas
 */
public class Main {
    public static void main(String[] args) {
        MenuService menuService = new MenuService();
        menuService.mostrarMenuPrincipal();
    }
}
