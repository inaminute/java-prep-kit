# Merge Sorted Arrays

## Problem Statement

You are given two integer arrays `nums1` and `nums2`, sorted in non-decreasing order, and two integers `m` and `n`, representing the number of elements in `nums1` and `nums2` respectively. Merge `nums2` into `nums1` as one sorted array. The final sorted array should be stored inside `nums1`. To accommodate this, `nums1` has a length of `m + n`, where the first `m` elements denote the elements that should be merged, and the last `n` elements are set to 0 and should be ignored.

**Example:**
- Input: `nums1 = [1,2,3,0,0,0]`, `m = 3`, `nums2 = [2,5,6]`, `n = 3`
- Output: `[1,2,2,3,5,6]` (stored in nums1)

**Constraints:**
- nums1.length = m + n
- nums2.length = n
- 0 ≤ m, n ≤ 200
- 1 ≤ m + n ≤ 200

## Approach

- Start from the end of both arrays to avoid overwriting elements in nums1
- Use three pointers: one for the last element of nums1 (m-1), one for the last element of nums2 (n-1), and one for the last position in the merged array (m+n-1)
- Compare elements from the end of both arrays and place the larger one at the end of nums1
- Continue until all elements from nums2 are merged
- If nums2 elements remain, copy them to nums1 (if nums1 elements remain, they're already in place)

## Solution

```java
public class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1;      // Pointer for nums1
        int p2 = n - 1;      // Pointer for nums2
        int p = m + n - 1;   // Pointer for merged position
        
        // Merge from the end
        while (p2 >= 0) {
            if (p1 >= 0 && nums1[p1] > nums2[p2]) {
                nums1[p] = nums1[p1];
                p1--;
            } else {
                nums1[p] = nums2[p2];
                p2--;
            }
            p--;
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(m + n) - We iterate through both arrays once, processing each element exactly once

**Space Complexity**: O(1) - We only use a constant amount of extra space for the three pointers

## Edge Cases and Pitfalls

- **Edge Case 1**: nums2 is empty (n=0) - No merge needed, nums1 remains unchanged
- **Edge Case 2**: nums1 is empty (m=0) - All elements from nums2 are copied to nums1
- **Edge Case 3**: All elements in nums2 are smaller than nums1 - They get placed at the beginning correctly
- **Common Pitfall 1**: Starting from the beginning - Would overwrite elements in nums1 before they're processed
- **Common Pitfall 2**: Not checking p1 >= 0 - Can cause array index out of bounds

## Interview-Ready Answer

I would merge from the end of both arrays to avoid overwriting unprocessed elements. Using three pointers, I compare elements from the back and place the larger one at the end of nums1, working backwards. This achieves O(m+n) time with O(1) space complexity.
