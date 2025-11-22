# Memory Management

## Problem Statement

Explain Java memory management including heap, stack, and metaspace. Demonstrate memory allocation, object lifecycle, and common memory issues. Show how to diagnose and fix memory problems.

**Requirements**:
- Explain memory areas (heap, stack, metaspace)
- Demonstrate object allocation and lifecycle
- Show memory leak patterns
- Explain memory profiling tools

## Approach

- Stack stores method frames and local variables
- Heap stores objects and instance variables
- Metaspace stores class metadata
- Objects become eligible for GC when unreachable
- Use profilers to identify memory issues

## Solution

```java
import java.lang.ref.*;
import java.util.*;

public class MemoryManagement {
    
    public static void main(String[] args) {
        explainMemoryAreas();
        demonstrateObjectLifecycle();
        demonstrateMemoryLeaks();
        demonstrateWeakReferences();
    }
    
    public static void explainMemoryAreas() {
        System.out.println("=== Memory Areas ===");
        
        System.out.println("Stack:");
        System.out.println("  - Thread-specific");
        System.out.println("  - Stores method frames, local variables, references");
        System.out.println("  - LIFO structure, fast allocation");
        System.out.println("  - StackOverflowError if too deep");
        
        System.out.println("\nHeap:");
        System.out.println("  - Shared across threads");
        System.out.println("  - Stores objects and arrays");
        System.out.println("  - Managed by GC");
        System.out.println("  - OutOfMemoryError if exhausted");
        
        System.out.println("\nMetaspace:");
        System.out.println("  - Stores class metadata");
        System.out.println("  - Native memory, not in heap");
        System.out.println("  - Replaced PermGen in Java 8");
    }
    
    public static void demonstrateObjectLifecycle() {
        System.out.println("\n=== Object Lifecycle ===");
        
        // 1. Created - allocated on heap
        String obj = new String("Hello");
        System.out.println("1. Object created: " + obj);
        
        // 2. In use - reachable
        System.out.println("2. Object in use");
        
        // 3. Unreachable - eligible for GC
        obj = null;
        System.out.println("3. Object unreachable, eligible for GC");
        
        // 4. Finalized (if finalize() defined)
        System.out.println("4. finalize() called before reclamation");
        
        // 5. Reclaimed - memory freed
        System.gc(); // Suggest GC (not guaranteed)
        System.out.println("5. Memory reclaimed by GC");
    }
    
    public static void demonstrateMemoryLeaks() {
        System.out.println("\n=== Common Memory Leaks ===");
        
        // 1. Static collections
        class StaticLeak {
            private static List<Object> list = new ArrayList<>();
            
            public static void add(Object obj) {
                list.add(obj); // Never removed!
            }
        }
        System.out.println("1. Static collections holding references");
        
        // 2. Unclosed resources
        System.out.println("2. Unclosed streams, connections");
        
        // 3. Listeners not removed
        System.out.println("3. Event listeners not unregistered");
        
        // 4. ThreadLocal not cleaned
        System.out.println("4. ThreadLocal variables not removed");
        
        // 5. Inner class references
        System.out.println("5. Non-static inner classes hold outer reference");
    }
    
    public static void demonstrateWeakReferences() {
        System.out.println("\n=== Weak References ===");
        
        // Strong reference - prevents GC
        Object strong = new Object();
        
        // Weak reference - allows GC
        WeakReference<Object> weak = new WeakReference<>(new Object());
        System.out.println("Weak ref before GC: " + weak.get());
        
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        System.out.println("Weak ref after GC: " + weak.get()); // Likely null
        
        // Soft reference - cleared when memory needed
        SoftReference<Object> soft = new SoftReference<>(new Object());
        System.out.println("Soft references cleared only when memory low");
        
        // Phantom reference - for cleanup actions
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> phantom = new PhantomReference<>(new Object(), queue);
        System.out.println("Phantom references for post-mortem cleanup");
    }
    
    // Memory profiling tips
    public static void profilingTips() {
        System.out.println("\n=== Memory Profiling ===");
        System.out.println("Tools:");
        System.out.println("  - jmap: Heap dump");
        System.out.println("  - jstat: GC statistics");
        System.out.println("  - VisualVM: Visual profiling");
        System.out.println("  - Eclipse MAT: Heap dump analysis");
        
        System.out.println("\nJVM Flags:");
        System.out.println("  -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("  -XX:HeapDumpPath=/path/to/dump");
        System.out.println("  -Xlog:gc*: GC logging");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for allocation, O(n) for GC marking

**Space Complexity**: Depends on object graph size

## Edge Cases and Pitfalls

- **Stack overflow**: Deep recursion or large local arrays
- **Heap exhaustion**: Too many objects or memory leaks
- **Metaspace overflow**: Too many classes loaded

## Interview-Ready Answer

"Java memory has stack (method frames, local variables), heap (objects), and metaspace (class metadata). Stack is thread-specific and fast, heap is shared and GC-managed. Objects become eligible for GC when unreachable. Common leaks: static collections, unclosed resources, unremoved listeners. Use weak/soft references for caches. Profile with jmap, jstat, VisualVM to diagnose issues."
