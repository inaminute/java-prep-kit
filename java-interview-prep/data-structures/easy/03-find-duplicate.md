# Find Duplicate Number

## Problem Statement

Given an array of integers `nums` containing `n + 1` integers where each integer is in the range `[1, n]` inclusive, there is only one repeated number. Return this repeated number. You must solve the problem without modifying the array and using only constant extra space.

**Example:**
- Input: `nums = [1,3,4,2,2]`
- Output: `2`

**Constraints:**
- 1 ≤ n ≤ 10⁵
- nums.length = n + 1
- 1 ≤ nums[i] ≤ n
- All integers in nums appear only once except for one integer which appears two or more times

## Approach

- Use Floyd's Cycle Detection algorithm (tortoise and hare)
- Treat the array as a linked list where each value points to an index
- Since there's a duplicate, there must be a cycle in this "linked list"
- Phase 1: Find the intersection point in the cycle using slow and fast pointers
- Phase 2: Find the entrance to the cycle (which is the duplicate number)
- Move one pointer to the start and keep the other at intersection, then move both at same speed
- The point where they meet is the duplicate number

## Solution

```java
public class Solution {
    public int findDuplicate(int[] nums) {
        // Phase 1: Find intersection point in the cycle
        int slow = nums[0];
        int fast = nums[0];
        
        do {
            slow = nums[slow];
            fast = nums[nums[fast]];
        } while (slow != fast);
        
        // Phase 2: Find the entrance to the cycle
        slow = nums[0];
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }
        
        return slow;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We traverse the array at most twice (once to find intersection, once to find entrance)

**Space Complexity**: O(1) - We only use two pointer variables regardless of input size

## Edge Cases and Pitfalls

- **Edge Case 1**: Duplicate appears at the beginning - Algorithm handles this correctly
- **Edge Case 2**: Duplicate appears multiple times (more than twice) - Still finds the duplicate correctly
- **Edge Case 3**: Minimum array size (n=1, array=[1,1]) - Works correctly
- **Common Pitfall 1**: Using a hash set - Violates the O(1) space requirement
- **Common Pitfall 2**: Sorting the array - Violates the "don't modify array" requirement

## Interview-Ready Answer

I would use Floyd's Cycle Detection algorithm, treating the array as a linked list where each value points to an index. Since there's a duplicate, a cycle exists. I use two phases: first find the cycle intersection with slow/fast pointers, then find the cycle entrance which is the duplicate. This achieves O(n) time with O(1) space.
