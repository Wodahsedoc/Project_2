package Project_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class StockPanel extends JPanel {

    private final StockService stockService;
    private final ProductService productService;
    private final Session session;


    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;


    private JComboBox<String> statusFilter;
    private JTextField searchField;
    private JButton searchBtn;
    private JButton clearBtn;


    private JButton restockBtn;
    private JButton reduceBtn;
    private JButton updateThresholdBtn;
    private JButton updateCapacityBtn;
    private JButton refreshBtn;


    private static final Color ACCENT       = new Color(30, 80, 140);
    private static final Color CONTENT_BG   = new Color(245, 247, 250);
    private static final Color BTN_GREEN    = new Color(40, 140, 80);
    private static final Color BTN_ORANGE   = new Color(200, 100, 0);
    private static final Color BTN_RED      = new Color(180, 40, 40);
    private static final Color BTN_GREY     = new Color(60, 60, 60);


    private static final String[] COLUMNS = {
        "Stock ID", "Product ID", "Product Name",
        "Qty In Stock", "Min Threshold", "Max Capacity",
        "Remaining Cap", "Status", "Last Restocked"
    };


    public StockPanel(StockService stockService,
                      ProductService productService,
                      Session session) {
        this.stockService   = stockService;
        this.productService = productService;
        this.session        = session;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        buildTopBar();
        buildSummaryBar();
        buildTable();
        buildButtonBar();

        loadStock();
    }


    private void buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));


        JLabel title = new JLabel("📊 Stock Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(ACCENT);
        topBar.add(title, BorderLayout.WEST);


        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(CONTENT_BG);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleSearch();
            }
        });
        searchPanel.add(searchField);


        String[] statuses = {"All Stock", "In Stock",
                "Low Stock", "Out of Stock", "Fully Stocked"};
        statusFilter = new JComboBox<>(statuses);
        statusFilter.setFont(new Font("Arial", Font.PLAIN, 13));
        statusFilter.addActionListener(e -> handleSearch());
        searchPanel.add(statusFilter);

        searchBtn = createButton("Search", ACCENT);
        searchBtn.addActionListener(e -> handleSearch());
        searchPanel.add(searchBtn);

        clearBtn = createButton("Clear", BTN_GREY);
        clearBtn.addActionListener(e -> handleClear());
        searchPanel.add(clearBtn);

        topBar.add(searchPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
    }


    private void buildSummaryBar() {
        JPanel summaryBar = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryBar.setBackground(CONTENT_BG);
        summaryBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        summaryBar.add(buildSummaryCard("Total Records",
                String.valueOf(stockService.getTotalStockItems()),
                ACCENT));
        summaryBar.add(buildSummaryCard("Low Stock",
                String.valueOf(stockService.getTotalLowStockCount()),
                BTN_ORANGE));
        summaryBar.add(buildSummaryCard("Out of Stock",
                String.valueOf(stockService.getTotalOutOfStockCount()),
                BTN_RED));
        summaryBar.add(buildSummaryCard("Total Units",
                String.valueOf(stockService.getTotalUnitsInStock()),
                BTN_GREEN));

        add(summaryBar, BorderLayout.NORTH);


        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.setBackground(CONTENT_BG);

        JPanel topBar = (JPanel) getComponent(0);
        remove(topBar);
        northWrapper.add(topBar, BorderLayout.NORTH);
        northWrapper.add(summaryBar, BorderLayout.SOUTH);
        add(northWrapper, BorderLayout.NORTH);
    }

    private JPanel buildSummaryCard(String label,
                                     String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComp.setForeground(new Color(120, 120, 120));
        card.add(labelComp, BorderLayout.SOUTH);

        return card;
    }


    private void buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        stockTable = new JTable(tableModel);
        stockTable.setFont(new Font("Arial", Font.PLAIN, 13));
        stockTable.setRowHeight(30);
        stockTable.setGridColor(new Color(230, 230, 230));
        stockTable.setSelectionBackground(new Color(210, 230, 255));
        stockTable.setSelectionForeground(Color.BLACK);
        stockTable.setShowVerticalLines(false);
        stockTable.setFillsViewportHeight(true);


        JTableHeader header = stockTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);


        stockTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        stockTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        stockTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        stockTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        stockTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        stockTable.getColumnModel().getColumn(8).setPreferredWidth(140);


        stockTable.getColumnModel().getColumn(7)
                .setCellRenderer(new StockStatusRenderer());


        stockTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(220, 220, 220)));
        add(scrollPane, BorderLayout.CENTER);
    }


    private void buildButtonBar() {
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonBar.setBackground(new Color(235, 238, 242));
        buttonBar.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(200, 200, 200)));

        refreshBtn = createButton("🔄 Refresh", BTN_GREY);
        refreshBtn.addActionListener(e -> loadStock());
        buttonBar.add(refreshBtn);

        if (session.isAdminOrStaff()) {
            restockBtn = createButton("➕ Restock", BTN_GREEN);
            restockBtn.addActionListener(e -> handleRestock());
            buttonBar.add(restockBtn);

            reduceBtn = createButton("➖ Reduce Stock", BTN_ORANGE);
            reduceBtn.addActionListener(e -> handleReduceStock());
            buttonBar.add(reduceBtn);

            updateThresholdBtn = createButton("📉 Update Threshold", ACCENT);
            updateThresholdBtn.addActionListener(
                    e -> handleUpdateThreshold());
            buttonBar.add(updateThresholdBtn);

            updateCapacityBtn = createButton("📈 Update Capacity", ACCENT);
            updateCapacityBtn.addActionListener(
                    e -> handleUpdateCapacity());
            buttonBar.add(updateCapacityBtn);
        }

        add(buttonBar, BorderLayout.SOUTH);
    }


    private void loadStock() {
        tableModel.setRowCount(0);
        List<StockItem> items = stockService.getAll();
        for (StockItem item : items) {
            Product product = productService.findById(item.getProductId());
            String productName = product != null
                    ? product.getName() : "Unknown";
            tableModel.addRow(new Object[]{
                item.getId(),
                item.getProductId(),
                productName,
                item.getQuantityInStock(),
                item.getMinimumThreshold(),
                item.getMaximumCapacity(),
                item.getRemainingCapacity(),
                item.getStockStatus(),
                item.getLastRestocked()
            });
        }
    }

    private void loadStock(List<StockItem> items) {
        tableModel.setRowCount(0);
        for (StockItem item : items) {
            Product product = productService.findById(item.getProductId());
            String productName = product != null
                    ? product.getName() : "Unknown";
            tableModel.addRow(new Object[]{
                item.getId(),
                item.getProductId(),
                productName,
                item.getQuantityInStock(),
                item.getMinimumThreshold(),
                item.getMaximumCapacity(),
                item.getRemainingCapacity(),
                item.getStockStatus(),
                item.getLastRestocked()
            });
        }
    }


    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        List<StockItem> results = stockService.getAll();


        if (selectedStatus != null
                && !selectedStatus.equals("All Stock")) {
            switch (selectedStatus) {
                case "Low Stock":
                    results = stockService.getLowStockItems();
                    break;
                case "Out of Stock":
                    results = stockService.getOutOfStockItems();
                    break;
                case "Fully Stocked":
                    results = stockService.getFullyStockedItems();
                    break;
                case "In Stock":
                    results.removeIf(i -> i.isLowStock()
                            || i.isOutOfStock());
                    break;
            }
        }


        if (!keyword.isEmpty()) {
            results.removeIf(item -> {
                Product p = productService.findById(item.getProductId());
                String name = p != null
                        ? p.getName().toLowerCase() : "";
                return !name.contains(keyword)
                        && !item.getProductId().toLowerCase()
                        .contains(keyword);
            });
        }

        loadStock(results);
    }

    private void handleClear() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        loadStock();
    }


    private void handleRestock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a stock item to restock.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stockId    = (String) tableModel.getValueAt(selectedRow, 0);
        String productId  = (String) tableModel.getValueAt(selectedRow, 1);
        String productName = (String) tableModel.getValueAt(selectedRow, 2);
        int currentQty    = (int) tableModel.getValueAt(selectedRow, 3);
        int remaining     = (int) tableModel.getValueAt(selectedRow, 6);

        StockItem item = stockService.findById(stockId);
        if (item == null) return;

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Product:"));
        form.add(new JLabel(productName));
        form.add(new JLabel("Current Stock:"));
        form.add(new JLabel(String.valueOf(currentQty)));
        form.add(new JLabel("Remaining Capacity:"));
        form.add(new JLabel(String.valueOf(remaining)));
        form.add(new JLabel("Recommended Qty:"));
        form.add(new JLabel(
                item.getRestockRecommendation() + " units"));
        form.add(new JLabel("Restock Quantity:"));
        JTextField qtyField = new JTextField(
                String.valueOf(item.getRestockRecommendation()));
        form.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Restock — " + productName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (stockService.restockProduct(productId, qty)) {
                    loadStock();
                    JOptionPane.showMessageDialog(this,
                            "Successfully restocked " + qty
                            + " units of " + productName,
                            "Restock Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid quantity.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleReduceStock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a stock item to reduce.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stockId     = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 2);
        String productId   = (String) tableModel.getValueAt(selectedRow, 1);
        int currentQty     = (int) tableModel.getValueAt(selectedRow, 3);

        StockItem item = stockService.findById(stockId);
        if (item == null) return;

        if (item.isOutOfStock()) {
            JOptionPane.showMessageDialog(this,
                    productName + " is already out of stock.",
                    "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Product:"));
        form.add(new JLabel(productName));
        form.add(new JLabel("Current Stock:"));
        form.add(new JLabel(String.valueOf(currentQty)));
        form.add(new JLabel("Reduce by:"));
        JTextField qtyField = new JTextField("1");
        form.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Reduce Stock — " + productName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (stockService.reduceStock(productId, qty)) {
                    loadStock();
                    JOptionPane.showMessageDialog(this,
                            "Stock reduced by " + qty
                            + " units for " + productName,
                            "Stock Reduced",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid quantity.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdateThreshold() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a stock item.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stockId     = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 2);
        String productId   = (String) tableModel.getValueAt(selectedRow, 1);
        int currentThresh  = (int) tableModel.getValueAt(selectedRow, 4);

        String input = JOptionPane.showInputDialog(this,
                "Current minimum threshold for " + productName
                + ": " + currentThresh
                + "\nEnter new minimum threshold:",
                "Update Threshold", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int newThreshold = Integer.parseInt(input.trim());
                if (stockService.updateMinimumThreshold(
                        productId, newThreshold)) {
                    loadStock();
                    JOptionPane.showMessageDialog(this,
                            "Minimum threshold updated to "
                            + newThreshold,
                            "Updated",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdateCapacity() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a stock item.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stockId     = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 2);
        String productId   = (String) tableModel.getValueAt(selectedRow, 1);
        int currentCap     = (int) tableModel.getValueAt(selectedRow, 5);

        String input = JOptionPane.showInputDialog(this,
                "Current maximum capacity for " + productName
                + ": " + currentCap
                + "\nEnter new maximum capacity:",
                "Update Capacity", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int newCapacity = Integer.parseInt(input.trim());
                if (stockService.updateMaximumCapacity(
                        productId, newCapacity)) {
                    loadStock();
                    JOptionPane.showMessageDialog(this,
                            "Maximum capacity updated to "
                            + newCapacity,
                            "Updated",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewDetails() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) return;

        String stockId     = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 2);
        StockItem item = stockService.findById(stockId);
        if (item == null) return;

        StringBuilder details = new StringBuilder();
        details.append("Stock Details\n");
        details.append("═══════════════════════════\n");
        details.append("Stock ID      : ").append(item.getId()).append("\n");
        details.append("Product ID    : ").append(item.getProductId()).append("\n");
        details.append("Product Name  : ").append(productName).append("\n");
        details.append("Qty In Stock  : ").append(item.getQuantityInStock()).append("\n");
        details.append("Min Threshold : ").append(item.getMinimumThreshold()).append("\n");
        details.append("Max Capacity  : ").append(item.getMaximumCapacity()).append("\n");
        details.append("Remaining Cap : ").append(item.getRemainingCapacity()).append("\n");
        details.append("Status        : ").append(item.getStockStatus()).append("\n");
        details.append("Recommended   : ").append(item.getRestockRecommendation()).append(" units\n");
        details.append("Last Restocked: ").append(item.getLastRestocked()).append("\n");
        details.append("Last Updated  : ").append(item.getLastUpdated()).append("\n");
        details.append("Created       : ").append(item.getCreatedDate()).append("\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "Stock Details — " + productName,
                JOptionPane.INFORMATION_MESSAGE);
    }


    private static class StockStatusRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                switch (value.toString()) {
                    case "IN STOCK":
                        setForeground(new Color(40, 140, 80));
                        break;
                    case "LOW STOCK":
                        setForeground(new Color(200, 100, 0));
                        break;
                    case "OUT OF STOCK":
                        setForeground(new Color(180, 40, 40));
                        break;
                    case "FULLY STOCKED":
                        setForeground(new Color(30, 80, 140));
                        break;
                    default:
                        setForeground(Color.BLACK);
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }
            return this;
        }
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