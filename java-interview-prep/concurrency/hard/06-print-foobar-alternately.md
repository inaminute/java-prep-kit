# Print FooBar Alternately

## Problem Statement

Two threads are given: one prints "foo" and another prints "bar". Ensure they print alternately to produce "foobarfoobarfoobar..." for n times. Implement multiple solutions using different synchronization mechanisms to coordinate the threads.

**Input**: Number of times to print (n)

**Output**: "foobar" printed n times with alternating threads

**Constraints**: 
- Must alternate between foo and bar
- Should print exactly n times
- Must use proper synchronization

## Approach

- Use synchronization to ensure alternation
- Solutions: Semaphore, wait/notify, ReentrantLock with Condition
- Foo thread signals bar thread after printing
- Bar thread signals foo thread after printing
- Use flags or counters to track whose turn it is

## Solution

```java
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Solution 1: Using Semaphores
class FooBarSemaphore {
    private int n;
    private Semaphore fooSem = new Semaphore(1);
    private Semaphore barSem = new Semaphore(0);
    
    public FooBarSemaphore(int n) {
        this.n = n;
    }
    
    public void foo() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            fooSem.acquire();
            System.out.print("foo");
            barSem.release();
        }
    }
    
    public void bar() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            barSem.acquire();
            System.out.print("bar");
            fooSem.release();
        }
    }
}

// Solution 2: Using wait/notify
class FooBarWaitNotify {
    private int n;
    private boolean fooTurn = true;
    private final Object lock = new Object();
    
    public FooBarWaitNotify(int n) {
        this.n = n;
    }
    
    public void foo() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                while (!fooTurn) {
                    lock.wait();
                }
                System.out.print("foo");
                fooTurn = false;
                lock.notifyAll();
            }
        }
    }
    
    public void bar() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                while (fooTurn) {
                    lock.wait();
                }
                System.out.print("bar");
                fooTurn = true;
                lock.notifyAll();
            }
        }
    }
}

// Solution 3: Using ReentrantLock and Condition
class FooBarLockCondition {
    private int n;
    private boolean fooTurn = true;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    
    public FooBarLockCondition(int n) {
        this.n = n;
    }
    
    public void foo() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            lock.lock();
            try {
                while (!fooTurn) {
                    condition.await();
                }
                System.out.print("foo");
                fooTurn = false;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
    
    public void bar() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            lock.lock();
            try {
                while (fooTurn) {
                    condition.await();
                }
                System.out.print("bar");
                fooTurn = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}

public class FooBarAlternatelyDemo {
    public static void main(String[] args) throws InterruptedException {
        int n = 5;
        
        System.out.println("=== Semaphore Solution ===");
        testSemaphoreSolution(n);
        
        System.out.println("\n=== Wait/Notify Solution ===");
        testWaitNotifySolution(n);
        
        System.out.println("\n=== Lock/Condition Solution ===");
        testLockConditionSolution(n);
    }
    
    private static void testSemaphoreSolution(int n) throws InterruptedException {
        FooBarSemaphore fooBar = new FooBarSemaphore(n);
        
        Thread t1 = new Thread(() -> {
            try {
                fooBar.foo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread t2 = new Thread(() -> {
            try {
                fooBar.bar();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        System.out.println();
    }
    
    private static void testWaitNotifySolution(int n) throws InterruptedException {
        FooBarWaitNotify fooBar = new FooBarWaitNotify(n);
        
        Thread t1 = new Thread(() -> {
            try {
                fooBar.foo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread t2 = new Thread(() -> {
            try {
                fooBar.bar();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        System.out.println();
    }
    
    private static void testLockConditionSolution(int n) throws InterruptedException {
        FooBarLockCondition fooBar = new FooBarLockCondition(n);
        
        Thread t1 = new Thread(() -> {
            try {
                fooBar.foo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread t2 = new Thread(() -> {
            try {
                fooBar.bar();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        System.out.println();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) where n is the number of iterations

**Space Complexity**: O(1) for synchronization primitives

## Edge Cases and Pitfalls

- **Initial state**: Ensure foo goes first by initializing semaphore/flag appropriately.
- **Spurious wakeups**: Use while loops in wait/notify solution to handle spurious wakeups.
- **Deadlock prevention**: Ensure proper signaling after each print to avoid deadlock.
- **Thread safety**: All shared state must be properly synchronized.

## Interview-Ready Answer

"To print foo and bar alternately, use synchronization primitives to coordinate turns. The semaphore solution uses two semaphores: foo starts with permit 1, bar with 0. After printing, each releases the other's semaphore. The wait/notify solution uses a boolean flag to track whose turn it is, with threads waiting when it's not their turn. ReentrantLock with Condition provides similar functionality with explicit lock management. All solutions ensure strict alternation through proper signaling between threads."
