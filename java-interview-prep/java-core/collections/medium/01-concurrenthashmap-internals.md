# ConcurrentHashMap Internals

## Problem Statement

Explain how ConcurrentHashMap achieves thread safety without locking the entire map. Compare the segment-based locking approach (Java 7) with the CAS-based approach (Java 8+), and demonstrate its advantages over Hashtable and synchronized HashMap.

**Requirements**:
- Explain Java 8+ CAS and synchronized bucket approach
- Compare with Hashtable and Collections.synchronizedMap()
- Demonstrate thread-safe operations
- Show performance benefits in concurrent scenarios

## Approach

- Java 7: Segment-based locking (16 segments by default)
- Java 8+: CAS operations + synchronized buckets for fine-grained locking
- No locking for read operations (volatile reads)
- Atomic operations: putIfAbsent(), compute(), merge()
- Better scalability than Hashtable
- Null keys and values not allowed

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentHashMapInternals {
    
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicOperations();
        demonstrateAtomicOperations();
        compareWithHashtable();
        demonstrateConcurrentPerformance();
    }
    
    public static void demonstrateBasicOperations() {
        System.out.println("=== Basic ConcurrentHashMap Operations ===");
        
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        
        // Basic put/get (thread-safe)
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        
        System.out.println("Map: " + map);
        System.out.println("Get 'A': " + map.get("A"));
        
        // Null not allowed
        try {
            map.put(null, 4);
        } catch (NullPointerException e) {
            System.out.println("Null keys not allowed");
        }
        
        try {
            map.put("D", null);
        } catch (NullPointerException e) {
            System.out.println("Null values not allowed");
        }
    }
    
    public static void demonstrateAtomicOperations() {
        System.out.println("\n=== Atomic Operations ===");
        
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("counter", 0);
        
        // putIfAbsent - atomic
        Integer prev = map.putIfAbsent("counter", 10);
        System.out.println("putIfAbsent returned: " + prev); // 0 (existing value)
        System.out.println("Value: " + map.get("counter"));  // Still 0
        
        prev = map.putIfAbsent("new", 5);
        System.out.println("putIfAbsent for new key: " + prev); // null
        System.out.println("New value: " + map.get("new"));     // 5
        
        // compute - atomic update
        map.compute("counter", (key, val) -> val == null ? 1 : val + 1);
        System.out.println("After compute: " + map.get("counter")); // 1
        
        // computeIfAbsent - lazy initialization
        map.computeIfAbsent("lazy", k -> {
            System.out.println("Computing value for " + k);
            return 100;
        });
        System.out.println("Lazy value: " + map.get("lazy"));
        
        // computeIfPresent - update if exists
        map.computeIfPresent("counter", (k, v) -> v * 2);
        System.out.println("After computeIfPresent: " + map.get("counter")); // 2
        
        // merge - combine values
        map.merge("counter", 10, (oldVal, newVal) -> oldVal + newVal);
        System.out.println("After merge: " + map.get("counter")); // 12
    }
    
    public static void compareWithHashtable() throws InterruptedException {
        System.out.println("\n=== ConcurrentHashMap vs Hashtable ===");
        
        // Hashtable - full synchronization
        Hashtable<Integer, Integer> hashtable = new Hashtable<>();
        
        // ConcurrentHashMap - fine-grained locking
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        
        int threads = 10;
        int operations = 10000;
        
        // Test Hashtable
        long start = System.nanoTime();
        Thread[] hashtableThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            hashtableThreads[i] = new Thread(() -> {
                for (int j = 0; j < operations; j++) {
                    hashtable.put(threadId * operations + j, j);
                }
            });
            hashtableThreads[i].start();
        }
        for (Thread t : hashtableThreads) {
            t.join();
        }
        long hashtableTime = System.nanoTime() - start;
        
        // Test ConcurrentHashMap
        start = System.nanoTime();
        Thread[] concurrentThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            concurrentThreads[i] = new Thread(() -> {
                for (int j = 0; j < operations; j++) {
                    concurrentMap.put(threadId * operations + j, j);
                }
            });
            concurrentThreads[i].start();
        }
        for (Thread t : concurrentThreads) {
            t.join();
        }
        long concurrentTime = System.nanoTime() - start;
        
        System.out.println("Hashtable time: " + hashtableTime + "ns");
        System.out.println("ConcurrentHashMap time: " + concurrentTime + "ns");
        System.out.println("ConcurrentHashMap is " + (hashtableTime / concurrentTime) + 
                         "x faster");
    }
    
    public static void demonstrateConcurrentPerformance() throws InterruptedException {
        System.out.println("\n=== Concurrent Performance ===");
        
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        
        // Pre-populate
        for (int i = 0; i < 10000; i++) {
            map.put(i, i);
        }
        
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        
        // Concurrent reads (no locking needed)
        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    map.get(j % 10000);
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        long readTime = System.nanoTime() - start;
        
        // Concurrent writes (fine-grained locking)
        CountDownLatch writeLatch = new CountDownLatch(threads);
        start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    map.put(threadId * 1000 + j, j);
                }
                writeLatch.countDown();
            }).start();
        }
        writeLatch.await();
        long writeTime = System.nanoTime() - start;
        
        System.out.println("Concurrent reads: " + readTime + "ns");
        System.out.println("Concurrent writes: " + writeTime + "ns");
        System.out.println("Read operations are lock-free (volatile reads)");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **get()**: O(1) average, lock-free
- **put()**: O(1) average, locks only specific bucket
- **putIfAbsent/compute/merge()**: O(1) average, atomic operations

**Space Complexity**: O(n) where n is number of entries

**Concurrency**:
- Java 8+: CAS for updates, synchronized for bucket-level locking
- Allows concurrent reads without blocking
- Multiple threads can write to different buckets simultaneously

## Edge Cases and Pitfalls

- **Null Not Allowed**: Neither keys nor values can be null (throws NullPointerException)
- **Weakly Consistent Iterators**: Iterators may not reflect recent modifications but never throw ConcurrentModificationException
- **Size/isEmpty**: May not reflect exact state in concurrent environment
- **Atomic Operations**: Use putIfAbsent(), compute(), merge() for thread-safe compound operations
- **Bulk Operations**: forEach(), search(), reduce() are designed for concurrent access
- **vs Hashtable**: ConcurrentHashMap is faster and more scalable
- **vs synchronizedMap**: ConcurrentHashMap has better concurrency, no full-map locking
- **Memory Consistency**: All actions before putting into ConcurrentHashMap happen-before retrieval

## Interview-Ready Answer

"ConcurrentHashMap achieves thread safety through fine-grained locking. In Java 8+, it uses CAS operations and synchronized buckets instead of segment-based locking. Read operations are lock-free using volatile reads, while writes lock only the specific bucket being modified. This allows high concurrency - multiple threads can read and write simultaneously to different buckets. It provides atomic operations like putIfAbsent(), compute(), and merge(). Unlike Hashtable which locks the entire map, ConcurrentHashMap offers much better scalability and performance in concurrent scenarios."
