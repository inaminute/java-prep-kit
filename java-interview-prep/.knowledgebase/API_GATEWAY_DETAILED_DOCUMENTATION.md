# API Gateway - Detailed Technical Documentation

> **📚 Also see:** [FAQ.md](./FAQ.md) for frequently asked questions about RequestCounter, CORS, JWT, Circuit Breaker, and more.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Component Breakdown](#component-breakdown)
3. [Scenario-Based Examples](#scenario-based-examples)
4. [Request Flow Diagrams](#request-flow-diagrams)
5. [Error Handling](#error-handling)
6. [Performance & Monitoring](#performance--monitoring)
7. [FAQ](./FAQ.md) - Frequently Asked Questions

---

## Architecture Overview

The API Gateway serves as the single entry point for all client requests in the Food Delivery Platform. It's built using Spring Cloud Gateway (reactive, non-blocking) and provides:

- **Request Routing**: Routes to 6 microservices (auth, user, restaurant, order, delivery, payment)
- **JWT Authentication**: Validates tokens and propagates user context
- **Rate Limiting**: 100 requests/minute per user
- **Circuit Breaker**: Resilience4j with fallback responses
- **CORS**: Cross-origin support for web clients
- **Load Balancing**: Via Eureka service discovery

### Technology Stack
```
Spring Cloud Gateway 4.x (Reactive WebFlux)
Spring Security (WebFlux)
Resilience4j (Circuit Breaker)
Eureka Client (Service Discovery)
JJWT (JWT Processing)
```

---

## Component Breakdown

### 1. ApiGatewayApplication.java
**Purpose**: Main entry point with service discovery enabled

```java
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

**Key Annotations**:
- `@EnableDiscoveryClient`: Registers with Eureka and enables service lookup


---

### 2. GatewayConfig.java
**Purpose**: Defines routing rules and circuit breaker configuration

```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("authServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallback/auth"))
                    .retry(config -> config.setRetries(3)))
                .uri("lb://auth-service"))
            // ... other routes
            .build();
    }
}
```

**Route Configuration Breakdown**:

| Component | Description | Example |
|-----------|-------------|---------|
| `route("auth-service", ...)` | Route ID for identification | "auth-service" |
| `.path("/api/auth/**")` | URL pattern to match | Matches `/api/auth/login`, `/api/auth/register` |
| `.circuitBreaker(...)` | Circuit breaker configuration | Opens after 50% failure rate |
| `.setFallbackUri(...)` | Fallback endpoint when circuit opens | `forward:/fallback/auth` |
| `.retry(...)` | Retry failed requests | 3 retries before failing |
| `.uri("lb://auth-service")` | Target service via load balancer | Looks up "auth-service" in Eureka |

**How Load Balancing Works**:
```
lb://auth-service
 │
 ├─ "lb://" = Load Balancer protocol
 └─ "auth-service" = Service name in Eureka
     │
     ├─ Eureka returns: [instance1:8081, instance2:8082, instance3:8083]
     └─ Gateway uses Round-Robin to select instance
```


---

### 3. JwtAuthenticationFilter.java
**Purpose**: Validates JWT tokens and extracts user context

**Filter Execution Flow**:
```
1. Request arrives → Extract Authorization header
2. Check if endpoint is public → Skip if public
3. Validate "Bearer <token>" format → Return 401 if invalid
4. Parse and validate JWT → Return 401 if expired/invalid
5. Extract claims (userId, email, role)
6. Add headers to request: X-User-Id, X-User-Email, X-User-Role
7. Forward to downstream service
```

**Code Example with Detailed Comments**:
```java
@Override
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // PUBLIC ENDPOINTS: Skip authentication
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // EXTRACT TOKEN: Get Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", 
                          HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // VALIDATE TOKEN: Check signature and expiration
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid or expired token", 
                              HttpStatus.UNAUTHORIZED);
            }

            // EXTRACT USER INFO: Parse JWT claims
            String userId = jwtUtil.extractUserId(token);    // From "sub" claim
            String email = jwtUtil.extractEmail(token);      // From "email" claim
            String role = jwtUtil.extractRole(token);        // From "role" claim

            // ADD CONTEXT HEADERS: For downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Token validation failed", 
                          HttpStatus.UNAUTHORIZED);
        }
    };
}
```

**Public Endpoints (No Authentication Required)**:
```java
private static final List<String> PUBLIC_ENDPOINTS = List.of(
    "/api/auth/login",      // User login
    "/api/auth/register",   // User registration
    "/api/auth/refresh",    // Token refresh
    "/api/restaurants",     // Browse restaurants (public)
    "/actuator/health"      // Health check
);
```


---

### 4. RateLimitFilter.java
**Purpose**: Limits requests to 100 per minute per user

**Rate Limiting Algorithm**:
```
Sliding Window Counter Algorithm:
1. Track request count per user ID
2. Reset counter every 60 seconds
3. Increment counter on each request
4. Reject if count > 100
```

**Implementation Details**:
```java
// In-memory storage: userId -> RequestCounter
private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

private static final int MAX_REQUESTS_PER_MINUTE = 100;
private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

@Override
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        String userId = getUserId(exchange); // From X-User-Id header

        // Skip rate limiting for public endpoints (no userId)
        if (userId == null) {
            return chain.filter(exchange);
        }

        // Get or create counter for this user
        RequestCounter counter = requestCounts.computeIfAbsent(
            userId, k -> new RequestCounter()
        );

        // Increment and check limit
        if (counter.incrementAndCheck() > MAX_REQUESTS_PER_MINUTE) {
            return onError(exchange, 
                "Rate limit exceeded. Maximum 100 requests per minute.", 
                HttpStatus.TOO_MANY_REQUESTS);
        }

        // Add rate limit headers to response
        exchange.getResponse().getHeaders().add(
            "X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE)
        );
        exchange.getResponse().getHeaders().add(
            "X-RateLimit-Remaining", 
            String.valueOf(MAX_REQUESTS_PER_MINUTE - counter.getCount())
        );
        exchange.getResponse().getHeaders().add(
            "X-RateLimit-Reset", 
            String.valueOf(counter.getResetTime())
        );

        return chain.filter(exchange);
    };
}
```

**RequestCounter Inner Class**:
```java
private static class RequestCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    private volatile long windowStart = System.currentTimeMillis();

    public int incrementAndCheck() {
        long now = System.currentTimeMillis();
        
        // Reset if window expired (thread-safe)
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

**Rate Limit Headers Explained**:
```
X-RateLimit-Limit: 100           → Maximum requests allowed
X-RateLimit-Remaining: 73        → Requests left in current window
X-RateLimit-Reset: 1700000000000 → Unix timestamp when limit resets
```


---

### 5. JwtUtil.java
**Purpose**: JWT token parsing and validation

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    // Create signing key from secret
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Parse JWT and extract all claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Verify signature
                .build()
                .parseSignedClaims(token)     // Parse token
                .getPayload();                // Get claims
    }

    // Extract specific claims
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject(); // "sub" claim
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Validate token (check expiration)
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Invalid signature, malformed, etc.
        }
    }
}
```

**JWT Token Structure**:
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",              // User ID (subject)
    "email": "user@example.com",   // User email
    "role": "CUSTOMER",            // User role
    "iat": 1700000000,             // Issued at
    "exp": 1700003600              // Expires at (1 hour later)
  },
  "signature": "..."
}
```


