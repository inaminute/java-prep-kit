# What is the join() method?

## Problem Statement

Explain the purpose of Thread.join() and how it's used to wait for thread completion.

## Approach

- **Thread coordination**: Wait for another thread to complete
- **Blocking operation**: Calling thread enters WAITING state
- **Timeout variants**: join(), join(millis), join(millis, nanos)
- **Sequential execution**: Ensures threads complete in specific order
- **InterruptedException**: Must handle interruption

## Solution

```java
public class JoinDemo {
    
    // Basic join() example
    static class Task implements Runnable {
        private String name;
        private int duration;
        
        public Task(String name, int duration) {
            this.name = name;
            this.duration = duration;
        }
        
        @Override
        public void run() {
            System.out.println(name + " started");
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " completed");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Task("Task-1", 2000));
        Thread t2 = new Thread(new Task("Task-2", 1000));
        Thread t3 = new Thread(new Task("Task-3", 1500));
        
        System.out.println("Starting threads...");
        t1.start();
        t2.start();
        t3.start();
        
        // Wait for all threads to complete
        t1.join(); // Main thread waits for t1
        System.out.println("Task-1 joined");
        
        t2.join(); // Main thread waits for t2
        System.out.println("Task-2 joined");
        
        t3.join(); // Main thread waits for t3
        System.out.println("Task-3 joined");
        
        System.out.println("All threads completed");
    }
}

// join() with timeout
class JoinWithTimeout {
    public static void main(String[] args) throws InterruptedException {
        Thread longTask = new Thread(() -> {
            try {
                System.out.println("Long task started");
                Thread.sleep(5000);
                System.out.println("Long task completed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        longTask.start();
        
        // Wait for maximum 2 seconds
        longTask.join(2000);
        
        if (longTask.isAlive()) {
            System.out.println("Task still running after timeout");
        } else {
            System.out.println("Task completed within timeout");
        }
    }
}

// Sequential execution using join()
class SequentialExecution {
    static class DownloadTask implements Runnable {
        private String file;
        
        public DownloadTask(String file) {
            this.file = file;
        }
        
        @Override
        public void run() {
            System.out.println("Downloading " + file);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Downloaded " + file);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        // Download files sequentially
        Thread download1 = new Thread(new DownloadTask("file1.txt"));
        Thread download2 = new Thread(new DownloadTask("file2.txt"));
        Thread download3 = new Thread(new DownloadTask("file3.txt"));
        
        download1.start();
        download1.join(); // Wait for file1 before starting file2
        
        download2.start();
        download2.join(); // Wait for file2 before starting file3
        
        download3.start();
        download3.join(); // Wait for file3 to complete
        
        System.out.println("All downloads completed");
    }
}

// Parallel execution with join()
class ParallelExecution {
    static class ProcessTask implements Runnable {
        private int id;
        
        public ProcessTask(int id) {
            this.id = id;
        }
        
        @Override
        public void run() {
            System.out.println("Processing task " + id);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Completed task " + id);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        // Start all tasks in parallel
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(new ProcessTask(i));
            threads[i].start();
        }
        
        // Wait for all to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("All tasks completed");
    }
}

// Handling InterruptedException
class JoinInterruption {
    public static void main(String[] args) {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Worker interrupted");
            }
        });
        
        Thread waiter = new Thread(() -> {
            try {
                worker.start();
                System.out.println("Waiting for worker...");
                worker.join(); // This can be interrupted
                System.out.println("Worker completed");
            } catch (InterruptedException e) {
                System.out.println("Waiter interrupted while joining");
            }
        });
        
        waiter.start();
        
        try {
            Thread.sleep(1000);
            waiter.interrupt(); // Interrupt the waiting thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## How join() Works

1. **Calling thread blocks**: Thread calling join() enters WAITING state
2. **Waits for target thread**: Blocks until target thread terminates
3. **Returns on completion**: Resumes execution after target thread dies
4. **Can be interrupted**: Throws InterruptedException if interrupted

## join() Variants

| Method | Description |
|--------|-------------|
| `join()` | Wait indefinitely for thread to die |
| `join(long millis)` | Wait at most millis milliseconds |
| `join(long millis, int nanos)` | Wait with nanosecond precision |

## Common Use Cases

1. **Wait for completion**: Ensure worker threads finish before proceeding
2. **Sequential execution**: Force specific execution order
3. **Parallel processing**: Start all threads, then wait for all to complete
4. **Resource cleanup**: Wait for threads before closing resources

## Edge Cases and Pitfalls

- **Calling join() on itself**: Thread calling join() on itself will wait forever (deadlock)
- **Joining already terminated thread**: Returns immediately
- **InterruptedException**: Must handle or propagate this checked exception
- **Common Pitfall**: Forgetting to call join() and proceeding before threads complete

## Interview-Ready Answer

"The join() method allows one thread to wait for another thread to complete execution. When a thread calls join() on another thread, it enters the WAITING state until the target thread terminates. It's commonly used to ensure worker threads complete before the main thread proceeds, and can accept a timeout parameter to avoid waiting indefinitely."

**Tags**: join, thread-coordination, synchronization
