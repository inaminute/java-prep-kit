# What is the Fork/Join framework?

## Problem Statement

Explain the Fork/Join framework for parallel task decomposition and work-stealing algorithm.

## Approach

- **Divide and conquer**: Break large tasks into smaller subtasks
- **Work stealing**: Idle threads steal work from busy threads
- **RecursiveTask**: Returns a result
- **RecursiveAction**: No result
- **ForkJoinPool**: Special thread pool for fork/join tasks

## Solution

```java
import java.util.concurrent.*;

// RecursiveTask example: Sum of array
class SumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000;
    private long[] array;
    private int start;
    private int end;
    
    public SumTask(long[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        int length = end - start;
        
        if (length <= THRESHOLD) {
            // Small enough, compute directly
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Split into subtasks
            int mid = start + length / 2;
            SumTask leftTask = new SumTask(array, start, mid);
            SumTask rightTask = new SumTask(array, mid, end);
            
            leftTask.fork(); // Async execution
            long rightResult = rightTask.compute(); // Compute in current thread
            long leftResult = leftTask.join(); // Wait for result
            
            return leftResult + rightResult;
        }
    }
    
    public static void main(String[] args) {
        long[] array = new long[10000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1;
        }
        
        ForkJoinPool pool = new ForkJoinPool();
        SumTask task = new SumTask(array, 0, array.length);
        long result = pool.invoke(task);
        
        System.out.println("Sum: " + result);
    }
}

// RecursiveAction example: Parallel array processing
class ArrayProcessorTask extends RecursiveAction {
    private static final int THRESHOLD = 100;
    private int[] array;
    private int start;
    private int end;
    
    public ArrayProcessorTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected void compute() {
        int length = end - start;
        
        if (length <= THRESHOLD) {
            // Process directly
            for (int i = start; i < end; i++) {
                array[i] = array[i] * 2;
            }
        } else {
            // Split into subtasks
            int mid = start + length / 2;
            ArrayProcessorTask left = new ArrayProcessorTask(array, start, mid);
            ArrayProcessorTask right = new ArrayProcessorTask(array, mid, end);
            
            invokeAll(left, right); // Fork both and wait
        }
    }
}

// Fibonacci example
class FibonacciTask extends RecursiveTask<Integer> {
    private final int n;
    
    public FibonacciTask(int n) {
        this.n = n;
    }
    
    @Override
    protected Integer compute() {
        if (n <= 1) {
            return n;
        }
        
        FibonacciTask f1 = new FibonacciTask(n - 1);
        FibonacciTask f2 = new FibonacciTask(n - 2);
        
        f1.fork();
        int result2 = f2.compute();
        int result1 = f1.join();
        
        return result1 + result2;
    }
    
    public static void main(String[] args) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        FibonacciTask task = new FibonacciTask(20);
        int result = pool.invoke(task);
        System.out.println("Fibonacci(20) = " + result);
    }
}
```

## Work-Stealing Algorithm

1. Each thread has its own deque of tasks
2. Thread takes tasks from its own deque (LIFO)
3. When deque is empty, thread "steals" from another thread's deque (FIFO)
4. Minimizes contention and maximizes CPU utilization

## When to Use Fork/Join

**Good for:**
- CPU-intensive tasks
- Recursive algorithms
- Large data processing
- Divide-and-conquer problems

**Not good for:**
- I/O-bound tasks
- Tasks with blocking operations
- Small tasks (overhead not worth it)

## Edge Cases and Pitfalls

- **Threshold too small**: Too much overhead from task creation
- **Threshold too large**: Not enough parallelism
- **Blocking operations**: Defeats work-stealing benefits
- **Common Pitfall**: Using for I/O-bound tasks

## Interview-Ready Answer

"The Fork/Join framework is designed for parallel processing of recursive, divide-and-conquer algorithms. It uses a work-stealing algorithm where idle threads steal tasks from busy threads' queues, maximizing CPU utilization. RecursiveTask returns results while RecursiveAction doesn't. It's ideal for CPU-intensive tasks that can be broken down into smaller subtasks."

**Tags**: fork-join, parallel, work-stealing
