# Comparable vs Comparator

## Problem Statement

Explain the difference between Comparable and Comparator interfaces in Java. Demonstrate when to use each, how to implement them, and how to use Comparator chaining and factory methods.

**Requirements**:
- Explain Comparable interface and natural ordering
- Demonstrate Comparator for custom sorting
- Show Comparator chaining and composition
- Compare use cases for each approach

## Approach

- Comparable: defines natural ordering within the class itself
- Comparator: defines external ordering, allows multiple sort orders
- Comparable has compareTo() method
- Comparator has compare() method
- Java 8+ provides Comparator factory methods and chaining
- Collections.sort() and Arrays.sort() use both

## Solution

```java
import java.util.*;

public class ComparableVsComparator {
    
    // Class implementing Comparable
    static class Employee implements Comparable<Employee> {
        String name;
        int age;
        double salary;
        
        public Employee(String name, int age, double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }
        
        @Override
        public int compareTo(Employee other) {
            // Natural ordering by name
            return this.name.compareTo(other.name);
        }
        
        @Override
        public String toString() {
            return String.format("%s(age=%d, salary=%.0f)", name, age, salary);
        }
    }
    
    public static void main(String[] args) {
        demonstrateComparable();
        demonstrateComparator();
        demonstrateComparatorChaining();
        demonstrateComparatorFactoryMethods();
    }
    
    public static void demonstrateComparable() {
        System.out.println("=== Comparable (Natural Ordering) ===");
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Charlie", 35, 75000));
        employees.add(new Employee("Alice", 30, 80000));
        employees.add(new Employee("Bob", 25, 70000));
        
        System.out.println("Before sorting: " + employees);
        Collections.sort(employees); // Uses compareTo()
        System.out.println("After sorting (by name): " + employees);
        
        // TreeSet uses Comparable automatically
        TreeSet<Employee> treeSet = new TreeSet<>(employees);
        System.out.println("TreeSet (sorted): " + treeSet);
    }
    
    public static void demonstrateComparator() {
        System.out.println("\n=== Comparator (Custom Ordering) ===");
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Charlie", 35, 75000));
        employees.add(new Employee("Alice", 30, 80000));
        employees.add(new Employee("Bob", 25, 70000));
        
        // Sort by age using anonymous class
        Comparator<Employee> byAge = new Comparator<Employee>() {
            @Override
            public int compare(Employee e1, Employee e2) {
                return Integer.compare(e1.age, e2.age);
            }
        };
        
        Collections.sort(employees, byAge);
        System.out.println("Sorted by age: " + employees);
        
        // Sort by salary using lambda
        Collections.sort(employees, (e1, e2) -> Double.compare(e1.salary, e2.salary));
        System.out.println("Sorted by salary: " + employees);
        
        // Reverse order
        Collections.sort(employees, byAge.reversed());
        System.out.println("Sorted by age (reversed): " + employees);
    }
    
    public static void demonstrateComparatorChaining() {
        System.out.println("\n=== Comparator Chaining ===");
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Alice", 30, 80000));
        employees.add(new Employee("Bob", 30, 70000));
        employees.add(new Employee("Charlie", 25, 75000));
        employees.add(new Employee("David", 30, 80000));
        
        // Sort by age, then by salary, then by name
        Comparator<Employee> multiSort = new Comparator<Employee>() {
            @Override
            public int compare(Employee e1, Employee e2) {
                int ageCompare = Integer.compare(e1.age, e2.age);
                if (ageCompare != 0) return ageCompare;
                
                int salaryCompare = Double.compare(e1.salary, e2.salary);
                if (salaryCompare != 0) return salaryCompare;
                
                return e1.name.compareTo(e2.name);
            }
        };
        
        Collections.sort(employees, multiSort);
        System.out.println("Multi-level sort: " + employees);
    }
    
    public static void demonstrateComparatorFactoryMethods() {
        System.out.println("\n=== Comparator Factory Methods (Java 8+) ===");
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Alice", 30, 80000));
        employees.add(new Employee("Bob", 30, 70000));
        employees.add(new Employee("Charlie", 25, 75000));
        employees.add(new Employee("David", 30, 80000));
        
        // Using Comparator.comparing()
        Comparator<Employee> byName = Comparator.comparing(e -> e.name);
        Collections.sort(employees, byName);
        System.out.println("By name: " + employees);
        
        // Method reference
        Comparator<Employee> byAge = Comparator.comparingInt(e -> e.age);
        Collections.sort(employees, byAge);
        System.out.println("By age: " + employees);
        
        // Chaining with thenComparing
        Comparator<Employee> chainedComparator = Comparator
            .comparingInt((Employee e) -> e.age)
            .thenComparingDouble(e -> e.salary)
            .thenComparing(e -> e.name);
        
        Collections.sort(employees, chainedComparator);
        System.out.println("Chained (age, salary, name): " + employees);
        
        // Null-safe comparator
        employees.add(null);
        Comparator<Employee> nullSafe = Comparator.nullsLast(
            Comparator.comparing(e -> e.name)
        );
        Collections.sort(employees, nullSafe);
        System.out.println("Null-safe sort: " + employees);
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **compareTo()**: Depends on implementation, typically O(1) for primitives
- **compare()**: Depends on implementation
- **Collections.sort()**: O(n log n) regardless of comparator

**Space Complexity**: O(1) for comparator objects

## Edge Cases and Pitfalls

- **Consistency with equals()**: compareTo() should be consistent with equals(); if a.equals(b), then a.compareTo(b) should be 0
- **Null Handling**: compareTo() throws NullPointerException for null; use Comparator.nullsFirst() or nullsLast()
- **Return Values**: compareTo/compare should return negative, zero, or positive (not just -1, 0, 1)
- **Transitivity**: If a > b and b > c, then a > c must hold
- **When to Use Comparable**: Single, natural ordering that makes sense for the class
- **When to Use Comparator**: Multiple sort orders, or sorting classes you can't modify
- **TreeSet/TreeMap**: Use Comparable by default, or accept Comparator in constructor

## Interview-Ready Answer

"Comparable defines natural ordering within a class via compareTo() method, used when there's one obvious way to sort. Comparator defines external ordering via compare() method, allowing multiple sort strategies without modifying the class. Java 8+ provides Comparator factory methods like comparing() and thenComparing() for easy composition. Use Comparable for default ordering, Comparator for custom or multiple orderings. Both should be consistent with equals()."
