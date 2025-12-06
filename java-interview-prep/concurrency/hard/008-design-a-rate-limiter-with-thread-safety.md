# Design a rate limiter with thread safety

## Problem Statement

Implement a thread-safe rate limiter using token bucket or sliding window algorithm.

## Approach

- **Token bucket**: Tokens refilled at fixed rate
- **Sliding window**: Track requests in time window
- **Thread-safe**: Use atomic operations or locks
- **Configurable**: Rate and burst capacity
- **Blocking/Non-blocking**: tryAcquire vs acquire

## Solution

```java
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

// Token Bucket Rate Limiter
class TokenBucketRateLimiter {
    private final long capacity;
    private final long refillRate; // tokens per second
    private final AtomicLong tokens;
    private final AtomicLong lastRefillTime;
    private final ReentrantLock lock = new ReentrantLock();
    
    public TokenBucketRateLimiter(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTime = new AtomicLong(System.nanoTime());
    }
    
    public boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    public boolean tryAcquire(long permits) {
        refill();
        
        lock.lock();
        try {
            long currentTokens = tokens.get();
            if (currentTokens >= permits) {
                tokens.addAndGet(-permits);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public void acquire() throws InterruptedException {
        acquire(1);
    }
    
    public void acquire(long permits) throws InterruptedException {
        while (!tryAcquire(permits)) {
            Thread.sleep(10);
        }
    }
    
    private void refill() {
        long now = System.nanoTime();
        long lastRefill = lastRefillTime.get();
        long elapsedNanos = now - lastRefill;
        long tokensToAdd = (elapsedNanos * refillRate) / 1_000_000_000L;
        
        if (tokensToAdd > 0) {
            lock.lock();
            try {
                long currentTokens = tokens.get();
                long newTokens = Math.min(capacity, currentTokens + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime.set(now);
            } finally {
                lock.unlock();
            }
        }
    }
    
    public long availableTokens() {
        refill();
        return tokens.get();
    }
}

// Sliding Window Rate Limiter
import java.util.concurrent.ConcurrentLinkedQueue;

class SlidingWindowRateLimiter {
    private final long windowSizeMillis;
    private final int maxRequests;
    private final ConcurrentLinkedQueue<Long> requestTimestamps;
    private final ReentrantLock lock = new ReentrantLock();
    
    public SlidingWindowRateLimiter(long windowSizeMillis, int maxRequests) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequests = maxRequests;
        this.requestTimestamps = new ConcurrentLinkedQueue<>();
    }
    
    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSizeMillis;
        
        lock.lock();
        try {
            // Remove old timestamps
            while (!requestTimestamps.isEmpty() && 
                   requestTimestamps.peek() < windowStart) {
                requestTimestamps.poll();
            }
            
            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.offer(now);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public int currentRequests() {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSizeMillis;
        
        lock.lock();
        try {
            while (!requestTimestamps.isEmpty() && 
                   requestTimestamps.peek() < windowStart) {
                requestTimestamps.poll();
            }
            return requestTimestamps.size();
        } finally {
            lock.unlock();
        }
    }
}

// Guava-style Rate Limiter
class SimpleRateLimiter {
    private final double permitsPerSecond;
    private double storedPermits;
    private long nextFreeTicketNanos;
    private final ReentrantLock lock = new ReentrantLock();
    
    public SimpleRateLimiter(double permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
        this.storedPermits = 0;
        this.nextFreeTicketNanos = System.nanoTime();
    }
    
    public void acquire() {
        acquire(1);
    }
    
    public void acquire(int permits) {
        long waitNanos = reserve(permits);
        if (waitNanos > 0) {
            try {
                Thread.sleep(waitNanos / 1_000_000, (int)(waitNanos % 1_000_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public boolean tryAcquire() {
        return tryAcquire(1, 0);
    }
    
    public boolean tryAcquire(int permits, long timeoutNanos) {
        long waitNanos = reserve(permits);
        if (waitNanos > timeoutNanos) {
            return false;
        }
        if (waitNanos > 0) {
            try {
                Thread.sleep(waitNanos / 1_000_000, (int)(waitNanos % 1_000_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }
    
    private long reserve(int permits) {
        lock.lock();
        try {
            long now = System.nanoTime();
            resync(now);
            
            long waitNanos = Math.max(0, nextFreeTicketNanos - now);
            double permitsToSpend = Math.min(permits, storedPermits);
            double freshPermits = permits - permitsToSpend;
            
            long waitNanosForFreshPermits = 
                (long)(freshPermits * (1_000_000_000.0 / permitsPerSecond));
            
            nextFreeTicketNanos += waitNanosForFreshPermits;
            storedPermits -= permitsToSpend;
            
            return waitNanos;
        } finally {
            lock.unlock();
        }
    }
    
    private void resync(long now) {
        if (now > nextFreeTicketNanos) {
            double newPermits = (now - nextFreeTicketNanos) * permitsPerSecond / 1_000_000_000.0;
            storedPermits = Math.min(permitsPerSecond, storedPermits + newPermits);
            nextFreeTicketNanos = now;
        }
    }
}

// Testing
class RateLimiterTest {
    public static void main(String[] args) throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 2);
        
        for (int i = 0; i < 20; i++) {
            final int requestId = i;
            new Thread(() -> {
                if (limiter.tryAcquire()) {
                    System.out.println("Request " + requestId + " allowed");
                } else {
                    System.out.println("Request " + requestId + " denied");
                }
            }).start();
            Thread.sleep(100);
        }
    }
}
```

## Algorithm Comparison

| Algorithm | Pros | Cons |
|-----------|------|------|
| Token Bucket | Smooth rate, allows bursts | Complex refill logic |
| Sliding Window | Accurate rate limiting | Memory overhead |
| Fixed Window | Simple | Burst at boundaries |
| Leaky Bucket | Smooth output | Doesn't allow bursts |

## Complexity Analysis

**Time Complexity**: 
- Token Bucket: O(1)
- Sliding Window: O(n) where n is requests in window

**Space Complexity**:
- Token Bucket: O(1)
- Sliding Window: O(n)

## Edge Cases and Pitfalls

- **Clock skew**: System time changes can affect rate
- **Burst handling**: Token bucket allows bursts up to capacity
- **Thread safety**: Must synchronize token updates
- **Common Pitfall**: Not handling refill atomically

## Interview-Ready Answer

"A thread-safe rate limiter can be implemented using token bucket or sliding window algorithms. Token bucket maintains a bucket of tokens that refill at a fixed rate, allowing requests when tokens are available. It uses atomic operations and locks for thread safety. The algorithm supports both blocking (acquire) and non-blocking (tryAcquire) modes, and allows controlled bursts up to bucket capacity."

**Tags**: rate-limiter, token-bucket, concurrency
