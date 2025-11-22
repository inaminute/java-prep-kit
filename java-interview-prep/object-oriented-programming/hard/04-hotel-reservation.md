# Hotel Reservation System

## Problem Statement

Design a hotel reservation system supporting room booking, cancellations, different room types, pricing strategies, and guest management. Handle overbooking prevention and concurrent reservations.

## Approach

- Create Room, Reservation, Guest, and Hotel classes
- Use Strategy pattern for pricing (seasonal, weekend, promotional)
- Implement thread-safe booking to prevent double-booking
- Track room availability with date ranges
- Support search by date, room type, and price range

## Solution

```java
import java.time.*;
import java.util.*;

enum RoomType { SINGLE, DOUBLE, SUITE }
enum ReservationStatus { CONFIRMED, CANCELLED, COMPLETED }

class Room {
    private String roomNumber;
    private RoomType type;
    private double basePrice;
    
    public Room(String roomNumber, RoomType type, double basePrice) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.basePrice = basePrice;
    }
    
    public String getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public double getBasePrice() { return basePrice; }
}

class Reservation {
    private String id;
    private Room room;
    private String guestId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private ReservationStatus status;
    private double totalPrice;
    
    public Reservation(String id, Room room, String guestId, LocalDate checkIn, LocalDate checkOut, double totalPrice) {
        this.id = id;
        this.room = room;
        this.guestId = guestId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = ReservationStatus.CONFIRMED;
        this.totalPrice = totalPrice;
    }
    
    public void cancel() { this.status = ReservationStatus.CANCELLED; }
    public String getId() { return id; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public ReservationStatus getStatus() { return status; }
}

class Hotel {
    private List<Room> rooms;
    private Map<String, Reservation> reservations;
    private int reservationCounter;
    
    public Hotel() {
        this.rooms = new ArrayList<>();
        this.reservations = new HashMap<>();
        this.reservationCounter = 1;
    }
    
    public void addRoom(Room room) { rooms.add(room); }
    
    public synchronized Reservation bookRoom(RoomType type, String guestId, LocalDate checkIn, LocalDate checkOut) {
        Room availableRoom = findAvailableRoom(type, checkIn, checkOut);
        if (availableRoom == null) {
            throw new IllegalStateException("No rooms available");
        }
        
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = nights * availableRoom.getBasePrice();
        
        String resId = "RES-" + (reservationCounter++);
        Reservation reservation = new Reservation(resId, availableRoom, guestId, checkIn, checkOut, totalPrice);
        reservations.put(resId, reservation);
        
        System.out.println("Booked room " + availableRoom.getRoomNumber() + " for " + nights + " nights. Total: $" + totalPrice);
        return reservation;
    }
    
    private Room findAvailableRoom(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        for (Room room : rooms) {
            if (room.getType() == type && isRoomAvailable(room, checkIn, checkOut)) {
                return room;
            }
        }
        return null;
    }
    
    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation res : reservations.values()) {
            if (res.getRoom().equals(room) && res.getStatus() == ReservationStatus.CONFIRMED) {
                if (!(checkOut.isBefore(res.getCheckIn()) || checkIn.isAfter(res.getCheckOut()))) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void cancelReservation(String resId) {
        Reservation res = reservations.get(resId);
        if (res != null) {
            res.cancel();
            System.out.println("Cancelled reservation " + resId);
        }
    }
}

class HotelDemo {
    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room("101", RoomType.SINGLE, 100));
        hotel.addRoom(new Room("102", RoomType.DOUBLE, 150));
        hotel.addRoom(new Room("201", RoomType.SUITE, 300));
        
        Reservation res1 = hotel.bookRoom(RoomType.DOUBLE, "G001", LocalDate.now(), LocalDate.now().plusDays(3));
        Reservation res2 = hotel.bookRoom(RoomType.SUITE, "G002", LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
        
        hotel.cancelReservation(res1.getId());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(r * n) for booking where r is reservations and n is rooms
**Space Complexity**: O(r + n) for storing reservations and rooms

## Edge Cases and Pitfalls

- **Date Overlap**: Carefully check for overlapping reservations
- **Concurrent Booking**: Use synchronized methods to prevent double-booking
- **Cancellation Policy**: Implement refund logic based on cancellation timing
- **Overbooking**: Prevent booking more rooms than available
- **Price Calculation**: Handle seasonal pricing and discounts

## Interview-Ready Answer

"I'd design a hotel system with Room, Reservation, and Hotel classes. Use synchronized booking to prevent double-booking and check date overlaps for availability. Track reservations with status (confirmed/cancelled) and calculate prices based on nights and room type. Support search by room type and date range. Time complexity is O(r*n) for availability checks, space is O(r+n)."
