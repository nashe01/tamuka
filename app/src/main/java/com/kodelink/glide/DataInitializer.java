package com.kodelink.glide;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class DataInitializer {
    private static final String TAG = "DataInitializer";
    
    public static void initializeSampleData(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/");
        DatabaseReference databaseRef = database.getReference();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        
        Log.d(TAG, "Starting database cleanup and verification...");
        
        // Clean up any test data first
        new DatabaseCleanup().cleanupAllTestData();
        
        // Clean up any invalid data from previous testing
        new DatabaseSyncVerifier().fixAllSyncIssues();
        
        Log.d(TAG, "Database cleanup and verification completed");
    }
    
    private static void initializeSampleUsers(FirebaseFirestore firestore) {
        WriteBatch batch = firestore.batch();
        
        // Sample Driver Users
        Map<String, Object> driver1User = new HashMap<>();
        driver1User.put("email", "john.doe@glide.com");
        driver1User.put("role", "driver");
        driver1User.put("entityId", "driver_001");
        batch.set(firestore.collection("users").document("sample_driver_001"), driver1User);
        
        Map<String, Object> driver2User = new HashMap<>();
        driver2User.put("email", "jane.smith@glide.com");
        driver2User.put("role", "driver");
        driver2User.put("entityId", "driver_002");
        batch.set(firestore.collection("users").document("sample_driver_002"), driver2User);
        
        Map<String, Object> driver3User = new HashMap<>();
        driver3User.put("email", "mike.johnson@glide.com");
        driver3User.put("role", "driver");
        driver3User.put("entityId", "driver_003");
        batch.set(firestore.collection("users").document("sample_driver_003"), driver3User);
        
        // Sample Commuter Users
        Map<String, Object> commuter1User = new HashMap<>();
        commuter1User.put("email", "blessing.moyo@glide.com");
        commuter1User.put("role", "commuter");
        commuter1User.put("entityId", "commuter_001");
        batch.set(firestore.collection("users").document("sample_commuter_001"), commuter1User);
        
        Map<String, Object> commuter2User = new HashMap<>();
        commuter2User.put("email", "sarah.chikwava@glide.com");
        commuter2User.put("role", "commuter");
        commuter2User.put("entityId", "commuter_002");
        batch.set(firestore.collection("users").document("sample_commuter_002"), commuter2User);
        
        batch.commit().addOnSuccessListener(aVoid -> 
            Log.d(TAG, "Sample users created successfully"))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Failed to create sample users", e));
    }
    
    private static void initializeSampleDrivers(FirebaseFirestore firestore, DatabaseReference databaseRef) {
        WriteBatch batch = firestore.batch();
        
        // Sample Driver 1
        Map<String, Object> driver1 = new HashMap<>();
        driver1.put("driverId", "driver_001");
        driver1.put("uid", "sample_driver_001");
        driver1.put("name", "John Doe");
        driver1.put("gender", "Male");
        Map<String, Object> location1 = new HashMap<>();
        location1.put("lat", -17.82486);
        location1.put("lng", 31.05343);
        location1.put("address", "Harare CBD");
        driver1.put("currentLocation", location1);
        driver1.put("status", "available");
        driver1.put("rating", 4.9);
        driver1.put("completedRides", 34);
        batch.set(firestore.collection("drivers").document("driver_001"), driver1);
        
        // Sample Driver 2
        Map<String, Object> driver2 = new HashMap<>();
        driver2.put("driverId", "driver_002");
        driver2.put("uid", "sample_driver_002");
        driver2.put("name", "Jane Smith");
        driver2.put("gender", "Female");
        Map<String, Object> location2 = new HashMap<>();
        location2.put("lat", -17.82765);
        location2.put("lng", 31.05612);
        location2.put("address", "Eastlea");
        driver2.put("currentLocation", location2);
        driver2.put("status", "available");
        driver2.put("rating", 4.7);
        driver2.put("completedRides", 28);
        batch.set(firestore.collection("drivers").document("driver_002"), driver2);
        
        // Sample Driver 3
        Map<String, Object> driver3 = new HashMap<>();
        driver3.put("driverId", "driver_003");
        driver3.put("uid", "sample_driver_003");
        driver3.put("name", "Mike Johnson");
        driver3.put("gender", "Male");
        Map<String, Object> location3 = new HashMap<>();
        location3.put("lat", -17.82000);
        location3.put("lng", 31.05000);
        location3.put("address", "Avondale");
        driver3.put("currentLocation", location3);
        driver3.put("status", "unavailable");
        driver3.put("rating", 4.8);
        driver3.put("completedRides", 42);
        batch.set(firestore.collection("drivers").document("driver_003"), driver3);
        
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Sample drivers created successfully in Firestore");
            // Now create corresponding live data in Realtime Database
            createDriverLiveData(databaseRef);
        }).addOnFailureListener(e -> 
            Log.e(TAG, "Failed to create sample drivers", e));
    }
    
    /**
     * Create synchronized live driver data in Realtime Database
     */
    private static void createDriverLiveData(DatabaseReference databaseRef) {
        Log.d(TAG, "Creating synchronized live driver data...");
        
        // Sample Driver 1 - Live data in Realtime Database
        DatabaseReference driver1Ref = databaseRef.child("drivers_live").child("driver_001");
        driver1Ref.child("status").setValue("available");
        driver1Ref.child("location").child("lat").setValue(-17.82486);
        driver1Ref.child("location").child("lng").setValue(31.05343);
        driver1Ref.child("location").child("timestamp").setValue(System.currentTimeMillis());
        
        // Sample Driver 2 - Live data in Realtime Database
        DatabaseReference driver2Ref = databaseRef.child("drivers_live").child("driver_002");
        driver2Ref.child("status").setValue("available");
        driver2Ref.child("location").child("lat").setValue(-17.82765);
        driver2Ref.child("location").child("lng").setValue(31.05612);
        driver2Ref.child("location").child("timestamp").setValue(System.currentTimeMillis());
        
        // Sample Driver 3 - Live data in Realtime Database
        DatabaseReference driver3Ref = databaseRef.child("drivers_live").child("driver_003");
        driver3Ref.child("status").setValue("unavailable");
        driver3Ref.child("location").child("lat").setValue(-17.82000);
        driver3Ref.child("location").child("lng").setValue(31.05000);
        driver3Ref.child("location").child("timestamp").setValue(System.currentTimeMillis());
        
        Log.d(TAG, "Live driver data created successfully in Realtime Database");
    }
    
    private static void initializeSampleVehicles(FirebaseFirestore firestore) {
        WriteBatch batch = firestore.batch();
        
        // Vehicle for Driver 1
        Map<String, Object> vehicle1 = new HashMap<>();
        vehicle1.put("vehicleId", "vehicle_001");
        vehicle1.put("driverId", "driver_001");
        vehicle1.put("vehicleType", "Sedan");
        vehicle1.put("plateNumber", "ABC123");
        batch.set(firestore.collection("vehicles").document("vehicle_001"), vehicle1);
        
        // Vehicle for Driver 2
        Map<String, Object> vehicle2 = new HashMap<>();
        vehicle2.put("vehicleId", "vehicle_002");
        vehicle2.put("driverId", "driver_002");
        vehicle2.put("vehicleType", "SUV");
        vehicle2.put("plateNumber", "XYZ789");
        batch.set(firestore.collection("vehicles").document("vehicle_002"), vehicle2);
        
        // Vehicle for Driver 3
        Map<String, Object> vehicle3 = new HashMap<>();
        vehicle3.put("vehicleId", "vehicle_003");
        vehicle3.put("driverId", "driver_003");
        vehicle3.put("vehicleType", "Hatchback");
        vehicle3.put("plateNumber", "DEF456");
        batch.set(firestore.collection("vehicles").document("vehicle_003"), vehicle3);
        
        batch.commit().addOnSuccessListener(aVoid -> 
            Log.d(TAG, "Sample vehicles created successfully"))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Failed to create sample vehicles", e));
    }
    
    private static void initializeSampleCommuters(FirebaseFirestore firestore) {
        WriteBatch batch = firestore.batch();
        
        // Sample Commuter 1
        Map<String, Object> commuter1 = new HashMap<>();
        commuter1.put("commuterId", "commuter_001");
        commuter1.put("uid", "sample_commuter_001");
        commuter1.put("name", "Blessing Moyo");
        Map<String, Object> commuterLocation1 = new HashMap<>();
        commuterLocation1.put("lat", -17.82486);
        commuterLocation1.put("lng", 31.05343);
        commuterLocation1.put("address", "Harare CBD");
        commuter1.put("currentLocation", commuterLocation1);
        batch.set(firestore.collection("commuters").document("commuter_001"), commuter1);
        
        // Sample Commuter 2
        Map<String, Object> commuter2 = new HashMap<>();
        commuter2.put("commuterId", "commuter_002");
        commuter2.put("uid", "sample_commuter_002");
        commuter2.put("name", "Sarah Chikwava");
        Map<String, Object> commuterLocation2 = new HashMap<>();
        commuterLocation2.put("lat", -17.82765);
        commuterLocation2.put("lng", 31.05612);
        commuterLocation2.put("address", "Eastlea");
        commuter2.put("currentLocation", commuterLocation2);
        batch.set(firestore.collection("commuters").document("commuter_002"), commuter2);
        
        batch.commit().addOnSuccessListener(aVoid -> 
            Log.d(TAG, "Sample commuters created successfully"))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Failed to create sample commuters", e));
    }
    
    
    private static void initializeSampleRideRequests(FirebaseFirestore firestore, DatabaseReference databaseRef) {
        WriteBatch batch = firestore.batch();
        
        // Sample Ride Request 1 (Completed)
        String rideId1 = "ride_001";
        Map<String, Object> rideRequest1 = new HashMap<>();
        rideRequest1.put("rideId", rideId1);
        rideRequest1.put("commuterId", "commuter_001");
        rideRequest1.put("driverId", "driver_001");
        
        Map<String, Object> pickup1 = new HashMap<>();
        pickup1.put("lat", -17.82486);
        pickup1.put("lng", 31.05343);
        pickup1.put("address", "Harare CBD");
        rideRequest1.put("pickupLocation", pickup1);
        
        Map<String, Object> destination1 = new HashMap<>();
        destination1.put("lat", -17.82765);
        destination1.put("lng", 31.05612);
        destination1.put("address", "Eastlea");
        rideRequest1.put("destination", destination1);
        
        rideRequest1.put("status", "completed");
        rideRequest1.put("people", 1);
        rideRequest1.put("priceEach", 5.0);
        rideRequest1.put("timestamp", System.currentTimeMillis() - 3600000); // 1 hour ago
        
        batch.set(firestore.collection("rideRequests").document(rideId1), rideRequest1);
        
        // Sample Ride Request 2 (Pending)
        String rideId2 = "ride_002";
        Map<String, Object> rideRequest2 = new HashMap<>();
        rideRequest2.put("rideId", rideId2);
        rideRequest2.put("commuterId", "commuter_002");
        rideRequest2.put("driverId", "driver_002");
        
        Map<String, Object> pickup2 = new HashMap<>();
        pickup2.put("lat", -17.82765);
        pickup2.put("lng", 31.05612);
        pickup2.put("address", "Eastlea");
        rideRequest2.put("pickupLocation", pickup2);
        
        Map<String, Object> destination2 = new HashMap<>();
        destination2.put("lat", -17.82000);
        destination2.put("lng", 31.05000);
        destination2.put("address", "Avondale");
        rideRequest2.put("destination", destination2);
        
        rideRequest2.put("status", "pending");
        rideRequest2.put("people", 2);
        rideRequest2.put("priceEach", 4.5);
        rideRequest2.put("timestamp", System.currentTimeMillis() - 300000); // 5 minutes ago
        
        batch.set(firestore.collection("rideRequests").document(rideId2), rideRequest2);
        
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Sample ride requests created successfully in Firestore");
            
            // Create corresponding live data in Realtime Database ONLY for pending rides
            databaseRef.child("rideRequestsLive").child(rideId2).setValue(rideRequest2)
                .addOnSuccessListener(aVoid1 -> 
                    Log.d(TAG, "Pending ride request added to Realtime Database"))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Failed to add pending ride to Realtime Database", e));
        }).addOnFailureListener(e -> 
            Log.e(TAG, "Failed to create sample ride requests", e));
    }
    
    
}


