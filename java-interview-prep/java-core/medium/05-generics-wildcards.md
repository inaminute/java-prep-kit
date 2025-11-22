# Generics and Wildcards

## Problem Statement

Explain generics and wildcards in Java. Demonstrate bounded type parameters, upper and lower bounded wildcards, and type erasure. Show when to use each wildcard type.

**Requirements**:
- Explain generic classes and methods
- Demonstrate upper bounded wildcards (? extends T)
- Show lower bounded wildcards (? super T)
- Explain PECS principle

## Approach

- Generics provide compile-time type safety
- Upper bounded wildcards (? extends T) for reading
- Lower bounded wildcards (? super T) for writing
- PECS: Producer Extends, Consumer Super
- Type erasure removes generic information at runtime

## Solution

```java
import java.util.*;

public class GenericsWildcards {
    
    // Generic class
    static class Box<T> {
        private T value;
        
        public void set(T value) { this.value = value; }
        public T get() { return value; }
    }
    
    // Generic method
    public static <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }
    
    // Upper bounded wildcard - can read
    public static double sumOfList(List<? extends Number> list) {
        double sum = 0.0;
        for (Number num : list) {
            sum += num.doubleValue();
        }
        return sum;
    }
    
    // Lower bounded wildcard - can write
    public static void addNumbers(List<? super Integer> list) {
        for (int i = 1; i <= 5; i++) {
            list.add(i);
        }
    }
    
    public static void main(String[] args) {
        demonstrateGenericClass();
        demonstrateUpperBoundedWildcard();
        demonstrateLowerBoundedWildcard();
        demonstratePECS();
    }
    
    public static void demonstrateGenericClass() {
        System.out.println("=== Generic Class ===");
        
        Box<Integer> intBox = new Box<>();
        intBox.set(42);
        System.out.println("Integer box: " + intBox.get());
        
        Box<String> strBox = new Box<>();
        strBox.set("Hello");
        System.out.println("String box: " + strBox.get());
    }
    
    public static void demonstrateUpperBoundedWildcard() {
        System.out.println("\n=== Upper Bounded Wildcard ===");
        
        List<Integer> integers = Arrays.asList(1, 2, 3);
        List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
        
        System.out.println("Sum of integers: " + sumOfList(integers));
        System.out.println("Sum of doubles: " + sumOfList(doubles));
    }
    
    public static void demonstrateLowerBoundedWildcard() {
        System.out.println("\n=== Lower Bounded Wildcard ===");
        
        List<Number> numbers = new ArrayList<>();
        addNumbers(numbers);
        System.out.println("Numbers: " + numbers);
    }
    
    public static void demonstratePECS() {
        System.out.println("\n=== PECS Principle ===");
        
        // Producer Extends - reading from source
        List<Integer> source = Arrays.asList(1, 2, 3);
        List<Number> dest = new ArrayList<>();
        copy(source, dest);
        System.out.println("Copied: " + dest);
    }
    
    // PECS: Producer Extends, Consumer Super
    public static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) {
            dest.add(item);
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for generic operations

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Type erasure**: Generic type information lost at runtime
- **Cannot create generic arrays**: new T[] is not allowed
- **Upper bounded can't add**: List<? extends T> can't add elements
- **Lower bounded can't read**: List<? super T> returns Object when reading

## Interview-Ready Answer

"Generics provide compile-time type safety. Upper bounded wildcards (? extends T) allow reading but not writing, used for producers. Lower bounded wildcards (? super T) allow writing but return Object when reading, used for consumers. PECS principle: Producer Extends, Consumer Super. Type erasure removes generic information at runtime for backward compatibility."
