package core;

import java.util.Optional;

import entities.User;

public class OptionalClass {
    public void initialize() {
        System.out.println("Optional Class Initialized");

        demonstrateCreation();
        demonstrateOperations();
    }

    private void demonstrateCreation() {
        System.out.println("=== Optional Creation ===");
        
        Optional<String> empty = Optional.empty();
        Optional<String> nonEmpty = Optional.of("value");
        Optional<String> nullable = Optional.ofNullable(null);
        
        System.out.println("Empty present: " + empty.isPresent());
        System.out.println("NonEmpty present: " + nonEmpty.isPresent());
        System.out.println("Nullable present: " + nullable.isPresent());
    }

    private void demonstrateOperations() {
        System.out.println("\n=== Optional Operations ===");
        
        User user1 = new User("Alice", "alice@example.com");
        User user2 = new User("Bob", null);

        System.out.println(user2.getEmail());
        user1.getEmail().ifPresent(System.out::println);
    }
}
