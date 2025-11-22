# Serialize and Deserialize Binary Tree

## Problem Statement

Design an algorithm to serialize and deserialize a binary tree. Serialization is the process of converting a data structure into a sequence of bits so that it can be stored or transmitted and reconstructed later. You do not need to follow a specific format, as long as you can serialize a tree to a string and deserialize the string back to the original tree structure.

**Example:**
- Input: `root = [1,2,3,null,null,4,5]`
- Output: `[1,2,3,null,null,4,5]`

**Constraints:**
- The number of nodes in the tree is in the range [0, 10⁴]
- -1000 ≤ Node.val ≤ 1000

## Approach

- Use pre-order traversal for serialization (root, left, right)
- Use a delimiter (comma) to separate values
- Use a special marker (null) for null nodes
- For deserialization, split the string and reconstruct using the same traversal order
- Use a queue or index to track position during deserialization

## Solution

```java
import java.util.*;

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
}

public class Codec {
    // Encodes a tree to a single string
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }
    
    private void serializeHelper(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append("null,");
            return;
        }
        sb.append(node.val).append(",");
        serializeHelper(node.left, sb);
        serializeHelper(node.right, sb);
    }
    
    // Decodes your encoded data to tree
    public TreeNode deserialize(String data) {
        Queue<String> queue = new LinkedList<>(Arrays.asList(data.split(",")));
        return deserializeHelper(queue);
    }
    
    private TreeNode deserializeHelper(Queue<String> queue) {
        String val = queue.poll();
        if (val.equals("null")) {
            return null;
        }
        TreeNode node = new TreeNode(Integer.parseInt(val));
        node.left = deserializeHelper(queue);
        node.right = deserializeHelper(queue);
        return node;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - Both serialization and deserialization visit each node once

**Space Complexity**: O(n) - For the serialized string and recursion stack (O(h) for stack)

## Edge Cases and Pitfalls

- **Edge Case 1**: Empty tree - Serializes to "null,"
- **Edge Case 2**: Single node - Correctly handles with two null children
- **Edge Case 3**: Skewed tree - Works correctly with many null markers
- **Common Pitfall 1**: Not handling null nodes - Can't reconstruct tree structure
- **Common Pitfall 2**: Using wrong traversal order for deserialization - Must match serialization order

## Interview-Ready Answer

I would use pre-order traversal for serialization, converting the tree to a comma-separated string with "null" markers for empty nodes. For deserialization, I split the string and reconstruct using the same pre-order pattern with a queue. Both operations are O(n) time and space.
