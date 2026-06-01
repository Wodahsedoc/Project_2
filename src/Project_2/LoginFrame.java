package Project_2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private final Session session;
    private final UserService userService;


    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel roleLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel errorLabel;
    private JComboBox<String> roleComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton guestButton;


    public LoginFrame() {
        this.session = Session.getInstance();
        this.userService = new UserService();
        initComponents();
        setupFrame();
    }


    private void setupFrame() {
        setTitle("Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }


    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());

        buildLeftPanel();
        buildRightPanel();

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }


    private void buildLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.setBackground(new Color(30, 80, 140));
        leftPanel.setPreferredSize(new Dimension(280, 500));
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;


        JLabel iconLabel = new JLabel("📦");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        gbc.gridy = 0;
        leftPanel.add(iconLabel, gbc);


        titleLabel = new JLabel("Inventory");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        leftPanel.add(titleLabel, gbc);

        JLabel titleLabel2 = new JLabel("Management");
        titleLabel2.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel2.setForeground(Color.WHITE);
        gbc.gridy = 2;
        leftPanel.add(titleLabel2, gbc);

        JLabel titleLabel3 = new JLabel("System");
        titleLabel3.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel3.setForeground(Color.WHITE);
        gbc.gridy = 3;
        leftPanel.add(titleLabel3, gbc);


        subtitleLabel = new JLabel("COMP603 Project 2");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(180, 210, 255));
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 20, 5, 20);
        leftPanel.add(subtitleLabel, gbc);

        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(150, 190, 240));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 20, 10, 20);
        leftPanel.add(versionLabel, gbc);
    }


    private void buildRightPanel() {
        rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 40, 8, 40);


        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(30, 80, 140));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 40, 5, 40);
        rightPanel.add(welcomeLabel, gbc);

        JLabel signInLabel = new JLabel("Sign in to your account");
        signInLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        signInLabel.setForeground(new Color(120, 120, 120));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 20, 40);
        rightPanel.add(signInLabel, gbc);


        roleLabel = new JLabel("Login As");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roleLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 40, 2, 40);
        rightPanel.add(roleLabel, gbc);

        roleComboBox = new JComboBox<>(new String[]{"Admin", "Staff"});
        roleComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setPreferredSize(new Dimension(300, 35));
        roleComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 40, 8, 40);
        rightPanel.add(roleComboBox, gbc);


        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        usernameLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 40, 2, 40);
        rightPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        usernameField.setPreferredSize(new Dimension(300, 35));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 8, 40);
        rightPanel.add(usernameField, gbc);


        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passwordLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 40, 2, 40);
        rightPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setPreferredSize(new Dimension(300, 35));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 40, 8, 40);
        rightPanel.add(passwordField, gbc);


        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(200, 50, 50));
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 40, 5, 40);
        rightPanel.add(errorLabel, gbc);


        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(30, 80, 140));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(300, 40));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        gbc.gridy = 9;
        gbc.insets = new Insets(10, 40, 5, 40);
        rightPanel.add(loginButton, gbc);


        guestButton = new JButton("Continue as Customer");
        guestButton.setFont(new Font("Arial", Font.PLAIN, 13));
        guestButton.setBackground(new Color(240, 240, 240));
        guestButton.setForeground(new Color(80, 80, 80));
        guestButton.setPreferredSize(new Dimension(300, 35));
        guestButton.setFocusPainted(false);
        guestButton.setBorderPainted(false);
        guestButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guestButton.addActionListener(e -> handleGuestLogin());
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 40, 30, 40);
        rightPanel.add(guestButton, gbc);


        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });


        roleComboBox.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setText("");
            usernameField.requestFocus();
        });
    }


    private void handleLogin() {
        String selectedRole = (String) roleComboBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());


        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }


        User user = userService.findByUsername(username);
        if (user == null) {
            showError("Username not found.");
            usernameField.requestFocus();
            return;
        }


        UserRole expectedRole = selectedRole.equals("Admin")
                ? UserRole.ADMIN : UserRole.STAFF;
        if (user.getRole() != expectedRole) {
            showError("This account does not have "
                    + selectedRole + " access.");
            return;
        }


        if (session.login(user, password)) {
            openMainFrame();
        } else {
            showError("Incorrect password. Please try again.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void handleGuestLogin() {
        User guestUser = new User(
            "guest", "guest",
            "guest@temp.com",
            UserRole.CUSTOMER
        );
        session.loginAsGuest(guestUser);
        openMainFrame();
    }

    private void openMainFrame() {
        dispose();
        new MainFrame(userService);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}