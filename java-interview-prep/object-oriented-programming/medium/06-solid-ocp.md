# SOLID - Open/Closed Principle (OCP)

## Problem Statement

Demonstrate the Open/Closed Principle by creating a shape area calculator that is open for extension but closed for modification. Show how to add new shapes without modifying existing code, and contrast this with a design that violates OCP.

**Requirements:**
- Show a design that violates OCP (requires modification to add new types)
- Refactor to follow OCP using polymorphism
- Add new shape types without modifying existing code
- Demonstrate how OCP reduces risk of breaking existing functionality
- Show how abstraction enables extensibility

## Approach

- Create a bad example using conditional logic for different types
- Show how adding new types requires modifying existing code
- Refactor using abstract base class or interface
- Each shape type implements its own behavior
- Add new shapes by creating new classes, not modifying old ones
- Use polymorphism to handle different types uniformly

## Solution

```java
// ========== BEFORE: Violating OCP ==========

class ShapeBad {
    public String type;
    public double dimension1;
    public double dimension2;
    
    public ShapeBad(String type, double dimension1, double dimension2) {
        this.type = type;
        this.dimension1 = dimension1;
        this.dimension2 = dimension2;
    }
}

class AreaCalculatorBad {
    public double calculateArea(ShapeBad shape) {
        // Violates OCP: Must modify this method to add new shapes
        if (shape.type.equals("circle")) {
            return Math.PI * shape.dimension1 * shape.dimension1;
        } else if (shape.type.equals("rectangle")) {
            return shape.dimension1 * shape.dimension2;
        } else if (shape.type.equals("triangle")) {
            return 0.5 * shape.dimension1 * shape.dimension2;
        }
        // Adding a new shape requires modifying this method!
        return 0;
    }
}

// ========== AFTER: Following OCP ==========

// Abstract base class - closed for modification
abstract class Shape {
    public abstract double calculateArea();
    public abstract String getName();
}

// Concrete shapes - open for extension
class Circle extends Shape {
    private double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public String getName() {
        return "Circle";
    }
}

class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return width * height;
    }
    
    @Override
    public String getName() {
        return "Rectangle";
    }
}

class Triangle extends Shape {
    private double base;
    private double height;
    
    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }
    
    @Override
    public String getName() {
        return "Triangle";
    }
}

// NEW SHAPE: Can add without modifying existing code!
class Pentagon extends Shape {
    private double side;
    
    public Pentagon(double side) {
        this.side = side;
    }
    
    @Override
    public double calculateArea() {
        // Formula for regular pentagon
        return (5 * side * side) / (4 * Math.tan(Math.PI / 5));
    }
    
    @Override
    public String getName() {
        return "Pentagon";
    }
}

class Hexagon extends Shape {
    private double side;
    
    public Hexagon(double side) {
        this.side = side;
    }
    
    @Override
    public double calculateArea() {
        // Formula for regular hexagon
        return (3 * Math.sqrt(3) * side * side) / 2;
    }
    
    @Override
    public String getName() {
        return "Hexagon";
    }
}

// Calculator class - closed for modification, works with any Shape
class AreaCalculator {
    public double calculateArea(Shape shape) {
        // No modification needed when adding new shapes!
        return shape.calculateArea();
    }
    
    public void printArea(Shape shape) {
        System.out.println(shape.getName() + " area: " + calculateArea(shape));
    }
    
    public double calculateTotalArea(Shape[] shapes) {
        double total = 0;
        for (Shape shape : shapes) {
            total += shape.calculateArea();
        }
        return total;
    }
}

// Demo class
class OCPDemo {
    public static void main(String[] args) {
        System.out.println("=== BEFORE: Violating OCP ===\n");
        
        ShapeBad circle = new ShapeBad("circle", 5, 0);
        ShapeBad rectangle = new ShapeBad("rectangle", 4, 6);
        ShapeBad triangle = new ShapeBad("triangle", 3, 4);
        
        AreaCalculatorBad badCalculator = new AreaCalculatorBad();
        System.out.println("Circle area: " + badCalculator.calculateArea(circle));
        System.out.println("Rectangle area: " + badCalculator.calculateArea(rectangle));
        System.out.println("Triangle area: " + badCalculator.calculateArea(triangle));
        
        // To add a new shape, we must modify AreaCalculatorBad!
        System.out.println("\nProblem: Adding pentagon requires modifying AreaCalculatorBad");
        
        System.out.println("\n=== AFTER: Following OCP ===\n");
        
        AreaCalculator calculator = new AreaCalculator();
        
        // Original shapes
        Shape circle2 = new Circle(5);
        Shape rectangle2 = new Rectangle(4, 6);
        Shape triangle2 = new Triangle(3, 4);
        
        calculator.printArea(circle2);
        calculator.printArea(rectangle2);
        calculator.printArea(triangle2);
        
        // NEW SHAPES: Added without modifying AreaCalculator!
        System.out.println("\nAdding new shapes (no modification to existing code):");
        Shape pentagon = new Pentagon(5);
        Shape hexagon = new Hexagon(4);
        
        calculator.printArea(pentagon);
        calculator.printArea(hexagon);
        
        // Calculate total area
        System.out.println("\nTotal area of all shapes:");
        Shape[] shapes = {circle2, rectangle2, triangle2, pentagon, hexagon};
        System.out.println("Total: " + calculator.calculateTotalArea(shapes));
        
        System.out.println("\n=== Benefits of OCP ===");
        System.out.println("- New shapes added without modifying AreaCalculator");
        System.out.println("- Existing code remains untouched and stable");
        System.out.println("- Reduced risk of breaking existing functionality");
        System.out.println("- Easy to extend with new shape types");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Calculating area for a single shape is constant time. Calculating total area for n shapes is O(n).

**Space Complexity**: O(1) - Each shape object uses constant space for its dimensions.

## Edge Cases and Pitfalls

- **Premature Abstraction**: Don't create abstractions until you have at least 2-3 concrete cases. Wait for patterns to emerge.
- **Over-Engineering**: Not every class needs to follow OCP. Apply it where you expect frequent extensions.
- **Modification vs Extension**: Some modifications are acceptable (bug fixes, performance improvements). OCP focuses on adding new functionality.
- **Abstraction Leakage**: Ensure the base abstraction is stable. If you frequently need to modify the base class, the abstraction may be wrong.
- **Balance**: OCP can lead to many small classes. Balance extensibility with simplicity.
- **Strategy Pattern**: OCP often works well with Strategy pattern for algorithm variations.

## Interview-Ready Answer

"The Open/Closed Principle states that software entities should be open for extension but closed for modification. I'd show a bad design where AreaCalculator uses if-else statements to handle different shapes, requiring modification for each new shape. Then I'd refactor using an abstract Shape class where each concrete shape implements calculateArea(). New shapes are added by creating new classes without modifying existing code. This reduces risk and makes the system more maintainable. Time complexity is O(1) per shape."
