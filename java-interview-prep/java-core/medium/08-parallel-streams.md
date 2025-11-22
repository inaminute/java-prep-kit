# Parallel Streams

## Problem Statement

Explain parallel streams in Java. Demonstrate when to use them for performance improvement and when to avoid them. Show the difference between sequential and parallel stream processing.

**Requirements**:
- Demonstrate parallel stream creation
- Compare performance with sequential streams
- Explain when parallel streams help
- Show pitfalls and thread safety issues

## Approach

- Parallel streams use ForkJoinPool for parallel processing
- Use for CPU-intensive operations on large datasets
- Avoid for I/O operations or small datasets
- Ensure operations are stateless and thread-safe
- Overhead of parallelization may outweigh benefits for small data

## Solution

```java
import java.util.*;
import java.util.stream.*;

public class ParallelStreams {
    
    public static void main(String[] args) {
        demonstrateParallelCreation();
        demonstratePerformanceComparison();
        demonstrateThreadSafety();
    }
    
    public static void demonstrateParallelCreation() {
        System.out.println("=== Creating Parallel Streams ===");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        
        // Method 1: parallel()
        numbers.stream().parallel().forEach(System.out::println);
        
        // Method 2: parallelStream()
        numbers.parallelStream().forEach(System.out::println);
    }
    
    public static void demonstratePerformanceComparison() {
        System.out.println("\n=== Performance Comparison ===");
        
        List<Integer> numbers = IntStream.rangeClosed(1, 1000000)
            .boxed()
            .collect(Collectors.toList());
        
        // Sequential
        long start = System.currentTimeMillis();
        long sum1 = numbers.stream()
            .mapToLong(i -> i * i)
            .sum();
        long seqTime = System.currentTimeMillis() - start;
        
        // Parallel
        start = System.currentTimeMillis();
        long sum2 = numbers.parallelStream()
            .mapToLong(i -> i * i)
            .sum();
        long parTime = System.currentTimeMillis() - start;
        
        System.out.println("Sequential: " + seqTime + "ms");
        System.out.println("Parallel: " + parTime + "ms");
    }
    
    public static void demonstrateThreadSafety() {
        System.out.println("\n=== Thread Safety ===");
        
        List<Integer> numbers = IntStream.rangeClosed(1, 100)
            .boxed()
            .collect(Collectors.toList());
        
        // UNSAFE: Using non-thread-safe collection
        List<Integer> unsafeList = new ArrayList<>();
        numbers.parallelStream().forEach(unsafeList::add);
        System.out.println("Unsafe size (may be wrong): " + unsafeList.size());
        
        // SAFE: Using collect
        List<Integer> safeList = numbers.parallelStream()
            .collect(Collectors.toList());
        System.out.println("Safe size: " + safeList.size());
    }
}
```

## Complexity Analysis

**Time Complexity**: Ideally O(n/p) where p is number of processors, but overhead exists

**Space Complexity**: O(n) plus overhead for thread management

## Edge Cases and Pitfalls

- **Small datasets**: Overhead may exceed benefits
- **I/O operations**: Don't parallelize I/O-bound operations
- **Stateful operations**: Avoid shared mutable state
- **Order**: Parallel streams may not preserve encounter order

## Interview-Ready Answer

"Parallel streams use ForkJoinPool for concurrent processing. Use for CPU-intensive operations on large datasets. Avoid for I/O operations, small datasets, or when order matters. Ensure operations are stateless and thread-safe. Use collect() instead of forEach() with mutable collections. Overhead of parallelization may outweigh benefits for small data."
