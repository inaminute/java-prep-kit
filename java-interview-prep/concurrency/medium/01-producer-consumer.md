# Producer-Consumer Pattern

## Problem Statement

Implement the classic Producer-Consumer pattern in Java using wait() and notify(). Create a bounded buffer where producer threads add items and consumer threads remove items. Ensure thread safety and proper coordination between producers and consumers to avoid race conditions, buffer overflow, and underflow.

**Input**: Multiple producer and consumer threads

**Output**: Thread-safe production and consumption of items

**Constraints**: 
- Buffer must have fixed capacity
- Producers must wait when buffer is full
- Consumers must wait when buffer is empty
- Must use wait() and notify() for coordination

## Approach

- Use a shared buffer (queue) with fixed capacity
- Synchronize access to the buffer using synchronized blocks
- Producers wait when buffer is full, notify consumers after adding
- Consumers wait when buffer is empty, notify producers after removing
- Use while loops (not if) to check conditions to handle spurious wakeups
- notifyAll() is safer than notify() to wake all waiting threads

## Solution

```java
import java.util.LinkedList;
import java.util.Queue;

// Bounded buffer implementation
class BoundedBuffer<T> {
    private Queue<T> queue;
    private int capacity;
    
    public BoundedBuffer(int capacity) {
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }
    
    // Producer calls this method
    public synchronized void produce(T item) throws InterruptedException {
        // Wait while buffer is full
        while (queue.size() == capacity) {
            System.out.println(Thread.currentThread().getName() + 
                             " waiting - buffer full");
            wait();
        }
        
        // Add item to buffer
        queue.add(item);
        System.out.println(Thread.currentThread().getName() + 
                         " produced: " + item + " (size: " + queue.size() + ")");
        
        // Notify waiting consumers
        notifyAll();
    }
    
    // Consumer calls this method
    public synchronized T consume() throws InterruptedException {
        // Wait while buffer is empty
        while (queue.isEmpty()) {
            System.out.println(Thread.currentThread().getName() + 
                             " waiting - buffer empty");
            wait();
        }
        
        // Remove item from buffer
        T item = queue.poll();
        System.out.println(Thread.currentThread().getName() + 
                         " consumed: " + item + " (size: " + queue.size() + ")");
        
        // Notify waiting producers
        notifyAll();
        
        return item;
    }
    
    public synchronized int size() {
        return queue.size();
    }
}

// Producer thread
class Producer implements Runnable {
    private BoundedBuffer<Integer> buffer;
    private int itemsToProduce;
    
    public Producer(BoundedBuffer<Integer> buffer, int itemsToProduce) {
        this.buffer = buffer;
        this.itemsToProduce = itemsToProduce;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 1; i <= itemsToProduce; i++) {
                buffer.produce(i);
                Thread.sleep(100); // Simulate production time
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(Thread.currentThread().getName() + " interrupted");
        }
    }
}

// Consumer thread
class Consumer implements Runnable {
    private BoundedBuffer<Integer> buffer;
    private int itemsToConsume;
    
    public Consumer(BoundedBuffer<Integer> buffer, int itemsToConsume) {
        this.buffer = buffer;
        this.itemsToConsume = itemsToConsume;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 1; i <= itemsToConsume; i++) {
                Integer item = buffer.consume();
                Thread.sleep(150); // Simulate consumption time
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(Thread.currentThread().getName() + " interrupted");
        }
    }
}

// Demonstration
public class ProducerConsumerDemo {
    public static void main(String[] args) throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(5);
        
        // Create producers
        Thread producer1 = new Thread(new Producer(buffer, 10), "Producer-1");
        Thread producer2 = new Thread(new Producer(buffer, 10), "Producer-2");
        
        // Create consumers
        Thread consumer1 = new Thread(new Consumer(buffer, 10), "Consumer-1");
        Thread consumer2 = new Thread(new Consumer(buffer, 10), "Consumer-2");
        
        // Start all threads
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        
        // Wait for completion
        producer1.join();
        producer2.join();
        consumer1.join();
        consumer2.join();
        
        System.out.println("All threads completed. Final buffer size: " + buffer.size());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) per produce/consume operation - Adding and removing from queue is constant time

**Space Complexity**: O(n) where n is the buffer capacity

## Edge Cases and Pitfalls

- **Using if instead of while**: Always use while loops to check conditions, not if statements. Spurious wakeups can cause threads to wake up without the condition being true.
- **Using notify() instead of notifyAll()**: notify() wakes only one thread, which might be the wrong type (producer waking producer). Use notifyAll() to wake all waiting threads.
- **Forgetting to synchronize**: All access to shared buffer must be synchronized to prevent race conditions and ensure visibility of changes.
- **Deadlock with separate locks**: Using separate locks for producers and consumers can cause deadlock. Use a single lock for the entire buffer.

## Interview-Ready Answer

"The Producer-Consumer pattern coordinates multiple producer threads adding items to a shared buffer and consumer threads removing items. I use a bounded buffer with synchronized methods, where producers call wait() when the buffer is full and consumers call wait() when it's empty. After producing or consuming, threads call notifyAll() to wake waiting threads. Using while loops instead of if statements handles spurious wakeups correctly. This pattern is fundamental for coordinating work between threads."
