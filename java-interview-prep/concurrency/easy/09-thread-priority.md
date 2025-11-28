# Thread Priority

## Problem Statement

Explain thread priorities in Java and demonstrate how to set and use them. Discuss the priority range, default values, and the impact of priorities on thread scheduling. Show examples of setting priorities and explain why priorities are only hints to the scheduler and not guarantees.

**Input**: Thread priority values (1-10)

**Output**: Threads with different priorities

**Constraints**: 
- Must explain priority range and constants
- Should demonstrate setting priorities
- Must explain platform-dependent behavior

## Approach

- Java thread priorities range from 1 (MIN_PRIORITY) to 10 (MAX_PRIORITY)
- Default priority is 5 (NORM_PRIORITY)
- Priorities are hints to the thread scheduler, not guarantees
- Higher priority threads are more likely to be scheduled, but not guaranteed
- Priority behavior is platform-dependent
- New threads inherit priority from their parent thread

## Solution

```java
// Example 1: Basic priority demonstration
class PriorityDemo {
    public static void demonstrate() {
        Thread lowPriority = new Thread(() -> {
            System.out.println("Low priority thread: " + Thread.currentThread().getPriority());
            for (int i = 0; i < 5; i++) {
                System.out.println("Low: " + i);
            }
        });
        
        Thread normalPriority = new Thread(() -> {
            System.out.println("Normal priority thread: " + Thread.currentThread().getPriority());
            for (int i = 0; i < 5; i++) {
                System.out.println("Normal: " + i);
            }
        });
        
        Thread highPriority = new Thread(() -> {
            System.out.println("High priority thread: " + Thread.currentThread().getPriority());
            for (int i = 0; i < 5; i++) {
                System.out.println("High: " + i);
            }
        });
        
        // Set priorities
        lowPriority.setPriority(Thread.MIN_PRIORITY);    // 1
        normalPriority.setPriority(Thread.NORM_PRIORITY); // 5
        highPriority.setPriority(Thread.MAX_PRIORITY);    // 10
        
        // Start threads
        lowPriority.start();
        normalPriority.start();
        highPriority.start();
    }
}

// Example 2: Priority constants
class PriorityConstants {
    public static void showConstants() {
        System.out.println("MIN_PRIORITY: " + Thread.MIN_PRIORITY);   // 1
        System.out.println("NORM_PRIORITY: " + Thread.NORM_PRIORITY); // 5
        System.out.println("MAX_PRIORITY: " + Thread.MAX_PRIORITY);   // 10
        
        Thread currentThread = Thread.currentThread();
        System.out.println("Current thread priority: " + currentThread.getPriority());
    }
}

// Example 3: Priority inheritance
class PriorityInheritance {
    public static void demonstrate() {
        Thread parentThread = new Thread(() -> {
            System.out.println("Parent priority: " + Thread.currentThread().getPriority());
            
            // Child thread inherits parent's priority
            Thread childThread = new Thread(() -> {
                System.out.println("Child priority (inherited): " + 
                                 Thread.currentThread().getPriority());
            });
            
            childThread.start();
            
            try {
                childThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        parentThread.setPriority(8);
        parentThread.start();
    }
}

// Example 4: Priority with computation-intensive tasks
class ComputationTask implements Runnable {
    private String name;
    private int priority;
    private volatile long counter = 0;
    
    public ComputationTask(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
    
    @Override
    public void run() {
        Thread.currentThread().setPriority(priority);
        long startTime = System.currentTimeMillis();
        
        // Compute for 1 second
        while (System.currentTimeMillis() - startTime < 1000) {
            counter++;
        }
        
        System.out.println(name + " (priority " + priority + "): " + counter + " iterations");
    }
}

// Example 5: Practical priority usage
class TaskScheduler {
    public void scheduleTasksWithPriority() throws InterruptedException {
        // Critical system task - high priority
        Thread criticalTask = new Thread(() -> {
            System.out.println("Critical task executing");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Critical task completed");
        });
        criticalTask.setPriority(Thread.MAX_PRIORITY);
        
        // Normal business logic - normal priority
        Thread normalTask = new Thread(() -> {
            System.out.println("Normal task executing");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Normal task completed");
        });
        normalTask.setPriority(Thread.NORM_PRIORITY);
        
        // Background cleanup - low priority
        Thread backgroundTask = new Thread(() -> {
            System.out.println("Background task executing");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Background task completed");
        });
        backgroundTask.setPriority(Thread.MIN_PRIORITY);
        
        // Start all tasks
        backgroundTask.start();
        normalTask.start();
        criticalTask.start();
        
        // Wait for completion
        criticalTask.join();
        normalTask.join();
        backgroundTask.join();
    }
}

// Main demonstration
public class ThreadPriorityDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Priority Constants ===");
        PriorityConstants.showConstants();
        
        System.out.println("\n=== Basic Priority Demo ===");
        PriorityDemo.demonstrate();
        Thread.sleep(1000);
        
        System.out.println("\n=== Priority Inheritance ===");
        PriorityInheritance.demonstrate();
        Thread.sleep(1000);
        
        System.out.println("\n=== Computation Tasks ===");
        Thread low = new Thread(new ComputationTask("Low", Thread.MIN_PRIORITY));
        Thread normal = new Thread(new ComputationTask("Normal", Thread.NORM_PRIORITY));
        Thread high = new Thread(new ComputationTask("High", Thread.MAX_PRIORITY));
        
        low.start();
        normal.start();
        high.start();
        
        low.join();
        normal.join();
        high.join();
        
        System.out.println("\n=== Task Scheduler ===");
        TaskScheduler scheduler = new TaskScheduler();
        scheduler.scheduleTasksWithPriority();
    }
}

/*
 * PRIORITY GUIDELINES:
 * 
 * 1. Range: 1 (MIN_PRIORITY) to 10 (MAX_PRIORITY)
 * 2. Default: 5 (NORM_PRIORITY)
 * 3. Inheritance: New threads inherit parent's priority
 * 4. Platform-dependent: Behavior varies across operating systems
 * 5. Not guaranteed: Priorities are hints, not guarantees
 * 
 * WHEN TO USE:
 * - Distinguish critical vs background tasks
 * - Optimize resource allocation
 * - Improve responsiveness for important operations
 * 
 * WHEN NOT TO USE:
 * - Don't rely on priorities for correctness
 * - Don't use as a synchronization mechanism
 * - Avoid on systems with few processors
 */
```

## Complexity Analysis

**Time Complexity**: O(1) - Setting and getting priority are constant time operations

**Space Complexity**: O(1) - Priority is stored as a single integer field

## Edge Cases and Pitfalls

- **Platform dependency**: Thread priority behavior varies significantly across operating systems. Some systems may ignore priorities entirely or map all priorities to a few levels.
- **Not a guarantee**: Higher priority doesn't guarantee execution order or more CPU time. It's only a hint to the scheduler.
- **Starvation risk**: Setting very low priorities can cause thread starvation where low-priority threads never get CPU time if high-priority threads keep running.
- **Invalid priority values**: Setting priority outside 1-10 range throws IllegalArgumentException. Always use the provided constants or validate custom values.

## Interview-Ready Answer

"Java thread priorities range from 1 (MIN_PRIORITY) to 10 (MAX_PRIORITY) with a default of 5 (NORM_PRIORITY). Priorities are hints to the thread scheduler suggesting which threads should get more CPU time, but they're not guarantees. The behavior is platform-dependent and varies across operating systems. New threads inherit their parent's priority. Priorities are useful for distinguishing critical tasks from background tasks, but should never be relied upon for program correctness or synchronization."
