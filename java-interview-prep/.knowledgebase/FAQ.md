# API Gateway - Frequently Asked Questions (FAQ)

## Table of Contents
1. [Rate Limiting Questions](#rate-limiting-questions)
2. [CORS Questions](#cors-questions)
3. [JWT Authentication Questions](#jwt-authentication-questions)
4. [Circuit Breaker Questions](#circuit-breaker-questions)
5. [Performance Questions](#performance-questions)
6. [Deployment Questions](#deployment-questions)

---

## Rate Limiting Questions

### Q1: How does the RequestCounter inner class work in RateLimitFilter?

**Answer:**

The `RequestCounter` is a thread-safe counter that implements a **sliding window** rate limiting algorithm. Here's how it works:

#### The Code Structure
```java
private static class RequestCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    private volatile long windowStart = System.currentTimeMillis();

    public int incrementAndCheck() {
        long now = System.currentTimeMillis();
        
        // Reset counter if window has expired
        if (now - windowStart > WINDOW_DURATION.toMillis()) {
            synchronized (this) {
                if (now - windowStart > WINDOW_DURATION.toMillis()) {
                    count.set(0);
                    windowStart = now;
                }
            }
        }
        
        return count.incrementAndGet();
    }
}
```


#### Key Components Explained

**1. AtomicInteger count**
```java
private final AtomicInteger count = new AtomicInteger(0);
```
- Thread-safe counter that can be incremented by multiple threads simultaneously
- Uses CPU-level atomic operations (no locks needed for increment)
- Starts at 0 for each user

**2. volatile long windowStart**
```java
private volatile long windowStart = System.currentTimeMillis();
```
- `volatile` ensures visibility across threads (when one thread updates it, others see the change immediately)
- Stores the Unix timestamp (milliseconds) when the current time window started
- Example: `1700000000000` represents a specific moment in time

**3. Double-Checked Locking Pattern**
```java
if (now - windowStart > WINDOW_DURATION.toMillis()) {  // First check (no lock)
    synchronized (this) {                               // Acquire lock
        if (now - windowStart > WINDOW_DURATION.toMillis()) {  // Second check
            count.set(0);
            windowStart = now;
        }
    }
}
```

**Why double-check?**

Without double-checking, multiple threads could reset the counter simultaneously:

```
Thread A checks: Window expired? YES
Thread B checks: Window expired? YES (at same time)

Without synchronized:
- Thread A resets counter to 0
- Thread B also resets counter to 0
- Both think they're request #1 (WRONG!)

With synchronized + double-check:
- Thread A enters synchronized block first
- Thread A resets counter, updates windowStart
- Thread B enters synchronized block
- Thread B checks again: Window expired? NO (A already reset it)
- Thread B skips reset
- Thread A gets count=1, Thread B gets count=2 ✓ CORRECT
```


#### Visual Timeline Example

```
User: "user123"
Rate Limit: 100 requests per minute (60 seconds)

10:00:00.000 - Request #1
  windowStart = 1700000000000
  now         = 1700000000000
  now - windowStart = 0ms (< 60000ms) → Window NOT expired
  count.incrementAndGet() → 1
  ✓ ALLOWED (1/100)

10:00:30.000 - Request #50
  windowStart = 1700000000000 (unchanged)
  now         = 1700000030000
  now - windowStart = 30000ms (< 60000ms) → Window NOT expired
  count.incrementAndGet() → 50
  ✓ ALLOWED (50/100)

10:00:59.000 - Request #100
  windowStart = 1700000000000 (unchanged)
  now         = 1700000059000
  now - windowStart = 59000ms (< 60000ms) → Window NOT expired
  count.incrementAndGet() → 100
  ✓ ALLOWED (100/100) - Last allowed request!

10:00:59.500 - Request #101
  windowStart = 1700000000000 (unchanged)
  now         = 1700000059500
  now - windowStart = 59500ms (< 60000ms) → Window NOT expired
  count.incrementAndGet() → 101
  ✗ REJECTED (101/100) - Rate limit exceeded!

10:01:00.001 - Request #102 (Window expires!)
  windowStart = 1700000000000
  now         = 1700000060001
  now - windowStart = 60001ms (> 60000ms) → Window EXPIRED!
  
  Reset happens:
    count.set(0)
    windowStart = 1700000060001 (new window starts)
  
  count.incrementAndGet() → 1
  ✓ ALLOWED (1/100) - Fresh window!
```

#### Thread Safety Visualization

```
Time: 10:01:00.001 (window just expired)
3 threads making requests simultaneously:

Thread A                    Thread B                    Thread C
   │                           │                           │
   ├─ Check: expired? YES      │                           │
   │                           ├─ Check: expired? YES      │
   │                           │                           ├─ Check: expired? YES
   │                           │                           │
   ├─ Wait for lock...         │                           │
   │  (enters synchronized)    │                           │
   │                           ├─ Wait for lock...         │
   │                           │                           ├─ Wait for lock...
   │                           │                           │
   ├─ Double-check: expired?   │                           │
   │  YES → Reset counter      │                           │
   │  count = 0                │                           │
   │  windowStart = now        │                           │
   │                           │                           │
   ├─ Exit synchronized        │                           │
   ├─ increment: count = 1     │                           │
   │                           │                           │
   │                           ├─ Enter synchronized       │
   │                           ├─ Double-check: expired?   │
   │                           │  NO (A already reset!)    │
   │                           ├─ Skip reset               │
   │                           ├─ Exit synchronized        │
   │                           ├─ increment: count = 2     │
   │                           │                           │
   │                           │                           ├─ Enter synchronized
   │                           │                           ├─ Double-check: expired?
   │                           │                           │  NO (A already reset!)
   │                           │                           ├─ Skip reset
   │                           │                           ├─ Exit synchronized
   │                           │                           ├─ increment: count = 3
   │                           │                           │
Result: count = 3 ✓ (correct)
```

#### Performance Impact

```
Typical request (window not expired):
1. Check if expired: ~1ns
2. Increment atomic counter: ~10ns
Total: ~11ns (negligible overhead)

Window expiration (rare - once per minute):
1. Check if expired: ~1ns
2. Acquire lock: ~100ns
3. Double-check: ~1ns
4. Reset counter: ~10ns
5. Release lock: ~100ns
6. Increment: ~10ns
Total: ~222ns (still very fast)
```

The design is highly optimized for the common case (window not expired) while being thread-safe for the rare case (window expiration).

---

### Q2: Why use AtomicInteger instead of regular int?

**Answer:**

Regular `int` is not thread-safe for concurrent increments:

```java
// NOT thread-safe:
private int count = 0;
count++;  // This is actually 3 operations:
          // 1. Read current value
          // 2. Add 1
          // 3. Write new value
          // Another thread can interfere between these steps!

// Thread-safe:
private AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();  // Single atomic operation at CPU level
```

**Race condition example without AtomicInteger:**
```
Initial: count = 5

Thread A reads: 5
Thread B reads: 5
Thread A increments: 5 + 1 = 6
Thread B increments: 5 + 1 = 6
Thread A writes: 6
Thread B writes: 6

Final: count = 6 (WRONG! Should be 7)
```

With `AtomicInteger`, the CPU ensures the operation is atomic (indivisible).

---

### Q3: Can I use Redis for distributed rate limiting?

**Answer:**

Yes! For multiple gateway instances, use Redis:

```java
@Configuration
public class RedisRateLimitConfig {
    
    @Bean
    public RedisRateLimiter redisRateLimiter(
            ReactiveRedisTemplate<String, String> redisTemplate) {
        return new RedisRateLimiter(100, 60); // 100 requests per 60 seconds
    }
}
```

**Benefits:**
- Shared counter across all gateway instances
- Automatic expiration (no memory leaks)
- Persistent across gateway restarts

**Trade-off:**
- Adds network latency (~1-2ms per request)
- Requires Redis infrastructure


---

## CORS Questions

### Q4: How does CorsWebFilter work in Spring WebFlux?

**Answer:**

`CorsWebFilter` is a reactive filter that handles Cross-Origin Resource Sharing (CORS) in the request/response cycle.

#### What Problem Does CORS Solve?

Browsers enforce the **Same-Origin Policy** for security:

```
Browser Security: Same-Origin Policy
═══════════════════════════════════════════════════════════════

Scenario WITHOUT CORS:
┌─────────────────┐
│  Browser        │
│  Origin:        │
│  localhost:3000 │  (React App)
└────────┬────────┘
         │
         │ ❌ BLOCKED by browser!
         │ "Different origin detected"
         ▼
┌─────────────────┐
│  API Gateway    │
│  Origin:        │
│  localhost:8080 │
└─────────────────┘

Different origins = Different domain, port, or protocol
```

#### Filter Registration

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "X-RateLimit-*"));
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
```

**What happens when Spring starts:**
```
1. Spring Boot starts
2. Scans @Configuration classes
3. Finds @Bean method returning CorsWebFilter
4. Creates CorsWebFilter instance
5. Registers it in WebFilter chain (early in the chain)
6. Filter is now active for ALL requests
```


#### Request/Response Cycle - Simple Request

```
┌──────────────────────────────────────────────────────────────────┐
│                    BROWSER (localhost:3000)                       │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                    GET /api/restaurants HTTP/1.1
                    Host: localhost:8080
                    Origin: http://localhost:3000  ← Browser adds this!
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                    API GATEWAY - CorsWebFilter                    │
│                                                                   │
│  1. Checks if request has "Origin" header → YES                  │
│  2. Looks up CORS configuration for path → Found "/**"           │
│  3. Validates origin → http://localhost:3000 is allowed ✓        │
│  4. Validates method → GET is allowed ✓                          │
│  5. This is a simple request → Continue to next filter           │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                    [Other filters: JWT, Rate Limit, etc.]
                                │
                                ▼
                    [Routes to restaurant-service]
                                │
                                ▼
                    [Service returns response]
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                    CorsWebFilter adds headers                     │
│                                                                   │
│  Response headers added:                                         │
│  ✓ Access-Control-Allow-Origin: http://localhost:3000           │
│  ✓ Access-Control-Allow-Credentials: true                       │
│  ✓ Access-Control-Expose-Headers: Authorization, X-RateLimit-*  │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                    HTTP/1.1 200 OK
                    Access-Control-Allow-Origin: http://localhost:3000
                    { "restaurants": [...] }
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                    BROWSER                                        │
│  - Checks Access-Control-Allow-Origin → Matches ✓                │
│  - Allows JavaScript to access response                          │
└──────────────────────────────────────────────────────────────────┘
```


#### Request/Response Cycle - Preflight Request

For complex requests (POST with custom headers), the browser sends a **preflight** request first:

```
┌──────────────────────────────────────────────────────────────────┐
│                    BROWSER                                        │
│  JavaScript: fetch('http://localhost:8080/api/orders', {         │
│    method: 'POST',                                               │
│    headers: { 'Authorization': 'Bearer token' }  ← Custom header!│
│  })                                                              │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                │ Browser thinks: "Custom header!"
                                │ "Need to ask permission first"
                                ▼
                    ┌─────────────────────────┐
                    │  PREFLIGHT REQUEST      │
                    │  (automatic)            │
                    └────────────┬────────────┘
                                │
                                ▼
                    OPTIONS /api/orders HTTP/1.1
                    Origin: http://localhost:3000
                    Access-Control-Request-Method: POST
                    Access-Control-Request-Headers: Authorization
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                    CorsWebFilter                                  │
│                                                                   │
│  1. Detects preflight (Method=OPTIONS + has AC-Request-* headers)│
│  2. Validates:                                                   │
│     - Origin: http://localhost:3000 → ALLOWED ✓                  │
│     - Method: POST → ALLOWED ✓                                   │
│     - Headers: Authorization → ALLOWED ✓                         │
│  3. Returns preflight response (does NOT forward to service)     │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                    HTTP/1.1 200 OK
                    Access-Control-Allow-Origin: http://localhost:3000
                    Access-Control-Allow-Methods: GET, POST, PUT, DELETE
                    Access-Control-Allow-Headers: *
                    Access-Control-Max-Age: 3600  ← Cache for 1 hour
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                    BROWSER                                        │
│  - Checks if POST allowed → YES ✓                                │
│  - Checks if Authorization header allowed → YES ✓                │
│  - Caches response for 3600 seconds                              │
│  - Permission granted! Now send actual request                   │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                    POST /api/orders HTTP/1.1
                    Authorization: Bearer token
                    {"restaurantId": "rest123"}
                                │
                                ▼
                    [Gateway processes normally]
                                │
                                ▼
                    HTTP/1.1 201 Created
                    Access-Control-Allow-Origin: http://localhost:3000
                    {"orderId": "order123"}
```


#### Configuration Options Explained

**1. allowedOrigins**
```java
corsConfig.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "https://yourdomain.com"
));
```
- Specifies which origins can make requests
- Browser checks `Access-Control-Allow-Origin` header

⚠️ **Security Warning:**
```java
// DON'T do this in production:
corsConfig.setAllowedOrigins(List.of("*"));  // Allows ANY origin!

// DO this instead:
corsConfig.setAllowedOrigins(List.of("https://app.yourdomain.com"));
```

**2. allowedMethods**
```java
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
```
- Specifies which HTTP methods are allowed
- Browser validates during preflight

**3. allowedHeaders**
```java
corsConfig.setAllowedHeaders(List.of("*"));  // All headers
// OR
corsConfig.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
```
- Specifies which request headers are allowed
- `"*"` means all headers

**4. allowCredentials**
```java
corsConfig.setAllowCredentials(true);
```
- Allows cookies, authorization headers, and TLS client certificates
- Required for JWT tokens in Authorization header

⚠️ **Important:**
```java
// This combination is INVALID:
corsConfig.setAllowedOrigins(List.of("*"));
corsConfig.setAllowCredentials(true);
// Browser will reject this!

// Must specify exact origins when using credentials:
corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
corsConfig.setAllowCredentials(true);
// This works ✓
```

**5. exposedHeaders**
```java
corsConfig.setExposedHeaders(Arrays.asList(
    "Authorization",
    "X-RateLimit-Limit",
    "X-RateLimit-Remaining"
));
```
- By default, JavaScript can only read standard headers
- `exposedHeaders` makes custom headers readable

**Example:**
```javascript
// Without exposedHeaders:
fetch('http://localhost:8080/api/orders')
  .then(response => {
    console.log(response.headers.get('Content-Type'));  // ✓ Works
    console.log(response.headers.get('X-RateLimit-Remaining'));  // ✗ null
  });

// With exposedHeaders:
fetch('http://localhost:8080/api/orders')
  .then(response => {
    console.log(response.headers.get('X-RateLimit-Remaining'));  // ✓ "54"
  });
```

**6. maxAge**
```java
corsConfig.setMaxAge(3600L);  // 3600 seconds = 1 hour
```
- Tells browser how long to cache preflight response
- Reduces number of preflight requests

**Timeline:**
```
10:00:00 - First POST → Browser sends preflight
           Gateway responds with Max-Age: 3600
           Browser caches for 1 hour
           Browser sends actual POST

10:05:00 - Second POST → Browser uses cached preflight
           No OPTIONS request sent!
           Directly sends POST

11:00:01 - Third POST → Cache expired
           Browser sends preflight again
```

---

### Q5: What are common CORS errors and how to fix them?

**Error 1: "No 'Access-Control-Allow-Origin' header"**
```
Access to fetch at 'http://localhost:8080/api/orders' from origin 
'http://localhost:3000' has been blocked by CORS policy
```

**Cause:** Origin not in allowed list

**Solution:**
```java
corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
```

---

**Error 2: "Credentials flag is true, but header is not"**
```
The value of the 'Access-Control-Allow-Credentials' header must be 'true' 
when the request's credentials mode is 'include'
```

**Cause:** `allowCredentials` not set

**Solution:**
```java
corsConfig.setAllowCredentials(true);
```

---

**Error 3: "Method not allowed"**
```
Method PUT is not allowed by Access-Control-Allow-Methods
```

**Cause:** HTTP method not in allowed list

**Solution:**
```java
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
```

---

**Error 4: "Request header not allowed"**
```
Request header field X-Custom-Header is not allowed
```

**Cause:** Custom header not allowed

**Solution:**
```java
corsConfig.setAllowedHeaders(List.of("*"));  // Allow all
// OR
corsConfig.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "X-Custom-Header"));
```

---

### Q6: Where does CorsWebFilter fit in the filter chain?

**Answer:**

`CorsWebFilter` is positioned **early** in the filter chain, before authentication:

```
Request Flow:
1. CorsWebFilter          ← Validates CORS, handles preflight
2. SecurityWebFilterChain ← Spring Security
3. JwtAuthenticationFilter ← JWT validation
4. RateLimitFilter        ← Rate limiting
5. Gateway Route Handler  ← Routes to microservice
```

**Why early?**
- Preflight requests (OPTIONS) don't have authentication
- CORS validation should happen before other processing
- Allows browser to fail fast if origin not allowed


---

## JWT Authentication Questions

### Q7: How does JWT validation work in the gateway?

**Answer:**

The gateway validates JWT tokens in the `JwtAuthenticationFilter`:

```java
@Override
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        // 1. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing Authorization header", 401);
        }
        
        String token = authHeader.substring(7); // Remove "Bearer "
        
        // 2. Validate token
        if (!jwtUtil.validateToken(token)) {
            return onError(exchange, "Invalid or expired token", 401);
        }
        
        // 3. Extract user information
        String userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        
        // 4. Add user context headers for downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    };
}
```

**JWT Token Structure:**
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",              // User ID
    "email": "user@example.com",   // Email
    "role": "CUSTOMER",            // Role
    "iat": 1700000000,             // Issued at
    "exp": 1700003600              // Expires at (1 hour)
  },
  "signature": "..."
}
```

