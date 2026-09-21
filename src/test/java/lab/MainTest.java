package lab;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MainTest {

    @Test
    void main_shouldPrintVersion() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, Main.run(new String[]{"--version"}));
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("%s%s".formatted("1.0.0", System.lineSeparator()),
            output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void main_shouldPrintHelp() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, Main.run(new String[]{"--help"}));
        } finally {
            System.setOut(originalOut);
        }

        String help = output.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("--help"));
        assertTrue(help.contains("--version"));
        assertTrue(help.contains("Приклад запуску:"));
        assertTrue(help.contains("data/input.csv"));
    }

    @ParameterizedTest
    @MethodSource("validProductLines")
    void parseLine_shouldAcceptValidProducts(String line, int expectedWeight, double expectedCost, double expectedPrice)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Object product = invokeParseLine(line, 1, errors);

        assertNotNull(product);
        assertTrue(errors.isEmpty());
        assertEquals(expectedWeight, readField(product, "weightG"));
        assertEquals(expectedCost, (double) readField(product, "cost"), 1e-9);
        assertEquals(expectedPrice, (double) readField(product, "price"), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("invalidProductLines")
    void parseLine_shouldRejectInvalidProducts(String line, String expectedFragment) throws Exception {
        List<String> errors = new ArrayList<>();
        Object product = invokeParseLine(line, 2, errors);

        assertNull(product);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains(expectedFragment)),
                () -> "Expected fragment '" + expectedFragment + "' in logs: " + errors);
    }

    @Test
    void parseLine_shouldIgnoreEmptyLine() throws Exception {
        List<String> errors = new ArrayList<>();
        assertNull(invokeParseLine("", 1, errors));
        assertEquals(0, errors.size());
        assertNull(invokeParseLine("   ", 2, errors));
        assertEquals(0, errors.size());
    }

    @Test
    void parseLine_shouldHandleZeroValues() throws Exception {
        List<String> errors = new ArrayList<>();
        Object product = invokeParseLine("Товар;Тип;0;0;0", 3, errors);

        assertNotNull(product);
        assertTrue(errors.isEmpty());
        assertEquals(0, readField(product, "weightG"));
        assertEquals(0.0, (double) readField(product, "cost"), 1e-9);
        assertEquals(0.0, (double) readField(product, "price"), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("extremeProductLines")
    void parseLine_shouldAcceptExtremeFiniteValues(String line, int expectedWeight, double expectedCost,
            double expectedPrice) throws Exception {
        List<String> errors = new ArrayList<>();
        Object product = invokeParseLine(line, 1, errors);

        assertNotNull(product);
        assertTrue(errors.isEmpty());
        assertEquals(expectedWeight, readField(product, "weightG"));
        assertEquals(expectedCost, (double) readField(product, "cost"));
        assertEquals(expectedPrice, (double) readField(product, "price"));
    }

    @Test
    void main_shouldReportMissingInputFile() throws Exception {
        Path input = Path.of("data", "input.csv");
        Path backup = Path.of("data", "input.csv.test-backup");
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;

        Files.move(input, backup);
        try {
            System.setErr(new PrintStream(errorOutput, true, StandardCharsets.UTF_8));
            assertEquals(1, Main.run(new String[0]));
        } finally {
            System.setErr(originalErr);
            Files.move(backup, input);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                .contains("Файл не знайдено за шляхом:"));
    }

    @Test
    void hasAllFields_shouldReportMissingAndExtraFields() throws Exception {
        Method method = Main.class.getDeclaredMethod("hasAllFields", String[].class, int.class, List.class);
        method.setAccessible(true);

        List<String> missingLogs = new ArrayList<>();
        String[] missing = {"Назва", "Тип", "", "10.0", "20.0"};
        assertFalse((boolean) method.invoke(null, (Object) missing, 10, missingLogs));
        assertTrue(missingLogs.stream().anyMatch(s -> s.contains("Вага")));

        List<String> extraLogs = new ArrayList<>();
        String[] extra = {"Назва", "Тип", "100", "10.0", "20.0", "999"};
        assertFalse((boolean) method.invoke(null, (Object) extra, 11, extraLogs));
        assertTrue(extraLogs.stream().anyMatch(s -> s.contains("забагато")));
    }

    @Test
    void aggregateMethods_shouldCalculateTotalWeightAndAverageMargin() throws Exception {
        Object p1 = createProduct("A", "Тип1", 100, 10.0, 50.0);
        Object p2 = createProduct("B", "Тип2", 200, 20.0, 70.0);
        Object p3 = createProduct("C", "Тип3", 300, 30.0, 90.0);

        List<Object> products = new ArrayList<>(List.of(p1, p2, p3));

        assertEquals(600.0, invokeTotalWeight(products), 1e-9);
        assertEquals(50.0, invokeAverageMargin(products), 1e-9);
    }

    @Test
    void calculateAverageMargin_shouldAllowNegativeMargins() throws Exception {
        Object product = createProduct("Збитковий товар", "Тип", 100, 100.0, 40.0);

        assertEquals(-60.0, invokeAverageMargin(List.of(product)), 1e-9);
    }

    @Test
    void aggregateMethods_shouldReturnNullForEmptyList() throws Exception {
        List<Object> empty = List.of();
        assertEquals(0.0, invokeTotalWeight(empty), 1e-9);
        assertEquals(0.0, invokeAverageMargin(empty), 1e-9);
        assertNull(invokeFindMostExpensiveProduct(empty));
    }

    @Test
    void findMostExpensiveProduct_shouldReturnHighestPrice() throws Exception {
        Object a = createProduct("A", "Тип1", 50, 10.0, 40.0);
        Object b = createProduct("B", "Тип2", 80, 15.0, 60.0);
        Object c = createProduct("C", "Тип3", 30, 5.0, 60.0);

        List<Object> products = List.of(a, b, c);
        Object mostExpensive = invokeFindMostExpensiveProduct(products);

        assertNotNull(mostExpensive);
        assertEquals("B", readField(mostExpensive, "name"));
    }

    @Test
    void findMostExpensiveProduct_shouldKeepFirstWhenPricesAreEqual() throws Exception {
        Object a = createProduct("A", "Тип1", 50, 10.0, 60.0);
        Object b = createProduct("B", "Тип2", 80, 15.0, 60.0);

        List<Object> products = List.of(a, b);
        Object winner = invokeFindMostExpensiveProduct(products);

        assertNotNull(winner);
        assertEquals("A", readField(winner, "name"));
    }

    private static Stream<Arguments> validProductLines() {
        return Stream.of(
                Arguments.of("Хлiб пшеничний;хлiб;500;22.00;38.00", 500, 22.0, 38.0),
                Arguments.of("Печиво;печиво;200;30;50.00", 200, 30.0, 50.0),
                Arguments.of("Товар;Тип;10;5;7.50", 10, 5.0, 7.5),
                Arguments.of("Товар ; Тип ; 125 ; 30;50.00 ", 125, 30.0, 50.0)
        );
    }

    private static Stream<Arguments> extremeProductLines() {
        return Stream.of(
                Arguments.of("Максимум;Тип;%d;%s;%s".formatted(
                        Integer.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
                        Integer.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
                Arguments.of("Український товар;випiчка;1;0.01;0.02", 1, 0.01, 0.02)
        );
    }

    private static Stream<Arguments> invalidProductLines() {
        return Stream.of(
                Arguments.of("Товар;Тип;;10.0;20.0", "вiдсутнє поле"),
                Arguments.of("Товар;Тип;100;10.0", "вiдсутнє поле"),
                Arguments.of("Товар;Тип;100;10.0;20.0;99", "забагато полiв"),
                Arguments.of("Товар;Тип;abc;10.0;20.0", "Вага"),
                Arguments.of("Товар;Тип;100;abc;20.0", "Собiвартiсть"),
                Arguments.of("Товар;Тип;100;10.0;abc", "Цiна"),
                Arguments.of("Товар;Тип;-1;10.0;20.0", "Вага"),
                Arguments.of("Товар;Тип;100;-1;20.0", "Собiвартiсть"),
                Arguments.of("Товар;Тип;100;10.0;-1", "Цiна"),
                Arguments.of("Товар;Тип;100;NaN;20.0", "нечислове значення"),
                Arguments.of("Товар;Тип;100;Infinity;20.0", "нечислове значення"),
                Arguments.of("Товар;Тип;100;10.0;NaN", "нечислове значення"),
                Arguments.of("Товар;Тип;100;10.0;Infinity", "нечислове значення")
        );
    }

    private static Object invokeParseLine(String line, int lineNumber, List<String> errorLogs) throws Exception {
        Method method = Main.class.getDeclaredMethod("parseLine", String.class, int.class, List.class);
        method.setAccessible(true);
        return method.invoke(null, line, lineNumber, errorLogs);
    }

    private static Object readField(Object product, String fieldName) throws Exception {
        Field field = product.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(product);
    }

    private static Object createProduct(String name, String type, int weightG, double cost, double price)
            throws Exception {
        Class<?> clazz = Class.forName("lab.Main$Product");
        Constructor<?> ctor = clazz.getDeclaredConstructor(
                String.class, String.class, int.class, double.class, double.class
        );
        ctor.setAccessible(true);
        return ctor.newInstance(name, type, weightG, cost, price);
    }

    private static double invokeTotalWeight(List<Object> products) throws Exception {
        Method method = Main.class.getDeclaredMethod("calculateTotalWeight", List.class);
        method.setAccessible(true);
        return (double) method.invoke(null, products);
    }

    private static double invokeAverageMargin(List<Object> products) throws Exception {
        Method method = Main.class.getDeclaredMethod("calculateAverageMargin", List.class);
        method.setAccessible(true);
        return (double) method.invoke(null, products);
    }

    private static Object invokeFindMostExpensiveProduct(List<Object> products) throws Exception {
        Method method = Main.class.getDeclaredMethod("findMostExpensiveProduct", List.class);
        method.setAccessible(true);
        return method.invoke(null, products);
    }
}