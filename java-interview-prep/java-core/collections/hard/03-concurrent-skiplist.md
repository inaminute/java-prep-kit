# ConcurrentSkipListMap and ConcurrentSkipListSet

## Problem Statement

Explain the skip list data structure used by ConcurrentSkipListMap and ConcurrentSkipListSet. Demonstrate their lock-free concurrent sorted collection capabilities and compare with TreeMap and ConcurrentHashMap.

**Requirements**:
- Explain skip list structure
- Demonstrate concurrent sorted operations
- Show NavigableMap operations
- Compare performance with TreeMap

## Approach

- Skip list: probabilistic data structure with multiple levels
- Lock-free concurrent operations using CAS
- Maintains sorted order like TreeMap
- O(log n) operations with high probability
- Better concurrency than synchronized TreeMap
- Implements NavigableMap/NavigableSet

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentSkipList {
    
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicOperations();
        demonstrateConcurrentAccess();
        compareWithTreeMap();
    }
    
    public static void demonstrateBasicOperations() {
        System.out.println("=== ConcurrentSkipListMap Operations ===");
        
        ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<>();
        
        map.put(5, "Five");
        map.put(2, "Two");
        map.put(8, "Eight");
        map.put(1, "One");
        
        System.out.println("Sorted map: " + map);
        
        // NavigableMap operations
        System.out.println("lowerEntry(5): " + map.lowerEntry(5));
        System.out.println("floorEntry(5): " + map.floorEntry(5));
        System.out.println("ceilingEntry(5): " + map.ceilingEntry(5));
        System.out.println("higherEntry(5): " + map.higherEntry(5));
        
        // Range views
        System.out.println("subMap(2, 8): " + map.subMap(2, 8));
        
        // ConcurrentSkipListSet
        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();
        set.addAll(Arrays.asList(5, 2, 8, 1, 9));
        System.out.println("\nSorted set: " + set);
    }
    
    public static void demonstrateConcurrentAccess() throws InterruptedException {
        System.out.println("\n=== Concurrent Access ===");
        
        ConcurrentSkipListMap<Integer, Integer> map = new ConcurrentSkipListMap<>();
        
        // Concurrent writes
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    map.put(threadId * 1000 + j, j);
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) t.join();
        
        System.out.println("Map size: " + map.size());
        System.out.println("First key: " + map.firstKey());
        System.out.println("Last key: " + map.lastKey());
        System.out.println("Lock-free concurrent sorted collection");
    }
    
    public static void compareWithTreeMap() throws InterruptedException {
        System.out.println("\n=== Performance Comparison ===");
        
        int size = 100000;
        
        // ConcurrentSkipListMap
        ConcurrentSkipListMap<Integer, String> skipMap = new ConcurrentSkipListMap<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            skipMap.put(i, "Value" + i);
        }
        long skipTime = System.nanoTime() - start;
        
        // TreeMap (synchronized)
        Map<Integer, String> treeMap = Collections.synchronizedMap(new TreeMap<>());
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeMap.put(i, "Value" + i);
        }
        long treeTime = System.nanoTime() - start;
        
        System.out.println("ConcurrentSkipListMap: " + skipTime + "ns");
        System.out.println("Synchronized TreeMap: " + treeTime + "ns");
        
        System.out.println("\nConcurrentSkipListMap advantages:");
        System.out.println("- Lock-free operations");
        System.out.println("- Better scalability");
        System.out.println("- Sorted order maintained");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(log n) average for put/get/remove

**Space Complexity**: O(n) with overhead for skip list levels

## Edge Cases and Pitfalls

- **Null Not Allowed**: Neither keys nor values can be null
- **Probabilistic**: Uses randomization for level assignment
- **Memory Overhead**: Higher than TreeMap due to multiple levels
- **When to Use**: Concurrent sorted collections
- **vs TreeMap**: Better concurrency, similar performance
- **vs ConcurrentHashMap**: Maintains sorted order, slightly slower

## Interview-Ready Answer

"ConcurrentSkipListMap uses a skip list data structure - a probabilistic multi-level linked list providing O(log n) operations. It achieves lock-free concurrency using CAS operations, making it ideal for concurrent sorted collections. Unlike synchronized TreeMap which locks the entire tree, ConcurrentSkipListMap allows multiple concurrent operations. It implements NavigableMap with all range query operations while maintaining thread safety. Use it when you need a concurrent sorted map; use ConcurrentHashMap when order doesn't matter."
