# Design a custom thread pool with work stealing

## Problem Statement

Implement a thread pool with work-stealing algorithm for load balancing across worker threads.

## Approach

- **Deque per thread**: Each worker has its own task queue
- **LIFO for own tasks**: Worker takes from head of own deque
- **FIFO for stealing**: Thieves take from tail of victim's deque
- **Load balancing**: Idle workers steal from busy workers
- **Minimizes contention**: Different ends of deque

## Solution

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class WorkStealingThreadPool {
    private final WorkerThread[] workers;
    private final AtomicInteger taskCount = new AtomicInteger(0);
    private volatile boolean shutdown = false;
    
    public WorkStealingThreadPool(int threadCount) {
        workers = new WorkerThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            workers[i] = new WorkerThread(i);
            workers[i].start();
        }
    }
    
    public void submit(Runnable task) {
        if (shutdown) {
            throw new RejectedExecutionException("Pool is shutdown");
        }
        
        // Submit to least loaded worker
        WorkerThread leastLoaded = workers[0];
        for (WorkerThread worker : workers) {
            if (worker.getQueueSize() < leastLoaded.getQueueSize()) {
                leastLoaded = worker;
            }
        }
        leastLoaded.addTask(task);
        taskCount.incrementAndGet();
    }
    
    public void shutdown() {
        shutdown = true;
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }
    
    class WorkerThread extends Thread {
        private final int id;
        private final Deque<Runnable> taskQueue = new LinkedBlockingDeque<>();
        private final Random random = new Random();
        
        WorkerThread(int id) {
            this.id = id;
            setName("Worker-" + id);
        }
        
        void addTask(Runnable task) {
            taskQueue.addFirst(task); // Add to head
        }
        
        int getQueueSize() {
            return taskQueue.size();
        }
        
        @Override
        public void run() {
            while (!shutdown || !taskQueue.isEmpty()) {
                try {
                    Runnable task = taskQueue.pollFirst(); // Take from head (LIFO)
                    
                    if (task == null) {
                        // Try to steal from other workers
                        task = steal();
                    }
                    
                    if (task != null) {
                        task.run();
                        taskCount.decrementAndGet();
                    } else {
                        Thread.sleep(10); // No work available
                    }
                } catch (InterruptedException e) {
                    if (shutdown) break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        private Runnable steal() {
            // Try to steal from random victim
            int attempts = workers.length * 2;
            for (int i = 0; i < attempts; i++) {
                int victimId = random.nextInt(workers.length);
                if (victimId == id) continue; // Don't steal from self
                
                WorkerThread victim = workers[victimId];
                Runnable task = victim.taskQueue.pollLast(); // Steal from tail (FIFO)
                if (task != null) {
                    System.out.println(getName() + " stole task from " + victim.getName());
                    return task;
                }
            }
            return null;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
        
        // Submit many tasks
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            pool.submit(() -> {
                System.out.println("Executing task " + taskId + " on " + 
                    Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        Thread.sleep(3000);
        pool.shutdown();
    }
}

// Simplified version using ForkJoinPool
class ForkJoinPoolExample {
    public static void main(String[] args) {
        // Java's built-in work-stealing pool
        ForkJoinPool pool = new ForkJoinPool(4);
        
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            pool.submit(() -> {
                System.out.println("Task " + taskId + " on " + 
                    Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        pool.shutdown();
    }
}
```

## Work-Stealing Algorithm

1. **Own tasks**: Worker takes tasks from head of its deque (LIFO)
2. **Stealing**: Idle worker takes from tail of victim's deque (FIFO)
3. **Minimizes contention**: Different ends reduce conflicts
4. **Load balancing**: Automatically balances work across threads

## Advantages

- **Better load balancing**: Idle threads help busy threads
- **Reduced contention**: Different ends of deque
- **Cache locality**: LIFO for own tasks improves cache hits
- **Scalability**: Works well with many threads

## Complexity Analysis

**Time Complexity**: O(1) for task submission and stealing

**Space Complexity**: O(n) where n is total number of tasks

## Edge Cases and Pitfalls

- **Stealing overhead**: Too much stealing can hurt performance
- **Starvation**: Possible if tasks keep being stolen
- **Deque implementation**: Must be thread-safe for stealing
- **Common Pitfall**: Not handling concurrent access to deques properly

## Interview-Ready Answer

"A work-stealing thread pool gives each worker its own task deque. Workers take tasks from the head of their own deque (LIFO) for better cache locality, while idle workers steal from the tail of other workers' deques (FIFO) to minimize contention. This provides automatic load balancing and better scalability than traditional thread pools. Java's ForkJoinPool implements this algorithm."

**Tags**: thread-pool, work-stealing, load-balancing
