# Implement a thread-safe counter

## Problem Statement

Create a thread-safe counter using different approaches: synchronized, AtomicInteger, and locks.

## Approach

- **Synchronized**: Simple but can be slower
- **AtomicInteger**: Lock-free, better performance
- **ReentrantLock**: Explicit locking with more control
- **Compare performance**: Benchmark different approaches

## Solution

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Approach 1: Synchronized
class SynchronizedCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public synchronized int get() {
        return count;
    }
}

// Approach 2: AtomicInteger
class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();
    }
    
    public int get() {
        return count.get();
    }
}

// Approach 3: ReentrantLock
class LockCounter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
    
    public int get() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

// Performance comparison
class CounterBenchmark {
    private static final int THREADS = 10;
    private static final int ITERATIONS = 100000;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Synchronized: " + benchmark(new SynchronizedCounter()) + "ms");
        System.out.println("Atomic: " + benchmark(new AtomicCounter()) + "ms");
        System.out.println("Lock: " + benchmark(new LockCounter()) + "ms");
    }
    
    private static long benchmark(Object counter) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ITERATIONS; j++) {
                    if (counter instanceof SynchronizedCounter) {
                        ((SynchronizedCounter) counter).increment();
                    } else if (counter instanceof AtomicCounter) {
                        ((AtomicCounter) counter).increment();
                    } else if (counter instanceof LockCounter) {
                        ((LockCounter) counter).increment();
                    }
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        return System.currentTimeMillis() - start;
    }
}
```

## Performance Comparison

Typically: **AtomicInteger > ReentrantLock > Synchronized**

AtomicInteger is usually fastest because:
- Lock-free using CAS operations
- No thread blocking
- Better scalability

## Edge Cases and Pitfalls

- **Overflow**: All approaches can overflow at Integer.MAX_VALUE
- **Visibility**: All approaches ensure visibility
- **Atomicity**: All approaches ensure atomic increment
- **Common Pitfall**: Using plain int without synchronization

## Interview-Ready Answer

"A thread-safe counter can be implemented using synchronized methods, AtomicInteger, or ReentrantLock. AtomicInteger is typically the best choice as it uses lock-free CAS operations for better performance and scalability. Synchronized is simpler but can be slower under high contention. ReentrantLock offers more control but requires explicit lock management."

**Tags**: atomic, thread-safety, counter
