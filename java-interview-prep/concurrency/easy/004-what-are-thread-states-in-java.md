# What are thread states in Java?

## Problem Statement

List and explain all thread states (NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED) and transitions between them.

## Approach

- **Thread.State enum**: Java defines 6 thread states
- **State transitions**: Understanding how threads move between states
- **getState() method**: Can query current thread state
- **Not OS thread states**: These are Java-level states, not OS-level

## Thread States

1. **NEW**: Thread created but not yet started
2. **RUNNABLE**: Thread executing or ready to execute
3. **BLOCKED**: Thread waiting to acquire a monitor lock
4. **WAITING**: Thread waiting indefinitely for another thread's action
5. **TIMED_WAITING**: Thread waiting for a specified time
6. **TERMINATED**: Thread has completed execution

## Solution

```java
public class ThreadStatesDemo {
    
    public static void main(String[] args) throws InterruptedException {
        final Object lock = new Object();
        
        // NEW state
        Thread thread = new Thread(() -> {
            try {
                // RUNNABLE state (executing)
                System.out.println("Thread running");
                
                // TIMED_WAITING state
                Thread.sleep(2000);
                
                synchronized(lock) {
                    // WAITING state
                    lock.wait();
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        System.out.println("State after creation: " + thread.getState()); // NEW
        
        thread.start();
        Thread.sleep(100);
        System.out.println("State after start: " + thread.getState()); // RUNNABLE
        
        Thread.sleep(500);
        System.out.println("State during sleep: " + thread.getState()); // TIMED_WAITING
        
        // Demonstrate BLOCKED state
        Thread blockedThread = new Thread(() -> {
            synchronized(lock) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread waitingThread = new Thread(() -> {
            synchronized(lock) {
                System.out.println("Acquired lock");
            }
        });
        
        blockedThread.start();
        Thread.sleep(100);
        waitingThread.start();
        Thread.sleep(100);
        System.out.println("Waiting thread state: " + waitingThread.getState()); // BLOCKED
        
        // Wait for thread to finish
        thread.join();
        System.out.println("State after completion: " + thread.getState()); // TERMINATED
    }
}

// Comprehensive state transition example
class StateTransitionExample {
    public static void main(String[] args) throws InterruptedException {
        Object lock1 = new Object();
        Object lock2 = new Object();
        
        Thread t1 = new Thread(() -> {
            synchronized(lock1) {
                System.out.println("T1: Acquired lock1");
                try {
                    Thread.sleep(100);
                    synchronized(lock2) {
                        System.out.println("T1: Acquired lock2");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread t2 = new Thread(() -> {
            synchronized(lock2) {
                System.out.println("T2: Acquired lock2");
                try {
                    Thread.sleep(100);
                    synchronized(lock1) {
                        System.out.println("T2: Acquired lock1");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        System.out.println("T1 initial state: " + t1.getState()); // NEW
        System.out.println("T2 initial state: " + t2.getState()); // NEW
        
        t1.start();
        t2.start();
        
        Thread.sleep(150);
        System.out.println("T1 state: " + t1.getState()); // Likely BLOCKED
        System.out.println("T2 state: " + t2.getState()); // Likely BLOCKED
    }
}
```

## State Transition Diagram

```
NEW
 |
 | start()
 ↓
RUNNABLE ←→ BLOCKED (waiting for monitor lock)
 |
 | wait() / join() / LockSupport.park()
 ↓
WAITING
 |
 | notify() / notifyAll() / unpark()
 ↑
 |
RUNNABLE
 |
 | sleep() / wait(timeout) / join(timeout)
 ↓
TIMED_WAITING
 |
 | timeout expires / notify()
 ↑
 |
RUNNABLE
 |
 | run() completes
 ↓
TERMINATED
```

## Methods That Cause State Changes

| Method | From State | To State |
|--------|-----------|----------|
| `start()` | NEW | RUNNABLE |
| `sleep(ms)` | RUNNABLE | TIMED_WAITING |
| `wait()` | RUNNABLE | WAITING |
| `wait(ms)` | RUNNABLE | TIMED_WAITING |
| `join()` | RUNNABLE | WAITING |
| `join(ms)` | RUNNABLE | TIMED_WAITING |
| `notify()/notifyAll()` | WAITING | RUNNABLE |
| Acquiring lock | BLOCKED | RUNNABLE |
| `run()` completes | RUNNABLE | TERMINATED |

## Edge Cases and Pitfalls

- **RUNNABLE doesn't mean running**: Thread may be waiting for CPU time from OS scheduler
- **Can't restart terminated thread**: Once TERMINATED, thread cannot be restarted
- **Spurious wakeups**: Thread in WAITING can wake up without notify() being called
- **Common Pitfall**: Confusing BLOCKED (waiting for lock) with WAITING (waiting for notification)

## Interview-Ready Answer

"Java defines six thread states: NEW (created but not started), RUNNABLE (executing or ready to execute), BLOCKED (waiting for a monitor lock), WAITING (waiting indefinitely for another thread), TIMED_WAITING (waiting for a specified time), and TERMINATED (completed execution). Threads transition between these states based on method calls like start(), sleep(), wait(), and notify()."

**Tags**: thread-states, lifecycle, fundamentals
