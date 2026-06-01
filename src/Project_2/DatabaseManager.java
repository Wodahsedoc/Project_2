package Project_2;

import java.sql.*;

public class DatabaseManager {

    private static DatabaseManager instance;
    private static final String DB_URL =
            "jdbc:derby:InventoryDB;create=true";
    private Connection connection;


    private DatabaseManager() {
        connect();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }


    private void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to Derby database: InventoryDB");
            createTables();
        } catch (Exception e) {
            System.out.println("Database connection failed: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.out.println("Error getting connection: "
                    + e.getMessage());
        }
        return connection;
    }


    private void createTables() {
        createUsersTable();
        createSuppliersTable();
        createProductsTable();
        createStockItemsTable();
        createOrderLinesTable();
        createOrdersTable();
        System.out.println("All tables verified.");
    }

    private void createUsersTable() {
        String sql = "CREATE TABLE users ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "created_date VARCHAR(30),"
                + "username VARCHAR(50) UNIQUE NOT NULL,"
                + "password VARCHAR(200) NOT NULL,"
                + "email VARCHAR(100) UNIQUE NOT NULL,"
                + "role VARCHAR(20) NOT NULL,"
                + "is_active BOOLEAN NOT NULL"
                + ")";
        executeCreate(sql, "users");
    }

    private void createProductsTable() {
        String sql = "CREATE TABLE products ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "created_date VARCHAR(30),"
                + "name VARCHAR(100) NOT NULL,"
                + "description VARCHAR(500),"
                + "price DOUBLE NOT NULL,"
                + "category VARCHAR(50) NOT NULL,"
                + "supplier_id VARCHAR(20),"
                + "is_available BOOLEAN NOT NULL"
                + ")";
        executeCreate(sql, "products");
    }

    private void createSuppliersTable() {
        String sql = "CREATE TABLE suppliers ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "created_date VARCHAR(30),"
                + "supplier_name VARCHAR(100) NOT NULL,"
                + "contact_person VARCHAR(100),"
                + "email VARCHAR(100) UNIQUE NOT NULL,"
                + "phone VARCHAR(20),"
                + "address VARCHAR(200),"
                + "product_ids VARCHAR(500),"
                + "is_active BOOLEAN NOT NULL"
                + ")";
        executeCreate(sql, "suppliers");
    }

    private void createStockItemsTable() {
        String sql = "CREATE TABLE stock_items ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "created_date VARCHAR(30),"
                + "product_id VARCHAR(20) NOT NULL,"
                + "quantity_in_stock INT NOT NULL,"
                + "minimum_threshold INT NOT NULL,"
                + "maximum_capacity INT NOT NULL,"
                + "last_restocked VARCHAR(30),"
                + "last_updated VARCHAR(30)"
                + ")";
        executeCreate(sql, "stock_items");
    }

    private void createOrderLinesTable() {
        String sql = "CREATE TABLE order_lines ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "created_date VARCHAR(30),"
                + "product_id VARCHAR(20) NOT NULL,"
                + "product_name VARCHAR(100) NOT NULL,"
                + "quantity INT NOT NULL,"
                + "unit_price DOUBLE NOT NULL,"
                + "discount_percent DOUBLE NOT NULL"
                + ")";
        executeCreate(sql, "order_lines");
    }

    private void createOrdersTable() {
        String sql = "CREATE TABLE orders ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "created_date VARCHAR(30),"
                + "supplier_id VARCHAR(20) NOT NULL,"
                + "supplier_name VARCHAR(100) NOT NULL,"
                + "created_by_user_id VARCHAR(20) NOT NULL,"
                + "order_line_ids VARCHAR(500),"
                + "status VARCHAR(20) NOT NULL,"
                + "last_updated VARCHAR(30),"
                + "notes VARCHAR(500)"
                + ")";
        executeCreate(sql, "orders");
    }

    private void executeCreate(String sql, String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Created table: " + tableName);
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32")) {
                System.out.println("Table already exists: " + tableName);
            } else {
                System.out.println("Error creating table "
                        + tableName + ": " + e.getMessage());
            }
        }
    }


    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }


    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            DriverManager.getConnection(
                    "jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ015")) {
                System.out.println("Derby shut down normally.");
            } else {
                System.out.println("Error shutting down Derby: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return "DatabaseManager[Connected: " + isConnected() + "]";
    }
}