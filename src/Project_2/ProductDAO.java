package Project_2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements DAO<Product> {

    private final Connection connection;

    public ProductDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void insert(Product product) {
        String sql = "INSERT INTO products (id, created_date, name, "
                + "description, price, category, supplier_id, "
                + "is_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, product.getId());
            stmt.setString(2, product.getCreatedDate());
            stmt.setString(3, product.getName());
            stmt.setString(4, product.getDescription());
            stmt.setDouble(5, product.getPrice());
            stmt.setString(6, product.getCategory().name());
            stmt.setString(7, product.getSupplierId());
            stmt.setBoolean(8, product.isAvailable());
            stmt.executeUpdate();
            System.out.println("Product inserted: " + product.getName());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Product already exists: "
                        + product.getName());
            } else {
                System.out.println("Error inserting product: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, "
                + "price = ?, category = ?, supplier_id = ?, "
                + "is_available = ? WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setString(4, product.getCategory().name());
            stmt.setString(5, product.getSupplierId());
            stmt.setBoolean(6, product.isAvailable());
            stmt.setString(7, product.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating product: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting product: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public Product findById(String id) {
        String sql = "SELECT * FROM products WHERE "
                + "UPPER(id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding product: "
                    + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all products: "
                    + e.getMessage());
        }
        return products;
    }

    public List<Product> findAvailable() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_available = true";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding available products: "
                    + e.getMessage());
        }
        return products;
    }

    public List<Product> findByCategory(Category category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? "
                + "AND is_available = true";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, category.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding products by category: "
                    + e.getMessage());
        }
        return products;
    }

    public List<Product> findBySupplier(String supplierId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE supplier_id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding products by supplier: "
                    + e.getMessage());
        }
        return products;
    }

    public List<Product> searchByName(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE "
                + "UPPER(name) LIKE UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error searching products: "
                    + e.getMessage());
        }
        return products;
    }

    public List<Product> findByPriceRange(double min, double max) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE price >= ? "
                + "AND price <= ? AND is_available = true";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding products by price: "
                    + e.getMessage());
        }
        return products;
    }

    public boolean existsByName(String name, String excludeId) {
        String sql = excludeId != null
                ? "SELECT COUNT(*) FROM products WHERE "
                    + "UPPER(name) = UPPER(?) AND id != ?"
                : "SELECT COUNT(*) FROM products WHERE "
                    + "UPPER(name) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            if (excludeId != null) stmt.setString(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking product name: "
                    + e.getMessage());
        }
        return false;
    }

    public int countAvailable() {
        String sql = "SELECT COUNT(*) FROM products "
                + "WHERE is_available = true";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting products: "
                    + e.getMessage());
        }
        return 0;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getString("id"),
            rs.getString("created_date"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            Category.valueOf(rs.getString("category")),
            rs.getString("supplier_id"),
            rs.getBoolean("is_available")
        );
    }
}