
package Project_2;

/**
 *
 * @author aneirinblosch
 */
public class Product extends AbstractEntity {

    private String name;
    private String description;
    private double price;
    private Category category;
    private String supplierId;
    private boolean isAvailable;


    public Product(String name, String description,
                   double price, Category category, String supplierId) {
        super();
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.supplierId = supplierId;
        this.isAvailable = true;
    }


    public Product(String id, String createdDate, String name, String description,
                   double price, Category category, String supplierId, boolean isAvailable) {
        super(id, createdDate);
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.supplierId = supplierId;
        this.isAvailable = isAvailable;
    }


    public boolean isValidPrice() {
        return price > 0;
    }

    public boolean isValidName() {
        return name != null && !name.trim().isEmpty();
    }

    public void markUnavailable() {
        this.isAvailable = false;
    }

    public void markAvailable() {
        this.isAvailable = true;
    }


    public double getDiscountedPrice(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        return price - (price * discountPercent / 100);
    }


    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }


    @Override
    public void displayDetails() {
        System.out.println("==========================");
        System.out.println("Product ID   : " + getId());
        System.out.println("Name         : " + name);
        System.out.println("Description  : " + description);
        System.out.println("Price        : " + getFormattedPrice());
        System.out.println("Category     : " + category.getDisplayName());
        System.out.println("Supplier ID  : " + supplierId);
        System.out.println("Available    : " + (isAvailable ? "Yes" : "No"));
        System.out.println("Created      : " + getCreatedDate());
        System.out.println("==========================");
    }


    public void displaySummary() {
        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n",
                getId(),
                name,
                category.getDisplayName(),
                getFormattedPrice(),
                isAvailable ? "Available" : "Unavailable");
    }


    public static void displayTableHeader() {
        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n",
                "ID", "Name", "Category", "Price", "Status");
        System.out.println("-".repeat(65));
    }

    public void displaySummaryWithStock(int qty, String stockStatus) {
        System.out.printf("%-10s %-20s %-15s %-10s %-6s %-12s%n",
                getId(),
                name,
                category.getDisplayName(),
                getFormattedPrice(),
                qty,
                stockStatus);
    }

    public static void displayTableHeaderWithStock() {
        System.out.printf("%-10s %-20s %-15s %-10s %-6s %-12s%n",
                "ID", "Name", "Category", "Price", "Qty", "Stock Status");
        System.out.println("-".repeat(73));
    }

    @Override
    public String toString() {
        return "Product[" + getId() + "] " + name + " - " + getFormattedPrice()
             + " (" + category.getDisplayName() + ")";
    }
}