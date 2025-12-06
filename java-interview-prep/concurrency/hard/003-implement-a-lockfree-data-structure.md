# Implement a lock-free data structure

## Problem Statement

Create a lock-free stack or queue using compare-and-swap (CAS) operations and atomic references.

## Approach

- **AtomicReference**: Use for lock-free updates
- **CAS loop**: Retry until successful
- **ABA problem**: Value changes from A to B back to A
- **No blocking**: Threads never block
- **Progress guarantee**: At least one thread makes progress

## Solution

```java
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

// Lock-free stack
class LockFreeStack<T> {
    private static class Node<T> {
        final T value;
        Node<T> next;
        
        Node(T value) {
            this.value = value;
        }
    }
    
    private final AtomicReference<Node<T>> head = new AtomicReference<>();
    
    public void push(T value) {
        Node<T> newNode = new Node<>(value);
        while (true) {
            Node<T> currentHead = head.get();
            newNode.next = currentHead;
            if (head.compareAndSet(currentHead, newNode)) {
                return; // Success
            }
            // CAS failed, retry
        }
    }
    
    public T pop() {
        while (true) {
            Node<T> currentHead = head.get();
            if (currentHead == null) {
                return null; // Stack is empty
            }
            Node<T> newHead = currentHead.next;
            if (head.compareAndSet(currentHead, newHead)) {
                return currentHead.value; // Success
            }
            // CAS failed, retry
        }
    }
    
    public boolean isEmpty() {
        return head.get() == null;
    }
}

// Lock-free queue (Michael-Scott algorithm)
class LockFreeQueue<T> {
    private static class Node<T> {
        final T value;
        final AtomicReference<Node<T>> next;
        
        Node(T value) {
            this.value = value;
            this.next = new AtomicReference<>();
        }
    }
    
    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;
    
    public LockFreeQueue() {
        Node<T> dummy = new Node<>(null);
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }
    
    public void enqueue(T value) {
        Node<T> newNode = new Node<>(value);
        while (true) {
            Node<T> currentTail = tail.get();
            Node<T> tailNext = currentTail.next.get();
            
            if (currentTail == tail.get()) {
                if (tailNext == null) {
                    // Try to link new node
                    if (currentTail.next.compareAndSet(null, newNode)) {
                        // Try to swing tail to new node
                        tail.compareAndSet(currentTail, newNode);
                        return;
                    }
                } else {
                    // Help other thread move tail
                    tail.compareAndSet(currentTail, tailNext);
                }
            }
        }
    }
    
    public T dequeue() {
        while (true) {
            Node<T> currentHead = head.get();
            Node<T> currentTail = tail.get();
            Node<T> headNext = currentHead.next.get();
            
            if (currentHead == head.get()) {
                if (currentHead == currentTail) {
                    if (headNext == null) {
                        return null; // Queue is empty
                    }
                    // Help move tail
                    tail.compareAndSet(currentTail, headNext);
                } else {
                    T value = headNext.value;
                    if (head.compareAndSet(currentHead, headNext)) {
                        return value;
                    }
                }
            }
        }
    }
}

// Lock-free counter
class LockFreeCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        while (true) {
            int current = count.get();
            int next = current + 1;
            if (count.compareAndSet(current, next)) {
                return;
            }
        }
    }
    
    public int get() {
        return count.get();
    }
}

// Testing
class LockFreeTest {
    public static void main(String[] args) throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        
        // Multiple threads pushing
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int value = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    stack.push(value * 100 + j);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Pop all elements
        int count = 0;
        while (!stack.isEmpty()) {
            stack.pop();
            count++;
        }
        System.out.println("Total elements: " + count); // Should be 1000
    }
}
```

## ABA Problem

The ABA problem occurs when:
1. Thread 1 reads value A
2. Thread 2 changes A to B, then back to A
3. Thread 1's CAS succeeds, but state may have changed

**Solution**: Use AtomicStampedReference or AtomicMarkableReference

```java
import java.util.concurrent.atomic.AtomicStampedReference;

class ABAPreventionExample {
    private AtomicStampedReference<Integer> value = 
        new AtomicStampedReference<>(0, 0);
    
    public void update(int newValue) {
        while (true) {
            int[] stamp = new int[1];
            Integer current = value.get(stamp);
            if (value.compareAndSet(current, newValue, stamp[0], stamp[0] + 1)) {
                return;
            }
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) amortized for operations

**Space Complexity**: O(n) where n is number of elements

## Edge Cases and Pitfalls

- **ABA problem**: Use stamped/markable references
- **Memory reclamation**: Difficult without garbage collection
- **Contention**: High contention can cause livelock
- **Common Pitfall**: Not handling ABA problem

## Interview-Ready Answer

"Lock-free data structures use atomic operations like CAS instead of locks, ensuring at least one thread always makes progress. A lock-free stack uses AtomicReference with CAS loops for push/pop. The main challenge is the ABA problem where a value changes and changes back, which can be solved using AtomicStampedReference. Lock-free structures provide better scalability under high contention."

**Tags**: lock-free, cas, atomic
