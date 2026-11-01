package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class BakeryQueriesTest {

    @Test
    void filterByMarginAbove_shouldExcludeProductsAtThresholdAndKeepInputOrder() {
        BakeryItem bread = Bread.create("Хліб", 500, 100, 130);
        BakeryItem pastryAtThreshold = Pastry.create(
                "На порозі", BakeryItem.BakeryKind.COOKIE, 100, 100, 130);
        BakeryItem pastryAboveThreshold = Pastry.create(
                "Печиво", BakeryItem.BakeryKind.COOKIE, 100, 100, 131);
        List<BakeryItem> products = List.of(bread, pastryAtThreshold, pastryAboveThreshold);

        assertEquals(List.of(bread, pastryAboveThreshold),
                BakeryQueries.filterByMarginAbove(products, 20.0));
        assertEquals(
                List.of(),
                BakeryQueries.filterByMarginAbove(List.of(pastryAtThreshold), 20.0));
    }

    @Test
    void filterByMarginAbove_shouldAllowNegativeThresholdAndEmptyInput() {
        BakeryItem lossMaking = Bread.create("Збитковий", 100, 100, 90);

        assertEquals(List.of(lossMaking),
                BakeryQueries.filterByMarginAbove(List.of(lossMaking), -20.0));
        assertEquals(List.of(), BakeryQueries.filterByMarginAbove(List.of(), 0.0));
    }

    @ParameterizedTest
    @MethodSource("nonFiniteThresholds")
    void filterByMarginAbove_shouldRejectNonFiniteThreshold(double threshold) {
        assertThrows(IllegalArgumentException.class,
                () -> BakeryQueries.filterByMarginAbove(List.of(), threshold));
    }

    @Test
    void filterByMarginAbove_shouldRejectNullProducts() {
        assertThrows(NullPointerException.class,
                () -> BakeryQueries.filterByMarginAbove(null, 0.0));
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.filterByMarginAbove(Collections.singletonList(null), 0.0));
    }

    @Test
    void toNames_shouldPreserveInputOrderAndDuplicateNames() {
        BakeryItem first = Bread.create("Багет", 100, 10, 20);
        BakeryItem second = Pastry.create("Печиво", BakeryItem.BakeryKind.COOKIE, 100, 10, 20);
        BakeryItem duplicateName = Pastry.create("Багет", BakeryItem.BakeryKind.PIE, 150, 12, 25);

        assertEquals(List.of("Багет", "Печиво", "Багет"),
                BakeryQueries.toNames(List.of(first, second, duplicateName)));
        assertEquals(List.of(), BakeryQueries.toNames(List.of()));
    }

    @Test
    void toNames_shouldRejectNullProducts() {
        assertThrows(NullPointerException.class, () -> BakeryQueries.toNames(null));
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.toNames(Collections.singletonList(null)));
    }

    @Test
    void findByName_shouldReturnFirstExactMatchOrEmpty() {
        BakeryItem firstMatch = Bread.create("Багет", 100, 10, 20);
        BakeryItem secondMatch = Pastry.create(
                "Багет", BakeryItem.BakeryKind.PIE, 150, 12, 25);
        List<BakeryItem> products = List.of(firstMatch, secondMatch);

        assertSame(firstMatch, BakeryQueries.findByName(products, "Багет").orElseThrow());
        assertEquals(Optional.empty(), BakeryQueries.findByName(products, "Батон"));
        assertEquals(Optional.empty(), BakeryQueries.findByName(products, "багет"));
        assertEquals(Optional.empty(), BakeryQueries.findByName(List.of(), "Багет"));
    }

    @Test
    void findByName_shouldRejectNullArguments() {
        assertThrows(NullPointerException.class, () -> BakeryQueries.findByName(null, "Багет"));
        assertThrows(NullPointerException.class, () -> BakeryQueries.findByName(List.of(), null));
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.findByName(Collections.singletonList(null), "Багет"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 5, 6})
    void topFiveByMargin_shouldRespectResultLimitAtBoundarySizes(int productCount) {
        List<BakeryItem> products = IntStream.range(0, productCount)
                .<BakeryItem>mapToObj(index -> Bread.create("Product %d".formatted(index), 100, 0, index))
                .toList();

        assertEquals(Math.min(productCount, 5), BakeryQueries.topFiveByMargin(products).size());
    }

    @Test
    void topFiveByMargin_shouldSortByMarginThenNameAndKeepStableTies() {
        BakeryItem highZ = Bread.create("Zeta", 100, 0, 80);
        BakeryItem highB = Bread.create("Beta", 100, 0, 80);
        BakeryItem medium = Bread.create("Gamma", 100, 0, 70);
        BakeryItem lowAlphaFirst = Bread.create("Alpha", 100, 0, 50);
        BakeryItem lowAlphaSecond = Pastry.create(
                "Alpha", BakeryItem.BakeryKind.PIE, 100, 0, 50);
        BakeryItem belowLimit = Bread.create("Omega", 100, 0, 40);

        assertEquals(
                List.of(highB, highZ, medium, lowAlphaFirst, lowAlphaSecond),
                BakeryQueries.topFiveByMargin(
                        List.of(highZ, highB, medium, lowAlphaFirst, lowAlphaSecond, belowLimit)));
        assertEquals(List.of(), BakeryQueries.topFiveByMargin(List.of()));
    }

    @Test
    void topFiveByMargin_shouldRejectNullProducts() {
        assertThrows(NullPointerException.class, () -> BakeryQueries.topFiveByMargin(null));
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.topFiveByMargin(Collections.singletonList(null)));
    }

    @Test
    void groupByKindAverageMargin_shouldAggregateByKindAndKeepInsertionOrder() {
        BakeryItem breadOne = Bread.create("Багет", 200, 40, 60);
        BakeryItem breadTwo = Bread.create("Хліб", 300, 50, 80);
        BakeryItem cookie = Pastry.create("Печиво", BakeryItem.BakeryKind.COOKIE, 120, 25, 45);
        BakeryItem pie = Pastry.create("Пиріг", BakeryItem.BakeryKind.PIE, 140, 30, 50);
        BakeryItem confectionery = Pastry.create(
                "Круасан", BakeryItem.BakeryKind.CONFECTIONERY, 100, 20, 35);
        BakeryItem bakedGood = Pastry.create(
                "Булочка", BakeryItem.BakeryKind.BAKED_GOOD, 90, 15, 25);

        Map<BakeryItem.BakeryKind, Double> averages = BakeryQueries.groupByKindAverageMargin(
                List.of(breadOne, breadTwo, cookie, pie, confectionery, bakedGood));

        assertEquals(
                Map.of(
                        BakeryItem.BakeryKind.BREAD, 22.75,
                        BakeryItem.BakeryKind.COOKIE, 17.5,
                        BakeryItem.BakeryKind.PIE, 17.0,
                        BakeryItem.BakeryKind.CONFECTIONERY, 13.0,
                        BakeryItem.BakeryKind.BAKED_GOOD, 8.5),
                averages);
        assertEquals(
                List.of(
                        BakeryItem.BakeryKind.BREAD,
                        BakeryItem.BakeryKind.COOKIE,
                        BakeryItem.BakeryKind.PIE,
                        BakeryItem.BakeryKind.CONFECTIONERY,
                        BakeryItem.BakeryKind.BAKED_GOOD),
                List.copyOf(averages.keySet()));
        assertEquals(Map.of(), BakeryQueries.groupByKindAverageMargin(List.of()));
    }

    @Test
    void groupByKindAverageMargin_shouldRejectNullProducts() {
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.groupByKindAverageMargin(null));
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.groupByKindAverageMargin(Collections.singletonList(null)));
    }

    @Test
    void weightStatistics_shouldComputeStatisticsForAllWeights() {
        BakeryItem bread = Bread.create("Багет", 200, 40, 60);
        BakeryItem cookie = Pastry.create("Печиво", BakeryItem.BakeryKind.COOKIE, 120, 25, 45);
        BakeryItem pie = Pastry.create("Пиріг", BakeryItem.BakeryKind.PIE, 140, 30, 50);

        IntSummaryStatistics statistics = BakeryQueries.weightStatistics(List.of(bread, cookie, pie));
        assertEquals(3, statistics.getCount());
        assertEquals(200, statistics.getMax());
        assertEquals(120, statistics.getMin());
        assertEquals(460, statistics.getSum());
        assertEquals(153.33333333333334, statistics.getAverage());
    }

    @Test
    void weightStatistics_shouldReturnEmptyStatisticsForEmptyProducts() {
        IntSummaryStatistics statistics = BakeryQueries.weightStatistics(List.of());

        assertEquals(0, statistics.getCount());
        assertEquals(0, statistics.getSum());
        assertEquals(0.0, statistics.getAverage());
        assertEquals(Integer.MAX_VALUE, statistics.getMin());
        assertEquals(Integer.MIN_VALUE, statistics.getMax());
    }

    @Test
    void weightStatistics_shouldHandleSingleZeroWeightProduct() {
        BakeryItem zeroWeight = Bread.create("Порожній", 0, 0, 0);

        IntSummaryStatistics statistics = BakeryQueries.weightStatistics(List.of(zeroWeight));

        assertEquals(1, statistics.getCount());
        assertEquals(0, statistics.getSum());
        assertEquals(0, statistics.getMin());
        assertEquals(0, statistics.getMax());
        assertEquals(0.0, statistics.getAverage());
    }

    @Test
    void bakeryQueries_shouldMatchManualLoopImplementationForRepresentativeDataset() {
        BakeryItem alpha = Bread.create("Alpha", 180, 15, 40);
        BakeryItem beta = Pastry.create("Beta", BakeryItem.BakeryKind.COOKIE, 100, 12, 32);
        BakeryItem gamma = Bread.create("Gamma", 220, 22, 45);
        BakeryItem delta = Pastry.create("Delta", BakeryItem.BakeryKind.PIE, 150, 18, 36);
        BakeryItem alphaDuplicate = Bread.create("Alpha", 200, 17, 44);
        BakeryItem omega = Bread.create("Omega", 120, 18, 24);

        List<BakeryItem> products = List.of(alpha, beta, gamma, delta, alphaDuplicate, omega);
        List<BakeryItem> before = new ArrayList<>(products);

        assertEquals(
                manualFilterByMarginAbove(products, 12.0),
                BakeryQueries.filterByMarginAbove(products, 12.0));
        assertEquals(
                manualToNames(products),
                BakeryQueries.toNames(products));
        assertEquals(
                manualFindByName(products, "Alpha"),
                BakeryQueries.findByName(products, "Alpha"));
        assertEquals(
                manualFindByName(products, "Missing"),
                BakeryQueries.findByName(products, "Missing"));
        assertSame(alpha, BakeryQueries.findByName(products, "Alpha").orElseThrow());
        assertEquals(Optional.empty(), BakeryQueries.findByName(products, "Missing"));
        assertEquals(
                manualTopFiveByMargin(products),
                BakeryQueries.topFiveByMargin(products));
        assertEquals(
                manualGroupByKindAverageMargin(products),
                BakeryQueries.groupByKindAverageMargin(products));
        assertStatisticsEquals(
                manualWeightStatistics(products),
                BakeryQueries.weightStatistics(products));
        assertEquals(before, products);
    }

    private static List<BakeryItem> manualFilterByMarginAbove(List<BakeryItem> products, double threshold) {
        Objects.requireNonNull(products, "products must not be null");
        if (!Double.isFinite(threshold)) {
            throw new IllegalArgumentException("Поріг маржі має бути скінченним числом.");
        }

        List<BakeryItem> result = new ArrayList<>();
        for (BakeryItem product : products) {
            BakeryItem item = Objects.requireNonNull(product, "products must not contain null");
            if (item.calculateMargin() > threshold) {
                result.add(item);
            }
        }
        return result;
    }

    private static List<String> manualToNames(List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        List<String> names = new ArrayList<>();
        for (BakeryItem product : products) {
            names.add(Objects.requireNonNull(product, "products must not contain null").getName());
        }
        return names;
    }

    private static Optional<BakeryItem> manualFindByName(List<BakeryItem> products, String name) {
        Objects.requireNonNull(products, "products must not be null");
        Objects.requireNonNull(name, "name must not be null");

        for (BakeryItem product : products) {
            BakeryItem item = Objects.requireNonNull(product, "products must not contain null");
            if (item.getName().equals(name)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private static List<BakeryItem> manualTopFiveByMargin(List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        List<BakeryItem> sorted = new ArrayList<>();
        for (BakeryItem product : products) {
            sorted.add(Objects.requireNonNull(product, "products must not contain null"));
        }

        sorted.sort(
                Comparator.comparingDouble(BakeryItem::calculateMargin)
                        .reversed()
                        .thenComparing(BakeryItem::getName));
        return sorted.subList(0, Math.min(sorted.size(), 5));
    }

    private static Map<BakeryItem.BakeryKind, Double> manualGroupByKindAverageMargin(
            List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        Map<BakeryItem.BakeryKind, Double> sums = new LinkedHashMap<>();
        Map<BakeryItem.BakeryKind, Integer> counts = new LinkedHashMap<>();
        for (BakeryItem product : products) {
            BakeryItem item = Objects.requireNonNull(product, "products must not contain null");
            BakeryItem.BakeryKind kind = item.getKind();

            sums.put(kind, sums.getOrDefault(kind, 0.0) + item.calculateMargin());
            counts.put(kind, counts.getOrDefault(kind, 0) + 1);
        }

        Map<BakeryItem.BakeryKind, Double> result = new LinkedHashMap<>();
        for (Map.Entry<BakeryItem.BakeryKind, Double> entry : sums.entrySet()) {
            BakeryItem.BakeryKind kind = entry.getKey();
            result.put(kind, entry.getValue() / counts.get(kind));
        }
        return result;
    }

    private static IntSummaryStatistics manualWeightStatistics(List<BakeryItem> products) {
        Objects.requireNonNull(products, "products must not be null");

        IntSummaryStatistics statistics = new IntSummaryStatistics();
        for (BakeryItem product : products) {
            statistics.accept(Objects.requireNonNull(product, "products must not contain null").getWeightG());
        }
        return statistics;
    }

    private static void assertStatisticsEquals(
            IntSummaryStatistics expected, IntSummaryStatistics actual) {
        assertEquals(expected.getCount(), actual.getCount());
        assertEquals(expected.getSum(), actual.getSum());
        assertEquals(expected.getMin(), actual.getMin());
        assertEquals(expected.getMax(), actual.getMax());
        assertEquals(expected.getAverage(), actual.getAverage());
    }

    @Test
    void weightStatistics_shouldRejectNullProducts() {
        assertThrows(NullPointerException.class, () -> BakeryQueries.weightStatistics(null));
        assertThrows(
                NullPointerException.class,
                () -> BakeryQueries.weightStatistics(Collections.singletonList(null)));
    }

    private static Stream<Double> nonFiniteThresholds() {
        return Stream.of(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }
}
