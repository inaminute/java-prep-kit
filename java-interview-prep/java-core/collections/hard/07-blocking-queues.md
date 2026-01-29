# BlockingQueue Implementations

## Problem Statement

Explain BlockingQueue interface and its implementations (ArrayBlockingQueue, LinkedBlockingQueue, PriorityBlockingQueue). Demonstrate producer-consumer pattern, blocking operations, and compare bounded vs unbounded queues.

**Requirements**:
- Explain blocking put() and take() operations
- Implement producer-consumer pattern
- Compare ArrayBlockingQueue vs LinkedBlockingQueue
- Show timeout operations

## Approach

- BlockingQueue: Thread-safe queue with blocking operations
- put() blocks when full, take() blocks when empty
- Bounded: ArrayBlockingQueue, LinkedBlockingQueue(capacity)
- Unbounded: LinkedBlockingQueue(), PriorityBlockingQueue
- Producer-consumer pattern without explicit synchronization

## Solution

```java
import java.util.concurrent.*;

public class BlockingQueues {
    
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicOperations();
        demonstrateProducerConsumer();
        compareImplementations();
    }
    
    public static void demonstrateBasicOperations() throws InterruptedException {
        System.out.println("=== BlockingQueue Operations ===");
        
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);
        
        // put() - blocks if full
        queue.put(1);
        queue.put(2);
        queue.put(3);
        System.out.println("Queue: " + queue);
        
        // offer() with timeout
        boolean added = queue.offer(4, 1, TimeUnit.SECONDS);
        System.out.println("offer(4) with timeout: " + added); // false
        
        // take() - blocks if empty
        System.out.println("take(): " + queue.take());
        
        // poll() with timeout
        Integer val = queue.poll(1, TimeUnit.SECONDS);
        System.out.println("poll() with timeout: " + val);
    }
    
    public static void demonstrateProducerConsumer() throws InterruptedException {
        System.out.println("\n=== Producer-Consumer Pattern ===");
        
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(10);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    queue.put(i);
                    System.out.println("Produced: " + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    Integer item = queue.take();
                    System.out.println("Consumed: " + item);
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        
        System.out.println("Producer-consumer completed");
    }
    
    public static void compareImplementations() {
        System.out.println("\n=== Implementation Comparison ===");
        
        System.out.println("ArrayBlockingQueue:");
        System.out.println("- Bounded, fixed capacity");
        System.out.println("- Array-based, better performance");
        System.out.println("- Single lock for put/take");
        
        System.out.println("\nLinkedBlockingQueue:");
        System.out.println("- Optionally bounded");
        System.out.println("- Node-based");
        System.out.println("- Separate locks for put/take (better concurrency)");
        
        System.out.println("\nPriorityBlockingQueue:");
        System.out.println("- Unbounded");
        System.out.println("- Heap-based, elements ordered by priority");
        System.out.println("- Blocking take(), non-blocking put()");
    }
}
```

## Complexity Analysis

**ArrayBlockingQueue**: O(1) for put/take

**LinkedBlockingQueue**: O(1) for put/take

**PriorityBlockingQueue**: O(log n) for put/take

## Edge Cases and Pitfalls

- **Bounded vs Unbounded**: Choose based on memory constraints
- **Blocking**: put/take block indefinitely; use offer/poll with timeout
- **Null Not Allowed**: BlockingQueue doesn't allow null elements
- **Producer-Consumer**: Natural fit for BlockingQueue
- **Fairness**: ArrayBlockingQueue supports optional fairness policy

## Interview-Ready Answer

"BlockingQueue provides thread-safe queue operations with blocking semantics. put() blocks when full, take() blocks when empty, eliminating need for explicit wait/notify. ArrayBlockingQueue is bounded and array-based with single lock. LinkedBlockingQueue is optionally bounded with separate put/take locks for better concurrency. PriorityBlockingQueue is unbounded and heap-based. Ideal for producer-consumer patterns."
