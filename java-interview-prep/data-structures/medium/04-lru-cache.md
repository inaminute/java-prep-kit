# LRU Cache

## Problem Statement

Design a data structure that follows the constraints of a Least Recently Used (LRU) cache. Implement the LRUCache class:
- `LRUCache(int capacity)` Initialize the LRU cache with positive size capacity
- `int get(int key)` Return the value of the key if exists, otherwise return -1
- `void put(int key, int value)` Update the value if key exists, otherwise add the key-value pair. If the number of keys exceeds capacity, evict the least recently used key

**Constraints:**
- 1 ≤ capacity ≤ 3000
- 0 ≤ key ≤ 10⁴
- 0 ≤ value ≤ 10⁵
- At most 2 * 10⁵ calls to get and put

## Approach

- Use a doubly linked list to maintain access order
- Use a hash map for O(1) key lookup
- Move accessed nodes to the front (most recently used)
- Remove from tail when capacity is exceeded (least recently used)

## Solution

```java
import java.util.*;

class LRUCache {
    class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) {
            key = k;
            value = v;
        }
    }
    
    private Map<Integer, Node> map;
    private Node head, tail;
    private int capacity;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node(0, 0);
        this.tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }
    
    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }
        Node node = map.get(key);
        remove(node);
        addToFront(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (map.containsKey(key)) {
            remove(map.get(key));
        }
        Node node = new Node(key, value);
        map.put(key, node);
        addToFront(node);
        
        if (map.size() > capacity) {
            Node lru = tail.prev;
            remove(lru);
            map.remove(lru.key);
        }
    }
    
    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for both get and put operations

**Space Complexity**: O(capacity) - Store at most capacity key-value pairs

## Edge Cases and Pitfalls

- **Edge Case 1**: Updating existing key - Remove old node before adding new one
- **Edge Case 2**: Capacity of 1 - Works correctly with single element
- **Edge Case 3**: Getting non-existent key - Returns -1
- **Common Pitfall 1**: Not updating access order on get - LRU logic breaks
- **Common Pitfall 2**: Forgetting to remove from map when evicting - Memory leak

## Interview-Ready Answer

I would use a doubly linked list for access order and a hash map for O(1) lookup. Get operations move nodes to the front, put operations add to front and evict from tail if over capacity. Both operations are O(1) with O(capacity) space.
