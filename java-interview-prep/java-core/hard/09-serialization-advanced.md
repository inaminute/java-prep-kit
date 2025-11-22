# Advanced Serialization

## Problem Statement

Explain Java serialization in depth. Demonstrate custom serialization, serialization security issues, and alternatives like Externalizable. Show how to handle versioning and compatibility.

**Requirements**:
- Explain serialization mechanism
- Implement custom serialization with writeObject/readObject
- Show Externalizable interface
- Demonstrate serialVersionUID and versioning

## Approach

- Serialization converts objects to byte streams
- Implement Serializable for default serialization
- Override writeObject/readObject for custom control
- Use Externalizable for complete control
- serialVersionUID ensures version compatibility

## Solution

```java
import java.io.*;

// Basic serialization
class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private transient int age; // Not serialized
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String toString() {
        return "Person{name='" + name + "', age=" + age + "}";
    }
}

// Custom serialization
class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private transient String password;
    
    public Employee(String name, String password) {
        this.name = name;
        this.password = password;
    }
    
    // Custom serialization
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // Encrypt password before writing
        String encrypted = encrypt(password);
        oos.writeObject(encrypted);
    }
    
    private void readObject(ObjectInputStream ois) 
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // Decrypt password after reading
        String encrypted = (String) ois.readObject();
        this.password = decrypt(encrypted);
    }
    
    private String encrypt(String s) {
        return new StringBuilder(s).reverse().toString(); // Simple example
    }
    
    private String decrypt(String s) {
        return new StringBuilder(s).reverse().toString();
    }
    
    public String toString() {
        return "Employee{name='" + name + "', password='" + password + "'}";
    }
}

// Externalizable for complete control
class Product implements Externalizable {
    private String name;
    private double price;
    
    public Product() {} // Required no-arg constructor
    
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeDouble(price);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException {
        name = in.readUTF();
        price = in.readDouble();
    }
    
    public String toString() {
        return "Product{name='" + name + "', price=" + price + "}";
    }
}

public class SerializationAdvanced {
    
    public static void main(String[] args) throws Exception {
        demonstrateBasicSerialization();
        demonstrateCustomSerialization();
        demonstrateExternalizable();
        explainSecurityIssues();
    }
    
    public static void demonstrateBasicSerialization() throws Exception {
        System.out.println("=== Basic Serialization ===");
        
        Person person = new Person("Alice", 30);
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(person);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Person restored = (Person) ois.readObject();
        ois.close();
        
        System.out.println("Original: " + person);
        System.out.println("Restored: " + restored);
        System.out.println("Note: transient age field is 0");
    }
    
    public static void demonstrateCustomSerialization() throws Exception {
        System.out.println("\n=== Custom Serialization ===");
        
        Employee emp = new Employee("Bob", "secret123");
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(emp);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Employee restored = (Employee) ois.readObject();
        ois.close();
        
        System.out.println("Original: " + emp);
        System.out.println("Restored: " + restored);
        System.out.println("Password was encrypted during serialization");
    }
    
    public static void demonstrateExternalizable() throws Exception {
        System.out.println("\n=== Externalizable ===");
        
        Product product = new Product("Laptop", 999.99);
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(product);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Product restored = (Product) ois.readObject();
        ois.close();
        
        System.out.println("Original: " + product);
        System.out.println("Restored: " + restored);
    }
    
    public static void explainSecurityIssues() {
        System.out.println("\n=== Security Issues ===");
        System.out.println("1. Deserialization attacks: Malicious data can execute code");
        System.out.println("2. Object injection: Untrusted data creates objects");
        System.out.println("3. DoS attacks: Large objects exhaust memory");
        
        System.out.println("\nMitigations:");
        System.out.println("- Validate input before deserialization");
        System.out.println("- Use ObjectInputFilter (Java 9+)");
        System.out.println("- Consider alternatives: JSON, Protocol Buffers");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) where n is object graph size

**Space Complexity**: O(n) for serialized data

## Edge Cases and Pitfalls

- **serialVersionUID**: Mismatch causes InvalidClassException
- **transient fields**: Not serialized, reset to default values
- **Static fields**: Not serialized (belong to class, not instance)
- **Security**: Deserialization can execute arbitrary code

## Interview-Ready Answer

"Serialization converts objects to byte streams. Implement Serializable for default behavior, override writeObject/readObject for custom control, or use Externalizable for complete control. transient fields aren't serialized. serialVersionUID ensures version compatibility. Security risks include deserialization attacks - validate input and consider alternatives like JSON. Use ObjectInputFilter in Java 9+ for protection."
