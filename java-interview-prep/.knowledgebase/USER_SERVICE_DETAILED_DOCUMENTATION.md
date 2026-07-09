# User Service - Detailed Documentation

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

The User Service is the central hub for managing customer profiles and delivery addresses in the Food Delivery Platform. It solves several critical problems:

- **Profile Management**: Stores and manages user information (name, contact details, preferences)
- **Address Management**: Handles multiple delivery addresses per user with geolocation support
- **User Preferences**: Manages notification settings and language preferences
- **Data Consistency**: Ensures user data integrity across the platform
- **Fast Retrieval**: Provides quick access to user information for other services

### How Does It Fit in the Overall System?

```
┌─────────────────┐
│   API Gateway   │
└────────┬────────┘
         │
         ├──────────────────────────────────┐
         │                                  │
         v                                  v
┌─────────────────┐              ┌─────────────────┐
│  Auth Service   │              │  User Service   │ ◄── Focus
│  (Login/Signup) │              │  (Profiles)     │
└────────┬────────┘              └────────┬────────┘
         │                                │
         │                                │
         v                                v
┌─────────────────────────────────────────────────┐
│           PostgreSQL Database                   │
│  ┌──────────────┐      ┌──────────────┐        │
│  │ Auth DB      │      │ User DB      │        │
│  └──────────────┘      └──────────────┘        │
└─────────────────────────────────────────────────┘
         │
         v
┌─────────────────┐
│ Service Registry│
│   (Eureka)      │
└─────────────────┘
```

**Key Relationships:**
- **Auth Service**: Creates user ID during registration; User Service uses this ID
- **Order Service**: Retrieves user addresses for delivery
- **Notification Service**: Uses user preferences for sending notifications
- **API Gateway**: Routes all external requests to User Service

### Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.x | Application framework |
| Language | Java | 17+ | Programming language |
| Database | PostgreSQL | 15+ | Persistent storage |
| ORM | Spring Data JPA | 3.x | Database abstraction |
| Service Discovery | Netflix Eureka Client | 4.x | Service registration |
| API Documentation | SpringDoc OpenAPI | 2.x | Swagger UI |
| Validation | Jakarta Validation | 3.x | Input validation |
| Build Tool | Maven | 3.6+ | Dependency management |
| Monitoring | Spring Actuator | 3.x | Health checks |

---

## 2. Complete File Analysis

### Project Structure

```
user-service/
├── src/
│   └── main/
│       ├── java/com/fooddelivery/user/
│       │   ├── UserServiceApplication.java          # Main entry point
│       │   ├── controller/
│       │   │   ├── UserController.java              # User profile endpoints
│       │   │   └── AddressController.java           # Address management endpoints
│       │   ├── service/
│       │   │   ├── UserService.java                 # User business logic
│       │   │   └── AddressService.java              # Address business logic
│       │   ├── repository/
│       │   │   ├── UserRepository.java              # User data access
│       │   │   └── AddressRepository.java           # Address data access
│       │   ├── entity/
│       │   │   ├── User.java                        # User entity
│       │   │   ├── Address.java                     # Address entity
│       │   │   └── UserPreferences.java             # Embedded preferences
│       │   ├── dto/
│       │   │   ├── UserProfileResponse.java         # User response DTO
│       │   │   ├── UpdateUserProfileRequest.java    # Update user DTO
│       │   │   ├── AddressResponse.java             # Address response DTO
│       │   │   ├── CreateAddressRequest.java        # Create address DTO
│       │   │   └── UpdateAddressRequest.java        # Update address DTO
│       │   ├── mapper/
│       │   │   ├── UserMapper.java                  # User entity-DTO mapper
│       │   │   └── AddressMapper.java               # Address entity-DTO mapper
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java      # Centralized error handling
│       │       └── ResourceNotFoundException.java   # Custom exception
│       └── resources/
│           ├── application.yml                      # Base configuration
│           ├── application-dev.yml                  # Development config
│           ├── application-prod.yml                 # Production config
│           └── bootstrap.yml                        # Bootstrap config
├── Dockerfile                                       # Container image
├── k8s-deployment.yml                              # Kubernetes deployment
└── pom.xml                                         # Maven dependencies
```

### File-by-File Analysis

#### 2.1 UserServiceApplication.java

**Purpose**: Main entry point for the Spring Boot application

```java
package com.fooddelivery.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication  // Line 7: Enables auto-configuration, component scanning, and configuration
@EnableDiscoveryClient  // Line 8: Registers this service with Eureka for service discovery
public class UserServiceApplication {

    public static void main(String[] args) {
        // Line 11: Bootstraps the Spring application context
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

**Line-by-Line Explanation:**
- **Line 7 (`@SpringBootApplication`)**: Composite annotation that combines:
  - `@Configuration`: Marks class as source of bean definitions
  - `@EnableAutoConfiguration`: Enables Spring Boot's auto-configuration
  - `@ComponentScan`: Scans for components in `com.fooddelivery.user` package
- **Line 8 (`@EnableDiscoveryClient`)**: Enables service registration with Eureka
  - Service will register as "user-service" (from bootstrap.yml)
  - Allows other services to discover this service by name
- **Line 11**: Starts the embedded Tomcat server on port 8082

**Interactions:**
- Scans all classes in `com.fooddelivery.user` package
- Initializes Spring context with all beans
- Connects to Eureka server at startup
- Starts web server to handle HTTP requests

---

#### 2.2 UserController.java

**Purpose**: REST controller for user profile operations

```java
package com.fooddelivery.user.controller;

import com.fooddelivery.user.dto.UpdateUserProfileRequest;
import com.fooddelivery.user.dto.UserProfileResponse;
import com.fooddelivery.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController                    // Line 13: Marks as REST controller (combines @Controller + @ResponseBody)
@RequestMapping("/api/users")      // Line 14: Base path for all endpoints in this controller
@Tag(name = "User Profile", description = "User profile management endpoints")  // Line 15: Swagger documentation
public class UserController {

    @Autowired                     // Line 18: Dependency injection of UserService
    private UserService userService;

    @GetMapping("/{userId}")       // Line 21: Maps GET /api/users/{userId}
    @Operation(summary = "Get user profile", description = "Retrieves user profile information by user ID")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        // Line 24: @PathVariable extracts userId from URL path
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);  // Returns 200 OK with user profile
    }

    @PutMapping("/{userId}")       // Line 29: Maps PUT /api/users/{userId}
    @Operation(summary = "Update user profile", description = "Updates user profile information")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {  // Line 33: @Valid triggers validation
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);  // Returns 200 OK with updated profile
    }
}
```

**Line-by-Line Explanation:**
- **Line 13 (`@RestController`)**: Combines `@Controller` and `@ResponseBody`
  - All methods return data directly (not view names)
  - Responses automatically serialized to JSON
- **Line 14 (`@RequestMapping`)**: Sets base URL path
  - All endpoints start with `/api/users`
- **Line 15 (`@Tag`)**: Swagger documentation metadata
  - Groups endpoints in Swagger UI
- **Line 18 (`@Autowired`)**: Injects UserService instance
  - Spring creates and manages UserService bean
- **Line 21 (`@GetMapping`)**: Maps HTTP GET requests
  - Full path: `GET /api/users/{userId}`
- **Line 24 (`@PathVariable`)**: Extracts path variable
  - URL `/api/users/123` → `userId = 123`
- **Line 29 (`@PutMapping`)**: Maps HTTP PUT requests
  - Used for updates (idempotent operation)
- **Line 33 (`@Valid`)**: Triggers JSR-303 validation
  - Validates request body against constraints
  - Throws `MethodArgumentNotValidException` if invalid
- **Line 33 (`@RequestBody`)**: Deserializes JSON to Java object
  - Reads HTTP request body
  - Converts JSON to `UpdateUserProfileRequest`

**Interactions:**
- Receives HTTP requests from API Gateway
- Delegates business logic to UserService
- Returns JSON responses to clients
- Swagger annotations generate API documentation

---

#### 2.3 AddressController.java

**Purpose**: REST controller for address management operations

```java
package com.fooddelivery.user.controller;

