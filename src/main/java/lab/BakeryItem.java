    package lab;

    import java.util.Locale;

        public class BakeryItem {
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
        public String getName() {return name;}

        /**
         * Повертає тип виробу.
         *
         * @return тип виробу
         */
        public String getType() {return type;}

        /**
         * Повертає вагу виробу в грамах.
         *
         * @return вага виробу в грамах
         */
        public int getWeightG() {return weightG;}

        /**
         * Повертає собівартість виробу.
         *
         * @return собівартість виробу
         */
        public double getCost() {return cost;}

        /**
         * Повертає ціну виробу.
         *
         * @return ціна виробу
         */
        public double getPrice() {return price;}

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