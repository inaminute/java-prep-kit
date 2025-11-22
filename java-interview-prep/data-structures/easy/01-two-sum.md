# Two Sum

## Problem Statement

Given an array of integers `nums` and an integer `target`, return the indices of the two numbers such that they add up to `target`. You may assume that each input would have exactly one solution, and you may not use the same element twice. You can return the answer in any order.

**Example:**
- Input: `nums = [2,7,11,15]`, `target = 9`
- Output: `[0,1]` (because `nums[0] + nums[1] = 2 + 7 = 9`)

**Constraints:**
- 2 ≤ nums.length ≤ 10⁴
- -10⁹ ≤ nums[i] ≤ 10⁹
- -10⁹ ≤ target ≤ 10⁹
- Only one valid answer exists

## Approach

- Use a hash map to store each number and its index as we iterate through the array
- For each element, calculate the complement (target - current element)
- Check if the complement exists in the hash map
- If found, return the current index and the complement's index
- If not found, add the current element and its index to the hash map
- This allows us to solve the problem in a single pass

## Solution

```java
import java.util.HashMap;
import java.util.Map;

public class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            
            if (map.containsKey(complement)) {
                return new int[] { map.get(complement), i };
            }
            
            map.put(nums[i], i);
        }
        
        // Should never reach here given problem constraints
        throw new IllegalArgumentException("No two sum solution");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We traverse the array once, and hash map operations (get/put) are O(1) on average

**Space Complexity**: O(n) - In the worst case, we store all n elements in the hash map before finding the solution

## Edge Cases and Pitfalls

- **Edge Case 1**: Array with exactly 2 elements - The solution handles this correctly as the minimum constraint
- **Edge Case 2**: Negative numbers in the array - The solution works correctly with negative values
- **Edge Case 3**: Target is zero - Works correctly when looking for two numbers that sum to zero
- **Common Pitfall 1**: Using the same element twice - Avoided by checking the map before adding the current element
- **Common Pitfall 2**: Using nested loops (brute force) - Results in O(n²) time complexity instead of optimal O(n)

## Interview-Ready Answer

I would use a hash map to solve this in one pass. As I iterate through the array, I calculate the complement (target minus current number) and check if it exists in the map. If found, I return both indices; otherwise, I store the current number and index. This gives us O(n) time and O(n) space complexity.
