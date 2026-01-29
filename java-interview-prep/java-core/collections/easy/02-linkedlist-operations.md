# LinkedList Operations and Use Cases

## Problem Statement

Explain how LinkedList works internally in Java and when to use it over ArrayList. Demonstrate the doubly-linked structure and analyze performance characteristics for different operations.

**Requirements**:
- Explain the doubly-linked node structure
- Compare with ArrayList for different operations
- Demonstrate when LinkedList is preferred
- Show Deque operations

## Approach

- LinkedList uses a doubly-linked list structure with Node objects
- Each node contains data, next reference, and previous reference
- Maintains references to first and last nodes for O(1) access at both ends
- Implements both List and Deque interfaces
- Better for frequent insertions/deletions at beginning or middle
- Worse for random access compared to ArrayList

## Solution

```java
import java.util.*;

public class LinkedListOperations {
    
    static class Node {
        int data;
        Node next;
        Node prev;
        
        Node(int data) {
            this.data = data;
        }
    }
    
    public static void main(String[] args) {
        demonstrateBasicOperations();
        demonstrateDequeOperations();
        compareWithArrayList();
        demonstrateCustomLinkedList();
    }
    
    public static void demonstrateBasicOperations() {
        System.out.println("=== Basic LinkedList Operations ===");
        LinkedList<String> list = new LinkedList<>();
        
        // Add operations
        list.add("A");
        list.add("B");
        list.add("C");
        list.addFirst("Start");
        list.addLast("End");
        
        System.out.println("List: " + list);
        
        // Access operations
        System.out.println("First: " + list.getFirst());
        System.out.println("Last: " + list.getLast());
        System.out.println("At index 2: " + list.get(2));
        
        // Remove operations
        list.removeFirst();
        list.removeLast();
        System.out.println("After removing first and last: " + list);
    }
    
    public static void demonstrateDequeOperations() {
        System.out.println("\n=== Deque Operations ===");
        Deque<Integer> deque = new LinkedList<>();
        
        // Use as stack (LIFO)
        deque.push(1);
        deque.push(2);
        deque.push(3);
        System.out.println("Stack pop: " + deque.pop());
        
        // Use as queue (FIFO)
        deque.offer(4);
        deque.offer(5);
        System.out.println("Queue poll: " + deque.poll());
        
        System.out.println("Remaining: " + deque);
    }
    
    public static void compareWithArrayList() {
        System.out.println("\n=== Performance Comparison ===");
        int size = 100000;
        
        // LinkedList - add at beginning
        LinkedList<Integer> linkedList = new LinkedList<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedList.addFirst(i);
        }
        long linkedTime = System.nanoTime() - start;
        
        // ArrayList - add at beginning
        ArrayList<Integer> arrayList = new ArrayList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(0, i);
        }
        long arrayTime = System.nanoTime() - start;
        
        System.out.println("LinkedList addFirst: " + linkedTime + "ns");
        System.out.println("ArrayList add(0): " + arrayTime + "ns");
        System.out.println("LinkedList is " + (arrayTime / linkedTime) + "x faster");
        
        // Random access comparison
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            linkedList.get(i * 10);
        }
        linkedTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            arrayList.get(i * 10);
        }
        arrayTime = System.nanoTime() - start;
        
        System.out.println("\nLinkedList random access: " + linkedTime + "ns");
        System.out.println("ArrayList random access: " + arrayTime + "ns");
        System.out.println("ArrayList is " + (linkedTime / arrayTime) + "x faster");
    }
    
    public static void demonstrateCustomLinkedList() {
        System.out.println("\n=== Custom Doubly-Linked List ===");
        
        Node head = new Node(1);
        Node second = new Node(2);
        Node third = new Node(3);
        
        // Link nodes
        head.next = second;
        second.prev = head;
        second.next = third;
        third.prev = second;
        
        // Traverse forward
        System.out.print("Forward: ");
        Node current = head;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.next;
        }
        
        // Traverse backward
        System.out.print("\nBackward: ");
        current = third;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.prev;
        }
        System.out.println();
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **addFirst/addLast**: O(1) - just update references
- **removeFirst/removeLast**: O(1) - just update references
- **add(int index, E e)**: O(n) - must traverse to index
- **get(int index)**: O(n) - must traverse to index
- **remove(Object o)**: O(n) - must search and traverse
- **contains(Object o)**: O(n) - linear search

**Space Complexity**: O(n) where n is number of elements, plus overhead for Node objects (3 references per element vs 1 in ArrayList)

## Edge Cases and Pitfalls

- **Memory Overhead**: Each element requires a Node object with 3 references (data, next, prev), using more memory than ArrayList
- **Cache Locality**: Poor cache performance due to non-contiguous memory allocation
- **Random Access**: O(n) time makes LinkedList poor choice when frequent random access is needed
- **Iterator Performance**: Iterator-based traversal is efficient, but index-based access is slow
- **When to Use LinkedList**: 
  - Frequent insertions/deletions at beginning or middle
  - Implementing queues or deques
  - When you never need random access
- **When to Use ArrayList**:
  - Frequent random access
  - Mostly adding at end
  - Memory-constrained environments

## Interview-Ready Answer

"LinkedList uses a doubly-linked node structure where each node has references to data, next, and previous nodes. It provides O(1) insertions and deletions at both ends, making it ideal for queue/deque implementations. However, random access is O(n) because it must traverse from the beginning or end. ArrayList is generally preferred unless you need frequent insertions/deletions at the beginning, as it has better cache locality and lower memory overhead."
