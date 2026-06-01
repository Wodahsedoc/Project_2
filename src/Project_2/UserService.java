package Project_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService implements Manageable<User> {

    private final UserDAO userDAO;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final Map<String, Integer> failedAttempts
            = new HashMap<>();


    public UserService() {
        this.userDAO = new UserDAO();
        seedDefaultAdmin();
    }


    @Override
    public void add(User user) {
        if (user == null) {
            System.out.println("Cannot add a null user.");
            return;
        }
        if (userDAO.existsByUsername(user.getUsername())) {
            System.out.println("Username '" + user.getUsername()
                    + "' already exists.");
            return;
        }
        if (userDAO.existsByEmail(user.getEmail())) {
            System.out.println("Email '" + user.getEmail()
                    + "' is already registered.");
            return;
        }
        userDAO.insert(user);
        System.out.println("User '" + user.getUsername()
                + "' registered successfully.");
    }

    @Override
    public boolean update(User updatedUser) {
        if (updatedUser == null) return false;
        User existing = userDAO.findById(updatedUser.getId());
        if (existing == null) {
            System.out.println("User not found with ID: "
                    + updatedUser.getId());
            return false;
        }
        User byUsername = userDAO.findByUsername(
                updatedUser.getUsername());
        if (byUsername != null && !byUsername.getId()
                .equals(updatedUser.getId())) {
            System.out.println("Username '"
                    + updatedUser.getUsername()
                    + "' is already taken.");
            return false;
        }
        boolean updated = userDAO.update(updatedUser);
        if (updated) {
            System.out.println("User '" + updatedUser.getUsername()
                    + "' updated successfully.");
        }
        return updated;
    }

    @Override
    public boolean delete(String id) {
        User userToDelete = findById(id);
        if (userToDelete == null) {
            System.out.println("User not found with ID: " + id);
            return false;
        }
        if (userToDelete.isAdmin() && countAdmins() <= 1) {
            System.out.println("Cannot delete the last admin.");
            return false;
        }
        boolean deleted = userDAO.delete(id);
        if (deleted) {
            System.out.println("User deleted successfully.");
        }
        return deleted;
    }

    @Override
    public User findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return userDAO.findById(id);
    }

    @Override
    public List<User> getAll() {
        return userDAO.findAll();
    }


    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        return userDAO.findByUsername(username);
    }

    public boolean authenticate(String username,
                                String plainPassword) {
        User user = findByUsername(username);
        if (user == null) {
            System.out.println("Username not found.");
            return false;
        }
        if (!user.isActive()) {
            System.out.println("Account is deactivated.");
            return false;
        }
        return user.verifyPassword(plainPassword);
    }


    public boolean register(String username, String password,
                            String email, UserRole role) {
        if (!isValidUsername(username)) {
            System.out.println("Invalid username. Must be 3-20 "
                    + "characters, letters and numbers only.");
            return false;
        }
        if (!isValidPassword(password)) {
            System.out.println("Invalid password. Must be at "
                    + "least 6 characters.");
            return false;
        }
        if (userDAO.existsByUsername(username)) {
            System.out.println("Username '" + username
                    + "' is already taken.");
            return false;
        }
        if (userDAO.existsByEmail(email)) {
            System.out.println("Email '" + email
                    + "' is already registered.");
            return false;
        }
        User newUser = new User(username, password, email, role);
        userDAO.insert(newUser);
        System.out.println("Registration successful! Welcome, "
                + username);
        return true;
    }


    public boolean deactivateUser(String id) {
        User user = findById(id);
        if (user == null) {
            System.out.println("User not found with ID: " + id);
            return false;
        }
        if (user.isAdmin() && countAdmins() <= 1) {
            System.out.println("Cannot deactivate the last admin.");
            return false;
        }
        user.setActive(false);
        boolean updated = userDAO.update(user);
        if (updated) {
            System.out.println("User '" + user.getUsername()
                    + "' deactivated.");
        }
        return updated;
    }

    public boolean activateUser(String id) {
        User user = findById(id);
        if (user == null) {
            System.out.println("User not found with ID: " + id);
            return false;
        }
        user.setActive(true);
        boolean updated = userDAO.update(user);
        if (updated) {
            System.out.println("User '" + user.getUsername()
                    + "' activated.");
        }
        return updated;
    }

    public boolean changePassword(String id, String oldPassword,
                                  String newPassword) {
        User user = findById(id);
        if (user == null) {
            System.out.println("User not found.");
            return false;
        }
        if (!user.verifyPassword(oldPassword)) {
            System.out.println("Incorrect current password.");
            return false;
        }
        if (!isValidPassword(newPassword)) {
            System.out.println("New password must be at least "
                    + "6 characters.");
            return false;
        }
        user.setPassword(newPassword);
        boolean updated = userDAO.updatePassword(id,
                user.getPassword());
        if (updated) {
            System.out.println("Password changed successfully.");
        }
        return updated;
    }

    public boolean changeRole(String id, UserRole newRole) {
        User user = findById(id);
        if (user == null) {
            System.out.println("User not found.");
            return false;
        }
        if (user.isAdmin() && newRole != UserRole.ADMIN
                && countAdmins() <= 1) {
            System.out.println("Cannot change role of last admin.");
            return false;
        }
        user.setRole(newRole);
        boolean updated = userDAO.update(user);
        if (updated) {
            System.out.println("Role updated to "
                    + newRole.getDisplayName()
                    + " for user '" + user.getUsername() + "'.");
        }
        return updated;
    }


    public void recordFailedLogin(String username) {
        if (username == null) return;
        String key = username.toLowerCase();
        int count = failedAttempts.getOrDefault(key, 0) + 1;
        failedAttempts.put(key, count);
        if (count >= MAX_FAILED_ATTEMPTS) {
            User user = findByUsername(username);
            if (user != null && user.isActive()) {
                user.setActive(false);
                userDAO.update(user);
                System.out.println("Account '" + username
                        + "' locked after " + MAX_FAILED_ATTEMPTS
                        + " failed attempts.");
            }
        }
    }

    public void clearFailedAttempts(String username) {
        if (username != null) {
            failedAttempts.remove(username.toLowerCase());
        }
    }


    public List<User> findByRole(UserRole role) {
        return userDAO.findByRole(role);
    }

    public List<User> findActiveUsers() {
        List<User> result = new ArrayList<>();
        for (User user : userDAO.findAll()) {
            if (user.isActive()) result.add(user);
        }
        return result;
    }

    public List<User> findInactiveUsers() {
        List<User> result = new ArrayList<>();
        for (User user : userDAO.findAll()) {
            if (!user.isActive()) result.add(user);
        }
        return result;
    }


    public boolean usernameExists(String username) {
        return userDAO.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userDAO.existsByEmail(email);
    }

    private boolean isValidUsername(String username) {
        return username != null
                && username.matches("[a-zA-Z0-9_]{3,20}");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private int countAdmins() {
        return userDAO.countByRole(UserRole.ADMIN);
    }


    private void seedDefaultAdmin() {
        if (userDAO.findAll().isEmpty()) {
            User defaultAdmin = new User(
                "admin", "admin123",
                "admin@inventory.com",
                UserRole.ADMIN
            );
            userDAO.insert(defaultAdmin);
            System.out.println("===================================");
            System.out.println("Default admin account created.");
            System.out.println("Username : admin");
            System.out.println("Password : admin123");
            System.out.println("Please change password after login.");
            System.out.println("===================================");
        }
    }


    public void displayAllUsers() {
        List<User> users = userDAO.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        System.out.printf("%-10s %-20s %-30s %-12s %-10s%n",
                "ID", "Username", "Email", "Role", "Status");
        System.out.println("-".repeat(82));
        for (User user : users) {
            System.out.printf("%-10s %-20s %-30s %-12s %-10s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().getDisplayName(),
                    user.isActive() ? "Active" : "Inactive");
        }
        System.out.println("Total users: " + users.size());
    }

    public void displayUsersByRole(UserRole role) {
        List<User> filtered = findByRole(role);
        if (filtered.isEmpty()) {
            System.out.println("No " + role.getDisplayName()
                    + " users found.");
            return;
        }
        System.out.println("===== " + role.getDisplayName()
                + " Users =====");
        for (User user : filtered) {
            user.displaySummary();
        }
    }

    public void displayStats() {
        System.out.println("===== User Statistics =====");
        List<User> all = userDAO.findAll();
        System.out.println("Total Users   : " + all.size());
        System.out.println("Admins        : "
                + findByRole(UserRole.ADMIN).size());
        System.out.println("Staff         : "
                + findByRole(UserRole.STAFF).size());
        System.out.println("Customers     : "
                + findByRole(UserRole.CUSTOMER).size());
        System.out.println("Active        : "
                + findActiveUsers().size());
        System.out.println("Inactive      : "
                + findInactiveUsers().size());
        System.out.println("===========================");
    }

    @Override
    public String toString() {
        return "UserService[Total Users: "
                + userDAO.findAll().size() + "]";
    }
}