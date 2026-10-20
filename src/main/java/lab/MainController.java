package lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Контролер обробки аргументів, CSV-даних і формування звіту.
 */
public final class MainController {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_INPUT_ERROR = 1;
    private static final int EXIT_OUTPUT_ERROR = 2;

    private static String helpMessage() {
        String version = BuildInfo.version();
        int buildSeparator = version.indexOf('+');
        String mavenVersion = buildSeparator < 0 ? version : version.substring(0, buildSeparator);
        return """
            Програма обробляє дані про товари з CSV-файлу.

            Обов'язкові параметри:
                --input <шлях>   шлях до вхідного CSV-файлу
                --output <шлях>  шлях до файлу звіту

            Додаткові параметри:
                --help           показати цю довідку
                --version        показати версію програми

            Приклад запуску:
                java -jar target/kzp-labv25-chyvantukh-%s-all.jar \\
                        --input data/input.csv --output data/report.txt
            """.formatted(mavenVersion);
    }

    private MainController() {
    }

    static int run(String[] args) {
        if (args.length == 1 && "--help".equals(args[0])) {
            System.out.print(helpMessage());
            return EXIT_SUCCESS;
        }

        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println(BuildInfo.version());
            return EXIT_SUCCESS;
        }

        CommandLineArguments commandLineArguments = parseArguments(args);
        if (commandLineArguments == null) {
            return EXIT_INPUT_ERROR;
        }

        Path file = commandLineArguments.inputPath();

        List<BakeryItem> validProducts = new ArrayList<>();
        List<String> errorLogs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                BakeryItem item = parseLine(line, ++lineNumber, errorLogs);
                if (item != null) {
                    validProducts.add(item);
                }
            }
        } catch (NoSuchFileException e) {
            System.err.printf("Файл не знайдено за шляхом: %s%n", file.toAbsolutePath());
            return EXIT_INPUT_ERROR;
        } catch (IOException e) {
            System.err.printf("Помилка читання файлу: %s%n", e.getMessage());
            return EXIT_INPUT_ERROR;
        }

        sb.append(String.format(Locale.ROOT, "=== УСПiШНО ЗАВАНТАЖЕНi ТОВАРИ (%d) ===%n",
                validProducts.size()));
        if (validProducts.isEmpty()) {
            sb.append("Не знайдено жодного коректного товару.")
                .append(System.lineSeparator());
        } else {
            for (BakeryItem item : validProducts) {
                sb.append(String.format(Locale.ROOT,
                        "Назва = %-15s | Тип = %-10s | Вага = %4dг | Собiвартiсть = %6.2f | Цiна = %6.2f%n",
                        item.getName(), item.getKind().csvValue(), item.getWeightG(),
                        item.getCost(), item.getPrice()));
            }
        }

        sb.append(System.lineSeparator());

        sb.append(String.format(Locale.ROOT, "=== ПОМИЛКОВi ТОВАРИ (%d) ===%n", errorLogs.size()));
        if (errorLogs.isEmpty()) {
            sb.append("Помилок пiд час обробки не виявлено.")
                    .append(System.lineSeparator());
        } else {
            for (String error : errorLogs) {
                sb.append(error).append(System.lineSeparator());
            }
        }

        sb.append(System.lineSeparator());

        BakeryItem mostExpensiveProduct = findMostExpensiveProduct(validProducts);
        sb.append("=== Предметний обрахунок ===").append(System.lineSeparator());
        WeightMargin weightMargin = calculateWeightMargin(validProducts);
        sb.append(String.format(Locale.ROOT, "Загальна вага: %.0f г%n", weightMargin.totalWeight()));
        sb.append(String.format(Locale.ROOT, "Середнiй маржинальний прибуток: %.2f грн%n",
                weightMargin.averageMargin()));
        sb.append("Найдорожчий товар: ");
        if (mostExpensiveProduct == null) {
            sb.append("відсутній");
        } else {
            sb.append(mostExpensiveProduct);
        }

        String finalReport = sb.toString();
        Path report = commandLineArguments.outputPath();
        try {
            Path outputDirectory = report.getParent();
            if (outputDirectory != null) {
                Files.createDirectories(outputDirectory);
            }
            Files.writeString(report, finalReport, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.printf("Помилка запису звіту: %s%n", ex.getMessage());
            return EXIT_OUTPUT_ERROR;
        }

        System.out.println(finalReport);
        return EXIT_SUCCESS;
    }

    private static CommandLineArguments parseArguments(String[] args) {
        Path inputPath = null;
        Path outputPath = null;

        for (int index = 0; index < args.length; index += 2) {
            if (index + 1 >= args.length) {
                System.err.println("Помилка: параметри --input і --output повинні мати шлях.");
                return null;
            }

            String option = args[index];
            String pathValue = args[index + 1];
            try {
                if ("--input".equals(option) && inputPath == null) {
                    inputPath = Path.of(pathValue);
                } else if ("--output".equals(option) && outputPath == null) {
                    outputPath = Path.of(pathValue);
                } else {
                    System.err.printf("Помилка: невідомий або повторений параметр %s%n", option);
                    return null;
                }
            } catch (InvalidPathException exception) {
                System.err.printf("Помилка: некоректний шлях %s%n", pathValue);
                return null;
            }
        }

        if (inputPath == null || outputPath == null) {
            System.err.println("Помилка: необхідно вказати --input і --output.");
            return null;
        }

        return new CommandLineArguments(inputPath, outputPath);
    }

    private static BakeryItem parseLine(String line, int lineNumber, List<String> errorLogs) {
        try {
            return BakeryItem.fromCsv(line);
        } catch (IllegalArgumentException exception) {
            errorLogs.add("Рядок %d: %s".formatted(lineNumber, exception.getMessage()));
            return null;
        }
    }

    private static WeightMargin calculateWeightMargin(List<BakeryItem> products) {
        double totalWeight = 0;
        double totalMargin = 0;
        for (BakeryItem item : products) {
            totalWeight += item.getWeightG();
            totalMargin += item.calculateMargin();
        }

        double averageMargin = products.isEmpty() ? 0.0 : totalMargin / products.size();
        return new WeightMargin(totalWeight, averageMargin);
    }

    private static BakeryItem findMostExpensiveProduct(List<BakeryItem> products) {
        if (products.isEmpty()) {
            return null;
        }

        BakeryItem maxProduct = products.get(0);
        for (BakeryItem item : products) {
            if (item.getPrice() > maxProduct.getPrice()) {
                maxProduct = item;
            }
        }
        return maxProduct;
    }

    private record WeightMargin(double totalWeight, double averageMargin) {
    }

    private record CommandLineArguments(Path inputPath, Path outputPath) {
    }
}