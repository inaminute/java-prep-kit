# Spring Boot - Interview Questions

This document contains 30 interview questions covering Spring Boot, organized by difficulty level.

**Total Questions:** 30 (10 Easy, 10 Medium, 10 Hard)

---

## 🟢 Easy Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| sb-e-001 | What is Spring Boot and how does it differ from Spring Framework? | Explain the purpose of Spring Boot, its key features, and how it simplifies Spring application development compared to traditional Spring Framework. | 5 | fundamentals, spring-boot, spring-framework |
| sb-e-002 | Explain the @SpringBootApplication annotation | Describe what the @SpringBootApplication annotation does and which annotations it combines. | 5 | annotations, configuration, fundamentals |
| sb-e-003 | What is dependency injection in Spring? | Explain the concept of dependency injection and how Spring implements it using @Autowired, constructor injection, and setter injection. | 5 | dependency-injection, ioc, fundamentals |
| sb-e-004 | Difference between @Component, @Service, and @Repository | Explain the purpose and differences between @Component, @Service, @Repository, and @Controller stereotype annotations. | 5 | annotations, stereotypes, architecture |
| sb-e-005 | What is application.properties and application.yml? | Describe how to configure Spring Boot applications using application.properties or application.yml files. | 5 | configuration, properties, fundamentals |
| sb-e-006 | Explain Spring Boot Actuator | Describe what Spring Boot Actuator provides and list common actuator endpoints for monitoring and management. | 5 | actuator, monitoring, production |
| sb-e-007 | What are Spring profiles? | Explain how Spring profiles work and how to use them for environment-specific configuration (dev, test, prod). | 5 | profiles, configuration, environments |
| sb-e-008 | How to create a simple REST API in Spring Boot? | Describe the steps and annotations needed to create a basic REST controller with GET and POST endpoints. | 5 | rest-api, controllers, web |
| sb-e-009 | What is the difference between @RequestParam and @PathVariable? | Explain when to use @RequestParam versus @PathVariable for extracting data from HTTP requests. | 5 | rest-api, annotations, web |
| sb-e-010 | Explain the @Bean annotation | Describe how the @Bean annotation works in @Configuration classes to define Spring-managed beans. | 5 | annotations, configuration, beans |

---

## 🟡 Medium Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| sb-m-001 | Implement exception handling in Spring Boot REST API | Create a global exception handler using @ControllerAdvice and @ExceptionHandler to return proper HTTP status codes and error responses. | 15 | exception-handling, rest-api, error-handling |
| sb-m-002 | Explain Spring Data JPA and repository pattern | Describe how Spring Data JPA simplifies database access and implement a repository with custom query methods. | 15 | spring-data, jpa, database |
| sb-m-003 | Implement request validation using Bean Validation | Use @Valid and validation annotations (@NotNull, @Size, @Email) to validate request bodies in REST controllers. | 15 | validation, rest-api, bean-validation |
| sb-m-004 | What is Spring Boot auto-configuration and how does it work? | Explain the auto-configuration mechanism in Spring Boot, including conditional annotations and how to create custom auto-configuration. | 15 | auto-configuration, spring-boot, advanced |
| sb-m-005 | Implement pagination and sorting in Spring Data | Create REST endpoints that support pagination and sorting using Pageable and Sort parameters. | 15 | spring-data, pagination, rest-api |
| sb-m-006 | Explain Spring transaction management | Describe how @Transactional works, transaction propagation levels, and isolation levels in Spring. | 15 | transactions, database, spring-data |
| sb-m-007 | Implement caching in Spring Boot | Configure and use Spring's caching abstraction with @Cacheable, @CacheEvict, and @CachePut annotations. | 15 | caching, performance, spring-cache |
| sb-m-008 | Create a custom Spring Boot starter | Design and implement a custom Spring Boot starter with auto-configuration for a reusable library. | 20 | starters, auto-configuration, libraries |
| sb-m-009 | Implement API versioning strategies | Describe and implement different REST API versioning approaches (URI, header, content negotiation) in Spring Boot. | 15 | rest-api, versioning, design |
| sb-m-010 | Configure and use Spring Security basics | Set up basic authentication and authorization in a Spring Boot application using Spring Security. | 20 | security, authentication, authorization |

---

## 🔴 Hard Questions

| ID | Title | Description | Time (min) | Tags |
|---|---|---|---|---|
| sb-h-001 | Implement JWT authentication with Spring Security | Create a complete JWT-based authentication system with token generation, validation, and refresh token mechanism. | 30 | security, jwt, authentication |
| sb-h-002 | Design a multi-tenancy architecture in Spring Boot | Implement a multi-tenant application with separate database schemas or databases per tenant using Spring Boot. | 35 | multi-tenancy, architecture, database |
| sb-h-003 | Implement event-driven architecture with Spring Events | Design an event-driven system using Spring's ApplicationEvent, @EventListener, and asynchronous event processing. | 25 | events, async, architecture |
| sb-h-004 | Create a custom Spring AOP aspect for cross-cutting concerns | Implement aspect-oriented programming with custom aspects for logging, auditing, or performance monitoring. | 30 | aop, aspects, cross-cutting |
| sb-h-005 | Implement distributed tracing with Spring Cloud Sleuth | Set up distributed tracing across microservices using Spring Cloud Sleuth and Zipkin for request correlation. | 30 | distributed-tracing, microservices, observability |
| sb-h-006 | Design a reactive REST API with Spring WebFlux | Create a non-blocking reactive REST API using Spring WebFlux, Mono, Flux, and reactive database drivers. | 35 | reactive, webflux, non-blocking |
| sb-h-007 | Implement circuit breaker pattern with Resilience4j | Add fault tolerance to microservices using Resilience4j circuit breaker, retry, and rate limiter patterns. | 30 | resilience, fault-tolerance, microservices |
| sb-h-008 | Create a custom Spring Boot metrics and monitoring solution | Implement custom metrics using Micrometer, integrate with Prometheus, and create Grafana dashboards. | 30 | metrics, monitoring, observability |
| sb-h-009 | Implement optimistic and pessimistic locking strategies | Design and implement both optimistic locking (@Version) and pessimistic locking strategies for concurrent data access. | 25 | concurrency, locking, jpa |
| sb-h-010 | Design a CQRS pattern implementation with Spring Boot | Implement Command Query Responsibility Segregation pattern with separate read and write models using Spring Boot. | 35 | cqrs, architecture, design-patterns |

---

## Summary Statistics

- **Easy Questions:** 10 (Average time: 5 minutes)
- **Medium Questions:** 10 (Average time: 16 minutes)
- **Hard Questions:** 10 (Average time: 31 minutes)
- **Total Questions:** 30
- **Total Estimated Time:** 520 minutes (~8.7 hours)

## Question Categories

The questions cover the following key areas:
- Spring Boot Fundamentals
- REST API Development
- Spring Data JPA & Database Access
- Security & Authentication
- Caching & Performance
- Microservices Patterns
- Reactive Programming
- Monitoring & Observability
- Transaction Management
- Advanced Architecture Patterns

