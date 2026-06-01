
package Project_2;

/**
 *
 * @author aneirinblosch
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Session {

    private static Session instance;

    private User currentUser;
    private String loginTime;
    private String lastActivityTime;
    private boolean isGuest;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");


    private Session() {
        this.currentUser = null;
        this.loginTime = null;
        this.lastActivityTime = null;
        this.isGuest = false;
    }


    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }


    public boolean login(User user, String plainPassword) {
        if (user == null) {
            System.out.println("User not found.");
            return false;
        }

        if (!user.isActive()) {
            System.out.println("This account has been deactivated. "
                             + "Please contact an administrator.");
            return false;
        }

        if (!user.verifyPassword(plainPassword)) {
            System.out.println("Incorrect password.");
            return false;
        }

        this.currentUser = user;
        this.loginTime = getCurrentDateTime();
        this.lastActivityTime = loginTime;
        System.out.println("Welcome, " + user.getUsername()
                         + "! Logged in as " + user.getRole().getDisplayName());
        return true;
    }

    public boolean loginAsGuest(User guestUser) {
        this.currentUser = guestUser;
        this.loginTime = getCurrentDateTime();
        this.lastActivityTime = loginTime;
        this.isGuest = true;
        System.out.println("Welcome! You are browsing as a Customer.");
        return true;
}

    public boolean isGuest() {
        return isGuest;
    }

    public void logout() {
        if (currentUser == null) {
            System.out.println("No user is currently logged in.");
            return;
        }
        if (isGuest) {
            System.out.println("Customer session ended. Goodbye!");
        } else {
            System.out.println("Goodbye, " + currentUser.getUsername()
                             + "! You have been logged out.");
        }
        this.currentUser = null;
        this.loginTime = null;
        this.lastActivityTime = null;
        this.isGuest = false;
        }


    public void updateActivity() {
        if (isLoggedIn()) {
            this.lastActivityTime = getCurrentDateTime();
        }
    }


    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }

    public boolean isStaff() {
        return isLoggedIn() && currentUser.isStaff();
    }

    public boolean isCustomer() {
        return isLoggedIn() && currentUser.isCustomer();
    }

    public boolean isAdminOrStaff() {
        return isAdmin() || isStaff();
    }

    public boolean hasPermission(UserRole requiredRole) {
        if (!isLoggedIn()) {
            System.out.println("Access denied. Please log in first.");
            return false;
        }
        boolean permitted = currentUser.hasPermission(requiredRole);
        if (!permitted) {
            System.out.println("Access denied. This action requires "
                             + requiredRole.getDisplayName() + " privileges.");
        }
        return permitted;
    }


    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserId() {
        return isLoggedIn() ? currentUser.getId() : null;
    }

    public String getCurrentUsername() {
        return isLoggedIn() ? currentUser.getUsername() : null;
    }

    public UserRole getCurrentUserRole() {
        return isLoggedIn() ? currentUser.getRole() : null;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public String getLastActivityTime() {
        return lastActivityTime;
    }


    public void displaySessionInfo() {
        System.out.println("==========================");
        if (isLoggedIn()) {
            System.out.println("Session Status  : Active");
            System.out.println("User            : " + currentUser.getUsername());
            System.out.println("Role            : " + currentUser.getRole().getDisplayName());
            System.out.println("Login Time      : " + loginTime);
            System.out.println("Last Activity   : " + lastActivityTime);
        } else {
            System.out.println("Session Status  : No active session");
        }
        System.out.println("==========================");
    }


    private String getCurrentDateTime() {
        return LocalDateTime.now().format(FORMATTER);
    }

    @Override
    public String toString() {
        if (isLoggedIn()) {
            return "Session[Active] User: " + currentUser.getUsername()
                 + " | Role: " + currentUser.getRole().getDisplayName()
                 + " | Since: " + loginTime;
        }
        return "Session[Inactive]";
    }
}