# SOLID - Interface Segregation Principle (ISP)

## Problem Statement

Demonstrate the Interface Segregation Principle by showing how clients should not be forced to depend on interfaces they don't use. Refactor a fat interface into smaller, more focused interfaces.

**Requirements:**
- Show a fat interface with many methods
- Demonstrate classes forced to implement unused methods
- Refactor into smaller, focused interfaces
- Show how ISP improves flexibility and reduces coupling
- Demonstrate role-based interface design

## Approach

- Create a large interface with many unrelated methods
- Show implementations that throw UnsupportedOperationException
- Identify cohesive groups of methods
- Split into multiple smaller interfaces
- Classes implement only the interfaces they need
- Use interface composition when multiple capabilities are needed

## Solution

```java
// ========== BEFORE: Violating ISP ==========

// Fat interface with too many methods
interface Worker {
    void work();
    void eat();
    void sleep();
    void attendMeeting();
    void writeCode();
    void designSystem();
    void testSoftware();
}

// Human worker can implement all methods
class HumanWorker implements Worker {
    @Override
    public void work() {
        System.out.println("Human is working");
    }
    
    @Override
    public void eat() {
        System.out.println("Human is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Human is sleeping");
    }
    
    @Override
    public void attendMeeting() {
        System.out.println("Human is attending meeting");
    }
    
    @Override
    public void writeCode() {
        System.out.println("Human is writing code");
    }
    
    @Override
    public void designSystem() {
        System.out.println("Human is designing system");
    }
    
    @Override
    public void testSoftware() {
        System.out.println("Human is testing software");
    }
}

// Robot worker forced to implement methods it doesn't need!
class RobotWorker implements Worker {
    @Override
    public void work() {
        System.out.println("Robot is working");
    }
    
    @Override
    public void eat() {
        throw new UnsupportedOperationException("Robots don't eat!");
    }
    
    @Override
    public void sleep() {
        throw new UnsupportedOperationException("Robots don't sleep!");
    }
    
    @Override
    public void attendMeeting() {
        throw new UnsupportedOperationException("Robots don't attend meetings!");
    }
    
    @Override
    public void writeCode() {
        System.out.println("Robot is writing code");
    }
    
    @Override
    public void designSystem() {
        throw new UnsupportedOperationException("Robots don't design!");
    }
    
    @Override
    public void testSoftware() {
        System.out.println("Robot is testing software");
    }
}

// ========== AFTER: Following ISP ==========

// Segregated interfaces - small and focused
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

interface Sleepable {
    void sleep();
}

interface Attendable {
    void attendMeeting();
}

interface Codeable {
    void writeCode();
}

interface Designable {
    void designSystem();
}

interface Testable {
    void testSoftware();
}

// Human implements all relevant interfaces
class HumanEmployee implements Workable, Eatable, Sleepable, Attendable, Codeable, Designable, Testable {
    private String name;
    
    public HumanEmployee(String name) {
        this.name = name;
    }
    
    @Override
    public void work() {
        System.out.println(name + " is working");
    }
    
    @Override
    public void eat() {
        System.out.println(name + " is eating lunch");
    }
    
    @Override
    public void sleep() {
        System.out.println(name + " is sleeping");
    }
    
    @Override
    public void attendMeeting() {
        System.out.println(name + " is attending meeting");
    }
    
    @Override
    public void writeCode() {
        System.out.println(name + " is writing code");
    }
    
    @Override
    public void designSystem() {
        System.out.println(name + " is designing system");
    }
    
    @Override
    public void testSoftware() {
        System.out.println(name + " is testing software");
    }
}

// Robot only implements what it can do
class RobotEmployee implements Workable, Codeable, Testable {
    private String id;
    
    public RobotEmployee(String id) {
        this.id = id;
    }
    
    @Override
    public void work() {
        System.out.println("Robot " + id + " is working 24/7");
    }
    
    @Override
    public void writeCode() {
        System.out.println("Robot " + id + " is generating code");
    }
    
    @Override
    public void testSoftware() {
        System.out.println("Robot " + id + " is running automated tests");
    }
}

// Manager implements different set of interfaces
class Manager implements Workable, Eatable, Sleepable, Attendable, Designable {
    private String name;
    
    public Manager(String name) {
        this.name = name;
    }
    
    @Override
    public void work() {
        System.out.println(name + " is managing team");
    }
    
    @Override
    public void eat() {
        System.out.println(name + " is having business lunch");
    }
    
    @Override
    public void sleep() {
        System.out.println(name + " is resting");
    }
    
    @Override
    public void attendMeeting() {
        System.out.println(name + " is leading meeting");
    }
    
    @Override
    public void designSystem() {
        System.out.println(name + " is creating architecture");
    }
}

// Intern implements subset of interfaces
class Intern implements Workable, Eatable, Sleepable, Codeable, Testable {
    private String name;
    
    public Intern(String name) {
        this.name = name;
    }
    
    @Override
    public void work() {
        System.out.println(name + " (intern) is learning and working");
    }
    
    @Override
    public void eat() {
        System.out.println(name + " is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println(name + " is sleeping");
    }
    
    @Override
    public void writeCode() {
        System.out.println(name + " is writing simple code");
    }
    
    @Override
    public void testSoftware() {
        System.out.println(name + " is testing features");
    }
}

// Demo class
class ISPDemo {
    public static void main(String[] args) {
        System.out.println("=== BEFORE: Violating ISP ===\n");
        
        Worker human = new HumanWorker();
        Worker robot = new RobotWorker();
        
        human.work();
        human.eat();
        
        robot.work();
        try {
            robot.eat();  // Throws exception!
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== AFTER: Following ISP ===\n");
        
        HumanEmployee developer = new HumanEmployee("Alice");
        RobotEmployee bot = new RobotEmployee("BOT-001");
        Manager manager = new Manager("Bob");
        Intern intern = new Intern("Charlie");
        
        // Each worker does what they can
        System.out.println("Developer:");
        developer.work();
        developer.writeCode();
        developer.attendMeeting();
        
        System.out.println("\nRobot:");
        bot.work();
        bot.writeCode();
        bot.testSoftware();
        // bot.eat();  // Doesn't compile - robot doesn't implement Eatable!
        
        System.out.println("\nManager:");
        manager.work();
        manager.attendMeeting();
        manager.designSystem();
        
        System.out.println("\nIntern:");
        intern.work();
        intern.writeCode();
        intern.testSoftware();
        
        // Polymorphic usage with specific interfaces
        System.out.println("\n=== Polymorphic Usage ===");
        Codeable[] coders = {developer, bot, intern};
        System.out.println("All coders writing code:");
        for (Codeable coder : coders) {
            coder.writeCode();
        }
        
        System.out.println("\n=== Benefits of ISP ===");
        System.out.println("- Classes only implement methods they actually use");
        System.out.println("- No UnsupportedOperationException needed");
        System.out.println("- More flexible and maintainable");
        System.out.println("- Easier to understand class capabilities");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - All method calls are constant time operations.

**Space Complexity**: O(1) - Each object uses constant space regardless of how many interfaces it implements.

## Edge Cases and Pitfalls

- **Too Many Interfaces**: Don't create an interface for every single method. Group related methods that change together.
- **Interface Pollution**: Having too many tiny interfaces can make code hard to navigate. Balance granularity with usability.
- **Default Methods**: Java 8+ default methods can help add functionality to interfaces without breaking implementations, but use sparingly.
- **Role Interfaces**: Design interfaces around roles or capabilities, not around classes.
- **Client-Specific Interfaces**: Create interfaces based on how clients use them, not based on implementation details.
- **Marker Interfaces**: Empty interfaces (like Serializable) are acceptable for marking capabilities.

## Interview-Ready Answer

"The Interface Segregation Principle states that clients should not be forced to depend on interfaces they don't use. I'd show a fat Worker interface with many methods where RobotWorker must throw UnsupportedOperationException for methods like eat() and sleep(). The fix is to split into smaller interfaces like Workable, Eatable, Codeable, and Testable. Each class implements only the interfaces it needs. HumanEmployee implements all, RobotEmployee implements only Workable, Codeable, and Testable. This eliminates forced dependencies and improves flexibility. Time and space are O(1)."
