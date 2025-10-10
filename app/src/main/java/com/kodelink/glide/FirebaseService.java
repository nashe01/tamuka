package com.kodelink.glide;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static FirebaseService instance;
    
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;

    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/").getReference();
    }

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
            put("lat", 0.0);
            put("lng", 0.0);
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
        
        return batch.commit();
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
            put("lat", 0.0);
            put("lng", 0.0);
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
        
        // Create ride request data
        Map<String, Object> rideData = new HashMap<>();
        rideData.put("rideId", rideId);
        rideData.put("commuterId", commuterId);
        rideData.put("driverId", driverId);
        rideData.put("pickupLocation", pickup);
        rideData.put("destination", destination);
        rideData.put("status", "pending");
        rideData.put("people", people);
        rideData.put("priceEach", priceEach);
        rideData.put("timestamp", FieldValue.serverTimestamp());
        
        // Create in Firestore
        DocumentReference rideRef = firestore.collection("rideRequests").document(rideId);
        
        // Create in Realtime Database for live updates
        DatabaseReference liveRideRef = realtimeDb.child("rideRequestsLive").child(rideId);
        
        return rideRef.set(rideData)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    return liveRideRef.setValue(rideData);
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