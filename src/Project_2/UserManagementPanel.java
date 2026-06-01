package Project_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserService userService;
    private final Session session;


    private JTable userTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;


    private JTextField searchField;
    private JComboBox<String> roleFilter;
    private JComboBox<String> statusFilter;
    private JButton searchBtn;
    private JButton clearBtn;


    private JButton createUserBtn;
    private JButton editUserBtn;
    private JButton changeRoleBtn;
    private JButton changePasswordBtn;
    private JButton deactivateBtn;
    private JButton activateBtn;
    private JButton deleteBtn;
    private JButton refreshBtn;


    private static final Color ACCENT     = new Color(30, 80, 140);
    private static final Color CONTENT_BG = new Color(245, 247, 250);
    private static final Color BTN_GREEN  = new Color(40, 140, 80);
    private static final Color BTN_ORANGE = new Color(200, 100, 0);
    private static final Color BTN_RED    = new Color(180, 40, 40);
    private static final Color BTN_GREY   = new Color(60, 60, 60);
    private static final Color BTN_PURPLE = new Color(120, 40, 140);


    private static final String[] COLUMNS = {
        "ID", "Username", "Email", "Role", "Status", "Created"
    };


    public UserManagementPanel(UserService userService,
                                Session session) {
        this.userService = userService;
        this.session     = session;

        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        buildTopBar();
        buildStatsBar();
        buildTable();
        buildButtonBar();

        loadUsers();
    }


    private void buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("👥 User Management");
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

        String[] roles = {"All Roles", "Admin", "Staff", "Customer"};
        roleFilter = new JComboBox<>(roles);
        roleFilter.setFont(new Font("Arial", Font.PLAIN, 13));
        roleFilter.addActionListener(e -> handleSearch());
        searchPanel.add(roleFilter);

        String[] statuses = {"All Status", "Active", "Inactive"};
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


    private void buildStatsBar() {
        JPanel statsBar = new JPanel(new GridLayout(1, 4, 10, 0));
        statsBar.setBackground(CONTENT_BG);
        statsBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        statsBar.add(buildStatCard("Total Users",
                String.valueOf(userService.getAll().size()),
                ACCENT));
        statsBar.add(buildStatCard("Admins",
                String.valueOf(userService
                        .findByRole(UserRole.ADMIN).size()),
                BTN_RED));
        statsBar.add(buildStatCard("Staff",
                String.valueOf(userService
                        .findByRole(UserRole.STAFF).size()),
                BTN_PURPLE));
        statsBar.add(buildStatCard("Active",
                String.valueOf(userService.findActiveUsers().size()),
                BTN_GREEN));

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.setBackground(CONTENT_BG);
        JPanel topBar = (JPanel) getComponent(0);
        remove(topBar);
        northWrapper.add(topBar, BorderLayout.NORTH);
        northWrapper.add(statsBar, BorderLayout.SOUTH);
        add(northWrapper, BorderLayout.NORTH);
    }

    private JPanel buildStatCard(String label,
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

        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 13));
        userTable.setRowHeight(30);
        userTable.setGridColor(new Color(230, 230, 230));
        userTable.setSelectionBackground(new Color(210, 230, 255));
        userTable.setSelectionForeground(Color.BLACK);
        userTable.setShowVerticalLines(false);
        userTable.setFillsViewportHeight(true);

        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);

        userTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150);


        userTable.getColumnModel().getColumn(3)
                .setCellRenderer(new RoleCellRenderer());
        userTable.getColumnModel().getColumn(4)
                .setCellRenderer(new StatusCellRenderer());


        userTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        scrollPane = new JScrollPane(userTable);
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
        refreshBtn.addActionListener(e -> loadUsers());
        buttonBar.add(refreshBtn);

        createUserBtn = createButton("➕ Create User", BTN_GREEN);
        createUserBtn.addActionListener(e -> handleCreateUser());
        buttonBar.add(createUserBtn);

        editUserBtn = createButton("✏ Edit User", ACCENT);
        editUserBtn.addActionListener(e -> handleEditUser());
        buttonBar.add(editUserBtn);

        changeRoleBtn = createButton("🔄 Change Role", BTN_PURPLE);
        changeRoleBtn.addActionListener(e -> handleChangeRole());
        buttonBar.add(changeRoleBtn);

        changePasswordBtn = createButton("🔑 Reset Password", BTN_ORANGE);
        changePasswordBtn.addActionListener(e -> handleResetPassword());
        buttonBar.add(changePasswordBtn);

        deactivateBtn = createButton("🚫 Deactivate", BTN_ORANGE);
        deactivateBtn.addActionListener(e -> handleDeactivateUser());
        buttonBar.add(deactivateBtn);

        activateBtn = createButton("✅ Activate", BTN_GREEN);
        activateBtn.addActionListener(e -> handleActivateUser());
        buttonBar.add(activateBtn);

        deleteBtn = createButton("🗑 Delete", BTN_RED);
        deleteBtn.addActionListener(e -> handleDeleteUser());
        buttonBar.add(deleteBtn);

        add(buttonBar, BorderLayout.SOUTH);
    }


    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userService.getAll();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().getDisplayName(),
                u.isActive() ? "Active" : "Inactive",
                u.getCreatedDate()
            });
        }
    }

    private void loadUsers(List<User> users) {
        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().getDisplayName(),
                u.isActive() ? "Active" : "Inactive",
                u.getCreatedDate()
            });
        }
    }


    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedRole   = (String) roleFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        List<User> results = userService.getAll();


        if (selectedRole != null
                && !selectedRole.equals("All Roles")) {
            switch (selectedRole) {
                case "Admin":
                    results = userService.findByRole(UserRole.ADMIN);
                    break;
                case "Staff":
                    results = userService.findByRole(UserRole.STAFF);
                    break;
                case "Customer":
                    results = userService.findByRole(UserRole.CUSTOMER);
                    break;
            }
        }


        if (selectedStatus != null
                && !selectedStatus.equals("All Status")) {
            if (selectedStatus.equals("Active")) {
                results.removeIf(u -> !u.isActive());
            } else {
                results.removeIf(User::isActive);
            }
        }


        if (!keyword.isEmpty()) {
            results.removeIf(u ->
                !u.getUsername().toLowerCase().contains(keyword)
                && !u.getEmail().toLowerCase().contains(keyword)
                && !u.getId().toLowerCase().contains(keyword)
            );
        }

        loadUsers(results);
    }

    private void handleClear() {
        searchField.setText("");
        roleFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        loadUsers();
    }


    private void handleCreateUser() {
        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), "Create New User", true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Create New User");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(title, gbc);

        gbc.gridy = 1;
        form.add(new JLabel("Username (3-20 chars):"), gbc);
        JTextField usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(350, 32));
        gbc.gridy = 2;
        form.add(usernameField, gbc);

        gbc.gridy = 3;
        form.add(new JLabel("Password (min 6 chars):"), gbc);
        JPasswordField passwordField = new JPasswordField();
        gbc.gridy = 4;
        form.add(passwordField, gbc);

        gbc.gridy = 5;
        form.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField();
        gbc.gridy = 6;
        form.add(emailField, gbc);

        gbc.gridy = 7;
        form.add(new JLabel("Role:"), gbc);
        JComboBox<String> roleCombo = new JComboBox<>(
                new String[]{"Admin", "Staff", "Customer"});
        gbc.gridy = 8;
        form.add(roleCombo, gbc);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = createButton("Cancel", BTN_GREY);
        JButton saveBtn   = createButton("Create User", BTN_GREEN);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String email    = emailField.getText().trim();
            String roleStr  = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()
                    || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "All fields are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserRole role;
            switch (roleStr) {
                case "Admin":  role = UserRole.ADMIN;    break;
                case "Staff":  role = UserRole.STAFF;    break;
                default:       role = UserRole.CUSTOMER; break;
            }

            if (userService.register(username, password,
                    email, role)) {
                loadUsers();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "User created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleEditUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        User user = userService.findById(userId);
        if (user == null) return;


        if (userId.equals(session.getCurrentUserId())
                && user.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "You cannot edit your own admin account here.\n"
                    + "Use My Account to change your details.",
                    "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities
                .getWindowAncestor(this), "Edit User", true);
        dialog.setSize(420, 300);
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
                "Edit User — " + user.getUsername());
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(title, gbc);

        gbc.gridy = 1;
        form.add(new JLabel("Username:"), gbc);
        JTextField usernameField = new JTextField(
                user.getUsername());
        usernameField.setPreferredSize(new Dimension(350, 32));
        gbc.gridy = 2;
        form.add(usernameField, gbc);

        gbc.gridy = 3;
        form.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(user.getEmail());
        gbc.gridy = 4;
        form.add(emailField, gbc);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = createButton("Cancel", BTN_GREY);
        JButton saveBtn   = createButton("Save Changes", ACCENT);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String newUsername = usernameField.getText().trim();
            String newEmail    = emailField.getText().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Username and email cannot be empty.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            user.setUsername(newUsername);
            user.setEmail(newEmail);
            if (userService.update(user)) {
                loadUsers();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "User updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleChangeRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to change role.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId   = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentRole = (String) tableModel
                .getValueAt(selectedRow, 3);

        String[] roles = {"Admin", "Staff", "Customer"};
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Change role for " + username
                + "\nCurrent role: " + currentRole,
                "Change Role",
                JOptionPane.PLAIN_MESSAGE,
                null, roles, currentRole);

        if (selected != null) {
            UserRole newRole;
            switch (selected) {
                case "Admin":  newRole = UserRole.ADMIN;    break;
                case "Staff":  newRole = UserRole.STAFF;    break;
                default:       newRole = UserRole.CUSTOMER; break;
            }
            if (userService.changeRole(userId, newRole)) {
                loadUsers();
                JOptionPane.showMessageDialog(this,
                        username + " is now " + selected + ".",
                        "Role Changed",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleResetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to reset password.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId   = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("New Password:"));
        JPasswordField newPassField = new JPasswordField();
        form.add(newPassField);

        form.add(new JLabel("Confirm Password:"));
        JPasswordField confirmPassField = new JPasswordField();
        form.add(confirmPassField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Reset Password for " + username,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPass     = new String(newPassField.getPassword());
            String confirmPass = new String(
                    confirmPassField.getPassword());

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Password cannot be empty.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this,
                        "Password must be at least 6 characters.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            User user = userService.findById(userId);
            if (user != null) {
                User updatedUser = new User(
                    user.getId(),
                    user.getCreatedDate(),
                    user.getUsername(),
                    newPass,
                    user.getEmail(),
                    user.getRole(),
                    user.isActive()
                );
                if (userService.update(updatedUser)) {
                    JOptionPane.showMessageDialog(this,
                            "Password reset successfully for "
                            + username + ".",
                            "Password Reset",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void handleDeactivateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to deactivate.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId   = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        if (userId.equals(session.getCurrentUserId())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot deactivate your own account.",
                    "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate user: " + username + "?\n"
                + "They will not be able to login.",
                "Confirm Deactivate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.deactivateUser(userId)) {
                loadUsers();
                JOptionPane.showMessageDialog(this,
                        username + " has been deactivated.",
                        "Deactivated",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleActivateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to activate.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId   = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        if (userService.activateUser(userId)) {
            loadUsers();
            JOptionPane.showMessageDialog(this,
                    username + " has been activated.",
                    "Activated",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId   = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        if (userId.equals(session.getCurrentUserId())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.",
                    "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete user: " + username + "?\n"
                + "This cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.delete(userId)) {
                loadUsers();
                JOptionPane.showMessageDialog(this,
                        username + " has been deleted.",
                        "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleViewDetails() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        User user = userService.findById(userId);
        if (user == null) return;

        StringBuilder details = new StringBuilder();
        details.append("User Details\n");
        details.append("═══════════════════════════\n");
        details.append("ID       : ").append(user.getId()).append("\n");
        details.append("Username : ").append(user.getUsername()).append("\n");
        details.append("Email    : ").append(user.getEmail()).append("\n");
        details.append("Role     : ").append(user.getRole().getDisplayName()).append("\n");
        details.append("Status   : ").append(user.isActive() ? "Active" : "Inactive").append("\n");
        details.append("Created  : ").append(user.getCreatedDate()).append("\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "User Details — " + user.getUsername(),
                JOptionPane.INFORMATION_MESSAGE);
    }


    private static class RoleCellRenderer
            extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                switch (value.toString()) {
                    case "Admin":
                        setForeground(new Color(180, 40, 40));
                        break;
                    case "Staff":
                        setForeground(new Color(120, 40, 140));
                        break;
                    case "Customer":
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