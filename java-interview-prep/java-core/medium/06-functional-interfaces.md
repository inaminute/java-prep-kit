# Functional Interfaces

## Problem Statement

Explain functional interfaces in Java 8+. Demonstrate built-in functional interfaces and how to create custom ones. Show how they enable lambda expressions and method references.

**Requirements**:
- Explain @FunctionalInterface annotation
- Demonstrate built-in interfaces (Predicate, Function, Consumer, Supplier)
- Create custom functional interfaces
- Show composition methods

## Approach

- Functional interface has exactly one abstract method
- @FunctionalInterface annotation ensures contract
- Built-in interfaces cover common patterns
- Can have default and static methods
- Enable lambda expressions and method references

## Solution

```java
import java.util.function.*;

@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
    
    default int add(int a, int b) {
        return a + b;
    }
}

public class FunctionalInterfaces {
    
    public static void main(String[] args) {
        demonstrateBuiltInInterfaces();
        demonstrateCustomInterface();
        demonstrateComposition();
    }
    
    public static void demonstrateBuiltInInterfaces() {
        System.out.println("=== Built-in Functional Interfaces ===");
        
        // Predicate<T> - takes T, returns boolean
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("4 is even: " + isEven.test(4));
        
        // Function<T, R> - takes T, returns R
        Function<String, Integer> length = String::length;
        System.out.println("Length of 'hello': " + length.apply("hello"));
        
        // Consumer<T> - takes T, returns void
        Consumer<String> printer = System.out::println;
        printer.accept("Hello World");
        
        // Supplier<T> - takes nothing, returns T
        Supplier<Double> random = Math::random;
        System.out.println("Random: " + random.get());
        
        // BiFunction<T, U, R> - takes T and U, returns R
        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
        System.out.println("3 * 4 = " + multiply.apply(3, 4));
    }
    
    public static void demonstrateCustomInterface() {
        System.out.println("\n=== Custom Functional Interface ===");
        
        Calculator multiply = (a, b) -> a * b;
        System.out.println("5 * 3 = " + multiply.calculate(5, 3));
        System.out.println("5 + 3 = " + multiply.add(5, 3));
    }
    
    public static void demonstrateComposition() {
        System.out.println("\n=== Function Composition ===");
        
        Function<Integer, Integer> multiplyBy2 = x -> x * 2;
        Function<Integer, Integer> add3 = x -> x + 3;
        
        // andThen: f.andThen(g) = g(f(x))
        Function<Integer, Integer> multiplyThenAdd = multiplyBy2.andThen(add3);
        System.out.println("(5 * 2) + 3 = " + multiplyThenAdd.apply(5));
        
        // compose: f.compose(g) = f(g(x))
        Function<Integer, Integer> addThenMultiply = multiplyBy2.compose(add3);
        System.out.println("(5 + 3) * 2 = " + addThenMultiply.apply(5));
        
        // Predicate composition
        Predicate<Integer> isPositive = x -> x > 0;
        Predicate<Integer> isEven = x -> x % 2 == 0;
        Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);
        System.out.println("4 is positive and even: " + isPositiveAndEven.test(4));
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for interface operations

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Multiple abstract methods**: Not a functional interface
- **Default methods**: Don't count toward single abstract method requirement
- **Exception handling**: Standard functional interfaces don't throw checked exceptions

## Interview-Ready Answer

"Functional interfaces have exactly one abstract method and enable lambda expressions. @FunctionalInterface annotation ensures contract. Built-in interfaces include Predicate (test), Function (apply), Consumer (accept), and Supplier (get). They support composition with methods like andThen, compose, and, or. Can have default and static methods without breaking functional interface contract."
