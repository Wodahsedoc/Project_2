
package Project_2;

/**
 *
 * @author aneirinblosch
 */
import java.util.ArrayList;
import java.util.List;

public class Supplier extends AbstractEntity {

    private String supplierName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private List<String> productIds;
    private boolean isActive;


    public Supplier(String supplierName, String contactPerson,
                    String email, String phone, String address) {
        super();
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.productIds = new ArrayList<>();
        this.isActive = true;
    }


    public Supplier(String id, String createdDate, String supplierName,
                    String contactPerson, String email, String phone,
                    String address, List<String> productIds, boolean isActive) {
        super(id, createdDate);
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.productIds = productIds != null ? productIds : new ArrayList<>();
        this.isActive = isActive;
    }


    public void addProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be empty");
        }
        if (!productIds.contains(productId)) {
            productIds.add(productId);
        } else {
            System.out.println("Product " + productId + " is already linked to this supplier");
        }
    }

    public void removeProduct(String productId) {
        if (!productIds.remove(productId)) {
            System.out.println("Product " + productId + " not found for this supplier");
        }
    }

    public boolean suppliesProduct(String productId) {
        return productIds.contains(productId);
    }

    public int getTotalProductsSupplied() {
        return productIds.size();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }


    public boolean isValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean isValidPhone() {
        return phone != null && phone.matches("\\d{7,15}");
    }

    public boolean isValidSupplierName() {
        return supplierName != null && !supplierName.trim().isEmpty();
    }


    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        if (supplierName == null || supplierName.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be empty");
        }
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getProductIds() {
        return new ArrayList<>(productIds);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    @Override
    public void displayDetails() {
        System.out.println("==========================");
        System.out.println("Supplier ID      : " + getId());
        System.out.println("Name             : " + supplierName);
        System.out.println("Contact Person   : " + contactPerson);
        System.out.println("Email            : " + email);
        System.out.println("Phone            : " + phone);
        System.out.println("Address          : " + address);
        System.out.println("Products Supplied: " + getTotalProductsSupplied());
        System.out.println("Status           : " + (isActive ? "Active" : "Inactive"));
        System.out.println("Created          : " + getCreatedDate());
        System.out.println("==========================");
    }


    public void displaySummary() {
        System.out.printf("%-10s %-20s %-25s %-15s %-10s%n",
                getId(),
                supplierName,
                email,
                phone,
                isActive ? "Active" : "Inactive");
    }


    public static void displayTableHeader() {
        System.out.printf("%-10s %-20s %-25s %-15s %-10s%n",
                "ID", "Supplier Name", "Email", "Phone", "Status");
        System.out.println("-".repeat(80));
    }

    @Override
    public String toString() {
        return "Supplier[" + getId() + "] " + supplierName
             + " | Contact: " + contactPerson
             + " | Products: " + getTotalProductsSupplied();
    }
}