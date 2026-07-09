# Auth Service - Comprehensive Documentation

## Table of Contents
1. [Architecture & Purpose](#architecture--purpose)
2. [Complete File Analysis](#complete-file-analysis)
3. [Component Deep Dive](#component-deep-dive)
4. [Scenario-Based Examples](#scenario-based-examples)
5. [Request/Response Flow](#requestresponse-flow)
6. [Database Schema](#database-schema)
7. [Integration Points](#integration-points)
8. [Error Handling](#error-handling)
9. [Configuration Reference](#configuration-reference)
10. [Testing Guide](#testing-guide)
11. [Performance & Monitoring](#performance--monitoring)
12. [Troubleshooting Guide](#troubleshooting-guide)
13. [FAQ Section](#faq-section)

---

## 1. Architecture & Purpose

### What Problem Does This Service Solve?

The Auth Service is the security backbone of the Food Delivery Platform. It solves several critical problems:

- **Centralized Authentication**: Single source of truth for user credentials across all microservices
- **Secure Token Management**: JWT-based stateless authentication with token blacklisting
- **Account Security**: Protection against brute force attacks with account lockout mechanisms
- **Password Security**: Industry-standard bcrypt hashing with configurable cost factor
- **Session Management**: Refresh token rotation and revocation for secure long-lived sessions

### How Does It Fit in the Overall System?

```
┌─────────────┐
│   Client    │
│ (Web/Mobile)│
└──────┬──────┘
       │
       │ 1. Login/Register
       ▼
┌─────────────────┐
│  API Gateway    │
│  (Port 8080)    │
└────────┬────────┘
         │
         │ 2. Forward Auth Request
         ▼
┌──────────────────────┐
│   Auth Service       │
│   (Port 8081)        │
│                      │
│  ┌────────────────┐  │
│  │ JWT Provider   │  │
│  └────────────────┘  │
└──────┬───────┬───────┘
       │       │
       │       │ 3. Store Credentials
       ▼       ▼
┌──────────┐ ┌──────────┐
│PostgreSQL│ │  Redis   │
│(Creds DB)│ │(Blacklist│
└──────────┘ └──────────┘
       │
       │ 4. Create User Profile
       ▼
┌──────────────────┐
│  User Service    │
│  (Port 8083)     │
└──────────────────┘
```

### Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.x | Core application framework |
| Security | Spring Security | 6.x | Security configuration |
| Database | PostgreSQL | 15+ | Persistent credential storage |
| Cache | Redis | 6+ | Token blacklist (in-memory) |
| JWT Library | JJWT | 0.12.x | Token generation/validation |
| Service Discovery | Eureka Client | 4.x | Service registration |
| API Docs | SpringDoc OpenAPI | 2.x | API documentation |
| Build Tool | Maven | 3.6+ | Dependency management |

---

## 2. Complete File Analysis

### Project Structure

```
auth-service/
├── src/
│   └── main/
│       ├── java/com/fooddelivery/auth/
│       │   ├── AuthServiceApplication.java
│       │   ├── config/
│       │   │   ├── AppConfig.java
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   └── AuthController.java
│       │   ├── dto/
│       │   │   ├── AuthResponse.java
│       │   │   ├── LoginRequest.java
│       │   │   └── RegisterUserRequest.java
│       │   ├── entity/
│       │   │   ├── RefreshToken.java
│       │   │   ├── UserCredential.java
│       │   │   └── UserRole.java
│       │   ├── exception/
│       │   │   ├── AccountLockedException.java
│       │   │   ├── AuthenticationException.java
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   └── UserAlreadyExistsException.java
│       │   ├── repository/
│       │   │   ├── RefreshTokenRepository.java
│       │   │   └── UserCredentialRepository.java
│       │   ├── security/
│       │   │   └── JwtTokenProvider.java
│       │   └── service/
│       │       └── AuthenticationService.java
│       └── resources/
│           ├── application.yml
│           └── bootstrap.yml
├── Dockerfile
└── pom.xml
```

### File-by-File Analysis

#### 2.1 AuthServiceApplication.java

**Purpose**: Main entry point for the Spring Boot application.

```java
package com.fooddelivery.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication  // Line 6: Enables auto-configuration, component scanning
@EnableDiscoveryClient  // Line 7: Registers with Eureka service registry
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

**Line-by-Line Explanation**:
- **Line 6**: `@SpringBootApplication` - Composite annotation that enables:
  - `@Configuration`: Marks class as source of bean definitions
  - `@EnableAutoConfiguration`: Enables Spring Boot's auto-configuration
  - `@ComponentScan`: Scans for components in current package and sub-packages
- **Line 7**: `@EnableDiscoveryClient` - Enables service registration with Eureka
- **Line 10-12**: Standard Spring Boot main method that bootstraps the application

**Interactions**: This is the starting point. It initializes the Spring context, which:
1. Loads all configuration classes (SecurityConfig, AppConfig)
2. Scans and registers all components (controllers, services, repositories)
3. Connects to Config Server to fetch configuration
4. Registers with Eureka Service Registry
5. Starts embedded Tomcat server on port 8081

---

#### 2.2 SecurityConfig.java

**Purpose**: Configures Spring Security and password encoding.

```java
package com.fooddelivery.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration              // Line 12: Marks as configuration class
@EnableWebSecurity         // Line 13: Enables Spring Security
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with cost factor 12 as per requirements
        return new BCryptPasswordEncoder(12);  // Line 19: Cost factor = 2^12 iterations
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Line 25: Disable CSRF for stateless API
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Line 27: No sessions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()      // Line 29: Public auth endpoints
                        .requestMatchers("/actuator/**").permitAll()      // Line 30: Public health checks
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()  // Line 31: Public docs
                        .anyRequest().authenticated()                     // Line 32: All others need auth
                );

        return http.build();
    }
}
```

**Line-by-Line Explanation**:
- **Line 19**: `BCryptPasswordEncoder(12)` - Creates password encoder with cost factor 12
  - Cost factor 12 = 2^12 = 4,096 iterations
  - Higher cost = more secure but slower (12 is industry standard)
  - Each increment doubles the computation time
- **Line 25**: Disables CSRF protection (not needed for stateless JWT APIs)
- **Line 27**: `STATELESS` - No HTTP sessions created (JWT handles state)
- **Lines 29-32**: Authorization rules:
  - `/api/auth/**` - Public (login, register, etc.)
  - `/actuator/**` - Public (health checks for Kubernetes)
  - `/swagger-ui/**` - Public (API documentation)
  - Everything else requires authentication

**Interactions**:
- Used by Spring Security filter chain on every request
- `PasswordEncoder` bean injected into `AuthenticationService`
- Security filter chain processes requests before reaching controllers

---

#### 2.3 AuthController.java

**Purpose**: REST API endpoints for authentication operations.

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authenticationService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String jti) {
        boolean isBlacklisted = authenticationService.isTokenBlacklisted(jti);
        return ResponseEntity.ok(Map.of("valid", !isBlacklisted));
    }
}
```

**Endpoint Breakdown**:

| Endpoint | Method | Purpose | Auth Required |
|----------|--------|---------|---------------|
| `/api/auth/register` | POST | Create new user account | No |
| `/api/auth/login` | POST | Authenticate and get tokens | No |
| `/api/auth/logout` | POST | Invalidate access token | Yes |
| `/api/auth/refresh` | POST | Get new access token | No (uses refresh token) |
| `/api/auth/validate` | GET | Check if token is blacklisted | No (internal use) |

**Key Annotations**:
- `@Valid`: Triggers validation on request DTOs
- `@RequestBody`: Deserializes JSON to Java object
- `@RequestHeader`: Extracts Authorization header
- `@RequestParam`: Extracts query parameter

**Interactions**:
- Delegates all business logic to `AuthenticationService`
- Returns standardized `AuthResponse` DTOs
- Validation errors caught by `GlobalExceptionHandler`

---


#### 2.4 DTOs (Data Transfer Objects)

##### RegisterUserRequest.java

**Purpose**: Validates and transfers user registration data.

```java
public class RegisterUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private UserRole role = UserRole.CUSTOMER;
    
    // Getters and setters...
}
```

**Validation Rules**:
- **Email**: Must be valid email format (RFC 5322)
- **Password**: 
  - Minimum 8 characters
  - At least one uppercase letter (A-Z)
  - At least one lowercase letter (a-z)
  - At least one digit (0-9)
- **Phone**: E.164 format (international phone number standard)
- **Role**: Defaults to CUSTOMER if not specified

**Example Valid Request**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+14155552671",
  "role": "CUSTOMER"
}
```

##### LoginRequest.java

**Purpose**: Validates login credentials.

```java
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    
    // Getters and setters...
}
```

**Example**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

##### AuthResponse.java

**Purpose**: Standardized authentication response with tokens.

```java
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private Long userId;
    private String email;
    private UserRole role;
    
    // Constructors, getters and setters...
}
```

**Example Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjMiLCJlbWFpbCI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwicm9sZSI6IkNVU1RPTUVSIiwianRpIjoiYWJjZC0xMjM0IiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjMiLCJqdGkiOiJ4eXp3LTU2NzgiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDYwNDgwMH0.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 123,
  "email": "john.doe@example.com",
  "role": "CUSTOMER"
}
```

---

#### 2.5 Entities

##### UserCredential.java

**Purpose**: JPA entity representing user authentication credentials.

```java
@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // References User Service user ID

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;  // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isEmailVerified = false;

    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) {
            role = UserRole.CUSTOMER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    // Getters and setters...
}
```

**Field Explanations**:
- **id**: Auto-generated primary key
- **userId**: Links to User Service (same value as id after creation)
- **email**: Unique identifier for login
- **passwordHash**: BCrypt hash (never store plain password)
- **role**: User's permission level (enum)
- **isActive**: Account enabled/disabled flag
- **isEmailVerified**: Email verification status
- **failedLoginAttempts**: Counter for brute force protection
- **lockedUntil**: Timestamp when account unlocks (null if not locked)
- **createdAt/updatedAt**: Audit timestamps

**Security Features**:
- `isAccountLocked()` method checks if current time is before `lockedUntil`
- `@PrePersist` and `@PreUpdate` automatically manage timestamps
- Password is NEVER stored in plain text

##### RefreshToken.java

**Purpose**: Stores refresh tokens for validation and revocation.

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isRevoked = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Getters and setters...
}
```

**Why Store Refresh Tokens?**
- Allows server-side revocation (logout all devices)
- Tracks token usage and expiration
- Prevents replay attacks with revoked tokens

##### UserRole.java

**Purpose**: Enum defining user permission levels.

```java
public enum UserRole {
    CUSTOMER,           // Regular users ordering food
    RESTAURANT_ADMIN,   // Restaurant owners/managers
    DELIVERY_AGENT,     // Delivery personnel
    SYSTEM_ADMIN        // Platform administrators
}
```

---

#### 2.6 Repositories

##### UserCredentialRepository.java

**Purpose**: Data access layer for user credentials.

```java
@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findByUserId(Long userId);

    boolean existsByEmail(String email);
}
```

**Query Methods**:
- `findByEmail`: Used during login to retrieve credentials
- `findByUserId`: Used when refreshing tokens
- `existsByEmail`: Used during registration to check duplicates

**Spring Data JPA Magic**: These methods are automatically implemented by Spring Data JPA using method name conventions.

##### RefreshTokenRepository.java

**Purpose**: Data access layer for refresh tokens.

```java
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
```

**Custom Queries**:
- `deleteByUserId`: Revokes all tokens for a user (logout all devices)
- `deleteExpiredTokens`: Cleanup job to remove expired tokens

---

#### 2.7 JwtTokenProvider.java

**Purpose**: Core JWT token generation and validation logic.

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms:86400000}") // 24 hours
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}") // 7 days
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserCredential credential) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(credential.getUserId().toString())
                .claim("email", credential.getEmail())
                .claim("role", credential.getRole().name())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserCredential credential) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(credential.getUserId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException | 
                 UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }
    
    // Additional utility methods...
}
```

**JWT Structure**:

Access Token:
```json
{
  "sub": "123",                    // Subject: User ID
  "email": "john@example.com",     // Custom claim
  "role": "CUSTOMER",              // Custom claim
  "jti": "abc-123-def-456",        // JWT ID (unique identifier)
  "iat": 1700000000,               // Issued at (Unix timestamp)
  "exp": 1700086400                // Expiration (Unix timestamp)
}
```

Refresh Token (minimal claims):
```json
{
  "sub": "123",                    // Subject: User ID
  "jti": "xyz-789-uvw-012",        // JWT ID
  "iat": 1700000000,               // Issued at
  "exp": 1700604800                // Expiration (7 days)
}
```

**Security Considerations**:
- Uses HS512 algorithm (HMAC with SHA-512)
- Secret key must be at least 512 bits (64 bytes)
- Each token has unique JTI for blacklisting
- Refresh tokens have minimal claims to reduce size

---

#### 2.8 AuthenticationService.java

**Purpose**: Core business logic for authentication operations.

**Key Methods**:

##### register()
```java
@Transactional
public AuthResponse register(RegisterUserRequest request) {
    // 1. Check if user exists
    if (credentialRepository.existsByEmail(request.getEmail())) {
        throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
    }

    // 2. Create credential with hashed password
    UserCredential credential = new UserCredential();
    credential.setEmail(request.getEmail());
    credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));  // BCrypt hash
    credential.setRole(request.getRole());
    credential.setIsActive(true);
    credential.setPasswordChangedAt(LocalDateTime.now());

    // 3. Save to get ID
    credential = credentialRepository.save(credential);

    // 4. Set userId to credential ID
    credential.setUserId(credential.getId());
    credential = credentialRepository.save(credential);

    // 5. Create user profile in User Service
    createUserProfile(credential.getUserId(), request);

    // 6. Generate tokens
    String accessToken = jwtTokenProvider.generateAccessToken(credential);
    String refreshToken = jwtTokenProvider.generateRefreshToken(credential);

    // 7. Save refresh token
    saveRefreshToken(credential.getUserId(), refreshToken);

    // 8. Return response
    return new AuthResponse(accessToken, refreshToken, 
        jwtTokenProvider.getAccessTokenExpirationMs() / 1000,
        credential.getUserId(), credential.getEmail(), credential.getRole());
}
```

**Flow**:
1. Validate email uniqueness
2. Hash password with BCrypt (cost factor 12)
3. Save credential to database
4. Create user profile in User Service (async)
5. Generate JWT tokens
6. Store refresh token
7. Return tokens to client

##### login()
```java
@Transactional
public AuthResponse login(LoginRequest request) {
    // 1. Find user by email
    UserCredential credential = credentialRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

    // 2. Check if account is locked
    if (credential.isAccountLocked()) {
        throw new AccountLockedException("Account is locked until " + credential.getLockedUntil());
    }

    // 3. Check if account is active
    if (!credential.getIsActive()) {
        throw new AuthenticationException("Account is inactive");
    }

    // 4. Verify password
    if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
        handleFailedLogin(credential);  // Increment failed attempts
        throw new AuthenticationException("Invalid email or password");
    }

    // 5. Reset failed attempts and update last login
    credential.setFailedLoginAttempts(0);
    credential.setLockedUntil(null);
    credential.setLastLoginAt(LocalDateTime.now());
    credentialRepository.save(credential);

    // 6. Generate tokens
    String accessToken = jwtTokenProvider.generateAccessToken(credential);
    String refreshToken = jwtTokenProvider.generateRefreshToken(credential);

    // 7. Save refresh token
    saveRefreshToken(credential.getUserId(), refreshToken);

    // 8. Return response
    return new AuthResponse(accessToken, refreshToken,
        jwtTokenProvider.getAccessTokenExpirationMs() / 1000,
        credential.getUserId(), credential.getEmail(), credential.getRole());
}
```

**Security Features**:
- Constant-time password comparison (BCrypt)
- Account lockout after 5 failed attempts
- 30-minute lockout duration
- Generic error messages (don't reveal if email exists)

##### logout()
```java
@Transactional
public void logout(String token) {
    // Extract token ID
    String jti = jwtTokenProvider.getTokenId(token);
    
    // Calculate remaining time until expiration
    long expirationTime = jwtTokenProvider.getExpirationTimeInSeconds(token);

    // Add to Redis blacklist with TTL
    if (expirationTime > 0) {
        redisTemplate.opsForValue().set(
                "blacklist:" + jti,
                "revoked",
                expirationTime,
                TimeUnit.SECONDS
        );
    }
}
```

**Why Redis for Blacklist?**
- Fast in-memory lookups (< 1ms)
- Automatic expiration with TTL
- No need to store expired tokens
- Scales horizontally

##### refreshToken()
```java
@Transactional
public AuthResponse refreshToken(String refreshToken) {
    // 1. Validate token signature and expiration
    if (!jwtTokenProvider.validateToken(refreshToken)) {
        throw new AuthenticationException("Invalid refresh token");
    }

    // 2. Check if token exists in database
    RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

    // 3. Check if revoked or expired
    if (storedToken.getIsRevoked() || storedToken.isExpired()) {
        throw new AuthenticationException("Refresh token is invalid or expired");
    }

    // 4. Get user credential
    Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
    UserCredential credential = credentialRepository.findByUserId(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));

    // 5. Generate new access token
    String newAccessToken = jwtTokenProvider.generateAccessToken(credential);

    // 6. Return response (same refresh token)
    return new AuthResponse(newAccessToken, refreshToken,
        jwtTokenProvider.getAccessTokenExpirationMs() / 1000,
        credential.getUserId(), credential.getEmail(), credential.getRole());
}
```

**Refresh Token Strategy**:
- Refresh tokens are long-lived (7 days)
- Access tokens are short-lived (24 hours)
- Refresh token is NOT rotated (same token returned)
- For rotation strategy, generate new refresh token and revoke old one

---

#### 2.9 Exception Handling

##### GlobalExceptionHandler.java

**Purpose**: Centralized exception handling with standardized error responses.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                "AUTHENTICATION_ERROR",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(
            AccountLockedException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                "ACCOUNT_LOCKED",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                "USER_ALREADY_EXISTS",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

**Error Response Format**:
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/login",
  "details": {}
}
```

**HTTP Status Codes**:
- `401 UNAUTHORIZED`: Invalid credentials or token
- `403 FORBIDDEN`: Account locked
- `409 CONFLICT`: User already exists
- `400 BAD REQUEST`: Validation errors
- `500 INTERNAL SERVER ERROR`: Unexpected errors

---

## 3. Component Deep Dive

### Controllers

**AuthController** exposes 5 REST endpoints:

| Endpoint | Method | Request | Response | Status Codes |
|----------|--------|---------|----------|--------------|
| `/api/auth/register` | POST | RegisterUserRequest | AuthResponse | 201, 400, 409 |
| `/api/auth/login` | POST | LoginRequest | AuthResponse | 200, 401, 403 |
| `/api/auth/logout` | POST | Authorization header | Success message | 200, 401 |
| `/api/auth/refresh` | POST | Refresh token | AuthResponse | 200, 401 |
| `/api/auth/validate` | GET | jti query param | Validation result | 200 |

### Services

**AuthenticationService** implements:
- User registration with password hashing
- User authentication with brute force protection
- Token generation and validation
- Token blacklisting for logout
- Refresh token rotation

### Repositories

**UserCredentialRepository**:
- CRUD operations for user credentials
- Email-based lookups
- Duplicate email checks

**RefreshTokenRepository**:
- CRUD operations for refresh tokens
- Token validation
- Bulk deletion for cleanup

### Security Components

**JwtTokenProvider**:
- Generates access tokens (24-hour expiration)
- Generates refresh tokens (7-day expiration)
- Validates token signatures
- Extracts claims from tokens

**SecurityConfig**:
- Configures BCrypt password encoder (cost factor 12)
- Disables CSRF for stateless API
- Configures public endpoints
- Enforces stateless session management

### DTOs

**Request DTOs**:
- `RegisterUserRequest`: Validates registration data
- `LoginRequest`: Validates login credentials

**Response DTOs**:
- `AuthResponse`: Standardized token response

### Entities

**UserCredential**:
- Stores hashed passwords
- Tracks failed login attempts
- Manages account lockout

**RefreshToken**:
- Stores refresh tokens
- Tracks expiration
- Supports revocation

---

## 4. Scenario-Based Examples

### Scenario 1: New User Registration

**Use Case**: A new customer wants to create an account.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123",
    "firstName": "Alice",
    "lastName": "Johnson",
    "phoneNumber": "+14155551234",
    "role": "CUSTOMER"
  }'
```

**Processing Flow**:
```
1. Request arrives at AuthController.register()
   ↓
2. @Valid annotation triggers validation
   - Email format check
   - Password strength check (min 8 chars, uppercase, lowercase, digit)
   - Phone number format check
   ↓
3. AuthenticationService.register() called
   ↓
4. Check if email exists: credentialRepository.existsByEmail()
   - If exists → throw UserAlreadyExistsException (409 CONFLICT)
   ↓
5. Hash password with BCrypt (cost factor 12)
   - Input: "SecurePass123"
   - Output: "$2a$12$KIXxKj3..."
   ↓
6. Save UserCredential to PostgreSQL
   - id: 1 (auto-generated)
   - userId: 1 (set to id)
   - email: "alice@example.com"
   - passwordHash: "$2a$12$KIXxKj3..."
   - role: CUSTOMER
   - isActive: true
   - failedLoginAttempts: 0
   ↓
7. Create user profile in User Service (async)
   - POST http://user-service/api/users
   ↓
8. Generate JWT tokens
   - Access token (24-hour expiration)
   - Refresh token (7-day expiration)
   ↓
9. Save refresh token to PostgreSQL
   - token: "eyJhbGciOiJIUzUxMiJ9..."
   - userId: 1
   - expiresAt: 2024-01-22T10:30:00
   ↓
10. Return AuthResponse (201 CREATED)
```

**Response** (201 CREATED):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDVVNUT01FUiIsImp0aSI6ImFiY2QtMTIzNC1lZmdoLTU2NzgiLCJpYXQiOjE3MDUzMTc2MDAsImV4cCI6MTcwNTQwNDAwMH0.signature",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwianRpIjoieHl6dy01Njc4LWFiY2QtMTIzNCIsImlhdCI6MTcwNTMxNzYwMCwiZXhwIjoxNzA1OTIyNDAwfQ.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 1,
  "email": "alice@example.com",
  "role": "CUSTOMER"
}
```

**Database State After Registration**:

user_credentials table:
```
| id | userId | email              | passwordHash      | role     | isActive | failedLoginAttempts | lockedUntil | createdAt           |
|----|--------|--------------------|-------------------|----------|----------|---------------------|-------------|---------------------|
| 1  | 1      | alice@example.com  | $2a$12$KIXxKj3...| CUSTOMER | true     | 0                   | null        | 2024-01-15 10:30:00 |
```

refresh_tokens table:
```
| id | token              | userId | expiresAt           | isRevoked | createdAt           |
|----|--------------------|--------|---------------------|-----------|---------------------|
| 1  | eyJhbGciOiJIUzUx...| 1      | 2024-01-22 10:30:00 | false     | 2024-01-15 10:30:00 |
```

---

### Scenario 2: User Login (Success)

**Use Case**: Registered user logs in with correct credentials.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'
```

**Processing Flow**:
```
1. AuthController.login() receives request
   ↓
2. Validation checks
   - Email format: ✓
   - Password not blank: ✓
   ↓
3. AuthenticationService.login() called
   ↓
4. Find user by email
   - Query: SELECT * FROM user_credentials WHERE email = 'alice@example.com'
   - Found: UserCredential(id=1, email=alice@example.com, ...)
   ↓
5. Check if account is locked
   - lockedUntil: null
   - isAccountLocked(): false ✓
   ↓
6. Check if account is active
   - isActive: true ✓
   ↓
7. Verify password
   - Input: "SecurePass123"
   - Stored hash: "$2a$12$KIXxKj3..."
   - BCrypt.matches(): true ✓
   ↓
8. Reset failed attempts
   - failedLoginAttempts: 0
   - lockedUntil: null
   - lastLoginAt: 2024-01-15T11:00:00
   ↓
9. Generate new tokens
   - New access token (fresh 24-hour expiration)
   - New refresh token (fresh 7-day expiration)
   ↓
10. Save new refresh token
   ↓
11. Return AuthResponse (200 OK)
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDVVNUT01FUiIsImp0aSI6Im5ld3Rva2VuLTEyMzQiLCJpYXQiOjE3MDUzMTg4MDAsImV4cCI6MTcwNTQwNTIwMH0.signature",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwianRpIjoibmV3cmVmcmVzaC01Njc4IiwiaWF0IjoxNzA1MzE4ODAwLCJleHAiOjE3MDU5MjM2MDB9.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 1,
  "email": "alice@example.com",
  "role": "CUSTOMER"
}
```

---

### Scenario 3: Failed Login (Wrong Password)

**Use Case**: User enters incorrect password.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "WrongPassword"
  }'
```

**Processing Flow**:
```
1. Find user by email: ✓ Found
   ↓
2. Check if locked: ✓ Not locked
   ↓
3. Check if active: ✓ Active
   ↓
4. Verify password
   - Input: "WrongPassword"
   - Stored hash: "$2a$12$KIXxKj3..."
   - BCrypt.matches(): false ✗
   ↓
5. handleFailedLogin() called
   - Increment failedLoginAttempts: 0 → 1
   - Save to database
   ↓
6. Throw AuthenticationException
   ↓
7. GlobalExceptionHandler catches exception
   ↓
8. Return error response (401 UNAUTHORIZED)
```

**Response** (401 UNAUTHORIZED):
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T11:05:00",
  "path": "/api/auth/login"
}
```

**Database State**:
```
failedLoginAttempts: 1
lockedUntil: null
```

---

### Scenario 4: Account Lockout (5 Failed Attempts)

**Use Case**: User fails login 5 times, account gets locked.

**Attempt 1-4**: Same as Scenario 3
- failedLoginAttempts increments: 1, 2, 3, 4

**Attempt 5**:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "WrongPassword"
  }'
```

**Processing Flow**:
```
1. Password verification fails
   ↓
2. handleFailedLogin() called
   - failedLoginAttempts: 4 → 5
   - Check if attempts >= 5: true
   - Set lockedUntil: LocalDateTime.now().plusMinutes(30)
   - lockedUntil: 2024-01-15T11:35:00
   - Save to database
   ↓
3. Throw AuthenticationException
```

**Response** (401 UNAUTHORIZED):
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T11:05:00",
  "path": "/api/auth/login"
}
```

**Attempt 6** (within 30 minutes):
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'
```

**Processing Flow**:
```
1. Find user by email: ✓
   ↓
2. Check if account is locked
   - lockedUntil: 2024-01-15T11:35:00
   - Current time: 2024-01-15T11:10:00
   - isAccountLocked(): true ✗
   ↓
3. Throw AccountLockedException
```

**Response** (403 FORBIDDEN):
```json
{
  "errorCode": "ACCOUNT_LOCKED",
  "message": "Account is locked until 2024-01-15T11:35:00",
  "timestamp": "2024-01-15T11:10:00",
  "path": "/api/auth/login"
}
```

---

### Scenario 5: User Logout

**Use Case**: User logs out, invalidating their access token.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDVVNUT01FUiIsImp0aSI6Im5ld3Rva2VuLTEyMzQiLCJpYXQiOjE3MDUzMTg4MDAsImV4cCI6MTcwNTQwNTIwMH0.signature"
```

**Processing Flow**:
```
1. AuthController.logout() receives request
   ↓
2. Extract token from Authorization header
   - Remove "Bearer " prefix
   - Token: "eyJhbGciOiJIUzUxMiJ9..."
   ↓
3. AuthenticationService.logout() called
   ↓
4. Extract JTI (JWT ID) from token
   - Parse JWT claims
   - jti: "newtoken-1234"
   ↓
5. Calculate remaining expiration time
   - exp: 1705405200 (2024-01-16T11:00:00)
   - now: 1705318800 (2024-01-15T11:00:00)
   - remaining: 86400 seconds (24 hours)
   ↓
6. Add to Redis blacklist with TTL
   - Key: "blacklist:newtoken-1234"
   - Value: "revoked"
   - TTL: 86400 seconds
   ↓
7. Return success message (200 OK)
```

**Response** (200 OK):
```json
{
  "message": "Logged out successfully"
}
```

**Redis State**:
```
Key: "blacklist:newtoken-1234"
Value: "revoked"
TTL: 86400 seconds
```

**Subsequent Request with Blacklisted Token**:
```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDVVNUT01FUiIsImp0aSI6Im5ld3Rva2VuLTEyMzQiLCJpYXQiOjE3MDUzMTg4MDAsImV4cCI6MTcwNTQwNTIwMH0.signature"
```

**API Gateway Validation**:
```
1. API Gateway intercepts request
   ↓
2. Extract JTI from token: "newtoken-1234"
   ↓
3. Call Auth Service validation endpoint
   - GET http://auth-service:8081/api/auth/validate?jti=newtoken-1234
   ↓
4. Auth Service checks Redis
   - redisTemplate.hasKey("blacklist:newtoken-1234"): true
   ↓
5. Return validation result
   - {"valid": false}
   ↓
6. API Gateway rejects request (401 UNAUTHORIZED)
```

---

### Scenario 6: Refresh Access Token

**Use Case**: Access token expired, user refreshes it using refresh token.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwianRpIjoibmV3cmVmcmVzaC01Njc4IiwiaWF0IjoxNzA1MzE4ODAwLCJleHAiOjE3MDU5MjM2MDB9.signature"
  }'
```

**Processing Flow**:
```
1. AuthController.refreshToken() receives request
   ↓
2. Extract refresh token from request body
   ↓
3. AuthenticationService.refreshToken() called
   ↓
4. Validate token signature and expiration
   - Parse JWT with signing key
   - Check expiration: exp > now
   - Result: Valid ✓
   ↓
5. Find token in database
   - Query: SELECT * FROM refresh_tokens WHERE token = '...'
   - Found: RefreshToken(id=2, userId=1, ...)
   ↓
6. Check if revoked or expired
   - isRevoked: false ✓
   - isExpired(): false ✓
   ↓
7. Extract userId from token
   - Parse claims
   - sub: "1"
   ↓
8. Find user credential
   - Query: SELECT * FROM user_credentials WHERE userId = 1
   - Found: UserCredential(id=1, email=alice@example.com, ...)
   ↓
9. Generate new access token
   - New JTI: "refreshed-token-9999"
   - New expiration: 24 hours from now
   ↓
10. Return AuthResponse (200 OK)
    - Same refresh token (not rotated)
    - New access token
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDVVNUT01FUiIsImp0aSI6InJlZnJlc2hlZC10b2tlbi05OTk5IiwiaWF0IjoxNzA1NDA1MjAwLCJleHAiOjE3MDU0OTE2MDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwianRpIjoibmV3cmVmcmVzaC01Njc4IiwiaWF0IjoxNzA1MzE4ODAwLCJleHAiOjE3MDU5MjM2MDB9.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 1,
  "email": "alice@example.com",
  "role": "CUSTOMER"
}
```

---

### Scenario 7: Validation Error (Weak Password)

**Use Case**: User tries to register with weak password.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "weak",
    "firstName": "Bob",
    "lastName": "Smith"
  }'
```

**Processing Flow**:
```
1. Request arrives at AuthController.register()
   ↓
2. @Valid annotation triggers validation
   ↓
3. Password validation fails
   - @Size(min = 8): "weak" has 4 characters ✗
   - @Pattern (uppercase, lowercase, digit): No uppercase or digit ✗
   ↓
4. Throw MethodArgumentNotValidException
   ↓
5. GlobalExceptionHandler catches exception
   ↓
6. Extract field errors
   - Field: "password"
   - Messages: ["Password must be at least 8 characters", 
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit"]
   ↓
7. Return error response (400 BAD REQUEST)
```

**Response** (400 BAD REQUEST):
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/auth/register",
  "details": {
    "password": "Password must be at least 8 characters"
  }
}
```

---

### Scenario 8: Duplicate Email Registration

**Use Case**: User tries to register with existing email.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123",
    "firstName": "Alice",
    "lastName": "Duplicate"
  }'
```

**Processing Flow**:
```
1. Validation passes
   ↓
2. AuthenticationService.register() called
   ↓
3. Check if email exists
   - Query: SELECT COUNT(*) FROM user_credentials WHERE email = 'alice@example.com'
   - Result: 1 (exists)
   ↓
4. Throw UserAlreadyExistsException
   ↓
5. GlobalExceptionHandler catches exception
   ↓
6. Return error response (409 CONFLICT)
```

**Response** (409 CONFLICT):
```json
{
  "errorCode": "USER_ALREADY_EXISTS",
  "message": "User with email alice@example.com already exists",
  "timestamp": "2024-01-15T12:05:00",
  "path": "/api/auth/register"
}
```

---

### Scenario 9: Token Validation (Internal Use)

**Use Case**: API Gateway validates token before forwarding request.

**Request**:
```bash
curl -X GET "http://localhost:8081/api/auth/validate?jti=newtoken-1234"
```

**Processing Flow**:
```
1. AuthController.validateToken() receives request
   ↓
2. Extract JTI from query parameter
   - jti: "newtoken-1234"
   ↓
3. AuthenticationService.isTokenBlacklisted() called
   ↓
4. Check Redis for blacklist entry
   - Key: "blacklist:newtoken-1234"
   - redisTemplate.hasKey(): true (token is blacklisted)
   ↓
5. Return validation result
   - valid: false (because blacklisted)
```

**Response** (200 OK):
```json
{
  "valid": false
}
```

**If Token Not Blacklisted**:
```json
{
  "valid": true
}
```

---

### Scenario 10: Restaurant Admin Registration

**Use Case**: Restaurant owner creates admin account.

**Request**:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "restaurant@example.com",
    "password": "AdminPass123",
    "firstName": "Restaurant",
    "lastName": "Owner",
    "phoneNumber": "+14155559999",
    "role": "RESTAURANT_ADMIN"
  }'
```

**Processing Flow**: Same as Scenario 1, but with different role.

**Response** (201 CREATED):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiZW1haWwiOiJyZXN0YXVyYW50QGV4YW1wbGUuY29tIiwicm9sZSI6IlJFU1RBVVJBTlRfQURNSU4iLCJqdGkiOiJhZG1pbi10b2tlbi0xMjM0IiwiaWF0IjoxNzA1MzIwMDAwLCJleHAiOjE3MDU0MDY0MDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwianRpIjoiYWRtaW4tcmVmcmVzaC01Njc4IiwiaWF0IjoxNzA1MzIwMDAwLCJleHAiOjE3MDU5MjQ4MDB9.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 2,
  "email": "restaurant@example.com",
  "role": "RESTAURANT_ADMIN"
}
```

**JWT Payload Difference**:
```json
{
  "sub": "2",
  "email": "restaurant@example.com",
  "role": "RESTAURANT_ADMIN",  // Different role
  "jti": "admin-token-1234",
  "iat": 1705320000,
  "exp": 1705406400
}
```

**Role-Based Access Control**:
- API Gateway extracts `role` claim from JWT
- Routes requests to appropriate services based on role
- RESTAURANT_ADMIN can access restaurant management endpoints
- CUSTOMER can only access customer-facing endpoints

---

## 5. Request/Response Flow

### Complete Registration Flow Diagram

```
┌─────────┐                                                                      
│ Client  │                                                                      
└────┬────┘                                                                      
     │                                                                           
     │ POST /api/auth/register                                                  
     │ {email, password, firstName, lastName}                                   
     ▼                                                                           
┌─────────────────────────────────────────────────────────────────────────┐    
│                         Auth Service (Port 8081)                        │    
│                                                                         │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 1. AuthController.register()                                     │  │    
│  │    - Receives HTTP request                                       │  │    
│  │    - @Valid triggers validation                                  │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 2. Bean Validation                                               │  │    
│  │    - Email format check                                          │  │    
│  │    - Password strength (min 8, uppercase, lowercase, digit)      │  │    
│  │    - Phone number format (E.164)                                 │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 3. AuthenticationService.register()                              │  │    
│  │    - Business logic layer                                        │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 4. Check Email Uniqueness                                        │  │    
│  │    - credentialRepository.existsByEmail()                        │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            │ SELECT COUNT(*) FROM user_credentials            
                            │ WHERE email = ?                                  
                            ▼                                                  
                    ┌───────────────┐                                          
                    │  PostgreSQL   │                                          
                    │  (auth_db)    │                                          
                    └───────┬───────┘                                          
                            │                                                  
                            │ Result: 0 (not exists)                           
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│                           │                                             │    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 5. Hash Password                                                 │  │    
│  │    - passwordEncoder.encode("SecurePass123")                     │  │    
│  │    - BCrypt with cost factor 12                                  │  │    
│  │    - Output: "$2a$12$KIXxKj3..."                                 │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 6. Save UserCredential                                           │  │    
│  │    - credentialRepository.save()                                 │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            │ INSERT INTO user_credentials                     
                            ▼                                                  
                    ┌───────────────┐                                          
                    │  PostgreSQL   │                                          
                    └───────┬───────┘                                          
                            │                                                  
                            │ Returns: id=1                                    
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 7. Update userId                                                 │  │    
│  │    - credential.setUserId(credential.getId())                    │  │    
│  │    - credentialRepository.save()                                 │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 8. Create User Profile (Async)                                   │  │    
│  │    - POST http://user-service/api/users                          │  │    
│  │    - {id, email, firstName, lastName, phoneNumber}               │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 9. Generate JWT Tokens                                           │  │    
│  │    - jwtTokenProvider.generateAccessToken()                      │  │    
│  │    - jwtTokenProvider.generateRefreshToken()                     │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 10. Save Refresh Token                                           │  │    
│  │     - refreshTokenRepository.save()                              │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            │ INSERT INTO refresh_tokens                       
                            ▼                                                  
                    ┌───────────────┐                                          
                    │  PostgreSQL   │                                          
                    └───────┬───────┘                                          
                            │                                                  
                            │ Success                                          
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 11. Build AuthResponse                                           │  │    
│  │     - accessToken, refreshToken, expiresIn, userId, email, role  │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 12. Return Response                                              │  │    
│  │     - HTTP 201 CREATED                                           │  │    
│  │     - JSON body with tokens                                      │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            │ 201 CREATED                                      
                            │ {accessToken, refreshToken, ...}                 
                            ▼                                                  
                    ┌───────────────┐                                          
                    │    Client     │                                          
                    │ Stores tokens │                                          
                    └───────────────┘                                          
```

### Complete Login Flow Diagram

```
┌─────────┐                                                                      
│ Client  │                                                                      
└────┬────┘                                                                      
     │                                                                           
     │ POST /api/auth/login                                                     
     │ {email, password}                                                        
     ▼                                                                           
┌─────────────────────────────────────────────────────────────────────────┐    
│                         Auth Service                                    │    
│                                                                         │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 1. Find User by Email                                            │  │    
│  │    - credentialRepository.findByEmail()                          │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │ SELECT * FROM user_credentials                   
                            │ WHERE email = ?                                  
                            ▼                                                  
                    ┌───────────────┐                                          
                    │  PostgreSQL   │                                          
                    └───────┬───────┘                                          
                            │ Returns UserCredential                           
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 2. Security Checks                                               │  │    
│  │    ┌─────────────────────────────────────────────────────────┐   │  │    
│  │    │ a. Check if account locked                              │   │  │    
│  │    │    - lockedUntil != null && lockedUntil > now?          │   │  │    
│  │    │    - If locked → throw AccountLockedException (403)     │   │  │    
│  │    └─────────────────────────────────────────────────────────┘   │  │    
│  │    ┌─────────────────────────────────────────────────────────┐   │  │    
│  │    │ b. Check if account active                              │   │  │    
│  │    │    - isActive == true?                                  │   │  │    
│  │    │    - If not → throw AuthenticationException (401)       │   │  │    
│  │    └─────────────────────────────────────────────────────────┘   │  │    
│  │    ┌─────────────────────────────────────────────────────────┐   │  │    
│  │    │ c. Verify password                                      │   │  │    
│  │    │    - passwordEncoder.matches(input, storedHash)         │   │  │    
│  │    │    - BCrypt comparison (constant time)                  │   │  │    
│  │    │    - If mismatch → handleFailedLogin() + throw (401)    │   │  │    
│  │    └─────────────────────────────────────────────────────────┘   │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │ All checks passed                           │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 3. Update Login Metadata                                         │  │    
│  │    - failedLoginAttempts = 0                                     │  │    
│  │    - lockedUntil = null                                          │  │    
│  │    - lastLoginAt = now                                           │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │ UPDATE user_credentials                          
                            ▼                                                  
                    ┌───────────────┐                                          
                    │  PostgreSQL   │                                          
                    └───────┬───────┘                                          
                            │                                                  
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 4. Generate New Tokens                                           │  │    
│  │    - Access token (24-hour expiration)                           │  │    
│  │    - Refresh token (7-day expiration)                            │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 5. Save Refresh Token                                            │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │ INSERT INTO refresh_tokens                       
                            ▼                                                  
                    ┌───────────────┐                                          
                    │  PostgreSQL   │                                          
                    └───────┬───────┘                                          
                            │                                                  
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 6. Return AuthResponse (200 OK)                                  │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            │ {accessToken, refreshToken, ...}                 
                            ▼                                                  
                    ┌───────────────┐                                          
                    │    Client     │                                          
                    └───────────────┘                                          
```

### Logout Flow with Token Blacklisting

```
┌─────────┐                                                                      
│ Client  │                                                                      
└────┬────┘                                                                      
     │                                                                           
     │ POST /api/auth/logout                                                    
     │ Authorization: Bearer <token>                                            
     ▼                                                                           
┌─────────────────────────────────────────────────────────────────────────┐    
│                         Auth Service                                    │    
│                                                                         │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 1. Extract Token from Header                                     │  │    
│  │    - Remove "Bearer " prefix                                     │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 2. Parse JWT and Extract JTI                                     │  │    
│  │    - jwtTokenProvider.getTokenId(token)                          │  │    
│  │    - JTI: "abc-123-def-456"                                      │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
│                           ▼                                             │    
│  ┌──────────────────────────────────────────────────────────────────┐  │    
│  │ 3. Calculate Remaining TTL                                       │  │    
│  │    - exp - now = 86400 seconds                                   │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            │ SET blacklist:abc-123-def-456 "revoked"         
                            │ EX 86400                                         
                            ▼                                                  
                    ┌───────────────┐                                          
                    │     Redis     │                                          
                    │  (Blacklist)  │                                          
                    └───────┬───────┘                                          
                            │                                                  
                            │ OK                                               
                            ▼                                                  
┌───────────────────────────┼─────────────────────────────────────────────┐    
│  ┌────────────────────────▼─────────────────────────────────────────┐  │    
│  │ 4. Return Success (200 OK)                                       │  │    
│  │    - {"message": "Logged out successfully"}                      │  │    
│  └────────────────────────┬─────────────────────────────────────────┘  │    
│                           │                                             │    
└───────────────────────────┼─────────────────────────────────────────────┘    
                            │                                                  
                            ▼                                                  
                    ┌───────────────┐                                          
                    │    Client     │                                          
                    │ Deletes tokens│                                          
                    └───────────────┘                                          
```

---

## 6. Database Schema

### PostgreSQL Database: auth_db

#### Table: user_credentials

```sql
CREATE TABLE user_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_credentials_email ON user_credentials(email);
CREATE INDEX idx_user_credentials_user_id ON user_credentials(user_id);
CREATE INDEX idx_user_credentials_locked_until ON user_credentials(locked_until);
```

**Column Descriptions**:

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | BIGSERIAL | No | Primary key, auto-increment |
| user_id | BIGINT | No | References User Service user ID (same as id) |
| email | VARCHAR(255) | No | Unique email address for login |
| password_hash | VARCHAR(255) | No | BCrypt hash of password (60 chars) |
| role | VARCHAR(50) | No | User role (CUSTOMER, RESTAURANT_ADMIN, etc.) |
| is_active | BOOLEAN | No | Account enabled/disabled flag |
| is_email_verified | BOOLEAN | No | Email verification status |
| last_login_at | TIMESTAMP | Yes | Last successful login timestamp |
| password_changed_at | TIMESTAMP | Yes | Last password change timestamp |
| failed_login_attempts | INTEGER | No | Counter for brute force protection |
| locked_until | TIMESTAMP | Yes | Account unlock timestamp (null if not locked) |
| created_at | TIMESTAMP | No | Account creation timestamp |
| updated_at | TIMESTAMP | No | Last update timestamp |

**Sample Data**:
```sql
INSERT INTO user_credentials VALUES
(1, 1, 'alice@example.com', '$2a$12$KIXxKj3...', 'CUSTOMER', true, false, 
 '2024-01-15 11:00:00', '2024-01-15 10:30:00', 0, null, 
 '2024-01-15 10:30:00', '2024-01-15 11:00:00'),
(2, 2, 'restaurant@example.com', '$2a$12$ABC123...', 'RESTAURANT_ADMIN', true, true,
 '2024-01-15 12:00:00', '2024-01-15 11:00:00', 0, null,
 '2024-01-15 11:00:00', '2024-01-15 12:00:00'),
(3, 3, 'locked@example.com', '$2a$12$XYZ789...', 'CUSTOMER', true, false,
 '2024-01-15 09:00:00', '2024-01-15 08:00:00', 5, '2024-01-15 14:30:00',
 '2024-01-15 08:00:00', '2024-01-15 14:00:00');
```

**Query Examples**:

Find user by email:
```sql
SELECT * FROM user_credentials WHERE email = 'alice@example.com';
```

Check if account is locked:
```sql
SELECT id, email, locked_until,
       CASE WHEN locked_until IS NOT NULL AND locked_until > NOW() 
            THEN true ELSE false END AS is_locked
FROM user_credentials
WHERE email = 'alice@example.com';
```

Find all locked accounts:
```sql
SELECT id, email, locked_until, failed_login_attempts
FROM user_credentials
WHERE locked_until IS NOT NULL AND locked_until > NOW();
```

Reset failed login attempts:
```sql
UPDATE user_credentials
SET failed_login_attempts = 0,
    locked_until = NULL,
    last_login_at = NOW(),
    updated_at = NOW()
WHERE email = 'alice@example.com';
```

---

#### Table: refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
```

**Column Descriptions**:

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | BIGSERIAL | No | Primary key, auto-increment |
| token | VARCHAR(500) | No | Unique refresh token (JWT string) |
| user_id | BIGINT | No | References user_credentials.user_id |
| expires_at | TIMESTAMP | No | Token expiration timestamp |
| created_at | TIMESTAMP | No | Token creation timestamp |
| is_revoked | BOOLEAN | No | Revocation flag (for logout all devices) |

**Sample Data**:
```sql
INSERT INTO refresh_tokens VALUES
(1, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwianRpIjoibmV3cmVmcmVzaC01Njc4IiwiaWF0IjoxNzA1MzE4ODAwLCJleHAiOjE3MDU5MjM2MDB9.signature',
 1, '2024-01-22 11:00:00', '2024-01-15 11:00:00', false),
(2, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwianRpIjoiYWRtaW4tcmVmcmVzaC01Njc4IiwiaWF0IjoxNzA1MzIwMDAwLCJleHAiOjE3MDU5MjQ4MDB9.signature',
 2, '2024-01-22 12:00:00', '2024-01-15 12:00:00', false),
(3, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwianRpIjoib2xkdG9rZW4tMTIzNCIsImlhdCI6MTcwNTIzMjQwMCwiZXhwIjoxNzA1ODM3MjAwfQ.signature',
 1, '2024-01-21 11:00:00', '2024-01-14 11:00:00', true);
```

**Query Examples**:

Find refresh token:
```sql
SELECT * FROM refresh_tokens 
WHERE token = 'eyJhbGciOiJIUzUxMiJ9...' 
  AND is_revoked = false 
  AND expires_at > NOW();
```

Revoke all tokens for a user (logout all devices):
```sql
UPDATE refresh_tokens
SET is_revoked = true
WHERE user_id = 1;
```

Delete expired tokens (cleanup job):
```sql
DELETE FROM refresh_tokens
WHERE expires_at < NOW();
```

Count active tokens per user:
```sql
SELECT user_id, COUNT(*) as active_tokens
FROM refresh_tokens
WHERE is_revoked = false AND expires_at > NOW()
GROUP BY user_id;
```

---

### Redis Data Structure

**Purpose**: Token blacklist for logout functionality

**Key Pattern**: `blacklist:{jti}`

**Data Type**: String

**TTL**: Remaining token expiration time (in seconds)

**Example Entries**:
```
Key: "blacklist:abc-123-def-456"
Value: "revoked"
TTL: 86400 seconds (24 hours)

Key: "blacklist:xyz-789-uvw-012"
Value: "revoked"
TTL: 43200 seconds (12 hours)
```

**Redis Commands**:

Check if token is blacklisted:
```bash
EXISTS blacklist:abc-123-def-456
# Returns: 1 (exists) or 0 (not exists)
```

Add token to blacklist:
```bash
SET blacklist:abc-123-def-456 "revoked" EX 86400
# EX 86400 = expires in 86400 seconds (24 hours)
```

Get TTL of blacklisted token:
```bash
TTL blacklist:abc-123-def-456
# Returns: remaining seconds or -2 (not exists)
```

List all blacklisted tokens (for debugging):
```bash
KEYS blacklist:*
# Returns: ["blacklist:abc-123-def-456", "blacklist:xyz-789-uvw-012"]
```

**Why Redis for Blacklist?**
- **Fast lookups**: O(1) time complexity, < 1ms latency
- **Automatic expiration**: TTL automatically removes expired entries
- **Memory efficient**: Only stores active blacklisted tokens
- **Scalable**: Can use Redis Cluster for horizontal scaling
- **No cleanup needed**: Expired keys automatically deleted

---

### Entity Relationships

```
┌─────────────────────────┐
│   user_credentials      │
│─────────────────────────│
│ id (PK)                 │
│ user_id                 │◄────┐
│ email (UNIQUE)          │     │
│ password_hash           │     │
│ role                    │     │
│ is_active               │     │
│ failed_login_attempts   │     │
│ locked_until            │     │
│ ...                     │     │
└─────────────────────────┘     │
                                │
                                │ 1:N
                                │
                                │
┌─────────────────────────┐     │
│   refresh_tokens        │     │
│─────────────────────────│     │
│ id (PK)                 │     │
│ token (UNIQUE)          │     │
│ user_id (FK)            │─────┘
│ expires_at              │
│ is_revoked              │
│ ...                     │
└─────────────────────────┘
```

**Relationship**: One user can have multiple refresh tokens (multiple devices/sessions)

---

## 7. Integration Points

### 7.1 Service Registry (Eureka)

**Purpose**: Service discovery and registration

**Configuration**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    hostname: localhost
    prefer-ip-address: true
```

**Registration Flow**:
```
1. Auth Service starts
   ↓
2. Connects to Eureka Server (localhost:8761)
   ↓
3. Registers with service name: "auth-service"
   ↓
4. Sends heartbeat every 30 seconds
   ↓
5. Other services discover Auth Service via Eureka
```

**Service Discovery**:
- API Gateway discovers Auth Service for token validation
- User Service discovers Auth Service for credential verification

---

### 7.2 Config Server

**Purpose**: Centralized configuration management

**Configuration**:
```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
        multiplier: 1.1
```

**Fetched Configuration**:
- Database connection details
- JWT secret key (encrypted)
- Redis connection details
- Token expiration times
- Security settings

**Configuration Refresh**:
```bash
# Refresh configuration without restart
curl -X POST http://localhost:8081/actuator/refresh
```

---

### 7.3 API Gateway Integration

**Purpose**: Token validation for incoming requests

**Flow**:
```
1. Client sends request to API Gateway
   - Authorization: Bearer <token>
   ↓
2. API Gateway extracts JWT
   ↓
3. API Gateway validates JWT signature
   ↓
4. API Gateway extracts JTI from token
   ↓
5. API Gateway calls Auth Service
   - GET http://auth-service:8081/api/auth/validate?jti=<jti>
   ↓
6. Auth Service checks Redis blacklist
   - Returns: {"valid": true/false}
   ↓
7. If valid, API Gateway forwards request
   - Adds user context headers (userId, email, role)
   ↓
8. If invalid, API Gateway rejects request (401)
```

**Headers Added by API Gateway**:
```
X-User-Id: 123
X-User-Email: alice@example.com
X-User-Role: CUSTOMER
```

---

### 7.4 User Service Integration

**Purpose**: Create user profile after registration

**Flow**:
```
1. Auth Service completes registration
   ↓
2. Auth Service calls User Service
   - POST http://user-service:8083/api/users
   - Body: {id, email, firstName, lastName, phoneNumber}
   ↓
3. User Service creates user profile
   ↓
4. User Service returns success
```

**Request Example**:
```json
POST http://user-service:8083/api/users
Content-Type: application/json

{
  "id": 123,
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Johnson",
  "phoneNumber": "+14155551234",
  "role": "CUSTOMER"
}
```

**Error Handling**:
- If User Service is down, registration still succeeds
- User profile creation can be retried later
- Consider using message queue (RabbitMQ/Kafka) for reliability

---

### 7.5 Event Publishing (Future Enhancement)

**Events to Publish**:

1. **UserRegisteredEvent**
   ```json
   {
     "eventType": "USER_REGISTERED",
     "userId": 123,
     "email": "alice@example.com",
     "role": "CUSTOMER",
     "timestamp": "2024-01-15T10:30:00Z"
   }
   ```

2. **UserLoggedInEvent**
   ```json
   {
     "eventType": "USER_LOGGED_IN",
     "userId": 123,
     "email": "alice@example.com",
     "ipAddress": "192.168.1.100",
     "timestamp": "2024-01-15T11:00:00Z"
   }
   ```

3. **AccountLockedEvent**
   ```json
   {
     "eventType": "ACCOUNT_LOCKED",
     "userId": 123,
     "email": "alice@example.com",
     "reason": "Too many failed login attempts",
     "lockedUntil": "2024-01-15T14:30:00Z",
     "timestamp": "2024-01-15T14:00:00Z"
   }
   ```

**Consumers**:
- Notification Service: Send email/SMS alerts
- Analytics Service: Track user behavior
- Audit Service: Security audit logs

---

## 8. Error Handling

### Error Response Format

All errors follow a standardized format:

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/endpoint",
  "details": {}
}
```

### Error Scenarios

#### 8.1 Authentication Errors (401 UNAUTHORIZED)

**Scenario 1: Invalid Credentials**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@example.com", "password": "WrongPassword"}'
```

Response:
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T11:05:00",
  "path": "/api/auth/login"
}
```

**Scenario 2: User Not Found**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "nonexistent@example.com", "password": "Password123"}'
```

Response:
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T11:05:00",
  "path": "/api/auth/login"
}
```

**Note**: Same error message for both scenarios (security best practice - don't reveal if email exists)

**Scenario 3: Invalid Refresh Token**
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "invalid.token.here"}'
```

Response:
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Invalid refresh token",
  "timestamp": "2024-01-15T11:10:00",
  "path": "/api/auth/refresh"
}
```

**Scenario 4: Expired Refresh Token**
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzUxMiJ9.expired.token"}'
```

Response:
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Refresh token is invalid or expired",
  "timestamp": "2024-01-15T11:10:00",
  "path": "/api/auth/refresh"
}
```

**Scenario 5: Inactive Account**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "inactive@example.com", "password": "Password123"}'
```

Response:
```json
{
  "errorCode": "AUTHENTICATION_ERROR",
  "message": "Account is inactive",
  "timestamp": "2024-01-15T11:15:00",
  "path": "/api/auth/login"
}
```

---

#### 8.2 Account Locked (403 FORBIDDEN)

**Scenario: Too Many Failed Attempts**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "locked@example.com", "password": "Password123"}'
```

Response:
```json
{
  "errorCode": "ACCOUNT_LOCKED",
  "message": "Account is locked until 2024-01-15T14:30:00",
  "timestamp": "2024-01-15T14:00:00",
  "path": "/api/auth/login"
}
```

**Troubleshooting**:
1. Wait until `lockedUntil` timestamp
2. Or manually unlock in database:
   ```sql
   UPDATE user_credentials
   SET failed_login_attempts = 0, locked_until = NULL
   WHERE email = 'locked@example.com';
   ```

---

#### 8.3 Validation Errors (400 BAD REQUEST)

**Scenario 1: Missing Required Fields**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

Response:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/auth/register",
  "details": {
    "password": "Password is required",
    "firstName": "First name is required",
    "lastName": "Last name is required"
  }
}
```

**Scenario 2: Invalid Email Format**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "Password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

Response:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/auth/register",
  "details": {
    "email": "Invalid email format"
  }
}
```

**Scenario 3: Weak Password**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "weak",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

Response:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/auth/register",
  "details": {
    "password": "Password must be at least 8 characters"
  }
}
```

**Scenario 4: Invalid Phone Number**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "123"
  }'
```

Response:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/auth/register",
  "details": {
    "phoneNumber": "Invalid phone number format"
  }
}
```

---

#### 8.4 Conflict Errors (409 CONFLICT)

**Scenario: Duplicate Email**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "Password123",
    "firstName": "Alice",
    "lastName": "Duplicate"
  }'
```

Response:
```json
{
  "errorCode": "USER_ALREADY_EXISTS",
  "message": "User with email alice@example.com already exists",
  "timestamp": "2024-01-15T12:05:00",
  "path": "/api/auth/register"
}
```

**Troubleshooting**:
- User should use "Forgot Password" flow instead
- Or login with existing credentials

---

#### 8.5 Internal Server Errors (500 INTERNAL SERVER ERROR)

**Scenario 1: Database Connection Failure**

Response:
```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-15T12:10:00",
  "path": "/api/auth/login"
}
```

**Troubleshooting**:
1. Check PostgreSQL is running:
   ```bash
   psql -h localhost -U auth_admin -d auth_db
   ```
2. Check connection pool:
   ```bash
   curl http://localhost:8081/actuator/health
   ```
3. Check logs:
   ```bash
   tail -f logs/auth-service.log
   ```

**Scenario 2: Redis Connection Failure**

Response:
```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-15T12:10:00",
  "path": "/api/auth/logout"
}
```

**Troubleshooting**:
1. Check Redis is running:
   ```bash
   redis-cli ping
   # Should return: PONG
   ```
2. Check Redis connection:
   ```bash
   redis-cli -h localhost -p 6379
   ```

**Scenario 3: JWT Secret Not Configured**

Response:
```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-15T12:10:00",
  "path": "/api/auth/login"
}
```

**Troubleshooting**:
1. Check JWT secret is configured:
   ```yaml
   jwt:
     secret: your-secret-key-here  # Must be at least 512 bits (64 bytes)
   ```
2. Generate new secret:
   ```bash
   openssl rand -base64 64
   ```

---

### HTTP Status Code Summary

| Status Code | Error Code | Description | Example |
|-------------|-----------|-------------|---------|
| 400 | VALIDATION_ERROR | Request validation failed | Invalid email format |
| 401 | AUTHENTICATION_ERROR | Invalid credentials or token | Wrong password |
| 403 | ACCOUNT_LOCKED | Account is locked | Too many failed attempts |
| 409 | USER_ALREADY_EXISTS | Duplicate email | Email already registered |
| 500 | INTERNAL_SERVER_ERROR | Unexpected server error | Database connection failure |

---

### Error Logging

All errors are logged with appropriate levels:

**WARN Level** (Expected errors):
```
2024-01-15 11:05:00 WARN  AuthenticationService - Failed login attempt for email: alice@example.com
2024-01-15 14:00:00 WARN  AuthenticationService - Account locked for email: locked@example.com
```

**ERROR Level** (Unexpected errors):
```
2024-01-15 12:10:00 ERROR AuthenticationService - Database connection failed
org.postgresql.util.PSQLException: Connection refused
    at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(...)
    ...
```

**Log Analysis**:
```bash
# Count failed login attempts
grep "Failed login attempt" logs/auth-service.log | wc -l

# Find locked accounts
grep "Account locked" logs/auth-service.log

# Find database errors
grep "ERROR" logs/auth-service.log | grep "Database"
```

---

## 9. Configuration Reference

### 9.1 application.yml (Local Configuration)

```yaml
# Minimal local configuration
# Most configuration is fetched from Config Server

spring:
  cloud:
    config:
      enabled: true

# Local fallback values (used only if Config Server is unavailable)
server:
  port: 8081

logging:
  level:
    com.fooddelivery.auth: INFO
```

---

### 9.2 bootstrap.yml (Bootstrap Configuration)

```yaml
spring:
  application:
    name: auth-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
        multiplier: 1.1
  profiles:
    active: dev
```

**Configuration Explanation**:
- `name`: Service name for Eureka registration
- `config.uri`: Config Server URL
- `fail-fast`: Fail startup if Config Server is unavailable
- `retry.max-attempts`: Retry 6 times before failing
- `retry.initial-interval`: Wait 1 second before first retry
- `retry.multiplier`: Increase wait time by 1.1x each retry
- `profiles.active`: Active profile (dev, prod, test)

---

### 9.3 Config Server Configuration (auth-service.yml)

```yaml
spring:
  application:
    name: auth-service
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: auth_admin
    password: '{cipher}AQB...'  # Encrypted password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create tables (use Flyway/Liquibase)
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
  
  # Redis Configuration
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 2
        max-wait: -1ms

# Server Configuration
server:
  port: 8081
  servlet:
    context-path: /
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

# JWT Configuration
jwt:
  secret: '{cipher}AQB...'  # Encrypted JWT secret (min 512 bits)
  access-token-expiration-ms: 86400000  # 24 hours
  refresh-token-expiration-ms: 604800000  # 7 days

# Security Configuration
security:
  password:
    bcrypt-strength: 12
  account-lockout:
    max-failed-attempts: 5
    lockout-duration-minutes: 30

# Eureka Configuration
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    hostname: localhost
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    redis:
      enabled: true
    db:
      enabled: true

# Logging Configuration
logging:
  level:
    root: INFO
    com.fooddelivery.auth: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/auth-service.log
    max-size: 10MB
    max-history: 30
```

---

### 9.4 Environment Variables

Override configuration with environment variables:

```bash
# Database
export DB_URL=jdbc:postgresql://prod-db:5432/auth_db
export DB_USERNAME=auth_admin
export DB_PASSWORD=secure_password

# Redis
export REDIS_HOST=prod-redis
export REDIS_PORT=6379

# JWT
export JWT_SECRET=your-512-bit-secret-key-here
export JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000
export JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000

# Server
export SERVER_PORT=8081

# Eureka
export EUREKA_URL=http://eureka-server:8761/eureka/

# Config Server
export CONFIG_SERVER_URL=http://config-server:8888
```

---

### 9.5 Docker Configuration

**Dockerfile**:
```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/auth-service-*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  auth-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/auth_db
      - DB_USERNAME=auth_admin
      - DB_PASSWORD=password
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JWT_SECRET=your-secret-key
      - EUREKA_URL=http://eureka:8761/eureka/
      - CONFIG_SERVER_URL=http://config-server:8888
    depends_on:
      - postgres
      - redis
      - eureka
      - config-server
    networks:
      - food-delivery-network

  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=auth_db
      - POSTGRES_USER=auth_admin
      - POSTGRES_PASSWORD=password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - food-delivery-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - food-delivery-network

volumes:
  postgres-data:

networks:
  food-delivery-network:
    driver: bridge
```

---

### 9.6 Kubernetes Configuration

**k8s-deployment.yml**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-service-config
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres-service:5432/auth_db
      redis:
        host: redis-service
        port: 6379
    eureka:
      client:
        service-url:
          defaultZone: http://eureka-service:8761/eureka/

---
apiVersion: v1
kind: Secret
metadata:
  name: auth-service-secret
type: Opaque
stringData:
  db-password: secure_password
  jwt-secret: your-512-bit-secret-key

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: food-delivery/auth-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: auth-service-secret
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: auth-service-secret
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"

---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
spec:
  selector:
    app: auth-service
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
  type: ClusterIP
```

---

## 10. Testing Guide

### 10.1 Manual Testing with cURL

#### Test 1: Register New User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123",
    "firstName": "Test",
    "lastName": "User",
    "pho

--`

-n"
}
``ductio "proment":"environ  ce",
"auth-servi": 
  "serviceerId": 1,
  "usom",.cice@example": "al,
  "emailgistered"r resege": "U "messae",
 ionServiccatntirvice.Autheth.seelivery.au"com.fooddgger": ",
  "loel": "INFO "lev30:00Z",
 -15T10:-01": "2024estamp
  "@tim``json
{
`ack):** (ELK Stionregatog Agg
**Ld
```
ction faileonneabase cvice - DatSerationuthentic0:00 ERROR A12:101-15 0:00
2024-T14:3-15til=2024-01ockedUncom, lple.d@examemail=locke: locked Account onService -atienticN  AuthWAR0:00 1-15 14:0
2024-0s=1 attemptexample.com,ice@=alailttempt: emed login aice - FailticationServWARN  Authen:05:00 01-15 11d=1
2024-m, userIcople.alice@exammail=logged in: ee - User ationServicichentAutNFO  5 11:00:00 I
2024-01-1=1erIdusle.com, mpe@examail=alicered: eser registce - UnServienticatioFO  Auth0:00 IN1-15 10:3
2024-0:

```g Examples**
**Lo)
ase failuretabrrors (daed e Unexpect*ERROR**:n)
- *iolidatgin, vas (failed lod error Expecte*:**WARN*
- gistration)in, re (logeventsortant *INFO**: Imp- *nformation
led flow itai De- **DEBUG**::
Log Levels****ogging

.5 L

### 11
---ts/minute
ttemp afailed: > 100 - Alert
   Graphn: izatioal
   - Visuer)ent countempl(immetric  Custom - Query:  ts**
 emp Login Att **Failed
6.it: bytes

   - Un Graphlization:  - Visuaeap"}`
 es{area="hbytd_ory_usey: `jvm_mem
   - Querry**. **JVM Memo
5ool size)
x: 10 (p Mage
   -ion: Gauisualizat  - Vs_active`
 connectionp_ic: `hikarQuery- *
   s*ctione Conneas*Databate

4. *% error rAlert: > 1   - Graph
ualization: 
   - Vis"}[5m])`..tus=~"5s_count{sta_secondr_requestshttp_serveery: `rate( - Qu
  or Rate****Erronds

3.  Unit: secph
   -: Graisualization)`
   - Vbucket[5m])sts_seconds_uever_reqttp_ser(h.95, ratentile(0quagram_uery: `histo
   - QTime (p95)**ponse 

2. **Resoduri, methp by: ou
   - Grraphization: Gual - Vis5m])`
  t[s_coun_second_requestsrverhttp_seate( - Query: `rRate**
  t Reques*:

1. **nels*ard Pabo
**Dashoards
ashbfana D 11.4 Gra--

###}
```

-ea="heap"d_bytes{arsejvm_memory_uy usage
 memor
# JVM
1"}ikariPool-"H=active{poolections_nnp_coool
hikaricconnection pase Databm])

# }[55.."us=~"s_count{stats_secondr_requestttp_servee
rate(hor rat

# Errucket[5m]))_bondsts_secues_server_reqttp rate(h(0.95,tilequan
histogram_ (p95)onse time
# Respn"}[5m])
h/logiapi/autount{uri="/onds_cecests_squ_server_re
rate(httpratequest romql
# Re``p:
`Monitor**ics to 
**Key Metr`
8081']
``host:['localgets:  - tar:
     atic_configss'
    stometheuator/prctuath: '/atrics_pice'
    me 'auth-servname: job_nfigs:
  -l
scrape_co
```yam Config**:crapeetheus S`

**Prom
``ed: true enabl       heus:
      prometexport:
s:
    metrictheus
  ome include: pr
       osure:  exp
     web::
   ntsoi  endpent:
anagemml
mya*:
```tion*onfiguration

**Cheus Integra3 Promet
### 11.--
jq
```

-uests | .server.reqtrics/httpor/meat8081/actut:calhos http://lourlh
c``bas
`ic**:trecific Mesage

**Sp: CPU u.cpu.usage``systemetrics
- P request ms`: HTTuester.req.servhttpections
- `se connctive databaive`: Actions.act.connearicp`hikeads
- hr Active tads.live`:.threage
- `jvmM memory usd`: JVmemory.use:
- `jvm.cs**etri
**Key M```
q
s | jtor/metrict:8081/actua://localhosl http```bash
cur

Metrics# 
```

###
}
  }  }}
  
       "7.0.0"n":rsio  "ve  
    ls": {tai     "deUP",
 "":     "status{
  edis": "r
       }, "UP"
 atus":st  ": {
      "ping"    },
  0
      }
8576104shold":       "thre000,
   250000000free":    "0,
    50000000000tal": "to
        ils": {    "deta"UP",
  atus":       "st: {
ce""diskSpa       },
   }
 d()"
   : "isVali"tionQuerylida "va
       ",PostgreSQL"": ase     "datab   ls": {
    "detai"UP",
  atus":   "st
     "db": { {
   omponents":
  "cUP",tus": ""sta
{
  jsonponse:
```
```

Resh | jq/healtoractuatalhost:8081/tp://lochtash
curl 

```b Checkealth### H

#sing Endpointitor11.2 Mon# ---

##)

ttuce pool-8 (Lens: 2ionects ConRedipool)
- CP kari 5-10 (Hiections:tabase Conn Daer load)
- (und cores), 2-41 core (idle: 0.5-
- CPUM heap)1GB (JV: 512MB-mory*:
- Meage**Resource Us
*ast)
ery fs is vdi00 req/s (Reon: 1000-20idatin Valst)
- Tokefas is s (Redireq/: 500-1000 
- LogoutBCrypt)d by eq/s (limite-120 rgin: 80Lo)
- ed by BCryptq/s (limit100 re: 50-gistration- Reput**:
ugh
**Thro
edis lookup)(R0-20ms ation: 1liden Vaon)
- TokJWT generati lookup + abase150ms (dat100-esh Token: te)
- Refr wriRedis 50-100ms (ogout:ion)
- Latpt verifics BCryclude50ms (in250-3: Login
-  hashing)es BCrypt(includ400-500ms ration: 
- Regists** (p95):nse Timeespo**Expected R Metrics

rformance## 11.1 Peing

# & Monitorerformance
## 11. Pnd

---
ests/seco0 requput: > 10)
- Through0ms (p95Logout: < 10
- 00ms (p95)n: < 3
- Logis (p95) < 500mtration::
- Regis**rmanceected Perfort

**Expry Repommaee, Suts TrResulw rs: Vie. Listenee 200/201
6onse codspons: Re5. Asserti
uth/logout/api/a POST TTP Request: Hn
4.logi/auth/t: POST /apies RequHTTPister
3. auth/regapi/t: POST /esTP Requds
2. HTecon-up 10 s, ramp100 usersGroup: . Thread an**:
1

**Test Plsing JMeter

#### UTest"}
```:"stName"oad","lame":"L"firstNa",Test123Loadd":"passworle.com","adtest@examplo""email":
# {ster.json:

# regiteruth/regis:8081/api/aalhostttp://loc
  hn/json \licatioappn -T ister.jso-p reg000 -c 10 n 1
ab -intndpotion etra regisash
# Test
```bab)
che Bench (ing Apa

#### Usng Testi0.5 Load
### 1
---

 }
}
```s());
   ist").exsswordpa"$.details.nPath(ect(jso.andExp              xists())
  s.email").eh("$.detailect(jsonPatExp.and                ))
ION_ERROR"IDATe("VAL).valuorCode""$.err(jsonPath(ndExpect        .a)
        uest()eq().isBadRect(status.andExp             
   est)))ing(requValueAsStr.writeperectMapobjcontent(   .       
      ION_JSON)CATe.APPLIyppe(MediaT  .contentTy          ter")
    regish//api/autpost("erform(Mvc.p      mock 
       );
  ord("weak"est.setPasswrequ      ");
  ild-ema"invalimail(setEt.    reques  quest();
  erUserRest new Regi request =rUserRequestiste
        Regption {throws Excerrors() onEatitestValid
    void Test  }

    @com"));
  xample.nint@eue("logialail").v$.emPath("xpect(json       .andE
         .exists())n")Toke("$.accessect(jsonPathdExp      .an        Ok())
  istatus().(spect  .andEx             uest)))
 ng(loginReqtririteValueAsS.wjectMapperent(ob      .cont   )
       ION_JSONLICATe.APPiaTypype(MedtentT.con              n")
  /logiapi/auth"/st(perform(poMvc.       mock 
        s123");
nIntPasd("Logiworest.setPassequinR
        logom");.cnint@example"logietEmail(equest.soginR        l
inRequest(); = new LognRequestoginRequest l  Logigin
             // Lo      
 
  ed());().isCreatect(statusandExp       .        est)))
 quRe(registeringAsStreValue.writapperectMtent(obj      .con     )
     ON_JSONAPPLICATIType.ype(Media   .contentT        r")
     auth/registepost("/api/m(forockMvc.per     m   
   ;
     tegration")stName("Inuest.setLaegisterReq r       Login");
tName("t.setFirsisterReques  reg);
      123"assLoginIntPssword("est.setPaerRequ   regist     e.com");
plnt@exam"loginiEmail(st.setegisterReque;
        ruest()rReqsterUse= new RegisterRequest egiquest rrUserReRegiste       
 r firstRegiste  //  {
      Exception) throws nt(ginEndpoitestLo void @Test
     }

  R"));
    ("CUSTOME").value$.roleath("nPxpect(jso  .andE           
   "))comple.tion@examintegra").value(""$.email(jsonPath(ndExpect .a         ))
      exists(Token").sh"$.refreath((jsonPndExpect         .a())
       stsToken").exissce"$.act(jsonPath(  .andExpec            eated())
  Crs().isect(statudExp         .an
       request)))lueAsString(r.writeVape(objectMap   .content          
   N)_JSOATIONPPLIC.AypeiaTedpe(M  .contentTy       ")
       egisterth/raust("/api/m(poerfor   mockMvc.p   
          est");
Name("Tastuest.setLeq    r");
    gration"IntetName(Firsest.set       requ
 );ass123"tionPd("IntegraassworsetP   request.     ;
")ple.comion@examat"integrl(st.setEmaique
        request();erRew RegisterUsequest = neuest rserReq  RegisterU {
      onws Exceptipoint() throerEndgisttRe    void tesest
  @Ter;

  tMappobjecpper ate ObjectMarivred
    p @Autowi  

 Mvc;c mockvate MockMv
    pried   @Autowir

 onTest {Integratitrollerss AuthConc
claigureMockMvonf
@AutoCT)ANDOM_POR.RbEnvironmentootTest.We SpringBronment =Envit(webTesota
@SpringBo```javts

 Tesntegration I10.4

### ---
`
 }
}
``
   });  );
      uestoginReqlogin(lService.thentication        au
    () -> {on.class, ckedExceptitLows(AccounertThroass        ss123");
tPaouword("Lockuest.setPasseqoginR
        lct passwordrreith co to login wTry/    /
           );
  ountLocked()isAccdential.(creertTrue     ass());
   ntilLockedUetential.gl(credtNotNul asser    ());
   AttemptstFailedLoginial.geedentEquals(5, crssert      aget();
  ).e.com"t@exampl"lockouyEmail(y.findBsitorialRepontal = credeentintial creddeUserCre
        ockedaccount is ly    // Verif      
   
       }    }
          d
   pecteEx    //          on e) {
   nExceptiatiothenticch (Au       } cat);
     inRequestogin(logice.lonServhenticati       aut     
       try {       ) {
  i++= 0; i < 5;  for (int i   
           
  rd");gPasswoword("Wronuest.setPassnReqlogi
        m");e.coout@exampll("lock.setEmaiquestinRe
        log();stoginRequew Luest = ne loginRequestginReq
        Login 5 times Fail lo
        //    uest);
    eqer(registerR.registonServiceticatithen      au;
  ")Name("Testt.setLastuesrReq registe
       ckout");Name("LosetFirstequest.sterR    regi3");
    Pass12ockoutord("Lswuest.setPasgisterReq re;
       .com")pleexam("lockout@mail.setEqueststerRe    regit();
    equesrRgisterUseRest = new gisterRequerRequest resterUse  Regi   er user
   isteg      // R {
  t()kouocccountLstA void te@Test
    }

    ts());
   ginAttemptFailedLoal.gentils(1, crede assertEqua    .get();
   .com")pass@exampleil("wrongy.findByEmapositorialRe= credental al credentiCredenti  Userd
      nteincremes  attemptrify failed     // Ve     
     });
         );
questloginRein(vice.logticationSer authen       
     () -> {n.class,eptioxccationEthentitThrows(Au      asser       
  ;
 3")ass12rd("WrongPsswoequest.setPaoginR     lom");
   s@example.cpasil("wronguest.setEmaloginReq;
        est()inRequ Logew = noginRequestRequest l     Loginssword
    wrong pagin with       // Lo
       
  equest);er(registerRvice.registionSerauthenticat       ");
 "PassastName(setLerRequest.egist;
        r"Wrong")irstName(tF.seequest  registerR
      ");ss123orrectPa("C.setPasswordterRequest regis");
       xample.comss@ewrongpa"st.setEmail(queregisterRe        );
st(equeterUserRRegisest = new quterRest regisqueterUserRegisRet
        er user firsgist/ Re     /rd() {
   woPassstLoginWrong
    void teest  @T }

  ail());
   getEmresponse.ple.com", amst@exls("logintertEqua     asse);
   cessToken().getAcull(responsetNotN asser     ;
  sponse)l(reertNotNulss
        a     st);
   uen(loginReqlogie.onServichenticatiautesponse = nse rAuthRespo              
23");
  "LoginPass1(Passwordsetest.oginRequ    lm");
    le.coampogintest@exail("lequest.setEmginR lo      uest();
 LoginRequest = new  loginRequestnReq  Logi       // Login
   
       est);
     quisterReister(regegonService.rtiticaauthen
        t");"TesastName(setLst.isterReque  reg);
      me("Login"irstNa.setFquestregisterRe       
 ass123");inPLogsword("uest.setPasegisterReq  r;
      ")xample.comntest@e("logietEmailequest.ssterRregi);
        rRequest(sterUse new RegirRequest =steuest regiterUserReqis  Regrst
      ster user figi  // Re {
      s()ccesLoginSuoid test  v  @Test
  }

    
     });t2);
     uesgister(reqService.rentication   authe
         ) -> {ion.class, (stsExceptreadyExis(UserAlassertThrow           
");
     User("tLastNamese   request2.   nd");
  "SecorstName(.setFi request2   ");
    herPass123d("Anotssworst2.setPaueeq      r
  mple.com");licate@exal("dup2.setEmairequest        ;
quest()UserRenew Registerest2 = uest requrUserReq     Registel
    same emaister withTry to regi        // 
        
);r(request1vice.registeionSercatauthenti        ");
"UserstName(1.setLaquest   re   
  ;t")irsirstName("Fest1.setF     requ23");
   ss1"SecurePassword(uest1.setPa    req);
    ample.com"licate@exil("duptEmast1.se     reque;
   equest()sterUserR = new Regiuest1reqUserRequest   Registerser
      er first uist   // Reg
     teEmail() {uplicatRegisterD  void tes @Test
    }

   $"));
  ("$2a$12).startsWithash(etPasswordHved.get().gtTrue(sa   asser   t());
  ensPresved.ie(sa  assertTru     .com");
 xampleewuser@e("nindByEmailitory.fntialReposd = credel> savetiaserCredenl<Una     Optio saved
   entialred // Verify c
               ole());
tRse.geER, responTOMole.CUSEquals(UserRrt      assel());
  maiesponse.getEm", rxample.cor@es("newuse assertEqual     ken());
  getRefreshToonse.(respNotNull assert);
       cessToken()sponse.getAcotNull(resertN
        as;onse)respsertNotNull(       as     
 t);
   r(reques.registeicervSetionnticanse = authesponse respo     AuthRe    
   ER);
    ole.CUSTOMUserRest.setRole(   requ  ");
   "UserstName(equest.setLa    r  ");
  me("NewtFirstNa  request.se     3");
 12ePassd("SecursetPassworquest. re      m");
 e.cowuser@exampletEmail("nerequest.s
        Request();isterUsernew Reg request = erRequestterUsegis
        Ress() {sterSuccstRegi void te  @Test
   ory;

  Repositentialcredry lRepositodentiaate UserCre  priv  owired


    @Autice;ticationServrvice authenenticationSe Authprivate
    towired

    @Aut {nServiceTeshenticatioclass Autsactional
Test
@Tranoot
@SpringB
```javavice
cation SerhentiTest: Aut#### }
```

);
    }
n(null)lidateTokerovider.vanPse(jwtTokertFal       asse""));
 ateToken(lidvider.vajwtTokenProsertFalse(
        asere"));en.halid.tokn("invateTokeovider.validkenPre(jwtToassertFals {
        oken()InvalidTst   void te  @Test
   }

  en));
  (tokTokenlidateider.varovjwtTokenPassertFalse(
        // (2000);ead.sleep Thr    //
    me)mocked tio, use l scenari reaon (inratior expi// Wait f 
              token));
 oken(idateTProvider.valkenwtTorue(jtT       assertely
 d immediauld be valiho/ Token s
        /
        dential);oken(cresTccesateA.genererenProvidok= jwtT token      Stringsting)
   r te (foration expisecondn with 1-nerate toke   // Ge
     );
        CUSTOMERerRole.e(Usal.setRolredenti        cle.com");
test@exampmail("al.setE    credenti
    Id(123L);setUsertial.eden
        crl();edentiarCrnew Usedential = l crerCredentia       Usen {
 tioptedExceps Interrution() throwkenExpiravoid testTo       @Test


 
    }R", role);("CUSTOMErtEquals    assemail);
    .com", eplest@examls("teua  assertEqId);
      , userEquals(123Lassert     
      ken);
     mToken(tor.getRoleFroTokenProvide jwtring role =      St);
  kenmToken(togetEmailFror.nProvideTokeail = jwting emStr      
  oken(token);serIdFromT.getUervidokenProerId = jwtTng usLoms
         clai/ Verify        /
        
natureyload.Sigeader.Pa/ H; /th == 3)ng.").leplit("\\ue(token.sssertTr a    token);
   ll(assertNotNu
           l);
     n(credentiassTokeceateAcvider.generkenProken = jwtToing to        Str
        
CUSTOMER);ole.serRe(Ul.setRoldentia
        cree.com");mpltest@examail("l.setE  credentia     
 123L);rId(tial.setUse    credenl();
    erCredentia new Uscredential =l erCredentia Us       Token() {
rateAccessid testGene  vo@Test
  

    ovider;wtTokenProvider jJwtTokenPre vat  prired
  Autowi

    @iderTest {kenProvlass JwtTot
cBootTesva
@Spring

```ja Generation JWT Tokent:

#### Tes }
}
```  ctor);
 ", costFa"12s(qual  assertE     \$")[2];
 it("\.splcodedFactor = en String cost $
        secondftere number ar is thactot f Cos   //    12$...
 at: $2a$rypt form // BC              
 d);
passworcode(r.enswordEncodeed = pasncodg einStr  
      3";12TestPassssword = " pa     Stringr() {
   tFactoCryptCosoid testB
    vest }

    @Trd));
   ncodedPasswod", esswor"WrongPa.matches(swordEncodere(paslsassertFa      
  ord));dPasswcodessword, ens(rawPaer.matchesswordEncod(pasertTrue     astching
   ma/ Verify         /  
"));
      12$h("$2a$tsWitstarrd.dPasswoue(encode    assertTr    ;
edPassword)odword, encals(rawPassEqutNot   asser    );
 swordPasl(encodedssertNotNul      a
  ingy encod // Verif
              
 wPassword);encode(rar.rdEncode = passwoPasswordencodedng   Stri   sword
    Encode pas//          
  ;
    s123"= "TestPasPassword ng raw      Striing() {
  rdEncodtPasswovoid tes@Test
        Encoder;

 passwordrdEncoderPasswo   private ed
   @Autowirst {

  igTerityConfecut
class STes
@SpringBoot

```javaodingEncrd wot: Pass
#### Tes
tsUnit Tes## 10.3 -

#
--;
```

})userId);, jsonData.serId"et("uent.snmronvi);
    pm.eokenfreshTta.reonDa", jsfreshToken("reonment.setir   pm.enven);
 ssToka.acce jsonDatssToken",("acceetment.snviron  pm.e
  ();e.json.responsata = pm   var jsonD{
 ion () ", functave tokensst("Se
pm.teesponsens from rtok
// Save avascript``jh):
`res/Logout/Refin Logcript** (forequest Sre-r`

**P""
}
``serId": ",
  "uken": " "refreshTo",
 ": "nsTokecces1",
  "at:808//localhostp:Url": "ht"base
  
{**:
```jsont Variablesmenviron*En

*
```nisted Tokelackl Bidate Val
    └──n Toke Active── Validateoken
    ├─ Validate Token
└─h Invalid T  └── Refresoken
│ ired TRefresh Exp ├── n
│  id Tokeal Refresh V  ├──
│ nfresh TokeReuccess
├── gout SLo  └── ut
│ nt
├── LogoLocked Accou── Login   └r
│  Usestentogin Non-exi ├── Lssword
│   PaongLogin Wr│   ├──  Success
── LoginLogin
│   ├
├── ict)(Conflcate Email er DupliRegist └── Error)
│  n datioaliord (Vsswk Pater with Weaegis├── Rn
│    Admiestauranter R Regist   ├──er
│er Custom── Regist│   ├ster
gi├── Reests
vice Tth Ser`
Aucture**:
``ruection Stn

**Collan CollectiotmPos 10.2 
###-

```

--/login"
}uth"/api/a: th"",
  "paT14:00:00-154-01amp": "202st
  "time14:30:00",01-15T until 2024-kedcount is loce": "Acsag",
  "mesT_LOCKED: "ACCOUNe" "errorCodn
{
 jsoN):
```FORBIDDE03 Response (4ed 

Expect | jq
```123"
  }'"TestPassword": "passom",
    xample.c"test@e"email": 
    '{-d " \
  n/jsonplicatio-Type: ap "Content
  -Hlogin \th/8081/api/auost:lhtp://loca-X POST htked
curl nt locouaccrn  should retumpt
# 6th atte

done""  echo | jq
    }' assword"
rongP"W d": "passwor,
     mple.com" "test@exa":"emaild '{
      
    -\tion/json" : applicaypeContent-T
    -H "th/login \api/au:8081/ost://localhX POST http
  curl -$i:"tempt Ato
  echo " d5};{1..i in d
for  passworwith wrongimes login 5 tt emp
# Att
```bash
koutnt Locou: Test Acct 6
#### Tes}
```
 true
":"validn
{
  K):
```jsoonse (200 Opected Resp
Ex jq
```
I" |=$JTtivalidate?j/auth/081/apiost:8ocalhT "http://l -X GE56"

curl123-def-4
JTI="abc-manually)code  or de (use jwt.iom tokenTI frot J Extrac`bash
#

``kenate Tolid: Vat 5## Tesen

##toke refresh amtoken with sess ): New accOKsponse (200 cted Re``

Expejq
`}" | KEN\"TO$REFRESH_": \"Token\efresh "{\"rn" \
  -dation/jsolice: appontent-Typ  -H "C\
th/refresh api/au:8081/hostlocal http://OST P
curl -X.."
9.JIUzUxMiJ"eyJhbGciOi_TOKEN=REFRESHsponse
ogin retoken from lh resref Save 
#bashToken

```esh est 4: Refr# T
###
}
```
ully"successfd out ": "Logge "message`json
{
 K):
``e (200 Osponscted Re``

Expe jq
` |KEN"_TO $ACCESStion: BearerorizathAuH "out \
  -/logauthst:8081/api/ocalho://lX POST httpcurl -

xMiJ9..."OiJIUzU"eyJhbGciSS_TOKEN=se
ACCEesponfrom login rtoken ccess 
# Save a

```basht 3: Logout## Tes
##onse
ration respste as regi0 OK): Same (20nsespocted Rxpe

E```| jq
23"
  }' Pass1est: "Tsword"
    "pasmple.com",est@exa": "t"email
    d '{on" \
  -ion/js: applicatt-Type"ContenH 
  -n \/logith81/api/aust:80alho/locOST http:/X P
curl -n

```bash2: Logi### Test }
```

#
STOMER": "CUrole"  "le.com",
test@examp": "
  "email": 1,"userId: 86400,
  resIn"
  "expi"Bearer",enType": "tok9...",
  MiJOiJIUzUxbGci"eyJh": shTokenfre
  "re",9...iJIUzUxMiJiOhbGc": "eyJnsToke"acces
  n
{):
```jsoREATEDponse (201 Cted Res

Expecq
```
  }' | jMER"USTOrole": "C
    "1234",: "+1415555neNumber"
# Auth Service Documentation - Part 2

## 12. Troubleshooting Guide

### 12.1 Service Won't Start

**Problem**: Service fails to start

**Possible Causes**:

1. **Config Server Unavailable**
   ```
   Error: Could not resolve placeholder 'jwt.secret'
   ```
   
   **Solution**:
   - Check Config Server is running: `curl http://localhost:8888/actuator/health`
   - Check bootstrap.yml has correct Config Server URL
   - Disable fail-fast temporarily: `spring.cloud.config.fail-fast=false`

2. **Database Connection Failed**
   ```
   Error: Connection to localhost:5432 refused
   ```
   
   **Solution**:
   - Check PostgreSQL is running: `psql -h localhost -U auth_admin -d auth_db`
   - Verify credentials in configuration
   - Check firewall rules
   - Verify database exists: `CREATE DATABASE auth_db;`

3. **Redis Connection Failed**
   ```
   Error: Unable to connect to Redis
   ```
   
   **Solution**:
   - Check Redis is running: `redis-cli ping`
   - Verify Redis host/port in configuration
   - Check Redis is accepting connections: `redis-cli -h localhost -p 6379`

4. **Port Already in Use**
   ```
   Error: Port 8081 is already in use
   ```
   
   **Solution**:
   - Find process using port: `netstat -ano | findstr :8081` (Windows)
   - Kill process or change port in configuration

---

### 12.2 Login Issues

**Problem**: User cannot login

**Diagnostic Steps**:

1. **Check if user exists**:
   ```sql
   SELECT * FROM user_credentials WHERE email = 'user@example.com';
   ```

2. **Check if account is active**:
   ```sql
   SELECT is_active, locked_until FROM user_credentials WHERE email = 'user@example.com';
   ```

3. **Check failed login attempts**:
   ```sql
   SELECT failed_login_attempts, locked_until FROM user_credentials WHERE email = 'user@example.com';
   ```

4. **Reset account lockout**:
   ```sql
   UPDATE user_credentials
   SET failed_login_attempts = 0, locked_until = NULL
   WHERE email = 'user@example.com';
   ```

5. **Reset password** (if forgotten):
   ```java
   // Generate new BCrypt hash
   String newHash = passwordEncoder.encode("NewPassword123");
   ```
   ```sql
   UPDATE user_credentials
   SET password_hash = '$2a$12$newHashHere...',
       password_changed_at = NOW()
   WHERE email = 'user@example.com';
   ```

---

### 12.3 Token Issues

**Problem**: Token validation fails

**Diagnostic Steps**:

1. **Check if token is blacklisted**:
   ```bash
   redis-cli
   > EXISTS blacklist:abc-123-def-456
   ```

2. **Check token expiration**:
   - Decode JWT at https://jwt.io
   - Check `exp` claim (Unix timestamp)
   - Compare with current time

3. **Verify JWT secret**:
   - Ensure same secret used for signing and verification
   - Check Config Server has correct secret
   - Verify secret is at least 512 bits (64 bytes)

4. **Clear blacklist** (if needed):
   ```bash
   redis-cli
   > DEL blacklist:abc-123-def-456
   ```

---

### 12.4 Performance Issues

**Problem**: Slow response times

**Diagnostic Steps**:

1. **Check database connection pool**:
   ```bash
   curl http://localhost:8081/actuator/metrics/hikaricp.connections.active | jq
   ```
   
   If pool is exhausted, increase pool size:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20  # Increase from 10
   ```

2. **Check Redis latency**:
   ```bash
   redis-cli --latency
   ```
   
   If high latency, check Redis server health

3. **Check BCrypt cost factor**:
   - Cost factor 12 = ~250ms per hash
   - If too slow, consider reducing to 10 (not recommended for production)

4. **Enable query logging**:
   ```yaml
   logging:
     level:
       org.hibernate.SQL: DEBUG
   ```
   
   Look for N+1 queries or missing indexes

5. **Check JVM memory**:
   ```bash
   curl http://localhost:8081/actuator/metrics/jvm.memory.used | jq
   ```
   
   If near limit, increase heap size:
   ```bash
   java -Xmx1g -jar auth-service.jar
   ```

---

### 12.5 Common Error Messages

| Error Message | Cause | Solution |
|---------------|-------|----------|
| "Invalid email or password" | Wrong credentials or user not found | Verify credentials, check database |
| "Account is locked until..." | Too many failed attempts | Wait or reset lockout in database |
| "User with email ... already exists" | Duplicate registration | Use different email or login |
| "Invalid refresh token" | Token expired, revoked, or invalid | Login again to get new tokens |
| "Validation failed" | Request validation error | Check request format and field values |
| "An unexpected error occurred" | Internal server error | Check logs for stack trace |

---

## 13. FAQ Section

### Q1: How does BCrypt password hashing work?

**Answer**: BCrypt is a password hashing function designed to be slow and resistant to brute force attacks.

**Key Features**:
- **Cost Factor**: Determines number of iterations (2^cost)
- **Salt**: Random value added to password before hashing
- **One-way**: Cannot reverse hash to get original password

**Example**:
```
Input: "SecurePass123"
Salt: Random 16-byte value
Cost: 12 (4,096 iterations)
Output: "$2a$12$KIXxKj3..." (60 characters)
```

**Format**: `$2a$12$saltsaltsaltsaltsalthashhashhashhashhashhashhash`
- `$2a$`: BCrypt algorithm version
- `12`: Cost factor
- Next 22 chars: Base64-encoded salt
- Remaining chars: Base64-encoded hash

**Why Cost Factor 12?**
- Balance between security and performance
- Takes ~250ms to hash (acceptable for login)
- Increases computation time for attackers
- Can be increased as hardware improves

---

### Q2: Why use JWT tokens instead of sessions?

**Answer**: JWT tokens enable stateless authentication, which is essential for microservices.

**Advantages**:
- **Stateless**: No server-side session storage
- **Scalable**: No session replication needed
- **Microservices-friendly**: Token contains all needed info
- **Cross-domain**: Works across different domains

**Disadvantages**:
- **Cannot revoke**: Must wait for expiration (solved with blacklist)
- **Size**: Larger than session IDs
- **Security**: Must protect secret key

**JWT vs Sessions**:
| Feature | JWT | Sessions |
|---------|-----|----------|
| Storage | Client-side | Server-side |
| Scalability | Excellent | Requires sticky sessions |
| Revocation | Difficult (need blacklist) | Easy |
| Size | Large (~200 bytes) | Small (session ID) |
| Microservices | Perfect | Challenging |

---

### Q3: Why store refresh tokens in database?

**Answer**: Database storage enables server-side revocation and tracking.

**Benefits**:
1. **Revocation**: Can invalidate tokens (logout all devices)
2. **Tracking**: Know which devices/sessions are active
3. **Security**: Detect suspicious activity
4. **Audit**: Track token usage

**Alternative Approaches**:
- **Redis**: Faster but less durable
- **No storage**: Cannot revoke tokens
- **Hybrid**: Store in both database and Redis

**Our Approach**:
- Store in PostgreSQL for durability
- Use Redis for blacklist (temporary data)

---

### Q4: How does account lockout prevent brute force attacks?

**Answer**: Account lockout limits the number of password guessing attempts.

**Mechanism**:
1. Track failed login attempts per account
2. After 5 failures, lock account for 30 minutes
3. Attacker cannot try more passwords during lockout
4. Successful login resets counter

**Security Considerations**:
- **Lockout Duration**: 30 minutes (balance between security and UX)
- **Max Attempts**: 5 (industry standard)
- **Account Enumeration**: Same error message for all failures
- **Distributed Attacks**: Consider IP-based rate limiting

**Limitations**:
- **Denial of Service**: Attacker can lock legitimate users
- **Distributed Attacks**: Attacker can use multiple IPs
- **Solution**: Add CAPTCHA after 3 failures

---

### Q5: Why use Redis for token blacklist?

**Answer**: Redis provides fast, temporary storage with automatic expiration.

**Benefits**:
1. **Speed**: < 1ms lookups (critical for every request)
2. **TTL**: Automatic cleanup of expired entries
3. **Memory**: Only stores active blacklisted tokens
4. **Scalability**: Can use Redis Cluster

**Why Not Database?**:
- Slower (10-50ms vs < 1ms)
- Requires manual cleanup
- More resource intensive

**Why Not In-Memory Map?**:
- Not shared across instances
- No automatic expiration
- Memory leaks if not cleaned

---

### Q6: How to handle token expiration gracefully?

**Answer**: Use refresh tokens to get new access tokens without re-login.

**Flow**:
```
1. Access token expires (24 hours)
   ↓
2. Client detects 401 error
   ↓
3. Client calls /api/auth/refresh with refresh token
   ↓
4. Server validates refresh token (7-day expiration)
   ↓
5. Server returns new access token
   ↓
6. Client retries original request with new token
```

**Client-Side Implementation**:
```javascript
async function apiCall(url, options) {
  let response = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  if (response.status === 401) {
    // Token expired, refresh it
    const refreshResponse = await fetch('/api/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken })
    });
    
    const { accessToken: newToken } = await refreshResponse.json();
    accessToken = newToken;
    
    // Retry original request
    response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    });
  }
  
  return response;
}
```

---

### Q7: How to implement "Remember Me" functionality?

**Answer**: Use longer-lived refresh tokens for "Remember Me".

**Implementation**:
```java
public AuthResponse login(LoginRequest request, boolean rememberMe) {
    // ... authentication logic ...
    
    // Adjust refresh token expiration
    long refreshExpiration = rememberMe 
        ? 30 * 24 * 60 * 60 * 1000  // 30 days
        : 7 * 24 * 60 * 60 * 1000;   // 7 days
    
    String refreshToken = jwtTokenProvider.generateRefreshToken(
        credential, refreshExpiration);
    
    // ... rest of logic ...
}
```

**Security Considerations**:
- Store "Remember Me" tokens securely (HttpOnly cookies)
- Allow users to view and revoke active sessions
- Implement device fingerprinting
- Require re-authentication for sensitive operations

---

### Q8: How to implement password reset?

**Answer**: Generate temporary reset token and send via email.

**Flow**:
```
1. User requests password reset
   ↓
2. Generate random reset token (UUID)
   ↓
3. Store token in database with expiration (1 hour)
   ↓
4. Send email with reset link
   ↓
5. User clicks link with token
   ↓
6. Validate token (not expired, not used)
   ↓
7. Allow user to set new password
   ↓
8. Hash new password and update database
   ↓
9. Invalidate reset token
```

**Implementation** (simplified):
```java
public void requestPasswordReset(String email) {
    UserCredential user = credentialRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException());
    
    String resetToken = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
    
    // Store token
    PasswordResetToken token = new PasswordResetToken(
        resetToken, user.getId(), expiresAt);
    resetTokenRepository.save(token);
    
    // Send email
    emailService.sendPasswordResetEmail(email, resetToken);
}

public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
        .orElseThrow(() -> new InvalidTokenException());
    
    if (resetToken.isExpired() || resetToken.isUsed()) {
        throw new InvalidTokenException();
    }
    
    UserCredential user = credentialRepository.findById(resetToken.getUserId())
        .orElseThrow(() -> new UserNotFoundException());
    
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setPasswordChangedAt(LocalDateTime.now());
    credentialRepository.save(user);
    
    resetToken.setUsed(true);
    resetTokenRepository.save(resetToken);
}
```

---

### Q9: How to implement multi-factor authentication (MFA)?

**Answer**: Add TOTP (Time-based One-Time Password) as second factor.

**Implementation Steps**:
1. Generate secret key for user
2. Display QR code for authenticator app
3. User scans QR code
4. Verify TOTP code during login
5. Store MFA status in user_credentials

**Code Example**:
```java
// Add to UserCredential entity
private Boolean mfaEnabled = false;
private String mfaSecret;

// Generate MFA secret
public String generateMfaSecret() {
    return new String(Base32.encode(
        SecureRandom.getInstanceStrong().generateSeed(20)));
}

// Verify TOTP code
public boolean verifyTotp(String secret, String code) {
    long timeWindow = System.currentTimeMillis() / 30000;
    String expectedCode = generateTotp(secret, timeWindow);
    return code.equals(expectedCode);
}

// Modified login flow
public AuthResponse login(LoginRequest request) {
    // ... password verification ...
    
    if (credential.getMfaEnabled()) {
        // Return temporary token requiring MFA
        return new MfaRequiredResponse(tempToken);
    }
    
    // ... generate tokens ...
}

public AuthResponse verifyMfa(String tempToken, String totpCode) {
    // Validate temp token
    // Verify TOTP code
    // Generate final tokens
}
```

---

### Q10: How to scale Auth Service horizontally?

**Answer**: Auth Service is designed to be stateless and horizontally scalable.

**Scaling Strategy**:
1. **Multiple Instances**: Run 3+ instances behind load balancer
2. **Shared Database**: All instances use same PostgreSQL
3. **Shared Redis**: All instances use same Redis (or Redis Cluster)
4. **Load Balancer**: Distribute requests across instances

**Configuration**:
```yaml
# Kubernetes Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3  # Run 3 instances
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: auth-service:1.0.0
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

**Considerations**:
- **Database Connection Pool**: Limit per instance (10 connections)
- **Redis Connection Pool**: Limit per instance (8 connections)
- **Session Affinity**: Not needed (stateless)
- **Health Checks**: Configure liveness and readiness probes

**Performance**:
- 1 instance: 100 req/s
- 3 instances: 300 req/s
- 10 instances: 1000 req/s

---

## Conclusion

The Auth Service is a critical component of the Food Delivery Platform, providing secure authentication and authorization for all users. It implements industry-standard security practices including BCrypt password hashing, JWT token-based authentication, account lockout protection, and token blacklisting.

**Key Features**:
- Secure password storage with BCrypt (cost factor 12)
- JWT-based stateless authentication
- Refresh token rotation for long-lived sessions
- Account lockout after 5 failed attempts
- Redis-based token blacklist for logout
- Role-based access control (CUSTOMER, RESTAURANT_ADMIN, DELIVERY_AGENT, SYSTEM_ADMIN)
- Comprehensive error handling and validation
- Horizontally scalable architecture

**Security Best Practices**:
- Never store passwords in plain text
- Use constant-time password comparison
- Implement rate limiting and account lockout
- Use secure random values for tokens
- Encrypt sensitive configuration (JWT secret)
- Log security events for audit
- Regularly rotate JWT secrets
- Monitor for suspicious activity

**Next Steps**:
- Implement password reset flow
- Add multi-factor authentication (MFA)
- Implement OAuth2/OpenID Connect
- Add device fingerprinting
- Implement session management UI
- Add security event notifications
- Implement IP-based rate limiting
- Add CAPTCHA for failed logins

For questions or issues, refer to the troubleshooting guide or contact the development team.
