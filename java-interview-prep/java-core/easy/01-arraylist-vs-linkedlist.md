# ArrayList vs LinkedList

## Problem Statement

Explain the differences between ArrayList and LinkedList in Java. When would you choose one over the other? Implement a simple performance comparison demonstrating the key differences in insertion and access operations.

**Requirements**:
- Compare time complexity for common operations
- Demonstrate practical use cases for each
- Show code examples of when each performs better

## Approach

- ArrayList uses a dynamic array internally, providing fast random access but slower insertions/deletions in the middle
- LinkedList uses a doubly-linked list structure, providing fast insertions/deletions but slower random access
- ArrayList is better for read-heavy operations with random access
- LinkedList is better for frequent insertions/deletions, especially at the beginning or middle
- Consider memory overhead: LinkedList uses more memory per element due to node pointers

## Solution

```java
import java.util.*;

public class ArrayListVsLinkedList {
    
    public static void main(String[] args) {
        demonstratePerformanceDifferences();
    }
    
    public static void demonstratePerformanceDifferences() {
        int size = 100000;
        
        // ArrayList performance
        List<Integer> arrayList = new ArrayList<>();
        long startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long arrayListAddTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += arrayList.get(i);
        }
        long arrayListAccessTime = System.nanoTime() - startTime;
        
        // LinkedList performance
        List<Integer> linkedList = new LinkedList<>();
        startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedList.add(i);
        }
        long linkedListAddTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        sum = 0;
        for (int i = 0; i < size; i++) {
            sum += linkedList.get(i);
        }
        long linkedListAccessTime = System.nanoTime() - startTime;
        
        System.out.println("ArrayList - Add: " + arrayListAddTime + "ns, Access: " + arrayListAccessTime + "ns");
        System.out.println("LinkedList - Add: " + linkedListAddTime + "ns, Access: " + linkedListAccessTime + "ns");
    }
    
    // When to use ArrayList
    public static class UseArrayList {
        private List<String> items = new ArrayList<>();
        
        public void addItem(String item) {
            items.add(item); // O(1) amortized
        }
        
        public String getItem(int index) {
            return items.get(index); // O(1)
        }
    }
    
    // When to use LinkedList
    public static class UseLinkedList {
        private LinkedList<String> queue = new LinkedList<>();
        
        public void addFirst(String item) {
            queue.addFirst(item); // O(1)
        }
        
        public String removeFirst() {
            return queue.removeFirst(); // O(1)
        }
    }
}
```

## Complexity Analysis

**ArrayList**:
- **Time Complexity**: 
  - Access by index: O(1)
  - Add at end: O(1) amortized
  - Add/Remove at arbitrary position: O(n)
  - Search: O(n)
- **Space Complexity**: O(n) with less overhead per element

**LinkedList**:
- **Time Complexity**:
  - Access by index: O(n)
  - Add/Remove at beginning/end: O(1)
  - Add/Remove at arbitrary position: O(n) to find, O(1) to modify
  - Search: O(n)
- **Space Complexity**: O(n) with more overhead per element (node pointers)

## Edge Cases and Pitfalls

- **Memory Overhead**: LinkedList uses approximately twice the memory of ArrayList due to storing previous and next node references
- **Cache Locality**: ArrayList has better cache performance due to contiguous memory allocation, making it faster in practice even for some operations where LinkedList has better theoretical complexity
- **Random Access**: Never use LinkedList when you need frequent random access by index - it will iterate from the beginning or end
- **Resizing**: ArrayList may need to resize and copy elements when capacity is exceeded, causing occasional O(n) operations

## Interview-Ready Answer

"ArrayList uses a dynamic array providing O(1) random access but O(n) insertions in the middle, while LinkedList uses a doubly-linked list with O(1) insertions/deletions at ends but O(n) random access. Choose ArrayList for read-heavy workloads with random access, and LinkedList for frequent insertions/deletions at the beginning or end, though ArrayList is generally preferred due to better cache locality and lower memory overhead."
