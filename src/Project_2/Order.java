
package Project_2;

/**
 *
 * @author aneirinblosch
 */
import java.util.ArrayList;
import java.util.List;

public class Order extends AbstractEntity implements Reportable {

    private String supplierId;
    private String supplierName;
    private String createdByUserId;
    private List<OrderLine> orderLines;
    private OrderStatus status;
    private String lastUpdated;
    private String notes;


    public enum OrderStatus {
        PENDING("Pending", "Order has been created but not yet processed"),
        CONFIRMED("Confirmed", "Order has been confirmed by supplier"),
        PROCESSING("Processing", "Order is being processed"),
        DELIVERED("Delivered", "Order has been delivered"),
        CANCELLED("Cancelled", "Order has been cancelled");

        private final String displayName;
        private final String description;

        OrderStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }


    public Order(String supplierId, String supplierName, String createdByUserId) {
        super();
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.createdByUserId = createdByUserId;
        this.orderLines = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.lastUpdated = getCreatedDate();
        this.notes = "";
    }


    public Order(String id, String createdDate, String supplierId,
                 String supplierName, String createdByUserId,
                 List<OrderLine> orderLines, OrderStatus status,
                 String lastUpdated, String notes) {
        super(id, createdDate);
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.createdByUserId = createdByUserId;
        this.orderLines = orderLines != null ? orderLines : new ArrayList<>();
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.notes = notes;
    }


