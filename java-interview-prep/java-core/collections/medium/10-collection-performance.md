# Collection Performance Comparison

## Problem Statement

Provide a comprehensive comparison of time and space complexity across different collection implementations. Demonstrate performance characteristics for common operations and guide selection of appropriate collections for specific use cases.

**Requirements**:
- Compare time complexity for major operations
- Analyze space complexity and memory overhead
- Provide performance benchmarks
- Give decision guidelines for collection selection

## Approach

- List implementations: ArrayList, LinkedList, CopyOnWriteArrayList
- Set implementations: HashSet, TreeSet, LinkedHashSet, EnumSet
- Map implementations: HashMap, TreeMap, LinkedHashMap, ConcurrentHashMap
- Queue/Deque: ArrayDeque, LinkedList, PriorityQueue
- Consider access patterns, thread safety, ordering requirements

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

public class CollectionPerformance {
    
    public static void main(String[] args) {
        compareListPerformance();
        compareSetPerformance();
        compareMapPerformance();
        printComplexityTable();
    }
    
    public static void compareListPerformance() {
        System.out.println("=== List Performance Comparison ===");
        
        int size = 100000;
        
        // ArrayList
        List<Integer> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long arrayListAdd = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            arrayList.get(i * 10);
        }
        long arrayListGet = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            arrayList.add(0, i);
        }
        long arrayListInsert = System.nanoTime() - start;
        
        // LinkedList
        List<Integer> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedList.add(i);
        }
        long linkedListAdd = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            linkedList.get(i * 10);
        }
        long linkedListGet = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            ((LinkedList<Integer>)linkedList).addFirst(i);
        }
        long linkedListInsert = System.nanoTime() - start;
        
        System.out.println("Add at end:");
        System.out.println("  ArrayList: " + arrayListAdd + "ns");
        System.out.println("  LinkedList: " + linkedListAdd + "ns");
        
        System.out.println("\nRandom access (get):");
        System.out.println("  ArrayList: " + arrayListGet + "ns");
        System.out.println("  LinkedList: " + linkedListGet + "ns");
        System.out.println("  ArrayList is " + (linkedListGet / arrayListGet) + "x faster");
        
        System.out.println("\nInsert at beginning:");
        System.out.println("  ArrayList: " + arrayListInsert + "ns");
        System.out.println("  LinkedList: " + linkedListInsert + "ns");
        System.out.println("  LinkedList is " + (arrayListInsert / linkedListInsert) + "x faster");
    }
    
    public static void compareSetPerformance() {
        System.out.println("\n=== Set Performance Comparison ===");
        
        int size = 100000;
        
        // HashSet
        Set<Integer> hashSet = new HashSet<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashSet.add(i);
        }
        long hashSetAdd = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashSet.contains(i);
        }
        long hashSetContains = System.nanoTime() - start;
        
        // TreeSet
        Set<Integer> treeSet = new TreeSet<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeSet.add(i);
        }
        long treeSetAdd = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeSet.contains(i);
        }
        long treeSetContains = System.nanoTime() - start;
        
        System.out.println("Add:");
        System.out.println("  HashSet: " + hashSetAdd + "ns (O(1))");
        System.out.println("  TreeSet: " + treeSetAdd + "ns (O(log n))");
        
        System.out.println("\nContains:");
        System.out.println("  HashSet: " + hashSetContains + "ns (O(1))");
        System.out.println("  TreeSet: " + treeSetContains + "ns (O(log n))");
        System.out.println("  HashSet is " + (treeSetAdd / hashSetAdd) + "x faster");
    }
    
    public static void compareMapPerformance() {
        System.out.println("\n=== Map Performance Comparison ===");
        
        int size = 100000;
        
        // HashMap
        Map<Integer, String> hashMap = new HashMap<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashMap.put(i, "Value" + i);
        }
        long hashMapPut = System.nanoTime() - start;
        
        // TreeMap
        Map<Integer, String> treeMap = new TreeMap<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeMap.put(i, "Value" + i);
        }
        long treeMapPut = System.nanoTime() - start;
        
        // LinkedHashMap
        Map<Integer, String> linkedHashMap = new LinkedHashMap<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedHashMap.put(i, "Value" + i);
        }
        long linkedHashMapPut = System.nanoTime() - start;
        
        System.out.println("Put:");
        System.out.println("  HashMap: " + hashMapPut + "ns (O(1))");
        System.out.println("  TreeMap: " + treeMapPut + "ns (O(log n))");
        System.out.println("  LinkedHashMap: " + linkedHashMapPut + "ns (O(1) + overhead)");
    }
    
    public static void printComplexityTable() {
        System.out.println("\n=== Time Complexity Summary ===");
        System.out.println("\nList Implementations:");
        System.out.println("Operation       ArrayList  LinkedList  CopyOnWrite");
        System.out.println("add(end)        O(1)*      O(1)        O(n)");
        System.out.println("add(index)      O(n)       O(n)        O(n)");
        System.out.println("get(index)      O(1)       O(n)        O(1)");
        System.out.println("remove(index)   O(n)       O(n)        O(n)");
        System.out.println("contains        O(n)       O(n)        O(n)");
        
        System.out.println("\nSet Implementations:");
        System.out.println("Operation       HashSet    TreeSet     LinkedHashSet");
        System.out.println("add             O(1)       O(log n)    O(1)");
        System.out.println("remove          O(1)       O(log n)    O(1)");
        System.out.println("contains        O(1)       O(log n)    O(1)");
        
        System.out.println("\nMap Implementations:");
        System.out.println("Operation       HashMap    TreeMap     LinkedHashMap");
        System.out.println("put             O(1)       O(log n)    O(1)");
        System.out.println("get             O(1)       O(log n)    O(1)");
        System.out.println("remove          O(1)       O(log n)    O(1)");
        
        System.out.println("\n* Amortized time complexity");
        
        System.out.println("\n=== Selection Guidelines ===");
        System.out.println("ArrayList: Default list choice, random access");
        System.out.println("LinkedList: Frequent insertions at beginning");
        System.out.println("HashSet: Fast operations, no order needed");
        System.out.println("TreeSet: Need sorted order");
        System.out.println("LinkedHashSet: Need insertion order");
        System.out.println("HashMap: Default map choice");
        System.out.println("TreeMap: Need sorted keys or range queries");
        System.out.println("LinkedHashMap: Need insertion/access order");
        System.out.println("ConcurrentHashMap: Thread-safe map");
        System.out.println("ArrayDeque: Stack/queue operations");
    }
}
```

## Complexity Analysis

See comprehensive table in solution above.

## Edge Cases and Pitfalls

- **Amortized vs Worst Case**: ArrayList add is O(1) amortized but O(n) when resizing
- **Cache Locality**: ArrayList faster than LinkedList in practice due to better cache performance
- **Memory Overhead**: LinkedList has higher per-element overhead (3 references vs 1)
- **Thread Safety**: Most collections not thread-safe; use concurrent variants
- **Null Handling**: Some collections allow nulls, others don't

## Interview-Ready Answer

"ArrayList provides O(1) random access and is the default list choice. LinkedList is better for frequent insertions at the beginning. HashSet/HashMap provide O(1) operations without ordering. TreeSet/TreeMap provide O(log n) operations with sorted order. LinkedHashSet/LinkedHashMap add insertion order with minimal overhead. For thread safety, use ConcurrentHashMap or CopyOnWriteArrayList. Choose based on access patterns: random access → ArrayList, sorted order → TreeSet/TreeMap, fast operations → HashSet/HashMap."
