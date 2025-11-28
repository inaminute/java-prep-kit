# ReentrantLock

## Problem Statement

Explain and demonstrate ReentrantLock from java.util.concurrent.locks package. Show how ReentrantLock provides more flexibility than synchronized blocks, including try-lock, timed locking, and interruptible locking. Compare ReentrantLock with synchronized and explain when to use each.

**Input**: Multiple threads accessing shared resources

**Output**: Thread-safe operations with advanced locking features

**Constraints**: 
- Must demonstrate ReentrantLock usage
- Should show lock() and unlock() methods
- Must explain advantages over synchronized

## Approach

- ReentrantLock is an explicit lock with more features than synchronized
- Must manually lock() and unlock() (use try-finally)
- Supports tryLock() for non-blocking lock attempts
- Supports lockInterruptibly() for interruptible locking
- Can be fair or unfair
- Provides condition variables for complex coordination
- Use synchronized for simple cases, ReentrantLock for advanced features

## Solution

```java
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

// Counter with ReentrantLock
class LockCounter {
    private int count = 0;
    private ReentrantLock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // Always unlock in finally
        }
    }
    
    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean tryIncrement() {
        if (lock.tryLock()) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }
}

// Bank account with ReentrantLock
class BankAccount {
    private double balance;
    private ReentrantLock lock = new ReentrantLock(true); // Fair lock
    
    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }
    
    public boolean transfer(BankAccount target, double amount) {
        // Try to acquire both locks
        if (this.lock.tryLock()) {
            try {
                if (target.lock.tryLock()) {
                    try {
                        if (this.balance >= amount) {
                            this.balance -= amount;
                            target.balance += amount;
                            System.out.println("Transfer successful: " + amount);
                            return true;
                        }
                    } finally {
                        target.lock.unlock();
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        System.out.println("Transfer failed");
        return false;
    }
    
    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }
}

public class ReentrantLockDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Basic ReentrantLock ===");
        testBasicLock();
        
        System.out.println("\n=== TryLock ===");
        testTryLock();
        
        System.out.println("\n=== Timed Lock ===");
        testTimedLock();
        
        System.out.println("\n=== Interruptible Lock ===");
        testInterruptibleLock();
        
        System.out.println("\n=== Condition Variables ===");
        testConditions();
    }
    
    private static void testBasicLock() throws InterruptedException {
        LockCounter counter = new LockCounter();
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Final count: " + counter.getCount());
    }
    
    private static void testTryLock() {
        LockCounter counter = new LockCounter();
        
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                if (counter.tryIncrement()) {
                    System.out.println("Thread 1 incremented");
                } else {
                    System.out.println("Thread 1 failed to acquire lock");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        thread1.start();
    }
    
    private static void testTimedLock() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        
        Thread thread1 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Thread 1 acquired lock");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                System.out.println("Thread 2 trying to acquire lock...");
                if (lock.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("Thread 2 acquired lock");
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("Thread 2 timeout");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        thread1.start();
        Thread.sleep(500);
        thread2.start();
        
        thread1.join();
        thread2.join();
    }
    
    private static void testInterruptibleLock() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        
        Thread thread1 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Thread 1 holding lock");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                System.out.println("Thread 2 waiting for lock (interruptible)");
                lock.lockInterruptibly();
                try {
                    System.out.println("Thread 2 acquired lock");
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("Thread 2 interrupted while waiting");
            }
        });
        
        thread1.start();
        Thread.sleep(500);
        thread2.start();
        Thread.sleep(1000);
        thread2.interrupt(); // Interrupt waiting thread
        
        thread1.join();
        thread2.join();
    }
    
    private static void testConditions() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        
        Thread producer = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Producer working...");
                Thread.sleep(2000);
                System.out.println("Producer signaling");
                condition.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        
        Thread consumer = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Consumer waiting...");
                condition.await();
                System.out.println("Consumer received signal");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        
        consumer.start();
        Thread.sleep(500);
        producer.start();
        
        producer.join();
        consumer.join();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for lock() and unlock() operations

**Space Complexity**: O(1) per lock instance

## Edge Cases and Pitfalls

- **Forgetting to unlock**: Always use try-finally to ensure unlock() is called. Failing to unlock causes deadlock.
- **Unlocking without locking**: Calling unlock() without holding the lock throws IllegalMonitorStateException.
- **Reentrant behavior**: Same thread can acquire the lock multiple times (must unlock same number of times).
- **Fair vs unfair**: Fair locks guarantee FIFO but have lower throughput. Default is unfair for better performance.

## Interview-Ready Answer

"ReentrantLock provides more flexibility than synchronized, including tryLock() for non-blocking attempts, timed locking with timeout, and lockInterruptibly() for interruptible waits. It must be manually locked and unlocked using try-finally blocks. ReentrantLock supports condition variables for complex coordination and can be fair or unfair. Use synchronized for simple cases and ReentrantLock when you need advanced features like try-lock, timeouts, or multiple conditions. The lock is reentrant, meaning the same thread can acquire it multiple times."
