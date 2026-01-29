# Collection Framework Design Patterns

## Problem Statement

Identify and explain the design patterns used in the Java Collections Framework. Demonstrate Iterator, Strategy, Factory, Template Method, and Adapter patterns with concrete examples from the framework.

**Requirements**:
- Explain Iterator pattern implementation
- Show Strategy pattern in Comparator
- Demonstrate Factory and Template Method patterns
- Identify Adapter pattern usage

## Approach

- Iterator: Traverse collections without exposing internal structure
- Strategy: Comparator allows different sorting algorithms
- Factory: Collections utility methods, EnumSet factory methods
- Template Method: AbstractList, AbstractMap skeletal implementations
- Adapter: Arrays.asList(), Collections wrappers

## Solution

```java
import java.util.*;

public class CollectionDesignPatterns {
    
    public static void main(String[] args) {
        demonstrateIteratorPattern();
        demonstrateStrategyPattern();
        demonstrateFactoryPattern();
        demonstrateTemplateMethod();
        demonstrateAdapterPattern();
    }
    
    public static void demonstrateIteratorPattern() {
        System.out.println("=== Iterator Pattern ===");
        
        List<String> list = Arrays.asList("A", "B", "C");
        
        // Iterator provides uniform traversal interface
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        
        System.out.println("Iterator decouples traversal from collection structure");
    }
    
    public static void demonstrateStrategyPattern() {
        System.out.println("\n=== Strategy Pattern (Comparator) ===");
        
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);
        
        // Different strategies for sorting
        Comparator<Integer> naturalOrder = Comparator.naturalOrder();
        Comparator<Integer> reverseOrder = Comparator.reverseOrder();
        
        Collections.sort(numbers, naturalOrder);
        System.out.println("Natural order: " + numbers);
        
        Collections.sort(numbers, reverseOrder);
        System.out.println("Reverse order: " + numbers);
        
        System.out.println("Comparator allows pluggable sorting strategies");
    }
    
    public static void demonstrateFactoryPattern() {
        System.out.println("\n=== Factory Pattern ===");
        
        // Collections factory methods
        List<String> emptyList = Collections.emptyList();
        Set<String> singleton = Collections.singleton("Only");
        Map<String, Integer> singletonMap = Collections.singletonMap("Key", 1);
        
        // EnumSet factory methods
        enum Day { MON, TUE, WED }
        EnumSet<Day> allDays = EnumSet.allOf(Day.class);
        EnumSet<Day> noDays = EnumSet.noneOf(Day.class);
        
        System.out.println("Factory methods create specific collection instances");
    }
    
    public static void demonstrateTemplateMethod() {
        System.out.println("\n=== Template Method (AbstractList) ===");
        
        // AbstractList provides template for list implementation
        class SimpleList extends AbstractList<String> {
            private String[] data = {"A", "B", "C"};
            
            @Override
            public String get(int index) {
                return data[index];
            }
            
            @Override
            public int size() {
                return data.length;
            }
            
            // AbstractList provides: iterator(), equals(), hashCode(), etc.
        }
        
        List<String> list = new SimpleList();
        System.out.println("List: " + list);
        System.out.println("AbstractList provides skeletal implementation");
    }
    
    public static void demonstrateAdapterPattern() {
        System.out.println("\n=== Adapter Pattern ===");
        
        // Arrays.asList adapts array to List interface
        String[] array = {"A", "B", "C"};
        List<String> list = Arrays.asList(array);
        System.out.println("Array adapted to List: " + list);
        
        // Collections.synchronizedList adapts to thread-safe version
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        System.out.println("Adapter adds synchronization");
        
        // Collections.unmodifiableList adapts to read-only version
        List<String> unmodList = Collections.unmodifiableList(list);
        System.out.println("Adapter makes collection unmodifiable");
    }
}
```

## Complexity Analysis

Design patterns don't change complexity, but improve code structure and maintainability.

## Edge Cases and Pitfalls

- **Iterator**: Fail-fast behavior, ConcurrentModificationException
- **Strategy**: Comparator must be consistent with equals
- **Factory**: Returns immutable or specialized collections
- **Template Method**: Subclasses must implement abstract methods
- **Adapter**: May have limitations (Arrays.asList is fixed-size)

## Interview-Ready Answer

"Collections Framework uses several design patterns: Iterator for uniform traversal, Strategy (Comparator) for pluggable algorithms, Factory (Collections.emptyList, EnumSet.allOf) for object creation, Template Method (AbstractList, AbstractMap) for skeletal implementations, and Adapter (Arrays.asList, Collections.synchronizedList) for interface adaptation. These patterns provide flexibility, reusability, and maintainability while following SOLID principles."
