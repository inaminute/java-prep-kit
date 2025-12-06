# Java 8 Streams - Interview Questions

This document contains 30 interview questions covering Java 8 Streams API, organized by difficulty level.

**Total Questions:** 30 (10 Easy, 10 Medium, 10 Hard)

---

## 🟢 Easy Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| str-e-001 | Filter and collect elements from a list | Given a list of integers, filter out even numbers and collect them into a new list using Stream API. | 5 | filter, collect, lambda |
| str-e-002 | Map strings to uppercase | Convert a list of strings to uppercase using the map() operation. | 5 | map, method-reference, transformation |
| str-e-003 | Find the sum of integers using reduce | Calculate the sum of all integers in a list using the reduce() operation. | 5 | reduce, aggregation, sum |
| str-e-004 | Check if any element matches a condition | Use anyMatch() to check if a list contains any element satisfying a given predicate. | 5 | anyMatch, predicate, short-circuit |
| str-e-005 | Sort a list of strings | Sort a list of strings in natural order using Stream API and collect the result. | 5 | sorted, ordering, collect |
| str-e-006 | Find distinct elements | Remove duplicate elements from a list using the distinct() operation. | 5 | distinct, deduplication, set |
| str-e-007 | Limit and skip elements | Use limit() and skip() to implement pagination on a stream of elements. | 5 | limit, skip, pagination |
| str-e-008 | Count elements in a stream | Count the number of elements in a stream that match a specific condition. | 5 | count, filter, terminal-operation |
| str-e-009 | Find first element matching condition | Use findFirst() to retrieve the first element that matches a predicate. | 5 | findFirst, optional, short-circuit |
| str-e-010 | Convert stream to array | Collect stream elements into an array using toArray() method. | 5 | toArray, collection, array |

---

## 🟡 Medium Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| str-m-001 | Group elements by property | Use Collectors.groupingBy() to group a list of objects by a specific property. | 15 | groupingBy, collectors, map |
| str-m-002 | Partition elements by predicate | Use Collectors.partitioningBy() to split a collection into two groups based on a predicate. | 15 | partitioningBy, collectors, predicate |
| str-m-003 | FlatMap nested collections | Flatten a list of lists into a single stream using flatMap() operation. | 15 | flatMap, nested-collections, flattening |
| str-m-004 | Find max and min with custom comparator | Use max() and min() operations with a custom Comparator to find extreme values. | 10 | max, min, comparator |
| str-m-005 | Collect to Map with key and value mappers | Use Collectors.toMap() to create a map from a stream with custom key and value extractors. | 15 | toMap, collectors, key-value |
| str-m-006 | Joining strings with delimiter | Use Collectors.joining() to concatenate strings with a delimiter, prefix, and suffix. | 10 | joining, string-concatenation, collectors |
| str-m-007 | Calculate average using collectors | Compute the average of numeric values using Collectors.averagingInt/Double/Long(). | 10 | averaging, statistics, collectors |
| str-m-008 | Peek for debugging streams | Use peek() to perform side-effect operations for debugging without affecting the stream. | 10 | peek, debugging, side-effects |
| str-m-009 | Implement custom collector | Create a custom Collector implementation using Collector.of() for specialized aggregation. | 20 | custom-collector, collector-of, advanced |
| str-m-010 | Summarizing statistics | Use Collectors.summarizingInt/Double/Long() to get count, sum, min, max, and average in one pass. | 10 | statistics, summarizing, collectors |

---

## 🔴 Hard Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| str-h-001 | Parallel stream processing with thread safety | Implement parallel stream processing ensuring thread-safe operations and proper use of concurrent collectors. | 25 | parallel-streams, concurrency, thread-safety |
| str-h-002 | Downstream collectors with groupingBy | Use nested collectors with groupingBy() to perform complex aggregations on grouped data. | 25 | groupingBy, downstream-collectors, nested |
| str-h-003 | Implement lazy evaluation with Stream.generate | Create infinite streams using Stream.generate() or Stream.iterate() with proper termination conditions. | 20 | infinite-streams, lazy-evaluation, generate |
| str-h-004 | Optimize stream pipeline performance | Analyze and optimize a complex stream pipeline for performance, considering short-circuiting and operation order. | 25 | optimization, performance, pipeline |
| str-h-005 | Implement teeing collector | Use Collectors.teeing() to apply two collectors simultaneously and merge their results. | 20 | teeing, collectors, java12 |
| str-h-006 | Handle exceptions in stream operations | Design a strategy to handle checked exceptions within stream lambda expressions elegantly. | 25 | exception-handling, lambda, functional |
| str-h-007 | Implement custom spliterator | Create a custom Spliterator for efficient parallel processing of a custom data structure. | 30 | spliterator, parallel, custom |
| str-h-008 | Reduce with complex accumulator | Implement a complex reduction operation using reduce() with identity, accumulator, and combiner for parallel streams. | 25 | reduce, parallel, combiner |
| str-h-009 | Stream of optionals handling | Process a stream of Optional values efficiently, filtering empty values and extracting present values. | 20 | optional, flatMap, filtering |
| str-h-010 | Implement sliding window with streams | Create a sliding window operation over a stream to process consecutive elements in groups. | 30 | sliding-window, stateful, custom-collector |

---

## Summary Statistics

- **Easy Questions:** 10 (Average time: 5 minutes)
- **Medium Questions:** 10 (Average time: 13 minutes)
- **Hard Questions:** 10 (Average time: 25 minutes)
- **Total Questions:** 30
- **Total Estimated Time:** 430 minutes (~7.2 hours)

## Question Categories

The questions cover the following key areas:
- Stream Operations (filter, map, reduce, flatMap)
- Collectors API (groupingBy, partitioningBy, toMap, joining)
- Terminal Operations (collect, count, findFirst, anyMatch)
- Intermediate Operations (sorted, distinct, limit, skip, peek)
- Parallel Streams & Performance
- Custom Collectors & Spliterators
- Functional Programming Patterns
- Exception Handling in Streams
- Advanced Stream Techniques

