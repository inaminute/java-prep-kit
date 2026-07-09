# Location Update Guide for Delivery Agents

## Overview

Delivery agents must update their location every 30 seconds to enable real-time tracking for customers. This guide explains how to implement location updates in the agent mobile application.

## Location Update Frequency

**Requirement 5.5**: THE Delivery Service SHALL allow delivery agents to update their location coordinates every 30 seconds

- **Update Interval**: 30 seconds
- **Storage**: Redis (real-time) + PostgreSQL (backup)
- **TTL**: 60 minutes in Redis
- **Response Time**: < 100ms

## API Endpoint

### Update Agent Location

```http
PUT /api/delivery/agents/{agentId}/location
Content-Type: application/json

{
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

**Response:**
```json
{
  "id": 123,
  "userId": 456,
  "vehicleType": "Motorcycle",
  "vehicleNumber": "ABC-1234",
  "status": "AVAILABLE",
  "currentLatitude": 40.7128,
  "currentLongitude": -74.0060,
  "totalDeliveries": 150,
  "rating": 4.8
}
```

## Implementation Examples

### Android (Kotlin)

```kotlin
import android.location.Location
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import retrofit2.http.*

// API Interface
interface DeliveryAgentApi {
    @PUT("api/delivery/agents/{agentId}/location")
    suspend fun updateLocation(
        @Path("agentId") agentId: Long,
        @Body location: LocationUpdate
    ): AgentResponse
}

data class LocationUpdate(
    val latitude: Double,
    val longitude: Double
)

