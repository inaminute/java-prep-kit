# Implement a concurrent skip list

## Problem Statement

Design a lock-free or fine-grained locking skip list for concurrent sorted data structure operations.

## Approach

- **Multiple levels**: Probabilistic balanced structure
- **Lock-free traversal**: Read without locks
- **CAS for updates**: Atomic insertions and deletions
- **Marking for deletion**: Logical then physical deletion
- **O(log n) operations**: Expected time complexity

## Solution

```java
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.Random;

public class ConcurrentSkipList<T extends Comparable<T>> {
    private static final int MAX_LEVEL = 16;
    private final Node<T> head;
    private final Random random = new Random();
    
    static class Node<T> {
        final T value;
        final AtomicMarkableReference<Node<T>>[] next;
        final int topLevel;
        
        @SuppressWarnings("unchecked")
        public Node(T value, int level) {
            this.value = value;
            this.topLevel = level;
            this.next = new AtomicMarkableReference[level + 1];
            for (int i = 0; i <= level; i++) {
                next[i] = new AtomicMarkableReference<>(null, false);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public ConcurrentSkipList() {
        head = new Node<>(null, MAX_LEVEL);
    }
    
    // Find position for key
    private boolean find(T key, Node<T>[] preds, Node<T>[] succs) {
        boolean[] marked = {false};
        Node<T> pred = head;
        
        for (int level = MAX_LEVEL; level >= 0; level--) {
            Node<T> curr = pred.next[level].getReference();
            
            while (curr != null) {
                Node<T> succ = curr.next[level].get(marked);
                
                while (marked[0]) {
                    // Node is marked for deletion, help remove it
                    if (!pred.next[level].compareAndSet(curr, succ, false, false)) {
                        return false; // Retry
                    }
                    curr = pred.next[level].getReference();
                    if (curr == null) break;
                    succ = curr.next[level].get(marked);
                }
                
                if (curr != null && curr.value.compareTo(key) < 0) {
                    pred = curr;
                    curr = succ;
                } else {
                    break;
                }
            }
            
            preds[level] = pred;
            succs[level] = curr;
        }
        
        return succs[0] != null && succs[0].value.equals(key);
    }
    
    // Add element
    public boolean add(T key) {
        int topLevel = randomLevel();
        Node<T>[] preds = new Node[MAX_LEVEL + 1];
        Node<T>[] succs = new Node[MAX_LEVEL + 1];
        
        while (true) {
            if (find(key, preds, succs)) {
                return false; // Already exists
            }
            
            Node<T> newNode = new Node<>(key, topLevel);
            
            // Link at level 0
            Node<T> pred = preds[0];
            Node<T> succ = succs[0];
            newNode.next[0].set(succ, false);
            
            if (!pred.next[0].compareAndSet(succ, newNode, false, false)) {
                continue; // Retry
            }
            
            // Link at higher levels
            for (int level = 1; level <= topLevel; level++) {
                while (true) {
                    pred = preds[level];
                    succ = succs[level];
                    newNode.next[level].set(succ, false);
                    
                    if (pred.next[level].compareAndSet(succ, newNode, false, false)) {
                        break;
                    }
                    find(key, preds, succs);
                }
            }
            
            return true;
        }
    }
    
    // Remove element
    public boolean remove(T key) {
        Node<T>[] preds = new Node[MAX_LEVEL + 1];
        Node<T>[] succs = new Node[MAX_LEVEL + 1];
        Node<T> victim;
        
        while (true) {
            if (!find(key, preds, succs)) {
                return false; // Not found
            }
            
            victim = succs[0];
            
            // Mark for deletion from top to bottom
            for (int level = victim.topLevel; level >= 1; level--) {
                boolean[] marked = {false};
                Node<T> succ = victim.next[level].get(marked);
                
                while (!marked[0]) {
                    victim.next[level].attemptMark(succ, true);
                    succ = victim.next[level].get(marked);
                }
            }
            
            // Mark level 0
            boolean[] marked = {false};
            Node<T> succ = victim.next[0].get(marked);
            
            while (true) {
                boolean iMarkedIt = victim.next[0].compareAndSet(succ, succ, false, true);
                succ = victim.next[0].get(marked);
                
                if (iMarkedIt) {
                    find(key, preds, succs); // Help remove
                    return true;
                } else if (marked[0]) {
                    return false;
                }
            }
        }
    }
    
    // Contains check
    public boolean contains(T key) {
        boolean[] marked = {false};
        Node<T> pred = head;
        Node<T> curr = null;
        
        for (int level = MAX_LEVEL; level >= 0; level--) {
            curr = pred.next[level].getReference();
            
            while (curr != null) {
                Node<T> succ = curr.next[level].get(marked);
                
                while (marked[0]) {
                    curr = pred.next[level].getReference();
                    if (curr == null) break;
                    succ = curr.next[level].get(marked);
                }
                
                if (curr != null && curr.value.compareTo(key) < 0) {
                    pred = curr;
                    curr = succ;
                } else {
                    break;
                }
            }
        }
        
        return curr != null && curr.value.equals(key);
    }
    
    // Random level generation
    private int randomLevel() {
        int level = 0;
        while (level < MAX_LEVEL && random.nextBoolean()) {
            level++;
        }
        return level;
    }
    
    // Testing
    public static void main(String[] args) throws InterruptedException {
        ConcurrentSkipList<Integer> list = new ConcurrentSkipList<>();
        
        // Multiple threads adding
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    list.add(threadId * 100 + j);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify
        System.out.println("Contains 500: " + list.contains(500));
        System.out.println("Contains 9999: " + list.contains(9999));
        
        // Remove
        list.remove(500);
        System.out.println("After remove, contains 500: " + list.contains(500));
    }
}
```

## Skip List Structure

```
Level 3:  head -----------------> 30 -----------------> null
Level 2:  head -------> 10 -----> 30 -------> 50 -----> null
Level 1:  head -> 5 -> 10 -> 20 -> 30 -> 40 -> 50 -----> null
Level 0:  head -> 5 -> 10 -> 20 -> 30 -> 40 -> 50 -> 60 -> null
```

## Key Concepts

- **Probabilistic balancing**: Random levels instead of rotations
- **Lock-free**: Uses CAS operations
- **Logical deletion**: Mark nodes before physical removal
- **Helper threads**: Threads help remove marked nodes

## Complexity Analysis

**Time Complexity**: 
- Search: O(log n) expected
- Insert: O(log n) expected
- Delete: O(log n) expected

**Space Complexity**: O(n log n) expected

## Edge Cases and Pitfalls

- **ABA problem**: Solved using AtomicMarkableReference
- **Helping mechanism**: Threads must help remove marked nodes
- **Level generation**: Must be truly random for balance
- **Common Pitfall**: Not handling marked nodes during traversal

## Interview-Ready Answer

"A concurrent skip list is a probabilistic data structure that provides O(log n) operations without locks. It uses multiple levels of linked lists, with higher levels acting as express lanes. Insertions and deletions use CAS operations with AtomicMarkableReference to handle concurrent modifications. Nodes are logically deleted by marking before physical removal, and threads help remove marked nodes during traversal."

**Tags**: skip-list, concurrent, lock-free
