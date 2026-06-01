package Project_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SupplierPanel extends JPanel {

    private final SupplierService supplierService;
    private final ProductService productService;
    private final Session session;


    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;


    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton searchBtn;
    private JButton clearBtn;


    private JButton addBtn;
    private JButton editBtn;
    private JButton deactivateBtn;
    private JButton activateBtn;
    private JButton linkProductBtn;
    private JButton unlinkProductBtn;
    private JButton refreshBtn;


    private static final Color ACCENT     = new Color(30, 80, 140);
    private static final Color CONTENT_BG = new Color(245, 247, 250);
    private static final Color BTN_GREEN  = new Color(40, 140, 80);
    private static final Color BTN_ORANGE = new Color(200, 100, 0);
    private static final Color BTN_RED    = new Color(180, 40, 40);
    private static final Color BTN_GREY   = new Color(60, 60, 60);


    private static final String[] COLUMNS = {
        "ID", "Supplier Name", "Contact Person",
        "Email", "Phone", "Products Supplied", "Status"
    };


    public SupplierPanel(SupplierService supplierService,
                         ProductService productService,
                         Session session) {
        this.supplierService = supplierService;
        this.productService  = productService;
        this.session         = session;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        buildTopBar();
        buildTable();
        buildButtonBar();

        loadSuppliers();
    }


    private void buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));


        JLabel title = new JLabel("🏭 Supplier Management");
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

        String[] statuses = {"All Suppliers", "Active", "Inactive"};
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


    private void buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        supplierTable = new JTable(tableModel);
        supplierTable.setFont(new Font("Arial", Font.PLAIN, 13));
        supplierTable.setRowHeight(30);
        supplierTable.setGridColor(new Color(230, 230, 230));
        supplierTable.setSelectionBackground(new Color(210, 230, 255));
        supplierTable.setSelectionForeground(Color.BLACK);
        supplierTable.setShowVerticalLines(false);
        supplierTable.setFillsViewportHeight(true);


        JTableHeader header = supplierTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);


        supplierTable.getColumnModel().getColumn(0)
                .setPreferredWidth(90);
        supplierTable.getColumnModel().getColumn(1)
                .setPreferredWidth(180);
        supplierTable.getColumnModel().getColumn(2)
                .setPreferredWidth(140);
        supplierTable.getColumnModel().getColumn(3)
                .setPreferredWidth(200);
        supplierTable.getColumnModel().getColumn(4)
                .setPreferredWidth(110);
        supplierTable.getColumnModel().getColumn(5)
                .setPreferredWidth(120);
        supplierTable.getColumnModel().getColumn(6)
                .setPreferredWidth(90);


        supplierTable.getColumnModel().getColumn(6)
                .setCellRenderer(new StatusCellRenderer());


        supplierTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        scrollPane = new JScrollPane(supplierTable);
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
        refreshBtn.addActionListener(e -> loadSuppliers());
        buttonBar.add(refreshBtn);

        if (session.isAdmin()) {
            addBtn = createButton("➕ Add Supplier", BTN_GREEN);
            addBtn.addActionListener(e -> handleAddSupplier());
            buttonBar.add(addBtn);

            editBtn = createButton("✏ Edit Supplier", ACCENT);
            editBtn.addActionListener(e -> handleEditSupplier());
            buttonBar.add(editBtn);

            deactivateBtn = createButton("🚫 Deactivate", BTN_ORANGE);
            deactivateBtn.addActionListener(
                    e -> handleDeactivateSupplier());
            buttonBar.add(deactivateBtn);

            activateBtn = createButton("✅ Activate", BTN_GREEN);
            activateBtn.addActionListener(e -> handleActivateSupplier());
            buttonBar.add(activateBtn);

            linkProductBtn = createButton("🔗 Link Product", ACCENT);
            linkProductBtn.addActionListener(e -> handleLinkProduct());
            buttonBar.add(linkProductBtn);

            unlinkProductBtn = createButton("🔓 Unlink Product", BTN_RED);
            unlinkProductBtn.addActionListener(
                    e -> handleUnlinkProduct());
            buttonBar.add(unlinkProductBtn);
        }

        add(buttonBar, BorderLayout.SOUTH);
    }


    private void loadSuppliers() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierService.getAll();
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getSupplierName(),
                s.getContactPerson(),
                s.getEmail(),
                s.getPhone(),
                s.getTotalProductsSupplied(),
                s.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void loadSuppliers(List<Supplier> suppliers) {
        tableModel.setRowCount(0);
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getSupplierName(),
                s.getContactPerson(),
                s.getEmail(),
                s.getPhone(),
                s.getTotalProductsSupplied(),
                s.isActive() ? "Active" : "Inactive"
            });
        }
    }


    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        List<Supplier> results = supplierService.getAll();


        if (selectedStatus != null) {
            switch (selectedStatus) {
                case "Active":
                    results = supplierService.getActiveSuppliers();
                    break;
                case "Inactive":
                    results = supplierService.getInactiveSuppliers();
                    break;
            }
        }


        if (!keyword.isEmpty()) {
            results.removeIf(s ->
                !s.getSupplierName().toLowerCase().contains(keyword)
                && !s.getEmail().toLowerCase().contains(keyword)
                && !s.getContactPerson().toLowerCase().contains(keyword)
            );
        }

        loadSuppliers(results);
    }

    private void handleClear() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        loadSuppliers();
    }


    private void handleAddSupplier() {
        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), "Add Supplier", true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Add New Supplier");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(title, gbc);

        gbc.gridy = 1;
        form.add(new JLabel("Supplier Name:"), gbc);
        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(350, 32));
        gbc.gridy = 2;
        form.add(nameField, gbc);

        gbc.gridy = 3;
        form.add(new JLabel("Contact Person:"), gbc);
        JTextField contactField = new JTextField();
        gbc.gridy = 4;
        form.add(contactField, gbc);

        gbc.gridy = 5;
        form.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField();
        gbc.gridy = 6;
        form.add(emailField, gbc);

        gbc.gridy = 7;
        form.add(new JLabel("Phone:"), gbc);
        JTextField phoneField = new JTextField();
        gbc.gridy = 8;
        form.add(phoneField, gbc);

        gbc.gridy = 9;
        form.add(new JLabel("Address:"), gbc);
        JTextField addressField = new JTextField();
        gbc.gridy = 10;
        form.add(addressField, gbc);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = createButton("Cancel", BTN_GREY);
        JButton saveBtn   = createButton("Save Supplier", BTN_GREEN);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String email   = emailField.getText().trim();
            String phone   = phoneField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Supplier name and email are required.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Supplier supplier = new Supplier(name, contact,
                    email, phone, address);
            supplierService.add(supplier);
            loadSuppliers();
            dialog.dispose();
            JOptionPane.showMessageDialog(this,
                    "Supplier added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleEditSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a supplier to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String supplierId = (String) tableModel
                .getValueAt(selectedRow, 0);
        Supplier supplier = supplierService.findById(supplierId);
        if (supplier == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), "Edit Supplier", true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 2;

        JLabel title = new JLabel(
                "Edit Supplier — " + supplier.getSupplierName());
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(title, gbc);

        gbc.gridy = 1;
        form.add(new JLabel("Supplier Name:"), gbc);
        JTextField nameField = new JTextField(
                supplier.getSupplierName());
        nameField.setPreferredSize(new Dimension(350, 32));
        gbc.gridy = 2;
        form.add(nameField, gbc);

        gbc.gridy = 3;
        form.add(new JLabel("Contact Person:"), gbc);
        JTextField contactField = new JTextField(
                supplier.getContactPerson());
        gbc.gridy = 4;
        form.add(contactField, gbc);

        gbc.gridy = 5;
        form.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(supplier.getEmail());
        gbc.gridy = 6;
        form.add(emailField, gbc);

        gbc.gridy = 7;
        form.add(new JLabel("Phone:"), gbc);
        JTextField phoneField = new JTextField(supplier.getPhone());
        gbc.gridy = 8;
        form.add(phoneField, gbc);

        gbc.gridy = 9;
        form.add(new JLabel("Address:"), gbc);
        JTextField addressField = new JTextField(
                supplier.getAddress());
        gbc.gridy = 10;
        form.add(addressField, gbc);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = createButton("Cancel", BTN_GREY);
        JButton saveBtn   = createButton("Save Changes", ACCENT);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Supplier name cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            supplier.setSupplierName(name);
            supplier.setContactPerson(contactField.getText().trim());
            supplier.setEmail(emailField.getText().trim());
            supplier.setPhone(phoneField.getText().trim());
            supplier.setAddress(addressField.getText().trim());
            supplierService.update(supplier);
            loadSuppliers();
            dialog.dispose();
            JOptionPane.showMessageDialog(this,
                    "Supplier updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleDeactivateSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a supplier to deactivate.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String supplierId   = (String) tableModel
                .getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel
                .getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate supplier: " + supplierName + "?",
                "Confirm Deactivate", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            supplierService.delete(supplierId);
            loadSuppliers();
            JOptionPane.showMessageDialog(this,
                    "Supplier deactivated.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleActivateSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a supplier to activate.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String supplierId = (String) tableModel
                .getValueAt(selectedRow, 0);
        supplierService.activateSupplier(supplierId);
        loadSuppliers();
        JOptionPane.showMessageDialog(this,
                "Supplier activated successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleLinkProduct() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a supplier first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String supplierId   = (String) tableModel
                .getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel
                .getValueAt(selectedRow, 1);

        List<Product> products = productService.getAvailableProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No available products to link.",
                    "No Products", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] productOptions = products.stream()
                .map(p -> p.getId() + " — " + p.getName())
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select a product to link to " + supplierName + ":",
                "Link Product",
                JOptionPane.PLAIN_MESSAGE,
                null,
                productOptions,
                productOptions[0]);

        if (selected != null) {
            String productId = selected.split(" — ")[0];
            supplierService.linkProduct(supplierId, productId);
            loadSuppliers();
            JOptionPane.showMessageDialog(this,
                    "Product linked successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleUnlinkProduct() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a supplier first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String supplierId   = (String) tableModel
                .getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel
                .getValueAt(selectedRow, 1);

        Supplier supplier = supplierService.findById(supplierId);
        if (supplier == null) return;

        List<String> productIds = supplier.getProductIds();
        if (productIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    supplierName + " has no linked products.",
                    "No Products", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] productOptions = productIds.stream()
                .map(id -> {
                    Product p = productService.findById(id);
                    return id + " — " + (p != null
                            ? p.getName() : "Unknown");
                })
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select a product to unlink from " + supplierName + ":",
                "Unlink Product",
                JOptionPane.PLAIN_MESSAGE,
                null,
                productOptions,
                productOptions[0]);

        if (selected != null) {
            String productId = selected.split(" — ")[0];
            supplierService.unlinkProduct(supplierId, productId);
            loadSuppliers();
            JOptionPane.showMessageDialog(this,
                    "Product unlinked successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleViewDetails() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) return;

        String supplierId = (String) tableModel
                .getValueAt(selectedRow, 0);
        Supplier supplier = supplierService.findById(supplierId);
        if (supplier == null) return;

        StringBuilder details = new StringBuilder();
        details.append("Supplier Details\n");
        details.append("═══════════════════════════\n");
        details.append("ID               : ")
                .append(supplier.getId()).append("\n");
        details.append("Name             : ")
                .append(supplier.getSupplierName()).append("\n");
        details.append("Contact Person   : ")
                .append(supplier.getContactPerson()).append("\n");
        details.append("Email            : ")
                .append(supplier.getEmail()).append("\n");
        details.append("Phone            : ")
                .append(supplier.getPhone()).append("\n");
        details.append("Address          : ")
                .append(supplier.getAddress()).append("\n");
        details.append("Status           : ")
                .append(supplier.isActive()
                        ? "Active" : "Inactive").append("\n");
        details.append("Products Supplied: ")
                .append(supplier.getTotalProductsSupplied()).append("\n");
        details.append("Created          : ")
                .append(supplier.getCreatedDate()).append("\n");

        if (!supplier.getProductIds().isEmpty()) {
            details.append("\nLinked Products\n");
            details.append("═══════════════════════════\n");
            for (String pid : supplier.getProductIds()) {
                Product p = productService.findById(pid);
                details.append("  • ").append(pid)
                        .append(" — ")
                        .append(p != null ? p.getName() : "Unknown")
                        .append("\n");
            }
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "Supplier Details — " + supplier.getSupplierName(),
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
            if ("Active".equals(value)) {
                setForeground(new Color(40, 140, 80));
            } else {
                setForeground(new Color(180, 40, 40));
            }
            setFont(getFont().deriveFont(Font.BOLD));
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