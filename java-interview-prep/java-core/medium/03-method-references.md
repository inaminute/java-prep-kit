# Method References

## Problem Statement

Explain method references in Java 8+. Demonstrate the four types of method references and when to use them instead of lambda expressions. Show how they improve code readability.

**Requirements**:
- Demonstrate all four types of method references
- Compare with equivalent lambda expressions
- Show practical use cases

## Approach

- Method references are shorthand for lambdas that only call a method
- Four types: static, instance, instance on parameter, constructor
- Syntax: ClassName::methodName or instance::methodName
- More readable than lambdas when simply delegating to a method

## Solution

```java
import java.util.*;
import java.util.function.*;

public class MethodReferences {
    
    public static void main(String[] args) {
        demonstrateStaticMethodReference();
        demonstrateInstanceMethodReference();
        demonstrateArbitraryObjectMethodReference();
        demonstrateConstructorReference();
    }
    
    // Type 1: Static method reference
    public static void demonstrateStaticMethodReference() {
        System.out.println("=== Static Method Reference ===");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        
        // Lambda
        numbers.forEach(n -> System.out.println(n));
        
        // Method reference
        numbers.forEach(System.out::println);
    }
    
    // Type 2: Instance method reference
    public static void demonstrateInstanceMethodReference() {
        System.out.println("\n=== Instance Method Reference ===");
        
        String prefix = "Number: ";
        Consumer<String> printer = prefix::concat;
        
        System.out.println(printer.toString());
    }
    
    // Type 3: Method reference on arbitrary object
    public static void demonstrateArbitraryObjectMethodReference() {
        System.out.println("\n=== Arbitrary Object Method Reference ===");
        
        List<String> words = Arrays.asList("apple", "banana", "cherry");
        
        // Lambda
        words.sort((s1, s2) -> s1.compareToIgnoreCase(s2));
        
        // Method reference
        words.sort(String::compareToIgnoreCase);
        System.out.println(words);
    }
    
    // Type 4: Constructor reference
    public static void demonstrateConstructorReference() {
        System.out.println("\n=== Constructor Reference ===");
        
        // Lambda
        Supplier<List<String>> listSupplier1 = () -> new ArrayList<>();
        
        // Constructor reference
        Supplier<List<String>> listSupplier2 = ArrayList::new;
        
        List<String> list = listSupplier2.get();
        list.add("test");
        System.out.println(list);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for method reference creation

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Overloaded methods**: Compiler infers which overload based on context
- **Generic types**: May need explicit type parameters
- **Ambiguity**: Sometimes lambda is clearer than method reference

## Interview-Ready Answer

"Method references are shorthand for lambdas that only call a method. Four types: static (ClassName::method), instance (instance::method), arbitrary object (ClassName::instanceMethod), and constructor (ClassName::new). They improve readability when lambdas simply delegate to a method. Use ClassName::methodName syntax."
