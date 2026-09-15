package lab;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MainTest {

    @ParameterizedTest
    @MethodSource("validProductLines")
    void parseLine_shouldAcceptValidProducts(
            String line,
            int expectedWeight,
            double expectedCost,
            double expectedPrice
    ) throws Exception {
        List<String> errors = new ArrayList<>();
        Object product = invokeParseLine(line, 7, errors);

        assertNotNull(product, "Коректний рядок має бути збережений");
        assertTrue(errors.isEmpty(), "Для валідного рядка логи помилок мають бути порожніми");

        assertEquals(expectedWeight, readField(product, "weightG"));
        assertEquals(expectedCost, (double) readField(product, "cost"), 1e-9);
        assertEquals(expectedPrice, (double) readField(product, "price"), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("invalidProductLines")
    void parseLine_shouldRejectInvalidProducts(String line, String expectedFragment) throws Exception {
        List<String> errors = new ArrayList<>();
        Object product = invokeParseLine(line, 12, errors);

        assertNull(product, "Некоректний рядок не повинен розбиратися");
        assertFalse(errors.isEmpty(), "Для невалідного рядка треба записати помилку");
        assertTrue(
                errors.stream().anyMatch(entry -> entry.contains(expectedFragment)),
                () -> "Не знайдено фрагмента '" + expectedFragment + "' у логах: " + errors
        );
    }

    @Test
    void parseLine_shouldIgnoreBlankLines() throws Exception {
        List<String> errors = new ArrayList<>();
        assertNull(invokeParseLine("   ", 1, errors), "Порожній рядок має ігноруватися");
        assertTrue(errors.isEmpty(), "Для пустого рядка не повинно бути помилок");
    }

    @Test
    void hasAllFields_shouldReportMissingAndExtraColumns() throws Exception {
        Method method = Main.class.getDeclaredMethod("hasAllFields", String[].class, int.class, List.class);
        method.setAccessible(true);

        List<String> missingLogs = new ArrayList<>();
        String[] missingFields = {"Товар", "Тип", "", "10.0", "20.0"};
        assertFalse((boolean) method.invoke(null, (Object) missingFields, 3, missingLogs));
        assertTrue(
                missingLogs.stream().anyMatch(entry -> entry.contains("Вага")),
                "Лог має містити назву відсутнього поля 'Вага'"
        );

        List<String> extraLogs = new ArrayList<>();
        String[] extraFields = {"Товар", "Тип", "300", "12.0", "25.0", "99.0"};
        assertFalse((boolean) method.invoke(null, (Object) extraFields, 4, extraLogs));
        assertTrue(
                extraLogs.stream().anyMatch(entry -> entry.contains("забагато")),
                "Лог має містити попередження про зайві поля"
        );
    }

    @Test
    void aggregateMethods_shouldHandleEmptyAndTypicalInput() throws Exception {
        List<Object> empty = List.of();

        assertEquals(0.0, invokeTotalWeight(empty), 1e-9);
        assertEquals(0.0, invokeAverageMargin(empty), 1e-9);
        assertNull(invokeFindMostExpensiveProduct(empty), "Для порожнього списку має бути null");

        Object first = createProduct("Яблуко", "фрукти", 100, 10.0, 50.0);
        Object second = createProduct("Груша", "фрукти", 200, 15.0, 65.0);
        Object third = createProduct("Слива", "фрукти", 300, 20.0, 45.0);

        List<Object> products = new ArrayList<>(List.of(first, second, third));

        assertEquals(600.0, invokeTotalWeight(products), 1e-9);
        assertEquals(38.333333333333336, invokeAverageMargin(products), 1e-9);
        assertEquals(
                65.0,
                (double) readField(invokeFindMostExpensiveProduct(products), "price"),
                1e-9
        );
    }

    private static Stream<Arguments> validProductLines() {
        return Stream.of(
                Arguments.of("Хлiб пшеничний,хлiб,500,22.00,38.00", 500, 22.0, 38.0),
                Arguments.of("Печиво,печиво,200,30.00,58.00", 200, 30.0, 58.0),
                Arguments.of("Молоко,напiй,250,18.50,41.25", 250, 18.5, 41.25)
        );
    }

    private static Stream<Arguments> invalidProductLines() {
        return Stream.of(
                Arguments.of("Круасан,кондитерський,90,помилка,55.00", "Собiвартiсть"),
                Arguments.of("Багет,хлiб,300,,22.50", "вiдсутнє поле"),
                Arguments.of("Пирiг яблучний,пирiг,-750,110.00,220.00", "Вага"),
                Arguments.of("Булочка з корицею,випiчка,120,18.50,32.00,13.25", "забагато полiв"),
                Arguments.of("Яблуко,фрукти,100,NaN,15.0", "нечислове значення"),
                Arguments.of("Груша,фрукти,100,10.0,Infinity", "нечислове значення"),
                Arguments.of("Сир,молоч,100,12.0,-1.5", "вiд'ємним")
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
        Class<?> productClass = Class.forName("lab.Main$Product");
        Constructor<?> constructor = productClass.getDeclaredConstructor(
                String.class, String.class, int.class, double.class, double.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(name, type, weightG, cost, price);
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