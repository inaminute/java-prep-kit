# Restaurant Service - Detailed Documentation

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

The Restaurant Service is the catalog and menu management backbone of the Food Delivery Platform. It solves several critical business problems:

1. **Restaurant Discovery**: Enables users to find restaurants near their location using geospatial search
2. **Menu Management**: Provides comprehensive menu item management with dietary information
3. **Search & Filtering**: Allows users to search by cuisine type, dietary preferences, and ratings
4. **Performance Optimization**: Uses Redis caching to ensure sub-300ms response times for searches
5. **Data Consistency**: Maintains restaurant and menu data integrity with proper validation

### How Does It Fit in the Overall System?

```
┌─────────────────────────────────────────────────────────────┐
│                     Food Delivery Platform                   │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  API Gateway (Port 8080)                                     │
│         │                                                     │
│         ├──> User Service (8081)                            │
│         ├──> Order Service (8082)                           │
│         ├──> Restaurant Service (8083) ◄── YOU ARE HERE     │
│         ├──> Delivery Service (8084)                        │
│         └──> Payment Service (8085)                         │
│                                                               │
│  Service Registry (Eureka - 8761)                           │
│  Config Server (8888)                                        │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

**Key Interactions:**
- **Order Service** → Queries restaurant and menu item details when creating orders
- **API Gateway** → Routes all `/api/restaurants/**` requests to this service
- **Service Registry** → Registers itself for service discovery
- **Config Server** → Fetches centralized configuration at startup


### Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.x | Core application framework |
| **Language** | Java | 17+ | Programming language |
| **Database** | PostgreSQL | 15+ | Persistent data storage |
| **Cache** | Redis | 6+ | Performance optimization |
| **Service Discovery** | Netflix Eureka Client | Latest | Service registration |
| **Configuration** | Spring Cloud Config | Latest | Centralized config management |
| **API Documentation** | SpringDoc OpenAPI | Latest | Auto-generated API docs |
| **Validation** | Jakarta Validation | Latest | Request validation |
| **Build Tool** | Maven | 3.6+ | Dependency & build management |

---

## 2. Complete File Analysis

### Project Structure

```
restaurant-service/
├── src/main/java/com/fooddelivery/restaurant/
│   ├── RestaurantServiceApplication.java      # Main application entry point
│   ├── config/
│   │   └── RedisConfig.java                   # Redis cache configuration
│   ├── controller/
│   │   ├── RestaurantController.java          # Public restaurant endpoints
│   │   ├── MenuController.java                # Public menu endpoints
│   │   └── RestaurantAdminController.java     # Admin management endpoints
│   ├── dto/
│   │   ├── RestaurantResponse.java            # Restaurant response DTO
│   │   ├── MenuItemResponse.java              # Menu item response DTO
│   │   ├── CreateRestaurantRequest.java       # Create restaurant request
│   │   ├── UpdateRestaurantRequest.java       # Update restaurant request
│   │   ├── CreateMenuItemRequest.java         # Create menu item request
│   │   ├── UpdateMenuItemRequest.java         # Update menu item request
│   │   ├── AddressDto.java                    # Address data transfer object
│   │   └── OperatingHoursDto.java             # Operating hours DTO
│   ├── entity/
│   │   ├── Restaurant.java                    # Restaurant JPA entity
│   │   ├── MenuItem.java                      # Menu item JPA entity
│   │   ├── Address.java                       # Embeddable address
│   │   └── OperatingHours.java                # Embeddable operating hours
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java        # Centralized error handling
│   │   └── ResourceNotFoundException.java     # Custom exception
│   ├── mapper/
│   │   ├── RestaurantMapper.java              # Entity to DTO mapper
│   │   └── MenuItemMapper.java                # Menu item mapper
│   ├── repository/
│   │   ├── RestaurantRepository.java          # Restaurant data access
│   │   └── MenuItemRepository.java            # Menu item data access
│   └── service/
│       ├── RestaurantService.java             # Restaurant business logic
│       ├── MenuService.java                   # Menu business logic
│       └── RestaurantAdminService.java        # Admin operations
├── src/main/resources/
│   ├── application.yml                        # Local configuration
│   └── bootstrap.yml                          # Config server connection
└── pom.xml                                    # Maven dependencies
```


### File-by-File Analysis

#### 1. RestaurantServiceApplication.java

**Purpose**: Main entry point for the Spring Boot application

**Code Explanation**:
```java
@SpringBootApplication          // Line 1: Enables auto-configuration, component scanning
@EnableDiscoveryClient          // Line 2: Registers with Eureka service registry
@EnableCaching                  // Line 3: Enables Spring's caching abstraction
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
```

**Key Points**:
- `@SpringBootApplication`: Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`
- `@EnableDiscoveryClient`: Makes this service discoverable by other services via Eureka
- `@EnableCaching`: Activates Redis caching for performance optimization
- On startup, this service:
  1. Connects to Config Server (via bootstrap.yml)
  2. Registers with Eureka Service Registry
  3. Initializes Redis cache manager
  4. Starts embedded Tomcat server on port 8083

---

#### 2. Entity Classes

##### Restaurant.java

**Purpose**: JPA entity representing a restaurant with all its attributes

**Database Mapping**:
```java
@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_restaurant_active", columnList = "isActive"),    // Fast active restaurant queries
    @Index(name = "idx_restaurant_cuisine", columnList = "cuisineType"), // Fast cuisine filtering
    @Index(name = "idx_restaurant_rating", columnList = "rating")        // Fast rating sorting
})
```

**Key Fields**:
- `id` (Long): Primary key, auto-generated
- `name` (String, max 200): Restaurant name
- `description` (String, max 1000): Detailed description
- `address` (Address): Embedded address with lat/long for geospatial search
- `cuisineType` (String, max 100): Type of cuisine (Italian, Chinese, etc.)
- `rating` (BigDecimal, 3,2): Average rating (0.00 to 5.00)
- `totalReviews` (Integer): Number of reviews received
- `operatingHours` (List<OperatingHours>): Weekly schedule
- `isActive` (Boolean): Whether restaurant is currently accepting orders
- `menuItems` (List<MenuItem>): One-to-many relationship with menu items
- `minimumOrderAmount` (BigDecimal): Minimum order value required
- `deliveryFee` (BigDecimal): Delivery charge
- `averageDeliveryTimeMinutes` (Integer): Expected delivery time
- `createdAt`, `updatedAt` (LocalDateTime): Audit timestamps

**Lifecycle Callbacks**:
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();  // Automatically updates timestamp on every save
}
```


##### MenuItem.java

**Purpose**: JPA entity representing a menu item in a restaurant

**Database Mapping**:
```java
@Entity
@Table(name = "menu_items", indexes = {
    @Index(name = "idx_menu_item_restaurant", columnList = "restaurant_id"),  // Fast restaurant menu queries
    @Index(name = "idx_menu_item_category", columnList = "category"),         // Fast category filtering
    @Index(name = "idx_menu_item_available", columnList = "isAvailable")      // Fast availability checks
})
```

**Key Fields**:
- `id` (Long): Primary key
- `restaurant` (Restaurant): Many-to-one relationship with Restaurant
- `name` (String, max 200): Menu item name
- `description` (String, max 1000): Item description
- `price` (BigDecimal, 10,2): Item price
- `category` (String, max 100): Category (Appetizer, Main Course, Dessert, etc.)
- `isVegetarian`, `isVegan`, `isGlutenFree` (Boolean): Dietary flags
- `isAvailable` (Boolean): Current availability status
- `ingredients` (String, max 500): List of ingredients
- `allergens` (String, max 200): Allergen information
- `calories` (Integer): Nutritional information
- `preparationTimeMinutes` (Integer): Expected prep time

**Relationship**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "restaurant_id", nullable = false)
private Restaurant restaurant;
```
- Uses LAZY loading to avoid fetching restaurant data unnecessarily
- Foreign key constraint ensures referential integrity

---

##### Address.java (Embeddable)

**Purpose**: Embeddable component for restaurant location

**Key Fields**:
```java
@Embeddable
public class Address {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Double latitude;   // Critical for geospatial search
    private Double longitude;  // Critical for geospatial search
}
```

**Why Embeddable?**
- Address is not a separate entity; it's part of Restaurant
- No separate table; columns are embedded in `restaurants` table
- Simplifies queries and improves performance

---

##### OperatingHours.java (Embeddable)

**Purpose**: Represents restaurant operating schedule for each day

**Key Fields**:
```java
@Embeddable
public class OperatingHours {
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;  // MONDAY, TUESDAY, etc.
    private LocalTime openTime;    // e.g., 09:00
    private LocalTime closeTime;   // e.g., 22:00
    private Boolean isClosed;      // True if restaurant is closed that day
}
```

**Storage**:
- Stored in separate table `operating_hours` with foreign key to `restaurants`
- Uses `@ElementCollection` in Restaurant entity
- Allows multiple operating hours per restaurant (one per day of week)


#### 3. Repository Classes

##### RestaurantRepository.java

**Purpose**: Data access layer for Restaurant entity with custom geospatial queries

**Key Methods**:

1. **findByIsActiveTrue()**: Returns all active restaurants
   ```java
   List<Restaurant> findByIsActiveTrue();
   ```

2. **findRestaurantsWithinRadius()**: Geospatial search using Haversine formula
   ```java
   @Query(value = "SELECT r.* FROM restaurants r " +
          "WHERE r.is_active = true " +
          "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * " +
          "cos(radians(r.longitude) - radians(:longitude)) + " +
          "sin(radians(:latitude)) * sin(radians(r.latitude)))) <= :radiusKm " +
          "ORDER BY distance",
          nativeQuery = true)
   List<Restaurant> findRestaurantsWithinRadius(
       @Param("latitude") Double latitude,
       @Param("longitude") Double longitude,
       @Param("radiusKm") Double radiusKm
   );
   ```
   
   **Haversine Formula Explanation**:
   - Calculates great-circle distance between two points on Earth
   - `6371` = Earth's radius in kilometers
   - `radians()` converts degrees to radians
   - `acos()` calculates arc cosine
   - Result is distance in kilometers
   - Filters restaurants within specified radius
   - Orders by distance (closest first)

3. **searchByName()**: Case-insensitive name search
   ```java
   @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
          "AND r.isActive = true")
   List<Restaurant> searchByName(@Param("searchTerm") String searchTerm);
   ```

4. **findTopRatedRestaurants()**: Get highly-rated restaurants
   ```java
   @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.rating >= :minRating " +
          "ORDER BY r.rating DESC, r.totalReviews DESC")
   List<Restaurant> findTopRatedRestaurants(@Param("minRating") Double minRating);
   ```

5. **findByCuisineTypeWithinRadius()**: Combined cuisine and location filter
   ```java
   // Native query combining cuisine filter with Haversine distance calculation
   ```

**Performance Considerations**:
- Uses database indexes on `isActive`, `cuisineType`, and `rating`
- Native queries for complex geospatial calculations
- Results are cached in Redis to reduce database load

---

##### MenuItemRepository.java

**Purpose**: Data access layer for MenuItem entity with dietary filtering

**Key Methods**:

1. **findByRestaurantIdAndIsAvailableTrue()**: Get available menu items
   ```java
   List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
   ```

2. **findWithDietaryFilters()**: Advanced filtering by dietary preferences
   ```java
   @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId " +
          "AND m.isAvailable = true " +
          "AND (:isVegetarian IS NULL OR m.isVegetarian = :isVegetarian) " +
          "AND (:isVegan IS NULL OR m.isVegan = :isVegan) " +
          "AND (:isGlutenFree IS NULL OR m.isGlutenFree = :isGlutenFree)")
   List<MenuItem> findWithDietaryFilters(
       @Param("restaurantId") Long restaurantId,
       @Param("isVegetarian") Boolean isVegetarian,
       @Param("isVegan") Boolean isVegan,
       @Param("isGlutenFree") Boolean isGlutenFree
   );
   ```
   - Null parameters are ignored (flexible filtering)
   - Can combine multiple dietary filters

3. **searchByNameInRestaurant()**: Search menu items by name
   ```java
   @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId " +
          "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
          "AND m.isAvailable = true")
   List<MenuItem> searchByNameInRestaurant(
       @Param("restaurantId") Long restaurantId,
       @Param("searchTerm") String searchTerm
   );
   ```


#### 4. Service Classes

##### RestaurantService.java

**Purpose**: Business logic for restaurant catalog and search operations

**Key Methods with Detailed Explanation**:

1. **getRestaurantById()**:
   ```java
   @Transactional(readOnly = true)
   @Cacheable(value = "restaurants", key = "#restaurantId")
   public RestaurantResponse getRestaurantById(Long restaurantId) {
       log.info("Fetching restaurant with id: {}", restaurantId);
       
       Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
               .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

       return restaurantMapper.toRestaurantResponse(restaurant);
   }
   ```
   
   **Flow**:
   1. Check Redis cache for key `restaurants::restaurantId`
   2. If cache hit → return cached response (fast!)
   3. If cache miss → query database
   4. Map entity to DTO
   5. Store in cache for 15 minutes
   6. Return response
   
   **Performance**: First call ~50ms (DB), subsequent calls ~5ms (cache)

2. **searchRestaurantsNearby()**: Location-based search with performance tracking
   ```java
   @Transactional(readOnly = true)
   @Cacheable(value = "restaurantSearch", key = "#latitude + '_' + #longitude + '_' + #radiusKm")
   public List<RestaurantResponse> searchRestaurantsNearby(Double latitude, Double longitude, Double radiusKm) {
       long startTime = System.currentTimeMillis();
       
       if (radiusKm == null || radiusKm <= 0) {
           radiusKm = defaultRadiusKm;  // Default: 10km
       }

       log.info("Searching restaurants within {}km of location ({}, {})", radiusKm, latitude, longitude);

       List<Restaurant> restaurants = restaurantRepository.findRestaurantsWithinRadius(
               latitude, longitude, radiusKm);

       List<RestaurantResponse> responses = restaurants.stream()
               .map(restaurantMapper::toRestaurantResponse)
               .limit(maxResults)  // Default: 50 results max
               .collect(Collectors.toList());

       long duration = System.currentTimeMillis() - startTime;
       log.info("Found {} restaurants in {}ms", responses.size(), duration);

       return responses;
   }
   ```
   
   **Performance Target**: < 300ms (requirement)
   - First call: ~200ms (DB query with Haversine calculation)
   - Cached calls: ~5ms
   - Cache TTL: 5 minutes (more dynamic than restaurant details)

3. **searchByName()**: Text-based search
   ```java
   @Transactional(readOnly = true)
   public List<RestaurantResponse> searchByName(String searchTerm) {
       if (searchTerm == null || searchTerm.trim().isEmpty()) {
           throw new IllegalArgumentException("Search term cannot be empty");
       }

       List<Restaurant> restaurants = restaurantRepository.searchByName(searchTerm);

       return restaurants.stream()
               .map(restaurantMapper::toRestaurantResponse)
               .limit(maxResults)
               .collect(Collectors.toList());
   }
   ```

**Configuration Properties**:
```java
@Value("${restaurant.search.default-radius-km:10}")
private Double defaultRadiusKm;  // Default search radius

@Value("${restaurant.search.max-results:50}")
private Integer maxResults;  // Maximum results to return
```


##### MenuService.java

**Purpose**: Business logic for menu operations

**Key Methods**:

1. **getRestaurantMenu()**: Get all available menu items
   ```java
   @Transactional(readOnly = true)
   @Cacheable(value = "menuItems", key = "#restaurantId")
   public List<MenuItemResponse> getRestaurantMenu(Long restaurantId) {
       // Verify restaurant exists
       restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
               .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

       List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);

       return menuItems.stream()
               .map(menuItemMapper::toMenuItemResponse)
               .collect(Collectors.toList());
   }
   ```
   - Cache TTL: 10 minutes
   - Only returns available items

2. **getMenuWithDietaryFilters()**: Advanced filtering
   ```java
   @Cacheable(value = "menuItems", 
              key = "#restaurantId + '_diet_' + #isVegetarian + '_' + #isVegan + '_' + #isGlutenFree")
   public List<MenuItemResponse> getMenuWithDietaryFilters(Long restaurantId, Boolean isVegetarian, 
                                                            Boolean isVegan, Boolean isGlutenFree) {
       List<MenuItem> menuItems = menuItemRepository.findWithDietaryFilters(
               restaurantId, isVegetarian, isVegan, isGlutenFree);

       return menuItems.stream()
               .map(menuItemMapper::toMenuItemResponse)
               .collect(Collectors.toList());
   }
   ```
   - Separate cache key for each filter combination
   - Example cache keys:
     - `menuItems::1_diet_true_null_null` (vegetarian only)
     - `menuItems::1_diet_null_true_true` (vegan and gluten-free)

---

##### RestaurantAdminService.java

**Purpose**: Admin operations for restaurant and menu management

**Key Methods**:

1. **createRestaurant()**: Create new restaurant
   ```java
   @Transactional
   @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
   public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
       Restaurant restaurant = new Restaurant();
       // Set all fields from request
       restaurant.setName(request.getName());
       restaurant.setAddress(toAddress(request.getAddress()));
       // ... more fields
       
       restaurant = restaurantRepository.save(restaurant);
       return restaurantMapper.toRestaurantResponse(restaurant);
   }
   ```
   - `@CacheEvict`: Clears all restaurant caches after creation
   - Ensures users see new restaurant immediately

2. **updateRestaurant()**: Update existing restaurant
   ```java
   @Transactional
   @CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
   public RestaurantResponse updateRestaurant(Long restaurantId, UpdateRestaurantRequest request) {
       Restaurant restaurant = restaurantRepository.findById(restaurantId)
               .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

       // Update only non-null fields (partial update)
       if (request.getName() != null) {
           restaurant.setName(request.getName());
       }
       // ... more fields
       
       restaurant = restaurantRepository.save(restaurant);
       return restaurantMapper.toRestaurantResponse(restaurant);
   }
   ```
   - Supports partial updates (only provided fields are updated)
   - Clears cache to ensure consistency

3. **addMenuItem()**: Add menu item to restaurant
   ```java
   @Transactional
   @CacheEvict(value = "menuItems", allEntries = true)
   public MenuItemResponse addMenuItem(Long restaurantId, CreateMenuItemRequest request) {
       Restaurant restaurant = restaurantRepository.findById(restaurantId)
               .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

       MenuItem menuItem = new MenuItem();
       menuItem.setRestaurant(restaurant);
       menuItem.setName(request.getName());
       // ... set all fields
       
       menuItem = menuItemRepository.save(menuItem);
       return menuItemMapper.toMenuItemResponse(menuItem);
   }
   ```
   - Clears menu cache after adding item
   - Validates restaurant exists before adding item


#### 5. Controller Classes

##### RestaurantController.java

**Purpose**: Public REST API for restaurant catalog and search

**Endpoints**:

| Method | Endpoint | Description | Cache |
|--------|----------|-------------|-------|
| GET | `/api/restaurants/{id}` | Get restaurant details | 15 min |
| GET | `/api/restaurants/search/nearby` | Location-based search | 5 min |
| GET | `/api/restaurants/search` | Search by name | No |
| GET | `/api/restaurants/search/cuisine` | Search by cuisine + location | 5 min |
| GET | `/api/restaurants/top-rated` | Get top-rated restaurants | 5 min |
| GET | `/api/restaurants` | Get all active restaurants | No |
| GET | `/api/restaurants/cuisine/{type}` | Get by cuisine type | 5 min |

**Example Endpoint**:
```java
@GetMapping("/search/nearby")
@Operation(summary = "Search restaurants nearby", 
           description = "Find restaurants within specified radius from user location. " +
                       "Returns results within 300ms as per requirements.")
public ResponseEntity<List<RestaurantResponse>> searchNearby(
        @Parameter(description = "User latitude") @RequestParam Double latitude,
        @Parameter(description = "User longitude") @RequestParam Double longitude,
        @Parameter(description = "Search radius in kilometers (default: 10km)") 
        @RequestParam(required = false) Double radiusKm) {
    List<RestaurantResponse> responses = restaurantService.searchRestaurantsNearby(
            latitude, longitude, radiusKm);
    return ResponseEntity.ok(responses);
}
```

**OpenAPI Annotations**:
- `@Tag`: Groups endpoints in Swagger UI
- `@Operation`: Describes endpoint purpose
- `@Parameter`: Documents request parameters

---

##### MenuController.java

**Purpose**: Public REST API for menu operations

**Endpoints**:

| Method | Endpoint | Description | Cache |
|--------|----------|-------------|-------|
| GET | `/api/restaurants/{id}/menu` | Get full menu | 10 min |
| GET | `/api/restaurants/{id}/menu/category/{cat}` | Get by category | 10 min |
| GET | `/api/restaurants/{id}/menu/filter` | Filter by dietary prefs | 10 min |
| GET | `/api/restaurants/{id}/menu/search` | Search menu items | No |
| GET | `/api/restaurants/{id}/menu/vegetarian` | Get vegetarian items | 10 min |
| GET | `/api/restaurants/{id}/menu/vegan` | Get vegan items | 10 min |
| GET | `/api/restaurants/{id}/menu/gluten-free` | Get gluten-free items | 10 min |
| GET | `/api/restaurants/{id}/menu/items/{itemId}` | Get specific item | No |

**Example Endpoint**:
```java
@GetMapping("/filter")
@Operation(summary = "Filter menu by dietary preferences")
public ResponseEntity<List<MenuItemResponse>> getMenuWithFilters(
        @PathVariable Long restaurantId,
        @RequestParam(required = false) Boolean vegetarian,
        @RequestParam(required = false) Boolean vegan,
        @RequestParam(required = false) Boolean glutenFree) {
    List<MenuItemResponse> menu = menuService.getMenuWithDietaryFilters(
            restaurantId, vegetarian, vegan, glutenFree);
    return ResponseEntity.ok(menu);
}
```

---

##### RestaurantAdminController.java

**Purpose**: Admin REST API for restaurant management

**Endpoints**:

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/admin/restaurants` | Create restaurant | RESTAURANT_ADMIN |
| PUT | `/api/admin/restaurants/{id}` | Update restaurant | RESTAURANT_ADMIN |
| POST | `/api/admin/restaurants/{id}/items` | Add menu item | RESTAURANT_ADMIN |
| PUT | `/api/admin/restaurants/{id}/items/{itemId}` | Update menu item | RESTAURANT_ADMIN |
| DELETE | `/api/admin/restaurants/{id}/items/{itemId}` | Delete menu item | RESTAURANT_ADMIN |

**Note**: In production, these endpoints should be protected with Spring Security and require `RESTAURANT_ADMIN` role.


#### 6. Configuration Classes

##### RedisConfig.java

**Purpose**: Configure Redis caching with different TTL for different cache types

**Key Configuration**:

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // Default TTL
            .serializeKeysWith(StringRedisSerializer)
            .serializeValuesWith(GenericJackson2JsonRedisSerializer)
            .disableCachingNullValues();  // Don't cache null results

        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Restaurant cache - 15 minutes TTL (less frequently updated)
        cacheConfigurations.put("restaurants", 
            defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Menu items cache - 10 minutes TTL
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
}
```

**Cache Strategy Rationale**:
- **restaurants (15 min)**: Restaurant details change infrequently
- **menuItems (10 min)**: Menu items may be updated more often (availability, price)
- **restaurantSearch (5 min)**: Search results should be relatively fresh

**Serialization**:
- Keys: String serialization (simple, readable in Redis CLI)
- Values: JSON serialization (preserves object structure, human-readable)

---

#### 7. Exception Handling

##### GlobalExceptionHandler.java

**Purpose**: Centralized exception handling with consistent error responses

**Handled Exceptions**:

1. **ResourceNotFoundException** → 404 NOT FOUND
   ```java
   @ExceptionHandler(ResourceNotFoundException.class)
   public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
           ResourceNotFoundException ex, WebRequest request) {
       ErrorResponse error = new ErrorResponse(
               "RESOURCE_NOT_FOUND",
               ex.getMessage(),
               LocalDateTime.now(),
               request.getDescription(false).replace("uri=", "")
       );
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
   }
   ```

2. **MethodArgumentNotValidException** → 400 BAD REQUEST
   ```java
   // Handles @Valid validation failures
   // Returns field-level validation errors
   ```

3. **IllegalArgumentException** → 400 BAD REQUEST
   ```java
   // Handles business logic validation failures
   // Example: Empty search term
   ```

4. **DataIntegrityViolationException** → 409 CONFLICT
   ```java
   // Handles database constraint violations
   // Example: Duplicate restaurant name
   ```

5. **Exception** → 500 INTERNAL SERVER ERROR
   ```java
   // Catches all unexpected errors
   // Logs full stack trace for debugging
   ```

**Error Response Format**:
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Restaurant not found with id: 999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/restaurants/999",
  "details": {}
}
```


---

## 3. Component Deep Dive

### Controllers Summary

**RestaurantController** (Public API):
- 7 endpoints for restaurant search and discovery
- Supports location-based, name-based, and cuisine-based search
- All responses cached for performance
- OpenAPI documentation enabled

**MenuController** (Public API):
- 8 endpoints for menu browsing
- Dietary filtering (vegetarian, vegan, gluten-free)
- Category-based filtering
- Search functionality

**RestaurantAdminController** (Admin API):
- 5 endpoints for CRUD operations
- Restaurant creation and updates
- Menu item management
- Cache invalidation on updates

### Services Summary

**RestaurantService**:
- Core business logic for restaurant operations
- Implements geospatial search with Haversine formula
- Performance monitoring (logs query duration)
- Configurable search radius and result limits

**MenuService**:
- Menu retrieval and filtering logic
- Dietary preference handling
- Cache management for menu data

**RestaurantAdminService**:
- Admin operations with validation
- Partial update support
- Cache invalidation strategies

### Repositories Summary

**RestaurantRepository**:
- 7 custom query methods
- Native SQL for geospatial calculations
- JPQL for text search and filtering
- Optimized with database indexes

**MenuItemRepository**:
- 9 custom query methods
- Flexible dietary filtering
- Category and availability filtering

### DTOs Summary

**Request DTOs**:
- `CreateRestaurantRequest`: Full validation with Jakarta Validation
- `UpdateRestaurantRequest`: Partial update support (nullable fields)
- `CreateMenuItemRequest`: Menu item creation with validation
- `UpdateMenuItemRequest`: Partial menu item updates

**Response DTOs**:
- `RestaurantResponse`: Complete restaurant information
- `MenuItemResponse`: Menu item details
- `AddressDto`: Address information
- `OperatingHoursDto`: Operating schedule

### Mappers Summary

**RestaurantMapper**:
- Entity → DTO conversion
- Handles nested objects (Address, OperatingHours)
- Null-safe mapping

**MenuItemMapper**:
- MenuItem entity → MenuItemResponse DTO
- Extracts restaurant ID from relationship

---

## 4. Scenario-Based Examples

### Scenario 1: User Searches for Nearby Restaurants

**Use Case**: User at location (40.7128, -74.0060) wants to find restaurants within 5km

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/search/nearby?latitude=40.7128&longitude=-74.0060&radiusKm=5" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at RestaurantController.searchNearby()
2. Parameters extracted: lat=40.7128, lon=-74.0060, radius=5
3. Check Redis cache with key: "restaurantSearch::40.7128_-74.0060_5.0"
4. Cache MISS (first request)
5. Call RestaurantService.searchRestaurantsNearby()
6. Start performance timer
7. Query database with Haversine formula:
   - Calculate distance from user location to each restaurant
   - Filter restaurants where distance <= 5km
   - Order by distance (closest first)
8. Database returns 12 restaurants
9. Map entities to DTOs (12 Restaurant → 12 RestaurantResponse)
10. Limit to maxResults (50, so all 12 returned)
11. Log: "Found 12 restaurants in 187ms"
12. Store result in Redis cache (TTL: 5 minutes)
13. Return HTTP 200 with JSON array
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Pizza Paradise",
    "description": "Authentic Italian pizza with wood-fired oven",
    "address": {
      "addressLine1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "latitude": 40.7129,
      "longitude": -74.0059
    },
    "cuisineType": "Italian",
    "rating": 4.5,
    "totalReviews": 234,
    "operatingHours": [
      {
        "dayOfWeek": "MONDAY",
        "openTime": "11:00:00",
        "closeTime": "22:00:00",
        "isClosed": false
      }
    ],
    "isActive": true,
    "imageUrl": "https://example.com/pizza-paradise.jpg",
    "minimumOrderAmount": 15.00,
    "deliveryFee": 3.99,
    "averageDeliveryTimeMinutes": 30,
    "distanceKm": 0.15
  },
  {
    "id": 2,
    "name": "Sushi Express",
    "description": "Fresh sushi and Japanese cuisine",
    "address": {
      "addressLine1": "456 Oak Ave",
      "city": "New York",
      "state": "NY",
      "zipCode": "10002",
      "latitude": 40.7135,
      "longitude": -74.0065
    },
    "cuisineType": "Japanese",
    "rating": 4.7,
    "totalReviews": 189,
    "isActive": true,
    "minimumOrderAmount": 20.00,
    "deliveryFee": 4.99,
    "averageDeliveryTimeMinutes": 35,
    "distanceKm": 0.82
  }
]
```

**Subsequent Request** (within 5 minutes):
- Cache HIT
- Response time: ~5ms
- No database query


### Scenario 2: Get Restaurant Details

**Use Case**: User wants to view details of a specific restaurant

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/1" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at RestaurantController.getRestaurant(1)
2. Check Redis cache with key: "restaurants::1"
3. Cache MISS
4. Call RestaurantService.getRestaurantById(1)
5. Query: SELECT * FROM restaurants WHERE id = 1 AND is_active = true
6. Restaurant found
7. Map Restaurant entity to RestaurantResponse DTO
8. Store in Redis cache (TTL: 15 minutes)
9. Return HTTP 200 with JSON
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Pizza Paradise",
  "description": "Authentic Italian pizza with wood-fired oven. Family-owned since 1985.",
  "address": {
    "addressLine1": "123 Main St",
    "addressLine2": "Suite 100",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "latitude": 40.7129,
    "longitude": -74.0059
  },
  "cuisineType": "Italian",
  "rating": 4.5,
  "totalReviews": 234,
  "operatingHours": [
    {
      "dayOfWeek": "MONDAY",
      "openTime": "11:00:00",
      "closeTime": "22:00:00",
      "isClosed": false
    },
    {
      "dayOfWeek": "TUESDAY",
      "openTime": "11:00:00",
      "closeTime": "22:00:00",
      "isClosed": false
    },
    {
      "dayOfWeek": "SUNDAY",
      "openTime": "12:00:00",
      "closeTime": "21:00:00",
      "isClosed": false
    }
  ],
  "isActive": true,
  "imageUrl": "https://example.com/pizza-paradise.jpg",
  "minimumOrderAmount": 15.00,
  "deliveryFee": 3.99,
  "averageDeliveryTimeMinutes": 30
}
```

**Error Case - Restaurant Not Found**:

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/999" \
  -H "Accept: application/json"
```

**Response** (404 NOT FOUND):
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Restaurant not found with id: 999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/restaurants/999"
}
```

---

### Scenario 3: Get Restaurant Menu

**Use Case**: User wants to browse the menu of a restaurant

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/1/menu" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at MenuController.getMenu(1)
2. Check Redis cache with key: "menuItems::1"
3. Cache MISS
4. Call MenuService.getRestaurantMenu(1)
5. Verify restaurant exists (throws exception if not found)
6. Query: SELECT * FROM menu_items WHERE restaurant_id = 1 AND is_available = true
7. Found 15 menu items
8. Map each MenuItem to MenuItemResponse
9. Store in Redis cache (TTL: 10 minutes)
10. Return HTTP 200 with JSON array
```

**Response** (200 OK):
```json
[
  {
    "id": 101,
    "restaurantId": 1,
    "name": "Margherita Pizza",
    "description": "Classic pizza with tomato sauce, mozzarella, and fresh basil",
    "price": 12.99,
    "category": "Pizza",
    "imageUrl": "https://example.com/margherita.jpg",
    "isVegetarian": true,
    "isVegan": false,
    "isGlutenFree": false,
    "isAvailable": true,
    "ingredients": "Tomato sauce, mozzarella cheese, fresh basil, olive oil",
    "allergens": "Dairy, Gluten",
    "calories": 850,
    "preparationTimeMinutes": 15
  },
  {
    "id": 102,
    "restaurantId": 1,
    "name": "Pepperoni Pizza",
    "description": "Classic pepperoni pizza with extra cheese",
    "price": 14.99,
    "category": "Pizza",
    "imageUrl": "https://example.com/pepperoni.jpg",
    "isVegetarian": false,
    "isVegan": false,
    "isGlutenFree": false,
    "isAvailable": true,
    "ingredients": "Tomato sauce, mozzarella cheese, pepperoni",
    "allergens": "Dairy, Gluten, Pork",
    "calories": 1050,
    "preparationTimeMinutes": 15
  },
  {
    "id": 103,
    "restaurantId": 1,
    "name": "Caesar Salad",
    "description": "Fresh romaine lettuce with Caesar dressing and croutons",
    "price": 8.99,
    "category": "Salad",
    "imageUrl": "https://example.com/caesar-salad.jpg",
    "isVegetarian": true,
    "isVegan": false,
    "isGlutenFree": false,
    "isAvailable": true,
    "ingredients": "Romaine lettuce, Caesar dressing, parmesan, croutons",
    "allergens": "Dairy, Gluten, Eggs",
    "calories": 320,
    "preparationTimeMinutes": 5
  }
]
```


### Scenario 4: Filter Menu by Dietary Preferences

**Use Case**: Vegan user wants to see only vegan menu items

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/1/menu/filter?vegan=true" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at MenuController.getMenuWithFilters(1, null, true, null)
2. Check Redis cache with key: "menuItems::1_diet_null_true_null"
3. Cache MISS
4. Call MenuService.getMenuWithDietaryFilters(1, null, true, null)
5. Query: SELECT * FROM menu_items WHERE restaurant_id = 1 
         AND is_available = true AND is_vegan = true
6. Found 3 vegan items
7. Map to DTOs
8. Store in cache (TTL: 10 minutes)
9. Return HTTP 200
```

**Response** (200 OK):
```json
[
  {
    "id": 105,
    "restaurantId": 1,
    "name": "Vegan Marinara Pizza",
    "description": "Pizza with tomato sauce, vegetables, no cheese",
    "price": 13.99,
    "category": "Pizza",
    "isVegetarian": true,
    "isVegan": true,
    "isGlutenFree": false,
    "isAvailable": true,
    "ingredients": "Tomato sauce, bell peppers, mushrooms, olives, onions",
    "allergens": "Gluten",
    "calories": 720,
    "preparationTimeMinutes": 15
  },
  {
    "id": 108,
    "restaurantId": 1,
    "name": "Garden Salad",
    "description": "Fresh mixed greens with balsamic vinaigrette",
    "price": 7.99,
    "category": "Salad",
    "isVegetarian": true,
    "isVegan": true,
    "isGlutenFree": true,
    "isAvailable": true,
    "ingredients": "Mixed greens, tomatoes, cucumbers, carrots, balsamic vinaigrette",
    "allergens": "None",
    "calories": 180,
    "preparationTimeMinutes": 5
  }
]
```

---

### Scenario 5: Search Restaurants by Cuisine Type Near Location

**Use Case**: User wants Italian restaurants within 10km

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/search/cuisine?cuisineType=Italian&latitude=40.7128&longitude=-74.0060&radiusKm=10" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at RestaurantController.searchByCuisineNearby()
2. Parameters: cuisineType="Italian", lat=40.7128, lon=-74.0060, radius=10
3. Check Redis cache: "restaurantSearch::Italian_40.7128_-74.0060_10.0"
4. Cache MISS
5. Call RestaurantService.searchByCuisineNearby()
6. Native SQL query with Haversine formula + cuisine filter
7. Found 5 Italian restaurants within 10km
8. Map to DTOs, order by distance
9. Cache result (TTL: 5 minutes)
10. Return HTTP 200
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Pizza Paradise",
    "cuisineType": "Italian",
    "rating": 4.5,
    "totalReviews": 234,
    "minimumOrderAmount": 15.00,
    "deliveryFee": 3.99,
    "averageDeliveryTimeMinutes": 30,
    "distanceKm": 0.15
  },
  {
    "id": 7,
    "name": "Pasta House",
    "cuisineType": "Italian",
    "rating": 4.3,
    "totalReviews": 156,
    "minimumOrderAmount": 18.00,
    "deliveryFee": 4.99,
    "averageDeliveryTimeMinutes": 35,
    "distanceKm": 2.8
  }
]
```

---

### Scenario 6: Search Restaurants by Name

**Use Case**: User searches for "pizza"

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/search?query=pizza" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at RestaurantController.searchByName("pizza")
2. Call RestaurantService.searchByName("pizza")
3. Validate search term is not empty
4. Query: SELECT * FROM restaurants WHERE LOWER(name) LIKE '%pizza%' AND is_active = true
5. Found 3 restaurants
6. Map to DTOs
7. Return HTTP 200
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Pizza Paradise",
    "cuisineType": "Italian",
    "rating": 4.5
  },
  {
    "id": 15,
    "name": "New York Pizza Co",
    "cuisineType": "American",
    "rating": 4.2
  },
  {
    "id": 23,
    "name": "Chicago Deep Dish Pizza",
    "cuisineType": "American",
    "rating": 4.6
  }
]
```

**Error Case - Empty Search Term**:

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/search?query=" \
  -H "Accept: application/json"
```

**Response** (400 BAD REQUEST):
```json
{
  "errorCode": "INVALID_ARGUMENT",
  "message": "Search term cannot be empty",
  "timestamp": "2024-01-15T10:35:00",
  "path": "/api/restaurants/search"
}
```


### Scenario 7: Admin Creates New Restaurant

**Use Case**: Restaurant admin adds a new restaurant to the platform

**Request**:
```bash
curl -X POST "http://localhost:8083/api/admin/restaurants" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "name": "Burger King",
    "description": "Fast food restaurant serving burgers and fries",
    "address": {
      "addressLine1": "789 Broadway",
      "city": "New York",
      "state": "NY",
      "zipCode": "10003",
      "country": "USA",
      "latitude": 40.7300,
      "longitude": -73.9950
    },
    "cuisineType": "American",
    "operatingHours": [
      {
        "dayOfWeek": "MONDAY",
        "openTime": "08:00:00",
        "closeTime": "23:00:00",
        "isClosed": false
      },
      {
        "dayOfWeek": "TUESDAY",
        "openTime": "08:00:00",
        "closeTime": "23:00:00",
        "isClosed": false
      }
    ],
    "imageUrl": "https://example.com/burger-king.jpg",
    "minimumOrderAmount": 10.00,
    "deliveryFee": 2.99,
    "averageDeliveryTimeMinutes": 25
  }'
```

**Processing Flow**:
```
1. Request arrives at RestaurantAdminController.createRestaurant()
2. @Valid annotation triggers validation
3. All validations pass
4. Call RestaurantAdminService.createRestaurant()
5. Create new Restaurant entity
6. Set all fields from request
7. Set isActive = true (default)
8. Save to database: INSERT INTO restaurants...
9. Database generates ID (e.g., 50)
10. @CacheEvict clears all "restaurants" and "restaurantSearch" caches
11. Map entity to RestaurantResponse
12. Return HTTP 201 CREATED
```

**Response** (201 CREATED):
```json
{
  "id": 50,
  "name": "Burger King",
  "description": "Fast food restaurant serving burgers and fries",
  "address": {
    "addressLine1": "789 Broadway",
    "city": "New York",
    "state": "NY",
    "zipCode": "10003",
    "country": "USA",
    "latitude": 40.7300,
    "longitude": -73.9950
  },
  "cuisineType": "American",
  "rating": null,
  "totalReviews": 0,
  "operatingHours": [
    {
      "dayOfWeek": "MONDAY",
      "openTime": "08:00:00",
      "closeTime": "23:00:00",
      "isClosed": false
    },
    {
      "dayOfWeek": "TUESDAY",
      "openTime": "08:00:00",
      "closeTime": "23:00:00",
      "isClosed": false
    }
  ],
  "isActive": true,
  "imageUrl": "https://example.com/burger-king.jpg",
  "minimumOrderAmount": 10.00,
  "deliveryFee": 2.99,
  "averageDeliveryTimeMinutes": 25
}
```

**Error Case - Validation Failure**:

**Request** (missing required field):
```bash
curl -X POST "http://localhost:8083/api/admin/restaurants" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "A restaurant",
    "cuisineType": "Italian"
  }'
```

**Response** (400 BAD REQUEST):
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-15T10:40:00",
  "path": "/api/admin/restaurants",
  "details": {
    "name": "Restaurant name is required",
    "address": "Address is required"
  }
}
```

---

### Scenario 8: Admin Updates Restaurant

**Use Case**: Admin updates restaurant delivery fee and operating hours

**Request**:
```bash
curl -X PUT "http://localhost:8083/api/admin/restaurants/1" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "deliveryFee": 4.99,
    "operatingHours": [
      {
        "dayOfWeek": "MONDAY",
        "openTime": "10:00:00",
        "closeTime": "23:00:00",
        "isClosed": false
      }
    ]
  }'
```

**Processing Flow**:
```
1. Request arrives at RestaurantAdminController.updateRestaurant(1, request)
2. Validation passes (partial update, only provided fields validated)
3. Call RestaurantAdminService.updateRestaurant(1, request)
4. Query: SELECT * FROM restaurants WHERE id = 1
5. Restaurant found
6. Update only non-null fields:
   - deliveryFee = 4.99
   - operatingHours = new list
   - Other fields unchanged
7. Save: UPDATE restaurants SET delivery_fee = 4.99, updated_at = NOW() WHERE id = 1
8. @CacheEvict clears all restaurant caches
9. Map to DTO
10. Return HTTP 200
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Pizza Paradise",
  "description": "Authentic Italian pizza with wood-fired oven",
  "cuisineType": "Italian",
  "rating": 4.5,
  "totalReviews": 234,
  "operatingHours": [
    {
      "dayOfWeek": "MONDAY",
      "openTime": "10:00:00",
      "closeTime": "23:00:00",
      "isClosed": false
    }
  ],
  "isActive": true,
  "minimumOrderAmount": 15.00,
  "deliveryFee": 4.99,
  "averageDeliveryTimeMinutes": 30
}
```


### Scenario 9: Admin Adds Menu Item

**Use Case**: Admin adds a new menu item to a restaurant

**Request**:
```bash
curl -X POST "http://localhost:8083/api/admin/restaurants/1/items" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "name": "Tiramisu",
    "description": "Classic Italian dessert with coffee and mascarpone",
    "price": 6.99,
    "category": "Dessert",
    "imageUrl": "https://example.com/tiramisu.jpg",
    "isVegetarian": true,
    "isVegan": false,
    "isGlutenFree": false,
    "isAvailable": true,
    "ingredients": "Mascarpone cheese, ladyfingers, espresso, cocoa powder",
    "allergens": "Dairy, Gluten, Eggs",
    "calories": 450,
    "preparationTimeMinutes": 10
  }'
```

**Processing Flow**:
```
1. Request arrives at RestaurantAdminController.addMenuItem(1, request)
2. Validation passes
3. Call RestaurantAdminService.addMenuItem(1, request)
4. Verify restaurant exists: SELECT * FROM restaurants WHERE id = 1
5. Restaurant found
6. Create new MenuItem entity
7. Set restaurant relationship
8. Set all fields from request
9. Save: INSERT INTO menu_items...
10. Database generates ID (e.g., 150)
11. @CacheEvict clears all "menuItems" caches
12. Map to DTO
13. Return HTTP 201 CREATED
```

**Response** (201 CREATED):
```json
{
  "id": 150,
  "restaurantId": 1,
  "name": "Tiramisu",
  "description": "Classic Italian dessert with coffee and mascarpone",
  "price": 6.99,
  "category": "Dessert",
  "imageUrl": "https://example.com/tiramisu.jpg",
  "isVegetarian": true,
  "isVegan": false,
  "isGlutenFree": false,
  "isAvailable": true,
  "ingredients": "Mascarpone cheese, ladyfingers, espresso, cocoa powder",
  "allergens": "Dairy, Gluten, Eggs",
  "calories": 450,
  "preparationTimeMinutes": 10
}
```

---

### Scenario 10: Admin Deletes Menu Item

**Use Case**: Admin removes an unavailable menu item

**Request**:
```bash
curl -X DELETE "http://localhost:8083/api/admin/restaurants/1/items/150" \
  -H "Accept: application/json"
```

**Processing Flow**:
```
1. Request arrives at RestaurantAdminController.deleteMenuItem(1, 150)
2. Call RestaurantAdminService.deleteMenuItem(1, 150)
3. Query: SELECT * FROM menu_items WHERE id = 150 AND restaurant_id = 1
4. Menu item found
5. Delete: DELETE FROM menu_items WHERE id = 150
6. @CacheEvict clears all "menuItems" caches
7. Return HTTP 200 with success message
```

**Response** (200 OK):
```json
{
  "message": "Menu item deleted successfully"
}
```

**Error Case - Menu Item Not Found**:

**Request**:
```bash
curl -X DELETE "http://localhost:8083/api/admin/restaurants/1/items/999" \
  -H "Accept: application/json"
```

**Response** (404 NOT FOUND):
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Menu item not found with id: 999 for restaurant: 1",
  "timestamp": "2024-01-15T10:45:00",
  "path": "/api/admin/restaurants/1/items/999"
}
```

---

## 5. Request/Response Flow

### Complete Flow Diagram: Search Nearby Restaurants

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │ GET /api/restaurants/search/nearby?lat=40.7128&lon=-74.0060&radiusKm=5
       ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                      │
│  - Route resolution                                          │
│  - Load balancing                                            │
│  - Forward to restaurant-service instance                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Restaurant Service (8083)                       │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │  RestaurantController.searchNearby()               │    │
│  │  - Extract parameters                              │    │
│  │  - Validate inputs                                 │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│                   ▼                                          │
│  ┌────────────────────────────────────────────────────┐    │
│  │  RestaurantService.searchRestaurantsNearby()       │    │
│  │  - Check cache                                     │    │
│  │  - Apply default radius if needed                  │    │
│  │  - Start performance timer                         │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│                   ▼                                          │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Redis Cache Check                                 │    │
│  │  Key: "restaurantSearch::40.7128_-74.0060_5.0"   │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│         ┌─────────┴─────────┐                               │
│         │                   │                               │
│    Cache HIT          Cache MISS                            │
│         │                   │                               │
│         │                   ▼                               │
│         │  ┌────────────────────────────────────────┐      │
│         │  │  RestaurantRepository                  │      │
│         │  │  .findRestaurantsWithinRadius()        │      │
│         │  └────────────────┬───────────────────────┘      │
│         │                   │                               │
│         │                   ▼                               │
│         │  ┌────────────────────────────────────────┐      │
│         │  │  PostgreSQL Database                   │      │
│         │  │  - Execute Haversine formula query     │      │
│         │  │  - Calculate distances                 │      │
│         │  │  - Filter by radius                    │      │
│         │  │  - Order by distance                   │      │
│         │  └────────────────┬───────────────────────┘      │
│         │                   │                               │
│         │                   ▼                               │
│         │  ┌────────────────────────────────────────┐      │
│         │  │  RestaurantMapper.toRestaurantResponse()│     │
│         │  │  - Convert entities to DTOs            │      │
│         │  │  - Map nested objects                  │      │
│         │  └────────────────┬───────────────────────┘      │
│         │                   │                               │
│         │                   ▼                               │
│         │  ┌────────────────────────────────────────┐      │
│         │  │  Store in Redis Cache                  │      │
│         │  │  TTL: 5 minutes                        │      │
│         │  └────────────────┬───────────────────────┘      │
│         │                   │                               │
│         └───────────────────┘                               │
│                   │                                          │
│                   ▼                                          │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Log Performance                                   │    │
│  │  "Found 12 restaurants in 187ms"                   │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
└───────────────────┼──────────────────────────────────────────┘
                    │
                    ▼
            ┌───────────────┐
            │  HTTP 200 OK  │
            │  JSON Array   │
            └───────────────┘
```


### Data Transformation Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Database Layer                            │
│                                                              │
│  Restaurant Entity (JPA)                                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ id: 1                                                 │  │
│  │ name: "Pizza Paradise"                                │  │
│  │ address: Address {                                    │  │
│  │   addressLine1: "123 Main St"                        │  │
│  │   latitude: 40.7129                                   │  │
│  │   longitude: -74.0059                                 │  │
│  │ }                                                     │  │
│  │ operatingHours: List<OperatingHours> [...]           │  │
│  │ menuItems: List<MenuItem> [...]                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          │ RestaurantMapper.toRestaurantResponse()
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│                                                              │
│  RestaurantResponse DTO                                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ id: 1                                                 │  │
│  │ name: "Pizza Paradise"                                │  │
│  │ address: AddressDto {                                 │  │
│  │   addressLine1: "123 Main St"                        │  │
│  │   latitude: 40.7129                                   │  │
│  │   longitude: -74.0059                                 │  │
│  │ }                                                     │  │
│  │ operatingHours: List<OperatingHoursDto> [...]        │  │
│  │ distanceKm: 0.15  (calculated)                       │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          │ Jackson JSON Serialization
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Response                             │
│                                                              │
│  JSON                                                        │
│  {                                                           │
│    "id": 1,                                                  │
│    "name": "Pizza Paradise",                                 │
│    "address": {                                              │
│      "addressLine1": "123 Main St",                         │
│      "latitude": 40.7129,                                    │
│      "longitude": -74.0059                                   │
│    },                                                        │
│    "operatingHours": [...],                                  │
│    "distanceKm": 0.15                                        │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Database Schema

### Tables

#### 1. restaurants

```sql
CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    
    -- Address (embedded)
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    
    -- Restaurant details
    cuisine_type VARCHAR(100),
    rating DECIMAL(3,2),
    total_reviews INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    image_url VARCHAR(500),
    
    -- Delivery information
    minimum_order_amount DECIMAL(10,2),
    delivery_fee DECIMAL(10,2),
    average_delivery_time_minutes INTEGER,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- Indexes for performance
    CONSTRAINT restaurants_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_restaurant_active ON restaurants(is_active);
CREATE INDEX idx_restaurant_cuisine ON restaurants(cuisine_type);
CREATE INDEX idx_restaurant_rating ON restaurants(rating);
```

**Sample Data**:
```sql
INSERT INTO restaurants (id, name, description, address_line1, city, state, zip_code, 
                        latitude, longitude, cuisine_type, rating, total_reviews, 
                        is_active, minimum_order_amount, delivery_fee, 
                        average_delivery_time_minutes, created_at, updated_at)
VALUES 
(1, 'Pizza Paradise', 'Authentic Italian pizza with wood-fired oven', 
 '123 Main St', 'New York', 'NY', '10001', 
 40.7129, -74.0059, 'Italian', 4.5, 234, 
 true, 15.00, 3.99, 30, NOW(), NOW()),
 
(2, 'Sushi Express', 'Fresh sushi and Japanese cuisine', 
 '456 Oak Ave', 'New York', 'NY', '10002', 
 40.7135, -74.0065, 'Japanese', 4.7, 189, 
 true, 20.00, 4.99, 35, NOW(), NOW());
```


#### 2. operating_hours

```sql
CREATE TABLE operating_hours (
    restaurant_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    open_time TIME,
    close_time TIME,
    is_closed BOOLEAN NOT NULL DEFAULT false,
    
    CONSTRAINT fk_operating_hours_restaurant 
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

CREATE INDEX idx_operating_hours_restaurant ON operating_hours(restaurant_id);
```

**Sample Data**:
```sql
INSERT INTO operating_hours (restaurant_id, day_of_week, open_time, close_time, is_closed)
VALUES 
(1, 'MONDAY', '11:00:00', '22:00:00', false),
(1, 'TUESDAY', '11:00:00', '22:00:00', false),
(1, 'WEDNESDAY', '11:00:00', '22:00:00', false),
(1, 'THURSDAY', '11:00:00', '22:00:00', false),
(1, 'FRIDAY', '11:00:00', '23:00:00', false),
(1, 'SATURDAY', '11:00:00', '23:00:00', false),
(1, 'SUNDAY', '12:00:00', '21:00:00', false);
```

---

#### 3. menu_items

```sql
CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    image_url VARCHAR(500),
    
    -- Dietary flags
    is_vegetarian BOOLEAN NOT NULL DEFAULT false,
    is_vegan BOOLEAN NOT NULL DEFAULT false,
    is_gluten_free BOOLEAN NOT NULL DEFAULT false,
    is_available BOOLEAN NOT NULL DEFAULT true,
    
    -- Nutritional information
    ingredients VARCHAR(500),
    allergens VARCHAR(200),
    calories INTEGER,
    preparation_time_minutes INTEGER,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_menu_item_restaurant 
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

CREATE INDEX idx_menu_item_restaurant ON menu_items(restaurant_id);
CREATE INDEX idx_menu_item_category ON menu_items(category);
CREATE INDEX idx_menu_item_available ON menu_items(is_available);
```

**Sample Data**:
```sql
INSERT INTO menu_items (id, restaurant_id, name, description, price, category, 
                       is_vegetarian, is_vegan, is_gluten_free, is_available,
                       ingredients, allergens, calories, preparation_time_minutes,
                       created_at, updated_at)
VALUES 
(101, 1, 'Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and fresh basil',
 12.99, 'Pizza', true, false, false, true,
 'Tomato sauce, mozzarella cheese, fresh basil, olive oil',
 'Dairy, Gluten', 850, 15, NOW(), NOW()),
 
(102, 1, 'Pepperoni Pizza', 'Classic pepperoni pizza with extra cheese',
 14.99, 'Pizza', false, false, false, true,
 'Tomato sauce, mozzarella cheese, pepperoni',
 'Dairy, Gluten, Pork', 1050, 15, NOW(), NOW()),
 
(103, 1, 'Caesar Salad', 'Fresh romaine lettuce with Caesar dressing and croutons',
 8.99, 'Salad', true, false, false, true,
 'Romaine lettuce, Caesar dressing, parmesan, croutons',
 'Dairy, Gluten, Eggs', 320, 5, NOW(), NOW());
```

---

### Entity Relationships

```
┌─────────────────────────┐
│     restaurants         │
│─────────────────────────│
│ id (PK)                 │
│ name                    │
│ address (embedded)      │
│ cuisine_type            │
│ rating                  │
│ ...                     │
└────────┬────────────────┘
         │
         │ 1:N
         │
         ├──────────────────────────────┐
         │                              │
         ▼                              ▼
┌─────────────────────┐    ┌─────────────────────────┐
│  operating_hours    │    │     menu_items          │
│─────────────────────│    │─────────────────────────│
│ restaurant_id (FK)  │    │ id (PK)                 │
│ day_of_week         │    │ restaurant_id (FK)      │
│ open_time           │    │ name                    │
│ close_time          │    │ price                   │
│ is_closed           │    │ is_vegetarian           │
└─────────────────────┘    │ is_vegan                │
                           │ is_gluten_free          │
                           │ ...                     │
                           └─────────────────────────┘
```

---

### Query Examples

#### 1. Find restaurants within 5km of user location

```sql
SELECT r.* 
FROM restaurants r 
WHERE r.is_active = true 
AND (6371 * acos(
    cos(radians(40.7128)) * cos(radians(r.latitude)) * 
    cos(radians(r.longitude) - radians(-74.0060)) + 
    sin(radians(40.7128)) * sin(radians(r.latitude))
)) <= 5
ORDER BY (6371 * acos(
    cos(radians(40.7128)) * cos(radians(r.latitude)) * 
    cos(radians(r.longitude) - radians(-74.0060)) + 
    sin(radians(40.7128)) * sin(radians(r.latitude))
));
```

#### 2. Get restaurant with operating hours

```sql
SELECT r.*, oh.day_of_week, oh.open_time, oh.close_time, oh.is_closed
FROM restaurants r
LEFT JOIN operating_hours oh ON r.id = oh.restaurant_id
WHERE r.id = 1 AND r.is_active = true;
```

#### 3. Get menu items with dietary filters

```sql
SELECT * 
FROM menu_items 
WHERE restaurant_id = 1 
AND is_available = true 
AND is_vegan = true;
```

#### 4. Search restaurants by name

```sql
SELECT * 
FROM restaurants 
WHERE LOWER(name) LIKE LOWER('%pizza%') 
AND is_active = true;
```


---

## 7. Integration Points

### Service Discovery (Eureka)

**How it works**:
1. On startup, Restaurant Service registers with Eureka Server
2. Sends heartbeat every 30 seconds
3. Other services can discover Restaurant Service by name: `restaurant-service`

**Configuration** (bootstrap.yml):
```yaml
spring:
  application:
    name: restaurant-service  # Service name in Eureka
```

**Eureka Client** (enabled by `@EnableDiscoveryClient`):
- Registers at: `http://localhost:8761/eureka/`
- Service URL: `http://RESTAURANT-SERVICE/api/restaurants`
- Other services use this URL instead of hardcoded host:port

**Health Check**:
- Eureka checks: `http://localhost:8083/actuator/health`
- If service is down, Eureka marks it as DOWN
- API Gateway stops routing requests to down instances

---

### Config Server Integration

**How it works**:
1. Restaurant Service starts
2. Reads bootstrap.yml first
3. Connects to Config Server at `http://localhost:8888`
4. Fetches configuration for `restaurant-service` profile
5. Merges with local application.yml
6. Starts application with combined configuration

**Configuration Hierarchy**:
```
1. Config Server (highest priority)
   └─ config-server/src/main/resources/config/restaurant-service.yml
2. Local application.yml (fallback)
   └─ restaurant-service/src/main/resources/application.yml
3. Default values in code
   └─ @Value("${property:defaultValue}")
```

**Fetched Configuration**:
```yaml
# From Config Server
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_db
    username: postgres
    password: postgres
  redis:
    host: localhost
    port: 6379
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

restaurant:
  search:
    default-radius-km: 10
    max-results: 50

server:
  port: 8083
```

**Retry Configuration**:
```yaml
spring:
  cloud:
    config:
      fail-fast: true  # Fail if Config Server is unavailable
      retry:
        max-attempts: 6
        initial-interval: 1000
        multiplier: 1.1
```

---

### API Gateway Integration

**Routing Configuration** (in API Gateway):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: restaurant-service
          uri: lb://restaurant-service  # Load-balanced via Eureka
          predicates:
            - Path=/api/restaurants/**
          filters:
            - StripPrefix=0
```

**Request Flow**:
```
Client Request: http://localhost:8080/api/restaurants/1
       ↓
API Gateway: Resolves "restaurant-service" via Eureka
       ↓
Eureka: Returns available instances (e.g., localhost:8083)
       ↓
API Gateway: Forwards to http://localhost:8083/api/restaurants/1
       ↓
Restaurant Service: Processes request
       ↓
Response: Returns to client via API Gateway
```

---

### Database Integration (PostgreSQL)

**Connection Pool** (HikariCP - default in Spring Boot):
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**JPA Configuration**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update tables
    show-sql: false     # Don't log SQL in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

---

### Cache Integration (Redis)

**Connection**:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

**Cache Keys in Redis**:
```
restaurants::1                                    # Restaurant by ID
restaurants::2
menuItems::1                                      # Menu for restaurant 1
menuItems::1_diet_null_true_null                 # Vegan items for restaurant 1
restaurantSearch::40.7128_-74.0060_5.0          # Location search
restaurantSearch::Italian_40.7128_-74.0060_10.0 # Cuisine + location search
restaurantSearch::top-rated_4.0                  # Top-rated restaurants
```

**Cache Operations**:
- `@Cacheable`: Check cache before method execution
- `@CacheEvict`: Clear cache after method execution
- `@CachePut`: Update cache after method execution

---

### Future Integration Points

**Order Service** (not yet implemented):
- Order Service will call Restaurant Service to:
  - Validate restaurant is active
  - Validate menu items exist and are available
  - Get menu item prices
  - Check minimum order amount
  - Get delivery fee and estimated time

**Example Integration**:
```java
// In Order Service
RestTemplate restTemplate = new RestTemplate();
String url = "http://restaurant-service/api/restaurants/1";
RestaurantResponse restaurant = restTemplate.getForObject(url, RestaurantResponse.class);

if (!restaurant.getIsActive()) {
    throw new RestaurantNotAvailableException();
}
```

**Notification Service** (future):
- Could subscribe to restaurant events:
  - New restaurant added
  - Menu updated
  - Restaurant temporarily closed


---

## 8. Error Handling

### Error Response Format

All errors follow a consistent format:

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/restaurants/999",
  "details": {}  // Optional field-level details
}
```

---

### HTTP Status Codes

| Status Code | Error Type | When It Occurs |
|-------------|-----------|----------------|
| 400 BAD REQUEST | Validation Error | Invalid request data, missing required fields |
| 400 BAD REQUEST | Invalid Argument | Empty search term, invalid parameter values |
| 400 BAD REQUEST | Type Mismatch | Wrong parameter type (e.g., string instead of number) |
| 404 NOT FOUND | Resource Not Found | Restaurant or menu item doesn't exist |
| 409 CONFLICT | Data Integrity Violation | Duplicate data, constraint violations |
| 500 INTERNAL SERVER ERROR | Unexpected Error | Database connection failure, unexpected exceptions |

---

### Error Scenarios

#### 1. Restaurant Not Found (404)

**Trigger**: Request restaurant with non-existent ID

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/999"
```

**Response**:
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Restaurant not found with id: 999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/restaurants/999"
}
```

**Troubleshooting**:
- Verify restaurant ID exists in database
- Check if restaurant is active (inactive restaurants return 404)
- Ensure database connection is working

---

#### 2. Validation Error (400)

**Trigger**: Create restaurant with missing required fields

**Request**:
```bash
curl -X POST "http://localhost:8083/api/admin/restaurants" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "A restaurant"
  }'
```

**Response**:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2024-01-15T10:35:00",
  "path": "/api/admin/restaurants",
  "details": {
    "name": "Restaurant name is required",
    "address": "Address is required",
    "cuisineType": "Cuisine type is required"
  }
}
```

**Troubleshooting**:
- Check request body against DTO validation rules
- Ensure all `@NotNull` and `@NotBlank` fields are provided
- Verify field lengths don't exceed max size
- Check numeric fields are within valid ranges

---

#### 3. Invalid Argument (400)

**Trigger**: Search with empty query string

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/search?query="
```

**Response**:
```json
{
  "errorCode": "INVALID_ARGUMENT",
  "message": "Search term cannot be empty",
  "timestamp": "2024-01-15T10:40:00",
  "path": "/api/restaurants/search"
}
```

**Troubleshooting**:
- Provide non-empty search term
- Check query parameter is properly URL-encoded
- Verify parameter name matches API specification

---

#### 4. Type Mismatch (400)

**Trigger**: Provide string value for numeric parameter

**Request**:
```bash
curl -X GET "http://localhost:8083/api/restaurants/abc"
```

**Response**:
```json
{
  "errorCode": "INVALID_PARAMETER",
  "message": "Invalid value 'abc' for parameter 'restaurantId'. Expected type: Long",
  "timestamp": "2024-01-15T10:45:00",
  "path": "/api/restaurants/abc"
}
```

**Troubleshooting**:
- Verify parameter types match API specification
- Use numeric values for ID parameters
- Use boolean values (true/false) for boolean parameters
- Use proper decimal format for price values

---

#### 5. Data Integrity Violation (409)

**Trigger**: Attempt to create duplicate restaurant (if unique constraint exists)

**Response**:
```json
{
  "errorCode": "DATA_INTEGRITY_VIOLATION",
  "message": "A record with the provided information already exists",
  "timestamp": "2024-01-15T10:50:00",
  "path": "/api/admin/restaurants"
}
```

**Troubleshooting**:
- Check for duplicate restaurant names
- Verify foreign key references exist
- Ensure data meets database constraints

---

#### 6. Internal Server Error (500)

**Trigger**: Database connection failure, unexpected exception

**Response**:
```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please try again later.",
  "timestamp": "2024-01-15T10:55:00",
  "path": "/api/restaurants/1"
}
```

**Troubleshooting**:
- Check application logs for stack trace
- Verify database is running and accessible
- Check Redis is running (if cache-related)
- Verify Config Server is accessible
- Check Eureka Server is running
- Review recent code changes

---

### Logging

**Log Levels**:
```yaml
logging:
  level:
    com.fooddelivery.restaurant: INFO
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

**Log Examples**:

**INFO - Normal Operation**:
```
2024-01-15 10:30:00 INFO  RestaurantService - Fetching restaurant with id: 1
2024-01-15 10:30:05 INFO  RestaurantService - Searching restaurants within 5km of location (40.7128, -74.0060)
2024-01-15 10:30:06 INFO  RestaurantService - Found 12 restaurants in 187ms
```

**WARN - Handled Errors**:
```
2024-01-15 10:35:00 WARN  GlobalExceptionHandler - Resource not found: Restaurant not found with id: 999
2024-01-15 10:40:00 WARN  GlobalExceptionHandler - Illegal argument: Search term cannot be empty
```

**ERROR - Unexpected Errors**:
```
2024-01-15 10:55:00 ERROR GlobalExceptionHandler - Unexpected error occurred: 
org.springframework.dao.DataAccessResourceFailureException: Unable to acquire JDBC Connection
    at org.springframework.jdbc.datasource.DataSourceUtils.getConnection(...)
    ...
```


---

## 9. Configuration Reference

### bootstrap.yml

**Purpose**: Loaded before application.yml, connects to Config Server

```yaml
spring:
  application:
    name: restaurant-service  # Service name for Config Server and Eureka
  cloud:
    config:
      uri: http://localhost:8888  # Config Server URL
      fail-fast: true             # Fail startup if Config Server unavailable
      retry:
        max-attempts: 6           # Retry 6 times
        initial-interval: 1000    # Wait 1 second between retries
        multiplier: 1.1           # Increase wait time by 10% each retry
  profiles:
    active: dev                   # Active profile (dev, prod, test)
```

---

### application.yml

**Purpose**: Local configuration, fallback if Config Server unavailable

```yaml
# Minimal local configuration
spring:
  cloud:
    config:
      enabled: true  # Enable Config Server integration

# Local fallback values (used only if Config Server is unavailable)
server:
  port: 8083

logging:
  level:
    com.fooddelivery.restaurant: INFO
```

---

### Config Server Configuration

**Location**: `config-server/src/main/resources/config/restaurant-service.yml`

**Full Configuration**:
```yaml
server:
  port: 8083

spring:
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_db
    username: postgres
    password: postgres
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
      ddl-auto: update  # Options: none, validate, update, create, create-drop
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
        min-idle: 0
        max-wait: -1ms

# Eureka Client Configuration
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
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
      show-details: always
  health:
    redis:
      enabled: true
    db:
      enabled: true

# Application-Specific Configuration
restaurant:
  search:
    default-radius-km: 10  # Default search radius
    max-results: 50        # Maximum results per query

# Logging Configuration
logging:
  level:
    com.fooddelivery.restaurant: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

### Environment-Specific Configuration

#### Development Profile (restaurant-service-dev.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_db_dev
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

logging:
  level:
    com.fooddelivery.restaurant: DEBUG
    org.hibernate.SQL: DEBUG
```

#### Production Profile (restaurant-service-prod.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db-server:5432/restaurant_db
    username: ${DB_USERNAME}  # From environment variable
    password: ${DB_PASSWORD}  # From environment variable
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate  # Don't auto-update schema in production

logging:
  level:
    com.fooddelivery.restaurant: INFO
    org.hibernate.SQL: WARN

restaurant:
  search:
    max-results: 100  # Allow more results in production
```

---

### Docker Configuration

**Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/restaurant-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml** (for local development):
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: restaurant_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

  restaurant-service:
    build: .
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/restaurant_db
      SPRING_REDIS_HOST: redis
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
```

---

### Kubernetes Configuration

**k8s-deployment.yml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: restaurant-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: restaurant-service
  template:
    metadata:
      labels:
        app: restaurant-service
    spec:
      containers:
      - name: restaurant-service
        image: restaurant-service:1.0.0
        ports:
        - containerPort: 8083
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/restaurant_db"
        - name: SPRING_REDIS_HOST
          value: "redis-service"
        - name: EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE
          value: "http://eureka-service:8761/eureka/"
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
            port: 8083
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8083
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: restaurant-service
spec:
  selector:
    app: restaurant-service
  ports:
  - protocol: TCP
    port: 8083
    targetPort: 8083
  type: ClusterIP
```


---

## 10. Testing Guide

### Manual Testing with cURL

#### 1. Get Restaurant by ID

```bash
curl -X GET "http://localhost:8083/api/restaurants/1" \
  -H "Accept: application/json" | jq
```

**Expected**: 200 OK with restaurant details

---

#### 2. Search Nearby Restaurants

```bash
curl -X GET "http://localhost:8083/api/restaurants/search/nearby?latitude=40.7128&longitude=-74.0060&radiusKm=5" \
  -H "Accept: application/json" | jq
```

**Expected**: 200 OK with array of restaurants within 5km

---

#### 3. Search by Name

```bash
curl -X GET "http://localhost:8083/api/restaurants/search?query=pizza" \
  -H "Accept: application/json" | jq
```

**Expected**: 200 OK with matching restaurants

---

#### 4. Get Restaurant Menu

```bash
curl -X GET "http://localhost:8083/api/restaurants/1/menu" \
  -H "Accept: application/json" | jq
```

**Expected**: 200 OK with array of menu items

---

#### 5. Filter Menu by Dietary Preferences

```bash
# Vegan items only
curl -X GET "http://localhost:8083/api/restaurants/1/menu/filter?vegan=true" \
  -H "Accept: application/json" | jq

# Vegetarian and gluten-free
curl -X GET "http://localhost:8083/api/restaurants/1/menu/filter?vegetarian=true&glutenFree=true" \
  -H "Accept: application/json" | jq
```

**Expected**: 200 OK with filtered menu items

---

#### 6. Create Restaurant (Admin)

```bash
curl -X POST "http://localhost:8083/api/admin/restaurants" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "name": "Test Restaurant",
    "description": "Test description",
    "address": {
      "addressLine1": "123 Test St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA",
      "latitude": 40.7128,
      "longitude": -74.0060
    },
    "cuisineType": "Italian",
    "minimumOrderAmount": 15.00,
    "deliveryFee": 3.99,
    "averageDeliveryTimeMinutes": 30
  }' | jq
```

**Expected**: 201 CREATED with new restaurant details

---

#### 7. Add Menu Item (Admin)

```bash
curl -X POST "http://localhost:8083/api/admin/restaurants/1/items" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "name": "Test Pizza",
    "description": "Test description",
    "price": 12.99,
    "category": "Pizza",
    "isVegetarian": true,
    "isVegan": false,
    "isGlutenFree": false,
    "isAvailable": true,
    "calories": 800,
    "preparationTimeMinutes": 15
  }' | jq
```

**Expected**: 201 CREATED with new menu item details

---

### Testing with Postman

**Collection Setup**:

1. Create new collection: "Restaurant Service"
2. Add environment variables:
   - `base_url`: `http://localhost:8083`
   - `restaurant_id`: `1`

**Sample Requests**:

**Get Restaurant**:
- Method: GET
- URL: `{{base_url}}/api/restaurants/{{restaurant_id}}`
- Headers: `Accept: application/json`

**Search Nearby**:
- Method: GET
- URL: `{{base_url}}/api/restaurants/search/nearby`
- Params:
  - `latitude`: `40.7128`
  - `longitude`: `-74.0060`
  - `radiusKm`: `5`

**Create Restaurant**:
- Method: POST
- URL: `{{base_url}}/api/admin/restaurants`
- Headers: `Content-Type: application/json`
- Body (raw JSON): [Use example from cURL section]

---

### Unit Testing

**Example Test Class**:

```java
@SpringBootTest
@AutoConfigureMockMvc
class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private RestaurantMapper restaurantMapper;

    @Test
    void testGetRestaurantById_Success() {
        // Arrange
        Long restaurantId = 1L;
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        restaurant.setIsActive(true);

        RestaurantResponse expectedResponse = new RestaurantResponse();
        expectedResponse.setId(restaurantId);
        expectedResponse.setName("Test Restaurant");

        when(restaurantRepository.findByIdAndIsActiveTrue(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(restaurantMapper.toRestaurantResponse(restaurant))
            .thenReturn(expectedResponse);

        // Act
        RestaurantResponse actualResponse = restaurantService.getRestaurantById(restaurantId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(restaurantId, actualResponse.getId());
        assertEquals("Test Restaurant", actualResponse.getName());
        verify(restaurantRepository).findByIdAndIsActiveTrue(restaurantId);
    }

    @Test
    void testGetRestaurantById_NotFound() {
        // Arrange
        Long restaurantId = 999L;
        when(restaurantRepository.findByIdAndIsActiveTrue(restaurantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            restaurantService.getRestaurantById(restaurantId);
        });
    }

    @Test
    void testSearchRestaurantsNearby() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        List<Restaurant> restaurants = Arrays.asList(
            createTestRestaurant(1L, "Restaurant 1"),
            createTestRestaurant(2L, "Restaurant 2")
        );

        when(restaurantRepository.findRestaurantsWithinRadius(latitude, longitude, radiusKm))
            .thenReturn(restaurants);

        // Act
        List<RestaurantResponse> responses = restaurantService.searchRestaurantsNearby(
            latitude, longitude, radiusKm);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(restaurantRepository).findRestaurantsWithinRadius(latitude, longitude, radiusKm);
    }

    private Restaurant createTestRestaurant(Long id, String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName(name);
        restaurant.setIsActive(true);
        return restaurant;
    }
}
```

---

### Integration Testing

**Example Integration Test**:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RestaurantControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetRestaurant_Success() {
        // Act
        ResponseEntity<RestaurantResponse> response = restTemplate.getForEntity(
            "/api/restaurants/1", RestaurantResponse.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetRestaurant_NotFound() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/restaurants/999", String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSearchNearby() {
        // Act
        String url = "/api/restaurants/search/nearby?latitude=40.7128&longitude=-74.0060&radiusKm=10";
        ResponseEntity<RestaurantResponse[]> response = restTemplate.getForEntity(
            url, RestaurantResponse[].class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }
}
```

---

### Load Testing

**Using Apache Bench (ab)**:

```bash
# Test search endpoint with 1000 requests, 10 concurrent
ab -n 1000 -c 10 "http://localhost:8083/api/restaurants/search/nearby?latitude=40.7128&longitude=-74.0060&radiusKm=5"
```

**Expected Results**:
- Requests per second: > 100
- Mean response time: < 100ms (with cache)
- 95th percentile: < 300ms

**Using JMeter**:
1. Create Thread Group: 100 users, ramp-up 10 seconds
2. Add HTTP Request: GET /api/restaurants/search/nearby
3. Add parameters: latitude, longitude, radiusKm
4. Add listeners: View Results Tree, Summary Report
5. Run test and analyze results


---

## 11. Performance & Monitoring

### Performance Metrics

#### Response Time Targets

| Endpoint | Target | With Cache | Without Cache |
|----------|--------|------------|---------------|
| Get Restaurant by ID | < 100ms | ~5ms | ~50ms |
| Search Nearby | < 300ms | ~5ms | ~200ms |
| Get Menu | < 100ms | ~5ms | ~80ms |
| Search by Name | < 200ms | N/A | ~150ms |
| Create Restaurant | < 500ms | N/A | ~300ms |

#### Cache Hit Rates

**Expected Cache Hit Rates**:
- Restaurant details: 80-90%
- Menu items: 70-80%
- Search results: 60-70%

**Monitoring Cache Performance**:
```bash
# Connect to Redis CLI
redis-cli

# Check cache keys
KEYS restaurants::*
KEYS menuItems::*
KEYS restaurantSearch::*

# Get cache statistics
INFO stats

# Monitor cache operations in real-time
MONITOR
```

---

### Actuator Endpoints

#### Health Check

```bash
curl http://localhost:8083/actuator/health | jq
```

**Response**:
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
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.6"
      }
    }
  }
}
```

---

#### Metrics

```bash
curl http://localhost:8083/actuator/metrics | jq
```

**Available Metrics**:
- `jvm.memory.used`: JVM memory usage
- `jvm.threads.live`: Active thread count
- `http.server.requests`: HTTP request metrics
- `hikaricp.connections.active`: Database connection pool
- `cache.gets`: Cache get operations
- `cache.puts`: Cache put operations
- `cache.evictions`: Cache evictions

**Specific Metric Example**:
```bash
curl http://localhost:8083/actuator/metrics/http.server.requests | jq
```

**Response**:
```json
{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1523
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 45.678
    },
    {
      "statistic": "MAX",
      "value": 0.287
    }
  ],
  "availableTags": [
    {
      "tag": "uri",
      "values": ["/api/restaurants/{id}", "/api/restaurants/search/nearby"]
    },
    {
      "tag": "status",
      "values": ["200", "404", "500"]
    }
  ]
}
```

---

### Logging Best Practices

#### Log Levels

**Development**:
```yaml
logging:
  level:
    com.fooddelivery.restaurant: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

**Production**:
```yaml
logging:
  level:
    com.fooddelivery.restaurant: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
```

#### Important Log Messages

**Performance Monitoring**:
```java
log.info("Found {} restaurants in {}ms", responses.size(), duration);
```

**Error Tracking**:
```java
log.error("Unexpected error occurred: ", ex);
```

**Business Events**:
```java
log.info("Restaurant created with id: {}", restaurant.getId());
log.info("Menu item deleted: {}", menuItemId);
```

---

### Monitoring Tools

#### Prometheus Integration

**Add Dependency** (pom.xml):
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Expose Prometheus Endpoint**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
```

**Scrape Configuration** (prometheus.yml):
```yaml
scrape_configs:
  - job_name: 'restaurant-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8083']
```

---

#### Grafana Dashboard

**Key Metrics to Monitor**:

1. **Request Rate**:
   - Query: `rate(http_server_requests_seconds_count[5m])`
   - Shows requests per second

2. **Response Time (95th percentile)**:
   - Query: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
   - Shows 95% of requests complete within X seconds

3. **Error Rate**:
   - Query: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`
   - Shows 5xx errors per second

4. **Cache Hit Rate**:
   - Query: `rate(cache_gets_total{result="hit"}[5m]) / rate(cache_gets_total[5m])`
   - Shows percentage of cache hits

5. **Database Connection Pool**:
   - Query: `hikaricp_connections_active`
   - Shows active database connections

6. **JVM Memory Usage**:
   - Query: `jvm_memory_used_bytes{area="heap"}`
   - Shows heap memory usage

---

### Performance Optimization Tips

#### 1. Database Optimization

**Add Indexes**:
```sql
CREATE INDEX idx_restaurant_location ON restaurants(latitude, longitude);
CREATE INDEX idx_menu_item_dietary ON menu_items(is_vegetarian, is_vegan, is_gluten_free);
```

**Optimize Queries**:
- Use `@EntityGraph` to avoid N+1 queries
- Use pagination for large result sets
- Use database-specific optimizations (e.g., PostgreSQL GiST index for geospatial)

#### 2. Cache Optimization

**Increase Cache TTL for Stable Data**:
```java
cacheConfigurations.put("restaurants", 
    defaultConfig.entryTtl(Duration.ofMinutes(30)));  // Increased from 15
```

**Use Cache Warming**:
```java
@PostConstruct
public void warmCache() {
    // Pre-load popular restaurants into cache
    List<Restaurant> topRestaurants = restaurantRepository.findTopRatedRestaurants(4.5);
    topRestaurants.forEach(r -> restaurantService.getRestaurantById(r.getId()));
}
```

#### 3. Connection Pool Tuning

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase for high load
      minimum-idle: 10
      connection-timeout: 20000
```

#### 4. JVM Tuning

```bash
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar restaurant-service.jar
```

---

### Alerting

**Key Alerts to Configure**:

1. **High Error Rate**:
   - Condition: Error rate > 5% for 5 minutes
   - Action: Send alert to on-call engineer

2. **Slow Response Time**:
   - Condition: 95th percentile > 500ms for 10 minutes
   - Action: Investigate performance issue

3. **Low Cache Hit Rate**:
   - Condition: Cache hit rate < 50% for 15 minutes
   - Action: Check Redis health, review cache configuration

4. **Database Connection Pool Exhaustion**:
   - Condition: Active connections > 90% of max pool size
   - Action: Increase pool size or investigate connection leaks

5. **Service Down**:
   - Condition: Health check fails for 2 consecutive checks
   - Action: Restart service, escalate if issue persists


---

## 12. Troubleshooting Guide

### Common Issues and Solutions

#### Issue 1: Service Won't Start

**Symptoms**:
- Application fails to start
- Error: "Failed to configure a DataSource"

**Possible Causes**:
1. Config Server is not running
2. Database is not accessible
3. Redis is not running

**Solutions**:

1. **Check Config Server**:
   ```bash
   curl http://localhost:8888/restaurant-service/dev
   ```
   - If fails, start Config Server first

2. **Check Database Connection**:
   ```bash
   psql -h localhost -U postgres -d restaurant_db
   ```
   - Verify database exists
   - Check credentials in Config Server configuration

3. **Check Redis**:
   ```bash
   redis-cli ping
   ```
   - Should return "PONG"
   - If not, start Redis: `redis-server`

4. **Check Eureka Server**:
   ```bash
   curl http://localhost:8761/eureka/apps
   ```
   - Verify Eureka is running

---

#### Issue 2: Slow Search Performance

**Symptoms**:
- Search nearby endpoint takes > 1 second
- Database CPU usage is high

**Diagnosis**:
```bash
# Check if query is using index
psql -U postgres -d restaurant_db
EXPLAIN ANALYZE SELECT * FROM restaurants WHERE is_active = true;
```

**Solutions**:

1. **Verify Indexes Exist**:
   ```sql
   SELECT indexname, indexdef 
   FROM pg_indexes 
   WHERE tablename = 'restaurants';
   ```

2. **Add Missing Indexes**:
   ```sql
   CREATE INDEX idx_restaurant_active ON restaurants(is_active);
   CREATE INDEX idx_restaurant_location ON restaurants(latitude, longitude);
   ```

3. **Check Cache Hit Rate**:
   ```bash
   redis-cli
   INFO stats
   ```
   - Look for `keyspace_hits` vs `keyspace_misses`
   - If hit rate < 50%, investigate cache configuration

4. **Increase Cache TTL**:
   ```java
   cacheConfigurations.put("restaurantSearch", 
       defaultConfig.entryTtl(Duration.ofMinutes(10)));  // Increased from 5
   ```

---

#### Issue 3: Cache Not Working

**Symptoms**:
- Every request hits database
- Response times are consistently slow
- Redis is running but not being used

**Diagnosis**:
```bash
# Monitor Redis operations
redis-cli MONITOR

# Make a request
curl http://localhost:8083/api/restaurants/1

# Check if Redis received any commands
```

**Solutions**:

1. **Verify @EnableCaching is Present**:
   ```java
   @SpringBootApplication
   @EnableCaching  // Must be present
   public class RestaurantServiceApplication { }
   ```

2. **Check Redis Connection**:
   ```yaml
   spring:
     redis:
       host: localhost
       port: 6379
   ```

3. **Verify Cache Configuration**:
   ```java
   @Bean
   public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
       // Verify this bean exists
   }
   ```

4. **Check Method Annotations**:
   ```java
   @Cacheable(value = "restaurants", key = "#restaurantId")
   public RestaurantResponse getRestaurantById(Long restaurantId) {
       // Method must be public
       // Class must be a Spring bean
   }
   ```

---

#### Issue 4: Restaurant Not Found (404) But Exists in Database

**Symptoms**:
- GET /api/restaurants/1 returns 404
- Restaurant exists in database with id=1

**Diagnosis**:
```sql
SELECT id, name, is_active FROM restaurants WHERE id = 1;
```

**Possible Causes**:
1. Restaurant is inactive (`is_active = false`)
2. Cache contains stale data

**Solutions**:

1. **Check Active Status**:
   ```sql
   UPDATE restaurants SET is_active = true WHERE id = 1;
   ```

2. **Clear Cache**:
   ```bash
   redis-cli
   DEL restaurants::1
   FLUSHDB  # Clear all cache (use with caution)
   ```

3. **Restart Service**:
   ```bash
   # Stop service
   # Start service
   ```

---

#### Issue 5: Validation Errors Not Showing Details

**Symptoms**:
- POST request returns 400 but no field-level errors
- Error response missing "details" field

**Diagnosis**:
- Check if `@Valid` annotation is present on controller method
- Verify GlobalExceptionHandler is handling MethodArgumentNotValidException

**Solutions**:

1. **Add @Valid Annotation**:
   ```java
   @PostMapping
   public ResponseEntity<RestaurantResponse> createRestaurant(
           @Valid @RequestBody CreateRestaurantRequest request) {
       // @Valid triggers validation
   }
   ```

2. **Verify Exception Handler**:
   ```java
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ErrorResponse> handleValidationException(...) {
       // Must be present in GlobalExceptionHandler
   }
   ```

---

#### Issue 6: Database Connection Pool Exhausted

**Symptoms**:
- Error: "Connection is not available, request timed out after 30000ms"
- Service becomes unresponsive under load

**Diagnosis**:
```bash
curl http://localhost:8083/actuator/metrics/hikaricp.connections.active
```

**Solutions**:

1. **Increase Pool Size**:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20  # Increase from 10
   ```

2. **Check for Connection Leaks**:
   - Ensure all repository methods use `@Transactional`
   - Verify connections are being closed properly

3. **Reduce Connection Timeout**:
   ```yaml
   spring:
     datasource:
       hikari:
         connection-timeout: 20000  # Reduce from 30000
   ```

4. **Add Connection Leak Detection**:
   ```yaml
   spring:
     datasource:
       hikari:
         leak-detection-threshold: 60000  # 60 seconds
   ```

---

#### Issue 7: Eureka Registration Fails

**Symptoms**:
- Service starts but doesn't appear in Eureka dashboard
- Other services can't discover restaurant-service

**Diagnosis**:
```bash
# Check Eureka dashboard
http://localhost:8761

# Check service logs for Eureka errors
grep -i eureka logs/restaurant-service.log
```

**Solutions**:

1. **Verify Eureka Configuration**:
   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/
       register-with-eureka: true  # Must be true
   ```

2. **Check Network Connectivity**:
   ```bash
   curl http://localhost:8761/eureka/apps
   ```

3. **Verify @EnableDiscoveryClient**:
   ```java
   @SpringBootApplication
   @EnableDiscoveryClient  // Must be present
   public class RestaurantServiceApplication { }
   ```

4. **Check Firewall Rules**:
   - Ensure port 8761 is accessible
   - Check if any firewall is blocking Eureka communication

---

### Debug Techniques

#### 1. Enable Debug Logging

```yaml
logging:
  level:
    com.fooddelivery.restaurant: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 2. Use Actuator for Runtime Inspection

```bash
# Check environment variables
curl http://localhost:8083/actuator/env

# Check configuration properties
curl http://localhost:8083/actuator/configprops

# Check beans
curl http://localhost:8083/actuator/beans
```

#### 3. Monitor Database Queries

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

#### 4. Profile Application Performance

```bash
# Add JVM flags
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar restaurant-service.jar

# Connect with IDE debugger on port 5005
```

---

### Log Analysis

#### Finding Errors

```bash
# Find all ERROR logs
grep ERROR logs/restaurant-service.log

# Find specific exception
grep -A 10 "ResourceNotFoundException" logs/restaurant-service.log

# Count errors by type
grep ERROR logs/restaurant-service.log | cut -d' ' -f5- | sort | uniq -c | sort -rn
```

#### Performance Analysis

```bash
# Find slow queries (> 1 second)
grep "Found .* restaurants in [0-9][0-9][0-9][0-9]ms" logs/restaurant-service.log

# Average response time
grep "Found .* restaurants in" logs/restaurant-service.log | \
  awk '{print $NF}' | sed 's/ms//' | \
  awk '{sum+=$1; count++} END {print sum/count}'
```


---

## 13. FAQ Section

### General Questions

#### Q1: Why is the service called "restaurant-service" and not "restaurant-catalog-service"?

**A**: Following microservices naming conventions, we use simple, domain-focused names. The service name clearly indicates it handles restaurant-related operations. The specific responsibilities (catalog, menu, search) are implementation details.

---

#### Q2: Why use Redis for caching instead of in-memory caching?

**A**: Redis provides several advantages:
1. **Distributed Caching**: Multiple service instances share the same cache
2. **Persistence**: Cache survives service restarts
3. **Scalability**: Can scale cache independently from application
4. **Monitoring**: Easy to monitor cache performance with Redis CLI
5. **Flexibility**: Can adjust TTL and eviction policies without code changes

---

#### Q3: Why use PostgreSQL instead of MongoDB for restaurant data?

**A**: PostgreSQL is better suited for this use case because:
1. **Structured Data**: Restaurant and menu data has a clear schema
2. **ACID Transactions**: Ensures data consistency for orders
3. **Geospatial Support**: Native support for location-based queries
4. **Relationships**: Strong support for foreign keys and joins
5. **Mature Ecosystem**: Better tooling and monitoring

---

#### Q4: How does the Haversine formula work for distance calculation?

**A**: The Haversine formula calculates the great-circle distance between two points on a sphere (Earth):

```
a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
c = 2 × atan2(√a, √(1−a))
d = R × c
```

Where:
- `lat1, lon1`: User's location
- `lat2, lon2`: Restaurant's location
- `R`: Earth's radius (6371 km)
- `d`: Distance in kilometers

**Accuracy**: ±0.5% error for distances up to 1000km

---

### Technical Questions

#### Q5: Why use @Transactional(readOnly = true) for read operations?

**A**: Benefits of `readOnly = true`:
1. **Performance**: Hibernate skips dirty checking
2. **Optimization**: Database can optimize read-only transactions
3. **Clarity**: Clearly indicates method doesn't modify data
4. **Safety**: Prevents accidental writes

---

#### Q6: Why separate RestaurantController and RestaurantAdminController?

**A**: Separation provides:
1. **Security**: Easier to apply different security rules
2. **Clarity**: Clear distinction between public and admin APIs
3. **Documentation**: Separate Swagger groups for different audiences
4. **Versioning**: Can version admin and public APIs independently

---

#### Q7: How does cache invalidation work when updating a restaurant?

**A**: Cache invalidation strategy:

```java
@CacheEvict(value = {"restaurants", "restaurantSearch"}, allEntries = true)
public RestaurantResponse updateRestaurant(...) {
    // Update logic
}
```

**What happens**:
1. Method executes (updates database)
2. After successful update, Spring clears ALL entries in "restaurants" and "restaurantSearch" caches
3. Next request will cache miss and fetch fresh data from database
4. Fresh data is cached for subsequent requests

**Why clear all entries?**
- A restaurant update might affect search results (e.g., changing cuisine type)
- Simpler than tracking which specific cache keys to invalidate
- Cache will be repopulated quickly with fresh data

---

#### Q8: Why use DTOs instead of returning entities directly?

**A**: DTOs provide:
1. **Decoupling**: API contract independent of database schema
2. **Security**: Don't expose internal entity structure
3. **Performance**: Can exclude unnecessary fields (e.g., lazy-loaded collections)
4. **Flexibility**: Can combine data from multiple entities
5. **Versioning**: Can maintain multiple DTO versions for API compatibility

---

#### Q9: How does the service handle concurrent updates to the same restaurant?

**A**: Concurrency handling:

1. **Database Level**: PostgreSQL uses MVCC (Multi-Version Concurrency Control)
   - Multiple transactions can read the same row simultaneously
   - Writes are serialized at the row level

2. **Application Level**: 
   - `@Transactional` ensures atomic operations
   - Last write wins (no optimistic locking implemented yet)

**Future Enhancement**: Add optimistic locking with `@Version`:
```java
@Entity
public class Restaurant {
    @Version
    private Long version;
    // ...
}
```

---

#### Q10: Why not use GraphQL instead of REST?

**A**: REST is chosen because:
1. **Simplicity**: Easier to implement and understand
2. **Caching**: HTTP caching works out of the box
3. **Tooling**: Better tooling support (Swagger, Postman)
4. **Team Familiarity**: Most developers know REST

**GraphQL Advantages** (for future consideration):
- Flexible queries (client specifies fields)
- Single endpoint
- Reduced over-fetching

---

### Design Decisions

#### Q11: Why embed Address instead of making it a separate entity?

**A**: Address is embedded because:
1. **Lifecycle**: Address has no meaning without a restaurant
2. **Simplicity**: No need for separate table and foreign keys
3. **Performance**: Fewer joins, faster queries
4. **Domain Model**: Address is a value object, not an entity

---

#### Q12: Why use @ElementCollection for OperatingHours instead of @OneToMany?

**A**: `@ElementCollection` is appropriate because:
1. **Value Objects**: OperatingHours are value objects, not entities
2. **No Identity**: Operating hours don't need their own ID
3. **Lifecycle**: Managed entirely by Restaurant entity
4. **Simplicity**: Cleaner code, no separate repository needed

---

#### Q13: Why limit search results to 50 by default?

**A**: Result limiting provides:
1. **Performance**: Prevents large result sets from overwhelming the system
2. **User Experience**: Users rarely need more than 50 results
3. **Network**: Reduces payload size
4. **Database**: Reduces query execution time

**Configurable**: Can be adjusted via configuration:
```yaml
restaurant:
  search:
    max-results: 100  # Increase if needed
```

---

#### Q14: Why use BigDecimal for prices instead of Double?

**A**: BigDecimal is essential for financial data:
1. **Precision**: No floating-point rounding errors
2. **Accuracy**: Exact decimal representation
3. **Standards**: Industry standard for monetary values

**Example of Double problem**:
```java
double price = 0.1 + 0.2;  // Result: 0.30000000000000004
BigDecimal price = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // Result: 0.3
```

---

### Operational Questions

#### Q15: How do I add a new restaurant in production?

**A**: Steps:
1. Use admin API endpoint: `POST /api/admin/restaurants`
2. Provide all required fields (name, address, cuisine type)
3. Verify restaurant appears in search results
4. Monitor logs for any errors

**Security Note**: In production, this endpoint should require authentication and RESTAURANT_ADMIN role.

---

#### Q16: How do I temporarily disable a restaurant?

**A**: Update restaurant status:
```bash
curl -X PUT "http://localhost:8083/api/admin/restaurants/1" \
  -H "Content-Type: application/json" \
  -d '{"isActive": false}'
```

**Effect**:
- Restaurant won't appear in search results
- Direct access returns 404
- Cache is cleared automatically

---

#### Q17: How do I monitor cache performance?

**A**: Multiple approaches:

1. **Redis CLI**:
   ```bash
   redis-cli INFO stats
   ```

2. **Actuator Metrics**:
   ```bash
   curl http://localhost:8083/actuator/metrics/cache.gets
   ```

3. **Application Logs**:
   - Look for performance logs: "Found X restaurants in Yms"
   - Compare times with and without cache

4. **Grafana Dashboard**:
   - Create dashboard with cache hit rate metric
   - Set up alerts for low hit rates

---

#### Q18: What happens if Redis goes down?

**A**: Graceful degradation:
1. Service continues to work (queries hit database)
2. Performance degrades (no caching)
3. Response times increase
4. Database load increases

**Recovery**:
1. Restart Redis
2. Cache will be repopulated automatically as requests come in
3. Performance returns to normal within minutes

**Monitoring**: Set up alerts for Redis downtime

---

#### Q19: How do I scale the service horizontally?

**A**: Scaling steps:

1. **Deploy Multiple Instances**:
   ```bash
   # Instance 1
   java -jar restaurant-service.jar --server.port=8083
   
   # Instance 2
   java -jar restaurant-service.jar --server.port=8084
   ```

2. **Register with Eureka**:
   - Both instances register automatically
   - Eureka provides load balancing

3. **Shared Resources**:
   - All instances share same PostgreSQL database
   - All instances share same Redis cache
   - No session state in application (stateless)

4. **Load Balancing**:
   - API Gateway distributes requests across instances
   - Eureka provides service discovery

---

#### Q20: How do I backup restaurant data?

**A**: Database backup strategies:

1. **PostgreSQL Dump**:
   ```bash
   pg_dump -U postgres restaurant_db > backup.sql
   ```

2. **Automated Backups**:
   ```bash
   # Daily backup cron job
   0 2 * * * pg_dump -U postgres restaurant_db > /backups/restaurant_db_$(date +\%Y\%m\%d).sql
   ```

3. **Point-in-Time Recovery**:
   - Enable WAL archiving in PostgreSQL
   - Configure continuous archiving

4. **Restore**:
   ```bash
   psql -U postgres restaurant_db < backup.sql
   ```

---

## Conclusion

This documentation provides a comprehensive guide to the Restaurant Service. For additional questions or issues not covered here, please:

1. Check application logs: `logs/restaurant-service.log`
2. Review Actuator endpoints: `http://localhost:8083/actuator`
3. Consult Spring Boot documentation: https://spring.io/projects/spring-boot
4. Contact the development team

**Last Updated**: January 2024  
**Version**: 1.0.0  
**Maintainer**: Food Delivery Platform Team

