package Project_2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements DAO<User> {

    private final Connection connection;

    public UserDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void insert(User user) {
        String sql = "INSERT INTO users (id, created_date, username, "
                + "password, email, role, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getCreatedDate());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getRole().name());
            stmt.setBoolean(7, user.isActive());
            stmt.executeUpdate();
            System.out.println("User inserted: " + user.getUsername());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("User already exists: "
                        + user.getUsername());
            } else {
                System.out.println("Error inserting user: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, "
                + "role = ?, is_active = ? WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setBoolean(4, user.isActive());
            stmt.setString(5, user.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating user: "
                    + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(String id, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating password: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting user: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding user: "
                    + e.getMessage());
        }
        return null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE "
                + "UPPER(username) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error finding user by username: "
                    + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all users: "
                    + e.getMessage());
        }
        return users;
    }

    public List<User> findByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding users by role: "
                    + e.getMessage());
        }
        return users;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE "
                + "UPPER(username) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking username: "
                    + e.getMessage());
        }
        return false;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE "
                + "UPPER(email) = UPPER(?)";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error checking email: "
                    + e.getMessage());
        }
        return false;
    }

    public int countByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ? "
                + "AND is_active = true";
        try (PreparedStatement stmt =
                connection.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting users: "
                    + e.getMessage());
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("created_date"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("email"),
            UserRole.valueOf(rs.getString("role")),
            rs.getBoolean("is_active")
        );
    }
}