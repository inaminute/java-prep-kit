# Singleton Pattern

## Problem Statement

Implement the Singleton design pattern in Java. The Singleton pattern ensures that a class has only one instance throughout the application lifecycle and provides a global point of access to that instance. Your implementation should be thread-safe and prevent multiple instances from being created even in a multi-threaded environment.

**Requirements:**
- Only one instance of the class should exist
- Provide a global access point to the instance
- Ensure thread safety
- Prevent instantiation through reflection or cloning

## Approach

- Make the constructor private to prevent external instantiation
- Create a private static instance variable to hold the single instance
- Provide a public static method (getInstance) to access the instance
- Use double-checked locking for thread-safe lazy initialization
- Implement readResolve() to prevent multiple instances during deserialization
- Override clone() to prevent cloning

## Solution

```java
import java.io.Serializable;

public class Singleton implements Serializable, Cloneable {
    // Volatile ensures visibility of changes across threads
    private static volatile Singleton instance;
    
    // Private constructor prevents instantiation
    private Singleton() {
        // Prevent reflection-based instantiation
        if (instance != null) {
            throw new IllegalStateException("Instance already exists!");
        }
    }
    
    // Double-checked locking for thread-safe lazy initialization
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
    
    // Prevent cloning
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not allowed for Singleton");
    }
    
    // Prevent multiple instances during deserialization
    protected Object readResolve() {
        return getInstance();
    }
    
    // Example business method
    public void showMessage() {
        System.out.println("Hello from Singleton instance!");
    }
}

// Alternative: Enum-based Singleton (simplest and most robust)
enum SingletonEnum {
    INSTANCE;
    
    public void showMessage() {
        System.out.println("Hello from Enum Singleton!");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Getting the instance is constant time. The synchronized block is only entered once during initialization.

**Space Complexity**: O(1) - Only one instance is created regardless of how many times getInstance() is called.

## Edge Cases and Pitfalls

- **Multi-threading**: Without proper synchronization, multiple threads could create multiple instances. Double-checked locking with volatile solves this.
- **Reflection Attack**: Reflection can bypass private constructors. Add a check in the constructor to throw an exception if instance already exists.
- **Serialization**: Deserializing a singleton creates a new instance. Implement readResolve() to return the existing instance.
- **Cloning**: Override clone() to prevent creating copies of the singleton.
- **Eager vs Lazy Initialization**: Eager initialization (creating instance at class loading) is simpler but wastes resources if never used. Lazy initialization is more efficient but requires thread safety.

## Interview-Ready Answer

"I'd implement Singleton using double-checked locking with a volatile instance variable for thread-safe lazy initialization. The constructor is private to prevent external instantiation, and getInstance() provides global access. For the most robust solution, I'd use an enum-based singleton which handles serialization and reflection automatically. Time and space complexity are both O(1)."
