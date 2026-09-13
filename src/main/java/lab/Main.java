package lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;

public class Main {
    private static final String[] FIELD_NAMES = {"Назва", "Тип", "Вага", "Собівартість", "Ціна"};

    public static void main(String[] args) {
        Path file = Path.of("data", "input.csv");

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                Product product = parseLine(line, ++lineNumber);
                if (product != null) {
                    printProduct(product);
                }
            }
        } catch (NoSuchFileException e) {
            System.err.printf("Файл не знайдено за шляхом: %s%n", file.toAbsolutePath());
        } catch (IOException e) {
            System.err.printf("Помилка читання файлу: %s%n", e.getMessage());
        }
    }

    private static Product parseLine(String line, int lineNumber) {
        if (line.trim().isEmpty()) {
            return null;
        }

        String[] fields = line.split(",", -1);
        if (!hasAllFields(fields, lineNumber)) {
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

            currentField = "Собівартість";
            cost = Double.parseDouble(fields[3].trim().replace(',', '.'));
            if (cost < 0) {
                throw new IllegalArgumentException();
            }

            currentField = "Ціна";
            price = Double.parseDouble(fields[4].trim().replace(',', '.'));
            if (price < 0) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            System.err.printf(Locale.ROOT, "Рядок %d: поле \"%s\" має нечислове значення%n",
                    lineNumber, currentField);
            return null;
        } catch (IllegalArgumentException e) {
            System.err.printf(Locale.ROOT, "Рядок %d: поле \"%s\" не може бути від'ємним%n",
                    lineNumber, currentField);
            return null;
        }

        return new Product(name, type, weightG, cost, price);
    }

    private static void printProduct(Product product) {
        System.out.printf(Locale.ROOT,
                "Назва = %s | Тип = %s | Вага = %dг | Собівартість = %.2f | Ціна = %.2f%n",
                product.name(), product.type(), product.weightG(), product.cost(), product.price());
    }

    private static boolean hasAllFields(String[] fields, int lineNumber) {
        boolean hasMissingField = false;
        for (int fieldIndex = 0; fieldIndex < FIELD_NAMES.length; fieldIndex++) {
            if (fieldIndex >= fields.length || fields[fieldIndex].trim().isEmpty()) {
                System.err.printf(Locale.ROOT, "Рядок %d: відсутнє поле \"%s\"%n",
                        lineNumber, FIELD_NAMES[fieldIndex]);
                hasMissingField = true;
            }
        }
        return !hasMissingField && fields.length <= FIELD_NAMES.length;
    }

    private record Product(String name, String type, int weightG, double cost, double price) {
    }
}