---

### 6. FallbackController.java
**Purpose**: Provides fallback responses when services are unavailable

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        return createFallbackResponse(
            "Order Service", 
            "Order service is temporarily unavailable. Please try again later."
        );
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(
            String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("service", serviceName);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 503);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
```

**Fallback Endpoints**:
- `/fallback/auth` → Auth Service unavailable
- `/fallback/user` → User Service unavailable
- `/fallback/restaurant` → Restaurant Service unavailable
- `/fallback/order` → Order Service unavailable
- `/fallback/delivery` → Delivery Service unavailable
- `/fallback/payment` → Payment Service unavailable


---

### 7. SecurityConfig.java
**Purpose**: Configures Spring Security for reactive gateway

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)  // Disable CSRF
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/fallback/**").permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }
}
```

**Security Rules**:
1. **CSRF Disabled**: Stateless API doesn't need CSRF protection
2. **Public Paths**: `/api/auth/**`, `/actuator/health`, `/fallback/**`
3. **Protected Paths**: All other endpoints require authentication
4. **Authentication**: Handled by JwtAuthenticationFilter (custom)

---

### 8. CorsConfig.java
**Purpose**: Enables cross-origin requests from web clients

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allowed origins (frontend URLs)
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:3000",  // React dev
                "http://localhost:4200",  // Angular dev
                "https://yourdomain.com"  // Production
        ));
        
        // Allowed HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, auth headers)
        corsConfig.setAllowCredentials(true);
        
        // Expose custom headers to client
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
        ));
        
        // Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
```

**CORS Preflight Request Example**:
```http
OPTIONS /api/orders HTTP/1.1
Host: localhost:8080
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: Authorization, Content-Type

Response:
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```


---

## Scenario-Based Examples

### Scenario 1: Successful Authenticated Request

**Client Request**:
```http
GET /api/orders/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Gateway Processing Steps**:
```
1. Request arrives at Gateway (port 8080)
   ↓
2. JwtAuthenticationFilter executes
   - Extracts token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   - Validates signature and expiration
   - Extracts claims: userId="user123", email="user@example.com", role="CUSTOMER"
   - Adds headers: X-User-Id, X-User-Email, X-User-Role
   ↓
3. RateLimitFilter executes
   - Gets userId from X-User-Id header
   - Checks request count: 45/100
   - Increments counter: 46/100
   - Adds rate limit headers
   ↓
4. Route matching
   - Path "/api/orders/123" matches route "order-service"
   - Target: lb://order-service
   ↓
5. Service discovery
   - Queries Eureka for "order-service" instances
   - Returns: [order-service-1:8083, order-service-2:8084]
   - Selects: order-service-1:8083 (round-robin)
   ↓
6. Circuit breaker check
   - Circuit state: CLOSED (healthy)
   - Allows request to proceed
   ↓
7. Forward to order-service
   - URL: http://order-service-1:8083/api/orders/123
   - Headers include: X-User-Id, X-User-Email, X-User-Role
   ↓
8. Order service processes request
   - Uses X-User-Id to verify ownership
   - Returns order details
   ↓
9. Gateway returns response to client
```

**Response to Client**:
```http
HTTP/1.1 200 OK
Content-Type: application/json
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 54
X-RateLimit-Reset: 1700000060000

{
  "orderId": "123",
  "userId": "user123",
  "restaurantId": "rest456",
  "items": [...],
  "status": "CONFIRMED",
  "totalAmount": 45.99
}
```


---

### Scenario 2: Missing JWT Token (401 Unauthorized)

**Client Request**:
```http
GET /api/orders/123 HTTP/1.1
Host: localhost:8080
```

**Gateway Processing**:
```
1. Request arrives at Gateway
   ↓
2. JwtAuthenticationFilter executes
   - Checks if path is public: NO (/api/orders/123 is protected)
   - Extracts Authorization header: NULL
   - Returns error: "Missing or invalid Authorization header"
   ↓
3. Request stops here (not forwarded to service)
```

**Response to Client**:
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header"
}
```

**How to Fix**:
```bash
# Include Authorization header with Bearer token
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8080/api/orders/123
```


---

### Scenario 3: Expired JWT Token (401 Unauthorized)

**Client Request**:
```http
GET /api/orders/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiZXhwIjoxNjk5OTk5OTk5fQ...
```

**Token Payload** (decoded):
```json
{
  "sub": "user123",
  "email": "user@example.com",
  "role": "CUSTOMER",
  "iat": 1699996399,
  "exp": 1699999999  // Expired timestamp
}
```

**Gateway Processing**:
```
1. Request arrives at Gateway
   ↓
2. JwtAuthenticationFilter executes
   - Extracts token successfully
   - Validates token:
     * Signature: VALID ✓
     * Expiration: EXPIRED ✗ (exp < current time)
   - Returns error: "Invalid or expired token"
   ↓
3. Request stops here
```

**Response to Client**:
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

**How to Fix**:
```bash
# Get a new token by logging in again
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Or use refresh token endpoint
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<your-refresh-token>"}'
```


---

### Scenario 4: Rate Limit Exceeded (429 Too Many Requests)

**Client Makes 101 Requests in 1 Minute**:

**Request #1-100** (Successful):
```http
GET /api/restaurants HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>

Response:
HTTP/1.1 200 OK
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99  (decrements with each request)
X-RateLimit-Reset: 1700000060000

{ "restaurants": [...] }
```

**Request #101** (Rate Limited):
```http
GET /api/restaurants HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

**Gateway Processing**:
```
1. Request arrives at Gateway
   ↓
2. JwtAuthenticationFilter executes
   - Validates token: VALID ✓
   - Extracts userId: "user123"
   - Adds X-User-Id header
   ↓
3. RateLimitFilter executes
   - Gets userId: "user123"
   - Checks request count: 100/100
   - Increments: 101/100
   - Limit exceeded! Returns error
   ↓
4. Request stops here (not forwarded)
```

