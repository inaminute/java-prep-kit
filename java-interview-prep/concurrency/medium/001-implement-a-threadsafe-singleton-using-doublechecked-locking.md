# Implement a thread-safe singleton using double-checked locking

## Problem Statement

Create a thread-safe singleton pattern using double-checked locking with proper use of volatile keyword.

## Approach

- **Lazy initialization**: Create instance only when needed
- **Double-checked locking**: Check twice to minimize synchronization overhead
- **Volatile keyword**: Prevents instruction reordering and ensures visibility
- **Performance optimization**: Synchronize only during first creation
- **Thread-safe**: Multiple threads can safely access getInstance()

## Solution

```java
// Correct implementation with volatile
public class Singleton {
    // volatile prevents instruction reordering
    private static volatile Singleton instance;
    
    private Singleton() {
        // Private constructor prevents instantiation
    }
    
    public static Singleton getInstance() {
        // First check (no locking)
        if (instance == null) {
            synchronized (Singleton.class) {
                // Second check (with locking)
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}

// Why volatile is necessary
class WithoutVolatile {
    private static WithoutVolatile instance; // WRONG: Missing volatile
    
    public static WithoutVolatile getInstance() {
        if (instance == null) {
            synchronized (WithoutVolatile.class) {
                if (instance == null) {
                    // Problem: Object construction is not atomic
                    // 1. Allocate memory
                    // 2. Initialize object
                    // 3. Assign reference
                    // Without volatile, steps 2 and 3 can be reordered!
                    instance = new WithoutVolatile();
                }
            }
        }
        return instance; // May return partially constructed object!
    }
}

// Alternative: Bill Pugh Singleton (Initialization-on-demand holder)
class BillPughSingleton {
    private BillPughSingleton() {}
    
    // Inner static class - loaded only when getInstance() is called
    private static class SingletonHolder {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }
    
    public static BillPughSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}

// Alternative: Enum Singleton (Joshua Bloch's approach)
enum EnumSingleton {
    INSTANCE;
    
    public void doSomething() {
        System.out.println("Enum singleton method");
    }
}

// Comparison of different approaches
class SingletonComparison {
    public static void main(String[] args) {
        // Double-checked locking
        Singleton s1 = Singleton.getInstance();
        Singleton s2 = Singleton.getInstance();
        System.out.println("Double-checked: " + (s1 == s2)); // true
        
        // Bill Pugh
        BillPughSingleton b1 = BillPughSingleton.getInstance();
        BillPughSingleton b2 = BillPughSingleton.getInstance();
        System.out.println("Bill Pugh: " + (b1 == b2)); // true
        
        // Enum
        EnumSingleton e1 = EnumSingleton.INSTANCE;
        EnumSingleton e2 = EnumSingleton.INSTANCE;
        System.out.println("Enum: " + (e1 == e2)); // true
    }
}

// Testing thread safety
class SingletonThreadSafetyTest {
    public static void main(String[] args) throws InterruptedException {
        final int THREAD_COUNT = 100;
        Thread[] threads = new Thread[THREAD_COUNT];
        final Singleton[] instances = new Singleton[THREAD_COUNT];
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = Singleton.getInstance();
            });
        }
        
        // Start all threads simultaneously
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all instances are the same
        Singleton first = instances[0];
        boolean allSame = true;
        for (Singleton instance : instances) {
            if (instance != first) {
                allSame = false;
                break;
            }
        }
        
        System.out.println("All instances same: " + allSame);
        System.out.println("Unique instances: " + 
            java.util.Arrays.stream(instances).distinct().count());
    }
}
```

## Why Double-Checked Locking?

### Without double-checking (always synchronized):
```java
public static synchronized Singleton getInstance() {
    if (instance == null) {
        instance = new Singleton();
    }
    return instance;
}
// Problem: Synchronization overhead on every call
```

### With double-checking:
- First check: Avoids synchronization after initialization
- Synchronized block: Only entered when instance is null
- Second check: Prevents multiple instances if multiple threads pass first check
- Result: Synchronization only during first creation

## Why Volatile is Critical

Without volatile, this can happen:
1. Thread A enters synchronized block
2. Thread A allocates memory for Singleton
3. Thread A assigns reference (before initialization completes)
4. Thread B sees non-null instance
5. Thread B returns partially constructed object!

Volatile prevents this by:
- Preventing instruction reordering
- Ensuring visibility of fully constructed object

## Comparison of Singleton Approaches

| Approach | Thread-Safe | Lazy | Performance | Complexity |
|----------|-------------|------|-------------|------------|
| Double-checked locking | Yes | Yes | High | Medium |
| Bill Pugh | Yes | Yes | High | Low |
| Enum | Yes | No | High | Very Low |
| Synchronized method | Yes | Yes | Low | Low |

## Edge Cases and Pitfalls

- **Missing volatile**: Can return partially constructed object
- **Reflection attack**: Can break singleton by accessing private constructor
- **Serialization**: Deserialization creates new instance (need readResolve())
- **Common Pitfall**: Forgetting volatile keyword in double-checked locking

## Interview-Ready Answer

"Double-checked locking is a thread-safe singleton pattern that minimizes synchronization overhead. It checks if the instance is null twice: once without locking for performance, and once inside a synchronized block to prevent multiple instances. The volatile keyword is critical to prevent instruction reordering that could expose a partially constructed object to other threads."

**Tags**: singleton, double-checked-locking, volatile
