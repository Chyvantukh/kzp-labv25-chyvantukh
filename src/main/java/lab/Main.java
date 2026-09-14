package lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    private static final String[] FIELD_NAMES = {"Назва", "Тип", "Вага", "Собiвартiсть", "Цiна"};

    public static void main(String[] args) {
        Path file = Path.of("data", "input.csv");
        
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
            return;
        } catch (IOException e) {
            System.err.printf("Помилка читання файлу: %s%n", e.getMessage());
            return;
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

        Path report = Path.of("data", "report.txt");
        try {
            Files.writeString(report, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        String finalReport = sb.toString();
        System.out.println(finalReport);
    }

    private static Product parseLine(String line, int lineNumber, List<String> errorLogs) {
        if (line.trim().isEmpty()) {
            return null;
        }

        String[] fields = line.split(",", -1);
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
            if (cost < 0) {
                throw new IllegalArgumentException();
            }

            currentField = "Цiна";
            price = Double.parseDouble(fields[4].trim().replace(',', '.'));
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

    private static void printProduct(Product product) {
        System.out.printf(Locale.ROOT,
                "Назва = %-15s | Тип = %-10s | Вага = %4dг | Собiвартiсть = %6.2f | Цiна = %6.2f%n",
                product.name(), product.type(), product.weightG(), product.cost(), product.price());
    }

    private static double calculateTotalWeight(List<Product> products) {
        double total = 0;
        for (Product product : products) {
            total += product.weightG();
        }
        return total;
    }

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
}