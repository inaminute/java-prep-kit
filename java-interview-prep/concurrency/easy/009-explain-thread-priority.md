# Explain thread priority

## Problem Statement

Describe how thread priorities work in Java and their impact on thread scheduling.

## Approach

- **Priority range**: 1 (MIN_PRIORITY) to 10 (MAX_PRIORITY)
- **Default priority**: 5 (NORM_PRIORITY)
- **Inheritance**: Child threads inherit parent's priority
- **Platform-dependent**: Actual behavior depends on OS scheduler
- **Hints, not guarantees**: Priority is a suggestion to the scheduler

## Solution

```java
public class ThreadPriorityDemo {
    
    static class PriorityTask implements Runnable {
        private String name;
        
        public PriorityTask(String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            System.out.println(name + " started with priority: " + 
                Thread.currentThread().getPriority());
            
            int count = 0;
            for (int i = 0; i < 1000000; i++) {
                count++;
            }
            
            System.out.println(name + " finished. Count: " + count);
        }
    }
    
    public static void main(String[] args) {
        // Priority constants
        System.out.println("MIN_PRIORITY: " + Thread.MIN_PRIORITY);   // 1
        System.out.println("NORM_PRIORITY: " + Thread.NORM_PRIORITY); // 5
        System.out.println("MAX_PRIORITY: " + Thread.MAX_PRIORITY);   // 10
        
        // Create threads with different priorities
        Thread lowPriority = new Thread(new PriorityTask("Low Priority"));
        Thread normalPriority = new Thread(new PriorityTask("Normal Priority"));
        Thread highPriority = new Thread(new PriorityTask("High Priority"));
        
        // Set priorities
        lowPriority.setPriority(Thread.MIN_PRIORITY);
        normalPriority.setPriority(Thread.NORM_PRIORITY);
        highPriority.setPriority(Thread.MAX_PRIORITY);
        
        // Start threads
        lowPriority.start();
        normalPriority.start();
        highPriority.start();
    }
}

// Demonstrating priority inheritance
class PriorityInheritance {
    public static void main(String[] args) {
        System.out.println("Main thread priority: " + 
            Thread.currentThread().getPriority());
        
        Thread parent = new Thread(() -> {
            System.out.println("Parent thread priority: " + 
                Thread.currentThread().getPriority());
            
            // Child thread inherits parent's priority
            Thread child = new Thread(() -> {
                System.out.println("Child thread priority: " + 
                    Thread.currentThread().getPriority());
            });
            child.start();
        });
        
        parent.setPriority(8);
        parent.start();
    }
}

// Practical example: Background vs foreground tasks
class TaskPriorityExample {
    static class BackgroundTask implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println("Background task: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static class ForegroundTask implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println("Foreground task: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        Thread background = new Thread(new BackgroundTask());
        Thread foreground = new Thread(new ForegroundTask());
        
        // Give foreground task higher priority
        background.setPriority(Thread.MIN_PRIORITY);
        foreground.setPriority(Thread.MAX_PRIORITY);
        
        background.start();
        foreground.start();
    }
}

// Demonstrating that priority is not a guarantee
class PriorityNotGuaranteed {
    static volatile int counter = 0;
    
    static class CounterTask implements Runnable {
        private String name;
        
        public CounterTask(String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                counter++;
                if (i % 20 == 0) {
                    System.out.println(name + " at: " + i);
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        Thread low = new Thread(new CounterTask("Low"));
        Thread high = new Thread(new CounterTask("High"));
        
        low.setPriority(Thread.MIN_PRIORITY);
        high.setPriority(Thread.MAX_PRIORITY);
        
        // High priority doesn't guarantee it finishes first
        low.start();
        high.start();
        
        low.join();
        high.join();
        
        System.out.println("Final counter: " + counter);
    }
}
```

## Priority Levels

| Constant | Value | Description |
|----------|-------|-------------|
| MIN_PRIORITY | 1 | Lowest priority |
| NORM_PRIORITY | 5 | Default priority |
| MAX_PRIORITY | 10 | Highest priority |

## Important Points

1. **Not a guarantee**: Higher priority suggests more CPU time but doesn't guarantee it
2. **Platform-dependent**: Different operating systems handle priorities differently
3. **Starvation risk**: Low priority threads may never execute if high priority threads keep running
4. **Use sparingly**: Rely on proper synchronization, not priorities, for correctness

## Edge Cases and Pitfalls

- **IllegalArgumentException**: Setting priority outside 1-10 range throws exception
- **Platform differences**: Priority behavior varies across Windows, Linux, macOS
- **Starvation**: Low priority threads may starve if high priority threads dominate
- **Common Pitfall**: Relying on priorities for program correctness instead of proper synchronization

## Interview-Ready Answer

"Thread priority in Java ranges from 1 (MIN_PRIORITY) to 10 (MAX_PRIORITY) with 5 as default (NORM_PRIORITY). It's a hint to the thread scheduler about relative importance, but doesn't guarantee execution order or CPU time. Priority is platform-dependent and should be used sparingly. Child threads inherit their parent's priority, and you should never rely on priorities for program correctness."

**Tags**: thread-priority, scheduling, fundamentals
