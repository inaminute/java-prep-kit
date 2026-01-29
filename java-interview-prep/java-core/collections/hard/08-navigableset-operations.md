# NavigableSet Advanced Operations

## Problem Statement

Demonstrate advanced NavigableSet operations including range views, descending operations, and ceiling/floor methods. Show practical use cases and compare with SortedSet.

**Requirements**:
- Demonstrate navigation methods
- Show range view operations
- Implement practical use cases
- Compare with SortedSet

## Approach

- NavigableSet extends SortedSet with navigation methods
- lower/floor/ceiling/higher for finding closest elements
- descendingSet() for reverse view
- subSet/headSet/tailSet with inclusive/exclusive bounds
- pollFirst/pollLast for retrieval and removal

## Solution

```java
import java.util.*;

public class NavigableSetOperations {
    
    public static void main(String[] args) {
        demonstrateNavigationMethods();
        demonstrateRangeViews();
        demonstratePracticalUseCases();
    }
    
    public static void demonstrateNavigationMethods() {
        System.out.println("=== Navigation Methods ===");
        
        NavigableSet<Integer> set = new TreeSet<>(Arrays.asList(1, 3, 5, 7, 9, 11, 13));
        
        System.out.println("Set: " + set);
        System.out.println("lower(7): " + set.lower(7));     // 5
        System.out.println("floor(7): " + set.floor(7));     // 7
        System.out.println("ceiling(7): " + set.ceiling(7)); // 7
        System.out.println("higher(7): " + set.higher(7));   // 9
        
        System.out.println("\nFor non-existent element 6:");
        System.out.println("lower(6): " + set.lower(6));     // 5
        System.out.println("floor(6): " + set.floor(6));     // 5
        System.out.println("ceiling(6): " + set.ceiling(6)); // 7
        System.out.println("higher(6): " + set.higher(6));   // 7
        
        // Poll operations
        System.out.println("\npollFirst: " + set.pollFirst()); // 1
        System.out.println("pollLast: " + set.pollLast());     // 13
        System.out.println("After polls: " + set);
    }
    
    public static void demonstrateRangeViews() {
        System.out.println("\n=== Range Views ===");
        
        NavigableSet<Integer> set = new TreeSet<>();
        for (int i = 0; i <= 100; i += 10) {
            set.add(i);
        }
        
        System.out.println("Full set: " + set);
        
        // Inclusive/exclusive bounds
        NavigableSet<Integer> subset = set.subSet(20, true, 70, false);
        System.out.println("subSet(20, true, 70, false): " + subset);
        
        // Descending view
        NavigableSet<Integer> descending = set.descendingSet();
        System.out.println("descendingSet: " + descending);
        
        // Head and tail sets
        System.out.println("headSet(50, true): " + set.headSet(50, true));
        System.out.println("tailSet(50, false): " + set.tailSet(50, false));
    }
    
    public static void demonstratePracticalUseCases() {
        System.out.println("\n=== Practical Use Cases ===");
        
        // Use case: Time-based event scheduling
        NavigableSet<Event> events = new TreeSet<>();
        events.add(new Event(10, "Meeting"));
        events.add(new Event(15, "Call"));
        events.add(new Event(20, "Lunch"));
        events.add(new Event(30, "Review"));
        
        System.out.println("All events: " + events);
        
        // Find next event after time 12
        Event nextEvent = events.ceiling(new Event(12, ""));
        System.out.println("Next event after 12: " + nextEvent);
        
        // Find events in time range [15, 25]
        NavigableSet<Event> rangeEvents = events.subSet(
            new Event(15, ""), true,
            new Event(25, ""), true
        );
        System.out.println("Events between 15-25: " + rangeEvents);
    }
    
    static class Event implements Comparable<Event> {
        int time;
        String name;
        
        Event(int time, String name) {
            this.time = time;
            this.name = name;
        }
        
        @Override
        public int compareTo(Event other) {
            return Integer.compare(this.time, other.time);
        }
        
        @Override
        public String toString() {
            return time + ":" + name;
        }
    }
}
```

## Complexity Analysis

All NavigableSet operations: O(log n)

## Edge Cases and Pitfalls

- **Null Returns**: Navigation methods return null if no match
- **Range Views**: Are backed by original set
- **Inclusive/Exclusive**: Be careful with bounds
- **Descending View**: Modifications affect original set

## Interview-Ready Answer

"NavigableSet extends SortedSet with navigation methods like lower, floor, ceiling, higher for finding closest elements in O(log n). Provides range views with inclusive/exclusive bounds via subSet, headSet, tailSet. descendingSet() returns reverse view. pollFirst/pollLast retrieve and remove. Useful for time-based scheduling, range queries, and finding nearest neighbors in sorted data."
