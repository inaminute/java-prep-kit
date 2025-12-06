# Explain the Java Memory Model and happens-before

## Problem Statement

Describe the Java Memory Model, happens-before relationships, and how they guarantee memory visibility.

## Approach

- **Memory visibility**: Ensures changes are visible across threads
- **Happens-before**: Ordering guarantee between operations
- **Synchronization actions**: volatile, synchronized, locks
- **Reordering**: Compiler and CPU can reorder instructions
- **Memory barriers**: Prevent reordering

## Solution

```java
// Problem: Without synchronization
class UnsafePublication {
    private int value;
    private boolean initialized = false;
    
    public void writer() {
        value = 42;              // 1
        initialized = true;      // 2
        // Without synchronization, 2 can happen before 1!
    }
    
    public int reader() {
        if (initialized) {       // 3
            return value;        // 4
        }
        return 0;
        // May return 0 even if initialized is true!
    }
}

// Solution 1: Using volatile
class VolatileSolution {
    private int value;
    private volatile boolean initialized = false;
    
    public void writer() {
        value = 42;              // Happens-before
        initialized = true;      // volatile write
    }
    
    public int reader() {
        if (initialized) {       // volatile read
            return value;        // Guaranteed to see 42
        }
        return 0;
    }
}

// Solution 2: Using synchronized
class SynchronizedSolution {
    private int value;
    private boolean initialized = false;
    
    public synchronized void writer() {
        value = 42;
        initialized = true;
    }
    
    public synchronized int reader() {
        if (initialized) {
            return value;
        }
        return 0;
    }
}

// Happens-before examples
class HappensBeforeExamples {
    private int a = 0;
    private volatile boolean flag = false;
    
    // Thread 1
    public void thread1() {
        a = 1;              // 1
        flag = true;        // 2 (volatile write)
    }
    
    // Thread 2
    public void thread2() {
        if (flag) {         // 3 (volatile read)
            int b = a;      // 4 - guaranteed to see a = 1
            System.out.println(b);
        }
    }
    
    // Happens-before chain: 1 -> 2 -> 3 -> 4
}

// Double-checked locking with happens-before
class Singleton {
    private static volatile Singleton instance;
    
    public static Singleton getInstance() {
        if (instance == null) {              // Read
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton(); // Write
                    // volatile ensures full construction visible
                }
            }
        }
        return instance;
    }
}

// Final fields and happens-before
class FinalFieldExample {
    private final int x;
    private int y;
    
    public FinalFieldExample() {
        x = 1;  // Final field write
        y = 2;  // Normal field write
    }
    
    // Another thread reading this object is guaranteed to see x = 1
    // but may see y = 0 or 2
}
```

## Happens-Before Rules

1. **Program order**: Each action happens-before subsequent actions in same thread
2. **Monitor lock**: Unlock happens-before subsequent lock on same monitor
3. **Volatile**: Write to volatile happens-before subsequent read
4. **Thread start**: Thread.start() happens-before actions in started thread
5. **Thread termination**: Actions in thread happen-before Thread.join() returns
6. **Transitivity**: If A happens-before B and B happens-before C, then A happens-before C

## Memory Barriers

| Type | Description |
|------|-------------|
| LoadLoad | Prevents reordering of loads |
| StoreStore | Prevents reordering of stores |
| LoadStore | Prevents load followed by store reordering |
| StoreLoad | Full barrier, most expensive |

## Edge Cases and Pitfalls

- **Partial construction**: Object reference visible before construction complete
- **Reordering**: Instructions can be reordered without synchronization
- **Word tearing**: Long/double reads may see partial updates without volatile
- **Common Pitfall**: Assuming sequential consistency without synchronization

## Interview-Ready Answer

"The Java Memory Model defines how threads interact through memory and what behaviors are allowed. Happens-before relationships guarantee that if one action happens-before another, the first is visible to and ordered before the second. Key happens-before rules include program order, monitor locks, volatile variables, and thread operations. These guarantees prevent unexpected behaviors from compiler and CPU reordering."

**Tags**: memory-model, happens-before, visibility
