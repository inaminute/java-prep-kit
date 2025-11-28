# Print in Order

## Problem Statement

Three threads are started simultaneously, but they must print their output in a specific order. Thread A prints "first", Thread B prints "second", and Thread C prints "third". Ensure they always print in the correct order regardless of thread scheduling. Implement multiple solutions using different synchronization mechanisms.

**Input**: Three threads starting simultaneously

**Output**: "first", "second", "third" printed in order

**Constraints**: 
- Threads start simultaneously
- Must print in correct order
- Should use proper synchronization

## Approach

- Use synchronization primitives to coordinate thread execution
- Solutions: CountDownLatch, Semaphore, wait/notify, AtomicInteger
- Each thread waits for previous thread to complete
- Signal next thread after completing own task
- Ensure proper ordering without busy-waiting

## Solution

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Solution 1: Using CountDownLatch
class PrintInOrderLatch {
    private CountDownLatch latch1 = new CountDownLatch(1);
    private CountDownLatch latch2 = new CountDownLatch(1);
    
    public void first() {
        System.out.print("first");
        latch1.countDown();
    }
    
    public void second() throws InterruptedException {
        latch1.await();
        System.out.print("second");
        latch2.countDown();
    }
    
    public void third() throws InterruptedException {
        latch2.await();
        System.out.print("third");
    }
}

// Solution 2: Using Semaphore
class PrintInOrderSemaphore {
    private Semaphore sem1 = new Semaphore(0);
    private Semaphore sem2 = new Semaphore(0);
    
    public void first() {
        System.out.print("first");
        sem1.release();
    }
    
    public void second() throws InterruptedException {
        sem1.acquire();
        System.out.print("second");
        sem2.release();
    }
    
    public void third() throws InterruptedException {
        sem2.acquire();
        System.out.print("third");
    }
}

// Solution 3: Using wait/notify
class PrintInOrderWaitNotify {
    private int order = 1;
    private final Object lock = new Object();
    
    public void first() {
        synchronized (lock) {
            System.out.print("first");
            order = 2;
            lock.notifyAll();
        }
    }
    
    public void second() throws InterruptedException {
        synchronized (lock) {
            while (order != 2) {
                lock.wait();
            }
            System.out.print("second");
            order = 3;
            lock.notifyAll();
        }
    }
    
    public void third() throws InterruptedException {
        synchronized (lock) {
            while (order != 3) {
                lock.wait();
            }
            System.out.print("third");
        }
    }
}

// Solution 4: Using AtomicInteger
class PrintInOrderAtomic {
    private AtomicInteger counter = new AtomicInteger(1);
    
    public void first() {
        while (counter.get() != 1) {
            Thread.yield();
        }
        System.out.print("first");
        counter.incrementAndGet();
    }
    
    public void second() {
        while (counter.get() != 2) {
            Thread.yield();
        }
        System.out.print("second");
        counter.incrementAndGet();
    }
    
    public void third() {
        while (counter.get() != 3) {
            Thread.yield();
        }
        System.out.print("third");
    }
}

public class PrintInOrderDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CountDownLatch Solution ===");
        testLatchSolution();
        
        System.out.println("\n=== Semaphore Solution ===");
        testSemaphoreSolution();
        
        System.out.println("\n=== Wait/Notify Solution ===");
        testWaitNotifySolution();
        
        System.out.println("\n=== Atomic Solution ===");
        testAtomicSolution();
    }
    
    private static void testLatchSolution() throws InterruptedException {
        PrintInOrderLatch printer = new PrintInOrderLatch();
        
        Thread t1 = new Thread(() -> printer.first());
        Thread t2 = new Thread(() -> {
            try {
                printer.second();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread t3 = new Thread(() -> {
            try {
                printer.third();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t3.start();
        t2.start();
        t1.start();
        
        t1.join();
        t2.join();
        t3.join();
        System.out.println();
    }
    
    private static void testSemaphoreSolution() throws InterruptedException {
        PrintInOrderSemaphore printer = new PrintInOrderSemaphore();
        
        Thread t1 = new Thread(() -> printer.first());
        Thread t2 = new Thread(() -> {
            try {
                printer.second();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread t3 = new Thread(() -> {
            try {
                printer.third();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t3.start();
        t2.start();
        t1.start();
        
        t1.join();
        t2.join();
        t3.join();
        System.out.println();
    }
    
    private static void testWaitNotifySolution() throws InterruptedException {
        PrintInOrderWaitNotify printer = new PrintInOrderWaitNotify();
        
        Thread t1 = new Thread(() -> printer.first());
        Thread t2 = new Thread(() -> {
            try {
                printer.second();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread t3 = new Thread(() -> {
            try {
                printer.third();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        t3.start();
        t2.start();
        t1.start();
        
        t1.join();
        t2.join();
        t3.join();
        System.out.println();
    }
    
    private static void testAtomicSolution() throws InterruptedException {
        PrintInOrderAtomic printer = new PrintInOrderAtomic();
        
        Thread t1 = new Thread(() -> printer.first());
        Thread t2 = new Thread(() -> printer.second());
        Thread t3 = new Thread(() -> printer.third());
        
        t3.start();
        t2.start();
        t1.start();
        
        t1.join();
        t2.join();
        t3.join();
        System.out.println();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) per thread operation

**Space Complexity**: O(1) for synchronization primitives

## Edge Cases and Pitfalls

- **Busy-waiting**: AtomicInteger solution uses busy-waiting which wastes CPU. Blocking solutions are more efficient.
- **Spurious wakeups**: wait/notify solution must use while loops to handle spurious wakeups.
- **Thread scheduling**: Even with synchronization, thread start order doesn't guarantee execution order without coordination.
- **Resource cleanup**: CountDownLatch cannot be reused. For multiple runs, create new instances.

## Interview-Ready Answer

"To ensure threads print in order, use synchronization primitives like CountDownLatch, Semaphore, or wait/notify. CountDownLatch is cleanest: each thread waits on a latch that the previous thread counts down. Semaphore works similarly with acquire/release. The wait/notify solution uses a shared counter and condition waiting. Avoid busy-waiting solutions as they waste CPU. The key is making each thread wait for a signal from the previous thread before proceeding."
