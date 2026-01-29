# TreeMap and NavigableMap Operations

## Problem Statement

Explain TreeMap's Red-Black tree implementation and demonstrate NavigableMap operations. Show how to use floor, ceiling, higher, lower methods, and how to create range views with subMap, headMap, and tailMap.

**Requirements**:
- Explain Red-Black tree structure
- Demonstrate NavigableMap methods
- Show range view operations
- Compare performance with HashMap

## Approach

- TreeMap is backed by a Red-Black tree (self-balancing BST)
- Maintains keys in sorted order
- NavigableMap provides navigation methods for finding closest matches
- Supports range views that are backed by the original map
- O(log n) operations vs HashMap's O(1)
- Useful when sorted order or range queries are needed

## Solution

```java
import java.util.*;

public class TreeMapNavigable {
    
    public static void main(String[] args) {
        demonstrateBasicOperations();
        demonstrateNavigationMethods();
        demonstrateRangeViews();
        demonstrateDescendingOperations();
        compareWithHashMap();
    }
    
    public static void demonstrateBasicOperations() {
        System.out.println("=== Basic TreeMap Operations ===");
        
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(5, "Five");
        map.put(2, "Two");
        map.put(8, "Eight");
        map.put(1, "One");
        map.put(9, "Nine");
        
        System.out.println("TreeMap (sorted): " + map);
        System.out.println("First key: " + map.firstKey());
        System.out.println("Last key: " + map.lastKey());
        System.out.println("First entry: " + map.firstEntry());
        System.out.println("Last entry: " + map.lastEntry());
    }
    
    public static void demonstrateNavigationMethods() {
        System.out.println("\n=== Navigation Methods ===");
        
        NavigableMap<Integer, String> map = new TreeMap<>();
        map.put(10, "Ten");
        map.put(20, "Twenty");
        map.put(30, "Thirty");
        map.put(40, "Forty");
        map.put(50, "Fifty");
        
        System.out.println("Map: " + map);
        
        // Find closest matches
        System.out.println("\nFor key 25:");
        System.out.println("lowerEntry (< 25): " + map.lowerEntry(25));     // 20
        System.out.println("floorEntry (<= 25): " + map.floorEntry(25));    // 20
        System.out.println("ceilingEntry (>= 25): " + map.ceilingEntry(25));// 30
        System.out.println("higherEntry (> 25): " + map.higherEntry(25));   // 30
        
        System.out.println("\nFor key 30 (exists):");
        System.out.println("lowerEntry (< 30): " + map.lowerEntry(30));     // 20
        System.out.println("floorEntry (<= 30): " + map.floorEntry(30));    // 30
        System.out.println("ceilingEntry (>= 30): " + map.ceilingEntry(30));// 30
        System.out.println("higherEntry (> 30): " + map.higherEntry(30));   // 40
        
        // Poll operations (remove and return)
        System.out.println("\npollFirstEntry: " + map.pollFirstEntry());
        System.out.println("pollLastEntry: " + map.pollLastEntry());
        System.out.println("After polls: " + map);
    }
    
    public static void demonstrateRangeViews() {
        System.out.println("\n=== Range Views ===");
        
        NavigableMap<Integer, String> map = new TreeMap<>();
        for (int i = 10; i <= 100; i += 10) {
            map.put(i, "Value" + i);
        }
        
        System.out.println("Full map: " + map);
        
        // headMap - keys < toKey
        SortedMap<Integer, String> headMap = map.headMap(50);
        System.out.println("headMap(50): " + headMap);
        
        // tailMap - keys >= fromKey
        SortedMap<Integer, String> tailMap = map.tailMap(50);
        System.out.println("tailMap(50): " + tailMap);
        
        // subMap - fromKey <= keys < toKey
        SortedMap<Integer, String> subMap = map.subMap(30, 70);
        System.out.println("subMap(30, 70): " + subMap);
        
        // Inclusive/exclusive variants
        NavigableMap<Integer, String> subMapInclusive = 
            map.subMap(30, true, 70, true);
        System.out.println("subMap(30, true, 70, true): " + subMapInclusive);
        
        // Views are backed by original map
        map.put(45, "NewValue");
        System.out.println("After adding 45 to original:");
        System.out.println("subMap now: " + subMap);
        
        // Modifications to view affect original
        subMap.remove(40);
        System.out.println("After removing 40 from subMap:");
        System.out.println("Original map: " + map);
    }
    
    public static void demonstrateDescendingOperations() {
        System.out.println("\n=== Descending Operations ===");
        
        NavigableMap<Integer, String> map = new TreeMap<>();
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");
        map.put(4, "Four");
        map.put(5, "Five");
        
        System.out.println("Original: " + map);
        
        // Descending map view
        NavigableMap<Integer, String> descendingMap = map.descendingMap();
        System.out.println("Descending: " + descendingMap);
        
        // Descending key set
        NavigableSet<Integer> descendingKeys = map.descendingKeySet();
        System.out.println("Descending keys: " + descendingKeys);
        
        // Navigate in reverse
        System.out.println("\nReverse iteration:");
        for (Map.Entry<Integer, String> entry : descendingMap.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
    
    public static void compareWithHashMap() {
        System.out.println("\n=== TreeMap vs HashMap Performance ===");
        
        int size = 100000;
        
        // TreeMap performance
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeMap.put(i, "Value" + i);
        }
        long treePutTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeMap.get(i);
        }
        long treeGetTime = System.nanoTime() - start;
        
        // HashMap performance
        HashMap<Integer, String> hashMap = new HashMap<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashMap.put(i, "Value" + i);
        }
        long hashPutTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashMap.get(i);
        }
        long hashGetTime = System.nanoTime() - start;
        
        System.out.println("TreeMap put: " + treePutTime + "ns (O(log n))");
        System.out.println("HashMap put: " + hashPutTime + "ns (O(1))");
        System.out.println("TreeMap get: " + treeGetTime + "ns (O(log n))");
        System.out.println("HashMap get: " + hashGetTime + "ns (O(1))");
        
        System.out.println("\nTreeMap advantages: sorted order, range queries");
        System.out.println("HashMap advantages: faster operations");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **put/get/remove**: O(log n)
- **firstKey/lastKey**: O(log n)
- **lowerKey/floorKey/ceilingKey/higherKey**: O(log n)
- **Range views**: O(log n) to create, O(1) for subsequent access within range

**Space Complexity**: O(n) for storing entries in Red-Black tree

## Edge Cases and Pitfalls

- **Null Keys**: Not allowed (throws NullPointerException)
- **Null Values**: Allowed
- **Comparator Required**: Keys must be Comparable or provide Comparator
- **Range Views Are Live**: Changes to views affect original map and vice versa
- **Inclusive/Exclusive**: Be careful with inclusive/exclusive bounds in range methods
- **Performance**: Slower than HashMap but provides sorted order
- **When to Use TreeMap**: Need sorted order, range queries, or NavigableMap operations
- **When to Use HashMap**: Only need key-value mapping, don't care about order

## Interview-Ready Answer

"TreeMap is backed by a Red-Black tree providing O(log n) operations while maintaining keys in sorted order. NavigableMap methods like floor, ceiling, higher, and lower find closest matches efficiently. Range views (headMap, tailMap, subMap) create backed views of portions of the map. These views are live - modifications affect both the view and original map. TreeMap is ideal when you need sorted iteration or range queries, though HashMap is faster for simple key-value operations."
