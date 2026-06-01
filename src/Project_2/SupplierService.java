package Project_2;

import java.util.ArrayList;
import java.util.List;

public class SupplierService implements Manageable<Supplier> {

    private final SupplierDAO supplierDAO;


    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
        if (supplierDAO.findAll().isEmpty()) {
            seedSampleSuppliers();
        }
    }


    @Override
    public void add(Supplier supplier) {
        if (supplier == null) {
            System.out.println("Cannot add a null supplier.");
            return;
        }
        if (!supplier.isValidSupplierName()) {
            System.out.println("Invalid supplier name.");
            return;
        }
        if (!supplier.isValidEmail()) {
            System.out.println("Invalid supplier email.");
            return;
        }
        if (!supplier.isValidPhone()) {
            System.out.println("Invalid supplier phone number. "
                             + "Must be 7-15 digits.");
            return;
        }
        if (nameExists(supplier.getSupplierName(), null)) {
            System.out.println("Supplier '" + supplier.getSupplierName()
                             + "' already exists.");
            return;
        }
        if (emailExists(supplier.getEmail(), null)) {
            System.out.println("Email '" + supplier.getEmail()
                             + "' is already registered.");
            return;
        }
        supplierDAO.insert(supplier);
        System.out.println("Supplier '" + supplier.getSupplierName()
                         + "' added successfully.");
    }

    @Override
    public boolean update(Supplier updatedSupplier) {
        if (updatedSupplier == null) return false;
        if (supplierDAO.findById(updatedSupplier.getId()) == null) {
            System.out.println("Supplier not found with ID: "
                    + updatedSupplier.getId());
            return false;
        }
        if (nameExists(updatedSupplier.getSupplierName(),
                updatedSupplier.getId())) {
            System.out.println("Supplier name '"
                    + updatedSupplier.getSupplierName()
                    + "' already exists.");
            return false;
        }
        if (emailExists(updatedSupplier.getEmail(),
                updatedSupplier.getId())) {
            System.out.println("Email '" + updatedSupplier.getEmail()
                    + "' is already registered.");
            return false;
        }
        boolean updated = supplierDAO.update(updatedSupplier);
        if (updated) {
            System.out.println("Supplier '"
                    + updatedSupplier.getSupplierName()
                    + "' updated successfully.");
        }
        return updated;
    }

    @Override
    public boolean delete(String id) {
        Supplier supplier = findById(id);
        if (supplier == null) {
            System.out.println("Supplier not found with ID: " + id);
            return false;
        }
        if (supplier.getTotalProductsSupplied() > 0) {
            System.out.println("Cannot delete supplier '"
                    + supplier.getSupplierName()
                    + "'. They still supply "
                    + supplier.getTotalProductsSupplied()
                    + " product(s). Reassign or remove products first.");
            return false;
        }

        supplier.deactivate();
        boolean updated = supplierDAO.update(supplier);
        if (updated) {
            System.out.println("Supplier '" + supplier.getSupplierName()
                             + "' has been deactivated.");
        }
        return updated;
    }

    @Override
    public Supplier findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return supplierDAO.findById(id);
    }

    @Override
    public List<Supplier> getAll() {
        return supplierDAO.findAll();
    }


    public boolean hardDelete(String id) {
        if (supplierDAO.findById(id) == null) {
            System.out.println("Supplier not found with ID: " + id);
            return false;
        }
        boolean deleted = supplierDAO.delete(id);
        if (deleted) {
            System.out.println("Supplier permanently deleted.");
        }
        return deleted;
    }


    public boolean linkProduct(String supplierId, String productId) {
        Supplier supplier = findById(supplierId);
        if (supplier == null) {
            System.out.println("Supplier not found with ID: " + supplierId);
            return false;
        }
        supplier.addProduct(productId);
        supplierDAO.update(supplier);
        System.out.println("Product " + productId
                         + " linked to supplier '"
                         + supplier.getSupplierName() + "'.");
        return true;
    }

    public boolean unlinkProduct(String supplierId, String productId) {
        Supplier supplier = findById(supplierId);
        if (supplier == null) {
            System.out.println("Supplier not found with ID: " + supplierId);
            return false;
        }
        supplier.removeProduct(productId);
        supplierDAO.update(supplier);
        System.out.println("Product " + productId
                         + " unlinked from supplier '"
                         + supplier.getSupplierName() + "'.");
        return true;
    }

    public Supplier findSupplierByProduct(String productId) {
        for (Supplier supplier : supplierDAO.findAll()) {
            if (supplier.suppliesProduct(productId)) {
                return supplier;
            }
        }
        return null;
    }


    public Supplier findByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        return supplierDAO.findByName(name);
    }

    public List<Supplier> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return new ArrayList<>();
        }
        List<Supplier> result = supplierDAO.searchByName(
                keyword.trim());
        if (result.isEmpty()) {
            System.out.println("No suppliers found matching: "
                    + keyword);
        }
        return result;
    }

    public List<Supplier> getActiveSuppliers() {
        return supplierDAO.findActive();
    }

    public List<Supplier> getInactiveSuppliers() {
        return supplierDAO.findInactive();
    }

    public List<Supplier> getSuppliersWithNoProducts() {
        List<Supplier> result = new ArrayList<>();
        for (Supplier supplier : supplierDAO.findActive()) {
            if (supplier.getTotalProductsSupplied() == 0) {
                result.add(supplier);
            }
        }
        return result;
    }


    public boolean activateSupplier(String id) {
        Supplier supplier = findById(id);
        if (supplier == null) {
            System.out.println("Supplier not found with ID: " + id);
            return false;
        }
        supplier.activate();
        boolean updated = supplierDAO.update(supplier);
        if (updated) {
            System.out.println("Supplier '" + supplier.getSupplierName()
                             + "' has been activated.");
        }
        return updated;
    }


    private boolean nameExists(String name, String excludeId) {
        return supplierDAO.existsByName(name, excludeId);
    }

    private boolean emailExists(String email, String excludeId) {
        return supplierDAO.existsByEmail(email, excludeId);
    }

    public boolean supplierExists(String id) {
        return findById(id) != null;
    }


    public int getTotalSuppliers() {
        return supplierDAO.findAll().size();
    }

    public int getTotalActiveSuppliers() {
        return supplierDAO.countActive();
    }

    public Supplier getMostProductiveSupplier() {
        List<Supplier> active = getActiveSuppliers();
        if (active.isEmpty()) return null;
        Supplier most = active.get(0);
        for (Supplier s : active) {
            if (s.getTotalProductsSupplied()
                    > most.getTotalProductsSupplied()) {
                most = s;
            }
        }
        return most;
    }


    private void seedSampleSuppliers() {
        System.out.println("Seeding sample suppliers...");

        Supplier s1 = new Supplier(
            "TechCorp", "John Smith",
            "john@techcorp.com", "0211234567",
            "123 Queen St, Auckland"
        );
        s1.setId("SUP001");
        add(s1);

        Supplier s2 = new Supplier(
            "FoodSupplies Ltd", "Mary Jones",
            "mary@foodsupplies.co.nz", "0221234567",
            "456 King St, Wellington"
        );
        s2.setId("SUP002");
        add(s2);

        Supplier s3 = new Supplier(
            "General Goods NZ", "Bob Wilson",
            "bob@generalgoods.co.nz", "0271234567",
            "789 High St, Christchurch"
        );
        s3.setId("SUP003");
        add(s3);

        System.out.println("Sample suppliers loaded.");
    }


    public void displayAllSuppliers() {
        List<Supplier> active = getActiveSuppliers();
        if (active.isEmpty()) {
            System.out.println("No active suppliers found.");
            return;
        }
        Supplier.displayTableHeader();
        for (Supplier supplier : active) {
            supplier.displaySummary();
        }
        System.out.println("\nTotal active suppliers: " + active.size());
    }

    public void displaySearchResults(String keyword) {
        List<Supplier> results = searchByName(keyword);
        if (results.isEmpty()) return;
        System.out.println("===== Search Results: '"
                + keyword + "' =====");
        Supplier.displayTableHeader();
        for (Supplier supplier : results) {
            supplier.displaySummary();
        }
        System.out.println("Found: " + results.size()
                + " supplier(s)");
    }

    public void displayStats() {
        System.out.println("===== Supplier Statistics =====");
        System.out.println("Total Suppliers  : " + getTotalSuppliers());
        System.out.println("Active           : "
                + getTotalActiveSuppliers());
        System.out.println("Inactive         : "
                + getInactiveSuppliers().size());
        System.out.println("No Products      : "
                + getSuppliersWithNoProducts().size());
        Supplier most = getMostProductiveSupplier();
        if (most != null) {
            System.out.println("Most Products    : "
                    + most.getSupplierName()
                    + " (" + most.getTotalProductsSupplied()
                    + " products)");
        }
        System.out.println("===============================");
    }

    @Override
    public String toString() {
        return "SupplierService[Total Suppliers: "
                + supplierDAO.findAll().size() + "]";
    }
}
