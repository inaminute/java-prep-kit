# Word Ladder

## Problem Statement

A transformation sequence from word `beginWord` to word `endWord` using a dictionary `wordList` is a sequence of words such that:
- The first word is `beginWord`
- The last word is `endWord`
- Each adjacent pair differs by exactly one letter
- Every word in the sequence is in `wordList`

Return the length of the shortest transformation sequence, or 0 if no such sequence exists.

**Example:**
- Input: `beginWord = "hit"`, `endWord = "cog"`, `wordList = ["hot","dot","dog","lot","log","cog"]`
- Output: `5` (hit -> hot -> dot -> dog -> cog)

**Constraints:**
- 1 ≤ beginWord.length ≤ 10
- endWord.length == beginWord.length
- 1 ≤ wordList.length ≤ 5000

## Approach

- Use BFS to find shortest path
- For each word, try changing each character
- Check if the transformed word exists in the word list
- Use a set for O(1) lookup and to track visited words
- BFS guarantees shortest path

## Solution

```java
import java.util.*;

public class Solution {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if (!wordSet.contains(endWord)) {
            return 0;
        }
        
        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        int level = 1;
        
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String currentWord = queue.poll();
                char[] chars = currentWord.toCharArray();
                
                // Try changing each character
                for (int j = 0; j < chars.length; j++) {
                    char originalChar = chars[j];
                    
                    // Try all 26 letters
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == originalChar) continue;
                        
                        chars[j] = c;
                        String newWord = new String(chars);
                        
                        if (newWord.equals(endWord)) {
                            return level + 1;
                        }
                        
                        if (wordSet.contains(newWord)) {
                            queue.offer(newWord);
                            wordSet.remove(newWord);
                        }
                    }
                    chars[j] = originalChar;
                }
            }
            level++;
        }
        
        return 0;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(M² × N) - Where M is word length and N is number of words (for each word, we try M positions × 26 letters)

**Space Complexity**: O(N) - For the queue and set

## Edge Cases and Pitfalls

- **Edge Case 1**: endWord not in wordList - Return 0 immediately
- **Edge Case 2**: beginWord equals endWord - Should return 1
- **Edge Case 3**: No transformation sequence exists - BFS completes without finding endWord
- **Common Pitfall 1**: Not removing visited words from set - Can cause infinite loops
- **Common Pitfall 2**: Not restoring original character - Affects subsequent iterations

## Interview-Ready Answer

I would use BFS to find the shortest transformation path. For each word, I try changing each character to all 26 letters and check if the result is in the word list. I use a set for O(1) lookup and remove visited words. This gives O(M²×N) time complexity.
