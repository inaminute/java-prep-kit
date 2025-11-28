# ConcurrentHashMap

## Problem Statement

Explain and demonstrate ConcurrentHashMap from java.util.concurrent package. Show how ConcurrentHashMap provides thread-safe map operations without locking the entire map. Compare it with synchronized HashMap and Hashtable, and demonstrate its atomic operations and performance benefits.

**Input**: Multiple threads performing map operations

**Output**: Thread-safe map with high concurrency

**Constraints**: 
- Must demonstrate ConcurrentHashMap usage
- Should show atomic operations
- Must explain performance advantages

## Approach

- ConcurrentHashMap uses lock striping for fine-grained locking
- Multiple threads can read and write different segments concurrently
- Provides atomic operations like putIfAbsent, computeIfAbsent
- Iterators are weakly consistent (don't throw ConcurrentModificationException)
- Better performance than synchronized HashMap or Hashtable
- Null keys and values are not allowed
- Use for high-concurrency scenarios

## Solution

```java
import java.util.concurrent.*;
import java.util.*;

public class ConcurrentHashMapDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Basic Operations ===");
        testBasicOperations();
        
        System.out.println("\n=== Atomic Operations ===");
        testAtomicOperations();
        
        System.out.println("\n=== Performance Comparison ===");
        testPerformance();
        
        System.out.println("\n=== Compute Methods ===");
        testComputeMethods();
    }
    
    private static void testBasicOperations() throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        
        // Multiple threads updating map
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String key = "key" + (j % 10);
                    map.merge(key, 1, Integer::sum);
                }
            }, "Thread-" + threadId);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Final map size: " + map.size());
        map.forEach((k, v) -> System.out.println(k + ": " + v));
    }
    
    private static void testAtomicOperations() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        
        // putIfAbsent - atomic operation
        Integer prev1 = map.putIfAbsent("counter", 1);
        System.out.println("First putIfAbsent: " + prev1); // null
        
        Integer prev2 = map.putIfAbsent("counter", 2);
        System.out.println("Second putIfAbsent: " + prev2); // 1
        System.out.println("Current value: " + map.get("counter")); // 1
        
        // replace - atomic operation
        boolean replaced = map.replace("counter", 1, 10);
        System.out.println("Replace success: " + replaced);
        System.out.println("After replace: " + map.get("counter"));
        
        // remove with value check - atomic operation
        boolean removed = map.remove("counter", 10);
        System.out.println("Remove success: " + removed);
        System.out.println("After remove: " + map.get("counter"));
    }
    
    private static void testPerformance() throws InterruptedException {
        int operations = 100000;
        int threads = 10;
        
        // Test ConcurrentHashMap
        long start = System.currentTimeMillis();
        testMapPerformance(new ConcurrentHashMap<>(), operations, threads);
        long concurrentTime = System.currentTimeMillis() - start;
        
        // Test synchronized HashMap
        start = System.currentTimeMillis();
        testMapPerformance(Collections.synchronizedMap(new HashMap<>()), operations, threads);
        long synchronizedTime = System.currentTimeMillis() - start;
        
        System.out.println("ConcurrentHashMap time: " + concurrentTime + "ms");
        System.out.println("Synchronized HashMap time: " + synchronizedTime + "ms");
        System.out.println("Speedup: " + (double)synchronizedTime / concurrentTime + "x");
    }
    
    private static void testMapPerformance(Map<Integer, Integer> map, 
                                          int operations, int threadCount) 
                                          throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                Random random = new Random();
                for (int j = 0; j < operations / threadCount; j++) {
                    int key = random.nextInt(1000);
                    map.put(key, key);
                    map.get(key);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    private static void testComputeMethods() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        
        // computeIfAbsent
        Integer value1 = map.computeIfAbsent("c", k -> 3);
        System.out.println("computeIfAbsent result: " + value1);
        
        // computeIfPresent
        Integer value2 = map.computeIfPresent("a", (k, v) -> v * 10);
        System.out.println("computeIfPresent result: " + value2);
        
        // compute
        Integer value3 = map.compute("b", (k, v) -> v == null ? 1 : v + 1);
        System.out.println("compute result: " + value3);
        
        // merge
        Integer value4 = map.merge("a", 5, Integer::sum);
        System.out.println("merge result: " + value4);
        
        System.out.println("Final map: " + map);
    }
}

// Word frequency counter example
class WordFrequencyCounter {
    private ConcurrentHashMap<String, Integer> frequencies = new ConcurrentHashMap<>();
    
    public void processText(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            frequencies.merge(word, 1, Integer::sum);
        }
    }
    
    public void processTextConcurrently(List<String> texts) throws InterruptedException {
        Thread[] threads = new Thread[texts.size()];
        
        for (int i = 0; i < texts.size(); i++) {
            final String text = texts.get(i);
            threads[i] = new Thread(() -> processText(text));
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    public Map<String, Integer> getFrequencies() {
        return new HashMap<>(frequencies);
    }
    
    public void printTopWords(int n) {
        frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(n)
            .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) average for get, put, remove operations

**Space Complexity**: O(n) where n is the number of entries

## Edge Cases and Pitfalls

- **Null keys/values not allowed**: Unlike HashMap, ConcurrentHashMap doesn't allow null keys or values. Attempting to use null throws NullPointerException.
- **Size may be approximate**: The size() method may not reflect exact count during concurrent modifications, but it's eventually consistent.
- **Iterators are weakly consistent**: Iterators don't throw ConcurrentModificationException but may not reflect recent modifications.
- **Atomic operations**: Use putIfAbsent, computeIfAbsent, merge for atomic compound operations instead of separate get-then-put sequences.

## Interview-Ready Answer

"ConcurrentHashMap provides thread-safe map operations with better concurrency than synchronized HashMap or Hashtable. It uses lock striping to allow multiple threads to read and write different segments concurrently. It provides atomic operations like putIfAbsent, computeIfAbsent, and merge for compound operations. Iterators are weakly consistent and don't throw ConcurrentModificationException. ConcurrentHashMap doesn't allow null keys or values. It's ideal for high-concurrency scenarios where multiple threads frequently access the map."
