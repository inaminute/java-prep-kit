# Explain Semaphore and its use cases

## Problem Statement

Describe how Semaphore works for controlling access to resources and implementing resource pools.

## Approach

- **Permits**: Controls number of concurrent accesses
- **Acquire/Release**: Get and return permits
- **Fairness**: Optional fair ordering
- **Resource pooling**: Limit concurrent access to resources
- **Rate limiting**: Control throughput

## Solution

```java
import java.util.concurrent.Semaphore;

// Basic semaphore example
class SemaphoreExample {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3); // 3 permits
        
        for (int i = 0; i < 10; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + id + " waiting for permit");
                    semaphore.acquire(); // Get permit
                    System.out.println("Thread " + id + " got permit");
                    Thread.sleep(2000);
                    System.out.println("Thread " + id + " releasing permit");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release(); // Return permit
                }
            }).start();
        }
    }
}

// Use case 1: Connection pool
class ConnectionPool {
    private final Semaphore semaphore;
    private final java.util.List<Connection> connections;
    
    public ConnectionPool(int poolSize) {
        semaphore = new Semaphore(poolSize);
        connections = new java.util.ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            connections.add(new Connection(i));
        }
    }
    
    public Connection getConnection() throws InterruptedException {
        semaphore.acquire();
        return getAvailableConnection();
    }
    
    public void releaseConnection(Connection conn) {
        returnConnection(conn);
        semaphore.release();
    }
    
    private synchronized Connection getAvailableConnection() {
        for (Connection conn : connections) {
            if (!conn.isInUse()) {
                conn.setInUse(true);
                return conn;
            }
        }
        return null;
    }
    
    private synchronized void returnConnection(Connection conn) {
        conn.setInUse(false);
    }
    
    static class Connection {
        private int id;
        private boolean inUse;
        
        public Connection(int id) {
            this.id = id;
        }
        
        public boolean isInUse() { return inUse; }
        public void setInUse(boolean inUse) { this.inUse = inUse; }
        public int getId() { return id; }
    }
}

// Use case 2: Rate limiter
class RateLimiter {
    private final Semaphore semaphore;
    
    public RateLimiter(int requestsPerSecond) {
        semaphore = new Semaphore(requestsPerSecond);
        
        // Refill permits every second
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    // Release permits up to max
                    int available = semaphore.availablePermits();
                    if (available < requestsPerSecond) {
                        semaphore.release(requestsPerSecond - available);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }
}

// Use case 3: Bounded resource access
class PrinterPool {
    private final Semaphore semaphore;
    
    public PrinterPool(int printerCount) {
        semaphore = new Semaphore(printerCount, true); // Fair semaphore
    }
    
    public void print(String document) {
        try {
            semaphore.acquire();
            System.out.println(Thread.currentThread().getName() + 
                " printing: " + document);
            Thread.sleep(2000); // Simulate printing
            System.out.println(Thread.currentThread().getName() + 
                " finished printing");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }
    
    public static void main(String[] args) {
        PrinterPool pool = new PrinterPool(2); // 2 printers
        
        for (int i = 0; i < 5; i++) {
            final int docId = i;
            new Thread(() -> {
                pool.print("Document-" + docId);
            }, "User-" + i).start();
        }
    }
}
```

## Key Methods

| Method | Description |
|--------|-------------|
| `acquire()` | Acquire a permit, blocking if none available |
| `tryAcquire()` | Try to acquire without blocking |
| `tryAcquire(timeout)` | Try to acquire with timeout |
| `release()` | Release a permit |
| `availablePermits()` | Get number of available permits |

## Common Use Cases

1. **Connection pools**: Limit database connections
2. **Rate limiting**: Control API request rates
3. **Resource pools**: Manage limited resources (printers, threads)
4. **Throttling**: Control concurrent operations
5. **Parking lot**: Limit parking spaces

## Edge Cases and Pitfalls

- **Not releasing**: Always release in finally block
- **Releasing without acquiring**: Increases permit count
- **Fairness overhead**: Fair semaphores are slower
- **Common Pitfall**: Forgetting to release permits

## Interview-Ready Answer

"Semaphore is a synchronization aid that maintains a set of permits controlling access to resources. Threads acquire permits to access resources and release them when done. It's useful for implementing resource pools, rate limiters, and controlling concurrent access to limited resources. Unlike locks, semaphores can have multiple permits and don't require the same thread to acquire and release."

**Tags**: semaphore, resource-control, synchronizers
