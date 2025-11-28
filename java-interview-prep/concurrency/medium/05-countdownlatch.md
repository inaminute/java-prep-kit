# CountDownLatch

## Problem Statement

Explain and demonstrate CountDownLatch from java.util.concurrent package. Show how CountDownLatch allows one or more threads to wait until a set of operations being performed by other threads completes. Include practical examples such as waiting for multiple services to start or coordinating parallel computations.

**Input**: Multiple threads performing initialization or computation

**Output**: Coordinated thread execution with synchronization points

**Constraints**: 
- Must demonstrate CountDownLatch usage
- Should show await() and countDown() methods
- Must explain one-time use nature

## Approach

- CountDownLatch is initialized with a count
- Threads call await() to wait until count reaches zero
- Other threads call countDown() to decrement the count
- Once count reaches zero, all waiting threads are released
- CountDownLatch is one-time use and cannot be reset
- Useful for coordinating startup, parallel tasks, or testing

## Solution

```java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// Service that takes time to start
class Service implements Runnable {
    private String serviceName;
    private CountDownLatch latch;
    private int startupTime;
    
    public Service(String serviceName, CountDownLatch latch, int startupTime) {
        this.serviceName = serviceName;
        this.latch = latch;
        this.startupTime = startupTime;
    }
    
    @Override
    public void run() {
        try {
            System.out.println(serviceName + " starting...");
            Thread.sleep(startupTime);
            System.out.println(serviceName + " started");
            latch.countDown(); // Signal completion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Worker performing computation
class Worker implements Runnable {
    private int workerId;
    private CountDownLatch startLatch;
    private CountDownLatch endLatch;
    
    public Worker(int workerId, CountDownLatch startLatch, CountDownLatch endLatch) {
        this.workerId = workerId;
        this.startLatch = startLatch;
        this.endLatch = endLatch;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Worker " + workerId + " ready");
            startLatch.await(); // Wait for start signal
            
            System.out.println("Worker " + workerId + " working...");
            Thread.sleep(1000 + (int)(Math.random() * 1000));
            System.out.println("Worker " + workerId + " completed");
            
            endLatch.countDown(); // Signal completion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Service Startup Coordination ===");
        testServiceStartup();
        
        System.out.println("\n=== Race Start Simulation ===");
        testRaceStart();
        
        System.out.println("\n=== Parallel Computation ===");
        testParallelComputation();
        
        System.out.println("\n=== Timeout Example ===");
        testTimeout();
    }
    
    private static void testServiceStartup() throws InterruptedException {
        int serviceCount = 3;
        CountDownLatch latch = new CountDownLatch(serviceCount);
        
        new Thread(new Service("Database", latch, 2000)).start();
        new Thread(new Service("Cache", latch, 1000)).start();
        new Thread(new Service("MessageQueue", latch, 1500)).start();
        
        System.out.println("Waiting for all services to start...");
        latch.await(); // Wait for all services
        System.out.println("All services started. Application ready!");
    }
    
    private static void testRaceStart() throws InterruptedException {
        int workerCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1); // Start signal
        CountDownLatch endLatch = new CountDownLatch(workerCount); // Completion signal
        
        // Create workers
        for (int i = 1; i <= workerCount; i++) {
            new Thread(new Worker(i, startLatch, endLatch)).start();
        }
        
        Thread.sleep(1000); // Let workers get ready
        System.out.println("Starting all workers...");
        startLatch.countDown(); // Release all workers simultaneously
        
        endLatch.await(); // Wait for all workers to complete
        System.out.println("All workers completed");
    }
    
    private static void testParallelComputation() throws InterruptedException {
        int taskCount = 4;
        CountDownLatch latch = new CountDownLatch(taskCount);
        long[] results = new long[taskCount];
        
        for (int i = 0; i < taskCount; i++) {
            final int index = i;
            new Thread(() -> {
                results[index] = computeSum(index * 1000, (index + 1) * 1000);
                System.out.println("Task " + index + " completed");
                latch.countDown();
            }).start();
        }
        
        latch.await(); // Wait for all computations
        
        long totalSum = 0;
        for (long result : results) {
            totalSum += result;
        }
        System.out.println("Total sum: " + totalSum);
    }
    
    private static long computeSum(int start, int end) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += i;
        }
        return sum;
    }
    
    private static void testTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Task 1 completed");
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Takes too long
                System.out.println("Task 2 completed");
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        if (completed) {
            System.out.println("All tasks completed within timeout");
        } else {
            System.out.println("Timeout! Not all tasks completed");
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for countDown() and await() operations

**Space Complexity**: O(1) - CountDownLatch uses constant space

## Edge Cases and Pitfalls

- **Cannot be reused**: CountDownLatch is one-time use. Once count reaches zero, it cannot be reset. Use CyclicBarrier if you need reusability.
- **Counting down too many times**: Calling countDown() more times than the initial count has no effect after reaching zero.
- **Forgetting to count down**: If any thread forgets to call countDown(), waiting threads will block forever. Always use try-finally to ensure countDown() is called.
- **Negative count**: CountDownLatch cannot be initialized with a negative count. It throws IllegalArgumentException.

## Interview-Ready Answer

"CountDownLatch is a synchronization aid that allows one or more threads to wait until a set of operations completes. It's initialized with a count, and threads call await() to wait until the count reaches zero. Other threads call countDown() to decrement the count. Once zero is reached, all waiting threads are released. It's useful for coordinating service startup, parallel computations, or testing. Unlike CyclicBarrier, CountDownLatch is one-time use and cannot be reset."