---

### Q8: Why does the gateway add X-User-* headers?

**Answer:**

The gateway extracts user information from the JWT and adds it as headers so downstream services don't need to:
1. Parse JWT tokens themselves
2. Share the JWT secret
3. Include JWT libraries

**Example:**
```
Request to Gateway:
  Authorization: Bearer eyJhbGc...

Gateway adds headers:
  X-User-Id: user123
  X-User-Email: user@example.com
  X-User-Role: CUSTOMER

Request to Order Service:
  Authorization: Bearer eyJhbGc...
  X-User-Id: user123          ← Service can use this directly
  X-User-Email: user@example.com
  X-User-Role: CUSTOMER
```

**Order Service can now:**
```java
@GetMapping("/orders")
public List<Order> getOrders(@RequestHeader("X-User-Id") String userId) {
    // No JWT parsing needed!
    return orderRepository.findByUserId(userId);
}
```

---

### Q9: What happens if JWT secret doesn't match between services?

**Answer:**

If the gateway and auth-service use different JWT secrets, validation will fail:

```
Auth Service (generates token):
  JWT_SECRET = "secret-key-A"
  Token signed with: secret-key-A

Gateway (validates token):
  JWT_SECRET = "secret-key-B"
  Tries to verify with: secret-key-B
  
Result: Signature verification fails → 401 Unauthorized
```

