# Custom Annotations

## Problem Statement

Create custom annotations in Java and demonstrate how to process them at runtime using reflection. Show different retention policies, target types, and how to build annotation-based frameworks.

**Requirements**:
- Create custom annotations with different retention policies
- Demonstrate @Target and @Retention
- Process annotations at runtime
- Show practical use cases

## Approach

- Annotations are metadata added to code
- @Retention defines when annotation is available (SOURCE, CLASS, RUNTIME)
- @Target specifies where annotation can be applied
- Use reflection to read RUNTIME annotations
- Common for frameworks, validation, configuration

## Solution

```java
import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotNull {
    String message() default "Field cannot be null";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Range {
    int min() default 0;
    int max() default 100;
    String message() default "Value out of range";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    String description() default "";
}

class User {
    @NotNull(message = "Name is required")
    private String name;
    
    @Range(min = 18, max = 100, message = "Age must be between 18 and 100")
    private Integer age;
    
    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public Integer getAge() { return age; }
}

public class CustomAnnotations {
    
    public static void main(String[] args) throws Exception {
        demonstrateValidation();
        demonstrateTestAnnotation();
    }
    
    public static void demonstrateValidation() throws Exception {
        System.out.println("=== Validation with Annotations ===");
        
        User validUser = new User("Alice", 25);
        User invalidUser1 = new User(null, 25);
        User invalidUser2 = new User("Bob", 150);
        
        System.out.println("Valid user: " + validate(validUser));
        System.out.println("Invalid user 1: " + validate(invalidUser1));
        System.out.println("Invalid user 2: " + validate(invalidUser2));
    }
    
    public static String validate(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);
            
            // Check @NotNull
            if (field.isAnnotationPresent(NotNull.class)) {
                if (value == null) {
                    NotNull annotation = field.getAnnotation(NotNull.class);
                    return annotation.message();
                }
            }
            
            // Check @Range
            if (field.isAnnotationPresent(Range.class)) {
                Range annotation = field.getAnnotation(Range.class);
                if (value instanceof Integer) {
                    int intValue = (Integer) value;
                    if (intValue < annotation.min() || intValue > annotation.max()) {
                        return annotation.message();
                    }
                }
            }
        }
        
        return "Valid";
    }
    
    @Test(description = "Test addition")
    public static void testAddition() {
        System.out.println("Running test: addition");
        assert 2 + 2 == 4;
    }
    
    @Test(description = "Test subtraction")
    public static void testSubtraction() {
        System.out.println("Running test: subtraction");
        assert 5 - 3 == 2;
    }
    
    public static void demonstrateTestAnnotation() {
        System.out.println("\n=== Test Annotation ===");
        
        for (Method method : CustomAnnotations.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                Test test = method.getAnnotation(Test.class);
                System.out.println("Found test: " + test.description());
                try {
                    method.invoke(null);
                } catch (Exception e) {
                    System.out.println("Test failed: " + e.getMessage());
                }
            }
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) where n is number of fields/methods to process

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Retention policy**: SOURCE annotations not available at runtime
- **Performance**: Reflection-based processing has overhead
- **Inheritance**: Annotations are not inherited by default

## Interview-Ready Answer

"Custom annotations are created using @interface. @Retention defines availability (SOURCE, CLASS, RUNTIME). @Target specifies where annotation applies. Process RUNTIME annotations using reflection with isAnnotationPresent() and getAnnotation(). Common uses include validation, testing frameworks, dependency injection, and ORM mapping. Annotations provide declarative metadata without cluttering code logic."
