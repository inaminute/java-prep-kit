# LFU Cache

## Problem Statement

Design and implement a data structure for a Least Frequently Used (LFU) cache. Implement the LFUCache class:
- `LFUCache(int capacity)` Initializes the object with the capacity
- `int get(int key)` Gets the value of the key if exists, otherwise returns -1
- `void put(int key, int value)` Updates the value if key exists, otherwise adds the key-value pair. When the cache reaches capacity, invalidate the least frequently used key before inserting a new item. If there is a tie, remove the least recently used key.

**Constraints:**
- 0 ≤ capacity ≤ 10⁴
- 0 ≤ key ≤ 10⁵
- 0 ≤ value ≤ 10⁹
- At most 2 * 10⁵ calls to get and put

## Approach

- Use hash map for key to value/frequency mapping
- Use hash map for frequency to list of keys
- Track minimum frequency for efficient eviction
- Use doubly linked list for each frequency level to maintain LRU order within same frequency

## Solution

```java
import java.util.*;

class LFUCache {
    class Node {
        int key, value, freq;
        Node prev, next;
        Node(int k, int v) {
            key = k;
            value = v;
            freq = 1;
        }
    }
    
    class DLList {
        Node head, tail;
        int size;
        
        DLList() {
            head = new Node(0, 0);
            tail = new Node(0, 0);
            head.next = tail;
            tail.prev = head;
        }
        
        void add(Node node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            size++;
        }
        
        void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }
        
        Node removeLast() {
            if (size > 0) {
                Node node = tail.prev;
                remove(node);
                return node;
            }
            return null;
        }
    }
    
    private Map<Integer, Node> cache;
    private Map<Integer, DLList> freqMap;
    private int capacity;
    private int minFreq;
    
    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.freqMap = new HashMap<>();
        this.minFreq = 0;
    }
    
    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }
        Node node = cache.get(key);
        updateFreq(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (capacity == 0) return;
        
        if (cache.containsKey(key)) {
            Node node = cache.get(key);
            node.value = value;
            updateFreq(node);
        } else {
            if (cache.size() >= capacity) {
                DLList minFreqList = freqMap.get(minFreq);
                Node toRemove = minFreqList.removeLast();
                cache.remove(toRemove.key);
            }
            
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            freqMap.putIfAbsent(1, new DLList());
            freqMap.get(1).add(newNode);
            minFreq = 1;
        }
    }
    
    private void updateFreq(Node node) {
        int freq = node.freq;
        DLList list = freqMap.get(freq);
        list.remove(node);
        
        if (freq == minFreq && list.size == 0) {
            minFreq++;
        }
        
        node.freq++;
        freqMap.putIfAbsent(node.freq, new DLList());
        freqMap.get(node.freq).add(node);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for both get and put operations

**Space Complexity**: O(capacity) - Store at most capacity key-value pairs

## Edge Cases and Pitfalls

- **Edge Case 1**: Capacity is 0 - All operations should be no-ops
- **Edge Case 2**: Tie in frequency - Remove least recently used among them
- **Edge Case 3**: Updating existing key - Increment frequency
- **Common Pitfall 1**: Not updating minFreq correctly - Eviction breaks
- **Common Pitfall 2**: Not maintaining LRU order within same frequency - Violates tie-breaking rule

## Interview-Ready Answer

I would use a hash map for key-to-node lookup and another map for frequency-to-list-of-nodes. Each frequency level has a doubly linked list maintaining LRU order. I track minimum frequency for O(1) eviction. Both get and put are O(1) with O(capacity) space.
