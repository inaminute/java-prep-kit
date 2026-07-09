# Food Delivery Platform - Frequently Asked Questions

## Table of Contents
- [Maven Project Structure](#maven-project-structure)
- [Service Registry (Eureka)](#service-registry-eureka)
- [Config Server](#config-server)

---

## Maven Project Structure

### Why do we need a parent pom.xml file?

The parent `pom.xml` solves several critical problems in a multi-module microservices project:

#### 1. Centralized Dependency Management
Instead of defining Spring Boot version, PostgreSQL version, Kafka version, etc. in each of the 10 services, you define them once in the parent. This means:
- All services use the **same compatible versions**
- Update a dependency version in one place, not 10 places
- Avoid version conflicts between services

```xml
<!-- Without parent: Define in EVERY service -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>  <!-- Repeated 7 times! -->
</dependency>

<!-- With parent: Define once, inherit everywhere -->
```

#### 2. Consistent Build Configuration
All services get the same:
- Java version (17)
- Compiler settings
- Plugin configurations (Lombok, MapStruct processors)
- Build behavior

This prevents "works on my service but not yours" issues.

#### 3. Single Command Builds
From the root directory:
```bash
mvn clean install  # Builds ALL 10 services in correct order
```

Without parent POM, you'd need to build each service individually and manage build order manually.

#### 4. Shared Dependencies
Common dependencies like Lombok and Spring Boot Test are defined once in the parent's `<dependencies>` section and automatically inherited by all children.

#### 5. Version Compatibility
Spring Boot 3.2.0 requires specific versions of Spring Cloud, Kafka, etc. The parent POM ensures all these versions are compatible with each other across all services.

#### Real Example from Your Project

Your `order-service` needs to talk to `restaurant-service` via Feign. Both need compatible Spring Cloud versions. The parent POM guarantees this:

```xml
<!-- Parent ensures both services use Spring Cloud 2023.0.0 -->
<spring-cloud.version>2023.0.0</spring-cloud.version>
```

Without it, one service might use Spring Cloud 2022.x and another 2023.x, causing runtime failures.

**Bottom line**: For a single microservice, you don't need a parent POM. For 10+ microservices that need to work together, it's essential for maintainability and consistency.

---

### How do microservices inherit from parent pom?

The inheritance works through a two-way connection:

#### 1. Parent declares its children (in root `pom.xml`):

```xml
<modules>
    <module>service-registry</module>
    <module>order-service</module>
    <module>user-service</module>
    <!-- etc -->
</modules>
```

This tells Maven: "These directories contain my child modules."

#### 2. Children declare their parent (in each service's `pom.xml`):

```xml
<parent>
    <groupId>com.fooddelivery</groupId>
    <artifactId>food-delivery-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>

<artifactId>order-service</artifactId>
```

This tells Maven: "I inherit from `food-delivery-platform`."

#### What Gets Inherited

**Inherited Automatically:**

1. **Dependency Versions** - Notice `order-service` doesn't specify versions:
```xml
<!-- In order-service/pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <!-- NO VERSION! Inherited from parent's <dependencyManagement> -->
</dependency>
```

The version `42.7.1` comes from the parent's `<dependencyManagement>` section.

2. **Properties** - All these are inherited:
```xml
<!-- From parent pom.xml -->
<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <!-- order-service automatically gets these -->
</properties>
```

3. **Common Dependencies** - These are in EVERY child automatically:
```xml
<!-- From parent's <dependencies> section -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

4. **Build Configuration** - Compiler settings, plugins, etc.

#### Visual Example

```
Parent pom.xml
├── Properties: java.version=17, postgresql.version=42.7.1
├── DependencyManagement: PostgreSQL 42.7.1, Kafka 3.6.0
├── Dependencies: Lombok (all children get this)
└── Build: Java 17 compiler, Lombok processor

        ↓ INHERITS ↓

order-service/pom.xml
├── Gets: java.version=17 ✓
├── Gets: postgresql.version=42.7.1 ✓
├── Gets: Lombok dependency ✓
├── Gets: Java 17 compiler ✓
└── Only declares: "I want PostgreSQL" (version comes from parent)
```

#### The Magic

When you run `mvn clean install` in `order-service/`, Maven:
1. Reads `order-service/pom.xml`
2. Sees `<parent>` declaration
3. Looks up one directory and finds parent `pom.xml`
4. Merges parent's configuration with child's
5. Builds with combined configuration

This is why `order-service` can declare dependencies without versions - Maven looks them up in the parent's `<dependencyManagement>` section.

---

## Service Registry (Eureka)

### Why do we need Service Registry?

Service Registry (Eureka) acts as a "phone book" for microservices, solving the critical problem of service discovery.

#### The Problem It Solves

Imagine your `order-service` needs to call `restaurant-service` to validate menu items. Where is `restaurant-service` running?

**Without Service Registry:**
```java
// Hard-coded URL - BAD!
String url = "http://localhost:8083/api/restaurants/validate";
// What if restaurant-service moves to a different server?
// What if you have 3 instances running on different ports?
// What if one instance crashes?
```

**With Service Registry:**
```java
// Just use the service name - GOOD!
@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/api/restaurants/validate")
    ValidationResponse validate(@RequestBody List<Long> itemIds);
}
// Eureka automatically finds available instances
// Load balances between multiple instances
// Skips crashed instances
```

#### How It Works

1. **Registration**: When `restaurant-service` starts:
   ```
   restaurant-service → "Hi Eureka, I'm running at 192.168.1.10:8083"
   ```

2. **Discovery**: When `order-service` needs it:
   ```
   order-service → "Eureka, where is restaurant-service?"
   Eureka → "Here are 3 healthy instances: 
             - 192.168.1.10:8083
             - 192.168.1.11:8083
             - 192.168.1.12:8083"
   ```

3. **Health Checks**: Eureka pings services every 30 seconds. If one dies, it's removed from the registry.

#### Real Scenario from Your Platform

```
Customer places order:
1. API Gateway → "Where is order-service?" → Eureka
2. Order Service → "Where is restaurant-service?" → Eureka
3. Order Service → "Where is payment-service?" → Eureka
4. Order Service → "Where is delivery-service?" → Eureka
```

Without Eureka, you'd need to hard-code all these URLs and manually update them when services move or scale.

---

## Config Server

### Why do we need Config Server?

Config Server acts as a "settings manager" for all microservices, providing centralized configuration management.

#### The Problem It Solves

You have 10 microservices, each needs configuration:
- Database URLs
- Kafka broker addresses
- Redis connection strings
- API keys
- Feature flags

**Without Config Server:**
```
order-service/src/main/resources/application.yml
user-service/src/main/resources/application.yml
payment-service/src/main/resources/application.yml
...

Need to change Kafka URL? 
→ Update 7 different files
→ Rebuild 7 services
→ Redeploy 7 services
```

**With Config Server:**
```
config-server/config-repo/
├── application.yml          # Shared by ALL services
├── order-service.yml        # Order service specific
├── payment-service.yml      # Payment service specific
└── application-prod.yml     # Production overrides

Change Kafka URL?
→ Update ONE file in config repo
→ Services refresh automatically (no rebuild/redeploy!)
```

#### How It Works

1. **Centralized Storage**: All configs in one Git repo or file system
2. **Service Startup**: Each service asks Config Server for its configuration
3. **Environment-Specific**: Different configs for dev, staging, prod
4. **Dynamic Refresh**: Change config without restarting services

#### Real Scenario from Your Platform

**Shared Configuration** (all services need this):
```yaml
# application.yml in Config Server
spring:
  kafka:
    bootstrap-servers: kafka.prod.company.com:9092

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

**Service-Specific Configuration**:
```yaml
# order-service.yml in Config Server
spring:
  datasource:
    url: jdbc:postgresql://order-db:5432/orders
    
order:
  saga:
    timeout: 30s
  max-items-per-order: 50
```

**Environment Override**:
```yaml
# application-prod.yml in Config Server
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Higher for production
```

#### The Power Combo

When `order-service` starts:
1. Asks **Config Server**: "Give me my configuration"
2. Gets: `application.yml` + `order-service.yml` + `application-prod.yml` (merged)
3. Asks **Eureka**: "I'm order-service, here's my location"
4. Now it can find other services and has all its settings

---

## Why Both Service Registry and Config Server Are Essential

| Problem | Solution |
|---------|----------|
| Where are my services? | Service Registry |
| What are my settings? | Config Server |
| Service moved to new IP | Registry auto-updates |
| Need to change DB password | Update Config Server once |
| Scale to 5 instances | Registry tracks all instances |
| Different configs per environment | Config Server profiles |

Without these, managing 10 microservices across dev/staging/prod environments would be a nightmare of hard-coded URLs and scattered configuration files.

---

---

## Spring Data JPA

### How does Spring create methods based on method signatures in repository interfaces?

Spring Data JPA uses a powerful mechanism called **query derivation** to automatically generate method implementations from method names. Here's how it works in the auth-service:

#### The Magic Behind the Scenes

When you extend `JpaRepository`, Spring Data JPA creates a proxy implementation at runtime that:

1. **Parses the method name** - Breaks down the method signature into keywords and property names
2. **Generates JPQL/SQL** - Converts the parsed structure into actual database queries
3. **Executes the query** - Runs it against your database
4. **Returns the result** - Maps the result to your specified return type

#### Examples from Auth-Service

**1. `findByEmail(String email)`**
```java
Optional<UserCredential> findByEmail(String email);
```
- **Parsed as**: `find` + `By` + `Email`
- **Generated query**: `SELECT * FROM user_credential WHERE email = ?`
- Spring knows `email` is a field in `UserCredential` entity and creates the WHERE clause automatically

**2. `findByUserId(Long userId)`**
```java
Optional<UserCredential> findByUserId(Long userId);
```
- **Parsed as**: `find` + `By` + `UserId`
- **Generated query**: `SELECT * FROM user_credential WHERE user_id = ?`

**3. `existsByEmail(String email)`**
```java
boolean existsByEmail(String email);
```
- **Parsed as**: `exists` + `By` + `Email`
- **Generated query**: `SELECT COUNT(*) > 0 FROM user_credential WHERE email = ?`
- Returns boolean instead of entity

**4. `findByToken(String token)`**
```java
Optional<RefreshToken> findByToken(String token);
```
- Works on the `RefreshToken` entity
- **Generated query**: `SELECT * FROM refresh_token WHERE token = ?`

#### Naming Convention Keywords

Spring supports various keywords in method names:

- **find...By**, **read...By**, **get...By**, **query...By** - Retrieve entities
- **exists...By** - Check existence (returns boolean)
- **count...By** - Count matching records
- **delete...By**, **remove...By** - Delete entities
- **...And**, **...Or** - Combine conditions (e.g., `findByEmailAndEnabled`)
- **...OrderBy** - Sort results (e.g., `findByRoleOrderByCreatedAtDesc`)
- **...Like**, **...Containing**, **...StartingWith** - Pattern matching
- **...GreaterThan**, **...LessThan**, **...Between** - Comparisons

#### More Complex Examples

**Combining conditions:**
```java
Optional<UserCredential> findByEmailAndEnabled(String email, boolean enabled);
// Generated: SELECT * FROM user_credential WHERE email = ? AND enabled = ?
```

**Sorting:**
```java
List<UserCredential> findByRoleOrderByCreatedAtDesc(UserRole role);
// Generated: SELECT * FROM user_credential WHERE role = ? ORDER BY created_at DESC
```

**Pattern matching:**
```java
List<UserCredential> findByEmailContaining(String emailPart);
// Generated: SELECT * FROM user_credential WHERE email LIKE %?%
```

**Comparisons:**
```java
List<RefreshToken> findByExpiresAtBefore(LocalDateTime dateTime);
// Generated: SELECT * FROM refresh_token WHERE expires_at < ?
```

#### Custom Queries with @Query

When method names get too complex, you can use `@Query` annotations like in `RefreshTokenRepository`:

```java
@Modifying
@Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
void deleteByUserId(Long userId);
```

This gives you full control over the JPQL while still benefiting from Spring's parameter binding and transaction management.

#### How Spring Knows What to Do

1. **Entity inspection** - Spring scans your `@Entity` classes to understand field names and types
2. **Method name parsing** - Uses regex patterns to break down method names
3. **Validation** - At startup, Spring validates that all method names can be parsed and match entity fields
4. **Proxy creation** - Creates a dynamic proxy implementing your interface with the generated logic

#### Why You Never Write Implementation Classes

This is why you never write implementation classes for repositories - Spring generates everything at runtime based on:
- Your method signatures
- The entity structure
- JPA annotations

If Spring can't parse a method name or the fields don't exist in the entity, it will fail at startup with a clear error message, helping you catch mistakes early.

#### Performance Considerations

- Spring generates these queries once at startup, not on every call
- The generated queries are optimized and use prepared statements
- For complex queries, `@Query` with native SQL can be more performant
- Method name queries are perfect for simple CRUD operations

---

## Additional Resources

- [Spring Cloud Netflix Eureka Documentation](https://spring.io/projects/spring-cloud-netflix)
- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Microservices Patterns](https://microservices.io/patterns/index.html)
- [Spring Data JPA Query Methods Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods)


---

## High Availability and Production Concerns

### Why would we need multiple Eureka servers in production?

Multiple Eureka servers solve a critical **single point of failure** problem. Let me explain with real scenarios:

#### The Problem: Single Eureka Server

Imagine you have only ONE Eureka server and it crashes:

```
                    ❌ EUREKA SERVER CRASHES ❌
                              ↓
    ┌─────────────────────────────────────────────┐
    │  All 10 microservices are still running     │
    │  BUT they can't find each other anymore!    │
    └─────────────────────────────────────────────┘

Order Service: "Where is Restaurant Service?" → No answer
Payment Service: "Where is Order Service?" → No answer
Delivery Service: "Where is Order Service?" → No answer

Result: ENTIRE PLATFORM GOES DOWN even though services are healthy!
```

#### The Solution: Multiple Eureka Servers

With 2+ Eureka servers, they form a **peer-to-peer cluster**:

```
        Eureka Server 1              Eureka Server 2
        (Primary)                    (Backup)
             ↕                            ↕
        Sync registry ←→ Sync registry
             ↓                            ↓
    Services register with BOTH servers
```

#### How It Works

**Normal Operation:**
```
Order Service registers with:
  → Eureka Server 1 ✓
  → Eureka Server 2 ✓

Restaurant Service registers with:
  → Eureka Server 1 ✓
  → Eureka Server 2 ✓

Both Eureka servers sync their registries
```

**When One Fails:**
```
Eureka Server 1: ❌ CRASHED
Eureka Server 2: ✓ Still running

Order Service: "Where is Restaurant Service?"
  → Tries Eureka Server 1: ❌ No response
  → Tries Eureka Server 2: ✓ Gets answer!

Platform continues working! ✓
```

#### Real-World Scenario from Your Platform

**Friday Night - Peak Order Time:**

**Without HA (Single Eureka):**
```
8:00 PM - 1000 orders/minute
8:15 PM - Eureka server crashes (memory issue)
8:15 PM - All services lose service discovery
8:15 PM - Order placement STOPS
8:15 PM - Customers can't order
8:20 PM - Ops team notices and restarts Eureka
8:25 PM - Services re-register
8:25 PM - Platform back online

Lost: 10 minutes = ~10,000 orders = $$$$$
```

**With HA (Multiple Eureka):**
```
8:00 PM - 1000 orders/minute
8:15 PM - Eureka Server 1 crashes
8:15 PM - Services automatically use Eureka Server 2
8:15 PM - Orders continue without interruption
8:20 PM - Ops team notices and restarts Eureka Server 1
8:25 PM - Eureka Server 1 rejoins cluster

Lost: 0 orders = $0
```

#### Configuration Example

**Eureka Server 1 (Primary):**
```yaml
eureka:
  instance:
    hostname: eureka-server-1
  client:
    service-url:
      defaultZone: http://eureka-server-2:8761/eureka/  # Points to peer
```

**Eureka Server 2 (Backup):**
```yaml
eureka:
  instance:
    hostname: eureka-server-2
  client:
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka/  # Points to peer
```

**Client Services (Order, Restaurant, etc.):**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka/,http://eureka-server-2:8761/eureka/
      # Comma-separated list - tries both!
```

#### Benefits

| Scenario | Single Eureka | Multiple Eureka |
|----------|---------------|-----------------|
| Server crashes | Platform down | Platform continues |
| Network partition | Services isolated | Services find alternate path |
| Maintenance/updates | Downtime required | Rolling updates, no downtime |
| Load distribution | Single server handles all | Load shared across servers |
| Data loss | Registry lost | Registry replicated |

#### How Many Eureka Servers?

- **Development**: 1 server (simplicity)
- **Staging**: 2 servers (test HA setup)
- **Production**: 3 servers (recommended for true HA)
  - Survives 1 server failure
  - Allows rolling updates
  - Geographic distribution possible

#### The Cost-Benefit

**Cost**: Running 2-3 extra Eureka servers (~1GB RAM each)

**Benefit**: Avoiding platform-wide outages that could cost:
- Lost revenue (orders not placed)
- Customer trust
- SLA penalties
- Emergency ops response

For a food delivery platform handling thousands of orders per hour, the cost of extra Eureka servers is negligible compared to the risk of downtime.

**Bottom line**: In production, multiple Eureka servers are like having backup generators - you hope you never need them, but when you do, they save your business.


---

### How do services inherit configurations from the Config Server?

Services fetch their configuration from the Config Server during startup through a specific loading process.

#### The Configuration Loading Process

**Step 1: Service Startup Sequence**

When a service (e.g., `order-service`) starts:

```
1. Service starts up
2. BEFORE loading application.yml
3. Reads bootstrap.yml (special file that loads FIRST)
4. Connects to Config Server
5. Downloads its configuration
6. Merges with local configuration
7. Continues startup with combined config
```

**Step 2: Bootstrap Configuration**

Each service needs a `bootstrap.yml` file (loaded before `application.yml`):

**Example: `order-service/src/main/resources/bootstrap.yml`**
```yaml
spring:
  application:
    name: order-service  # This tells Config Server which config to fetch
  cloud:
    config:
      uri: http://localhost:8888  # Config Server location
      fail-fast: true  # Fail if Config Server is unreachable
      retry:
        max-attempts: 6
        initial-interval: 1000
  profiles:
    active: dev  # Which profile to use
```

**Step 3: Config Server Lookup**

Config Server uses a naming convention to find the right configuration:

```
http://localhost:8888/{application-name}/{profile}

Examples:
- http://localhost:8888/order-service/dev
- http://localhost:8888/user-service/prod
- http://localhost:8888/restaurant-service/dev
```

**Step 4: Configuration Merging**

Config Server returns configurations in this priority order (highest to lowest):

```
1. {service-name}-{profile}.yml     (e.g., order-service-dev.yml)
2. {service-name}.yml               (e.g., order-service.yml)
3. application-{profile}.yml        (e.g., application-dev.yml)
4. application.yml                  (shared by all services)
```

**Example for `order-service` with `dev` profile:**

```yaml
# Config Server merges these files:

# 1. application.yml (shared)
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# 2. application-dev.yml (dev overrides)
logging:
  level:
    com.fooddelivery: DEBUG

# 3. order-service.yml (service-specific)
server:
  port: 8084
order:
  saga:
    timeout-seconds: 30

# 4. order-service-dev.yml (if exists - service + profile specific)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db_dev
```

#### Visual Flow

```
┌─────────────────┐
│  Order Service  │
│   Starting...   │
└────────┬────────┘
         │
         │ 1. Read bootstrap.yml
         │    - application.name: order-service
         │    - config.uri: http://localhost:8888
         │    - profiles.active: dev
         │
         ▼
┌─────────────────────────────────────────┐
│  HTTP GET Request to Config Server      │
│  http://localhost:8888/order-service/dev│
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│  Config Server  │
│  Looks for:     │
│  1. application.yml              ✓
│  2. application-dev.yml          ✓
│  3. order-service.yml            ✓
│  4. order-service-dev.yml        ✗ (doesn't exist)
└────────┬────────┘
         │
         │ 2. Returns merged JSON
         │
         ▼
┌─────────────────┐
│  Order Service  │
│  Receives:      │
│  {              │
│    "server": {  │
│      "port": 8084│
│    },           │
│    "eureka": {...}│
│    "order": {...} │
│  }              │
└────────┬────────┘
         │
         │ 3. Merges with local application.yml (if exists)
         │
         ▼
┌─────────────────┐
│  Order Service  │
│  Fully          │
│  Configured ✓   │
└─────────────────┘
```

#### Real Example

Let's trace what happens when `order-service` starts:

**1. Order Service reads `bootstrap.yml`:**
```yaml
spring:
  application:
    name: order-service
  cloud:
    config:
      uri: http://localhost:8888
  profiles:
    active: dev
```

**2. Makes HTTP request:**
```bash
GET http://localhost:8888/order-service/dev
```

**3. Config Server responds with merged configuration:**
```json
{
  "name": "order-service",
  "profiles": ["dev"],
  "propertySources": [
    {
      "name": "classpath:/config/order-service.yml",
      "source": {
        "server.port": 8084,
        "spring.datasource.url": "jdbc:postgresql://localhost:5432/order_db",
        "order.saga.timeout-seconds": 30
      }
    },
    {
      "name": "classpath:/config/application-dev.yml",
      "source": {
        "logging.level.com.fooddelivery": "DEBUG"
      }
    },
    {
      "name": "classpath:/config/application.yml",
      "source": {
        "eureka.client.service-url.defaultZone": "http://localhost:8761/eureka/",
        "spring.kafka.bootstrap-servers": "localhost:9092"
      }
    }
  ]
}
```

**4. Order Service uses these properties:**
```java
@SpringBootApplication
public class OrderServiceApplication {
    
    @Value("${server.port}")  // Gets 8084 from order-service.yml
    private int port;
    
    @Value("${order.saga.timeout-seconds}")  // Gets 30 from order-service.yml
    private int sagaTimeout;
    
    @Value("${eureka.client.service-url.defaultZone}")  // Gets from application.yml
    private String eurekaUrl;
}
```

#### Dependencies Required

For a service to connect to Config Server, add to its `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

#### Key Points

1. **Bootstrap.yml loads FIRST** - before application.yml
2. **Application name matters** - must match config file name
3. **Profile determines environment** - dev, staging, prod
4. **Automatic merging** - Config Server merges all applicable files
5. **Priority order** - More specific configs override general ones
6. **Fail-fast option** - Service won't start if Config Server is down (when enabled)

#### What if Config Server is Down?

**With `fail-fast: true` (default):**
```
Order Service starts → Can't reach Config Server → FAILS TO START
```

**With `fail-fast: false`:**
```
Order Service starts → Can't reach Config Server → Uses local application.yml → STARTS ANYWAY
```

This is why we have retry configuration:
```yaml
spring:
  cloud:
    config:
      fail-fast: true
      retry:
        max-attempts: 6  # Try 6 times
        initial-interval: 1000  # Wait 1 second between attempts
```

#### Configuration Priority Summary

When the same property is defined in multiple places, this is the priority (highest wins):

1. Service-specific profile config (`order-service-dev.yml`)
2. Service-specific config (`order-service.yml`)
3. Shared profile config (`application-dev.yml`)
4. Shared config (`application.yml`)
5. Local `application.yml` in the service (lowest priority)

This allows you to:
- Define common settings once in `application.yml`
- Override for environments in `application-{profile}.yml`
- Customize per service in `{service-name}.yml`
- Fine-tune per service per environment in `{service-name}-{profile}.yml`


---

### How can we verify whether the Config Server starts up correctly and functions as expected?

You can verify the Config Server through a series of tests to ensure it's working correctly.

#### Verification Steps

**1. Start the Config Server**

Start the Config Server using IntelliJ IDEA:
1. Open `config-server/src/main/java/com/fooddelivery/configserver/ConfigServerApplication.java`
2. Right-click and select "Run ConfigServerApplication"

**What to look for in the console:**
```
Started ConfigServerApplication in X.XXX seconds
Tomcat started on port(s): 8888 (http)
```

**2. Check Health Endpoint**

Open your browser or use curl:

```bash
curl http://localhost:8888/actuator/health
```

**Expected response:**
```json
{
  "status": "UP"
}
```

**3. Test Configuration Retrieval**

Test if Config Server can serve configurations:

**Test 1: Get order-service configuration**
```bash
curl http://localhost:8888/order-service/default
```

**Expected response (JSON):**
```json
{
  "name": "order-service",
  "profiles": ["default"],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "classpath:/config/order-service.yml",
      "source": {
        "spring.application.name": "order-service",
        "server.port": 8084,
        "order.saga.timeout-seconds": 30,
        ...
      }
    },
    {
      "name": "classpath:/config/application.yml",
      "source": {
        "eureka.client.service-url.defaultZone": "http://localhost:8761/eureka/",
        ...
      }
    }
  ]
}
```

**Test 2: Get user-service configuration**
```bash
curl http://localhost:8888/user-service/default
```

**Test 3: Get configuration with dev profile**
```bash
curl http://localhost:8888/order-service/dev
```

This should include properties from both `order-service.yml` and `application-dev.yml`.

**4. Test Encryption (if configured)**

**Encrypt a value:**
```bash
curl http://localhost:8888/encrypt -d "my-secret-password"
```

**Expected response:**
```
AQA1234567890abcdef...
```

**Decrypt the value:**
```bash
curl http://localhost:8888/decrypt -d "AQA1234567890abcdef..."
```

**Expected response:**
```
my-secret-password
```

**5. Verify All Service Configurations**

Test each service configuration:

```bash
# Auth Service
curl http://localhost:8888/auth-service/default

# User Service
curl http://localhost:8888/user-service/default

# Restaurant Service
curl http://localhost:8888/restaurant-service/default

# Order Service
curl http://localhost:8888/order-service/default

# Delivery Service
curl http://localhost:8888/delivery-service/default

# Payment Service
curl http://localhost:8888/payment-service/default

# Notification Service
curl http://localhost:8888/notification-service/default
```

Each should return a JSON response with configuration properties.

**6. Check Eureka Registration (if Eureka is running)**

If you have the Service Registry running:

1. Open http://localhost:8761
2. Look for "CONFIG-SERVER" in the registered instances

**7. Verify Configuration Files Exist**

Check that all config files are in the correct location:

```bash
# List config files (Windows)
dir config-server\src\main\resources\config

# List config files (Linux/Mac)
ls config-server/src/main/resources/config
```

**Expected files:**
```
application.yml
application-dev.yml
application-prod.yml
auth-service.yml
user-service.yml
restaurant-service.yml
order-service.yml
delivery-service.yml
payment-service.yml
notification-service.yml
```

#### Common Issues and Solutions

**Issue 1: Config Server won't start**

**Error:** `Port 8888 is already in use`

**Solution:**
```bash
# Windows: Find process using port 8888
netstat -ano | findstr :8888

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Linux/Mac: Find and kill process
lsof -ti:8888 | xargs kill -9
```

**Issue 2: Configuration not found**

**Error:** `404 Not Found` when accessing config

**Possible causes:**
1. Config files not in correct location
2. Application name mismatch
3. Profile doesn't exist

**Solution:**
- Verify file path: `config-server/src/main/resources/config/`
- Check file naming: `{application-name}.yml`
- Ensure profile files exist if using profiles

**Issue 3: Encryption not working**

**Error:** `Cannot decrypt: ...`

**Solution:**
- Verify `encrypt.key` is set in `application.yml`
- Check that encrypted values start with `{cipher}` prefix
- Ensure JCE Unlimited Strength Jurisdiction Policy is installed

#### Quick Verification Script

Create a file `verify-config-server.ps1` (Windows PowerShell):

```powershell
# Verify Config Server
Write-Host "Testing Config Server..." -ForegroundColor Green

# Test health
Write-Host "`n1. Checking health endpoint..." -ForegroundColor Yellow
curl http://localhost:8888/actuator/health

# Test configurations
Write-Host "`n2. Testing order-service config..." -ForegroundColor Yellow
curl http://localhost:8888/order-service/default

Write-Host "`n3. Testing user-service config..." -ForegroundColor Yellow
curl http://localhost:8888/user-service/default

Write-Host "`n4. Testing encryption..." -ForegroundColor Yellow
$encrypted = curl http://localhost:8888/encrypt -d "test-password"
Write-Host "Encrypted: $encrypted"

Write-Host "`nConfig Server verification complete!" -ForegroundColor Green
```

Run it:
```powershell
.\verify-config-server.ps1
```

Or create `verify-config-server.sh` (Linux/Mac):

```bash
#!/bin/bash

echo "Testing Config Server..."

# Test health
echo -e "\n1. Checking health endpoint..."
curl http://localhost:8888/actuator/health

# Test configurations
echo -e "\n2. Testing order-service config..."
curl http://localhost:8888/order-service/default

echo -e "\n3. Testing user-service config..."
curl http://localhost:8888/user-service/default

# Test encryption
echo -e "\n4. Testing encryption..."
encrypted=$(curl -s http://localhost:8888/encrypt -d "test-password")
echo "Encrypted: $encrypted"

echo -e "\nConfig Server verification complete!"
```

Run it:
```bash
chmod +x verify-config-server.sh
./verify-config-server.sh
```

#### Visual Verification Checklist

✅ **Config Server Started**
- Console shows "Started ConfigServerApplication"
- Port 8888 is listening

✅ **Health Check Passes**
- `/actuator/health` returns `{"status":"UP"}`

✅ **Configurations Accessible**
- All service configs return 200 OK
- JSON response contains expected properties

✅ **Encryption Works** (if enabled)
- `/encrypt` endpoint returns encrypted string
- `/decrypt` endpoint returns original value

✅ **Eureka Registration** (if Eureka running)
- Config Server appears in Eureka dashboard

If all these checks pass, your Config Server is working correctly!

#### Browser-Based Verification

You can also verify using a web browser:

1. **Health Check:** http://localhost:8888/actuator/health
2. **Order Service Config:** http://localhost:8888/order-service/default
3. **User Service Config:** http://localhost:8888/user-service/default
4. **Dev Profile Config:** http://localhost:8888/order-service/dev

The browser will display the JSON response, making it easy to inspect the configuration values.





---

## Redis Caching

### What is RedisConfig and how does it work?

RedisConfig is a Spring Boot configuration class that sets up Redis as a distributed caching layer for the restaurant service. It improves performance by storing frequently accessed data in memory instead of repeatedly querying the database.

#### Purpose

Provides distributed caching to reduce database load and improve response times for frequently accessed restaurant and menu data.

#### Key Components

**@Configuration & @EnableCaching**
- Marks this as a Spring configuration class
- Enables Spring's annotation-driven cache management throughout the application

**redisTemplate() Bean**

Creates a RedisTemplate for direct Redis operations (not just caching):

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    
    // Use String serializer for keys
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    
    // Use JSON serializer for values
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    
    template.afterPropertiesSet();
    return template;
}
```

**Configuration details:**
- Keys stored as plain strings (e.g., "restaurants::123")
- Values stored as JSON (allows complex objects)
- Allows manual Redis operations if needed

**cacheManager() Bean**

Sets up the main caching infrastructure with different TTL (Time To Live) for different cache types:

```java
@Bean
public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Default cache configuration - 10 minutes
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        )
        .disableCachingNullValues();

    // Specific cache configurations
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
    
    // Restaurant cache - 15 minutes TTL (relatively static data)
    cacheConfigurations.put("restaurants", 
        defaultConfig.entryTtl(Duration.ofMinutes(15)));
    
    // Menu items cache - 10 minutes TTL (moderate change frequency)
    cacheConfigurations.put("menuItems", 
        defaultConfig.entryTtl(Duration.ofMinutes(10)));
    
    // Restaurant search results - 5 minutes TTL (more dynamic)
    cacheConfigurations.put("restaurantSearch", 
        defaultConfig.entryTtl(Duration.ofMinutes(5)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
}
```

**TTL Strategy:**
- `restaurants`: 15 minutes (restaurant details change infrequently)
- `menuItems`: 10 minutes (menu items updated occasionally)
- `restaurantSearch`: 5 minutes (search results more dynamic)
- Default: 10 minutes for any other caches
- Null values not cached (avoids storing empty results)

#### How It Works

When you use Spring's caching annotations in your service layer:

**1. First Request (Cache Miss):**
```
User requests restaurant ID 123
  ↓
@Cacheable checks Redis for key "restaurants::123"
  ↓
Not found in cache
  ↓
Method executes → queries database
  ↓
Result stored in Redis with 15-minute TTL
  ↓
Result returned to user
```

**2. Subsequent Requests (Cache Hit):**
```
User requests restaurant ID 123
  ↓
@Cacheable checks Redis for key "restaurants::123"
  ↓
Found in cache!
  ↓
Return cached data immediately (no database query)
  ↓
Response time: 5-20ms instead of 200-500ms
```

**3. After TTL Expires:**
```
15 minutes pass
  ↓
Redis automatically removes the cached entry
  ↓
Next request triggers cache miss
  ↓
Fresh data fetched from database
  ↓
Cache updated with new data
```

#### Real Examples from Restaurant Service

**Example 1: Single Restaurant Lookup (15-minute cache)**

```java
@Cacheable(value = "restaurants", key = "#restaurantId")
public RestaurantResponse getRestaurantById(Long restaurantId) {
    // First call: hits database
    // Next calls within 15 min: returns from Redis
    Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId);
    return restaurantMapper.toRestaurantResponse(restaurant);
}
```

**How it works:**
- User requests restaurant ID 123
- First time: queries database, stores in Redis with key `restaurants::123`
- Next 15 minutes: returns cached data instantly
- After 15 minutes: cache expires, fetches fresh data

**Example 2: Location-Based Search (5-minute cache)**

```java
@Cacheable(value = "restaurantSearch", key = "#latitude + '_' + #longitude + '_' + #radiusKm")
public List<RestaurantResponse> searchRestaurantsNearby(Double latitude, Double longitude, Double radiusKm) {
    // Cached for 5 minutes (more dynamic data)
    List<Restaurant> restaurants = restaurantRepository.findRestaurantsWithinRadius(
        latitude, longitude, radiusKm);
    return restaurants.stream()
        .map(restaurantMapper::toRestaurantResponse)
        .collect(Collectors.toList());
}
```

**Cache key example:** `restaurantSearch::40.7128_-74.0060_5.0`

**Example 3: Cuisine Search (5-minute cache)**

```java
@Cacheable(value = "restaurantSearch", 
           key = "#cuisineType + '_' + #latitude + '_' + #longitude + '_' + #radiusKm")
public List<RestaurantResponse> searchByCuisineNearby(String cuisineType, Double latitude, 
                                                       Double longitude, Double radiusKm) {
    // Cache key: "Italian_40.7128_-74.0060_5.0"
    List<Restaurant> restaurants = restaurantRepository.findByCuisineTypeWithinRadius(
        cuisineType, latitude, longitude, radiusKm);
    return restaurants.stream()
        .map(restaurantMapper::toRestaurantResponse)
        .collect(Collectors.toList());
}
```

**Example 4: Top-Rated Restaurants (5-minute cache)**

```java
@Cacheable(value = "restaurantSearch", key = "'top-rated_' + #minRating")
public List<RestaurantResponse> getTopRatedRestaurants(Double minRating) {
    // Cache key: "top-rated_4.0"
    List<Restaurant> restaurants = restaurantRepository.findTopRatedRestaurants(minRating);
    return restaurants.stream()
        .map(restaurantMapper::toRestaurantResponse)
        .collect(Collectors.toList());
}
```

#### Performance Impact

**Without Redis:**
- Every request hits the database
- Response time: 200-500ms per request
- Database under heavy load during peak hours

**With Redis:**
- First request: 200-500ms (database + cache write)
- Cached requests: 5-20ms (Redis lookup)
- **10-50x faster for cached data**
- Database load reduced by 80-90%

#### Cache Eviction

When data is updated, you need to clear stale cache entries:

```java
@CacheEvict(value = "restaurants", key = "#restaurantId")
public RestaurantResponse updateRestaurant(Long restaurantId, UpdateRestaurantRequest request) {
    // Update database
    Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
    restaurant.setName(request.getName());
    restaurantRepository.save(restaurant);
    
    // Redis cache automatically cleared for this restaurant
    return restaurantMapper.toRestaurantResponse(restaurant);
}
```

**Clear all entries in a cache:**

```java
@CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
    // Create new restaurant
    Restaurant restaurant = new Restaurant();
    restaurant.setName(request.getName());
    restaurantRepository.save(restaurant);
    
    // All restaurant caches cleared (new restaurant might appear in searches)
    return restaurantMapper.toRestaurantResponse(restaurant);
}
```

#### Benefits

1. **Reduced Database Load**: 80-90% fewer database queries
2. **Faster Response Times**: 10-50x faster for cached data
3. **Better Scalability**: Can handle more concurrent users
4. **Cost Savings**: Less database resources needed
5. **Improved User Experience**: Faster page loads

#### When Cache is Used

- ✅ Frequently accessed data (popular restaurants)
- ✅ Data that doesn't change often (restaurant details)
- ✅ Expensive queries (location-based searches)
- ❌ Real-time data (order status)
- ❌ User-specific data (unless carefully keyed)
- ❌ Data that changes frequently (inventory levels)

RedisConfig makes all of this work automatically behind the scenes with just a few annotations!

---

## Database Transactions

### How does @Transactional work?

@Transactional is a Spring annotation that wraps your method in a database transaction, providing an "all or nothing" guarantee for database operations.

#### What It Does

A transaction ensures that either:
- **All database changes succeed** → Changes are committed
- **Any operation fails** → All changes are rolled back (like they never happened)

This prevents partial updates that could leave your database in an inconsistent state.

#### Basic Concept

```java
@Transactional
public void transferMoney(Long fromAccount, Long toAccount, BigDecimal amount) {
    // Step 1: Deduct from source account
    accountRepository.deduct(fromAccount, amount);
    
    // Step 2: Add to destination account
    accountRepository.add(toAccount, amount);
    
    // If Step 2 fails, Step 1 is automatically rolled back
    // Money isn't lost!
}
```

**Without @Transactional:**
- Step 1 succeeds → Money deducted
- Step 2 fails → Money not added
- Result: Money disappeared! 💸

**With @Transactional:**
- Step 1 succeeds → Tracked by transaction
- Step 2 fails → Step 1 automatically rolled back
- Result: No money lost, database consistent ✓

#### Real Examples from Restaurant Service

**Example 1: Creating a Restaurant (Write Transaction)**

```java
@Transactional
@CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
    // Step 1: Create restaurant object
    Restaurant restaurant = new Restaurant();
    restaurant.setName(request.getName());
    restaurant.setAddress(toAddress(request.getAddress()));
    restaurant.setCuisineType(request.getCuisineType());
    
    // Step 2: Save to database
    restaurant = restaurantRepository.save(restaurant);
    
    // If ANY exception occurs above, NOTHING is saved
    // If successful, everything is committed together
    
    return restaurantMapper.toRestaurantResponse(restaurant);
}
```

**What happens:**
- Transaction starts when method begins
- All database operations are tracked
- If exception occurs → automatic rollback
- If method completes → automatic commit
- Cache is cleared only after successful commit

**Example 2: Adding Menu Item (Multi-Step Transaction)**

```java
@Transactional
@CacheEvict(value = "menuItems", allEntries = true)
public MenuItemResponse addMenuItem(Long restaurantId, CreateMenuItemRequest request) {
    // Step 1: Find restaurant (database read)
    Restaurant restaurant = restaurantRepository.findById(restaurantId)
        .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    
    // Step 2: Create menu item
    MenuItem menuItem = new MenuItem();
    menuItem.setRestaurant(restaurant);
    menuItem.setName(request.getName());
    menuItem.setPrice(request.getPrice());
    menuItem.setCategory(request.getCategory());
    
    // Step 3: Save menu item (database write)
    menuItem = menuItemRepository.save(menuItem);
    
    // All 3 steps succeed together or fail together
    return menuItemMapper.toMenuItemResponse(menuItem);
}
```

**Why this matters:**
- If restaurant doesn't exist → exception → no menu item created
- If menu item save fails → transaction rolls back
- Ensures data consistency (no orphaned menu items)

**Example 3: Updating Menu Item (Complex Update)**

```java
@Transactional
@CacheEvict(value = "menuItems", allEntries = true)
public MenuItemResponse updateMenuItem(Long restaurantId, Long menuItemId, 
                                       UpdateMenuItemRequest request) {
    // Step 1: Find menu item
    MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
        .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    
    // Step 2: Update multiple fields
    if (request.getName() != null) {
        menuItem.setName(request.getName());
    }
    if (request.getPrice() != null) {
        menuItem.setPrice(request.getPrice());
    }
    if (request.getIsAvailable() != null) {
        menuItem.setIsAvailable(request.getIsAvailable());
    }
    
    // Step 3: Save changes
    menuItem = menuItemRepository.save(menuItem);
    
    // All updates happen atomically
    return menuItemMapper.toMenuItemResponse(menuItem);
}
```

**Example 4: Deleting Menu Item**

```java
@Transactional
@CacheEvict(value = "menuItems", allEntries = true)
public void deleteMenuItem(Long restaurantId, Long menuItemId) {
    // Step 1: Find menu item
    MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
        .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    
    // Step 2: Delete from database
    menuItemRepository.delete(menuItem);
    
    // If delete fails, nothing happens
    // If successful, menu item removed and cache cleared
}
```

#### Read-Only Transactions

For read operations, use `readOnly = true` for optimization:

```java
@Transactional(readOnly = true)
@Cacheable(value = "restaurants", key = "#restaurantId")
public RestaurantResponse getRestaurantById(Long restaurantId) {
    Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
        .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    return restaurantMapper.toRestaurantResponse(restaurant);
}
```

**Benefits of readOnly = true:**
- Tells database this is read-only (optimization hint)
- Database can skip locking mechanisms
- Slightly better performance
- Prevents accidental writes
- Allows database to optimize query execution

#### Real-World Scenario: What Could Go Wrong

**WITHOUT @Transactional - DANGEROUS:**

```java
// NO @Transactional - BAD!
public void addMenuItemBad(Long restaurantId, CreateMenuItemRequest request) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
    
    MenuItem menuItem = new MenuItem();
    menuItem.setRestaurant(restaurant);
    menuItem.setName(request.getName());
    
    // This saves immediately to database
    menuItemRepository.save(menuItem);  
    
    // Oops! Exception here (maybe validation fails)
    if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Price cannot be negative!");
    }
    
    // Problem: Menu item is already saved in database!
    // Can't undo it automatically
    // Database now has invalid data
}
```

**WITH @Transactional - SAFE:**

```java
// WITH @Transactional - GOOD!
@Transactional
public void addMenuItemGood(Long restaurantId, CreateMenuItemRequest request) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
    
    MenuItem menuItem = new MenuItem();
    menuItem.setRestaurant(restaurant);
    menuItem.setName(request.getName());
    
    // This is tracked by transaction, not committed yet
    menuItemRepository.save(menuItem);
    
    // Validation fails
    if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Price cannot be negative!");
    }
    
    // Transaction automatically rolls back
    // Menu item is NOT in database - clean state!
    // No cleanup needed
}
```

#### Transaction Lifecycle

```
Method with @Transactional called
         ↓
Transaction begins
         ↓
Execute method code
         ↓
All database operations tracked
         ↓
    ┌────────────┐
    │  Success?  │
    └─────┬──────┘
          │
    ┌─────┴─────┐
    │           │
   YES         NO
    │           │
    ↓           ↓
COMMIT      ROLLBACK
    │           │
    ↓           ↓
Changes     Changes
saved       discarded
```

#### ACID Properties

@Transactional provides ACID guarantees:

**Atomicity**: All operations succeed or all fail together
```java
@Transactional
public void createOrder() {
    orderRepository.save(order);        // Operation 1
    inventoryRepository.reduce(items);  // Operation 2
    paymentRepository.charge(amount);   // Operation 3
    // All 3 succeed or all 3 fail
}
```

**Consistency**: Database stays in valid state
```java
@Transactional
public void transferFunds() {
    // Total money before = Total money after
    accountA.balance -= 100;
    accountB.balance += 100;
    // Sum is preserved
}
```

**Isolation**: Other transactions don't see partial changes
```java
@Transactional
public void updateRestaurant() {
    restaurant.setName("New Name");
    restaurant.setAddress("New Address");
    // Other users see either old values or new values
    // Never see partial update (new name, old address)
}
```

**Durability**: Committed changes are permanent
```java
@Transactional
public void placeOrder() {
    orderRepository.save(order);
    // After commit, even if server crashes,
    // order is safely stored in database
}
```

#### Common Patterns

**Pattern 1: Service Layer Transactions**

```java
@Service
public class RestaurantService {
    
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(Long id) {
        // Read-only operations
    }
    
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        // Write operations
    }
    
    @Transactional
    public RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request) {
        // Write operations
    }
}
```

**Pattern 2: Cascading Operations**

```java
@Transactional
public RestaurantResponse createRestaurantWithMenu(CreateRestaurantRequest request) {
    // Create restaurant
    Restaurant restaurant = restaurantRepository.save(new Restaurant());
    
    // Create multiple menu items
    for (MenuItemDto itemDto : request.getMenuItems()) {
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        menuItemRepository.save(item);
    }
    
    // All succeed together or all fail together
    return restaurantMapper.toRestaurantResponse(restaurant);
}
```

**Pattern 3: Exception Handling**

```java
@Transactional
public void processOrder(OrderRequest request) {
    try {
        Order order = orderRepository.save(new Order());
        paymentService.charge(order.getAmount());
        // If charge fails, order is rolled back
    } catch (PaymentException e) {
        // Transaction already rolled back automatically
        log.error("Payment failed, order not created", e);
        throw e;
    }
}
```

#### When to Use @Transactional

**Use @Transactional for:**
- ✅ Any method that modifies database (INSERT, UPDATE, DELETE)
- ✅ Methods with multiple database operations
- ✅ Operations that must be atomic
- ✅ Read operations (with `readOnly = true`)

**Don't need @Transactional for:**
- ❌ Methods with no database operations
- ❌ Simple calculations or validations
- ❌ Methods that only call other @Transactional methods

#### Key Points

1. **Automatic Management**: Spring handles transaction begin/commit/rollback
2. **Exception-Based Rollback**: Any unchecked exception triggers rollback
3. **Propagation**: Transactions can be nested or joined
4. **Isolation Levels**: Control how transactions see each other's changes
5. **Read-Only Optimization**: Use for queries to improve performance

Your code uses @Transactional correctly:
- Write operations use `@Transactional`
- Read operations use `@Transactional(readOnly = true)`
- Combined with caching for optimal performance

This ensures data consistency while maintaining high performance!



---

### When do redisTemplate and cacheManager get invoked?

Both `redisTemplate` and `cacheManager` are Spring beans that are created once during application startup, not when you call methods. Understanding when they're created and used is key to understanding how Redis caching works.

#### When Are They Created?

**Application Startup Sequence:**

```
1. Spring Boot Application starts
   ↓
2. Component scanning finds @Configuration classes
   ↓
3. RedisConfig class discovered
   ↓
4. Spring creates beans in order:
   
   a) RedisConnectionFactory (auto-configured by Spring Boot)
      ↓
   b) redisTemplate() method called
      - Creates RedisTemplate bean
      - Stored in Spring container
      ↓
   c) cacheManager() method called
      - Creates CacheManager bean
      - Stored in Spring container
   ↓
5. @EnableCaching activates
   - Scans for @Cacheable, @CacheEvict annotations
   - Creates proxies for methods with cache annotations
   - Wires cacheManager into the caching infrastructure
   ↓
6. Application ready to handle requests
```

**Important:** These beans are created **once** and reused for all requests. They're singleton beans managed by Spring's application context.

#### When Are They Used?

**cacheManager - Used Automatically by Spring**

The `cacheManager` is used **automatically** by Spring's caching infrastructure when you use cache annotations:

```java
@Cacheable(value = "restaurants", key = "#restaurantId")
public RestaurantResponse getRestaurantById(Long restaurantId) {
    // Your code
}
```

**Runtime flow:**

```
User calls getRestaurantById(123)
   ↓
Spring intercepts the call (AOP proxy)
   ↓
Spring asks cacheManager: "Do you have 'restaurants::123'?"
   ↓
cacheManager checks Redis
   ↓
┌─────────────┐
│ Found?      │
└──────┬──────┘
       │
   ┌───┴───┐
   │       │
  YES     NO
   │       │
   ↓       ↓
Return   Execute
cached   method
value    ↓
         Store in
         cache via
         cacheManager
         ↓
         Return
         result
```

**You never call cacheManager directly** - Spring does it for you when it sees `@Cacheable`, `@CacheEvict`, `@CachePut`.

**redisTemplate - Used When You Need Manual Control**

The `redisTemplate` is used when you want to **manually** interact with Redis (not through cache annotations):

```java
@Service
public class CustomService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void manualCacheOperation() {
        // Manual Redis operations
        redisTemplate.opsForValue().set("myKey", "myValue");
        String value = (String) redisTemplate.opsForValue().get("myKey");
        redisTemplate.delete("myKey");
    }
}
```

#### Real-World Flow Example

Let's trace a complete request through the restaurant service:

**Scenario: User requests restaurant ID 123**

**First Request (Cache Miss):**

```
1. HTTP Request arrives
   GET /api/restaurants/123
   ↓
2. RestaurantController.getRestaurantById(123)
   ↓
3. Calls RestaurantService.getRestaurantById(123)
   ↓
4. Spring AOP intercepts (because of @Cacheable)
   ↓
5. Spring calls cacheManager.getCache("restaurants")
   ↓
6. cacheManager checks Redis for key "restaurants::123"
   ↓
7. NOT FOUND (cache miss)
   ↓
8. Spring executes the actual method:
   restaurantRepository.findByIdAndIsActiveTrue(123)
   ↓
9. Database query executes
   ↓
10. Result returned: RestaurantResponse object
    ↓
11. Spring calls cacheManager to store result:
    cacheManager.getCache("restaurants").put("123", result)
    ↓
12. cacheManager stores in Redis with 15-minute TTL
    ↓
13. Result returned to user
    
Total time: ~300ms (database query)
```

**Second Request (Cache Hit):**

```
1. HTTP Request arrives
   GET /api/restaurants/123
   ↓
2. RestaurantController.getRestaurantById(123)
   ↓
3. Calls RestaurantService.getRestaurantById(123)
   ↓
4. Spring AOP intercepts (because of @Cacheable)
   ↓
5. Spring calls cacheManager.getCache("restaurants")
   ↓
6. cacheManager checks Redis for key "restaurants::123"
   ↓
7. FOUND! (cache hit)
   ↓
8. Method body SKIPPED (not executed)
   ↓
9. Cached result returned directly
   ↓
10. Result returned to user
    
Total time: ~10ms (Redis lookup)
```

**Performance difference: 30x faster!**

#### Behind the Scenes: How cacheManager Works

When you use `@Cacheable`, Spring creates a proxy around your service class. Here's a simplified version of what Spring generates internally:

```java
// What Spring generates internally (simplified)
public class RestaurantService$$Proxy {
    
    private RestaurantService target;  // Your actual service
    private CacheManager cacheManager;  // Injected by Spring
    
    public RestaurantResponse getRestaurantById(Long restaurantId) {
        // 1. Get the cache
        Cache cache = cacheManager.getCache("restaurants");
        
        // 2. Build cache key
        String key = String.valueOf(restaurantId); // "123"
        
        // 3. Try to get from cache
        Cache.ValueWrapper wrapper = cache.get(key);
        
        if (wrapper != null) {
            // Cache hit - return cached value
            return (RestaurantResponse) wrapper.get();
        }
        
        // Cache miss - execute actual method
        RestaurantResponse result = target.getRestaurantById(restaurantId);
        
        // Store in cache
        cache.put(key, result);
        
        return result;
    }
}
```

This is why:
- Your method is **not executed** on cache hits
- Database queries are **skipped** when data is cached
- You don't need to write any caching logic yourself

#### When Would You Use redisTemplate Directly?

You'd use `redisTemplate` for operations that don't fit the cache annotation model:

**Example 1: Custom Cache Key Patterns**
```java
@Service
public class RestaurantService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void invalidateAllRestaurantCaches(Long restaurantId) {
        // Delete all keys matching a pattern
        Set<String> keys = redisTemplate.keys("restaurant*" + restaurantId + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

**Example 2: Distributed Locks**
```java
public boolean tryLock(String lockKey, long timeout) {
    Boolean success = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "locked", timeout, TimeUnit.SECONDS);
    return Boolean.TRUE.equals(success);
}
```

**Example 3: Counters and Metrics**
```java
public Long incrementViewCount(Long restaurantId) {
    String key = "restaurant:views:" + restaurantId;
    return redisTemplate.opsForValue().increment(key);
}

public void trackPopularSearches(String searchTerm) {
    String key = "search:popular";
    redisTemplate.opsForZSet().incrementScore(key, searchTerm, 1);
}
```

**Example 4: Pub/Sub Messaging**
```java
public void publishRestaurantUpdate(Long restaurantId) {
    String channel = "restaurant-updates";
    String message = "Restaurant " + restaurantId + " updated";
    redisTemplate.convertAndSend(channel, message);
}
```

**Example 5: Session Management**
```java
public void storeUserSession(String sessionId, UserSession session) {
    String key = "session:" + sessionId;
    redisTemplate.opsForValue().set(key, session, 30, TimeUnit.MINUTES);
}
```

**Example 6: Rate Limiting**
```java
public boolean isRateLimitExceeded(String userId) {
    String key = "rate-limit:" + userId;
    Long count = redisTemplate.opsForValue().increment(key);
    
    if (count == 1) {
        // First request, set expiration
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);
    }
    
    return count > 100; // Max 100 requests per minute
}
```

#### Comparison: cacheManager vs redisTemplate

| Aspect | cacheManager | redisTemplate |
|--------|-------------|---------------|
| **Usage** | Automatic (via annotations) | Manual (explicit calls) |
| **When Created** | Application startup | Application startup |
| **When Invoked** | Every @Cacheable/@CacheEvict | When you call it |
| **Purpose** | Method-level caching | Custom Redis operations |
| **Abstraction** | High-level (cache abstraction) | Low-level (Redis operations) |
| **TTL Management** | Configured in RedisConfig | Set per operation |
| **Key Generation** | Automatic (from method params) | Manual (you create keys) |
| **Best For** | Standard caching patterns | Custom Redis use cases |

#### Summary

**Key Points:**

1. **Both created once** at application startup, not per request
2. **cacheManager** is used automatically by Spring's caching infrastructure
3. **redisTemplate** is for manual Redis operations
4. **You rarely need redisTemplate** if cache annotations cover your needs
5. **Spring handles all the caching logic** - you just add annotations
6. **Method execution is skipped** on cache hits (huge performance gain)

**In your current restaurant service:**
- You're using `cacheManager` through `@Cacheable` and `@CacheEvict` annotations
- The `redisTemplate` bean is available but not currently used
- This is the recommended approach - use annotations first, manual operations only when needed

**When to use each:**

Use **cacheManager** (via annotations) for:
- ✅ Caching method results
- ✅ Standard cache patterns
- ✅ Automatic cache management

Use **redisTemplate** (manual) for:
- ✅ Distributed locks
- ✅ Counters and metrics
- ✅ Pub/Sub messaging
- ✅ Rate limiting
- ✅ Custom cache key patterns
- ✅ Session management

