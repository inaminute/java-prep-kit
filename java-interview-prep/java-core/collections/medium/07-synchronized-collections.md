# Synchronized Collections Wrappers

## Problem Statement

Explain Collections.synchronizedXxx() wrappers and their proper usage. Demonstrate thread-safe operations, manual synchronization requirements for iteration, and compare with concurrent collections.

**Requirements**:
- Demonstrate synchronized wrappers
- Show manual synchronization for iteration
- Compare with ConcurrentHashMap and CopyOnWriteArrayList
- Explain performance implications

## Approach

- Collections.synchronizedList/Map/Set wrap collections with synchronized methods
- All methods are synchronized on the wrapper object
- Iteration requires manual synchronization
- Less efficient than concurrent collections
- Useful for making existing collections thread-safe
- Prefer concurrent collections for better performance

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class SynchronizedCollections {
    
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicUsage();
        demonstrateIterationSynchronization();
        compareWithConcurrent();
    }
    
    public static void demonstrateBasicUsage() {
        System.out.println("=== Synchronized Wrappers ===");
        
        List<String> list = new ArrayList<>();
        List<String> syncList = Collections.synchronizedList(list);
        
        Set<Integer> set = new HashSet<>();
        Set<Integer> syncSet = Collections.synchronizedSet(set);
        
        Map<String, Integer> map = new HashMap<>();
        Map<String, Integer> syncMap = Collections.synchronizedMap(map);
        
        // Thread-safe operations
        syncList.add("A");
        syncList.add("B");
        syncSet.add(1);
        syncSet.add(2);
        syncMap.put("key", 1);
        
        System.out.println("Synchronized list: " + syncList);
        System.out.println("Synchronized set: " + syncSet);
        System.out.println("Synchronized map: " + syncMap);
    }
    
    public static void demonstrateIterationSynchronization() throws InterruptedException {
        System.out.println("\n=== Iteration Synchronization ===");
        
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
        
        // Populate list
        for (int i = 0; i < 100; i++) {
            syncList.add(i);
        }
        
        // WRONG: Iteration without synchronization
        Thread t1 = new Thread(() -> {
            try {
                for (Integer num : syncList) {
                    Thread.sleep(1); // Simulate work
                }
            } catch (Exception e) {
                System.out.println("Exception during unsynchronized iteration: " + 
                                 e.getClass().getSimpleName());
            }
        });
        
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(10);
                syncList.add(100); // Concurrent modification
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        // CORRECT: Manual synchronization for iteration
        Thread t3 = new Thread(() -> {
            synchronized(syncList) {
                for (Integer num : syncList) {
                    // Safe iteration
                }
            }
        });
        
        Thread t4 = new Thread(() -> {
            syncList.add(101); // Will wait for iteration to complete
        });
        
        t3.start();
        t4.start();
        t3.join();
        t4.join();
        
        System.out.println("Manual synchronization prevents ConcurrentModificationException");
    }
    
    public static void compareWithConcurrent() throws InterruptedException {
        System.out.println("\n=== Synchronized vs Concurrent Collections ===");
        
        int threads = 10;
        int operations = 10000;
        
        // Synchronized List
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
        long start = System.nanoTime();
        
        Thread[] syncThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            syncThreads[i] = new Thread(() -> {
                for (int j = 0; j < operations; j++) {
                    syncList.add(threadId * operations + j);
                }
            });
            syncThreads[i].start();
        }
        
        for (Thread t : syncThreads) {
            t.join();
        }
        long syncTime = System.nanoTime() - start;
        
        // CopyOnWriteArrayList
        List<Integer> cowList = new CopyOnWriteArrayList<>();
        start = System.nanoTime();
        
        Thread[] cowThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            cowThreads[i] = new Thread(() -> {
                for (int j = 0; j < operations; j++) {
                    cowList.add(threadId * operations + j);
                }
            });
            cowThreads[i].start();
        }
        
        for (Thread t : cowThreads) {
            t.join();
        }
        long cowTime = System.nanoTime() - start;
        
        System.out.println("Synchronized List: " + syncTime + "ns");
        System.out.println("CopyOnWriteArrayList: " + cowTime + "ns");
        
        // Map comparison
        Map<Integer, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        start = System.nanoTime();
        
        Thread[] syncMapThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            syncMapThreads[i] = new Thread(() -> {
                for (int j = 0; j < operations; j++) {
                    syncMap.put(threadId * operations + j, j);
                }
            });
            syncMapThreads[i].start();
        }
        
        for (Thread t : syncMapThreads) {
            t.join();
        }
        long syncMapTime = System.nanoTime() - start;
        
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
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
        long concurrentMapTime = System.nanoTime() - start;
        
        System.out.println("\nSynchronized Map: " + syncMapTime + "ns");
        System.out.println("ConcurrentHashMap: " + concurrentMapTime + "ns");
        System.out.println("ConcurrentHashMap is " + (syncMapTime / concurrentMapTime) + 
                         "x faster");
    }
}
```

## Complexity Analysis

**Time Complexity**: Same as underlying collection, plus synchronization overhead

**Synchronization Overhead**: 
- Every method call acquires lock
- Reduces concurrency compared to concurrent collections

## Edge Cases and Pitfalls

- **Manual Iteration Sync**: Must synchronize on collection object when iterating
- **Compound Operations**: Multiple operations not atomic; need manual synchronization
- **Performance**: Slower than concurrent collections due to coarse-grained locking
- **Deadlock Risk**: Be careful with nested synchronization
- **When to Use**: Legacy code, simple thread-safety needs
- **Better Alternatives**: ConcurrentHashMap, CopyOnWriteArrayList, ConcurrentLinkedQueue
- **Views**: Returned views (keySet, values, entrySet) also require manual synchronization

## Interview-Ready Answer

"Collections.synchronizedXxx() creates thread-safe wrappers by synchronizing all methods on the wrapper object. However, iteration requires manual synchronization on the collection to prevent ConcurrentModificationException. These wrappers use coarse-grained locking, making them slower than concurrent collections like ConcurrentHashMap which use fine-grained locking. Prefer concurrent collections for better performance; use synchronized wrappers only for legacy code or simple thread-safety needs."
