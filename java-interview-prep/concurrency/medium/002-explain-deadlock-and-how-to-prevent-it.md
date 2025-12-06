# Explain deadlock and how to prevent it

## Problem Statement

Describe what causes deadlock in multithreaded programs and strategies to prevent or detect it.

## Approach

- **Circular wait**: Threads waiting for each other's resources
- **Four necessary conditions**: Mutual exclusion, hold and wait, no preemption, circular wait
- **Prevention strategies**: Lock ordering, timeouts, deadlock detection
- **Avoidance**: Careful resource allocation
- **Detection**: Monitor thread states and resource graphs

## Solution

```java
// Example of deadlock
public class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {
            System.out.println(Thread.currentThread().getName() + ": Holding lock1...");
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            System.out.println(Thread.currentThread().getName() + ": Waiting for lock2...");
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + ": Holding lock1 & lock2");
            }
        }
    }
    
    public void method2() {
        synchronized (lock2) {
            System.out.println(Thread.currentThread().getName() + ": Holding lock2...");
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            System.out.println(Thread.currentThread().getName() + ": Waiting for lock1...");
            synchronized (lock1) {
                System.out.println(Thread.currentThread().getName() + ": Holding lock2 & lock1");
            }
        }
    }
    
    public static void main(String[] args) {
        DeadlockExample deadlock = new DeadlockExample();
        
        Thread t1 = new Thread(() -> deadlock.method1(), "Thread-1");
        Thread t2 = new Thread(() -> deadlock.method2(), "Thread-2");
        
        t1.start();
        t2.start();
        // Deadlock occurs!
    }
}

// Solution 1: Lock ordering
class LockOrdering {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {
            System.out.println("Method1: Holding lock1");
            synchronized (lock2) {
                System.out.println("Method1: Holding lock1 & lock2");
            }
        }
    }
    
    public void method2() {
        // Same order as method1 - prevents deadlock
        synchronized (lock1) {
            System.out.println("Method2: Holding lock1");
            synchronized (lock2) {
                System.out.println("Method2: Holding lock1 & lock2");
            }
        }
    }
}

// Solution 2: Using tryLock with timeout
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

class TryLockSolution {
    private final Lock lock1 = new ReentrantLock();
    private final Lock lock2 = new ReentrantLock();
    
    public void method1() {
        try {
            if (lock1.tryLock(1000, TimeUnit.MILLISECONDS)) {
                try {
                    if (lock2.tryLock(1000, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println("Method1: Got both locks");
                        } finally {
                            lock2.unlock();
                        }
                    } else {
                        System.out.println("Method1: Couldn't get lock2, releasing lock1");
                    }
                } finally {
                    lock1.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Solution 3: Lock-free approach using atomic operations
import java.util.concurrent.atomic.AtomicInteger;

class LockFreeCounter {
    private AtomicInteger counter = new AtomicInteger(0);
    
    public void increment() {
        counter.incrementAndGet(); // No locks needed
    }
    
    public int get() {
        return counter.get();
    }
}
```

## Four Conditions for Deadlock

All four must be present for deadlock to occur:

1. **Mutual Exclusion**: Resources cannot be shared
2. **Hold and Wait**: Thread holds resources while waiting for others
3. **No Preemption**: Resources cannot be forcibly taken
4. **Circular Wait**: Circular chain of threads waiting for resources

## Prevention Strategies

### 1. Lock Ordering
- Always acquire locks in the same order
- Prevents circular wait condition

### 2. Lock Timeout
- Use tryLock() with timeout
- Release locks if can't acquire all needed resources

### 3. Deadlock Detection
- Monitor thread states
- Use thread dumps to identify deadlocks
- JVM can detect deadlocks via ThreadMXBean

### 4. Avoid Nested Locks
- Minimize holding multiple locks
- Use lock-free data structures when possible

### 5. Use Higher-Level Concurrency Utilities
- ExecutorService, ConcurrentHashMap, etc.
- Built-in deadlock prevention

## Deadlock Detection

```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;

class DeadlockDetector {
    public static void detectDeadlock() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        
        if (deadlockedThreads != null) {
            System.out.println("Deadlock detected!");
            ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads);
            for (ThreadInfo info : threadInfos) {
                System.out.println("Thread: " + info.getThreadName());
                System.out.println("Locked on: " + info.getLockName());
            }
        }
    }
}
```

## Edge Cases and Pitfalls

- **Livelock**: Threads keep changing state in response to each other without progress
- **Resource starvation**: Thread never gets resources due to priority issues
- **Hidden deadlocks**: Deadlocks in third-party libraries
- **Common Pitfall**: Inconsistent lock ordering across different methods

## Interview-Ready Answer

"Deadlock occurs when two or more threads are blocked forever, each waiting for resources held by others, forming a circular dependency. It requires four conditions: mutual exclusion, hold and wait, no preemption, and circular wait. Prevention strategies include consistent lock ordering, using tryLock with timeouts, avoiding nested locks, and using higher-level concurrency utilities that handle synchronization internally."

**Tags**: deadlock, prevention, concurrency-issues
