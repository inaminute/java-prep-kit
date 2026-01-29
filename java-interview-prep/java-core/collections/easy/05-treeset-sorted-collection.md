# TreeSet and Sorted Collections

## Problem Statement

Explain how TreeSet maintains sorted order in Java and demonstrate its usage with natural ordering and custom Comparators. Compare TreeSet with HashSet and show NavigableSet operations.

**Requirements**:
- Explain Red-Black tree implementation
- Demonstrate natural ordering vs Comparator
- Show NavigableSet operations
- Compare performance with HashSet

## Approach

- TreeSet is backed by a TreeMap (Red-Black tree)
- Elements are sorted according to natural ordering or provided Comparator
- No duplicates allowed (like all Sets)
- NavigableSet operations: floor, ceiling, higher, lower
- O(log n) for add, remove, contains operations
- Null elements not allowed (would cause NullPointerException during comparison)

## Solution

```java
import java.util.*;

public class TreeSetSortedCollection {
    
    static class Person implements Comparable<Person> {
        String name;
        int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public int compareTo(Person other) {
            // Natural ordering by age
            return Integer.compare(this.age, other.age);
        }
        
        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }
    
    public static void main(String[] args) {
        demonstrateNaturalOrdering();
        demonstrateCustomComparator();
        demonstrateNavigableOperations();
        compareWithHashSet();
    }
    
    public static void demonstrateNaturalOrdering() {
        System.out.println("=== Natural Ordering ===");
        TreeSet<Integer> numbers = new TreeSet<>();
        
        // Add in random order
        numbers.addAll(Arrays.asList(5, 2, 8, 1, 9, 3, 7));
        
        System.out.println("TreeSet (sorted): " + numbers);
        System.out.println("First: " + numbers.first());
        System.out.println("Last: " + numbers.last());
        
        // Custom objects with Comparable
        TreeSet<Person> people = new TreeSet<>();
        people.add(new Person("Alice", 30));
        people.add(new Person("Bob", 25));
        people.add(new Person("Charlie", 35));
        
        System.out.println("People sorted by age: " + people);
    }
    
    public static void demonstrateCustomComparator() {
        System.out.println("\n=== Custom Comparator ===");
        
        // Sort by name instead of age
        Comparator<Person> byName = Comparator.comparing(p -> p.name);
        TreeSet<Person> peopleByName = new TreeSet<>(byName);
        
        peopleByName.add(new Person("Charlie", 35));
        peopleByName.add(new Person("Alice", 30));
        peopleByName.add(new Person("Bob", 25));
        
        System.out.println("People sorted by name: " + peopleByName);
        
        // Reverse order
        TreeSet<Integer> reverseNumbers = new TreeSet<>(Comparator.reverseOrder());
        reverseNumbers.addAll(Arrays.asList(5, 2, 8, 1, 9, 3));
        
        System.out.println("Reverse order: " + reverseNumbers);
    }
    
    public static void demonstrateNavigableOperations() {
        System.out.println("\n=== NavigableSet Operations ===");
        NavigableSet<Integer> set = new TreeSet<>();
        set.addAll(Arrays.asList(1, 3, 5, 7, 9, 11, 13));
        
        System.out.println("Set: " + set);
        
        // Navigation methods
        System.out.println("lower(7): " + set.lower(7));     // < 7
        System.out.println("floor(7): " + set.floor(7));     // <= 7
        System.out.println("ceiling(7): " + set.ceiling(7)); // >= 7
        System.out.println("higher(7): " + set.higher(7));   // > 7
        
        System.out.println("lower(6): " + set.lower(6));     // 5
        System.out.println("ceiling(6): " + set.ceiling(6)); // 7
        
        // Subset operations
        System.out.println("headSet(7): " + set.headSet(7));           // < 7
        System.out.println("tailSet(7): " + set.tailSet(7));           // >= 7
        System.out.println("subSet(3, 11): " + set.subSet(3, 11));     // [3, 11)
        
        // Descending operations
        System.out.println("descendingSet: " + set.descendingSet());
        System.out.println("pollFirst: " + set.pollFirst());
        System.out.println("pollLast: " + set.pollLast());
        System.out.println("After polls: " + set);
    }
    
    public static void compareWithHashSet() {
        System.out.println("\n=== TreeSet vs HashSet Performance ===");
        
        int size = 100000;
        
        // TreeSet performance
        TreeSet<Integer> treeSet = new TreeSet<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeSet.add(i);
        }
        long treeAddTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            treeSet.contains(i * 100);
        }
        long treeContainsTime = System.nanoTime() - start;
        
        // HashSet performance
        HashSet<Integer> hashSet = new HashSet<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashSet.add(i);
        }
        long hashAddTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            hashSet.contains(i * 100);
        }
        long hashContainsTime = System.nanoTime() - start;
        
        System.out.println("TreeSet add: " + treeAddTime + "ns (O(log n))");
        System.out.println("HashSet add: " + hashAddTime + "ns (O(1))");
        System.out.println("TreeSet contains: " + treeContainsTime + "ns");
        System.out.println("HashSet contains: " + hashContainsTime + "ns");
        
        System.out.println("\nTreeSet advantages: sorted order, range operations");
        System.out.println("HashSet advantages: faster operations");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **add(E e)**: O(log n)
- **remove(Object o)**: O(log n)
- **contains(Object o)**: O(log n)
- **first/last**: O(log n)
- **lower/floor/ceiling/higher**: O(log n)

**Space Complexity**: O(n) for storing elements in tree structure

## Edge Cases and Pitfalls

- **Null Elements**: TreeSet does not allow null (throws NullPointerException)
- **Comparable Requirement**: Elements must be Comparable or provide a Comparator
- **ClassCastException**: Adding incomparable elements throws ClassCastException
- **Comparator Consistency**: Comparator should be consistent with equals for predictable behavior
- **Performance**: Slower than HashSet but provides sorted order
- **When to Use TreeSet**: Need sorted order, range queries, or NavigableSet operations
- **When to Use HashSet**: Only need uniqueness, don't care about order, need faster operations

## Interview-Ready Answer

"TreeSet is backed by a Red-Black tree (TreeMap internally) and maintains elements in sorted order. It provides O(log n) operations for add, remove, and contains. Elements must implement Comparable or you must provide a Comparator. TreeSet supports NavigableSet operations like floor, ceiling, higher, lower for range queries. Unlike HashSet's O(1) operations, TreeSet is slower but provides sorted order and powerful range operations. Null elements are not allowed."
