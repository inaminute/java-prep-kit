# Thread-Safe Collection Strategies

## Problem Statement

Compare different strategies for achieving thread safety in collections. Analyze synchronized wrappers, concurrent collections, and immutable collections. Provide guidelines for choosing the right approach based on use case.

**Requirements**:
- Compare synchronization strategies
- Analyze concurrent collection types
- Discuss immutable collections
- Provide selection guidelines

## Approach

- Synchronized wrappers: Collections.synchronizedXxx()
- Concurrent collections: ConcurrentHashMap, CopyOnWriteArrayList
- Lock-free: ConcurrentLinkedQueue, ConcurrentSkipListMap
- Immutable: Collections.unmodifiableXxx(), List.of(), Set.of()
- Choose based on read/write ratio, contention level, ordering needs

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class ThreadSafeStrategies {
    
    public static void main(String[] args) throws InterruptedException {
        compareSynchronizedWrapper();
        compareConcurrentCollections();
        demonstrateImmutableCollections();
        provideGuidelines();
    }
    
    public static void compareSynchronizedWrapper() throws InterruptedException {
        System.out.println("=== Synchronized Wrapper ===");
        
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
        
        // Pros: Simple, works with any collection
        // Cons: Coarse-grained locking, iteration needs manual sync
        
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    syncList.add(threadId * 1000 + j);
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) t.join();
        
        System.out.println("Size: " + syncList.size());
        System.out.println("Synchronized wrapper: Simple but limited concurrency");
    }
    
    public static void compareConcurrentCollections() throws InterruptedException {
        System.out.println("\n=== Concurrent Collections ===");
        
        // ConcurrentHashMap: Fine-grained locking
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        
        Thread[] mapThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            mapThreads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    concurrentMap.put(threadId * 1000 + j, j);
                }
            });
            mapThreads[i].start();
        }
        
        for (Thread t : mapThreads) t.join();
        
        System.out.println("ConcurrentHashMap size: " + concurrentMap.size());
        System.out.println("Fine-grained locking, better scalability");
        
        // CopyOnWriteArrayList: Read-heavy scenarios
        CopyOnWriteArrayList<Integer> cowList = new CopyOnWriteArrayList<>();
        System.out.println("\nCopyOnWriteArrayList: Ideal for read-heavy workloads");
        
        // ConcurrentLinkedQueue: Lock-free
        ConcurrentLinkedQueue<Integer> lockFreeQueue = new ConcurrentLinkedQueue<>();
        System.out.println("ConcurrentLinkedQueue: Lock-free, high throughput");
    }
    
    public static void demonstrateImmutableCollections() {
        System.out.println("\n=== Immutable Collections ===");
        
        // Java 9+ factory methods
        List<String> immutableList = List.of("A", "B", "C");
        Set<String> immutableSet = Set.of("X", "Y", "Z");
        Map<String, Integer> immutableMap = Map.of("A", 1, "B", 2);
        
        System.out.println("Immutable list: " + immutableList);
        
        try {
            immutableList.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("Truly immutable - cannot modify");
        }
        
        System.out.println("Immutable collections: Thread-safe by nature");
    }
    
    public static void provideGuidelines() {
        System.out.println("\n=== Selection Guidelines ===");
        
        System.out.println("\n1. Synchronized Wrapper:");
        System.out.println("   - Simple thread-safety needs");
        System.out.println("   - Low contention");
        System.out.println("   - Legacy code compatibility");
        
        System.out.println("\n2. ConcurrentHashMap:");
        System.out.println("   - High contention map operations");
        System.out.println("   - Frequent reads and writes");
        System.out.println("   - Need atomic operations (putIfAbsent, compute)");
        
        System.out.println("\n3. CopyOnWriteArrayList:");
        System.out.println("   - Read-heavy workloads (90%+ reads)");
        System.out.println("   - Small to medium size");
        System.out.println("   - Infrequent modifications");
        
        System.out.println("\n4. ConcurrentLinkedQueue:");
        System.out.println("   - Producer-consumer patterns");
        System.out.println("   - High throughput needed");
        System.out.println("   - Lock-free operations");
        
        System.out.println("\n5. BlockingQueue:");
        System.out.println("   - Producer-consumer with blocking");
        System.out.println("   - Bounded buffer scenarios");
        System.out.println("   - Thread coordination");
        
        System.out.println("\n6. Immutable Collections:");
        System.out.println("   - Data never changes");
        System.out.println("   - Share across threads safely");
        System.out.println("   - Functional programming style");
    }
}
```

## Complexity Analysis

Varies by implementation - see individual collection documentation.

## Edge Cases and Pitfalls

- **Synchronized Wrapper**: Iteration needs manual synchronization
- **ConcurrentHashMap**: Weakly consistent iterators
- **CopyOnWrite**: Expensive writes, snapshot iterators
- **Immutable**: Cannot be modified after creation
- **Choose Wisely**: Consider read/write ratio, contention, memory

## Interview-Ready Answer

"Thread-safe collection strategies: (1) Synchronized wrappers - simple but coarse-grained locking; (2) ConcurrentHashMap - fine-grained locking for high concurrency; (3) CopyOnWriteArrayList - copy-on-write for read-heavy scenarios; (4) ConcurrentLinkedQueue - lock-free for high throughput; (5) BlockingQueue - producer-consumer with blocking; (6) Immutable collections - inherently thread-safe. Choose based on read/write ratio, contention level, and whether you need blocking, ordering, or atomic operations."
