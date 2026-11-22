package lab;

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

    /**
     * Створює текст довідки з версією проєкту без номера збірки.
     *
     * @return довідка з параметрами запуску та прикладом команди
     */
    private static String helpMessage() {
        String version = BuildInfo.version();
        int buildSeparator = version.indexOf('+');
        String mavenVersion = buildSeparator < 0 ? version : version.substring(0, buildSeparator);
        return String.join("%n", List.of(
                "Програма обробляє дані про товари з CSV-файлу.",
                "",
                "Обов'язкові параметри:",
                "    --input <шлях>   шлях до вхідного CSV-файлу",
                "    --output <шлях>  шлях до файлу звіту",
                "",
                "Додаткові параметри:",
                "    --help           показати цю довідку",
                "    --version        показати версію програми",
                "",
                "Приклад запуску:",
                "    java -jar target/kzp-labv25-chyvantukh-%s-all.jar \\",
                "            --input data/input.csv --output data/report.txt"))
                .formatted(mavenVersion);
    }

    /**
     * Забороняє створення екземплярів контролера, який містить лише статичні операції.
     */
    private MainController() {
    }

    /**
     * Обробляє аргументи командного рядка, CSV-файл і записує підсумковий звіт.
     *
     * @param args параметри командного рядка; підтримуються {@code --input},
     *             {@code --output}, {@code --help} і {@code --version}
     * @return {@code 0} у разі успіху, {@code 1} у разі помилки аргументів або читання,
     *         {@code 2} у разі помилки запису звіту
     */
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

        Repository<BakeryItem> productRepository = new InMemoryRepository<>();
        List<String> errorLogs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        Csv.Importer<BakeryItem> importer =
                new Csv.Importer<>(BakeryItem.class, ',', BakeryItem::fromCsvColumns);
        try {
            importer.readHeaderless(file, (recordNumber, exception) -> errorLogs.add(
                    "Рядок %d: %s".formatted(recordNumber + 1, exception.getMessage())))
                    .forEach(productRepository::add);
        } catch (FileAccessException exception) {
            if (exception.getCause() instanceof NoSuchFileException) {
                System.err.printf("Файл не знайдено за шляхом: %s%n", file.toAbsolutePath());
            } else {
                System.err.printf("Помилка читання файлу: %s%n", exception.getMessage());
            }
            return EXIT_INPUT_ERROR;
        } catch (FileFormatException exception) {
            System.err.printf("Помилка читання файлу: %s%n", exception.getMessage());
            return EXIT_INPUT_ERROR;
        }

        List<BakeryItem> validProducts = productRepository.all();
        sb.append(String.format(Locale.ROOT, "=== УСПiШНО ЗАВАНТАЖЕНi ТОВАРИ (%d) ===%n",
                validProducts.size()));
        if (validProducts.isEmpty()) {
            sb.append("Не знайдено жодного коректного товару.")
                .append(System.lineSeparator());
        } else {
            for (BakeryItem item : validProducts) {
                sb.append(String.format(Locale.ROOT,
                        "Назва = %-15s | Тип = %-10s | Вага = %4dг | Собiвартiсть = %6.2f | Цiна = %6.2f | Час випiкання = %6.2f хв%n",
                        item.getName(), item.getKind().csvValue(), item.getWeightG(),
                        item.getCost(), item.getPrice(), item.getBakingTimeMinutes()));
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

    /**
     * Перевіряє пари параметрів і створює шляхи до вхідного та вихідного файлів.
     *
     * @param args аргументи командного рядка
     * @return шляхи до файлів або {@code null}, якщо параметри некоректні
     */
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

    /**
     * Потоково підсумовує вагу та обчислює середню маржу товарів.
     *
     * @param products коректні товари
     * @return загальна вага та середня маржа; для порожнього списку обидва значення дорівнюють нулю
     */
    private static WeightMargin calculateWeightMargin(List<BakeryItem> products) {
        double totalWeight = products.stream()
                .mapToDouble(BakeryItem::getWeightG)
                .reduce(0.0, Double::sum);
        double totalMargin = products.stream()
                .mapToDouble(BakeryItem::calculateMargin)
                .reduce(0.0, Double::sum);
        double averageMargin = products.isEmpty() ? 0.0 : totalMargin / products.size();
        return new WeightMargin(totalWeight, averageMargin);
    }

    /**
     * Потоково знаходить коректний товар із найбільшою ціною.
     *
     * @param products коректні товари
     * @return перший товар із найбільшою ціною або {@code null}, якщо список порожній
     */
    private static BakeryItem findMostExpensiveProduct(List<BakeryItem> products) {
        return products.stream()
                .reduce((mostExpensive, candidate) ->
                        candidate.getPrice() > mostExpensive.getPrice() ? candidate : mostExpensive)
                .orElse(null);
    }

    /**
     * Експортує поточні записи продуктів із репозиторію у CSV-файл.
     *
     * @param path шлях до CSV-файлу
     * @param repository репозиторій коректних продуктів
     * @throws FileAccessException якщо не вдалося записати файл
     */
    public static void exportProducts(Path path, Repository<BakeryItem> repository) {
        new Csv.Exporter<BakeryItem>(BakeryItem.class).write(path, repository);
    }

    /**
     * Зберігає агреговані показники звіту.
     *
     * @param totalWeight загальна вага товарів у грамах
     * @param averageMargin середня маржа у гривнях
     */
    private record WeightMargin(double totalWeight, double averageMargin) {
    }

    /**
     * Зберігає шляхи до вхідного CSV-файлу та вихідного звіту.
     *
     * @param inputPath шлях до вхідного файлу
     * @param outputPath шлях до файлу звіту
     */
    private record CommandLineArguments(Path inputPath, Path outputPath) {
    }
}