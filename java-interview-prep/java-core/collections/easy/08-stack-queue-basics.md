# Stack and Queue Basics

## Problem Statement

Explain Stack and Queue data structures in Java Collections Framework. Demonstrate LIFO (Stack) and FIFO (Queue) operations, and show when to use each implementation (Stack, ArrayDeque, LinkedList, PriorityQueue).

**Requirements**:
- Demonstrate Stack LIFO operations
- Show Queue FIFO operations
- Explain Deque as both stack and queue
- Compare different implementations

## Approach

- Stack: LIFO (Last-In-First-Out) - push, pop, peek
- Queue: FIFO (First-In-First-Out) - offer, poll, peek
- Deque: Double-ended queue - operations at both ends
- Stack class is legacy; prefer Deque implementations
- ArrayDeque is preferred for stack/queue operations
- PriorityQueue for priority-based ordering

## Solution

```java
import java.util.*;

public class StackQueueBasics {
    
    public static void main(String[] args) {
        demonstrateStack();
        demonstrateQueue();
        demonstrateDeque();
        demonstratePriorityQueue();
        compareImplementations();
    }
    
    public static void demonstrateStack() {
        System.out.println("=== Stack (LIFO) ===");
        
        // Legacy Stack class
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        
        System.out.println("Stack: " + stack);
        System.out.println("Peek: " + stack.peek());    // 3 (doesn't remove)
        System.out.println("Pop: " + stack.pop());      // 3 (removes)
        System.out.println("Pop: " + stack.pop());      // 2
        System.out.println("After pops: " + stack);
        
        // Preferred: Deque as Stack
        Deque<String> stackDeque = new ArrayDeque<>();
        stackDeque.push("A");
        stackDeque.push("B");
        stackDeque.push("C");
        
        System.out.println("\nDeque as Stack: " + stackDeque);
        System.out.println("Pop: " + stackDeque.pop());
    }
    
    public static void demonstrateQueue() {
        System.out.println("\n=== Queue (FIFO) ===");
        
        Queue<String> queue = new LinkedList<>();
        queue.offer("First");
        queue.offer("Second");
        queue.offer("Third");
        
        System.out.println("Queue: " + queue);
        System.out.println("Peek: " + queue.peek());    // First (doesn't remove)
        System.out.println("Poll: " + queue.poll());    // First (removes)
        System.out.println("Poll: " + queue.poll());    // Second
        System.out.println("After polls: " + queue);
        
        // add() vs offer(), remove() vs poll(), element() vs peek()
        Queue<Integer> q = new LinkedList<>();
        q.offer(1);  // Returns false if fails (capacity-restricted queues)
        q.add(2);    // Throws exception if fails
        
        System.out.println("\nQueue methods:");
        System.out.println("poll() returns null if empty: " + new LinkedList<>().poll());
        try {
            new LinkedList<>().remove(); // Throws NoSuchElementException
        } catch (NoSuchElementException e) {
            System.out.println("remove() throws exception if empty");
        }
    }
    
    public static void demonstrateDeque() {
        System.out.println("\n=== Deque (Double-Ended Queue) ===");
        
        Deque<Integer> deque = new ArrayDeque<>();
        
        // Add at both ends
        deque.addFirst(1);
        deque.addLast(2);
        deque.addFirst(0);
        deque.addLast(3);
        
        System.out.println("Deque: " + deque);  // [0, 1, 2, 3]
        
        // Remove from both ends
        System.out.println("removeFirst: " + deque.removeFirst());  // 0
        System.out.println("removeLast: " + deque.removeLast());    // 3
        System.out.println("After removals: " + deque);
        
        // Deque as Stack (LIFO)
        Deque<String> stack = new ArrayDeque<>();
        stack.push("A");
        stack.push("B");
        System.out.println("Stack operations: " + stack.pop());
        
        // Deque as Queue (FIFO)
        Deque<String> queue = new ArrayDeque<>();
        queue.offer("X");
        queue.offer("Y");
        System.out.println("Queue operations: " + queue.poll());
    }
    
    public static void demonstratePriorityQueue() {
        System.out.println("\n=== PriorityQueue (Heap-based) ===");
        
        // Natural ordering (min-heap)
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(5);
        pq.offer(2);
        pq.offer(8);
        pq.offer(1);
        
        System.out.println("PriorityQueue: " + pq);
        System.out.println("Poll (min): " + pq.poll());  // 1
        System.out.println("Poll (min): " + pq.poll());  // 2
        
        // Custom ordering (max-heap)
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.offer(5);
        maxHeap.offer(2);
        maxHeap.offer(8);
        maxHeap.offer(1);
        
        System.out.println("\nMax-heap poll: " + maxHeap.poll());  // 8
        System.out.println("Max-heap poll: " + maxHeap.poll());    // 5
    }
    
    public static void compareImplementations() {
        System.out.println("\n=== Implementation Comparison ===");
        
        int size = 100000;
        
        // Stack (legacy)
        Stack<Integer> stack = new Stack<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            stack.push(i);
        }
        for (int i = 0; i < size; i++) {
            stack.pop();
        }
        long stackTime = System.nanoTime() - start;
        
        // ArrayDeque as Stack
        Deque<Integer> arrayDeque = new ArrayDeque<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayDeque.push(i);
        }
        for (int i = 0; i < size; i++) {
            arrayDeque.pop();
        }
        long arrayDequeTime = System.nanoTime() - start;
        
        // LinkedList as Queue
        Queue<Integer> linkedQueue = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedQueue.offer(i);
        }
        for (int i = 0; i < size; i++) {
            linkedQueue.poll();
        }
        long linkedTime = System.nanoTime() - start;
        
        System.out.println("Stack (legacy): " + stackTime + "ns");
        System.out.println("ArrayDeque: " + arrayDequeTime + "ns");
        System.out.println("LinkedList: " + linkedTime + "ns");
        System.out.println("\nRecommendation: Use ArrayDeque for stack/queue operations");
    }
}
```

## Complexity Analysis

**Stack (ArrayDeque)**:
- **push/pop**: O(1)
- **peek**: O(1)

**Queue (ArrayDeque/LinkedList)**:
- **offer/poll**: O(1)
- **peek**: O(1)

**PriorityQueue**:
- **offer**: O(log n)
- **poll**: O(log n)
- **peek**: O(1)

**Space Complexity**: O(n) for all implementations

## Edge Cases and Pitfalls

- **Stack Class**: Legacy, synchronized (slower); prefer Deque
- **Empty Operations**: poll()/peek() return null; remove()/element() throw exceptions
- **ArrayDeque**: Faster than LinkedList, no capacity restrictions, not thread-safe
- **PriorityQueue**: Not sorted array; only guarantees min/max at head
- **Null Elements**: ArrayDeque and PriorityQueue don't allow nulls; LinkedList does
- **Thread Safety**: None are thread-safe; use ConcurrentLinkedQueue or BlockingQueue
- **When to Use Stack**: Function call stack, undo operations, expression evaluation
- **When to Use Queue**: BFS, task scheduling, buffering
- **When to Use PriorityQueue**: Dijkstra's algorithm, task scheduling by priority

## Interview-Ready Answer

"Stack provides LIFO operations (push, pop, peek) while Queue provides FIFO operations (offer, poll, peek). The legacy Stack class is synchronized and slow; prefer ArrayDeque which implements Deque interface for both stack and queue operations with O(1) performance. PriorityQueue is a heap-based implementation providing O(log n) operations with elements ordered by priority. ArrayDeque is generally the best choice for stack/queue needs due to better performance than LinkedList."
