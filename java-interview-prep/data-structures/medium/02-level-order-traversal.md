# Binary Tree Level Order Traversal

## Problem Statement

Given the root of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level).

**Example:**
- Input: `root = [3,9,20,null,null,15,7]`
- Output: `[[3],[9,20],[15,7]]`

**Constraints:**
- The number of nodes in the tree is in the range [0, 2000]
- -1000 ≤ Node.val ≤ 1000

## Approach

- Use a queue for breadth-first search (BFS)
- Process nodes level by level
- For each level, record the number of nodes at that level
- Process exactly that many nodes, adding their children to the queue
- This ensures we process complete levels at a time

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
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Integer> currentLevel = new ArrayList<>();
            
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node.val);
                
                if (node.left != null) {
                    queue.offer(node.left);
                }
                if (node.right != null) {
                    queue.offer(node.right);
                }
            }
            
            result.add(currentLevel);
        }
        
        return result;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - We visit each node exactly once

**Space Complexity**: O(w) - Where w is the maximum width of the tree (maximum nodes at any level)

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty tree - Returns empty list
- **Edge Case 2**: Single node - Returns list with one level containing one element
- **Edge Case 3**: Complete binary tree - Queue size reaches n/2 at the last level
- **Common Pitfall 1**: Not capturing level size before the loop - Queue size changes as we add children
- **Common Pitfall 2**: Adding null nodes to queue - Should check before adding

## Interview-Ready Answer

I would use BFS with a queue. For each level, I capture the current queue size, process exactly that many nodes, and add their children to the queue. This ensures level-by-level processing with O(n) time and O(w) space complexity.
