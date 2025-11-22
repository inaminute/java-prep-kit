# Implement Stack Using Array

## Problem Statement

Implement a stack data structure using an array with the following operations:
- `push(x)`: Push element x onto the stack
- `pop()`: Remove and return the element on top of the stack
- `peek()`: Return the element on top of the stack without removing it
- `isEmpty()`: Return whether the stack is empty

**Constraints:**
- 1 ≤ x ≤ 10⁹
- At most 10⁴ calls will be made to push, pop, peek, and isEmpty
- All calls to pop and peek are valid (stack is not empty)

## Approach

- Use an array to store stack elements with a fixed or dynamic capacity
- Maintain a top pointer/index that tracks the position of the top element
- Initialize top to -1 to indicate an empty stack
- For push: increment top and add element at that position (resize if needed)
- For pop: return element at top and decrement top
- For peek: return element at top without modifying top
- For isEmpty: check if top is -1
- Optionally implement dynamic resizing when array is full

## Solution

```java
public class MyStack {
    private int[] array;
    private int top;
    private int capacity;
    
    public MyStack(int capacity) {
        this.capacity = capacity;
        this.array = new int[capacity];
        this.top = -1;
    }
    
    public void push(int x) {
        if (top == capacity - 1) {
            // Resize array if full
            resize();
        }
        array[++top] = x;
    }
    
    public int pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return array[top--];
    }
    
    public int peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return array[top];
    }
    
    public boolean isEmpty() {
        return top == -1;
    }
    
    private void resize() {
        capacity *= 2;
        int[] newArray = new int[capacity];
        System.arraycopy(array, 0, newArray, 0, array.length);
        array = newArray;
    }
}
```

## Complexity Analysis

**Time Complexity**: 
- push: O(1) amortized (O(n) when resizing, but rare)
- pop: O(1)
- peek: O(1)
- isEmpty: O(1)

**Space Complexity**: O(n) - Where n is the number of elements in the stack

## Edge Cases and Pitfalls

- **Edge Case 1**: Popping from empty stack - Throws exception to prevent invalid operation
- **Edge Case 2**: Stack reaches capacity - Handled by dynamic resizing
- **Edge Case 3**: Peeking at empty stack - Throws exception for safety
- **Common Pitfall 1**: Not checking for empty stack before pop/peek - Can cause array index errors
- **Common Pitfall 2**: Not handling array full condition - Can cause ArrayIndexOutOfBoundsException

## Interview-Ready Answer

I would implement a stack using an array with a top pointer initialized to -1. Push increments top and adds the element, pop returns the element at top and decrements it, peek returns without modifying, and isEmpty checks if top is -1. All operations are O(1) with O(n) space for n elements.
