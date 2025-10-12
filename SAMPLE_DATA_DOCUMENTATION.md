# 🚗 Glide App - Comprehensive Sample Data Documentation

## 📊 **Overview**
The Glide app now includes comprehensive sample data for testing the hybrid Firebase setup. This data includes users, drivers, vehicles, commuters, and ride requests in both Firestore and Realtime Database.

## 🗄️ **Database Structure**

### **Firestore Collections (Permanent Storage)**

#### 1. **Users Collection** (`/users/`)
```json
{
  "sample_driver_001": {
    "email": "john.doe@glide.com",
    "role": "driver",
    "entityId": "driver_001"
  },
  "sample_driver_002": {
    "email": "jane.smith@glide.com", 
    "role": "driver",
    "entityId": "driver_002"
  },
  "sample_driver_003": {
    "email": "mike.johnson@glide.com",
    "role": "driver", 
    "entityId": "driver_003"
  },
  "sample_commuter_001": {
    "email": "blessing.moyo@glide.com",
    "role": "commuter",
    "entityId": "commuter_001"
  },
  "sample_commuter_002": {
    "email": "sarah.chikwava@glide.com",
    "role": "commuter",
    "entityId": "commuter_002"
  }
}
```

#### 2. **Drivers Collection** (`/drivers/`)
```json
{
  "driver_001": {
    "driverId": "driver_001",
    "uid": "sample_driver_001",
    "name": "John Doe",
    "gender": "Male",
    "currentLocation": {
      "lat": -17.82486,
      "lng": 31.05343,
      "address": "Harare CBD"
    },
    "status": "available",
    "rating": 4.9,
    "completedRides": 34
  },
  "driver_002": {
    "driverId": "driver_002", 
    "uid": "sample_driver_002",
    "name": "Jane Smith",
    "gender": "Female",
    "currentLocation": {
      "lat": -17.82765,
      "lng": 31.05612,
      "address": "Eastlea"
    },
    "status": "available",
    "rating": 4.7,
    "completedRides": 28
  },
  "driver_003": {
    "driverId": "driver_003",
    "uid": "sample_driver_003", 
    "name": "Mike Johnson",
    "gender": "Male",
    "currentLocation": {
      "lat": -17.82000,
      "lng": 31.05000,
      "address": "Avondale"
    },
    "status": "unavailable",
    "rating": 4.8,
    "completedRides": 42
  }
}
```

#### 3. **Vehicles Collection** (`/vehicles/`)
```json
{
  "vehicle_001": {
    "vehicleId": "vehicle_001",
    "driverId": "driver_001",
    "vehicleType": "Sedan",
    "plateNumber": "ABC123"
  },
  "vehicle_002": {
    "vehicleId": "vehicle_002",
    "driverId": "driver_002", 
    "vehicleType": "SUV",
    "plateNumber": "XYZ789"
  },
  "vehicle_003": {
    "vehicleId": "vehicle_003",
    "driverId": "driver_003",
    "vehicleType": "Hatchback", 
    "plateNumber": "DEF456"
  }
}
```

#### 4. **Commuters Collection** (`/commuters/`)
```json
{
  "commuter_001": {
    "commuterId": "commuter_001",
    "uid": "sample_commuter_001",
    "name": "Blessing Moyo",
    "currentLocation": {
      "lat": -17.82486,
      "lng": 31.05343,
      "address": "Harare CBD"
    }
  },
  "commuter_002": {
    "commuterId": "commuter_002",
    "uid": "sample_commuter_002", 
    "name": "Sarah Chikwava",
    "currentLocation": {
      "lat": -17.82765,
      "lng": 31.05612,
      "address": "Eastlea"
    }
  }
}
```

#### 5. **Ride Requests Collection** (`/rideRequests/`)
```json
{
  "ride_001": {
    "rideId": "ride_001",
    "commuterId": "commuter_001",
    "driverId": "driver_001",
    "pickupLocation": {
      "lat": -17.82486,
      "lng": 31.05343,
      "address": "Harare CBD"
    },
    "destination": {
      "lat": -17.82765,
      "lng": 31.05612,
      "address": "Eastlea"
    },
    "status": "completed",
    "people": 1,
    "priceEach": 5.0,
    "timestamp": [1 hour ago]
  },
  "ride_002": {
    "rideId": "ride_002",
    "commuterId": "commuter_002",
    "driverId": "driver_002",
    "pickupLocation": {
      "lat": -17.82765,
      "lng": 31.05612,
      "address": "Eastlea"
    },
    "destination": {
      "lat": -17.82000,
      "lng": 31.05000,
      "address": "Avondale"
    },
    "status": "pending",
    "people": 2,
    "priceEach": 4.5,
    "timestamp": [5 minutes ago]
  }
}
```

### **Realtime Database (Live Data)**

