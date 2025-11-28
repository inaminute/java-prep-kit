# Synchronized Method

## Problem Statement

Demonstrate the use of synchronized methods in Java to prevent race conditions when multiple threads access shared resources. Implement a counter class that can be safely incremented by multiple threads concurrently. Show both the problem (without synchronization) and the solution (with synchronized methods).

**Input**: Multiple threads incrementing a shared counter

**Output**: Correct final count value

**Constraints**: 
- Must demonstrate race condition without synchronization
- Must show how synchronized methods solve the problem
- Should explain the locking mechanism

## Approach

- Without synchronization, multiple threads can interfere with each other's operations
- The synchronized keyword on a method acquires the object's intrinsic lock
- Only one thread can execute a synchronized method on the same object at a time
- Other threads attempting to call synchronized methods on the same object will block
- The lock is automatically released when the method completes
- Synchronized methods prevent race conditions but may impact performance

## Solution

```java
// Unsafe counter without synchronization
class UnsafeCounter {
    private int count = 0;
    
    // Not thread-safe - race condition possible
    public void increment() {
        count++; // This is actually three operations: read, increment, write
    }
    
    public int getCount() {
        return count;
    }
}

// Safe counter with synchronized methods
class SafeCounter {
    private int count = 0;
    
    // Thread-safe - synchronized method
    public synchronized void increment() {
        count++;
    }
    
    public synchronized void decrement() {
        count--;
    }
    
    public synchronized int getCount() {
        return count;
    }
    
    // Can also synchronize on specific operations
    public void incrementBy(int value) {
        synchronized (this) {
            count += value;
        }
    }
}

// Demonstration of the problem and solution
public class SynchronizedMethodDemo {
    
    public static void main(String[] args) throws InterruptedException {
        // Demonstrate unsafe counter
        System.out.println("=== Unsafe Counter (Race Condition) ===");
        testUnsafeCounter();
        
        // Demonstrate safe counter
        System.out.println("\n=== Safe Counter (Synchronized) ===");
        testSafeCounter();
    }
    
    private static void testUnsafeCounter() throws InterruptedException {
        UnsafeCounter counter = new UnsafeCounter();
        int numThreads = 10;
        int incrementsPerThread = 1000;
        
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        int expected = numThreads * incrementsPerThread;
        int actual = counter.getCount();
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " + actual);
        System.out.println("Lost updates: " + (expected - actual));
    }
    
    private static void testSafeCounter() throws InterruptedException {
        SafeCounter counter = new SafeCounter();
        int numThreads = 10;
        int incrementsPerThread = 1000;
        
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        int expected = numThreads * incrementsPerThread;
        int actual = counter.getCount();
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " + actual);
        System.out.println("Correct: " + (expected == actual));
    }
}

// Example with synchronized static methods
class StaticSynchronizedExample {
    private static int sharedCounter = 0;
    
    // Synchronized on the class object (StaticSynchronizedExample.class)
    public static synchronized void incrementStatic() {
        sharedCounter++;
    }
    
    // Equivalent to:
    public static void incrementStaticExplicit() {
        synchronized (StaticSynchronizedExample.class) {
            sharedCounter++;
        }
    }
    
    public static synchronized int getSharedCounter() {
        return sharedCounter;
    }
}

// Bank account example
class BankAccount {
    private double balance;
    
    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }
    
    // Synchronized to prevent race conditions
    public synchronized void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println(Thread.currentThread().getName() + 
                             " deposited: " + amount + ", Balance: " + balance);
        }
    }
    
    public synchronized boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            System.out.println(Thread.currentThread().getName() + 
                             " withdrew: " + amount + ", Balance: " + balance);
            return true;
        }
        return false;
    }
    
    public synchronized double getBalance() {
        return balance;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Synchronized method execution is constant time, though there's overhead for lock acquisition

**Space Complexity**: O(1) - Each object has one intrinsic lock, requiring constant space

## Edge Cases and Pitfalls

- **Deadlock risk**: If synchronized methods call other synchronized methods on different objects, deadlock can occur. Always acquire locks in a consistent order.
- **Performance overhead**: Synchronization adds overhead and can become a bottleneck if many threads contend for the same lock. Consider finer-grained locking or lock-free alternatives for high-contention scenarios.
- **Synchronized on wrong object**: Instance methods synchronize on 'this', static methods on the class object. Mixing them doesn't provide mutual exclusion.
- **Forgetting to synchronize all access**: If some methods are synchronized but others aren't, race conditions can still occur. All methods accessing shared mutable state must be synchronized.

## Interview-Ready Answer

"Synchronized methods in Java use the object's intrinsic lock to ensure only one thread can execute the method at a time, preventing race conditions. When a thread enters a synchronized method, it acquires the lock, and other threads trying to call synchronized methods on the same object will block until the lock is released. This is essential for thread safety when multiple threads access shared mutable state. The synchronized keyword can be applied to instance methods (locks on 'this') or static methods (locks on the class object)."
