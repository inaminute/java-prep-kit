# Explain ReentrantLock vs synchronized

## Problem Statement

Compare ReentrantLock with synchronized keyword in terms of features, flexibility, and use cases.

## Approach

- **Explicit locking**: ReentrantLock requires manual lock/unlock
- **Advanced features**: tryLock, timed locking, interruptible locking
- **Fairness policy**: ReentrantLock supports fair lock acquisition
- **Condition variables**: Multiple condition queues
- **Performance**: Similar in most cases

## Solution

```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

// Using synchronized
class SynchronizedExample {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// Using ReentrantLock
class ReentrantLockExample {
    private int count = 0;
    private final Lock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // Must unlock in finally block
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
}

// Advanced feature: tryLock
class TryLockExample {
    private final Lock lock = new ReentrantLock();
    
    public void performTask() {
        if (lock.tryLock()) {
            try {
                System.out.println("Lock acquired, performing task");
                // Do work
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("Could not acquire lock, doing alternative");
            // Do alternative work
        }
    }
    
    public void performTaskWithTimeout() {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Lock acquired within timeout");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("Timeout waiting for lock");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Advanced feature: Interruptible locking
class InterruptibleLockExample {
    private final Lock lock = new ReentrantLock();
    
    public void performTask() throws InterruptedException {
        lock.lockInterruptibly(); // Can be interrupted while waiting
        try {
            System.out.println("Performing long task");
            Thread.sleep(5000);
        } finally {
            lock.unlock();
        }
    }
}

// Advanced feature: Fairness
class FairnessExample {
    private final Lock fairLock = new ReentrantLock(true); // Fair lock
    private final Lock unfairLock = new ReentrantLock(); // Unfair (default)
    
    public void useFairLock() {
        fairLock.lock();
        try {
            // Threads acquire lock in order they requested it
        } finally {
            fairLock.unlock();
        }
    }
}

// Advanced feature: Multiple conditions
class BoundedBuffer {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Object[] items = new Object[10];
    private int count, putIndex, takeIndex;
    
    public void put(Object item) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) {
                notFull.await(); // Wait on notFull condition
            }
            items[putIndex] = item;
            putIndex = (putIndex + 1) % items.length;
            count++;
            notEmpty.signal(); // Signal notEmpty condition
        } finally {
            lock.unlock();
        }
    }
    
    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await(); // Wait on notEmpty condition
            }
            Object item = items[takeIndex];
            takeIndex = (takeIndex + 1) % items.length;
            count--;
            notFull.signal(); // Signal notFull condition
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

## Feature Comparison

| Feature | synchronized | ReentrantLock |
|---------|--------------|---------------|
| Lock acquisition | Implicit | Explicit (lock/unlock) |
| Try lock | No | Yes (tryLock) |
| Timed lock | No | Yes (tryLock with timeout) |
| Interruptible | No | Yes (lockInterruptibly) |
| Fairness | No | Yes (optional) |
| Multiple conditions | No | Yes (newCondition) |
| Lock status | No | Yes (isLocked, getHoldCount) |
| Automatic release | Yes | No (must use finally) |

## When to Use Each

### Use synchronized when:
- Simple locking requirements
- Want automatic lock release
- Don't need advanced features
- Prefer simpler code

### Use ReentrantLock when:
- Need tryLock functionality
- Need timed lock acquisition
- Need interruptible locking
- Need fairness guarantees
- Need multiple condition variables
- Need lock status information

## Edge Cases and Pitfalls

- **Forgetting unlock**: Always use try-finally with ReentrantLock
- **Deadlock with fairness**: Fair locks can be slower
- **Reentrancy**: Both support reentrant locking
- **Common Pitfall**: Not unlocking ReentrantLock in finally block

## Interview-Ready Answer

"ReentrantLock provides more flexibility than synchronized with features like tryLock, timed locking, interruptible locking, fairness policies, and multiple condition variables. However, it requires explicit lock/unlock in try-finally blocks. Use synchronized for simple cases and ReentrantLock when you need advanced features. Performance is similar in most scenarios."

**Tags**: reentrantlock, synchronized, locks
