package lab;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільні властивості хлібобулочних виробів, зчитаних із CSV-файлу.
 *
 * <p>Категорія виробу зберігається як {@link BakeryKind}, а фабричний метод
 * створює відповідний підтип {@link Bread} або {@link Pastry}.</p>
 */
public abstract sealed class BakeryItem permits Bread, Pastry {
    private static final String[] FIELD_NAMES = {
        "Назва", "Тип", "Вага", "Собiвартiсть", "Цiна", "Час випiкання"
    };
    private static final int LEGACY_FIELD_COUNT = FIELD_NAMES.length - 1;

    private final String name;
    private final BakeryKind kind;
    private final int weightG;
    private final double cost;
    private final double price;
    private final double bakingTimeMinutes;

    /**
     * Категорії виробів, що підтримуються у вхідному CSV.
     */
    public enum BakeryKind {
        /** Хліб; значення у CSV — {@code хлiб}. */
        BREAD("хлiб"),
        /** Пиріг; значення у CSV — {@code пирiг}. */
        PIE("пирiг"),
        /** Кондитерський виріб; значення у CSV — {@code кондитерський}. */
        CONFECTIONERY("кондитерський"),
        /** Печиво; значення у CSV — {@code печиво}. */
        COOKIE("печиво"),
        /** Інша випічка; значення у CSV — {@code випiчка}. */
        BAKED_GOOD("випiчка");

        private final String csvValue;

        /**
         * Створює категорію з її текстовим значенням у CSV.
         *
         * @param csvValue текстове значення категорії у CSV
         */
        BakeryKind(String csvValue) {
            this.csvValue = csvValue;
        }

        /**
         * Повертає текстове значення категорії у CSV.
         *
         * @return значення категорії з CSV
         */
        public String csvValue() {
            return csvValue;
        }

        /**
         * Знаходить категорію за її текстовим значенням у CSV.
         *
         * @param value текстове значення категорії
         * @return відповідна категорія
         * @throws IllegalArgumentException якщо значення не відповідає жодній категорії
         */
        private static BakeryKind fromCsv(String value) {
            for (BakeryKind kind : values()) {
                if (kind.csvValue.equals(value)) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("невiдомий тип товару: %s".formatted(value));
        }
    }

    /**
     * Перевіряє спільні інваріанти виробу перед створенням об'єкта.
     *
     * @param name назва виробу
     * @param kind категорія виробу
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     * @param bakingTimeMinutes час випікання у хвилинах
     * @return перевірені дані для конструктора підтипу
     * @throws IllegalArgumentException якщо значення порушує інваріанти виробу
     */
    static ValidatedData validate(
            String name, BakeryKind kind, int weightG, double cost, double price, double bakingTimeMinutes) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва товару не може бути порожньою!");
        }
        if (kind == null) {
            throw new IllegalArgumentException("Назва типу товару не може бути порожньою!");
        }
        if (weightG < 0) {
            throw new IllegalArgumentException("поле \"Вага\" не може бути вiд'ємним");
        }
        if (!Double.isFinite(cost)) {
            throw new IllegalArgumentException("поле \"Собiвартiсть\" має нечислове значення");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("поле \"Собiвартiсть\" не може бути вiд'ємним");
        }
        if (!Double.isFinite(price)) {
            throw new IllegalArgumentException("поле \"Цiна\" має нечислове значення");
        }
        if (price < 0) {
            throw new IllegalArgumentException("поле \"Цiна\" не може бути вiд'ємним");
        }
        if (!Double.isFinite(bakingTimeMinutes)) {
            throw new IllegalArgumentException("поле \"Час випiкання\" має нечислове значення");
        }
        if (bakingTimeMinutes < 0) {
            throw new IllegalArgumentException("поле \"Час випiкання\" не може бути вiд'ємним");
        }

