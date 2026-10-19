package lab;

public final class Pastry extends BakeryItem {
    public Pastry(String name, BakeryItem.BakeryKind kind, int weightG, double cost, double price) {
        super(name, requirePastryKind(kind), weightG, cost, price);
    }

    private static BakeryItem.BakeryKind requirePastryKind(BakeryItem.BakeryKind kind) {
        if (kind == null || kind == BakeryItem.BakeryKind.BREAD) {
            throw new IllegalArgumentException("Pastry потребує категорію, відмінну від BREAD");
        }
        return kind;
    }
}