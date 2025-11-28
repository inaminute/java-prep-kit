# BlockingQueue

## Problem Statement

Demonstrate the use of BlockingQueue interface from java.util.concurrent package. Show how BlockingQueue simplifies the Producer-Consumer pattern by providing thread-safe queue operations with blocking behavior. Compare different BlockingQueue implementations (ArrayBlockingQueue, LinkedBlockingQueue) and explain their characteristics.

**Input**: Multiple producer and consumer threads

**Output**: Thread-safe queue operations

**Constraints**: 
- Must use BlockingQueue from java.util.concurrent
- Should demonstrate put() and take() methods
- Must show different implementations

## Approach

- BlockingQueue provides thread-safe queue with blocking operations
- put() blocks when queue is full, take() blocks when queue is empty
- No need for explicit synchronization or wait/notify
- ArrayBlockingQueue has fixed capacity, LinkedBlockingQueue can be bounded or unbounded
- offer() and poll() are non-blocking alternatives with timeout options
- BlockingQueue handles all synchronization internally

## Solution

```java
import java.util.concurrent.*;

// Producer using BlockingQueue
class BlockingQueueProducer implements Runnable {
    private BlockingQueue<Integer> queue;
    private int itemsToProduce;
    
    public BlockingQueueProducer(BlockingQueue<Integer> queue, int itemsToProduce) {
        this.queue = queue;
        this.itemsToProduce = itemsToProduce;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 1; i <= itemsToProduce; i++) {
                queue.put(i); // Blocks if queue is full
                System.out.println(Thread.currentThread().getName() + 
                                 " produced: " + i);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Consumer using BlockingQueue
class BlockingQueueConsumer implements Runnable {
    private BlockingQueue<Integer> queue;
    private int itemsToConsume;
    
    public BlockingQueueConsumer(BlockingQueue<Integer> queue, int itemsToConsume) {
        this.queue = queue;
        this.itemsToConsume = itemsToConsume;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 1; i <= itemsToConsume; i++) {
                Integer item = queue.take(); // Blocks if queue is empty
                System.out.println(Thread.currentThread().getName() + 
                                 " consumed: " + item);
                Thread.sleep(150);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Demonstration of different BlockingQueue implementations
public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ArrayBlockingQueue (Fixed Capacity) ===");
        testArrayBlockingQueue();
        
        System.out.println("\n=== LinkedBlockingQueue (Unbounded) ===");
        testLinkedBlockingQueue();
        
        System.out.println("\n=== PriorityBlockingQueue ===");
        testPriorityBlockingQueue();
        
        System.out.println("\n=== Timeout Operations ===");
        testTimeoutOperations();
    }
    
    private static void testArrayBlockingQueue() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        
        Thread producer = new Thread(new BlockingQueueProducer(queue, 10), "Producer");
        Thread consumer = new Thread(new BlockingQueueConsumer(queue, 10), "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
    }
    
    private static void testLinkedBlockingQueue() throws InterruptedException {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        
        Thread producer = new Thread(new BlockingQueueProducer(queue, 5), "Producer");
        Thread consumer = new Thread(new BlockingQueueConsumer(queue, 5), "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
    }
    
    private static void testPriorityBlockingQueue() throws InterruptedException {
        BlockingQueue<Integer> queue = new PriorityBlockingQueue<>();
        
        queue.put(5);
        queue.put(1);
        queue.put(3);
        
        System.out.println("Items in priority order:");
        while (!queue.isEmpty()) {
            System.out.println(queue.take());
        }
    }
    
    private static void testTimeoutOperations() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);
        
        // Fill the queue
        queue.put(1);
        queue.put(2);
        
        // Try to add with timeout (will fail)
        boolean added = queue.offer(3, 1, TimeUnit.SECONDS);
        System.out.println("Added with timeout: " + added);
        
        // Remove and try again
        queue.take();
        added = queue.offer(3, 1, TimeUnit.SECONDS);
        System.out.println("Added after removal: " + added);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for put() and take() operations

**Space Complexity**: O(n) where n is the queue capacity

## Edge Cases and Pitfalls

- **Unbounded queues**: LinkedBlockingQueue without capacity can grow indefinitely, potentially causing OutOfMemoryError. Always consider setting a capacity.
- **Null elements**: BlockingQueue implementations don't allow null elements. Attempting to add null throws NullPointerException.
- **Interrupted exceptions**: put() and take() throw InterruptedException which must be handled properly. Don't ignore interrupts.
- **offer() vs put()**: offer() returns false if queue is full (non-blocking), while put() blocks. Choose based on whether you want blocking behavior.

## Interview-Ready Answer

"BlockingQueue is a thread-safe queue interface that provides blocking operations for producer-consumer scenarios. The put() method blocks when the queue is full, and take() blocks when empty, eliminating the need for explicit synchronization. Common implementations include ArrayBlockingQueue (fixed capacity) and LinkedBlockingQueue (optionally bounded). BlockingQueue simplifies concurrent programming by handling all synchronization internally and is widely used in thread pools and task queues."
