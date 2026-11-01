package lab;

/**
 * Хлібобулочний виріб із розрахунком маржі з урахуванням виробничих витрат.
 */
public final class Bread extends BakeryItem {
    private static final double MANUFACTURING_COST_RATE = 0.05;

    /**
     * Ініціалізує хліб даними, попередньо перевіреними фабрикою {@link #create}.
     *
     * @param data перевірені спільні властивості виробу
     */
    private Bread(BakeryItem.ValidatedData data) {
        super(data);
    }

    /**
     * Створює хліб після перевірки його спільних властивостей.
     *
     * @param name назва виробу
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     * @return створений хліб
     * @throws IllegalArgumentException якщо будь-яке значення порушує інваріанти виробу
     */
    public static Bread create(String name, int weightG, double cost, double price) {
        return create(name, weightG, cost, price, 0.0);
    }

    /**
     * Створює хліб із заданим часом випікання після перевірки властивостей.
     *
     * @param name назва виробу
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     * @param bakingTimeMinutes час випікання у хвилинах
     * @return створений хліб
     * @throws IllegalArgumentException якщо будь-яке значення порушує інваріанти виробу
     */
    public static Bread create(
            String name, int weightG, double cost, double price, double bakingTimeMinutes) {
        return new Bread(BakeryItem.validate(
                name, BakeryItem.BakeryKind.BREAD, weightG, cost, price, bakingTimeMinutes));
    }

    /**
     * Обчислює маржу за вирахуванням виробничих витрат, що становлять 5% собівартості.
     *
     * @return ціна мінус собівартість і виробничі витрати
     */
    @Override
    public double calculateMargin() {
        return getPrice() - getCost() - getCost() * MANUFACTURING_COST_RATE;
    }
}