package Project_2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderLineDAO implements DAO<OrderLine> {

    private final Connection connection;

    public OrderLineDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void insert(OrderLine orderLine) {
        String sql = "INSERT INTO order_lines (id, created_date, "
                + "product_id, product_name, quantity, "
                + "unit_price, discount_percent) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, orderLine.getId());
            stmt.setString(2, orderLine.getCreatedDate());
            stmt.setString(3, orderLine.getProductId());
            stmt.setString(4, orderLine.getProductName());
            stmt.setInt(5, orderLine.getQuantity());
            stmt.setDouble(6, orderLine.getUnitPrice());
            stmt.setDouble(7, orderLine.getDiscountPercent());
            stmt.executeUpdate();
            System.out.println("Order line inserted for product: "
                    + orderLine.getProductName());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Order line already exists: "
                        + orderLine.getId());
            } else {
                System.out.println("Error inserting order line: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean update(OrderLine orderLine) {
        String sql = "UPDATE order_lines SET product_name = ?, "
                + "quantity = ?, unit_price = ?, "
                + "discount_percent = ? WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, orderLine.getProductName());
            stmt.setInt(2, orderLine.getQuantity());
            stmt.setDouble(3, orderLine.getUnitPrice());
            stmt.setDouble(4, orderLine.getDiscountPercent());
            stmt.setString(5, orderLine.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating order line: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM order_lines WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting order line: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public OrderLine findById(String id) {
        String sql = "SELECT * FROM order_lines WHERE "
                + "UPPER(id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding order line: "
                    + e.getMessage());
        }
        return null;
    }

    @Override
    public List<OrderLine> findAll() {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT * FROM order_lines";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all order lines: "
                    + e.getMessage());
        }
        return lines;
    }

    public List<OrderLine> findByIds(List<String> ids) {
        List<OrderLine> lines = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return lines;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) placeholders.append(",");
        }

        String sql = "SELECT * FROM order_lines WHERE id IN ("
                + placeholders + ")";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding order lines by ids: "
                    + e.getMessage());
        }
        return lines;
    }

    public List<OrderLine> findByProductId(String productId) {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT * FROM order_lines WHERE "
                + "UPPER(product_id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding order lines by product: "
                    + e.getMessage());
        }
        return lines;
    }

    public boolean deleteByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return true;
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) placeholders.append(",");
        }
        String sql = "DELETE FROM order_lines WHERE id IN ("
                + placeholders + ")";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error deleting order lines: "
                    + e.getMessage());
            return false;
        }
    }

    private OrderLine mapRow(ResultSet rs) throws SQLException {
        return new OrderLine(
            rs.getString("id"),
            rs.getString("created_date"),
            rs.getString("product_id"),
            rs.getString("product_name"),
            rs.getInt("quantity"),
            rs.getDouble("unit_price"),
            rs.getDouble("discount_percent")
        );
    }
}
