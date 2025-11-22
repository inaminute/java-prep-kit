# SOLID - Dependency Inversion Principle (DIP)

## Problem Statement

Demonstrate the Dependency Inversion Principle by showing how high-level modules should not depend on low-level modules; both should depend on abstractions. Refactor code to invert dependencies and achieve loose coupling.

**Requirements:**
- Show high-level code directly depending on low-level implementations
- Demonstrate tight coupling and difficulty in testing/changing
- Introduce abstractions (interfaces) between layers
- Use dependency injection to provide implementations
- Show how DIP enables flexibility and testability

## Approach

- Create high-level business logic directly using concrete classes
- Show the problems: tight coupling, hard to test, hard to change
- Introduce interfaces for dependencies
- Refactor high-level code to depend on interfaces
- Inject concrete implementations through constructors or setters
- Demonstrate improved testability with mock implementations

## Solution

```java
// ========== BEFORE: Violating DIP ==========

// Low-level module
class MySQLDatabase {
    public void connect() {
        System.out.println("Connecting to MySQL database");
    }
    
    public void saveData(String data) {
        System.out.println("Saving to MySQL: " + data);
    }
}

// High-level module directly depends on low-level module
class UserServiceBad {
    private MySQLDatabase database;  // Tight coupling!
    
    public UserServiceBad() {
        this.database = new MySQLDatabase();  // Creates concrete instance
    }
    
    public void saveUser(String userData) {
        database.connect();
        database.saveData(userData);
    }
}

// ========== AFTER: Following DIP ==========

// Abstraction (interface)
interface Database {
    void connect();
    void saveData(String data);
    void disconnect();
}

// Low-level modules implement the abstraction
class MySQLDatabaseImpl implements Database {
    @Override
    public void connect() {
        System.out.println("Connecting to MySQL database");
    }
    
    @Override
    public void saveData(String data) {
        System.out.println("Saving to MySQL: " + data);
    }
    
    @Override
    public void disconnect() {
        System.out.println("Disconnecting from MySQL");
    }
}

class PostgreSQLDatabase implements Database {
    @Override
    public void connect() {
        System.out.println("Connecting to PostgreSQL database");
    }
    
    @Override
    public void saveData(String data) {
        System.out.println("Saving to PostgreSQL: " + data);
    }
    
    @Override
    public void disconnect() {
        System.out.println("Disconnecting from PostgreSQL");
    }
}

class MongoDBDatabase implements Database {
    @Override
    public void connect() {
        System.out.println("Connecting to MongoDB");
    }
    
    @Override
    public void saveData(String data) {
        System.out.println("Saving to MongoDB: " + data);
    }
    
    @Override
    public void disconnect() {
        System.out.println("Disconnecting from MongoDB");
    }
}

// Mock database for testing
class MockDatabase implements Database {
    private boolean connected = false;
    private String lastSavedData = null;
    
    @Override
    public void connect() {
        connected = true;
        System.out.println("Mock: Connected");
    }
    
    @Override
    public void saveData(String data) {
        if (!connected) {
            throw new IllegalStateException("Not connected!");
        }
        lastSavedData = data;
        System.out.println("Mock: Saved data");
    }
    
    @Override
    public void disconnect() {
        connected = false;
        System.out.println("Mock: Disconnected");
    }
    
    public String getLastSavedData() {
        return lastSavedData;
    }
}

// High-level module depends on abstraction
class UserService {
    private Database database;  // Depends on interface, not concrete class
    
    // Dependency injection through constructor
    public UserService(Database database) {
        this.database = database;
    }
    
    // Setter injection (alternative)
    public void setDatabase(Database database) {
        this.database = database;
    }
    
    public void saveUser(String userData) {
        database.connect();
        database.saveData(userData);
        database.disconnect();
    }
    
    public void updateUser(String userData) {
        database.connect();
        database.saveData("UPDATE: " + userData);
        database.disconnect();
    }
}

// Another high-level module
class ReportService {
    private Database database;
    
    public ReportService(Database database) {
        this.database = database;
    }
    
    public void generateReport(String reportData) {
        database.connect();
        database.saveData("REPORT: " + reportData);
        database.disconnect();
    }
}

// Demo class
class DIPDemo {
    public static void main(String[] args) {
        System.out.println("=== BEFORE: Violating DIP ===\n");
        
        UserServiceBad badService = new UserServiceBad();
        badService.saveUser("John Doe");
        System.out.println("Problem: Cannot switch to PostgreSQL without modifying UserServiceBad");
        System.out.println("Problem: Cannot test UserServiceBad without real database");
        
        System.out.println("\n=== AFTER: Following DIP ===\n");
        
        // Use MySQL
        System.out.println("Using MySQL:");
        Database mysql = new MySQLDatabaseImpl();
        UserService userService1 = new UserService(mysql);
        userService1.saveUser("Alice Smith");
        
        System.out.println("\nUsing PostgreSQL:");
        Database postgres = new PostgreSQLDatabase();
        UserService userService2 = new UserService(postgres);
        userService2.saveUser("Bob Johnson");
        
        System.out.println("\nUsing MongoDB:");
        Database mongo = new MongoDBDatabase();
        UserService userService3 = new UserService(mongo);
        userService3.saveUser("Charlie Brown");
        
        // Switch database at runtime
        System.out.println("\nSwitching database:");
        userService1.setDatabase(postgres);
        userService1.updateUser("Alice Smith (updated)");
        
        // Use mock for testing
        System.out.println("\nTesting with mock:");
        MockDatabase mockDb = new MockDatabase();
        UserService testService = new UserService(mockDb);
        testService.saveUser("Test User");
        System.out.println("Last saved: " + mockDb.getLastSavedData());
        
        // Multiple services using same database
        System.out.println("\nMultiple services:");
        Database sharedDb = new MySQLDatabaseImpl();
        UserService userSvc = new UserService(sharedDb);
        ReportService reportSvc = new ReportService(sharedDb);
        
        userSvc.saveUser("David Lee");
        reportSvc.generateReport("Monthly Report");
        
        System.out.println("\n=== Benefits of DIP ===");
        System.out.println("- High-level modules independent of low-level details");
        System.out.println("- Easy to switch implementations");
        System.out.println("- Easy to test with mocks");
        System.out.println("- Loose coupling between layers");
        System.out.println("- Both depend on abstractions, not concretions");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Dependency injection and method calls through interfaces are constant time. The interface dispatch adds negligible overhead.

**Space Complexity**: O(1) - Each service holds one reference to a database implementation. No additional space overhead from using interfaces.

## Edge Cases and Pitfalls

- **Over-Abstraction**: Don't create interfaces for everything. Only abstract dependencies that are likely to change or need testing.
- **Dependency Injection Frameworks**: For complex applications, consider using DI frameworks (Spring, Guice) to manage dependencies.
- **Constructor vs Setter Injection**: Constructor injection ensures dependencies are always set. Setter injection allows changing dependencies at runtime.
- **Circular Dependencies**: Be careful not to create circular dependencies between modules. Use interfaces to break cycles.
- **Interface Ownership**: High-level modules should define the interfaces they need, not use interfaces defined by low-level modules.
- **Testing**: DIP makes unit testing much easier by allowing mock implementations to be injected.

## Interview-Ready Answer

"The Dependency Inversion Principle states that high-level modules should not depend on low-level modules; both should depend on abstractions. I'd show a UserService directly creating a MySQLDatabase instance, causing tight coupling. The fix is to introduce a Database interface, make concrete databases implement it, and inject the database into UserService through the constructor. This allows switching between MySQL, PostgreSQL, MongoDB, or mock implementations without modifying UserService. It enables loose coupling and easy testing. Time and space complexity are O(1)."
