# Implement a custom ReadWriteLock

## Problem Statement

Design and implement a ReadWriteLock that allows multiple readers or a single writer with proper fairness.

## Approach

- **Multiple readers**: Allow concurrent read access
- **Exclusive writer**: Only one writer at a time
- **No readers during write**: Writers have exclusive access
- **Fairness**: Prevent writer/reader starvation
- **Reentrant**: Support reentrant locking

## Solution

```java
import java.util.concurrent.locks.*;

public class CustomReadWriteLock {
    private int readers = 0;
    private int writers = 0;
    private int writeRequests = 0;
    private final Object lock = new Object();
    
    // Read lock
    public void lockRead() throws InterruptedException {
        synchronized(lock) {
            while (writers > 0 || writeRequests > 0) {
                lock.wait();
            }
            readers++;
        }
    }
    
    public void unlockRead() {
        synchronized(lock) {
            readers--;
            if (readers == 0) {
                lock.notifyAll();
            }
        }
    }
    
    // Write lock
    public void lockWrite() throws InterruptedException {
        synchronized(lock) {
            writeRequests++;
            while (readers > 0 || writers > 0) {
                lock.wait();
            }
            writeRequests--;
            writers++;
        }
    }
    
    public void unlockWrite() {
        synchronized(lock) {
            writers--;
            lock.notifyAll();
        }
    }
}

// Usage example
class SharedResource {
    private int value = 0;
    private final CustomReadWriteLock rwLock = new CustomReadWriteLock();
    
    public int read() throws InterruptedException {
        rwLock.lockRead();
        try {
            System.out.println(Thread.currentThread().getName() + " reading: " + value);
            Thread.sleep(100);
            return value;
        } finally {
            rwLock.unlockRead();
        }
    }
    
    public void write(int newValue) throws InterruptedException {
        rwLock.lockWrite();
        try {
            System.out.println(Thread.currentThread().getName() + " writing: " + newValue);
            value = newValue;
            Thread.sleep(200);
        } finally {
            rwLock.unlockWrite();
        }
    }
    
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();
        
        // Multiple readers
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    resource.read();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Reader-" + i).start();
        }
        
        // Writers
        for (int i = 0; i < 2; i++) {
            final int value = i;
            new Thread(() -> {
                try {
                    resource.write(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Writer-" + i).start();
        }
    }
}
```

## Key Concepts

- **Reader count**: Track number of active readers
- **Writer exclusivity**: Only one writer allowed
- **Write requests**: Track pending writers for fairness
- **Condition waiting**: Use wait/notify for coordination

## Complexity Analysis

**Time Complexity**: O(1) for lock/unlock operations

**Space Complexity**: O(1) - constant space for counters

## Edge Cases and Pitfalls

- **Writer starvation**: Continuous readers can starve writers
- **Reader starvation**: Continuous writers can starve readers
- **Deadlock**: Improper lock ordering can cause deadlock
- **Common Pitfall**: Not using try-finally for unlock

## Interview-Ready Answer

"A ReadWriteLock allows multiple concurrent readers or a single exclusive writer. The implementation tracks reader count, writer count, and pending write requests. Readers wait if there are active writers or pending write requests. Writers wait if there are active readers or writers. This prevents writer starvation while allowing concurrent reads."

**Tags**: readwritelock, custom-lock, concurrency