import com.fooddelivery.user.dto.AddressResponse;
import com.fooddelivery.user.dto.CreateAddressRequest;
import com.fooddelivery.user.dto.UpdateAddressRequest;
import com.fooddelivery.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/addresses")  // Line 19: Nested resource path
@Tag(name = "Address Management", description = "User address management endpoints")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping  // Line 26: Maps GET /api/users/{userId}/addresses
    @Operation(summary = "Get user addresses", description = "Retrieves all addresses for a user")
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@PathVariable Long userId) {
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);  // Returns 200 OK with address list
    }

    @PostMapping  // Line 33: Maps POST /api/users/{userId}/addresses
    @Operation(summary = "Create address", description = "Creates a new address for a user")
    public ResponseEntity<AddressResponse> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody CreateAddressRequest request) {
        AddressResponse response = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // Returns 201 CREATED
    }

    @PutMapping("/{addressId}")  // Line 42: Maps PUT /api/users/{userId}/addresses/{addressId}
    @Operation(summary = "Update address", description = "Updates an existing address")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,  // Line 46: Two path variables
            @Valid @RequestBody UpdateAddressRequest request) {
        AddressResponse response = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(response);  // Returns 200 OK
    }

    @DeleteMapping("/{addressId}")  // Line 52: Maps DELETE /api/users/{userId}/addresses/{addressId}
    @Operation(summary = "Delete address", description = "Deletes an address")
    public ResponseEntity<Map<String, String>> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));  // Returns 200 OK
    }
}
```

**Key Design Decisions:**
- **Nested Resource Path**: `/api/users/{userId}/addresses` clearly shows addresses belong to users
- **RESTful Design**: Uses appropriate HTTP methods (GET, POST, PUT, DELETE)
- **Status Codes**: Returns 201 CREATED for new resources, 200 OK for others
- **Two Path Variables**: Both userId and addressId ensure address belongs to correct user

**Interactions:**
- Validates user exists before operating on addresses
- Delegates to AddressService for business logic
- Returns appropriate HTTP status codes

---

#### 2.4 UserService.java

**Purpose**: Business logic for user profile operations

```java
package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.UpdateUserProfileRequest;
import com.fooddelivery.user.dto.UserProfileResponse;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.exception.ResourceNotFoundException;
import com.fooddelivery.user.mapper.UserMapper;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service  // Line 13: Marks as service layer component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)  // Line 22: Read-only transaction (optimization)
    public UserProfileResponse getUserProfile(Long userId) {
        // Line 24-25: Fetch user or throw exception
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toUserProfileResponse(user);  // Convert entity to DTO
    }

    @Transactional  // Line 31: Read-write transaction
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Lines 36-50: Update only provided fields (partial update)
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getPreferences() != null) {
            user.setPreferences(request.getPreferences());
        }

        user = userRepository.save(user);  // Line 52: Persist changes
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);  // Efficient existence check
    }
}
```

**Line-by-Line Explanation:**
- **Line 13 (`@Service`)**: Marks as Spring service component
  - Automatically detected by component scanning
  - Eligible for dependency injection
- **Line 22 (`@Transactional(readOnly = true)`)**: Optimizes read operations
  - No flush to database
  - Better performance for queries
  - Prevents accidental modifications
- **Line 24-25**: Uses Optional pattern
  - `findById()` returns `Optional<User>`
  - `orElseThrow()` throws exception if not found
  - Cleaner than null checks
- **Line 31 (`@Transactional`)**: Ensures ACID properties
  - Automatic rollback on exceptions
  - Commits on successful completion
- **Lines 36-50**: Partial update pattern
  - Only updates fields that are provided
  - Null fields are ignored
  - Allows flexible updates
- **Line 52**: JPA automatically detects changes
  - Dirty checking mechanism
  - Generates UPDATE SQL only for changed fields

**Why This Design?**
- **Separation of Concerns**: Business logic separate from controllers
- **Transaction Management**: Ensures data consistency
- **Partial Updates**: Clients can update specific fields
- **Mapper Pattern**: Keeps entities separate from DTOs

---

#### 2.5 AddressService.java

**Purpose**: Business logic for address management

```java
package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.AddressResponse;
import com.fooddelivery.user.dto.CreateAddressRequest;
import com.fooddelivery.user.dto.UpdateAddressRequest;
import com.fooddelivery.user.entity.Address;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.exception.ResourceNotFoundException;
import com.fooddelivery.user.mapper.AddressMapper;
import com.fooddelivery.user.repository.AddressRepository;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        // Line 32-34: Validate user exists first
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Line 37-40: Fetch and transform addresses
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toAddressResponse)  // Method reference
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Line 49-51: Handle default address logic
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultAddresses(userId);  // Unset other defaults
        }

        Address address = addressMapper.toAddress(request);
        address.setUser(user);  // Line 55: Establish relationship

        address = addressRepository.save(address);
        return addressMapper.toAddressResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        // Line 63-65: Fetch address ensuring it belongs to user
        Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId + " for user: " + userId));

        // Lines 68-88: Partial update pattern
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }
        if (request.getIsDefault() != null) {
            if (Boolean.TRUE.equals(request.getIsDefault())) {
                unsetDefaultAddresses(userId);  // Unset others if setting as default
            }
            address.setIsDefault(request.getIsDefault());
        }

        address = addressRepository.save(address);
        return addressMapper.toAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId + " for user: " + userId));

        addressRepository.delete(address);
    }

    // Line 107-111: Private helper method
    private void unsetDefaultAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        addresses.forEach(addr -> addr.setIsDefault(false));  // Unset all defaults
        addressRepository.saveAll(addresses);  // Batch update
    }
}
```

**Key Business Logic:**
- **Default Address Management**: Only one address can be default
  - When setting new default, unsets all others
  - Ensures data consistency
- **User Validation**: Always validates user exists before operations
- **Security**: Verifies address belongs to user (prevents unauthorized access)
- **Batch Operations**: Uses `saveAll()` for efficiency

**Interactions:**
- Works with both UserRepository and AddressRepository
- Uses AddressMapper for entity-DTO conversion
- Manages bidirectional relationship between User and Address

---

#### 2.6 Entity Classes

##### User.java

**Purpose**: JPA entity representing a user in the database

```java
@Entity  // Line 10: Marks as JPA entity
@Table(name = "users")  // Line 11: Maps to "users" table
public class User {

    @Id  // Line 14: Primary key
    private Long id;  // Note: Not auto-generated, comes from Auth Service

    @Column(unique = true, nullable = false)  // Line 17: Database constraints
    @Email(message = "Invalid email format")  // Line 18: Validation
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(unique = true)  // Line 28: Unique constraint
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;  // E.164 format

    private String profileImageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)  // Line 34
    private List<Address> addresses = new ArrayList<>();

    @Embedded  // Line 37: Embeds UserPreferences fields into users table
    private UserPreferences preferences;

    @Column(nullable = false, updatable = false)  // Line 40: Audit field
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist  // Line 46: Called before INSERT
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (preferences == null) {
            preferences = new UserPreferences();  // Default preferences
        }
    }

    @PreUpdate  // Line 55: Called before UPDATE
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for bidirectional relationship
    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);  // Maintain both sides
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }
    
    // ... getters and setters ...
}
```

**Key Design Decisions:**
- **ID Not Auto-Generated**: User ID comes from Auth Service
  - Ensures consistency across services
  - Auth Service creates user during registration
- **Email & Phone Unique**: Prevents duplicate accounts
- **Bidirectional Relationship**: User ↔ Address
  - `mappedBy = "user"`: Address owns the relationship
  - `cascade = CascadeType.ALL`: Operations cascade to addresses
  - `orphanRemoval = true`: Deletes addresses when removed from list
- **Embedded Preferences**: Stored in same table (no join needed)
- **Audit Fields**: Automatic timestamp management
- **E.164 Phone Format**: International phone number standard
  - Pattern: `^\\+?[1-9]\\d{1,14}$`
  - Examples: +14155552671, 14155552671

**Database Mapping:**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) UNIQUE,
    profile_image_url VARCHAR(255),
    sms_notifications BOOLEAN,
    email_notifications BOOLEAN,
    push_notifications BOOLEAN,
    preferred_language VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

##### Address.java

**Purpose**: JPA entity representing a delivery address

```java
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Line 6: Auto-increment
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // Line 9: Many addresses to one user
    @JoinColumn(name = "user_id", nullable = false)  // Line 10: Foreign key column
    private User user;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;  // Optional

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Zip code is required")
    private String zipCode;

    @NotNull(message = "Latitude is required")
    private Double latitude;  // For delivery tracking

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Boolean isDefault = false;  // Line 33: Default value

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ... getters and setters ...
}
```

**Key Design Decisions:**
- **Auto-Generated ID**: Unlike User, Address ID is auto-generated
- **Lazy Loading**: User is loaded only when accessed
  - Improves performance when fetching address lists
- **Geolocation**: Latitude/Longitude for delivery tracking
  - Used by delivery service to calculate routes
  - Required fields (not optional)
- **Default Flag**: Only one address should be default per user
  - Business logic enforced in AddressService
- **Audit Fields**: Track when address was created/updated

**Database Mapping:**
```sql
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    zip_code VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_default ON addresses(user_id, is_default);
```

---

##### UserPreferences.java

**Purpose**: Embeddable object for user notification preferences

```java
@Embeddable  // Line 3: Not a separate table, embedded in users table
public class UserPreferences {

    private Boolean smsNotifications = true;      // Default: enabled
    private Boolean emailNotifications = true;    // Default: enabled
    private Boolean pushNotifications = true;     // Default: enabled
    private String preferredLanguage = "en";      // Default: English

    // Constructors
    public UserPreferences() {
    }

    public UserPreferences(Boolean smsNotifications, Boolean emailNotifications, 
                          Boolean pushNotifications, String preferredLanguage) {
        this.smsNotifications = smsNotifications;
        this.emailNotifications = emailNotifications;
        this.pushNotifications = pushNotifications;
        this.preferredLanguage = preferredLanguage;
    }
    
    // ... getters and setters ...
}
```

**Why Embeddable?**
- **No Separate Table**: Fields stored directly in users table
- **Always Loaded**: No lazy loading issues
- **Simpler Queries**: No joins needed
- **Atomic Updates**: Updated with user in single transaction

**Use Case:**
- Notification Service reads preferences before sending notifications
- User can enable/disable notification channels
- Language preference for localized messages

---

#### 2.7 Repository Interfaces

##### UserRepository.java

**Purpose**: Data access layer for User entity

```java
@Repository  // Line 9: Marks as repository component
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);  // Custom query method

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);  // Existence check

    boolean existsByPhoneNumber(String phoneNumber);
}
```

**Spring Data JPA Magic:**
- **Method Name Parsing**: Spring generates queries from method names
  - `findByEmail` → `SELECT * FROM users WHERE email = ?`
  - `existsByEmail` → `SELECT COUNT(*) > 0 FROM users WHERE email = ?`
- **No Implementation Needed**: Spring creates proxy at runtime
- **Type Safety**: Compile-time checking of method signatures

**Generated SQL:**
```sql
-- findByEmail
SELECT u.* FROM users u WHERE u.email = ?

-- existsByEmail
SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM users u WHERE u.email = ?
```

---

##### AddressRepository.java

**Purpose**: Data access layer for Address entity

```java
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);  // Find all addresses for user

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id = :addressId")
    Optional<Address> findByUserIdAndAddressId(@Param("userId") Long userId, @Param("addressId") Long addressId);
}
```

**Custom Queries:**
- **JPQL Queries**: Object-oriented query language
  - `Address a` refers to entity, not table
  - `a.user.id` navigates relationship
- **Named Parameters**: `:userId` and `:addressId`
  - Safer than positional parameters
  - More readable

**Why Custom Queries?**
- **findDefaultAddressByUserId**: Combines two conditions (userId + isDefault)
- **findByUserIdAndAddressId**: Security check (ensures address belongs to user)

**Generated SQL:**
```sql
-- findByUserId
SELECT a.* FROM addresses a WHERE a.user_id = ?

-- findDefaultAddressByUserId
SELECT a.* FROM addresses a WHERE a.user_id = ? AND a.is_default = TRUE

