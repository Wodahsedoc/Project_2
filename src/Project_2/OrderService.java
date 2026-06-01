package Project_2;

import java.util.ArrayList;
import java.util.List;

public class OrderService implements Manageable<Order>, Reportable {

    private final OrderDAO orderDAO;
    private final StockService stockService;


    public OrderService(StockService stockService) {
        this.orderDAO = new OrderDAO();
        this.stockService = stockService;
    }


    @Override
    public void add(Order order) {
        if (order == null) return;
        orderDAO.insert(order);
    }

    @Override
    public boolean update(Order order) {
        if (order == null) return false;
        return orderDAO.update(order);
    }

    @Override
    public boolean delete(String id) {
        return orderDAO.delete(id);
    }

    @Override
    public Order findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return orderDAO.findById(id);
    }

    @Override
    public List<Order> getAll() {
        return orderDAO.findAll();
    }


    public Order createOrder(String supplierId, String supplierName,
                             String createdByUserId) {
        Order order = new Order(supplierId, supplierName,
                createdByUserId);
        System.out.println("New order created: " + order.getId());
        return order;
    }

    public boolean addLineToOrder(Order order, String productId,
                                  String productName, int quantity,
                                  double unitPrice) {
        if (order == null) return false;
        if (!stockService.isAvailable(productId, quantity)) {
            System.out.println("Insufficient stock for: " + productName
                    + ". Available: "
                    + stockService.getQuantityByProduct(productId));
            return false;
        }
        order.addOrderLine(new OrderLine(productId, productName,
                quantity, unitPrice));
        return true;
    }

    public boolean addLineToOrderWithDiscount(Order order,
                                              String productId,
                                              String productName,
                                              int quantity,
                                              double unitPrice,
                                              double discountPercent) {
        if (order == null) return false;
        if (!stockService.isAvailable(productId, quantity)) {
            System.out.println("Insufficient stock for: "
                    + productName);
            return false;
        }
        order.addOrderLine(new OrderLine(productId, productName,
                quantity, unitPrice, discountPercent));
        return true;
    }

    public boolean submitOrder(Order order) {
        if (order == null) {
            System.out.println("Order cannot be null.");
            return false;
        }
        if (order.getOrderLines().isEmpty()) {
            System.out.println("Cannot submit an empty order.");
            return false;
        }

        for (OrderLine line : order.getOrderLines()) {
            if (!stockService.isAvailable(line.getProductId(),
                    line.getQuantity())) {
                System.out.println("Stock check failed for: "
                        + line.getProductName()
                        + ". Available: "
                        + stockService.getQuantityByProduct(
                                line.getProductId()));
                return false;
            }
        }

        for (OrderLine line : order.getOrderLines()) {
            stockService.reduceStock(line.getProductId(),
                    line.getQuantity());
        }
        try {
            order.confirm();
            orderDAO.insert(order);
            System.out.println("Order " + order.getId()
                    + " submitted successfully.");
            System.out.println("Total: "
                    + order.getFormattedOrderTotal());
            return true;
        } catch (IllegalStateException e) {
            System.out.println("Submit failed: " + e.getMessage());
            return false;
        }
    }


    public boolean processOrder(String id) {
        Order order = orderDAO.findById(id);
        if (order == null) {
            System.out.println("Order not found: " + id);
            return false;
        }
        try {
            order.process();
            orderDAO.update(order);
            return true;
        } catch (IllegalStateException e) {
            System.out.println("Cannot process order: "
                    + e.getMessage());
            return false;
        }
    }

    public boolean deliverOrder(String id) {
        Order order = orderDAO.findById(id);
        if (order == null) {
            System.out.println("Order not found: " + id);
            return false;
        }
        try {
            order.deliver();
            orderDAO.update(order);
            System.out.println("Order " + id
                    + " marked as delivered.");
            return true;
        } catch (IllegalStateException e) {
            System.out.println("Cannot deliver order: "
                    + e.getMessage());
            return false;
        }
    }

    public boolean cancelOrder(String id) {
        Order order = orderDAO.findById(id);
        if (order == null) {
            System.out.println("Order not found: " + id);
            return false;
        }
        try {
            if (!order.isPending()) {
                for (OrderLine line : order.getOrderLines()) {
                    stockService.restockProduct(line.getProductId(),
                            line.getQuantity());
                }
                System.out.println(
                        "Stock restored for cancelled order.");
            }
            order.cancel();
            orderDAO.update(order);
            return true;
        } catch (IllegalStateException e) {
            System.out.println("Cannot cancel order: "
                    + e.getMessage());
            return false;
        }
    }


    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderDAO.findByStatus(status);
    }

    public List<Order> findBySupplier(String supplierId) {
        return orderDAO.findBySupplier(supplierId);
    }

    public List<Order> getPendingOrders() {
        return orderDAO.findByStatus(Order.OrderStatus.PENDING);
    }

    public List<Order> getConfirmedOrders() {
        return orderDAO.findByStatus(Order.OrderStatus.CONFIRMED);
    }

    public List<Order> getProcessingOrders() {
        return orderDAO.findByStatus(Order.OrderStatus.PROCESSING);
    }

    public List<Order> getDeliveredOrders() {
        return orderDAO.findByStatus(Order.OrderStatus.DELIVERED);
    }

    public List<Order> getCancelledOrders() {
        return orderDAO.findByStatus(Order.OrderStatus.CANCELLED);
    }


    public int getTotalOrders() {
        return orderDAO.countTotal();
    }

    public double getTotalRevenue() {
        return orderDAO.getTotalRevenue();
    }

    public String getFormattedTotalRevenue() {
        return String.format("$%.2f", getTotalRevenue());
    }

    public double getAverageOrderValue() {
        List<Order> active = new ArrayList<>();
        for (Order o : orderDAO.findAll()) {
            if (!o.isCancelled()) active.add(o);
        }
        if (active.isEmpty()) return 0;
        double total = 0;
        for (Order o : active) total += o.getOrderTotal();
        return total / active.size();
    }

    public Order getHighestValueOrder() {
        List<Order> all = orderDAO.findAll();
        if (all.isEmpty()) return null;
        Order highest = all.get(0);
        for (Order o : all) {
            if (o.getOrderTotal() > highest.getOrderTotal()) {
                highest = o;
            }
        }
        return highest;
    }

    public double getRevenueBySupplier(String supplierId) {
        double total = 0;
        for (Order order : orderDAO.findBySupplier(supplierId)) {
            if (!order.isCancelled()) {
                total += order.getOrderTotal();
            }
        }
        return total;
    }


    public void displayAllOrders() {
        List<Order> orders = orderDAO.findAll();
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        Order.displayTableHeader();
        for (Order order : orders) {
            order.displaySummary();
        }
        System.out.println("\nTotal orders: " + orders.size());
    }

    public void displayOrderDetails(String id) {
        Order order = findById(id);
        if (order == null) {
            System.out.println("Order not found: " + id);
            return;
        }
        order.displayDetails();
    }

    public void displayOrdersByStatus(Order.OrderStatus status) {
        List<Order> filtered = findByStatus(status);
        if (filtered.isEmpty()) {
            System.out.println("No " + status.getDisplayName()
                    + " orders found.");
            return;
        }
        Order.displayTableHeader();
        for (Order order : filtered) {
            order.displaySummary();
        }
        System.out.println("Total: " + filtered.size());
    }

    public void displayStats() {
        System.out.println("===== Order Statistics =====");
        System.out.println("Total Orders      : " + getTotalOrders());
        System.out.println("Pending           : "
                + getPendingOrders().size());
        System.out.println("Confirmed         : "
                + getConfirmedOrders().size());
        System.out.println("Processing        : "
                + getProcessingOrders().size());
        System.out.println("Delivered         : "
                + getDeliveredOrders().size());
        System.out.println("Cancelled         : "
                + getCancelledOrders().size());
        System.out.println("Total Revenue     : "
                + getFormattedTotalRevenue());
        System.out.printf("Average Order     : $%.2f%n",
                getAverageOrderValue());
        Order highest = getHighestValueOrder();
        if (highest != null) {
            System.out.println("Highest Order     : "
                    + highest.getId()
                    + " (" + highest.getFormattedOrderTotal() + ")");
        }
        System.out.println("============================");
    }


    @Override
    public String generateReport() {
        List<Order> orders = orderDAO.findAll();
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("        FULL ORDER REPORT               \n");
        report.append("========================================\n");
        report.append(String.format("Total Orders     : %d%n",
                getTotalOrders()));
        report.append(String.format("Pending          : %d%n",
                getPendingOrders().size()));
        report.append(String.format("Confirmed        : %d%n",
                getConfirmedOrders().size()));
        report.append(String.format("Delivered        : %d%n",
                getDeliveredOrders().size()));
        report.append(String.format("Cancelled        : %d%n",
                getCancelledOrders().size()));
        report.append(String.format("Total Revenue    : %s%n",
                getFormattedTotalRevenue()));
        report.append("----------------------------------------\n");
        report.append("ORDER DETAILS:\n");
        report.append("----------------------------------------\n");
        if (orders.isEmpty()) {
            report.append("No orders found.\n");
        } else {
            for (Order order : orders) {
                report.append(order.getSummary()).append("\n");
            }
        }
        report.append("========================================\n");
        return report.toString();
    }

    @Override
    public String getSummary() {
        return String.format(
            "Orders: %d total | Pending: %d | Delivered: %d"
            + " | Revenue: %s",
            getTotalOrders(),
            getPendingOrders().size(),
            getDeliveredOrders().size(),
            getFormattedTotalRevenue()
        );
    }

    @Override
    public void printReport() {
        System.out.println(generateReport());
    }

    public String generateLowStockReport() {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("        LOW STOCK REPORT                \n");
        report.append("========================================\n");
        List<StockItem> lowStock = stockService.getLowStockItems();
        if (lowStock.isEmpty()) {
            report.append("No low stock items found.\n");
        } else {
            report.append(String.format(
                    "%-10s %-10s %-10s %-10s %-15s%n",
                    "Stock ID", "Prod ID", "Current",
                    "Min", "Recommended"));
            report.append("-".repeat(55)).append("\n");
            for (StockItem item : lowStock) {
                report.append(String.format(
                        "%-10s %-10s %-10s %-10s %-15s%n",
                        item.getId(),
                        item.getProductId(),
                        item.getQuantityInStock(),
                        item.getMinimumThreshold(),
                        item.getRestockRecommendation() + " units"));
            }
        }
        report.append("========================================\n");
        report.append("Total low stock items: ")
                .append(lowStock.size()).append("\n");
        report.append("========================================\n");
        return report.toString();
    }

    public String generateOrderReport(String id) {
        Order order = findById(id);
        if (order == null) return "Order not found with ID: " + id;
        return order.generateReport();
    }

    @Override
    public String toString() {
        return "OrderService[Total Orders: "
                + orderDAO.countTotal() + "]";
    }
}
