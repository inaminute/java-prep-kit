package core;

import java.util.*;
import java.util.stream.*;

import entities.Employee;

public class StreamOperations {
    public void initialize() {
        List<Employee> employees = Arrays.asList(
                new Employee("Alice", "Engineering", 90000),
                new Employee("Bob", "Engineering", 85000),
                new Employee("Charlie", "Sales", 70000),
                new Employee("David", "Sales", 75000),
                new Employee("Eve", "HR", 65000));

        demonstrateFilterAndMap(employees);
        demonstrateReduce(employees);
        demonstrateCollectors(employees);
    }

    private void demonstrateFilterAndMap(List<Employee> employees) {
        System.out.println("=== Starting Filter and Map Operations ===");
        // Filter employees with salary > 70000 and get names
        List<String> empNames = employees.stream()
                .filter(e -> e.getSalary() > 70000)
                .map(e -> e.getName())
                .collect(Collectors.toList());

        System.out.println(empNames);

        // Filter engineers and get average salary in double.
        double averageSalary = employees.stream()
                .filter(e -> e.getDepartment().toLowerCase().equals("engineering"))
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);

        System.out.println("Average salary: " + averageSalary);
    }

    private void demonstrateReduce(List<Employee> employees) {
        System.out.println("=== Starting Reduce Operations ===");
        // Total salary of all employees
        double totalSalary = employees.stream()
                .mapToDouble(Employee::getSalary)
                .sum();

        System.out.println("Total Salary: " + totalSalary);

        // Concatenate all employee names
        // System.out.println("All Employee Names: " + allNames);
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

        System.out.println("High salary count: " + partitioned);
    }

}
