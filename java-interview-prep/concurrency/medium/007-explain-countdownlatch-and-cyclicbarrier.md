# Explain CountDownLatch and CyclicBarrier

## Problem Statement

Compare CountDownLatch and CyclicBarrier synchronization aids, including their use cases and differences.

## Approach

- **CountDownLatch**: One-time synchronization, threads wait for count to reach zero
- **CyclicBarrier**: Reusable synchronization, threads wait for each other
- **Use cases**: Different coordination scenarios
- **Barrier action**: CyclicBarrier can execute action when all threads arrive

## Solution

```java
import java.util.concurrent.*;

// CountDownLatch example
class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        int workerCount = 3;
        CountDownLatch latch = new CountDownLatch(workerCount);
        
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i;
            new Thread(() -> {
                System.out.println("Worker " + workerId + " starting");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Worker " + workerId + " done");
                latch.countDown(); // Decrement count
            }).start();
        }
        
        System.out.println("Main thread waiting for workers...");
        latch.await(); // Wait until count reaches 0
        System.out.println("All workers completed!");
    }
}

// CyclicBarrier example
class CyclicBarrierExample {
    public static void main(String[] args) {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
            System.out.println("All parties arrived, barrier action executing!");
        });
        
        for (int i = 0; i < parties; i++) {
            final int partyId = i;
            new Thread(() -> {
                try {
                    System.out.println("Party " + partyId + " working on phase 1");
                    Thread.sleep(1000);
                    System.out.println("Party " + partyId + " waiting at barrier");
                    barrier.await(); // Wait for all parties
                    
                    System.out.println("Party " + partyId + " working on phase 2");
                    Thread.sleep(1000);
                    System.out.println("Party " + partyId + " waiting at barrier");
                    barrier.await(); // Reusable!
                    
                    System.out.println("Party " + partyId + " completed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}

// Real-world: Parallel computation with CountDownLatch
class ParallelComputation {
    public static void main(String[] args) throws InterruptedException {
        int taskCount = 5;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(taskCount);
        
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    startSignal.await(); // Wait for start signal
                    System.out.println("Task " + taskId + " processing");
                    Thread.sleep(1000);
                    System.out.println("Task " + taskId + " completed");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            }).start();
        }
        
        System.out.println("Preparing tasks...");
        Thread.sleep(2000);
        System.out.println("Starting all tasks!");
        startSignal.countDown(); // Release all waiting threads
        
        doneSignal.await(); // Wait for all tasks to complete
        System.out.println("All tasks finished!");
    }
}

// Real-world: Multi-phase algorithm with CyclicBarrier
class MultiPhaseAlgorithm {
    public static void main(String[] args) {
        int threadCount = 4;
        CyclicBarrier barrier = new CyclicBarrier(threadCount, () -> {
            System.out.println("=== Phase completed ===");
        });
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    for (int phase = 1; phase <= 3; phase++) {
                        System.out.println("Thread " + threadId + " phase " + phase);
                        Thread.sleep(1000);
                        barrier.await(); // Wait for all threads to complete phase
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
```

## Key Differences

| Aspect | CountDownLatch | CyclicBarrier |
|--------|----------------|---------------|
| Reusability | One-time use | Reusable |
| Parties | Fixed at creation | Fixed at creation |
| Action | No | Optional barrier action |
| Decrement | Any thread | Only waiting threads |
| Use case | Wait for events | Coordinate phases |

## Edge Cases and Pitfalls

- **CountDownLatch cannot be reset**: Create new instance for reuse
- **CyclicBarrier broken**: If thread interrupted, barrier breaks
- **Timeout**: Both support timed waiting
- **Common Pitfall**: Using CountDownLatch when CyclicBarrier is more appropriate

## Interview-Ready Answer

"CountDownLatch is a one-time synchronization aid where threads wait for a count to reach zero, useful for waiting for multiple tasks to complete. CyclicBarrier is reusable and makes threads wait for each other at a common barrier point, useful for multi-phase algorithms. The key difference is CountDownLatch is one-time while CyclicBarrier can be reused across multiple phases."

**Tags**: countdownlatch, cyclicbarrier, synchronizers
