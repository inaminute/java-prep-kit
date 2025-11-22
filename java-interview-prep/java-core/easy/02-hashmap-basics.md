# HashMap Basics

## Problem Statement

Explain how HashMap works internally in Java. Implement a simple example demonstrating HashMap usage, collision handling, and the importance of proper hashCode() and equals() implementation.

**Requirements**:
- Explain the internal structure (buckets, hash function, collision resolution)
- Demonstrate proper usage with custom objects
- Show what happens with poor hashCode() implementation

## Approach

- HashMap uses an array of buckets where each bucket can hold multiple entries
- Hash function determines which bucket an entry goes into
- Collisions are handled using linked lists (or trees in Java 8+ when bucket size exceeds threshold)
- Proper hashCode() and equals() implementation is critical for correct behavior
- Load factor determines when the HashMap resizes (default 0.75)

## Solution

```java
import java.util.*;

public class HashMapBasics {
    
    // Custom class demonstrating proper hashCode and equals
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
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
        
        @Override
        public String toString() {
            return name + " (" + age + ")";
        }
    }
    
    // Example with poor hashCode - all objects go to same bucket
    static class BadHashPerson {
        private String name;
        private int age;
        
        public BadHashPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BadHashPerson that = (BadHashPerson) o;
            return age == that.age && Objects.equals(name, that.name);
        }
        
        @Override
        public int hashCode() {
            return 1; // Poor implementation - causes all entries to collide
        }
    }
    
    public static void main(String[] args) {
        demonstrateBasicUsage();
        demonstrateCustomObjects();
        demonstrateCollisions();
    }
    
    public static void demonstrateBasicUsage() {
        Map<String, Integer> ages = new HashMap<>();
        
        // Basic operations
        ages.put("Alice", 30);
        ages.put("Bob", 25);
        ages.put("Charlie", 35);
        
        System.out.println("Age of Alice: " + ages.get("Alice"));
        System.out.println("Contains Bob? " + ages.containsKey("Bob"));
        
        // Iteration
        for (Map.Entry<String, Integer> entry : ages.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
    
    public static void demonstrateCustomObjects() {
        Map<Person, String> personMap = new HashMap<>();
        
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Bob", 25);
        Person p3 = new Person("Alice", 30); // Same as p1
        
        personMap.put(p1, "Engineer");
        personMap.put(p2, "Designer");
        
        // p3 equals p1, so it retrieves the same value
        System.out.println("Occupation of p3: " + personMap.get(p3));
        System.out.println("Map size: " + personMap.size()); // Still 2
    }
    
    public static void demonstrateCollisions() {
        Map<BadHashPerson, String> badMap = new HashMap<>();
        
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            badMap.put(new BadHashPerson("Person" + i, i), "Value" + i);
        }
        long badTime = System.nanoTime() - startTime;
        
        Map<Person, String> goodMap = new HashMap<>();
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            goodMap.put(new Person("Person" + i, i), "Value" + i);
        }
        long goodTime = System.nanoTime() - startTime;
        
        System.out.println("Bad hashCode time: " + badTime + "ns");
        System.out.println("Good hashCode time: " + goodTime + "ns");
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **get()**: O(1) average case, O(n) worst case (all keys in same bucket)
- **put()**: O(1) average case, O(n) worst case
- **remove()**: O(1) average case, O(n) worst case
- **containsKey()**: O(1) average case, O(n) worst case

**Space Complexity**: O(n) where n is the number of key-value pairs

## Edge Cases and Pitfalls

- **Null Keys and Values**: HashMap allows one null key and multiple null values, unlike Hashtable
- **Poor hashCode()**: If hashCode() always returns the same value, HashMap degrades to O(n) performance as all entries collide
- **Missing equals()**: If you override hashCode() but not equals(), or vice versa, HashMap will not work correctly
- **Mutable Keys**: Never modify an object used as a key after insertion, as it may become unretrievable if its hashCode changes
- **Concurrent Modification**: HashMap is not thread-safe; use ConcurrentHashMap for concurrent access

## Interview-Ready Answer

"HashMap uses an array of buckets where entries are stored based on their hash code. It provides O(1) average-case performance for get and put operations. Collisions are handled using linked lists or trees. Proper implementation of hashCode() and equals() is critical - they must be consistent, meaning equal objects must have equal hash codes. HashMap allows one null key and multiple null values, but is not thread-safe."