-- findByUserIdAndAddressId
SELECT a.* FROM addresses a WHERE a.user_id = ? AND a.id = ?
```

---

#### 2.8 DTO Classes

##### UserProfileResponse.java

**Purpose**: Response DTO for user profile data

```java
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImageUrl;
    private List<AddressResponse> addresses;  // Nested DTOs
    private UserPreferences preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ... getters and setters ...
}
```

**Why DTOs?**
- **Decoupling**: API contract independent of database schema
- **Security**: Don't expose internal entity structure
- **Flexibility**: Can combine data from multiple entities
- **Performance**: Include only needed fields

**Example JSON:**
```json
{
  "id": 123,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+14155552671",
  "profileImageUrl": "https://example.com/profiles/john.jpg",
  "addresses": [
    {
      "id": 1,
      "addressLine1": "123 Main St",
      "city": "San Francisco",
      "state": "CA",
      "zipCode": "94102",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "isDefault": true,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "preferences": {
    "smsNotifications": true,
    "emailNotifications": true,
    "pushNotifications": false,
    "preferredLanguage": "en"
  },
  "createdAt": "2024-01-10T08:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

##### UpdateUserProfileRequest.java

**Purpose**: Request DTO for updating user profile

```java
public class UpdateUserProfileRequest {
    private String firstName;  // All fields optional
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String profileImageUrl;
    private UserPreferences preferences;
    
    // ... getters and setters ...
}
```

**Design Decisions:**
- **All Fields Optional**: Supports partial updates
- **Validation**: Phone number format validated
- **No Email Update**: Email is immutable (business rule)
- **No ID**: ID comes from path variable

**Example JSON:**
```json
{
  "firstName": "John",
  "phoneNumber": "+14155552671",
  "preferences": {
    "pushNotifications": false
  }
}
```

---

##### CreateAddressRequest.java

**Purpose**: Request DTO for creating new address

```java
public class CreateAddressRequest {
    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;  // Optional

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Zip code is required")
    private String zipCode;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Boolean isDefault = false;
    
    // ... getters and setters ...
}
```

**Validation Rules:**
- **Required Fields**: addressLine1, city, state, zipCode, latitude, longitude
- **Optional Fields**: addressLine2, isDefault
- **Default Value**: isDefault = false

**Example JSON:**
```json
{
  "addressLine1": "123 Main St",
  "addressLine2": "Apt 4B",
  "city": "San Francisco",
  "state": "CA",
  "zipCode": "94102",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "isDefault": true
}
```

---

#### 2.9 Mapper Classes

##### UserMapper.java

**Purpose**: Converts between User entity and UserProfileResponse DTO

```java
@Component  // Line 11: Spring-managed bean
public class UserMapper {

    @Autowired
    private AddressMapper addressMapper;  // Dependency for nested mapping

    public UserProfileResponse toUserProfileResponse(User user) {
        if (user == null) {
            return null;  // Null-safe
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setPreferences(user.getPreferences());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Line 32-37: Map nested addresses
        if (user.getAddresses() != null) {
            response.setAddresses(
                    user.getAddresses().stream()
                            .map(addressMapper::toAddressResponse)  // Method reference
                            .collect(Collectors.toList())
            );
        }

        return response;
    }
}
```

**Why Manual Mapping?**
- **Control**: Full control over mapping logic
- **Flexibility**: Can add custom transformations
- **No Magic**: Explicit and easy to debug
- **Alternative**: Could use MapStruct for auto-generation

**Stream API Explanation:**
```java
user.getAddresses().stream()                    // Create stream from list
    .map(addressMapper::toAddressResponse)      // Transform each address
    .collect(Collectors.toList())               // Collect to list
```

---

##### AddressMapper.java

**Purpose**: Converts between Address entity and DTOs

```java
@Component
public class AddressMapper {

    // Entity → DTO
    public AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setZipCode(address.getZipCode());
        response.setLatitude(address.getLatitude());
        response.setLongitude(address.getLongitude());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());

        return response;
    }

    // DTO → Entity
    public Address toAddress(CreateAddressRequest request) {
        if (request == null) {
            return null;
        }

        Address address = new Address();
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setIsDefault(request.getIsDefault());

        return address;
    }
}
```

**Two-Way Mapping:**
- **toAddressResponse**: Entity → DTO (for responses)
- **toAddress**: DTO → Entity (for creation)

**Note**: No `toAddress(UpdateAddressRequest)` method
- Updates handled directly in service layer
- Avoids overwriting unchanged fields

---

#### 2.10 Exception Handling

##### ResourceNotFoundException.java

**Purpose**: Custom exception for resource not found scenarios

```java
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);  // Pass message to RuntimeException
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);  // Include cause for debugging
    }
}
```

**Why RuntimeException?**
- **Unchecked Exception**: No need for try-catch everywhere
- **Spring Transaction**: Triggers automatic rollback
- **Cleaner Code**: No exception declarations in method signatures

---

##### GlobalExceptionHandler.java

**Purpose**: Centralized exception handling for consistent error responses

```java
@RestControllerAdvice  // Line 20: Global exception handler for all controllers
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handler 1: Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Handler 2: Validation Errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", validationErrors);

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                validationErrors  // Field-level errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handler 3: Type Mismatch (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch: {} for parameter {}", ex.getValue(), ex.getName());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse error = new ErrorResponse(
                "INVALID_PARAMETER",
                message,
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handler 4: Data Integrity Violations (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation. The operation conflicts with existing data.";
        if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Unique index")) {
            message = "A record with the provided information already exists";
        } else if (ex.getMessage().contains("foreign key constraint")) {
            message = "The operation references non-existent data";
        }
        
        ErrorResponse error = new ErrorResponse(
                "DATA_INTEGRITY_VIOLATION",
                message,
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Handler 5: Illegal Arguments (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handler 6: Catch-All (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: ", ex);
        
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Standard error response format
    static class ErrorResponse {
        private String errorCode;
        private String message;
        private LocalDateTime timestamp;
        private String path;
        private Map<String, String> details;

        // Constructors and getters...
    }
}
```

**Exception Handling Strategy:**

| Exception | HTTP Status | Error Code | Use Case |
|-----------|-------------|------------|----------|
| ResourceNotFoundException | 404 NOT FOUND | RESOURCE_NOT_FOUND | User/Address not found |
| MethodArgumentNotValidException | 400 BAD REQUEST | VALIDATION_ERROR | Invalid input data |
| MethodArgumentTypeMismatchException | 400 BAD REQUEST | INVALID_PARAMETER | Wrong parameter type |
| DataIntegrityViolationException | 409 CONFLICT | DATA_INTEGRITY_VIOLATION | Unique constraint violation |
| IllegalArgumentException | 400 BAD REQUEST | INVALID_ARGUMENT | Business rule violation |
| Exception | 500 INTERNAL SERVER ERROR | INTERNAL_SERVER_ERROR | Unexpected errors |

**Error Response Format:**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/123",
  "details": {
    "phoneNumber": "Invalid phone number format",
    "firstName": "First name is required"
  }
}
```

**Why This Design?**
- **Consistent Format**: All errors follow same structure
- **Client-Friendly**: Clear error codes and messages
- **Debugging**: Includes timestamp and path
- **Field-Level Errors**: Validation errors show which fields failed
- **Logging**: Different log levels (WARN vs ERROR)

---

## 3. Component Deep Dive

### 3.1 Controllers

#### UserController Endpoints

**Endpoint 1: Get User Profile**
```
GET /api/users/{userId}
```

**Purpose**: Retrieve complete user profile including addresses

**Request:**
```bash
curl -X GET http://localhost:8082/api/users/123
```

**Response (200 OK):**
```json
{
  "id": 123,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+14155552671",
  "profileImageUrl": "https://example.com/profiles/john.jpg",
  "addresses": [
    {
      "id": 1,
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "city": "San Francisco",
      "state": "CA",
      "zipCode": "94102",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "isDefault": true,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "preferences": {
    "smsNotifications": true,
    "emailNotifications": true,
    "pushNotifications": false,
    "preferredLanguage": "en"
  },
  "createdAt": "2024-01-10T08:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Error Response (404 NOT FOUND):**
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with id: 123",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/123"
}
```

**Performance**: < 200ms (as per requirements)

---

**Endpoint 2: Update User Profile**
```
PUT /api/users/{userId}
```

**Purpose**: Update user profile information (partial update supported)

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "phoneNumber": "+14155552671",
    "preferences": {
      "pushNotifications": false,
      "preferredLanguage": "es"
    }
  }'
```

**Response (200 OK):**
```json
{
  "id": 123,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+14155552671",
  "profileImageUrl": "https://example.com/profiles/john.jpg",
  "addresses": [...],
  "preferences": {
    "smsNotifications": true,
    "emailNotifications": true,
    "pushNotifications": false,
    "preferredLanguage": "es"
  },
  "createdAt": "2024-01-10T08:00:00",
  "updatedAt": "2024-01-15T10:35:00"
}
```

**Validation Error (400 BAD REQUEST):**
```bash
curl -X PUT http://localhost:8082/api/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "invalid-phone"
  }'
```

Response:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/123",
  "details": {
    "phoneNumber": "Invalid phone number format"
  }
}
```

**Performance**: < 500ms (as per requirements)

---

#### AddressController Endpoints

**Endpoint 1: Get User Addresses**
```
GET /api/users/{userId}/addresses
```

**Purpose**: Retrieve all addresses for a user

**Request:**
```bash
curl -X GET http://localhost:8082/api/users/123/addresses
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94102",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "isDefault": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "addressLine1": "456 Oak Ave",
    "city": "Oakland",
    "state": "CA",
    "zipCode": "94601",
    "latitude": 37.8044,
    "longitude": -122.2712,
    "isDefault": false,
    "createdAt": "2024-01-16T14:20:00",
    "updatedAt": "2024-01-16T14:20:00"
  }
]
```

**Empty List (200 OK):**
```json
[]
```

---

**Endpoint 2: Create Address**
```
POST /api/users/{userId}/addresses
```

**Purpose**: Create a new delivery address for a user

**Request:**
```bash
curl -X POST http://localhost:8082/api/users/123/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine1": "789 Pine St",
    "addressLine2": "Suite 200",
    "city": "Berkeley",
    "state": "CA",
    "zipCode": "94704",
    "latitude": 37.8715,
    "longitude": -122.2730,
    "isDefault": false
  }'
```

**Response (201 CREATED):**
```json
{
  "id": 3,
  "addressLine1": "789 Pine St",
  "addressLine2": "Suite 200",
  "city": "Berkeley",
  "state": "CA",
  "zipCode": "94704",
  "latitude": 37.8715,
  "longitude": -122.2730,
  "isDefault": false,
  "createdAt": "2024-01-17T09:15:00",
  "updatedAt": "2024-01-17T09:15:00"
}
```

**Validation Error (400 BAD REQUEST):**
```bash
curl -X POST http://localhost:8082/api/users/123/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine1": "789 Pine St",
    "city": "Berkeley"
  }'
```

Response:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-17T09:15:00",
  "path": "/api/users/123/addresses",
  "details": {
    "state": "State is required",
    "zipCode": "Zip code is required",
    "latitude": "Latitude is required",
    "longitude": "Longitude is required"
  }
}
```

---

**Endpoint 3: Update Address**
```
PUT /api/users/{userId}/addresses/{addressId}
```

**Purpose**: Update an existing address (partial update supported)

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123/addresses/2 \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine2": "Unit 5",
    "isDefault": true
  }'
```

**Response (200 OK):**
```json
{
  "id": 2,
  "addressLine1": "456 Oak Ave",
  "addressLine2": "Unit 5",
  "city": "Oakland",
  "state": "CA",
  "zipCode": "94601",
  "latitude": 37.8044,
  "longitude": -122.2712,
  "isDefault": true,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-17T09:20:00"
}
```

**Note**: When setting `isDefault: true`, all other addresses for this user are automatically set to `isDefault: false`.

---

**Endpoint 4: Delete Address**
```
DELETE /api/users/{userId}/addresses/{addressId}
```

**Purpose**: Delete a delivery address

**Request:**
```bash
curl -X DELETE http://localhost:8082/api/users/123/addresses/3
```

**Response (200 OK):**
```json
{
  "message": "Address deleted successfully"
}
```

**Error (404 NOT FOUND):**
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Address not found with id: 3 for user: 123",
  "timestamp": "2024-01-17T09:25:00",
  "path": "/api/users/123/addresses/3"
}
```

---

### 3.2 Services

#### UserService Business Logic

**Key Responsibilities:**
1. Fetch user profiles with addresses
2. Update user information
3. Validate user existence

**Transaction Management:**
- Read operations: `@Transactional(readOnly = true)` for optimization
- Write operations: `@Transactional` for ACID guarantees

**Partial Update Strategy:**
```java
// Only updates provided fields
if (request.getFirstName() != null) {
    user.setFirstName(request.getFirstName());
}
```

**Benefits:**
- Clients can update specific fields without sending entire object
- Reduces bandwidth
- Prevents accidental overwrites

---

#### AddressService Business Logic

