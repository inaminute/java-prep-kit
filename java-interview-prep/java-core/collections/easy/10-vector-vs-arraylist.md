# Vector vs ArrayList

## Problem Statement

Compare Vector and ArrayList in Java, explaining their differences in thread safety, performance, and growth strategy. Demonstrate when to use each and why ArrayList is generally preferred in modern Java.

**Requirements**:
- Explain synchronization differences
- Compare growth strategies
- Demonstrate performance implications
- Show legacy vs modern alternatives

## Approach

- Vector is synchronized (thread-safe), ArrayList is not
- Vector grows by 100% (doubles), ArrayList grows by 50%
- Vector is legacy (Java 1.0), ArrayList is modern (Java 1.2)
- Synchronization overhead makes Vector slower
- For thread safety, prefer CopyOnWriteArrayList or Collections.synchronizedList()
- ArrayList is preferred for single-threaded scenarios

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class VectorVsArrayList {
    
    public static void main(String[] args) {
        demonstrateBasicDifferences();
        demonstrateGrowthStrategy();
        comparePerformance();
        demonstrateThreadSafety();
        showModernAlternatives();
    }
    
    public static void demonstrateBasicDifferences() {
        System.out.println("=== Basic Differences ===");
        
        // Vector - synchronized
        Vector<String> vector = new Vector<>();
        vector.add("A");
        vector.add("B");
        vector.addElement("C"); // Legacy method
        
        System.out.println("Vector: " + vector);
        System.out.println("First element: " + vector.firstElement());
        System.out.println("Last element: " + vector.lastElement());
        
        // ArrayList - not synchronized
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("A");
        arrayList.add("B");
        arrayList.add("C");
        
        System.out.println("ArrayList: " + arrayList);
        System.out.println("First element: " + arrayList.get(0));
        System.out.println("Last element: " + arrayList.get(arrayList.size() - 1));
    }
    
    public static void demonstrateGrowthStrategy() {
        System.out.println("\n=== Growth Strategy ===");
        
        // Vector doubles capacity
        Vector<Integer> vector = new Vector<>(2);
        System.out.println("Vector initial capacity: 2");
        for (int i = 0; i < 5; i++) {
            vector.add(i);
            System.out.println("Size: " + vector.size() + 
                             ", Capacity: " + vector.capacity());
        }
        
        // ArrayList grows by 50%
        ArrayList<Integer> arrayList = new ArrayList<>(2);
        System.out.println("\nArrayList initial capacity: 2");
        for (int i = 0; i < 5; i++) {
            arrayList.add(i);
            // Capacity is private, but we can infer from behavior
            System.out.println("Size: " + arrayList.size());
        }
        
        System.out.println("\nVector growth: 2 -> 4 -> 8 -> 16 (100%)");
        System.out.println("ArrayList growth: 2 -> 3 -> 4 -> 6 -> 9 (50%)");
    }
    
    public static void comparePerformance() {
        System.out.println("\n=== Performance Comparison ===");
        
        int size = 1000000;
        
        // Vector performance (synchronized overhead)
        Vector<Integer> vector = new Vector<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            vector.add(i);
        }
        long vectorAddTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            int val = vector.get(i);
        }
        long vectorGetTime = System.nanoTime() - start;
        
        // ArrayList performance (no synchronization)
        ArrayList<Integer> arrayList = new ArrayList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long arrayListAddTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            int val = arrayList.get(i);
        }
        long arrayListGetTime = System.nanoTime() - start;
        
        System.out.println("Vector add: " + vectorAddTime + "ns");
        System.out.println("ArrayList add: " + arrayListAddTime + "ns");
        System.out.println("ArrayList is " + (vectorAddTime / arrayListAddTime) + 
                         "x faster for add");
        
        System.out.println("\nVector get: " + vectorGetTime + "ns");
        System.out.println("ArrayList get: " + arrayListGetTime + "ns");
        System.out.println("ArrayList is " + (vectorGetTime / arrayListGetTime) + 
                         "x faster for get");
    }
    
    public static void demonstrateThreadSafety() {
        System.out.println("\n=== Thread Safety ===");
        
        // Vector is thread-safe
        Vector<Integer> vector = new Vector<>();
        
        // ArrayList is not thread-safe
        ArrayList<Integer> arrayList = new ArrayList<>();
        
        // Concurrent modification with ArrayList (unsafe)
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                arrayList.add(i);
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 1000; i < 2000; i++) {
                arrayList.add(i);
            }
        });
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("ArrayList size (may be incorrect): " + arrayList.size());
        System.out.println("Expected: 2000, but may be less due to race conditions");
        
        // Vector is thread-safe (but still not recommended)
        vector.clear();
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                vector.add(i);
            }
        });
        
        Thread t4 = new Thread(() -> {
            for (int i = 1000; i < 2000; i++) {
                vector.add(i);
            }
        });
        
        t3.start();
        t4.start();
        
        try {
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Vector size (correct): " + vector.size());
    }
    
    public static void showModernAlternatives() {
        System.out.println("\n=== Modern Alternatives ===");
        
        // For single-threaded: ArrayList
        List<Integer> singleThreaded = new ArrayList<>();
        System.out.println("Single-threaded: Use ArrayList");
        
        // For multi-threaded: CopyOnWriteArrayList
        List<Integer> concurrent = new CopyOnWriteArrayList<>();
        System.out.println("Multi-threaded (read-heavy): Use CopyOnWriteArrayList");
        
        // For multi-threaded: Collections.synchronizedList
        List<Integer> synchronized = Collections.synchronizedList(new ArrayList<>());
        System.out.println("Multi-threaded (general): Use Collections.synchronizedList");
        
        System.out.println("\nRecommendation: Avoid Vector in new code");
        System.out.println("- Single-threaded: ArrayList");
        System.out.println("- Multi-threaded read-heavy: CopyOnWriteArrayList");
        System.out.println("- Multi-threaded general: Collections.synchronizedList");
        System.out.println("- High concurrency: Consider ConcurrentLinkedQueue");
    }
}
```

## Complexity Analysis

**Time Complexity** (both Vector and ArrayList):
- **add(E e)**: O(1) amortized
- **get(int index)**: O(1)
- **remove(int index)**: O(n)
- **contains(Object o)**: O(n)

**Synchronization Overhead**:
- Vector: Every method is synchronized, adding overhead even in single-threaded scenarios
- ArrayList: No synchronization overhead

**Space Complexity**:
- Vector: More memory waste due to 100% growth
- ArrayList: More memory efficient with 50% growth

## Edge Cases and Pitfalls

- **Synchronization Overhead**: Vector's synchronization adds 20-30% overhead even in single-threaded code
- **Growth Waste**: Vector's 100% growth wastes more memory than ArrayList's 50% growth
- **Legacy Methods**: Vector has legacy methods like addElement(), elementAt() from Enumeration era
- **Iteration Not Synchronized**: Even Vector requires manual synchronization for iteration
- **False Thread Safety**: Vector's method-level synchronization doesn't guarantee compound operations are atomic
- **When to Use Vector**: Almost never in modern Java; legacy code compatibility only
- **When to Use ArrayList**: Default choice for single-threaded list operations
- **Modern Thread Safety**: Use CopyOnWriteArrayList or Collections.synchronizedList() instead of Vector

## Interview-Ready Answer

"Vector and ArrayList are both resizable array implementations, but Vector is synchronized (thread-safe) while ArrayList is not. Vector is a legacy class from Java 1.0 with synchronization overhead that makes it 20-30% slower. Vector grows by 100% when full, while ArrayList grows by 50%, making ArrayList more memory efficient. In modern Java, use ArrayList for single-threaded scenarios and CopyOnWriteArrayList or Collections.synchronizedList() for thread safety. Vector should only be used for legacy code compatibility."
