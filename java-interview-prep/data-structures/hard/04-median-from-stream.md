# Find Median from Data Stream

## Problem Statement

The median is the middle value in an ordered integer list. If the size of the list is even, there is no middle value, and the median is the mean of the two middle values. Implement the MedianFinder class:
- `MedianFinder()` initializes the object
- `void addNum(int num)` adds the integer num to the data structure
- `double findMedian()` returns the median of all elements so far

**Constraints:**
- -10⁵ ≤ num ≤ 10⁵
- At most 5 * 10⁴ calls to addNum and findMedian

## Approach

- Use two heaps: max heap for smaller half, min heap for larger half
- Keep heaps balanced (sizes differ by at most 1)
- Median is either the top of one heap or average of both tops
- Max heap stores smaller half (top is largest of small numbers)
- Min heap stores larger half (top is smallest of large numbers)
- Rebalance heaps after each insertion

## Solution

```java
import java.util.*;

class MedianFinder {
    private PriorityQueue<Integer> maxHeap; // smaller half
    private PriorityQueue<Integer> minHeap; // larger half
    
    public MedianFinder() {
        maxHeap = new PriorityQueue<>((a, b) -> b - a);
        minHeap = new PriorityQueue<>();
    }
    
    public void addNum(int num) {
        // Add to max heap first
        maxHeap.offer(num);
        
        // Balance: move largest from max heap to min heap
        minHeap.offer(maxHeap.poll());
        
        // Ensure max heap has equal or one more element
        if (maxHeap.size() < minHeap.size()) {
            maxHeap.offer(minHeap.poll());
        }
    }
    
    public double findMedian() {
        if (maxHeap.size() > minHeap.size()) {
            return maxHeap.peek();
        }
        return (maxHeap.peek() + minHeap.peek()) / 2.0;
    }
}
```

## Complexity Analysis

**Time Complexity**: 
- addNum: O(log n) - Heap insertion and removal
- findMedian: O(1) - Just peek at heap tops

**Space Complexity**: O(n) - Storing all n numbers in the heaps

## Edge Cases and Pitfalls

- **Edge Case 1**: Single element - Max heap has it, median is that element
- **Edge Case 2**: Two elements - One in each heap, median is average
- **Edge Case 3**: All elements same value - Works correctly
- **Common Pitfall 1**: Not maintaining heap balance - Median calculation breaks
- **Common Pitfall 2**: Integer overflow when calculating average - Use 2.0 for division

## Interview-Ready Answer

I would use two heaps: a max heap for the smaller half and a min heap for the larger half. When adding a number, I insert it into max heap, move the largest to min heap, then rebalance if needed. The median is either the max heap top or the average of both tops. AddNum is O(log n), findMedian is O(1).
