
package Project_2;

/**
 *
 * @author aneirinblosch
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockItem extends AbstractEntity {

    private String productId;
    private int quantityInStock;
    private int minimumThreshold;
    private int maximumCapacity;
    private String lastRestocked;
    private String lastUpdated;


    public StockItem(String productId, int quantityInStock,
                     int minimumThreshold, int maximumCapacity) {
        super();
        validateQuantity(quantityInStock);
        validateThreshold(minimumThreshold, maximumCapacity);
        this.productId = productId;
        this.quantityInStock = quantityInStock;
        this.minimumThreshold = minimumThreshold;
        this.maximumCapacity = maximumCapacity;
        this.lastRestocked = "Never";
        this.lastUpdated = getCurrentDateTime();
    }


    public StockItem(String id, String createdDate, String productId,
                     int quantityInStock, int minimumThreshold,
                     int maximumCapacity, String lastRestocked, String lastUpdated) {
        super(id, createdDate);
        this.productId = productId;
        this.quantityInStock = quantityInStock;
        this.minimumThreshold = minimumThreshold;
        this.maximumCapacity = maximumCapacity;
        this.lastRestocked = lastRestocked;
        this.lastUpdated = lastUpdated;
    }


    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than 0");
        }
        if (quantityInStock + quantity > maximumCapacity) {
            throw new IllegalArgumentException(
                "Restock quantity exceeds maximum capacity of " + maximumCapacity
                + ". You can only add " + getRemainingCapacity() + " more units."
            );
        }
        quantityInStock += quantity;
        lastRestocked = getCurrentDateTime();
        lastUpdated = getCurrentDateTime();
        System.out.println("Successfully restocked " + quantity
                         + " units. New stock level: " + quantityInStock);
    }

    public boolean reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quantity > quantityInStock) {
            System.out.println("Insufficient stock. Available: " + quantityInStock);
            return false;
        }
        quantityInStock -= quantity;
        lastUpdated = getCurrentDateTime();
        return true;
    }

    public boolean isLowStock() {
        return quantityInStock <= minimumThreshold;
    }

    public boolean isOutOfStock() {
        return quantityInStock == 0;
    }

    public boolean isFullyStocked() {
        return quantityInStock >= maximumCapacity;
    }

    public int getRemainingCapacity() {
        return maximumCapacity - quantityInStock;
    }

    public int getRestockRecommendation() {

        int targetStock = (int) (maximumCapacity * 0.8);
        return Math.max(0, targetStock - quantityInStock);
    }

    public String getStockStatus() {
        if (isOutOfStock()) return "OUT OF STOCK";
        if (isLowStock())   return "LOW STOCK";
        if (isFullyStocked()) return "FULLY STOCKED";
        return "IN STOCK";
    }


    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    private void validateThreshold(int minimum, int maximum) {
        if (minimum < 0) {
            throw new IllegalArgumentException("Minimum threshold cannot be negative");
        }
        if (maximum <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be greater than 0");
        }
        if (minimum >= maximum) {
            throw new IllegalArgumentException(
                "Minimum threshold must be less than maximum capacity"
            );
        }
    }


    private String getCurrentDateTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }


    public String getProductId() {
        return productId;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        validateQuantity(quantityInStock);
        this.quantityInStock = quantityInStock;
        this.lastUpdated = getCurrentDateTime();
    }

    public int getMinimumThreshold() {
        return minimumThreshold;
    }

    public void setMinimumThreshold(int minimumThreshold) {
        validateThreshold(minimumThreshold, maximumCapacity);
        this.minimumThreshold = minimumThreshold;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        validateThreshold(minimumThreshold, maximumCapacity);
        this.maximumCapacity = maximumCapacity;
    }

    public String getLastRestocked() {
        return lastRestocked;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }


    @Override
    public void displayDetails() {
        System.out.println("==========================");
        System.out.println("Stock ID          : " + getId());
        System.out.println("Product ID        : " + productId);
        System.out.println("Quantity In Stock : " + quantityInStock);
        System.out.println("Minimum Threshold : " + minimumThreshold);
        System.out.println("Maximum Capacity  : " + maximumCapacity);
        System.out.println("Remaining Capacity: " + getRemainingCapacity());
        System.out.println("Stock Status      : " + getStockStatus());
        System.out.println("Restock Recommend : " + getRestockRecommendation() + " units");
        System.out.println("Last Restocked    : " + lastRestocked);
        System.out.println("Last Updated      : " + lastUpdated);
        System.out.println("Created           : " + getCreatedDate());
        System.out.println("==========================");
    }


    public void displaySummary(String productName) {
        System.out.printf("%-12s %-22s %-6s %-6s %-6s %-14s %-15s%n",
                productId,
                productName,
                quantityInStock,
                minimumThreshold,
                maximumCapacity,
                getStockStatus(),
                lastRestocked);
    }

    public static void displayTableHeader() {
        System.out.printf("%-12s %-22s %-6s %-6s %-6s %-14s %-15s%n",
                "Prod ID", "Name", "Qty", "Min", "Max", "Status", "Last Restocked");
        System.out.println("-".repeat(81));
    }


    public void displayLowStockAlert() {
        System.out.println("⚠ LOW STOCK ALERT");
        System.out.println("  Product ID        : " + productId);
        System.out.println("  Current Stock     : " + quantityInStock);
        System.out.println("  Minimum Threshold : " + minimumThreshold);
        System.out.println("  Recommended Restock: " + getRestockRecommendation() + " units");
    }

    @Override
    public String toString() {
        return "StockItem[" + getId() + "] Product: " + productId
             + " | Qty: " + quantityInStock
             + " | Status: " + getStockStatus();
    }
}