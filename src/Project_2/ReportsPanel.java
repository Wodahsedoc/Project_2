package Project_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final OrderService orderService;
    private final StockService stockService;
    private final ProductService productService;
    private final Session session;
    private final SupplierService supplierService;
    private final FileHandler<Order> fileHandler;


    private static final Color ACCENT     = new Color(30, 80, 140);
    private static final Color CONTENT_BG = new Color(245, 247, 250);
    private static final Color BTN_GREEN  = new Color(40, 140, 80);
    private static final Color BTN_ORANGE = new Color(200, 100, 0);
    private static final Color BTN_RED    = new Color(180, 40, 40);
    private static final Color BTN_GREY   = new Color(60, 60, 60);
    private static final Color BTN_PURPLE = new Color(120, 40, 140);


    public ReportsPanel(OrderService orderService,
                        StockService stockService,
                        ProductService productService,
                        SupplierService supplierService,
                        Session session) {
        this.orderService   = orderService;
        this.stockService   = stockService;
        this.productService = productService;
        this.session        = session;
        this.supplierService = supplierService;
        this.fileHandler    = new FileHandler<>();

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        buildTopBar();
        buildContent();
    }


    private void buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("📄 Reports");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(ACCENT);
        topBar.add(title, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);
    }


    private void buildContent() {
        JPanel content = new JPanel(new GridLayout(2, 2, 15, 15));
        content.setBackground(CONTENT_BG);

        content.add(buildReportCard(
            "📋 Full Order Report",
            "View a complete summary of all orders including "
            + "status breakdown and total revenue.",
            "View Report", BTN_PURPLE,
            e -> showFullOrderReport()
        ));

        content.add(buildReportCard(
            "⚠ Low Stock Report",
            "View all products that are below their "
            + "minimum stock threshold and need restocking.",
            "View Report", BTN_ORANGE,
            e -> showLowStockReport()
        ));

        content.add(buildReportCard(
            "📦 Inventory Summary",
            "View a complete summary of all products, "
            + "stock levels, values and categories.",
            "View Report", ACCENT,
            e -> showInventorySummary()
        ));

        content.add(buildReportCard(
            "💰 Revenue Report",
            "View revenue breakdown by supplier "
            + "and order statistics.",
            "View Report", BTN_GREEN,
            e -> showRevenueReport()
        ));

        add(content, BorderLayout.CENTER);


        JPanel exportPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10));
        exportPanel.setBackground(new Color(235, 238, 242));
        exportPanel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(200, 200, 200)));

        JLabel exportLabel = new JLabel("Export Reports:");
        exportLabel.setFont(new Font("Arial", Font.BOLD, 13));
        exportLabel.setForeground(new Color(80, 80, 80));
        exportPanel.add(exportLabel);

        JButton exportOrderBtn = createButton(
                "Export Order Report", BTN_PURPLE);
        exportOrderBtn.addActionListener(
                e -> handleExportOrderReport());
        exportPanel.add(exportOrderBtn);

        JButton exportLowStockBtn = createButton(
                "Export Low Stock Report", BTN_ORANGE);
        exportLowStockBtn.addActionListener(
                e -> handleExportLowStockReport());
        exportPanel.add(exportLowStockBtn);

        JButton exportInventoryBtn = createButton(
                "Export Inventory Report", ACCENT);
        exportInventoryBtn.addActionListener(
                e -> handleExportInventoryReport());
        exportPanel.add(exportInventoryBtn);

        add(exportPanel, BorderLayout.SOUTH);
    }

    private JPanel buildReportCard(String title, String description,
                                    String btnText, Color btnColor,
                                    ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(ACCENT);
        card.add(titleLabel, BorderLayout.NORTH);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setForeground(new Color(100, 100, 100));
        descArea.setBackground(Color.WHITE);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        card.add(descArea, BorderLayout.CENTER);

        JButton btn = createButton(btnText, btnColor);
        btn.addActionListener(action);
        card.add(btn, BorderLayout.SOUTH);

        return card;
    }


    private void showFullOrderReport() {
        String report = orderService.generateReport();
        showReportDialog("Full Order Report", report);
    }

    private void showLowStockReport() {
        String report = orderService.generateLowStockReport();
        showReportDialog("Low Stock Report", report);
    }

    private void showInventorySummary() {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("         INVENTORY SUMMARY REPORT       \n");
        report.append("========================================\n");
        report.append(String.format("Total Products   : %d%n",
                productService.getTotalProducts()));
        report.append(String.format("Available        : %d%n",
                productService.getTotalAvailable()));
        report.append(String.format("Unavailable      : %d%n",
                productService.getUnavailableProducts().size()));
        report.append(String.format("Average Price    : $%.2f%n",
                productService.getAveragePrice()));

        Product most = productService.getMostExpensive();
        Product cheap = productService.getCheapest();
        if (most != null) {
            report.append(String.format("Most Expensive   : %s (%s)%n",
                    most.getName(), most.getFormattedPrice()));
        }
        if (cheap != null) {
            report.append(String.format("Cheapest         : %s (%s)%n",
                    cheap.getName(), cheap.getFormattedPrice()));
        }

        report.append("----------------------------------------\n");
        report.append("STOCK LEVELS:\n");
        report.append("----------------------------------------\n");
        report.append(String.format("Total Stock Records : %d%n",
                stockService.getTotalStockItems()));
        report.append(String.format("Low Stock Items     : %d%n",
                stockService.getTotalLowStockCount()));
        report.append(String.format("Out of Stock        : %d%n",
                stockService.getTotalOutOfStockCount()));
        report.append(String.format("Total Units         : %d%n",
                stockService.getTotalUnitsInStock()));
        report.append(String.format("Total Stock Value   : $%.2f%n",
                stockService.getTotalStockValue(productService)));

        report.append("----------------------------------------\n");
        report.append("PRODUCTS BY CATEGORY:\n");
        report.append("----------------------------------------\n");
        for (Category cat : Category.values()) {
            List<Product> catProducts =
                    productService.filterByCategory(cat);
            if (!catProducts.isEmpty()) {
                report.append(String.format("%-15s : %d products%n",
                        cat.getDisplayName(), catProducts.size()));
            }
        }
        report.append("========================================\n");

        showReportDialog("Inventory Summary", report.toString());
    }

    private void showRevenueReport() {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("           REVENUE REPORT               \n");
        report.append("========================================\n");
        report.append(String.format("Total Revenue    : %s%n",
                orderService.getFormattedTotalRevenue()));
        report.append(String.format("Total Orders     : %d%n",
                orderService.getTotalOrders()));
        report.append(String.format("Average Order    : $%.2f%n",
                orderService.getAverageOrderValue()));

        Order highest = orderService.getHighestValueOrder();
        if (highest != null) {
            report.append(String.format(
                    "Highest Order    : %s (%s)%n",
                    highest.getId(),
                    highest.getFormattedOrderTotal()));
        }

        report.append("----------------------------------------\n");
        report.append("REVENUE BY SUPPLIER:\n");
        report.append("----------------------------------------\n");
        for (Supplier s : supplierService.getAll()) {
            double revenue = orderService
                    .getRevenueBySupplier(s.getId());
            if (revenue > 0) {
                report.append(String.format("%-25s : $%.2f%n",
                        s.getSupplierName(), revenue));
            }
        }

        report.append("----------------------------------------\n");
        report.append("ORDER STATUS BREAKDOWN:\n");
        report.append("----------------------------------------\n");
        report.append(String.format("Pending    : %d%n",
                orderService.getPendingOrders().size()));
        report.append(String.format("Confirmed  : %d%n",
                orderService.getConfirmedOrders().size()));
        report.append(String.format("Processing : %d%n",
                orderService.findByStatus(
                Order.OrderStatus.PROCESSING).size()));
        report.append(String.format("Delivered  : %d%n",
                orderService.getDeliveredOrders().size()));
        report.append(String.format("Cancelled  : %d%n",
                orderService.getCancelledOrders().size()));
        report.append("========================================\n");

        showReportDialog("Revenue Report", report.toString());
    }

    private void showReportDialog(String title, String content) {
        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), title, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());


        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);


        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(245, 247, 250));

        JButton exportBtn = createButton("Export to File", BTN_GREEN);
        exportBtn.addActionListener(e -> {
            fileHandler.exportReport(content,
                    title.replace(" ", "_"));
            JOptionPane.showMessageDialog(dialog,
                    "Report exported to data/reports/ folder.",
                    "Exported",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeBtn = createButton("Close", BTN_GREY);
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(exportBtn);
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }


    private void handleExportOrderReport() {
        fileHandler.exportReport(
                orderService.generateReport(), "FullOrderReport");
        JOptionPane.showMessageDialog(this,
                "Order report exported to data/reports/ folder.",
                "Exported", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleExportLowStockReport() {
        fileHandler.exportReport(
                orderService.generateLowStockReport(),
                "LowStockReport");
        JOptionPane.showMessageDialog(this,
                "Low stock report exported to data/reports/ folder.",
                "Exported", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleExportInventoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("INVENTORY SUMMARY REPORT\n");
        report.append("Total Products : ")
                .append(productService.getTotalProducts()).append("\n");
        report.append("Available      : ")
                .append(productService.getTotalAvailable()).append("\n");
        report.append("Total Units    : ")
                .append(stockService.getTotalUnitsInStock()).append("\n");
        report.append(String.format("Stock Value    : $%.2f%n",
                stockService.getTotalStockValue(productService)));

        fileHandler.exportReport(report.toString(),
                "InventorySummaryReport");
        JOptionPane.showMessageDialog(this,
                "Inventory report exported to data/reports/ folder.",
                "Exported", JOptionPane.INFORMATION_MESSAGE);
    }


    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });
        return btn;
    }
}