**Key Responsibilities:**
1. Manage user addresses (CRUD operations)
2. Enforce default address business rule
3. Validate address ownership

**Default Address Logic:**
```java
if (Boolean.TRUE.equals(request.getIsDefault())) {
    unsetDefaultAddresses(userId);  // Unset all other defaults
}
```

**Flow:**
1. Check if new address is being set as default
2. If yes, fetch all user's addresses
3. Set `isDefault = false` for all addresses
4. Save all addresses (batch update)
5. Set new address as default

**Security Check:**
```java
Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
    .orElseThrow(() -> new ResourceNotFoundException(...));
```

**Why?** Prevents users from accessing/modifying other users' addresses
- URL: `/api/users/123/addresses/456`
- Query: `WHERE user_id = 123 AND address_id = 456`
- If address 456 belongs to user 789, query returns empty → 404 error

---

### 3.3 Repositories

#### Spring Data JPA Features Used

**1. Method Name Query Derivation**
```java
Optional<User> findByEmail(String email);
```
Generated SQL:
```sql
SELECT * FROM users WHERE email = ?
```

**2. Custom JPQL Queries**
```java
@Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);
```

**3. Existence Checks**
```java
boolean existsByEmail(String email);
```
More efficient than `findByEmail().isPresent()`

**4. Relationship Navigation**
```java
@Query("SELECT a FROM Address a WHERE a.user.id = :userId")
```
Navigates `Address → User` relationship

---

### 3.4 DTOs and Mappers

#### DTO Design Principles

**1. Request DTOs**
- Contain only fields that can be set/updated
- Include validation annotations
- No IDs (come from path variables)

**2. Response DTOs**
- Include all relevant data
- May combine multiple entities
- Include audit fields (createdAt, updatedAt)

**3. Separation of Concerns**
- Entities: Database representation
- DTOs: API contract
- Mappers: Translation layer

**Mapper Pattern Benefits:**
- **Flexibility**: Can transform data during mapping
- **Security**: Don't expose internal structure
- **Versioning**: Can support multiple API versions
- **Testing**: Easy to test mapping logic

---

### 3.5 Configuration

#### Application Configuration Files

**bootstrap.yml** (Loaded first)
```yaml
spring:
  application:
    name: user-service  # Service name in Eureka
  cloud:
    config:
      uri: http://localhost:8888  # Config Server URL
      fail-fast: true  # Fail if Config Server unavailable
      retry:
        max-attempts: 6
        initial-interval: 1000
        multiplier: 1.1
  profiles:
    active: dev  # Default profile
```

**application.yml** (Base configuration)
```yaml
spring:
  cloud:
    config:
      enabled: true  # Enable Config Server

server:
  port: 8082  # Fallback port

logging:
  level:
    com.fooddelivery.user: INFO
```

**application-dev.yml** (Development profile)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_service_db
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update schema
    show-sql: true  # Log SQL statements

logging:
  level:
    com.fooddelivery.user: DEBUG  # Verbose logging
```

**application-prod.yml** (Production profile)
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://postgres:5432/user_service_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  
  jpa:
    hibernate:
      ddl-auto: validate  # Only validate schema
    show-sql: false  # No SQL logging

logging:
  level:
    com.fooddelivery.user: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
```

**Configuration Hierarchy:**
1. bootstrap.yml (loaded first)
2. Config Server configuration (if available)
3. application.yml
4. application-{profile}.yml

---

## 4. Scenario-Based Examples

### Scenario 1: New User Registration Flow

**Context**: User just registered via Auth Service

**Step 1**: Auth Service creates user account
```bash
# Auth Service creates user with ID 123
POST /api/auth/register
{
  "email": "alice@example.com",
  "password": "SecurePass123!",
  "firstName": "Alice",
  "lastName": "Johnson"
}
```

**Step 2**: Auth Service creates user profile in User Service
```bash
# Internal call from Auth Service to User Service
POST /api/users (internal endpoint)
{
  "id": 123,
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Johnson"
}
```

**Step 3**: User retrieves their profile
```bash
curl -X GET http://localhost:8082/api/users/123
```

Response:
```json
{
  "id": 123,
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Johnson",
  "phoneNumber": null,
  "profileImageUrl": null,
  "addresses": [],
  "preferences": {
    "smsNotifications": true,
    "emailNotifications": true,
    "pushNotifications": true,
    "preferredLanguage": "en"
  },
  "createdAt": "2024-01-17T10:00:00",
  "updatedAt": "2024-01-17T10:00:00"
}
```

**Timing Diagram:**
```
Client          Auth Service      User Service      Database
  |                  |                  |               |
  |-- Register ----->|                  |               |
  |                  |-- Create User -->|               |
  |                  |                  |-- INSERT ---->|
  |                  |                  |<-- OK --------|
  |                  |<-- User ID ------|               |
  |<-- Success ------|                  |               |
  |                                     |               |
  |-- Get Profile ---------------------->|               |
  |                                     |-- SELECT ---->|
  |                                     |<-- User ------|
  |<-- Profile --------------------------|               |
```

---

### Scenario 2: User Updates Profile

**Context**: User wants to add phone number and update preferences

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+14155552671",
    "preferences": {
      "smsNotifications": true,
      "emailNotifications": false,
      "pushNotifications": true,
      "preferredLanguage": "en"
    }
  }'
```

**Processing Flow:**
1. Controller receives request
2. `@Valid` triggers validation (phone number format)
3. Service fetches user from database
4. Service updates only provided fields
5. JPA detects changes (dirty checking)
6. Transaction commits, UPDATE SQL executed
7. Mapper converts entity to DTO
8. Controller returns response

**SQL Generated:**
```sql
-- Fetch user
SELECT * FROM users WHERE id = 123;

-- Update user (only changed fields)
UPDATE users 
SET phone_number = '+14155552671',
    email_notifications = false,
    updated_at = '2024-01-17T10:05:00'
WHERE id = 123;
```

**Response:**
```json
{
  "id": 123,
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Johnson",
  "phoneNumber": "+14155552671",
  "profileImageUrl": null,
  "addresses": [],
  "preferences": {
    "smsNotifications": true,
    "emailNotifications": false,
    "pushNotifications": true,
    "preferredLanguage": "en"
  },
  "createdAt": "2024-01-17T10:00:00",
  "updatedAt": "2024-01-17T10:05:00"
}
```

**Performance**: ~150ms

---

### Scenario 3: User Adds First Address

**Context**: User adds their home address for delivery

**Request:**
```bash
curl -X POST http://localhost:8082/api/users/123/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine1": "742 Evergreen Terrace",
    "city": "Springfield",
    "state": "OR",
    "zipCode": "97477",
    "latitude": 44.0462,
    "longitude": -123.0236,
    "isDefault": true
  }'
```

**Processing Flow:**
1. Controller validates request body
2. Service fetches user (validates existence)
3. Service checks if `isDefault = true`
4. Since it's true, calls `unsetDefaultAddresses(123)`
   - Fetches all addresses for user 123
   - Sets `isDefault = false` for all (none exist yet)
5. Mapper converts DTO to entity
6. Sets user relationship
7. Saves address to database
8. Returns response with 201 CREATED

**SQL Generated:**
```sql
-- Validate user exists
SELECT COUNT(*) FROM users WHERE id = 123;

-- Fetch existing addresses (returns empty)
SELECT * FROM addresses WHERE user_id = 123;

-- Insert new address
INSERT INTO addresses (user_id, address_line1, city, state, zip_code, 
                       latitude, longitude, is_default, created_at, updated_at)
VALUES (123, '742 Evergreen Terrace', 'Springfield', 'OR', '97477',
        44.0462, -123.0236, true, '2024-01-17T10:10:00', '2024-01-17T10:10:00');
```

**Response (201 CREATED):**
```json
{
  "id": 1,
  "addressLine1": "742 Evergreen Terrace",
  "addressLine2": null,
  "city": "Springfield",
  "state": "OR",
  "zipCode": "97477",
  "latitude": 44.0462,
  "longitude": -123.0236,
  "isDefault": true,
  "createdAt": "2024-01-17T10:10:00",
  "updatedAt": "2024-01-17T10:10:00"
}
```

**Performance**: ~200ms

---

### Scenario 4: User Adds Second Address (Work)

**Context**: User adds work address, wants it as default

**Request:**
```bash
curl -X POST http://localhost:8082/api/users/123/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine1": "1 Infinite Loop",
    "city": "Cupertino",
    "state": "CA",
    "zipCode": "95014",
    "latitude": 37.3318,
    "longitude": -122.0312,
    "isDefault": true
  }'
```

**Processing Flow:**
1. Validates request
2. Fetches user
3. Checks `isDefault = true`
4. Calls `unsetDefaultAddresses(123)`
   - Fetches all addresses (finds address ID 1)
   - Sets address 1's `isDefault = false`
   - Saves address 1
5. Creates new address with `isDefault = true`
6. Saves new address

**SQL Generated:**
```sql
-- Fetch existing addresses
SELECT * FROM addresses WHERE user_id = 123;
-- Returns: [{ id: 1, isDefault: true, ... }]

-- Unset previous default
UPDATE addresses SET is_default = false, updated_at = '2024-01-17T10:15:00'
WHERE id = 1;

-- Insert new address
INSERT INTO addresses (user_id, address_line1, city, state, zip_code,
                       latitude, longitude, is_default, created_at, updated_at)
VALUES (123, '1 Infinite Loop', 'Cupertino', 'CA', '95014',
        37.3318, -122.0312, true, '2024-01-17T10:15:00', '2024-01-17T10:15:00');
```

**Response (201 CREATED):**
```json
{
  "id": 2,
  "addressLine1": "1 Infinite Loop",
  "addressLine2": null,
  "city": "Cupertino",
  "state": "CA",
  "zipCode": "95014",
  "latitude": 37.3318,
  "longitude": -122.0312,
  "isDefault": true,
  "createdAt": "2024-01-17T10:15:00",
  "updatedAt": "2024-01-17T10:15:00"
}
```

**Result**: Address 1 is no longer default, Address 2 is now default

---

### Scenario 5: User Retrieves All Addresses

**Context**: User wants to see all saved addresses

**Request:**
```bash
curl -X GET http://localhost:8082/api/users/123/addresses
```

**Processing Flow:**
1. Validates user exists
2. Fetches all addresses for user
3. Maps each address to DTO
4. Returns list

**SQL Generated:**
```sql
-- Validate user
SELECT COUNT(*) FROM users WHERE id = 123;

-- Fetch addresses
SELECT * FROM addresses WHERE user_id = 123 ORDER BY id;
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "addressLine1": "742 Evergreen Terrace",
    "addressLine2": null,
    "city": "Springfield",
    "state": "OR",
    "zipCode": "97477",
    "latitude": 44.0462,
    "longitude": -123.0236,
    "isDefault": false,
    "createdAt": "2024-01-17T10:10:00",
    "updatedAt": "2024-01-17T10:15:00"
  },
  {
    "id": 2,
    "addressLine1": "1 Infinite Loop",
    "addressLine2": null,
    "city": "Cupertino",
    "state": "CA",
    "zipCode": "95014",
    "latitude": 37.3318,
    "longitude": -122.0312,
    "isDefault": true,
    "createdAt": "2024-01-17T10:15:00",
    "updatedAt": "2024-01-17T10:15:00"
  }
]
```

**Performance**: ~100ms

---

### Scenario 6: User Updates Address

**Context**: User moves to a different apartment in same building

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123/addresses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine2": "Apt 5C"
  }'
```