#### 1. **Live Drivers** (`/drivers_live/`)
```json
{
  "driver_001": {
    "status": "available",
    "location": {
      "lat": -17.82486,
      "lng": 31.05343,
      "timestamp": [current timestamp]
    }
  },
  "driver_002": {
    "status": "available",
    "location": {
      "lat": -17.82765,
      "lng": 31.05612,
      "timestamp": [current timestamp]
    }
  },
  "driver_003": {
    "status": "unavailable",
    "location": {
      "lat": -17.82000,
      "lng": 31.05000,
      "timestamp": [current timestamp]
    }
  }
}
```

#### 2. **Live Ride Requests** (`/rideRequestsLive/`)
```json
{
  "ride_002": {
    "rideId": "ride_002",
    "commuterId": "commuter_002",
    "driverId": "driver_002",
    "pickupLocation": {
      "lat": -17.82765,
      "lng": 31.05612,
      "address": "Eastlea"
    },
    "destination": {
      "lat": -17.82000,
      "lng": 31.05000,
      "address": "Avondale"
    },
    "status": "pending",
    "people": 2,
    "priceEach": 4.5,
    "timestamp": [5 minutes ago]
  }
}
```

## 🧪 **Testing Features**

### **Sample Accounts for Testing**
To test the app with real authentication, create these accounts in Firebase Console:

#### **Driver Accounts:**
- **Email:** john.doe@glide.com | **Password:** testpass123
- **Email:** jane.smith@glide.com | **Password:** testpass123  
- **Email:** mike.johnson@glide.com | **Password:** testpass123

#### **Commuter Accounts:**
- **Email:** blessing.moyo@glide.com | **Password:** testpass123
- **Email:** sarah.chikwava@glide.com | **Password:** testpass123

### **Test Data Manager**
The `TestDataManager` class provides utility methods for testing:

```java
TestDataManager testManager = new TestDataManager();

// Update driver locations
testManager.updateDriverLocations();

// Toggle driver status
testManager.toggleDriverStatus("driver_001", "unavailable");

// Create test ride request
testManager.createTestRideRequest("commuter_001", "driver_001");

// Get database statistics
testManager.getDatabaseStats();

// Clean up test data
testManager.cleanupTestData();
```

## 🗺️ **Location Data**
All sample data uses real locations in **Harare, Zimbabwe**:

- **Harare CBD:** -17.82486, 31.05343
- **Eastlea:** -17.82765, 31.05612  
- **Avondale:** -17.82000, 31.05000

## 🚀 **How to Use**

### **1. Initialize Sample Data**
The sample data is automatically initialized when `HomeCommuterActivity` starts:
```java
DataInitializer.initializeSampleData(this);
```

### **2. Test Commuter Flow**
1. Login as a commuter (blessing.moyo@glide.com or sarah.chikwava@glide.com)
2. Set a destination on the map
3. See available drivers (John Doe and Jane Smith)
4. Tap a driver marker to request a ride
5. Watch for real-time status updates

### **3. Test Driver Flow**
1. Login as a driver (john.doe@glide.com, jane.smith@glide.com, or mike.johnson@glide.com)
2. Toggle availability status
3. Receive ride requests in real-time
4. Accept or decline requests
5. Update location in real-time

### **4. Test Hybrid Database**
- **Firestore:** Contains permanent user data, driver profiles, vehicles, and ride history
- **Realtime Database:** Contains live location updates, driver status, and active ride requests
- **Synchronization:** Both databases stay in sync automatically

## 🔧 **Customization**

### **Add More Sample Data**
To add more sample data, modify the `DataInitializer.java` file:

```java
// Add more drivers
Map<String, Object> newDriver = new HashMap<>();
newDriver.put("driverId", "driver_004");
newDriver.put("name", "New Driver Name");
// ... add other fields
batch.set(firestore.collection("drivers").document("driver_004"), newDriver);
```

### **Change Locations**
Update the latitude and longitude values in the sample data to use different locations.

### **Modify Test Scenarios**
Use the `TestDataManager` to create custom test scenarios for different ride-sharing situations.

## ⚠️ **Important Notes**

1. **Sample data is for testing only** - Remove in production
2. **Firebase Auth users must be created manually** in Firebase Console
3. **Location data uses Harare, Zimbabwe** - Update for your region
4. **Data is automatically synchronized** between Firestore and Realtime Database
5. **Test data can be cleaned up** using the `TestDataManager.cleanupTestData()` method

## 🎯 **Testing Scenarios**

### **Scenario 1: Basic Ride Request**
1. Commuter sets destination
2. Driver appears on map
3. Commuter requests ride
4. Driver receives notification
5. Driver accepts/declines

### **Scenario 2: Real-time Location Updates**
1. Driver moves around
2. Location updates in Realtime Database
3. Commuter sees driver movement
4. Firestore updated periodically

### **Scenario 3: Multiple Drivers**
1. Multiple drivers available
2. Commuter can choose between drivers
3. Different vehicle types shown
4. Real-time availability updates

This comprehensive sample data setup allows you to test all aspects of the hybrid Firebase implementation with realistic data and scenarios.
