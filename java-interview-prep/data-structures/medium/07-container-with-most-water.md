# Container With Most Water

## Problem Statement

You are given an integer array `height` of length `n`. There are `n` vertical lines drawn such that the two endpoints of the `i`th line are `(i, 0)` and `(i, height[i])`. Find two lines that together with the x-axis form a container that contains the most water. Return the maximum amount of water a container can store.

**Example:**
- Input: `height = [1,8,6,2,5,4,8,3,7]`
- Output: `49`

**Constraints:**
- n == height.length
- 2 ≤ n ≤ 10⁵
- 0 ≤ height[i] ≤ 10⁴

## Approach

- Use two pointers starting at both ends
- Calculate area with current pointers
- Move the pointer with smaller height inward
- The area is limited by the shorter line
- Moving the shorter line inward might find a taller line
- Moving the taller line inward can only decrease area

## Solution

```java
public class Solution {
    public int maxArea(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int maxArea = 0;
        
        while (left < right) {
            int width = right - left;
            int currentHeight = Math.min(height[left], height[right]);
            int area = width * currentHeight;
            maxArea = Math.max(maxArea, area);
            
            // Move the pointer with smaller height
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        
        return maxArea;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We traverse the array once with two pointers

**Space Complexity**: O(1) - We only use a constant amount of extra space

## Edge Cases and Pitfalls

- **Edge Case 1**: Two lines of equal height - Either pointer can be moved
- **Edge Case 2**: Lines in ascending order - Left pointer moves through entire array
- **Edge Case 3**: Lines in descending order - Right pointer moves through entire array
- **Common Pitfall 1**: Moving both pointers - Might skip the optimal solution
- **Common Pitfall 2**: Moving the taller line - Can only decrease area, never increase

## Interview-Ready Answer

I would use two pointers starting at both ends. I calculate the area with the current pointers and move the pointer with the smaller height inward, as moving the taller one can only decrease area. This achieves O(n) time with O(1) space complexity.