**Processing Flow:**
1. Fetches address ensuring it belongs to user 123
2. Updates only `addressLine2` field
3. Saves changes
4. Returns updated address

**SQL Generated:**
```sql
-- Fetch and validate ownership
SELECT * FROM addresses WHERE user_id = 123 AND id = 1;

-- Update only changed field
UPDATE addresses 
SET address_line2 = 'Apt 5C', updated_at = '2024-01-17T10:20:00'
WHERE id = 1;
```

**Response (200 OK):**
```json
{
  "id": 1,
  "addressLine1": "742 Evergreen Terrace",
  "addressLine2": "Apt 5C",
  "city": "Springfield",
  "state": "OR",
  "zipCode": "97477",
  "latitude": 44.0462,
  "longitude": -123.0236,
  "isDefault": false,
  "createdAt": "2024-01-17T10:10:00",
  "updatedAt": "2024-01-17T10:20:00"
}
```

---

### Scenario 7: User Switches Default Address

**Context**: User wants to make home address default again

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123/addresses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "isDefault": true
  }'
```

**Processing Flow:**
1. Fetches address 1
2. Detects `isDefault = true` in request
3. Calls `unsetDefaultAddresses(123)`
   - Fetches all addresses (finds address 2 with `isDefault = true`)
   - Sets address 2's `isDefault = false`
4. Sets address 1's `isDefault = true`
5. Saves both addresses

**SQL Generated:**
```sql
-- Fetch address 1
SELECT * FROM addresses WHERE user_id = 123 AND id = 1;

-- Fetch all addresses
SELECT * FROM addresses WHERE user_id = 123;

-- Unset address 2
UPDATE addresses SET is_default = false, updated_at = '2024-01-17T10:25:00'
WHERE id = 2;

-- Set address 1 as default
UPDATE addresses SET is_default = true, updated_at = '2024-01-17T10:25:00'
WHERE id = 1;
```

**Response (200 OK):**
```json
{
  "id": 1,
  "addressLine1": "742 Evergreen Terrace",
  "addressLine2": "Apt 5C",
  "city": "Springfield",
  "state": "OR",
  "zipCode": "97477",
  "latitude": 44.0462,
  "longitude": -123.0236,
  "isDefault": true,
  "createdAt": "2024-01-17T10:10:00",
  "updatedAt": "2024-01-17T10:25:00"
}
```

---

### Scenario 8: User Deletes Address

**Context**: User no longer needs work address

**Request:**
```bash
curl -X DELETE http://localhost:8082/api/users/123/addresses/2
```

**Processing Flow:**
1. Fetches address 2, validates ownership
2. Deletes address from database
3. Returns success message

**SQL Generated:**
```sql
-- Fetch and validate
SELECT * FROM addresses WHERE user_id = 123 AND id = 2;

-- Delete
DELETE FROM addresses WHERE id = 2;
```

**Response (200 OK):**
```json
{
  "message": "Address deleted successfully"
}
```

**Note**: If deleted address was default, no other address is automatically promoted. User must explicitly set another as default.

---

### Scenario 9: Validation Error Handling

**Context**: User submits invalid phone number

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "123-456-7890"
  }'
```

**Processing Flow:**
1. Controller receives request
2. `@Valid` annotation triggers validation
3. Phone number fails regex pattern
4. `MethodArgumentNotValidException` thrown
5. GlobalExceptionHandler catches exception
6. Extracts field errors
7. Returns 400 BAD REQUEST with details

**Response (400 BAD REQUEST):**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/123",
  "details": {
    "phoneNumber": "Invalid phone number format"
  }
}
```

**Valid Phone Formats:**
- `+14155552671` (E.164 with +)
- `14155552671` (E.164 without +)
- `+442071234567` (UK)
- `+81312345678` (Japan)

**Invalid Formats:**
- `123-456-7890` (dashes)
- `(415) 555-2671` (parentheses)
- `415.555.2671` (dots)

---

### Scenario 10: Unauthorized Address Access

**Context**: User tries to access another user's address

**Request:**
```bash
# User 123 tries to access User 456's address
curl -X GET http://localhost:8082/api/users/456/addresses/10
```

**Processing Flow:**
1. Service queries: `WHERE user_id = 456 AND address_id = 10`
2. If address 10 belongs to user 123 (not 456), query returns empty
3. Throws ResourceNotFoundException
4. Returns 404 NOT FOUND

**Response (404 NOT FOUND):**
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Address not found with id: 10 for user: 456",
  "timestamp": "2024-01-17T10:35:00",
  "path": "/api/users/456/addresses/10"
}
```

**Security**: This prevents unauthorized access to other users' data

---

## 5. Request/Response Flow

### Complete Request Flow Diagram

```
┌─────────────┐
│   Client    │
│  (Browser/  │
│   Mobile)   │
└──────┬──────┘
       │
       │ HTTP Request
       │ GET /api/users/123
       ▼
┌─────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)               │
│  - Authentication                                        │
│  - Rate Limiting                                         │
│  - Request Routing                                       │
└──────┬──────────────────────────────────────────────────┘
       │
       │ Forward to user-service
       │ (via Eureka service discovery)
       ▼
┌─────────────────────────────────────────────────────────┐
│              User Service (Port 8082)                    │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  1. DispatcherServlet                          │    │
│  │     - Receives HTTP request                    │    │
│  │     - Maps to controller method                │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  2. UserController.getUserProfile()            │    │
│  │     - Extracts path variable (userId=123)      │    │
│  │     - Calls service layer                      │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  3. UserService.getUserProfile()               │    │
│  │     - Starts read-only transaction             │    │
│  │     - Calls repository                         │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  4. UserRepository.findById()                  │    │
│  │     - Spring Data JPA proxy                    │    │
│  │     - Generates SQL query                      │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
└───────────────┼──────────────────────────────────────────┘
                │
                │ SQL Query
                ▼
┌─────────────────────────────────────────────────────────┐
│           PostgreSQL Database                            │
│                                                          │
│  SELECT u.*, a.*                                        │
│  FROM users u                                           │
│  LEFT JOIN addresses a ON u.id = a.user_id             │
│  WHERE u.id = 123;                                      │
│                                                          │
└────────────┬────────────────────────────────────────────┘
             │
             │ Result Set
             ▼
┌─────────────────────────────────────────────────────────┐
│              User Service (Port 8082)                    │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  5. JPA Entity Hydration                       │    │
│  │     - Maps result set to User entity           │    │
│  │     - Populates Address collection             │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  6. UserMapper.toUserProfileResponse()         │    │
│  │     - Converts User entity to DTO              │    │
│  │     - Maps nested Address entities to DTOs     │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  7. Controller Returns ResponseEntity          │    │
│  │     - HTTP 200 OK                              │    │
│  │     - Content-Type: application/json           │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  8. Jackson JSON Serialization                 │    │
│  │     - Converts DTO to JSON                     │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
└───────────────┼──────────────────────────────────────────┘
                │
                │ HTTP Response
                ▼
┌─────────────────────────────────────────────────────────┐
│                    API Gateway                           │
│  - Adds response headers                                │
│  - Logs response                                        │
└──────┬──────────────────────────────────────────────────┘
       │
       │ JSON Response
       ▼
┌─────────────┐
│   Client    │
│  Receives   │
│  User Data  │
└─────────────┘
```

### Data Transformation Flow

```
Database Row → JPA Entity → DTO → JSON

┌─────────────────────────────────────────────────────────┐
│ Database (users table)                                  │
├─────────────────────────────────────────────────────────┤
│ id: 123                                                 │
│ email: "john@example.com"                               │
│ first_name: "John"                                      │
│ last_name: "Doe"                                        │
│ phone_number: "+14155552671"                            │
│ sms_notifications: true                                 │
│ email_notifications: true                               │
│ created_at: 2024-01-10 08:00:00                         │
└─────────────────────────────────────────────────────────┘
                    │
                    │ JPA Mapping
                    ▼
┌─────────────────────────────────────────────────────────┐
│ User Entity                                             │
├─────────────────────────────────────────────────────────┤
│ id: 123L                                                │
│ email: "john@example.com"                               │
│ firstName: "John"                                       │
│ lastName: "Doe"                                         │
│ phoneNumber: "+14155552671"                             │
│ preferences: UserPreferences {                          │
│   smsNotifications: true                                │
│   emailNotifications: true                              │
│ }                                                       │
│ addresses: List<Address> [...]                         │
│ createdAt: LocalDateTime                                │
└─────────────────────────────────────────────────────────┘
                    │
                    │ Mapper
                    ▼
┌─────────────────────────────────────────────────────────┐
│ UserProfileResponse DTO                                 │
├─────────────────────────────────────────────────────────┤
│ id: 123L                                                │
│ email: "john@example.com"                               │
│ firstName: "John"                                       │
│ lastName: "Doe"                                         │
│ phoneNumber: "+14155552671"                             │
│ preferences: UserPreferences {...}                      │
│ addresses: List<AddressResponse> [...]                 │
│ createdAt: LocalDateTime                                │
└─────────────────────────────────────────────────────────┘
                    │
                    │ Jackson Serialization
                    ▼
┌─────────────────────────────────────────────────────────┐
│ JSON Response                                           │
├─────────────────────────────────────────────────────────┤
│ {                                                       │
│   "id": 123,                                            │
│   "email": "john@example.com",                          │
│   "firstName": "John",                                  │
│   "lastName": "Doe",                                    │
│   "phoneNumber": "+14155552671",                        │
│   "preferences": {                                      │
│     "smsNotifications": true,                           │
│     "emailNotifications": true                          │
│   },                                                    │
│   "addresses": [...],                                   │
│   "createdAt": "2024-01-10T08:00:00"                    │
│ }                                                       │
└─────────────────────────────────────────────────────────┘
```

### Update Request Flow

```
Client → Controller → Service → Repository → Database

┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ PUT /api/users/123
       │ { "firstName": "Jane" }
       ▼
┌──────────────────────┐
│  UserController      │
│  - @Valid triggers   │
│  - Validation passes │
└──────┬───────────────┘
       │
       │ updateUserProfile(123, request)
       ▼
┌──────────────────────────────────────┐
│  UserService                         │
│  1. Start transaction                │
│  2. Fetch user (SELECT)              │
│  3. Update firstName                 │
│  4. Save user                        │
│  5. Commit transaction               │
└──────┬───────────────────────────────┘
       │
       │ findById(123)
       ▼
┌──────────────────────┐
│  UserRepository      │
│  - JPA query         │
└──────┬───────────────┘
       │
       │ SELECT * FROM users WHERE id = 123
       ▼
┌──────────────────────┐
│  PostgreSQL          │
│  - Returns user row  │
└──────┬───────────────┘
       │
       │ User entity
       ▼
┌──────────────────────────────────────┐
│  UserService                         │
│  - user.setFirstName("Jane")         │
│  - JPA dirty checking detects change │
└──────┬───────────────────────────────┘
       │
       │ save(user)
       ▼
┌──────────────────────┐
│  UserRepository      │
│  - Flush changes     │
└──────┬───────────────┘
       │
       │ UPDATE users SET first_name = 'Jane', 
       │                  updated_at = NOW()
       │ WHERE id = 123
       ▼
┌──────────────────────┐
│  PostgreSQL          │
│  - Executes UPDATE   │
│  - Returns success   │
└──────┬───────────────┘
       │
       │ Updated user
       ▼
┌──────────────────────┐
│  UserMapper          │
│  - Entity → DTO      │
└──────┬───────────────┘
       │
       │ UserProfileResponse
       ▼
┌──────────────────────┐
│  UserController      │
│  - Returns 200 OK    │
└──────┬───────────────┘
       │
       │ JSON response
       ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

### Error Handling Flow

```
Exception → GlobalExceptionHandler → Error Response

