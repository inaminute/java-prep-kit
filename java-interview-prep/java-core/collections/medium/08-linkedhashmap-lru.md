# LinkedHashMap and LRU Cache

## Problem Statement

Explain LinkedHashMap's dual-linked list structure that maintains insertion or access order. Demonstrate how to implement an LRU (Least Recently Used) cache using LinkedHashMap's access-order mode and removeEldestEntry() hook.

**Requirements**:
- Explain insertion-order vs access-order modes
- Implement LRU cache using LinkedHashMap
- Demonstrate removeEldestEntry() hook
- Compare with HashMap performance

## Approach

- LinkedHashMap extends HashMap with doubly-linked list
- Maintains insertion order by default
- Access-order mode for LRU cache implementation
- removeEldestEntry() hook for automatic eviction
- Slightly slower than HashMap due to linked list maintenance
- Predictable iteration order unlike HashMap

## Solution

```java
import java.util.*;

public class LinkedHashMapLRU {
    
    // LRU Cache implementation
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        
        public LRUCache(int capacity) {
            super(capacity, 0.75f, true); // access-order mode
            this.capacity = capacity;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
    
    public static void main(String[] args) {
        demonstrateInsertionOrder();
        demonstrateAccessOrder();
        demonstrateLRUCache();
        comparePerformance();
    }
    
    public static void demonstrateInsertionOrder() {
        System.out.println("=== Insertion Order (Default) ===");
        
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("C", 3);
        map.put("A", 1);
        map.put("B", 2);
        map.put("D", 4);
        
        System.out.println("Insertion order maintained: " + map);
        
        // Update existing key doesn't change order
        map.put("A", 10);
        System.out.println("After updating 'A': " + map);
        
        // Compare with HashMap (no order)
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("C", 3);
        hashMap.put("A", 1);
        hashMap.put("B", 2);
        hashMap.put("D", 4);
        System.out.println("HashMap (no order): " + hashMap);
    }
    
    public static void demonstrateAccessOrder() {
        System.out.println("\n=== Access Order Mode ===");
        
        // true = access-order, false = insertion-order
        Map<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
        
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);
        
        System.out.println("Initial: " + map);
        
        // Access 'B' - moves to end
        map.get("B");
        System.out.println("After get('B'): " + map);
        
        // Access 'A' - moves to end
        map.get("A");
        System.out.println("After get('A'): " + map);
        
        // put() also counts as access
        map.put("C", 30);
        System.out.println("After put('C', 30): " + map);
    }
    
    public static void demonstrateLRUCache() {
        System.out.println("\n=== LRU Cache Implementation ===");
        
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        System.out.println("Cache (capacity 3): " + cache);
        
        // Access 1 - makes it recently used
        cache.get(1);
        System.out.println("After accessing 1: " + cache);
        
        // Add 4 - evicts least recently used (2)
        cache.put(4, "Four");
        System.out.println("After adding 4 (evicts 2): " + cache);
        
        // Add 5 - evicts least recently used (3)
        cache.put(5, "Five");
        System.out.println("After adding 5 (evicts 3): " + cache);
        
        // Access 1 again
        cache.get(1);
        System.out.println("After accessing 1: " + cache);
        
        // Add 6 - evicts 4 (least recently used)
        cache.put(6, "Six");
        System.out.println("After adding 6 (evicts 4): " + cache);
        
        System.out.println("\nFinal cache: " + cache);
        System.out.println("LRU cache automatically evicts least recently used entries");
    }
    
    public static void comparePerformance() {
        System.out.println("\n=== Performance Comparison ===");
        
        int size = 100000;
        
        // HashMap performance
        Map<Integer, String> hashMap = new HashMap<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashMap.put(i, "Value" + i);
        }
        long hashMapPutTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashMap.get(i);
        }
        long hashMapGetTime = System.nanoTime() - start;
        
        // LinkedHashMap performance
        Map<Integer, String> linkedHashMap = new LinkedHashMap<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedHashMap.put(i, "Value" + i);
        }
        long linkedPutTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedHashMap.get(i);
        }
        long linkedGetTime = System.nanoTime() - start;
        
        System.out.println("HashMap put: " + hashMapPutTime + "ns");
        System.out.println("LinkedHashMap put: " + linkedPutTime + "ns");
        System.out.println("Overhead: " + ((linkedPutTime - hashMapPutTime) * 100 / hashMapPutTime) + "%");
        
        System.out.println("\nHashMap get: " + hashMapGetTime + "ns");
        System.out.println("LinkedHashMap get: " + linkedGetTime + "ns");
        
        System.out.println("\nLinkedHashMap is slightly slower due to linked list maintenance");
        System.out.println("But provides predictable iteration order");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **put/get/remove**: O(1) same as HashMap
- **Iteration**: O(n) in insertion/access order

**Space Complexity**: O(n) plus overhead for doubly-linked list (2 extra references per entry)

## Edge Cases and Pitfalls

- **Access-Order Mode**: get() and put() both count as access
- **removeEldestEntry()**: Called after insertion; return true to remove eldest
- **Iteration Order**: Predictable unlike HashMap
- **Performance**: Slightly slower than HashMap (5-10% overhead)
- **When to Use LinkedHashMap**: Need predictable iteration order or LRU cache
- **When to Use HashMap**: Don't care about order, need maximum performance
- **Thread Safety**: Not thread-safe; synchronize externally if needed

## Interview-Ready Answer

"LinkedHashMap extends HashMap with a doubly-linked list to maintain insertion order or access order. In access-order mode (third constructor parameter = true), it moves accessed entries to the end, perfect for LRU cache implementation. Override removeEldestEntry() to automatically evict old entries when capacity is exceeded. It provides O(1) operations like HashMap but with predictable iteration order, at the cost of slightly higher memory usage and 5-10% performance overhead for maintaining the linked list."