    public void addOrderLine(OrderLine orderLine) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot modify order lines. Order status is: " + status.getDisplayName()
            );
        }

        for (OrderLine line : orderLines) {
            if (line.getProductId().equals(orderLine.getProductId())) {
                line.updateQuantity(line.getQuantity() + orderLine.getQuantity());
                updateLastUpdated();
                System.out.println("Updated quantity for " + orderLine.getProductName()
                                 + " in order");
                return;
            }
        }
        orderLines.add(orderLine);
        updateLastUpdated();
        System.out.println("Added " + orderLine.getProductName() + " to order");
    }

    public boolean removeOrderLine(String productId) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot modify order lines. Order status is: " + status.getDisplayName()
            );
        }
        boolean removed = orderLines.removeIf(
            line -> line.getProductId().equals(productId)
        );
        if (removed) {
            updateLastUpdated();
            System.out.println("Removed product " + productId + " from order");
        } else {
            System.out.println("Product " + productId + " not found in order");
        }
        return removed;
    }

    public OrderLine findOrderLine(String productId) {
        for (OrderLine line : orderLines) {
            if (line.getProductId().equals(productId)) {
                return line;
            }
        }
        return null;
    }


    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        }
        if (orderLines.isEmpty()) {
            throw new IllegalStateException("Cannot confirm an empty order");
        }
        this.status = OrderStatus.CONFIRMED;
        updateLastUpdated();
        System.out.println("Order " + getId() + " has been confirmed");
    }

    public void process() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be processed");
        }
        this.status = OrderStatus.PROCESSING;
        updateLastUpdated();
        System.out.println("Order " + getId() + " is now being processed");
    }

    public void deliver() {
        if (status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Only PROCESSING orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED;
        updateLastUpdated();
        System.out.println("Order " + getId() + " has been delivered");
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        updateLastUpdated();
        System.out.println("Order " + getId() + " has been cancelled");
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean isModifiable() {
        return status == OrderStatus.PENDING;
    }


    public double getOrderTotal() {
        double total = 0;
        for (OrderLine line : orderLines) {
            total += line.getFinalTotal();
        }
        return total;
    }

    public double getTotalDiscount() {
        double discount = 0;
        for (OrderLine line : orderLines) {
            discount += line.getDiscountAmount();
        }
        return discount;
    }

    public double getOrderSubTotal() {
        double subTotal = 0;
        for (OrderLine line : orderLines) {
            subTotal += line.getSubTotal();
        }
        return subTotal;
    }

    public int getTotalItems() {
        int total = 0;
        for (OrderLine line : orderLines) {
            total += line.getQuantity();
        }
        return total;
    }

    public String getFormattedOrderTotal() {
        return String.format("$%.2f", getOrderTotal());
    }

    public String getFormattedSubTotal() {
        return String.format("$%.2f", getOrderSubTotal());
    }

    public String getFormattedTotalDiscount() {
        return String.format("$%.2f", getTotalDiscount());
    }


    @Override
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("           ORDER REPORT                 \n");
        report.append("========================================\n");
        report.append("Order ID      : ").append(getId()).append("\n");
        report.append("Supplier      : ").append(supplierName).append("\n");
        report.append("Created By    : ").append(createdByUserId).append("\n");
        report.append("Status        : ").append(status.getDisplayName()).append("\n");
        report.append("Created       : ").append(getCreatedDate()).append("\n");
        report.append("Last Updated  : ").append(lastUpdated).append("\n");
        if (!notes.isEmpty()) {
            report.append("Notes         : ").append(notes).append("\n");
        }
        report.append("----------------------------------------\n");
        report.append(String.format("  %-10s %-20s %-8s %-10s %-10s %-10s%n",
                "Prod ID", "Product Name", "Qty", "Unit Price", "Discount", "Total"));
        report.append("  ").append("-".repeat(68)).append("\n");
        for (OrderLine line : orderLines) {
            report.append(String.format("  %-10s %-20s %-8s %-10s %-10s %-10s%n",
                    line.getProductId(),
                    line.getProductName(),
                    line.getQuantity(),
                    line.getFormattedUnitPrice(),
                    line.getDiscountPercent() > 0 ? line.getDiscountPercent() + "%" : "None",
                    line.getFormattedFinalTotal()));
        }
        report.append("----------------------------------------\n");
        report.append(String.format("  %-40s %s%n", "Sub Total:", getFormattedSubTotal()));
        report.append(String.format("  %-40s %s%n", "Total Discount:", getFormattedTotalDiscount()));
        report.append(String.format("  %-40s %s%n", "Order Total:", getFormattedOrderTotal()));
        report.append(String.format("  %-40s %s%n", "Total Items:", getTotalItems()));
        report.append("========================================\n");
        return report.toString();
    }

    @Override
    public String getSummary() {
        return "Order[" + getId() + "]"
             + " | Supplier: " + supplierName
             + " | Items: " + orderLines.size()
             + " | Total: " + getFormattedOrderTotal()
             + " | Status: " + status.getDisplayName();
    }

    @Override
    public void printReport() {
        System.out.println(generateReport());
    }


    private void updateLastUpdated() {
        this.lastUpdated = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public void addNote(String note) {
        if (!notes.isEmpty()) {
            notes += " | " + note;
        } else {
            notes = note;
        }
        updateLastUpdated();
    }


    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public List<OrderLine> getOrderLines() {
        return new ArrayList<>(orderLines);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    @Override
    public void displayDetails() {
        System.out.println(generateReport());
    }

    public void displaySummary() {
        System.out.printf("%-10s %-20s %-8s %-12s %-15s %-20s%n",
                getId(),
                supplierName,
                orderLines.size(),
                getFormattedOrderTotal(),
                status.getDisplayName(),
                getCreatedDate());
    }

    public static void displayTableHeader() {
        System.out.printf("%-10s %-20s %-8s %-12s %-15s %-20s%n",
                "Order ID", "Supplier", "Items", "Total", "Status", "Created");
        System.out.println("-".repeat(85));
    }


    public String toFileString() {

        StringBuilder lineIds = new StringBuilder();
        for (int i = 0; i < orderLines.size(); i++) {
            lineIds.append(orderLines.get(i).getId());
            if (i < orderLines.size() - 1) lineIds.append("|");
        }
        return getId() + "," + getCreatedDate() + "," + supplierId + ","
             + supplierName + "," + createdByUserId + "," + lineIds + ","
             + status.name() + "," + lastUpdated + "," + notes;
    }

    public static Order fromFileString(String line, List<OrderLine> resolvedOrderLines) {
        String[] parts = line.split(",");
        return new Order(
            parts[0],
            parts[1],
            parts[2],
            parts[3],
            parts[4],
            resolvedOrderLines,
            OrderStatus.valueOf(parts[6]),
            parts[7],
            parts.length > 8 ? parts[8] : ""
        );
    }

    @Override
    public String toString() {
        return "Order[" + getId() + "] "
             + supplierName + " | "
             + orderLines.size() + " items | "
             + getFormattedOrderTotal() + " | "
             + status.getDisplayName();
    }
}
