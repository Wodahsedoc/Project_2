package Project_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final StockService stockService;
    private final Session session;


    private JTable productTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;


    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> sortFilter;
    private JButton searchBtn;
    private JButton clearBtn;


    private JButton addBtn;
    private JButton editBtn;
    private JButton deactivateBtn;
    private JButton deleteBtn;
    private JButton refreshBtn;


    private static final Color ACCENT       = new Color(30, 80, 140);
    private static final Color CONTENT_BG   = new Color(245, 247, 250);
    private static final Color BTN_GREEN    = new Color(40, 140, 80);
    private static final Color BTN_ORANGE   = new Color(200, 100, 0);
    private static final Color BTN_RED      = new Color(180, 40, 40);
    private static final Color BTN_GREY     = new Color(60, 60, 60);


    private static final String[] COLUMNS = {
        "ID", "Name", "Category", "Price", "Supplier ID", "Status"
    };


    public ProductPanel(ProductService productService,
                        SupplierService supplierService,
                        StockService stockService,
                        Session session) {
        this.productService  = productService;
        this.supplierService = supplierService;
        this.stockService    = stockService;
        this.session         = session;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        buildTopBar();
        buildTable();
        buildButtonBar();

        loadProducts();
    }


    private void buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));


        JLabel title = new JLabel("📦 Product Management");
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
        searchField.putClientProperty("JTextField.placeholderText",
                "Search products...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleSearch();
            }
        });
        searchPanel.add(searchField);


        String[] categories = {"All Categories", "Electronics",
                "Food", "Clothing", "Stationery", "Other"};
        categoryFilter = new JComboBox<>(categories);
        categoryFilter.setFont(new Font("Arial", Font.PLAIN, 13));
        categoryFilter.addActionListener(e -> handleSearch());
        searchPanel.add(categoryFilter);


        String[] sorts = {"Default", "Name A-Z", "Price Low-High",
                "Price High-Low", "Category"};
        sortFilter = new JComboBox<>(sorts);
        sortFilter.setFont(new Font("Arial", Font.PLAIN, 13));
        sortFilter.addActionListener(e -> handleSearch());
        searchPanel.add(sortFilter);


        searchBtn = createButton("Search", ACCENT);
        searchBtn.addActionListener(e -> handleSearch());
        searchPanel.add(searchBtn);


        clearBtn = createButton("Clear", BTN_GREY);
        clearBtn.addActionListener(e -> handleClear());
        searchPanel.add(clearBtn);

        topBar.add(searchPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
    }


    private void buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(30);
        productTable.setGridColor(new Color(230, 230, 230));
        productTable.setSelectionBackground(new Color(210, 230, 255));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setShowVerticalLines(false);
        productTable.setFillsViewportHeight(true);


        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);


        productTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(90);


        productTable.getColumnModel().getColumn(5)
                .setCellRenderer(new StatusCellRenderer());


        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        scrollPane = new JScrollPane(productTable);
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
        refreshBtn.addActionListener(e -> loadProducts());
        buttonBar.add(refreshBtn);


        if (session.isAdminOrStaff()) {
            addBtn = createButton("➕ Add Product", BTN_GREEN);
            addBtn.addActionListener(e -> handleAddProduct());
            buttonBar.add(addBtn);

            editBtn = createButton("✏ Edit Product", ACCENT);
            editBtn.addActionListener(e -> handleEditProduct());
            buttonBar.add(editBtn);

            deactivateBtn = createButton("🚫 Deactivate", BTN_ORANGE);
            deactivateBtn.addActionListener(e -> handleDeactivateProduct());
            buttonBar.add(deactivateBtn);
        }


        if (session.isAdmin()) {
            deleteBtn = createButton("🗑 Delete", BTN_RED);
            deleteBtn.addActionListener(e -> handleDeleteProduct());
            buttonBar.add(deleteBtn);
        }


        JLabel countLabel = new JLabel();
        countLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        countLabel.setForeground(new Color(120, 120, 120));
        buttonBar.add(Box.createHorizontalStrut(20));
        buttonBar.add(countLabel);

        add(buttonBar, BorderLayout.SOUTH);
    }


    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productService.getAvailableProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory().getDisplayName(),
                p.getFormattedPrice(),
                p.getSupplierId(),
                p.isAvailable() ? "Available" : "Unavailable"
            });
        }
    }

    private void loadProducts(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory().getDisplayName(),
                p.getFormattedPrice(),
                p.getSupplierId(),
                p.isAvailable() ? "Available" : "Unavailable"
            });
        }
    }


    private void handleSearch() {
        String keyword = searchField.getText().trim();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String selectedSort = (String) sortFilter.getSelectedItem();

        List<Product> results;


        if (selectedCategory != null
                && !selectedCategory.equals("All Categories")) {
            try {
                Category cat = Category.valueOf(
                        selectedCategory.toUpperCase());
                results = productService.filterByCategory(cat);
            } catch (IllegalArgumentException e) {
                results = productService.getAvailableProducts();
            }
        } else {
            results = productService.getAvailableProducts();
        }


        if (!keyword.isEmpty()) {
            results.removeIf(p -> !p.getName().toLowerCase()
                    .contains(keyword.toLowerCase()));
        }


        if (selectedSort != null) {
            switch (selectedSort) {
                case "Name A-Z":
                    results.sort((a, b) -> a.getName()
                            .compareToIgnoreCase(b.getName()));
                    break;
                case "Price Low-High":
                    results.sort((a, b) -> Double.compare(
                            a.getPrice(), b.getPrice()));
                    break;
                case "Price High-Low":
                    results.sort((a, b) -> Double.compare(
                            b.getPrice(), a.getPrice()));
                    break;
                case "Category":
                    results.sort((a, b) -> a.getCategory()
                            .getDisplayName()
                            .compareToIgnoreCase(
                                    b.getCategory().getDisplayName()));
                    break;
            }
        }

        loadProducts(results);
    }

    private void handleClear() {
        searchField.setText("");
        categoryFilter.setSelectedIndex(0);
        sortFilter.setSelectedIndex(0);
        loadProducts();
    }


    private void handleAddProduct() {
        if (supplierService.getActiveSuppliers().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No active suppliers found.\n"
                + "Please add a supplier before adding products.",
                "No Suppliers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), "Add Product", true);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 2;


        JLabel title = new JLabel("Add New Product");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(title, gbc);


        gbc.gridy = 1;
        form.add(new JLabel("Product Name:"), gbc);
        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(350, 32));
        gbc.gridy = 2;
        form.add(nameField, gbc);


        gbc.gridy = 3;
        form.add(new JLabel("Description:"), gbc);
        JTextField descField = new JTextField();
        gbc.gridy = 4;
        form.add(descField, gbc);


        gbc.gridy = 5;
        form.add(new JLabel("Price ($):"), gbc);
        JTextField priceField = new JTextField();
        gbc.gridy = 6;
        form.add(priceField, gbc);


        gbc.gridy = 7;
        form.add(new JLabel("Category:"), gbc);
        JComboBox<String> catCombo = new JComboBox<>(new String[]{
            "Electronics", "Food", "Clothing", "Stationery", "Other"
        });
        gbc.gridy = 8;
        form.add(catCombo, gbc);


        gbc.gridy = 9;
        form.add(new JLabel("Supplier:"), gbc);
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        String[] supplierNames = suppliers.stream()
                .map(s -> s.getId() + " — " + s.getSupplierName())
                .toArray(String[]::new);
        JComboBox<String> supplierCombo = new JComboBox<>(supplierNames);
        gbc.gridy = 10;
        form.add(supplierCombo, gbc);


        gbc.gridy = 11;
        form.add(new JLabel("Initial Stock Quantity:"), gbc);
        JTextField stockField = new JTextField("0");
        gbc.gridy = 12;
        form.add(stockField, gbc);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = createButton("Cancel", BTN_GREY);
        JButton saveBtn   = createButton("Save Product", BTN_GREEN);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                double price = Double.parseDouble(
                        priceField.getText().trim());
                Category cat = Category.valueOf(
                        catCombo.getSelectedItem()
                        .toString().toUpperCase());
                String supplierId = suppliers
                        .get(supplierCombo.getSelectedIndex()).getId();
                int stockQty = Integer.parseInt(
                        stockField.getText().trim());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Product name cannot be empty.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Product product = new Product(name, desc,
                        price, cat, supplierId);
                productService.add(product);


                StockItem stockItem = new StockItem(
                        product.getId(), stockQty, 10, 100);
                stockService.add(stockItem);


                supplierService.linkProduct(supplierId, product.getId());

                loadProducts();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Product added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid numbers for price and stock.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleEditProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        Product product = productService.findById(productId);
        if (product == null) {
            JOptionPane.showMessageDialog(this,
                    "Product not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), "Edit Product", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Edit Product — " + product.getName());
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(title, gbc);

        gbc.gridy = 1;
        form.add(new JLabel("Product Name:"), gbc);
        JTextField nameField = new JTextField(product.getName());
        gbc.gridy = 2;
        form.add(nameField, gbc);

        gbc.gridy = 3;
        form.add(new JLabel("Description:"), gbc);
        JTextField descField = new JTextField(product.getDescription());
        gbc.gridy = 4;
        form.add(descField, gbc);

        gbc.gridy = 5;
        form.add(new JLabel("Price ($):"), gbc);
        JTextField priceField = new JTextField(
                String.valueOf(product.getPrice()));
        gbc.gridy = 6;
        form.add(priceField, gbc);

        gbc.gridy = 7;
        form.add(new JLabel("Category:"), gbc);
        String[] cats = {"Electronics", "Food",
                "Clothing", "Stationery", "Other"};
        JComboBox<String> catCombo = new JComboBox<>(cats);
        catCombo.setSelectedItem(product.getCategory().getDisplayName());
        gbc.gridy = 8;
        form.add(catCombo, gbc);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = createButton("Cancel", BTN_GREY);
        JButton saveBtn   = createButton("Save Changes", ACCENT);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            try {
                product.setName(nameField.getText().trim());
                product.setDescription(descField.getText().trim());
                product.setPrice(Double.parseDouble(
                        priceField.getText().trim()));
                product.setCategory(Category.valueOf(
                        catCombo.getSelectedItem()
                        .toString().toUpperCase()));
                productService.update(product);
                loadProducts();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Product updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid price.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleDeactivateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to deactivate.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate product: " + productName + "?\n"
                + "It will be hidden from the product list.",
                "Confirm Deactivate", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            productService.delete(productId);
            loadProducts();
            JOptionPane.showMessageDialog(this,
                    "Product deactivated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete: " + productName + "?\n"
                + "This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            productService.hardDelete(productId);
            loadProducts();
            JOptionPane.showMessageDialog(this,
                    "Product permanently deleted.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleViewDetails() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) return;

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        Product product = productService.findById(productId);
        if (product == null) return;

        StockItem stock = stockService.getStockByProduct(productId);
        Supplier supplier = supplierService.findById(product.getSupplierId());

        StringBuilder details = new StringBuilder();
        details.append("Product Details\n");
        details.append("═══════════════════════════\n");
        details.append("ID          : ").append(product.getId()).append("\n");
        details.append("Name        : ").append(product.getName()).append("\n");
        details.append("Description : ").append(product.getDescription()).append("\n");
        details.append("Price       : ").append(product.getFormattedPrice()).append("\n");
        details.append("Category    : ").append(product.getCategory().getDisplayName()).append("\n");
        details.append("Status      : ").append(product.isAvailable() ? "Available" : "Unavailable").append("\n");
        details.append("Created     : ").append(product.getCreatedDate()).append("\n");
        details.append("\nStock Information\n");
        details.append("═══════════════════════════\n");
        if (stock != null) {
            details.append("Qty In Stock : ").append(stock.getQuantityInStock()).append("\n");
            details.append("Min Threshold: ").append(stock.getMinimumThreshold()).append("\n");
            details.append("Max Capacity : ").append(stock.getMaximumCapacity()).append("\n");
            details.append("Status       : ").append(stock.getStockStatus()).append("\n");
        } else {
            details.append("No stock record found.\n");
        }
        details.append("\nSupplier Information\n");
        details.append("═══════════════════════════\n");
        if (supplier != null) {
            details.append("Supplier  : ").append(supplier.getSupplierName()).append("\n");
            details.append("Contact   : ").append(supplier.getContactPerson()).append("\n");
            details.append("Email     : ").append(supplier.getEmail()).append("\n");
        } else {
            details.append("No supplier found.\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "Product Details — " + product.getName(),
                JOptionPane.INFORMATION_MESSAGE);
    }


    private static class StatusCellRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if ("Available".equals(value)) {
                setForeground(new Color(40, 140, 80));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(new Color(180, 40, 40));
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