# ReadWriteLock

## Problem Statement

Explain and demonstrate ReadWriteLock from java.util.concurrent.locks package. Show how ReadWriteLock allows multiple concurrent readers or a single writer, improving performance for read-heavy workloads. Implement a cache using ReadWriteLock and compare it with using a single lock.

**Input**: Multiple reader and writer threads

**Output**: Optimized concurrent access with separate read and write locks

**Constraints**: 
- Must demonstrate ReadWriteLock usage
- Should show readLock() and writeLock() methods
- Must explain performance benefits

## Approach

- ReadWriteLock maintains two locks: read lock and write lock
- Multiple threads can hold read lock simultaneously
- Only one thread can hold write lock (exclusive)
- Write lock blocks all readers and other writers
- Improves performance for read-heavy scenarios
- Use ReentrantReadWriteLock implementation
- Always unlock in finally blocks

## Solution

```java
import java.util.concurrent.locks.*;
import java.util.*;

// Cache with ReadWriteLock
class Cache<K, V> {
    private Map<K, V> map = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    
    public V get(K key) {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " reading: " + key);
            Thread.sleep(100); // Simulate read time
            return map.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    public void put(K key, V value) {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " writing: " + key);
            Thread.sleep(200); // Simulate write time
            map.put(key, value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            writeLock.unlock();
        }
    }
    
    public int size() {
        readLock.lock();
        try {
            return map.size();
        } finally {
            readLock.unlock();
        }
    }
}

// Shared resource with statistics
class SharedResource {
    private int value = 0;
    private int readCount = 0;
    private int writeCount = 0;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public int read() {
        lock.readLock().lock();
        try {
            readCount++;
            System.out.println(Thread.currentThread().getName() + 
                             " reading value: " + value);
            Thread.sleep(50);
            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void write(int newValue) {
        lock.writeLock().lock();
        try {
            writeCount++;
            System.out.println(Thread.currentThread().getName() + 
                             " writing value: " + newValue);
            value = newValue;
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void printStats() {
        lock.readLock().lock();
        try {
            System.out.println("Reads: " + readCount + ", Writes: " + writeCount);
        } finally {
            lock.readLock().unlock();
        }
    }
}

public class ReadWriteLockDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Cache with ReadWriteLock ===");
        testCache();
        
        System.out.println("\n=== Multiple Readers ===");
        testMultipleReaders();
        
        System.out.println("\n=== Lock Downgrading ===");
        testLockDowngrading();
    }
    
    private static void testCache() throws InterruptedException {
        Cache<String, String> cache = new Cache<>();
        
        // Writer thread
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                cache.put("key" + i, "value" + i);
            }
        }, "Writer");
        
        // Reader threads
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 1; j <= 5; j++) {
                    cache.get("key" + j);
                }
            }, "Reader-" + (i + 1));
        }
        
        writer.start();
        Thread.sleep(500); // Let writer add some data
        
        for (Thread reader : readers) {
            reader.start();
        }
        
        writer.join();
        for (Thread reader : readers) {
            reader.join();
        }
        
        System.out.println("Cache size: " + cache.size());
    }
    
    private static void testMultipleReaders() throws InterruptedException {
        SharedResource resource = new SharedResource();
        resource.write(100);
        
        // Create multiple reader threads
        Thread[] readers = new Thread[5];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    resource.read();
                }
            }, "Reader-" + (i + 1));
        }
        
        // Create writer thread
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                resource.write(i * 100);
            }
        }, "Writer");
        
        // Start all threads
        for (Thread reader : readers) {
            reader.start();
        }
        writer.start();
        
        // Wait for completion
        for (Thread reader : readers) {
            reader.join();
        }
        writer.join();
        
        resource.printStats();
    }
    
    private static void testLockDowngrading() {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        
        // Acquire write lock
        lock.writeLock().lock();
        try {
            System.out.println("Write lock acquired");
            
            // Downgrade to read lock
            lock.readLock().lock();
            System.out.println("Read lock acquired (while holding write lock)");
        } finally {
            lock.writeLock().unlock();
            System.out.println("Write lock released");
        }
        
        try {
            System.out.println("Now holding only read lock");
        } finally {
            lock.readLock().unlock();
            System.out.println("Read lock released");
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for lock acquisition and release

**Space Complexity**: O(1) per ReadWriteLock instance

## Edge Cases and Pitfalls

- **Write starvation**: With many readers, writers may starve. ReentrantReadWriteLock can be configured as fair to prevent this.
- **Lock upgrade not supported**: Cannot upgrade from read lock to write lock directly. Must release read lock first, then acquire write lock.
- **Lock downgrade supported**: Can downgrade from write lock to read lock by acquiring read lock before releasing write lock.
- **Forgetting to unlock**: Always use try-finally for both read and write locks to prevent deadlocks.

## Interview-Ready Answer

"ReadWriteLock provides separate locks for reading and writing. Multiple threads can hold the read lock simultaneously, but the write lock is exclusive. This improves performance for read-heavy workloads since readers don't block each other. ReentrantReadWriteLock is the standard implementation. It supports lock downgrading (write to read) but not upgrading (read to write). Use ReadWriteLock when you have many more reads than writes to allow concurrent reads while maintaining write safety."
