# Daemon Threads

## Problem Statement

Explain daemon threads in Java and demonstrate their usage. Show the difference between daemon and user threads, how to create daemon threads, and when to use them. Include examples demonstrating that the JVM exits when only daemon threads remain running.

**Input**: Thread configuration (daemon or user)

**Output**: Threads with different lifecycle behaviors

**Constraints**: 
- Must explain daemon vs user threads
- Should demonstrate JVM exit behavior
- Must show how to create daemon threads

## Approach

- Daemon threads are background threads that don't prevent JVM from exiting
- User threads keep the JVM running until they complete
- JVM exits when all user threads finish, even if daemon threads are still running
- Daemon threads are typically used for background services (GC, monitoring)
- Must call setDaemon(true) before starting the thread
- Daemon status is inherited by child threads

## Solution

```java
// Example 1: User thread vs Daemon thread
class UserVsDaemonDemo {
    public static void demonstrateUserThread() {
        Thread userThread = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("User thread: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("User thread completed");
        });
        
        userThread.start(); // JVM waits for this to complete
        System.out.println("Main thread ending (JVM will wait for user thread)");
    }
    
    public static void demonstrateDaemonThread() {
        Thread daemonThread = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                System.out.println("Daemon thread: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Daemon thread completed (may not print)");
        });
        
        daemonThread.setDaemon(true); // Must be called before start()
        daemonThread.start();
        
        System.out.println("Main thread ending (JVM will exit, daemon may be interrupted)");
    }
}

// Example 2: Daemon thread for monitoring
class MonitoringService implements Runnable {
    private volatile boolean running = true;
    
    @Override
    public void run() {
        System.out.println("Monitoring service started (daemon)");
        
        while (running) {
            System.out.println("Monitoring... Active threads: " + 
                             Thread.activeCount());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Monitoring interrupted");
                break;
            }
        }
        
        System.out.println("Monitoring service stopped");
    }
    
    public void stop() {
        running = false;
    }
}

// Example 3: Daemon thread inheritance
class DaemonInheritance {
    public static void demonstrate() {
        Thread parentDaemon = new Thread(() -> {
            System.out.println("Parent is daemon: " + Thread.currentThread().isDaemon());
            
            // Child inherits daemon status
            Thread childThread = new Thread(() -> {
                System.out.println("Child is daemon: " + Thread.currentThread().isDaemon());
            });
            
            childThread.start();
            
            try {
                childThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        parentDaemon.setDaemon(true);
        parentDaemon.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Example 4: Background cleanup service
class CleanupService implements Runnable {
    @Override
    public void run() {
        System.out.println("Cleanup service started");
        
        while (true) {
            try {
                Thread.sleep(2000);
                performCleanup();
            } catch (InterruptedException e) {
                System.out.println("Cleanup service interrupted");
                break;
            }
        }
    }
    
    private void performCleanup() {
        System.out.println("Performing cleanup...");
        // Cleanup temporary files, cache, etc.
    }
}

// Example 5: Heartbeat daemon
class HeartbeatService implements Runnable {
    private String serviceName;
    
    public HeartbeatService(String serviceName) {
        this.serviceName = serviceName;
    }
    
    @Override
    public void run() {
        while (true) {
            System.out.println(serviceName + " heartbeat: " + 
                             System.currentTimeMillis());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

// Example 6: Application with daemon services
public class DaemonThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== User Thread Demo ===");
        UserVsDaemonDemo.demonstrateUserThread();
        Thread.sleep(3000);
        
        System.out.println("\n=== Daemon Thread Demo ===");
        UserVsDaemonDemo.demonstrateDaemonThread();
        Thread.sleep(1000); // Main exits, daemon may be interrupted
        
        System.out.println("\n=== Application with Daemon Services ===");
        runApplicationWithDaemons();
    }
    
    private static void runApplicationWithDaemons() throws InterruptedException {
        // Start monitoring daemon
        MonitoringService monitor = new MonitoringService();
        Thread monitorThread = new Thread(monitor);
        monitorThread.setDaemon(true);
        monitorThread.start();
        
        // Start cleanup daemon
        Thread cleanupThread = new Thread(new CleanupService());
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        
        // Start heartbeat daemon
        Thread heartbeatThread = new Thread(new HeartbeatService("MyApp"));
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
        
        // Simulate application work (user thread)
        System.out.println("Application doing work...");
        for (int i = 1; i <= 3; i++) {
            System.out.println("Work iteration: " + i);
            Thread.sleep(1500);
        }
        
        System.out.println("Application work completed");
        System.out.println("Main thread exiting (daemons will be terminated)");
        // JVM exits here, all daemon threads are abruptly stopped
    }
}

/*
 * DAEMON THREAD CHARACTERISTICS:
 * 
 * 1. Background service threads
 * 2. Don't prevent JVM from exiting
 * 3. Abruptly terminated when all user threads finish
 * 4. Must call setDaemon(true) before start()
 * 5. Child threads inherit daemon status
 * 
 * COMMON USE CASES:
 * - Garbage collection
 * - Monitoring and logging
 * - Heartbeat services
 * - Background cleanup
 * - Cache management
 * 
 * IMPORTANT NOTES:
 * - Don't use for I/O operations (may be interrupted mid-operation)
 * - Don't use for critical cleanup (use shutdown hooks instead)
 * - Can't change daemon status after thread starts
 */
```

## Complexity Analysis

**Time Complexity**: O(1) - Setting daemon status is a constant time operation

**Space Complexity**: O(1) - Daemon flag is a single boolean field

## Edge Cases and Pitfalls

- **Setting daemon after start**: Calling setDaemon() after start() throws IllegalThreadStateException. Always set daemon status before starting the thread.
- **Abrupt termination**: Daemon threads are terminated abruptly when the JVM exits, potentially leaving resources in inconsistent states. Don't use daemons for critical cleanup or I/O operations.
- **Finally blocks may not execute**: When the JVM exits, finally blocks in daemon threads may not execute, leading to resource leaks.
- **Daemon for critical tasks**: Never use daemon threads for tasks that must complete, like database transactions or file writes. Use user threads or shutdown hooks instead.

## Interview-Ready Answer

"Daemon threads are background service threads that don't prevent the JVM from exiting. When all user (non-daemon) threads complete, the JVM terminates immediately, even if daemon threads are still running. You create a daemon thread by calling setDaemon(true) before start(). Daemon threads are useful for background services like monitoring, logging, and garbage collection, but shouldn't be used for critical operations since they can be abruptly terminated without cleanup."
