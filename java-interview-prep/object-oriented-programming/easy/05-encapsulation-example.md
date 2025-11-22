# Encapsulation Example

## Problem Statement

Demonstrate the principle of encapsulation by creating a `BankAccount` class that protects its internal state and provides controlled access through public methods. The class should prevent direct access to sensitive data (balance) and ensure that all modifications go through validation logic. Show how encapsulation helps maintain data integrity and hides implementation details.

**Requirements:**
- Private fields to hide internal state
- Public getter and setter methods for controlled access
- Validation logic in setter methods
- Business methods that maintain invariants
- Prevent invalid state transitions

## Approach

- Declare all fields as private to hide internal state
- Provide public getter methods to read field values
- Provide public setter methods with validation logic
- Implement business methods (deposit, withdraw) that maintain account rules
- Use meaningful method names that describe operations
- Throw exceptions for invalid operations
- Keep implementation details hidden from clients

## Solution

```java
public class BankAccount {
    // Private fields - encapsulated data
    private String accountNumber;
    private String accountHolder;
    private double balance;
    private boolean isActive;
    
    // Constructor
    public BankAccount(String accountNumber, String accountHolder, double initialBalance) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        if (accountHolder == null || accountHolder.isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be null or empty");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = initialBalance;
        this.isActive = true;
    }
    
    // Getter methods - controlled read access
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public String getAccountHolder() {
        return accountHolder;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    // Setter with validation
    public void setAccountHolder(String accountHolder) {
        if (accountHolder == null || accountHolder.isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be null or empty");
        }
        this.accountHolder = accountHolder;
    }
    
    // Business methods with validation
    public void deposit(double amount) {
        if (!isActive) {
            throw new IllegalStateException("Cannot deposit to inactive account");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        balance += amount;
        System.out.println("Deposited: $" + amount + ", New balance: $" + balance);
    }
    
    public void withdraw(double amount) {
        if (!isActive) {
            throw new IllegalStateException("Cannot withdraw from inactive account");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds. Balance: $" + balance);
        }
        
        balance -= amount;
        System.out.println("Withdrawn: $" + amount + ", New balance: $" + balance);
    }
    
    public void closeAccount() {
        if (!isActive) {
            throw new IllegalStateException("Account is already closed");
        }
        if (balance > 0) {
            throw new IllegalStateException("Cannot close account with positive balance. Please withdraw all funds.");
        }
        
        isActive = false;
        System.out.println("Account closed successfully");
    }
    
    public void displayAccountInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Holder: " + accountHolder);
        System.out.println("Balance: $" + balance);
        System.out.println("Status: " + (isActive ? "Active" : "Closed"));
    }
}

// Demo class
class EncapsulationDemo {
    public static void main(String[] args) {
        // Create account
        BankAccount account = new BankAccount("ACC001", "John Doe", 1000.0);
        
        // Access through public methods only
        account.displayAccountInfo();
        
        // Perform operations
        account.deposit(500.0);
        account.withdraw(200.0);
        
        // Try to access balance directly - won't compile
        // account.balance = 10000.0;  // Compilation error!
        
        // Must use getter
        System.out.println("Current balance: $" + account.getBalance());
        
        // Validation prevents invalid operations
        try {
            account.withdraw(2000.0);  // Insufficient funds
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        try {
            account.deposit(-100.0);  // Invalid amount
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - All getter, setter, and business methods perform constant time operations (field access, arithmetic, validation checks).

**Space Complexity**: O(1) - Each BankAccount object uses a fixed amount of memory for its fields regardless of the number of operations performed.

## Edge Cases and Pitfalls

- **Direct Field Access**: Without encapsulation, clients could set balance to negative values or bypass validation. Private fields prevent this.
- **Validation Bypass**: Always validate in setters and business methods. Don't assume clients will call methods correctly.
- **Immutable Fields**: Some fields like accountNumber should never change after creation. Don't provide setters for such fields.
- **Defensive Copying**: If fields are mutable objects (like Date or List), return copies in getters to prevent external modification.
- **Null Checks**: Always validate that reference type parameters are not null before assignment.
- **State Consistency**: Business methods should maintain object invariants (e.g., balance never negative, can't operate on closed account).

## Interview-Ready Answer

"Encapsulation bundles data and methods that operate on that data within a class, hiding internal state through private fields and exposing controlled access via public methods. I'd create a BankAccount class with private fields for balance and account details, providing getters for read access and business methods like deposit() and withdraw() with validation logic. This prevents invalid state and protects data integrity. All operations are O(1) time and space."
