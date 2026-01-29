# ArrayDeque Implementation

## Problem Statement

Explain ArrayDeque's circular array implementation and demonstrate its use as both a stack and queue. Compare performance with LinkedList and Stack, and show why ArrayDeque is the preferred implementation for deque operations.

**Requirements**:
- Explain circular array structure
- Demonstrate stack and queue operations
- Compare with LinkedList and Stack
- Show performance benefits

## Approach

- ArrayDeque uses resizable circular array
- No capacity restrictions (grows as needed)
- Faster than LinkedList for stack/queue operations
- Better cache locality than LinkedList
- Null elements not allowed
- Not thread-safe

## Solution

```java
import java.util.*;

public class ArrayDequeImplementation {
    
    public static void main(String[] args) {
        demonstrateAsStack();
        demonstrateAsQueue();
        demonstrateAsDeque();
        comparePerformance();
    }
    
    public static void demonstrateAsStack() {
        System.out.println("=== ArrayDeque as Stack (LIFO) ===");
        
        Deque<String> stack = new ArrayDeque<>();
        
        // Push operations
        stack.push("First");
        stack.push("Second");
        stack.push("Third");
        
        System.out.println("Stack: " + stack);
        System.out.println("Peek: " + stack.peek());
        System.out.println("Pop: " + stack.pop());
        System.out.println("Pop: " + stack.pop());
        System.out.println("Remaining: " + stack);
    }
    
    public static void demonstrateAsQueue() {
        System.out.println("\n=== ArrayDeque as Queue (FIFO) ===");
        
        Deque<Integer> queue = new ArrayDeque<>();
        
        // Enqueue operations
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        
        System.out.println("Queue: " + queue);
        System.out.println("Peek: " + queue.peek());
        System.out.println("Poll: " + queue.poll());
        System.out.println("Poll: " + queue.poll());
        System.out.println("Remaining: " + queue);
    }
    
    public static void demonstrateAsDeque() {
        System.out.println("\n=== ArrayDeque as Double-Ended Queue ===");
        
        Deque<String> deque = new ArrayDeque<>();
        
        // Add at both ends
        deque.addFirst("A");
        deque.addLast("B");
        deque.addFirst("Z");
        deque.addLast("C");
        
        System.out.println("Deque: " + deque);
        
        // Remove from both ends
        System.out.println("removeFirst: " + deque.removeFirst());
        System.out.println("removeLast: " + deque.removeLast());
        System.out.println("Remaining: " + deque);
        
        // Peek at both ends
        System.out.println("peekFirst: " + deque.peekFirst());
        System.out.println("peekLast: " + deque.peekLast());
    }
    
    public static void comparePerformance() {
        System.out.println("\n=== Performance Comparison ===");
        
        int size = 100000;
        
        // ArrayDeque as stack
        Deque<Integer> arrayDeque = new ArrayDeque<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayDeque.push(i);
        }
        for (int i = 0; i < size; i++) {
            arrayDeque.pop();
        }
        long arrayDequeTime = System.nanoTime() - start;
        
        // LinkedList as stack
        Deque<Integer> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedList.push(i);
        }
        for (int i = 0; i < size; i++) {
            linkedList.pop();
        }
        long linkedListTime = System.nanoTime() - start;
        
        // Legacy Stack
        Stack<Integer> stack = new Stack<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            stack.push(i);
        }
        for (int i = 0; i < size; i++) {
            stack.pop();
        }
        long stackTime = System.nanoTime() - start;
        
        System.out.println("ArrayDeque: " + arrayDequeTime + "ns");
        System.out.println("LinkedList: " + linkedListTime + "ns");
        System.out.println("Stack: " + stackTime + "ns");
        System.out.println("\nArrayDeque is " + (linkedListTime / arrayDequeTime) + "x faster than LinkedList");
        System.out.println("ArrayDeque is " + (stackTime / arrayDequeTime) + "x faster than Stack");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **addFirst/addLast**: O(1) amortized
- **removeFirst/removeLast**: O(1)
- **peekFirst/peekLast**: O(1)

**Space Complexity**: O(n) with better cache locality than LinkedList

## Edge Cases and Pitfalls

- **Null Not Allowed**: Throws NullPointerException
- **No Capacity Limit**: Grows as needed
- **Circular Array**: Head and tail pointers wrap around
- **Better Than LinkedList**: Faster and more memory efficient
- **Not Thread-Safe**: Use ConcurrentLinkedDeque for thread safety
- **Preferred Implementation**: Use ArrayDeque instead of Stack or LinkedList for stack/queue

## Interview-Ready Answer

"ArrayDeque uses a resizable circular array providing O(1) operations at both ends. It's faster than LinkedList due to better cache locality and no node allocation overhead. It's the preferred implementation for stack and queue operations, replacing the legacy Stack class and outperforming LinkedList. Null elements are not allowed, and it's not thread-safe. The circular array automatically grows when capacity is exceeded."
