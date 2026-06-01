package Project_2;

import java.util.ArrayList;
import java.util.List;

public class ProductService implements Manageable<Product> {

    private final ProductDAO productDAO;


    public ProductService() {
        this.productDAO = new ProductDAO();
        if (productDAO.findAll().isEmpty()) {
            seedSampleProducts();
        }
    }


    @Override
    public void add(Product product) {
        if (product == null) {
            System.out.println("Cannot add a null product.");
            return;
        }
        if (!product.isValidName()) {
            System.out.println("Invalid product name.");
            return;
        }
        if (!product.isValidPrice()) {
            System.out.println("Invalid product price.");
            return;
        }
        if (nameExists(product.getName(), null)) {
            System.out.println("Product '" + product.getName()
                             + "' already exists.");
            return;
        }
        productDAO.insert(product);
        System.out.println("Product '" + product.getName()
                         + "' added successfully.");
    }

    @Override
    public boolean update(Product updatedProduct) {
        if (updatedProduct == null) return false;
        if (productDAO.findById(updatedProduct.getId()) == null) {
            System.out.println("Product not found with ID: "
                    + updatedProduct.getId());
            return false;
        }
        if (nameExists(updatedProduct.getName(), updatedProduct.getId())) {
            System.out.println("Product name '" + updatedProduct.getName()
                             + "' already exists.");
            return false;
        }
        boolean updated = productDAO.update(updatedProduct);
        if (updated) {
            System.out.println("Product '" + updatedProduct.getName()
                             + "' updated successfully.");
        }
        return updated;
    }

    @Override
    public boolean delete(String id) {
        Product product = findById(id);
        if (product == null) {
            System.out.println("Product not found with ID: " + id);
            return false;
        }

        product.markUnavailable();
        boolean updated = productDAO.update(product);
        if (updated) {
            System.out.println("Product '" + product.getName()
                             + "' has been removed from inventory.");
        }
        return updated;
    }

