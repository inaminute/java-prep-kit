# Factory Pattern

## Problem Statement

Implement the Factory design pattern to create different types of shapes (Circle, Rectangle, Square) without exposing the instantiation logic to the client. The factory should take a shape type as input and return the appropriate shape object. This pattern provides an interface for creating objects but lets subclasses decide which class to instantiate.

**Requirements:**
- Create a Shape interface with a draw() method
- Implement concrete shape classes (Circle, Rectangle, Square)
- Create a ShapeFactory that returns shape objects based on type
- Client code should not use 'new' keyword to create shapes

## Approach

- Define a Shape interface with common behavior (draw method)
- Create concrete implementations for each shape type
- Implement a ShapeFactory class with a static factory method
- Use conditional logic or switch statement to determine which object to create
- Return the created object as the interface type for polymorphism
- Handle invalid shape types gracefully

## Solution

```java
// Shape interface
interface Shape {
    void draw();
}

// Concrete implementations
class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing a Circle");
    }
}

class Rectangle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing a Rectangle");
    }
}

class Square implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing a Square");
    }
}

// Factory class
class ShapeFactory {
    // Factory method to create shapes
    public static Shape createShape(String shapeType) {
        if (shapeType == null || shapeType.isEmpty()) {
            return null;
        }
        
        switch (shapeType.toUpperCase()) {
            case "CIRCLE":
                return new Circle();
            case "RECTANGLE":
                return new Rectangle();
            case "SQUARE":
                return new Square();
            default:
                throw new IllegalArgumentException("Unknown shape type: " + shapeType);
        }
    }
}

// Client code
class FactoryPatternDemo {
    public static void main(String[] args) {
        // Create shapes using factory
        Shape circle = ShapeFactory.createShape("CIRCLE");
        circle.draw();
        
        Shape rectangle = ShapeFactory.createShape("RECTANGLE");
        rectangle.draw();
        
        Shape square = ShapeFactory.createShape("SQUARE");
        square.draw();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Creating an object through the factory is constant time, regardless of the shape type.

**Space Complexity**: O(1) - Each factory method call creates one object. The factory itself doesn't maintain state.

## Edge Cases and Pitfalls

- **Null or Empty Input**: Always validate the shape type parameter to avoid NullPointerException. Return null or throw an exception for invalid input.
- **Case Sensitivity**: Convert input to uppercase/lowercase to handle case-insensitive matching.
- **Unknown Types**: Throw IllegalArgumentException for unrecognized shape types rather than returning null, making errors explicit.
- **Factory Bloat**: As more shape types are added, the factory method grows. Consider using reflection or a registry pattern for extensibility.
- **Tight Coupling**: The factory is coupled to all concrete classes. Adding new shapes requires modifying the factory (violates Open-Closed Principle).

## Interview-Ready Answer

"The Factory pattern encapsulates object creation logic in a dedicated factory class. I'd create a Shape interface, implement concrete shape classes, and provide a ShapeFactory with a createShape() method that uses a switch statement to instantiate the appropriate object. This decouples client code from concrete classes and centralizes creation logic. Time and space complexity are O(1) per object creation."
