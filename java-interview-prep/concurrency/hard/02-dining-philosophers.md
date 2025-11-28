# Dining Philosophers Problem

## Problem Statement

Implement a solution to the classic Dining Philosophers problem that avoids deadlock. Five philosophers sit at a round table with five forks between them. Each philosopher needs two forks to eat. Implement a solution that prevents deadlock and starvation while allowing maximum concurrency.

**Input**: Five philosophers and five forks

**Output**: Deadlock-free dining simulation

**Constraints**: 
- Must prevent deadlock
- Should prevent starvation
- Must allow concurrent eating

## Approach

- Classic synchronization problem demonstrating deadlock
- Solutions: resource ordering, semaphore limiting, Chandy-Misra algorithm
- Resource ordering: number forks, always acquire lower-numbered fork first
- Semaphore: limit concurrent philosophers to n-1
- Use ReentrantLock for explicit lock management
- Ensure fairness to prevent starvation

## Solution

```java
import java.util.concurrent.locks.*;
import java.util.concurrent.Semaphore;

// Fork representation
class Fork {
    private final int id;
    private final ReentrantLock lock = new ReentrantLock();
    
    public Fork(int id) {
        this.id = id;
    }
    
    public boolean pickUp() {
        return lock.tryLock();
    }
    
    public void putDown() {
        lock.unlock();
    }
    
    public int getId() {
        return id;
    }
}

// Philosopher using resource ordering
class Philosopher implements Runnable {
    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;
    private int mealsEaten = 0;
    
    public Philosopher(int id, Fork leftFork, Fork rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }
    
    @Override
    public void run() {
        try {
            while (mealsEaten < 3) {
                think();
                eat();
            }
            System.out.println("Philosopher " + id + " finished eating " + mealsEaten + " meals");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void think() throws InterruptedException {
        System.out.println("Philosopher " + id + " is thinking");
        Thread.sleep((long)(Math.random() * 1000));
    }
    
    private void eat() throws InterruptedException {
        // Resource ordering: always pick up lower-numbered fork first
        Fork first = leftFork.getId() < rightFork.getId() ? leftFork : rightFork;
        Fork second = leftFork.getId() < rightFork.getId() ? rightFork : leftFork;
        
        first.pickUp();
        System.out.println("Philosopher " + id + " picked up fork " + first.getId());
        
        second.pickUp();
        System.out.println("Philosopher " + id + " picked up fork " + second.getId());
        
        System.out.println("Philosopher " + id + " is eating");
        Thread.sleep((long)(Math.random() * 1000));
        mealsEaten++;
        
        second.putDown();
        System.out.println("Philosopher " + id + " put down fork " + second.getId());
        
        first.putDown();
        System.out.println("Philosopher " + id + " put down fork " + first.getId());
    }
}

// Alternative solution using semaphore
class PhilosopherWithSemaphore implements Runnable {
    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;
    private final Semaphore semaphore;
    private int mealsEaten = 0;
    
    public PhilosopherWithSemaphore(int id, Fork leftFork, Fork rightFork, Semaphore semaphore) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.semaphore = semaphore;
    }
    
    @Override
    public void run() {
        try {
            while (mealsEaten < 3) {
                think();
                semaphore.acquire(); // Limit concurrent philosophers
                eat();
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void think() throws InterruptedException {
        System.out.println("Philosopher " + id + " is thinking");
        Thread.sleep((long)(Math.random() * 500));
    }
    
    private void eat() throws InterruptedException {
        leftFork.pickUp();
        rightFork.pickUp();
        
        System.out.println("Philosopher " + id + " is eating");
        Thread.sleep((long)(Math.random() * 500));
        mealsEaten++;
        
        rightFork.putDown();
        leftFork.putDown();
    }
}

public class DiningPhilosophersDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Resource Ordering Solution ===");
        testResourceOrdering();
        
        System.out.println("\n=== Semaphore Solution ===");
        testSemaphoreSolution();
    }
    
    private static void testResourceOrdering() throws InterruptedException {
        int numPhilosophers = 5;
        Fork[] forks = new Fork[numPhilosophers];
        Philosopher[] philosophers = new Philosopher[numPhilosophers];
        Thread[] threads = new Thread[numPhilosophers];
        
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < numPhilosophers; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % numPhilosophers];
            philosophers[i] = new Philosopher(i, leftFork, rightFork);
            threads[i] = new Thread(philosophers[i], "Philosopher-" + i);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("All philosophers finished eating");
    }
    
    private static void testSemaphoreSolution() throws InterruptedException {
        int numPhilosophers = 5;
        Fork[] forks = new Fork[numPhilosophers];
        Semaphore semaphore = new Semaphore(numPhilosophers - 1); // Allow n-1 concurrent
        Thread[] threads = new Thread[numPhilosophers];
        
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < numPhilosophers; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % numPhilosophers];
            PhilosopherWithSemaphore philosopher = 
                new PhilosopherWithSemaphore(i, leftFork, rightFork, semaphore);
            threads[i] = new Thread(philosopher, "Philosopher-" + i);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("All philosophers finished eating");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n*m) where n is philosophers and m is meals per philosopher

**Space Complexity**: O(n) for forks and philosopher objects

## Edge Cases and Pitfalls

- **Circular wait deadlock**: Without prevention, all philosophers can pick up left fork simultaneously, causing deadlock.
- **Starvation**: Some philosophers may never eat if others are faster. Use fair locks or semaphores to prevent this.
- **Resource ordering breaks symmetry**: Numbering resources and acquiring in order prevents circular wait.
- **Semaphore limit**: Allowing only n-1 philosophers to eat simultaneously guarantees at least one can acquire both forks.

## Interview-Ready Answer

"The Dining Philosophers problem demonstrates deadlock in resource allocation. Five philosophers need two forks to eat, but only five forks exist. Solutions include resource ordering (always acquire lower-numbered fork first), limiting concurrent philosophers to n-1 using a semaphore, or using tryLock with backoff. Resource ordering is most efficient as it breaks the circular wait condition. The problem illustrates key concurrency concepts: deadlock, starvation, and the importance of consistent resource acquisition order."
