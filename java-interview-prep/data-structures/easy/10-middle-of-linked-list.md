# Middle of Linked List

## Problem Statement

Given the head of a singly linked list, return the middle node of the linked list. If there are two middle nodes, return the second middle node.

**Example:**
- Input: `head = [1,2,3,4,5]`
- Output: Node with value 3
- Input: `head = [1,2,3,4,5,6]`
- Output: Node with value 4

**Constraints:**
- The number of nodes in the list is in the range [1, 100]
- 1 ≤ Node.val ≤ 100

## Approach

- Use the two-pointer technique (slow and fast pointers)
- Initialize both pointers at the head
- Move slow pointer one step at a time
- Move fast pointer two steps at a time
- When fast reaches the end, slow will be at the middle
- For odd-length lists, slow will be exactly at the middle
- For even-length lists, slow will be at the second middle node

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
    public ListNode middleNode(ListNode head) {
        if (head == null) {
            return null;
        }
        
        ListNode slow = head;
        ListNode fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        return slow;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We traverse the list once, where n is the number of nodes

**Space Complexity**: O(1) - We only use two pointer variables

## Edge Cases and Pitfalls

- **Edge Case 1**: Single node list - Returns that node correctly
- **Edge Case 2**: Two node list - Returns the second node
- **Edge Case 3**: Even-length list - Returns the second middle node as required
- **Common Pitfall 1**: Not checking fast.next - Can cause NullPointerException
- **Common Pitfall 2**: Counting nodes first - Works but requires two passes instead of one

## Interview-Ready Answer

I would use the two-pointer technique with slow and fast pointers. The slow pointer moves one step while the fast pointer moves two steps. When fast reaches the end, slow is at the middle. This achieves O(n) time with O(1) space complexity.
