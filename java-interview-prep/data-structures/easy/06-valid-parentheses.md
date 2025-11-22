# Valid Parentheses

## Problem Statement

Given a string `s` containing just the characters `'('`, `')'`, `'{'`, `'}'`, `'['` and `']'`, determine if the input string is valid. An input string is valid if:
1. Open brackets must be closed by the same type of brackets
2. Open brackets must be closed in the correct order
3. Every close bracket has a corresponding open bracket of the same type

**Example:**
- Input: `s = "()[]{}"`
- Output: `true`
- Input: `s = "(]"`
- Output: `false`

**Constraints:**
- 1 ≤ s.length ≤ 10⁴
- s consists of parentheses only '()[]{}'

## Approach

- Use a stack to track opening brackets
- Iterate through each character in the string
- If it's an opening bracket ('(', '{', '['), push it onto the stack
- If it's a closing bracket (')', '}', ']'), check if the stack is empty or if the top doesn't match
- If stack is empty or doesn't match, return false
- Pop the matching opening bracket from the stack
- After processing all characters, the stack should be empty for a valid string
- A non-empty stack means there are unmatched opening brackets

## Solution

```java
import java.util.Stack;

public class Solution {
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                
                char top = stack.pop();
                if (c == ')' && top != '(') return false;
                if (c == '}' && top != '{') return false;
                if (c == ']' && top != '[') return false;
            }
        }
        
        return stack.isEmpty();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We iterate through the string once, where n is the length of the string

**Space Complexity**: O(n) - In the worst case (all opening brackets), we store all characters in the stack

## Edge Cases and Pitfalls

- **Edge Case 1**: String with only opening brackets - Returns false as stack won't be empty
- **Edge Case 2**: String with only closing brackets - Returns false immediately when stack is empty
- **Edge Case 3**: Single character - Returns false (can't be valid with just one bracket)
- **Common Pitfall 1**: Not checking if stack is empty before popping - Can cause EmptyStackException
- **Common Pitfall 2**: Forgetting to check if stack is empty at the end - Unmatched opening brackets would be missed

## Interview-Ready Answer

I would use a stack to solve this problem. For each opening bracket, I push it onto the stack. For each closing bracket, I check if the stack is empty or if the top doesn't match, returning false if so. After processing all characters, I verify the stack is empty. This gives O(n) time and O(n) space complexity.
