package com.kodelink.glide;

// Android logging utility
import android.util.Log;

// Google Play Services task management
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

// Firebase Authentication imports
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// Firebase Realtime Database imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Firebase Firestore imports
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

// Java collections and utilities
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FirebaseService - Centralized service for all Firebase operations
 * 
 * This singleton service handles all interactions with Firebase services including:
 * - Firebase Authentication for user management
 * - Firestore for persistent data storage (user profiles, ride history)
 * - Realtime Database for live data (driver locations, ride requests)
 * 
 * Key Features:
 * - User authentication and registration
 * - Driver and commuter profile management
 * - Real-time driver location tracking
 * - Ride request creation and management
 * - Ride history retrieval
 * - Database synchronization between Firestore and Realtime Database
 * 
 * Database Structure:
 * - Firestore: Users, drivers, commuters, vehicles, ride_history
 * - Realtime Database: driver_locations, ride_requests
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class FirebaseService {
    // Logging tag for debugging
    private static final String TAG = "FirebaseService";
    
    // Singleton instance
    private static FirebaseService instance;
    
    // Firebase service instances
    private FirebaseAuth auth;                  // Authentication service
    private FirebaseFirestore firestore;        // Firestore database service
    private DatabaseReference realtimeDb;       // Realtime database reference

    /**
     * Private constructor for singleton pattern
     * Initializes all Firebase services
     */
    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/").getReference();
    }

    /**
     * Gets the singleton instance of FirebaseService
     * @return FirebaseService instance
     */
    public static FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    // ==================== AUTHENTICATION ====================
    
    /**
     * Register a new user with email/password and create appropriate entity in Firestore
     */
    public Task<AuthResult> registerUser(String email, String password, String role) {
        return auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                String uid = result.getUser().getUid();
                Map<String, Object> user = new HashMap<>();
                user.put("email", email);
                user.put("role", role);
                
                // Create user document in Firestore
                firestore.collection("users").document(uid).set(user)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User document created in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to create user document", e));
            });
    }

    /**
     * Get current user's role from Firestore
     */
    public Task<String> getUserRole(String uid) {
        return firestore.collection("users").document(uid).get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    return task.getResult().getString("role");
                }
                return null;
            });
    }

    // ==================== DRIVER OPERATIONS ====================
    
    /**
     * Create a new driver in Firestore
     */
    public Task<Void> createDriver(String uid, String name, String gender, String vehicleType, String vehiclePlate) {
        WriteBatch batch = firestore.batch();
        
        // Create driver document
        String driverId = firestore.collection("drivers").document().getId();
        Map<String, Object> driverData = new HashMap<>();
        driverData.put("driverId", driverId);
        driverData.put("uid", uid);
        driverData.put("name", name);
        driverData.put("gender", gender);
        driverData.put("currentLocation", new HashMap<String, Object>() {{
            put("lat", -17.82486);  // Harare CBD default location
            put("lng", 31.05343);   // Harare CBD default location
            put("address", "Location not set");
        }});
        driverData.put("status", "unavailable");
        driverData.put("rating", 0.0);
        driverData.put("completedRides", 0);
        
        DocumentReference driverRef = firestore.collection("drivers").document(driverId);
        batch.set(driverRef, driverData);
        
        // Create vehicle document
        String vehicleId = firestore.collection("vehicles").document().getId();
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("vehicleId", vehicleId);
        vehicleData.put("driverId", driverId);
        vehicleData.put("vehicleType", vehicleType);
        vehicleData.put("plateNumber", vehiclePlate);
        
        DocumentReference vehicleRef = firestore.collection("vehicles").document(vehicleId);
        batch.set(vehicleRef, vehicleData);
        
        // Create or update user document with entityId
        DocumentReference userRef = firestore.collection("users").document(uid);
        Map<String, Object> userData = new HashMap<>();
        userData.put("role", "driver");
        userData.put("entityId", driverId);
        batch.set(userRef, userData, SetOptions.merge());
        
        return batch.commit()
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    // Create live driver data in Realtime Database
                    createDriverLiveData(driverId, -17.82486, 31.05343, "unavailable");
                    return Tasks.forResult(null);
                } else {
                    throw task.getException();
                }
            });
    }

    /**
     * Create live driver data in Realtime Database
     */
    private void createDriverLiveData(String driverId, double lat, double lng, String status) {
        Log.d(TAG, "Creating live driver data for: " + driverId);
        
        // Create driver status
        realtimeDb.child("drivers_live").child(driverId).child("status").setValue(status);
        
        // Create driver location
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("lat", lat);
        locationData.put("lng", lng);
        locationData.put("timestamp", System.currentTimeMillis());
        realtimeDb.child("drivers_live").child(driverId).child("location").setValue(locationData);
        
        Log.d(TAG, "Live driver data created for: " + driverId + " at " + lat + ", " + lng);
    }

    /**
     * Update driver location in Realtime Database (for live updates)
     */
    public void updateDriverLocationLive(String driverId, double lat, double lng) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("lat", lat);
        locationData.put("lng", lng);
        locationData.put("timestamp", System.currentTimeMillis());
        
        realtimeDb.child("drivers_live").child(driverId).child("location").setValue(locationData);
    }

    /**
     * Update driver location in Firestore (for permanent storage)
     */
    public Task<Void> updateDriverLocationFirestore(String driverId, double lat, double lng) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("lat", lat);
        locationData.put("lng", lng);
        
        return firestore.collection("drivers").document(driverId)
            .update("currentLocation", locationData);
    }

    /**
     * Update driver status in both databases
     */
    public void updateDriverStatus(String driverId, String status) {
        // Update Firestore
        firestore.collection("drivers").document(driverId)
            .update("status", status);
        
        // Update Realtime Database
        realtimeDb.child("drivers_live").child(driverId).child("status").setValue(status);
    }

    /**
     * Listen for available drivers from Realtime Database
     */
    public void listenAvailableDrivers(ValueEventListener listener) {
        realtimeDb.child("drivers_live")
            .orderByChild("status")
            .equalTo("available")
            .addValueEventListener(listener);
    }

    /**
     * Get driver details from Firestore
     */
    public Task<Driver> getDriverDetails(String driverId) {
        return firestore.collection("drivers").document(driverId).get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot doc = task.getResult();
                    Driver driver = new Driver();
                    driver.driverId = doc.getString("driverId");
                    driver.uid = doc.getString("uid");
                    driver.name = doc.getString("name");
                    driver.gender = doc.getString("gender");
                    driver.status = doc.getString("status");
                    driver.rating = doc.getDouble("rating");
                    driver.completedRides = doc.getLong("completedRides").intValue();
                    
                    // Parse location data
                    Map<String, Object> locationMap = (Map<String, Object>) doc.get("currentLocation");
                    if (locationMap != null) {
                        driver.currentLocation = new Driver.LocationData(
                            ((Number) locationMap.get("lat")).doubleValue(),
                            ((Number) locationMap.get("lng")).doubleValue(),
                            (String) locationMap.get("address")
                        );
                    }
                    
                    return driver;
                }
                return null;
            });
    }

    /**
     * Get commuter details from Firestore
     */
    public Task<Commuter> getCommuterDetails(String commuterId) {
        return firestore.collection("commuters").document(commuterId).get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot doc = task.getResult();
                    Commuter commuter = new Commuter();
                    commuter.commuterId = doc.getString("commuterId");
                    commuter.uid = doc.getString("uid");
                    commuter.name = doc.getString("name");
                    
                    // Parse location data
                    Map<String, Object> locationMap = (Map<String, Object>) doc.get("currentLocation");
                    if (locationMap != null) {
                        commuter.currentLocation = new Commuter.LocationData(
                            ((Number) locationMap.get("lat")).doubleValue(),
                            ((Number) locationMap.get("lng")).doubleValue(),
                            (String) locationMap.get("address")
                        );
                    }
                    
                    return commuter;
                }
                return null;
            });
    }

    // ==================== COMMUTER OPERATIONS ====================
    
    /**
     * Create a new commuter in Firestore
     */
    public Task<Void> createCommuter(String uid, String name) {
        String commuterId = firestore.collection("commuters").document().getId();
        Map<String, Object> commuterData = new HashMap<>();
        commuterData.put("commuterId", commuterId);
        commuterData.put("uid", uid);
        commuterData.put("name", name);
        commuterData.put("currentLocation", new HashMap<String, Object>() {{
            put("lat", -17.82486);  // Harare CBD default location
            put("lng", 31.05343);   // Harare CBD default location
            put("address", "Location not set");
        }});
        
        // Create commuter document
        DocumentReference commuterRef = firestore.collection("commuters").document(commuterId);
        
        // Create or update user document with entityId
        DocumentReference userRef = firestore.collection("users").document(uid);
        Map<String, Object> userData = new HashMap<>();
        userData.put("role", "commuter");
        userData.put("entityId", commuterId);
        
        WriteBatch batch = firestore.batch();
        batch.set(commuterRef, commuterData);
        batch.set(userRef, userData, SetOptions.merge());
        
        return batch.commit();
    }

    /**
     * Update commuter location
     */
    public Task<Void> updateCommuterLocation(String commuterId, double lat, double lng) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("lat", lat);
        locationData.put("lng", lng);
        
        return firestore.collection("commuters").document(commuterId)
            .update("currentLocation", locationData);
    }

    // ==================== RIDE REQUEST OPERATIONS ====================
    
    /**
     * Create a new ride request in both Firestore and Realtime Database
     */
    public Task<Void> createRideRequest(String commuterId, String driverId, 
                                       RideRequest.LocationData pickup, RideRequest.LocationData destination,
                                       int people, double priceEach) {
        String rideId = firestore.collection("rideRequests").document().getId();
        
        // Create ride request data for Firestore
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("rideId", rideId);
        firestoreData.put("commuterId", commuterId);
        firestoreData.put("driverId", driverId);
        firestoreData.put("pickupLocation", pickup);
        firestoreData.put("destination", destination);
        firestoreData.put("status", "pending");
        firestoreData.put("people", people);
        firestoreData.put("priceEach", priceEach);
        firestoreData.put("timestamp", FieldValue.serverTimestamp());
        
        // Create ride request data for Realtime Database (with regular timestamp)
        Map<String, Object> realtimeData = new HashMap<>();
        realtimeData.put("rideId", rideId);
        realtimeData.put("commuterId", commuterId);
        realtimeData.put("driverId", driverId);
        realtimeData.put("pickupLocation", pickup);
        realtimeData.put("destination", destination);
        realtimeData.put("status", "pending");
        realtimeData.put("people", people);
        realtimeData.put("priceEach", priceEach);
        realtimeData.put("timestamp", System.currentTimeMillis());
        
        // Create in Firestore
        DocumentReference rideRef = firestore.collection("rideRequests").document(rideId);
        
        // Create in Realtime Database for live updates
        DatabaseReference liveRideRef = realtimeDb.child("rideRequestsLive").child(rideId);
        
        return rideRef.set(firestoreData)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    return liveRideRef.setValue(realtimeData);
                } else {
                    throw task.getException();
            }
        });
    }

    /**
     * Update ride request status in both databases
     */
    public Task<Void> updateRideRequestStatus(String rideId, String status) {
        // Update Firestore
        Task<Void> firestoreTask = firestore.collection("rideRequests").document(rideId)
            .update("status", status);
        
        // Update Realtime Database
        Task<Void> realtimeTask = realtimeDb.child("rideRequestsLive").child(rideId).child("status").setValue(status);
        
        return Tasks.whenAll(firestoreTask, realtimeTask);
    }

    /**
     * Listen for ride requests for a specific driver
     */
    public void listenDriverRideRequests(String driverId, ValueEventListener listener) {
        realtimeDb.child("rideRequestsLive")
            .orderByChild("driverId")
            .equalTo(driverId)
            .addValueEventListener(listener);
    }

    /**
     * Listen for ride request updates for a specific ride
     */
    public void listenRideRequest(String rideId, ValueEventListener listener) {
        realtimeDb.child("rideRequestsLive").child(rideId).addValueEventListener(listener);
    }

    /**
     * Get ride request details from Firestore
     */
    public Task<RideRequest> getRideRequestDetails(String rideId) {
        return firestore.collection("rideRequests").document(rideId).get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot doc = task.getResult();
                    RideRequest rideRequest = new RideRequest();
                    rideRequest.rideId = doc.getString("rideId");
                    rideRequest.commuterId = doc.getString("commuterId");
                    rideRequest.driverId = doc.getString("driverId");
                    rideRequest.status = doc.getString("status");
                    rideRequest.people = doc.getLong("people").intValue();
                    rideRequest.priceEach = doc.getDouble("priceEach");
                    rideRequest.timestamp = doc.getTimestamp("timestamp").getSeconds() * 1000;
                    
                    // Parse location data
                    Map<String, Object> pickupMap = (Map<String, Object>) doc.get("pickupLocation");
                    if (pickupMap != null) {
                        rideRequest.pickupLocation = new RideRequest.LocationData(
                            ((Number) pickupMap.get("lat")).doubleValue(),
                            ((Number) pickupMap.get("lng")).doubleValue(),
                            (String) pickupMap.get("address")
                        );
                    }
                    
                    Map<String, Object> destMap = (Map<String, Object>) doc.get("destination");
                    if (destMap != null) {
                        rideRequest.destination = new RideRequest.LocationData(
                            ((Number) destMap.get("lat")).doubleValue(),
                            ((Number) destMap.get("lng")).doubleValue(),
                            (String) destMap.get("address")
                        );
                    }
                    
                    return rideRequest;
                }
                return null;
            });
    }

    /**
     * Clean up completed ride from Realtime Database
     */
    public void cleanupRideRequestLive(String rideId) {
        realtimeDb.child("rideRequestsLive").child(rideId).removeValue();
    }

    // ==================== VEHICLE OPERATIONS ====================
    
    /**
     * Get vehicle details for a driver
     */
    public Task<Vehicle> getVehicleDetails(String driverId) {
        return firestore.collection("vehicles")
            .whereEqualTo("driverId", driverId)
            .limit(1)
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    Vehicle vehicle = new Vehicle();
                    vehicle.vehicleId = doc.getString("vehicleId");
                    vehicle.driverId = doc.getString("driverId");
                    vehicle.vehicleType = doc.getString("vehicleType");
                    vehicle.plateNumber = doc.getString("plateNumber");
                    return vehicle;
                }
                return null;
            });
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Get current user's entity ID (driverId or commuterId)
     */
    public Task<String> getCurrentUserEntityId() {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("No authenticated user"));
        }
        
        return firestore.collection("users").document(auth.getCurrentUser().getUid()).get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    return task.getResult().getString("entityId");
                }
                return null;
            });
    }

    /**
     * Get current user's role
     */
    public Task<String> getCurrentUserRole() {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("No authenticated user"));
        }
        
        return getUserRole(auth.getCurrentUser().getUid());
    }

    // ==================== RIDE HISTORY ====================
    
    /**
     * Get ride history for a driver
     */
    public Task<List<RideRequest>> getDriverRideHistory(String driverId) {
        return firestore.collection("rideRequests")
            .whereEqualTo("driverId", driverId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .continueWith(task -> {
                List<RideRequest> rides = new ArrayList<>();
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RideRequest ride = parseRideRequest(doc);
                        if (ride != null) {
                            rides.add(ride);
                        }
                    }
                }
                return rides;
            });
    }

    /**
     * Get ride history for a commuter
     */
    public Task<List<RideRequest>> getCommuterRideHistory(String commuterId) {
        return firestore.collection("rideRequests")
            .whereEqualTo("commuterId", commuterId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .continueWith(task -> {
                List<RideRequest> rides = new ArrayList<>();
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RideRequest ride = parseRideRequest(doc);
                        if (ride != null) {
                            rides.add(ride);
                        }
                    }
                }
                return rides;
            });
    }

    /**
     * Parse RideRequest from Firestore document
     */
    private RideRequest parseRideRequest(DocumentSnapshot doc) {
        try {
            RideRequest ride = new RideRequest();
            ride.rideId = doc.getString("rideId");
            ride.commuterId = doc.getString("commuterId");
            ride.driverId = doc.getString("driverId");
            ride.status = doc.getString("status");
            ride.people = doc.getLong("people").intValue();
            ride.priceEach = doc.getDouble("priceEach");
            
            // Parse timestamp
            Object timestamp = doc.get("timestamp");
            if (timestamp instanceof com.google.firebase.Timestamp) {
                ride.timestamp = ((com.google.firebase.Timestamp) timestamp).toDate().getTime();
            } else if (timestamp instanceof Long) {
                ride.timestamp = (Long) timestamp;
            } else {
                ride.timestamp = System.currentTimeMillis();
            }
            
            // Parse pickup location
            Map<String, Object> pickupMap = (Map<String, Object>) doc.get("pickupLocation");
            if (pickupMap != null) {
                ride.pickupLocation = new RideRequest.LocationData(
                    ((Number) pickupMap.get("lat")).doubleValue(),
                    ((Number) pickupMap.get("lng")).doubleValue(),
                    (String) pickupMap.get("address")
                );
            }
            
            // Parse destination location
            Map<String, Object> destMap = (Map<String, Object>) doc.get("destination");
            if (destMap != null) {
                ride.destination = new RideRequest.LocationData(
                    ((Number) destMap.get("lat")).doubleValue(),
                    ((Number) destMap.get("lng")).doubleValue(),
                    (String) destMap.get("address")
                );
            }
            
            return ride;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing ride request", e);
            return null;
        }
    }

    // ==================== CALLBACK INTERFACES ====================
    
    public interface DatabaseCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface RideRequestListener {
        void onRideRequestUpdated(RideRequest rideRequest);
        void onError(String error);
    }

    public interface DriversListener {
        void onDriversReceived(List<Driver> drivers);
        void onError(String error);
    }
}