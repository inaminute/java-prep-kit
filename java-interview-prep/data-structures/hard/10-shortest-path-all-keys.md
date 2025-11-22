# Shortest Path to Get All Keys

## Problem Statement

You are given an m x n grid where:
- '.' is an empty cell
- '#' is a wall
- '@' is the starting point
- Lowercase letters represent keys
- Uppercase letters represent locks

You start at the starting point and one move consists of walking one space in one of the four cardinal directions. You cannot walk outside the grid or walk into a wall. Return the lowest number of moves to acquire all keys. If it is impossible, return -1.

**Example:**
- Input: `grid = ["@.a..","###.#","b.A.B"]`
- Output: `8`

**Constraints:**
- m == grid.length
- n == grid[i].length
- 1 ≤ m, n ≤ 30

## Approach

- Use BFS with state (x, y, keys_collected)
- Keys collected can be represented as a bitmask
- Each bit represents whether a specific key has been collected
- Use 3D visited array or set to track (x, y, keys) states
- BFS guarantees shortest path

## Solution

```java
import java.util.*;

public class Solution {
    public int shortestPathAllKeys(String[] grid) {
        int m = grid.length;
        int n = grid[0].length();
        int startX = 0, startY = 0;
        int allKeys = 0;
        
        // Find start position and count keys
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                char c = grid[i].charAt(j);
                if (c == '@') {
                    startX = i;
                    startY = j;
                } else if (c >= 'a' && c <= 'f') {
                    allKeys |= (1 << (c - 'a'));
                }
            }
        }
        
        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.offer(new int[]{startX, startY, 0, 0}); // x, y, keys, steps
        visited.add(startX + "," + startY + "," + 0);
        
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0], y = curr[1], keys = curr[2], steps = curr[3];
            
            if (keys == allKeys) {
                return steps;
            }
            
            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                int newKeys = keys;
                
                if (nx < 0 || nx >= m || ny < 0 || ny >= n) continue;
                
                char c = grid[nx].charAt(ny);
                if (c == '#') continue;
                
                // Check if it's a lock and we don't have the key
                if (c >= 'A' && c <= 'F') {
                    if ((keys & (1 << (c - 'A'))) == 0) continue;
                }
                
                // Pick up key if present
                if (c >= 'a' && c <= 'f') {
                    newKeys |= (1 << (c - 'a'));
                }
                
                String state = nx + "," + ny + "," + newKeys;
                if (!visited.contains(state)) {
                    visited.add(state);
                    queue.offer(new int[]{nx, ny, newKeys, steps + 1});
                }
            }
        }
        
        return -1;
    }
}
```

## Complexity Analysis

**Time Complexity**: O(m × n × 2^k) - Where k is number of keys (at most 6), we visit each cell with each possible key combination

**Space Complexity**: O(m × n × 2^k) - For the visited set storing all possible states

## Edge Cases and Pitfalls

- **Edge Case 1**: No keys in grid - Return 0 if starting at goal
- **Edge Case 2**: Keys behind locks - Must collect keys in correct order
- **Edge Case 3**: Impossible to reach all keys - Return -1
- **Common Pitfall 1**: Not using bitmask for keys - Can't efficiently track collected keys
- **Common Pitfall 2**: Not including keys in state - May revisit same position with different keys

## Interview-Ready Answer

I would use BFS with state (x, y, keys_collected) where keys are represented as a bitmask. Each bit indicates if a key is collected. I use a visited set to track states and explore all four directions, checking for walls and locks. This achieves O(m×n×2^k) time and space complexity.
