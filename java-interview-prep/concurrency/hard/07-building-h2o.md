# Building H2O Molecules

## Problem Statement

Implement a barrier that allows threads representing hydrogen and oxygen atoms to form water molecules (H2O). Two hydrogen threads and one oxygen thread must synchronize to form each molecule. Ensure proper synchronization so that molecules are formed correctly without deadlock or starvation.

**Input**: Multiple hydrogen and oxygen threads

**Output**: Properly formed H2O molecules

**Constraints**: 
- Each molecule needs exactly 2 H and 1 O
- Must prevent partial molecules
- Should handle any number of threads

## Approach

- Use semaphores or barriers to coordinate molecule formation
- Hydrogen threads wait until 2 are available
- Oxygen thread waits until 2 hydrogen threads are ready
- Use CyclicBarrier or custom synchronization
- Ensure atomic molecule formation
- Handle thread coordination without deadlock

## Solution

```java
import java.util.concurrent.*;

class H2OBuilder {
    private Semaphore hydrogenSem = new Semaphore(2);
    private Semaphore oxygenSem = new Semaphore(1);
    private CyclicBarrier barrier = new CyclicBarrier(3, () -> {
        System.out.println("H2O molecule formed!");
        hydrogenSem.release(2);
        oxygenSem.release(1);
    });
    
    public void hydrogen() throws InterruptedException, BrokenBarrierException {
        hydrogenSem.acquire();
        System.out.print("H");
        barrier.await();
    }
    
    public void oxygen() throws InterruptedException, BrokenBarrierException {
        oxygenSem.acquire();
        System.out.print("O");
        barrier.await();
    }
}

// Alternative solution using CountDownLatch
class H2OBuilderLatch {
    private Semaphore hydrogenSem = new Semaphore(0);
    private Semaphore oxygenSem = new Semaphore(0);
    private final Object lock = new Object();
    private int hydrogenCount = 0;
    private int oxygenCount = 0;
    
    public void hydrogen() throws InterruptedException {
        synchronized (lock) {
            hydrogenCount++;
            checkAndFormMolecule();
        }
        
        hydrogenSem.acquire();
        System.out.print("H");
    }
    
    public void oxygen() throws InterruptedException {
        synchronized (lock) {
            oxygenCount++;
            checkAndFormMolecule();
        }
        
        oxygenSem.acquire();
        System.out.print("O");
    }
    
    private void checkAndFormMolecule() {
        while (hydrogenCount >= 2 && oxygenCount >= 1) {
            hydrogenSem.release(2);
            oxygenSem.release(1);
            hydrogenCount -= 2;
            oxygenCount -= 1;
            System.out.println(" -> H2O formed");
        }
    }
}

public class H2ODemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CyclicBarrier Solution ===");
        testBarrierSolution();
        
        System.out.println("\n=== Latch Solution ===");
        testLatchSolution();
    }
    
    private static void testBarrierSolution() throws InterruptedException {
        H2OBuilder builder = new H2OBuilder();
        
        // Create 6 hydrogen and 3 oxygen threads (3 molecules)
        Thread[] threads = new Thread[9];
        
        for (int i = 0; i < 6; i++) {
            threads[i] = new Thread(() -> {
                try {
                    builder.hydrogen();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, "H-" + i);
        }
        
        for (int i = 6; i < 9; i++) {
            threads[i] = new Thread(() -> {
                try {
                    builder.oxygen();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, "O-" + (i - 6));
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    private static void testLatchSolution() throws InterruptedException {
        H2OBuilderLatch builder = new H2OBuilderLatch();
        
        Thread[] threads = new Thread[9];
        
        for (int i = 0; i < 6; i++) {
            threads[i] = new Thread(() -> {
                try {
                    builder.hydrogen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "H-" + i);
        }
        
        for (int i = 6; i < 9; i++) {
            threads[i] = new Thread(() -> {
                try {
                    builder.oxygen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "O-" + (i - 6));
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) per atom operation

**Space Complexity**: O(1) for synchronization primitives

## Edge Cases and Pitfalls

- **Partial molecules**: Ensure atomic formation of complete molecules to prevent partial H2O.
- **Starvation**: If hydrogen threads arrive much faster than oxygen, they may starve. Use fair semaphores.
- **Barrier reuse**: CyclicBarrier automatically resets, making it suitable for forming multiple molecules.
- **Thread coordination**: Proper signaling is critical to prevent deadlock when threads arrive in different orders.

## Interview-Ready Answer

"The H2O problem requires coordinating 2 hydrogen and 1 oxygen thread to form each molecule. Use CyclicBarrier with 3 parties and semaphores to control access: 2 permits for hydrogen, 1 for oxygen. When the barrier trips (all 3 atoms arrive), form the molecule and release permits for the next molecule. Alternatively, track atom counts and release semaphores when enough atoms are available. The key is ensuring atomic molecule formation and preventing partial molecules or deadlock."
