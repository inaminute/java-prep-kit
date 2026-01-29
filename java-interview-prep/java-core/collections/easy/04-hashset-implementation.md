# HashSet Implementation and Uniqueness

## Problem Statement

Explain how HashSet is implemented internally in Java and how it guarantees uniqueness. Demonstrate the relationship between HashSet and HashMap, and show proper usage with custom objects.

**Requirements**:
- Explain HashSet's internal HashMap implementation
- Demonstrate uniqueness guarantee
- Show proper equals() and hashCode() implementation
- Compare with TreeSet

## Approach

- HashSet is backed by a HashMap internally
- Elements are stored as keys in the HashMap
- All values in the backing HashMap are the same dummy object (PRESENT)
- Uniqueness is guaranteed by HashMap's key uniqueness
- add() returns false if element already exists
- No ordering guarantee (not sorted, not insertion-order)

## Solution

```java
import java.util.*;

public class HashSetImplementation {
    
    static class Person {
        private String name;
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Person)) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
        
        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }
    
    static class BadPerson {
        private String name;
        private int age;
        
        public BadPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        // Missing equals() and hashCode() - will use Object's implementation
        
        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }
    
    public static void main(String[] args) {
        demonstrateBasicUsage();
        demonstrateUniqueness();
        demonstrateCustomObjects();
        demonstrateBadImplementation();
        compareWithTreeSet();
    }
    
    public static void demonstrateBasicUsage() {
        System.out.println("=== Basic HashSet Usage ===");
        Set<String> set = new HashSet<>();
        
        // Add elements
        System.out.println("Add 'Apple': " + set.add("Apple"));
        System.out.println("Add 'Banana': " + set.add("Banana"));
        System.out.println("Add 'Apple' again: " + set.add("Apple")); // false
        
        System.out.println("Set: " + set);
        System.out.println("Size: " + set.size());
    }
    
    public static void demonstrateUniqueness() {
        System.out.println("\n=== Uniqueness Guarantee ===");
        Set<Integer> set = new HashSet<>();
        
        // Add duplicates
        int[] numbers = {1, 2, 3, 2, 4, 3, 5, 1};
        for (int num : numbers) {
            boolean added = set.add(num);
            System.out.println("Add " + num + ": " + added);
        }
        
        System.out.println("Final set: " + set);
        System.out.println("Original array length: " + numbers.length);
        System.out.println("Set size: " + set.size());
    }
    
    public static void demonstrateCustomObjects() {
        System.out.println("\n=== Custom Objects with Proper equals/hashCode ===");
        Set<Person> people = new HashSet<>();
        
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Bob", 25);
        Person p3 = new Person("Alice", 30); // Duplicate of p1
        
        System.out.println("Add p1: " + people.add(p1));
        System.out.println("Add p2: " + people.add(p2));
        System.out.println("Add p3 (duplicate): " + people.add(p3));
        
        System.out.println("Set size: " + people.size());
        System.out.println("Contains new Person('Alice', 30): " + 
                         people.contains(new Person("Alice", 30)));
    }
    
    public static void demonstrateBadImplementation() {
        System.out.println("\n=== Bad Implementation (Missing equals/hashCode) ===");
        Set<BadPerson> badPeople = new HashSet<>();
        
        BadPerson bp1 = new BadPerson("Alice", 30);
        BadPerson bp2 = new BadPerson("Alice", 30); // Should be duplicate
        
        System.out.println("Add bp1: " + badPeople.add(bp1));
        System.out.println("Add bp2 (should be duplicate): " + badPeople.add(bp2));
        
        System.out.println("Set size: " + badPeople.size()); // 2 instead of 1!
        System.out.println("Problem: Without proper equals/hashCode, " +
                         "identical objects are treated as different");
    }
    
    public static void compareWithTreeSet() {
        System.out.println("\n=== HashSet vs TreeSet ===");
        
        Set<Integer> hashSet = new HashSet<>();
        Set<Integer> treeSet = new TreeSet<>();
        
        int[] numbers = {5, 2, 8, 1, 9, 3};
        
        for (int num : numbers) {
            hashSet.add(num);
            treeSet.add(num);
        }
        
        System.out.println("HashSet (no order): " + hashSet);
        System.out.println("TreeSet (sorted): " + treeSet);
        
        // Performance comparison
        Set<Integer> largeHashSet = new HashSet<>();
        Set<Integer> largeTreeSet = new TreeSet<>();
        
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            largeHashSet.add(i);
        }
        long hashTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            largeTreeSet.add(i);
        }
        long treeTime = System.nanoTime() - start;
        
        System.out.println("\nHashSet add time: " + hashTime + "ns");
        System.out.println("TreeSet add time: " + treeTime + "ns");
        System.out.println("HashSet is " + (treeTime / hashTime) + "x faster");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **add(E e)**: O(1) average case
- **remove(Object o)**: O(1) average case
- **contains(Object o)**: O(1) average case
- **size()**: O(1)
- **Iteration**: O(n) where n is capacity + size

**Space Complexity**: O(n) where n is number of elements

**Comparison with TreeSet**:
- HashSet: O(1) operations, no ordering
- TreeSet: O(log n) operations, sorted order

## Edge Cases and Pitfalls

- **Null Element**: HashSet allows one null element (unlike TreeSet)
- **No Ordering**: Elements are not stored in any particular order; iteration order may vary
- **equals() and hashCode()**: Must override both consistently for custom objects
- **Mutable Elements**: Don't modify elements after adding to set; may become unfindable
- **Initial Capacity**: Like HashMap, can specify initial capacity to avoid rehashing
- **Load Factor**: Default 0.75; higher values save memory but increase collision chance
- **Thread Safety**: Not thread-safe; use Collections.synchronizedSet() or ConcurrentHashMap.newKeySet()
- **Backed by HashMap**: Understanding HashMap helps understand HashSet behavior

## Interview-Ready Answer

"HashSet is internally backed by a HashMap where elements are stored as keys and all values are a dummy PRESENT object. This leverages HashMap's key uniqueness to guarantee no duplicates. Operations are O(1) average case. Elements must properly implement equals() and hashCode() for correct behavior. Unlike TreeSet which maintains sorted order with O(log n) operations, HashSet provides no ordering but faster O(1) operations."
