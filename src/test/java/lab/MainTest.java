package lab;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
        assertTrue(help.contains("--input"));
        assertTrue(help.contains("--output"));
        assertTrue(help.contains("Приклад запуску:"));
    }

    @ParameterizedTest
    @MethodSource("validProductLines")
    void parseLine_shouldAcceptValidProducts(String line, int expectedWeight, double expectedCost, double expectedPrice)
            throws Exception {
        List<String> errors = new ArrayList<>();
        BakeryItem product = invokeParseLine(line, 1, errors);

        assertNotNull(product);
        assertTrue(errors.isEmpty());
        assertEquals(expectedWeight, product.getWeightG());
        assertEquals(expectedCost, product.getCost(), 1e-9);
        assertEquals(expectedPrice, product.getPrice(), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("invalidProductLines")
    void parseLine_shouldRejectInvalidProducts(String line, String expectedFragment) throws Exception {
        List<String> errors = new ArrayList<>();
        BakeryItem product = invokeParseLine(line, 2, errors);

        assertNull(product);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains(expectedFragment)),
                () -> "Expected fragment '" + expectedFragment + "' in logs: " + errors);
    }

    @Test
    void parseLine_shouldReportEmptyLineAsError() throws Exception {
        List<String> errors = new ArrayList<>();
        assertNull(invokeParseLine("", 1, errors));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Рядок 1: порожній рядок"));

        errors.clear();
        assertNull(invokeParseLine("   ", 2, errors));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Рядок 2: порожній рядок"));
    }

    @Test
    void parseLine_shouldHandleZeroValues() throws Exception {
        List<String> errors = new ArrayList<>();
        BakeryItem product = invokeParseLine("Товар;Тип;0;0;0", 3, errors);

        assertNotNull(product);
        assertTrue(errors.isEmpty());
        assertEquals(0, product.getWeightG());
        assertEquals(0.0, product.getCost(), 1e-9);
        assertEquals(0.0, product.getPrice(), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("validConstructorArguments")
    void constructor_shouldCreateValidItems(String name, String type, int weightG, double cost, double price) {
        BakeryItem item = new BakeryItem(name, type, weightG, cost, price);

        assertEquals(name, item.getName());
        assertEquals(type, item.getType());
        assertEquals(weightG, item.getWeightG());
        assertEquals(cost, item.getCost());
        assertEquals(price, item.getPrice());
    }

    @ParameterizedTest
    @MethodSource("invalidConstructorArguments")
    void constructor_shouldRejectInvalidArguments(String name, String type, int weightG, double cost, double price,
            String expectedFragment) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new BakeryItem(name, type, weightG, cost, price));

        assertTrue(exception.getMessage().contains(expectedFragment),
                () -> "Expected message to contain '%s' but was: %s".formatted(expectedFragment, exception.getMessage()));
    }

    @ParameterizedTest
    @MethodSource("extremeProductLines")
    void parseLine_shouldAcceptExtremeFiniteValues(String line, int expectedWeight, double expectedCost,
            double expectedPrice) throws Exception {
        List<String> errors = new ArrayList<>();
        BakeryItem product = invokeParseLine(line, 1, errors);

        assertNotNull(product);
        assertTrue(errors.isEmpty());
        assertEquals(expectedWeight, product.getWeightG());
        assertEquals(expectedCost, product.getCost());
        assertEquals(expectedPrice, product.getPrice());
    }

    @Test
    void run_shouldRequireInputAndOutputArguments() {
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(errorOutput, true, StandardCharsets.UTF_8));
            assertEquals(1, Main.run(new String[0]));
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                .contains("необхідно вказати --input і --output"));
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
            assertEquals(1, Main.run(new String[]{
                    "--input", input.toString(), "--output", "data/report.txt"
            }));
        } finally {
            System.setErr(originalErr);
            Files.move(backup, input);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                .contains("Файл не знайдено за шляхом:"));
    }

    @Test
    void run_shouldReportNoMostExpensiveProductWhenInputIsInvalid() throws Exception {
        Path input = Files.createTempFile("invalid-products", ".csv");
        Path output = Files.createTempFile("invalid-products-report", ".txt");

        Files.writeString(input, "Товар;Тип;abc;10.0;20.0\nТовар;Тип;100;10.0;abc\n",
                StandardCharsets.UTF_8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
            assertEquals(0, Main.run(new String[]{"--input", input.toString(), "--output", output.toString()}));
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
        }

        String report = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("Найдорожчий товар: відсутній") || report.contains("Найдорожчий товар: вiдсутнiй"));
    }

    @Test
    void aggregateMethods_shouldCalculateTotalWeightAndAverageMargin() throws Exception {
        List<BakeryItem> products = List.of(
                new BakeryItem("A", "Тип1", 100, 10.0, 50.0),
                new BakeryItem("B", "Тип2", 200, 20.0, 70.0),
                new BakeryItem("C", "Тип3", 300, 30.0, 90.0));
        Object result = invokeWeightMargin(products);

        assertEquals(600.0, readRecordComponent(result, "totalWeight"), 1e-9);
        assertEquals(50.0, readRecordComponent(result, "averageMargin"), 1e-9);
    }

    @Test
    void calculateAverageMargin_shouldAllowNegativeMargins() throws Exception {
        BakeryItem product = new BakeryItem("Збитковий товар", "Тип", 100, 100.0, 40.0);

        assertEquals(-60.0, readRecordComponent(invokeWeightMargin(List.of(product)), "averageMargin"), 1e-9);
    }

    @Test
    void aggregateMethods_shouldReturnNullForEmptyList() throws Exception {
        List<BakeryItem> empty = List.of();
        Object result = invokeWeightMargin(empty);
        assertEquals(0.0, readRecordComponent(result, "totalWeight"), 1e-9);
        assertEquals(0.0, readRecordComponent(result, "averageMargin"), 1e-9);
        assertNull(invokeFindMostExpensiveProduct(empty));
    }

    @Test
    void findMostExpensiveProduct_shouldReturnHighestPrice() throws Exception {
        BakeryItem a = new BakeryItem("A", "Тип1", 50, 10.0, 40.0);
        BakeryItem b = new BakeryItem("B", "Тип2", 80, 15.0, 60.0);
        BakeryItem c = new BakeryItem("C", "Тип3", 30, 5.0, 60.0);

        List<BakeryItem> products = List.of(a, b, c);
        Object mostExpensive = invokeFindMostExpensiveProduct(products);

        assertNotNull(mostExpensive);
        assertEquals("B", ((BakeryItem) mostExpensive).getName());
    }

    @Test
    void findMostExpensiveProduct_shouldKeepFirstWhenPricesAreEqual() throws Exception {
        BakeryItem a = new BakeryItem("A", "Тип1", 50, 10.0, 60.0);
        BakeryItem b = new BakeryItem("B", "Тип2", 80, 15.0, 60.0);

        List<BakeryItem> products = List.of(a, b);
        Object winner = invokeFindMostExpensiveProduct(products);

        assertNotNull(winner);
        assertEquals("A", ((BakeryItem) winner).getName());
    }

    private static Stream<Arguments> validProductLines() {
        return Stream.of(
                Arguments.of("Хлiб пшеничний;хлiб;500;22.00;38.00", 500, 22.0, 38.0),
                Arguments.of("Печиво;печиво;200;30;50.00", 200, 30.0, 50.0),
                Arguments.of("Товар;Тип;10;5;7.50", 10, 5.0, 7.5),
                Arguments.of("Товар ; Тип ; 125 ; 30;50.00 ", 125, 30.0, 50.0)
        );
    }

    private static Stream<Arguments> validConstructorArguments() {
        return Stream.of(
                Arguments.of("Хліб", "Пшеничний", 500, 22.0, 38.0),
                Arguments.of("Товар", "Тип", 0, 0.0, 0.0),
                Arguments.of("Максимум", "Тип", Integer.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
        );
    }

    private static Stream<Arguments> invalidConstructorArguments() {
        return Stream.of(
                Arguments.of((String) null, "Тип", 100, 10.0, 20.0, "Назва"),
                Arguments.of("", "Тип", 100, 10.0, 20.0, "Назва"),
                Arguments.of("   ", "Тип", 100, 10.0, 20.0, "Назва"),
                Arguments.of("Товар", (String) null, 100, 10.0, 20.0, "тип"),
                Arguments.of("Товар", "", 100, 10.0, 20.0, "тип"),
                Arguments.of("Товар", "   ", 100, 10.0, 20.0, "тип"),
                Arguments.of("Товар", "Тип", -1, 10.0, 20.0, "Вага"),
                Arguments.of("Товар", "Тип", 100, -1.0, 20.0, "Собiвартiсть"),
                Arguments.of("Товар", "Тип", 100, Double.NaN, 20.0, "Собiвартiсть"),
                Arguments.of("Товар", "Тип", 100, 10.0, -1.0, "Цiна"),
                Arguments.of("Товар", "Тип", 100, 10.0, Double.POSITIVE_INFINITY, "Цiна")
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

    private static BakeryItem invokeParseLine(String line, int lineNumber, List<String> errorLogs) throws Exception {
        Method method = Main.class.getDeclaredMethod("parseLine", String.class, int.class, List.class);
        method.setAccessible(true);
        return (BakeryItem) method.invoke(null, line, lineNumber, errorLogs);
    }

    private static Object invokeWeightMargin(List<BakeryItem> products) throws Exception {
        Method method = Main.class.getDeclaredMethod("calculateWeightMargin", List.class);
        method.setAccessible(true);
        return method.invoke(null, products);
    }

    private static double readRecordComponent(Object record, String componentName) throws Exception {
        Method accessor = record.getClass().getDeclaredMethod(componentName);
        accessor.setAccessible(true);
        return (double) accessor.invoke(record);
    }

    private static Object invokeFindMostExpensiveProduct(List<BakeryItem> products) throws Exception {
        Method method = Main.class.getDeclaredMethod("findMostExpensiveProduct", List.class);
        method.setAccessible(true);
        return method.invoke(null, products);
    }

    @Test
    void main_shouldCreateMissingOutputDirectories() throws Exception {
        Path input = Files.createTempFile("products", ".csv");
        Path tempRoot = Files.createTempDirectory("output-directory-test");
        Path outputDirectory = tempRoot.resolve("missing").resolve("nested");
        Path report = outputDirectory.resolve("report.txt");
        Files.writeString(input, "Товар;Тип;100;10.0;20.0\n", StandardCharsets.UTF_8);

        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
            assertEquals(0, Main.run(new String[]{
                    "--input", input.toString(), "--output", report.toString()
            }));
            assertTrue(Files.isRegularFile(report));
            assertTrue(Files.readString(report, StandardCharsets.UTF_8).contains("Товар"));
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(report);
            Files.deleteIfExists(outputDirectory);
            Files.deleteIfExists(outputDirectory.getParent());
            Files.deleteIfExists(tempRoot);
            Files.deleteIfExists(input);
        }
    }

    @Test
    void run_shouldCreateMissingOutputDirectories() throws Exception {
        Path input = Files.createTempFile("products", ".csv");
        Path tempRoot = Files.createTempDirectory("output-directory-test");
        Path outputDirectory = tempRoot.resolve("missing").resolve("nested");
        Path report = outputDirectory.resolve("report.txt");
        Files.writeString(input, "Товар;Тип;100;10.0;20.0\n", StandardCharsets.UTF_8);

        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
            assertEquals(0, Main.run(new String[]{
                    "--input", input.toString(), "--output", report.toString()
            }));
            assertTrue(Files.isRegularFile(report));
            assertTrue(Files.readString(report, StandardCharsets.UTF_8).contains("Товар"));
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(report);
            Files.deleteIfExists(outputDirectory);
            Files.deleteIfExists(outputDirectory.getParent());
            Files.deleteIfExists(tempRoot);
            Files.deleteIfExists(input);
        }
    }
}