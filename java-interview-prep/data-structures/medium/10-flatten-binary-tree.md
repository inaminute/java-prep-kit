# Flatten Binary Tree to Linked List

## Problem Statement

Given the root of a binary tree, flatten the tree into a "linked list":
- The "linked list" should use the same TreeNode class where the right child pointer points to the next node and the left child pointer is always null
- The "linked list" should be in the same order as a pre-order traversal of the binary tree

**Example:**
- Input: `root = [1,2,5,3,4,null,6]`
- Output: `[1,null,2,null,3,null,4,null,5,null,6]`

**Constraints:**
- The number of nodes in the tree is in the range [0, 2000]
- -100 ≤ Node.val ≤ 100

## Approach

- Use modified pre-order traversal
- For each node, save the right subtree
- Move left subtree to right
- Find the rightmost node of left subtree
- Connect it to the original right subtree
- Repeat for each node

## Solution

```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
}

public class Solution {
    public void flatten(TreeNode root) {
        if (root == null) {
            return;
        }
        
        TreeNode current = root;
        
        while (current != null) {
            if (current.left != null) {
                // Find the rightmost node of left subtree
                TreeNode rightmost = current.left;
                while (rightmost.right != null) {
                    rightmost = rightmost.right;
                }
                
                // Connect rightmost to current's right
                rightmost.right = current.right;
                
                // Move left subtree to right
                current.right = current.left;
                current.left = null;
            }
            
            current = current.right;
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We visit each node once

**Space Complexity**: O(1) - We only use a constant amount of extra space (no recursion stack)

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty tree - Returns immediately
- **Edge Case 2**: Tree with only left children - Correctly flattens to right-only list
- **Edge Case 3**: Tree with only right children - Already flattened, no changes needed
- **Common Pitfall 1**: Losing reference to right subtree - Must save it before moving left to right
- **Common Pitfall 2**: Not setting left to null - Violates the linked list requirement

## Interview-Ready Answer

I would use an iterative approach. For each node with a left child, I find the rightmost node of the left subtree, connect it to the current right subtree, then move the left subtree to the right. This achieves O(n) time with O(1) space complexity.
