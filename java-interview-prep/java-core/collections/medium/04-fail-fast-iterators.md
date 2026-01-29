# Fail-Fast vs Fail-Safe Iterators

## Problem Statement

Explain the difference between fail-fast and fail-safe iterators in Java Collections. Demonstrate when ConcurrentModificationException occurs and how fail-safe collections like CopyOnWriteArrayList handle concurrent modifications.

**Requirements**:
- Explain fail-fast behavior and ConcurrentModificationException
- Demonstrate fail-safe iterators
- Compare CopyOnWriteArrayList with ArrayList
- Show use cases for each approach

## Approach

- Fail-fast: Throws ConcurrentModificationException if collection modified during iteration
- Fail-safe: Works on copy of collection, doesn't throw exception
- Most collections (ArrayList, HashMap) are fail-fast
- Concurrent collections (CopyOnWriteArrayList, ConcurrentHashMap) are fail-safe
- Fail-fast uses modCount to detect modifications
- Fail-safe trades memory for safety

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class FailFastVsFailSafe {
    
    public static void main(String[] args) {
        demonstrateFailFast();
        demonstrateFailSafe();
        demonstrateCopyOnWrite();
        comparePerformance();
    }
    
    public static void demonstrateFailFast() {
        System.out.println("=== Fail-Fast Iterators ===");
        
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // This will throw ConcurrentModificationException
        try {
            for (String item : list) {
                System.out.println("Processing: " + item);
                if (item.equals("B")) {
                    list.remove(item); // Modification during iteration
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Caught ConcurrentModificationException");
        }
        
        // Correct way: use Iterator.remove()
        list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.equals("B")) {
                iterator.remove(); // Safe removal
            }
        }
        System.out.println("After safe removal: " + list);
        
        // HashMap is also fail-fast
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        
        try {
            for (String key : map.keySet()) {
                if (key.equals("B")) {
                    map.remove(key); // Will throw exception
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("HashMap is also fail-fast");
        }
    }
    
    public static void demonstrateFailSafe() {
        System.out.println("\n=== Fail-Safe Iterators ===");
        
        // CopyOnWriteArrayList is fail-safe
        List<String> list = new CopyOnWriteArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // Can modify during iteration (no exception)
        for (String item : list) {
            System.out.println("Processing: " + item);
            if (item.equals("B")) {
                list.remove(item); // Safe, but won't affect current iteration
            }
        }
        
        System.out.println("After modification: " + list);
        
        // Iterator sees snapshot
        list = new CopyOnWriteArrayList<>(Arrays.asList("A", "B", "C"));
        Iterator<String> iterator = list.iterator();
        
        list.add("D"); // Modify after creating iterator
        
        System.out.println("\nIterator sees snapshot:");
        while (iterator.hasNext()) {
            System.out.println(iterator.next()); // Won't see "D"
        }
        
        System.out.println("Actual list: " + list); // Contains "D"
    }
    
    public static void demonstrateCopyOnWrite() {
        System.out.println("\n=== CopyOnWriteArrayList Details ===");
        
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        
        // Add elements
        for (int i = 0; i < 5; i++) {
            list.add(i);
        }
        
        System.out.println("Original: " + list);
        
        // Multiple iterators can coexist
        Iterator<Integer> iter1 = list.iterator();
        Iterator<Integer> iter2 = list.iterator();
        
        // Modify list
        list.add(5);
        list.add(6);
        
        System.out.println("After adding 5 and 6:");
        System.out.println("iter1 (old snapshot): ");
        iter1.forEachRemaining(System.out::print); // 0,1,2,3,4
        
        System.out.println("\niter2 (old snapshot): ");
        iter2.forEachRemaining(System.out::print); // 0,1,2,3,4
        
        System.out.println("\nNew iterator (current state): ");
        list.iterator().forEachRemaining(System.out::print); // 0,1,2,3,4,5,6
        
        System.out.println("\n\nCopyOnWrite: Each modification creates new array copy");
    }
    
    public static void comparePerformance() {
        System.out.println("\n=== Performance Comparison ===");
        
        int size = 10000;
        
        // ArrayList (fail-fast) - write performance
        List<Integer> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long arrayListWriteTime = System.nanoTime() - start;
        
        // CopyOnWriteArrayList (fail-safe) - write performance
        List<Integer> cowList = new CopyOnWriteArrayList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            cowList.add(i);
        }
        long cowWriteTime = System.nanoTime() - start;
        
        // Read performance
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.get(i);
        }
        long arrayListReadTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            cowList.get(i);
        }
        long cowReadTime = System.nanoTime() - start;
        
        System.out.println("ArrayList write: " + arrayListWriteTime + "ns");
        System.out.println("CopyOnWrite write: " + cowWriteTime + "ns");
        System.out.println("CopyOnWrite is " + (cowWriteTime / arrayListWriteTime) + 
                         "x slower for writes");
        
        System.out.println("\nArrayList read: " + arrayListReadTime + "ns");
        System.out.println("CopyOnWrite read: " + cowReadTime + "ns");
        
        System.out.println("\nUse CopyOnWrite for read-heavy scenarios");
    }
}
```

## Complexity Analysis

**Fail-Fast (ArrayList)**:
- **Iteration**: O(n)
- **Modification detection**: O(1) via modCount

**Fail-Safe (CopyOnWriteArrayList)**:
- **Iteration**: O(n)
- **Write operations**: O(n) due to array copying
- **Read operations**: O(1)

## Edge Cases and Pitfalls

- **Fail-Fast Not Guaranteed**: ConcurrentModificationException is best-effort, not guaranteed
- **Iterator.remove() Only**: Only safe way to remove during iteration in fail-fast collections
- **CopyOnWrite Memory**: Each write creates new array copy; expensive for write-heavy scenarios
- **Snapshot Semantics**: Fail-safe iterators see snapshot; won't reflect concurrent changes
- **When to Use Fail-Fast**: Single-threaded or externally synchronized scenarios
- **When to Use Fail-Safe**: Concurrent reads with occasional writes
- **ConcurrentHashMap**: Weakly consistent iterators (between fail-fast and fail-safe)

## Interview-Ready Answer

"Fail-fast iterators throw ConcurrentModificationException if the collection is modified during iteration, using a modCount to detect changes. Most collections like ArrayList and HashMap are fail-fast. Fail-safe iterators work on a copy of the collection and never throw exceptions. CopyOnWriteArrayList is fail-safe - it creates a new array copy on each write, making writes expensive but reads lock-free. Use fail-fast for single-threaded scenarios and fail-safe for concurrent read-heavy scenarios."
