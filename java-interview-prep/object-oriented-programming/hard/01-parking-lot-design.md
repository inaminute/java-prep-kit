# Parking Lot Design

## Problem Statement

Design an object-oriented parking lot system that can handle multiple floors, different vehicle types (motorcycle, car, bus), and various parking spot sizes. The system should support parking and unparking vehicles, tracking available spots, and calculating parking fees based on duration.

**Requirements:**
- Support multiple floors with different spot types (compact, regular, large)
- Handle different vehicle types with size restrictions
- Track available and occupied spots
- Calculate parking fees based on vehicle type and duration
- Support finding nearest available spot
- Handle concurrent parking requests

## Approach

- Identify key entities: ParkingLot, Floor, ParkingSpot, Vehicle, Ticket
- Use enums for vehicle types and spot types
- Implement strategy pattern for fee calculation
- Use factory pattern for vehicle creation
- Implement observer pattern for spot availability notifications
- Design thread-safe operations for concurrent access

## Solution

```java
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

// Enums
enum VehicleType {
    MOTORCYCLE, CAR, BUS
}

enum SpotType {
    COMPACT, REGULAR, LARGE
}

enum ParkingSpotStatus {
    AVAILABLE, OCCUPIED
}

// Vehicle hierarchy
abstract class Vehicle {
    protected String licensePlate;
    protected VehicleType type;
    
    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }
    
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
    public abstract int getRequiredSpots();
}

class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }
    
    @Override
    public int getRequiredSpots() { return 1; }
}

class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }
    
    @Override
    public int getRequiredSpots() { return 1; }
}

class Bus extends Vehicle {
    public Bus(String licensePlate) {
        super(licensePlate, VehicleType.BUS);
    }
    
    @Override
    public int getRequiredSpots() { return 5; }  // Bus needs 5 spots
}

// Parking Spot
class ParkingSpot {
    private int id;
    private SpotType type;
    private ParkingSpotStatus status;
    private Vehicle parkedVehicle;
    private int floor;
    
    public ParkingSpot(int id, SpotType type, int floor) {
        this.id = id;
        this.type = type;
        this.status = ParkingSpotStatus.AVAILABLE;
        this.floor = floor;
    }
    
    public synchronized boolean isAvailable() {
        return status == ParkingSpotStatus.AVAILABLE;
    }
    
    public synchronized boolean canFitVehicle(Vehicle vehicle) {
        if (!isAvailable()) return false;
        
        switch (type) {
            case COMPACT:
                return vehicle.getType() == VehicleType.MOTORCYCLE;
            case REGULAR:
                return vehicle.getType() == VehicleType.MOTORCYCLE || 
                       vehicle.getType() == VehicleType.CAR;
            case LARGE:
                return true;  // Can fit any vehicle
            default:
                return false;
        }
    }
    
    public synchronized void parkVehicle(Vehicle vehicle) {
        if (!canFitVehicle(vehicle)) {
            throw new IllegalStateException("Vehicle cannot fit in this spot");
        }
        this.parkedVehicle = vehicle;
        this.status = ParkingSpotStatus.OCCUPIED;
    }
    
    public synchronized void removeVehicle() {
        this.parkedVehicle = null;
        this.status = ParkingSpotStatus.AVAILABLE;
    }
    
    public int getId() { return id; }
    public SpotType getType() { return type; }
    public int getFloor() { return floor; }
    public Vehicle getParkedVehicle() { return parkedVehicle; }
}

// Parking Ticket
class ParkingTicket {
    private String ticketId;
    private Vehicle vehicle;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private List<ParkingSpot> assignedSpots;
    private double fee;
    
    public ParkingTicket(String ticketId, Vehicle vehicle, List<ParkingSpot> spots) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.assignedSpots = spots;
        this.entryTime = LocalDateTime.now();
    }
    
    public void markExit() {
        this.exitTime = LocalDateTime.now();
    }
    
    public long getParkingDurationMinutes() {
        LocalDateTime end = exitTime != null ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end).toMinutes();
    }
    
    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public List<ParkingSpot> getAssignedSpots() { return assignedSpots; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
}

// Fee calculation strategy
interface FeeCalculationStrategy {
    double calculateFee(ParkingTicket ticket);
}

class HourlyFeeStrategy implements FeeCalculationStrategy {
    private static final double MOTORCYCLE_RATE = 2.0;
    private static final double CAR_RATE = 4.0;
    private static final double BUS_RATE = 10.0;
    
    @Override
    public double calculateFee(ParkingTicket ticket) {
        long minutes = ticket.getParkingDurationMinutes();
        double hours = Math.ceil(minutes / 60.0);
        
        double rate;
        switch (ticket.getVehicle().getType()) {
            case MOTORCYCLE:
                rate = MOTORCYCLE_RATE;
                break;
            case CAR:
                rate = CAR_RATE;
                break;
            case BUS:
                rate = BUS_RATE;
                break;
            default:
                rate = CAR_RATE;
        }
        
        return hours * rate;
    }
}

// Parking Floor
class ParkingFloor {
    private int floorNumber;
    private List<ParkingSpot> spots;
    
    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }
    
    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }
    
    public List<ParkingSpot> findAvailableSpots(Vehicle vehicle, int count) {
        List<ParkingSpot> availableSpots = new ArrayList<>();
        
        for (ParkingSpot spot : spots) {
            if (spot.canFitVehicle(vehicle)) {
                availableSpots.add(spot);
                if (availableSpots.size() == count) {
                    break;
                }
            }
        }
        
        return availableSpots.size() == count ? availableSpots : new ArrayList<>();
    }
    
    public int getAvailableSpotCount() {
        return (int) spots.stream().filter(ParkingSpot::isAvailable).count();
    }
    
    public int getFloorNumber() { return floorNumber; }
}

// Main Parking Lot
class ParkingLot {
    private static ParkingLot instance;
    private String name;
    private List<ParkingFloor> floors;
    private Map<String, ParkingTicket> activeTickets;
    private FeeCalculationStrategy feeStrategy;
    private int ticketCounter;
    
    private ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
        this.activeTickets = new HashMap<>();
        this.feeStrategy = new HourlyFeeStrategy();
        this.ticketCounter = 1;
    }
    
    public static synchronized ParkingLot getInstance(String name) {
        if (instance == null) {
            instance = new ParkingLot(name);
        }
        return instance;
    }
    
    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }
    
    public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
        int requiredSpots = vehicle.getRequiredSpots();
        
        // Find available spots
        for (ParkingFloor floor : floors) {
            List<ParkingSpot> availableSpots = floor.findAvailableSpots(vehicle, requiredSpots);
            
            if (!availableSpots.isEmpty()) {
                // Park vehicle in found spots
                for (ParkingSpot spot : availableSpots) {
                    spot.parkVehicle(vehicle);
                }
                
                // Create ticket
                String ticketId = "TICKET-" + (ticketCounter++);
                ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, availableSpots);
                activeTickets.put(ticketId, ticket);
                
                System.out.println("Vehicle " + vehicle.getLicensePlate() + " parked on floor " + 
                                 floor.getFloorNumber() + ", spots: " + 
                                 availableSpots.stream().map(s -> String.valueOf(s.getId()))
                                 .reduce((a, b) -> a + ", " + b).orElse(""));
                
                return ticket;
            }
        }
        
        throw new IllegalStateException("No available spots for vehicle type: " + vehicle.getType());
    }
    
    public synchronized double unparkVehicle(String ticketId) {
        ParkingTicket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Invalid ticket ID");
        }
        
        // Mark exit time
        ticket.markExit();
        
        // Calculate fee
        double fee = feeStrategy.calculateFee(ticket);
        ticket.setFee(fee);
        
        // Free up spots
        for (ParkingSpot spot : ticket.getAssignedSpots()) {
            spot.removeVehicle();
        }
        
        // Remove from active tickets
        activeTickets.remove(ticketId);
        
        System.out.println("Vehicle " + ticket.getVehicle().getLicensePlate() + 
                         " unparked. Duration: " + ticket.getParkingDurationMinutes() + 
                         " minutes, Fee: $" + fee);
        
        return fee;
    }
    
    public void displayAvailability() {
        System.out.println("\n=== Parking Lot: " + name + " ===");
        for (ParkingFloor floor : floors) {
            System.out.println("Floor " + floor.getFloorNumber() + ": " + 
                             floor.getAvailableSpotCount() + " spots available");
        }
    }
}

// Demo
class ParkingLotDemo {
    public static void main(String[] args) throws InterruptedException {
        // Create parking lot
        ParkingLot parkingLot = ParkingLot.getInstance("Downtown Parking");
        
        // Create floors with spots
        ParkingFloor floor1 = new ParkingFloor(1);
        for (int i = 1; i <= 5; i++) {
            floor1.addSpot(new ParkingSpot(i, SpotType.COMPACT, 1));
        }
        for (int i = 6; i <= 15; i++) {
            floor1.addSpot(new ParkingSpot(i, SpotType.REGULAR, 1));
        }
        for (int i = 16; i <= 20; i++) {
            floor1.addSpot(new ParkingSpot(i, SpotType.LARGE, 1));
        }
        
        parkingLot.addFloor(floor1);
        
        parkingLot.displayAvailability();
        
        // Park vehicles
        Vehicle motorcycle = new Motorcycle("MOTO-123");
        Vehicle car1 = new Car("CAR-456");
        Vehicle car2 = new Car("CAR-789");
        Vehicle bus = new Bus("BUS-001");
        
        ParkingTicket ticket1 = parkingLot.parkVehicle(motorcycle);
        ParkingTicket ticket2 = parkingLot.parkVehicle(car1);
        ParkingTicket ticket3 = parkingLot.parkVehicle(car2);
        ParkingTicket ticket4 = parkingLot.parkVehicle(bus);
        
        parkingLot.displayAvailability();
        
        // Simulate time passing
        Thread.sleep(2000);
        
        // Unpark vehicles
        parkingLot.unparkVehicle(ticket1.getTicketId());
        parkingLot.unparkVehicle(ticket2.getTicketId());
        
        parkingLot.displayAvailability();
    }
}
```

