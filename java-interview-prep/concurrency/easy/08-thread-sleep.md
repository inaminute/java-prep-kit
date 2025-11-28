# Thread Sleep

## Problem Statement

Explain and demonstrate the Thread.sleep() method in Java. Show how sleep() pauses thread execution for a specified duration and discuss its use cases, behavior, and important considerations. Include examples of proper exception handling and common patterns using sleep().

**Input**: Time duration in milliseconds

**Output**: Thread pauses for specified duration

**Constraints**: 
- Must demonstrate sleep() usage
- Should explain InterruptedException
- Must show practical use cases

## Approach

- Thread.sleep(millis) pauses the current thread for the specified time
- Sleep is a static method that always affects the current thread
- The thread releases CPU but retains all locks it holds
- Sleep throws InterruptedException if the thread is interrupted
- Actual sleep time may be longer due to system scheduling
- Use sleep for delays, polling, and rate limiting

## Solution

```java
// Example 1: Basic sleep usage
class BasicSleepDemo {
    public static void demonstrate() {
        System.out.println("Starting at: " + System.currentTimeMillis());
        
        try {
            Thread.sleep(2000); // Sleep for 2 seconds
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted");
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
        
        System.out.println("Ending at: " + System.currentTimeMillis());
    }
}

// Example 2: Sleep in a loop (polling pattern)
class PollingDemo {
    private volatile boolean dataReady = false;
    
    public void waitForData() {
        System.out.println("Waiting for data...");
        
        while (!dataReady) {
            try {
                Thread.sleep(100); // Poll every 100ms
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting");
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        System.out.println("Data is ready!");
    }
    
    public void setDataReady() {
        dataReady = true;
    }
}

// Example 3: Countdown timer
class CountdownTimer {
    public void countdown(int seconds) {
        System.out.println("Countdown starting...");
        
        for (int i = seconds; i > 0; i--) {
            System.out.println(i + "...");
            try {
                Thread.sleep(1000); // 1 second delay
            } catch (InterruptedException e) {
                System.out.println("Countdown interrupted");
                return;
            }
        }
        
        System.out.println("Time's up!");
    }
}

// Example 4: Rate limiting
class RateLimiter {
    private int requestsPerSecond;
    
    public RateLimiter(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }
    
    public void processRequests(int totalRequests) {
        long delayMillis = 1000 / requestsPerSecond;
        
        for (int i = 1; i <= totalRequests; i++) {
            System.out.println("Processing request " + i);
            
            if (i < totalRequests) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    System.out.println("Rate limiter interrupted");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

// Example 5: Sleep doesn't release locks
class SleepWithLock {
    private final Object lock = new Object();
    
    public void demonstrateLockRetention() {
        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Thread 1: Acquired lock");
                try {
                    Thread.sleep(2000); // Holds lock while sleeping
                    System.out.println("Thread 1: Woke up, still has lock");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 1: Releasing lock");
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(500); // Ensure thread1 gets lock first
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.println("Thread 2: Trying to acquire lock...");
            synchronized (lock) {
                System.out.println("Thread 2: Acquired lock");
            }
        });
        
        thread1.start();
        thread2.start();
    }
}

// Example 6: Animated progress indicator
class ProgressIndicator {
    public void showProgress(int durationSeconds) {
        String[] frames = {"|", "/", "-", "\\"};
        int frameIndex = 0;
        long endTime = System.currentTimeMillis() + (durationSeconds * 1000);
        
        System.out.print("Processing: ");
        
        while (System.currentTimeMillis() < endTime) {
            System.out.print("\r" + frames[frameIndex] + " Processing...");
            frameIndex = (frameIndex + 1) % frames.length;
            
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("\nProgress interrupted");
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        System.out.println("\rDone!            ");
    }
}

// Main demonstration
public class ThreadSleepDemo {
    public static void main(String[] args) {
        System.out.println("=== Basic Sleep ===");
        BasicSleepDemo.demonstrate();
        
        System.out.println("\n=== Countdown Timer ===");
        CountdownTimer timer = new CountdownTimer();
        timer.countdown(3);
        
        System.out.println("\n=== Rate Limiter ===");
        RateLimiter limiter = new RateLimiter(2); // 2 requests per second
        limiter.processRequests(5);
        
        System.out.println("\n=== Sleep with Lock ===");
        SleepWithLock lockDemo = new SleepWithLock();
        lockDemo.demonstrateLockRetention();
    }
}

/*
 * IMPORTANT NOTES:
 * 
 * 1. Sleep is static - always affects current thread
 * 2. Sleep doesn't release locks - thread retains all monitors
 * 3. Sleep time is minimum - actual time may be longer
 * 4. InterruptedException must be handled
 * 5. Don't use sleep for synchronization - use wait/notify instead
 */
```

## Complexity Analysis

**Time Complexity**: O(1) - The sleep() call itself is constant time

**Space Complexity**: O(1) - No additional space required

## Edge Cases and Pitfalls

- **Sleep doesn't release locks**: Unlike wait(), sleep() doesn't release monitors, which can cause other threads to block unnecessarily. Use wait() if you need to release locks.
- **Imprecise timing**: The actual sleep duration may be longer than specified due to system scheduling and thread priorities. Don't rely on sleep() for precise timing.
- **Calling on wrong thread**: Thread.sleep() is static and always affects the current thread, even if called on a thread object like thread.sleep(). This is confusing and should be avoided.
- **Ignoring InterruptedException**: Always handle InterruptedException properly. Either propagate it or restore the interrupt status with Thread.currentThread().interrupt().

## Interview-Ready Answer

"Thread.sleep(millis) pauses the current thread for the specified duration in milliseconds. It's a static method that throws InterruptedException if the thread is interrupted. Sleep is useful for delays, polling, and rate limiting, but it doesn't release locks held by the thread. The actual sleep time may be longer than specified due to system scheduling. For synchronization purposes, use wait() instead of sleep() as it releases locks."
