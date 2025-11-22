# Optional Class

## Problem Statement

Explain the Optional class in Java 8+. Demonstrate how it helps avoid NullPointerException and promotes better null handling. Show proper usage patterns and anti-patterns.

**Requirements**:
- Demonstrate Optional creation and usage
- Show methods like map, flatMap, orElse, orElseGet
- Explain when to use Optional
- Show anti-patterns to avoid

## Approach

- Optional is a container that may or may not contain a value
- Use to represent absence of value instead of null
- Provides functional methods for handling presence/absence
- Never use Optional as method parameter or field
- Use for return types where value might be absent

## Solution

```java
import java.util.*;

public class OptionalClass {
    
    static class User {
        private String name;
        private Optional<String> email;
        
        public User(String name, String email) {
            this.name = name;
            this.email = Optional.ofNullable(email);
        }
        
        public String getName() { return name; }
        public Optional<String> getEmail() { return email; }
    }
    
    public static void main(String[] args) {
        demonstrateCreation();
        demonstrateOperations();
        demonstrateAntiPatterns();
    }
    
    public static void demonstrateCreation() {
        System.out.println("=== Optional Creation ===");
        
        Optional<String> empty = Optional.empty();
        Optional<String> nonEmpty = Optional.of("value");
        Optional<String> nullable = Optional.ofNullable(null);
        
        System.out.println("Empty present: " + empty.isPresent());
        System.out.println("NonEmpty present: " + nonEmpty.isPresent());
    }
    
    public static void demonstrateOperations() {
        System.out.println("\n=== Optional Operations ===");
        
        User user1 = new User("Alice", "alice@example.com");
        User user2 = new User("Bob", null);
        
        // orElse
        String email1 = user1.getEmail().orElse("no-email");
        String email2 = user2.getEmail().orElse("no-email");
        System.out.println("Email1: " + email1);
        System.out.println("Email2: " + email2);
        
        // map
        Optional<Integer> emailLength = user1.getEmail().map(String::length);
        System.out.println("Email length: " + emailLength.orElse(0));
        
        // ifPresent
        user1.getEmail().ifPresent(e -> System.out.println("Has email: " + e));
    }
    
    public static void demonstrateAntiPatterns() {
        System.out.println("\n=== Anti-Patterns ===");
        
        Optional<String> opt = Optional.of("value");
        
        // BAD: Using get() without checking
        // String value = opt.get(); // May throw NoSuchElementException
        
        // GOOD: Use orElse or ifPresent
        String value = opt.orElse("default");
        System.out.println("Value: " + value);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for all Optional operations

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Never use Optional.get()**: Without checking, may throw NoSuchElementException
- **Don't use as field**: Optional is not serializable, adds overhead
- **Don't use as parameter**: Makes API more complex
- **orElse vs orElseGet**: orElse always evaluates, orElseGet is lazy

## Interview-Ready Answer

"Optional is a container for values that may be absent, helping avoid NullPointerException. Create with of(), ofNullable(), or empty(). Use methods like map, flatMap, orElse, and ifPresent for functional handling. Use Optional for return types where value might be absent, but never as fields or parameters. Prefer orElseGet over orElse for expensive operations."
