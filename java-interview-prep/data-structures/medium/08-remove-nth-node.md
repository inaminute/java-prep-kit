# Remove Nth Node From End of List

## Problem Statement

Given the head of a linked list, remove the nth node from the end of the list and return its head.

**Example:**
- Input: `head = [1,2,3,4,5]`, `n = 2`
- Output: `[1,2,3,5]`

**Constraints:**
- The number of nodes in the list is sz
- 1 ≤ sz ≤ 30
- 0 ≤ Node.val ≤ 100
- 1 ≤ n ≤ sz

## Approach

- Use two pointers with a gap of n nodes between them
- Move both pointers until the first reaches the end
- The second pointer will be just before the node to remove
- Use a dummy node to handle edge case of removing head
- This approach requires only one pass through the list

## Solution

```java
class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
}

public class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode first = dummy;
        ListNode second = dummy;
        
        // Move first pointer n+1 steps ahead
        for (int i = 0; i <= n; i++) {
            first = first.next;
        }
        
        // Move both pointers until first reaches end
        while (first != null) {
            first = first.next;
            second = second.next;
        }
        
        // Remove the nth node
        second.next = second.next.next;
        
        return dummy.next;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(L) - Where L is the length of the list, we traverse it once

**Space Complexity**: O(1) - We only use two pointer variables

## Edge Cases and Pitfalls

- **Edge Case 1**: Removing the head node (n equals list length) - Handled by dummy node
- **Edge Case 2**: Single node list - Correctly removes it
- **Edge Case 3**: Removing last node - Works correctly
- **Common Pitfall 1**: Not using dummy node - Complicates handling of head removal
- **Common Pitfall 2**: Off-by-one error in gap size - Should be n+1 steps for second pointer to be before target

## Interview-Ready Answer

I would use two pointers with a gap of n nodes between them. After moving the first pointer n+1 steps ahead, I move both until the first reaches the end. The second pointer will be just before the node to remove. This achieves O(L) time with O(1) space in one pass.
