package Project_2;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class User extends AbstractEntity {

    private String username;
    private String password;
    private String email;
    private UserRole role;
    private boolean isActive;


    public User(String username, String password, String email, UserRole role) {
        super();
        this.username = username;
        this.password = hashPassword(password);
        this.email = email;
        this.role = role;
        this.isActive = true;
    }


    public User(String id, String createdDate, String username,
                String password, String email, UserRole role, boolean isActive) {
        super(id, createdDate);
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }


    private String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return plainPassword;
        }
    }

    public boolean verifyPassword(String plainPassword) {
        return this.password.equals(hashPassword(plainPassword));
    }

    public void setPassword(String plainPassword) {
        this.password = hashPassword(plainPassword);
    }
    public String getPassword() {
        return password;
    }


    public boolean hasPermission(UserRole requiredRole) {
        switch (requiredRole) {
            case CUSTOMER: return true;
            case STAFF:    return this.role == UserRole.STAFF || this.role == UserRole.ADMIN;
            case ADMIN:    return this.role == UserRole.ADMIN;
            default:       return false;
        }
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isStaff() {
        return this.role == UserRole.STAFF;
    }

    public boolean isCustomer() {
        return this.role == UserRole.CUSTOMER;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }


    @Override
    public void displayDetails() {
        System.out.println("==========================");
        System.out.println("User ID   : " + getId());
        System.out.println("Username  : " + username);
        System.out.println("Email     : " + email);
        System.out.println("Role      : " + role.getDisplayName());
        System.out.println("Active    : " + (isActive ? "Yes" : "No"));
        System.out.println("Created   : " + getCreatedDate());
        System.out.println("==========================");
    }

    public void displaySummary() {
        System.out.printf("%-10s %-20s %-30s %-12s %-10s%n",
                getId(), username, email,
                role.getDisplayName(),
                isActive ? "Active" : "Inactive");
    }


    public String toFileString() {
        return getId() + "," + getCreatedDate() + "," + username + ","
             + password + "," + email + "," + role.name() + "," + isActive;
    }

    public static User fromFileString(String line) {
        String[] parts = line.split(",");
        return new User(
            parts[0],
            parts[1],
            parts[2],
            parts[3],
            parts[4],
            UserRole.valueOf(parts[5]),
            Boolean.parseBoolean(parts[6])
        );
    }

    @Override
    public String toString() {
        return "User[" + getId() + "] " + username + " | " + role.getDisplayName();
    }
}
