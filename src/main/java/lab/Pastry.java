package lab;

/**
 * Хлібобулочний виріб, для якого маржа враховує витрати на пакування.
 */
public final class Pastry extends BakeryItem {
    private static final double PACKAGING_COST_RATE = 0.10;

    /**
     * Ініціалізує виріб даними, попередньо перевіреними фабрикою {@link #create}.
     *
     * @param data перевірені спільні властивості виробу
     */
    private Pastry(BakeryItem.ValidatedData data) {
        super(data);
    }

    /**
     * Створює виріб не хлібної категорії після перевірки його властивостей.
     *
     * @param name назва виробу
     * @param kind категорія виробу; не може бути {@code null} або {@link BakeryItem.BakeryKind#BREAD}
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     * @return створений виріб
     * @throws IllegalArgumentException якщо категорія чи інше значення порушує інваріанти
     */
    public static Pastry create(
            String name, BakeryItem.BakeryKind kind, int weightG, double cost, double price) {
        BakeryItem.BakeryKind pastryKind = requirePastryKind(kind);
        return new Pastry(BakeryItem.validate(name, pastryKind, weightG, cost, price));
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