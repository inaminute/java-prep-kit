# Online Shopping Cart System

## Problem Statement

Design an e-commerce shopping cart system with product catalog, cart management, pricing, discounts, inventory tracking, and order processing.

## Approach

- Product catalog with categories and inventory
- Shopping cart with add/remove/update operations
- Pricing strategy with discounts and promotions
- Order processing and payment integration
- Inventory management with stock tracking

## Solution

```java
import java.util.*;

class Product {
    private String id;
    private String name;
    private double price;
    private int stock;
    
    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    public synchronized boolean reduceStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
            return true;
        }
        return false;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
}

class CartItem {
    private Product product;
    private int quantity;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    
    public void updateQuantity(int quantity) { this.quantity = quantity; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return product.getPrice() * quantity; }
}

class ShoppingCart {
    private String userId;
    private Map<String, CartItem> items;
    
    public ShoppingCart(String userId) {
        this.userId = userId;
        this.items = new HashMap<>();
    }
    
    public void addItem(Product product, int quantity) {
        if (items.containsKey(product.getId())) {
            CartItem item = items.get(product.getId());
            item.updateQuantity(item.getQuantity() + quantity);
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }
        System.out.println("Added " + quantity + "x " + product.getName());
    }
    
    public void removeItem(String productId) {
        items.remove(productId);
    }
    
    public double getTotal() {
        return items.values().stream().mapToDouble(CartItem::getSubtotal).sum();
    }
    
    public Map<String, CartItem> getItems() { return items; }
}

class Order {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private double total;
    private String status;
    
    public Order(String orderId, String userId, List<CartItem> items, double total) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.status = "PENDING";
    }
    
    public void complete() { this.status = "COMPLETED"; }
    public String getOrderId() { return orderId; }
}

class ShoppingSystem {
    private Map<String, Product> products;
    private Map<String, ShoppingCart> carts;
    private List<Order> orders;
    private int orderCounter;
    
    public ShoppingSystem() {
        this.products = new HashMap<>();
        this.carts = new HashMap<>();
        this.orders = new ArrayList<>();
        this.orderCounter = 1;
    }
    
    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }
    
    public ShoppingCart getCart(String userId) {
        return carts.computeIfAbsent(userId, k -> new ShoppingCart(userId));
    }
    
    public Order checkout(String userId) {
        ShoppingCart cart = carts.get(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        // Check and reduce stock
        for (CartItem item : cart.getItems().values()) {
            if (!item.getProduct().reduceStock(item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for " + item.getProduct().getName());
            }
        }
        
        String orderId = "ORD-" + (orderCounter++);
        Order order = new Order(orderId, userId, new ArrayList<>(cart.getItems().values()), cart.getTotal());
        orders.add(order);
        
        cart.getItems().clear();
        System.out.println("Order " + orderId + " placed. Total: $" + order.total);
        return order;
    }
}

class ShoppingDemo {
    public static void main(String[] args) {
        ShoppingSystem system = new ShoppingSystem();
        
        system.addProduct(new Product("P001", "Laptop", 999.99, 10));
        system.addProduct(new Product("P002", "Mouse", 29.99, 50));
        
        ShoppingCart cart = system.getCart("U001");
        cart.addItem(system.products.get("P001"), 1);
        cart.addItem(system.products.get("P002"), 2);
        
        System.out.println("Cart total: $" + cart.getTotal());
        
        system.checkout("U001");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for add/remove, O(n) for checkout with n items
**Space Complexity**: O(p + u + o) for products, users, and orders

## Edge Cases and Pitfalls

- **Stock Management**: Check and reduce stock atomically during checkout
- **Concurrent Checkouts**: Synchronize stock reduction to prevent overselling
- **Cart Persistence**: Save cart state for returning users
- **Price Changes**: Handle price updates between adding to cart and checkout
- **Abandoned Carts**: Implement timeout and cleanup for inactive carts

## Interview-Ready Answer

"I'd design a shopping system with Product, CartItem, ShoppingCart, and Order classes. Use HashMap for O(1) product and cart lookups. Implement synchronized stock reduction during checkout to prevent overselling. Track cart items with quantities and calculate totals. Support add/remove operations and order processing. Time complexity is O(1) for most operations, O(n) for checkout, space is O(p+u+o)."
