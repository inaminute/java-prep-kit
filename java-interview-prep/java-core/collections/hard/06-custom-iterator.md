# Custom Iterator Implementation

## Problem Statement

Implement custom Iterator and ListIterator from scratch. Demonstrate stateful iteration logic, support for remove() operation, and bidirectional traversal. Show how to make collections iterable.

**Requirements**:
- Implement Iterator interface
- Support remove() operation safely
- Implement ListIterator for bidirectional traversal
- Make custom collection iterable

## Approach

- Iterator requires hasNext(), next(), remove()
- Track current position and last returned element
- remove() can only be called once per next()
- ListIterator adds previous(), hasPrevious(), add(), set()
- Implement Iterable to support enhanced for-loop
- Use modCount for fail-fast behavior

## Solution

```java
import java.util.*;

class CustomList<E> implements Iterable<E> {
    private Object[] elements;
    private int size;
    private int modCount = 0;
    
    public CustomList() {
        elements = new Object[10];
        size = 0;
    }
    
    public void add(E e) {
        if (size == elements.length) {
            elements = Arrays.copyOf(elements, size * 2);
        }
        elements[size++] = e;
        modCount++;
    }
    
    public int size() {
        return size;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new CustomIterator();
    }
    
    public ListIterator<E> listIterator() {
        return new CustomListIterator(0);
    }
    
    // Custom Iterator implementation
    private class CustomIterator implements Iterator<E> {
        int cursor = 0;
        int lastRet = -1;
        int expectedModCount = modCount;
        
        @Override
        public boolean hasNext() {
            return cursor < size;
        }
        
        @Override
        public E next() {
            checkForComodification();
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            lastRet = cursor;
            return (E) elements[cursor++];
        }
        
        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();
            
            // Shift elements
            System.arraycopy(elements, lastRet + 1, elements, lastRet, size - lastRet - 1);
            size--;
            cursor = lastRet;
            lastRet = -1;
            modCount++;
            expectedModCount = modCount;
        }
        
        void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    // Custom ListIterator implementation
    private class CustomListIterator implements ListIterator<E> {
        int cursor;
        int lastRet = -1;
        int expectedModCount = modCount;
        
        CustomListIterator(int index) {
            cursor = index;
        }
        
        @Override
        public boolean hasNext() {
            return cursor < size;
        }
        
        @Override
        public E next() {
            if (cursor >= size) throw new NoSuchElementException();
            lastRet = cursor;
            return (E) elements[cursor++];
        }
        
        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }
        
        @Override
        public E previous() {
            if (cursor <= 0) throw new NoSuchElementException();
            lastRet = --cursor;
            return (E) elements[cursor];
        }
        
        @Override
        public int nextIndex() {
            return cursor;
        }
        
        @Override
        public int previousIndex() {
            return cursor - 1;
        }
        
        @Override
        public void remove() {
            if (lastRet < 0) throw new IllegalStateException();
            System.arraycopy(elements, lastRet + 1, elements, lastRet, size - lastRet - 1);
            size--;
            if (lastRet < cursor) cursor--;
            lastRet = -1;
            modCount++;
            expectedModCount = modCount;
        }
        
        @Override
        public void set(E e) {
            if (lastRet < 0) throw new IllegalStateException();
            elements[lastRet] = e;
        }
        
        @Override
        public void add(E e) {
            // Shift and insert
            if (size == elements.length) {
                elements = Arrays.copyOf(elements, size * 2);
            }
            System.arraycopy(elements, cursor, elements, cursor + 1, size - cursor);
            elements[cursor++] = e;
            size++;
            lastRet = -1;
            modCount++;
            expectedModCount = modCount;
        }
    }
}

public class CustomIteratorImpl {
    public static void main(String[] args) {
        CustomList<String> list = new CustomList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        
        // Enhanced for-loop (uses Iterator)
        System.out.println("=== Iteration ===");
        for (String s : list) {
            System.out.println(s);
        }
        
        // Remove using Iterator
        System.out.println("\n=== Remove 'B' ===");
        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if (s.equals("B")) {
                iter.remove();
            }
        }
        
        for (String s : list) {
            System.out.println(s);
        }
        
        // ListIterator bidirectional
        System.out.println("\n=== ListIterator ===");
        ListIterator<String> listIter = list.listIterator();
        System.out.println("Forward:");
        while (listIter.hasNext()) {
            System.out.println(listIter.next());
        }
        
        System.out.println("Backward:");
        while (listIter.hasPrevious()) {
            System.out.println(listIter.previous());
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for hasNext/next, O(n) for remove

**Space Complexity**: O(1) for iterator object

## Edge Cases and Pitfalls

- **remove() State**: Can only call once per next()
- **Fail-Fast**: Check modCount for concurrent modifications
- **NoSuchElementException**: Throw when no more elements
- **IllegalStateException**: Throw if remove() called without next()
- **ListIterator Complexity**: add/set/remove affect cursor position

## Interview-Ready Answer

"Implement Iterator with hasNext(), next(), and remove(). Track cursor position and lastRet for remove() support. Use modCount for fail-fast behavior. ListIterator adds bidirectional traversal with previous(), hasPrevious(), and modification operations add() and set(). Implement Iterable interface to support enhanced for-loop. Key challenges are managing cursor state correctly and handling remove() edge cases."
