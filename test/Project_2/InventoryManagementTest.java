package Project_2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class InventoryManagementTest {

    private UserDAO userDAO;
    private ProductDAO productDAO;
    private StockDAO stockDAO;
    private OrderDAO orderDAO;
    private ProductService productService;
    private StockService stockService;
    private OrderService orderService;

    // =====================
    // Setup and Teardown
    // =====================

    @Before
    public void setUp() {
        DatabaseManager.getInstance();
        userDAO        = new UserDAO();
        productDAO     = new ProductDAO();
        stockDAO       = new StockDAO();
        orderDAO       = new OrderDAO();
        productService = new ProductService();
        stockService   = new StockService(productService);
        orderService   = new OrderService(stockService);
    }

    @After
    public void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            java.sql.Connection conn =
                    DatabaseManager.getInstance().getConnection();
            conn.createStatement().executeUpdate(
                "DELETE FROM orders WHERE id LIKE 'TEST%'");
            conn.createStatement().executeUpdate(
                "DELETE FROM order_lines WHERE id LIKE 'TEST%'");
            conn.createStatement().executeUpdate(
                "DELETE FROM stock_items WHERE id LIKE 'TEST%'");
            conn.createStatement().executeUpdate(
                "DELETE FROM products WHERE id LIKE 'TEST%'");
            conn.createStatement().executeUpdate(
                "DELETE FROM users WHERE id LIKE 'TEST%'");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }

    // =====================
    // Test 1: User Insert and Find
    // =====================

    @Test
    public void testUserInsertAndFind() {
        System.out.println("TEST 1: User insert and findByUsername");

        User testUser = new User(
            "testuser1", "password123",
            "test1@test.com", UserRole.STAFF
        );
        testUser.setId("TEST_USER_001");

        userDAO.insert(testUser);

        User found = userDAO.findByUsername("testuser1");

        assertNotNull("User should be found in database", found);
        assertEquals("Username should match",
                "testuser1", found.getUsername());
        assertEquals("Email should match",
                "test1@test.com", found.getEmail());
        assertEquals("Role should be STAFF",
                UserRole.STAFF, found.getRole());
        assertTrue("User should be active by default",
                found.isActive());

        System.out.println("PASSED: User inserted and found correctly");
    }

    // =====================
    // Test 2: User Update
    // =====================

    @Test
    public void testUserUpdateAndDeactivate() {
        System.out.println("TEST 2: User update and deactivate");

        User testUser = new User(
            "testuser2", "password123",
            "test2@test.com", UserRole.STAFF
        );
        testUser.setId("TEST_USER_002");
        userDAO.insert(testUser);

        // Deactivate
        testUser.setActive(false);
        boolean updated = userDAO.update(testUser);

        User found = userDAO.findById("TEST_USER_002");

        assertTrue("Update should return true", updated);
        assertNotNull("User should still exist", found);
        assertFalse("User should now be inactive", found.isActive());

        System.out.println("PASSED: User deactivated correctly");
    }

    // =====================
    // Test 3: Product CRUD
    // =====================

    @Test
    public void testProductInsertFindAndUpdate() {
        System.out.println("TEST 3: Product insert, find and update");

        Product testProduct = new Product(
            "Test Laptop", "A test laptop",
            999.99, Category.ELECTRONICS, "SUP001"
        );
        testProduct.setId("TEST_PROD_001");

        productDAO.insert(testProduct);

        // Find by ID
        Product found = productDAO.findById("TEST_PROD_001");
        assertNotNull("Product should be found", found);
        assertEquals("Name should match",
                "Test Laptop", found.getName());
        assertEquals("Price should match",
                999.99, found.getPrice(), 0.001);
        assertEquals("Category should match",
                Category.ELECTRONICS, found.getCategory());
        assertTrue("Product should be available by default",
                found.isAvailable());

        // Update price
        found.setPrice(1299.99);
        boolean updated = productDAO.update(found);
        assertTrue("Update should succeed", updated);

        // Verify update persisted
        Product afterUpdate = productDAO.findById("TEST_PROD_001");
        assertEquals("Updated price should be 1299.99",
                1299.99, afterUpdate.getPrice(), 0.001);

        System.out.println("PASSED: Product CRUD works correctly");
    }

    // =====================
    // Test 4: Product Search
    // =====================

    @Test
    public void testProductSearchByName() {
        System.out.println("TEST 4: Product search by name");

        Product p1 = new Product(
            "TestWidget Pro", "First test widget",
            49.99, Category.ELECTRONICS, "SUP001"
        );
        p1.setId("TEST_PROD_002");

        Product p2 = new Product(
            "TestWidget Lite", "Second test widget",
            29.99, Category.ELECTRONICS, "SUP001"
        );
        p2.setId("TEST_PROD_003");

        productDAO.insert(p1);
        productDAO.insert(p2);

        List<Product> results = productDAO.searchByName("TestWidget");

        assertTrue("Should find at least 2 results",
                results.size() >= 2);

        System.out.println("PASSED: Search found "
                + results.size() + " results");
    }

    // =====================
    // Test 5: Stock Insert and Restock
    // =====================

    @Test
    public void testStockInsertAndRestock() {
        System.out.println("TEST 5: Stock insert and restock");

        // Insert a product first
        Product testProduct = new Product(
            "Stock Test Item", "For stock testing",
            19.99, Category.STATIONERY, "SUP001"
        );
        testProduct.setId("TEST_PROD_004");
        productDAO.insert(testProduct);

        // Create stock item
        StockItem testStock = new StockItem(
            "TEST_PROD_004", 20, 5, 100
        );
        testStock.setId("TEST_STOCK_001");
        stockDAO.insert(testStock);

        // Find and verify initial quantity
        StockItem found = stockDAO.findByProductId("TEST_PROD_004");
        assertNotNull("Stock item should be found", found);
        assertEquals("Initial quantity should be 20",
                20, found.getQuantityInStock());
        assertFalse("Should not be low stock",
                found.isLowStock());

        // Restock by 30
        found.restock(30);
        stockDAO.update(found);

        // Verify after restock
        StockItem afterRestock =
                stockDAO.findByProductId("TEST_PROD_004");
        assertEquals("Quantity should be 50 after restock",
                50, afterRestock.getQuantityInStock());

        System.out.println("PASSED: Stock restocked correctly");
    }

    // =====================
    // Test 6: Low Stock Detection
    // =====================

    @Test
    public void testStockReduceAndLowStockDetection() {
        System.out.println("TEST 6: Stock reduce and low stock detection");

        // Insert product
        Product testProduct = new Product(
            "Low Stock Test Item", "For low stock testing",
            9.99, Category.FOOD, "SUP001"
        );
        testProduct.setId("TEST_PROD_005");
        productDAO.insert(testProduct);

        // Stock with qty=15, threshold=10
        StockItem testStock = new StockItem(
            "TEST_PROD_005", 15, 10, 100
        );
        testStock.setId("TEST_STOCK_002");
        stockDAO.insert(testStock);

        // Verify not low stock yet
        assertFalse("Should not be low stock with qty=15, min=10",
                testStock.isLowStock());

        // Reduce stock below threshold
        boolean reduced = testStock.reduceStock(10);
        assertTrue("Reduce should succeed", reduced);
        stockDAO.update(testStock);

        // Verify low stock detected
        StockItem afterReduce =
                stockDAO.findByProductId("TEST_PROD_005");
        assertEquals("Quantity should be 5",
                5, afterReduce.getQuantityInStock());
        assertTrue("Should now be low stock",
                afterReduce.isLowStock());
        assertEquals("Status should be LOW STOCK",
                "LOW STOCK", afterReduce.getStockStatus());

        System.out.println("PASSED: Low stock detected correctly");
    }

    // =====================
    // Test 7: Order Creation and Submission
    // =====================

    @Test
    public void testOrderCreationAndSubmission() {
        System.out.println("TEST 7: Order creation and submission");

        // Setup product and stock
        Product testProduct = new Product(
            "Order Test Item", "For order testing",
            50.00, Category.ELECTRONICS, "SUP001"
        );
        testProduct.setId("TEST_PROD_006");
        productDAO.insert(testProduct);

        StockItem testStock = new StockItem(
            "TEST_PROD_006", 50, 5, 100
        );
        testStock.setId("TEST_STOCK_003");
        stockDAO.insert(testStock);

        // Create order
        Order order = orderService.createOrder(
            "SUP001", "TechCorp", "admin"
        );
        assertNotNull("Order should be created", order);
        assertEquals("New order should be PENDING",
                Order.OrderStatus.PENDING, order.getStatus());
        assertTrue("Order should have no lines initially",
                order.getOrderLines().isEmpty());

        // Add line
        boolean lineAdded = orderService.addLineToOrder(
            order, "TEST_PROD_006",
            "Order Test Item", 5, 50.00
        );
        assertTrue("Order line should be added", lineAdded);
        assertEquals("Order should have 1 line",
                1, order.getOrderLines().size());

        // Verify total
        assertEquals("Order total should be 250.00",
                250.00, order.getOrderTotal(), 0.001);
        assertEquals("Formatted total should be $250.00",
                "$250.00", order.getFormattedOrderTotal());

        System.out.println("PASSED: Order created with total "
                + order.getFormattedOrderTotal());
    }

    // =====================
    // Test 8: Order Status Transitions
    // =====================

    @Test
    public void testOrderStatusTransitions() {
        System.out.println("TEST 8: Order status transitions");

        // Setup
        Product testProduct = new Product(
            "Status Test Item", "For status testing",
            25.00, Category.FOOD, "SUP001"
        );
        testProduct.setId("TEST_PROD_007");
        productDAO.insert(testProduct);

        StockItem testStock = new StockItem(
            "TEST_PROD_007", 100, 5, 200
        );
        testStock.setId("TEST_STOCK_004");
        stockDAO.insert(testStock);

        // Create and submit — should move to CONFIRMED
        Order order = orderService.createOrder(
            "SUP001", "TechCorp", "admin"
        );
        orderService.addLineToOrder(
            order, "TEST_PROD_007",
            "Status Test Item", 10, 25.00
        );
        boolean submitted = orderService.submitOrder(order);
        assertTrue("Order should submit successfully", submitted);
        assertEquals("Order should be CONFIRMED after submit",
                Order.OrderStatus.CONFIRMED, order.getStatus());

        // Process — should move to PROCESSING
        boolean processed = orderService.processOrder(order.getId());
        assertTrue("Order should process successfully", processed);

        // Verify in database
        Order fromDB = orderDAO.findById(order.getId());
        assertNotNull("Order should exist in database", fromDB);
        assertEquals("Order should be PROCESSING in database",
                Order.OrderStatus.PROCESSING, fromDB.getStatus());

        System.out.println("PASSED: Order status transitions correctly");
    }

    // =====================
    // Test 9: Password Hashing
    // =====================

    @Test
    public void testPasswordHashingAndVerification() {
        System.out.println("TEST 9: Password hashing and verification");

        User testUser = new User(
            "testuser3", "SecurePass123",
            "test3@test.com", UserRole.CUSTOMER
        );
        testUser.setId("TEST_USER_003");
        userDAO.insert(testUser);

        User found = userDAO.findByUsername("testuser3");
        assertNotNull("User should be found", found);

        // Correct password should verify
        assertTrue("Correct password should verify",
                found.verifyPassword("SecurePass123"));

        // Wrong password should not verify
        assertFalse("Wrong password should fail",
                found.verifyPassword("wrongpassword"));

        // Password should be stored as hash not plain text
        assertNotEquals("Password should not be stored as plain text",
                "SecurePass123", found.getPassword());

        System.out.println("PASSED: Password hashing works correctly");
    }

    // =====================
    // Test 10: Order Line Calculations
    // =====================

    @Test
    public void testOrderLinePriceCalculations() {
        System.out.println("TEST 10: Order line price calculations");

        // No discount
        OrderLine line1 = new OrderLine(
            "PROD001", "Widget", 4, 25.00
        );
        assertEquals("Subtotal should be 100.00",
                100.00, line1.getSubTotal(), 0.001);
        assertEquals("Final total without discount should be 100.00",
                100.00, line1.getFinalTotal(), 0.001);
        assertEquals("Discount amount should be 0.00",
                0.00, line1.getDiscountAmount(), 0.001);
        assertFalse("Should have no discount",
                line1.hasDiscount());

        // With 20% discount
        OrderLine line2 = new OrderLine(
            "PROD002", "Gadget", 2, 100.00, 20.0
        );
        assertEquals("Subtotal should be 200.00",
                200.00, line2.getSubTotal(), 0.001);
        assertEquals("Discount amount should be 40.00",
                40.00, line2.getDiscountAmount(), 0.001);
        assertEquals("Final total with 20% discount should be 160.00",
                160.00, line2.getFinalTotal(), 0.001);
        assertTrue("Should have discount",
                line2.hasDiscount());

        System.out.println("PASSED: Order line calculations correct");
    }

    // =====================
    // Test 11: Stock Status Logic
    // =====================

    @Test
    public void testStockStatusLogic() {
        System.out.println("TEST 11: Stock status business logic");

        // OUT OF STOCK — qty = 0
        StockItem outOfStock = new StockItem(
                "PROD_A", 0, 5, 100);
        assertTrue("qty=0 should be out of stock",
                outOfStock.isOutOfStock());
        assertEquals("OUT OF STOCK",
                outOfStock.getStockStatus());

        // LOW STOCK — qty below threshold
        StockItem lowStock = new StockItem(
                "PROD_B", 3, 5, 100);
        assertTrue("qty=3 below min=5 should be low stock",
                lowStock.isLowStock());
        assertEquals("LOW STOCK",
                lowStock.getStockStatus());

        // IN STOCK — qty above threshold, below max
        StockItem inStock = new StockItem(
                "PROD_C", 50, 5, 100);
        assertFalse("qty=50 should not be low stock",
                inStock.isLowStock());
        assertFalse("qty=50 should not be out of stock",
                inStock.isOutOfStock());
        assertEquals("IN STOCK",
                inStock.getStockStatus());

        // FULLY STOCKED — qty at max
        StockItem fullyStocked = new StockItem(
                "PROD_D", 100, 5, 100);
        assertTrue("qty=100 at max=100 should be fully stocked",
                fullyStocked.isFullyStocked());
        assertEquals("FULLY STOCKED",
                fullyStocked.getStockStatus());

        // Restock recommendation — 80% of max minus current
        StockItem needsRestock = new StockItem(
                "PROD_E", 10, 5, 100);
        assertEquals("Restock recommendation should be 70",
                70, needsRestock.getRestockRecommendation());

        // Remaining capacity
        assertEquals("Remaining capacity should be 90",
                90, needsRestock.getRemainingCapacity());

        System.out.println("PASSED: All stock status logic correct");
    }
}