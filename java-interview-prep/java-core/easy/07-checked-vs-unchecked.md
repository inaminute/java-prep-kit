# Checked vs Unchecked Exceptions

## Problem Statement

Explain the difference between checked and unchecked exceptions in Java. When should you use each? Demonstrate examples of both types and explain the compiler's role in exception handling.

**Requirements**:
- Define checked and unchecked exceptions
- Show examples of each type
- Explain when to use each
- Demonstrate compiler enforcement

## Approach

- Checked exceptions extend Exception (except RuntimeException) and must be declared or caught
- Unchecked exceptions extend RuntimeException and don't require explicit handling
- Use checked exceptions for recoverable conditions
- Use unchecked exceptions for programming errors
- Compiler enforces handling of checked exceptions

## Solution

```java
import java.io.*;

public class CheckedVsUnchecked {
    
    // Checked exception example - must be declared or caught
    public static void readFile(String filename) throws IOException {
        FileReader reader = new FileReader(filename);
        reader.close();
    }
    
    // Unchecked exception example - no declaration required
    public static int divide(int a, int b) {
        return a / b; // May throw ArithmeticException
    }
    
    public static void main(String[] args) {
        demonstrateCheckedExceptions();
        demonstrateUncheckedExceptions();
        demonstrateCustomExceptions();
    }
    
    public static void demonstrateCheckedExceptions() {
        System.out.println("=== Checked Exceptions ===");
        
        // Must handle checked exceptions
        try {
            readFile("test.txt");
        } catch (IOException e) {
            System.out.println("Caught checked exception: " + e.getMessage());
        }
        
        // Common checked exceptions
        try {
            Class.forName("NonExistentClass");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        }
    }
    
    public static void demonstrateUncheckedExceptions() {
        System.out.println("\n=== Unchecked Exceptions ===");
        
        // Unchecked exceptions - optional to handle
        try {
            int result = divide(10, 0);
        } catch (ArithmeticException e) {
            System.out.println("Caught unchecked exception: " + e.getMessage());
        }
        
        // Common unchecked exceptions
        try {
            String str = null;
            str.length(); // NullPointerException
        } catch (NullPointerException e) {
            System.out.println("Null pointer exception");
        }
        
        try {
            int[] arr = new int[5];
            arr[10] = 1; // ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index out of bounds");
        }
    }
    
    // Custom checked exception
    static class InvalidAgeException extends Exception {
        public InvalidAgeException(String message) {
            super(message);
        }
    }
    
    // Custom unchecked exception
    static class InvalidConfigurationException extends RuntimeException {
        public InvalidConfigurationException(String message) {
            super(message);
        }
    }
    
    public static void validateAge(int age) throws InvalidAgeException {
        if (age < 0 || age > 150) {
            throw new InvalidAgeException("Invalid age: " + age);
        }
    }
    
    public static void validateConfig(String config) {
        if (config == null || config.isEmpty()) {
            throw new InvalidConfigurationException("Configuration cannot be empty");
        }
    }
    
    public static void demonstrateCustomExceptions() {
        System.out.println("\n=== Custom Exceptions ===");
        
        // Custom checked exception - must handle
        try {
            validateAge(200);
        } catch (InvalidAgeException e) {
            System.out.println("Checked: " + e.getMessage());
        }
        
        // Custom unchecked exception - optional to handle
        try {
            validateConfig(null);
        } catch (InvalidConfigurationException e) {
            System.out.println("Unchecked: " + e.getMessage());
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for exception creation and throwing

**Space Complexity**: O(n) where n is the stack trace depth

## Edge Cases and Pitfalls

- **Overusing checked exceptions**: Can lead to verbose code with many try-catch blocks
- **Swallowing exceptions**: Catching exceptions without proper handling or logging
- **Checked exception in interfaces**: Makes all implementations handle the exception
- **RuntimeException misuse**: Don't use for recoverable conditions

## Interview-Ready Answer

"Checked exceptions extend Exception and must be declared or caught at compile time, used for recoverable conditions like IOException. Unchecked exceptions extend RuntimeException, don't require explicit handling, and represent programming errors like NullPointerException. Use checked for recoverable conditions and unchecked for programming bugs. The compiler enforces handling of checked exceptions."
