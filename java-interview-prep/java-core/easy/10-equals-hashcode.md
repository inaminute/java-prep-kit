# equals() and hashCode()

## Problem Statement

Explain the contract between equals() and hashCode() methods in Java. Why must they be overridden together? Demonstrate proper implementation and the consequences of incorrect implementation.

**Requirements**:
- Explain the equals() and hashCode() contract
- Demonstrate proper implementation
- Show what happens with incorrect implementation
- Explain usage in collections

## Approach

- equals() determines logical equality between objects
- hashCode() returns an integer hash value for the object
- Contract: equal objects must have equal hash codes
- If equals() is overridden, hashCode() must also be overridden
- Critical for proper behavior in hash-based collections (HashMap, HashSet)

## Solution

```java
import java.util.*;

public class EqualsHashCode {
    
    // Correct implementation
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
    
    // Incorrect: equals() overridden but not hashCode()
    static class BrokenPerson {
        private String name;
        private int age;
        
        public BrokenPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BrokenPerson that = (BrokenPerson) o;
            return age == that.age && Objects.equals(name, that.name);
        }
        
        // Missing hashCode() override!
        
        @Override
        public String toString() {
            return name + " (" + age + ")";
        }
    }
    
    public static void main(String[] args) {
        demonstrateCorrectImplementation();
        demonstrateBrokenImplementation();
        demonstrateHashCodeContract();
    }
    
    public static void demonstrateCorrectImplementation() {
        System.out.println("=== Correct Implementation ===");
        
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Alice", 30);
        
        System.out.println("p1.equals(p2): " + p1.equals(p2));
        System.out.println("p1.hashCode(): " + p1.hashCode());
        System.out.println("p2.hashCode(): " + p2.hashCode());
        
        // Works correctly in HashMap
        Map<Person, String> map = new HashMap<>();
        map.put(p1, "Engineer");
        System.out.println("Retrieved with p2: " + map.get(p2)); // Works!
        
        // Works correctly in HashSet
        Set<Person> set = new HashSet<>();
        set.add(p1);
        System.out.println("Set contains p2: " + set.contains(p2)); // true
    }
    
    public static void demonstrateBrokenImplementation() {
        System.out.println("\n=== Broken Implementation ===");
        
        BrokenPerson bp1 = new BrokenPerson("Bob", 25);
        BrokenPerson bp2 = new BrokenPerson("Bob", 25);
        
        System.out.println("bp1.equals(bp2): " + bp1.equals(bp2)); // true
        System.out.println("bp1.hashCode(): " + bp1.hashCode());
        System.out.println("bp2.hashCode(): " + bp2.hashCode()); // Different!
        
        // Broken in HashMap
        Map<BrokenPerson, String> map = new HashMap<>();
        map.put(bp1, "Designer");
        System.out.println("Retrieved with bp2: " + map.get(bp2)); // null!
        
        // Broken in HashSet
        Set<BrokenPerson> set = new HashSet<>();
        set.add(bp1);
        System.out.println("Set contains bp2: " + set.contains(bp2)); // false!
    }
    
    public static void demonstrateHashCodeContract() {
        System.out.println("\n=== HashCode Contract ===");
        
        Person p1 = new Person("Charlie", 35);
        Person p2 = new Person("Charlie", 35);
        Person p3 = new Person("David", 40);
        
        // Rule 1: Equal objects must have equal hash codes
        if (p1.equals(p2)) {
            System.out.println("p1 equals p2, hash codes equal: " + 
                (p1.hashCode() == p2.hashCode()));
        }
        
        // Rule 2: Unequal objects may have equal hash codes (collision)
        System.out.println("p1 equals p3: " + p1.equals(p3));
        System.out.println("Hash codes may still collide (unlikely with good hash)");
        
        // Rule 3: hashCode() must be consistent
        int hash1 = p1.hashCode();
        int hash2 = p1.hashCode();
        System.out.println("Consistent hashCode: " + (hash1 == hash2));
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **equals()**: O(1) for primitive fields, O(n) for comparing strings/collections
- **hashCode()**: O(1) for primitive fields, O(n) for strings/collections

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Breaking the contract**: If equals() returns true but hashCode() differs, hash collections fail
- **Mutable fields in hash**: Don't use mutable fields in hashCode() if object is used as key
- **Null handling**: equals() should handle null gracefully, hashCode() should handle null fields
- **Inheritance**: Be careful with equals() in inheritance hierarchies

## Interview-Ready Answer

"equals() and hashCode() must be overridden together. The contract states equal objects must have equal hash codes. This is critical for hash-based collections like HashMap and HashSet. If you override equals() but not hashCode(), equal objects may have different hash codes, causing them to be stored in different buckets and breaking collection behavior."
