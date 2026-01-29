# Iterator Pattern and Usage

## Problem Statement

Explain the Iterator pattern in Java Collections Framework. Demonstrate proper iterator usage, the difference between Iterator and enhanced for-loop, and how to handle ConcurrentModificationException.

**Requirements**:
- Explain Iterator interface methods
- Demonstrate safe element removal during iteration
- Show ConcurrentModificationException scenarios
- Compare Iterator with enhanced for-loop and forEach

## Approach

- Iterator provides a way to traverse collections without exposing internal structure
- Methods: hasNext(), next(), remove()
- Enhanced for-loop uses Iterator internally
- Fail-fast behavior: throws ConcurrentModificationException if collection modified during iteration
- Only Iterator.remove() is safe during iteration
- ListIterator extends Iterator with bidirectional traversal

## Solution

```java
import java.util.*;

public class IteratorPattern {
    
    public static void main(String[] args) {
        demonstrateBasicIterator();
        demonstrateSafeRemoval();
        demonstrateConcurrentModification();
        demonstrateListIterator();
        compareIterationMethods();
    }
    
    public static void demonstrateBasicIterator() {
        System.out.println("=== Basic Iterator Usage ===");
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // Using Iterator
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            System.out.println("Element: " + element);
        }
        
        // Enhanced for-loop (uses Iterator internally)
        System.out.println("\nEnhanced for-loop:");
        for (String element : list) {
            System.out.println("Element: " + element);
        }
    }
    
    public static void demonstrateSafeRemoval() {
        System.out.println("\n=== Safe Element Removal ===");
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        
        System.out.println("Original: " + numbers);
        
        // Remove even numbers using Iterator
        Iterator<Integer> iterator = numbers.iterator();
        while (iterator.hasNext()) {
            Integer num = iterator.next();
            if (num % 2 == 0) {
                iterator.remove(); // Safe removal
            }
        }
        
        System.out.println("After removing evens: " + numbers);
    }
    
    public static void demonstrateConcurrentModification() {
        System.out.println("\n=== ConcurrentModificationException ===");
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // This will throw ConcurrentModificationException
        try {
            for (String element : list) {
                if (element.equals("B")) {
                    list.remove(element); // WRONG: modifying during iteration
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Caught ConcurrentModificationException");
        }
        
        // Correct way using Iterator
        System.out.println("Correct removal using Iterator:");
        list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.equals("B")) {
                iterator.remove(); // Correct
            }
        }
        System.out.println("After removal: " + list);
        
        // Alternative: use removeIf (Java 8+)
        list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        list.removeIf(e -> e.equals("B"));
        System.out.println("Using removeIf: " + list);
    }
    
    public static void demonstrateListIterator() {
        System.out.println("\n=== ListIterator (Bidirectional) ===");
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        ListIterator<String> listIterator = list.listIterator();
        
        // Forward traversal
        System.out.println("Forward:");
        while (listIterator.hasNext()) {
            System.out.print(listIterator.next() + " ");
        }
        
        // Backward traversal
        System.out.println("\nBackward:");
        while (listIterator.hasPrevious()) {
            System.out.print(listIterator.previous() + " ");
        }
        
        // Add and set operations
        System.out.println("\n\nModifying during iteration:");
        listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            String element = listIterator.next();
            if (element.equals("B")) {
                listIterator.set("B_MODIFIED"); // Replace current element
                listIterator.add("B_INSERTED");  // Insert after current
            }
        }
        System.out.println("Modified list: " + list);
    }
    
    public static void compareIterationMethods() {
        System.out.println("\n=== Iteration Methods Comparison ===");
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }
        
        // Method 1: Iterator
        long start = System.nanoTime();
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer val = iterator.next();
        }
        long iteratorTime = System.nanoTime() - start;
        
        // Method 2: Enhanced for-loop
        start = System.nanoTime();
        for (Integer val : list) {
            // Process
        }
        long forEachTime = System.nanoTime() - start;
        
        // Method 3: Index-based
        start = System.nanoTime();
        for (int i = 0; i < list.size(); i++) {
            Integer val = list.get(i);
        }
        long indexTime = System.nanoTime() - start;
        
        // Method 4: forEach with lambda (Java 8+)
        start = System.nanoTime();
        list.forEach(val -> {
            // Process
        });
        long lambdaTime = System.nanoTime() - start;
        
        System.out.println("Iterator: " + iteratorTime + "ns");
        System.out.println("Enhanced for-loop: " + forEachTime + "ns");
        System.out.println("Index-based: " + indexTime + "ns");
        System.out.println("forEach lambda: " + lambdaTime + "ns");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **hasNext()**: O(1)
- **next()**: O(1)
- **remove()**: O(1) for Iterator itself, but may be O(n) for underlying collection (e.g., ArrayList)

**Space Complexity**: O(1) for Iterator object

## Edge Cases and Pitfalls

- **ConcurrentModificationException**: Thrown when collection is modified during iteration (except via Iterator.remove())
- **NoSuchElementException**: Thrown by next() when no more elements exist; always check hasNext()
- **IllegalStateException**: Thrown by remove() if next() hasn't been called or remove() already called after last next()
- **Enhanced For-Loop Limitation**: Cannot remove elements; must use Iterator explicitly
- **Fail-Fast vs Fail-Safe**: Most collections are fail-fast; concurrent collections like CopyOnWriteArrayList are fail-safe
- **Multiple remove() Calls**: Can only call remove() once per next() call
- **Iterator Invalidation**: After structural modification, old iterators become invalid

## Interview-Ready Answer

"Iterator provides a standard way to traverse collections. It has hasNext(), next(), and remove() methods. The key advantage is safe element removal during iteration using iterator.remove(). Enhanced for-loops use Iterator internally but don't allow removal. Collections are fail-fast by default - they throw ConcurrentModificationException if modified during iteration except via Iterator.remove(). ListIterator extends Iterator with bidirectional traversal and add/set operations."
