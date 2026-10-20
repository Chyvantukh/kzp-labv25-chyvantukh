package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BakeryItemTest {

    @Test
    void equals_shouldTreatItemsWithSameValuesAsEqual() {
        BakeryItem first = new Bread("Хліб", 500, 22.5, 38.75);
        BakeryItem second = BakeryItem.fromCsv("Хліб;хлiб;500;22.5;38.75");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equals_shouldTreatDifferentKindsAsDifferentItems() {
        BakeryItem bread = BakeryItem.fromCsv("Виріб;хлiб;100;10;20");
        BakeryItem pastry = BakeryItem.fromCsv("Виріб;печиво;100;10;20");

        assertNotEquals(bread, pastry);
    }

    @ParameterizedTest
    @MethodSource("itemsWithDifferentValues")
    void equals_shouldCompareEveryEntityField(BakeryItem first, BakeryItem second) {
        assertNotEquals(first, second);
    }

    @Test
    void hashSet_shouldRecognizeEquivalentItems() {
        Set<BakeryItem> items = new HashSet<>();
        items.add(new Bread("Хліб", 500, 22.5, 38.75));
        items.add(BakeryItem.fromCsv("Хліб;хлiб;500;22.5;38.75"));

        assertTrue(items.contains(BakeryItem.fromCsv("Хліб;хлiб;500;22.5;38.75")));
        assertEquals(1, items.size());
    }

    @Test
    void fromCsv_shouldCreateBreadWithTrimmedFieldsAndDecimalComma() {
        BakeryItem item = BakeryItem.fromCsv("  Хліб ; хлiб ; 500 ; 22,50 ; 38.75 ");

        assertInstanceOf(Bread.class, item);
        assertEquals("Хліб", item.getName());
        assertEquals(BakeryItem.BakeryKind.BREAD, item.getKind());
        assertEquals(500, item.getWeightG());
        assertEquals(22.5, item.getCost());
        assertEquals(38.75, item.getPrice());
        assertEquals("Хліб (38.75 грн)", item.toString());
    }

    @Test
    void breadConstructor_shouldAlwaysUseBreadKind() {
        Bread bread = new Bread("Хліб", 500, 22.5, 38.75);

        assertEquals(BakeryItem.BakeryKind.BREAD, bread.getKind());
    }

    @Test
    void bread_shouldCalculateMarginAfterManufacturingCost() {
        Bread bread = new Bread("Хліб", 500, 100, 130);

        assertEquals(25.0, bread.calculateMargin(), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("pastryKinds")
    void fromCsv_shouldCreatePastryForNonBreadKinds(BakeryItem.BakeryKind kind) {
        BakeryItem item = BakeryItem.fromCsv(
                "Виріб;%s;100;10;20".formatted(kind.csvValue()));

        assertInstanceOf(Pastry.class, item);
        assertEquals(kind, item.getKind());
    }

    @Test
    void pastryConstructor_shouldRejectBreadKind() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pastry("Хліб", BakeryItem.BakeryKind.BREAD, 500, 22.5, 38.75));
    }

    @Test
    void pastryConstructor_shouldRejectNullKind() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pastry("Виріб", null, 100, 10, 20));
    }

    @Test
    void pastry_shouldCalculateMarginAfterPackagingCost() {
        Pastry pastry = new Pastry("Печиво", BakeryItem.BakeryKind.COOKIE, 100, 100, 130);

        assertEquals(20.0, pastry.calculateMargin(), 1e-9);
    }

    @ParameterizedTest
    @MethodSource("invalidFieldCounts")
    void fromCsv_shouldRejectIncorrectFieldCount(String row, String messageFragment) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> BakeryItem.fromCsv(row));

        assertTrue(exception.getMessage().contains(messageFragment));
    }

    @ParameterizedTest
    @MethodSource("invalidNumericFields")
    void fromCsv_shouldRejectInvalidNumericValues(String row, String messageFragment) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> BakeryItem.fromCsv(row));

        assertTrue(exception.getMessage().contains(messageFragment));
    }

    @ParameterizedTest
    @MethodSource("rowsViolatingItemInvariants")
    void fromCsv_shouldRejectRowsViolatingItemInvariants(String row, String messageFragment) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> BakeryItem.fromCsv(row));

        assertTrue(exception.getMessage().contains(messageFragment));
    }

    @ParameterizedTest
    @MethodSource("rowsWithEmptyRequiredFields")
    void fromCsv_shouldRejectEmptyRequiredFields(String row, String messageFragment) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> BakeryItem.fromCsv(row));

        assertTrue(exception.getMessage().contains(messageFragment));
    }

    @ParameterizedTest
    @MethodSource("emptyRows")
    void fromCsv_shouldRejectEmptyRow(String row) {
        assertThrows(IllegalArgumentException.class, () -> BakeryItem.fromCsv(row));
    }

    private static Stream<BakeryItem.BakeryKind> pastryKinds() {
        return Stream.of(
                BakeryItem.BakeryKind.PIE,
                BakeryItem.BakeryKind.CONFECTIONERY,
                BakeryItem.BakeryKind.COOKIE,
                BakeryItem.BakeryKind.BAKED_GOOD);
    }

    private static Stream<Arguments> itemsWithDifferentValues() {
        BakeryItem baseline = new Bread("Хліб", 500, 22.5, 38.75);
        return Stream.of(
                Arguments.of(
                        baseline, new Bread("Багет", 500, 22.5, 38.75)),
                Arguments.of(
                        baseline, new Bread("Хліб", 501, 22.5, 38.75)),
                Arguments.of(
                        baseline, new Bread("Хліб", 500, 22.6, 38.75)),
                Arguments.of(
                        baseline, new Bread("Хліб", 500, 22.5, 38.76)),
                Arguments.of(
                        baseline, new Pastry("Хліб", BakeryItem.BakeryKind.COOKIE,
                                500, 22.5, 38.75)),
                Arguments.of(
                        new Pastry("Виріб", BakeryItem.BakeryKind.COOKIE, 100, 10, 20),
                        new Pastry("Виріб", BakeryItem.BakeryKind.PIE, 100, 10, 20))
        );
    }

    private static Stream<Arguments> invalidFieldCounts() {
        return Stream.of(
                Arguments.of("Товар;хлiб;100;10", "вiдсутнє поле"),
                Arguments.of("Товар;хлiб;100;10;20;30", "забагато полiв")
        );
    }

    private static Stream<Arguments> invalidNumericFields() {
        return Stream.of(
                Arguments.of("Товар;хлiб;abc;10;20", "Вага"),
                Arguments.of("Товар;хлiб;2147483648;10;20", "Вага"),
                Arguments.of("Товар;хлiб;100;abc;20", "Собiвартiсть"),
                Arguments.of("Товар;хлiб;100;10;abc", "Цiна"),
                Arguments.of("Товар;хлiб;100;NaN;20", "нечислове значення"),
                Arguments.of("Товар;хлiб;100;10;Infinity", "нечислове значення")
        );
    }

    private static Stream<Arguments> rowsViolatingItemInvariants() {
        return Stream.of(
                Arguments.of(";хлiб;100;10;20", "Назва товару"),
                Arguments.of("   ;хлiб;100;10;20", "Назва товару"),
                Arguments.of("Товар;невідомий;100;10;20", "невiдомий тип товару"),
                Arguments.of("Товар;хлiб;-1;10;20", "Вага"),
                Arguments.of("Товар;хлiб;100;-1;20", "Собiвартiсть"),
                Arguments.of("Товар;хлiб;100;10;-1", "Цiна"),
                Arguments.of("Товар;хлiб;100;Infinity;20", "Собiвартiсть"),
                Arguments.of("Товар;хлiб;100;10;NaN", "Цiна")
        );
    }

    private static Stream<Arguments> rowsWithEmptyRequiredFields() {
        return Stream.of(
                Arguments.of("Товар;;100;10;20", "Тип"),
                Arguments.of("Товар;хлiб;;10;20", "Вага"),
                Arguments.of("Товар;хлiб;100;;20", "Собiвартiсть"),
                Arguments.of("Товар;хлiб;100;10;", "Цiна")
        );
    }

    private static Stream<String> emptyRows() {
        return Stream.of(null, "", "   ");
    }
}