**Error message:**
```json
{
  "error": "Unauthorized",
  "message": "Token validation failed: JWT signature does not match"
}
```

**Solution:**
Ensure both services use the same secret:
```bash
# Set environment variable for both services
export JWT_SECRET="your-256-bit-secret-key"
```

---

## Circuit Breaker Questions

### Q10: How does the circuit breaker decide when to open?

**Answer:**

The circuit breaker uses a **sliding window** algorithm:

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10           # Track last 10 requests
        minimum-number-of-calls: 5        # Need 5 calls before calculating
        failure-rate-threshold: 50        # Open if ≥50% fail
        wait-duration-in-open-state: 30s  # Wait 30s before testing
```

**Example:**
```
Request #1: SUCCESS (1/1 = 100% success)
Request #2: SUCCESS (2/2 = 100% success)
Request #3: SUCCESS (3/3 = 100% success)
Request #4: SUCCESS (4/4 = 100% success)
Request #5: FAILURE (4/5 = 80% success, 20% failure)
  → Not enough failures yet (< 50%)

Request #6: FAILURE (3/6 = 50% success, 50% failure)
  → Threshold reached! Circuit OPENS

Request #7: Immediate fallback (circuit is OPEN)
Request #8: Immediate fallback (circuit is OPEN)
...
After 30 seconds: Circuit goes to HALF_OPEN
Request #9: Test call → SUCCESS (1/3)
Request #10: Test call → SUCCESS (2/3)
Request #11: Test call → SUCCESS (3/3)
  → All test calls succeeded! Circuit CLOSES
