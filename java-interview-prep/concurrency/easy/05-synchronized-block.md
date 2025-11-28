# Synchronized Block

## Problem Statement

Demonstrate the use of synchronized blocks in Java for fine-grained thread synchronization. Compare synchronized blocks with synchronized methods and show how synchronized blocks provide more flexibility by allowing you to lock on specific objects and synchronize only critical sections of code.

**Input**: Multiple threads accessing shared resources

**Output**: Thread-safe operations with optimized locking

**Constraints**: 
- Must show synchronized block syntax
- Should demonstrate locking on different objects
- Must compare with synchronized methods

## Approach

- Synchronized blocks provide more granular control than synchronized methods
- Can lock on any object, not just 'this' or the class object
- Only the critical section needs to be synchronized, improving performance
- Multiple synchronized blocks can use different locks for better concurrency
- Syntax: synchronized(lockObject) { critical section }
- Choose lock objects carefully to avoid deadlocks and ensure proper synchronization

## Solution

```java
import java.util.ArrayList;
import java.util.List;

// Example 1: Synchronized block vs synchronized method
class SynchronizedComparison {
    private int count = 0;
    private final Object lock = new Object();
    
    // Synchronized method - locks entire method
    public synchronized void incrementMethod() {
        count++;
    }
    
    // Synchronized block - locks only critical section
    public void incrementBlock() {
        // Non-critical code here (not synchronized)
        System.out.println("Before critical section");
        
        synchronized (this) {
            // Only critical section is synchronized
            count++;
        }
        
        // Non-critical code here (not synchronized)
        System.out.println("After critical section");
    }
    
    // Synchronized block with custom lock object
    public void incrementWithCustomLock() {
        synchronized (lock) {
            count++;
        }
    }
    
    public int getCount() {
        synchronized (this) {
            return count;
        }
    }
}

// Example 2: Multiple locks for better concurrency
class BankAccountWithMultipleLocks {
    private double balance;
    private List<String> transactionHistory;
    
    private final Object balanceLock = new Object();
    private final Object historyLock = new Object();
    
    public BankAccountWithMultipleLocks(double initialBalance) {
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }
    
    public void deposit(double amount) {
        // Lock only balance for deposit
        synchronized (balanceLock) {
            balance += amount;
        }
        
        // Lock only history for logging (can happen concurrently with balance operations)
        synchronized (historyLock) {
            transactionHistory.add("Deposit: " + amount);
        }
    }
    
    public void withdraw(double amount) {
        synchronized (balanceLock) {
            if (balance >= amount) {
                balance -= amount;
            }
        }
        
        synchronized (historyLock) {
            transactionHistory.add("Withdraw: " + amount);
        }
    }
    
    public double getBalance() {
        synchronized (balanceLock) {
            return balance;
        }
    }
    
    public List<String> getTransactionHistory() {
        synchronized (historyLock) {
            return new ArrayList<>(transactionHistory);
        }
    }
}

// Example 3: Synchronized block on class object
class StaticSynchronizedBlock {
    private static int staticCounter = 0;
    
    public static void incrementStatic() {
        // Synchronized on the class object
        synchronized (StaticSynchronizedBlock.class) {
            staticCounter++;
        }
    }
    
    public static int getStaticCounter() {
        synchronized (StaticSynchronizedBlock.class) {
            return staticCounter;
        }
    }
}

// Example 4: Demonstration
public class SynchronizedBlockDemo {
    public static void main(String[] args) throws InterruptedException {
        BankAccountWithMultipleLocks account = new BankAccountWithMultipleLocks(1000);
        
        // Create multiple threads for deposits and withdrawals
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    account.deposit(10);
                }
            });
        }
        
        for (int i = 5; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    account.withdraw(5);
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Final balance: " + account.getBalance());
        System.out.println("Transaction count: " + account.getTransactionHistory().size());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Lock acquisition and release are constant time operations

**Space Complexity**: O(1) - Each lock object requires constant space

## Edge Cases and Pitfalls

- **Locking on null**: Attempting to synchronize on a null object throws NullPointerException. Always ensure lock objects are properly initialized.
- **Locking on mutable objects**: Don't synchronize on objects that can change (like String literals or Integer objects). Use final Object instances as locks.
- **Deadlock with multiple locks**: When using multiple locks, always acquire them in a consistent order across all threads to prevent deadlock.
- **Synchronizing on 'this' in public methods**: External code can lock on your object, potentially causing unexpected behavior. Use private lock objects for better encapsulation.

## Interview-Ready Answer

"Synchronized blocks provide finer-grained control than synchronized methods by allowing you to lock on specific objects and synchronize only critical sections of code. The syntax is synchronized(lockObject) { critical section }. This improves performance by reducing lock contention and allows multiple independent synchronized blocks with different locks to execute concurrently. You can lock on 'this', a class object, or custom private lock objects for better encapsulation."
