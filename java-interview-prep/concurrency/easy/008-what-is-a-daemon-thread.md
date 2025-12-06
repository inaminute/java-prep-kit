# What is a daemon thread?

## Problem Statement

Explain daemon threads, how they differ from user threads, and when to use them.

## Approach

- **Background service threads**: Daemon threads run in the background
- **JVM shutdown**: JVM exits when only daemon threads remain
- **setDaemon() method**: Must be called before start()
- **Low priority tasks**: Used for housekeeping, monitoring, garbage collection
- **No guarantee of completion**: May be terminated abruptly

## Solution

```java
public class DaemonThreadDemo {
    
    // User thread example
    static class UserThread extends Thread {
        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                System.out.println("User thread: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("User thread completed");
        }
    }
    
    // Daemon thread example
    static class DaemonThread extends Thread {
        @Override
        public void run() {
            while (true) {
                System.out.println("Daemon thread running...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // This line will never execute
            // System.out.println("Daemon thread completed");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        UserThread userThread = new UserThread();
        DaemonThread daemonThread = new DaemonThread();
        
        // Set as daemon BEFORE starting
        daemonThread.setDaemon(true);
        
        System.out.println("Is daemon: " + daemonThread.isDaemon());
        
        userThread.start();
        daemonThread.start();
        
        // JVM will exit after user thread completes
        // Daemon thread will be terminated abruptly
    }
}

// Practical example: Monitoring thread
class MonitoringExample {
    private static volatile boolean running = true;
    
    static class MonitorThread extends Thread {
        @Override
        public void run() {
            while (running) {
                System.out.println("Monitoring system... " + 
                    new java.util.Date());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Monitor stopped");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        MonitorThread monitor = new MonitorThread();
        monitor.setDaemon(true); // Won't prevent JVM from exiting
        monitor.start();
        
        // Do some work
        System.out.println("Main thread working...");
        Thread.sleep(5000);
        System.out.println("Main thread done");
        
        // JVM exits, daemon thread terminates
    }
}

// Example: Garbage collection simulation
class GarbageCollectorSimulation {
    static class GCThread extends Thread {
        @Override
        public void run() {
            while (true) {
                System.out.println("Running garbage collection...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        GCThread gc = new GCThread();
        gc.setDaemon(true);
        gc.start();
        
        // Simulate application work
        for (int i = 0; i < 3; i++) {
            System.out.println("Application working: " + i);
            Thread.sleep(1000);
        }
        
        System.out.println("Application finished");
        // GC daemon thread will be terminated
    }
}

// Demonstrating setDaemon() restrictions
class DaemonRestrictions {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            System.out.println("Thread running");
        });
        
        thread.start();
        
        try {
            thread.setDaemon(true); // IllegalThreadStateException!
        } catch (IllegalThreadStateException e) {
            System.out.println("Cannot set daemon after thread started");
        }
    }
}
```

## User Thread vs Daemon Thread

| Aspect | User Thread | Daemon Thread |
|--------|-------------|---------------|
| JVM exit | JVM waits for completion | JVM doesn't wait |
| Priority | High priority | Low priority |
| Purpose | Main application tasks | Background services |
| Default | User thread | Inherits from parent |
| Examples | Main thread, worker threads | GC, monitoring, logging |

## Common Use Cases for Daemon Threads

1. **Garbage Collection**: JVM's GC runs as daemon thread
2. **Monitoring**: System health checks, metrics collection
3. **Logging**: Background log processing
4. **Caching**: Cache cleanup and maintenance
5. **Heartbeat**: Keep-alive signals

## Edge Cases and Pitfalls

- **Must set before start()**: Calling setDaemon() after start() throws IllegalThreadStateException
- **No cleanup guarantee**: Daemon threads may not execute finally blocks
- **Child threads inherit**: Threads created by daemon threads are also daemon
- **Common Pitfall**: Using daemon threads for critical tasks that must complete

## Interview-Ready Answer

"Daemon threads are low-priority background threads that provide services to user threads. The key difference is that the JVM exits when only daemon threads remain, without waiting for them to complete. They're used for housekeeping tasks like garbage collection and monitoring. You must call setDaemon(true) before starting the thread, and they shouldn't be used for critical operations that must complete."

**Tags**: daemon-thread, thread-types, fundamentals
