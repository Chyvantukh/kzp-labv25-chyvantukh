package lab;

import java.util.Locale;

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
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Назва товару не може бути порожньою!");
    }
    if (type == null || type.trim().isEmpty()) {
        throw new IllegalArgumentException("Назва типу товару не може бути порожньою!");
    }
    if (weightG < 0) {
        throw new IllegalArgumentException("Вага не може бути від'ємною!");
    }
    if (cost < 0 || !Double.isFinite(cost)) {
        throw new IllegalArgumentException(
                "Собівартість має бути скінченною і невід'ємною!");
    }
    if (price < 0 || !Double.isFinite(price)) {
        throw new IllegalArgumentException("Ціна має бути скінченною і невід'ємною!");
    }

    this.name = name;
    this.type = type;
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

    for (int index = 0; index < fields.length; index++) {
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
        if (!Double.isFinite(cost)) {
            throw new NumberFormatException();
        }
        currentField = FIELD_NAMES[4];
        price = Double.parseDouble(fields[4].trim().replace(',', '.'));
        if (!Double.isFinite(price)) {
            throw new NumberFormatException();
        }
    } catch (NumberFormatException exception) {
        throw new IllegalArgumentException(
                "поле \"%s\" має нечислове значення".formatted(currentField), exception);
    }

    if (weightG < 0) {
        throw new IllegalArgumentException("поле \"%s\" не може бути вiд'ємним".formatted(FIELD_NAMES[2]));
    }
    if (cost < 0) {
        throw new IllegalArgumentException("поле \"%s\" не може бути вiд'ємним".formatted(FIELD_NAMES[3]));
    }
    if (price < 0) {
        throw new IllegalArgumentException("поле \"%s\" не може бути вiд'ємним".formatted(FIELD_NAMES[4]));
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
}