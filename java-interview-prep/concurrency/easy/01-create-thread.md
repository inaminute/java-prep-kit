# Create Thread in Java

## Problem Statement

Explain and demonstrate the different ways to create and start a thread in Java. Show how to create a thread using both the Thread class and the Runnable interface. Your solution should include working examples of both approaches and explain when to use each method.

**Input**: N/A (Demonstration of thread creation)

**Output**: Running threads that print messages to console

**Constraints**: 
- Must demonstrate both Thread class extension and Runnable interface implementation
- Threads should perform a simple task (e.g., printing messages)

## Approach

- Java provides two primary ways to create threads: extending Thread class or implementing Runnable interface
- Extending Thread class is simpler but limits inheritance (Java single inheritance)
- Implementing Runnable is preferred as it allows the class to extend other classes
- Both approaches require overriding the run() method with the thread's task
- Threads are started by calling the start() method, not run() directly
- The start() method creates a new thread of execution and calls run() in that thread

## Solution

```java
// Approach 1: Extending Thread class
class MyThread extends Thread {
    private String threadName;
    
    public MyThread(String name) {
        this.threadName = name;
    }
    
    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(threadName + " - Count: " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(threadName + " interrupted");
            }
        }
        System.out.println(threadName + " finished");
    }
}

// Approach 2: Implementing Runnable interface
class MyRunnable implements Runnable {
    private String threadName;
    
    public MyRunnable(String name) {
        this.threadName = name;
    }
    
    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(threadName + " - Count: " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(threadName + " interrupted");
            }
        }
        System.out.println(threadName + " finished");
    }
}

// Approach 3: Using Lambda (Java 8+)
public class ThreadCreationDemo {
    public static void main(String[] args) {
        // Method 1: Extending Thread
        MyThread thread1 = new MyThread("Thread-1");
        thread1.start();
        
        // Method 2: Implementing Runnable
        Thread thread2 = new Thread(new MyRunnable("Thread-2"));
        thread2.start();
        
        // Method 3: Using Lambda expression
        Thread thread3 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Thread-3 - Count: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Thread-3 interrupted");
                }
            }
            System.out.println("Thread-3 finished");
        });
        thread3.start();
        
        // Method 4: Anonymous class
        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 5; i++) {
                    System.out.println("Thread-4 - Count: " + i);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println("Thread-4 interrupted");
                    }
                }
                System.out.println("Thread-4 finished");
            }
        });
        thread4.start();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Thread creation is a constant time operation

**Space Complexity**: O(1) per thread - Each thread requires fixed memory for its stack and thread-local storage

## Edge Cases and Pitfalls

- **Calling run() instead of start()**: Calling run() directly executes the method in the current thread, not in a new thread. Always use start() to create a new thread.
- **Starting a thread twice**: Calling start() on the same thread object twice throws IllegalThreadStateException. Create a new thread object if you need to run the task again.
- **Thread vs Runnable choice**: Prefer Runnable interface over extending Thread class to maintain flexibility for inheritance and better separation of concerns.
- **Forgetting InterruptedException handling**: The sleep() method throws InterruptedException which must be caught or declared.

## Interview-Ready Answer

"Java provides multiple ways to create threads. You can extend the Thread class and override the run() method, or implement the Runnable interface and pass it to a Thread constructor. The Runnable approach is preferred because it allows better code reusability and doesn't restrict inheritance. You start a thread by calling start(), which creates a new execution thread and invokes run() in that context. Modern Java also supports lambda expressions for concise thread creation."
