# FizzBuzz Multithreaded

## Problem Statement

Implement a multithreaded version of FizzBuzz with four threads. One thread prints "fizz" for multiples of 3, another prints "buzz" for multiples of 5, a third prints "fizzbuzz" for multiples of 15, and the fourth prints numbers for all other cases. Ensure correct ordering from 1 to n.

**Input**: Number n

**Output**: FizzBuzz sequence from 1 to n with correct thread coordination

**Constraints**: 
- Must use four separate threads
- Must maintain correct order
- Each thread handles specific cases

## Approach

- Use shared counter to track current number
- Each thread checks if current number matches its condition
- Use synchronization to ensure only one thread acts at a time
- Signal other threads after printing
- Continue until reaching n

## Solution

```java
import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

class FizzBuzz {
    private int n;
    private Semaphore fizzSem = new Semaphore(0);
    private Semaphore buzzSem = new Semaphore(0);
    private Semaphore fizzbuzzSem = new Semaphore(0);
    private Semaphore numberSem = new Semaphore(1);
    private int current = 1;
    
    public FizzBuzz(int n) {
        this.n = n;
    }
    
    public void fizz(Runnable printFizz) throws InterruptedException {
        while (true) {
            fizzSem.acquire();
            if (current > n) {
                fizzSem.release();
                break;
            }
            printFizz.run();
            current++;
            releaseNext();
        }
    }
    
    public void buzz(Runnable printBuzz) throws InterruptedException {
        while (true) {
            buzzSem.acquire();
            if (current > n) {
                buzzSem.release();
                break;
            }
            printBuzz.run();
            current++;
            releaseNext();
        }
    }
    
    public void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        while (true) {
            fizzbuzzSem.acquire();
            if (current > n) {
                fizzbuzzSem.release();
                break;
            }
            printFizzBuzz.run();
            current++;
            releaseNext();
        }
    }
    
    public void number(IntConsumer printNumber) throws InterruptedException {
        while (true) {
            numberSem.acquire();
            if (current > n) {
                // Release all to terminate
                fizzSem.release();
                buzzSem.release();
                fizzbuzzSem.release();
                break;
            }
            printNumber.accept(current);
            current++;
            releaseNext();
        }
    }
    
    private void releaseNext() {
        if (current > n) {
            fizzSem.release();
            buzzSem.release();
            fizzbuzzSem.release();
            numberSem.release();
            return;
        }
        
        if (current % 15 == 0) {
            fizzbuzzSem.release();
        } else if (current % 3 == 0) {
            fizzSem.release();
        } else if (current % 5 == 0) {
            buzzSem.release();
        } else {
            numberSem.release();
        }
    }
}

public class FizzBuzzMultithreadedDemo {
    public static void main(String[] args) throws InterruptedException {
        int n = 15;
        FizzBuzz fizzBuzz = new FizzBuzz(n);
        
        Thread fizzThread = new Thread(() -> {
            try {
                fizzBuzz.fizz(() -> System.out.print("fizz, "));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread buzzThread = new Thread(() -> {
            try {
                fizzBuzz.buzz(() -> System.out.print("buzz, "));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread fizzbuzzThread = new Thread(() -> {
            try {
                fizzBuzz.fizzbuzz(() -> System.out.print("fizzbuzz, "));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread numberThread = new Thread(() -> {
            try {
                fizzBuzz.number(num -> System.out.print(num + ", "));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        fizzThread.start();
        buzzThread.start();
        fizzbuzzThread.start();
        numberThread.start();
        
        fizzThread.join();
        buzzThread.join();
        fizzbuzzThread.join();
        numberThread.join();
        
        System.out.println("\nCompleted");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) where n is the input number

**Space Complexity**: O(1) for synchronization primitives

## Edge Cases and Pitfalls

- **Order of checks**: Must check divisibility by 15 before checking 3 or 5 to handle fizzbuzz correctly.
- **Thread termination**: Ensure all threads terminate properly when reaching n.
- **Initial state**: Number thread should start first, so initialize its semaphore with 1 permit.
- **Signaling**: After each print, signal the appropriate next thread based on the next number.

## Interview-Ready Answer

"Multithreaded FizzBuzz uses four threads with semaphores for coordination. Each thread has a semaphore that's released when it's that thread's turn. After printing, determine which thread should go next based on the next number's divisibility. Check divisibility by 15 first, then 3, then 5, otherwise use the number thread. The key is proper signaling to maintain order and ensuring all threads terminate when reaching n."
