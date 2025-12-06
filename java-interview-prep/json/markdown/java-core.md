# Java Core Concepts - Interview Questions

This document contains 30 interview questions covering Java Core Concepts, organized by difficulty level.

**Total Questions:** 30 (10 Easy, 10 Medium, 10 Hard)

---

## 🟢 Easy Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| jc-e-001 | Explain the difference between == and equals() in Java | Describe when to use == versus equals() method for comparing objects in Java, including primitives and reference types. | 5 | operators, object-comparison, fundamentals |
| jc-e-002 | What is the difference between String, StringBuilder, and StringBuffer? | Compare and contrast String, StringBuilder, and StringBuffer classes in terms of mutability, thread-safety, and performance. | 5 | strings, immutability, concurrency |
| jc-e-003 | Explain method overloading vs method overriding | Describe the differences between method overloading and method overriding with examples of when to use each. | 5 | oop, polymorphism, inheritance |
| jc-e-004 | What are the access modifiers in Java? | List and explain all access modifiers in Java (public, private, protected, default) and their visibility scope. | 5 | access-modifiers, encapsulation, fundamentals |
| jc-e-005 | Explain the final keyword in Java | Describe how the final keyword works when applied to variables, methods, and classes. | 5 | keywords, immutability, inheritance |
| jc-e-006 | What is autoboxing and unboxing? | Explain the automatic conversion between primitive types and their corresponding wrapper classes in Java. | 5 | primitives, wrapper-classes, type-conversion |
| jc-e-007 | Difference between ArrayList and LinkedList | Compare ArrayList and LinkedList in terms of internal structure, performance characteristics, and use cases. | 5 | collections, data-structures, performance |
| jc-e-008 | What is the purpose of the static keyword? | Explain how static works for variables, methods, blocks, and nested classes in Java. | 5 | keywords, static, memory-management |
| jc-e-009 | Explain checked vs unchecked exceptions | Describe the difference between checked and unchecked exceptions, with examples of each type. | 5 | exceptions, error-handling, fundamentals |
| jc-e-010 | What is the difference between abstract class and interface? | Compare abstract classes and interfaces in Java, including when to use each and their key differences. | 5 | oop, abstraction, interfaces |

---

## 🟡 Medium Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| jc-m-001 | Implement a custom immutable class | Design and implement a fully immutable class in Java following best practices, including handling mutable fields. | 15 | immutability, design, best-practices |
| jc-m-002 | Explain the contract between equals() and hashCode() | Describe the relationship between equals() and hashCode() methods and why they must be overridden together. | 10 | object-methods, collections, contracts |
| jc-m-003 | How does HashMap work internally? | Explain the internal implementation of HashMap including hashing, collision handling, and resizing mechanism. | 15 | collections, hashmap, data-structures |
| jc-m-004 | Implement a singleton pattern with thread safety | Create a thread-safe singleton implementation using different approaches (eager, lazy, double-checked locking, enum). | 15 | design-patterns, singleton, thread-safety |
| jc-m-005 | Explain Java memory model and garbage collection | Describe the Java memory structure (heap, stack, metaspace) and how garbage collection works. | 15 | memory-management, gc, jvm |
| jc-m-006 | What are generics and type erasure? | Explain Java generics, type parameters, bounded types, and the concept of type erasure at runtime. | 15 | generics, type-system, compile-time |
| jc-m-007 | Implement a custom exception hierarchy | Design a custom exception hierarchy for a specific domain with proper use of checked and unchecked exceptions. | 15 | exceptions, design, error-handling |
| jc-m-008 | Explain the Comparable vs Comparator interfaces | Compare Comparable and Comparator interfaces, when to use each, and implement custom sorting logic. | 10 | interfaces, sorting, collections |
| jc-m-009 | What is reflection and when should you use it? | Explain Java reflection API, its capabilities, use cases, and performance implications. | 15 | reflection, metaprogramming, advanced |
| jc-m-010 | Implement a deep copy mechanism | Create a mechanism to perform deep copy of objects including nested objects and collections. | 15 | cloning, object-copying, design |

---

## 🔴 Hard Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| jc-h-001 | Design a custom class loader | Implement a custom ClassLoader that can load classes from non-standard sources with proper delegation. | 30 | classloading, jvm, advanced |
| jc-h-002 | Implement a thread-safe LRU cache | Design and implement a Least Recently Used (LRU) cache with O(1) operations that is thread-safe. | 30 | caching, data-structures, concurrency |
| jc-h-003 | Explain JVM internals and bytecode | Describe how the JVM executes bytecode, including class loading, verification, JIT compilation, and optimization. | 25 | jvm, bytecode, performance |
| jc-h-004 | Implement a custom annotation processor | Create a compile-time annotation processor that generates code or validates annotations. | 30 | annotations, metaprogramming, code-generation |
| jc-h-005 | Design a type-safe heterogeneous container | Implement a container that can hold different types safely using generics and type tokens. | 25 | generics, type-safety, design-patterns |
| jc-h-006 | Explain weak, soft, and phantom references | Describe the different types of references in Java and their use cases for memory-sensitive caching. | 20 | memory-management, gc, references |
| jc-h-007 | Implement a custom serialization mechanism | Create a custom serialization framework that handles circular references and version compatibility. | 30 | serialization, design, advanced |
| jc-h-008 | Design a dynamic proxy with method interception | Implement a dynamic proxy using Java's Proxy class or CGLIB that intercepts method calls for AOP-like behavior. | 30 | proxy, aop, reflection |
| jc-h-009 | Explain happens-before relationship and memory visibility | Describe the Java Memory Model's happens-before guarantees and how they ensure memory visibility across threads. | 25 | concurrency, memory-model, jvm |
| jc-h-010 | Implement a custom concurrent collection | Design a thread-safe collection with fine-grained locking or lock-free algorithms for high concurrency. | 35 | concurrency, collections, lock-free |

---

## Summary Statistics

- **Easy Questions:** 10 (Average time: 5 minutes)
- **Medium Questions:** 10 (Average time: 14 minutes)
- **Hard Questions:** 10 (Average time: 28 minutes)
- **Total Questions:** 30
- **Total Estimated Time:** 470 minutes (~7.8 hours)

## Question Categories

The questions cover the following key areas:
- Object-Oriented Programming (OOP)
- Collections Framework
- Memory Management & GC
- Concurrency & Thread Safety
- Generics & Type System
- Exception Handling
- JVM Internals
- Design Patterns
- Advanced Java Features
