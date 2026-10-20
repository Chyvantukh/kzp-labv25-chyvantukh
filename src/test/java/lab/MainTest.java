package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void run_shouldPrintVersion() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, MainController.run(new String[]{"--version"}));
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(BuildInfo.version() + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_shouldPrintHelp() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, MainController.run(new String[]{"--help"}));
        } finally {
            System.setOut(originalOut);
        }

        String help = output.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("--help"));
        assertTrue(help.contains("--version"));
        assertTrue(help.contains("--input"));
        assertTrue(help.contains("--output"));
        assertTrue(help.contains("Приклад запуску:"));
        String version = BuildInfo.version();
        int buildSeparator = version.indexOf('+');
        String mavenVersion = buildSeparator < 0 ? version : version.substring(0, buildSeparator);
        assertTrue(help.contains("java -jar target/kzp-labv25-chyvantukh-%s-all.jar"
                .formatted(mavenVersion)));
        assertFalse(help.contains("kzp-labv25-chyvantukh-%s-all.jar".formatted(version)));
    }

    @Test
    void run_shouldRequireInputAndOutputArguments() {
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(errorOutput, true, StandardCharsets.UTF_8));
            assertEquals(1, MainController.run(new String[0]));
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                .contains("необхідно вказати --input і --output"));
    }

    @Test
    void run_shouldReportMissingInputFile() {
        Path missingInput = temporaryDirectory.resolve("missing.csv");
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(errorOutput, true, StandardCharsets.UTF_8));
            assertEquals(1, MainController.run(new String[]{
                    "--input", missingInput.toString(),
                    "--output", temporaryDirectory.resolve("report.txt").toString()
            }));
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                .contains("Файл не знайдено за шляхом:"));
    }

    @Test
    void run_shouldContinueAfterInvalidRowsAndWriteUtf8Report() throws Exception {
        Path input = temporaryDirectory.resolve("products.csv");
        Path report = temporaryDirectory.resolve("nested").resolve("report.txt");
        Files.writeString(input, """
                Хліб;хлiб;100;10,50;12.00
                Товар;невідомий;200;20;30
                Печиво;печиво;50;5;15
                Збитковий;випiчка;20;20;10
                """, StandardCharsets.UTF_8);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, MainController.run(new String[]{
                    "--input", input.toString(), "--output", report.toString()
            }));
        } finally {
            System.setOut(originalOut);
        }

        String reportContents = Files.readString(report, StandardCharsets.UTF_8);
        assertEquals(reportContents + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
        assertTrue(reportContents.contains("=== УСПiШНО ЗАВАНТАЖЕНi ТОВАРИ (3) ==="));
        assertTrue(reportContents.contains("Рядок 2: невiдомий тип товару: невідомий"));
        assertTrue(reportContents.contains("Загальна вага: 170 г"));
        assertTrue(reportContents.contains("Середнiй маржинальний прибуток: -0.51 грн"));
        assertTrue(reportContents.contains("Найдорожчий товар: Печиво (15.00 грн)"));
    }

    @Test
    void run_shouldReportNoMostExpensiveItemWhenAllRowsAreInvalid() throws Exception {
        Path input = temporaryDirectory.resolve("invalid.csv");
        Path report = temporaryDirectory.resolve("report.txt");
        Files.writeString(input, "Товар;хлiб;abc;10;20\n", StandardCharsets.UTF_8);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            assertEquals(0, MainController.run(new String[]{
                    "--input", input.toString(), "--output", report.toString()
            }));
        } finally {
            System.setOut(originalOut);
        }

        String reportContents = Files.readString(report, StandardCharsets.UTF_8);
        assertTrue(reportContents.contains("Найдорожчий товар: відсутній"));
        assertTrue(reportContents.contains("Загальна вага: 0 г"));
        assertTrue(reportContents.contains("Середнiй маржинальний прибуток: 0.00 грн"));
        assertEquals(reportContents + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_shouldReturnOutputErrorWhenReportParentIsAFile() throws Exception {
        Path input = temporaryDirectory.resolve("products.csv");
        Path outputParent = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(input, "Товар;хлiб;100;10;20\n", StandardCharsets.UTF_8);
        Files.writeString(outputParent, "file", StandardCharsets.UTF_8);

        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(errorOutput, true, StandardCharsets.UTF_8));
            assertEquals(2, MainController.run(new String[]{
                    "--input", input.toString(),
                    "--output", outputParent.resolve("report.txt").toString()
            }));
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8).contains("Помилка запису звіту:"));
    }
}
