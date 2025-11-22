# Polymorphism Demo

## Problem Statement

Demonstrate polymorphism in Java by creating a payment processing system that can handle different payment methods (CreditCard, PayPal, Bitcoin) through a common interface. Show how polymorphism allows treating different objects uniformly while each maintains its specific behavior. Implement both compile-time (method overloading) and runtime (method overriding) polymorphism.

**Requirements:**
- Create a Payment interface with a common method
- Implement multiple payment types with specific behavior
- Demonstrate runtime polymorphism through method overriding
- Show compile-time polymorphism through method overloading
- Process payments uniformly through the interface

## Approach

- Define a Payment interface with processPayment() method
- Create concrete classes implementing the interface
- Override processPayment() in each class with specific logic
- Use interface reference to hold different implementation objects
- Demonstrate method overloading for compile-time polymorphism
- Show how polymorphism enables flexible and extensible code

## Solution

```java
// Payment interface
interface Payment {
    void processPayment(double amount);
    String getPaymentType();
}

// CreditCard implementation
class CreditCardPayment implements Payment {
    private String cardNumber;
    private String cardHolder;
    
    public CreditCardPayment(String cardNumber, String cardHolder) {
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
    }
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit card payment of $" + amount);
        System.out.println("Card: " + maskCardNumber(cardNumber) + ", Holder: " + cardHolder);
    }
    
    @Override
    public String getPaymentType() {
        return "Credit Card";
    }
    
    private String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

// PayPal implementation
class PayPalPayment implements Payment {
    private String email;
    
    public PayPalPayment(String email) {
        this.email = email;
    }
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing PayPal payment of $" + amount);
        System.out.println("PayPal account: " + email);
    }
    
    @Override
    public String getPaymentType() {
        return "PayPal";
    }
}

// Bitcoin implementation
class BitcoinPayment implements Payment {
    private String walletAddress;
    
    public BitcoinPayment(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing Bitcoin payment of $" + amount);
        System.out.println("Wallet: " + walletAddress);
    }
    
    @Override
    public String getPaymentType() {
        return "Bitcoin";
    }
}

// Payment processor demonstrating polymorphism
class PaymentProcessor {
    // Runtime polymorphism - method accepts interface type
    public void executePayment(Payment payment, double amount) {
        System.out.println("\n--- Processing " + payment.getPaymentType() + " ---");
        payment.processPayment(amount);
        System.out.println("Payment completed successfully!");
    }
    
    // Compile-time polymorphism - method overloading
    public void executePayment(Payment payment, double amount, String currency) {
        System.out.println("\n--- Processing " + payment.getPaymentType() + " in " + currency + " ---");
        payment.processPayment(amount);
        System.out.println("Payment completed successfully!");
    }
    
    // Another overloaded method
    public void executePayment(Payment payment, double amount, boolean sendReceipt) {
        executePayment(payment, amount);
        if (sendReceipt) {
            System.out.println("Receipt sent to customer");
        }
    }
}

// Demo class
class PolymorphismDemo {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor();
        
        // Create different payment objects
        Payment creditCard = new CreditCardPayment("1234567890123456", "John Doe");
        Payment paypal = new PayPalPayment("john@example.com");
        Payment bitcoin = new BitcoinPayment("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        
        // Runtime polymorphism - same method call, different behavior
        processor.executePayment(creditCard, 100.00);
        processor.executePayment(paypal, 50.00);
        processor.executePayment(bitcoin, 75.00);
        
        // Compile-time polymorphism - method overloading
        processor.executePayment(creditCard, 200.00, "USD");
        processor.executePayment(paypal, 150.00, true);
        
        // Polymorphic array
        Payment[] payments = {creditCard, paypal, bitcoin};
        System.out.println("\n--- Processing batch payments ---");
        for (Payment payment : payments) {
            payment.processPayment(25.00);
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Each payment processing operation takes constant time. Method dispatch for polymorphic calls is O(1) through virtual method tables.

**Space Complexity**: O(1) - Each payment object uses constant space for its fields. The polymorphic reference doesn't add overhead.

## Edge Cases and Pitfalls

- **Null References**: Always check for null before calling methods on polymorphic references to avoid NullPointerException.
- **Type Casting**: Avoid downcasting (casting interface to concrete type) unless absolutely necessary. Use instanceof check before casting.
- **Method Overloading vs Overriding**: Overloading is resolved at compile time based on parameter types, while overriding is resolved at runtime based on object type.
- **Covariant Return Types**: Overriding methods can return a subtype of the parent's return type, but parameter types must match exactly.
- **Static Methods**: Static methods cannot be overridden (they're hidden, not overridden), so they don't participate in runtime polymorphism.

## Interview-Ready Answer

"Polymorphism allows objects of different types to be treated uniformly through a common interface. I'd create a Payment interface with processPayment(), implement it in CreditCard, PayPal, and Bitcoin classes with specific behavior, and use interface references to process payments uniformly. This demonstrates runtime polymorphism through method overriding. I'd also show compile-time polymorphism through method overloading. Time and space complexity are O(1)."
