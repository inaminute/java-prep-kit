# Custom Exceptions

## Problem Statement

Demonstrate how to create and use custom exceptions in Java. Show when to extend Exception vs RuntimeException. Implement custom exceptions with additional context and demonstrate proper exception handling patterns.

**Requirements**:
- Create custom checked and unchecked exceptions
- Add context information to exceptions
- Demonstrate exception chaining
- Show proper usage patterns

## Approach

- Extend Exception for checked exceptions (recoverable conditions)
- Extend RuntimeException for unchecked exceptions (programming errors)
- Add fields for additional context
- Use exception chaining to preserve original cause
- Provide meaningful error messages

## Solution

```java
// Custom checked exception
class InsufficientBalanceException extends Exception {
    private double balance;
    private double amount;
    
    public InsufficientBalanceException(double balance, double amount) {
        super(String.format("Insufficient balance: %.2f, required: %.2f", balance, amount));
        this.balance = balance;
        this.amount = amount;
    }
    
    public double getShortfall() {
        return amount - balance;
    }
}

// Custom unchecked exception
class InvalidAccountException extends RuntimeException {
    private String accountId;
    
    public InvalidAccountException(String accountId) {
        super("Invalid account: " + accountId);
        this.accountId = accountId;
    }
    
    public String getAccountId() {
        return accountId;
    }
}

class BankAccount {
    private String accountId;
    private double balance;
    
    public BankAccount(String accountId, double balance) {
        if (accountId == null || accountId.isEmpty()) {
            throw new InvalidAccountException(accountId);
        }
        this.accountId = accountId;
        this.balance = balance;
    }
    
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > balance) {
            throw new InsufficientBalanceException(balance, amount);
        }
        balance -= amount;
    }
    
    public double getBalance() {
        return balance;
    }
}

public class CustomExceptions {
    
    public static void main(String[] args) {
        demonstrateCheckedException();
        demonstrateUncheckedException();
        demonstrateExceptionChaining();
    }
    
    public static void demonstrateCheckedException() {
        System.out.println("=== Checked Exception ===");
        
        BankAccount account = new BankAccount("ACC001", 100.0);
        try {
            account.withdraw(150.0);
        } catch (InsufficientBalanceException e) {
            System.out.println(e.getMessage());
            System.out.println("Shortfall: " + e.getShortfall());
        }
    }
    
    public static void demonstrateUncheckedException() {
        System.out.println("\n=== Unchecked Exception ===");
        
        try {
            BankAccount account = new BankAccount("", 100.0);
        } catch (InvalidAccountException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void demonstrateExceptionChaining() {
        System.out.println("\n=== Exception Chaining ===");
        
        try {
            processTransaction();
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
        }
    }
    
    private static void processTransaction() throws Exception {
        try {
            throw new InsufficientBalanceException(50, 100);
        } catch (InsufficientBalanceException e) {
            throw new Exception("Transaction failed", e);
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for exception creation

**Space Complexity**: O(n) where n is stack trace depth

## Edge Cases and Pitfalls

- **Overusing custom exceptions**: Only create when standard exceptions don't fit
- **Exception hierarchy**: Design hierarchy carefully for catch blocks
- **Serialization**: Make custom exceptions serializable if needed

## Interview-Ready Answer

"Create custom exceptions by extending Exception (checked) or RuntimeException (unchecked). Add fields for context information and provide meaningful messages. Use exception chaining to preserve original cause. Extend Exception for recoverable conditions requiring explicit handling, RuntimeException for programming errors. Include getters for additional context."
