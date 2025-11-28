# Deadlock Detection and Prevention

## Problem Statement

Implement a deadlock detection mechanism and demonstrate various deadlock prevention strategies in Java. Show how to detect deadlocks using ThreadMXBean, implement timeout-based deadlock prevention, and use lock ordering to avoid deadlocks. Include examples of deadlock scenarios and their solutions.

**Input**: Multiple threads acquiring multiple locks

**Output**: Deadlock detection and prevention mechanisms

**Constraints**: 
- Must demonstrate deadlock scenario
- Should implement detection mechanism
- Must show prevention strategies

## Approach

- Deadlock occurs when threads wait for each other's locks in a cycle
- Use ThreadMXBean to detect deadlocks programmatically
- Prevention strategies: lock ordering, timeout-based locking, tryLock
- Always acquire locks in consistent order across all threads
- Use ReentrantLock with tryLock for timeout-based prevention
- Implement resource allocation graph for detection
- Avoid nested locks when possible

## Solution

```java
import java.lang.management.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

// Deadlock scenario
class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {
            System.out.println(Thread.currentThread().getName() + " acquired lock1");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + " acquired lock2");
            }
        }
    }
    
    public void method2() {
        synchronized (lock2) {
            System.out.println(Thread.currentThread().getName() + " acquired lock2");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            synchronized (lock1) {
                System.out.println(Thread.currentThread().getName() + " acquired lock1");
            }
        }
    }
}

// Deadlock detector
class DeadlockDetector {
    public static void detectDeadlock() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        
        if (deadlockedThreads != null) {
            System.out.println("Deadlock detected!");
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreads);
            
            for (ThreadInfo threadInfo : threadInfos) {
                System.out.println("Thread: " + threadInfo.getThreadName());
                System.out.println("State: " + threadInfo.getThreadState());
                System.out.println("Locked on: " + threadInfo.getLockName());
                System.out.println("Locked by: " + threadInfo.getLockOwnerName());
                System.out.println();
            }
        } else {
            System.out.println("No deadlock detected");
        }
    }
}

// Prevention using lock ordering
class LockOrderingExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {
            System.out.println(Thread.currentThread().getName() + " acquired lock1");
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + " acquired lock2");
            }
        }
    }
    
    public void method2() {
        // Same lock order as method1 - prevents deadlock
        synchronized (lock1) {
            System.out.println(Thread.currentThread().getName() + " acquired lock1");
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + " acquired lock2");
            }
        }
    }
}

// Prevention using tryLock with timeout
class TimeoutBasedPrevention {
    private final ReentrantLock lock1 = new ReentrantLock();
    private final ReentrantLock lock2 = new ReentrantLock();
    
    public boolean transfer(int amount) {
        boolean lock1Acquired = false;
        boolean lock2Acquired = false;
        
        try {
            lock1Acquired = lock1.tryLock(1, TimeUnit.SECONDS);
            if (!lock1Acquired) {
                System.out.println("Could not acquire lock1");
                return false;
            }
            
            lock2Acquired = lock2.tryLock(1, TimeUnit.SECONDS);
            if (!lock2Acquired) {
                System.out.println("Could not acquire lock2");
                return false;
            }
            
            // Perform transfer
            System.out.println(Thread.currentThread().getName() + " performing transfer");
            Thread.sleep(100);
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock2Acquired) lock2.unlock();
            if (lock1Acquired) lock1.unlock();
        }
    }
}

public class DeadlockDetectionDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Deadlock Scenario ===");
        testDeadlock();
        
        System.out.println("\n=== Lock Ordering Prevention ===");
        testLockOrdering();
        
        System.out.println("\n=== Timeout-Based Prevention ===");
        testTimeoutPrevention();
    }
    
    private static void testDeadlock() throws InterruptedException {
        DeadlockExample example = new DeadlockExample();
        
        Thread t1 = new Thread(() -> example.method1(), "Thread-1");
        Thread t2 = new Thread(() -> example.method2(), "Thread-2");
        
        t1.start();
        t2.start();
        
        Thread.sleep(2000);
        DeadlockDetector.detectDeadlock();
    }
    
    private static void testLockOrdering() throws InterruptedException {
        LockOrderingExample example = new LockOrderingExample();
        
        Thread t1 = new Thread(() -> example.method1(), "Thread-1");
        Thread t2 = new Thread(() -> example.method2(), "Thread-2");
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        System.out.println("Completed without deadlock");
    }
    
    private static void testTimeoutPrevention() throws InterruptedException {
        TimeoutBasedPrevention prevention = new TimeoutBasedPrevention();
        
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    prevention.transfer(100);
                }
            }, "Thread-" + i);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) for deadlock detection where n is number of threads

**Space Complexity**: O(n) for storing thread information

## Edge Cases and Pitfalls

- **Circular wait**: Deadlock requires circular wait condition. Breaking any condition prevents deadlock.
- **Detection overhead**: Continuous deadlock detection adds overhead. Use periodic checks or on-demand detection.
- **False positives**: Some detection algorithms may report false positives for legitimate waiting scenarios.
- **Recovery complexity**: Detecting deadlock is easier than recovering from it. Prevention is preferred over detection and recovery.

## Interview-Ready Answer

"Deadlock occurs when threads wait for each other's locks in a cycle. Detection can be done using ThreadMXBean.findDeadlockedThreads(). Prevention strategies include: consistent lock ordering (always acquire locks in same order), timeout-based locking with tryLock(), and avoiding nested locks. Lock ordering is the most reliable prevention method. Use ReentrantLock with tryLock() for timeout-based prevention. Always design systems to avoid deadlock rather than relying on detection and recovery."
