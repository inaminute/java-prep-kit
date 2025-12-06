# Implement a thread pool using ExecutorService

## Problem Statement

Create a thread pool using ExecutorService to manage concurrent task execution efficiently.

## Approach

- **Thread reuse**: Avoid overhead of creating new threads
- **Task queue**: Queue tasks when all threads are busy
- **Different pool types**: Fixed, cached, scheduled, single-threaded
- **Graceful shutdown**: Proper cleanup of resources
- **Future for results**: Get return values from tasks

## Solution

```java
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class ExecutorServiceExample {
    
    // Fixed thread pool
    public static void fixedThreadPoolExample() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " executed by " + 
                    Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        executor.shutdown(); // Graceful shutdown
    }
    
    // Cached thread pool
    public static void cachedThreadPoolExample() {
        ExecutorService executor = Executors.newCachedThreadPool();
        
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " by " + 
                    Thread.currentThread().getName());
            });
        }
        
        executor.shutdown();
    }
    
    // Single thread executor
    public static void singleThreadExecutorExample() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " executed sequentially");
            });
        }
        
        executor.shutdown();
    }
    
    // Using Callable and Future
    public static void callableFutureExample() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Callable<Integer> task = () -> {
            Thread.sleep(1000);
            return 42;
        };
        
        Future<Integer> future = executor.submit(task);
        
        System.out.println("Doing other work...");
        
        Integer result = future.get(); // Blocks until result is available
        System.out.println("Result: " + result);
        
        executor.shutdown();
    }
    
    // invokeAll - execute multiple tasks
    public static void invokeAllExample() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            tasks.add(() -> {
                Thread.sleep(1000);
                return "Result from task " + taskId;
            });
        }
        
        List<Future<String>> futures = executor.invokeAll(tasks);
        
        for (Future<String> future : futures) {
            System.out.println(future.get());
        }
        
        executor.shutdown();
    }
    
    // invokeAny - return first completed task
    public static void invokeAnyExample() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        List<Callable<String>> tasks = new ArrayList<>();
        tasks.add(() -> {
            Thread.sleep(2000);
            return "Task 1";
        });
        tasks.add(() -> {
            Thread.sleep(1000);
            return "Task 2";
        });
        tasks.add(() -> {
            Thread.sleep(3000);
            return "Task 3";
        });
        
        String result = executor.invokeAny(tasks); // Returns "Task 2"
        System.out.println("First result: " + result);
        
        executor.shutdown();
    }
    
    // Custom ThreadPoolExecutor
    public static void customThreadPoolExample() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                      // corePoolSize
            4,                      // maximumPoolSize
            60,                     // keepAliveTime
            TimeUnit.SECONDS,       // time unit
            new LinkedBlockingQueue<>(10), // work queue
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
        
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " by " + 
                    Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        executor.shutdown();
    }
    
    // Proper shutdown
    public static void properShutdownExample() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Submit tasks
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        // Graceful shutdown
        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                // Force shutdown if tasks don't complete
                executor.shutdownNow();
                
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

## Executor Types

| Type | Description | Use Case |
|------|-------------|----------|
| FixedThreadPool | Fixed number of threads | Known workload |
| CachedThreadPool | Creates threads as needed | Short-lived tasks |
| SingleThreadExecutor | Single worker thread | Sequential execution |
| ScheduledThreadPool | Scheduled/periodic tasks | Cron-like jobs |

## Shutdown Methods

| Method | Description |
|--------|-------------|
| `shutdown()` | Graceful shutdown, completes submitted tasks |
| `shutdownNow()` | Attempts to stop all tasks immediately |
| `awaitTermination()` | Blocks until all tasks complete or timeout |

## Edge Cases and Pitfalls

- **Not shutting down**: Executor keeps JVM alive
- **Blocking on Future.get()**: Can cause deadlock
- **Queue overflow**: Unbounded queues can cause OutOfMemoryError
- **Common Pitfall**: Not handling RejectedExecutionException

## Interview-Ready Answer

"ExecutorService provides a high-level API for managing thread pools. It offers different pool types like FixedThreadPool for a fixed number of threads, CachedThreadPool for dynamic sizing, and ScheduledThreadPool for periodic tasks. It handles thread lifecycle, task queuing, and provides Future objects for getting results. Always remember to shut down the executor to release resources."

**Tags**: executor-service, thread-pool, concurrency
