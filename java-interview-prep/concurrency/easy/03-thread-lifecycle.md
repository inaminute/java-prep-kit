# Thread Lifecycle

## Problem Statement

Explain the complete lifecycle of a thread in Java, including all possible states a thread can be in and the transitions between these states. Demonstrate with code how threads move through different states and how to check the current state of a thread.

**Input**: N/A (Demonstration of thread states)

**Output**: Thread state transitions and explanations

**Constraints**: 
- Must cover all thread states defined in Thread.State enum
- Should demonstrate state transitions with code
- Must explain what causes each state transition

## Approach

- Java threads have six distinct states defined in Thread.State enum
- Threads transition between states based on method calls and system events
- Understanding the lifecycle is crucial for debugging and thread management
- Use Thread.getState() to check current state
- Demonstrate each state with practical examples
- Explain blocking vs waiting states and their differences

## Solution

```java
public class ThreadLifecycleDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Lifecycle States ===\n");
        
        // 1. NEW - Thread created but not started
        Thread newThread = new Thread(() -> {
            System.out.println("Thread is running");
        });
        System.out.println("1. NEW state: " + newThread.getState());
        
        // 2. RUNNABLE - Thread is executing or ready to execute
        newThread.start();
        System.out.println("2. RUNNABLE state: " + newThread.getState());
        Thread.sleep(100); // Give it time to complete
        
        // 3. TERMINATED - Thread has completed execution
        System.out.println("3. TERMINATED state: " + newThread.getState());
        
        // Demonstrating TIMED_WAITING
        Thread timedWaitingThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        timedWaitingThread.start();
        Thread.sleep(100); // Ensure thread is sleeping
        System.out.println("4. TIMED_WAITING state: " + timedWaitingThread.getState());
        
        // Demonstrating WAITING
        Object lock = new Object();
        Thread waitingThread = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait(); // Wait indefinitely
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        waitingThread.start();
        Thread.sleep(100); // Ensure thread is waiting
        System.out.println("5. WAITING state: " + waitingThread.getState());
        
        // Demonstrating BLOCKED
        Object sharedLock = new Object();
        Thread blockingThread = new Thread(() -> {
            synchronized (sharedLock) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread blockedThread = new Thread(() -> {
            synchronized (sharedLock) {
                System.out.println("Got the lock");
            }
        });
        
        blockingThread.start();
        Thread.sleep(100); // Let first thread acquire lock
        blockedThread.start();
        Thread.sleep(100); // Let second thread try to acquire lock
        System.out.println("6. BLOCKED state: " + blockedThread.getState());
        
        // Cleanup
        synchronized (lock) {
            lock.notify();
        }
    }
}

// Comprehensive example showing state transitions
class ThreadStateMonitor {
    public static void monitorThreadStates() throws InterruptedException {
        Thread worker = new Thread(new WorkerTask());
        
        // State 1: NEW
        System.out.println("After creation: " + worker.getState());
        
        worker.start();
        
        // State 2: RUNNABLE
        Thread.sleep(100);
        System.out.println("After start: " + worker.getState());
        
        // Wait for completion
        worker.join();
        
        // State 3: TERMINATED
        System.out.println("After completion: " + worker.getState());
    }
    
    static class WorkerTask implements Runnable {
        @Override
        public void run() {
            try {
                // RUNNABLE state
                System.out.println("Working...");
                
                // TIMED_WAITING state
                Thread.sleep(1000);
                
                // Back to RUNNABLE
                System.out.println("Work completed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/*
 * THREAD STATES:
 * 
 * 1. NEW: Thread object created but start() not called yet
 *    - Created with new Thread()
 *    - Not yet scheduled for execution
 * 
 * 2. RUNNABLE: Thread is executing or ready to execute
 *    - After calling start()
 *    - May be running or waiting for CPU time
 *    - Includes both "ready" and "running" states
 * 
 * 3. BLOCKED: Thread is blocked waiting for a monitor lock
 *    - Trying to enter synchronized block/method
 *    - Another thread holds the lock
 *    - Will become RUNNABLE when lock is acquired
 * 
 * 4. WAITING: Thread is waiting indefinitely for another thread
 *    - Called Object.wait() without timeout
 *    - Called Thread.join() without timeout
 *    - Called LockSupport.park()
 * 
 * 5. TIMED_WAITING: Thread is waiting for a specified time
 *    - Called Thread.sleep(time)
 *    - Called Object.wait(timeout)
 *    - Called Thread.join(timeout)
 * 
 * 6. TERMINATED: Thread has completed execution
 *    - run() method completed normally
 *    - run() method threw an exception
 *    - Cannot be restarted
 */
```

## Complexity Analysis

**Time Complexity**: O(1) - State transitions are constant time operations

**Space Complexity**: O(1) - Thread state information is stored in fixed-size fields

## Edge Cases and Pitfalls

- **Cannot restart terminated threads**: Once a thread reaches TERMINATED state, calling start() again throws IllegalThreadStateException. Create a new thread if you need to run the task again.
- **BLOCKED vs WAITING confusion**: BLOCKED means waiting for a monitor lock (synchronized), while WAITING means explicitly waiting via wait(), join(), or park() methods.
- **RUNNABLE doesn't mean running**: A thread in RUNNABLE state might be waiting for CPU time from the OS scheduler, not actively executing.
- **Race conditions in state checking**: The state returned by getState() is a snapshot and may change immediately after the call, so don't rely on it for synchronization logic.

## Interview-Ready Answer

"A Java thread goes through six states: NEW (created but not started), RUNNABLE (executing or ready to execute), BLOCKED (waiting for a monitor lock), WAITING (waiting indefinitely for another thread), TIMED_WAITING (waiting for a specific time), and TERMINATED (completed execution). Threads transition between states through method calls like start(), sleep(), wait(), and notify(). Understanding these states is crucial for debugging concurrency issues and managing thread behavior effectively."
