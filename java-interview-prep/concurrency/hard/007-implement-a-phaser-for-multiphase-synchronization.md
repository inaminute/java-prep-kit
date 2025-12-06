# Implement a Phaser for multi-phase synchronization

## Problem Statement

Use Phaser to coordinate multiple phases of parallel computation with dynamic thread registration.

## Approach

- **Phase advancement**: Coordinate threads across multiple phases
- **Dynamic registration**: Threads can register/deregister dynamically
- **Termination**: Can terminate after specific phase
- **Tiered phasers**: Create hierarchies for scalability
- **More flexible**: Than CountDownLatch or CyclicBarrier

## Solution

```java
import java.util.concurrent.Phaser;

// Basic Phaser example
class BasicPhaserExample {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(1); // Register main thread
        
        for (int i = 0; i < 3; i++) {
            final int workerId = i;
            phaser.register(); // Register worker
            
            new Thread(() -> {
                System.out.println("Worker " + workerId + " phase 1");
                phaser.arriveAndAwaitAdvance(); // Wait for all
                
                System.out.println("Worker " + workerId + " phase 2");
                phaser.arriveAndAwaitAdvance();
                
                System.out.println("Worker " + workerId + " phase 3");
                phaser.arriveAndDeregister(); // Done
            }).start();
        }
        
        phaser.arriveAndDeregister(); // Main thread done
    }
}

// Multi-phase computation
class MultiPhaseComputation {
    public static void main(String[] args) {
        int parties = 4;
        Phaser phaser = new Phaser(parties) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("=== Phase " + phase + " completed ===");
                return phase >= 2 || registeredParties == 0; // Terminate after phase 2
            }
        };
        
        for (int i = 0; i < parties; i++) {
            final int workerId = i;
            new Thread(() -> {
                for (int phase = 0; phase <= 2; phase++) {
                    System.out.println("Worker " + workerId + " executing phase " + phase);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    phaser.arriveAndAwaitAdvance();
                }
            }).start();
        }
    }
}

// Dynamic registration
class DynamicRegistration {
    public static void main(String[] args) throws InterruptedException {
        Phaser phaser = new Phaser(1); // Start with main thread
        
        System.out.println("Starting with " + phaser.getRegisteredParties() + " parties");
        
        // Dynamically add workers
        for (int i = 0; i < 3; i++) {
            final int workerId = i;
            Thread.sleep(500);
            
            phaser.register();
            System.out.println("Registered worker " + workerId + 
                ", total parties: " + phaser.getRegisteredParties());
            
            new Thread(() -> {
                for (int phase = 0; phase < 3; phase++) {
                    System.out.println("Worker " + workerId + " phase " + phase);
                    phaser.arriveAndAwaitAdvance();
                }
                phaser.arriveAndDeregister();
                System.out.println("Worker " + workerId + " deregistered");
            }).start();
        }
        
        // Main thread participates
        for (int phase = 0; phase < 3; phase++) {
            System.out.println("Main thread phase " + phase);
            phaser.arriveAndAwaitAdvance();
        }
        
        phaser.arriveAndDeregister();
        System.out.println("Main thread done");
    }
}

// Tiered Phaser for scalability
class TieredPhaserExample {
    public static void main(String[] args) {
        int workersPerGroup = 3;
        int groups = 3;
        
        Phaser mainPhaser = new Phaser();
        
        for (int g = 0; g < groups; g++) {
            Phaser groupPhaser = new Phaser(mainPhaser); // Child phaser
            
            for (int w = 0; w < workersPerGroup; w++) {
                final int groupId = g;
                final int workerId = w;
                groupPhaser.register();
                
                new Thread(() -> {
                    for (int phase = 0; phase < 3; phase++) {
                        System.out.println("Group " + groupId + " Worker " + 
                            workerId + " phase " + phase);
                        groupPhaser.arriveAndAwaitAdvance();
                    }
                    groupPhaser.arriveAndDeregister();
                }).start();
            }
        }
    }
}

// Practical example: Parallel merge sort
class ParallelMergeSort {
    private static final int THRESHOLD = 1000;
    
    static class SortTask implements Runnable {
        private int[] array;
        private int start, end;
        private Phaser phaser;
        
        SortTask(int[] array, int start, int end, Phaser phaser) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.phaser = phaser;
            phaser.register();
        }
        
        @Override
        public void run() {
            if (end - start <= THRESHOLD) {
                java.util.Arrays.sort(array, start, end);
            } else {
                int mid = start + (end - start) / 2;
                new Thread(new SortTask(array, start, mid, phaser)).start();
                new Thread(new SortTask(array, mid, end, phaser)).start();
            }
            phaser.arriveAndDeregister();
        }
    }
    
    public static void sort(int[] array) {
        Phaser phaser = new Phaser(1);
        new Thread(new SortTask(array, 0, array.length, phaser)).start();
        phaser.arriveAndAwaitAdvance();
    }
}
```

## Key Methods

| Method | Description |
|--------|-------------|
| `register()` | Add a party |
| `arriveAndAwaitAdvance()` | Arrive and wait for others |
| `arriveAndDeregister()` | Arrive and leave |
| `arrive()` | Arrive without waiting |
| `awaitAdvance(phase)` | Wait for specific phase |

## Phaser vs Others

| Feature | Phaser | CountDownLatch | CyclicBarrier |
|---------|--------|----------------|---------------|
| Reusable | Yes | No | Yes |
| Dynamic parties | Yes | No | No |
| Phases | Multiple | One | Multiple |
| Termination | Controllable | Automatic | Manual |

## Complexity Analysis

**Time Complexity**: O(1) for arrive/register operations

**Space Complexity**: O(parties)

## Edge Cases and Pitfalls

- **Forgetting to deregister**: Phaser never terminates
- **Phase number overflow**: Wraps around at Integer.MAX_VALUE
- **Termination condition**: Must implement onAdvance() for termination
- **Common Pitfall**: Not registering before starting work

## Interview-Ready Answer

"Phaser is a flexible synchronization barrier for multi-phase parallel algorithms. Unlike CountDownLatch (one-time) or CyclicBarrier (fixed parties), Phaser supports dynamic registration/deregistration and multiple phases. Threads call arriveAndAwaitAdvance() to synchronize at phase boundaries. You can override onAdvance() to control termination. It's ideal for iterative parallel algorithms with varying numbers of participants."

**Tags**: phaser, multi-phase, synchronizers
