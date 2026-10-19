package lab;

public final class Bread extends BakeryItem {
    public Bread(String name, int weightG, double cost, double price) {
        super(name, BakeryItem.BakeryKind.BREAD, weightG, cost, price);
    }
}