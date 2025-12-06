# Explain StampedLock and optimistic locking

## Problem Statement

Describe StampedLock's optimistic read mode and when it provides better performance than ReadWriteLock.

## Approach

- **Three modes**: Read, write, and optimistic read
- **Stamp validation**: Check if data changed during read
- **No blocking reads**: Optimistic reads don't block
- **Better performance**: When reads greatly outnumber writes
- **Not reentrant**: Unlike ReentrantReadWriteLock

## Solution

```java
import java.util.concurrent.locks.StampedLock;

public class StampedLockExample {
    private double x, y;
    private final StampedLock lock = new StampedLock();
    
    // Optimistic read
    public double distanceFromOrigin() {
        long stamp = lock.tryOptimisticRead(); // Get optimistic stamp
        double currentX = x;
        double currentY = y;
        
        if (!lock.validate(stamp)) {
            // Data changed, upgrade to read lock
            stamp = lock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
    
    // Write lock
    public void move(double deltaX, double deltaY) {
        long stamp = lock.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
    
    // Read lock
    public double getX() {
        long stamp = lock.readLock();
        try {
            return x;
        } finally {
            lock.unlockRead(stamp);
        }
    }
    
    // Optimistic read with retry
    public double[] getCoordinates() {
        long stamp = lock.tryOptimisticRead();
        double currentX = x;
        double currentY = y;
        
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        
        return new double[]{currentX, currentY};
    }
}

// Comparison with ReadWriteLock
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ReadWriteLockExample {
    private double x, y;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public double distanceFromOrigin() {
        lock.readLock().lock(); // Always blocks if writer present
        try {
            return Math.sqrt(x * x + y * y);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void move(double deltaX, double deltaY) {
        lock.writeLock().lock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            lock.writeLock().unlock();
        }
    }
}

// Lock conversion
class LockConversionExample {
    private int value = 0;
    private final StampedLock lock = new StampedLock();
    
    public void conditionalUpdate() {
        long stamp = lock.readLock();
        try {
            while (value < 100) {
                // Try to convert to write lock
                long writeStamp = lock.tryConvertToWriteLock(stamp);
                if (writeStamp != 0) {
                    // Conversion succeeded
                    stamp = writeStamp;
                    value++;
                    break;
                } else {
                    // Conversion failed, release read and acquire write
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                }
            }
        } finally {
            lock.unlock(stamp);
        }
    }
}

// Performance comparison
class PerformanceTest {
    private static final int ITERATIONS = 1000000;
    
    static class StampedCounter {
        private long count = 0;
        private final StampedLock lock = new StampedLock();
        
        public long read() {
            long stamp = lock.tryOptimisticRead();
            long currentCount = count;
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    currentCount = count;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return currentCount;
        }
        
        public void write() {
            long stamp = lock.writeLock();
            try {
                count++;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }
    
    static class RWLockCounter {
        private long count = 0;
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        
        public long read() {
            lock.readLock().lock();
            try {
                return count;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        public void write() {
            lock.writeLock().lock();
            try {
                count++;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("StampedLock: " + benchmark(new StampedCounter()) + "ms");
        System.out.println("ReadWriteLock: " + benchmark(new RWLockCounter()) + "ms");
    }
    
    private static long benchmark(Object counter) throws InterruptedException {
        Thread[] readers = new Thread[8];
        Thread writer = new Thread(() -> {
            for (int i = 0; i < ITERATIONS / 100; i++) {
                if (counter instanceof StampedCounter) {
                    ((StampedCounter) counter).write();
                } else {
                    ((RWLockCounter) counter).write();
                }
            }
        });
        
        long start = System.currentTimeMillis();
        writer.start();
        
        for (int i = 0; i < 8; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < ITERATIONS; j++) {
                    if (counter instanceof StampedCounter) {
                        ((StampedCounter) counter).read();
                    } else {
                        ((RWLockCounter) counter).read();
                    }
                }
            });
            readers[i].start();
        }
        
        writer.join();
        for (Thread reader : readers) {
            reader.join();
        }
        
        return System.currentTimeMillis() - start;
    }
}
```

## StampedLock Modes

| Mode | Description | Blocking |
|------|-------------|----------|
| Optimistic Read | No lock acquired, validate later | No |
| Read Lock | Shared read access | Yes (if writer) |
| Write Lock | Exclusive write access | Yes |

## When to Use StampedLock

**Use when:**
- Reads greatly outnumber writes
- Read operations are short
- Can tolerate retry on validation failure
- Don't need reentrancy

**Don't use when:**
- Need reentrant locking
- Writes are frequent
- Read operations are long
- Code complexity not worth performance gain

## Complexity Analysis

**Time Complexity**: O(1) for lock operations

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Not reentrant**: Same thread cannot reacquire lock
- **Validation failure**: Must handle retry logic
- **Stamp overflow**: Stamps can wrap around (rare)
- **Common Pitfall**: Forgetting to validate optimistic reads

## Interview-Ready Answer

"StampedLock provides three locking modes: write, read, and optimistic read. Optimistic reads don't acquire a lock but return a stamp that's validated later. If validation fails, you upgrade to a read lock. This provides better performance than ReadWriteLock when reads greatly outnumber writes, as optimistic reads don't block. However, it's not reentrant and requires careful validation handling."

**Tags**: stampedlock, optimistic-locking, performance
