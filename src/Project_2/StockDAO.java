package Project_2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO implements DAO<StockItem> {

    private final Connection connection;

    public StockDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void insert(StockItem item) {
        String sql = "INSERT INTO stock_items (id, created_date, "
                + "product_id, quantity_in_stock, minimum_threshold, "
                + "maximum_capacity, last_restocked, last_updated) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getCreatedDate());
            stmt.setString(3, item.getProductId());
            stmt.setInt(4, item.getQuantityInStock());
            stmt.setInt(5, item.getMinimumThreshold());
            stmt.setInt(6, item.getMaximumCapacity());
            stmt.setString(7, item.getLastRestocked());
            stmt.setString(8, item.getLastUpdated());
            stmt.executeUpdate();
            System.out.println("Stock item inserted for product: "
                    + item.getProductId());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Stock item already exists for: "
                        + item.getProductId());
            } else {
                System.out.println("Error inserting stock item: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean update(StockItem item) {
        String sql = "UPDATE stock_items SET quantity_in_stock = ?, "
                + "minimum_threshold = ?, maximum_capacity = ?, "
                + "last_restocked = ?, last_updated = ? "
                + "WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setInt(1, item.getQuantityInStock());
            stmt.setInt(2, item.getMinimumThreshold());
            stmt.setInt(3, item.getMaximumCapacity());
            stmt.setString(4, item.getLastRestocked());
            stmt.setString(5, item.getLastUpdated());
            stmt.setString(6, item.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating stock item: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM stock_items WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting stock item: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public StockItem findById(String id) {
        String sql = "SELECT * FROM stock_items WHERE "
                + "UPPER(id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding stock item: "
                    + e.getMessage());
        }
        return null;
    }

    @Override
    public List<StockItem> findAll() {
        List<StockItem> items = new ArrayList<>();
        String sql = "SELECT * FROM stock_items";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all stock items: "
                    + e.getMessage());
        }
        return items;
    }

    public StockItem findByProductId(String productId) {
        String sql = "SELECT * FROM stock_items WHERE "
                + "UPPER(product_id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding stock by product: "
                    + e.getMessage());
        }
        return null;
    }

    public List<StockItem> findLowStock() {
        List<StockItem> items = new ArrayList<>();
        String sql = "SELECT * FROM stock_items WHERE "
                + "quantity_in_stock <= minimum_threshold";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding low stock items: "
                    + e.getMessage());
        }
        return items;
    }

    public List<StockItem> findOutOfStock() {
        List<StockItem> items = new ArrayList<>();
        String sql = "SELECT * FROM stock_items WHERE "
                + "quantity_in_stock = 0";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding out of stock items: "
                    + e.getMessage());
        }
        return items;
    }

    public List<StockItem> findFullyStocked() {
        List<StockItem> items = new ArrayList<>();
        String sql = "SELECT * FROM stock_items WHERE "
                + "quantity_in_stock >= maximum_capacity";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding fully stocked items: "
                    + e.getMessage());
        }
        return items;
    }

    public boolean existsForProduct(String productId) {
        String sql = "SELECT COUNT(*) FROM stock_items WHERE "
                + "UPPER(product_id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking stock existence: "
                    + e.getMessage());
        }
        return false;
    }

    public int getTotalUnits() {
        String sql = "SELECT SUM(quantity_in_stock) "
                + "FROM stock_items";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error getting total units: "
                    + e.getMessage());
        }
        return 0;
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM stock_items WHERE "
                + "quantity_in_stock <= minimum_threshold";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting low stock: "
                    + e.getMessage());
        }
        return 0;
    }

    public int countOutOfStock() {
        String sql = "SELECT COUNT(*) FROM stock_items WHERE "
                + "quantity_in_stock = 0";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting out of stock: "
                    + e.getMessage());
        }
        return 0;
    }

    private StockItem mapRow(ResultSet rs) throws SQLException {
        return new StockItem(
            rs.getString("id"),
            rs.getString("created_date"),
            rs.getString("product_id"),
            rs.getInt("quantity_in_stock"),
            rs.getInt("minimum_threshold"),
            rs.getInt("maximum_capacity"),
            rs.getString("last_restocked"),
            rs.getString("last_updated")
        );
    }
}