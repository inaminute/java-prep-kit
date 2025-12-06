# What is thread synchronization?

## Problem Statement

Explain the concept of thread synchronization in Java and why it's necessary for concurrent programming.

## Approach

- **Race conditions**: Multiple threads accessing shared data simultaneously can lead to inconsistent state
- **Critical sections**: Code segments that access shared resources need protection
- **Mutual exclusion**: Only one thread should execute critical section at a time
- **Synchronization mechanisms**: Java provides synchronized keyword, locks, and atomic classes
- **Visibility and ordering**: Ensures changes made by one thread are visible to others

## Solution

```java
// Problem: Race condition without synchronization
class UnsafeCounter {
    private int count = 0;
    
    public void increment() {
        count++; // Not atomic: read, increment, write
    }
    
    public int getCount() {
        return count;
    }
}

// Solution: Synchronized counter
class SafeCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++; // Now thread-safe
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// Demonstration
public class SynchronizationDemo {
    public static void main(String[] args) throws InterruptedException {
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SafeCounter safeCounter = new SafeCounter();
        
        // Create 1000 threads incrementing each counter
        Thread[] threads = new Thread[1000];
        
        for (int i = 0; i < 1000; i++) {
            threads[i] = new Thread(() -> {
                unsafeCounter.increment();
                safeCounter.increment();
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Unsafe counter: " + unsafeCounter.getCount()); // Likely < 1000
        System.out.println("Safe counter: " + safeCounter.getCount());     // Always 1000
    }
}
```

## Why Synchronization is Necessary

1. **Prevents race conditions**: Ensures atomic operations on shared data
2. **Memory visibility**: Guarantees that changes are visible across threads
3. **Ordering guarantees**: Prevents instruction reordering that could cause issues
4. **Data consistency**: Maintains invariants in multi-threaded environments

## Edge Cases and Pitfalls

- **Over-synchronization**: Synchronizing too much can lead to performance bottlenecks and potential deadlocks
- **Under-synchronization**: Missing synchronization causes subtle, hard-to-reproduce bugs
- **Synchronizing on wrong object**: Using different locks for related data breaks protection
- **Common Pitfall**: Assuming single operations like `count++` are atomic (they're not)

## Interview-Ready Answer

"Thread synchronization is a mechanism to control access to shared resources in concurrent programming. It's necessary because without it, multiple threads can simultaneously access and modify shared data, leading to race conditions and inconsistent state. Java provides the synchronized keyword and other mechanisms to ensure only one thread executes critical sections at a time, maintaining data integrity."

**Tags**: synchronization, race-condition, fundamentals
