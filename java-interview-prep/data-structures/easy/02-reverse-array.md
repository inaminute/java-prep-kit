# Reverse Array

## Problem Statement

Given an array of integers, reverse the array in-place. This means you should modify the original array without using extra space for another array.

**Example:**
- Input: `nums = [1, 2, 3, 4, 5]`
- Output: `[5, 4, 3, 2, 1]`

**Constraints:**
- 0 ≤ nums.length ≤ 10⁴
- -10⁹ ≤ nums[i] ≤ 10⁹

## Approach

- Use two pointers: one at the start (left) and one at the end (right) of the array
- Swap the elements at these two positions
- Move the left pointer forward and the right pointer backward
- Continue until the pointers meet or cross each other
- This approach modifies the array in-place without requiring additional space

## Solution

```java
public class Solution {
    public void reverseArray(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return;
        }
        
        int left = 0;
        int right = nums.length - 1;
        
        while (left < right) {
            // Swap elements
            int temp = nums[left];
            nums[left] = nums[right];
            nums[right] = temp;
            
            left++;
            right--;
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We iterate through half of the array, performing constant-time swaps

**Space Complexity**: O(1) - We only use a constant amount of extra space (temp variable and two pointers)

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty array - Handled by the null/length check at the beginning
- **Edge Case 2**: Single element array - No reversal needed, handled by length check
- **Edge Case 3**: Array with two elements - Correctly swaps the two elements
- **Common Pitfall 1**: Not checking for null input - Can cause NullPointerException
- **Common Pitfall 2**: Using extra space by creating a new array - Violates the in-place requirement

## Interview-Ready Answer

I would use the two-pointer technique to reverse the array in-place. Starting with pointers at both ends, I swap elements and move the pointers toward the center until they meet. This achieves O(n) time complexity with O(1) space complexity.
