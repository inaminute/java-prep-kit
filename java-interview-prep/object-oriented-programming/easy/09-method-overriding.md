# Method Overriding

## Problem Statement

Demonstrate method overriding in Java by creating an employee hierarchy where subclasses override methods from the parent class to provide specialized behavior. Show the rules for valid method overriding, the use of @Override annotation, and how to call parent class methods using the super keyword.

**Requirements:**
- Create a parent class with methods to override
- Implement subclasses that override parent methods
- Use @Override annotation
- Demonstrate super keyword usage
- Show runtime polymorphism through overriding
- Explain overriding rules and restrictions

## Approach

- Create a base Employee class with methods
- Define subclasses that override specific methods
- Use @Override annotation for compile-time checking
- Call parent implementation using super.method()
- Demonstrate that overriding is resolved at runtime
- Show covariant return types
- Explain access modifier rules for overriding

## Solution

```java
// Base class
class Employee {
    protected String name;
    protected String id;
    protected double baseSalary;
    
    public Employee(String name, String id, double baseSalary) {
        this.name = name;
        this.id = id;
        this.baseSalary = baseSalary;
    }
    
    // Method to be overridden
    public double calculateSalary() {
        return baseSalary;
    }
    
    // Method to be overridden
    public String getRole() {
        return "Employee";
    }
    
    // Method to be overridden
    public void displayInfo() {
        System.out.println("Name: " + name);
        System.out.println("ID: " + id);
        System.out.println("Role: " + getRole());
        System.out.println("Salary: $" + calculateSalary());
    }
    
    // Final method - cannot be overridden
    public final String getEmployeeId() {
        return id;
    }
}

// Manager subclass
class Manager extends Employee {
    private double bonus;
    private int teamSize;
    
    public Manager(String name, String id, double baseSalary, double bonus, int teamSize) {
        super(name, id, baseSalary);
        this.bonus = bonus;
        this.teamSize = teamSize;
    }
    
    // Override with same return type
    @Override
    public double calculateSalary() {
        return baseSalary + bonus;
    }
    
    @Override
    public String getRole() {
        return "Manager";
    }
    
    // Override and extend parent behavior
    @Override
    public void displayInfo() {
        super.displayInfo();  // Call parent implementation
        System.out.println("Bonus: $" + bonus);
        System.out.println("Team Size: " + teamSize);
    }
    
    // Additional method specific to Manager
    public void conductMeeting() {
        System.out.println(name + " is conducting a team meeting");
    }
}

// Developer subclass
class Developer extends Employee {
    private String programmingLanguage;
    private int projectCount;
    
    public Developer(String name, String id, double baseSalary, String language, int projectCount) {
        super(name, id, baseSalary);
        this.programmingLanguage = language;
        this.projectCount = projectCount;
    }
    
    @Override
    public double calculateSalary() {
        // Salary increases with project count
        return baseSalary + (projectCount * 1000);
    }
    
    @Override
    public String getRole() {
        return "Developer (" + programmingLanguage + ")";
    }
    
    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Language: " + programmingLanguage);
        System.out.println("Projects: " + projectCount);
    }
    
    public void writeCode() {
        System.out.println(name + " is writing " + programmingLanguage + " code");
    }
}

// Intern subclass with covariant return type example
class Intern extends Employee {
    private String university;
    private int duration;  // in months
    
    public Intern(String name, String id, double baseSalary, String university, int duration) {
        super(name, id, baseSalary);
        this.university = university;
        this.duration = duration;
    }
    
    @Override
    public double calculateSalary() {
        // Interns get reduced salary
        return baseSalary * 0.5;
    }
    
    @Override
    public String getRole() {
        return "Intern";
    }
    
    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("University: " + university);
        System.out.println("Duration: " + duration + " months");
    }
}

// Demo class
class MethodOverridingDemo {
    public static void main(String[] args) {
        // Create different employee types
        Employee emp = new Employee("John Doe", "E001", 50000);
        Manager mgr = new Manager("Jane Smith", "M001", 80000, 20000, 5);
        Developer dev = new Developer("Bob Johnson", "D001", 70000, "Java", 3);
        Intern intern = new Intern("Alice Brown", "I001", 30000, "MIT", 6);
        
        // Display information
        System.out.println("=== Employee ===");
        emp.displayInfo();
        
        System.out.println("\n=== Manager ===");
        mgr.displayInfo();
        mgr.conductMeeting();
        
        System.out.println("\n=== Developer ===");
        dev.displayInfo();
        dev.writeCode();
        
        System.out.println("\n=== Intern ===");
        intern.displayInfo();
        
        // Runtime polymorphism - method resolved at runtime
        System.out.println("\n=== Polymorphic Behavior ===");
        Employee[] employees = {emp, mgr, dev, intern};
        
        for (Employee e : employees) {
            System.out.println(e.getRole() + ": $" + e.calculateSalary());
        }
        
        // Demonstrate that overriding is runtime behavior
        Employee polymorphicRef = new Manager("Tom Wilson", "M002", 90000, 25000, 8);
        System.out.println("\nPolymorphic reference:");
        System.out.println("Role: " + polymorphicRef.getRole());  // Calls Manager's getRole()
        System.out.println("Salary: $" + polymorphicRef.calculateSalary());  // Calls Manager's calculateSalary()
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Method overriding resolution happens at runtime through virtual method tables (vtables), but the lookup is constant time.

**Space Complexity**: O(1) - Each object stores its own fields. The vtable pointer adds negligible overhead.

## Edge Cases and Pitfalls

- **@Override Annotation**: Always use @Override to catch errors at compile time. Without it, typos create new methods instead of overriding.
- **Access Modifiers**: Overriding method cannot have more restrictive access than parent method. Can be same or less restrictive (protected → public is OK, public → protected is not).
- **Return Type**: Must be same or covariant (subtype). Cannot change to unrelated type.
- **Static Methods**: Static methods cannot be overridden, only hidden. They're resolved at compile time based on reference type.
- **Final Methods**: Methods marked final cannot be overridden. Use final to prevent subclasses from changing critical behavior.
- **Private Methods**: Private methods are not inherited, so they cannot be overridden.
- **Constructor Calls**: Constructors are not inherited or overridden. Use super() to call parent constructor.

## Interview-Ready Answer

"Method overriding allows subclasses to provide specific implementations of methods defined in the parent class. I'd create an Employee base class with calculateSalary() and getRole() methods, then override them in Manager, Developer, and Intern subclasses with specialized behavior. Use @Override annotation for compile-time checking and super.method() to call parent implementation. Overriding enables runtime polymorphism where the actual object type determines which method executes. Time and space complexity are O(1)."
