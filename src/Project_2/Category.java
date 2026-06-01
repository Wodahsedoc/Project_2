
package Project_2;

/**
 *
 * @author aneirinblosch
 */
public enum Category {

    ELECTRONICS("Electronics", "Electronic devices and accessories"),
    FOOD("Food", "Food and beverage products"),
    CLOTHING("Clothing", "Apparel and fashion items"),
    STATIONERY("Stationery", "Office and school supplies"),
    OTHER("Other", "Miscellaneous products");

    private final String displayName;
    private final String description;

    Category(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }


    public static void displayAll() {
        System.out.println("===== CATEGORIES =====");
        for (int i = 0; i < values().length; i++) {
            System.out.println((i + 1) + ". " + values()[i].getDisplayName()
                             + " - " + values()[i].getDescription());
        }
    }


    public static Category fromIndex(int index) {
        if (index < 1 || index > values().length) {
            throw new IllegalArgumentException("Invalid category selection: " + index);
        }
        return values()[index - 1];
    }

    @Override
    public String toString() {
        return displayName;
    }
}