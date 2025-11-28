# Thread-Safe Singleton Pattern

## Problem Statement

Implement various thread-safe singleton patterns in Java. Demonstrate eager initialization, synchronized lazy initialization, double-checked locking, and the Bill Pugh solution. Explain the trade-offs of each approach and when to use them.

**Input**: Multiple threads requesting singleton instance

**Output**: Single instance shared across all threads

**Constraints**: 
- Must ensure only one instance
- Must be thread-safe
- Should minimize synchronization overhead

## Approach

- Singleton ensures only one instance exists
- Thread-safety required when multiple threads access getInstance()
- Eager initialization: simple but wastes memory if not used
- Synchronized method: thread-safe but slow
- Double-checked locking: efficient but requires volatile
- Bill Pugh (static holder): best approach using class loading
- Enum singleton: simplest and prevents serialization issues

## Solution

```java
// 1. Eager Initialization
class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();
    
    private EagerSingleton() {
        System.out.println("EagerSingleton instance created");
    }
    
    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}

// 2. Synchronized Lazy Initialization
class SynchronizedSingleton {
    private static SynchronizedSingleton instance;
    
    private SynchronizedSingleton() {
        System.out.println("SynchronizedSingleton instance created");
    }
    
    public static synchronized SynchronizedSingleton getInstance() {
        if (instance == null) {
            instance = new SynchronizedSingleton();
        }
        return instance;
    }
}

// 3. Double-Checked Locking
class DoubleCheckedSingleton {
    private static volatile DoubleCheckedSingleton instance;
    
    private DoubleCheckedSingleton() {
        System.out.println("DoubleCheckedSingleton instance created");
    }
    
    public static DoubleCheckedSingleton getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckedSingleton.class) {
                if (instance == null) {
                    instance = new DoubleCheckedSingleton();
                }
            }
        }
        return instance;
    }
}

// 4. Bill Pugh Solution (Static Holder)
class BillPughSingleton {
    private BillPughSingleton() {
        System.out.println("BillPughSingleton instance created");
    }
    
    private static class SingletonHolder {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }
    
    public static BillPughSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}

// 5. Enum Singleton (Best for most cases)
enum EnumSingleton {
    INSTANCE;
    
    EnumSingleton() {
        System.out.println("EnumSingleton instance created");
    }
    
    public void doSomething() {
        System.out.println("EnumSingleton doing something");
    }
}

public class ThreadSafeSingletonDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Eager Singleton ===");
        testSingleton(() -> EagerSingleton.getInstance());
        
        System.out.println("\n=== Synchronized Singleton ===");
        testSingleton(() -> SynchronizedSingleton.getInstance());
        
        System.out.println("\n=== Double-Checked Locking ===");
        testSingleton(() -> DoubleCheckedSingleton.getInstance());
        
        System.out.println("\n=== Bill Pugh Singleton ===");
        testSingleton(() -> BillPughSingleton.getInstance());
        
        System.out.println("\n=== Enum Singleton ===");
        testEnumSingleton();
    }
    
    private static void testSingleton(Runnable task) throws InterruptedException {
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    private static void testEnumSingleton() throws InterruptedException {
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                EnumSingleton instance = EnumSingleton.INSTANCE;
                instance.doSomething();
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for getInstance() in all approaches

**Space Complexity**: O(1) - single instance

## Edge Cases and Pitfalls

- **Reflection attack**: All approaches except enum can be broken using reflection to call private constructor. Enum is immune.
- **Serialization issues**: Deserialization creates new instances. Implement readResolve() or use enum.
- **Volatile keyword required**: In double-checked locking, volatile prevents instruction reordering that could expose partially constructed objects.
- **Class loading timing**: Bill Pugh solution delays initialization until first access, combining benefits of lazy and eager initialization.

## Interview-Ready Answer

"Thread-safe singleton implementations include: eager initialization (simple but not lazy), synchronized method (thread-safe but slow), double-checked locking (efficient but requires volatile), Bill Pugh static holder (best for most cases), and enum (simplest and prevents reflection/serialization issues). Double-checked locking checks instance twice - once without synchronization for performance, once inside synchronized block for safety. The Bill Pugh solution uses static inner class for lazy initialization with thread-safety guaranteed by class loading. Enum is recommended for its simplicity and built-in serialization safety."
