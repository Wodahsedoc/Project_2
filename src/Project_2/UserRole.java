
package Project_2;

/**
 *
 * @author aneirinblosch
 */
public enum UserRole {

    ADMIN("Admin", "Full system access"),
    STAFF("Staff", "Operational access"),
    CUSTOMER("Customer", "View only access");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName + " - " + description;
    }
}