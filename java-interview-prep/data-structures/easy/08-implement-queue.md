# Implement Queue Using Array

## Problem Statement

Implement a queue data structure using an array with the following operations:
- `enqueue(x)`: Add element x to the rear of the queue
- `dequeue()`: Remove and return the element from the front of the queue
- `peek()`: Return the element at the front without removing it
- `isEmpty()`: Return whether the queue is empty

**Constraints:**
- 1 ≤ x ≤ 10⁹
- At most 10⁴ calls will be made to enqueue, dequeue, peek, and isEmpty
- All calls to dequeue and peek are valid (queue is not empty)

## Approach

- Use a circular array to efficiently utilize space
- Maintain two pointers: front (index of first element) and rear (index where next element will be added)
- Track the size to distinguish between empty and full queue
- For enqueue: add element at rear position and increment rear (with wraparound)
- For dequeue: return element at front and increment front (with wraparound)
- For peek: return element at front without modifying pointers
- Use modulo operation for circular wraparound
- Optionally implement dynamic resizing when queue is full

## Solution

```java
public class MyQueue {
    private int[] array;
    private int front;
    private int rear;
    private int size;
    private int capacity;
    
    public MyQueue(int capacity) {
        this.capacity = capacity;
        this.array = new int[capacity];
        this.front = 0;
        this.rear = 0;
        this.size = 0;
    }
    
    public void enqueue(int x) {
        if (size == capacity) {
            resize();
        }
        array[rear] = x;
        rear = (rear + 1) % capacity;
        size++;
    }
    
    public int dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        int value = array[front];
        front = (front + 1) % capacity;
        size--;
        return value;
    }
    
    public int peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return array[front];
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private void resize() {
        int newCapacity = capacity * 2;
        int[] newArray = new int[newCapacity];
        for (int i = 0; i < size; i++) {
            newArray[i] = array[(front + i) % capacity];
        }
        array = newArray;
        front = 0;
        rear = size;
        capacity = newCapacity;
    }
}
```

## Complexity Analysis

**Time Complexity**: 
- enqueue: O(1) amortized (O(n) when resizing)
- dequeue: O(1)
- peek: O(1)
- isEmpty: O(1)

**Space Complexity**: O(n) - Where n is the number of elements in the queue

## Edge Cases and Pitfalls

- **Edge Case 1**: Dequeuing from empty queue - Throws exception to prevent invalid operation
- **Edge Case 2**: Queue reaches capacity - Handled by dynamic resizing
- **Edge Case 3**: Circular wraparound - Properly handled using modulo operation
- **Common Pitfall 1**: Not using circular array - Wastes space as front moves forward
- **Common Pitfall 2**: Confusing empty and full conditions - Solved by tracking size separately

## Interview-Ready Answer

I would implement a queue using a circular array with front and rear pointers, plus a size counter. Enqueue adds at rear and increments it with wraparound, dequeue removes from front and increments it. Using modulo for circular behavior ensures efficient space usage. All operations are O(1) with O(n) space.
