# Inheritance Basics

## Problem Statement

Demonstrate the fundamental concepts of inheritance in Java by creating a class hierarchy for vehicles. Create a base class `Vehicle` with common properties and methods, and derive specific vehicle types (`Car`, `Motorcycle`) that inherit from the base class and add their own specific features. Show how inheritance promotes code reuse and establishes an "is-a" relationship.

**Requirements:**
- Create a base Vehicle class with common properties (brand, model, year)
- Implement derived classes that extend Vehicle
- Override methods to provide specific behavior
- Use the super keyword to access parent class members
- Demonstrate constructor chaining

## Approach

- Define a Vehicle base class with common attributes and methods
- Use protected or public access modifiers for inherited members
- Create derived classes using the 'extends' keyword
- Call parent constructor using super() as the first statement in child constructor
- Override methods in child classes to provide specialized behavior
- Use @Override annotation for clarity and compile-time checking
- Demonstrate polymorphism by treating child objects as parent type

## Solution

```java
// Base class
class Vehicle {
    protected String brand;
    protected String model;
    protected int year;
    
    // Constructor
    public Vehicle(String brand, String model, int year) {
        this.brand = brand;
        this.model = model;
        this.year = year;
    }
    
    // Common method
    public void start() {
        System.out.println("Vehicle is starting...");
    }
    
    public void stop() {
        System.out.println("Vehicle is stopping...");
    }
    
    public void displayInfo() {
        System.out.println("Brand: " + brand + ", Model: " + model + ", Year: " + year);
    }
}

// Derived class - Car
class Car extends Vehicle {
    private int numberOfDoors;
    private boolean hasAirConditioning;
    
    // Constructor with constructor chaining
    public Car(String brand, String model, int year, int numberOfDoors, boolean hasAirConditioning) {
        super(brand, model, year);  // Call parent constructor
        this.numberOfDoors = numberOfDoors;
        this.hasAirConditioning = hasAirConditioning;
    }
    
    // Override parent method
    @Override
    public void start() {
        System.out.println("Car engine starting with key ignition...");
    }
    
    // Additional method specific to Car
    public void openTrunk() {
        System.out.println("Trunk is opening...");
    }
    
    @Override
    public void displayInfo() {
        super.displayInfo();  // Call parent method
        System.out.println("Doors: " + numberOfDoors + ", AC: " + hasAirConditioning);
    }
}

// Derived class - Motorcycle
class Motorcycle extends Vehicle {
    private boolean hasSidecar;
    private String engineType;
    
    public Motorcycle(String brand, String model, int year, boolean hasSidecar, String engineType) {
        super(brand, model, year);
        this.hasSidecar = hasSidecar;
        this.engineType = engineType;
    }
    
    @Override
    public void start() {
        System.out.println("Motorcycle starting with kick/button start...");
    }
    
    public void wheelie() {
        System.out.println("Performing a wheelie!");
    }
    
    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Sidecar: " + hasSidecar + ", Engine: " + engineType);
    }
}

// Demo class
class InheritanceDemo {
    public static void main(String[] args) {
        // Create objects
        Car car = new Car("Toyota", "Camry", 2023, 4, true);
        Motorcycle motorcycle = new Motorcycle("Harley-Davidson", "Street 750", 2022, false, "V-Twin");
        
        // Demonstrate inheritance
        car.displayInfo();
        car.start();
        car.openTrunk();
        car.stop();
        
        System.out.println();
        
        motorcycle.displayInfo();
        motorcycle.start();
        motorcycle.wheelie();
        motorcycle.stop();
        
        System.out.println();
        
        // Demonstrate polymorphism
        Vehicle vehicle1 = car;
        Vehicle vehicle2 = motorcycle;
        
        vehicle1.start();  // Calls Car's start()
        vehicle2.start();  // Calls Motorcycle's start()
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Creating objects and calling methods are constant time operations. Method dispatch in inheritance is resolved at compile time (for non-virtual methods) or runtime (for overridden methods) but still O(1).

**Space Complexity**: O(1) - Each object occupies memory for its own fields plus inherited fields. The space is proportional to the number of fields, which is constant for a given class.

## Edge Cases and Pitfalls

- **Constructor Chaining**: Always call super() as the first statement in the child constructor. If not explicitly called, Java automatically calls the no-arg parent constructor, which may not exist.
- **Access Modifiers**: Private members are not inherited. Use protected for members that should be accessible to subclasses but not to external classes.
- **Method Overriding Rules**: Overridden methods must have the same signature, return type (or covariant), and cannot have more restrictive access modifiers.
- **super Keyword**: Use super.method() to call parent implementation when overriding. Forgetting this can lead to loss of parent functionality.
- **Multiple Inheritance**: Java doesn't support multiple inheritance of classes (only interfaces) to avoid the diamond problem.

## Interview-Ready Answer

"Inheritance establishes an 'is-a' relationship where child classes inherit properties and methods from a parent class. I'd create a Vehicle base class with common attributes, then extend it with Car and Motorcycle classes that add specific features. Child constructors use super() to call parent constructors, and methods can be overridden with @Override annotation. This promotes code reuse and enables polymorphism. Time and space complexity are O(1) for object operations."
