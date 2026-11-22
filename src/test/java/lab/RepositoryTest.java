package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositoryTest {
    @TempDir
    Path tempDirectory;

    @Test
    void repository_shouldPreserveInsertionOrder() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();

        repository.add("first");
        repository.add("second");
        repository.add("third");

        assertEquals(List.of("first", "second", "third"), repository.all());
    }

    @Test
    void repository_shouldReturnMatchingRowsInInsertionOrder() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();
        repository.add("alpha");
        repository.add("beta");
        repository.add("gamma");
        repository.add("beta-two");

        assertEquals(List.of("beta", "beta-two"), repository.find(value -> value.startsWith("beta")));
        assertEquals(List.of(), repository.find(value -> false));
    }

    @Test
    void repository_shouldRejectNulls() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();

        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
    }

    @Test
    void repository_shouldReturnImmutableSnapshots() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();
        repository.add("first");

        List<String> snapshot = repository.all();
        List<String> matches = repository.find(value -> true);

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("second"));
        assertThrows(UnsupportedOperationException.class, () -> matches.add("third"));
    }

    @Test
    void repository_snapshot_shouldStayStableAfterAdd() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();
        repository.add("first");

        List<String> snapshot = repository.all();
        repository.add("second");

        assertEquals(List.of("first"), snapshot);
        assertEquals(List.of("first", "second"), repository.all());
    }

    @Test
    void commaCsv_shouldRoundTripRepositoryRecordsInInsertionOrder() throws Exception {
        Path input = tempDirectory.resolve("input.csv");
        Path exported = tempDirectory.resolve("exported.csv");
        Files.writeString(input,
                "\"Хліб, \"\"особливий\"\"\nпшеничний\",хлiб,500,22.50,38.75,35\r\n"
                        + "Печиво,печиво,200,30.00,58.00,18.5\r\n",
                StandardCharsets.UTF_8);

        Csv.Importer<BakeryItem> importer =
                new Csv.Importer<>(BakeryItem.class, ',', BakeryItem::fromCsvColumns);
        InMemoryRepository<BakeryItem> repository = new InMemoryRepository<>();
        importer.readHeaderless(input, (rowNumber, exception) -> {
            throw exception;
        }).forEach(repository::add);

        assertEquals(
                List.of("Хліб, \"особливий\"\nпшеничний", "Печиво"),
                repository.all().stream().map(BakeryItem::getName).toList());

        Csv.Exporter<BakeryItem> exporter = new Csv.Exporter<>(BakeryItem.class);
        exporter.write(exported, repository);

        String exportedCsv = Files.readString(exported, StandardCharsets.UTF_8);
        assertEquals(
                "Час випiкання,Собiвартiсть,Вид,Назва,Цiна,Вага\r\n"
                        + "35.0,22.5,хлiб,\"Хліб, \"\"особливий\"\"\nпшеничний\",38.75,500\r\n"
                        + "18.5,30.0,печиво,Печиво,58.0,200\r\n",
                exportedCsv);

        List<BakeryItem> reimported = importer.read(exported);
        assertEquals(repository.all(), reimported);
    }

    @Test
    void commaCsv_shouldRejectWrongHeader() throws Exception {
        Path input = tempDirectory.resolve("wrong-header.csv");
        Files.writeString(input,
                "Час випiкання,Собiвартiсть,Вид,Найменування,Цiна,Вага\r\n",
                StandardCharsets.UTF_8);

        Csv.Importer<BakeryItem> importer =
                new Csv.Importer<>(BakeryItem.class, ',', BakeryItem::fromCsvColumns);

        assertThrows(FileFormatException.class, () -> importer.read(input));
    }

    @Test
    void commaCsv_shouldReportInvalidEnumNumbersAndFieldCounts() throws Exception {
        Path input = tempDirectory.resolve("invalid-rows.csv");
        Files.writeString(input,
                "Час випiкання,Собiвартiсть,Вид,Назва,Цiна,Вага\r\n"
                        + "5,10.5,хлiб,Хліб,20.25,100\r\n"
                        + "5,10.5,невідомий,Товар,20.25,100\r\n"
                        + "5,10.5,хлiб,Некоректна вага,20.25,вага\r\n"
                        + "5,10.5,хлiб,Мало полів,20.25\r\n",
                StandardCharsets.UTF_8);

        Csv.Importer<BakeryItem> importer =
                new Csv.Importer<>(BakeryItem.class, ',', BakeryItem::fromCsvColumns);
        List<String> errors = new ArrayList<>();

        List<BakeryItem> records = importer.read(input,
                (recordNumber, exception) -> errors.add("%d:%s".formatted(recordNumber, exception.getMessage())));

        assertEquals(1, records.size());
        assertEquals(BakeryItem.BakeryKind.BREAD, records.getFirst().getKind());
        assertEquals(100, records.getFirst().getWeightG());
        assertEquals(10.5, records.getFirst().getCost());
        assertEquals(20.25, records.getFirst().getPrice());
        assertEquals(5.0, records.getFirst().getBakingTimeMinutes());
        assertEquals(List.of(
                "2:невiдомий тип товару: невідомий",
                "3:Числове CSV-поле має некоректне значення",
                "4:очікувалося 6 полів, знайдено 5"), errors);
    }
}
