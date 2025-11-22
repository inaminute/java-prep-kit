# String Immutability

## Problem Statement

Explain why Strings are immutable in Java and the benefits of this design. Demonstrate the implications of immutability and when to use StringBuilder or StringBuffer instead. Show how string pooling works.

**Requirements**:
- Explain string immutability and its benefits
- Demonstrate string pooling
- Compare String, StringBuilder, and StringBuffer
- Show performance implications

## Approach

- Strings are immutable - once created, their content cannot be changed
- Benefits include thread safety, security, caching, and string pooling
- String pool stores unique string literals for memory efficiency
- Use StringBuilder for single-threaded string manipulation
- Use StringBuffer for thread-safe string manipulation
- Avoid string concatenation in loops

## Solution

```java
public class StringImmutability {
    
    public static void main(String[] args) {
        demonstrateImmutability();
        demonstrateStringPool();
        demonstrateStringBuilderVsString();
        demonstrateStringBufferVsStringBuilder();
    }
    
    public static void demonstrateImmutability() {
        System.out.println("=== String Immutability ===");
        
        String str1 = "Hello";
        String str2 = str1;
        
        str1 = str1 + " World"; // Creates new string, doesn't modify original
        
        System.out.println("str1: " + str1); // Hello World
        System.out.println("str2: " + str2); // Hello (unchanged)
        
        // Attempting to modify via reflection (not recommended)
        String original = "Immutable";
        System.out.println("Original: " + original);
        // Even with reflection, it's complex and breaks immutability contract
    }
    
    public static void demonstrateStringPool() {
        System.out.println("\n=== String Pool ===");
        
        // String literals go to pool
        String s1 = "Hello";
        String s2 = "Hello";
        System.out.println("s1 == s2: " + (s1 == s2)); // true (same reference)
        
        // new String() creates object in heap
        String s3 = new String("Hello");
        System.out.println("s1 == s3: " + (s1 == s3)); // false (different reference)
        System.out.println("s1.equals(s3): " + s1.equals(s3)); // true (same content)
        
        // intern() moves to pool
        String s4 = s3.intern();
        System.out.println("s1 == s4: " + (s1 == s4)); // true (same reference)
    }
    
    public static void demonstrateStringBuilderVsString() {
        System.out.println("\n=== String vs StringBuilder Performance ===");
        
        int iterations = 10000;
        
        // String concatenation - creates many objects
        long startTime = System.nanoTime();
        String result = "";
        for (int i = 0; i < iterations; i++) {
            result += i; // Creates new String object each time
        }
        long stringTime = System.nanoTime() - startTime;
        
        // StringBuilder - mutable, efficient
        startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append(i); // Modifies same object
        }
        String sbResult = sb.toString();
        long sbTime = System.nanoTime() - startTime;
        
        System.out.println("String concatenation: " + stringTime + "ns");
        System.out.println("StringBuilder: " + sbTime + "ns");
        System.out.println("StringBuilder is " + (stringTime / sbTime) + "x faster");
    }
    
    public static void demonstrateStringBufferVsStringBuilder() {
        System.out.println("\n=== StringBuilder vs StringBuffer ===");
        
        // StringBuilder - not thread-safe, faster
        StringBuilder builder = new StringBuilder();
        builder.append("Hello");
        builder.append(" ");
        builder.append("World");
        System.out.println("StringBuilder: " + builder.toString());
        
        // StringBuffer - thread-safe, slower
        StringBuffer buffer = new StringBuffer();
        buffer.append("Hello");
        buffer.append(" ");
        buffer.append("World");
        System.out.println("StringBuffer: " + buffer.toString());
    }
    
    // Example: Why immutability matters for security
    static class UserAuthentication {
        private String password;
        
        public UserAuthentication(String password) {
            this.password = password;
        }
        
        public boolean authenticate(String input) {
            return password.equals(input);
        }
        
        // If String were mutable, someone could modify the password
        // after passing it to the constructor
    }
}
```

## Complexity Analysis

**String Concatenation in Loop**:
- **Time Complexity**: O(n²) - creates new string each time
- **Space Complexity**: O(n²) - many intermediate strings

**StringBuilder**:
- **Time Complexity**: O(n) for n append operations
- **Space Complexity**: O(n)

## Edge Cases and Pitfalls

- **String concatenation in loops**: Creates many intermediate objects, use StringBuilder instead
- **String pool memory**: Too many unique strings can fill the pool
- **== vs equals()**: Use equals() for content comparison, == compares references
- **StringBuilder vs StringBuffer**: Use StringBuilder unless thread safety is needed

## Interview-Ready Answer

"Strings are immutable in Java - once created, their content cannot change. Benefits include thread safety, security, caching, and string pooling for memory efficiency. String literals are stored in the string pool. For string manipulation, use StringBuilder (single-threaded) or StringBuffer (thread-safe) to avoid creating many intermediate objects. Never concatenate strings in loops."
