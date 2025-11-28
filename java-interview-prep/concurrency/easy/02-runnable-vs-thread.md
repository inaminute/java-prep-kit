# Runnable vs Thread

## Problem Statement

Explain the differences between implementing Runnable interface and extending Thread class for creating threads in Java. Discuss the advantages and disadvantages of each approach, and provide recommendations on when to use each method. Include code examples demonstrating both approaches.

**Input**: N/A (Conceptual comparison)

**Output**: Explanation with code examples

**Constraints**: 
- Must clearly explain the differences
- Should include practical examples
- Must discuss inheritance implications

## Approach

- Compare the two fundamental approaches to thread creation in Java
- Analyze the inheritance model: Thread extends Object, Runnable is an interface
- Consider object-oriented design principles (composition vs inheritance)
- Evaluate resource usage and flexibility
- Examine code reusability and maintainability
- Discuss modern best practices and recommendations

## Solution

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Approach 1: Extending Thread class
class ThreadExtension extends Thread {
    private String taskName;
    
    public ThreadExtension(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public void run() {
        System.out.println(taskName + " executed by: " + Thread.currentThread().getName());
    }
}

// Approach 2: Implementing Runnable interface
class RunnableImplementation implements Runnable {
    private String taskName;
    
    public RunnableImplementation(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public void run() {
        System.out.println(taskName + " executed by: " + Thread.currentThread().getName());
    }
}

// Example showing Runnable allows extending other classes
class BaseClass {
    protected void baseMethod() {
        System.out.println("Base class method");
    }
}

class RunnableWithInheritance extends BaseClass implements Runnable {
    @Override
    public void run() {
        baseMethod(); // Can use inherited methods
        System.out.println("Runnable with inheritance");
    }
}

// Demonstration class
public class RunnableVsThreadComparison {
    public static void main(String[] args) {
        System.out.println("=== Thread Extension Approach ===");
        ThreadExtension thread1 = new ThreadExtension("Task-1");
        thread1.start();
        
        System.out.println("\n=== Runnable Implementation Approach ===");
        Thread thread2 = new Thread(new RunnableImplementation("Task-2"));
        thread2.start();
        
        // Runnable can be reused with different threads
        Runnable task = new RunnableImplementation("Task-3");
        Thread thread3 = new Thread(task);
        Thread thread4 = new Thread(task);
        thread3.start();
        thread4.start();
        
        // Runnable works better with thread pools
        System.out.println("\n=== Runnable with ExecutorService ===");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(new RunnableImplementation("Task-4"));
        executor.submit(new RunnableImplementation("Task-5"));
        executor.shutdown();
        
        // Demonstrating inheritance advantage
        System.out.println("\n=== Runnable with Inheritance ===");
        Thread thread5 = new Thread(new RunnableWithInheritance());
        thread5.start();
    }
}

/*
 * KEY DIFFERENCES:
 * 
 * 1. INHERITANCE:
 *    - Thread: Cannot extend another class (Java single inheritance)
 *    - Runnable: Can extend other classes while implementing Runnable
 * 
 * 2. REUSABILITY:
 *    - Thread: Each task requires a new Thread object
 *    - Runnable: Same Runnable instance can be used with multiple threads
 * 
 * 3. SEPARATION OF CONCERNS:
 *    - Thread: Mixes task logic with thread management
 *    - Runnable: Separates task logic from thread management
 * 
 * 4. THREAD POOL COMPATIBILITY:
 *    - Thread: Less suitable for thread pools
 *    - Runnable: Designed for use with ExecutorService and thread pools
 * 
 * 5. OVERHEAD:
 *    - Thread: Creates a full Thread object with all its overhead
 *    - Runnable: Lightweight, just defines the task
 * 
 * RECOMMENDATION: Prefer Runnable interface for better design and flexibility
 */
```

## Complexity Analysis

**Time Complexity**: O(1) - Both approaches have constant time for thread creation

**Space Complexity**: O(1) - Thread class has slightly more overhead due to additional fields, but both are constant space per thread

## Edge Cases and Pitfalls

- **Inheritance limitation**: Extending Thread prevents extending other classes, which can be a significant limitation in complex applications.
- **Poor separation of concerns**: Thread extension mixes the task logic with thread management, violating single responsibility principle.
- **Thread pool incompatibility**: Thread objects are harder to use with modern ExecutorService and thread pools, which are designed for Runnable tasks.
- **Resource waste**: Creating Thread objects for simple tasks wastes resources compared to lightweight Runnable instances that can be pooled and reused.

## Interview-Ready Answer

"The main difference is that Runnable is an interface while Thread is a class. Implementing Runnable is preferred because it allows your class to extend other classes, provides better separation of concerns, and works seamlessly with modern concurrency utilities like ExecutorService. Runnable instances are also reusable and lightweight. Extending Thread should only be used when you need to override other Thread methods, which is rare. The Runnable approach follows composition over inheritance and is considered a best practice."
