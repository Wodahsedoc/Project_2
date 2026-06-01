
package Project_2;

/**
 *
 * @author aneirinblosch
 */
public class OrderLine extends AbstractEntity {

    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double discountPercent;


    public OrderLine(String productId, String productName,
                     int quantity, double unitPrice) {
        super();
        validateQuantity(quantity);
        validatePrice(unitPrice);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = 0.0;
    }


    public OrderLine(String productId, String productName,
                     int quantity, double unitPrice, double discountPercent) {
        super();
        validateQuantity(quantity);
        validatePrice(unitPrice);
        validateDiscount(discountPercent);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent;
    }


    public OrderLine(String id, String createdDate, String productId,
                     String productName, int quantity, double unitPrice,
                     double discountPercent) {
        super(id, createdDate);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent;
    }


    public double getSubTotal() {
        return unitPrice * quantity;
    }

    public double getDiscountAmount() {
        return getSubTotal() * (discountPercent / 100);
    }

    public double getFinalTotal() {
        return getSubTotal() - getDiscountAmount();
    }

    public void updateQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public void applyDiscount(double discountPercent) {
        validateDiscount(discountPercent);
        this.discountPercent = discountPercent;
        System.out.println("Discount of " + discountPercent + "% applied to "
                         + productName);
    }

    public void removeDiscount() {
        this.discountPercent = 0.0;
        System.out.println("Discount removed from " + productName);
    }

    public boolean hasDiscount() {
        return discountPercent > 0;
    }


    public String getFormattedUnitPrice() {
        return String.format("$%.2f", unitPrice);
    }

    public String getFormattedSubTotal() {
        return String.format("$%.2f", getSubTotal());
    }

    public String getFormattedDiscountAmount() {
        return String.format("$%.2f", getDiscountAmount());
    }

    public String getFormattedFinalTotal() {
        return String.format("$%.2f", getFinalTotal());
    }


    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    private void validatePrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than 0");
        }
    }

    private void validateDiscount(double discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
    }


    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        validatePrice(unitPrice);
        this.unitPrice = unitPrice;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        validateDiscount(discountPercent);
        this.discountPercent = discountPercent;
    }


    @Override
    public void displayDetails() {
        System.out.println("  --------------------------");
        System.out.println("  Order Line ID  : " + getId());
        System.out.println("  Product ID     : " + productId);
        System.out.println("  Product Name   : " + productName);
        System.out.println("  Quantity       : " + quantity);
        System.out.println("  Unit Price     : " + getFormattedUnitPrice());
        System.out.println("  Sub Total      : " + getFormattedSubTotal());
        if (hasDiscount()) {
            System.out.println("  Discount       : " + discountPercent + "%");
            System.out.println("  Discount Amount: " + getFormattedDiscountAmount());
        }
        System.out.println("  Final Total    : " + getFormattedFinalTotal());
        System.out.println("  --------------------------");
    }


    public void displaySummary() {
        System.out.printf("  %-10s %-20s %-8s %-10s %-10s %-10s%n",
                productId,
                productName,
                quantity,
                getFormattedUnitPrice(),
                hasDiscount() ? discountPercent + "%" : "None",
                getFormattedFinalTotal());
    }

    public static void displayTableHeader() {
        System.out.printf("  %-10s %-20s %-8s %-10s %-10s %-10s%n",
                "Prod ID", "Product Name", "Qty", "Unit Price", "Discount", "Total");
        System.out.println("  " + "-".repeat(68));
    }

    @Override
    public String toString() {
        return "OrderLine[" + getId() + "] " + productName
             + " | Qty: " + quantity
             + " | Unit: " + getFormattedUnitPrice()
             + " | Total: " + getFormattedFinalTotal();
    }
}