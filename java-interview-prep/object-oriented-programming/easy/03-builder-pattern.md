# Builder Pattern

## Problem Statement

Implement the Builder design pattern to construct a complex User object with multiple optional parameters. The User class should have required fields (id, username) and optional fields (email, phone, address, age). The Builder pattern should provide a fluent interface for setting parameters and ensure that required fields are always set before object creation.

**Requirements:**
- User class with required and optional fields
- Fluent interface for setting parameters
- Immutable User object once created
- Validation of required fields
- Clean and readable object construction

## Approach

- Make the User class constructor private
- Create a static nested Builder class inside User
- Builder has the same fields as User
- Provide setter methods in Builder that return 'this' for method chaining
- Implement a build() method that creates and returns the User object
- Validate required fields in the build() method
- Make User fields final for immutability

## Solution

```java
public class User {
    // Required fields
    private final String id;
    private final String username;
    
    // Optional fields
    private final String email;
    private final String phone;
    private final String address;
    private final int age;
    
    // Private constructor - only Builder can create User
    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.age = builder.age;
    }
    
    // Getters only (no setters - immutable)
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public int getAge() { return age; }
    
    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username + 
               "', email='" + email + "', phone='" + phone + 
               "', address='" + address + "', age=" + age + "}";
    }
    
    // Static nested Builder class
    public static class Builder {
        // Required fields
        private final String id;
        private final String username;
        
        // Optional fields - initialized to default values
        private String email = "";
        private String phone = "";
        private String address = "";
        private int age = 0;
        
        // Constructor with required parameters
        public Builder(String id, String username) {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("ID cannot be null or empty");
            }
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            this.id = id;
            this.username = username;
        }
        
        // Fluent setter methods for optional fields
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        
        public Builder age(int age) {
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }
            this.age = age;
            return this;
        }
        
        // Build method creates the User object
        public User build() {
            return new User(this);
        }
    }
}

// Usage example
class BuilderPatternDemo {
    public static void main(String[] args) {
        // Create user with all fields
        User user1 = new User.Builder("1", "john_doe")
                .email("john@example.com")
                .phone("123-456-7890")
                .address("123 Main St")
                .age(30)
                .build();
        
        // Create user with only required fields
        User user2 = new User.Builder("2", "jane_doe")
                .email("jane@example.com")
                .build();
        
        System.out.println(user1);
        System.out.println(user2);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Building an object involves setting fields and creating one instance, all constant time operations.

**Space Complexity**: O(1) - One User object and one temporary Builder object are created per build operation.

## Edge Cases and Pitfalls

- **Required Fields Validation**: Validate required fields in the Builder constructor to fail fast if essential data is missing.
- **Immutability**: Make User fields final and provide only getters to ensure objects cannot be modified after creation.
- **Null Values**: Handle null values for optional fields by providing default values or explicit null checks.
- **Builder Reuse**: Don't reuse Builder instances after calling build() as it can lead to unexpected state. Create a new Builder for each object.
- **Thread Safety**: Builder pattern itself is not thread-safe. If multiple threads share a Builder, synchronization is needed.

## Interview-Ready Answer

"The Builder pattern separates object construction from representation, ideal for objects with many optional parameters. I'd create a static nested Builder class with fluent setter methods that return 'this' for chaining. Required fields go in the Builder constructor, optional fields have setter methods, and build() creates the immutable User object. This avoids telescoping constructors and makes code more readable. Time and space complexity are O(1)."
