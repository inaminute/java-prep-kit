# Collectors Usage

## Problem Statement

Demonstrate the Collectors utility class in Java Stream API. Show how to use various collectors for grouping, partitioning, joining, and custom aggregations. Explain when to use each collector type.

**Requirements**:
- Demonstrate groupingBy and partitioningBy
- Show joining and summarizing collectors
- Create custom collectors
- Explain downstream collectors

## Approach

- Collectors transform stream elements into collections or other results
- groupingBy groups elements by classifier function
- partitioningBy splits into two groups based on predicate
- joining concatenates strings
- Custom collectors implement Collector interface

## Solution

```java
import java.util.*;
import java.util.stream.*;

public class CollectorsUsage {
    
    static class Employee {
        String name;
        String dept;
        double salary;
        
        public Employee(String name, String dept, double salary) {
            this.name = name;
            this.dept = dept;
            this.salary = salary;
        }
        
        public String getName() { return name; }
        public String getDept() { return dept; }
        public double getSalary() { return salary; }
    }
    
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "IT", 90000),
            new Employee("Bob", "IT", 85000),
            new Employee("Charlie", "HR", 70000),
            new Employee("David", "HR", 75000)
        );
        
        demonstrateGrouping(employees);
        demonstratePartitioning(employees);
        demonstrateJoining(employees);
        demonstrateSummarizing(employees);
    }
    
    public static void demonstrateGrouping(List<Employee> employees) {
        System.out.println("=== Grouping ===");
        
        // Group by department
        Map<String, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDept));
        
        System.out.println("By department: " + byDept.keySet());
        
        // Group and count
        Map<String, Long> countByDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()));
        
        System.out.println("Count by dept: " + countByDept);
        
        // Group and sum salaries
        Map<String, Double> salaryByDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::getDept,
                Collectors.summingDouble(Employee::getSalary)
            ));
        
        System.out.println("Total salary by dept: " + salaryByDept);
    }
    
    public static void demonstratePartitioning(List<Employee> employees) {
        System.out.println("\n=== Partitioning ===");
        
        Map<Boolean, List<Employee>> partitioned = employees.stream()
            .collect(Collectors.partitioningBy(e -> e.getSalary() > 80000));
        
        System.out.println("High earners: " + partitioned.get(true).size());
        System.out.println("Others: " + partitioned.get(false).size());
    }
    
    public static void demonstrateJoining(List<Employee> employees) {
        System.out.println("\n=== Joining ===");
        
        String names = employees.stream()
            .map(Employee::getName)
            .collect(Collectors.joining(", "));
        
        System.out.println("Names: " + names);
    }
    
    public static void demonstrateSummarizing(List<Employee> employees) {
        System.out.println("\n=== Summarizing ===");
        
        DoubleSummaryStatistics stats = employees.stream()
            .collect(Collectors.summarizingDouble(Employee::getSalary));
        
        System.out.println("Average salary: " + stats.getAverage());
        System.out.println("Max salary: " + stats.getMax());
        System.out.println("Min salary: " + stats.getMin());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) for most collectors where n is stream size

**Space Complexity**: O(n) for collecting into collections

## Edge Cases and Pitfalls

- **Null values**: Some collectors don't handle nulls well
- **Parallel streams**: Ensure collectors are thread-safe
- **Downstream collectors**: Can nest collectors for complex aggregations

## Interview-Ready Answer

"Collectors transform stream elements into results. groupingBy groups by classifier function, partitioningBy splits into two groups. joining concatenates strings, summarizing provides statistics. Use downstream collectors for nested aggregations like grouping and counting. Collectors work with both sequential and parallel streams."
