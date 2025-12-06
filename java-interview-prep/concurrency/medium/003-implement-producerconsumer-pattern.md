# Implement Producer-Consumer pattern

## Problem Statement

Design a producer-consumer solution using wait/notify or BlockingQueue for thread-safe data exchange.

## Approach

- **Shared buffer**: Queue or list for data exchange
- **Synchronization**: Prevent race conditions
- **Blocking operations**: Wait when buffer full/empty
- **Multiple producers/consumers**: Support concurrent access
- **BlockingQueue**: Simplifies implementation

## Solution

```java
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

// Solution 1: Using wait/notify
class ProducerConsumerWaitNotify {
    private Queue<Integer> buffer = new LinkedList<>();
    private int capacity = 5;
    
    public synchronized void produce(int value) throws InterruptedException {
        while (buffer.size() == capacity) {
            System.out.println("Buffer full, producer waiting...");
            wait();
        }
        
        buffer.add(value);
        System.out.println("Produced: " + value);
        notifyAll(); // Wake up consumers
    }
    
    public synchronized int consume() throws InterruptedException {
        while (buffer.isEmpty()) {
            System.out.println("Buffer empty, consumer waiting...");
            wait();
        }
        
        int value = buffer.poll();
        System.out.println("Consumed: " + value);
        notifyAll(); // Wake up producers
        return value;
    }
    
    public static void main(String[] args) {
        ProducerConsumerWaitNotify pc = new ProducerConsumerWaitNotify();
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    pc.produce(i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    pc.consume();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        producer.start();
        consumer.start();
    }
}

// Solution 2: Using BlockingQueue (Preferred)
class ProducerConsumerBlockingQueue {
    private BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
    
    public void produce(int value) throws InterruptedException {
        queue.put(value); // Blocks if queue is full
        System.out.println("Produced: " + value);
    }
    
    public int consume() throws InterruptedException {
        int value = queue.take(); // Blocks if queue is empty
        System.out.println("Consumed: " + value);
        return value;
    }
    
    public static void main(String[] args) {
        ProducerConsumerBlockingQueue pc = new ProducerConsumerBlockingQueue();
        
        // Multiple producers
        for (int i = 0; i < 2; i++) {
            final int producerId = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        pc.produce(producerId * 10 + j);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Producer-" + i).start();
        }
        
        // Multiple consumers
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        pc.consume();
                        Thread.sleep(150);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Consumer-" + i).start();
        }
    }
}

// Real-world example: Task processing system
class TaskProcessor {
    static class Task {
        private int id;
        private String data;
        
        public Task(int id, String data) {
            this.id = id;
            this.data = data;
        }
        
        public void process() {
            System.out.println("Processing task " + id + ": " + data);
            try {
                Thread.sleep(500); // Simulate processing
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(10);
    
    class Producer implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 20; i++) {
                    Task task = new Task(i, "Data-" + i);
                    taskQueue.put(task);
                    System.out.println("Produced task: " + i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    class Consumer implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Task task = taskQueue.take();
                    task.process();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void start() {
        new Thread(new Producer()).start();
        new Thread(new Consumer()).start();
        new Thread(new Consumer()).start();
    }
    
    public static void main(String[] args) {
        new TaskProcessor().start();
    }
}
```

## Comparison: wait/notify vs BlockingQueue

| Aspect | wait/notify | BlockingQueue |
|--------|-------------|---------------|
| Complexity | High | Low |
| Error-prone | Yes | No |
| Flexibility | More control | Less control |
| Recommended | No | Yes |

## Edge Cases and Pitfalls

- **Spurious wakeups**: Always use while loop with wait()
- **notify() vs notifyAll()**: Use notifyAll() to avoid missed signals
- **Buffer size**: Choose appropriate capacity
- **Common Pitfall**: Using if instead of while with wait()

## Interview-Ready Answer

"The Producer-Consumer pattern coordinates threads that produce and consume data through a shared buffer. Producers add items to the buffer and wait when it's full, while consumers remove items and wait when it's empty. The modern approach uses BlockingQueue which handles synchronization internally, but it can also be implemented using wait/notify with careful synchronization."

**Tags**: producer-consumer, blocking-queue, design-pattern