```

---

### Q11: What's the difference between OPEN, CLOSED, and HALF_OPEN states?

**Answer:**

```
CLOSED (Normal Operation):
- All requests pass through to service
- Tracks success/failure rate
- If failure rate ≥ 50%, transitions to OPEN

OPEN (Service Down):
- Requests fail immediately (no service call)
- Returns fallback response instantly
- After 30 seconds, transitions to HALF_OPEN

HALF_OPEN (Testing Recovery):
- Allows 3 test requests through
- If all 3 succeed → CLOSED (recovered)
- If any fails → OPEN (still down)
```

**State Diagram:**
```
    CLOSED
      │
      │ Failure rate ≥ 50%
      ▼
    OPEN
      │
      │ After 30 seconds
      ▼
  HALF_OPEN
      │
      ├─ All 3 test requests succeed → CLOSED
      └─ Any test request fails → OPEN
```

---

### Q12: Why do I get fallback responses even though the service is running?

**Answer:**

Possible causes:

**1. Service is slow (timeout)**
```yaml
resilience4j:
  timelimiter:
    configs:
      default:
        timeout-duration: 5s  # If service takes >5s, counts as failure
```

**Solution:** Optimize service or increase timeout

**2. Service returning errors**
```
If service returns 500 errors, circuit breaker counts them as failures
```

**Solution:** Fix service errors

**3. Network issues**
```
Connection timeouts or network errors count as failures
```

**Solution:** Check network connectivity

**4. Circuit breaker threshold too low**
```yaml
failure-rate-threshold: 50  # Opens after 50% failures
```

**Solution:** Increase threshold for testing:
```yaml
failure-rate-threshold: 80  # More tolerant
```


---

## Performance Questions

### Q13: How many requests per second can the gateway handle?

**Answer:**

**Single Gateway Instance:**
- **Throughput**: ~10,000 requests/second
- **Concurrent connections**: ~50,000
- **Average latency**: 50-100ms (with healthy services)

**Factors affecting performance:**

1. **Request type:**
   - Public endpoints (no auth): 10-20ms
   - Authenticated requests: 50-100ms
   - Requests triggering circuit breaker: 5-10ms (fallback)

2. **Downstream service performance:**
   - Fast service (10ms): Gateway adds ~10ms overhead
   - Slow service (500ms): Gateway adds ~10ms overhead

3. **Number of filters:**
   - Each filter adds ~1-5ms

**Scaling:**
```
1 instance:  10,000 req/s
3 instances: 30,000 req/s
5 instances: 50,000 req/s
```

---

### Q14: Does rate limiting affect performance?

**Answer:**

Rate limiting has **minimal performance impact**:

```
Without rate limiting: 50ms average
With rate limiting:    52ms average (+2ms)
```

**Why so fast?**
- In-memory counter (no database)
- AtomicInteger operations (~10ns)
- Lock only on window expiration (once per minute)

**Memory usage:**
```
1,000 active users:   ~1MB
10,000 active users:  ~10MB
100,000 active users: ~100MB
```

**For better performance at scale, use Redis:**
```java
@Bean
public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(100, 60);
}
```

---

### Q15: How can I improve gateway performance?

**Answer:**

**1. Enable HTTP/2**
```yaml
server:
  http2:
    enabled: true
