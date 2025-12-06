package oops;

// Demo class
public class InheritanceDemo {
    public static void main() {
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
        Car vehicle1 = car;
        Vehicle vehicle2 = motorcycle;
        
        vehicle1.start();  // Calls Car's start()
        vehicle2.start();  // Calls Motorcycle's start()
        vehicle1.openTrunk();
    }
}
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
