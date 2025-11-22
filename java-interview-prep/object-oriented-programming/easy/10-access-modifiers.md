# Access Modifiers

## Problem Statement

Demonstrate the four access modifiers in Java (private, default/package-private, protected, public) by creating classes that show the visibility and accessibility rules for each modifier. Explain when to use each access level and how they control encapsulation and class design.

**Requirements:**
- Show all four access modifiers in action
- Demonstrate access from same class, same package, subclass, and different package
- Explain visibility rules for each modifier
- Show practical use cases for each access level
- Demonstrate how access modifiers enforce encapsulation

## Approach

- Create classes in the same package demonstrating different access levels
- Show access from within the same class
- Demonstrate package-level access
- Show protected access in subclasses
- Demonstrate public access from anywhere
- Create examples showing compilation errors for invalid access
- Provide guidelines for choosing appropriate access levels

## Solution

```java
// ========== SAME PACKAGE CLASSES ==========

// Class demonstrating all access modifiers
class AccessModifierDemo {
    // Private - accessible only within this class
    private String privateField = "Private Field";
    
    // Default (package-private) - accessible within same package
    String defaultField = "Default Field";
    
    // Protected - accessible within same package and subclasses
    protected String protectedField = "Protected Field";
    
    // Public - accessible from anywhere
    public String publicField = "Public Field";
    
    // Private method
    private void privateMethod() {
        System.out.println("Private method called");
    }
    
    // Default method
    void defaultMethod() {
        System.out.println("Default method called");
    }
    
    // Protected method
    protected void protectedMethod() {
        System.out.println("Protected method called");
    }
    
    // Public method
    public void publicMethod() {
        System.out.println("Public method called");
    }
    
    // Method to demonstrate access within same class
    public void accessAllMembers() {
        System.out.println("=== Access from same class ===");
        System.out.println(privateField);      // OK
        System.out.println(defaultField);      // OK
        System.out.println(protectedField);    // OK
        System.out.println(publicField);       // OK
        
        privateMethod();    // OK
        defaultMethod();    // OK
        protectedMethod();  // OK
        publicMethod();     // OK
    }
}

// Class in same package
class SamePackageClass {
    public void accessMembers() {
        AccessModifierDemo demo = new AccessModifierDemo();
        
        System.out.println("=== Access from same package ===");
        // System.out.println(demo.privateField);   // Compilation error!
        System.out.println(demo.defaultField);      // OK
        System.out.println(demo.protectedField);    // OK
        System.out.println(demo.publicField);       // OK
        
        // demo.privateMethod();   // Compilation error!
        demo.defaultMethod();      // OK
        demo.protectedMethod();    // OK
        demo.publicMethod();       // OK
    }
}

// Subclass in same package
class SamePackageSubclass extends AccessModifierDemo {
    public void accessInheritedMembers() {
        System.out.println("=== Access from subclass (same package) ===");
        // System.out.println(privateField);   // Compilation error!
        System.out.println(defaultField);      // OK - inherited
        System.out.println(protectedField);    // OK - inherited
        System.out.println(publicField);       // OK - inherited
        
        // privateMethod();   // Compilation error!
        defaultMethod();      // OK - inherited
        protectedMethod();    // OK - inherited
        publicMethod();       // OK - inherited
    }
}

// ========== DIFFERENT PACKAGE EXAMPLE ==========
// Note: In a real scenario, this would be in a different package
// For demonstration, we'll use comments to show what would happen

/*
package differentpackage;

import originalpackage.AccessModifierDemo;

class DifferentPackageClass {
    public void accessMembers() {
        AccessModifierDemo demo = new AccessModifierDemo();
        
        // System.out.println(demo.privateField);   // Compilation error!
        // System.out.println(demo.defaultField);   // Compilation error!
        // System.out.println(demo.protectedField); // Compilation error!
        System.out.println(demo.publicField);       // OK - only public accessible
        
        // demo.privateMethod();   // Compilation error!
        // demo.defaultMethod();   // Compilation error!
        // demo.protectedMethod(); // Compilation error!
        demo.publicMethod();       // OK - only public accessible
    }
}

class DifferentPackageSubclass extends AccessModifierDemo {
    public void accessInheritedMembers() {
        // System.out.println(privateField);   // Compilation error!
        // System.out.println(defaultField);   // Compilation error!
        System.out.println(protectedField);    // OK - inherited through subclass
        System.out.println(publicField);       // OK - inherited
        
        // privateMethod();   // Compilation error!
        // defaultMethod();   // Compilation error!
        protectedMethod();    // OK - inherited through subclass
        publicMethod();       // OK - inherited
    }
}
*/

// Practical example: Bank Account with proper encapsulation
class BankAccountExample {
    // Private - internal implementation details
    private double balance;
    private String accountNumber;
    
    // Protected - accessible to subclasses (e.g., SavingsAccount, CheckingAccount)
    protected double interestRate;
    
    // Public - interface for clients
    public BankAccountExample(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.interestRate = 0.01;
    }
    
    // Public methods - API
    public double getBalance() {
        return balance;
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
    
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    // Protected - for subclass use
    protected void applyInterest() {
        balance += balance * interestRate;
    }
    
    // Private - internal helper
    private void logTransaction(String type, double amount) {
        System.out.println(type + ": $" + amount);
    }
}

// Demo class
class AccessModifiersDemo {
    public static void main(String[] args) {
        // Test same class access
        AccessModifierDemo demo = new AccessModifierDemo();
        demo.accessAllMembers();
        
        System.out.println();
        
        // Test same package access
        SamePackageClass samePackage = new SamePackageClass();
        samePackage.accessMembers();
        
        System.out.println();
        
        // Test subclass access
        SamePackageSubclass subclass = new SamePackageSubclass();
        subclass.accessInheritedMembers();
        
        System.out.println();
        
        // Practical example
        System.out.println("=== Bank Account Example ===");
        BankAccountExample account = new BankAccountExample("ACC001", 1000);
        System.out.println("Balance: $" + account.getBalance());  // Public method
        account.deposit(500);
        System.out.println("Balance: $" + account.getBalance());
        
        // account.balance = 10000;  // Compilation error - private field
        // account.logTransaction("Deposit", 500);  // Compilation error - private method
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Access modifier checks happen at compile time, not runtime. All field and method access operations are constant time.

**Space Complexity**: O(1) - Access modifiers don't add runtime overhead. They're compile-time constructs.

## Edge Cases and Pitfalls

- **Default vs Protected**: Default (package-private) is accessible within the same package only. Protected is accessible within the same package AND in subclasses in different packages.
- **Subclass Access**: Protected members are accessible in subclasses, but only through inheritance, not through object references of the parent type in different packages.
- **Overriding Access**: When overriding methods, you cannot make them more restrictive. Can go from protected → public, but not public → protected.
- **Class Access**: Top-level classes can only be public or default (package-private), not private or protected. Inner classes can use all modifiers.
- **Constructor Access**: Private constructors prevent instantiation (useful for Singleton, utility classes). Protected constructors allow subclass instantiation only.
- **Best Practice**: Use the most restrictive access level possible. Start with private and increase visibility only when needed.

## Interview-Ready Answer

"Java has four access modifiers: private (class only), default/package-private (same package), protected (same package and subclasses), and public (everywhere). I'd demonstrate with a class having fields and methods with each modifier, showing access from same class, same package, subclass, and different package. Private hides implementation details, protected allows subclass extension, default limits to package, and public exposes the API. All access checks happen at compile time with O(1) complexity."
