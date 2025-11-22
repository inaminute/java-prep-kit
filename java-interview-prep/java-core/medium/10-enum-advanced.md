# Advanced Enum Usage

## Problem Statement

Demonstrate advanced enum features in Java. Show how to add fields, methods, and constructors to enums. Implement enums with behavior, use EnumSet and EnumMap, and show the Strategy pattern with enums.

**Requirements**:
- Create enums with fields and methods
- Demonstrate enum constructors
- Show EnumSet and EnumMap usage
- Implement Strategy pattern with enums

## Approach

- Enums can have fields, constructors, and methods
- Each enum constant can override methods
- EnumSet provides efficient set implementation for enums
- EnumMap provides efficient map implementation with enum keys
- Enums are type-safe and can implement interfaces

## Solution

```java
import java.util.*;

enum Day {
    MONDAY("Weekday"), TUESDAY("Weekday"), WEDNESDAY("Weekday"),
    THURSDAY("Weekday"), FRIDAY("Weekday"),
    SATURDAY("Weekend"), SUNDAY("Weekend");
    
    private String type;
    
    Day(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isWeekend() {
        return type.equals("Weekend");
    }
}

enum Operation {
    PLUS {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS {
        public double apply(double x, double y) { return x - y; }
    },
    MULTIPLY {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE {
        public double apply(double x, double y) { return x / y; }
    };
    
    public abstract double apply(double x, double y);
}

public class EnumAdvanced {
    
    public static void main(String[] args) {
        demonstrateEnumWithFields();
        demonstrateEnumWithBehavior();
        demonstrateEnumSet();
        demonstrateEnumMap();
    }
    
    public static void demonstrateEnumWithFields() {
        System.out.println("=== Enum with Fields ===");
        
        for (Day day : Day.values()) {
            System.out.println(day + " is a " + day.getType());
        }
        
        System.out.println("Is Saturday weekend? " + Day.SATURDAY.isWeekend());
    }
    
    public static void demonstrateEnumWithBehavior() {
        System.out.println("\n=== Enum with Behavior ===");
        
        double x = 10, y = 5;
        for (Operation op : Operation.values()) {
            System.out.println(x + " " + op + " " + y + " = " + op.apply(x, y));
        }
    }
    
    public static void demonstrateEnumSet() {
        System.out.println("\n=== EnumSet ===");
        
        EnumSet<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
        EnumSet<Day> weekdays = EnumSet.complementOf(weekend);
        
        System.out.println("Weekend: " + weekend);
        System.out.println("Weekdays: " + weekdays);
    }
    
    public static void demonstrateEnumMap() {
        System.out.println("\n=== EnumMap ===");
        
        EnumMap<Day, String> activities = new EnumMap<>(Day.class);
        activities.put(Day.MONDAY, "Work");
        activities.put(Day.SATURDAY, "Relax");
        
        System.out.println("Monday activity: " + activities.get(Day.MONDAY));
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for enum operations, EnumSet and EnumMap operations

**Space Complexity**: O(1) for enum constants

## Edge Cases and Pitfalls

- **Enum constructors**: Must be private or package-private
- **Serialization**: Enums are serializable by default
- **Comparison**: Use == for enum comparison, not equals()

## Interview-Ready Answer

"Enums can have fields, constructors, and methods. Each constant can override methods for different behavior. EnumSet provides efficient bit-vector implementation for enum sets. EnumMap provides efficient array-based map with enum keys. Enums are type-safe, can implement interfaces, and are useful for implementing Strategy pattern with constant-specific behavior."