**Response to Client**:
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Maximum 100 requests per minute."
}
```

**Timeline Example**:
```
10:00:00 - Request #1   → 200 OK (Remaining: 99)
10:00:01 - Request #2   → 200 OK (Remaining: 98)
...
10:00:30 - Request #100 → 200 OK (Remaining: 0)
10:00:31 - Request #101 → 429 Too Many Requests
10:00:45 - Request #102 → 429 Too Many Requests
10:01:00 - Counter resets (new window starts)
10:01:01 - Request #103 → 200 OK (Remaining: 99)
```

**How to Handle**:
```javascript
// Client-side rate limit handling
async function makeRequest() {
  const response = await fetch('http://localhost:8080/api/restaurants', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (response.status === 429) {
    const resetTime = response.headers.get('X-RateLimit-Reset');
    const waitTime = resetTime - Date.now();
    
    console.log(`Rate limited. Retry after ${waitTime}ms`);
    await sleep(waitTime);
    return makeRequest(); // Retry
  }

  return response.json();
}
```


---

### Scenario 5: Circuit Breaker Opens (503 Service Unavailable)

**Initial State**: Order Service is healthy, circuit is CLOSED

**Request #1-5** (Service starts failing):
```http
GET /api/orders/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

**Gateway Processing**:
```
Request #1:
  Gateway → Order Service: Connection timeout (5s)
  Circuit Breaker: Records failure (1/10)
  Response: 503 Service Unavailable (fallback)

Request #2:
  Gateway → Order Service: Connection timeout (5s)
  Circuit Breaker: Records failure (2/10)
  Response: 503 Service Unavailable (fallback)

Request #3:
  Gateway → Order Service: Connection timeout (5s)
  Circuit Breaker: Records failure (3/10)
  Response: 503 Service Unavailable (fallback)

Request #4:
  Gateway → Order Service: Connection timeout (5s)
  Circuit Breaker: Records failure (4/10)
  Response: 503 Service Unavailable (fallback)

Request #5:
  Gateway → Order Service: Connection timeout (5s)
  Circuit Breaker: Records failure (5/10)
  Failure rate: 5/10 = 50% (threshold reached!)
  Circuit state: CLOSED → OPEN
  Response: 503 Service Unavailable (fallback)
```

**Request #6** (Circuit is OPEN):
```http
GET /api/orders/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

**Gateway Processing**:
```
1. Request arrives at Gateway
   ↓
2. Authentication & Rate Limiting: PASS ✓
   ↓
3. Circuit Breaker check
   - Circuit state: OPEN
   - Does NOT forward to service (fail fast)
   - Immediately returns fallback response
   ↓
4. Fallback Controller executes
   - Returns: /fallback/order response
```

**Response to Client**:
```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "error": "Service Unavailable",
  "service": "Order Service",
  "message": "Order service is temporarily unavailable. Please try again later.",
  "timestamp": "2024-11-18T10:30:45",
  "status": 503
}
```

**Circuit Breaker State Transitions**:
```
CLOSED (Normal operation)
  ↓ (50% failure rate reached)
OPEN (Fail fast, return fallback)
  ↓ (Wait 30 seconds)
HALF_OPEN (Test if service recovered)
  ↓ (3 successful requests)
CLOSED (Back to normal)

OR

HALF_OPEN
  ↓ (Any failure)
OPEN (Back to open state)
```

**Timeline Example**:
```
10:00:00 - Request #1 → Timeout → Circuit: CLOSED (1/10 failures)
10:00:05 - Request #2 → Timeout → Circuit: CLOSED (2/10 failures)
10:00:10 - Request #3 → Timeout → Circuit: CLOSED (3/10 failures)
10:00:15 - Request #4 → Timeout → Circuit: CLOSED (4/10 failures)
10:00:20 - Request #5 → Timeout → Circuit: OPEN (5/10 = 50%)
10:00:21 - Request #6 → Fallback (instant, no service call)
10:00:22 - Request #7 → Fallback (instant, no service call)
...
10:00:50 - Circuit: OPEN → HALF_OPEN (30s elapsed)
10:00:51 - Request #8 → Test call to service → Success!
10:00:52 - Request #9 → Test call to service → Success!
10:00:53 - Request #10 → Test call to service → Success!
10:00:54 - Circuit: HALF_OPEN → CLOSED (3 successes)
10:00:55 - Request #11 → Normal operation resumes
```


---

### Scenario 6: Public Endpoint Access (No Authentication)

**Client Request** (No token):
```http
GET /api/restaurants HTTP/1.1
Host: localhost:8080
```

**Gateway Processing**:
```
1. Request arrives at Gateway
   ↓
2. JwtAuthenticationFilter executes
   - Checks if path is public: YES (/api/restaurants is public)
   - Skips authentication
   - No X-User-Id header added
   ↓
3. RateLimitFilter executes
   - Gets userId from X-User-Id header: NULL
   - Skips rate limiting (no user to track)
   ↓
4. Routes to restaurant-service
   - Forwards request without user context
   ↓
5. Restaurant service returns public data
```

**Response to Client**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "restaurants": [
    {
      "id": "rest1",
      "name": "Pizza Palace",
      "cuisine": "Italian",
      "rating": 4.5,
      "deliveryTime": "30-40 min"
    },
    {
      "id": "rest2",
      "name": "Burger House",
      "cuisine": "American",
      "rating": 4.2,
      "deliveryTime": "25-35 min"
    }
  ]
}
```

**Note**: No rate limit headers because user is not authenticated.


---

### Scenario 7: User Login Flow (Complete Example)

**Step 1: User Registration**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!",
    "name": "John Doe",
    "phone": "+1234567890"
  }'
```

**Response**:
```json
{
  "userId": "user123",
  "email": "john@example.com",
  "name": "John Doe",
  "message": "Registration successful"
}
```

**Step 2: User Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwMDM2MDB9.signature",
  "refreshToken": "refresh-token-here",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Step 3: Use Access Token**
```bash
# Store token in variable
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Make authenticated request
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "orders": [
    {
      "orderId": "order123",
      "restaurantName": "Pizza Palace",
      "status": "DELIVERED",
      "totalAmount": 45.99,
      "createdAt": "2024-11-18T09:30:00"
    }
  ]
}
```

**Step 4: Token Expires (After 1 hour)**
```bash
# Same request after 1 hour
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

**Step 5: Refresh Token**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "refresh-token-here"
  }'
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new-token...",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```


---

### Scenario 8: Order Creation Flow (End-to-End)

**Step 1: Browse Restaurants** (Public)
```bash
curl http://localhost:8080/api/restaurants
```

**Step 2: View Restaurant Menu** (Public)
```bash
curl http://localhost:8080/api/restaurants/rest123/menu
```

**Step 3: Create Order** (Authenticated)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "rest123",
    "items": [
      {"menuItemId": "item1", "quantity": 2},
      {"menuItemId": "item2", "quantity": 1}
    ],
    "deliveryAddress": {
      "street": "123 Main St",
      "city": "New York",
      "zipCode": "10001"
    }
  }'
```

**Gateway Processing**:
```
1. JwtAuthenticationFilter
   - Validates token ✓
   - Extracts: userId="user123", role="CUSTOMER"
   - Adds headers: X-User-Id, X-User-Email, X-User-Role
   ↓
