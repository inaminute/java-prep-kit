# Explain the difference between Thread and Runnable

## Problem Statement

Describe the differences between extending Thread class and implementing Runnable interface, including when to use each approach.

## Approach

- **Inheritance vs Interface**: Thread is a class, Runnable is a functional interface
- **Single inheritance limitation**: Java doesn't support multiple inheritance, so extending Thread prevents extending other classes
- **Separation of concerns**: Runnable separates the task from the thread execution mechanism
- **Resource sharing**: Multiple threads can share the same Runnable instance
- **Best practice**: Prefer Runnable for better design flexibility

## Solution

```java
// Approach 1: Extending Thread class
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running: " + Thread.currentThread().getName());
    }
}

// Approach 2: Implementing Runnable interface
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running: " + Thread.currentThread().getName());
    }
}

// Usage
public class ThreadVsRunnable {
    public static void main(String[] args) {
        // Using Thread
        MyThread thread1 = new MyThread();
        thread1.start();
        
        // Using Runnable
        Thread thread2 = new Thread(new MyRunnable());
        thread2.start();
        
        // Lambda expression (since Runnable is functional interface)
        Thread thread3 = new Thread(() -> {
            System.out.println("Lambda running: " + Thread.currentThread().getName());
        });
        thread3.start();
        
        // Sharing same Runnable instance
        Runnable task = new MyRunnable();
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
    }
}
```

## Key Differences

| Aspect | Thread | Runnable |
|--------|--------|----------|
| Type | Class | Interface |
| Inheritance | Cannot extend other classes | Can extend other classes |
| Reusability | Less flexible | More flexible |
| Resource sharing | Each thread is separate object | Multiple threads can share instance |
| Modern usage | Less preferred | Preferred approach |

## Edge Cases and Pitfalls

- **Calling run() instead of start()**: Calling run() directly executes in the same thread, not a new one
- **Thread class overhead**: Extending Thread creates unnecessary overhead when you only need to define task logic
- **Design inflexibility**: Extending Thread limits your class hierarchy options
- **Common Pitfall**: Forgetting that Runnable needs to be wrapped in a Thread object to execute

## Interview-Ready Answer

"The main difference is that Thread is a class while Runnable is a functional interface. Implementing Runnable is preferred because it allows your class to extend other classes, promotes better separation of concerns, and enables resource sharing across multiple threads. Thread should only be extended when you need to override other Thread methods beyond run()."

**Tags**: thread, runnable, fundamentals
