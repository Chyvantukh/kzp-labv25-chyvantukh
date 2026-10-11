package lab;

import java.util.Locale;

/**
 * Представляє один товар, прочитаний із рядка CSV-файлу.
 *
 * <p>Клас зберігає основні властивості товару, перевіряє їхню коректність при створенні
 * та надає фабричний метод для створення екземпляра зі строки у форматі
 * {@code Назва;Тип;Вага;Собівартість;Ціна}.</p>
 */
public class BakeryItem {
private static final String[] FIELD_NAMES = {"Назва", "Тип", "Вага", "Собiвартiсть", "Цiна"};

private final String name;
private final String type;
private final int weightG;
private final double cost;
private final double price;

/**
 * Створює хлібобулочний виріб із заданими характеристиками.
 *
 * @param name назва виробу; не може бути {@code null} або порожньою
 * @param type тип виробу; не може бути {@code null} або порожнім
 * @param weightG вага виробу в грамах; не може бути від'ємною
 * @param cost собівартість виробу; має бути скінченною та невід'ємною
 * @param price ціна виробу; має бути скінченною та невід'ємною
 * @throws IllegalArgumentException якщо будь-який аргумент не відповідає вимогам
 */
public BakeryItem(String name, String type, int weightG, double cost, double price) {
    this(validateArguments(name, type, weightG, cost, price));
}

/**
 * Приватний конструктор, який ініціалізує об'єкт уже перевіреними значеннями.
 *
 * <p>Використовується як внутрішня точка створення після виконання валідації аргументів,
 * що дає змогу централізовано перевіряти коректність даних перед збереженням у поля.</p>
 *
 * @param arguments перевірений набір параметрів для створення товару
 */
private BakeryItem(ValidatedArguments arguments) {
this.name = arguments.name();
this.type = arguments.type();
this.weightG = arguments.weightG();
this.cost = arguments.cost();
this.price = arguments.price();
}

/**
 * Перевіряє аргументи конструктора та формує об'єкт, що містить тільки валідні значення.
 *
 * @param name назва товару
 * @param type тип товару
 * @param weightG вага товару в грамах
 * @param cost собівартість товару
 * @param price ціна товару
 * @return перевірені значення, що готові до створення екземпляра
 * @throws IllegalArgumentException якщо назва, тип, вага або числові поля не відповідають
 *         допустимим обмеженням
 */
private static ValidatedArguments validateArguments(String name, String type, int weightG, double cost, double price) {
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Назва товару не може бути порожньою!");
    }
    if (type == null || type.trim().isEmpty()) {
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

    return new ValidatedArguments(name, type, weightG, cost, price);
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
 * Повертає тип виробу.
 *
 * @return тип виробу
 */
public String getType() {
    return type;
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
 * Створює виріб із CSV-рядка.
 *
 * @param line рядок у форматі «Назва;Тип;Вага;Собівартість;Ціна»
 * @return виріб, створений із рядка
 * @throws IllegalArgumentException якщо рядок або одне з його полів некоректне
 */
public static BakeryItem fromCsv(String line) {
    if (line == null || line.trim().isEmpty()) {
        throw new IllegalArgumentException("порожній рядок");
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

    for (int index = 2; index < fields.length; index++) {
        if (fields[index].trim().isEmpty()) {
            throw new IllegalArgumentException("вiдсутнє поле \"%s\"".formatted(FIELD_NAMES[index]));
        }
    }

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

    return new BakeryItem(fields[0].trim(), fields[1].trim(), weightG, cost, price);
}

/**
 * Повертає зрозуміле текстове представлення виробу з ціною у форматі, незалежному від локалі ОС.
 *
 * @return назва виробу та його ціна з двома десятковими знаками
 */
@Override
public String toString() {
    return String.format(Locale.ROOT, "%s (%.2f грн)", name, price);
}

/**
 * Внутрішній контейнер для перевірених параметрів конструктора.
 *
 * @param name назва товару
 * @param type тип товару
 * @param weightG вага товару в грамах
 * @param cost собівартість товару
 * @param price ціна товару
 */
private record ValidatedArguments(String name, String type, int weightG, double cost, double price) {
}
}