2. RateLimitFilter
   - Checks rate limit: 23/100 ✓
   - Increments: 24/100
   ↓
3. Routes to order-service
   - POST http://order-service:8083/api/orders
   - Headers include user context
   ↓
4. Order Service
   - Reads X-User-Id: "user123"
   - Creates order for this user
   - Validates restaurant and menu items
   - Calculates total amount
   - Publishes OrderCreated event
   ↓
5. Gateway returns response
```

**Response**:
```json
{
  "orderId": "order789",
  "userId": "user123",
  "restaurantId": "rest123",
  "status": "PENDING",
  "items": [
    {
      "menuItemId": "item1",
      "name": "Margherita Pizza",
      "quantity": 2,
      "price": 12.99,
      "subtotal": 25.98
    },
    {
      "menuItemId": "item2",
      "name": "Caesar Salad",
      "quantity": 1,
      "price": 8.99,
      "subtotal": 8.99
    }
  ],
  "subtotal": 34.97,
  "tax": 3.50,
  "deliveryFee": 5.00,
  "totalAmount": 43.47,
  "estimatedDeliveryTime": "2024-11-18T11:30:00",
  "createdAt": "2024-11-18T10:45:00"
}
```

**Step 4: Track Order** (Authenticated)
```bash
curl http://localhost:8080/api/orders/order789 \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "orderId": "order789",
  "status": "CONFIRMED",
  "restaurant": {
    "id": "rest123",
    "name": "Pizza Palace"
  },
  "delivery": {
    "status": "ASSIGNED",
    "driverId": "driver456",
    "driverName": "Mike Johnson",
    "estimatedArrival": "2024-11-18T11:25:00"
  },
  "timeline": [
    {"status": "PENDING", "timestamp": "2024-11-18T10:45:00"},
    {"status": "CONFIRMED", "timestamp": "2024-11-18T10:46:00"},
    {"status": "PREPARING", "timestamp": "2024-11-18T10:50:00"}
  ]
}
```


---

## Request Flow Diagrams

### Complete Request Flow with All Components

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT                                     │
│  (Web Browser / Mobile App / Postman)                               │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ HTTP Request
                             │ GET /api/orders/123
                             │ Authorization: Bearer <token>
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Port 8080)                         │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ 1. JwtAuthenticationFilter                                  │    │
│  │    - Check if public endpoint → NO                          │    │
│  │    - Extract Authorization header → Bearer <token>          │    │
│  │    - Validate JWT signature → VALID ✓                       │    │
│  │    - Check expiration → NOT EXPIRED ✓                       │    │
│  │    - Extract claims:                                        │    │
│  │      * userId: "user123"                                    │    │
│  │      * email: "user@example.com"                            │    │
│  │      * role: "CUSTOMER"                                     │    │
│  │    - Add headers:                                           │    │
│  │      * X-User-Id: user123                                   │    │
│  │      * X-User-Email: user@example.com                       │    │
│  │      * X-User-Role: CUSTOMER                                │    │
│  └────────────────────────────────────────────────────────────┘    │
│                             ↓                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ 2. RateLimitFilter                                          │    │
│  │    - Get userId from X-User-Id → "user123"                  │    │
│  │    - Check request count → 45/100                           │    │
│  │    - Increment counter → 46/100                             │    │
│  │    - Add response headers:                                  │    │
│  │      * X-RateLimit-Limit: 100                               │    │
│  │      * X-RateLimit-Remaining: 54                            │    │
│  │      * X-RateLimit-Reset: 1700000060000                     │    │
│  └────────────────────────────────────────────────────────────┘    │
│                             ↓                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ 3. Route Matching                                           │    │
│  │    - Path: /api/orders/123                                  │    │
│  │    - Matches route: "order-service"                         │    │
│  │    - Target: lb://order-service                             │    │
│  └────────────────────────────────────────────────────────────┘    │
│                             ↓                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ 4. Service Discovery (Eureka)                               │    │
│  │    - Query Eureka for "order-service"                       │    │
│  │    - Available instances:                                   │    │
│  │      * order-service-1: 192.168.1.10:8083                   │    │
│  │      * order-service-2: 192.168.1.11:8083                   │    │
│  │    - Load balancing: Round-Robin                            │    │
│  │    - Selected: order-service-1 (192.168.1.10:8083)          │    │
│  └────────────────────────────────────────────────────────────┘    │
│                             ↓                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ 5. Circuit Breaker Check                                    │    │
│  │    - Circuit name: orderServiceCircuitBreaker               │    │
│  │    - Current state: CLOSED                                  │    │
│  │    - Failure rate: 10% (below 50% threshold)                │    │
│  │    - Decision: ALLOW REQUEST                                │    │
│  └────────────────────────────────────────────────────────────┘    │
│                             ↓                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ 6. Retry Configuration                                      │    │
│  │    - Max retries: 3                                         │    │
│  │    - Retry on: Connection errors, timeouts                  │    │
│  └────────────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ Forward Request
                             │ GET http://192.168.1.10:8083/api/orders/123
                             │ Headers: X-User-Id, X-User-Email, X-User-Role
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ORDER SERVICE (Port 8083)                         │
│                                                                      │
│  - Receives request with user context headers                       │
│  - Validates user owns order (X-User-Id == order.userId)            │
│  - Retrieves order from database                                    │
│  - Returns order details                                            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ Response
                             │ 200 OK
                             │ { "orderId": "123", ... }
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY                                     │
│                                                                      │
│  - Receives response from order-service                             │
│  - Adds rate limit headers                                          │
│  - Records success in circuit breaker                               │
│  - Returns response to client                                       │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ HTTP Response
                             │ 200 OK
                             │ X-RateLimit-Limit: 100
                             │ X-RateLimit-Remaining: 54
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT                                     │
│  Receives order details                                             │
└─────────────────────────────────────────────────────────────────────┘
```


---

### Circuit Breaker State Machine

```
                    ┌─────────────────────────────────────┐
                    │         CLOSED                      │
                    │   (Normal Operation)                │
                    │                                     │
                    │  - All requests pass through        │
                    │  - Tracks success/failure rate      │
                    │  - Sliding window: 10 requests      │
                    └──────────┬──────────────────────────┘
                               │
                               │ Failure rate ≥ 50%
                               │ (5 out of 10 requests fail)
                               ▼
                    ┌─────────────────────────────────────┐
                    │          OPEN                       │
                    │   (Service Down)                    │
                    │                                     │
                    │  - Requests fail immediately        │
                    │  - Returns fallback response        │
                    │  - No calls to service              │
                    │  - Wait 30 seconds                  │
                    └──────────┬──────────────────────────┘
                               │
                               │ After 30 seconds
                               │ (automatic transition)
                               ▼
                    ┌─────────────────────────────────────┐
                    │       HALF_OPEN                     │
                    │   (Testing Recovery)                │
                    │                                     │
                    │  - Allow 3 test requests            │
                    │  - Monitor success rate             │
                    └──────────┬──────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
                │ All 3 succeed               │ Any failure
                ▼                             ▼
     ┌──────────────────┐          ┌──────────────────┐
     │     CLOSED       │          │      OPEN        │
     │  (Recovered)     │          │  (Still Down)    │
     └──────────────────┘          └──────────────────┘
```

