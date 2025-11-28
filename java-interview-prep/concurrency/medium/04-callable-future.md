# Callable and Future

## Problem Statement

Explain and demonstrate Callable and Future interfaces in Java for executing tasks that return results. Show how Callable differs from Runnable, how to submit Callable tasks to an ExecutorService, and how to retrieve results using Future. Include examples of timeout handling and cancellation.

**Input**: Tasks that compute and return results

**Output**: Results retrieved via Future objects

**Constraints**: 
- Must demonstrate Callable implementation
- Should show Future.get() usage
- Must explain exception handling

## Approach

- Callable is like Runnable but can return a result and throw checked exceptions
- Future represents the result of an asynchronous computation
- Submit Callable to ExecutorService which returns a Future
- Future.get() blocks until result is available
- Future supports timeout and cancellation
- Use Future.isDone() to check completion without blocking

## Solution

```java
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

// Callable that returns a result
class FactorialCalculator implements Callable<Long> {
    private int number;
    
    public FactorialCalculator(int number) {
        this.number = number;
    }
    
    @Override
    public Long call() throws Exception {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }
        
        System.out.println(Thread.currentThread().getName() + 
                         " calculating factorial of " + number);
        
        long result = 1;
        for (int i = 2; i <= number; i++) {
            result *= i;
            Thread.sleep(100); // Simulate computation
        }
        
        return result;
    }
}

// Callable that may throw exception
class DataFetcher implements Callable<String> {
    private String url;
    
    public DataFetcher(String url) {
        this.url = url;
    }
    
    @Override
    public String call() throws Exception {
        System.out.println("Fetching data from: " + url);
        Thread.sleep(1000);
        
        if (url.contains("error")) {
            throw new Exception("Failed to fetch data from " + url);
        }
        
        return "Data from " + url;
    }
}

public class CallableFutureDemo {
    public static void main(String[] args) {
        System.out.println("=== Basic Callable and Future ===");
        testBasicCallable();
        
        System.out.println("\n=== Multiple Futures ===");
        testMultipleFutures();
        
        System.out.println("\n=== Future with Timeout ===");
        testFutureTimeout();
        
        System.out.println("\n=== Future Cancellation ===");
        testFutureCancellation();
        
        System.out.println("\n=== Exception Handling ===");
        testExceptionHandling();
    }
    
    private static void testBasicCallable() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Future<Long> future = executor.submit(new FactorialCalculator(5));
        
        try {
            System.out.println("Waiting for result...");
            Long result = future.get(); // Blocks until result is available
            System.out.println("Factorial result: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
    
    private static void testMultipleFutures() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Long>> futures = new ArrayList<>();
        
        // Submit multiple tasks
        for (int i = 1; i <= 5; i++) {
            Future<Long> future = executor.submit(new FactorialCalculator(i));
            futures.add(future);
        }
        
        // Retrieve results
        for (int i = 0; i < futures.size(); i++) {
            try {
                Long result = futures.get(i).get();
                System.out.println("Factorial of " + (i + 1) + " = " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
    }
    
    private static void testFutureTimeout() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Future<Long> future = executor.submit(new FactorialCalculator(10));
        
        try {
            // Wait for at most 500 milliseconds
            Long result = future.get(500, TimeUnit.MILLISECONDS);
            System.out.println("Result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Task timed out");
            future.cancel(true); // Cancel the task
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
    
    private static void testFutureCancellation() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Future<Long> future = executor.submit(new FactorialCalculator(20));
        
        try {
            Thread.sleep(300);
            
            if (!future.isDone()) {
                System.out.println("Cancelling task...");
                boolean cancelled = future.cancel(true);
                System.out.println("Cancelled: " + cancelled);
                System.out.println("Is cancelled: " + future.isCancelled());
            }
            
            Long result = future.get(); // Will throw CancellationException
        } catch (CancellationException e) {
            System.out.println("Task was cancelled");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
    
    private static void testExceptionHandling() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Future<String> future1 = executor.submit(new DataFetcher("http://api.example.com"));
        Future<String> future2 = executor.submit(new DataFetcher("http://error.example.com"));
        
        try {
            String result1 = future1.get();
            System.out.println("Result 1: " + result1);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        try {
            String result2 = future2.get();
            System.out.println("Result 2: " + result2);
        } catch (ExecutionException e) {
            System.out.println("Task threw exception: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for Future operations, O(n) for task execution

**Space Complexity**: O(1) per Future object

## Edge Cases and Pitfalls

- **Blocking get()**: Future.get() blocks indefinitely. Always use timeout variant or check isDone() first to avoid blocking forever.
- **Exception wrapping**: Exceptions thrown by Callable are wrapped in ExecutionException. Use getCause() to retrieve the original exception.
- **Cancellation doesn't guarantee stop**: cancel(true) attempts to interrupt the thread, but the task must check for interruption and respond appropriately.
- **Multiple get() calls**: Calling get() multiple times on the same Future is safe and returns the same result, but the first call blocks.

## Interview-Ready Answer

"Callable is similar to Runnable but can return a result and throw checked exceptions. When you submit a Callable to an ExecutorService, it returns a Future representing the pending result. Future.get() blocks until the result is available, with optional timeout support. Future also supports cancellation via cancel() and status checking with isDone() and isCancelled(). This pattern is essential for asynchronous computations where you need to retrieve results or handle exceptions from background tasks."
