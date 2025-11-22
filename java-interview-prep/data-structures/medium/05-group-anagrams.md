# Group Anagrams

## Problem Statement

Given an array of strings `strs`, group the anagrams together. You can return the answer in any order. An anagram is a word or phrase formed by rearranging the letters of a different word or phrase, typically using all the original letters exactly once.

**Example:**
- Input: `strs = ["eat","tea","tan","ate","nat","bat"]`
- Output: `[["bat"],["nat","tan"],["ate","eat","tea"]]`

**Constraints:**
- 1 ≤ strs.length ≤ 10⁴
- 0 ≤ strs[i].length ≤ 100
- strs[i] consists of lowercase English letters

## Approach

- Use a hash map where the key is a sorted version of the string
- All anagrams will have the same sorted string as their key
- Iterate through each string, sort it, and use as key
- Group all strings with the same sorted key together

## Solution

```java
import java.util.*;

public class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> map = new HashMap<>();
        
        for (String str : strs) {
            char[] chars = str.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);
            
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(str);
        }
        
        return new ArrayList<>(map.values());
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n * k log k) - Where n is the number of strings and k is the maximum length of a string (sorting each string)

**Space Complexity**: O(n * k) - Storing all strings in the hash map

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty string in array - Handled correctly as empty key
- **Edge Case 2**: Single character strings - Works correctly
- **Edge Case 3**: All strings are anagrams - All grouped together
- **Common Pitfall 1**: Using unsorted string as key - Won't group anagrams correctly
- **Common Pitfall 2**: Not initializing list for new keys - NullPointerException

## Interview-Ready Answer

I would use a hash map where the key is the sorted version of each string. All anagrams will have the same sorted string, so they'll be grouped together. This achieves O(n * k log k) time complexity where n is the number of strings and k is the average string length.
