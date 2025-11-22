# Method Overloading

## Problem Statement

Demonstrate method overloading in Java by creating a Calculator class with multiple versions of arithmetic methods that accept different parameter types and counts. Show how Java resolves overloaded methods at compile time based on method signatures and explain the rules for valid method overloading.

**Requirements:**
- Create multiple methods with the same name but different parameters
- Show overloading with different parameter counts
- Show overloading with different parameter types
- Demonstrate automatic type promotion in method resolution
- Explain method signature and overloading rules

## Approach

- Create methods with the same name but different parameter lists
- Vary the number of parameters (arity)
- Vary the types of parameters
- Show how return type alone doesn't enable overloading
- Demonstrate varargs in overloading
- Show compile-time method resolution

## Solution

```java
public class Calculator {
    
    // Overloaded add methods - different number of parameters
    public int add(int a, int b) {
        System.out.println("add(int, int) called");
        return a + b;
    }
    
    public int add(int a, int b, int c) {
        System.out.println("add(int, int, int) called");
        return a + b + c;
    }
    
    // Overloaded add methods - different parameter types
    public double add(double a, double b) {
        System.out.println("add(double, double) called");
        return a + b;
    }
    
    public String add(String a, String b) {
        System.out.println("add(String, String) called");
        return a + b;
    }
    
    // Overloaded multiply methods
    public int multiply(int a, int b) {
        System.out.println("multiply(int, int) called");
        return a * b;
    }
    
    public double multiply(double a, double b) {
        System.out.println("multiply(double, double) called");
        return a * b;
    }
    
    // Mixed types
    public double multiply(int a, double b) {
        System.out.println("multiply(int, double) called");
        return a * b;
    }
    
    public double multiply(double a, int b) {
        System.out.println("multiply(double, int) called");
        return a * b;
    }
    
    // Varargs overloading
    public int sum(int... numbers) {
        System.out.println("sum(int...) called with " + numbers.length + " arguments");
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }
    
    // Overloading with arrays
    public int sum(int[] numbers) {
        System.out.println("sum(int[]) called");
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }
    
    // Different return types alone DON'T enable overloading
    // This would cause compilation error if uncommented:
    // public double add(int a, int b) { return a + b; }
    
    // Display method with different parameter types
    public void display(int value) {
        System.out.println("Integer: " + value);
    }
    
    public void display(double value) {
        System.out.println("Double: " + value);
    }
    
    public void display(String value) {
        System.out.println("String: " + value);
    }
    
    public void display(boolean value) {
        System.out.println("Boolean: " + value);
    }
}

// Demo class
class MethodOverloadingDemo {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        
        // Different number of parameters
        System.out.println("Result: " + calc.add(5, 10));
        System.out.println("Result: " + calc.add(5, 10, 15));
        
        System.out.println();
        
        // Different parameter types
        System.out.println("Result: " + calc.add(5.5, 10.5));
        System.out.println("Result: " + calc.add("Hello", "World"));
        
        System.out.println();
        
        // Type promotion - int promoted to double
        System.out.println("Result: " + calc.multiply(5, 10));
        System.out.println("Result: " + calc.multiply(5.5, 10.5));
        System.out.println("Result: " + calc.multiply(5, 10.5));
        System.out.println("Result: " + calc.multiply(5.5, 10));
        
        System.out.println();
        
        // Varargs
        System.out.println("Result: " + calc.sum(1, 2, 3, 4, 5));
        System.out.println("Result: " + calc.sum(10, 20));
        
        System.out.println();
        
        // Array
        int[] numbers = {1, 2, 3, 4, 5};
        System.out.println("Result: " + calc.sum(numbers));
        
        System.out.println();
        
        // Display with different types
        calc.display(42);
        calc.display(3.14);
        calc.display("Java");
        calc.display(true);
        
        System.out.println();
        
        // Automatic type promotion
        byte b = 10;
        short s = 20;
        calc.display(b);  // byte promoted to int
        calc.display(s);  // short promoted to int
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) - Method resolution happens at compile time. Each arithmetic operation is constant time. The varargs sum method is O(n) where n is the number of arguments.

**Space Complexity**: O(1) - No additional space is used beyond the parameters and local variables.

## Edge Cases and Pitfalls

- **Return Type Alone**: Changing only the return type doesn't create a valid overload. Method signature includes name and parameter types, not return type.
- **Type Promotion**: Java automatically promotes smaller types (byte → short → int → long → float → double). This can cause ambiguity if multiple overloads match.
- **Ambiguous Calls**: If multiple overloaded methods match after type promotion, compilation fails. Example: having both add(int, double) and add(double, int) makes add(5, 10) ambiguous.
- **Varargs vs Array**: Varargs and array parameters can cause ambiguity. Prefer explicit array parameter if you always expect an array.
- **Null Arguments**: Passing null can be ambiguous if multiple overloads accept reference types. Cast null to specific type to resolve.
- **Autoboxing**: Primitive and wrapper types (int vs Integer) create different overloads. Be aware of autoboxing/unboxing behavior.

## Interview-Ready Answer

"Method overloading allows multiple methods with the same name but different parameter lists in the same class. The compiler selects the appropriate method based on the number, types, and order of arguments at compile time. I'd create a Calculator with overloaded add() methods accepting different parameter counts and types. Return type alone doesn't enable overloading. Java performs automatic type promotion to find the best match. Time complexity is O(1) for method resolution and most operations."
