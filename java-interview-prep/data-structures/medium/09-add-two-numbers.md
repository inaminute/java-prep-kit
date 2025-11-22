# Add Two Numbers

## Problem Statement

You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse order, and each of their nodes contains a single digit. Add the two numbers and return the sum as a linked list.

**Example:**
- Input: `l1 = [2,4,3]`, `l2 = [5,6,4]`
- Output: `[7,0,8]` (342 + 465 = 807)

**Constraints:**
- The number of nodes in each linked list is in the range [1, 100]
- 0 ≤ Node.val ≤ 9
- It is guaranteed that the list represents a number that does not have leading zeros

## Approach

- Traverse both lists simultaneously
- Add corresponding digits plus any carry
- Handle carry from previous addition
- Create new nodes for the result list
- Continue until both lists are exhausted and no carry remains

## Solution

```java
class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
}

public class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        int carry = 0;
        
        while (l1 != null || l2 != null || carry != 0) {
            int sum = carry;
            
            if (l1 != null) {
                sum += l1.val;
                l1 = l1.next;
            }
            
            if (l2 != null) {
                sum += l2.val;
                l2 = l2.next;
            }
            
            carry = sum / 10;
            current.next = new ListNode(sum % 10);
            current = current.next;
        }
        
        return dummy.next;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(max(m, n)) - Where m and n are the lengths of the two lists

**Space Complexity**: O(max(m, n)) - The result list will have at most max(m, n) + 1 nodes

## Edge Cases and Pitfalls

- **Edge Case 1**: Lists of different lengths - Handled by checking null before accessing
- **Edge Case 2**: Final carry exists - Loop continues while carry is non-zero
- **Edge Case 3**: Both lists are single digit - Works correctly
- **Common Pitfall 1**: Not handling final carry - Would lose the most significant digit
- **Common Pitfall 2**: Not checking for null before accessing val - NullPointerException

## Interview-Ready Answer

I would traverse both lists simultaneously, adding corresponding digits plus any carry. I create new nodes for the result and continue until both lists are exhausted and no carry remains. This achieves O(max(m,n)) time and space complexity.