## Complexity Analysis

**Time Complexity**: 
- parkVehicle(): O(f * s) where f is number of floors and s is spots per floor
- unparkVehicle(): O(1) with HashMap lookup
- displayAvailability(): O(f * s) to count available spots

**Space Complexity**: O(f * s + t) where f is floors, s is spots per floor, and t is active tickets

## Edge Cases and Pitfalls

- **Concurrent Access**: Use synchronized methods to prevent race conditions when multiple vehicles park simultaneously
- **Bus Parking**: Buses need multiple consecutive spots, which adds complexity to spot allocation
- **Spot Size Matching**: Ensure vehicles can only park in appropriate spot sizes
- **Ticket Management**: Use unique ticket IDs and validate tickets before unparking
- **Fee Calculation**: Handle edge cases like minimum parking time and maximum daily rates
- **Spot Availability**: Update availability in real-time and handle cases where spots become unavailable during allocation

## Interview-Ready Answer

"I'd design a parking lot system with key classes: ParkingLot (Singleton), ParkingFloor, ParkingSpot, Vehicle hierarchy, and ParkingTicket. Use enums for vehicle and spot types. Implement Strategy pattern for fee calculation and ensure thread safety with synchronized methods. The system finds available spots matching vehicle requirements, handles buses needing multiple spots, and calculates fees based on duration. Time complexity is O(f*s) for parking, space is O(f*s+t) for floors, spots, and tickets."
