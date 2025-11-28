# Traffic Light Controlled Intersection

## Problem Statement

Implement a traffic light system for a four-way intersection. Cars arrive from four directions (North, South, East, West) and can turn left, go straight, or turn right. Ensure that conflicting movements don't happen simultaneously and maximize throughput by allowing compatible movements to proceed together.

**Input**: Cars arriving from different directions with different turn intentions

**Output**: Safe and efficient traffic flow

**Constraints**: 
- Must prevent collisions
- Should maximize throughput
- Must handle all turn types

## Approach

- Identify conflicting movements (e.g., North straight conflicts with South straight)
- Group compatible movements that can proceed simultaneously
- Use semaphores or locks to control access to intersection
- Implement green light phases for different movement groups
- Ensure fairness to prevent starvation
- Use ReentrantLock with conditions for complex coordination

## Solution

```java
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

enum Direction {
    NORTH, SOUTH, EAST, WEST
}

enum TurnType {
    LEFT, STRAIGHT, RIGHT
}

class TrafficLight {
    private Lock lock = new ReentrantLock(true); // Fair lock
    private Condition northSouth = lock.newCondition();
    private Condition eastWest = lock.newCondition();
    
    private boolean northSouthGreen = true;
    private int carsInIntersection = 0;
    private static final int MAX_CARS = 3;
    
    public void carArrived(int carId, Direction direction, TurnType turn) 
            throws InterruptedException {
        lock.lock();
        try {
            boolean isNorthSouth = (direction == Direction.NORTH || direction == Direction.SOUTH);
            
            // Wait for green light and space in intersection
            while ((isNorthSouth && !northSouthGreen) || 
                   (!isNorthSouth && northSouthGreen) ||
                   carsInIntersection >= MAX_CARS) {
                if (isNorthSouth) {
                    northSouth.await();
                } else {
                    eastWest.await();
                }
            }
            
            carsInIntersection++;
            System.out.println("Car " + carId + " from " + direction + 
                             " turning " + turn + " entering intersection");
        } finally {
            lock.unlock();
        }
        
        // Simulate crossing intersection
        Thread.sleep((long)(Math.random() * 1000));
        
        lock.lock();
        try {
            carsInIntersection--;
            System.out.println("Car " + carId + " exited intersection");
            
            // Signal waiting cars
            if (northSouthGreen) {
                northSouth.signalAll();
            } else {
                eastWest.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void switchLight() {
        lock.lock();
        try {
            // Wait for intersection to clear
            while (carsInIntersection > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            northSouthGreen = !northSouthGreen;
            System.out.println("Light switched: " + 
                             (northSouthGreen ? "North-South GREEN" : "East-West GREEN"));
            
            if (northSouthGreen) {
                northSouth.signalAll();
            } else {
                eastWest.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}

// Advanced solution with movement compatibility
class AdvancedTrafficLight {
    private Lock lock = new ReentrantLock(true);
    private Set<String> activeMovements = ConcurrentHashMap.newKeySet();
    private static final int MAX_CONCURRENT = 4;
    
    public void carArrived(int carId, Direction direction, TurnType turn) 
            throws InterruptedException {
        String movement = direction + "-" + turn;
        
        lock.lock();
        try {
            // Wait until this movement is compatible with active movements
            while (!canProceed(direction, turn)) {
                lock.unlock();
                Thread.sleep(50);
                lock.lock();
            }
            
            activeMovements.add(movement);
            System.out.println("Car " + carId + ": " + movement + " entering");
        } finally {
            lock.unlock();
        }
        
        // Cross intersection
        Thread.sleep((long)(Math.random() * 1000));
        
        lock.lock();
        try {
            activeMovements.remove(movement);
            System.out.println("Car " + carId + ": " + movement + " exited");
        } finally {
            lock.unlock();
        }
    }
    
    private boolean canProceed(Direction direction, TurnType turn) {
        if (activeMovements.size() >= MAX_CONCURRENT) {
            return false;
        }
        
        // Right turns are always compatible
        if (turn == TurnType.RIGHT) {
            return true;
        }
        
        // Check for conflicts with active movements
        for (String active : activeMovements) {
            String[] parts = active.split("-");
            Direction activeDir = Direction.valueOf(parts[0]);
            TurnType activeTurn = TurnType.valueOf(parts[1]);
            
            if (hasConflict(direction, turn, activeDir, activeTurn)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean hasConflict(Direction d1, TurnType t1, Direction d2, TurnType t2) {
        // Opposite directions going straight conflict
        if (t1 == TurnType.STRAIGHT && t2 == TurnType.STRAIGHT) {
            if ((d1 == Direction.NORTH && d2 == Direction.SOUTH) ||
                (d1 == Direction.SOUTH && d2 == Direction.NORTH) ||
                (d1 == Direction.EAST && d2 == Direction.WEST) ||
                (d1 == Direction.WEST && d2 == Direction.EAST)) {
                return true;
            }
        }
        
        // Left turns conflict with opposite straight
        if (t1 == TurnType.LEFT && t2 == TurnType.STRAIGHT) {
            if ((d1 == Direction.NORTH && d2 == Direction.SOUTH) ||
                (d1 == Direction.SOUTH && d2 == Direction.NORTH) ||
                (d1 == Direction.EAST && d2 == Direction.WEST) ||
                (d1 == Direction.WEST && d2 == Direction.EAST)) {
                return true;
            }
        }
        
        return false;
    }
}

public class TrafficLightDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Basic Traffic Light ===");
        testBasicTrafficLight();
        
        System.out.println("\n=== Advanced Traffic Light ===");
        testAdvancedTrafficLight();
    }
    
    private static void testBasicTrafficLight() throws InterruptedException {
        TrafficLight light = new TrafficLight();
        
        // Light switcher thread
        Thread switcher = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(3000);
                    light.switchLight();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        switcher.start();
        
        // Car threads
        Thread[] cars = new Thread[12];
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        TurnType[] turns = {TurnType.LEFT, TurnType.STRAIGHT, TurnType.RIGHT};
        
        for (int i = 0; i < cars.length; i++) {
            final int carId = i;
            final Direction dir = directions[i % 4];
            final TurnType turn = turns[i % 3];
            
            cars[i] = new Thread(() -> {
                try {
                    light.carArrived(carId, dir, turn);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            cars[i].start();
            Thread.sleep(200);
        }
        
        for (Thread car : cars) {
            car.join();
        }
        switcher.interrupt();
    }
    
    private static void testAdvancedTrafficLight() throws InterruptedException {
        AdvancedTrafficLight light = new AdvancedTrafficLight();
        
        Thread[] cars = new Thread[10];
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        TurnType[] turns = {TurnType.LEFT, TurnType.STRAIGHT, TurnType.RIGHT};
        
        for (int i = 0; i < cars.length; i++) {
            final int carId = i;
            final Direction dir = directions[i % 4];
            final TurnType turn = turns[i % 3];
            
            cars[i] = new Thread(() -> {
                try {
                    light.carArrived(carId, dir, turn);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            cars[i].start();
            Thread.sleep(300);
        }
        
        for (Thread car : cars) {
            car.join();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) per car operation

**Space Complexity**: O(n) where n is maximum concurrent cars

## Edge Cases and Pitfalls

- **Deadlock prevention**: Use fair locks and proper signaling to prevent deadlock when multiple directions wait.
- **Starvation**: Fair locks ensure no direction starves indefinitely.
- **Movement compatibility**: Right turns rarely conflict, straight movements from perpendicular directions are compatible, left turns conflict with opposite straight.
- **Throughput optimization**: Allow compatible movements simultaneously rather than strict alternation.

## Interview-Ready Answer

"A traffic light system coordinates cars from four directions with different turn types. Use ReentrantLock with conditions to control access. Basic solution alternates between North-South and East-West green lights. Advanced solution checks movement compatibility - right turns rarely conflict, perpendicular straight movements are compatible, but opposite straight movements and left turns with opposite straight conflict. Use fair locks to prevent starvation and allow multiple compatible cars simultaneously for better throughput."