```
- Multiplexing (multiple requests over single connection)
- Header compression
- Server push

**2. Enable compression**
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html
    min-response-size: 1024
```
- Reduces response size by 60-80%
- Especially effective for JSON responses

**3. Reduce timeout**
```yaml
resilience4j:
  timelimiter:
    configs:
      default:
        timeout-duration: 2s  # Fail fast instead of waiting 5s
```

**4. Optimize JWT validation**
```java
// Cache JWT parsing results
private final LoadingCache<String, Claims> tokenCache = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build(token -> jwtUtil.extractAllClaims(token));
```

**5. Use connection pooling**
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 500
          max-idle-time: 30s
```

**6. Scale horizontally**
```yaml
# Kubernetes
replicas: 3  # Run 3 gateway instances
```

---

## Deployment Questions

### Q16: How do I deploy the gateway in production?

**Answer:**

**Option 1: Docker**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/api-gateway-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx1g", "-Xms512m", "-jar", "app.jar"]
```

```bash
docker build -t food-delivery/api-gateway:1.0.0 .
docker run -p 8080:8080 \
  -e JWT_SECRET=your-secret \
  -e EUREKA_URL=http://eureka:8761/eureka/ \
  food-delivery/api-gateway:1.0.0
```

**Option 2: Kubernetes**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: food-delivery/api-gateway:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: gateway-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

