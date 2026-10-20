package lab;

/**
 * Точка входу консольного застосунку.
 */
public final class Main {
    private Main() {
    }

    /**
     * Передає аргументи контролеру та завершує JVM отриманим кодом.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        System.exit(MainController.run(args));
    }
}