**Configuration Values**:
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10                    # Track last 10 requests
        minimum-number-of-calls: 5                 # Need 5 calls before calculating
        failure-rate-threshold: 50                 # Open if ≥50% fail
        wait-duration-in-open-state: 30s           # Wait 30s before testing
        permitted-number-of-calls-in-half-open-state: 3  # 3 test requests
```


---

## Error Handling

### Error Response Format

All errors from the API Gateway follow a consistent format:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": "2024-11-18T10:30:00",
  "status": 400
}
```

### HTTP Status Codes

| Status Code | Error Type | Cause | Solution |
|-------------|------------|-------|----------|
| **400** | Bad Request | Invalid request format | Check request body/parameters |
| **401** | Unauthorized | Missing/invalid JWT token | Include valid Bearer token |
| **403** | Forbidden | Insufficient permissions | Check user role |
| **404** | Not Found | Resource doesn't exist | Verify resource ID |
| **429** | Too Many Requests | Rate limit exceeded | Wait for rate limit reset |
| **500** | Internal Server Error | Gateway error | Check gateway logs |
| **503** | Service Unavailable | Service down or circuit open | Wait for service recovery |
| **504** | Gateway Timeout | Service timeout (>5s) | Service may be overloaded |

---

### Error Scenarios with Examples

#### 1. Authentication Errors (401)

**Missing Authorization Header**:
```bash
curl http://localhost:8080/api/orders
```
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "status": 401
}
```

**Invalid Token Format**:
```bash
curl -H "Authorization: InvalidToken" http://localhost:8080/api/orders
```
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "status": 401
}
```

**Expired Token**:
```bash
curl -H "Authorization: Bearer expired-token" http://localhost:8080/api/orders
```
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "status": 401
}
```

**Invalid Signature**:
```bash
curl -H "Authorization: Bearer tampered-token" http://localhost:8080/api/orders
```
```json
{
  "error": "Unauthorized",
  "message": "Token validation failed: JWT signature does not match",
  "status": 401
}
```

---

#### 2. Rate Limiting Errors (429)

```bash
# After 100 requests in 1 minute
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders
```
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Maximum 100 requests per minute.",
  "status": 429
}
```

**Response Headers**:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1700000060000
```

---

#### 3. Service Unavailable Errors (503)

**Circuit Breaker Open**:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders
```
```json
{
  "error": "Service Unavailable",
  "service": "Order Service",
  "message": "Order service is temporarily unavailable. Please try again later.",
  "timestamp": "2024-11-18T10:30:45",
  "status": 503
}
```

**Service Not Registered**:
```json
{
  "error": "Service Unavailable",
  "message": "No instances available for service: order-service",
  "status": 503
}
```

---

#### 4. Timeout Errors (504)

**Service Response Timeout (>5 seconds)**:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders
```
```json
{
  "error": "Gateway Timeout",
  "message": "Request timeout after 5000ms",
  "status": 504
}
```


---

## Performance & Monitoring

### Actuator Endpoints

The API Gateway exposes several monitoring endpoints:

#### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    },
    "eureka": {
      "status": "UP",
      "details": {
        "applications": {
          "AUTH-SERVICE": 2,
          "USER-SERVICE": 2,
          "ORDER-SERVICE": 3,
          "RESTAURANT-SERVICE": 2,
          "DELIVERY-SERVICE": 2,
          "PAYMENT-SERVICE": 2
        }
      }
    }
  }
}
```

---

#### 2. Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

**Response**:
```json
[
  {
    "route_id": "auth-service",
    "route_definition": {
      "id": "auth-service",
      "predicates": [
        {
          "name": "Path",
          "args": {
            "pattern": "/api/auth/**"
          }
        }
      ],
      "filters": [
        {
          "name": "CircuitBreaker",
          "args": {
            "name": "authServiceCircuitBreaker",
            "fallbackUri": "forward:/fallback/auth"
          }
        },
        {
          "name": "Retry",
          "args": {
            "retries": 3
          }
        }
      ],
      "uri": "lb://auth-service",
      "order": 0
    },
    "order": 0
  }
]
```

---

#### 3. Metrics
```bash
# General metrics
curl http://localhost:8080/actuator/metrics

# Gateway-specific metrics
curl http://localhost:8080/actuator/metrics/spring.cloud.gateway.requests

# Circuit breaker metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls
```

**Gateway Request Metrics**:
```json
{
  "name": "spring.cloud.gateway.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 15234
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 45678.5
    }
  ],
  "availableTags": [
    {
      "tag": "routeId",
      "values": ["auth-service", "order-service", "restaurant-service"]
    },
    {
      "tag": "status",
      "values": ["200", "401", "429", "503"]
    }
  ]
}
```

**Circuit Breaker Metrics**:
```json
{
  "name": "resilience4j.circuitbreaker.calls",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1000
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": ["orderServiceCircuitBreaker", "authServiceCircuitBreaker"]
    },
    {
      "tag": "kind",
      "values": ["successful", "failed", "ignored"]
    }
  ]
}
```

---

### Performance Characteristics

#### Response Times

| Scenario | Average Response Time | Notes |
|----------|----------------------|-------|
| **Public endpoint** (no auth) | 10-20ms | Minimal processing |
| **Authenticated request** (service healthy) | 50-100ms | JWT validation + service call |
| **Rate limit check** | +2-5ms | In-memory counter lookup |
| **Circuit breaker (closed)** | +1-2ms | State check only |
| **Circuit breaker (open)** | 5-10ms | Immediate fallback |
| **Service timeout** | 5000ms | Configured timeout |

#### Throughput

```
Single Gateway Instance:
- Max requests/second: ~10,000 (reactive, non-blocking)
- Concurrent connections: ~50,000
- Memory usage: ~512MB (base) + ~1MB per 1000 active users (rate limiting)
```

#### Scaling

**Horizontal Scaling**:
```yaml
# Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3  # Run 3 instances
  template:
    spec:
      containers:
      - name: api-gateway
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

**Load Distribution**:
```
Client → Load Balancer (Nginx/AWS ALB)
           ↓
    ┌──────┼──────┐
    ▼      ▼      ▼
  GW-1   GW-2   GW-3  (3 Gateway instances)
    │      │      │
    └──────┼──────┘
           ▼
    Service Registry (Eureka)
           ↓
    Microservices
