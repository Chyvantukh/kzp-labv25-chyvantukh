package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BakeryItemTest {

    @Test
    void fromCsv_shouldCreateItemFromValidRow() {
        BakeryItem item = BakeryItem.fromCsv("  Хліб ; Пшеничний ; 500 ; 22,50 ; 38.75 ");

        assertEquals("Хліб", item.getName());
        assertEquals("Пшеничний", item.getType());
        assertEquals(500, item.getWeightG());
        assertEquals(22.5, item.getCost());
        assertEquals(38.75, item.getPrice());
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

    private static Stream<Arguments> invalidFieldCounts() {
        return Stream.of(
                Arguments.of("Товар;Тип;100;10", "вiдсутнє поле"),
                Arguments.of("Товар;Тип;100;10;20;30", "забагато полiв")
        );
    }

    private static Stream<Arguments> invalidNumericFields() {
        return Stream.of(
                Arguments.of("Товар;Тип;abc;10;20", "Вага"),
                Arguments.of("Товар;Тип;2147483648;10;20", "Вага"),
                Arguments.of("Товар;Тип;100;abc;20", "Собiвартiсть"),
                Arguments.of("Товар;Тип;100;10;abc", "Цiна")
        );
    }

    private static Stream<Arguments> rowsViolatingItemInvariants() {
        return Stream.of(
                Arguments.of(";Тип;100;10;20", "Назва"),
                Arguments.of("   ;Тип;100;10;20", "Назва"),
                Arguments.of("Товар; ;100;10;20", "типу товару"),
                Arguments.of("Товар;Тип;-1;10;20", "Вага"),
                Arguments.of("Товар;Тип;100;-1;20", "Собiвартiсть"),
                Arguments.of("Товар;Тип;100;10;-1", "Цiна"),
                Arguments.of("Товар;Тип;100;NaN;20", "Собiвартiсть"),
                Arguments.of("Товар;Тип;100;10;Infinity", "Цiна")
        );
    }

    private static Stream<Arguments> rowsWithEmptyRequiredFields() {
        return Stream.of(
                Arguments.of("Товар;;100;10;20", "типу товару"),
                Arguments.of("Товар;Тип;;10;20", "Вага"),
                Arguments.of("Товар;Тип;100;;20", "Собiвартiсть"),
                Arguments.of("Товар;Тип;100;10;", "Цiна")
        );
    }

    private static Stream<String> emptyRows() {
        return Stream.of(null, "", "   ");
    }
}
