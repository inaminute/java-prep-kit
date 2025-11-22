# Validate Binary Search Tree

## Problem Statement

Given the root of a binary tree, determine if it is a valid binary search tree (BST). A valid BST is defined as follows:
- The left subtree of a node contains only nodes with keys less than the node's key
- The right subtree of a node contains only nodes with keys greater than the node's key
- Both the left and right subtrees must also be binary search trees

**Example:**
- Input: `root = [2,1,3]`
- Output: `true`

**Constraints:**
- The number of nodes in the tree is in the range [1, 10⁴]
- -2³¹ ≤ Node.val ≤ 2³¹ - 1

## Approach

- Use recursive validation with min and max bounds
- For each node, check if its value is within the valid range
- Pass down the valid range as we recurse
- Left child must be less than parent, right child must be greater

## Solution

```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
}

public class Solution {
    public boolean isValidBST(TreeNode root) {
        return validate(root, null, null);
    }
    
    private boolean validate(TreeNode node, Integer min, Integer max) {
        if (node == null) {
            return true;
        }
        
        // Check if current node violates min/max constraint
        if ((min != null && node.val <= min) || 
            (max != null && node.val >= max)) {
            return false;
        }
        
        // Recursively validate left and right subtrees
        return validate(node.left, min, node.val) && 
               validate(node.right, node.val, max);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We visit each node once

**Space Complexity**: O(h) - Recursion stack depth equals tree height (O(n) worst case for skewed tree)

## Edge Cases and Pitfalls

- **Edge Case 1**: Single node - Always valid BST
- **Edge Case 2**: Duplicate values - Not allowed in BST, correctly rejected
- **Edge Case 3**: Integer.MIN_VALUE or MAX_VALUE nodes - Handled by using Integer objects (null for no bound)
- **Common Pitfall 1**: Only comparing with immediate parent - Must check against all ancestors
- **Common Pitfall 2**: Using int instead of Integer for bounds - Can't represent "no bound"

## Interview-Ready Answer

I would use recursive validation with min and max bounds. For each node, I verify it's within the valid range, then recursively validate left subtree with updated max bound and right subtree with updated min bound. This gives O(n) time and O(h) space complexity.
