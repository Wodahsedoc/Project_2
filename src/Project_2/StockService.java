package Project_2;

import java.util.ArrayList;
import java.util.List;

public class StockService implements Manageable<StockItem> {

    private final StockDAO stockDAO;
    private final ProductService productService;


    public StockService(ProductService productService) {
        this.stockDAO = new StockDAO();
        this.productService = productService;
        if (stockDAO.findAll().isEmpty()) {
            seedSampleStock();
        }
    }


    @Override
    public void add(StockItem stockItem) {
        if (stockItem == null) {
            System.out.println("Cannot add a null stock item.");
            return;
        }
        if (stockDAO.existsForProduct(stockItem.getProductId())) {
            System.out.println("Stock item already exists for product ID: "
                             + stockItem.getProductId()
                             + ". Use restock to update quantity.");
            return;
        }
        stockDAO.insert(stockItem);
        System.out.println("Stock item created for product ID: "
                         + stockItem.getProductId());
    }

    @Override
    public boolean update(StockItem updatedItem) {
        if (updatedItem == null) return false;
        boolean updated = stockDAO.update(updatedItem);
        if (updated) {
            System.out.println("Stock item updated for product ID: "
                             + updatedItem.getProductId());
        } else {
            System.out.println("Stock item not found with ID: "
                             + updatedItem.getId());
        }
        return updated;
    }

    @Override
    public boolean delete(String id) {
        boolean deleted = stockDAO.delete(id);
        if (deleted) {
            System.out.println("Stock item deleted successfully.");
        } else {
            System.out.println("Stock item not found with ID: " + id);
        }
        return deleted;
    }