┌──────────────────────┐
│  Controller/Service  │
│  throws Exception    │
└──────┬───────────────┘
       │
       │ ResourceNotFoundException
       ▼
┌────────────────────────────────────────────┐
│  GlobalExceptionHandler                    │
│  @ExceptionHandler(ResourceNotFoundException)│
│  1. Catches exception                      │
│  2. Logs warning                           │
│  3. Creates ErrorResponse                  │
│  4. Returns 404 NOT FOUND                  │
└──────┬─────────────────────────────────────┘
       │
       │ ResponseEntity<ErrorResponse>
       ▼
┌──────────────────────┐
│  DispatcherServlet   │
│  - Serializes error  │
└──────┬───────────────┘
       │
       │ JSON error response
       ▼
┌─────────────┐
│   Client    │
│  Receives   │
│  404 error  │
└─────────────┘
```

---

## 6. Database Schema

### Entity-Relationship Diagram

```
┌─────────────────────────────────────────────┐
│                   users                     │
├─────────────────────────────────────────────┤
│ PK  id                    BIGINT            │
│ UK  email                 VARCHAR(255)      │
│ UK  phone_number          VARCHAR(255)      │
│     first_name            VARCHAR(255)      │
│     last_name             VARCHAR(255)      │
│     profile_image_url     VARCHAR(255)      │
│     sms_notifications     BOOLEAN           │
│     email_notifications   BOOLEAN           │
│     push_notifications    BOOLEAN           │
│     preferred_language    VARCHAR(255)      │
│     created_at            TIMESTAMP         │
│     updated_at            TIMESTAMP         │
└─────────────┬───────────────────────────────┘
              │
              │ 1:N
              │
              ▼
┌─────────────────────────────────────────────┐
│                 addresses                   │
├─────────────────────────────────────────────┤
│ PK  id                    BIGSERIAL         │
│ FK  user_id               BIGINT            │
│     address_line1         VARCHAR(255)      │
│     address_line2         VARCHAR(255)      │
│     city                  VARCHAR(255)      │
│     state                 VARCHAR(255)      │
│     zip_code              VARCHAR(255)      │
│     latitude              DOUBLE PRECISION  │
│     longitude             DOUBLE PRECISION  │
│     is_default            BOOLEAN           │
│     created_at            TIMESTAMP         │
│     updated_at            TIMESTAMP         │
└─────────────────────────────────────────────┘

Indexes:
- idx_addresses_user_id ON addresses(user_id)
- idx_addresses_default ON addresses(user_id, is_default)
```

### Table Definitions

#### users Table

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) UNIQUE,
    profile_image_url VARCHAR(255),
    sms_notifications BOOLEAN DEFAULT TRUE,
    email_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    preferred_language VARCHAR(255) DEFAULT 'en',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE UNIQUE INDEX idx_users_phone ON users(phone_number);
```

**Constraints:**
- **Primary Key**: `id` (not auto-generated, from Auth Service)
- **Unique Constraints**: `email`, `phone_number`
- **Not Null**: `id`, `email`, `first_name`, `last_name`, `created_at`, `updated_at`

**Sample Data:**
```sql
INSERT INTO users (id, email, first_name, last_name, phone_number, 
                   sms_notifications, email_notifications, push_notifications,
                   preferred_language, created_at, updated_at)
VALUES 
(1, 'john.doe@example.com', 'John', 'Doe', '+14155552671',
 true, true, false, 'en', '2024-01-10 08:00:00', '2024-01-10 08:00:00'),
(2, 'jane.smith@example.com', 'Jane', 'Smith', '+14155552672',
 true, false, true, 'es', '2024-01-11 09:00:00', '2024-01-11 09:00:00'),
(3, 'bob.johnson@example.com', 'Bob', 'Johnson', '+14155552673',
 false, true, true, 'en', '2024-01-12 10:00:00', '2024-01-12 10:00:00');
```

---

#### addresses Table

```sql
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    zip_code VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_default ON addresses(user_id, is_default);
```

**Constraints:**
- **Primary Key**: `id` (auto-generated)
- **Foreign Key**: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- **Not Null**: `user_id`, `address_line1`, `city`, `state`, `zip_code`, `latitude`, `longitude`, `created_at`, `updated_at`

**Cascade Delete**: When a user is deleted, all their addresses are automatically deleted

**Sample Data:**
```sql
INSERT INTO addresses (user_id, address_line1, address_line2, city, state, 
                       zip_code, latitude, longitude, is_default, 
                       created_at, updated_at)
VALUES 
-- User 1's addresses
(1, '123 Main St', 'Apt 4B', 'San Francisco', 'CA', '94102',
 37.7749, -122.4194, true, '2024-01-10 08:05:00', '2024-01-10 08:05:00'),
(1, '456 Oak Ave', NULL, 'Oakland', 'CA', '94601',
 37.8044, -122.2712, false, '2024-01-11 10:00:00', '2024-01-11 10:00:00'),

-- User 2's addresses
(2, '789 Pine St', 'Suite 200', 'Berkeley', 'CA', '94704',
 37.8715, -122.2730, true, '2024-01-11 09:05:00', '2024-01-11 09:05:00'),

-- User 3's addresses
(3, '321 Elm Dr', NULL, 'Palo Alto', 'CA', '94301',
 37.4419, -122.1430, true, '2024-01-12 10:05:00', '2024-01-12 10:05:00'),
(3, '654 Maple Ln', 'Unit 12', 'Mountain View', 'CA', '94040',
 37.3861, -122.0839, false, '2024-01-13 11:00:00', '2024-01-13 11:00:00');
```

---

### Common Queries

#### Query 1: Get User with All Addresses
```sql
SELECT u.*, a.*
FROM users u
LEFT JOIN addresses a ON u.id = a.user_id
WHERE u.id = 123;
```

**Result**: User row with all associated address rows

---

#### Query 2: Get User's Default Address
```sql
SELECT a.*
FROM addresses a
WHERE a.user_id = 123 AND a.is_default = true;
```

**Result**: Single address row (or empty if no default)

---

#### Query 3: Count Users by Notification Preference
```sql
SELECT 
    COUNT(*) FILTER (WHERE sms_notifications = true) AS sms_enabled,
    COUNT(*) FILTER (WHERE email_notifications = true) AS email_enabled,
    COUNT(*) FILTER (WHERE push_notifications = true) AS push_enabled
FROM users;
```

**Result**:
```
sms_enabled | email_enabled | push_enabled
------------|---------------|-------------
     2      |       2       |      2
```

---

#### Query 4: Find Users Without Addresses
```sql
SELECT u.*
FROM users u
LEFT JOIN addresses a ON u.id = a.user_id
WHERE a.id IS NULL;
```

**Result**: Users who haven't added any addresses

---

#### Query 5: Find Addresses Near Location (Within 10km)
```sql
SELECT a.*, u.first_name, u.last_name,
       (6371 * acos(cos(radians(37.7749)) * cos(radians(a.latitude)) * 
        cos(radians(a.longitude) - radians(-122.4194)) + 
        sin(radians(37.7749)) * sin(radians(a.latitude)))) AS distance_km
FROM addresses a
JOIN users u ON a.user_id = u.id
WHERE (6371 * acos(cos(radians(37.7749)) * cos(radians(a.latitude)) * 
       cos(radians(a.longitude) - radians(-122.4194)) + 
       sin(radians(37.7749)) * sin(radians(a.latitude)))) < 10
ORDER BY distance_km;
```

**Purpose**: Find delivery addresses near a restaurant location

---

### Database Indexes

**Purpose of Indexes:**
1. **idx_users_email**: Fast lookup by email (login, uniqueness check)
2. **idx_users_phone**: Fast lookup by phone (uniqueness check)
3. **idx_addresses_user_id**: Fast retrieval of user's addresses
4. **idx_addresses_default**: Fast lookup of default address

**Index Usage Examples:**
```sql
-- Uses idx_users_email
SELECT * FROM users WHERE email = 'john@example.com';

-- Uses idx_addresses_user_id
SELECT * FROM addresses WHERE user_id = 123;

-- Uses idx_addresses_default
SELECT * FROM addresses WHERE user_id = 123 AND is_default = true;
```

---

## 7. Integration Points

### 7.1 Service Discovery (Eureka)

**Registration:**
```yaml
# bootstrap.yml
spring:
  application:
    name: user-service  # Service name in Eureka
```

**Eureka Client Configuration:**
- Service registers at startup
- Sends heartbeats every 30 seconds
- Deregisters on shutdown

**Eureka Dashboard:**
```
http://localhost:8761/

Registered Services:
- USER-SERVICE (1 instance)
  - Status: UP
  - Instance ID: localhost:user-service:8082
  - Home Page: http://localhost:8082/
```

**Other Services Discover User Service:**
```java
// In Order Service
@Autowired
private RestTemplate restTemplate;

// Call user-service by name (not hardcoded URL)
String url = "http://user-service/api/users/" + userId;
UserProfileResponse user = restTemplate.getForObject(url, UserProfileResponse.class);
```

---

### 7.2 Integration with Auth Service

**Flow:**
1. User registers via Auth Service
2. Auth Service creates authentication record
3. Auth Service calls User Service to create profile
4. User Service stores user information

**Auth Service → User Service Call:**
```java
// In Auth Service
POST http://user-service/api/users
{
  "id": 123,  // Generated by Auth Service
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Note**: This is an internal endpoint, not exposed via API Gateway

---

### 7.3 Integration with Order Service

**Scenario**: Order Service needs user's default delivery address

**Order Service → User Service Call:**
```java
// In Order Service
GET http://user-service/api/users/123/addresses

// Filter for default address
List<AddressResponse> addresses = ...;
AddressResponse defaultAddress = addresses.stream()
    .filter(AddressResponse::getIsDefault)
    .findFirst()
    .orElseThrow(() -> new NoDefaultAddressException());
```

**Alternative**: User Service could expose dedicated endpoint
```java
GET http://user-service/api/users/123/addresses/default
```

---

### 7.4 Integration with Notification Service

**Scenario**: Notification Service checks user preferences before sending

**Notification Service → User Service Call:**
```java
// In Notification Service
GET http://user-service/api/users/123

