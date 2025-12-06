# Implement a concurrent LRU cache

## Problem Statement

Design a thread-safe LRU cache with O(1) operations using fine-grained locking or lock-free techniques.

## Approach

- **ConcurrentHashMap**: For O(1) lookups
- **Doubly-linked list**: For LRU ordering
- **Segment locking**: Fine-grained locking for better concurrency
- **Atomic operations**: For lock-free updates where possible
- **Eviction**: Remove least recently used on capacity

## Solution

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentLRUCache<K, V> {
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> map;
    private final Node<K, V> head;
    private final Node<K, V> tail;
    private final ReentrantLock lock;
    
    public ConcurrentLRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        this.lock = new ReentrantLock();
        
        head.next = tail;
        tail.prev = head;
    }
    
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        
        // Move to front (most recently used)
        lock.lock();
        try {
            removeNode(node);
            addToFront(node);
        } finally {
            lock.unlock();
        }
        
        return node.value;
    }
    
    public void put(K key, V value) {
        Node<K, V> node = map.get(key);
        
        if (node != null) {
            // Update existing node
            node.value = value;
            lock.lock();
            try {
                removeNode(node);
                addToFront(node);
            } finally {
                lock.unlock();
            }
        } else {
            // Add new node
            Node<K, V> newNode = new Node<>(key, value);
            
            lock.lock();
            try {
                if (map.size() >= capacity) {
                    // Evict least recently used
                    Node<K, V> lru = tail.prev;
                    removeNode(lru);
                    map.remove(lru.key);
                }
                
                addToFront(newNode);
                map.put(key, newNode);
            } finally {
                lock.unlock();
            }
        }
    }
    
    private void addToFront(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
    
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    public int size() {
        return map.size();
    }
}

// Alternative: Using LinkedHashMap with synchronization
import java.util.LinkedHashMap;
import java.util.Map;

class SynchronizedLRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;
    
    public SynchronizedLRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }
    
    public synchronized V get(K key) {
        return cache.get(key);
    }
    
    public synchronized void put(K key, V value) {
        cache.put(key, value);
    }
    
    public synchronized int size() {
        return cache.size();
    }
}

// Testing
class LRUCacheTest {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentLRUCache<Integer, String> cache = new ConcurrentLRUCache<>(3);
        
        // Multiple threads accessing cache
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    int key = (threadId * 100 + j) % 10;
                    cache.put(key, "Value-" + key);
                    String value = cache.get(key);
                    if (value == null) {
                        System.out.println("Cache miss for key: " + key);
                    }
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Final cache size: " + cache.size());
    }
}
```

## Design Choices

### ConcurrentHashMap
- O(1) average lookup
- Built-in thread safety
- Segment-level locking

### Doubly-Linked List
- O(1) add/remove operations
- Maintains LRU order
- Requires synchronization

### Single Lock
- Simpler implementation
- Good for moderate contention
- Can be bottleneck under high load

## Complexity Analysis

**Time Complexity**: 
- get(): O(1) average
- put(): O(1) average

**Space Complexity**: O(capacity)

## Edge Cases and Pitfalls

- **Concurrent eviction**: Multiple threads evicting simultaneously
- **Lock granularity**: Single lock can be bottleneck
- **Memory visibility**: Ensure proper synchronization
- **Common Pitfall**: Not synchronizing list operations

## Interview-Ready Answer

"A concurrent LRU cache combines ConcurrentHashMap for O(1) lookups with a doubly-linked list for LRU ordering. The map provides thread-safe access while a lock protects list modifications. On get(), we move the accessed node to the front. On put(), we add new nodes to the front and evict from the tail when at capacity. This provides O(1) operations with good concurrency."

**Tags**: lru-cache, concurrent, fine-grained-locking
