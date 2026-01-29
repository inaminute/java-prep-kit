# WeakHashMap and Memory Management

## Problem Statement

Explain how WeakHashMap uses weak references for automatic memory management. Demonstrate its use cases for implementing caches and preventing memory leaks, and compare with regular HashMap.

**Requirements**:
- Explain weak references and garbage collection
- Demonstrate automatic entry removal
- Show cache implementation use case
- Compare with HashMap memory behavior

## Approach

- WeakHashMap uses WeakReference for keys
- Entries are removed when keys are garbage collected
- Useful for caches where entries can be discarded when memory is low
- Values are strongly referenced (only keys are weak)
- Not thread-safe (use Collections.synchronizedMap if needed)
- Helps prevent memory leaks in certain scenarios

## Solution

```java
import java.util.*;

public class WeakHashMapUsage {
    
    static class LargeObject {
        private byte[] data = new byte[1024 * 1024]; // 1MB
        private String name;
        
        public LargeObject(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static void main(String[] args) {
        demonstrateWeakReferences();
        demonstrateCache();
        compareWithHashMap();
    }
    
    public static void demonstrateWeakReferences() {
        System.out.println("=== Weak References ===");
        
        WeakHashMap<LargeObject, String> weakMap = new WeakHashMap<>();
        
        LargeObject key1 = new LargeObject("Key1");
        LargeObject key2 = new LargeObject("Key2");
        LargeObject key3 = new LargeObject("Key3");
        
        weakMap.put(key1, "Value1");
        weakMap.put(key2, "Value2");
        weakMap.put(key3, "Value3");
        
        System.out.println("Initial size: " + weakMap.size());
        System.out.println("Map: " + weakMap);
        
        // Remove strong references
        key1 = null;
        key2 = null;
        
        // Suggest garbage collection
        System.gc();
        
        // Wait a bit for GC
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("After GC, size: " + weakMap.size());
        System.out.println("Map: " + weakMap);
        System.out.println("Entries with null keys were removed automatically");
    }
    
    public static void demonstrateCache() {
        System.out.println("\n=== Cache Implementation ===");
        
        // Simple cache using WeakHashMap
        WeakHashMap<String, byte[]> cache = new WeakHashMap<>();
        
        // Add entries to cache
        String key1 = new String("data1"); // new String to avoid string pool
        String key2 = new String("data2");
        String key3 = new String("data3");
        
        cache.put(key1, new byte[1024]);
        cache.put(key2, new byte[1024]);
        cache.put(key3, new byte[1024]);
        
        System.out.println("Cache size: " + cache.size());
        
        // Simulate keys going out of scope
        key1 = null;
        key2 = null;
        
        System.gc();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("After GC, cache size: " + cache.size());
        System.out.println("Cache automatically cleaned up unused entries");
    }
    
    public static void compareWithHashMap() {
        System.out.println("\n=== WeakHashMap vs HashMap ===");
        
        // HashMap - entries never removed
        Map<LargeObject, String> hashMap = new HashMap<>();
        LargeObject hashKey = new LargeObject("HashKey");
        hashMap.put(hashKey, "Value");
        
        System.out.println("HashMap size before nulling key: " + hashMap.size());
        hashKey = null;
        System.gc();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("HashMap size after GC: " + hashMap.size());
        System.out.println("HashMap still holds entry (memory leak potential)");
        
        // WeakHashMap - entries removed when keys are GC'd
        WeakHashMap<LargeObject, String> weakMap = new WeakHashMap<>();
        LargeObject weakKey = new LargeObject("WeakKey");
        weakMap.put(weakKey, "Value");
        
        System.out.println("\nWeakHashMap size before nulling key: " + weakMap.size());
        weakKey = null;
        System.gc();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("WeakHashMap size after GC: " + weakMap.size());
        System.out.println("WeakHashMap automatically removed entry");
    }
}
```

## Complexity Analysis

**Time Complexity**: Same as HashMap - O(1) average for get/put

**Space Complexity**: O(n) but entries can be garbage collected

**Memory Management**:
- Entries removed automatically when keys are GC'd
- Helps prevent memory leaks in cache scenarios
- Values are strongly referenced until entry is removed

## Edge Cases and Pitfalls

- **String Literals**: String literals are in string pool and won't be GC'd; use `new String()` for testing
- **Values Not Weak**: Only keys are weak references; values are strong references
- **GC Timing**: Entry removal is not immediate; depends on GC timing
- **Thread Safety**: Not thread-safe; use Collections.synchronizedMap if needed
- **Use Cases**: Caches, metadata storage, canonicalizing mappings
- **Not for All Caches**: If you need control over eviction, use other cache implementations
- **Null Keys**: Allowed (unlike ConcurrentHashMap)

## Interview-Ready Answer

"WeakHashMap uses weak references for keys, allowing entries to be garbage collected when keys are no longer strongly referenced elsewhere. This is useful for implementing caches that can automatically free memory when needed. Unlike HashMap which holds strong references and can cause memory leaks, WeakHashMap's entries are removed automatically by the garbage collector. Note that only keys are weak - values are strongly referenced until the entry is removed. It's ideal for metadata caches and canonicalizing mappings."
