# Merge K Sorted Lists

## Problem Statement

You are given an array of `k` linked-lists `lists`, each linked-list is sorted in ascending order. Merge all the linked-lists into one sorted linked-list and return it.

**Example:**
- Input: `lists = [[1,4,5],[1,3,4],[2,6]]`
- Output: `[1,1,2,3,4,4,5,6]`

**Constraints:**
- k == lists.length
- 0 ≤ k ≤ 10⁴
- 0 ≤ lists[i].length ≤ 500
- -10⁴ ≤ lists[i][j] ≤ 10⁴

## Approach

- Use a min heap (priority queue) to track the smallest element from each list
- Initially add the head of each list to the heap
- Repeatedly extract minimum and add its next node
- This ensures we always pick the globally smallest element
- Time complexity is better than merging lists one by one

## Solution

```java
import java.util.*;

class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
}

public class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }
        
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((a, b) -> a.val - b.val);
        
        // Add first node of each list to heap
        for (ListNode node : lists) {
            if (node != null) {
                minHeap.offer(node);
            }
        }
        
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        
        while (!minHeap.isEmpty()) {
            ListNode smallest = minHeap.poll();
            current.next = smallest;
            current = current.next;
            
            if (smallest.next != null) {
                minHeap.offer(smallest.next);
            }
        }
        
        return dummy.next;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(N log k) - Where N is total number of nodes and k is number of lists (each insertion/removal is O(log k))

**Space Complexity**: O(k) - Heap stores at most k nodes at a time

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty lists array - Returns null
- **Edge Case 2**: Some lists are empty - Skipped when building initial heap
- **Edge Case 3**: Single list - Returns that list
- **Common Pitfall 1**: Not checking for null nodes before adding to heap - NullPointerException
- **Common Pitfall 2**: Forgetting to add next node after polling - Loses remaining elements

## Interview-Ready Answer

I would use a min heap to track the smallest element from each list. Initially, I add the head of each list to the heap. Then I repeatedly extract the minimum, add it to the result, and insert its next node. This achieves O(N log k) time with O(k) space.
