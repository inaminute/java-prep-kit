# EnumSet and EnumMap

## Problem Statement

Explain the specialized enum collections EnumSet and EnumMap. Demonstrate their bit vector implementation, performance advantages, and appropriate use cases compared to regular Set and Map implementations.

**Requirements**:
- Explain bit vector implementation for EnumSet
- Demonstrate EnumMap array-based implementation
- Show performance benefits
- Compare with HashSet and HashMap

## Approach

- EnumSet uses bit vector internally (very compact and fast)
- EnumMap uses array indexed by enum ordinal
- Both are type-safe and optimized for enum types
- Much faster and more memory-efficient than HashSet/HashMap for enums
- EnumSet has no public constructor; use factory methods
- Both maintain natural enum order

## Solution

```java
import java.util.*;

public class EnumSetEnumMap {
    
    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
    
    enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static void main(String[] args) {
        demonstrateEnumSet();
        demonstrateEnumMap();
        comparePerformance();
    }
    
    public static void demonstrateEnumSet() {
        System.out.println("=== EnumSet Operations ===");
        
        // Factory methods
        EnumSet<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
        System.out.println("Weekdays: " + weekdays);
        
        EnumSet<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
        System.out.println("Weekend: " + weekend);
        
        EnumSet<Day> allDays = EnumSet.allOf(Day.class);
        System.out.println("All days: " + allDays);
        
        EnumSet<Day> noDays = EnumSet.noneOf(Day.class);
        System.out.println("No days: " + noDays);
        
        // Complement
        EnumSet<Day> notWeekend = EnumSet.complementOf(weekend);
        System.out.println("Not weekend: " + notWeekend);
        
        // Set operations
        EnumSet<Day> set1 = EnumSet.of(Day.MONDAY, Day.WEDNESDAY, Day.FRIDAY);
        EnumSet<Day> set2 = EnumSet.of(Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY);
        
        EnumSet<Day> union = EnumSet.copyOf(set1);
        union.addAll(set2);
        System.out.println("\nUnion: " + union);
        
        EnumSet<Day> intersection = EnumSet.copyOf(set1);
        intersection.retainAll(set2);
        System.out.println("Intersection: " + intersection);
        
        // Maintains enum order
        EnumSet<Day> unordered = EnumSet.of(Day.FRIDAY, Day.MONDAY, Day.WEDNESDAY);
        System.out.println("Maintains order: " + unordered);
    }
    
    public static void demonstrateEnumMap() {
        System.out.println("\n=== EnumMap Operations ===");
        
        EnumMap<Priority, String> tasks = new EnumMap<>(Priority.class);
        
        tasks.put(Priority.HIGH, "Fix critical bug");
        tasks.put(Priority.MEDIUM, "Code review");
        tasks.put(Priority.LOW, "Update documentation");
        tasks.put(Priority.CRITICAL, "Server down!");
        
        System.out.println("Tasks: " + tasks);
        
        // Iteration in enum order
        System.out.println("\nTasks by priority:");
        for (Map.Entry<Priority, String> entry : tasks.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        // Get or default
        String task = tasks.getOrDefault(Priority.LOW, "No task");
        System.out.println("\nLow priority task: " + task);
        
        // Clone
        EnumMap<Priority, String> clone = tasks.clone();
        System.out.println("Clone: " + clone);
    }
    
    public static void comparePerformance() {
        System.out.println("\n=== Performance Comparison ===");
        
        int iterations = 1000000;
        
        // EnumSet vs HashSet
        long start = System.nanoTime();
        EnumSet<Day> enumSet = EnumSet.noneOf(Day.class);
        for (int i = 0; i < iterations; i++) {
            enumSet.add(Day.MONDAY);
            enumSet.contains(Day.MONDAY);
            enumSet.remove(Day.MONDAY);
        }
        long enumSetTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        Set<Day> hashSet = new HashSet<>();
        for (int i = 0; i < iterations; i++) {
            hashSet.add(Day.MONDAY);
            hashSet.contains(Day.MONDAY);
            hashSet.remove(Day.MONDAY);
        }
        long hashSetTime = System.nanoTime() - start;
        
        System.out.println("EnumSet time: " + enumSetTime + "ns");
        System.out.println("HashSet time: " + hashSetTime + "ns");
        System.out.println("EnumSet is " + (hashSetTime / enumSetTime) + "x faster");
        
        // EnumMap vs HashMap
        start = System.nanoTime();
        EnumMap<Priority, String> enumMap = new EnumMap<>(Priority.class);
        for (int i = 0; i < iterations; i++) {
            enumMap.put(Priority.HIGH, "Task");
            enumMap.get(Priority.HIGH);
            enumMap.remove(Priority.HIGH);
        }
        long enumMapTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        Map<Priority, String> hashMap = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            hashMap.put(Priority.HIGH, "Task");
            hashMap.get(Priority.HIGH);
            hashMap.remove(Priority.HIGH);
        }
        long hashMapTime = System.nanoTime() - start;
        
        System.out.println("\nEnumMap time: " + enumMapTime + "ns");
        System.out.println("HashMap time: " + hashMapTime + "ns");
        System.out.println("EnumMap is " + (hashMapTime / enumMapTime) + "x faster");
        
        // Memory comparison
        System.out.println("\nMemory efficiency:");
        System.out.println("EnumSet: Bit vector (1 bit per enum constant)");
        System.out.println("EnumMap: Array (1 slot per enum constant)");
        System.out.println("Both are much more compact than hash-based collections");
    }
}
```

## Complexity Analysis

**EnumSet**:
- **add/remove/contains**: O(1) - bit operations
- **Space**: O(1) - single long or long[] for bit vector

**EnumMap**:
- **put/get/remove**: O(1) - array access by ordinal
- **Space**: O(n) where n is number of enum constants

## Edge Cases and Pitfalls

- **Type Safety**: Both require enum type at creation
- **Null Not Allowed**: EnumSet doesn't allow null elements
- **EnumMap Null Values**: Allows null values but not null keys
- **Ordering**: Both maintain natural enum order (declaration order)
- **Performance**: Significantly faster than HashSet/HashMap for enums
- **Memory**: Much more compact than hash-based collections
- **When to Use**: Always use for enum collections instead of HashSet/HashMap
- **Thread Safety**: Not thread-safe; synchronize externally if needed

## Interview-Ready Answer

"EnumSet uses a bit vector implementation where each enum constant is represented by a single bit, making it extremely fast and memory-efficient. EnumMap uses an array indexed by enum ordinal values. Both maintain natural enum order and provide O(1) operations. They're significantly faster and more compact than HashSet and HashMap for enum types. Always use EnumSet and EnumMap instead of regular collections when working with enums."