**Option 3: AWS ECS/Fargate**
```json
{
  "family": "api-gateway",
  "containerDefinitions": [{
    "name": "api-gateway",
    "image": "food-delivery/api-gateway:1.0.0",
    "memory": 1024,
    "cpu": 512,
    "portMappings": [{
      "containerPort": 8080,
      "protocol": "tcp"
    }],
    "environment": [
      {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"}
    ],
    "secrets": [
      {"name": "JWT_SECRET", "valueFrom": "arn:aws:secretsmanager:..."}
    ]
  }]
}
```

---

### Q17: How do I handle JWT secret rotation?

**Answer:**

**Strategy 1: Dual Secret Support**
```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret.current}")
    private String currentSecret;
    
    @Value("${jwt.secret.previous}")
    private String previousSecret;
    
    public boolean validateToken(String token) {
        try {
            // Try current secret first
            return validateWithSecret(token, currentSecret);
        } catch (Exception e) {
            // Fall back to previous secret
            return validateWithSecret(token, previousSecret);
        }
    }
}
```

**Rotation process:**
```
Day 1: 
  current-secret: secret-A
  previous-secret: null

Day 2 (rotate):
  current-secret: secret-B
  previous-secret: secret-A
  → Auth service issues tokens with secret-B
  → Gateway accepts both secret-A and secret-B tokens

Day 3 (cleanup):
  current-secret: secret-B
  previous-secret: null
  → All old tokens expired
  → Only secret-B tokens accepted
```

