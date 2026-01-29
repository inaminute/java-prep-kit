# ArrayList Internals and Dynamic Resizing

## Problem Statement

Explain how ArrayList works internally in Java, focusing on its dynamic array implementation and resizing mechanism. Demonstrate the growth strategy and analyze the performance implications of adding elements.

**Requirements**:
- Explain the internal array structure
- Demonstrate dynamic resizing behavior
- Show capacity vs size difference
- Analyze amortized time complexity

## Approach

- ArrayList uses an internal Object array to store elements
- Initial default capacity is 10 (when using no-arg constructor)
- When capacity is exceeded, array grows by 50% (newCapacity = oldCapacity + oldCapacity >> 1)
- Growing involves creating a new array and copying all elements
- ensureCapacity() can be used to minimize resizing operations
- Size tracks actual elements, capacity tracks array length

## Solution

```java
import java.util.*;
import java.lang.reflect.*;

public class ArrayListInternals {
    
    public static void main(String[] args) {
        demonstrateGrowth();
        demonstrateCapacityManagement();
        demonstratePerformance();
    }
    
    public static void demonstrateGrowth() {
        System.out.println("=== ArrayList Growth Demonstration ===");
        ArrayList<Integer> list = new ArrayList<>();
        
        System.out.println("Initial capacity: " + getCapacity(list));
        
        // Add elements and observe capacity changes
        for (int i = 0; i < 15; i++) {
            list.add(i);
            System.out.println("Size: " + list.size() + 
                             ", Capacity: " + getCapacity(list));
        }
    }
    
    public static void demonstrateCapacityManagement() {
        System.out.println("\n=== Capacity Management ===");
        
        // Without pre-sizing
        ArrayList<Integer> list1 = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            list1.add(i);
        }
        long time1 = System.nanoTime() - start;
        
        // With pre-sizing
        ArrayList<Integer> list2 = new ArrayList<>(100000);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            list2.add(i);
        }
        long time2 = System.nanoTime() - start;
        
        System.out.println("Without pre-sizing: " + time1 + "ns");
        System.out.println("With pre-sizing: " + time2 + "ns");
        System.out.println("Improvement: " + ((time1 - time2) * 100.0 / time1) + "%");
    }
    
    public static void demonstratePerformance() {
        System.out.println("\n=== Performance Analysis ===");
        ArrayList<Integer> list = new ArrayList<>();
        
        // Add at end - O(1) amortized
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        System.out.println("Add at end (10000 elements): " + 
                         (System.nanoTime() - start) + "ns");
        
        // Add at beginning - O(n)
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            list.add(0, i);
        }
        System.out.println("Add at beginning (1000 elements): " + 
                         (System.nanoTime() - start) + "ns");
        
        // Random access - O(1)
        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            int val = list.get(i);
        }
        System.out.println("Random access (10000 gets): " + 
                         (System.nanoTime() - start) + "ns");
    }
    
    // Helper method to get capacity using reflection
    private static int getCapacity(ArrayList<?> list) {
        try {
            Field field = ArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] elementData = (Object[]) field.get(list);
            return elementData.length;
        } catch (Exception e) {
            return -1;
        }
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **add(E e)**: O(1) amortized (O(n) when resizing occurs)
- **add(int index, E e)**: O(n) due to shifting elements
- **get(int index)**: O(1) direct array access
- **remove(int index)**: O(n) due to shifting elements
- **contains(Object o)**: O(n) linear search

**Space Complexity**: O(n) where n is the number of elements

**Growth Analysis**:
- Each resize operation is O(n) for copying
- Resizing happens at capacities: 10, 15, 22, 33, 49, 73, 109...
- Amortized cost per insertion is O(1) because resizing is infrequent

## Edge Cases and Pitfalls

- **Initial Capacity**: Default capacity is 10, but first element added to empty list (created with no-arg constructor) triggers capacity allocation
- **Growth Factor**: 1.5x growth (50% increase) balances memory usage and resize frequency
- **Memory Waste**: After many removals, capacity remains high; use trimToSize() to reclaim memory
- **Concurrent Modification**: Not thread-safe; use Collections.synchronizedList() or CopyOnWriteArrayList
- **Null Elements**: ArrayList allows null elements, unlike some other collections
- **Index Bounds**: Always check bounds before accessing; IndexOutOfBoundsException is unchecked
- **Capacity vs Size**: Capacity is internal array length, size is number of actual elements

## Interview-Ready Answer

"ArrayList uses a dynamic array internally with a default initial capacity of 10. When capacity is exceeded, it grows by 50% (1.5x), creating a new array and copying elements. This gives O(1) amortized time for add operations at the end. Random access is O(1) due to array indexing, but insertions/deletions in the middle are O(n) due to element shifting. Pre-sizing with ensureCapacity() can improve performance when the final size is known."
