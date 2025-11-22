# Adapter Pattern

## Problem Statement

Implement the Adapter design pattern to integrate a third-party payment processing library with an incompatible interface into your existing payment system. The adapter should convert the interface of the third-party class into an interface your system expects, allowing them to work together without modifying existing code.

**Requirements:**
- Create a target interface that your system uses
- Implement an adaptee class (third-party library with different interface)
- Create an adapter that implements the target interface
- Adapter should delegate calls to the adaptee
- Demonstrate both class adapter (inheritance) and object adapter (composition)
- Show how adapter enables incompatible interfaces to work together

## Approach

- Define the target interface your system expects
- Create an adaptee class with a different interface
- Implement an adapter class that implements the target interface
- Adapter wraps the adaptee and translates method calls
- Use composition (object adapter) for flexibility
- Optionally show inheritance-based adapter (class adapter)

## Solution

```java
// Target interface - what our system expects
interface PaymentProcessor {
    void processPayment(String accountId, double amount);
    boolean validateAccount(String accountId);
}

// Existing implementation that works with our interface
class InternalPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(String accountId, double amount) {
        System.out.println("Internal processor: Processing $" + amount + " for account " + accountId);
    }
    
    @Override
    public boolean validateAccount(String accountId) {
        System.out.println("Internal processor: Validating account " + accountId);
        return accountId != null && !accountId.isEmpty();
    }
}

// Adaptee - Third-party payment library with incompatible interface
class ThirdPartyPaymentGateway {
    public void makePayment(String userId, String userEmail, double amount) {
        System.out.println("Third-party gateway: Processing $" + amount);
        System.out.println("User: " + userId + " (" + userEmail + ")");
    }
    
    public boolean checkUser(String userId, String userEmail) {
        System.out.println("Third-party gateway: Checking user " + userId);
        return userId != null && userEmail != null;
    }
    
    public String getTransactionId() {
        return "TXN-" + System.currentTimeMillis();
    }
}

// Object Adapter - uses composition
class PaymentAdapter implements PaymentProcessor {
    private ThirdPartyPaymentGateway gateway;
    private String userEmail;  // Additional data needed for adaptation
    
    public PaymentAdapter(ThirdPartyPaymentGateway gateway, String userEmail) {
        this.gateway = gateway;
        this.userEmail = userEmail;
    }
    
    @Override
    public void processPayment(String accountId, double amount) {
        // Adapt the interface: convert accountId to userId and add email
        System.out.println("Adapter: Converting request to third-party format");
        gateway.makePayment(accountId, userEmail, amount);
        String txnId = gateway.getTransactionId();
        System.out.println("Adapter: Transaction ID: " + txnId);
    }
    
    @Override
    public boolean validateAccount(String accountId) {
        System.out.println("Adapter: Converting validation request");
        return gateway.checkUser(accountId, userEmail);
    }
}

// Another adaptee - Legacy payment system
class LegacyPaymentSystem {
    public void pay(int accountNumber, int amountInCents) {
        System.out.println("Legacy system: Processing " + amountInCents + " cents for account " + accountNumber);
    }
    
    public boolean verify(int accountNumber) {
        System.out.println("Legacy system: Verifying account " + accountNumber);
        return accountNumber > 0;
    }
}

// Adapter for legacy system
class LegacyPaymentAdapter implements PaymentProcessor {
    private LegacyPaymentSystem legacySystem;
    
    public LegacyPaymentAdapter(LegacyPaymentSystem legacySystem) {
        this.legacySystem = legacySystem;
    }
    
    @Override
    public void processPayment(String accountId, double amount) {
        // Convert String to int and dollars to cents
        System.out.println("Legacy adapter: Converting modern format to legacy format");
        int accountNumber = Integer.parseInt(accountId);
        int amountInCents = (int) (amount * 100);
        legacySystem.pay(accountNumber, amountInCents);
    }
    
    @Override
    public boolean validateAccount(String accountId) {
        System.out.println("Legacy adapter: Converting account ID");
        int accountNumber = Integer.parseInt(accountId);
        return legacySystem.verify(accountNumber);
    }
}

// Client code that uses PaymentProcessor interface
class PaymentService {
    public void executePayment(PaymentProcessor processor, String accountId, double amount) {
        System.out.println("\n--- Payment Service ---");
        if (processor.validateAccount(accountId)) {
            processor.processPayment(accountId, amount);
            System.out.println("Payment completed successfully!");
        } else {
            System.out.println("Account validation failed!");
        }
    }
}

// Demo class
class AdapterPatternDemo {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();
        
        // Use internal processor directly
        System.out.println("=== Using Internal Processor ===");
        PaymentProcessor internal = new InternalPaymentProcessor();
        service.executePayment(internal, "ACC123", 100.00);
        
        // Use third-party gateway through adapter
        System.out.println("\n=== Using Third-Party Gateway (via Adapter) ===");
        ThirdPartyPaymentGateway gateway = new ThirdPartyPaymentGateway();
        PaymentProcessor adapter = new PaymentAdapter(gateway, "user@example.com");
        service.executePayment(adapter, "USER456", 250.00);
        
        // Use legacy system through adapter
        System.out.println("\n=== Using Legacy System (via Adapter) ===");
        LegacyPaymentSystem legacy = new LegacyPaymentSystem();
        PaymentProcessor legacyAdapter = new LegacyPaymentAdapter(legacy);
        service.executePayment(legacyAdapter, "789", 75.50);
        
        // Demonstrate polymorphism - all processors used uniformly
        System.out.println("\n=== Polymorphic Usage ===");
        PaymentProcessor[] processors = {internal, adapter, legacyAdapter};
        for (int i = 0; i < processors.length; i++) {
            System.out.println("Processor " + (i + 1) + ":");
            processors[i].validateAccount("TEST" + i);
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Adapter methods perform constant time operations (method delegation and simple data conversions).

**Space Complexity**: O(1) - Adapter stores a reference to the adaptee and possibly some additional data for conversion, all constant space.

## Edge Cases and Pitfalls

- **Data Loss**: When converting between interfaces, ensure no critical data is lost. Document any limitations of the adaptation.
- **Type Conversion**: Converting between types (String to int, dollars to cents) can cause errors. Add validation and error handling.
- **Two-Way Adaptation**: Adapters typically work one way. If you need bidirectional conversion, create separate adapters for each direction.
- **Performance**: Adapters add an extra layer of indirection. For performance-critical code, consider refactoring to use compatible interfaces directly.
- **Multiple Adaptees**: If adapting multiple incompatible classes, you'll need multiple adapter classes. Consider using a factory to manage them.
- **Incomplete Adaptation**: If the adaptee doesn't provide all functionality needed by the target interface, you may need to implement workarounds or throw UnsupportedOperationException.

## Interview-Ready Answer

"The Adapter pattern converts the interface of a class into another interface clients expect, allowing incompatible interfaces to work together. I'd create a PaymentProcessor target interface, a ThirdPartyPaymentGateway adaptee with different methods, and a PaymentAdapter that implements the target interface and wraps the adaptee. The adapter translates method calls and data formats between the two interfaces. This enables integrating third-party libraries without modifying existing code. Time and space complexity are O(1)."
