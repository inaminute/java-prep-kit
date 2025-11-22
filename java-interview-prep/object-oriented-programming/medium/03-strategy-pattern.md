# Strategy Pattern

## Problem Statement

Implement the Strategy design pattern to create a payment processing system where the payment method can be selected at runtime. The system should support multiple payment strategies (CreditCard, PayPal, Cryptocurrency) and allow switching between them without modifying the client code.

**Requirements:**
- Create a PaymentStrategy interface
- Implement concrete strategy classes for different payment methods
- Create a ShoppingCart context class that uses a strategy
- Allow changing the strategy at runtime
- Demonstrate that different strategies can be used interchangeably
- Show how strategy pattern eliminates conditional logic

## Approach

- Define a PaymentStrategy interface with a pay() method
- Implement concrete strategies for each payment method
- Create a context class (ShoppingCart) that holds a strategy reference
- Provide a method to set/change the strategy
- Delegate payment processing to the current strategy
- Client code selects and sets the appropriate strategy

## Solution

```java
// Strategy interface
interface PaymentStrategy {
    void pay(double amount);
    String getPaymentType();
}

// Concrete Strategy - Credit Card
class CreditCardStrategy implements PaymentStrategy {
    private String cardNumber;
    private String cvv;
    private String expiryDate;
    
    public CreditCardStrategy(String cardNumber, String cvv, String expiryDate) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Credit Card");
        System.out.println("Card: **** **** **** " + cardNumber.substring(cardNumber.length() - 4));
    }
    
    @Override
    public String getPaymentType() {
        return "Credit Card";
    }
}

// Concrete Strategy - PayPal
class PayPalStrategy implements PaymentStrategy {
    private String email;
    private String password;
    
    public PayPalStrategy(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using PayPal");
        System.out.println("Account: " + email);
    }
    
    @Override
    public String getPaymentType() {
        return "PayPal";
    }
}

// Concrete Strategy - Cryptocurrency
class CryptoStrategy implements PaymentStrategy {
    private String walletAddress;
    private String cryptoType;
    
    public CryptoStrategy(String walletAddress, String cryptoType) {
        this.walletAddress = walletAddress;
        this.cryptoType = cryptoType;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using " + cryptoType);
        System.out.println("Wallet: " + walletAddress);
    }
    
    @Override
    public String getPaymentType() {
        return cryptoType + " Cryptocurrency";
    }
}

// Context class
class ShoppingCart {
    private PaymentStrategy paymentStrategy;
    private double totalAmount;
    
    public ShoppingCart() {
        this.totalAmount = 0.0;
    }
    
    public void addItem(String item, double price) {
        System.out.println("Added: " + item + " - $" + price);
        totalAmount += price;
    }
    
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
        System.out.println("Payment method set to: " + strategy.getPaymentType());
    }
    
    public void checkout() {
        if (paymentStrategy == null) {
            System.out.println("Please select a payment method!");
            return;
        }
        
        System.out.println("\n--- Checkout ---");
        System.out.println("Total amount: $" + totalAmount);
        paymentStrategy.pay(totalAmount);
        System.out.println("Payment successful!\n");
        
        // Reset cart
        totalAmount = 0.0;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
}

// Demo class
class StrategyPatternDemo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        
        // Scenario 1: Pay with Credit Card
        System.out.println("=== Order 1: Credit Card Payment ===");
        cart.addItem("Laptop", 999.99);
        cart.addItem("Mouse", 29.99);
        cart.setPaymentStrategy(new CreditCardStrategy("1234567890123456", "123", "12/25"));
        cart.checkout();
        
        // Scenario 2: Pay with PayPal
        System.out.println("=== Order 2: PayPal Payment ===");
        cart.addItem("Headphones", 149.99);
        cart.addItem("Keyboard", 79.99);
        cart.setPaymentStrategy(new PayPalStrategy("user@example.com", "password123"));
        cart.checkout();
        
        // Scenario 3: Pay with Cryptocurrency
        System.out.println("=== Order 3: Crypto Payment ===");
        cart.addItem("Monitor", 299.99);
        cart.setPaymentStrategy(new CryptoStrategy("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", "Bitcoin"));
        cart.checkout();
        
        // Scenario 4: Change strategy at runtime
        System.out.println("=== Order 4: Changing Payment Method ===");
        cart.addItem("Webcam", 89.99);
        cart.setPaymentStrategy(new PayPalStrategy("another@example.com", "pass456"));
        System.out.println("Changed mind about payment method...");
        cart.setPaymentStrategy(new CreditCardStrategy("9876543210987654", "456", "06/26"));
        cart.checkout();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Setting and executing a strategy are constant time operations. The strategy execution time depends on the specific strategy implementation.

**Space Complexity**: O(1) - The context holds a single reference to the current strategy. Creating strategy objects uses constant space per strategy.

## Edge Cases and Pitfalls

- **Null Strategy**: Always check if a strategy is set before using it. Provide a default strategy or throw an exception if none is set.
- **Strategy State**: Strategies should be stateless or have minimal state. If strategies need significant state, consider the State pattern instead.
- **Strategy Selection**: Client code must know about all available strategies to select one. Consider using a factory or registry to manage strategy creation.
- **Overhead**: For simple cases with few variations, strategy pattern may be overkill. Use conditional logic if there are only 2-3 simple options.
- **Thread Safety**: If strategies maintain state, ensure thread safety when used in concurrent environments.
- **Strategy Validation**: Validate strategy-specific data (card numbers, email formats) before executing payment to fail fast.

## Interview-Ready Answer

"The Strategy pattern defines a family of algorithms, encapsulates each one, and makes them interchangeable at runtime. I'd create a PaymentStrategy interface with a pay() method, implement concrete strategies for CreditCard, PayPal, and Crypto, and use a ShoppingCart context class that holds a strategy reference. The client sets the desired strategy, and the context delegates payment processing to it. This eliminates conditional logic and makes adding new payment methods easy. Time and space complexity are O(1)."
