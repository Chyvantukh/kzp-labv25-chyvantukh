package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CsvTest {

    @TempDir
    Path tempDir;

    @Test
    void exporterAndImporter_shouldRoundTripRecords() throws IOException {
        List<Record> records = List.of(
                new Record("Хліб", "Пшеничний; \"смачний\"", 42.5),
                new Record("Булка", "Звичайна", 12.0));

        Path path = tempDir.resolve("records.csv");
        Csv.Exporter<Record> exporter = new Csv.Exporter<>(Record.class);

        String csv = exporter.toCsv(records);
        exporter.write(path, records);

        assertTrue(csv.contains("\"Пшеничний; \"\"смачний\"\"\""));
        assertEquals(csv, Files.readString(path, StandardCharsets.UTF_8));

        List<Record> actual = new Csv.Importer<>(Record.class, Record::fromMap).read(path);
        assertEquals(records, actual);
    }

    @Test
    void importer_shouldRejectEmptyCsv() throws IOException {
        Path path = tempDir.resolve("empty.csv");
        Files.writeString(path, "", StandardCharsets.UTF_8);

        var importer = new Csv.Importer<>(Record.class, Record::fromMap);
        assertThrows(FileFormatException.class, () -> importer.read(path));
    }

    @Test
    void importer_shouldRejectWrongHeaderAndIncludeExpectedHeader() throws IOException {
        Path path = tempDir.resolve("wrong-header.csv");
        Files.writeString(path, "Назва;Невiдома;Цiна\nХлiб;Пшеничний;42,5",
                StandardCharsets.UTF_8);

        var importer = new Csv.Importer<>(Record.class, Record::fromMap);
        FileFormatException exception = assertThrows(FileFormatException.class, () -> importer.read(path));

        assertTrue(exception.getMessage().contains("Опис;Назва;Цiна"));
    }

    @Test
    void importer_shouldRejectWrongFieldCountDuringImport() throws IOException {
        Path path = tempDir.resolve("wrong-field-count.csv");
        Files.writeString(path,
                "Опис;Назва;Цiна\r\nПшеничний;Хлiб",
                StandardCharsets.UTF_8);

        var importer = new Csv.Importer<>(Record.class, Record::fromMap);
        FileFormatException exception = assertThrows(FileFormatException.class, () -> importer.read(path));

        assertTrue(exception.getMessage().contains("очікувалося 3 полів"));
    }

    @Test
    void importer_shouldRejectUnclosedQuotedField() throws IOException {
        Path path = tempDir.resolve("unclosed.csv");
        Files.writeString(path, "Опис;Назва;Цiна\r\n\"Пшеничний;Хлiб;42,5",
                StandardCharsets.UTF_8);

        var importer = new Csv.Importer<>(Record.class, Record::fromMap);
        assertThrows(FileFormatException.class, () -> importer.read(path));
    }

    @Test
    void importer_shouldWrapFileSystemErrorsAndKeepCause() throws IOException {
        Path path = tempDir.resolve("missing.csv");
        var importer = new Csv.Importer<>(Record.class, Record::fromMap);

        FileAccessException exception = assertThrows(FileAccessException.class, () -> importer.read(path));
        assertNotNull(exception.getCause());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void exporter_shouldEscapeCommasAndNewLines() {
        Csv.Exporter<Record> exporter = new Csv.Exporter<>(Record.class);
        String csv = exporter.toCsv(List.of(new Record("Хліб", "Перший, другий\nтретiй", 10.0)));

        assertTrue(csv.contains("\"Перший, другий\nтретiй\""));
        assertTrue(csv.contains("Опис;Назва;Цiна"));
    }

    @ParameterizedTest
    @MethodSource("invalidImportValues")
    void importer_shouldRejectInvalidNumericValuesAndUnknownKinds(String csvValue, String expectedFragment)
            throws IOException {
        Path path = tempDir.resolve("invalid.csv");
        Files.writeString(path,
                "Час випiкання;Собiвартiсть;Тип;Назва;Цiна;Вага\r\n%s".formatted(csvValue),
                StandardCharsets.UTF_8);

        var importer = new Csv.Importer<>(BakeryItem.class, BakeryItem::fromCsvColumns);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> importer.read(path));

        assertTrue(exception.getMessage().contains(expectedFragment));
    }

    @Test
    void importer_shouldRejectDuplicateHeaders() {
        assertThrows(IllegalArgumentException.class,
                () -> new Csv.Importer<>(DuplicateHeaderRecord.class, DuplicateHeaderRecord::fromMap));
        assertThrows(IllegalArgumentException.class,
                () -> new Csv.Exporter<>(DuplicateHeaderRecord.class));
    }

    @Test
    void importer_shouldRejectTypeWithoutAnnotatedFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new Csv.Importer<>(NoColumnsRecord.class, row -> null));
        assertThrows(IllegalArgumentException.class,
                () -> new Csv.Exporter<>(NoColumnsRecord.class));
    }

    private static Stream<Arguments> invalidImportValues() {
        return Stream.of(
                Arguments.of("12,5;22,5;хлiб;Хліб;не число;500", "Числове CSV-поле має некоректне значення"),
                Arguments.of("12,5;22,5;невідомий;Хліб;38,75;500", "невiдомий тип товару"));
    }

    private record Record(
            @Csv.CsvColumn("Назва") String name,
            @Csv.CsvColumn("Опис") String description,
            @Csv.CsvColumn("Цiна") double price) {

        static Record fromMap(Map<String, String> values) {
            return new Record(
                    values.get("Назва"),
                    values.get("Опис"),
                    Double.parseDouble(values.get("Цiна").replace(',', '.')));
        }
    }

    private record DuplicateHeaderRecord(
            @Csv.CsvColumn("Назва") String firstValue,
            @Csv.CsvColumn("Назва") String secondValue) {

        static DuplicateHeaderRecord fromMap(Map<String, String> values) {
            return new DuplicateHeaderRecord(values.get("Назва"), values.get("Назва"));
        }
    }

    private record NoColumnsRecord() {
    }
}