    @Override
    public Product findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return productDAO.findById(id);
    }

    @Override
    public List<Product> getAll() {
        return productDAO.findAll();
    }


    public boolean hardDelete(String id) {
        if (productDAO.findById(id) == null) {
            System.out.println("Product not found with ID: " + id);
            return false;
        }
        boolean deleted = productDAO.delete(id);
        if (deleted) {
            System.out.println("Product permanently deleted.");
        }
        return deleted;
    }


    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return new ArrayList<>();
        }
        List<Product> result = productDAO.searchByName(keyword.trim());
        if (result.isEmpty()) {
            System.out.println("No products found matching: " + keyword);
        }
        return result;
    }

    public List<Product> filterByCategory(Category category) {
        if (category == null) return new ArrayList<>();
        List<Product> result = productDAO.findByCategory(category);
        if (result.isEmpty()) {
            System.out.println("No products found in category: "
                             + category.getDisplayName());
        }
        return result;
    }

    public List<Product> filterBySupplier(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return productDAO.findBySupplier(supplierId);
    }

    public List<Product> filterByPriceRange(double minPrice,
                                             double maxPrice) {
        if (minPrice < 0 || maxPrice < minPrice) {
            System.out.println("Invalid price range.");
            return new ArrayList<>();
        }
        List<Product> result = productDAO.findByPriceRange(
                minPrice, maxPrice);
        if (result.isEmpty()) {
            System.out.println("No products found in price range: $"
                             + minPrice + " - $" + maxPrice);
        }
        return result;
    }

    public List<Product> getAvailableProducts() {
        return productDAO.findAvailable();
    }

    public List<Product> getUnavailableProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : productDAO.findAll()) {
            if (!p.isAvailable()) result.add(p);
        }
        return result;
    }


    public List<Product> sortByName() {
        List<Product> sorted = new ArrayList<>(getAvailableProducts());
        sorted.sort((a, b) ->
                a.getName().compareToIgnoreCase(b.getName()));
        return sorted;
    }

    public List<Product> sortByPriceAscending() {
        List<Product> sorted = new ArrayList<>(getAvailableProducts());
        sorted.sort((a, b) ->
                Double.compare(a.getPrice(), b.getPrice()));
        return sorted;
    }

    public List<Product> sortByPriceDescending() {
        List<Product> sorted = new ArrayList<>(getAvailableProducts());
        sorted.sort((a, b) ->
                Double.compare(b.getPrice(), a.getPrice()));
        return sorted;
    }

    public List<Product> sortByCategory() {
        List<Product> sorted = new ArrayList<>(getAvailableProducts());
        sorted.sort((a, b) -> a.getCategory().getDisplayName()
                .compareToIgnoreCase(b.getCategory().getDisplayName()));
        return sorted;
    }


    private boolean nameExists(String name, String excludeId) {
        return productDAO.existsByName(name, excludeId);
    }

    public boolean productExists(String id) {
        return findById(id) != null;
    }


    public int getTotalProducts() {
        return productDAO.findAll().size();
    }

    public int getTotalAvailable() {
        return productDAO.countAvailable();
    }

    public double getAveragePrice() {
        List<Product> available = getAvailableProducts();
        if (available.isEmpty()) return 0;
        double total = 0;
        for (Product p : available) total += p.getPrice();
        return total / available.size();
    }

    public Product getMostExpensive() {
        List<Product> available = getAvailableProducts();
        if (available.isEmpty()) return null;
        Product most = available.get(0);
        for (Product p : available) {
            if (p.getPrice() > most.getPrice()) most = p;
        }
        return most;
    }

    public Product getCheapest() {
        List<Product> available = getAvailableProducts();
        if (available.isEmpty()) return null;
        Product cheapest = available.get(0);
        for (Product p : available) {
            if (p.getPrice() < cheapest.getPrice()) cheapest = p;
        }
        return cheapest;
    }


    private void seedSampleProducts() {
        System.out.println("Seeding sample products...");
        add(new Product("Samsung 55\" TV", "4K UHD Smart TV",
                999.99, Category.ELECTRONICS, "SUP001"));
        add(new Product("HDMI Cable 2m", "High speed HDMI cable",
                15.99, Category.ELECTRONICS, "SUP001"));
        add(new Product("Basmati Rice 5kg", "Premium long grain rice",
                12.50, Category.FOOD, "SUP002"));
        add(new Product("Olive Oil 1L", "Extra virgin olive oil",
                18.99, Category.FOOD, "SUP002"));
        add(new Product("Nike T-Shirt", "100% cotton casual t-shirt",
                45.00, Category.CLOTHING, "SUP003"));
        add(new Product("A4 Notebook", "200 page ruled notebook",
                4.99, Category.STATIONERY, "SUP003"));
        System.out.println("Sample products loaded.");
    }


    public void displayAllProducts() {
        List<Product> available = getAvailableProducts();
        if (available.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        Product.displayTableHeader();
        for (Product product : available) {
            product.displaySummary();
        }
        System.out.println("\nTotal products: " + available.size());
    }

    public void displayAllProducts(StockService stockService) {
        List<Product> available = getAvailableProducts();
        if (available.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        Product.displayTableHeaderWithStock();
        for (Product product : available) {
            int qty = stockService.getQuantityByProduct(product.getId());
            String status = stockService.getStatusByProduct(
                    product.getId());
            product.displaySummaryWithStock(qty, status);
        }
        System.out.println("\nTotal products: " + available.size());
    }

    public void displayByCategory(Category category) {
        List<Product> filtered = filterByCategory(category);
        if (filtered.isEmpty()) return;
        System.out.println("===== " + category.getDisplayName()
                + " =====");
        Product.displayTableHeader();
        for (Product product : filtered) {
            product.displaySummary();
        }
    }

    public void displaySearchResults(String keyword) {
        List<Product> results = searchByName(keyword);
        if (results.isEmpty()) return;
        System.out.println("===== Search Results: '"
                + keyword + "' =====");
        Product.displayTableHeader();
        for (Product product : results) {
            product.displaySummary();
        }
        System.out.println("Found: " + results.size() + " product(s)");
    }

    public void displayStats() {
        System.out.println("===== Product Statistics =====");
        System.out.println("Total Products  : " + getTotalProducts());
        System.out.println("Available       : " + getTotalAvailable());
        System.out.println("Unavailable     : "
                + getUnavailableProducts().size());
        System.out.printf("Average Price   : $%.2f%n",
                getAveragePrice());
        Product most = getMostExpensive();
        Product cheap = getCheapest();
        if (most != null) {
            System.out.println("Most Expensive  : " + most.getName()
                    + " (" + most.getFormattedPrice() + ")");
        }
        if (cheap != null) {
            System.out.println("Cheapest        : " + cheap.getName()
                    + " (" + cheap.getFormattedPrice() + ")");
        }
        System.out.println("==============================");
    }

    @Override
    public String toString() {
        return "ProductService[Total Products: "
                + productDAO.findAll().size() + "]";
    }
}
