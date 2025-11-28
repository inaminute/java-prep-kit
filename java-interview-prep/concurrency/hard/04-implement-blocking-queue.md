# Implement Blocking Queue

## Problem Statement

Implement a custom BlockingQueue from scratch using wait() and notify(). Your implementation should support put() (blocking when full) and take() (blocking when empty) operations. Include proper synchronization, handle interruptions, and support bounded capacity.

**Input**: Producer and consumer threads

**Output**: Thread-safe blocking queue implementation

**Constraints**: 
- Must implement put() and take() with blocking
- Should handle interruptions properly
- Must support bounded capacity

## Approach

- Use array or LinkedList as underlying storage
- Synchronize all operations on the queue
- Use wait() when queue is full (put) or empty (take)
- Use notifyAll() to wake waiting threads after state changes
- Handle InterruptedException properly
- Use while loops for wait conditions to handle spurious wakeups
- Track size and capacity for bounds checking

## Solution

```java
import java.util.LinkedList;
import java.util.Queue;

public class CustomBlockingQueue<T> {
    private Queue<T> queue;
    private int capacity;
    
    public CustomBlockingQueue(int capacity) {
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }
    
    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            wait(); // Wait until space available
        }
        
        queue.add(item);
        notifyAll(); // Notify waiting consumers
    }
    
    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // Wait until item available
        }
        
        T item = queue.poll();
        notifyAll(); // Notify waiting producers
        return item;
    }
    
    public synchronized int size() {
        return queue.size();
    }
    
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
    
    public synchronized boolean isFull() {
        return queue.size() == capacity;
    }
    
    // Non-blocking operations
    public synchronized boolean offer(T item) {
        if (queue.size() == capacity) {
            return false;
        }
        queue.add(item);
        notifyAll();
        return true;
    }
    
    public synchronized T poll() {
        if (queue.isEmpty()) {
            return null;
        }
        T item = queue.poll();
        notifyAll();
        return item;
    }
}

// Test the implementation
class BlockingQueueTest {
    public static void main(String[] args) throws InterruptedException {
        CustomBlockingQueue<Integer> queue = new CustomBlockingQueue<>(5);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.put(i);
                    System.out.println("Produced: " + i + " (size: " + queue.size() + ")");
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Integer item = queue.take();
                    System.out.println("Consumed: " + item + " (size: " + queue.size() + ")");
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("Test completed");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for put() and take() operations

**Space Complexity**: O(n) where n is the capacity

## Edge Cases and Pitfalls

- **Spurious wakeups**: Always use while loops, not if statements, to check conditions after waking from wait().
- **notifyAll() vs notify()**: Use notifyAll() to wake all waiting threads. notify() might wake the wrong type of thread.
- **InterruptedException handling**: Properly handle interruptions by restoring interrupt status or propagating the exception.
- **Synchronization on all methods**: All methods accessing shared state must be synchronized to prevent race conditions.

## Interview-Ready Answer

"A custom BlockingQueue uses wait() and notify() for thread coordination. The put() method waits while the queue is full, and take() waits while empty. After adding or removing items, notifyAll() wakes waiting threads. Use while loops for wait conditions to handle spurious wakeups. All operations must be synchronized. The implementation demonstrates fundamental concurrency concepts: mutual exclusion, condition waiting, and thread coordination using Java's built-in synchronization primitives."
