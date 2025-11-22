# SOLID - Liskov Substitution Principle (LSP)

## Problem Statement

Demonstrate the Liskov Substitution Principle by showing how subclasses should be substitutable for their base classes without breaking program correctness. Show examples of LSP violations and how to fix them.

**Requirements:**
- Show a class hierarchy that violates LSP
- Demonstrate how substituting a subclass breaks expectations
- Refactor to follow LSP
- Show proper use of inheritance vs composition
- Demonstrate that subclasses should strengthen, not weaken, contracts

## Approach

- Create a base class with certain behaviors and contracts
- Show a subclass that violates those contracts
- Demonstrate the problem when using polymorphism
- Refactor using proper inheritance or composition
- Ensure subclasses honor base class contracts
- Use preconditions, postconditions, and invariants correctly

## Solution

```java
// ========== BEFORE: Violating LSP ==========

class Bird {
    public void fly() {
        System.out.println("Bird is flying");
    }
}

class Sparrow extends Bird {
    @Override
    public void fly() {
        System.out.println("Sparrow is flying");
    }
}

// Violates LSP: Penguin is a bird but can't fly!
class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins can't fly!");
    }
}

// ========== AFTER: Following LSP ==========

// Base class with common behavior
abstract class BirdLSP {
    protected String name;
    
    public BirdLSP(String name) {
        this.name = name;
    }
    
    public void eat() {
        System.out.println(name + " is eating");
    }
    
    public abstract void move();
}

// Interface for flying capability
interface Flyable {
    void fly();
}

// Flying birds implement both
class SparrowLSP extends BirdLSP implements Flyable {
    public SparrowLSP() {
        super("Sparrow");
    }
    
    @Override
    public void move() {
        fly();
    }
    
    @Override
    public void fly() {
        System.out.println(name + " is flying");
    }
}

class Eagle extends BirdLSP implements Flyable {
    public Eagle() {
        super("Eagle");
    }
    
    @Override
    public void move() {
        fly();
    }
    
    @Override
    public void fly() {
        System.out.println(name + " is soaring high");
    }
}

// Non-flying birds don't implement Flyable
class PenguinLSP extends BirdLSP {
    public PenguinLSP() {
        super("Penguin");
    }
    
    @Override
    public void move() {
        swim();
    }
    
    public void swim() {
        System.out.println(name + " is swimming");
    }
}

// Another LSP example: Rectangle and Square

// WRONG: Square violating LSP
class RectangleBad {
    protected int width;
    protected int height;
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getArea() {
        return width * height;
    }
}

class SquareBad extends RectangleBad {
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;  // Violates LSP: changes both dimensions
    }
    
    @Override
    public void setHeight(int height) {
        this.width = height;  // Violates LSP: changes both dimensions
        this.height = height;
    }
}

// CORRECT: Using composition instead
interface Shape {
    int getArea();
    String getName();
}

class RectangleLSP implements Shape {
    private int width;
    private int height;
    
    public RectangleLSP(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public int getArea() {
        return width * height;
    }
    
    @Override
    public String getName() {
        return "Rectangle";
    }
}

class SquareLSP implements Shape {
    private int side;
    
    public SquareLSP(int side) {
        this.side = side;
    }
    
    @Override
    public int getArea() {
        return side * side;
    }
    
    @Override
    public String getName() {
        return "Square";
    }
}

// Demo class
class LSPDemo {
    public static void main(String[] args) {
        System.out.println("=== BEFORE: Violating LSP ===\n");
        
        Bird sparrow = new Sparrow();
        Bird penguin = new Penguin();
        
        sparrow.fly();  // Works fine
        
        try {
            penguin.fly();  // Throws exception! Violates LSP
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Rectangle-Square problem
        System.out.println("\nRectangle-Square LSP violation:");
        RectangleBad rect = new RectangleBad();
        rect.setWidth(5);
        rect.setHeight(4);
        System.out.println("Rectangle area: " + rect.getArea());  // 20
        
        RectangleBad square = new SquareBad();
        square.setWidth(5);
        square.setHeight(4);  // Unexpectedly changes width too!
        System.out.println("Square area: " + square.getArea());  // 16, not 20!
        
        System.out.println("\n=== AFTER: Following LSP ===\n");
        
        // All birds can move
        BirdLSP sparrow2 = new SparrowLSP();
        BirdLSP eagle = new Eagle();
        BirdLSP penguin2 = new PenguinLSP();
        
        sparrow2.move();
        eagle.move();
        penguin2.move();
        
        // Only flying birds can fly
        System.out.println("\nFlying birds:");
        Flyable[] flyingBirds = {new SparrowLSP(), new Eagle()};
        for (Flyable bird : flyingBirds) {
            bird.fly();
        }
        
        // Rectangle-Square with proper design
        System.out.println("\nShapes with proper LSP:");
        Shape rect2 = new RectangleLSP(5, 4);
        Shape square2 = new SquareLSP(4);
        
        System.out.println(rect2.getName() + " area: " + rect2.getArea());
        System.out.println(square2.getName() + " area: " + square2.getArea());
        
        System.out.println("\n=== Benefits of LSP ===");
        System.out.println("- Subclasses can be used wherever base class is expected");
        System.out.println("- No unexpected exceptions or behavior changes");
        System.out.println("- Polymorphism works correctly");
        System.out.println("- Code is more maintainable and predictable");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - All operations (move, fly, swim, area calculation) are constant time.

**Space Complexity**: O(1) - Each object uses constant space for its fields.

## Edge Cases and Pitfalls

- **Behavioral Subtyping**: Subclasses must preserve the behavior expected by clients of the base class. Don't weaken preconditions or strengthen postconditions.
- **Exception Throwing**: If base class doesn't throw exceptions, subclass shouldn't either. Throwing new exceptions violates LSP.
- **Return Types**: Subclass methods can return more specific types (covariant), but not less specific types.
- **Invariants**: Subclasses must maintain all invariants of the base class.
- **Is-A Relationship**: Just because something "is-a" in real world doesn't mean it should inherit in code. Square is-a rectangle mathematically, but not in OOP.
- **Composition Over Inheritance**: When LSP is hard to maintain, consider composition instead of inheritance.

## Interview-Ready Answer

"The Liskov Substitution Principle states that objects of a subclass should be substitutable for objects of the base class without breaking program correctness. I'd show a Bird base class with fly() method and a Penguin subclass that throws an exception, violating LSP. The fix is to separate flying capability into a Flyable interface, so only flying birds implement it. Another example is Square extending Rectangle, which violates LSP because setting width/height independently doesn't work for squares. The solution is to make them separate classes implementing a Shape interface. Time and space are O(1)."
