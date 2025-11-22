# Decorator Pattern

## Problem Statement

Implement the Decorator design pattern to create a flexible coffee ordering system where beverages can be dynamically enhanced with various condiments (milk, mocha, whip) without modifying the base beverage classes. Each decorator should add its own cost and description to the beverage.

**Requirements:**
- Create a Beverage base class with cost() and getDescription() methods
- Implement concrete beverage classes (Espresso, DarkRoast, etc.)
- Create a CondimentDecorator that wraps beverages
- Implement concrete decorators for different condiments
- Support multiple decorators wrapping the same beverage
- Calculate total cost by summing base beverage and all decorators

## Approach

- Define an abstract Beverage class with cost() and getDescription()
- Create concrete beverage classes extending Beverage
- Create an abstract CondimentDecorator extending Beverage
- Each decorator has a Beverage instance variable (the wrapped object)
- Decorators delegate to the wrapped object and add their own behavior
- Support recursive wrapping for multiple decorators

## Solution

```java
// Abstract base component
abstract class Beverage {
    protected String description = "Unknown Beverage";
    
    public String getDescription() {
        return description;
    }
    
    public abstract double cost();
}

// Concrete components
class Espresso extends Beverage {
    public Espresso() {
        description = "Espresso";
    }
    
    @Override
    public double cost() {
        return 1.99;
    }
}

class DarkRoast extends Beverage {
    public DarkRoast() {
        description = "Dark Roast Coffee";
    }
    
    @Override
    public double cost() {
        return 0.99;
    }
}

class Decaf extends Beverage {
    public Decaf() {
        description = "Decaf Coffee";
    }
    
    @Override
    public double cost() {
        return 1.05;
    }
}

// Abstract decorator
abstract class CondimentDecorator extends Beverage {
    protected Beverage beverage;  // The wrapped beverage
    
    public abstract String getDescription();
}

// Concrete decorators
class Milk extends CondimentDecorator {
    public Milk(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Milk";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.10;
    }
}

class Mocha extends CondimentDecorator {
    public Mocha(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Mocha";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.20;
    }
}

class Whip extends CondimentDecorator {
    public Whip(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Whip";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.15;
    }
}

class Soy extends CondimentDecorator {
    public Soy(Beverage beverage) {
        this.beverage = beverage;
    }
    
    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Soy";
    }
    
    @Override
    public double cost() {
        return beverage.cost() + 0.25;
    }
}

// Demo class
class DecoratorPatternDemo {
    public static void main(String[] args) {
        // Order 1: Plain espresso
        Beverage beverage1 = new Espresso();
        System.out.println(beverage1.getDescription() + " $" + beverage1.cost());
        
        // Order 2: Dark Roast with Mocha and Whip
        Beverage beverage2 = new DarkRoast();
        beverage2 = new Mocha(beverage2);
        beverage2 = new Whip(beverage2);
        System.out.println(beverage2.getDescription() + " $" + beverage2.cost());
        
        // Order 3: Decaf with double Mocha, Milk, and Soy
        Beverage beverage3 = new Decaf();
        beverage3 = new Mocha(beverage3);
        beverage3 = new Mocha(beverage3);  // Double mocha
        beverage3 = new Milk(beverage3);
        beverage3 = new Soy(beverage3);
        System.out.println(beverage3.getDescription() + " $" + beverage3.cost());
        
        // Order 4: Espresso with everything
        Beverage beverage4 = new Espresso();
        beverage4 = new Milk(beverage4);
        beverage4 = new Mocha(beverage4);
        beverage4 = new Whip(beverage4);
        beverage4 = new Soy(beverage4);
        System.out.println(beverage4.getDescription() + " $" + beverage4.cost());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - Where n is the number of decorators wrapping the beverage. Each method call (cost(), getDescription()) traverses the decorator chain.

**Space Complexity**: O(n) - Each decorator creates a new object wrapping the previous one, forming a chain of n decorator objects.

## Edge Cases and Pitfalls

- **Deep Nesting**: Too many decorators can make the chain hard to debug and understand. Consider limiting decorator depth or using a different pattern for complex scenarios.
- **Type Checking**: Once wrapped, you lose access to concrete beverage methods unless you downcast, which defeats the purpose. Design interfaces carefully.
- **Order Matters**: The order of decoration can affect behavior if decorators interact. Document any ordering requirements.
- **Performance**: Each decorator adds a method call in the chain. For performance-critical code, consider alternatives.
- **Null Wrapping**: Always validate that the wrapped object is not null in decorator constructors.
- **Immutability**: Decorators should not modify the wrapped object's state, only add behavior around it.

## Interview-Ready Answer

"The Decorator pattern attaches additional responsibilities to an object dynamically by wrapping it with decorator objects. I'd create a Beverage base class, concrete beverages like Espresso, and a CondimentDecorator abstract class. Each decorator (Milk, Mocha, Whip) wraps a Beverage and adds its cost and description. Multiple decorators can wrap the same object, with each delegating to the wrapped object and adding its own behavior. Time complexity is O(n) for n decorators, space is O(n) for the decorator chain."