        return new ValidatedData(name, kind, weightG, cost, price, bakingTimeMinutes);
    }

    /**
     * Ініціалізує базовий стан даними, перевіреними фабрикою підтипу.
     *
     * @param data перевірені значення виробу
     */
    protected BakeryItem(ValidatedData data) {
        this.name = data.name();
        this.kind = data.kind();
        this.weightG = data.weightG();
        this.cost = data.cost();
        this.price = data.price();
        this.bakingTimeMinutes = data.bakingTimeMinutes();
    }

    /**
     * Перевірені значення, потрібні для створення підтипу виробу.
     *
     * @param name назва виробу
     * @param kind категорія виробу
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     * @param bakingTimeMinutes час випікання у хвилинах
     */
    record ValidatedData(
            String name, BakeryKind kind, int weightG, double cost, double price, double bakingTimeMinutes) {
    }

    /**
     * Повертає назву виробу.
     *
     * @return назва виробу
     */
    public String getName() {
        return name;
    }

    /**
     * Повертає enum-категорію виробу.
     *
     * @return категорія виробу
     */
    public BakeryKind getKind() {
        return kind;
    }

    /**
     * Повертає вагу виробу в грамах.
     *
     * @return вага виробу в грамах
     */
    public int getWeightG() {
        return weightG;
    }

    /**
     * Повертає собівартість виробу.
     *
     * @return собівартість виробу
     */
    public double getCost() {
        return cost;
    }

    /**
     * Повертає ціну виробу.
     *
     * @return ціна виробу
     */
    public double getPrice() {
        return price;
    }

    /**
     * Повертає час випікання у хвилинах.
     *
     * @return час випікання; для рядків старого CSV-формату — {@code 0.0}
     */
    public double getBakingTimeMinutes() {
        return bakingTimeMinutes;
    }

    /**
     * Обчислює маржу виробу з урахуванням додаткових витрат його підтипу.
     *
     * @return ціна мінус собівартість і додаткові витрати
     */
    public abstract double calculateMargin();

    /**
     * Порівнює вироби за всіма спільними значеннями сутності.
     *
     * @param other об'єкт для порівняння
     * @return {@code true}, якщо збігаються всі властивості виробів
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BakeryItem item)) {
            return false;
        }
        return weightG == item.weightG
                && Double.compare(cost, item.cost) == 0
                && Double.compare(price, item.price) == 0
                && Double.compare(bakingTimeMinutes, item.bakingTimeMinutes) == 0
                && name.equals(item.name)
                && kind == item.kind;
    }

    /**
     * Повертає хеш-код, узгоджений з рівністю виробів.
     *
     * @return хеш-код усіх властивостей виробу
     */
    @Override
    public final int hashCode() {
        return Objects.hash(name, kind, weightG, cost, price, bakingTimeMinutes);
    }

    /**
     * Створює виріб із CSV-рядка та визначає його конкретний підтип.
     *
     * @param line рядок старого формату з п'ятьма полями або нового формату з часом випікання
     * @return створений об'єкт {@link Bread} або {@link Pastry}
     * @throws IllegalArgumentException якщо рядок або одне з його полів некоректне
     */
    public static BakeryItem fromCsv(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("порожнiй рядок");
        }

        String[] fields = line.split(";", -1);
        if (fields.length < LEGACY_FIELD_COUNT) {
            for (int index = 0; index < LEGACY_FIELD_COUNT; index++) {
                if (index >= fields.length || fields[index].trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "вiдсутнє поле \"%s\"".formatted(FIELD_NAMES[index]));
                }
            }
        }
        if (fields.length > FIELD_NAMES.length) {
            throw new IllegalArgumentException("забагато полiв (очiкувалося 6, знайдено %d)"
                    .formatted(fields.length));
        }

        for (int index = 1; index < fields.length; index++) {
            if (fields[index].trim().isEmpty()) {
                throw new IllegalArgumentException("вiдсутнє поле \"%s\"".formatted(FIELD_NAMES[index]));
            }
        }

        BakeryKind kind = BakeryKind.fromCsv(fields[1].trim());
        int weightG;
        double cost;
        double price;
        double bakingTimeMinutes = 0.0;
        String currentField = FIELD_NAMES[2];
        try {
            weightG = Integer.parseInt(fields[2].trim());
            currentField = FIELD_NAMES[3];
            cost = Double.parseDouble(fields[3].trim().replace(',', '.'));
            currentField = FIELD_NAMES[4];
            price = Double.parseDouble(fields[4].trim().replace(',', '.'));
            if (fields.length == FIELD_NAMES.length) {
                currentField = FIELD_NAMES[5];
                bakingTimeMinutes = Double.parseDouble(fields[5].trim().replace(',', '.'));
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "поле \"%s\" має нечислове значення".formatted(currentField), exception);
        }

        String name = fields[0].trim();
        if (kind == BakeryKind.BREAD) {
            return Bread.create(name, weightG, cost, price, bakingTimeMinutes);
        }
        return Pastry.create(name, kind, weightG, cost, price, bakingTimeMinutes);
    }

    /**
     * Повертає назву виробу та ціну у форматі, незалежному від локалі ОС.
     *
     * @return назва виробу та його ціна з двома десятковими знаками
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%.2f грн)", name, price);
    }
}