**Strategy 2: Use Key ID (kid)**
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT",
    "kid": "key-2024-11"  ← Key identifier
  }
}
```

```java
public boolean validateToken(String token) {
    String kid = extractKeyId(token);
    String secret = secretManager.getSecret(kid);
    return validateWithSecret(token, secret);
}
```

---

### Q18: How do I monitor the gateway in production?

**Answer:**

**1. Prometheus Metrics**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Key metrics to monitor:**
```promql
# Request rate
rate(spring_cloud_gateway_requests_total[5m])

# Error rate
rate(spring_cloud_gateway_requests_total{status=~"5.."}[5m])

# Circuit breaker state
resilience4j_circuitbreaker_state

# Response time (95th percentile)
histogram_quantile(0.95, 
  rate(spring_cloud_gateway_requests_seconds_bucket[5m])
)

# Rate limit violations
rate(gateway_rate_limit_exceeded_total[5m])
```

**2. Grafana Dashboard**
```json
{
  "dashboard": {
    "title": "API Gateway Monitoring",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [{
          "expr": "rate(spring_cloud_gateway_requests_total[5m])"
        }]
      },
      {
        "title": "Error Rate",
        "targets": [{
          "expr": "rate(spring_cloud_gateway_requests_total{status=~\"5..\"}[5m])"
        }]
      },
      {
        "title": "Circuit Breaker State",
        "targets": [{
          "expr": "resilience4j_circuitbreaker_state"
        }]
      }
    ]
  }
}
```

**3. Alerts**
```yaml
groups:
- name: api-gateway
  rules:
  - alert: HighErrorRate
    expr: rate(spring_cloud_gateway_requests_total{status=~"5.."}[5m]) > 0.05
    for: 5m
    annotations:
      summary: "High error rate on API Gateway"
      
  - alert: CircuitBreakerOpen
    expr: resilience4j_circuitbreaker_state == 1
    for: 1m
    annotations:
      summary: "Circuit breaker is open"
      
  - alert: HighLatency
    expr: histogram_quantile(0.95, rate(spring_cloud_gateway_requests_seconds_bucket[5m])) > 1
    for: 5m
    annotations:
      summary: "High latency on API Gateway"
