# HashMap Internal Working and Collision Handling

## Problem Statement

Explain the internal working of HashMap in Java 8+, including the bucket structure, hashing mechanism, collision resolution, and the tree conversion optimization. Demonstrate how hash collisions are handled.

**Requirements**:
- Explain bucket array and hash function
- Demonstrate collision handling (chaining and treeification)
- Show the impact of load factor
- Explain rehashing process

## Approach

- HashMap uses an array of Node objects (buckets)
- Hash function: `(n - 1) & hash` where n is array length
- Collisions handled by chaining (linked list)
- When bucket size exceeds 8, converts to red-black tree (Java 8+)
- Default load factor 0.75 triggers rehashing
- Rehashing doubles the array size and redistributes entries

## Solution

```java
import java.util.*;
import java.lang.reflect.*;

public class HashMapWorking {
    
    static class Key {
        private int value;
        
        public Key(int value) {
            this.value = value;
        }
        
        @Override
        public int hashCode() {
            // Intentionally poor hash to demonstrate collisions
            return value % 4;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Key)) return false;
            return this.value == ((Key) obj).value;
        }
        
        @Override
        public String toString() {
            return "Key(" + value + ")";
        }
    }
    
    public static void main(String[] args) {
        demonstrateBasicHashing();
        demonstrateCollisions();
        demonstrateLoadFactor();
        demonstrateTreeification();
    }
    
    public static void demonstrateBasicHashing() {
        System.out.println("=== Basic HashMap Hashing ===");
        Map<String, Integer> map = new HashMap<>();
        
        map.put("Alice", 30);
        map.put("Bob", 25);
        map.put("Charlie", 35);
        
        // Show hash codes and bucket indices
        String[] keys = {"Alice", "Bob", "Charlie"};
        for (String key : keys) {
            int hash = key.hashCode();
            int bucket = hash & (16 - 1); // Assuming default capacity 16
            System.out.println(key + " -> hashCode: " + hash + 
                             ", bucket: " + bucket);
        }
    }
    
    public static void demonstrateCollisions() {
        System.out.println("\n=== Collision Handling ===");
        Map<Key, String> map = new HashMap<>();
        
        // These will collide due to poor hash function
        for (int i = 0; i < 12; i++) {
            map.put(new Key(i), "Value" + i);
        }
        
        System.out.println("Map size: " + map.size());
        
        // Verify retrieval works despite collisions
        System.out.println("Get Key(5): " + map.get(new Key(5)));
        System.out.println("Get Key(9): " + map.get(new Key(9)));
        
        // Show hash distribution
        Map<Integer, Integer> hashDistribution = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            int hash = new Key(i).hashCode();
            hashDistribution.put(hash, hashDistribution.getOrDefault(hash, 0) + 1);
        }
        System.out.println("Hash distribution: " + hashDistribution);
    }
    
    public static void demonstrateLoadFactor() {
        System.out.println("\n=== Load Factor and Rehashing ===");
        
        // Default load factor 0.75
        Map<Integer, String> map = new HashMap<>(4);
        
        System.out.println("Initial capacity: 4");
        System.out.println("Threshold (capacity * loadFactor): " + (4 * 0.75));
        
        for (int i = 0; i < 10; i++) {
            map.put(i, "Value" + i);
            if (i == 2) {
                System.out.println("After 3 elements: approaching threshold");
            }
            if (i == 3) {
                System.out.println("After 4 elements: rehashing triggered, new capacity: 8");
            }
            if (i == 6) {
                System.out.println("After 7 elements: rehashing triggered, new capacity: 16");
            }
        }
    }
    
    public static void demonstrateTreeification() {
        System.out.println("\n=== Treeification (Java 8+) ===");
        
        // Create many collisions to trigger treeification
        Map<Key, String> map = new HashMap<>();
        
        System.out.println("Adding elements with same hash...");
        for (int i = 0; i < 20; i++) {
            map.put(new Key(i * 4), "Value" + i); // All have same hash
        }
        
        System.out.println("Map size: " + map.size());
        System.out.println("When bucket size > 8 and capacity >= 64, " +
                         "linked list converts to red-black tree");
        
        // Performance comparison
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            map.get(new Key(76)); // Last element
        }
        long time = System.nanoTime() - start;
        
        System.out.println("Lookup time (with tree): " + time + "ns");
        System.out.println("Tree provides O(log n) instead of O(n) for collisions");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **put(K key, V value)**: O(1) average, O(log n) worst case (with treeification)
- **get(Object key)**: O(1) average, O(log n) worst case
- **remove(Object key)**: O(1) average, O(log n) worst case
- **containsKey(Object key)**: O(1) average, O(log n) worst case

**Space Complexity**: O(n) where n is number of entries

**Load Factor Impact**:
- Higher load factor (e.g., 0.9): Less memory, more collisions
- Lower load factor (e.g., 0.5): More memory, fewer collisions
- Default 0.75 is a good balance

## Edge Cases and Pitfalls

- **Null Key**: HashMap allows one null key (stored at bucket 0)
- **Null Values**: Multiple null values are allowed
- **Poor hashCode()**: If all keys have same hash, performance degrades to O(n) or O(log n)
- **Mutable Keys**: Never modify key objects after insertion; hash code change makes entry unretrievable
- **Initial Capacity**: Choose initial capacity based on expected size to avoid rehashing
- **Treeification Threshold**: Bucket converts to tree when size > 8 and total capacity >= 64
- **Untreeification**: Tree converts back to list when bucket size drops below 6
- **Thread Safety**: HashMap is not thread-safe; use ConcurrentHashMap for concurrent access

## Interview-Ready Answer

"HashMap uses an array of buckets where each bucket can hold multiple entries. The hash function determines the bucket index using `(n-1) & hash`. Collisions are handled by chaining - initially as a linked list, but converting to a red-black tree when bucket size exceeds 8 (Java 8+). This gives O(log n) worst-case instead of O(n). The default load factor of 0.75 balances memory and performance. When the threshold is exceeded, the array doubles in size and all entries are rehashed."