UserProfileResponse user = ...;
if (user.getPreferences().getSmsNotifications()) {
    sendSMS(user.getPhoneNumber(), message);
}
if (user.getPreferences().getEmailNotifications()) {
    sendEmail(user.getEmail(), message);
}
```

---

### 7.5 API Gateway Integration

**Gateway Routes:**
```yaml
# API Gateway configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service  # Load-balanced via Eureka
          predicates:
            - Path=/api/users/**
          filters:
            - name: AuthenticationFilter  # Verify JWT token
            - name: RateLimiter  # Rate limiting
```

**Request Flow:**
```
Client → API Gateway → User Service
  |          |              |
  |          |-- Verify JWT token
  |          |-- Check rate limit
  |          |-- Route to user-service
  |          |
  |<---------|-- Forward request
  |                         |
  |<------------------------|-- Response
```

---

### 7.6 Event Publishing (Future Enhancement)

**Potential Events:**
- `UserProfileUpdated`: When user updates profile
- `AddressAdded`: When user adds new address
- `AddressDeleted`: When user deletes address

**Example with Spring Cloud Stream:**
```java
@Service
public class UserService {
    
    @Autowired
    private StreamBridge streamBridge;
    
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        // ... update logic ...
        
        // Publish event
        UserProfileUpdatedEvent event = new UserProfileUpdatedEvent(userId, user.getEmail());
        streamBridge.send("user-events", event);
        
        return response;
    }
}
```

**Consumers:**
- Notification Service: Send profile update confirmation
- Analytics Service: Track user activity
- Audit Service: Log profile changes

---

## 8. Error Handling

### Error Response Format

All errors follow a consistent format:

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/123",
  "details": {
    "field1": "error detail 1",
    "field2": "error detail 2"
  }
}
```

### Error Scenarios

#### 1. Resource Not Found (404)

**Scenario**: User or address doesn't exist

**Request:**
```bash
curl -X GET http://localhost:8082/api/users/999
```

**Response:**
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with id: 999",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/999"
}
```

**HTTP Status**: 404 NOT FOUND

**Causes:**
- User ID doesn't exist
- Address ID doesn't exist
- Address doesn't belong to specified user

---

#### 2. Validation Error (400)

**Scenario**: Invalid input data

**Request:**
```bash
curl -X PUT http://localhost:8082/api/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "invalid",
    "firstName": ""
  }'
```

**Response:**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/123",
  "details": {
    "phoneNumber": "Invalid phone number format",
    "firstName": "First name is required"
  }
}
```

**HTTP Status**: 400 BAD REQUEST

**Common Validation Errors:**
- Empty required fields
- Invalid phone number format
- Invalid email format
- Missing latitude/longitude

---

#### 3. Type Mismatch (400)

**Scenario**: Wrong parameter type

**Request:**
```bash
curl -X GET http://localhost:8082/api/users/abc
```

**Response:**
```json
{
  "errorCode": "INVALID_PARAMETER",
  "message": "Invalid value 'abc' for parameter 'userId'. Expected type: Long",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/abc"
}
```

**HTTP Status**: 400 BAD REQUEST

**Causes:**
- String instead of number in path variable
- Invalid boolean value
- Malformed JSON

---

#### 4. Data Integrity Violation (409)

**Scenario**: Unique constraint violation

**Request:**
```bash
# Try to update phone number to one that already exists
curl -X PUT http://localhost:8082/api/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+14155552672"
  }'
```

**Response:**
```json
{
  "errorCode": "DATA_INTEGRITY_VIOLATION",
  "message": "A record with the provided information already exists",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/123"
}
```

**HTTP Status**: 409 CONFLICT

**Causes:**
- Duplicate email
- Duplicate phone number
- Foreign key constraint violation

---

#### 5. Internal Server Error (500)

**Scenario**: Unexpected error

**Response:**
```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please try again later.",
  "timestamp": "2024-01-17T10:30:00",
  "path": "/api/users/123"
}
```

**HTTP Status**: 500 INTERNAL SERVER ERROR

**Causes:**
- Database connection failure
- Null pointer exception
- Unhandled runtime exception

**Note**: Full stack trace is logged server-side but not exposed to client

---

### Error Handling Best Practices

**1. Logging Strategy:**
```java
// Expected errors: WARN level
log.warn("Resource not found: {}", ex.getMessage());

// Unexpected errors: ERROR level with stack trace
log.error("Unexpected error occurred: ", ex);
```

**2. Client-Friendly Messages:**
- Don't expose internal details
- Provide actionable information
- Include error codes for support

**3. Field-Level Validation:**
```json
{
  "details": {
    "phoneNumber": "Invalid phone number format",
    "city": "City is required"
  }
}
```

**4. Consistent Format:**
- All errors use same structure
- Easy for clients to parse
- Includes timestamp and path for debugging

---

## 9. Configuration Reference

### application.yml (Base Configuration)

```yaml
# Minimal local configuration
# Most configuration is fetched from Config Server via bootstrap.yml

spring:
  cloud:
    config:
      enabled: true  # Enable Config Server integration

# Local fallback values (used only if Config Server is unavailable)
server:
  port: 8082  # Service port

logging:
  level:
    com.fooddelivery.user: INFO  # Application logging level
```

**Purpose**: Provides fallback configuration if Config Server is unavailable

---

### bootstrap.yml (Bootstrap Configuration)

```yaml
spring:
  application:
    name: user-service  # Service name (used for Eureka registration)
  
  cloud:
    config:
      uri: http://localhost:8888  # Config Server URL
      fail-fast: true  # Fail startup if Config Server unavailable
      retry:
        max-attempts: 6  # Retry 6 times
        initial-interval: 1000  # Wait 1 second initially
        multiplier: 1.1  # Increase wait time by 10% each retry
  
  profiles:
    active: dev  # Default profile (dev, prod, test)
```

**Purpose**: Loaded before application.yml, configures Config Server connection

**Retry Logic:**
- Attempt 1: Wait 1000ms
- Attempt 2: Wait 1100ms
- Attempt 3: Wait 1210ms
- Attempt 4: Wait 1331ms
- Attempt 5: Wait 1464ms
- Attempt 6: Wait 1610ms

---

### application-dev.yml (Development Profile)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_service_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update schema
    show-sql: true  # Log SQL statements
    properties:
      hibernate:
        format_sql: true  # Format SQL for readability
        use_sql_comments: true  # Add comments to SQL

logging:
  level:
    com.fooddelivery.user: DEBUG  # Verbose application logging
    org.springframework.web: DEBUG  # Spring web logging
    org.hibernate.SQL: DEBUG  # SQL logging
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # Parameter logging
```

**Purpose**: Development-friendly configuration with verbose logging

**Features:**
- Auto-creates database schema
- Logs all SQL statements
- Detailed logging for debugging

---

### application-prod.yml (Production Profile)

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://postgres:5432/user_service_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20  # Connection pool size
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate  # Only validate schema (don't modify)
    show-sql: false  # Don't log SQL
    properties:
      hibernate:
        jdbc:
          batch_size: 20  # Batch inserts/updates
        order_inserts: true
        order_updates: true

logging:
  level:
    com.fooddelivery.user: INFO  # Standard logging
    org.springframework.web: WARN  # Minimal Spring logging
    org.hibernate.SQL: WARN  # No SQL logging
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  
  file:
    name: /var/log/user-service/application.log
    max-size: 10MB
    max-history: 30
```

**Purpose**: Production-optimized configuration

**Features:**
- Environment variable configuration
- Connection pooling
- Batch operations
- Minimal logging
- Log file rotation

---

### Environment Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `DB_URL` | Database JDBC URL | `jdbc:postgresql://postgres:5432/user_service_db` | `jdbc:postgresql://prod-db:5432/users` |
| `DB_USERNAME` | Database username | `postgres` | `user_service_app` |
| `DB_PASSWORD` | Database password | `postgres` | `SecurePassword123!` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` | `prod` |
| `SERVER_PORT` | Service port | `8082` | `8082` |
| `EUREKA_URI` | Eureka server URL | `http://localhost:8761/eureka/` | `http://eureka:8761/eureka/` |

**Setting Environment Variables:**

**Linux/Mac:**
```bash
export DB_URL=jdbc:postgresql://prod-db:5432/users
export DB_USERNAME=user_service_app
export DB_PASSWORD=SecurePassword123!
export SPRING_PROFILES_ACTIVE=prod
```

**Windows:**
```cmd
set DB_URL=jdbc:postgresql://prod-db:5432/users
set DB_USERNAME=user_service_app
set DB_PASSWORD=SecurePassword123!
set SPRING_PROFILES_ACTIVE=prod
```

**Docker:**
```bash
docker run -e DB_URL=jdbc:postgresql://prod-db:5432/users \
           -e DB_USERNAME=user_service_app \
           -e DB_PASSWORD=SecurePassword123! \
           -e SPRING_PROFILES_ACTIVE=prod \
           user-service:latest
```

---

### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine  # Lightweight Java 17 runtime

WORKDIR /app  # Set working directory

COPY target/user-service-*.jar app.jar  # Copy JAR file

EXPOSE 8082  # Expose service port

# Health check configuration
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]  # Start application
```

**Build Image:**
```bash
# Build JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t user-service:latest .
```

**Run Container:**
```bash
docker run -d \
  --name user-service \
  -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/user_service_db \
  user-service:latest
```

---

### Kubernetes Deployment

**Key Features:**
- 3 replicas for high availability
- Horizontal Pod Autoscaler (3-10 pods)
- Liveness and readiness probes
- Resource limits
- ConfigMap for configuration
- Secret for database credentials

**Deploy to Kubernetes:**
```bash
# Create namespace
kubectl create namespace food-delivery

# Apply deployment
kubectl apply -f k8s-deployment.yml

# Check status
kubectl get pods -n food-delivery
kubectl get svc -n food-delivery

# View logs
kubectl logs -f deployment/user-service -n food-delivery
```

**Scale Manually:**
```bash
kubectl scale deployment user-service --replicas=5 -n food-delivery
```

**HPA Auto-Scaling:**
- Min replicas: 3
- Max replicas: 10
- Scale up when CPU > 70% or Memory > 80%

---

## 10. Testing Guide

### Manual Testing with curl

#### Test 1: Get User Profile
```bash
curl -X GET http://localhost:8082/api/users/1 | jq
```

**Expected**: 200 OK with user profile

---

#### Test 2: Update User Profile
```bash
curl -X PUT http://localhost:8082/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "phoneNumber": "+14155559999"
  }' | jq
```

**Expected**: 200 OK with updated profile

---

#### Test 3: Create Address
```bash
curl -X POST http://localhost:8082/api/users/1/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine1": "123 Test St",
    "city": "Test City",
    "state": "CA",
    "zipCode": "12345",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "isDefault": true
  }' | jq
```

**Expected**: 201 CREATED with new address

---

#### Test 4: Get All Addresses
```bash
curl -X GET http://localhost:8082/api/users/1/addresses | jq
```

**Expected**: 200 OK with address list

---

#### Test 5: Update Address
```bash
curl -X PUT http://localhost:8082/api/users/1/addresses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine2": "Apt 10"
  }' | jq
```

**Expected**: 200 OK with updated address

---

#### Test 6: Delete Address
```bash
curl -X DELETE http://localhost:8082/api/users/1/addresses/1 | jq
```

**Expected**: 200 OK with success message

---

#### Test 7: Validation Error
```bash
curl -X POST http://localhost:8082/api/users/1/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressLine1": "123 Test St"
  }' | jq
```

**Expected**: 400 BAD REQUEST with validation errors

---

#### Test 8: Resource Not Found
```bash
curl -X GET http://localhost:8082/api/users/999 | jq
```

**Expected**: 404 NOT FOUND

---

### Testing with Postman

**Import Collection:**
1. Create new collection "User Service"
2. Add requests for each endpoint
3. Set base URL variable: `{{baseUrl}} = http://localhost:8082`

**Example Request:**
```
GET {{baseUrl}}/api/users/1
```

**Tests Script:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has user data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('email');
    pm.expect(jsonData).to.have.property('firstName');
});
```

---

### Unit Testing Example

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUserProfile_Success() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setId(123L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");

        when(userService.getUserProfile(123L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getUserProfile_NotFound() throws Exception {
        // Arrange
        when(userService.getUserProfile(999L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
```

---

### Integration Testing Example

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.yml")
class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createAndRetrieveUser() {
        // Create user
        User user = new User(1L, "test@example.com", "John", "Doe");
        userRepository.save(user);

        // Retrieve user
        ResponseEntity<UserProfileResponse> response = 
            restTemplate.getForEntity("/api/users/1", UserProfileResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }
}
```

---

## 11. Performance & Monitoring

### Expected Performance Metrics

| Operation | Target | Typical | Notes |
|-----------|--------|---------|-------|
| Get User Profile | < 200ms | 50-100ms | Includes addresses |
| Update User Profile | < 500ms | 100-200ms | Database write |
| Get Addresses | < 150ms | 30-80ms | List query |
| Create Address | < 300ms | 80-150ms | With default logic |
| Update Address | < 300ms | 80-150ms | Partial update |
| Delete Address | < 200ms | 50-100ms | Simple delete |

### Actuator Endpoints

**Health Check:**
```bash
curl http://localhost:8082/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
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
    }
  }
}
```

---

**Metrics:**
```bash
curl http://localhost:8082/actuator/metrics
```

**Specific Metric:**
```bash
curl http://localhost:8082/actuator/metrics/http.server.requests
```

---

**Info:**
```bash
curl http://localhost:8082/actuator/info
```

---

### Key Metrics to Monitor

**1. HTTP Request Metrics:**
- Request count
- Response times (p50, p95, p99)
- Error rates
- Status code distribution

**2. Database Metrics:**
- Connection pool usage
- Query execution time
- Transaction count
- Deadlocks

**3. JVM Metrics:**
- Heap memory usage
- Garbage collection time
- Thread count
- CPU usage

**4. Business Metrics:**
- User registrations per hour
- Address additions per day
- Profile updates per hour
- Error rate by endpoint

---

### Logging Examples

**INFO Level:**
```
2024-01-17 10:30:00 - User profile retrieved: userId=123
2024-01-17 10:30:05 - User profile updated: userId=123
2024-01-17 10:30:10 - Address created: userId=123, addressId=1
```

**WARN Level:**
```
2024-01-17 10:30:15 - Resource not found: User not found with id: 999
2024-01-17 10:30:20 - Validation failed: {phoneNumber=Invalid phone number format}
```

**ERROR Level:**
```
2024-01-17 10:30:25 - Data integrity violation: Duplicate phone number
2024-01-17 10:30:30 - Unexpected error occurred: NullPointerException
  at com.fooddelivery.user.service.UserService.updateUserProfile(UserService.java:45)
  ...
```

---

## 12. Troubleshooting Guide

### Common Issues

#### Issue 1: Service Won't Start

**Symptoms:**
- Application fails to start
- Error: "Failed to configure a DataSource"

**Causes:**
- Database not running
- Wrong database credentials
- Database doesn't exist

**Solutions:**
```bash
# Check if PostgreSQL is running
psql -U postgres -h localhost

# Create database
CREATE DATABASE user_service_db;

# Verify connection
psql -U postgres -h localhost -d user_service_db

# Check application-dev.yml credentials
```

---

#### Issue 2: Eureka Registration Failed

**Symptoms:**
- Service starts but not visible in Eureka dashboard
- Error: "Connection refused: localhost:8761"

**Causes:**
- Eureka server not running
- Wrong Eureka URL

**Solutions:**
```bash
# Start Eureka server first
cd service-registry
mvn spring-boot:run

# Verify Eureka is running
curl http://localhost:8761/

# Check bootstrap.yml Eureka configuration
```

---

#### Issue 3: Validation Errors

**Symptoms:**
- 400 BAD REQUEST
- Validation error messages

**Causes:**
- Invalid phone number format
- Missing required fields
- Invalid data types

**Solutions:**
```bash
# Use correct phone format (E.164)
# Valid: +14155552671, 14155552671
# Invalid: 415-555-2671, (415) 555-2671

# Include all required fields
# Address: addressLine1, city, state, zipCode, latitude, longitude
```

---

#### Issue 4: Duplicate Key Errors

**Symptoms:**
- 409 CONFLICT
- "A record with the provided information already exists"

**Causes:**
- Duplicate email
- Duplicate phone number

**Solutions:**
```bash
# Check if email/phone already exists
SELECT * FROM users WHERE email = 'test@example.com';
SELECT * FROM users WHERE phone_number = '+14155552671';

# Use different email/phone number
```

---

#### Issue 5: Slow Queries

**Symptoms:**
- Requests taking > 1 second
- Database connection pool exhausted

**Causes:**
- Missing indexes
- N+1 query problem
- Large result sets

**Solutions:**
```sql
# Add indexes
CREATE INDEX idx_addresses_user_id ON addresses(user_id);

# Use pagination for large lists
GET /api/users/123/addresses?page=0&size=20

# Enable query logging to identify slow queries
spring.jpa.show-sql=true
```

---

### Debug Techniques

**1. Enable Debug Logging:**
```yaml
logging:
  level:
    com.fooddelivery.user: DEBUG
    org.hibernate.SQL: DEBUG
```

**2. Check Actuator Health:**
```bash
curl http://localhost:8082/actuator/health
```

**3. View Application Logs:**
```bash
# Docker
docker logs user-service

# Kubernetes
kubectl logs deployment/user-service -n food-delivery

# Local
tail -f logs/application.log
```

**4. Database Query Analysis:**
```sql
# Check active connections
SELECT * FROM pg_stat_activity WHERE datname = 'user_service_db';

# Check slow queries
SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;
```

---

## 13. FAQ Section

### Q1: Why is the User ID not auto-generated?

**A**: The User ID comes from the Auth Service to maintain consistency across services. When a user registers:
1. Auth Service creates authentication record with auto-generated ID
2. Auth Service calls User Service with same ID
3. Both services use same ID for the user

This ensures referential integrity across the platform.

---

### Q2: How does the default address logic work?

**A**: Only one address can be default per user. When setting an address as default:
1. Service fetches all user's addresses
2. Sets `isDefault = false` for all addresses
3. Sets `isDefault = true` for the new default address
4. Saves all changes in a transaction

This ensures atomicity and consistency.

---

### Q3: Why use DTOs instead of returning entities directly?

**A**: DTOs provide several benefits:
- **Security**: Don't expose internal entity structure
- **Flexibility**: Can combine data from multiple entities
- **Versioning**: Can support multiple API versions
- **Performance**: Include only needed fields
- **Decoupling**: API contract independent of database schema

---

### Q4: How are partial updates handled?

**A**: The service checks each field in the request:
```java
if (request.getFirstName() != null) {
    user.setFirstName(request.getFirstName());
}
```

Only non-null fields are updated. This allows clients to update specific fields without sending the entire object.

---

### Q5: What happens when a user is deleted?

**A**: Due to `ON DELETE CASCADE` in the foreign key:
```sql
user_id BIGINT REFERENCES users(id) ON DELETE CASCADE
```

When a user is deleted, all their addresses are automatically deleted. This maintains referential integrity.

---

### Q6: How is phone number validation implemented?

**A**: Using regex pattern for E.164 format:
```java
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
```

**Valid formats:**
- `+14155552671` (with country code)
- `14155552671` (without + symbol)

**Invalid formats:**
- `415-555-2671` (dashes)
- `(415) 555-2671` (parentheses)

---

### Q7: Why use `@Transactional(readOnly = true)` for queries?

**A**: Read-only transactions provide optimizations:
- No flush to database (better performance)
- Database can optimize query execution
- Prevents accidental modifications
- Clearer intent in code

---

### Q8: How does service discovery work?

**A**: Using Netflix Eureka:
1. User Service registers with Eureka at startup
2. Sends heartbeats every 30 seconds
3. Other services query Eureka for "user-service"
4. Eureka returns available instances
5. Client-side load balancing distributes requests

---

### Q9: What's the difference between dev and prod profiles?

**A**:

| Feature | Dev | Prod |
|---------|-----|------|
| Schema Management | Auto-update | Validate only |
| SQL Logging | Enabled | Disabled |
| Log Level | DEBUG | INFO |
| Connection Pool | Small | Large |
| Error Details | Verbose | Minimal |

---

### Q10: How to add a new field to User entity?

**A**:
1. Add field to `User` entity
2. Add field to `UserProfileResponse` DTO
3. Add field to `UpdateUserProfileRequest` DTO (if updatable)
4. Update `UserMapper` to map new field
5. Update service layer to handle new field
6. Run with `ddl-auto: update` to add column (dev)
7. Create migration script for production

---

### Q11: How to handle concurrent default address updates?

**A**: The current implementation uses database-level locking through transactions. For high concurrency:

**Option 1**: Optimistic Locking
```java
@Entity
public class Address {
    @Version
    private Long version;
    // ...
}
```

**Option 2**: Pessimistic Locking
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Address> findByUserIdAndAddressId(Long userId, Long addressId);
```

---

### Q12: How to implement pagination for addresses?

**A**:
```java
// Repository
Page<Address> findByUserId(Long userId, Pageable pageable);

// Service
public Page<AddressResponse> getUserAddresses(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Address> addresses = addressRepository.findByUserId(userId, pageable);
    return addresses.map(addressMapper::toAddressResponse);
}

// Controller
@GetMapping
public ResponseEntity<Page<AddressResponse>> getUserAddresses(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(addressService.getUserAddresses(userId, page, size));
}
```

---

## Conclusion

This documentation provides a comprehensive guide to the User Service, covering:
- Architecture and design decisions
- Complete code analysis with explanations
- Real-world usage scenarios
- Database schema and queries
- Integration with other services
- Error handling strategies
- Configuration for different environments
- Testing approaches
- Performance monitoring
- Troubleshooting common issues

The User Service is a critical component of the Food Delivery Platform, providing reliable user profile and address management with strong data consistency guarantees and excellent performance characteristics.

For questions or issues, refer to the troubleshooting section or contact the development team.

---

**Document Version**: 1.0  
**Last Updated**: January 17, 2024  
**Service Version**: 1.0.0-SNAPSHOT
