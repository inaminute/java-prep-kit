# What is ThreadLocal and when to use it?

## Problem Statement

Explain ThreadLocal variables, their use cases, and potential memory leak issues.

## Approach

- **Thread-confined state**: Each thread has its own copy of the variable
- **No synchronization needed**: Variables are not shared between threads
- **Use cases**: User context, database connections, date formatters
- **Memory leaks**: Must remove ThreadLocal values in thread pools
- **InheritableThreadLocal**: Child threads can inherit parent's values

## Solution

```java
public class ThreadLocalExample {
    // Simple ThreadLocal
    private static ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 0);
    
    public static void main(String[] args) {
        // Thread 1
        Thread t1 = new Thread(() -> {
            threadLocal.set(100);
            System.out.println("Thread 1: " + threadLocal.get()); // 100
        });
        
        // Thread 2
        Thread t2 = new Thread(() -> {
            threadLocal.set(200);
            System.out.println("Thread 2: " + threadLocal.get()); // 200
        });
        
        t1.start();
        t2.start();
    }
}

// Use case 1: User context
class UserContext {
    private static ThreadLocal<String> currentUser = new ThreadLocal<>();
    
    public static void setUser(String user) {
        currentUser.set(user);
    }
    
    public static String getUser() {
        return currentUser.get();
    }
    
    public static void clear() {
        currentUser.remove(); // Important to prevent memory leaks
    }
}

// Use case 2: SimpleDateFormat (not thread-safe)
class DateFormatter {
    private static ThreadLocal<java.text.SimpleDateFormat> formatter = 
        ThreadLocal.withInitial(() -> new java.text.SimpleDateFormat("yyyy-MM-dd"));
    
    public static String format(java.util.Date date) {
        return formatter.get().format(date);
    }
}

// Use case 3: Database connection per thread
class DatabaseConnection {
    private static ThreadLocal<java.sql.Connection> connection = new ThreadLocal<>();
    
    public static java.sql.Connection getConnection() {
        if (connection.get() == null) {
            // Create connection
            // connection.set(createConnection());
        }
        return connection.get();
    }
    
    public static void closeConnection() {
        java.sql.Connection conn = connection.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            connection.remove();
        }
    }
}

// InheritableThreadLocal example
class InheritableThreadLocalExample {
    private static InheritableThreadLocal<String> inheritableThreadLocal = 
        new InheritableThreadLocal<>();
    
    public static void main(String[] args) {
        inheritableThreadLocal.set("Parent value");
        
        Thread child = new Thread(() -> {
            System.out.println("Child thread: " + inheritableThreadLocal.get()); // Parent value
            inheritableThreadLocal.set("Child value");
            System.out.println("Child thread after set: " + inheritableThreadLocal.get());
        });
        
        child.start();
        
        try {
            child.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Parent thread: " + inheritableThreadLocal.get()); // Parent value
    }
}

// Memory leak prevention
class ThreadLocalMemoryLeak {
    private static ThreadLocal<byte[]> threadLocal = new ThreadLocal<>();
    
    public static void main(String[] args) {
        java.util.concurrent.ExecutorService executor = 
            java.util.concurrent.Executors.newFixedThreadPool(2);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    // Set large object
                    threadLocal.set(new byte[1024 * 1024]); // 1MB
                    
                    // Do work
                    System.out.println("Working...");
                    
                } finally {
                    // IMPORTANT: Remove to prevent memory leak
                    threadLocal.remove();
                }
            });
        }
        
        executor.shutdown();
    }
}
```

## Common Use Cases

1. **User context**: Store user information for request processing
2. **Transaction context**: Maintain transaction state
3. **Database connections**: One connection per thread
4. **Date formatters**: SimpleDateFormat is not thread-safe
5. **Request/Response context**: Web applications

## Memory Leak Problem

ThreadLocal can cause memory leaks in thread pools because:
1. Thread pool reuses threads
2. ThreadLocal values remain in thread's ThreadLocalMap
3. Values are not garbage collected until thread dies
4. In thread pools, threads rarely die

**Solution**: Always call `remove()` in finally block

## Edge Cases and Pitfalls

- **Memory leaks in thread pools**: Always call remove()
- **Not a substitute for synchronization**: Use for thread-confined data only
- **Inheritance**: InheritableThreadLocal for parent-child thread scenarios
- **Common Pitfall**: Forgetting to remove ThreadLocal values

## Interview-Ready Answer

"ThreadLocal provides thread-confined variables where each thread has its own independent copy. It's useful for storing per-thread context like user information, database connections, or non-thread-safe objects like SimpleDateFormat. The main pitfall is memory leaks in thread pools, which can be prevented by calling remove() in a finally block after use."

**Tags**: threadlocal, thread-confinement, memory
