# Remove Element

## Problem Statement

Given an integer array `nums` and an integer `val`, remove all occurrences of `val` in `nums` in-place. The order of the elements may be changed. Return the number of elements in `nums` which are not equal to `val`. Consider the number of elements in `nums` which are not equal to `val` be `k`, to get accepted, you need to do the following: change the array `nums` such that the first `k` elements of `nums` contain the elements which are not equal to `val`.

**Example:**
- Input: `nums = [3,2,2,3]`, `val = 3`
- Output: `2`, `nums = [2,2,_,_]` (underscores represent don't care values)

**Constraints:**
- 0 ≤ nums.length ≤ 100
- 0 ≤ nums[i] ≤ 50
- 0 ≤ val ≤ 100

## Approach

- Use two pointers: one for reading (iterating through array) and one for writing (tracking position of next valid element)
- Iterate through the array with the read pointer
- When we find an element that is not equal to val, copy it to the write pointer position
- Increment the write pointer only when we copy a valid element
- The write pointer's final value represents the count of elements not equal to val
- This approach maintains relative order of valid elements

## Solution

```java
public class Solution {
    public int removeElement(int[] nums, int val) {
        int writeIndex = 0;
        
        for (int readIndex = 0; readIndex < nums.length; readIndex++) {
            if (nums[readIndex] != val) {
                nums[writeIndex] = nums[readIndex];
                writeIndex++;
            }
        }
        
        return writeIndex;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We iterate through the array once, where n is the length of the array

**Space Complexity**: O(1) - We only use two pointer variables regardless of input size

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty array - Returns 0 correctly
- **Edge Case 2**: No elements equal to val - All elements remain, returns original length
- **Edge Case 3**: All elements equal to val - Returns 0, all elements removed
- **Common Pitfall 1**: Creating a new array - Violates the in-place requirement
- **Common Pitfall 2**: Not maintaining relative order - While not required, the two-pointer approach naturally preserves it

## Interview-Ready Answer

I would use a two-pointer approach with a read pointer iterating through the array and a write pointer tracking where to place valid elements. When I find an element not equal to val, I copy it to the write position and increment the write pointer. This achieves O(n) time with O(1) space.
