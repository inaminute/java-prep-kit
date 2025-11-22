# Binary Tree Inorder Traversal

## Problem Statement

Given the root of a binary tree, return the inorder traversal of its nodes' values. Inorder traversal visits nodes in the order: left subtree, root, right subtree.

**Example:**
- Input: `root = [1,null,2,3]`
- Output: `[1,3,2]`

**Constraints:**
- The number of nodes in the tree is in the range [0, 100]
- -100 ≤ Node.val ≤ 100

## Approach

- Use iterative approach with a stack to simulate recursion
- Start from root and go as far left as possible, pushing nodes onto stack
- When we can't go left anymore, pop from stack, process the node
- Then move to the right child and repeat
- This avoids recursion and uses explicit stack management

## Solution

```java
import java.util.*;

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
}

public class Solution {
    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode current = root;
        
        while (current != null || !stack.isEmpty()) {
            // Go to the leftmost node
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            
            // Process the node
            current = stack.pop();
            result.add(current.val);
            
            // Move to right subtree
            current = current.right;
        }
        
        return result;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We visit each node exactly once

**Space Complexity**: O(h) - Stack space where h is the height of the tree (O(n) worst case for skewed tree)

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty tree - Returns empty list
- **Edge Case 2**: Single node - Returns list with one element
- **Edge Case 3**: Skewed tree - Stack grows to O(n)
- **Common Pitfall 1**: Forgetting to check if stack is empty in while condition
- **Common Pitfall 2**: Not moving to right child after processing node

## Interview-Ready Answer

I would use an iterative approach with a stack. I traverse to the leftmost node, pushing nodes onto the stack. Then I pop, process the node, and move to its right child. This gives O(n) time and O(h) space complexity.
