package Project_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class OrderPanel extends JPanel {

    private final OrderService orderService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final StockService stockService;
    private final Session session;


    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;


    private JComboBox<String> statusFilter;
    private JTextField searchField;
    private JButton searchBtn;
    private JButton clearBtn;


    private JButton createOrderBtn;
    private JButton viewDetailsBtn;
    private JButton processBtn;
    private JButton deliverBtn;
    private JButton cancelBtn;
    private JButton refreshBtn;


    private static final Color ACCENT     = new Color(30, 80, 140);
    private static final Color CONTENT_BG = new Color(245, 247, 250);
    private static final Color BTN_GREEN  = new Color(40, 140, 80);
    private static final Color BTN_ORANGE = new Color(200, 100, 0);
    private static final Color BTN_RED    = new Color(180, 40, 40);
    private static final Color BTN_GREY   = new Color(60, 60, 60);
    private static final Color BTN_PURPLE = new Color(120, 40, 140);


    private static final String[] COLUMNS = {
        "Order ID", "Supplier", "Items",
        "Total", "Status", "Created By", "Created Date"
    };


    public OrderPanel(OrderService orderService,
                      ProductService productService,
                      SupplierService supplierService,
                      StockService stockService,
                      Session session) {
        this.orderService    = orderService;
        this.productService  = productService;
        this.supplierService = supplierService;
        this.stockService    = stockService;
        this.session         = session;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        buildTopBar();
        buildSummaryBar();
        buildTable();
        buildButtonBar();

        loadOrders();
    }


    private void buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("📋 Order Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(ACCENT);
        topBar.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));
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

        String[] statuses = {"All Orders", "Pending", "Confirmed",
                "Processing", "Delivered", "Cancelled"};
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
        JPanel summaryBar = new JPanel(new GridLayout(1, 5, 10, 0));
        summaryBar.setBackground(CONTENT_BG);
        summaryBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        summaryBar.add(buildSummaryCard("Total Orders",
                String.valueOf(orderService.getTotalOrders()),
                ACCENT));
        summaryBar.add(buildSummaryCard("Pending",
                String.valueOf(orderService.getPendingOrders().size()),
                BTN_ORANGE));
        summaryBar.add(buildSummaryCard("Confirmed",
                String.valueOf(orderService.getConfirmedOrders().size()),
                ACCENT));
        summaryBar.add(buildSummaryCard("Delivered",
                String.valueOf(orderService.getDeliveredOrders().size()),
                BTN_GREEN));
        summaryBar.add(buildSummaryCard("Total Revenue",
                orderService.getFormattedTotalRevenue(),
                BTN_PURPLE));

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
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
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

        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 13));
        orderTable.setRowHeight(30);
        orderTable.setGridColor(new Color(230, 230, 230));
        orderTable.setSelectionBackground(new Color(210, 230, 255));
        orderTable.setSelectionForeground(Color.BLACK);
        orderTable.setShowVerticalLines(false);
        orderTable.setFillsViewportHeight(true);

        JTableHeader header = orderTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);

        orderTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(150);


        orderTable.getColumnModel().getColumn(4)
                .setCellRenderer(new OrderStatusRenderer());


        orderTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(220, 220, 220)));
        add(scrollPane, BorderLayout.CENTER);
    }


    private void buildButtonBar() {
        JPanel buttonBar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonBar.setBackground(new Color(235, 238, 242));
        buttonBar.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(200, 200, 200)));

        refreshBtn = createButton("🔄 Refresh", BTN_GREY);
        refreshBtn.addActionListener(e -> loadOrders());
        buttonBar.add(refreshBtn);

        viewDetailsBtn = createButton("🔍 View Details", ACCENT);
        viewDetailsBtn.addActionListener(e -> handleViewDetails());
        buttonBar.add(viewDetailsBtn);

        if (session.isAdminOrStaff()) {
            createOrderBtn = createButton("➕ Create Order", BTN_GREEN);
            createOrderBtn.addActionListener(e -> handleCreateOrder());
            buttonBar.add(createOrderBtn);

            processBtn = createButton("⚙ Process", BTN_ORANGE);
            processBtn.addActionListener(e -> handleProcessOrder());
            buttonBar.add(processBtn);

            deliverBtn = createButton("✅ Deliver", BTN_GREEN);
            deliverBtn.addActionListener(e -> handleDeliverOrder());
            buttonBar.add(deliverBtn);

            cancelBtn = createButton("❌ Cancel Order", BTN_RED);
            cancelBtn.addActionListener(e -> handleCancelOrder());
            buttonBar.add(cancelBtn);
        }

        add(buttonBar, BorderLayout.SOUTH);
    }


    private void loadOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = orderService.getAll();
        for (Order o : orders) {
            tableModel.addRow(new Object[]{
                o.getId(),
                o.getSupplierName(),
                o.getOrderLines().size(),
                o.getFormattedOrderTotal(),
                o.getStatus().getDisplayName(),
                o.getCreatedByUserId(),
                o.getCreatedDate()
            });
        }
    }

    private void loadOrders(List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order o : orders) {
            tableModel.addRow(new Object[]{
                o.getId(),
                o.getSupplierName(),
                o.getOrderLines().size(),
                o.getFormattedOrderTotal(),
                o.getStatus().getDisplayName(),
                o.getCreatedByUserId(),
                o.getCreatedDate()
            });
        }
    }


    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        List<Order> results = orderService.getAll();

        if (selectedStatus != null
                && !selectedStatus.equals("All Orders")) {
            switch (selectedStatus) {
                case "Pending":
                    results = orderService.getPendingOrders();
                    break;
                case "Confirmed":
                    results = orderService.getConfirmedOrders();
                    break;
                case "Delivered":
                    results = orderService.getDeliveredOrders();
                    break;
                case "Cancelled":
                    results = orderService.getCancelledOrders();
                    break;
                case "Processing":
                    results = orderService.findByStatus(
                            Order.OrderStatus.PROCESSING);
                    break;
            }
        }

        if (!keyword.isEmpty()) {
            results.removeIf(o ->
                !o.getId().toLowerCase().contains(keyword)
                && !o.getSupplierName().toLowerCase().contains(keyword)
            );
        }

        loadOrders(results);
    }

    private void handleClear() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        loadOrders();
    }


    private void handleCreateOrder() {
        List<Supplier> activeSuppliers =
                supplierService.getActiveSuppliers();
        if (activeSuppliers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No active suppliers available.\n"
                    + "Please add a supplier first.",
                    "No Suppliers", JOptionPane.WARNING_MESSAGE);
            return;
        }


        String[] supplierOptions = activeSuppliers.stream()
                .map(s -> s.getId() + " — " + s.getSupplierName())
                .toArray(String[]::new);

        String selectedSupplier = (String) JOptionPane.showInputDialog(
                this,
                "Select a supplier for this order:",
                "Create Order — Step 1 of 2",
                JOptionPane.PLAIN_MESSAGE,
                null,
                supplierOptions,
                supplierOptions[0]);

        if (selectedSupplier == null) return;

        String supplierId = selectedSupplier.split(" — ")[0];
        Supplier supplier = supplierService.findById(supplierId);
        if (supplier == null) return;


        Order order = orderService.createOrder(
                supplierId,
                supplier.getSupplierName(),
                session.getCurrentUserId()
        );

        if (order == null) return;

        showOrderBuilderDialog(order);
    }

    private void showOrderBuilderDialog(Order order) {
        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this),
                "Order Builder — " + order.getSupplierName(), true);
        dialog.setSize(700, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());


        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        JLabel headerLabel = new JLabel("Building Order for: "
                + order.getSupplierName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);


        String[] lineCols = {"Product ID", "Product Name",
                "Qty", "Unit Price", "Discount %", "Total"};
        DefaultTableModel lineModel = new DefaultTableModel(lineCols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable lineTable = new JTable(lineModel);
        lineTable.setFont(new Font("Arial", Font.PLAIN, 12));
        lineTable.setRowHeight(28);
        lineTable.getTableHeader().setBackground(new Color(60, 100, 160));
        lineTable.getTableHeader().setForeground(Color.WHITE);
        lineTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12));


        JLabel totalLabel = new JLabel("Order Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(ACCENT);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));


        Runnable refreshLines = () -> {
            lineModel.setRowCount(0);
            for (OrderLine line : order.getOrderLines()) {
                lineModel.addRow(new Object[]{
                    line.getProductId(),
                    line.getProductName(),
                    line.getQuantity(),
                    line.getFormattedUnitPrice(),
                    line.getDiscountPercent() + "%",
                    line.getFormattedFinalTotal()
                });
            }
            totalLabel.setText("Order Total: "
                    + order.getFormattedOrderTotal());
        };

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        tablePanel.add(new JScrollPane(lineTable), BorderLayout.CENTER);
        tablePanel.add(totalLabel, BorderLayout.SOUTH);
        dialog.add(tablePanel, BorderLayout.CENTER);


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setBackground(new Color(245, 247, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(220, 220, 220)));

        JButton addLineBtn    = createButton("➕ Add Product", BTN_GREEN);
        JButton removeLineBtn = createButton("➖ Remove Product", BTN_RED);
        JButton submitBtn     = createButton("✅ Submit Order", ACCENT);
        JButton cancelDialogBtn = createButton("Cancel", BTN_GREY);


        addLineBtn.addActionListener(e -> {
            List<Product> products =
                    productService.getAvailableProducts();
            if (products.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No available products.",
                        "No Products", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] productOptions = products.stream()
                    .map(p -> p.getId() + " — " + p.getName()
                            + " (" + p.getFormattedPrice() + ")")
                    .toArray(String[]::new);

            String selectedProduct = (String) JOptionPane
                    .showInputDialog(dialog,
                    "Select a product to add:",
                    "Add Product to Order",
                    JOptionPane.PLAIN_MESSAGE,
                    null, productOptions, productOptions[0]);

            if (selectedProduct == null) return;

            String productId = selectedProduct.split(" — ")[0];
            Product product  = productService.findById(productId);
            if (product == null) return;


            String qtyStr = JOptionPane.showInputDialog(dialog,
                    "Enter quantity for " + product.getName()
                    + "\nAvailable: "
                    + stockService.getQuantityByProduct(productId),
                    "Quantity", JOptionPane.PLAIN_MESSAGE);
            if (qtyStr == null || qtyStr.trim().isEmpty()) return;


            String discStr = JOptionPane.showInputDialog(dialog,
                    "Enter discount % (0 for none):",
                    "Discount", JOptionPane.PLAIN_MESSAGE);
            if (discStr == null) return;

            try {
                int qty = Integer.parseInt(qtyStr.trim());
                double disc = Double.parseDouble(
                        discStr.trim().isEmpty() ? "0" : discStr.trim());

                if (disc > 0) {
                    orderService.addLineToOrderWithDiscount(
                            order, productId, product.getName(),
                            qty, product.getPrice(), disc);
                } else {
                    orderService.addLineToOrder(
                            order, productId, product.getName(),
                            qty, product.getPrice());
                }
                refreshLines.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid numbers.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });


        removeLineBtn.addActionListener(e -> {
            int selectedRow = lineTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select a line to remove.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String productId = (String) lineModel
                    .getValueAt(selectedRow, 0);
            order.removeOrderLine(productId);
            refreshLines.run();
        });


        submitBtn.addActionListener(e -> {
            if (order.getOrderLines().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Cannot submit an empty order.\n"
                        + "Please add at least one product.",
                        "Empty Order", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Submit order for "
                    + order.getSupplierName() + "?\n"
                    + "Total: " + order.getFormattedOrderTotal()
                    + "\nThis will reduce stock levels.",
                    "Confirm Submit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (orderService.submitOrder(order)) {
                    loadOrders();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Order submitted successfully!\n"
                            + "Order ID: " + order.getId(),
                            "Order Submitted",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        cancelDialogBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(addLineBtn);
        btnPanel.add(removeLineBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(submitBtn);
        btnPanel.add(cancelDialogBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleViewDetails() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an order to view.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        Order order = orderService.findById(orderId);
        if (order == null) return;

        JTextArea textArea = new JTextArea(order.generateReport());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "Order Details — " + orderId,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleProcessOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an order to process.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        if (orderService.processOrder(orderId)) {
            loadOrders();
            JOptionPane.showMessageDialog(this,
                    "Order " + orderId + " is now processing.",
                    "Order Processing",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not process order.\n"
                    + "Order must be in Confirmed status.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeliverOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an order to deliver.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        if (orderService.deliverOrder(orderId)) {
            loadOrders();
            JOptionPane.showMessageDialog(this,
                    "Order " + orderId + " marked as delivered.",
                    "Order Delivered",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not deliver order.\n"
                    + "Order must be in Processing status.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an order to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String status  = (String) tableModel.getValueAt(selectedRow, 4);

        if ("Delivered".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Delivered orders cannot be cancelled.",
                    "Cannot Cancel", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel order " + orderId + "?\n"
                + "Stock will be restored if order was confirmed.",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (orderService.cancelOrder(orderId)) {
                loadOrders();
                JOptionPane.showMessageDialog(this,
                        "Order " + orderId + " has been cancelled.",
                        "Order Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private static class OrderStatusRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                switch (value.toString()) {
                    case "Pending":
                        setForeground(new Color(200, 100, 0));
                        break;
                    case "Confirmed":
                        setForeground(new Color(30, 80, 140));
                        break;
                    case "Processing":
                        setForeground(new Color(120, 40, 140));
                        break;
                    case "Delivered":
                        setForeground(new Color(40, 140, 80));
                        break;
                    case "Cancelled":
                        setForeground(new Color(180, 40, 40));
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
