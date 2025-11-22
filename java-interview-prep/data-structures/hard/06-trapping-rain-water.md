# Trapping Rain Water

## Problem Statement

Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.

**Example:**
- Input: `height = [0,1,0,2,1,0,1,3,2,1,2,1]`
- Output: `6`

**Constraints:**
- n == height.length
- 1 ≤ n ≤ 2 * 10⁴
- 0 ≤ height[i] ≤ 10⁵

## Approach

- Use two pointers from both ends
- Track the maximum height seen from left and right
- Water at position is limited by the minimum of left_max and right_max
- Move the pointer with smaller max inward
- Calculate water trapped at current position

## Solution

```java
public class Solution {
    public int trap(int[] height) {
        if (height == null || height.length == 0) {
            return 0;
        }
        
        int left = 0;
        int right = height.length - 1;
        int leftMax = 0;
        int rightMax = 0;
        int water = 0;
        
        while (left < right) {
            if (height[left] < height[right]) {
                if (height[left] >= leftMax) {
                    leftMax = height[left];
                } else {
                    water += leftMax - height[left];
                }
                left++;
            } else {
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    water += rightMax - height[right];
                }
                right--;
            }
        }
        
        return water;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - Single pass through the array with two pointers

**Space Complexity**: O(1) - Only using a constant amount of extra space

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty array - Returns 0
- **Edge Case 2**: Monotonically increasing or decreasing - No water trapped
- **Edge Case 3**: All same height - No water trapped
- **Common Pitfall 1**: Not updating max heights correctly - Leads to incorrect water calculation
- **Common Pitfall 2**: Using extra arrays for left/right max - Works but uses O(n) space instead of O(1)

## Interview-Ready Answer

I would use two pointers from both ends, tracking the maximum height seen from each side. At each step, I move the pointer with the smaller max inward and calculate trapped water as the difference between the max and current height. This achieves O(n) time with O(1) space.
