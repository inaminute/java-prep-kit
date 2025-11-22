# Try-With-Resources

## Problem Statement

Explain the try-with-resources statement introduced in Java 7. How does it improve resource management? Demonstrate its usage and explain the AutoCloseable interface. Compare it with traditional try-finally approach.

**Requirements**:
- Demonstrate try-with-resources syntax
- Show multiple resources in one statement
- Explain AutoCloseable interface
- Compare with traditional approach

## Approach

- Try-with-resources automatically closes resources that implement AutoCloseable
- Resources are closed in reverse order of declaration
- Eliminates need for explicit finally blocks for resource cleanup
- Suppressed exceptions are available via getSuppressed()
- More concise and less error-prone than try-finally

## Solution

```java
import java.io.*;

public class TryWithResources {
    
    public static void main(String[] args) {
        demonstrateTraditionalApproach();
        demonstrateTryWithResources();
        demonstrateMultipleResources();
        demonstrateCustomResource();
    }
    
    // Traditional approach - verbose and error-prone
    public static void demonstrateTraditionalApproach() {
        System.out.println("=== Traditional Try-Finally ===");
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("test.txt"));
            String line = reader.readLine();
            System.out.println(line);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Error closing reader");
                }
            }
        }
    }
    
    // Try-with-resources - clean and automatic
    public static void demonstrateTryWithResources() {
        System.out.println("\n=== Try-With-Resources ===");
        
        try (BufferedReader reader = new BufferedReader(new FileReader("test.txt"))) {
            String line = reader.readLine();
            System.out.println(line);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // reader is automatically closed here
    }
    
    // Multiple resources
    public static void demonstrateMultipleResources() {
        System.out.println("\n=== Multiple Resources ===");
        
        try (
            FileInputStream input = new FileInputStream("input.txt");
            FileOutputStream output = new FileOutputStream("output.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.write(line.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // All resources closed in reverse order: reader, output, input
    }
    
    // Custom AutoCloseable resource
    static class DatabaseConnection implements AutoCloseable {
        private String connectionId;
        
        public DatabaseConnection(String id) {
            this.connectionId = id;
            System.out.println("Opening connection: " + connectionId);
        }
        
        public void executeQuery(String query) {
            System.out.println("Executing: " + query);
        }
        
        @Override
        public void close() {
            System.out.println("Closing connection: " + connectionId);
        }
    }
    
    public static void demonstrateCustomResource() {
        System.out.println("\n=== Custom AutoCloseable ===");
        
        try (DatabaseConnection conn = new DatabaseConnection("DB-001")) {
            conn.executeQuery("SELECT * FROM users");
        }
        // Connection automatically closed
    }
    
    // Demonstrating suppressed exceptions
    static class ResourceWithException implements AutoCloseable {
        private String name;
        
        public ResourceWithException(String name) {
            this.name = name;
        }
        
        public void doWork() throws Exception {
            throw new Exception("Exception during work");
        }
        
        @Override
        public void close() throws Exception {
            throw new Exception("Exception during close");
        }
    }
    
    public static void demonstrateSuppressedExceptions() {
        try (ResourceWithException resource = new ResourceWithException("test")) {
            resource.doWork();
        } catch (Exception e) {
            System.out.println("Main exception: " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("Suppressed: " + suppressed.getMessage());
            }
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for resource management overhead

**Space Complexity**: O(1) for try-with-resources mechanism

## Edge Cases and Pitfalls

- **Resource must implement AutoCloseable**: Only works with AutoCloseable or Closeable resources
- **Close order**: Resources are closed in reverse order of declaration
- **Suppressed exceptions**: Exceptions during close() are suppressed if exception occurs in try block
- **Null resources**: If resource initialization fails, close() won't be called on null

## Interview-Ready Answer

"Try-with-resources automatically closes resources implementing AutoCloseable, eliminating explicit finally blocks. Resources are declared in parentheses after try and closed automatically in reverse order. It's more concise and safer than try-finally, handles suppressed exceptions properly, and ensures resources are always closed even if exceptions occur."
