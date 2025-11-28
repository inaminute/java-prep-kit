# ThreadPoolExecutor

## Problem Statement

Explain and demonstrate ThreadPoolExecutor in Java for managing a pool of worker threads. Show how to create thread pools with different configurations, submit tasks for execution, and properly shutdown the executor. Compare different thread pool types and explain when to use each.

**Input**: Multiple tasks to execute

**Output**: Tasks executed by pooled threads

**Constraints**: 
- Must demonstrate ThreadPoolExecutor creation
- Should show task submission methods
- Must explain proper shutdown

## Approach

- ThreadPoolExecutor manages a pool of reusable worker threads
- Avoids overhead of creating new threads for each task
- Core pool size: minimum number of threads to keep alive
- Maximum pool size: maximum number of threads allowed
- Queue holds tasks waiting for available threads
- Use Executors factory methods for common configurations
- Always shutdown executor to release resources

## Solution

```java
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

// Example task
class Task implements Runnable {
    private int taskId;
    
    public Task(int taskId) {
        this.taskId = taskId;
    }
    
    @Override
    public void run() {
        System.out.println("Task " + taskId + " started by " + 
                         Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Task " + taskId + " completed");
    }
}

// Callable task with return value
class ComputationTask implements Callable<Integer> {
    private int number;
    
    public ComputationTask(int number) {
        this.number = number;
    }
    
    @Override
    public Integer call() throws Exception {
        System.out.println("Computing square of " + number);
        Thread.sleep(500);
        return number * number;
    }
}

public class ThreadPoolExecutorDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== Fixed Thread Pool ===");
        testFixedThreadPool();
        
        System.out.println("\n=== Cached Thread Pool ===");
        testCachedThreadPool();
        
        System.out.println("\n=== Custom ThreadPoolExecutor ===");
        testCustomThreadPool();
        
        System.out.println("\n=== Callable and Future ===");
        testCallableAndFuture();
    }
    
    private static void testFixedThreadPool() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 1; i <= 10; i++) {
            executor.submit(new Task(i));
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    
    private static void testCachedThreadPool() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        
        for (int i = 1; i <= 5; i++) {
            executor.submit(new Task(i));
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    
    private static void testCustomThreadPool() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                      // corePoolSize
            4,                      // maximumPoolSize
            60L,                    // keepAliveTime
            TimeUnit.SECONDS,       // time unit
            new LinkedBlockingQueue<>(10)  // work queue
        );
        
        for (int i = 1; i <= 8; i++) {
            executor.submit(new Task(i));
        }
        
        System.out.println("Active threads: " + executor.getActiveCount());
        System.out.println("Pool size: " + executor.getPoolSize());
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    
    private static void testCallableAndFuture() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Integer>> futures = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Future<Integer> future = executor.submit(new ComputationTask(i));
            futures.add(future);
        }
        
        for (Future<Integer> future : futures) {
            Integer result = future.get(); // Blocks until result is available
            System.out.println("Result: " + result);
        }
        
        executor.shutdown();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for task submission, O(n) for processing n tasks

**Space Complexity**: O(m + n) where m is pool size and n is queue capacity

## Edge Cases and Pitfalls

- **Forgetting to shutdown**: Always call shutdown() or shutdownNow() to release resources. Executors don't automatically terminate.
- **Using shutdown() vs shutdownNow()**: shutdown() waits for submitted tasks to complete, shutdownNow() attempts to stop running tasks immediately.
- **Unbounded queues**: Using unbounded queues can cause memory issues if tasks are submitted faster than they're processed.
- **Exception handling**: Exceptions in submitted tasks are silently caught. Use Future.get() to retrieve exceptions or provide a custom ThreadFactory with exception handlers.

## Interview-Ready Answer

"ThreadPoolExecutor manages a pool of reusable worker threads to execute tasks efficiently. It has core and maximum pool sizes, a work queue for pending tasks, and configurable keep-alive times. Common configurations include fixed thread pools (constant size), cached thread pools (grows as needed), and single thread executors. Always shutdown executors to release resources. Thread pools eliminate the overhead of creating threads for each task and provide better resource management through task queuing and thread reuse."