```


---

### Logging

#### Log Levels

```yaml
logging:
  level:
    org.springframework.cloud.gateway: INFO      # Gateway routing
    com.fooddelivery.gateway: DEBUG              # Custom filters
    reactor.netty: INFO                          # Network layer
```

#### Sample Logs

**Successful Request**:
```
2024-11-18 10:30:00 - [INFO] Route matched: order-service
2024-11-18 10:30:00 - [DEBUG] JWT validation successful for user: user123
2024-11-18 10:30:00 - [DEBUG] Rate limit check: 46/100 for user: user123
2024-11-18 10:30:00 - [INFO] Circuit breaker state: CLOSED for orderServiceCircuitBreaker
2024-11-18 10:30:00 - [INFO] Forwarding request to: lb://order-service
2024-11-18 10:30:00 - [INFO] Response received: 200 OK in 85ms
```

**Authentication Failure**:
```
2024-11-18 10:31:00 - [WARN] JWT validation failed: Token expired
2024-11-18 10:31:00 - [INFO] Returning 401 Unauthorized
```

**Rate Limit Exceeded**:
```
2024-11-18 10:32:00 - [WARN] Rate limit exceeded for user: user123 (101/100)
2024-11-18 10:32:00 - [INFO] Returning 429 Too Many Requests
```

**Circuit Breaker Opens**:
```
2024-11-18 10:33:00 - [ERROR] Request to order-service failed: Connection timeout
2024-11-18 10:33:00 - [WARN] Circuit breaker failure rate: 50% (5/10)
2024-11-18 10:33:00 - [ERROR] Circuit breaker state changed: CLOSED → OPEN
2024-11-18 10:33:00 - [INFO] Returning fallback response for order-service
```

**Circuit Breaker Recovers**:
```
2024-11-18 10:34:00 - [INFO] Circuit breaker state changed: OPEN → HALF_OPEN
2024-11-18 10:34:01 - [INFO] Test request to order-service: SUCCESS (1/3)
2024-11-18 10:34:02 - [INFO] Test request to order-service: SUCCESS (2/3)
2024-11-18 10:34:03 - [INFO] Test request to order-service: SUCCESS (3/3)
2024-11-18 10:34:03 - [INFO] Circuit breaker state changed: HALF_OPEN → CLOSED
```

---

### Monitoring Dashboard Example

**Prometheus Metrics** (exposed at `/actuator/prometheus`):
```
# Gateway request count by route and status
spring_cloud_gateway_requests_total{routeId="order-service",status="200"} 8543
spring_cloud_gateway_requests_total{routeId="order-service",status="503"} 12

# Circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)
resilience4j_circuitbreaker_state{name="orderServiceCircuitBreaker"} 0

# Circuit breaker calls
resilience4j_circuitbreaker_calls_total{name="orderServiceCircuitBreaker",kind="successful"} 8543
resilience4j_circuitbreaker_calls_total{name="orderServiceCircuitBreaker",kind="failed"} 12

# Rate limiter
gateway_rate_limit_exceeded_total{userId="user123"} 5
```

**Grafana Dashboard Queries**:
```promql
# Request rate per route
rate(spring_cloud_gateway_requests_total[5m])

# Error rate
rate(spring_cloud_gateway_requests_total{status=~"5.."}[5m])

# Circuit breaker failure rate
rate(resilience4j_circuitbreaker_calls_total{kind="failed"}[5m])

# Average response time
rate(spring_cloud_gateway_requests_seconds_sum[5m]) / 
rate(spring_cloud_gateway_requests_seconds_count[5m])
```


---

## Testing Guide

### 1. Manual Testing with cURL

#### Test Authentication Flow
```bash
# 1. Login (public endpoint)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  -v

# Expected: 200 OK with JWT token

# 2. Use token for authenticated request
TOKEN="<token-from-login>"
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -v

# Expected: 200 OK with orders list

# 3. Test without token
curl http://localhost:8080/api/orders -v

# Expected: 401 Unauthorized
```

#### Test Rate Limiting
```bash
# Bash script to test rate limiting
TOKEN="<your-token>"

for i in {1..105}; do
  echo "Request $i:"
  curl -s -o /dev/null -w "Status: %{http_code}\n" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/restaurants
  sleep 0.1
done

# Expected: First 100 return 200, next 5 return 429
```

#### Test Circuit Breaker
```bash
# 1. Stop order-service
docker stop order-service

# 2. Make requests to trigger circuit breaker
for i in {1..10}; do
  echo "Request $i:"
  curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/orders \
    -w "\nStatus: %{http_code}\n"
  sleep 1
done

# Expected: 
# - First 5 requests: 503 after 5s timeout
# - After 5 failures: Circuit opens, immediate 503 fallback

# 3. Start order-service
docker start order-service

# 4. Wait 30 seconds for circuit to go HALF_OPEN
sleep 30

# 5. Make 3 test requests
for i in {1..3}; do
  curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/orders
done

# Expected: Circuit closes after 3 successful requests
```

---

### 2. Integration Testing with Postman

**Collection Structure**:
```
Food Delivery API Gateway Tests
├── Authentication
│   ├── Login (Success)
│   ├── Login (Invalid Credentials)
│   ├── Register (Success)
│   └── Refresh Token
├── Protected Endpoints
│   ├── Get Orders (With Token)
│   ├── Get Orders (Without Token) → 401
│   ├── Create Order
│   └── Get User Profile
├── Rate Limiting
│   └── Exceed Rate Limit (Loop 101 times)
├── Circuit Breaker
│   ├── Service Available
│   └── Service Unavailable → Fallback
└── Public Endpoints
    ├── Browse Restaurants
    └── Health Check
```

**Environment Variables**:
```json
{
  "gateway_url": "http://localhost:8080",
  "access_token": "{{login_response.accessToken}}",
  "user_id": "{{login_response.userId}}"
}
```

**Test Scripts** (Postman):
```javascript
// After login request
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has access token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.accessToken).to.exist;
    pm.environment.set("access_token", jsonData.accessToken);
});

// Test rate limit headers
pm.test("Rate limit headers present", function () {
    pm.response.to.have.header("X-RateLimit-Limit");
    pm.response.to.have.header("X-RateLimit-Remaining");
    pm.response.to.have.header("X-RateLimit-Reset");
});

// Test circuit breaker fallback
pm.test("Fallback response structure", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.error).to.eql("Service Unavailable");
    pm.expect(jsonData.service).to.exist;
    pm.expect(jsonData.message).to.exist;
});
```

---

### 3. Load Testing with Apache JMeter

**Test Plan**:
```xml
<TestPlan>
  <ThreadGroup name="Gateway Load Test">
    <numThreads>100</numThreads>
    <rampUp>10</rampUp>
    <loops>100</loops>
    
    <HTTPSamplerProxy name="Login">
      <domain>localhost</domain>
      <port>8080</port>
      <path>/api/auth/login</path>
      <method>POST</method>
      <body>{"email":"test@example.com","password":"password123"}</body>
    </HTTPSamplerProxy>
    
    <HTTPSamplerProxy name="Get Orders">
      <domain>localhost</domain>
      <port>8080</port>
      <path>/api/orders</path>
      <method>GET</method>
      <header>Authorization: Bearer ${token}</header>
    </HTTPSamplerProxy>
  </ThreadGroup>
