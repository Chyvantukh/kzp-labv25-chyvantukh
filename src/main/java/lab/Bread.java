package lab;

/**
 * Хлібобулочний виріб із розрахунком маржі з урахуванням виробничих витрат.
 */
public final class Bread extends BakeryItem {
    private static final double MANUFACTURING_COST_RATE = 0.05;

    /**
     * Створює хліб із заданими назвою, вагою, собівартістю та ціною.
     *
     * @param name назва виробу
     * @param weightG вага виробу в грамах
     * @param cost собівартість виробу
     * @param price ціна виробу
     */
    public Bread(String name, int weightG, double cost, double price) {
        super(name, BakeryItem.BakeryKind.BREAD, weightG, cost, price);
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