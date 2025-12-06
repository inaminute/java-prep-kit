# What is the volatile keyword?

## Problem Statement

Explain how the volatile keyword ensures visibility of variable changes across threads.

## Approach

- **Memory visibility**: Ensures changes are immediately visible to all threads
- **No caching**: Prevents thread-local caching of variable values
- **Happens-before relationship**: Establishes ordering guarantees
- **Not atomic for compound operations**: volatile doesn't make `count++` atomic
- **Lightweight synchronization**: Less overhead than synchronized

## Solution

```java
// Problem: Without volatile
class WithoutVolatile {
    private boolean flag = false;
    
    public void writer() {
        flag = true; // May not be visible to reader thread immediately
    }
    
    public void reader() {
        while (!flag) {
            // May loop forever due to caching
        }
        System.out.println("Flag is true!");
    }
}

// Solution: With volatile
class WithVolatile {
    private volatile boolean flag = false;
    
    public void writer() {
        flag = true; // Immediately visible to all threads
    }
    
    public void reader() {
        while (!flag) {
            // Will see the change
        }
        System.out.println("Flag is true!");
    }
}

// Demonstration
public class VolatileDemo {
    public static void main(String[] args) throws InterruptedException {
        WithVolatile example = new WithVolatile();
        
        Thread reader = new Thread(() -> {
            System.out.println("Reader waiting...");
            example.reader();
        });
        
        Thread writer = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Writer setting flag");
                example.writer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        reader.start();
        writer.start();
        
        reader.join();
        writer.join();
    }
}

// Volatile is NOT enough for compound operations
class VolatileNotAtomic {
    private volatile int count = 0;
    
    // WRONG: This is still not thread-safe!
    public void increment() {
        count++; // Read-modify-write is not atomic
    }
    
    // CORRECT: Use synchronized or AtomicInteger
    public synchronized void incrementSafe() {
        count++;
    }
}

// Good use case: Status flags
class StatusFlag {
    private volatile boolean running = true;
    
    public void run() {
        while (running) {
            // Do work
        }
    }
    
    public void stop() {
        running = false; // Immediately visible to run() thread
    }
}

// Double-checked locking with volatile
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
```

## When to Use Volatile

### Good Use Cases:
1. **Status flags**: Boolean flags for thread coordination
2. **One writer, multiple readers**: Single thread writes, others read
3. **Independent variables**: Variable doesn't depend on its current value
4. **Double-checked locking**: Singleton pattern implementation

### When NOT to Use:
1. **Compound operations**: `count++`, `count += 5`
2. **Invariants involving multiple variables**: Need synchronized
3. **Complex state transitions**: Use locks instead

## Volatile vs Synchronized

| Aspect | volatile | synchronized |
|--------|----------|--------------|
| Atomicity | No | Yes |
| Visibility | Yes | Yes |
| Ordering | Limited | Full |
| Blocking | No | Yes |
| Performance | Faster | Slower |
| Use case | Simple flags | Complex operations |

## Edge Cases and Pitfalls

- **Not atomic**: `volatile int count++` is still not thread-safe
- **64-bit values**: Without volatile, long/double reads may see partial updates
- **Performance**: volatile is faster than synchronized but still has cost
- **Common Pitfall**: Using volatile for compound operations thinking it provides atomicity

## Interview-Ready Answer

"The volatile keyword ensures that changes to a variable are immediately visible to all threads by preventing thread-local caching and establishing happens-before relationships. It's useful for status flags and simple coordination but doesn't provide atomicity for compound operations like increment. For those cases, you need synchronized or atomic classes."

**Tags**: volatile, memory-visibility, keywords
