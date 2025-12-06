# Difference between sleep() and wait()

## Problem Statement

Compare Thread.sleep() and Object.wait() methods in terms of lock release and usage context.

## Approach

- **Lock behavior**: sleep() retains lock, wait() releases lock
- **Method type**: sleep() is static, wait() is instance method
- **Usage context**: sleep() anywhere, wait() requires synchronized context
- **Purpose**: sleep() for timed pause, wait() for inter-thread communication
- **Waking up**: sleep() wakes after timeout, wait() needs notify()

## Solution

```java
public class SleepVsWaitDemo {
    private static final Object lock = new Object();
    
    // Demonstrating sleep() - DOES NOT release lock
    static class SleepExample implements Runnable {
        @Override
        public void run() {
            synchronized(lock) {
                System.out.println(Thread.currentThread().getName() + 
                    " acquired lock, going to sleep");
                try {
                    Thread.sleep(2000); // Holds the lock while sleeping
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + 
                    " woke up, releasing lock");
            }
        }
    }
    
    // Demonstrating wait() - RELEASES lock
    static class WaitExample implements Runnable {
        @Override
        public void run() {
            synchronized(lock) {
                System.out.println(Thread.currentThread().getName() + 
                    " acquired lock, going to wait");
                try {
                    lock.wait(2000); // Releases the lock while waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + 
                    " woke up, reacquired lock");
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Testing sleep() ===");
        Thread t1 = new Thread(new SleepExample(), "Thread-1");
        Thread t2 = new Thread(new SleepExample(), "Thread-2");
        t1.start();
        Thread.sleep(100); // Ensure t1 starts first
        t2.start(); // t2 will be blocked until t1 releases lock
        t1.join();
        t2.join();
        
        System.out.println("\n=== Testing wait() ===");
        Thread t3 = new Thread(new WaitExample(), "Thread-3");
        Thread t4 = new Thread(new WaitExample(), "Thread-4");
        t3.start();
        Thread.sleep(100);
        t4.start(); // t4 can acquire lock while t3 is waiting
        t3.join();
        t4.join();
    }
}

// Practical example showing the difference
class ProducerConsumerExample {
    private int data;
    private boolean hasData = false;
    
    // Using wait() - correct approach
    public synchronized void produce(int value) throws InterruptedException {
        while (hasData) {
            wait(); // Releases lock, allows consumer to run
        }
        data = value;
        hasData = true;
        System.out.println("Produced: " + value);
        notify();
    }
    
    // If we used sleep() instead - WRONG!
    public synchronized void produceWrong(int value) throws InterruptedException {
        while (hasData) {
            Thread.sleep(100); // Holds lock, consumer can't run - DEADLOCK!
        }
        data = value;
        hasData = true;
        System.out.println("Produced: " + value);
    }
    
    public synchronized int consume() throws InterruptedException {
        while (!hasData) {
            wait(); // Releases lock, allows producer to run
        }
        hasData = false;
        System.out.println("Consumed: " + data);
        notify();
        return data;
    }
}
```

## Key Differences

| Aspect | sleep() | wait() |
|--------|---------|--------|
| Class | Thread (static method) | Object (instance method) |
| Lock behavior | Retains lock | Releases lock |
| Synchronized required | No | Yes |
| Waking up | After timeout | notify()/notifyAll() or timeout |
| Purpose | Pause execution | Inter-thread communication |
| Exception | InterruptedException | InterruptedException |
| Usage | `Thread.sleep(1000)` | `object.wait()` |

## When to Use Each

### Use sleep():
- Pause execution for a specific time
- Simulate delays or throttling
- Polling with delays
- No need for inter-thread communication

### Use wait():
- Waiting for a condition to be met
- Producer-consumer patterns
- Thread coordination
- Need to release lock for other threads

## Edge Cases and Pitfalls

- **IllegalMonitorStateException**: wait() throws this if called outside synchronized block
- **Spurious wakeups**: wait() can wake up without notify(), always use in while loop
- **Deadlock with sleep()**: Using sleep() in synchronized block can cause deadlock
- **Common Pitfall**: Using sleep() for thread coordination instead of wait()/notify()

## Interview-Ready Answer

"The main difference is that sleep() is a static method of Thread class that pauses execution for a specified time while retaining any locks held, whereas wait() is an instance method of Object class that releases the lock and waits for notification. sleep() can be called anywhere, but wait() must be called within a synchronized context. Use sleep() for timed pauses and wait() for inter-thread communication."

**Tags**: sleep, wait, thread-control
