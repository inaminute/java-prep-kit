# Collections Utility Class Methods

## Problem Statement

Demonstrate the utility methods provided by the Collections class in Java. Show sorting, searching, shuffling, reversing, and creating unmodifiable/synchronized wrappers for collections.

**Requirements**:
- Demonstrate sorting and searching methods
- Show collection manipulation (reverse, shuffle, rotate, swap)
- Explain unmodifiable and synchronized wrappers
- Show singleton and empty collection methods

## Approach

- Collections class provides static utility methods for collection operations
- Sorting: sort(), reverseOrder(), binarySearch()
- Manipulation: reverse(), shuffle(), rotate(), swap(), fill(), copy()
- Wrappers: unmodifiableXxx(), synchronizedXxx()
- Factory methods: singleton(), singletonList(), emptyList()
- All methods work on List interface (except some on Collection)

## Solution

```java
import java.util.*;

public class CollectionsUtility {
    
    public static void main(String[] args) {
        demonstrateSorting();
        demonstrateSearching();
        demonstrateManipulation();
        demonstrateUnmodifiable();
        demonstrateSynchronized();
        demonstrateFactoryMethods();
    }
    
    public static void demonstrateSorting() {
        System.out.println("=== Sorting ===");
        List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3));
        
        System.out.println("Original: " + numbers);
        
        // Sort in natural order
        Collections.sort(numbers);
        System.out.println("Sorted: " + numbers);
        
        // Sort in reverse order
        Collections.sort(numbers, Collections.reverseOrder());
        System.out.println("Reverse sorted: " + numbers);
        
        // Custom comparator
        List<String> words = new ArrayList<>(Arrays.asList("apple", "pie", "a", "banana"));
        Collections.sort(words, Comparator.comparingInt(String::length));
        System.out.println("Sorted by length: " + words);
        
        // Reverse a sorted list
        Collections.reverse(numbers);
        System.out.println("Reversed: " + numbers);
    }
    
    public static void demonstrateSearching() {
        System.out.println("\n=== Searching ===");
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        
        // Binary search (list must be sorted)
        int index = Collections.binarySearch(numbers, 7);
        System.out.println("Index of 7: " + index);
        
        index = Collections.binarySearch(numbers, 6);
        System.out.println("Index of 6 (not found): " + index); // Negative value
        
        // Find min and max
        System.out.println("Min: " + Collections.min(numbers));
        System.out.println("Max: " + Collections.max(numbers));
        
        // Frequency
        List<String> letters = Arrays.asList("a", "b", "a", "c", "a", "b");
        System.out.println("Frequency of 'a': " + Collections.frequency(letters, "a"));
    }
    
    public static void demonstrateManipulation() {
        System.out.println("\n=== Manipulation ===");
        
        // Shuffle
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        System.out.println("Original: " + numbers);
        Collections.shuffle(numbers);
        System.out.println("Shuffled: " + numbers);
        
        // Rotate
        numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.rotate(numbers, 2);
        System.out.println("Rotated by 2: " + numbers);
        
        // Swap
        Collections.swap(numbers, 0, 4);
        System.out.println("After swap(0, 4): " + numbers);
        
        // Fill
        Collections.fill(numbers, 0);
        System.out.println("Filled with 0: " + numbers);
        
        // Replace all
        List<String> words = new ArrayList<>(Arrays.asList("a", "b", "a", "c"));
        Collections.replaceAll(words, "a", "X");
        System.out.println("Replace 'a' with 'X': " + words);
        
        // Copy
        List<Integer> source = Arrays.asList(10, 20, 30);
        List<Integer> dest = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.copy(dest, source);
        System.out.println("After copy: " + dest);
    }
    
    public static void demonstrateUnmodifiable() {
        System.out.println("\n=== Unmodifiable Collections ===");
        
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> unmodifiableList = Collections.unmodifiableList(list);
        
        System.out.println("Unmodifiable list: " + unmodifiableList);
        
        try {
            unmodifiableList.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify unmodifiable list");
        }
        
        // Original list can still be modified
        list.add("D");
        System.out.println("After modifying original: " + unmodifiableList);
        
        // Other unmodifiable wrappers
        Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> unmodifiableSet = Collections.unmodifiableSet(set);
        
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        Map<String, Integer> unmodifiableMap = Collections.unmodifiableMap(map);
        
        System.out.println("Unmodifiable set: " + unmodifiableSet);
        System.out.println("Unmodifiable map: " + unmodifiableMap);
    }
    
    public static void demonstrateSynchronized() {
        System.out.println("\n=== Synchronized Collections ===");
        
        List<Integer> list = new ArrayList<>();
        List<Integer> syncList = Collections.synchronizedList(list);
        
        // Thread-safe operations
        syncList.add(1);
        syncList.add(2);
        
        // Must synchronize manually for iteration
        synchronized(syncList) {
            for (Integer num : syncList) {
                System.out.println("Element: " + num);
            }
        }
        
        // Other synchronized wrappers
        Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());
        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        
        System.out.println("Note: Prefer ConcurrentHashMap over synchronizedMap");
    }
    
    public static void demonstrateFactoryMethods() {
        System.out.println("\n=== Factory Methods ===");
        
        // Singleton collections (immutable, single element)
        Set<String> singleton = Collections.singleton("Only");
        List<String> singletonList = Collections.singletonList("Only");
        Map<String, Integer> singletonMap = Collections.singletonMap("Key", 1);
        
        System.out.println("Singleton set: " + singleton);
        System.out.println("Singleton list: " + singletonList);
        System.out.println("Singleton map: " + singletonMap);
        
        // Empty collections (immutable)
        List<String> emptyList = Collections.emptyList();
        Set<String> emptySet = Collections.emptySet();
        Map<String, Integer> emptyMap = Collections.emptyMap();
        
        System.out.println("Empty list: " + emptyList);
        
        // nCopies - immutable list with n copies of element
        List<String> copies = Collections.nCopies(5, "X");
        System.out.println("5 copies of 'X': " + copies);
        
        // Checked collections (runtime type checking)
        List<String> checkedList = Collections.checkedList(
            new ArrayList<>(), String.class
        );
        checkedList.add("Valid");
        System.out.println("Checked list: " + checkedList);
    }
}
```

## Complexity Analysis

**Time Complexity**:
- **sort()**: O(n log n)
- **binarySearch()**: O(log n) for RandomAccess lists, O(n) otherwise
- **reverse()**: O(n)
- **shuffle()**: O(n)
- **min/max()**: O(n)
- **frequency()**: O(n)

**Space Complexity**: Most operations are O(1) additional space

## Edge Cases and Pitfalls

- **binarySearch()**: List must be sorted; returns negative value if not found
- **Unmodifiable vs Immutable**: Unmodifiable collections can change if backing collection changes
- **Synchronized Iteration**: Must manually synchronize when iterating synchronized collections
- **Empty Collections**: Use Collections.emptyList() instead of new ArrayList<>() for empty returns
- **Singleton**: Immutable single-element collection, useful for method parameters
- **Copy**: Destination list must be at least as long as source list
- **Thread Safety**: Synchronized wrappers are slower than concurrent collections
- **Type Safety**: Checked collections provide runtime type checking

## Interview-Ready Answer

"Collections class provides static utility methods for common collection operations. Key methods include sort() for O(n log n) sorting, binarySearch() for O(log n) searching in sorted lists, and manipulation methods like reverse(), shuffle(), and rotate(). It provides unmodifiable wrappers for read-only views and synchronized wrappers for thread safety, though concurrent collections are preferred. Factory methods like emptyList() and singleton() create immutable collections efficiently."
