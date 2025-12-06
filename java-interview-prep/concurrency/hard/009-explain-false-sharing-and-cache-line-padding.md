# Explain false sharing and cache line padding

## Problem Statement

Describe false sharing problem in concurrent programs and how to prevent it using cache line padding.

## Approach

- **CPU cache lines**: Typically 64 bytes
- **False sharing**: Multiple threads modify variables in same cache line
- **Performance impact**: Cache line invalidation causes slowdown
- **Padding**: Add padding to separate variables into different cache lines
- **@Contended annotation**: Java 8+ automatic padding

## Solution

```java
// Problem: False sharing
class FalseSharingProblem {
    private volatile long x;
    private volatile long y; // In same cache line as x!
    
    public void incrementX() {
        x++;
    }
    
    public void incrementY() {
        y++;
    }
    
    // When thread 1 updates x and thread 2 updates y,
    // they invalidate each other's cache lines constantly
}

// Solution 1: Manual padding
class PaddedCounter {
    private volatile long value;
    
    // Padding to fill cache line (64 bytes)
    private long p1, p2, p3, p4, p5, p6, p7;
    
    public void increment() {
        value++;
    }
    
    public long getValue() {
        return value;
    }
}

// Solution 2: Using @Contended (Java 8+)
import sun.misc.Contended;

class ContendedCounter {
    @Contended
    private volatile long value;
    
    public void increment() {
        value++;
    }
    
    public long getValue() {
        return value;
    }
}

// Practical example: Striped counter
class StripedCounter {
    @Contended
    static class Cell {
        volatile long value;
    }
    
    private final Cell[] cells;
    
    public StripedCounter(int stripes) {
        cells = new Cell[stripes];
        for (int i = 0; i < stripes; i++) {
            cells[i] = new Cell();
        }
    }
    
    public void increment() {
        int index = (int)(Thread.currentThread().getId() % cells.length);
        cells[index].value++;
    }
    
    public long sum() {
        long total = 0;
        for (Cell cell : cells) {
            total += cell.value;
        }
        return total;
    }
}

// Performance comparison
class FalseSharingBenchmark {
    private static final int ITERATIONS = 100_000_000;
    
    // Without padding
    static class UnpaddedCounters {
        volatile long counter1;
        volatile long counter2;
    }
    
    // With padding
    static class PaddedCounters {
        volatile long counter1;
        long p1, p2, p3, p4, p5, p6, p7; // Padding
        volatile long counter2;
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Unpadded: " + benchmark(new UnpaddedCounters()) + "ms");
        System.out.println("Padded: " + benchmark(new PaddedCounters()) + "ms");
    }
    
    private static long benchmark(Object counters) throws InterruptedException {
        long start = System.currentTimeMillis();
        
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                if (counters instanceof UnpaddedCounters) {
                    ((UnpaddedCounters) counters).counter1++;
                } else {
                    ((PaddedCounters) counters).counter1++;
                }
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                if (counters instanceof UnpaddedCounters) {
                    ((UnpaddedCounters) counters).counter2++;
                } else {
                    ((PaddedCounters) counters).counter2++;
                }
            }
        });
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        return System.currentTimeMillis() - start;
    }
}

// Real-world example: Ring buffer
class PaddedRingBuffer<T> {
    @Contended
    static class Sequence {
        volatile long value;
    }
    
    private final Object[] buffer;
    private final int mask;
    private final Sequence readSequence = new Sequence();
    private final Sequence writeSequence = new Sequence();
    
    public PaddedRingBuffer(int size) {
        if ((size & (size - 1)) != 0) {
            throw new IllegalArgumentException("Size must be power of 2");
        }
        this.buffer = new Object[size];
        this.mask = size - 1;
    }
    
    public boolean offer(T item) {
        long write = writeSequence.value;
        long read = readSequence.value;
        
        if (write - read >= buffer.length) {
            return false; // Buffer full
        }
        
        buffer[(int)(write & mask)] = item;
        writeSequence.value = write + 1;
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public T poll() {
        long read = readSequence.value;
        long write = writeSequence.value;
        
        if (read >= write) {
            return null; // Buffer empty
        }
        
        T item = (T) buffer[(int)(read & mask)];
        readSequence.value = read + 1;
        return item;
    }
}
```

## Cache Line Basics

- **Size**: Typically 64 bytes (512 bits)
- **Coherence**: CPU caches must stay synchronized
- **Invalidation**: Writing to cache line invalidates it in other caches
- **False sharing**: Unrelated variables share cache line

## Performance Impact

Without padding:
- Thread 1 writes to variable A
- Cache line containing A and B is invalidated in Thread 2's cache
- Thread 2 writes to variable B
- Cache line is invalidated in Thread 1's cache
- Constant cache line bouncing between cores

With padding:
- A and B are in different cache lines
- No invalidation when different variables are updated
- Much better performance

## Padding Strategies

1. **Manual padding**: Add dummy fields
2. **@Contended**: Automatic padding (requires -XX:-RestrictContended)
3. **Separate objects**: Put variables in different objects
4. **Array elements**: Pad array elements

## Edge Cases and Pitfalls

- **JVM optimization**: JVM may remove unused padding fields
- **Cache line size**: Varies by architecture (usually 64 bytes)
- **Over-padding**: Wastes memory
- **Common Pitfall**: Not enabling @Contended with JVM flag

## Interview-Ready Answer

"False sharing occurs when multiple threads modify variables that reside in the same CPU cache line, causing constant cache invalidation and performance degradation. Even though the variables are independent, they share a cache line (typically 64 bytes). The solution is cache line padding - adding dummy fields to ensure variables occupy separate cache lines. Java 8+ provides the @Contended annotation for automatic padding."

**Tags**: false-sharing, cache-lines, performance
