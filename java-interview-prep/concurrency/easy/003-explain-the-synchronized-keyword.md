# Explain the synchronized keyword

## Problem Statement

Describe how the synchronized keyword works for methods and blocks, including its impact on thread execution.

## Approach

- **Monitor locks**: Every object in Java has an intrinsic lock (monitor)
- **Mutual exclusion**: Only one thread can hold the lock at a time
- **Method synchronization**: Locks on `this` (instance) or `Class` object (static)
- **Block synchronization**: Allows fine-grained control over lock object
- **Reentrant**: Same thread can acquire the same lock multiple times

## Solution

```java
public class SynchronizedExample {
    private int count = 0;
    private final Object lock = new Object();
    
    // 1. Synchronized instance method (locks on 'this')
    public synchronized void incrementMethod() {
        count++;
    }
    
    // 2. Synchronized static method (locks on Class object)
    public static synchronized void staticMethod() {
        System.out.println("Static synchronized method");
    }
    
    // 3. Synchronized block with 'this'
    public void incrementBlock() {
        synchronized(this) {
            count++;
        }
    }
    
    // 4. Synchronized block with custom lock object
    public void incrementCustomLock() {
        synchronized(lock) {
            count++;
        }
    }
    
    // 5. Multiple synchronized blocks with different locks
    private int balance = 0;
    private final Object balanceLock = new Object();
    
    public void updateBoth() {
        synchronized(lock) {
            count++;
        }
        synchronized(balanceLock) {
            balance++;
        }
    }
    
    // 6. Reentrant example
    public synchronized void outerMethod() {
        System.out.println("Outer method");
        innerMethod(); // Can call another synchronized method
    }
    
    public synchronized void innerMethod() {
        System.out.println("Inner method");
    }
}

// Demonstration
class SynchronizedDemo {
    public static void main(String[] args) throws InterruptedException {
        SynchronizedExample example = new SynchronizedExample();
        
        // Create multiple threads
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                example.incrementMethod();
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                example.incrementBlock();
            }
        });
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        System.out.println("Final count: " + example.count);
    }
}
```

## How Synchronized Works

1. **Acquiring lock**: Thread must acquire object's monitor before entering synchronized code
2. **Blocking**: If lock is held by another thread, current thread blocks (BLOCKED state)
3. **Executing**: Once lock is acquired, thread executes the synchronized code
4. **Releasing lock**: Lock is automatically released when exiting synchronized block/method
5. **Memory visibility**: Changes made in synchronized block are visible to other threads

## Synchronized Method vs Block

| Aspect | Synchronized Method | Synchronized Block |
|--------|-------------------|-------------------|
| Lock object | `this` or `Class` | Any object |
| Granularity | Entire method | Specific code section |
| Performance | May lock more than needed | Better performance |
| Flexibility | Less flexible | More flexible |

## Edge Cases and Pitfalls

- **Synchronizing on null**: Throws NullPointerException
- **Synchronizing on String literals**: Can cause unexpected blocking across unrelated code
- **Synchronizing on boxed primitives**: Auto-boxing can create different objects
- **Common Pitfall**: Synchronizing on a local variable (has no effect across threads)

## Interview-Ready Answer

"The synchronized keyword provides mutual exclusion by using intrinsic locks. For methods, it locks on 'this' for instance methods or the Class object for static methods. For blocks, you can specify any object as the lock. Only one thread can hold a lock at a time, ensuring thread-safe access to shared resources. It's reentrant, meaning the same thread can acquire the same lock multiple times."

**Tags**: synchronized, locks, mutual-exclusion
