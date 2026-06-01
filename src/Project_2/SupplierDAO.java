package Project_2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO implements DAO<Supplier> {

    private final Connection connection;

    public SupplierDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void insert(Supplier supplier) {
        String sql = "INSERT INTO suppliers (id, created_date, "
                + "supplier_name, contact_person, email, phone, "
                + "address, product_ids, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, supplier.getId());
            stmt.setString(2, supplier.getCreatedDate());
            stmt.setString(3, supplier.getSupplierName());
            stmt.setString(4, supplier.getContactPerson());
            stmt.setString(5, supplier.getEmail());
            stmt.setString(6, supplier.getPhone());
            stmt.setString(7, supplier.getAddress());
            stmt.setString(8, serializeProductIds(
                    supplier.getProductIds()));
            stmt.setBoolean(9, supplier.isActive());
            stmt.executeUpdate();
            System.out.println("Supplier inserted: "
                    + supplier.getSupplierName());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Supplier already exists: "
                        + supplier.getSupplierName());
            } else {
                System.out.println("Error inserting supplier: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE suppliers SET supplier_name = ?, "
                + "contact_person = ?, email = ?, phone = ?, "
                + "address = ?, product_ids = ?, is_active = ? "
                + "WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.setString(6, serializeProductIds(
                    supplier.getProductIds()));
            stmt.setBoolean(7, supplier.isActive());
            stmt.setString(8, supplier.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating supplier: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting supplier: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public Supplier findById(String id) {
        String sql = "SELECT * FROM suppliers WHERE "
                + "UPPER(id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding supplier: "
                    + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all suppliers: "
                    + e.getMessage());
        }
        return suppliers;
    }

    public List<Supplier> findActive() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers "
                + "WHERE is_active = true";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding active suppliers: "
                    + e.getMessage());
        }
        return suppliers;
    }

    public List<Supplier> findInactive() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers "
                + "WHERE is_active = false";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding inactive suppliers: "
                    + e.getMessage());
        }
        return suppliers;
    }

    public List<Supplier> searchByName(String keyword) {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE "
                + "UPPER(supplier_name) LIKE UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error searching suppliers: "
                    + e.getMessage());
        }
        return suppliers;
    }

    public Supplier findByName(String name) {
        String sql = "SELECT * FROM suppliers WHERE "
                + "UPPER(supplier_name) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding supplier by name: "
                    + e.getMessage());
        }
        return null;
    }

    public boolean existsByName(String name, String excludeId) {
        String sql = excludeId != null
                ? "SELECT COUNT(*) FROM suppliers WHERE "
                    + "UPPER(supplier_name) = UPPER(?) AND id != ?"
                : "SELECT COUNT(*) FROM suppliers WHERE "
                    + "UPPER(supplier_name) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            if (excludeId != null) stmt.setString(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking supplier name: "
                    + e.getMessage());
        }
        return false;
    }

    public boolean existsByEmail(String email, String excludeId) {
        String sql = excludeId != null
                ? "SELECT COUNT(*) FROM suppliers WHERE "
                    + "UPPER(email) = UPPER(?) AND id != ?"
                : "SELECT COUNT(*) FROM suppliers WHERE "
                    + "UPPER(email) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            if (excludeId != null) stmt.setString(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking supplier email: "
                    + e.getMessage());
        }
        return false;
    }

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM suppliers "
                + "WHERE is_active = true";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting suppliers: "
                    + e.getMessage());
        }
        return 0;
    }


    private String serializeProductIds(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) return "";
        return String.join("|", productIds);
    }

    private List<String> deserializeProductIds(String productIds) {
        List<String> result = new ArrayList<>();
        if (productIds == null || productIds.trim().isEmpty()) {
            return result;
        }
        for (String id : productIds.split("\\|")) {
            if (!id.trim().isEmpty()) {
                result.add(id.trim());
            }
        }
        return result;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
            rs.getString("id"),
            rs.getString("created_date"),
            rs.getString("supplier_name"),
            rs.getString("contact_person"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            deserializeProductIds(rs.getString("product_ids")),
            rs.getBoolean("is_active")
        );
    }
}