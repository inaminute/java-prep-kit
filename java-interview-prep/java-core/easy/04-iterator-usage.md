# Iterator Usage

## Problem Statement

Explain the Iterator pattern in Java and demonstrate its proper usage. Show how to safely remove elements during iteration and explain the difference between Iterator and enhanced for-loop. Implement examples showing common pitfalls and best practices.

**Requirements**:
- Demonstrate basic Iterator usage
- Show safe element removal during iteration
- Explain ConcurrentModificationException
- Compare Iterator with enhanced for-loop

## Approach

- Iterator provides a way to traverse collections without exposing internal structure
- Use iterator.remove() to safely remove elements during iteration
- Enhanced for-loop creates an implicit iterator but doesn't allow removal
- ConcurrentModificationException occurs when collection is modified during iteration (except via iterator.remove())
- ListIterator extends Iterator with bidirectional traversal and element modification

## Solution

```java
import java.util.*;

public class IteratorUsage {
    
    public static void main(String[] args) {
        demonstrateBasicIteration();
        demonstrateSafeRemoval();
        demonstrateConcurrentModification();
        demonstrateListIterator();
    }
    
    public static void demonstrateBasicIteration() {
        System.out.println("=== Basic Iterator Usage ===");
        
        List<String> fruits = new ArrayList<>(Arrays.asList("Apple", "Banana", "Cherry", "Date"));
        
        // Using Iterator
        Iterator<String> iterator = fruits.iterator();
        while (iterator.hasNext()) {
            String fruit = iterator.next();
            System.out.println(fruit);
        }
        
        // Using enhanced for-loop (creates implicit iterator)
        System.out.println("\nUsing enhanced for-loop:");
        for (String fruit : fruits) {
            System.out.println(fruit);
        }
    }
    
    public static void demonstrateSafeRemoval() {
        System.out.println("\n=== Safe Element Removal ===");
        
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        
        // Correct way: using iterator.remove()
        Iterator<Integer> iterator = numbers.iterator();
        while (iterator.hasNext()) {
            Integer num = iterator.next();
            if (num % 2 == 0) {
                iterator.remove(); // Safe removal
            }
        }
        System.out.println("After removing even numbers: " + numbers);
        
        // Alternative: removeIf (Java 8+)
        List<Integer> numbers2 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        numbers2.removeIf(num -> num % 2 == 0);
        System.out.println("Using removeIf: " + numbers2);
    }
    
    public static void demonstrateConcurrentModification() {
        System.out.println("\n=== ConcurrentModificationException ===");
        
        List<String> items = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // This will throw ConcurrentModificationException
        try {
            for (String item : items) {
                if (item.equals("B")) {
                    items.remove(item); // Modifying collection during iteration
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("ConcurrentModificationException caught!");
        }
        
        // Correct approach
        items = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        Iterator<String> iterator = items.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.equals("B")) {
                iterator.remove(); // Safe
            }
        }
        System.out.println("After safe removal: " + items);
    }
    
    public static void demonstrateListIterator() {
        System.out.println("\n=== ListIterator Usage ===");
        
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // Forward iteration
        ListIterator<String> listIterator = list.listIterator();
        System.out.print("Forward: ");
        while (listIterator.hasNext()) {
            System.out.print(listIterator.next() + " ");
        }
        System.out.println();
        
        // Backward iteration
        System.out.print("Backward: ");
        while (listIterator.hasPrevious()) {
            System.out.print(listIterator.previous() + " ");
        }
        System.out.println();
        
        // Modifying elements
        listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            String item = listIterator.next();
            if (item.equals("B")) {
                listIterator.set("B_MODIFIED"); // Replace element
            }
            if (item.equals("C")) {
                listIterator.add("C_INSERTED"); // Insert after C
            }
        }
        System.out.println("After modifications: " + list);
    }
    
    // Custom iterator example
    static class CustomCollection implements Iterable<String> {
        private List<String> items = new ArrayList<>();
        
        public void add(String item) {
            items.add(item);
        }
        
        @Override
        public Iterator<String> iterator() {
            return items.iterator();
        }
    }
    
    // Filtering iterator example
    static class FilteringIterator<T> implements Iterator<T> {
        private Iterator<T> iterator;
        private T next;
        private boolean hasNext;
        private java.util.function.Predicate<T> predicate;
        
        public FilteringIterator(Iterator<T> iterator, java.util.function.Predicate<T> predicate) {
            this.iterator = iterator;
            this.predicate = predicate;
            advance();
        }
        
        private void advance() {
            hasNext = false;
            while (iterator.hasNext()) {
                next = iterator.next();
                if (predicate.test(next)) {
                    hasNext = true;
                    break;
                }
            }
        }
        
        @Override
        public boolean hasNext() {
            return hasNext;
        }
        
        @Override
        public T next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            T result = next;
            advance();
            return result;
        }
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **hasNext()**: O(1)
- **next()**: O(1)
- **remove()**: O(1) for LinkedList, O(n) for ArrayList (due to shifting elements)

**Space Complexity**: O(1) - Iterator maintains minimal state

## Edge Cases and Pitfalls

- **ConcurrentModificationException**: Thrown when collection is modified during iteration except via iterator.remove()
- **Multiple remove() calls**: Calling iterator.remove() twice without calling next() throws IllegalStateException
- **Enhanced for-loop limitation**: Cannot remove elements using enhanced for-loop - must use explicit Iterator
- **Iterator invalidation**: After calling iterator.remove(), you must call next() before calling remove() again
- **Concurrent access**: Standard iterators are not thread-safe; use concurrent collections or external synchronization

## Interview-Ready Answer

"Iterator provides a way to traverse collections without exposing their internal structure. Use iterator.remove() to safely remove elements during iteration. Enhanced for-loops create an implicit iterator but don't allow removal. Modifying a collection during iteration (except via iterator.remove()) throws ConcurrentModificationException. ListIterator extends Iterator with bidirectional traversal and the ability to modify elements during iteration."
