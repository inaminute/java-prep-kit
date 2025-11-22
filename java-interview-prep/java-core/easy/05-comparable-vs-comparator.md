# Comparable vs Comparator

## Problem Statement

Explain the difference between Comparable and Comparator interfaces in Java. When would you use each? Implement examples showing natural ordering with Comparable and custom ordering with Comparator.

**Requirements**:
- Demonstrate Comparable for natural ordering
- Show multiple Comparator implementations for different sorting criteria
- Explain when to use each interface

## Approach

- Comparable defines natural ordering within the class itself (compareTo method)
- Comparator defines external ordering logic (compare method)
- Use Comparable when there's one obvious natural ordering
- Use Comparator when you need multiple sorting strategies or can't modify the class
- Comparator allows sorting without modifying the original class

## Solution

```java
import java.util.*;

// Using Comparable for natural ordering
class Employee implements Comparable<Employee> {
    private String name;
    private int id;
    private double salary;
    
    public Employee(String name, int id, double salary) {
        this.name = name;
        this.id = id;
        this.salary = salary;
    }
    
    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id); // Natural ordering by ID
    }
    
    public String getName() { return name; }
    public int getId() { return id; }
    public double getSalary() { return salary; }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %d, Salary: %.2f)", name, id, salary);
    }
}

public class ComparableVsComparator {
    
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", 103, 75000),
            new Employee("Bob", 101, 65000),
            new Employee("Charlie", 102, 80000)
        );
        
        demonstrateComparable(new ArrayList<>(employees));
        demonstrateComparator(new ArrayList<>(employees));
    }
    
    public static void demonstrateComparable(List<Employee> employees) {
        System.out.println("=== Using Comparable (Natural Ordering by ID) ===");
        Collections.sort(employees);
        employees.forEach(System.out::println);
    }
    
    public static void demonstrateComparator(List<Employee> employees) {
        System.out.println("\n=== Using Comparator ===");
        
        // Sort by name
        Comparator<Employee> nameComparator = new Comparator<Employee>() {
            @Override
            public int compare(Employee e1, Employee e2) {
                return e1.getName().compareTo(e2.getName());
            }
        };
        
        Collections.sort(employees, nameComparator);
        System.out.println("Sorted by name:");
        employees.forEach(System.out::println);
        
        // Sort by salary (using lambda)
        employees.sort((e1, e2) -> Double.compare(e1.getSalary(), e2.getSalary()));
        System.out.println("\nSorted by salary:");
        employees.forEach(System.out::println);
        
        // Sort by salary descending (using Comparator methods)
        employees.sort(Comparator.comparingDouble(Employee::getSalary).reversed());
        System.out.println("\nSorted by salary (descending):");
        employees.forEach(System.out::println);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n log n) for sorting with either Comparable or Comparator

**Space Complexity**: O(1) for comparison logic, O(log n) for sorting stack space

## Edge Cases and Pitfalls

- **Null Handling**: compareTo and compare should handle null values appropriately or throw NullPointerException
- **Consistency with equals**: If compareTo returns 0, equals should return true for proper behavior in sorted collections
- **Modifying sorted collections**: Don't modify objects used as keys in TreeSet/TreeMap after insertion
- **Multiple Comparators**: Use Comparator.thenComparing() for multi-level sorting

## Interview-Ready Answer

"Comparable defines natural ordering within a class using compareTo(), while Comparator provides external ordering using compare(). Use Comparable when there's one obvious natural ordering. Use Comparator for multiple sorting strategies or when you can't modify the class. Comparator is more flexible as it allows different sorting criteria without changing the class."
