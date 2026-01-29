# Custom Collection Implementation

## Problem Statement

Implement a custom collection from scratch by extending AbstractList or AbstractCollection. Demonstrate proper implementation of core methods, iterator support, and optional operations while following the Collection Framework contracts.

**Requirements**:
- Extend AbstractList or implement Collection interface
- Implement iterator and optional operations
- Follow Collection Framework contracts
- Handle edge cases properly

## Approach

- AbstractList provides skeletal implementation
- Must implement size() and get() for AbstractList
- Implement add(), remove() for modifiable collections
- Provide custom Iterator implementation
- Follow fail-fast behavior conventions
- Ensure proper equals() and hashCode()

## Solution

```java
import java.util.*;

// Custom ArrayList-like implementation
class SimpleList<E> extends AbstractList<E> {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;
    
    public SimpleList() {
        elements = new Object[DEFAULT_CAPACITY];
        size = 0;
    }
    
    @Override
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (E) elements[index];
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean add(E e) {
        ensureCapacity();
        elements[size++] = e;
        modCount++; // For fail-fast iterator
        return true;
    }
    
    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        E oldValue = (E) elements[index];
        elements[index] = element;
        return oldValue;
    }
    
    @Override
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        E oldValue = (E) elements[index];
        
        // Shift elements
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[--size] = null;
        modCount++;
        return oldValue;
    }
    
    private void ensureCapacity() {
        if (size == elements.length) {
            int newCapacity = elements.length * 2;
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }
}

public class CustomCollectionImpl {
    public static void main(String[] args) {
        List<String> list = new SimpleList<>();
        
        list.add("A");
        list.add("B");
        list.add("C");
        
        System.out.println("List: " + list);
        System.out.println("Get(1): " + list.get(1));
        
        list.set(1, "B_MODIFIED");
        System.out.println("After set: " + list);
        
        list.remove(0);
        System.out.println("After remove: " + list);
        
        // Iterator (provided by AbstractList)
        for (String s : list) {
            System.out.println("Element: " + s);
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for get/add, O(n) for remove

**Space Complexity**: O(n)

## Edge Cases and Pitfalls

- **modCount**: Must increment for structural modifications (fail-fast)
- **Bounds Checking**: Always validate indices
- **Null Handling**: Decide if nulls are allowed
- **Thread Safety**: Document if not thread-safe
- **Optional Operations**: Throw UnsupportedOperationException if not supported

## Interview-Ready Answer

"Extend AbstractList for list implementations, providing size() and get() at minimum. Implement add() and remove() for modifiable collections. Increment modCount for fail-fast iterators. AbstractList provides iterator, equals, hashCode implementations. Follow Collection Framework contracts: throw IndexOutOfBoundsException for invalid indices, UnsupportedOperationException for unsupported operations, and maintain fail-fast behavior."