    @Override
    public StockItem findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return stockDAO.findById(id);
    }

    @Override
    public List<StockItem> getAll() {
        return stockDAO.findAll();
    }


    public boolean restockProduct(String productId, int quantity) {
        StockItem item = getStockByProduct(productId);
        if (item == null) {
            System.out.println("No stock record found for product ID: "
                             + productId);
            return false;
        }
        try {
            item.restock(quantity);
            stockDAO.update(item);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Restock failed: " + e.getMessage());
            return false;
        }
    }

    public boolean reduceStock(String productId, int quantity) {
        StockItem item = getStockByProduct(productId);
        if (item == null) {
            System.out.println("No stock record found for product ID: "
                             + productId);
            return false;
        }
        boolean reduced = item.reduceStock(quantity);
        if (reduced) {
            stockDAO.update(item);
            if (item.isLowStock()) {
                System.out.println("⚠ WARNING: Stock for product "
                        + productId + " is now low!");
                item.displayLowStockAlert();
            }
        }
        return reduced;
    }


    public StockItem getStockByProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) return null;
        return stockDAO.findByProductId(productId);
    }

    public int getQuantityByProduct(String productId) {
        StockItem item = getStockByProduct(productId);
        return item != null ? item.getQuantityInStock() : 0;
    }

    public String getStatusByProduct(String productId) {
        StockItem item = getStockByProduct(productId);
        return item != null ? item.getStockStatus() : "NO RECORD";
    }

    public boolean isAvailable(String productId,
                                int requestedQuantity) {
        StockItem item = getStockByProduct(productId);
        if (item == null) return false;
        return item.getQuantityInStock() >= requestedQuantity;
    }


    public List<StockItem> getLowStockItems() {
        return stockDAO.findLowStock();
    }

    public List<StockItem> getOutOfStockItems() {
        return stockDAO.findOutOfStock();
    }

    public List<StockItem> getFullyStockedItems() {
        return stockDAO.findFullyStocked();
    }

    public boolean hasLowStockAlerts() {
        return stockDAO.countLowStock() > 0;
    }


    public boolean updateMinimumThreshold(String productId,
                                          int newThreshold) {
        StockItem item = getStockByProduct(productId);
        if (item == null) {
            System.out.println("No stock record found for product ID: "
                             + productId);
            return false;
        }
        try {
            item.setMinimumThreshold(newThreshold);
            stockDAO.update(item);
            System.out.println("Minimum threshold updated to "
                    + newThreshold + " for product: " + productId);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid threshold: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMaximumCapacity(String productId,
                                         int newCapacity) {
        StockItem item = getStockByProduct(productId);
        if (item == null) {
            System.out.println("No stock record found for product ID: "
                             + productId);
            return false;
        }
        try {
            item.setMaximumCapacity(newCapacity);
            stockDAO.update(item);
            System.out.println("Maximum capacity updated to "
                    + newCapacity + " for product: " + productId);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid capacity: " + e.getMessage());
            return false;
        }
    }


    public void displayRestockRecommendations() {
        List<StockItem> lowStock = getLowStockItems();
        if (lowStock.isEmpty()) {
            System.out.println("All stock levels are healthy. "
                             + "No restocking needed.");
            return;
        }
        System.out.println("===== RESTOCK RECOMMENDATIONS =====");
        System.out.printf("%-10s %-10s %-10s %-10s %-15s%n",
                "Stock ID", "Prod ID", "Current", "Min",
                "Recommended");
        System.out.println("-".repeat(55));
        for (StockItem item : lowStock) {
            System.out.printf("%-10s %-10s %-10s %-10s %-15s%n",
                    item.getId(),
                    item.getProductId(),
                    item.getQuantityInStock(),
                    item.getMinimumThreshold(),
                    item.getRestockRecommendation() + " units");
        }
        System.out.println("====================================");
        System.out.println("Total items needing restock: "
                + lowStock.size());
    }


    public boolean stockExistsForProduct(String productId) {
        return stockDAO.existsForProduct(productId);
    }


    public int getTotalStockItems() {
        return stockDAO.findAll().size();
    }

    public int getTotalLowStockCount() {
        return stockDAO.countLowStock();
    }

    public int getTotalOutOfStockCount() {
        return stockDAO.countOutOfStock();
    }

    public int getTotalUnitsInStock() {
        return stockDAO.getTotalUnits();
    }

    public double getTotalStockValue(ProductService productService) {
        double total = 0;
        for (StockItem item : stockDAO.findAll()) {
            Product product = productService.findById(
                    item.getProductId());
            if (product != null) {
                total += product.getPrice() * item.getQuantityInStock();
            } else {
                System.out.println(
                        "Warning: No product found for stock item: "
                        + item.getProductId());
            }
        }
        return total;
    }


    private void seedSampleStock() {
        System.out.println("Seeding sample stock...");
        List<Product> products = productService.getAll();
        if (products.isEmpty()) {
            System.out.println("No products found to seed stock for.");
            return;
        }

        int[][] stockData = {
            {45, 10, 100},
            {120, 20, 200},
            {80, 15, 150},
            {60, 10, 100},
            {35, 10, 100},
            {5,  10, 100}
        };

        for (int i = 0; i < products.size()
                && i < stockData.length; i++) {
            add(new StockItem(
                products.get(i).getId(),
                stockData[i][0],
                stockData[i][1],
                stockData[i][2]
            ));
        }
        System.out.println("Sample stock loaded.");
    }


    public void displayAllStock() {
        List<StockItem> items = stockDAO.findAll();
        if (items.isEmpty()) {
            System.out.println("No stock records found.");
            return;
        }
        StockItem.displayTableHeader();
        for (StockItem item : items) {
            Product p = productService.findById(item.getProductId());
            String name = (p != null) ? p.getName() : "(unknown)";
            item.displaySummary(name);
        }
        System.out.println("\nTotal stock records: " + items.size());
    }

    public void displayLowStockAlerts() {
        List<StockItem> lowStock = getLowStockItems();
        if (lowStock.isEmpty()) {
            System.out.println(
                    "No low stock alerts. All levels are healthy.");
            return;
        }
        System.out.println("========== LOW STOCK ALERTS ==========");
        for (StockItem item : lowStock) {
            item.displayLowStockAlert();
            System.out.println();
        }
        System.out.println("Total alerts: " + lowStock.size());
        System.out.println("======================================");
    }

    public void displayOutOfStockItems() {
        List<StockItem> outOfStock = getOutOfStockItems();
        if (outOfStock.isEmpty()) {
            System.out.println("No out of stock items.");
            return;
        }
        System.out.println("========== OUT OF STOCK ==========");
        StockItem.displayTableHeader();
        for (StockItem item : outOfStock) {
            Product p = productService.findById(item.getProductId());
            String name = (p != null) ? p.getName() : "(unknown)";
            item.displaySummary(name);
        }
        System.out.println("Total out of stock: "
                + outOfStock.size());
        System.out.println("==================================");
    }

    public void displayStats(ProductService productService) {
        System.out.println("===== Stock Statistics =====");
        System.out.println("Total Stock Records : "
                + getTotalStockItems());
        System.out.println("Low Stock Items     : "
                + getTotalLowStockCount());
        System.out.println("Out of Stock        : "
                + getTotalOutOfStockCount());
        System.out.println("Fully Stocked       : "
                + getFullyStockedItems().size());
        System.out.println("Total Units         : "
                + getTotalUnitsInStock());
        System.out.printf("Total Stock Value   : $%.2f%n",
                getTotalStockValue(productService));
        System.out.println("============================");
    }

    @Override
    public String toString() {
        return "StockService[Total Stock Records: "
                + stockDAO.findAll().size() + "]";
    }
}
