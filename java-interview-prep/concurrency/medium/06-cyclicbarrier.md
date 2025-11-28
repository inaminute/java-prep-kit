# CyclicBarrier

## Problem Statement

Explain and demonstrate CyclicBarrier from java.util.concurrent package. Show how CyclicBarrier allows a set of threads to wait for each other to reach a common barrier point. Compare it with CountDownLatch and demonstrate its reusability for multiple synchronization cycles.

**Input**: Multiple threads that need to synchronize at barrier points

**Output**: Coordinated execution with reusable barriers

**Constraints**: 
- Must demonstrate CyclicBarrier usage
- Should show await() method
- Must explain reusability

## Approach

- CyclicBarrier synchronizes a fixed number of threads at a barrier point
- All threads call await() and block until all parties arrive
- Once all threads arrive, barrier is tripped and all threads are released
- CyclicBarrier can be reused for multiple cycles
- Optional barrier action runs when barrier is tripped
- Useful for parallel algorithms with multiple phases

## Solution

```java
import java.util.concurrent.*;

class ParallelWorker implements Runnable {
    private int workerId;
    private CyclicBarrier barrier;
    
    public ParallelWorker(int workerId, CyclicBarrier barrier) {
        this.workerId = workerId;
        this.barrier = barrier;
    }
    
    @Override
    public void run() {
        try {
            for (int phase = 1; phase <= 3; phase++) {
                System.out.println("Worker " + workerId + " - Phase " + phase + " started");
                Thread.sleep((int)(Math.random() * 1000));
                System.out.println("Worker " + workerId + " - Phase " + phase + " completed");
                
                barrier.await(); // Wait for all workers to complete this phase
                System.out.println("Worker " + workerId + " - Proceeding to next phase");
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}

public class CyclicBarrierDemo {
    public static void main(String[] args) {
        System.out.println("=== Multi-Phase Computation ===");
        testMultiPhase();
        
        System.out.println("\n=== Barrier Action ===");
        testBarrierAction();
        
        System.out.println("\n=== Matrix Computation ===");
        testMatrixComputation();
    }
    
    private static void testMultiPhase() {
        int workerCount = 3;
        CyclicBarrier barrier = new CyclicBarrier(workerCount);
        
        for (int i = 1; i <= workerCount; i++) {
            new Thread(new ParallelWorker(i, barrier)).start();
        }
    }
    
    private static void testBarrierAction() {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
            System.out.println("*** All parties arrived, barrier action executing ***");
        });
        
        for (int i = 1; i <= parties; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + id + " working");
                    Thread.sleep(1000);
                    System.out.println("Thread " + id + " waiting at barrier");
                    barrier.await();
                    System.out.println("Thread " + id + " passed barrier");
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private static void testMatrixComputation() {
        int rows = 4;
        int[][] matrix = new int[rows][1000];
        CyclicBarrier barrier = new CyclicBarrier(rows, () -> {
            System.out.println("All rows processed, computing final result");
        });
        
        for (int i = 0; i < rows; i++) {
            final int row = i;
            new Thread(() -> {
                try {
                    for (int col = 0; col < matrix[row].length; col++) {
                        matrix[row][col] = row * col;
                    }
                    System.out.println("Row " + row + " computed");
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for await() operation

**Space Complexity**: O(n) where n is the number of parties

## Edge Cases and Pitfalls

- **Broken barrier**: If a thread is interrupted or times out while waiting, the barrier becomes broken and all other waiting threads receive BrokenBarrierException.
- **Wrong party count**: If the number of threads doesn't match the party count, some threads will wait forever.
- **Reusability**: Unlike CountDownLatch, CyclicBarrier automatically resets after all parties arrive, making it reusable for multiple cycles.
- **Barrier action exceptions**: If the barrier action throws an exception, the barrier is broken and all threads receive BrokenBarrierException.

## Interview-Ready Answer

"CyclicBarrier synchronizes a fixed number of threads at a common barrier point. All threads call await() and block until all parties arrive, then all are released simultaneously. Unlike CountDownLatch, CyclicBarrier is reusable and automatically resets for the next cycle. It can execute an optional barrier action when all threads arrive. This is useful for parallel algorithms with multiple phases where threads must synchronize between phases, such as parallel matrix computations or iterative algorithms."
