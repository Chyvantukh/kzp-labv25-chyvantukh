package lab;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainIntegrationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void main_shouldProcessInputAndGenerateCompleteReport() throws Exception {
        Path input = Path.of("data", "input.csv");
        Path report = temporaryDirectory.resolve("report.txt");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            assertTrue(Files.exists(input));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, MainController.run(new String[]{
                    "--input", input.toString(), "--output", report.toString()
            }));

            String consoleOutput = output.toString(StandardCharsets.UTF_8);
            String reportOutput = Files.readString(report, StandardCharsets.UTF_8);

            assertAll(
                    () -> assertContainsAll(consoleOutput,
                            "=== УСПiШНО ЗАВАНТАЖЕНi ТОВАРИ (2) ===",
                            "Хлiб пшеничний",
                            "Тип = хлiб",
                            "Печиво",
                            "Тип = печиво",
                            "=== ПОМИЛКОВi ТОВАРИ (4) ===",
                            "Рядок 2",
                            "Рядок 3",
                            "Рядок 4",
                            "Рядок 6",
                            "Загальна вага: 700 г",
                            "Середнiй маржинальний прибуток: 19.95 грн",
                            "Найдорожчий товар: Печиво (58.00 грн)"),
                    () -> assertContainsAll(reportOutput,
                            "=== УСПiШНО ЗАВАНТАЖЕНi ТОВАРИ (2) ===",
                            "Хлiб пшеничний",
                            "Тип = хлiб",
                            "Печиво",
                            "Тип = печиво",
                            "=== ПОМИЛКОВi ТОВАРИ (4) ===",
                            "Рядок 2",
                            "Рядок 3",
                            "Рядок 4",
                            "Рядок 6",
                            "Загальна вага: 700 г",
                            "Середнiй маржинальний прибуток: 19.95 грн",
                            "Найдорожчий товар: Печиво (58.00 грн)"),
                    () -> assertEquals("%s%s".formatted(reportOutput, System.lineSeparator()), consoleOutput)
            );
        } finally {
            System.setOut(originalOut);
        }
    }

    private static void assertContainsAll(String actual, String... expectedFragments) {
        for (String expectedFragment : expectedFragments) {
            assertTrue(actual.contains(expectedFragment),
                    () -> "Expected output to contain: %s".formatted(expectedFragment));
        }
    }
}