# Volatile Keyword

## Problem Statement

Explain the volatile keyword in Java and demonstrate its use in multithreaded programming. Show how volatile ensures visibility of changes across threads and when to use it instead of synchronized. Include examples demonstrating the problem without volatile and the solution with volatile.

**Input**: Multiple threads reading and writing shared variables

**Output**: Correct visibility of variable changes across threads

**Constraints**: 
- Must explain visibility problem
- Should demonstrate volatile usage
- Must explain when volatile is sufficient vs when synchronization is needed

## Approach

- Without volatile, threads may cache variables locally, causing visibility issues
- The volatile keyword ensures changes to a variable are immediately visible to all threads
- Volatile prevents instruction reordering around the variable
- Volatile guarantees visibility but NOT atomicity
- Use volatile for flags and state variables that are written by one thread and read by others
- For compound operations (like increment), use synchronized or atomic classes instead

## Solution

```java
// Example 1: Problem without volatile
class WithoutVolatile {
    private boolean running = true; // Not volatile
    
    public void start() {
        new Thread(() -> {
            System.out.println("Thread started");
            while (running) {
                // Thread may cache 'running' and never see the change
            }
            System.out.println("Thread stopped");
        }).start();
    }
    
    public void stop() {
        running = false; // Change may not be visible to other thread
        System.out.println("Stop called");
    }
}

// Example 2: Solution with volatile
class WithVolatile {
    private volatile boolean running = true; // Volatile ensures visibility
    
    public void start() {
        new Thread(() -> {
            System.out.println("Thread started");
            while (running) {
                // Thread will see changes to 'running' immediately
            }
            System.out.println("Thread stopped");
        }).start();
    }
    
    public void stop() {
        running = false; // Change is immediately visible to all threads
        System.out.println("Stop called");
    }
}

// Example 3: Volatile is NOT sufficient for compound operations
class VolatileNotAtomic {
    private volatile int counter = 0; // Volatile doesn't make increment atomic
    
    public void increment() {
        counter++; // NOT thread-safe! (read-modify-write)
    }
    
    public int getCounter() {
        return counter; // Reading is safe
    }
}

// Example 4: Correct usage - simple flag
class VolatileFlag {
    private volatile boolean shutdownRequested = false;
    
    public void requestShutdown() {
        shutdownRequested = true;
    }
    
    public void doWork() {
        while (!shutdownRequested) {
            // Do work
            System.out.println("Working...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Shutdown completed");
    }
}

// Example 5: Double-checked locking with volatile
class Singleton {
    private static volatile Singleton instance;
    
    private Singleton() {}
    
    public static Singleton getInstance() {
        if (instance == null) { // First check (no locking)
            synchronized (Singleton.class) {
                if (instance == null) { // Second check (with locking)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}

// Demonstration
public class VolatileDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Testing Volatile Flag ===");
        VolatileFlag worker = new VolatileFlag();
        
        Thread workerThread = new Thread(worker::doWork);
        workerThread.start();
        
        // Let it work for a while
        Thread.sleep(500);
        
        // Request shutdown
        worker.requestShutdown();
        
        // Wait for worker to finish
        workerThread.join();
        
        System.out.println("\n=== Testing Singleton ===");
        Singleton s1 = Singleton.getInstance();
        Singleton s2 = Singleton.getInstance();
        System.out.println("Same instance: " + (s1 == s2));
    }
}

/*
 * VOLATILE GUARANTEES:
 * 
 * 1. VISIBILITY: Changes to volatile variables are immediately visible to all threads
 * 2. ORDERING: Prevents instruction reordering around volatile variables
 * 3. HAPPENS-BEFORE: Writes to volatile happen-before subsequent reads
 * 
 * WHEN TO USE VOLATILE:
 * - Simple flags (boolean status variables)
 * - State variables read by many threads, written by one
 * - Double-checked locking pattern
 * - Publishing immutable objects
 * 
 * WHEN NOT TO USE VOLATILE:
 * - Compound operations (increment, check-then-act)
 * - Operations requiring atomicity
 * - Multiple related variables that must be updated together
 * 
 * Use synchronized or java.util.concurrent.atomic classes instead
 */
```

## Complexity Analysis

**Time Complexity**: O(1) - Volatile reads and writes are constant time, though slightly slower than non-volatile

**Space Complexity**: O(1) - No additional space required

## Edge Cases and Pitfalls

- **Not atomic for compound operations**: volatile doesn't make operations like increment (counter++) atomic. Use AtomicInteger or synchronized for such operations.
- **Multiple related variables**: If you need to update multiple variables atomically, volatile isn't sufficient. Use synchronized blocks to ensure atomicity.
- **Performance overhead**: Volatile variables are slower than regular variables because they prevent caching and reordering optimizations. Use only when necessary.
- **Misunderstanding scope**: Volatile only guarantees visibility and ordering, not mutual exclusion. Multiple threads can still read and write simultaneously.

## Interview-Ready Answer

"The volatile keyword in Java ensures that changes to a variable are immediately visible to all threads and prevents instruction reordering. It's useful for simple flags and state variables but doesn't provide atomicity for compound operations like increment. For atomic operations, use synchronized or atomic classes from java.util.concurrent.atomic. Volatile is lighter weight than synchronization but has more limited guarantees."
