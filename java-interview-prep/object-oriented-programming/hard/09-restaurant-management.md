# Restaurant Management System

## Problem Statement

Design a restaurant management system handling table reservations, orders, menu management, billing, and kitchen operations.

## Approach

- Manage tables with capacity and availability
- Handle orders with multiple items and modifications
- Track order status through kitchen workflow
- Calculate bills with taxes and tips
- Support reservations with time slots

## Solution

```java
import java.time.*;
import java.util.*;

enum TableStatus { AVAILABLE, RESERVED, OCCUPIED }
enum OrderStatus { PENDING, PREPARING, READY, SERVED, PAID }

class MenuItem {
    private String id;
    private String name;
    private double price;
    private String category;
    
    public MenuItem(String id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

class OrderItem {
    private MenuItem menuItem;
    private int quantity;
    private String specialInstructions;
    
    public OrderItem(MenuItem menuItem, int quantity, String specialInstructions) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
    }
    
    public double getSubtotal() { return menuItem.getPrice() * quantity; }
    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
}

class Table {
    private int tableNumber;
    private int capacity;
    private TableStatus status;
    
    public Table(int tableNumber, int capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
    }
    
    public boolean isAvailable() { return status == TableStatus.AVAILABLE; }
    public void occupy() { status = TableStatus.OCCUPIED; }
    public void free() { status = TableStatus.AVAILABLE; }
    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
}

class Order {
    private String orderId;
    private int tableNumber;
    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime orderTime;
    
    public Order(String orderId, int tableNumber) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
    }
    
    public void addItem(OrderItem item) { items.add(item); }
    
    public double getTotal() {
        return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }
    
    public void updateStatus(OrderStatus status) {
        this.status = status;
        System.out.println("Order " + orderId + " status: " + status);
    }
    
    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
}

class Bill {
    private Order order;
    private double subtotal;
    private double tax;
    private double tip;
    private double total;
    
    public Bill(Order order, double taxRate) {
        this.order = order;
        this.subtotal = order.getTotal();
        this.tax = subtotal * taxRate;
        this.tip = 0;
        this.total = subtotal + tax;
    }
    
    public void addTip(double tipAmount) {
        this.tip = tipAmount;
        this.total = subtotal + tax + tip;
    }
    
    public void printBill() {
        System.out.println("=== Bill ===");
        System.out.println("Subtotal: $" + subtotal);
        System.out.println("Tax: $" + tax);
        System.out.println("Tip: $" + tip);
        System.out.println("Total: $" + total);
    }
    
    public double getTotal() { return total; }
}

class Restaurant {
    private List<Table> tables;
    private Map<String, MenuItem> menu;
    private Map<String, Order> activeOrders;
    private int orderCounter;
    
    public Restaurant() {
        this.tables = new ArrayList<>();
        this.menu = new HashMap<>();
        this.activeOrders = new HashMap<>();
        this.orderCounter = 1;
    }
    
    public void addTable(Table table) { tables.add(table); }
    public void addMenuItem(MenuItem item) { menu.put(item.getId(), item); }
    
    public Table findAvailableTable(int partySize) {
        return tables.stream()
            .filter(t -> t.isAvailable() && t.getCapacity() >= partySize)
            .findFirst()
            .orElse(null);
    }
    
    public Order createOrder(int tableNumber) {
        String orderId = "ORD-" + (orderCounter++);
        Order order = new Order(orderId, tableNumber);
        activeOrders.put(orderId, order);
        System.out.println("Created order " + orderId + " for table " + tableNumber);
        return order;
    }
    
    public Bill generateBill(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order == null) return null;
        
        Bill bill = new Bill(order, 0.08);  // 8% tax
        order.updateStatus(OrderStatus.PAID);
        activeOrders.remove(orderId);
        return bill;
    }
}

class RestaurantDemo {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        
        restaurant.addTable(new Table(1, 4));
        restaurant.addTable(new Table(2, 2));
        
        restaurant.addMenuItem(new MenuItem("M001", "Burger", 12.99, "Main"));
        restaurant.addMenuItem(new MenuItem("M002", "Fries", 4.99, "Side"));
        
        Table table = restaurant.findAvailableTable(2);
        if (table != null) {
            table.occupy();
            Order order = restaurant.createOrder(table.getTableNumber());
            order.addItem(new OrderItem(restaurant.menu.get("M001"), 2, "No onions"));
            order.addItem(new OrderItem(restaurant.menu.get("M002"), 1, ""));
            
            order.updateStatus(OrderStatus.PREPARING);
            order.updateStatus(OrderStatus.READY);
            order.updateStatus(OrderStatus.SERVED);
            
            Bill bill = restaurant.generateBill(order.getOrderId());
            bill.addTip(5.00);
            bill.printBill();
            
            table.free();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(t) for finding tables with t tables, O(1) for order operations
**Space Complexity**: O(t + m + o) for tables, menu items, and orders

## Edge Cases and Pitfalls

- **Table Management**: Handle reservations and walk-ins
- **Order Modifications**: Support item modifications and cancellations
- **Split Bills**: Allow splitting bills among multiple customers
- **Kitchen Workflow**: Track order status through preparation stages
- **Concurrent Orders**: Handle multiple orders simultaneously

## Interview-Ready Answer

"I'd design a restaurant system with Table, MenuItem, Order, and Bill classes. Track table status (available/occupied), manage orders with items and modifications, and calculate bills with tax and tip. Use enums for order status workflow. Support finding available tables by capacity and generating itemized bills. Time complexity is O(t) for table search, O(1) for orders, space is O(t+m+o)."
