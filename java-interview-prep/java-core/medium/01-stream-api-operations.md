# Stream API Operations

## Problem Statement

Demonstrate the Java Stream API with various intermediate and terminal operations. Show how to use filter, map, reduce, collect, and other common operations. Explain the difference between intermediate and terminal operations.

**Requirements**:
- Demonstrate intermediate operations (filter, map, flatMap, sorted)
- Show terminal operations (collect, reduce, forEach, count)
- Explain lazy evaluation
- Show practical use cases

## Approach

- Streams provide functional-style operations on collections
- Intermediate operations are lazy and return streams
- Terminal operations trigger computation and return results
- Streams can only be consumed once
- Use method chaining for readable pipelines

## Solution

```java
import java.util.*;
import java.util.stream.*;

public class StreamAPIOperations {
    
    static class Employee {
        String name;
        String department;
        double salary;
        
        public Employee(String name, String department, double salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }
        
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public double getSalary() { return salary; }
    }
    
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "Engineering", 90000),
            new Employee("Bob", "Engineering", 85000),
            new Employee("Charlie", "Sales", 70000),
            new Employee("David", "Sales", 75000),
            new Employee("Eve", "HR", 65000)
        );
        
        demonstrateFilterAndMap(employees);
        demonstrateReduce(employees);
        demonstrateFlatMap();
        demonstrateCollectors(employees);
    }
    
    public static void demonstrateFilterAndMap(List<Employee> employees) {
        System.out.println("=== Filter and Map ===");
        
        // Filter employees with salary > 70000 and get names
        List<String> highEarners = employees.stream()
            .filter(e -> e.getSalary() > 70000)
            .map(Employee::getName)
            .collect(Collectors.toList());
        
        System.out.println("High earners: " + highEarners);
        
        // Chain multiple operations
        double avgEngineeringSalary = employees.stream()
            .filter(e -> e.getDepartment().equals("Engineering"))
            .mapToDouble(Employee::getSalary)
            .average()
            .orElse(0.0);
        
        System.out.println("Avg Engineering salary: " + avgEngineeringSalary);
    }
    
    public static void demonstrateReduce(List<Employee> employees) {
        System.out.println("\n=== Reduce Operations ===");
        
        // Sum of all salaries
        double totalSalary = employees.stream()
            .mapToDouble(Employee::getSalary)
            .sum();
        
        System.out.println("Total salary: " + totalSalary);
        
        // Using reduce
        double total = employees.stream()
            .map(Employee::getSalary)
            .reduce(0.0, Double::sum);
        
        System.out.println("Total (using reduce): " + total);
    }
    
    public static void demonstrateFlatMap() {
        System.out.println("\n=== FlatMap ===");
        
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );
        
        List<Integer> flattened = nested.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        System.out.println("Flattened: " + flattened);
    }
    
    public static void demonstrateCollectors(List<Employee> employees) {
        System.out.println("\n=== Collectors ===");
        
        // Group by department
        Map<String, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDepartment));
        
        System.out.println("Grouped by department: " + byDept.keySet());
        
        // Partition by salary
        Map<Boolean, List<Employee>> partitioned = employees.stream()
            .collect(Collectors.partitioningBy(e -> e.getSalary() > 75000));
        
        System.out.println("High salary count: " + partitioned.get(true).size());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) for most stream operations where n is the number of elements

**Space Complexity**: O(n) for collecting results, O(1) for operations like count()

## Edge Cases and Pitfalls

- **Stream reuse**: Streams can only be consumed once, reusing throws IllegalStateException
- **Lazy evaluation**: Intermediate operations don't execute until terminal operation is called
- **Parallel streams**: Be careful with stateful operations in parallel streams
- **Null handling**: Use Optional or filter to handle nulls

## Interview-Ready Answer

"Stream API provides functional operations on collections. Intermediate operations like filter and map are lazy and return streams, while terminal operations like collect and reduce trigger computation. Streams support method chaining for readable pipelines and can only be consumed once. Use collectors for complex aggregations like grouping and partitioning."
