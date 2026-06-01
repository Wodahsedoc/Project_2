package Project_2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private final Session session;
    private final UserService userService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final StockService stockService;
    private final OrderService orderService;


    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private CardLayout cardLayout;


    private JButton dashboardBtn;
    private JButton productsBtn;
    private JButton stockBtn;
    private JButton suppliersBtn;
    private JButton ordersBtn;
    private JButton reportsBtn;
    private JButton usersBtn;
    private JButton logoutBtn;


    private static final String DASHBOARD  = "Dashboard";
    private static final String PRODUCTS   = "Products";
    private static final String STOCK      = "Stock";
    private static final String SUPPLIERS  = "Suppliers";
    private static final String ORDERS     = "Orders";
    private static final String REPORTS    = "Reports";
    private static final String USERS      = "Users";


    private static final Color SIDEBAR_BG       = new Color(25, 55, 100);
    private static final Color SIDEBAR_HOVER    = new Color(40, 80, 140);
    private static final Color SIDEBAR_ACTIVE   = new Color(50, 100, 180);
    private static final Color SIDEBAR_TEXT     = new Color(200, 220, 255);
    private static final Color HEADER_BG        = new Color(245, 247, 250);
    private static final Color CONTENT_BG       = new Color(245, 247, 250);
    private static final Color ACCENT           = new Color(30, 80, 140);


    private JButton activeButton = null;


    public MainFrame(UserService userService) {
        this.session        = Session.getInstance();
        this.userService    = userService;
        this.productService = new ProductService();
        this.supplierService = new SupplierService();
        this.stockService   = new StockService(productService);
        this.orderService   = new OrderService(stockService);

        initComponents();
        setupFrame();


        showPanel(DASHBOARD);
        setActiveButton(dashboardBtn);
    }


    private void setupFrame() {
        setTitle("Inventory Management System — "
                + session.getCurrentUsername()
                + " ["
                + session.getCurrentUserRole().getDisplayName()
                + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }


    private void initComponents() {
        setLayout(new BorderLayout());

        buildHeader();
        buildSidebar();
        buildContentPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }


    private void buildHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(1200, 55));
        headerPanel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(220, 220, 220)));


        JLabel titleLabel = new JLabel("  📦  Inventory Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ACCENT);
        headerPanel.add(titleLabel, BorderLayout.WEST);


        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        rightPanel.setBackground(Color.WHITE);


        if (stockService.hasLowStockAlerts()) {
            JLabel alertLabel = new JLabel(
                "⚠ " + stockService.getTotalLowStockCount() + " Low Stock");
            alertLabel.setFont(new Font("Arial", Font.BOLD, 12));
            alertLabel.setForeground(new Color(200, 100, 0));
            alertLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 100, 0)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            rightPanel.add(alertLabel);
        }


        String roleDisplay = session.isGuest()
                ? "Customer"
                : session.getCurrentUsername()
                + " | " + session.getCurrentUserRole().getDisplayName();
        JLabel userLabel = new JLabel("👤 " + roleDisplay);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        userLabel.setForeground(new Color(80, 80, 80));
        rightPanel.add(userLabel);

        headerPanel.add(rightPanel, BorderLayout.EAST);
    }


    private void buildSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(200, 750));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));


        JPanel sidebarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        sidebarHeader.setBackground(SIDEBAR_BG);
        sidebarHeader.setMaximumSize(new Dimension(200, 55));
        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 11));
        menuLabel.setForeground(new Color(140, 170, 220));
        sidebarHeader.add(menuLabel);
        sidebarPanel.add(sidebarHeader);


        dashboardBtn  = createSidebarButton("🏠  Dashboard",  DASHBOARD);
        productsBtn   = createSidebarButton("📦  Products",   PRODUCTS);
        stockBtn      = createSidebarButton("📊  Stock",      STOCK);

        sidebarPanel.add(dashboardBtn);
        sidebarPanel.add(productsBtn);
        sidebarPanel.add(stockBtn);


        if (session.isAdminOrStaff()) {
            suppliersBtn = createSidebarButton("🏭  Suppliers", SUPPLIERS);
            ordersBtn    = createSidebarButton("📋  Orders",    ORDERS);
            reportsBtn   = createSidebarButton("📄  Reports",   REPORTS);
            sidebarPanel.add(suppliersBtn);
            sidebarPanel.add(ordersBtn);
            sidebarPanel.add(reportsBtn);
        }


        if (session.isAdmin()) {
            usersBtn = createSidebarButton("👥  Users", USERS);
            sidebarPanel.add(usersBtn);
        }


        sidebarPanel.add(Box.createVerticalGlue());


        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(60, 90, 140));
        separator.setMaximumSize(new Dimension(200, 1));
        sidebarPanel.add(separator);


        logoutBtn = new JButton("🚪  Logout");
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        logoutBtn.setForeground(new Color(255, 150, 150));
        logoutBtn.setBackground(SIDEBAR_BG);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(200, 45));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(120, 30, 30));
            }
            @Override public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(SIDEBAR_BG);
            }
        });
        logoutBtn.addActionListener(e -> handleLogout());
        sidebarPanel.add(logoutBtn);
        sidebarPanel.add(Box.createVerticalStrut(10));
    }

    private JButton createSidebarButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        btn.addActionListener(e -> {
            showPanel(panelName);
            setActiveButton(btn);
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
            activeButton.setFont(new Font("Arial", Font.PLAIN, 13));
        }
        activeButton = btn;
        activeButton.setBackground(SIDEBAR_ACTIVE);
        activeButton.setFont(new Font("Arial", Font.BOLD, 13));
    }


    private void buildContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);


        contentPanel.add(buildDashboardPanel(), DASHBOARD);
        contentPanel.add(new ProductPanel(productService,
                supplierService, stockService, session), PRODUCTS);
        contentPanel.add(new StockPanel(stockService,
                productService, session), STOCK);

        if (session.isAdminOrStaff()) {
            contentPanel.add(new SupplierPanel(supplierService,
                    productService, session), SUPPLIERS);
            contentPanel.add(new OrderPanel(orderService,
                    productService, supplierService,
                    stockService, session), ORDERS);
            contentPanel.add(new ReportsPanel(orderService,
                    stockService, productService,
                    supplierService, session), REPORTS);
        }

        if (session.isAdmin()) {
            contentPanel.add(new UserManagementPanel(
                    userService, session), USERS);
        }
    }

    private void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }


    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));


        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 22));
        pageTitle.setForeground(ACCENT);
        panel.add(pageTitle, BorderLayout.NORTH);


        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(CONTENT_BG);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        statsPanel.setPreferredSize(new Dimension(0, 130));

        statsPanel.add(buildStatCard("Total Products",
                String.valueOf(productService.getTotalAvailable()),
                new Color(30, 80, 140), "📦"));
        statsPanel.add(buildStatCard("Low Stock Items",
                String.valueOf(stockService.getTotalLowStockCount()),
                new Color(200, 100, 0), "⚠"));
        statsPanel.add(buildStatCard("Total Suppliers",
                String.valueOf(supplierService.getTotalActiveSuppliers()),
                new Color(40, 140, 80), "🏭"));
        statsPanel.add(buildStatCard("Total Orders",
                String.valueOf(orderService.getTotalOrders()),
                new Color(120, 40, 140), "📋"));


        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBackground(CONTENT_BG);
        bottomPanel.add(buildLowStockWidget());
        bottomPanel.add(buildQuickInfoWidget());


        JPanel mainContent = new JPanel(new BorderLayout(0, 15));
        mainContent.setBackground(CONTENT_BG);
        mainContent.add(statsPanel, BorderLayout.NORTH);
        mainContent.add(bottomPanel, BorderLayout.CENTER);

        panel.add(mainContent, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatCard(String title, String value,
                                  Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(0, 120));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        card.add(iconLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(120, 120, 120));
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildLowStockWidget() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("⚠ Low Stock Alerts");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(200, 100, 0));
        panel.add(title, BorderLayout.NORTH);

        java.util.List<StockItem> lowStock = stockService.getLowStockItems();
        if (lowStock.isEmpty()) {
            JLabel noAlerts = new JLabel("✅ All stock levels are healthy");
            noAlerts.setFont(new Font("Arial", Font.PLAIN, 13));
            noAlerts.setForeground(new Color(40, 140, 80));
            noAlerts.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            panel.add(noAlerts, BorderLayout.CENTER);
        } else {
            String[] columns = {"Product ID", "Qty", "Min", "Status"};
            String[][] data = new String[Math.min(lowStock.size(), 5)][4];
            for (int i = 0; i < data.length; i++) {
                StockItem item = lowStock.get(i);
                data[i][0] = item.getProductId();
                data[i][1] = String.valueOf(item.getQuantityInStock());
                data[i][2] = String.valueOf(item.getMinimumThreshold());
                data[i][3] = item.getStockStatus();
            }
            JTable table = new JTable(data, columns);
            table.setFont(new Font("Arial", Font.PLAIN, 12));
            table.setRowHeight(25);
            table.setEnabled(false);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            panel.add(scroll, BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel buildQuickInfoWidget() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("📊 System Summary");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(ACCENT);
        panel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addInfoRow(infoPanel, "Available Products:",
                String.valueOf(productService.getTotalAvailable()));
        addInfoRow(infoPanel, "Out of Stock:",
                String.valueOf(stockService.getTotalOutOfStockCount()));
        addInfoRow(infoPanel, "Active Suppliers:",
                String.valueOf(supplierService.getTotalActiveSuppliers()));
        addInfoRow(infoPanel, "Pending Orders:",
                String.valueOf(orderService.getPendingOrders().size()));
        addInfoRow(infoPanel, "Delivered Orders:",
                String.valueOf(orderService.getDeliveredOrders().size()));
        addInfoRow(infoPanel, "Total Revenue:",
                orderService.getFormattedTotalRevenue());

        panel.add(infoPanel, BorderLayout.CENTER);


        JLabel userInfo = new JLabel("Logged in: "
                + (session.isGuest() ? "Customer (Guest)"
                : session.getCurrentUsername()
                + " — " + session.getCurrentUserRole().getDisplayName()));
        userInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        userInfo.setForeground(new Color(150, 150, 150));
        panel.add(userInfo, BorderLayout.SOUTH);

        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComp.setForeground(new Color(80, 80, 80));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.BOLD, 12));
        valueComp.setForeground(new Color(30, 80, 140));

        panel.add(labelComp);
        panel.add(valueComp);
    }


    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            session.logout();
            dispose();
            new LoginFrame();
        }
    }
}