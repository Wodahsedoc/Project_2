package Project_2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements DAO<Order> {

    private final Connection connection;
    private final OrderLineDAO orderLineDAO;

    public OrderDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.orderLineDAO = new OrderLineDAO();
    }

    @Override
    public void insert(Order order) {
        String sql = "INSERT INTO orders (id, created_date, "
                + "supplier_id, supplier_name, created_by_user_id, "
                + "order_line_ids, status, last_updated, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, order.getId());
            stmt.setString(2, order.getCreatedDate());
            stmt.setString(3, order.getSupplierId());
            stmt.setString(4, order.getSupplierName());
            stmt.setString(5, order.getCreatedByUserId());
            stmt.setString(6, serializeLineIds(
                    order.getOrderLines()));
            stmt.setString(7, order.getStatus().name());
            stmt.setString(8, order.getLastUpdated());
            stmt.setString(9, order.getNotes());
            stmt.executeUpdate();


            for (OrderLine line : order.getOrderLines()) {
                orderLineDAO.insert(line);
            }

            System.out.println("Order inserted: " + order.getId());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Order already exists: "
                        + order.getId());
            } else {
                System.out.println("Error inserting order: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean update(Order order) {
        String sql = "UPDATE orders SET supplier_id = ?, "
                + "supplier_name = ?, order_line_ids = ?, "
                + "status = ?, last_updated = ?, notes = ? "
                + "WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, order.getSupplierId());
            stmt.setString(2, order.getSupplierName());
            stmt.setString(3, serializeLineIds(
                    order.getOrderLines()));
            stmt.setString(4, order.getStatus().name());
            stmt.setString(5, order.getLastUpdated());
            stmt.setString(6, order.getNotes());
            stmt.setString(7, order.getId());
            int rows = stmt.executeUpdate();


            for (OrderLine line : order.getOrderLines()) {
                if (orderLineDAO.findById(line.getId()) != null) {
                    orderLineDAO.update(line);
                } else {
                    orderLineDAO.insert(line);
                }
            }

            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating order: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {

        Order order = findById(id);
        if (order != null) {
            List<String> lineIds = new ArrayList<>();
            for (OrderLine line : order.getOrderLines()) {
                lineIds.add(line.getId());
            }
            orderLineDAO.deleteByIds(lineIds);
        }

        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting order: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public Order findById(String id) {
        String sql = "SELECT * FROM orders WHERE "
                + "UPPER(id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding order: "
                    + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all orders: "
                    + e.getMessage());
        }
        return orders;
    }

    public List<Order> findByStatus(Order.OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding orders by status: "
                    + e.getMessage());
        }
        return orders;
    }

    public List<Order> findBySupplier(String supplierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE "
                + "UPPER(supplier_id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding orders by supplier: "
                    + e.getMessage());
        }
        return orders;
    }

    public List<Order> findByUser(String userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE "
                + "UPPER(created_by_user_id) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding orders by user: "
                    + e.getMessage());
        }
        return orders;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting orders: "
                    + e.getMessage());
        }
        return 0;
    }

    public double getTotalRevenue() {
        List<Order> allOrders = findAll();
        double total = 0;
        for (Order order : allOrders) {
            if (!order.isCancelled()) {
                total += order.getOrderTotal();
            }
        }
        return total;
    }


    private String serializeLineIds(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i).getId());
            if (i < lines.size() - 1) sb.append("|");
        }
        return sb.toString();
    }

    private List<String> deserializeLineIds(String lineIds) {
        List<String> ids = new ArrayList<>();
        if (lineIds == null || lineIds.trim().isEmpty()) return ids;
        for (String id : lineIds.split("\\|")) {
            if (!id.trim().isEmpty()) ids.add(id.trim());
        }
        return ids;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        String lineIdsStr = rs.getString("order_line_ids");
        List<String> lineIds = deserializeLineIds(lineIdsStr);
        List<OrderLine> orderLines = orderLineDAO.findByIds(lineIds);

        return new Order(
            rs.getString("id"),
            rs.getString("created_date"),
            rs.getString("supplier_id"),
            rs.getString("supplier_name"),
            rs.getString("created_by_user_id"),
            orderLines,
            Order.OrderStatus.valueOf(rs.getString("status")),
            rs.getString("last_updated"),
            rs.getString("notes") != null
                    ? rs.getString("notes") : ""
        );
    }
}