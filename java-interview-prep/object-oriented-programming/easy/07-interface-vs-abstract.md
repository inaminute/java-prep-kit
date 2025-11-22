# Interface vs Abstract Class

## Problem Statement

Explain and demonstrate the differences between interfaces and abstract classes in Java by implementing a shape hierarchy using both approaches. Show when to use each construct and highlight their key differences in terms of multiple inheritance, method implementation, fields, and design intent.

**Requirements:**
- Create an example using an interface
- Create an example using an abstract class
- Demonstrate multiple interface implementation
- Show abstract class with partial implementation
- Explain when to use each approach

## Approach

- Define an interface with method signatures only (pre-Java 8 style)
- Create an abstract class with both abstract and concrete methods
- Implement multiple interfaces in a single class
- Extend an abstract class and implement remaining abstract methods
- Compare capabilities: fields, constructors, access modifiers
- Discuss design guidelines for choosing between them

## Solution

```java
// ========== INTERFACE EXAMPLE ==========

// Interface - contract with no implementation (pre-Java 8)
interface Drawable {
    void draw();  // Abstract method (implicitly public abstract)
    double getArea();  // All methods are public by default
}

// Interface for coloring
interface Colorable {
    void setColor(String color);
    String getColor();
}

// Class implementing multiple interfaces
class Circle implements Drawable, Colorable {
    private double radius;
    private String color;
    
    public Circle(double radius) {
        this.radius = radius;
        this.color = "black";
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing a " + color + " circle with radius " + radius);
    }
    
    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public void setColor(String color) {
        this.color = color;
    }
    
    @Override
    public String getColor() {
        return color;
    }
}

// ========== ABSTRACT CLASS EXAMPLE ==========

// Abstract class - partial implementation
abstract class Shape {
    protected String name;
    protected String color;
    
    // Constructor in abstract class
    public Shape(String name) {
        this.name = name;
        this.color = "black";
    }
    
    // Concrete method with implementation
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getColor() {
        return color;
    }
    
    // Abstract methods - must be implemented by subclasses
    public abstract void draw();
    public abstract double getArea();
    
    // Concrete method using abstract method (Template Method pattern)
    public void displayInfo() {
        System.out.println("Shape: " + name + ", Color: " + color + ", Area: " + getArea());
    }
}

// Concrete class extending abstract class
class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle(double width, double height) {
        super("Rectangle");  // Call parent constructor
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing a " + color + " rectangle: " + width + "x" + height);
    }
    
    @Override
    public double getArea() {
        return width * height;
    }
}

class Triangle extends Shape {
    private double base;
    private double height;
    
    public Triangle(double base, double height) {
        super("Triangle");
        this.base = base;
        this.height = height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing a " + color + " triangle: base=" + base + ", height=" + height);
    }
    
    @Override
    public double getArea() {
        return 0.5 * base * height;
    }
}

// Demo class
class InterfaceVsAbstractDemo {
    public static void main(String[] args) {
        // Using interface implementation
        Circle circle = new Circle(5.0);
        circle.setColor("red");
        circle.draw();
        System.out.println("Area: " + circle.getArea());
        
        System.out.println();
        
        // Using abstract class
        Shape rectangle = new Rectangle(4.0, 6.0);
        rectangle.setColor("blue");
        rectangle.draw();
        rectangle.displayInfo();
        
        System.out.println();
        
        Shape triangle = new Triangle(3.0, 4.0);
        triangle.setColor("green");
        triangle.draw();
        triangle.displayInfo();
        
        // Multiple interface implementation
        System.out.println("\nCircle implements both Drawable and Colorable");
        System.out.println("Color: " + circle.getColor());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - All method calls and object creation operations are constant time.

**Space Complexity**: O(1) - Each object uses constant space for its fields.

## Edge Cases and Pitfalls

- **Multiple Inheritance**: A class can implement multiple interfaces but extend only one abstract class. Use interfaces for "can-do" relationships and abstract classes for "is-a" relationships.
- **Default Methods (Java 8+)**: Interfaces can now have default methods with implementation, blurring the line with abstract classes.
- **Constructors**: Abstract classes can have constructors; interfaces cannot. Abstract class constructors are called when subclass is instantiated.
- **Fields**: Interface fields are implicitly public static final (constants). Abstract classes can have instance variables with any access modifier.
- **Method Access**: Interface methods are implicitly public. Abstract class methods can have any access modifier.
- **When to Use**: Use interfaces for contracts and capabilities (Serializable, Comparable). Use abstract classes for shared implementation and common state.

## Interview-Ready Answer

"Interfaces define contracts with method signatures, while abstract classes provide partial implementation. A class can implement multiple interfaces but extend only one abstract class. Interfaces have implicitly public abstract methods and public static final fields, while abstract classes can have constructors, instance variables, and methods with any access modifier. Use interfaces for 'can-do' capabilities and abstract classes for 'is-a' relationships with shared code. Both enable polymorphism with O(1) operations."
