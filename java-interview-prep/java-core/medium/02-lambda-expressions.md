# Lambda Expressions

## Problem Statement

Explain lambda expressions in Java 8+. Demonstrate their syntax, usage with functional interfaces, and how they enable functional programming. Show how lambdas improve code readability and reduce boilerplate.

**Requirements**:
- Explain lambda syntax
- Show usage with functional interfaces
- Demonstrate method references
- Compare with anonymous classes

## Approach

- Lambdas provide concise syntax for implementing functional interfaces
- Syntax: (parameters) -> expression or (parameters) -> { statements }
- Lambdas can access effectively final variables from enclosing scope
- Method references provide even more concise syntax
- Lambdas enable functional programming style in Java

## Solution

```java
import java.util.*;
import java.util.function.*;

public class LambdaExpressions {
    
    public static void main(String[] args) {
        demonstrateLambdaSyntax();
        demonstrateWithCollections();
        demonstrateMethodReferences();
        demonstrateBuiltInFunctionalInterfaces();
    }
    
    public static void demonstrateLambdaSyntax() {
        System.out.println("=== Lambda Syntax ===");
        
        // Traditional anonymous class
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous class");
            }
        };
        
        // Lambda expression
        Runnable r2 = () -> System.out.println("Lambda expression");
        
        r1.run();
        r2.run();
        
        // Lambda with parameters
        Comparator<String> comp = (s1, s2) -> s1.length() - s2.length();
        System.out.println("Compare 'abc' and 'defgh': " + comp.compare("abc", "defgh"));
    }
    
    public static void demonstrateWithCollections() {
        System.out.println("\n=== Lambdas with Collections ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");
        
        // forEach with lambda
        names.forEach(name -> System.out.println(name));
        
        // Sorting with lambda
        names.sort((s1, s2) -> s2.compareTo(s1));
        System.out.println("Sorted descending: " + names);
    }
    
    public static void demonstrateMethodReferences() {
        System.out.println("\n=== Method References ===");
        
        List<String> words = Arrays.asList("hello", "world", "java");
        
        // Lambda
        words.forEach(s -> System.out.println(s));
        
        // Method reference
        words.forEach(System.out::println);
    }
    
    public static void demonstrateBuiltInFunctionalInterfaces() {
        System.out.println("\n=== Built-in Functional Interfaces ===");
        
        // Predicate
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("4 is even: " + isEven.test(4));
        
        // Function
        Function<String, Integer> length = s -> s.length();
        System.out.println("Length of 'hello': " + length.apply("hello"));
        
        // Consumer
        Consumer<String> printer = s -> System.out.println("Value: " + s);
        printer.accept("test");
        
        // Supplier
        Supplier<Double> random = () -> Math.random();
        System.out.println("Random: " + random.get());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for lambda creation and invocation

**Space Complexity**: O(1) for lambda object

## Edge Cases and Pitfalls

- **Effectively final**: Lambdas can only access final or effectively final variables
- **this reference**: In lambdas, 'this' refers to enclosing class, not the lambda itself
- **Exception handling**: Checked exceptions in lambdas require wrapping
- **Serialization**: Lambdas are serializable only if target type is serializable

## Interview-Ready Answer

"Lambda expressions provide concise syntax for implementing functional interfaces using (parameters) -> expression. They enable functional programming, reduce boilerplate compared to anonymous classes, and can access effectively final variables from enclosing scope. Method references provide even more concise syntax. Lambdas are commonly used with Stream API and collections."
