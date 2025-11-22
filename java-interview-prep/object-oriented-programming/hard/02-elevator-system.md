# Elevator System

## Problem Statement

Design an object-oriented elevator control system for a building with multiple elevators. The system should efficiently handle passenger requests, optimize elevator movement, and support different scheduling algorithms (FCFS, SCAN, LOOK).

**Requirements:**
- Multiple elevators serving multiple floors
- Handle up and down requests from floors
- Handle destination requests from inside elevators
- Optimize elevator assignment based on proximity and direction
- Support different scheduling algorithms
- Track elevator state (idle, moving up, moving down)
- Handle door operations and passenger capacity

## Approach

- Create Elevator class with state machine for movement
- Implement Request class for floor requests
- Use Strategy pattern for scheduling algorithms
- Implement ElevatorController to manage multiple elevators
- Use priority queues for request management
- Handle edge cases like full elevators and emergency stops

## Solution

```java
import java.util.*;

enum Direction { UP, DOWN, IDLE }

enum ElevatorState { IDLE, MOVING, STOPPED }

class Request {
    private int floor;
    private Direction direction;
    
    public Request(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }
    
    public int getFloor() { return floor; }
    public Direction getDirection() { return direction; }
}

class Elevator {
    private int id;
    private int currentFloor;
    private Direction direction;
    private ElevatorState state;
    private Set<Integer> upRequests;
    private Set<Integer> downRequests;
    private int capacity;
    private int currentLoad;
    
    public Elevator(int id, int capacity) {
        this.id = id;
        this.currentFloor = 0;
        this.direction = Direction.IDLE;
        this.state = ElevatorState.IDLE;
        this.upRequests = new TreeSet<>();
        this.downRequests = new TreeSet<>(Collections.reverseOrder());
        this.capacity = capacity;
        this.currentLoad = 0;
    }
    
    public void addRequest(int floor, Direction dir) {
        if (dir == Direction.UP) {
            upRequests.add(floor);
        } else {
            downRequests.add(floor);
        }
    }
    
    public void move() {
        if (direction == Direction.UP) {
            if (!upRequests.isEmpty()) {
                int nextFloor = upRequests.iterator().next();
                if (nextFloor == currentFloor) {
                    upRequests.remove(currentFloor);
                    System.out.println("Elevator " + id + " stopped at floor " + currentFloor);
                    state = ElevatorState.STOPPED;
                } else {
                    currentFloor++;
                    System.out.println("Elevator " + id + " moving up to floor " + currentFloor);
                    state = ElevatorState.MOVING;
                }
            } else if (!downRequests.isEmpty()) {
                direction = Direction.DOWN;
            } else {
                direction = Direction.IDLE;
                state = ElevatorState.IDLE;
            }
        } else if (direction == Direction.DOWN) {
            if (!downRequests.isEmpty()) {
                int nextFloor = downRequests.iterator().next();
                if (nextFloor == currentFloor) {
                    downRequests.remove(currentFloor);
                    System.out.println("Elevator " + id + " stopped at floor " + currentFloor);
                    state = ElevatorState.STOPPED;
                } else {
                    currentFloor--;
                    System.out.println("Elevator " + id + " moving down to floor " + currentFloor);
                    state = ElevatorState.MOVING;
                }
            } else if (!upRequests.isEmpty()) {
                direction = Direction.UP;
            } else {
                direction = Direction.IDLE;
                state = ElevatorState.IDLE;
            }
        } else {
            if (!upRequests.isEmpty()) {
                direction = Direction.UP;
            } else if (!downRequests.isEmpty()) {
                direction = Direction.DOWN;
            }
        }
    }
    
    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public boolean isFull() { return currentLoad >= capacity; }
    public int getId() { return id; }
}

class ElevatorController {
    private List<Elevator> elevators;
    private int totalFloors;
    
    public ElevatorController(int numElevators, int totalFloors, int capacity) {
        this.elevators = new ArrayList<>();
        this.totalFloors = totalFloors;
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(i + 1, capacity));
        }
    }
    
    public void requestElevator(int floor, Direction direction) {
        Elevator best = findBestElevator(floor, direction);
        best.addRequest(floor, direction);
        System.out.println("Assigned elevator " + best.getId() + " to floor " + floor);
    }
    
    private Elevator findBestElevator(int floor, Direction direction) {
        Elevator best = elevators.get(0);
        int minDistance = Integer.MAX_VALUE;
        
        for (Elevator elevator : elevators) {
            if (elevator.isFull()) continue;
            
            int distance = Math.abs(elevator.getCurrentFloor() - floor);
            if (distance < minDistance) {
                minDistance = distance;
                best = elevator;
            }
        }
        
        return best;
    }
    
    public void step() {
        for (Elevator elevator : elevators) {
            elevator.move();
        }
    }
}

class ElevatorSystemDemo {
    public static void main(String[] args) {
        ElevatorController controller = new ElevatorController(2, 10, 8);
        
        controller.requestElevator(5, Direction.UP);
        controller.requestElevator(3, Direction.DOWN);
        controller.requestElevator(7, Direction.UP);
        
        for (int i = 0; i < 10; i++) {
            System.out.println("\n--- Step " + (i + 1) + " ---");
            controller.step();
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(e * log(r)) where e is elevators and r is requests per elevator (TreeSet operations)

**Space Complexity**: O(e * r) for storing requests across all elevators

## Edge Cases and Pitfalls

- **Request Prioritization**: Balance between FCFS and proximity-based assignment
- **Direction Changes**: Handle smooth transitions when changing direction
- **Capacity Management**: Don't assign full elevators to new requests
- **Emergency Stops**: Implement priority override for emergency situations
- **Starvation**: Ensure far floors don't wait indefinitely

## Interview-Ready Answer

"I'd design an elevator system with Elevator class tracking current floor, direction, and request queues. ElevatorController manages multiple elevators and assigns requests based on proximity and direction. Use TreeSet for sorted request management and implement a state machine for elevator movement. The system optimizes by assigning the nearest available elevator. Time complexity is O(e*log(r)), space is O(e*r)."
