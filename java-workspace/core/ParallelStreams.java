package core;

import java.util.*;

public class ParallelStreams {
    public void initialize() {
        System.out.println("Parallel Streams Initialized");
        demonstrateParallelCreation();
    }

    private void demonstrateParallelCreation() {
        System.out.println("=== Parallel Stream Creation ===");

        // Create a parallel stream from a collection
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // Method 1: parallel()
        numbers.stream().parallel().forEach(System.out::println);
        System.out.println("---");
        // Method 2: parallelStream()
        numbers.parallelStream().forEach(System.out::println);
    }
}
