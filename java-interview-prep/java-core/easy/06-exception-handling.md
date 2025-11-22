# Exception Handling

## Problem Statement

Explain exception handling in Java. Demonstrate proper use of try-catch-finally blocks, exception propagation, and best practices for handling exceptions. Show the difference between checked and unchecked exceptions.

**Requirements**:
- Demonstrate try-catch-finally usage
- Show exception propagation
- Explain when to catch vs throw exceptions
- Demonstrate custom exception creation

## Approach

- Use try-catch to handle exceptions where you can recover
- Use finally for cleanup code that must execute regardless of exceptions
- Throw exceptions when the current method cannot handle the error
- Catch specific exceptions before general ones
- Create custom exceptions for domain-specific errors

## Solution

```java
import java.io.*;

public class ExceptionHandling {
    
    public static void main(String[] args) {
        demonstrateTryCatchFinally();
        demonstrateExceptionPropagation();
        demonstrateCustomException();
    }
    
    public static void demonstrateTryCatchFinally() {
        System.out.println("=== Try-Catch-Finally ===");
        
        FileReader reader = null;
        try {
            reader = new FileReader("nonexistent.txt");
            // Read file
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } finally {
            System.out.println("Finally block always executes");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Error closing file");
                }
            }
        }
    }
    
    public static void demonstrateExceptionPropagation() {
        System.out.println("\n=== Exception Propagation ===");
        
        try {
            method1();
        } catch (Exception e) {
            System.out.println("Caught in main: " + e.getMessage());
        }
    }
    
    private static void method1() throws Exception {
        method2();
    }
    
    private static void method2() throws Exception {
        method3();
    }
    
    private static void method3() throws Exception {
        throw new Exception("Exception from method3");
    }
    
    // Custom exception
    static class InsufficientFundsException extends Exception {
        private double amount;
        
        public InsufficientFundsException(double amount) {
            super("Insufficient funds: " + amount);
            this.amount = amount;
        }
        
        public double getAmount() {
            return amount;
        }
    }
    
    static class BankAccount {
        private double balance;
        
        public BankAccount(double balance) {
            this.balance = balance;
        }
        
        public void withdraw(double amount) throws InsufficientFundsException {
            if (amount > balance) {
                throw new InsufficientFundsException(amount - balance);
            }
            balance -= amount;
        }
        
        public double getBalance() {
            return balance;
        }
    }
    
    public static void demonstrateCustomException() {
        System.out.println("\n=== Custom Exception ===");
        
        BankAccount account = new BankAccount(100);
        try {
            account.withdraw(150);
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
            System.out.println("Short by: " + e.getAmount());
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for exception throwing and catching (though stack unwinding has overhead)

**Space Complexity**: O(n) where n is the call stack depth for exception propagation

## Edge Cases and Pitfalls

- **Catching Exception**: Avoid catching generic Exception unless necessary; catch specific exceptions
- **Empty catch blocks**: Never use empty catch blocks; at minimum log the exception
- **Finally and return**: If finally block has a return statement, it overrides try/catch return values
- **Resource leaks**: Always close resources in finally or use try-with-resources

## Interview-Ready Answer

"Exception handling uses try-catch-finally blocks. Try contains code that might throw exceptions, catch handles specific exceptions, and finally executes cleanup code regardless of exceptions. Throw exceptions when you can't handle them locally. Use custom exceptions for domain-specific errors. Always catch specific exceptions before general ones and avoid empty catch blocks."