</TestPlan>
```

**Expected Results**:
```
Threads: 100 concurrent users
Ramp-up: 10 seconds
Iterations: 100 per user
Total requests: 10,000

Results:
- Average response time: 50-100ms
- 95th percentile: <200ms
- Error rate: <1%
- Throughput: ~1000 requests/second
```

---

### 4. Automated Testing with JUnit

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void testPublicEndpoint_NoAuth_Success() {
        webClient.get()
            .uri("/api/restaurants")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void testProtectedEndpoint_NoAuth_Unauthorized() {
        webClient.get()
            .uri("/api/orders")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Unauthorized");
    }

    @Test
    void testProtectedEndpoint_WithValidToken_Success() {
        String token = getValidToken();
        
        webClient.get()
            .uri("/api/orders")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-RateLimit-Limit")
            .expectHeader().exists("X-RateLimit-Remaining");
    }

    @Test
    void testRateLimit_ExceedLimit_TooManyRequests() {
        String token = getValidToken();
        
        // Make 101 requests
        for (int i = 0; i < 101; i++) {
            var response = webClient.get()
                .uri("/api/orders")
                .header("Authorization", "Bearer " + token)
                .exchange();
            
            if (i < 100) {
                response.expectStatus().isOk();
            } else {
                response.expectStatus().isEqualTo(429)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Too Many Requests");
            }
        }
    }

    @Test
    void testCircuitBreaker_ServiceDown_Fallback() {
        // Simulate service down
        stopOrderService();
        
        String token = getValidToken();
        
        webClient.get()
            .uri("/api/orders")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Service Unavailable")
            .jsonPath("$.service").isEqualTo("Order Service");
    }
}
```


---

## Configuration Reference

### application.yml (Complete)

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      # Enable service discovery integration
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      
      # Global filters applied to all routes
      default-filters:
        - name: JwtAuthenticationFilter
        - name: RateLimitFilter
      
      # CORS configuration
      globalcors:
        add-to-simple-url-handler-mapping: true
        cors-configurations:
          '[/**]':
            allowed-origins:
              - "http://localhost:3000"
              - "http://localhost:4200"
              - "https://yourdomain.com"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
              - OPTIONS
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600

server:
  port: 8080
  compression:
    enabled: true
  http2:
    enabled: true

# Eureka client configuration
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
    registry-fetch-interval-seconds: 5
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    instance-id: ${spring.application.name}:${random.value}

# JWT configuration
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production}
  expiration: 3600000  # 1 hour in milliseconds

# Resilience4j Circuit Breaker
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        record-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.client.ResourceAccessException
    instances:
      authServiceCircuitBreaker:
        base-config: default
      userServiceCircuitBreaker:
        base-config: default
      restaurantServiceCircuitBreaker:
        base-config: default
      orderServiceCircuitBreaker:
        base-config: default
      deliveryServiceCircuitBreaker:
        base-config: default
      paymentServiceCircuitBreaker:
        base-config: default
  
  timelimiter:
    configs:
      default:
        timeout-duration: 5s
        cancel-running-future: true

# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
    gateway:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}

# Logging configuration
logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: INFO
    org.springframework.web: INFO
    com.fooddelivery.gateway: DEBUG
    io.github.resilience4j: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/api-gateway.log
    max-size: 10MB
    max-history: 30
```

---

### Environment Variables

**Required**:
```bash
JWT_SECRET=your-256-bit-secret-key-must-be-at-least-256-bits-long
```

**Optional**:
```bash
# Server configuration
SERVER_PORT=8080

# Eureka configuration
EUREKA_URL=http://localhost:8761/eureka/

# Circuit breaker tuning
CIRCUIT_BREAKER_FAILURE_THRESHOLD=50
CIRCUIT_BREAKER_WAIT_DURATION=30s

# Rate limiting
RATE_LIMIT_MAX_REQUESTS=100
RATE_LIMIT_WINDOW_DURATION=60s

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_GATEWAY=DEBUG
```

---

### Docker Configuration

**Dockerfile**:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/api-gateway-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - EUREKA_URL=http://service-registry:8761/eureka/
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - service-registry
    networks:
      - food-delivery-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

networks:
  food-delivery-network:
    driver: bridge
```

---

### Kubernetes Configuration

**deployment.yml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  labels:
    app: api-gateway
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
        - name: EUREKA_URL
          value: "http://service-registry:8761/eureka/"
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
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
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  type: LoadBalancer
  selector:
    app: api-gateway
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
---
apiVersion: v1
kind: Secret
metadata:
  name: gateway-secrets
type: Opaque
data:
  jwt-secret: <base64-encoded-secret>
```


---

## Troubleshooting Guide

### Common Issues and Solutions

#### Issue 1: Gateway Cannot Connect to Eureka

**Symptoms**:
```
ERROR - DiscoveryClient: Cannot execute request on any known server
```

**Causes**:
1. Eureka server not running
2. Wrong Eureka URL
3. Network connectivity issues

**Solutions**:
```bash
# 1. Check if Eureka is running
curl http://localhost:8761/

# 2. Verify Eureka URL in configuration
cat application.yml | grep eureka

# 3. Check network connectivity
ping localhost
telnet localhost 8761

# 4. Check Docker network (if using Docker)
docker network inspect food-delivery-network
```

---

#### Issue 2: JWT Validation Fails

**Symptoms**:
```json
{
  "error": "Unauthorized",
  "message": "Token validation failed: JWT signature does not match"
}
```

**Causes**:
1. JWT secret mismatch between auth-service and gateway
2. Token generated with different secret
3. Token tampered with

**Solutions**:
```bash
# 1. Verify JWT secret matches in both services
# Gateway:
echo $JWT_SECRET

# Auth Service:
# Check auth-service JWT secret

# 2. Generate new token with correct secret
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# 3. Decode token to verify claims (use jwt.io)
```

---

#### Issue 3: Rate Limiting Not Working

**Symptoms**:
- Can make more than 100 requests per minute
- No rate limit headers in response

**Causes**:
1. User not authenticated (no X-User-Id header)
2. RateLimitFilter not applied
3. Multiple gateway instances with separate counters

**Solutions**:
```bash
# 1. Verify authentication is working
curl -v http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  | grep "X-RateLimit"

# 2. Check filter configuration
curl http://localhost:8080/actuator/gateway/routes \
  | grep RateLimitFilter

