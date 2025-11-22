# Garbage Collection

## Problem Statement

Explain Java garbage collection mechanisms. Demonstrate different GC algorithms, generations, and how to tune GC performance. Show how to monitor and analyze GC behavior.

**Requirements**:
- Explain generational garbage collection
- Describe different GC algorithms (Serial, Parallel, CMS, G1, ZGC)
- Show GC tuning parameters
- Demonstrate memory leak scenarios

## Approach

- GC automatically reclaims memory from unreachable objects
- Generational hypothesis: most objects die young
- Young generation (Eden, Survivor) and Old generation
- Different collectors optimize for throughput or latency
- Use GC logs and tools to monitor and tune

## Solution

```java
import java.lang.management.*;
import java.util.*;

public class GarbageCollection {
    
    public static void main(String[] args) {
        demonstrateGenerations();
        demonstrateGCMonitoring();
        demonstrateMemoryLeak();
        explainGCAlgorithms();
    }
    
    public static void demonstrateGenerations() {
        System.out.println("=== Generational GC ===");
        System.out.println("Young Generation:");
        System.out.println("  - Eden: New objects allocated here");
        System.out.println("  - Survivor 0 & 1: Objects that survive minor GC");
        System.out.println("Old Generation:");
        System.out.println("  - Long-lived objects promoted from young gen");
        System.out.println("Metaspace:");
        System.out.println("  - Class metadata (replaced PermGen in Java 8)");
    }
    
    public static void demonstrateGCMonitoring() {
        System.out.println("\n=== GC Monitoring ===");
        
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            System.out.println("Pool: " + pool.getName());
            System.out.println("  Type: " + pool.getType());
            System.out.println("  Usage: " + pool.getUsage());
        }
        
        // Get GC information
        List<GarbageCollectorMXBean> gcBeans = 
            ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("\nGC: " + gcBean.getName());
            System.out.println("  Collections: " + gcBean.getCollectionCount());
            System.out.println("  Time: " + gcBean.getCollectionTime() + "ms");
        }
    }
    
    public static void demonstrateMemoryLeak() {
        System.out.println("\n=== Memory Leak Example ===");
        
        // Example: Static collection holding references
        class LeakyCache {
            private static List<byte[]> cache = new ArrayList<>();
            
            public static void addToCache() {
                cache.add(new byte[1024 * 1024]); // 1MB
            }
        }
        
        System.out.println("Memory leak: Static collection never releases objects");
        System.out.println("Solution: Use WeakReference or bounded cache");
    }
    
    public static void explainGCAlgorithms() {
        System.out.println("\n=== GC Algorithms ===");
        
        System.out.println("Serial GC (-XX:+UseSerialGC):");
        System.out.println("  - Single-threaded, simple");
        System.out.println("  - Good for small apps, single CPU");
        
        System.out.println("\nParallel GC (-XX:+UseParallelGC):");
        System.out.println("  - Multi-threaded, throughput-focused");
        System.out.println("  - Default in Java 8, good for batch processing");
        
        System.out.println("\nCMS (-XX:+UseConcMarkSweepGC):");
        System.out.println("  - Concurrent, low-pause");
        System.out.println("  - Deprecated in Java 9, removed in Java 14");
        
        System.out.println("\nG1 GC (-XX:+UseG1GC):");
        System.out.println("  - Region-based, predictable pauses");
        System.out.println("  - Default since Java 9, good for large heaps");
        
        System.out.println("\nZGC (-XX:+UseZGC):");
        System.out.println("  - Ultra-low latency (<10ms pauses)");
        System.out.println("  - Scalable, good for large heaps (>100GB)");
    }
    
    // GC tuning parameters
    public static void explainTuningParameters() {
        System.out.println("\n=== GC Tuning Parameters ===");
        System.out.println("-Xms: Initial heap size");
        System.out.println("-Xmx: Maximum heap size");
        System.out.println("-XX:NewRatio: Old/Young generation ratio");
        System.out.println("-XX:MaxGCPauseMillis: Target pause time (G1)");
        System.out.println("-XX:+PrintGCDetails: Print GC logs");
    }
}
```

## Complexity Analysis

**Time Complexity**: Varies by algorithm, typically O(n) for marking live objects

**Space Complexity**: O(n) for heap memory

## Edge Cases and Pitfalls

- **Memory leaks**: Static collections, unclosed resources, listeners
- **OutOfMemoryError**: Heap exhausted or too many threads
- **GC overhead**: Too frequent GC indicates undersized heap

## Interview-Ready Answer

"Java GC uses generational collection: young generation (Eden, Survivors) for new objects, old generation for long-lived objects. Different collectors optimize for throughput (Parallel) or latency (G1, ZGC). G1 is default since Java 9, provides predictable pauses. Tune with -Xms/-Xmx for heap size, -XX:MaxGCPauseMillis for pause target. Monitor with GC logs and tools like VisualVM."
