# Thread Join

## Problem Statement

Explain and demonstrate the join() method in Java threads. Show how join() allows one thread to wait for another thread to complete before continuing execution. Include examples with and without join() to illustrate the difference, and demonstrate the timeout variants of join().

**Input**: Multiple threads with dependencies

**Output**: Coordinated thread execution

**Constraints**: 
- Must show join() usage
- Should demonstrate execution order with and without join()
- Must explain timeout variants

## Approach

- The join() method makes the calling thread wait until the target thread completes
- Without join(), threads execute independently and may finish in any order
- join() is useful when one thread depends on another thread's results
- join(long millis) waits for at most the specified time
- join() throws InterruptedException which must be handled
- Multiple threads can join on the same thread

## Solution

```java
// Example 1: Without join() - unpredictable order
class WithoutJoinDemo {
    public static void demonstrate() {
        Thread thread1 = new Thread(() -> {
            System.out.println("Thread 1 starting");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread 1 completed");
        });
        
        Thread thread2 = new Thread(() -> {
            System.out.println("Thread 2 starting");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread 2 completed");
        });
        
        thread1.start();
        thread2.start();
        
        // Main thread continues immediately
        System.out.println("Main thread finished (threads may still be running)");
    }
}

// Example 2: With join() - guaranteed order
class WithJoinDemo {
    public static void demonstrate() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            System.out.println("Thread 1 starting");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread 1 completed");
        });
        
        Thread thread2 = new Thread(() -> {
            System.out.println("Thread 2 starting");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread 2 completed");
        });
        
        thread1.start();
        thread2.start();
        
        // Wait for both threads to complete
        thread1.join();
        thread2.join();
        
        System.out.println("Main thread finished (all threads completed)");
    }
}

// Example 3: Join with timeout
class JoinWithTimeoutDemo {
    public static void demonstrate() throws InterruptedException {
        Thread longRunningThread = new Thread(() -> {
            System.out.println("Long running thread started");
            try {
                Thread.sleep(5000); // 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Long running thread completed");
        });
        
        longRunningThread.start();
        
        // Wait for at most 2 seconds
        System.out.println("Waiting for thread (max 2 seconds)...");
        longRunningThread.join(2000);
        
        if (longRunningThread.isAlive()) {
            System.out.println("Thread still running after timeout");
        } else {
            System.out.println("Thread completed within timeout");
        }
    }
}

// Example 4: Sequential processing with join
class DataProcessor {
    private String data;
    
    public void processData() throws InterruptedException {
        // Step 1: Load data
        Thread loadThread = new Thread(() -> {
            System.out.println("Loading data...");
            try {
                Thread.sleep(1000);
                data = "Sample Data";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Data loaded: " + data);
        });
        
        loadThread.start();
        loadThread.join(); // Wait for data to load
        
        // Step 2: Process data (depends on step 1)
        Thread processThread = new Thread(() -> {
            System.out.println("Processing data: " + data);
            try {
                Thread.sleep(1000);
                data = data.toUpperCase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Data processed: " + data);
        });
        
        processThread.start();
        processThread.join(); // Wait for processing to complete
        
        // Step 3: Save data (depends on step 2)
        Thread saveThread = new Thread(() -> {
            System.out.println("Saving data: " + data);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Data saved successfully");
        });
        
        saveThread.start();
        saveThread.join(); // Wait for save to complete
        
        System.out.println("All processing completed");
    }
}

// Example 5: Parallel tasks with join
public class ThreadJoinDemo {
    public static void main(String[] args) {
        try {
            System.out.println("=== Without Join ===");
            WithoutJoinDemo.demonstrate();
            Thread.sleep(3000); // Wait to see output
            
            System.out.println("\n=== With Join ===");
            WithJoinDemo.demonstrate();
            
            System.out.println("\n=== Join with Timeout ===");
            JoinWithTimeoutDemo.demonstrate();
            
            System.out.println("\n=== Sequential Processing ===");
            DataProcessor processor = new DataProcessor();
            processor.processData();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Practical example: Parallel computation
class ParallelSum {
    public static long computeSum(int[] array) throws InterruptedException {
        int mid = array.length / 2;
        
        SumTask task1 = new SumTask(array, 0, mid);
        SumTask task2 = new SumTask(array, mid, array.length);
        
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        
        thread1.start();
        thread2.start();
        
        // Wait for both computations to complete
        thread1.join();
        thread2.join();
        
        return task1.getResult() + task2.getResult();
    }
    
    static class SumTask implements Runnable {
        private int[] array;
        private int start, end;
        private long result;
        
        public SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public void run() {
            result = 0;
            for (int i = start; i < end; i++) {
                result += array[i];
            }
        }
        
        public long getResult() {
            return result;
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - The join() call itself is constant time, though it blocks until the thread completes

**Space Complexity**: O(1) - No additional space required

## Edge Cases and Pitfalls

- **Joining on current thread**: Calling join() on the current thread causes deadlock as the thread waits for itself to complete.
- **Joining terminated thread**: Calling join() on an already terminated thread returns immediately, which is safe but may indicate logic errors.
- **InterruptedException handling**: join() throws InterruptedException which must be properly handled. Ignoring it can lead to incorrect program behavior.
- **Timeout doesn't stop thread**: join(timeout) returns after the timeout even if the thread is still running. Always check isAlive() after timeout to determine if the thread completed.

## Interview-Ready Answer

"The join() method allows one thread to wait for another thread to complete before continuing execution. When thread A calls threadB.join(), thread A blocks until thread B finishes. This is essential for coordinating dependent tasks and ensuring proper execution order. The method has timeout variants like join(millis) that wait for a maximum time. join() throws InterruptedException and should be used when you need to wait for thread completion before proceeding."
