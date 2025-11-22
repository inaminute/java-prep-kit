# Linked List Cycle

## Problem Statement

Given the head of a linked list, determine if the linked list has a cycle in it. A cycle exists if there is some node in the list that can be reached again by continuously following the next pointer. Return `true` if there is a cycle in the linked list, otherwise return `false`.

**Example:**
- Input: `head = [3,2,0,-4]`, with tail connecting to node at index 1
- Output: `true`

**Constraints:**
- The number of nodes in the list is in the range [0, 10⁴]
- -10⁵ ≤ Node.val ≤ 10⁵

## Approach

- Use Floyd's Cycle Detection algorithm (tortoise and hare)
- Initialize two pointers: slow (moves one step) and fast (moves two steps)
- If there's no cycle, fast will reach the end (null)
- If there's a cycle, fast will eventually catch up to slow inside the cycle
- The key insight is that in a cycle, the faster pointer will lap the slower one
- This approach avoids using extra space for a hash set

## Solution

```java
class ListNode {
    int val;
    ListNode next;
    ListNode(int x) {
        val = x;
        next = null;
    }
}

public class Solution {
    public boolean hasCycle(ListNode head) {
        if (head == null || head.next == null) {
            return false;
        }
        
        ListNode slow = head;
        ListNode fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            
            if (slow == fast) {
                return true;
            }
        }
        
        return false;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - In the worst case, we visit each node once. If there's a cycle, fast catches slow within n iterations

**Space Complexity**: O(1) - We only use two pointer variables regardless of the list size

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty list (head is null) - Returns false correctly
- **Edge Case 2**: Single node with no cycle - Returns false
- **Edge Case 3**: Single node pointing to itself - Returns true
- **Common Pitfall 1**: Not checking fast.next before accessing fast.next.next - Can cause NullPointerException
- **Common Pitfall 2**: Using a hash set to track visited nodes - Works but uses O(n) space instead of O(1)

## Interview-Ready Answer

I would use Floyd's Cycle Detection algorithm with two pointers moving at different speeds. The slow pointer moves one step and the fast pointer moves two steps. If there's a cycle, they'll eventually meet; otherwise, fast reaches the end. This achieves O(n) time with O(1) space complexity.
