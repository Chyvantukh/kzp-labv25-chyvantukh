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
    private static final String[] FIELD_NAMES = {"Назва", "Тип", "Вага", "Собiвартiсть", "Цiна"};

    private final String name;
    private final BakeryKind kind;
    private final int weightG;
    private final double cost;
    private final double price;

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
     * Створює базовий виріб із перевіреними спільними властивостями.
     *
     * @param name назва виробу; не може бути порожньою
     * @param kind категорія виробу; не може бути {@code null}
     * @param weightG вага виробу в грамах; не може бути від'ємною
     * @param cost собівартість виробу; має бути скінченною та невід'ємною
     * @param price ціна виробу; має бути скінченною та невід'ємною
     * @throws IllegalArgumentException якщо будь-який аргумент не відповідає вимогам
     */
    protected BakeryItem(String name, BakeryKind kind, int weightG, double cost, double price) {
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

        this.name = name;
        this.kind = kind;
        this.weightG = weightG;
        this.cost = cost;
        this.price = price;
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
     * Обчислює маржу виробу з урахуванням додаткових витрат його підтипу.
     *
     * @return ціна мінус собівартість і додаткові витрати
     */
    public abstract double calculateMargin();

    /**
     * Порівнює вироби за всіма спільними значеннями сутності.
     *
     * @param other об'єкт для порівняння
     * @return {@code true}, якщо збігаються назва, категорія, вага, собівартість і ціна
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
                && name.equals(item.name)
                && kind == item.kind;
    }

    /**
     * Повертає хеш-код, узгоджений з рівністю виробів.
     *
     * @return хеш-код назви, категорії, ваги, собівартості й ціни
     */
    @Override
    public final int hashCode() {
        return Objects.hash(name, kind, weightG, cost, price);
    }

    /**
     * Створює виріб із CSV-рядка та визначає його конкретний підтип.
     *
     * @param line рядок у форматі «Назва;Тип;Вага;Собівартість;Ціна»
     * @return створений об'єкт {@link Bread} або {@link Pastry}
     * @throws IllegalArgumentException якщо рядок або одне з його полів некоректне
     */
    public static BakeryItem fromCsv(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("порожнiй рядок");
        }

        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_NAMES.length) {
            if (fields.length < FIELD_NAMES.length) {
                for (int index = 0; index < FIELD_NAMES.length; index++) {
                    if (index >= fields.length || fields[index].trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                "вiдсутнє поле \"%s\"".formatted(FIELD_NAMES[index]));
                    }
                }
            }
            throw new IllegalArgumentException("забагато полiв (очiкувалося %d, знайдено %d)"
                    .formatted(FIELD_NAMES.length, fields.length));
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
        String currentField = FIELD_NAMES[2];
        try {
            weightG = Integer.parseInt(fields[2].trim());
            currentField = FIELD_NAMES[3];
            cost = Double.parseDouble(fields[3].trim().replace(',', '.'));
            currentField = FIELD_NAMES[4];
            price = Double.parseDouble(fields[4].trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "поле \"%s\" має нечислове значення".formatted(currentField), exception);
        }

        String name = fields[0].trim();
        if (kind == BakeryKind.BREAD) {
            return new Bread(name, weightG, cost, price);
        }
        return new Pastry(name, kind, weightG, cost, price);
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
