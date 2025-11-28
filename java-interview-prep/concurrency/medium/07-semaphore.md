# Semaphore

## Problem Statement

Explain and demonstrate Semaphore from java.util.concurrent package. Show how Semaphore controls access to a shared resource by maintaining a set of permits. Include examples of limiting concurrent access to resources like database connections or thread pools.

**Input**: Multiple threads competing for limited resources

**Output**: Controlled access with permit-based synchronization

**Constraints**: 
- Must demonstrate Semaphore usage
- Should show acquire() and release() methods
- Must explain permit management

## Approach

- Semaphore maintains a count of available permits
- acquire() blocks if no permits available, decrements count when acquired
- release() increments permit count, potentially unblocking waiting threads
- Useful for limiting concurrent access to resources
- Can be fair (FIFO) or unfair
- tryAcquire() provides non-blocking alternative

## Solution

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// Connection pool using Semaphore
class ConnectionPool {
    private Semaphore semaphore;
    private int poolSize;
    
    public ConnectionPool(int poolSize) {
        this.poolSize = poolSize;
        this.semaphore = new Semaphore(poolSize);
    }
    
    public void useConnection(int clientId) {
        try {
            System.out.println("Client " + clientId + " requesting connection...");
            semaphore.acquire();
            System.out.println("Client " + clientId + " acquired connection");
            
            // Use connection
            Thread.sleep(2000);
            
            System.out.println("Client " + clientId + " releasing connection");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }
    
    public int availableConnections() {
        return semaphore.availablePermits();
    }
}

// Parking lot example
class ParkingLot {
    private Semaphore spaces;
    
    public ParkingLot(int capacity) {
        this.spaces = new Semaphore(capacity, true); // Fair semaphore
    }
    
    public void park(int carId) {
        try {
            System.out.println("Car " + carId + " trying to park");
            spaces.acquire();
            System.out.println("Car " + carId + " parked. Available: " + spaces.availablePermits());
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void leave(int carId) {
        System.out.println("Car " + carId + " leaving");
        spaces.release();
        System.out.println("Car " + carId + " left. Available: " + spaces.availablePermits());
    }
}

public class SemaphoreDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Connection Pool ===");
        testConnectionPool();
        
        System.out.println("\n=== Parking Lot ===");
        testParkingLot();
        
        System.out.println("\n=== Try Acquire ===");
        testTryAcquire();
    }
    
    private static void testConnectionPool() throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(3);
        
        for (int i = 1; i <= 10; i++) {
            final int clientId = i;
            new Thread(() -> pool.useConnection(clientId)).start();
            Thread.sleep(500);
        }
        
        Thread.sleep(5000);
    }
    
    private static void testParkingLot() throws InterruptedException {
        ParkingLot lot = new ParkingLot(2);
        
        for (int i = 1; i <= 5; i++) {
            final int carId = i;
            new Thread(() -> {
                lot.park(carId);
                lot.leave(carId);
            }).start();
        }
        
        Thread.sleep(10000);
    }
    
    private static void testTryAcquire() throws InterruptedException {
        Semaphore semaphore = new Semaphore(2);
        
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                        System.out.println("Thread " + id + " acquired permit");
                        Thread.sleep(2000);
                        semaphore.release();
                        System.out.println("Thread " + id + " released permit");
                    } else {
                        System.out.println("Thread " + id + " could not acquire permit");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        Thread.sleep(5000);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for acquire() and release() operations

**Space Complexity**: O(n) where n is the number of waiting threads

## Edge Cases and Pitfalls

- **Forgetting to release**: Always use try-finally to ensure release() is called, even if exceptions occur. Failing to release causes permit leaks.
- **Releasing without acquiring**: release() can be called without prior acquire(), increasing permits beyond initial count. This can break resource limits.
- **Fair vs unfair**: Fair semaphores guarantee FIFO order but have lower throughput. Choose based on whether fairness or performance is more important.
- **Acquiring multiple permits**: acquire(n) is atomic but can cause deadlock if threads acquire different numbers of permits.

## Interview-Ready Answer

"Semaphore controls access to shared resources by maintaining a count of available permits. Threads call acquire() to obtain a permit (blocking if none available) and release() to return it. This is useful for limiting concurrent access to resources like database connections or thread pools. Semaphores can be fair (FIFO) or unfair (better performance). Unlike locks, semaphores don't have ownership - any thread can release a permit. Always use try-finally to ensure permits are released."