# 3. For distributed rate limiting, use Redis
# Add to pom.xml:
# <dependency>
#   <groupId>org.springframework.boot</groupId>
#   <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
# </dependency>
```

---

#### Issue 4: Circuit Breaker Not Opening

**Symptoms**:
- Service is down but requests still timeout (5s)
- No fallback response

**Causes**:
1. Not enough requests to trigger (need 5 minimum)
2. Failure rate below threshold (need 50%)
3. Circuit breaker not configured for route

**Solutions**:
```bash
# 1. Check circuit breaker configuration
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state

# 2. Verify circuit breaker is applied to route
curl http://localhost:8080/actuator/gateway/routes \
  | grep -A 10 "order-service"

# 3. Check circuit breaker metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls

# 4. Lower threshold for testing
# In application.yml:
# resilience4j:
#   circuitbreaker:
#     configs:
#       default:
#         failure-rate-threshold: 30  # Lower threshold
```

---

#### Issue 5: CORS Errors in Browser

**Symptoms**:
```
Access to fetch at 'http://localhost:8080/api/orders' from origin 
'http://localhost:3000' has been blocked by CORS policy
```

**Causes**:
1. Frontend origin not in allowed origins list
2. CORS configuration not applied
3. Preflight request failing

**Solutions**:
```bash
# 1. Add frontend origin to CorsConfig.java
# corsConfig.setAllowedOrigins(List.of(
#     "http://localhost:3000",  // Add your frontend URL
#     "http://localhost:4200"
# ));

# 2. Test CORS with curl
curl -X OPTIONS http://localhost:8080/api/orders \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Expected headers in response:
# Access-Control-Allow-Origin: http://localhost:3000
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS

# 3. Check if CORS filter is registered
# Look for CorsWebFilter in logs
```

---

#### Issue 6: Service Not Found (503)

**Symptoms**:
```json
{
  "error": "Service Unavailable",
  "message": "No instances available for service: order-service"
}
```

**Causes**:
1. Service not registered with Eureka
2. Service name mismatch
3. Service instances are down

**Solutions**:
```bash
# 1. Check Eureka dashboard
open http://localhost:8761/

# 2. Verify service is registered
curl http://localhost:8761/eureka/apps/ORDER-SERVICE

# 3. Check service name in route configuration
# In GatewayConfig.java:
# .uri("lb://order-service")  // Must match Eureka registration

# 4. Restart service with correct configuration
# In order-service application.yml:
# spring:
#   application:
#     name: order-service  # Must match route URI
```

---

#### Issue 7: High Memory Usage

**Symptoms**:
- Gateway memory usage grows over time
- OutOfMemoryError

**Causes**:
1. Rate limit counters not being cleaned up
2. Too many concurrent connections
3. Memory leak in custom filters

**Solutions**:
```bash
# 1. Monitor memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# 2. Add cleanup for rate limit counters
# In RateLimitFilter.java, add scheduled cleanup:
# @Scheduled(fixedRate = 300000) // Every 5 minutes
# public void cleanupExpiredCounters() {
#     long now = System.currentTimeMillis();
#     requestCounts.entrySet().removeIf(entry -> 
#         now - entry.getValue().getWindowStart() > 600000
#     );
# }

# 3. Increase heap size
java -Xmx1g -Xms512m -jar api-gateway.jar

# 4. Use Redis for rate limiting (distributed + automatic cleanup)
```

---

#### Issue 8: Slow Response Times

**Symptoms**:
- Requests taking >1 second
- High latency

**Causes**:
1. Downstream service is slow
2. Too many filters
3. Network latency
4. Circuit breaker timeout too high

**Solutions**:
```bash
# 1. Check response times by route
curl http://localhost:8080/actuator/metrics/spring.cloud.gateway.requests \
  | grep -A 5 "TOTAL_TIME"

# 2. Reduce circuit breaker timeout
# In application.yml:
# resilience4j:
#   timelimiter:
#     configs:
#       default:
#         timeout-duration: 2s  # Reduce from 5s

# 3. Enable HTTP/2
# In application.yml:
# server:
#   http2:
#     enabled: true

# 4. Enable compression
# server:
#   compression:
#     enabled: true
#     mime-types: application/json,application/xml,text/html,text/xml,text/plain

# 5. Profile with JProfiler or VisualVM
```

---

## Best Practices

### 1. Security

✅ **DO**:
- Use strong JWT secrets (256+ bits)
- Rotate JWT secrets regularly
- Store secrets in environment variables or secret managers
- Enable HTTPS in production
- Implement request size limits
- Add security headers (X-Frame-Options, X-Content-Type-Options)

❌ **DON'T**:
- Hardcode JWT secrets in code
- Use weak secrets like "secret" or "password"
- Expose sensitive information in error messages
- Allow unlimited request sizes

### 2. Performance

✅ **DO**:
- Use reactive programming (WebFlux)
- Enable HTTP/2 and compression
- Set appropriate timeouts (2-5 seconds)
- Use connection pooling
- Cache static responses
- Monitor and optimize slow routes

❌ **DON'T**:
- Block threads with synchronous calls
- Set timeouts too high (>10 seconds)
- Log full request/response bodies in production
- Keep rate limit counters indefinitely

### 3. Resilience

✅ **DO**:
- Configure circuit breakers for all routes
- Provide meaningful fallback responses
- Set appropriate retry policies
- Monitor circuit breaker states
- Test failure scenarios regularly

❌ **DON'T**:
- Retry on all errors (avoid retry storms)
- Set circuit breaker thresholds too low
- Ignore circuit breaker metrics
- Forget to test fallback responses

### 4. Monitoring

✅ **DO**:
- Expose Prometheus metrics
- Log important events (auth failures, rate limits, circuit breaker state changes)
- Set up alerts for high error rates
- Monitor response times by route
- Track circuit breaker state changes

❌ **DON'T**:
- Log sensitive data (passwords, tokens)
- Ignore warning logs
- Disable health checks
- Forget to monitor downstream services

---

## Summary

The API Gateway is a critical component that provides:

1. **Single Entry Point**: All client requests go through the gateway
2. **Authentication**: JWT validation and user context propagation
3. **Rate Limiting**: 100 requests/minute per user
4. **Resilience**: Circuit breakers with fallback responses
5. **Routing**: Dynamic routing to microservices via Eureka
6. **Security**: CORS, security headers, request validation
7. **Monitoring**: Actuator endpoints, metrics, health checks

**Key Files**:
- `GatewayConfig.java`: Route definitions
- `JwtAuthenticationFilter.java`: JWT validation
- `RateLimitFilter.java`: Rate limiting logic
- `FallbackController.java`: Fallback responses
- `application.yml`: Configuration

**Performance**:
- Response time: 50-100ms (authenticated)
- Throughput: ~10,000 requests/second per instance
- Scales horizontally with multiple instances

**Monitoring**:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Routes: `/actuator/gateway/routes`

For questions or issues, refer to the troubleshooting guide or check the logs at `logs/api-gateway.log`.
