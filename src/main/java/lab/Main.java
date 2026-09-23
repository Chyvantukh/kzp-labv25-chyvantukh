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
 * Консольна програма для обробки даних про товари, збережених у CSV-файлі.
 *
 * <p>Програма читає записи з CSV-файлу, шлях до якого передається через {@code --input},
 * перевіряє їхню коректність,
 * відкидає некоректні рядки, обчислює агреговані показники та створює звіт у
 * стандартному виводі та у файлі, переданому через {@code --output}.</p>
 */
public class Main {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_INPUT_ERROR = 1;
    private static final int EXIT_OUTPUT_ERROR = 2;
        private static final String VERSION = "1.0.0";
        private static final String HELP_MESSAGE = """
                        Програма обробляє дані про товари з CSV-файлу.

                        Обов'язкові параметри:
                            --input <шлях>   шлях до вхідного CSV-файлу
                            --output <шлях>  шлях до файлу звіту

                        Додаткові параметри:
                            --help           показати цю довідку
                            --version        показати версію програми

                        Приклад запуску:
                            java -jar target/kzp-labv25-chyvantukh-1.0.0.jar \\
                                    --input data/input.csv --output data/report.txt
                        """;
    private static final String[] FIELD_NAMES = {"Назва", "Тип", "Вага", "Собiвартiсть", "Цiна"};

    /**
     * Запускає програму та завершує JVM із кодом, який повертає обробник аргументів.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        System.exit(run(args));
    }

    /**
     * Обробляє аргументи, вхідний файл і формування звіту.
     *
      * @param args аргументи командного рядка; {@code --input} і {@code --output}
      *             задають шляхи файлів, {@code --help} виводить довідку,
      *             {@code --version} виводить версію програми
      * @return код завершення: {@code 0} для успішного виконання, {@code 1} для помилки читання
      *         або {@code 2} для помилки запису
     */
    static int run(String[] args) {
        if (args.length == 1 && "--help".equals(args[0])) {
            System.out.print(HELP_MESSAGE);
            return EXIT_SUCCESS;
        }

        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println(VERSION);
            return EXIT_SUCCESS;
        }

        CommandLineArguments commandLineArguments = parseArguments(args);
        if (commandLineArguments == null) {
            return EXIT_INPUT_ERROR;
        }

        Path file = commandLineArguments.inputPath();

