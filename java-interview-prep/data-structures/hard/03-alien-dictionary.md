# Alien Dictionary

## Problem Statement

There is a new alien language that uses the English alphabet. However, the order among letters is unknown to you. You are given a list of strings `words` from the alien language's dictionary, where the strings in `words` are sorted lexicographically by the rules of this new language. Return a string of the unique letters in the new alien language sorted in lexicographically increasing order. If there is no solution, return "". If there are multiple solutions, return any of them.

**Example:**
- Input: `words = ["wrt","wrf","er","ett","rftt"]`
- Output: `"wertf"`

**Constraints:**
- 1 ≤ words.length ≤ 100
- 1 ≤ words[i].length ≤ 100
- words[i] consists of only lowercase English letters

## Approach

- Build a graph where edges represent character ordering
- Compare adjacent words to find character relationships
- Use topological sort to determine the order
- Detect cycles which indicate invalid ordering

## Solution

```java
import java.util.*;

public class Solution {
    public String alienOrder(String[] words) {
        Map<Character, Set<Character>> graph = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();
        
        // Initialize graph
        for (String word : words) {
            for (char c : word.toCharArray()) {
                graph.putIfAbsent(c, new HashSet<>());
                inDegree.putIfAbsent(c, 0);
            }
        }
        
        // Build graph by comparing adjacent words
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            int minLen = Math.min(word1.length(), word2.length());
            
            // Check for invalid case: word1 is prefix of word2 but longer
            if (word1.length() > word2.length() && word1.startsWith(word2)) {
                return "";
            }
            
            // Find first different character
            for (int j = 0; j < minLen; j++) {
                char c1 = word1.charAt(j);
                char c2 = word2.charAt(j);
                if (c1 != c2) {
                    if (!graph.get(c1).contains(c2)) {
                        graph.get(c1).add(c2);
                        inDegree.put(c2, inDegree.get(c2) + 1);
                    }
                    break;
                }
            }
        }
        
        // Topological sort using BFS
        Queue<Character> queue = new LinkedList<>();
        for (char c : inDegree.keySet()) {
            if (inDegree.get(c) == 0) {
                queue.offer(c);
            }
        }
        
        StringBuilder result = new StringBuilder();
        while (!queue.isEmpty()) {
            char c = queue.poll();
            result.append(c);
            
            for (char neighbor : graph.get(c)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        return result.length() == inDegree.size() ? result.toString() : "";
    }
}
```

## Complexity Analysis

**Time Complexity**: O(C) - Where C is the total length of all words (building graph + topological sort)

**Space Complexity**: O(1) or O(26) - At most 26 characters in the graph

## Edge Cases and Pitfalls

- **Edge Case 1**: Invalid ordering (cycle exists) - Topological sort won't include all characters
- **Edge Case 2**: Longer word is prefix of shorter word - Invalid, return empty string
- **Edge Case 3**: Single word - All characters have no ordering constraints
- **Common Pitfall 1**: Not checking for invalid prefix case - Can lead to incorrect results
- **Common Pitfall 2**: Adding duplicate edges - Affects in-degree calculation

## Interview-Ready Answer

I would build a directed graph by comparing adjacent words to find character ordering relationships, then use topological sort with BFS to determine the final order. I check for cycles and invalid cases like longer prefixes. This achieves O(C) time complexity where C is total character count.