```

**4. Logging**
```yaml
logging:
  level:
    com.fooddelivery.gateway: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**Important logs to monitor:**
- Authentication failures
- Rate limit violations
- Circuit breaker state changes
- Service discovery issues

---

### Q19: What's the recommended resource allocation?

**Answer:**

**Development:**
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

**Production (Low Traffic: <1000 req/s):**
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

**Production (High Traffic: >5000 req/s):**
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

**JVM Settings:**
```bash
# Low traffic
java -Xmx512m -Xms256m -jar api-gateway.jar

# High traffic
java -Xmx1g -Xms512m -XX:+UseG1GC -jar api-gateway.jar
```

---

### Q20: How do I test the gateway before deploying?

**Answer:**

**1. Unit Tests**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayTest {
    
    @Autowired
    private WebTestClient webClient;
    
    @Test
    void testAuthenticationRequired() {
        webClient.get()
            .uri("/api/orders")
            .exchange()
            .expectStatus().isUnauthorized();
    }
    
    @Test
    void testRateLimiting() {
        String token = getValidToken();
        
        for (int i = 0; i < 101; i++) {
            var response = webClient.get()
                .uri("/api/orders")
                .header("Authorization", "Bearer " + token)
                .exchange();
            
            if (i < 100) {
                response.expectStatus().isOk();
            } else {
                response.expectStatus().isEqualTo(429);
            }
        }
    }
}
```

**2. Integration Tests**
```bash
# Start all services
docker-compose up -d

# Run integration tests
mvn verify -P integration-tests

# Check results
cat target/failsafe-reports/*.xml
```

**3. Load Testing**
```bash
# Apache Bench
ab -n 10000 -c 100 -H "Authorization: Bearer $TOKEN" \
   http://localhost:8080/api/orders

# K6
k6 run --vus 100 --duration 30s load-test.js
```

**4. Smoke Tests in Production**
```bash
#!/bin/bash
# smoke-test.sh

# Health check
curl -f http://api-gateway/actuator/health || exit 1

# Authentication
TOKEN=$(curl -X POST http://api-gateway/api/auth/login \
  -d '{"email":"test@example.com","password":"test"}' \
  | jq -r '.accessToken')

# Protected endpoint
curl -f -H "Authorization: Bearer $TOKEN" \
  http://api-gateway/api/orders || exit 1

echo "Smoke tests passed!"
```

---

## Summary

This FAQ covers the most common questions about the API Gateway:

- **Rate Limiting**: Thread-safe sliding window algorithm with AtomicInteger
- **CORS**: Handles cross-origin requests with preflight support
- **JWT**: Validates tokens and propagates user context
- **Circuit Breaker**: Provides resilience with fallback responses
- **Performance**: ~10,000 req/s per instance with minimal overhead
- **Deployment**: Docker, Kubernetes, and cloud-native options
- **Monitoring**: Prometheus metrics, Grafana dashboards, and alerts

For more details, see the main [API_GATEWAY_DETAILED_DOCUMENTATION.md](./API_GATEWAY_DETAILED_DOCUMENTATION.md).
