package lab;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Запити для фільтрації, перетворення та агрегації колекцій хлібобулочних виробів.
 */
public final class BakeryQueries {
    private BakeryQueries() {
    }

    /**
     * Відбирає вироби з маржею, строго більшою за поріг.
     *
     * @param products коректні вироби у початковому порядку
     * @param threshold скінченний поріг маржі
     * @return новий список виробів, що перевищують поріг, у початковому порядку
     * @throws NullPointerException якщо список або його елемент є {@code null}
     * @throws IllegalArgumentException якщо поріг не є скінченним числом
     */
    public static List<BakeryItem> filterByMarginAbove(List<BakeryItem> products, double threshold) {
        Objects.requireNonNull(products, "products must not be null");
        if (!Double.isFinite(threshold)) {
            throw new IllegalArgumentException("Поріг маржі має бути скінченним числом.");
        }

        List<BakeryItem> filtered = new ArrayList<>();
        for (BakeryItem product : products) {
            BakeryItem item = Objects.requireNonNull(product, "products must not contain null");
            if (item.calculateMargin() > threshold) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /**
     * Перетворює вироби на їхні назви, зберігаючи порядок і повторення.
     *
     * @param products коректні вироби у початковому порядку
     * @return новий список назв у початковому порядку
     * @throws NullPointerException якщо список або його елемент є {@code null}
     */
    public static List<String> toNames(List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        List<String> names = new ArrayList<>(products.size());
        for (BakeryItem product : products) {
            names.add(Objects.requireNonNull(product, "products must not contain null").getName());
        }
        return names;
    }

    /**
     * Групує вироби за видом і обчислює середню маржу для кожної групи.
     *
     * @param products коректні вироби у початковому порядку
     * @return карта виду до середньої маржі; порядок входжень видів зберігається
     * @throws NullPointerException якщо список або його елемент є {@code null}
     */
    public static Map<BakeryItem.BakeryKind, Double> groupByKindAverageMargin(List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        return products.stream()
                .map(product -> Objects.requireNonNull(product, "products must not contain null"))
                .collect(Collectors.groupingBy(
                        BakeryItem::getKind,
                        LinkedHashMap::new,
                        Collectors.averagingDouble(BakeryItem::calculateMargin)));
    }

    /**
     * Повертає статистику ваги для всіх коректних виробів.
     *
     * @param products коректні вироби у початковому порядку
     * @return статистика ваги за `getWeightG()`
     * @throws NullPointerException якщо список або його елемент є {@code null}
     */
    public static IntSummaryStatistics weightStatistics(List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        return products.stream()
                .mapToInt(product -> Objects.requireNonNull(product, "products must not contain null").getWeightG())
                .summaryStatistics();
    }
}
