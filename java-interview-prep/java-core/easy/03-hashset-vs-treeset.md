# HashSet vs TreeSet

## Problem Statement

Compare HashSet and TreeSet in Java. When would you use each? Implement examples demonstrating the key differences in ordering, performance, and use cases.

**Requirements**:
- Explain the internal implementation of each
- Compare time complexity for common operations
- Demonstrate ordering differences
- Show practical use cases for each

## Approach

- HashSet uses HashMap internally, provides O(1) operations but no ordering
- TreeSet uses Red-Black tree (TreeMap internally), provides O(log n) operations with sorted order
- HashSet is faster for basic operations (add, remove, contains)
- TreeSet maintains elements in sorted order and supports range operations
- Choose HashSet for performance, TreeSet when you need sorted data

## Solution

```java
import java.util.*;

public class HashSetVsTreeSet {
    
    public static void main(String[] args) {
        demonstrateOrdering();
        demonstratePerformance();
        demonstrateTreeSetOperations();
    }
    
    public static void demonstrateOrdering() {
        System.out.println("=== Ordering Demonstration ===");
        
        // HashSet - no guaranteed order
        Set<Integer> hashSet = new HashSet<>();
        hashSet.add(5);
        hashSet.add(1);
        hashSet.add(3);
        hashSet.add(2);
        hashSet.add(4);
        
        System.out.print("HashSet: ");
        for (Integer num : hashSet) {
            System.out.print(num + " ");
        }
        System.out.println();
        
        // TreeSet - sorted order
        Set<Integer> treeSet = new TreeSet<>();
        treeSet.add(5);
        treeSet.add(1);
        treeSet.add(3);
        treeSet.add(2);
        treeSet.add(4);
        
        System.out.print("TreeSet: ");
        for (Integer num : treeSet) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
    
    public static void demonstratePerformance() {
        System.out.println("\n=== Performance Comparison ===");
        
        int size = 100000;
        
        // HashSet performance
        Set<Integer> hashSet = new HashSet<>();
        long startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashSet.add(i);
        }
        long hashSetAddTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            hashSet.contains(i);
        }
        long hashSetContainsTime = System.nanoTime() - startTime;
        
        // TreeSet performance
        Set<Integer> treeSet = new TreeSet<>();
        startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeSet.add(i);
        }
        long treeSetAddTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            treeSet.contains(i);
        }
        long treeSetContainsTime = System.nanoTime() - startTime;
        
        System.out.println("HashSet - Add: " + hashSetAddTime + "ns, Contains: " + hashSetContainsTime + "ns");
        System.out.println("TreeSet - Add: " + treeSetAddTime + "ns, Contains: " + treeSetContainsTime + "ns");
    }
    
    public static void demonstrateTreeSetOperations() {
        System.out.println("\n=== TreeSet Special Operations ===");
        
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.addAll(Arrays.asList(10, 20, 30, 40, 50, 60, 70, 80, 90));
        
        System.out.println("First element: " + treeSet.first());
        System.out.println("Last element: " + treeSet.last());
        System.out.println("Lower than 50: " + treeSet.lower(50));
        System.out.println("Higher than 50: " + treeSet.higher(50));
        System.out.println("Floor of 55: " + treeSet.floor(55));
        System.out.println("Ceiling of 55: " + treeSet.ceiling(55));
        System.out.println("Subset [30, 70): " + treeSet.subSet(30, 70));
        System.out.println("HeadSet < 50: " + treeSet.headSet(50));
        System.out.println("TailSet >= 50: " + treeSet.tailSet(50));
    }
    
    // Use case: HashSet for fast lookups
    static class UniqueWordCounter {
        private Set<String> uniqueWords = new HashSet<>();
        
        public void addWord(String word) {
            uniqueWords.add(word.toLowerCase());
        }
        
        public int getUniqueCount() {
            return uniqueWords.size();
        }
        
        public boolean hasWord(String word) {
            return uniqueWords.contains(word.toLowerCase());
        }
    }
    
    // Use case: TreeSet for sorted data
    static class LeaderboardManager {
        private TreeSet<Integer> scores = new TreeSet<>(Collections.reverseOrder());
        
        public void addScore(int score) {
            scores.add(score);
        }
        
        public Integer getTopScore() {
            return scores.isEmpty() ? null : scores.first();
        }
        
        public Set<Integer> getTopNScores(int n) {
            return scores.stream().limit(n).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        }
    }
}
```

## Complexity Analysis

**HashSet**:
- **Time Complexity**:
  - add(): O(1) average
  - remove(): O(1) average
  - contains(): O(1) average
- **Space Complexity**: O(n)

**TreeSet**:
- **Time Complexity**:
  - add(): O(log n)
  - remove(): O(log n)
  - contains(): O(log n)
  - first()/last(): O(log n)
  - Range operations: O(log n + k) where k is result size
- **Space Complexity**: O(n)

## Edge Cases and Pitfalls

- **Null Elements**: HashSet allows one null element, TreeSet does not allow null (throws NullPointerException)
- **Custom Objects**: TreeSet requires elements to be Comparable or provide a Comparator, HashSet requires proper hashCode() and equals()
- **Performance Trade-off**: HashSet is faster but TreeSet provides ordering and range operations
- **Iteration Order**: HashSet order is unpredictable and may change, TreeSet always iterates in sorted order
- **Memory Overhead**: TreeSet uses more memory per element due to tree node structure

## Interview-Ready Answer

"HashSet uses a hash table providing O(1) operations but no ordering, while TreeSet uses a Red-Black tree providing O(log n) operations with sorted order. Use HashSet when you need fast lookups and don't care about order. Use TreeSet when you need sorted data or range operations like finding elements between two values. TreeSet doesn't allow null elements, while HashSet allows one null."
