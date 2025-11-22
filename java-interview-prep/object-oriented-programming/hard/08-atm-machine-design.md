# ATM Machine Design

## Problem Statement

Design an ATM system supporting authentication, balance inquiry, cash withdrawal, deposit, and transaction history with proper security and error handling.

## Approach

- Implement State pattern for ATM states (idle, authenticated, processing)
- Use Strategy pattern for transaction types
- Handle cash dispensing with denomination management
- Implement security with PIN validation and attempt limits
- Track transaction history

## Solution

```java
import java.util.*;

enum ATMState { IDLE, CARD_INSERTED, AUTHENTICATED, PROCESSING }

class Card {
    private String cardNumber;
    private String pin;
    
    public Card(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }
    
    public boolean validatePin(String pin) { return this.pin.equals(pin); }
    public String getCardNumber() { return cardNumber; }
}

class Account {
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;
    
    public Account(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
    }
    
    public synchronized boolean withdraw(double amount) {
        if (amount > balance) return false;
        balance -= amount;
        transactions.add(new Transaction("WITHDRAWAL", amount));
        return true;
    }
    
    public synchronized void deposit(double amount) {
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount));
    }
    
    public double getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; }
    public List<Transaction> getTransactions() { return transactions; }
}

class Transaction {
    private String type;
    private double amount;
    private long timestamp;
    
    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return type + ": $" + amount + " at " + new Date(timestamp);
    }
}

class CashDispenser {
    private Map<Integer, Integer> denominations;
    
    public CashDispenser() {
        denominations = new HashMap<>();
        denominations.put(100, 10);
        denominations.put(50, 20);
        denominations.put(20, 50);
        denominations.put(10, 100);
    }
    
    public boolean dispenseCash(double amount) {
        if (amount % 10 != 0) return false;
        
        int remaining = (int) amount;
        Map<Integer, Integer> toDispense = new HashMap<>();
        
        for (int denom : new int[]{100, 50, 20, 10}) {
            int count = Math.min(remaining / denom, denominations.get(denom));
            if (count > 0) {
                toDispense.put(denom, count);
                remaining -= count * denom;
            }
        }
        
        if (remaining > 0) return false;
        
        // Deduct from inventory
        for (Map.Entry<Integer, Integer> entry : toDispense.entrySet()) {
            denominations.put(entry.getKey(), denominations.get(entry.getKey()) - entry.getValue());
        }
        
        System.out.println("Dispensed: " + toDispense);
        return true;
    }
}

class ATM {
    private ATMState state;
    private Card currentCard;
    private Account currentAccount;
    private CashDispenser cashDispenser;
    private Map<String, Account> accounts;
    private int pinAttempts;
    private static final int MAX_PIN_ATTEMPTS = 3;
    
    public ATM() {
        this.state = ATMState.IDLE;
        this.cashDispenser = new CashDispenser();
        this.accounts = new HashMap<>();
        this.pinAttempts = 0;
    }
    
    public void addAccount(String cardNumber, Account account) {
        accounts.put(cardNumber, account);
    }
    
    public boolean insertCard(Card card) {
        if (state != ATMState.IDLE) return false;
        this.currentCard = card;
        this.state = ATMState.CARD_INSERTED;
        this.pinAttempts = 0;
        System.out.println("Card inserted");
        return true;
    }
    
    public boolean enterPin(String pin) {
        if (state != ATMState.CARD_INSERTED) return false;
        
        if (currentCard.validatePin(pin)) {
            currentAccount = accounts.get(currentCard.getCardNumber());
            if (currentAccount != null) {
                state = ATMState.AUTHENTICATED;
                System.out.println("Authentication successful");
                return true;
            }
        }
        
        pinAttempts++;
        if (pinAttempts >= MAX_PIN_ATTEMPTS) {
            System.out.println("Card blocked due to too many attempts");
            ejectCard();
        }
        return false;
    }
    
    public boolean withdraw(double amount) {
        if (state != ATMState.AUTHENTICATED) return false;
        
        if (currentAccount.withdraw(amount) && cashDispenser.dispenseCash(amount)) {
            System.out.println("Withdrawal successful. New balance: $" + currentAccount.getBalance());
            return true;
        }
        System.out.println("Withdrawal failed");
        return false;
    }
    
    public double checkBalance() {
        if (state != ATMState.AUTHENTICATED) return -1;
        return currentAccount.getBalance();
    }
    
    public void ejectCard() {
        currentCard = null;
        currentAccount = null;
        state = ATMState.IDLE;
        System.out.println("Card ejected");
    }
}

class ATMDemo {
    public static void main(String[] args) {
        ATM atm = new ATM();
        
        Account account = new Account("ACC001", 1000);
        atm.addAccount("CARD001", account);
        
        Card card = new Card("CARD001", "1234");
        
        atm.insertCard(card);
        atm.enterPin("1234");
        System.out.println("Balance: $" + atm.checkBalance());
        atm.withdraw(100);
        atm.ejectCard();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for most operations, O(d) for cash dispensing with d denominations
**Space Complexity**: O(a + t) for accounts and transactions

## Edge Cases and Pitfalls

- **PIN Security**: Limit attempts and block card after failures
- **Concurrent Access**: Synchronize account operations
- **Cash Availability**: Check denomination availability before dispensing
- **Transaction Atomicity**: Ensure withdrawal and dispensing are atomic
- **Network Failures**: Handle connection issues gracefully

## Interview-Ready Answer

"I'd design an ATM with State pattern for states (idle, authenticated, processing). Implement Card, Account, and CashDispenser classes. Validate PIN with attempt limits, synchronize account operations for thread safety, and handle cash dispensing with denomination management. Track transactions and balance. Time complexity is O(1) for most operations, space is O(a+t) for accounts and transactions."
