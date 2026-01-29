# Collection View Patterns

## Problem Statement

Explain collection views in Java Collections Framework, including keySet(), values(), entrySet() for maps, and subList() for lists. Demonstrate how modifications to views affect backing collections and vice versa.

**Requirements**:
- Explain backed vs independent collections
- Demonstrate view modification propagation
- Show unmodifiable vs immutable collections
- Explain use cases for views

## Approach

- Views are backed by original collection
- Modifications to view affect original and vice versa
- keySet(), values(), entrySet() are views of Map
- subList(), headSet(), tailSet() are views
- Unmodifiable wrappers are views, not copies
- Views provide different perspectives on same data

## Solution

```java
import java.util.*;

public class CollectionViews {
    
    public static void main(String[] args) {
        demonstrateMapViews();
        demonstrateListViews();
        demonstrateUnmodifiableViews();
    }
    
    public static void demonstrateMapViews() {
        System.out.println("=== Map Views ===");
        
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        
        // keySet view
        Set<String> keys = map.keySet();
        System.out.println("Keys: " + keys);
        
        // Remove from view affects map
        keys.remove("B");
        System.out.println("After removing 'B' from keySet:");
        System.out.println("Map: " + map);
        
        // values view
        Collection<Integer> values = map.values();
        System.out.println("Values: " + values);
        
        // Modify map affects view
        map.put("D", 4);
        System.out.println("After adding to map:");
        System.out.println("Values: " + values);
        
        // entrySet view
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            if (entry.getKey().equals("C")) {
                entry.setValue(30); // Modify through view
            }
        }
        System.out.println("After modifying through entrySet:");
        System.out.println("Map: " + map);
    }
    
    public static void demonstrateListViews() {
        System.out.println("\n=== List Views ===");
        
        List<Integer> list = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        
        // subList view
        List<Integer> subList = list.subList(2, 7);
        System.out.println("Original: " + list);
        System.out.println("subList(2, 7): " + subList);
        
        // Modify subList affects original
        subList.set(0, 20);
        System.out.println("After subList.set(0, 20):");
        System.out.println("Original: " + list);
        
        // Modify original affects subList
        list.set(3, 30);
        System.out.println("After list.set(3, 30):");
        System.out.println("subList: " + subList);
        
        // Clear subList
        subList.clear();
        System.out.println("After subList.clear():");
        System.out.println("Original: " + list);
    }
    
    public static void demonstrateUnmodifiableViews() {
        System.out.println("\n=== Unmodifiable Views ===");
        
        List<String> original = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> unmodifiable = Collections.unmodifiableList(original);
        
        System.out.println("Unmodifiable: " + unmodifiable);
        
        // Cannot modify through unmodifiable view
        try {
            unmodifiable.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify unmodifiable view");
        }
        
        // But original can be modified
        original.add("D");
        System.out.println("After modifying original:");
        System.out.println("Unmodifiable: " + unmodifiable); // Reflects change!
        
        System.out.println("\nUnmodifiable ≠ Immutable");
        System.out.println("Unmodifiable view can change if backing collection changes");
    }
}
```

## Complexity Analysis

Views have same complexity as underlying collection operations.

## Edge Cases and Pitfalls

- **Backed Collections**: Views reflect changes to original
- **Structural Modifications**: May invalidate views
- **Unmodifiable ≠ Immutable**: Unmodifiable views can change
- **ConcurrentModification**: Modifying backing collection during iteration
- **Use Cases**: Efficient range operations, different perspectives

## Interview-Ready Answer

"Collection views provide different perspectives on the same underlying data. Map's keySet(), values(), and entrySet() are backed views - modifications propagate bidirectionally. List's subList() creates a backed range view. Unmodifiable wrappers are views, not copies, so they reflect changes to the backing collection. Views are memory-efficient as they don't copy data, but structural modifications to backing collection may invalidate them."
