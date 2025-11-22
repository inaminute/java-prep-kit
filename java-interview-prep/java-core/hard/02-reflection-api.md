# Reflection API

## Problem Statement

Demonstrate the Java Reflection API. Show how to inspect classes, invoke methods, access fields, and create instances dynamically at runtime. Explain use cases and performance implications.

**Requirements**:
- Inspect class structure (fields, methods, constructors)
- Invoke methods dynamically
- Access and modify private fields
- Create instances using reflection

## Approach

- Reflection allows runtime inspection and manipulation of classes
- Use Class.forName() or .class to get Class object
- getDeclaredFields/Methods includes private members
- setAccessible(true) bypasses access control
- Reflection is slower than direct access

## Solution

```java
import java.lang.reflect.*;

class Person {
    private String name;
    private int age;
    
    public Person() {
        this.name = "Unknown";
        this.age = 0;
    }
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    private void secretMethod() {
        System.out.println("Secret method called!");
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

public class ReflectionAPI {
    
    public static void main(String[] args) throws Exception {
        inspectClass();
        invokeMethod();
        accessPrivateField();
        createInstance();
    }
    
    public static void inspectClass() throws Exception {
        System.out.println("=== Inspect Class ===");
        
        Class<?> clazz = Person.class;
        
        System.out.println("Class name: " + clazz.getName());
        
        // Fields
        System.out.println("\nFields:");
        for (Field field : clazz.getDeclaredFields()) {
            System.out.println("  " + field.getName() + ": " + field.getType());
        }
        
        // Methods
        System.out.println("\nMethods:");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("  " + method.getName());
        }
        
        // Constructors
        System.out.println("\nConstructors:");
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            System.out.println("  " + constructor);
        }
    }
    
    public static void invokeMethod() throws Exception {
        System.out.println("\n=== Invoke Method ===");
        
        Person person = new Person("Alice", 30);
        Class<?> clazz = person.getClass();
        
        // Invoke public method
        Method getName = clazz.getMethod("getName");
        String name = (String) getName.invoke(person);
        System.out.println("Name: " + name);
        
        // Invoke private method
        Method secretMethod = clazz.getDeclaredMethod("secretMethod");
        secretMethod.setAccessible(true);
        secretMethod.invoke(person);
    }
    
    public static void accessPrivateField() throws Exception {
        System.out.println("\n=== Access Private Field ===");
        
        Person person = new Person("Bob", 25);
        Class<?> clazz = person.getClass();
        
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true);
        
        String name = (String) nameField.get(person);
        System.out.println("Original name: " + name);
        
        nameField.set(person, "Charlie");
        System.out.println("Modified name: " + person.getName());
    }
    
    public static void createInstance() throws Exception {
        System.out.println("\n=== Create Instance ===");
        
        Class<?> clazz = Person.class;
        
        // Using default constructor
        Person p1 = (Person) clazz.getDeclaredConstructor().newInstance();
        System.out.println("Default: " + p1.getName());
        
        // Using parameterized constructor
        Constructor<?> constructor = clazz.getConstructor(String.class, int.class);
        Person p2 = (Person) constructor.newInstance("David", 35);
        System.out.println("Parameterized: " + p2.getName());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for most operations, but slower than direct access

**Space Complexity**: O(1)

## Edge Cases and Pitfalls

- **Performance**: Reflection is significantly slower than direct access
- **Security**: setAccessible may be blocked by SecurityManager
- **Type safety**: Reflection bypasses compile-time type checking

## Interview-Ready Answer

"Reflection API allows runtime inspection and manipulation of classes. Use Class object to get fields, methods, and constructors. setAccessible(true) bypasses access control for private members. Common uses include frameworks, serialization, and dependency injection. Reflection is slower than direct access and bypasses type safety, so use sparingly."