        List<Product> validProducts = new ArrayList<>();
        List<String> errorLogs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                Product product = parseLine(line, ++lineNumber, errorLogs);
                if (product != null) {
                    validProducts.add(product);
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
            for (Product product : validProducts) {
                sb.append(String.format(Locale.ROOT,
                        "Назва = %-15s | Тип = %-10s | Вага = %4dг | Собiвартiсть = %6.2f | Цiна = %6.2f%n",
                        product.name(), product.type(), product.weightG(), product.cost(), product.price()));
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

        sb.append("=== Предметний обрахунок ===").append(System.lineSeparator());
        sb.append(String.format(Locale.ROOT, "Загальна вага: %.0f г%n", calculateTotalWeight(validProducts)));
        sb.append(String.format(Locale.ROOT, "Середнiй маржинальний прибуток: %.2f грн%n", calculateAverageMargin(validProducts)));
        sb.append("Найдорожчий товар: " + findMostExpensiveProduct(validProducts));

        Path report = commandLineArguments.outputPath();
        try {
            Files.writeString(report, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.printf("Помилка запису звіту: %s%n", ex.getMessage());
            return EXIT_OUTPUT_ERROR;
        }

        String finalReport = sb.toString();
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

    /**
     * Розбирає один рядок CSV-файлу та перетворює його на об'єкт товару.
     *
     * @param line рядок даних для аналізу
     * @param lineNumber номер рядка у файлі
     * @param errorLogs список помилок, які треба зафіксувати при валідації
     * @return об'єкт товару, якщо рядок коректний; інакше {@code null}
     */
    private static Product parseLine(String line, int lineNumber, List<String> errorLogs) {
        if (line.trim().isEmpty()) {
            errorLogs.add(String.format(Locale.ROOT, "Рядок %d: порожній рядок", lineNumber));
            return null;
        }

        String[] fields = line.split(";", -1);
        if (!hasAllFields(fields, lineNumber, errorLogs)) {
            return null;
        }

        String name = fields[0].trim();
        String type = fields[1].trim();
        int weightG;
        double cost;
        double price;
        String currentField = "";

        try {
            currentField = "Вага";
            weightG = Integer.parseInt(fields[2].trim());
            if (weightG < 0) {
                throw new IllegalArgumentException();
            }

            currentField = "Собiвартiсть";
            cost = Double.parseDouble(fields[3].trim().replace(',', '.'));
            if (!Double.isFinite(cost)) {
                throw new NumberFormatException();
            }
            if (cost < 0) {
                throw new IllegalArgumentException();
            }

            currentField = "Цiна";
            price = Double.parseDouble(fields[4].trim().replace(',', '.'));
            if (!Double.isFinite(price)) {
                throw new NumberFormatException();
            }
            if (price < 0) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            errorLogs.add(String.format(Locale.ROOT, "Рядок %d: поле \"%s\" має нечислове значення",
                    lineNumber, currentField));
            return null;
        } catch (IllegalArgumentException e) {
            errorLogs.add(String.format(Locale.ROOT, "Рядок %d: поле \"%s\" не може бути вiд'ємним",
                    lineNumber, currentField));
            return null;
        }

        return new Product(name, type, weightG, cost, price);
    }

    /**
     * Перевіряє, чи рядок має всі необхідні поля і чи не містить зайвих значень.
     *
      * @param fields масив полів, отриманий після поділу рядка за крапкою з комою
     * @param lineNumber номер рядка у файлі
     * @param errorLogs список логів помилок
     * @return {@code true}, якщо всі поля присутні й немає зайвих значень; інакше {@code false}
     */
    private static boolean hasAllFields(String[] fields, int lineNumber, List<String> errorLogs) {
        boolean hasMissingField = false;

        for (int fieldIndex = 0; fieldIndex < FIELD_NAMES.length; fieldIndex++) {
            if (fieldIndex >= fields.length || fields[fieldIndex].trim().isEmpty()) {
                errorLogs.add(String.format(Locale.ROOT, "Рядок %d: вiдсутнє поле \"%s\"",
                        lineNumber, FIELD_NAMES[fieldIndex]));
                hasMissingField = true;
            }
        }

        if (fields.length > FIELD_NAMES.length) {
            errorLogs.add(String.format(Locale.ROOT,
                    "Рядок %d: забагато полiв (очiкувалося %d, знайдено %d)",
                    lineNumber, FIELD_NAMES.length, fields.length));
            return false;
        }

        return !hasMissingField;
    }

    /**
     * Підраховує загальну вагу всіх коректних товарів.
     *
     * @param products список товарів
     * @return сумарна вага у грамах
     */
    private static double calculateTotalWeight(List<Product> products) {
        double total = 0;
        for (Product product : products) {
            total += product.weightG();
        }
        return total;
    }

    /**
     * Обчислює середній маржинальний прибуток для списку товарів.
     *
     * @param products список товарів
     * @return середня різниця між ціною та собівартістю або {@code 0.0}, якщо список порожній
     */
    private static double calculateAverageMargin(List<Product> products) {
        if (products.isEmpty()) {
            return 0.0;
        }

        double totalMargin = 0;
        for (Product product : products) {
            totalMargin += (product.price() - product.cost());
        }

        return totalMargin / products.size();
    }

    /**
     * Знаходить товар з найбільшою ціною серед коректних записів.
     *
     * @param products список товарів
     * @return товар з максимальною ціною або {@code null}, якщо список порожній
     */
    private static Product findMostExpensiveProduct(List<Product> products) {
        if (products.isEmpty()) {
            return null;
        }

        Product maxProduct = products.get(0);
        for (Product product : products) {
            if (product.price() > maxProduct.price()) {
                maxProduct = product;
            }
        }
        return maxProduct;
    }

    private record Product(String name, String type, int weightG, double cost, double price) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s (%.2f грн)", name, price);
        }
    }

    private record CommandLineArguments(Path inputPath, Path outputPath) {
    }
}