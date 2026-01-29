# Memory-Efficient Collections

## Problem Statement

Explore memory-efficient alternatives to standard Java collections for primitive types and large datasets. Discuss primitive collections libraries, compact data structures, and strategies to reduce memory overhead.

**Requirements**:
- Explain memory overhead of wrapper classes
- Discuss primitive collection alternatives
- Show memory-efficient strategies
- Compare memory usage

## Approach

- Standard collections box primitives (memory overhead)
- Primitive collections libraries: Eclipse Collections, Trove, FastUtil
- Compact structures: BitSet for boolean arrays
- Memory-efficient strategies: use arrays, specialized collections
- Trade-offs: memory vs convenience

## Solution

```java
import java.util.*;

public class MemoryEfficientCollections {
    
    public static void main(String[] args) {
        demonstrateBoxingOverhead();
        demonstrateBitSet();
        demonstrateCompactStrategies();
    }
    
    public static void demonstrateBoxingOverhead() {
        System.out.println("=== Boxing Overhead ===");
        
        // Integer object: 16 bytes (object header + int value + padding)
        // vs int primitive: 4 bytes
        
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i); // Boxing: int -> Integer
        }
        
        System.out.println("ArrayList<Integer> for 1000 elements:");
        System.out.println("- 1000 Integer objects: ~16KB");
        System.out.println("- ArrayList overhead: ~4KB");
        System.out.println("- Total: ~20KB");
        
        System.out.println("\nint[] array for 1000 elements:");
        System.out.println("- 1000 ints: 4KB");
        System.out.println("- Array overhead: ~16 bytes");
        System.out.println("- Total: ~4KB");
        
        System.out.println("\nBoxing overhead: 5x memory usage!");
    }
    
    public static void demonstrateBitSet() {
        System.out.println("\n=== BitSet for Boolean Arrays ===");
        
        // BitSet: 1 bit per boolean
        BitSet bitSet = new BitSet(1000);
        for (int i = 0; i < 1000; i += 2) {
            bitSet.set(i); // Set even indices
        }
        
        System.out.println("BitSet size: " + bitSet.size());
        System.out.println("Cardinality (true count): " + bitSet.cardinality());
        
        // Operations
        System.out.println("Get(10): " + bitSet.get(10));
        System.out.println("Get(11): " + bitSet.get(11));
        
        // BitSet operations
        BitSet other = new BitSet(1000);
        other.set(0, 500);
        
        BitSet and = (BitSet) bitSet.clone();
        and.and(other);
        System.out.println("AND cardinality: " + and.cardinality());
        
        System.out.println("\nBitSet memory: ~125 bytes for 1000 booleans");
        System.out.println("Boolean[] memory: ~1KB for 1000 Booleans");
    }
    
    public static void demonstrateCompactStrategies() {
        System.out.println("\n=== Compact Strategies ===");
        
        // Strategy 1: Use EnumSet for enum flags
        enum Feature { FEATURE_A, FEATURE_B, FEATURE_C }
        EnumSet<Feature> features = EnumSet.of(Feature.FEATURE_A, Feature.FEATURE_C);
        System.out.println("EnumSet uses bit vector (very compact)");
        
        // Strategy 2: Use primitive arrays when possible
        int[] primitiveArray = new int[1000];
        System.out.println("Primitive array: 4KB for 1000 ints");
        
        // Strategy 3: Use ArrayList with initial capacity
        List<String> list = new ArrayList<>(1000); // Avoid resizing
        System.out.println("Pre-sized ArrayList avoids wasted capacity");
        
        // Strategy 4: Use LinkedHashSet only when needed
        System.out.println("\nMemory comparison:");
        System.out.println("HashSet: ~32 bytes per entry");
        System.out.println("LinkedHashSet: ~40 bytes per entry (extra links)");
        System.out.println("TreeSet: ~40 bytes per entry (tree nodes)");
    }
}
```

## Complexity Analysis

Memory overhead varies by collection type and element type.

## Edge Cases and Pitfalls

- **Primitive Collections**: Libraries like Trove, FastUtil, Eclipse Collections
- **BitSet**: Ideal for boolean flags, bit manipulation
- **EnumSet**: Most compact for enum sets
- **Arrays**: Most memory-efficient but fixed size
- **Trade-offs**: Memory efficiency vs API convenience

## Interview-Ready Answer

"Standard Java collections box primitives, adding significant memory overhead (Integer uses 16 bytes vs 4 for int). For memory efficiency: use primitive arrays when possible, BitSet for boolean arrays, EnumSet for enums, and consider primitive collection libraries like Eclipse Collections or FastUtil. Pre-size collections to avoid wasted capacity. Trade-off is between memory efficiency and API convenience of standard collections."
