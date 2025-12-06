# Multithreading - Interview Questions

This document contains 30 interview questions covering Multithreading, organized by difficulty level.

**Total Questions:** 30 (10 Easy, 10 Medium, 10 Hard)

---

## 🟢 Easy Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| mt-e-001 | Explain the difference between Thread and Runnable | Describe the differences between extending Thread class and implementing Runnable interface, including when to use each approach. | 5 | thread, runnable, fundamentals |
| mt-e-002 | What is thread synchronization? | Explain the concept of thread synchronization in Java and why it's necessary for concurrent programming. | 5 | synchronization, race-condition, fundamentals |
| mt-e-003 | Explain the synchronized keyword | Describe how the synchronized keyword works for methods and blocks, including its impact on thread execution. | 5 | synchronized, locks, mutual-exclusion |
| mt-e-004 | What are thread states in Java? | List and explain all thread states (NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED) and transitions between them. | 5 | thread-states, lifecycle, fundamentals |
| mt-e-005 | Explain wait(), notify(), and notifyAll() | Describe the purpose and usage of wait(), notify(), and notifyAll() methods for inter-thread communication. | 5 | wait-notify, inter-thread-communication, object-methods |
| mt-e-006 | What is the volatile keyword? | Explain how the volatile keyword ensures visibility of variable changes across threads. | 5 | volatile, memory-visibility, keywords |
| mt-e-007 | Difference between sleep() and wait() | Compare Thread.sleep() and Object.wait() methods in terms of lock release and usage context. | 5 | sleep, wait, thread-control |
| mt-e-008 | What is a daemon thread? | Explain daemon threads, how they differ from user threads, and when to use them. | 5 | daemon-thread, thread-types, fundamentals |
| mt-e-009 | Explain thread priority | Describe how thread priorities work in Java and their impact on thread scheduling. | 5 | thread-priority, scheduling, fundamentals |
| mt-e-010 | What is the join() method? | Explain the purpose of Thread.join() and how it's used to wait for thread completion. | 5 | join, thread-coordination, synchronization |

---

## 🟡 Medium Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| mt-m-001 | Implement a thread-safe singleton using double-checked locking | Create a thread-safe singleton pattern using double-checked locking with proper use of volatile keyword. | 15 | singleton, double-checked-locking, volatile |
| mt-m-002 | Explain deadlock and how to prevent it | Describe what causes deadlock in multithreaded programs and strategies to prevent or detect it. | 15 | deadlock, prevention, concurrency-issues |
| mt-m-003 | Implement Producer-Consumer pattern | Design a producer-consumer solution using wait/notify or BlockingQueue for thread-safe data exchange. | 20 | producer-consumer, blocking-queue, design-pattern |
| mt-m-004 | Explain ReentrantLock vs synchronized | Compare ReentrantLock with synchronized keyword in terms of features, flexibility, and use cases. | 15 | reentrantlock, synchronized, locks |
| mt-m-005 | What is ThreadLocal and when to use it? | Explain ThreadLocal variables, their use cases, and potential memory leak issues. | 15 | threadlocal, thread-confinement, memory |
| mt-m-006 | Implement a thread pool using ExecutorService | Create a thread pool using ExecutorService to manage concurrent task execution efficiently. | 15 | executor-service, thread-pool, concurrency |
| mt-m-007 | Explain CountDownLatch and CyclicBarrier | Compare CountDownLatch and CyclicBarrier synchronization aids, including their use cases and differences. | 15 | countdownlatch, cyclicbarrier, synchronizers |
| mt-m-008 | What is the Fork/Join framework? | Explain the Fork/Join framework for parallel task decomposition and work-stealing algorithm. | 15 | fork-join, parallel, work-stealing |
| mt-m-009 | Implement a thread-safe counter | Create a thread-safe counter using different approaches: synchronized, AtomicInteger, and locks. | 15 | atomic, thread-safety, counter |
| mt-m-010 | Explain Semaphore and its use cases | Describe how Semaphore works for controlling access to resources and implementing resource pools. | 15 | semaphore, resource-control, synchronizers |

---

## 🔴 Hard Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| mt-h-001 | Implement a custom ReadWriteLock | Design and implement a ReadWriteLock that allows multiple readers or a single writer with proper fairness. | 30 | readwritelock, custom-lock, concurrency |
| mt-h-002 | Explain the Java Memory Model and happens-before | Describe the Java Memory Model, happens-before relationships, and how they guarantee memory visibility. | 25 | memory-model, happens-before, visibility |
| mt-h-003 | Implement a lock-free data structure | Create a lock-free stack or queue using compare-and-swap (CAS) operations and atomic references. | 35 | lock-free, cas, atomic |
| mt-h-004 | Design a custom thread pool with work stealing | Implement a thread pool with work-stealing algorithm for load balancing across worker threads. | 35 | thread-pool, work-stealing, load-balancing |
| mt-h-005 | Implement a concurrent LRU cache | Design a thread-safe LRU cache with O(1) operations using fine-grained locking or lock-free techniques. | 35 | lru-cache, concurrent, fine-grained-locking |
| mt-h-006 | Explain StampedLock and optimistic locking | Describe StampedLock's optimistic read mode and when it provides better performance than ReadWriteLock. | 25 | stampedlock, optimistic-locking, performance |
| mt-h-007 | Implement a Phaser for multi-phase synchronization | Use Phaser to coordinate multiple phases of parallel computation with dynamic thread registration. | 30 | phaser, multi-phase, synchronizers |
| mt-h-008 | Design a rate limiter with thread safety | Implement a thread-safe rate limiter using token bucket or sliding window algorithm. | 30 | rate-limiter, token-bucket, concurrency |
| mt-h-009 | Explain false sharing and cache line padding | Describe false sharing problem in concurrent programs and how to prevent it using cache line padding. | 25 | false-sharing, cache-lines, performance |
| mt-h-010 | Implement a concurrent skip list | Design a lock-free or fine-grained locking skip list for concurrent sorted data structure operations. | 40 | skip-list, concurrent, lock-free |

---

## Summary Statistics

- **Easy Questions:** 10 (Average time: 5 minutes)
- **Medium Questions:** 10 (Average time: 15.5 minutes)
- **Hard Questions:** 10 (Average time: 31 minutes)
- **Total Questions:** 30
- **Total Estimated Time:** 515 minutes (~8.6 hours)

## Question Categories

The questions cover the following key areas:
- Thread Fundamentals & Lifecycle
- Synchronization Mechanisms
- Locks & Concurrent Utilities
- Thread Pools & Executors
- Memory Model & Visibility
- Lock-Free Programming
- Advanced Synchronizers
- Performance Optimization
- Concurrent Data Structures
- Design Patterns for Concurrency