// Location Update Service
class LocationUpdateService(
    private val api: DeliveryAgentApi,
    private val agentId: Long
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var updateJob: Job? = null
    
    fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
        val locationRequest = LocationRequest.create().apply {
            interval = 30_000 // 30 seconds
            fastestInterval = 30_000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    updateLocation(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    
    private fun updateLocation(location: Location) {
        scope.launch {
            try {
                val update = LocationUpdate(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                
                val response = api.updateLocation(agentId, update)
                Log.d("LocationUpdate", "Location updated successfully")
                
            } catch (e: Exception) {
                Log.e("LocationUpdate", "Failed to update location", e)
                // Retry logic can be added here
            }
        }
    }
    
    fun stopLocationUpdates() {
        updateJob?.cancel()
    }
}
```

### iOS (Swift)

```swift
import CoreLocation
import Combine

struct LocationUpdate: Codable {
    let latitude: Double
    let longitude: Double
}

class LocationUpdateService: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let agentId: Int64
    private let apiClient: DeliveryAgentAPI
    private var updateTimer: Timer?
    
    init(agentId: Int64, apiClient: DeliveryAgentAPI) {
        self.agentId = agentId
        self.apiClient = apiClient
        super.init()
        
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 10 // meters
    }
    
    func startLocationUpdates() {
        locationManager.requestAlwaysAuthorization()
        locationManager.startUpdatingLocation()
        
        // Update every 30 seconds
        updateTimer = Timer.scheduledTimer(
            withTimeInterval: 30.0,
            repeats: true
        ) { [weak self] _ in
            self?.sendLocationUpdate()
        }
    }
    
    func stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
        updateTimer?.invalidate()
        updateTimer = nil
    }
    
    private func sendLocationUpdate() {
        guard let location = locationManager.location else { return }
        
        let update = LocationUpdate(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        )
        
        apiClient.updateLocation(agentId: agentId, location: update) { result in
            switch result {
            case .success:
                print("Location updated successfully")
            case .failure(let error):
                print("Failed to update location: \(error)")
                // Retry logic can be added here
            }
        }
    }
}
```

### React Native (JavaScript)

```javascript
import Geolocation from '@react-native-community/geolocation';
import axios from 'axios';

class LocationUpdateService {
  constructor(agentId, apiBaseUrl) {
    this.agentId = agentId;
    this.apiBaseUrl = apiBaseUrl;
    this.watchId = null;
    this.updateInterval = null;
    this.currentLocation = null;
  }

  startLocationUpdates() {
    // Watch for location changes
    this.watchId = Geolocation.watchPosition(
      (position) => {
        this.currentLocation = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        };
      },
      (error) => {
        console.error('Location error:', error);
      },
      {
        enableHighAccuracy: true,
        distanceFilter: 10, // meters
        interval: 30000, // 30 seconds
        fastestInterval: 30000,
      }
    );

    // Send updates every 30 seconds
    this.updateInterval = setInterval(() => {
      this.sendLocationUpdate();
    }, 30000);
  }

  async sendLocationUpdate() {
    if (!this.currentLocation) {
      console.warn('No location available');
      return;
    }

    try {
      const response = await axios.put(
        `${this.apiBaseUrl}/api/delivery/agents/${this.agentId}/location`,
        this.currentLocation,
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.authToken}`,
          },
          timeout: 5000, // 5 second timeout
        }
      );

      console.log('Location updated successfully:', response.data);
    } catch (error) {
      console.error('Failed to update location:', error);
      // Retry logic can be added here
    }
  }

  stopLocationUpdates() {
    if (this.watchId !== null) {
      Geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }

    if (this.updateInterval !== null) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
    }
  }
}

export default LocationUpdateService;
```

## Best Practices

### 1. Battery Optimization

- Use appropriate location accuracy (HIGH_ACCURACY for active deliveries)
- Reduce update frequency when agent is OFFLINE
- Stop updates when app is in background (if not delivering)

### 2. Network Optimization

- Batch updates if network is unavailable
- Use exponential backoff for retries
- Compress location data if sending multiple points

### 3. Error Handling

```javascript
async function updateLocationWithRetry(agentId, location, maxRetries = 3) {
  let retries = 0;
  let delay = 1000; // Start with 1 second

  while (retries < maxRetries) {
    try {
      await updateLocation(agentId, location);
      return; // Success
    } catch (error) {
      retries++;
      if (retries >= maxRetries) {
        console.error('Max retries reached:', error);
        // Store locally for later sync
        storeLocationLocally(location);
        return;
      }
      
      // Exponential backoff
      await sleep(delay);
      delay *= 2;
    }
  }
}
```

### 4. Offline Support

Store location updates locally when offline and sync when connection is restored:

```javascript
class OfflineLocationQueue {
  constructor() {
    this.queue = [];
  }

  addLocation(location) {
    this.queue.push({
      ...location,
      timestamp: Date.now(),
    });
    
    // Keep only last 100 locations
    if (this.queue.length > 100) {
      this.queue.shift();
    }
  }

  async syncLocations(agentId, apiClient) {
    while (this.queue.length > 0) {
      const location = this.queue[0];
      
      try {
        await apiClient.updateLocation(agentId, location);
        this.queue.shift(); // Remove synced location
      } catch (error) {
        console.error('Sync failed:', error);
        break; // Stop syncing on error
      }
    }
  }
}
```

### 5. Status-Based Updates

Adjust update frequency based on agent status:

```javascript
function getUpdateInterval(agentStatus) {
  switch (agentStatus) {
    case 'BUSY': // Active delivery
      return 30000; // 30 seconds
    case 'AVAILABLE': // Waiting for orders
      return 60000; // 1 minute
    case 'OFFLINE': // Not working
      return null; // No updates
    default:
      return 60000;
  }
}
```

## Testing Location Updates

### Manual Testing

```bash
# Update location for agent 123
curl -X PUT http://localhost:8085/api/delivery/agents/123/location \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 40.7128,
    "longitude": -74.0060
  }'

# Verify location in Redis
redis-cli GET "agent:location:123"

# Track delivery to see current location
curl http://localhost:8085/api/delivery/track/456
```

### Load Testing

Simulate multiple agents updating locations:

```bash
# Using Apache Bench
ab -n 1000 -c 10 -T 'application/json' \
  -p location.json \
  http://localhost:8085/api/delivery/agents/123/location
```

## Monitoring

### Key Metrics to Monitor

1. **Update Frequency**: Ensure updates arrive every ~30 seconds
2. **Update Latency**: Should be < 100ms
3. **Redis Hit Rate**: Location queries should hit Redis
4. **Failed Updates**: Track and alert on high failure rates

### Logging

```java
// Service logs location updates
log.debug("Updated location for agent {}: {}, {}", 
    agentId, latitude, longitude);

// Monitor in production
log.info("Location update stats - Agent: {}, Updates: {}, Avg Latency: {}ms",
    agentId, updateCount, avgLatency);
```

## Troubleshooting

### Location Not Updating

1. Check agent status is not OFFLINE
2. Verify Redis connection
3. Check network connectivity
4. Verify authentication token
5. Check location permissions

### High Latency

1. Check Redis performance
2. Verify network latency
3. Check database connection pool
4. Monitor server load

### Battery Drain

1. Reduce location accuracy when not delivering
2. Increase update interval for AVAILABLE status
3. Stop updates when OFFLINE
4. Use geofencing to trigger updates

## Security Considerations

1. **Authentication**: Always include JWT token in requests
2. **Authorization**: Agents can only update their own location
3. **Rate Limiting**: Prevent abuse with rate limits
4. **Data Privacy**: Location data is sensitive - handle with care

## Requirements Satisfied

- **5.5**: Location updates every 30 seconds ✅
- **6.1**: Real-time location tracking ✅
- **6.3**: Response time < 200ms ✅

