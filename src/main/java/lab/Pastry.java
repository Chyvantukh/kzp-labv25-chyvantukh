package lab;

/**
 * Хлібобулочний виріб, для якого маржа враховує витрати на пакування.
 */
public final class Pastry extends BakeryItem {
    private static final double PACKAGING_COST_RATE = 0.10;

    /**
     * Створює виріб не хлібної категорії із заданими характеристиками.
     *
     * @param name назва виробу
     * @param kind категорія виробу; не може бути {@code null} або {@link BakeryItem.BakeryKind#BREAD}
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     * @throws IllegalArgumentException якщо категорія є {@code null} або
     *                                  {@link BakeryItem.BakeryKind#BREAD}, або інші значення
     *                                  не відповідають вимогам базового класу
     */
    public Pastry(String name, BakeryItem.BakeryKind kind, int weightG, double cost, double price) {
        super(name, requirePastryKind(kind), weightG, cost, price);
    }

    /**
     * Перевіряє, що категорія виробу не є хлібом.
     *
     * @param kind категорія для перевірки
     * @return передана категорія, якщо вона не {@code null} і не {@link BakeryItem.BakeryKind#BREAD}
     * @throws IllegalArgumentException якщо категорія є {@code null} або
     *                                  {@link BakeryItem.BakeryKind#BREAD}
     */
    private static BakeryItem.BakeryKind requirePastryKind(BakeryItem.BakeryKind kind) {
        if (kind == null || kind == BakeryItem.BakeryKind.BREAD) {
            throw new IllegalArgumentException("Pastry потребує категорію, відмінну від BREAD");
        }
        return kind;
    }

    /**
     * Обчислює маржу за вирахуванням витрат на пакування, що становлять 10% собівартості.
     *
     * @return ціна мінус собівартість і витрати на пакування
     */
    @Override
    public double calculateMargin() {
        return getPrice() - getCost() - getCost() * PACKAGING_COST_RATE;
    }
}