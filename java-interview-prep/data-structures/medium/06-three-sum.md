# Three Sum

## Problem Statement

Given an integer array `nums`, return all the triplets `[nums[i], nums[j], nums[k]]` such that `i != j`, `i != k`, and `j != k`, and `nums[i] + nums[j] + nums[k] == 0`. Notice that the solution set must not contain duplicate triplets.

**Example:**
- Input: `nums = [-1,0,1,2,-1,-4]`
- Output: `[[-1,-1,2],[-1,0,1]]`

**Constraints:**
- 3 ≤ nums.length ≤ 3000
- -10⁵ ≤ nums[i] ≤ 10⁵

## Approach

- Sort the array first
- Fix one element and use two pointers for the remaining two
- Skip duplicates to avoid duplicate triplets
- Move pointers based on sum comparison with target (0)
- This reduces the problem from O(n³) to O(n²)

## Solution

```java
import java.util.*;

public class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        
        for (int i = 0; i < nums.length - 2; i++) {
            // Skip duplicates for first element
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            
            int left = i + 1;
            int right = nums.length - 1;
            
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                
                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    
                    // Skip duplicates for second element
                    while (left < right && nums[left] == nums[left + 1]) {
                        left++;
                    }
                    // Skip duplicates for third element
                    while (left < right && nums[right] == nums[right - 1]) {
                        right--;
                    }
                    
                    left++;
                    right--;
                } else if (sum < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        
        return result;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n²) - Sorting takes O(n log n), then we have nested loops with two pointers

**Space Complexity**: O(1) - Not counting the output array, we use constant extra space

## Edge Cases and Pitfalls

- **Edge Case 1**: Array with less than 3 elements - Returns empty list
- **Edge Case 2**: All positive or all negative numbers - No valid triplets
- **Edge Case 3**: Multiple duplicate triplets - Correctly skipped
- **Common Pitfall 1**: Not skipping duplicates - Results in duplicate triplets
- **Common Pitfall 2**: Not sorting first - Two-pointer approach won't work

## Interview-Ready Answer

I would sort the array first, then fix one element and use two pointers for the remaining two. For each fixed element, I move pointers based on the sum comparison with zero, skipping duplicates to avoid duplicate triplets. This achieves O(n²) time complexity.
