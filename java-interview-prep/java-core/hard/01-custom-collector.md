# Custom Collector Implementation

## Problem Statement

Implement a custom Collector from scratch. Demonstrate how to create collectors for specific aggregation needs that aren't covered by built-in collectors. Explain the Collector interface and its methods.

**Requirements**:
- Implement Collector interface
- Explain supplier, accumulator, combiner, and finisher
- Show characteristics (CONCURRENT, UNORDERED, IDENTITY_FINISH)
- Create practical custom collector

## Approach

- Collector has 5 methods: supplier, accumulator, combiner, finisher, characteristics
- Supplier creates result container
- Accumulator adds element to container
- Combiner merges two containers (for parallel streams)
- Finisher transforms container to final result
- Characteristics optimize collection process

## Solution

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class CustomCollector {
    
    // Custom collector to collect into immutable list
    static class ImmutableListCollector<T> implements Collector<T, List<T>, List<T>> {
        
        @Override
        public Supplier<List<T>> supplier() {
            return ArrayList::new;
        }
        
        @Override
        public BiConsumer<List<T>, T> accumulator() {
            return List::add;
        }
        
        @Override
        public BinaryOperator<List<T>> combiner() {
            return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
            };
        }
        
        @Override
        public Function<List<T>, List<T>> finisher() {
            return Collections::unmodifiableList;
        }
        
        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
    
    // Custom collector for statistics
    static class Stats {
        long count = 0;
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        void accept(double value) {
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        Stats combine(Stats other) {
            count += other.count;
            sum += other.sum;
            min = Math.min(min, other.min);
            max = Math.max(max, other.max);
            return this;
        }
        
        double getAverage() {
            return count > 0 ? sum / count : 0;
        }
    }
    
    static Collector<Double, Stats, Stats> customStats() {
        return Collector.of(
            Stats::new,
            Stats::accept,
            Stats::combine,
            Collector.Characteristics.IDENTITY_FINISH
        );
    }
    
    public static void main(String[] args) {
        demonstrateImmutableListCollector();
        demonstrateCustomStatsCollector();
    }
    
    public static void demonstrateImmutableListCollector() {
        System.out.println("=== Immutable List Collector ===");
        
        List<String> result = Stream.of("a", "b", "c")
            .collect(new ImmutableListCollector<>());
        
        System.out.println("Result: " + result);
        
        try {
            result.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("List is immutable!");
        }
    }
    
    public static void demonstrateCustomStatsCollector() {
        System.out.println("\n=== Custom Stats Collector ===");
        
        Stats stats = Stream.of(1.0, 2.0, 3.0, 4.0, 5.0)
            .collect(customStats());
        
        System.out.println("Count: " + stats.count);
        System.out.println("Sum: " + stats.sum);
        System.out.println("Average: " + stats.getAverage());
        System.out.println("Min: " + stats.min);
        System.out.println("Max: " + stats.max);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) for collection process

**Space Complexity**: O(n) for result container

## Edge Cases and Pitfalls

- **Thread safety**: Combiner must handle parallel streams correctly
- **Characteristics**: Wrong characteristics can cause incorrect results
- **Finisher**: IDENTITY_FINISH avoids unnecessary transformation

## Interview-Ready Answer

"Custom Collector implements five methods: supplier creates container, accumulator adds elements, combiner merges containers for parallel streams, finisher transforms to final result, and characteristics optimize collection. Use Collector.of() factory method for simpler implementation. Characteristics like CONCURRENT and IDENTITY_FINISH affect how collector is used."
