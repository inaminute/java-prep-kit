# Graph Valid Tree

## Problem Statement

Given `n` nodes labeled from `0` to `n-1` and a list of undirected edges, check if these edges form a valid tree. A valid tree must satisfy:
- The graph is connected (all nodes are reachable)
- The graph has no cycles
- The graph has exactly n-1 edges

**Example:**
- Input: `n = 5`, `edges = [[0,1],[0,2],[0,3],[1,4]]`
- Output: `true`

**Constraints:**
- 1 ≤ n ≤ 2000
- 0 ≤ edges.length ≤ 5000
- edges[i].length == 2
- 0 ≤ ai, bi < n

## Approach

- A tree with n nodes must have exactly n-1 edges
- Use DFS or BFS to check if graph is connected
- Use visited set to detect cycles
- Track parent during DFS to avoid false cycle detection
- Ensure all nodes are visited (connected graph)

## Solution

```java
import java.util.*;

public class Solution {
    public boolean validTree(int n, int[][] edges) {
        // A tree must have exactly n-1 edges
        if (edges.length != n - 1) {
            return false;
        }
        
        // Build adjacency list
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            graph.get(edge[0]).add(edge[1]);
            graph.get(edge[1]).add(edge[0]);
        }
        
        // DFS to check connectivity and cycles
        boolean[] visited = new boolean[n];
        if (hasCycle(graph, 0, -1, visited)) {
            return false;
        }
        
        // Check if all nodes are visited (connected)
        for (boolean v : visited) {
            if (!v) return false;
        }
        
        return true;
    }
    
    private boolean hasCycle(List<List<Integer>> graph, int node, int parent, boolean[] visited) {
        visited[node] = true;
        
        for (int neighbor : graph.get(node)) {
            if (!visited[neighbor]) {
                if (hasCycle(graph, neighbor, node, visited)) {
                    return true;
                }
            } else if (neighbor != parent) {
                // Visited neighbor that's not parent means cycle
                return true;
            }
        }
        
        return false;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(V + E) - Where V is number of vertices and E is number of edges (DFS traversal)

**Space Complexity**: O(V + E) - For adjacency list and recursion stack

## Edge Cases and Pitfalls

- **Edge Case 1**: Single node with no edges - Valid tree
- **Edge Case 2**: Disconnected components - Not a valid tree
- **Edge Case 3**: Too many or too few edges - Quick check before DFS
- **Common Pitfall 1**: Not tracking parent in DFS - False positive for cycles in undirected graph
- **Common Pitfall 2**: Not checking connectivity - Graph could have cycles removed but still be disconnected

## Interview-Ready Answer

I would first check if the graph has exactly n-1 edges, which is necessary for a tree. Then I use DFS to detect cycles by tracking the parent node and checking if we visit a node that's not the parent. Finally, I verify all nodes were visited to ensure connectivity. This achieves O(V+E) time complexity.
