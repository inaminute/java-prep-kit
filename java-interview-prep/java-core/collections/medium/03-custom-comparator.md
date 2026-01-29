# Custom Comparator Implementation

## Problem Statement

Demonstrate advanced Comparator implementations including multi-field sorting, null handling, and using Java 8+ Comparator factory methods. Show how to chain comparators and create complex sorting logic.

**Requirements**:
- Implement multi-field comparators
- Handle null values safely
- Use Comparator.comparing() and chaining
- Show reverse ordering and custom logic

## Approach

- Comparator.comparing() creates comparators from key extractors
- thenComparing() chains multiple comparators
- nullsFirst() and nullsLast() handle null values
- reversed() inverts ordering
- Custom compare() logic for complex scenarios
- Comparator is functional interface (can use lambdas)

## Solution

```java
import java.util.*;

public class CustomComparator {
    
    static class Employee {
        String name;
        String department;
        int age;
        double salary;
        
        public Employee(String name, String department, int age, double salary) {
            this.name = name;
            this.department = department;
            this.age = age;
            this.salary = salary;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s, age=%d, $%.0f)", 
                name, department, age, salary);
        }
    }
    
    public static void main(String[] args) {
        demonstrateBasicComparator();
        demonstrateChaining();
        demonstrateNullHandling();
        demonstrateComplexLogic();
    }
    
    public static void demonstrateBasicComparator() {
        System.out.println("=== Basic Comparator ===");
        
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "IT", 30, 80000),
            new Employee("Bob", "HR", 25, 60000),
            new Employee("Charlie", "IT", 35, 90000)
        );
        
        // Sort by name (lambda)
        employees.sort((e1, e2) -> e1.name.compareTo(e2.name));
        System.out.println("By name: " + employees);
        
        // Sort by age (Comparator.comparingInt)
        employees.sort(Comparator.comparingInt(e -> e.age));
        System.out.println("By age: " + employees);
        
        // Sort by salary (Comparator.comparingDouble)
        employees.sort(Comparator.comparingDouble(e -> e.salary));
        System.out.println("By salary: " + employees);
        
        // Reverse order
        employees.sort(Comparator.comparingDouble((Employee e) -> e.salary).reversed());
        System.out.println("By salary (desc): " + employees);
    }
    
    public static void demonstrateChaining() {
        System.out.println("\n=== Comparator Chaining ===");
        
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "IT", 30, 80000),
            new Employee("Bob", "IT", 30, 70000),
            new Employee("Charlie", "HR", 25, 75000),
            new Employee("David", "IT", 30, 80000)
        );
        
        // Sort by department, then age, then salary, then name
        Comparator<Employee> comparator = Comparator
            .comparing((Employee e) -> e.department)
            .thenComparingInt(e -> e.age)
            .thenComparingDouble(e -> e.salary)
            .thenComparing(e -> e.name);
        
        employees.sort(comparator);
        System.out.println("Multi-level sort:");
        employees.forEach(System.out::println);
    }
    
    public static void demonstrateNullHandling() {
        System.out.println("\n=== Null Handling ===");
        
        List<Employee> employees = new ArrayList<>(Arrays.asList(
            new Employee("Alice", "IT", 30, 80000),
            null,
            new Employee("Bob", "HR", 25, 60000),
            null,
            new Employee("Charlie", null, 35, 90000)
        ));
        
        // Handle null employees - nulls last
        Comparator<Employee> nullSafeEmployee = Comparator.nullsLast(
            Comparator.comparing(e -> e.name)
        );
        
        employees.sort(nullSafeEmployee);
        System.out.println("Nulls last:");
        employees.forEach(System.out::println);
        
        // Handle null fields - department with nulls first
        employees = new ArrayList<>(Arrays.asList(
            new Employee("Alice", "IT", 30, 80000),
            new Employee("Bob", null, 25, 60000),
            new Employee("Charlie", "HR", 35, 90000),
            new Employee("David", null, 28, 70000)
        ));
        
        Comparator<Employee> nullSafeDept = Comparator.comparing(
            (Employee e) -> e.department,
            Comparator.nullsFirst(Comparator.naturalOrder())
        );
        
        employees.sort(nullSafeDept);
        System.out.println("\nNull departments first:");
        employees.forEach(System.out::println);
    }
    
    public static void demonstrateComplexLogic() {
        System.out.println("\n=== Complex Custom Logic ===");
        
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "IT", 30, 80000),
            new Employee("Bob", "HR", 25, 60000),
            new Employee("Charlie", "IT", 35, 90000),
            new Employee("David", "HR", 28, 65000)
        );
        
        // Custom logic: IT department first, then by salary descending
        Comparator<Employee> customComparator = (e1, e2) -> {
            // IT department has priority
            boolean e1IsIT = "IT".equals(e1.department);
            boolean e2IsIT = "IT".equals(e2.department);
            
            if (e1IsIT && !e2IsIT) return -1;
            if (!e1IsIT && e2IsIT) return 1;
            
            // Within same department priority, sort by salary descending
            return Double.compare(e2.salary, e1.salary);
        };
        
        employees.sort(customComparator);
        System.out.println("IT first, then by salary desc:");
        employees.forEach(System.out::println);
        
        // Salary bands: group by salary ranges
        Comparator<Employee> salaryBands = Comparator.comparingInt(e -> {
            if (e.salary < 65000) return 1;      // Low
            if (e.salary < 85000) return 2;      // Medium
            return 3;                             // High
        }).thenComparingDouble(e -> e.salary);
        
        employees.sort(salaryBands);
        System.out.println("\nGrouped by salary bands:");
        employees.forEach(System.out::println);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n log n) for sorting with any comparator

**Space Complexity**: O(1) for comparator objects

## Edge Cases and Pitfalls

- **Null Handling**: Always use nullsFirst() or nullsLast() when nulls are possible
- **Consistency**: Comparator should be consistent with equals for predictable behavior
- **Transitivity**: Ensure a > b and b > c implies a > c
- **Performance**: Complex comparators may slow down sorting; keep logic simple
- **Chaining Order**: Order matters in thenComparing() - primary sort first
- **Type Safety**: Use specific comparingInt/Long/Double for primitives to avoid boxing
- **Reverse**: reversed() creates new comparator; doesn't modify original

## Interview-Ready Answer

"Java 8+ provides Comparator factory methods like comparing() that extract sort keys, and thenComparing() for chaining multiple sort criteria. Use nullsFirst() or nullsLast() for null-safe comparisons. Comparator.comparingInt/Long/Double avoid boxing overhead. You can chain comparators to create multi-level sorting, and reversed() inverts any comparator. For complex logic, implement compare() directly while ensuring transitivity and consistency with equals."
