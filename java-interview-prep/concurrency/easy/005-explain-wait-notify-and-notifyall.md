# Explain wait(), notify(), and notifyAll()

## Problem Statement

Describe the purpose and usage of wait(), notify(), and notifyAll() methods for inter-thread communication.

## Approach

- **Object class methods**: Defined in Object class, available to all objects
- **Must be synchronized**: Can only be called within synchronized context
- **Releases lock**: wait() releases the monitor lock
- **Condition-based waiting**: Threads wait for specific conditions to be met
- **Notification mechanism**: notify() wakes one thread, notifyAll() wakes all

## Solution

```java
// Producer-Consumer example using wait/notify
class SharedBuffer {
    private int data;
    private boolean hasData = false;
    
    public synchronized void produce(int value) throws InterruptedException {
        // Wait while buffer has data
        while (hasData) {
            wait(); // Releases lock and waits
        }
        
        data = value;
        hasData = true;
        System.out.println("Produced: " + value);
        
        notify(); // Wake up one waiting consumer
    }
    
    public synchronized int consume() throws InterruptedException {
        // Wait while buffer is empty
        while (!hasData) {
            wait(); // Releases lock and waits
        }
        
        hasData = false;
        System.out.println("Consumed: " + data);
        
        notify(); // Wake up one waiting producer
        return data;
    }
}

// Demonstration
public class WaitNotifyDemo {
    public static void main(String[] args) {
        SharedBuffer buffer = new SharedBuffer();
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.produce(i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.consume();
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        producer.start();
        consumer.start();
    }
}

// Example with notifyAll()
class MultipleWaiters {
    private boolean ready = false;
    
    public synchronized void waitForReady() throws InterruptedException {
        while (!ready) {
            System.out.println(Thread.currentThread().getName() + " waiting...");
            wait();
        }
        System.out.println(Thread.currentThread().getName() + " proceeding!");
    }
    
    public synchronized void setReady() {
        ready = true;
        System.out.println("Setting ready, notifying all waiters");
        notifyAll(); // Wake up ALL waiting threads
    }
    
    public static void main(String[] args) throws InterruptedException {
        MultipleWaiters mw = new MultipleWaiters();
        
        // Create multiple waiting threads
        for (int i = 1; i <= 3; i++) {
            new Thread(() -> {
                try {
                    mw.waitForReady();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread-" + i).start();
        }
        
        Thread.sleep(1000);
        mw.setReady(); // Wake up all waiting threads
    }
}
```

## Key Concepts

### wait()
- Releases the lock on the object
- Thread enters WAITING state
- Must be called within synchronized block/method
- Can specify timeout: `wait(1000)` for TIMED_WAITING

### notify()
- Wakes up ONE waiting thread (arbitrary selection)
- Does NOT release the lock immediately
- Woken thread must re-acquire lock before proceeding
- If no threads waiting, notify() has no effect

### notifyAll()
- Wakes up ALL waiting threads
- All threads compete to re-acquire the lock
- Preferred when multiple threads might be waiting
- Prevents missed signals

## Why Use While Loop with wait()

```java
// WRONG - using if
synchronized(obj) {
    if (!condition) {
        obj.wait();
    }
    // Process
}

// CORRECT - using while
synchronized(obj) {
    while (!condition) {
        obj.wait();
    }
    // Process
}
```

Reasons:
1. **Spurious wakeups**: Thread can wake up without notify()
2. **Multiple waiters**: Another thread might change condition before this thread runs
3. **Safety**: Always recheck condition after waking up

## Edge Cases and Pitfalls

- **IllegalMonitorStateException**: Thrown if called outside synchronized context
- **Missed signals**: If notify() called before wait(), the signal is lost
- **notify() vs notifyAll()**: Using notify() with multiple waiters can cause deadlock
- **Common Pitfall**: Using if instead of while loop with wait()
- **Lock not released by notify()**: Notified thread must wait for notifying thread to release lock

## Interview-Ready Answer

"wait(), notify(), and notifyAll() are Object class methods used for inter-thread communication. wait() releases the lock and puts the thread in WAITING state until another thread calls notify() or notifyAll() on the same object. notify() wakes one waiting thread while notifyAll() wakes all. They must be called within a synchronized context, and wait() should always be used in a while loop to handle spurious wakeups."

**Tags**: wait-notify, inter-thread-communication, object-methods
