# SOLID - Single Responsibility Principle (SRP)

## Problem Statement

Demonstrate the Single Responsibility Principle by refactoring a poorly designed Employee class that violates SRP into a well-designed system where each class has only one reason to change. Show the problems with violating SRP and the benefits of adhering to it.

**Requirements:**
- Show a class that violates SRP with multiple responsibilities
- Identify the different responsibilities in the violating class
- Refactor into separate classes, each with a single responsibility
- Demonstrate how SRP makes code more maintainable and testable
- Show how changes to one responsibility don't affect others

## Approach

- Create an Employee class that handles multiple concerns (data, persistence, reporting, validation)
- Identify each distinct responsibility
- Extract each responsibility into its own class
- Use composition to combine the separated concerns
- Show how changes now affect only one class
- Demonstrate improved testability and maintainability

## Solution

```java
// ========== BEFORE: Violating SRP ==========

class EmployeeBad {
    private String name;
    private String id;
    private double salary;
    
    public EmployeeBad(String name, String id, double salary) {
        this.name = name;
        this.id = id;
        this.salary = salary;
    }
    
    // Responsibility 1: Data management
    public String getName() { return name; }
    public String getId() { return id; }
    public double getSalary() { return salary; }
    
    // Responsibility 2: Salary calculation
    public double calculateTax() {
        return salary * 0.25;
    }
    
    public double calculateBonus() {
        return salary * 0.10;
    }
    
    // Responsibility 3: Database operations
    public void saveToDatabase() {
        System.out.println("Saving employee " + name + " to database...");
        // Database logic here
    }
    
    public void deleteFromDatabase() {
        System.out.println("Deleting employee " + name + " from database...");
        // Database logic here
    }
    
    // Responsibility 4: Reporting
    public void generateReport() {
        System.out.println("=== Employee Report ===");
        System.out.println("Name: " + name);
        System.out.println("ID: " + id);
        System.out.println("Salary: $" + salary);
        System.out.println("Tax: $" + calculateTax());
        System.out.println("Bonus: $" + calculateBonus());
    }
    
    // Responsibility 5: Validation
    public boolean isValidEmployee() {
        return name != null && !name.isEmpty() && 
               id != null && !id.isEmpty() && 
               salary > 0;
    }
}

// ========== AFTER: Following SRP ==========

// Responsibility 1: Data management only
class Employee {
    private final String name;
    private final String id;
    private final double salary;
    
    public Employee(String name, String id, double salary) {
        this.name = name;
        this.id = id;
        this.salary = salary;
    }
    
    public String getName() { return name; }
    public String getId() { return id; }
    public double getSalary() { return salary; }
}

// Responsibility 2: Salary calculations
class SalaryCalculator {
    private static final double TAX_RATE = 0.25;
    private static final double BONUS_RATE = 0.10;
    
    public double calculateTax(Employee employee) {
        return employee.getSalary() * TAX_RATE;
    }
    
    public double calculateBonus(Employee employee) {
        return employee.getSalary() * BONUS_RATE;
    }
    
    public double calculateNetSalary(Employee employee) {
        return employee.getSalary() - calculateTax(employee) + calculateBonus(employee);
    }
}

// Responsibility 3: Database operations
class EmployeeRepository {
    public void save(Employee employee) {
        System.out.println("Saving employee " + employee.getName() + " to database...");
        // Actual database logic would go here
        System.out.println("Employee saved successfully");
    }
    
    public void delete(Employee employee) {
        System.out.println("Deleting employee " + employee.getName() + " from database...");
        // Actual database logic would go here
        System.out.println("Employee deleted successfully");
    }
    
    public Employee findById(String id) {
        System.out.println("Finding employee with ID: " + id);
        // Database query logic here
        return null;  // Placeholder
    }
}

// Responsibility 4: Reporting
class EmployeeReportGenerator {
    private SalaryCalculator calculator;
    
    public EmployeeReportGenerator(SalaryCalculator calculator) {
        this.calculator = calculator;
    }
    
    public void generateReport(Employee employee) {
        System.out.println("=== Employee Report ===");
        System.out.println("Name: " + employee.getName());
        System.out.println("ID: " + employee.getId());
        System.out.println("Salary: $" + employee.getSalary());
        System.out.println("Tax: $" + calculator.calculateTax(employee));
        System.out.println("Bonus: $" + calculator.calculateBonus(employee));
        System.out.println("Net Salary: $" + calculator.calculateNetSalary(employee));
    }
    
    public void generateSummaryReport(Employee employee) {
        System.out.println(employee.getName() + " (ID: " + employee.getId() + 
                         ") - Net: $" + calculator.calculateNetSalary(employee));
    }
}

// Responsibility 5: Validation
class EmployeeValidator {
    public boolean isValid(Employee employee) {
        return employee != null &&
               isValidName(employee.getName()) &&
               isValidId(employee.getId()) &&
               isValidSalary(employee.getSalary());
    }
    
    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }
    
    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && id.matches("[A-Z0-9]+");
    }
    
    private boolean isValidSalary(double salary) {
        return salary > 0 && salary < 1000000;  // Reasonable range
    }
    
    public String getValidationErrors(Employee employee) {
        StringBuilder errors = new StringBuilder();
        if (!isValidName(employee.getName())) {
            errors.append("Invalid name. ");
        }
        if (!isValidId(employee.getId())) {
            errors.append("Invalid ID format. ");
        }
        if (!isValidSalary(employee.getSalary())) {
            errors.append("Invalid salary range. ");
        }
        return errors.toString();
    }
}

// Demo class
class SRPDemo {
    public static void main(String[] args) {
        System.out.println("=== BEFORE: Violating SRP ===\n");
        EmployeeBad badEmployee = new EmployeeBad("John Doe", "E001", 75000);
        badEmployee.generateReport();
        badEmployee.saveToDatabase();
        System.out.println("Valid: " + badEmployee.isValidEmployee());
        
        System.out.println("\n=== AFTER: Following SRP ===\n");
        
        // Create employee (data only)
        Employee employee = new Employee("Jane Smith", "E002", 85000);
        
        // Use separate classes for different responsibilities
        SalaryCalculator calculator = new SalaryCalculator();
        EmployeeRepository repository = new EmployeeRepository();
        EmployeeReportGenerator reportGenerator = new EmployeeReportGenerator(calculator);
        EmployeeValidator validator = new EmployeeValidator();
        
        // Validate
        if (validator.isValid(employee)) {
            System.out.println("Employee is valid\n");
            
            // Generate report
            reportGenerator.generateReport(employee);
            
            // Save to database
            System.out.println();
            repository.save(employee);
            
            // Calculate salary components
            System.out.println("\nSalary Details:");
            System.out.println("Tax: $" + calculator.calculateTax(employee));
            System.out.println("Bonus: $" + calculator.calculateBonus(employee));
            System.out.println("Net: $" + calculator.calculateNetSalary(employee));
        } else {
            System.out.println("Validation errors: " + validator.getValidationErrors(employee));
        }
        
        // Demonstrate benefit: Easy to change tax calculation without affecting other code
        System.out.println("\n=== Benefits of SRP ===");
        System.out.println("- Each class has one reason to change");
        System.out.println("- Easy to test each responsibility independently");
        System.out.println("- Changes to reporting don't affect database logic");
        System.out.println("- Can replace SalaryCalculator without touching Employee class");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - All operations (validation, calculation, reporting) are constant time for a single employee.

**Space Complexity**: O(1) - Each class uses constant space. The separation doesn't add significant memory overhead.

## Edge Cases and Pitfalls

- **Over-Separation**: Don't create a class for every single method. Group related methods that change for the same reason.
- **God Objects**: Classes with too many responsibilities are hard to maintain and test. If a class has many unrelated methods, it likely violates SRP.
- **Identifying Responsibilities**: Ask "What is the reason this class would change?" If there are multiple answers, split the class.
- **Cohesion**: Methods in a class should be highly cohesive (work together toward a single purpose). Low cohesion indicates SRP violation.
- **Dependencies**: After refactoring, manage dependencies carefully. Use dependency injection to avoid tight coupling.
- **Testing**: SRP makes unit testing easier since each class has a focused purpose. If a class is hard to test, it may have too many responsibilities.

## Interview-Ready Answer

"The Single Responsibility Principle states that a class should have only one reason to change. I'd show an Employee class that violates SRP by handling data, calculations, database operations, reporting, and validation. Then I'd refactor into separate classes: Employee for data, SalaryCalculator for calculations, EmployeeRepository for persistence, EmployeeReportGenerator for reporting, and EmployeeValidator for validation. This makes each class easier to understand, test, and modify independently. Time and space complexity remain O(1)."
