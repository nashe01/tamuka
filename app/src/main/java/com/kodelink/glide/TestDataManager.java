package com.kodelink.glide;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class for managing test data and database operations
 * This class provides methods to create, update, and clean test data
 */
public class TestDataManager {
    private static final String TAG = "TestDataManager";
    
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;
    private FirebaseAuth auth;
    
    public TestDataManager() {
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/").getReference();
        auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Create sample Firebase Auth users for testing
     * Note: This requires Firebase Admin SDK or manual creation in Firebase Console
     */
    public void createSampleAuthUsers() {
        Log.d(TAG, "Creating sample auth users...");
        
        // These would need to be created manually in Firebase Console or using Admin SDK
        // For now, we'll just log the credentials that should be created
        
        String[] driverEmails = {
            "john.doe@glide.com",
            "jane.smith@glide.com", 
            "mike.johnson@glide.com"
        };
        
        String[] commuterEmails = {
            "blessing.moyo@glide.com",
            "sarah.chikwava@glide.com"
        };
        
        Log.d(TAG, "Sample Driver Accounts to create:");
        for (String email : driverEmails) {
            Log.d(TAG, "Email: " + email + " | Password: testpass123 | Role: driver");
        }
        
        Log.d(TAG, "Sample Commuter Accounts to create:");
        for (String email : commuterEmails) {
            Log.d(TAG, "Email: " + email + " | Password: testpass123 | Role: commuter");
        }
    }
    
    /**
     * Update driver locations for testing
     */
    public void updateDriverLocations() {
        Log.d(TAG, "Updating driver locations...");
        
        // Update Driver 1 location
        DatabaseReference driver1Ref = realtimeDb.child("drivers_live").child("driver_001");
        driver1Ref.child("location").child("lat").setValue(-17.82486 + (Math.random() - 0.5) * 0.01);
        driver1Ref.child("location").child("lng").setValue(31.05343 + (Math.random() - 0.5) * 0.01);
        driver1Ref.child("location").child("timestamp").setValue(System.currentTimeMillis());
        
        // Update Driver 2 location
        DatabaseReference driver2Ref = realtimeDb.child("drivers_live").child("driver_002");
        driver2Ref.child("location").child("lat").setValue(-17.82765 + (Math.random() - 0.5) * 0.01);
        driver2Ref.child("location").child("lng").setValue(31.05612 + (Math.random() - 0.5) * 0.01);
        driver2Ref.child("location").child("timestamp").setValue(System.currentTimeMillis());
        
        Log.d(TAG, "Driver locations updated");
    }
    
    /**
     * Simulate driver status changes
     */
    public void toggleDriverStatus(String driverId, String newStatus) {
        Log.d(TAG, "Toggling driver " + driverId + " status to " + newStatus);
        
        // Update Realtime Database
        realtimeDb.child("drivers_live").child(driverId).child("status").setValue(newStatus);
        
        // Update Firestore
        firestore.collection("drivers").document(driverId)
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Driver status updated in Firestore"))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to update driver status in Firestore", e));
    }
    
    /**
     * Create a test ride request
     */
    public void createTestRideRequest(String commuterId, String driverId) {
        Log.d(TAG, "Creating test ride request...");
        
        String rideId = "test_ride_" + System.currentTimeMillis();
        
        // Create ride request data
        java.util.Map<String, Object> rideData = new java.util.HashMap<>();
        rideData.put("rideId", rideId);
        rideData.put("commuterId", commuterId);
        rideData.put("driverId", driverId);
        
        java.util.Map<String, Object> pickup = new java.util.HashMap<>();
        pickup.put("lat", -17.82486);
        pickup.put("lng", 31.05343);
        pickup.put("address", "Test Pickup Location");
        rideData.put("pickupLocation", pickup);
        
        java.util.Map<String, Object> destination = new java.util.HashMap<>();
        destination.put("lat", -17.82765);
        destination.put("lng", 31.05612);
        destination.put("address", "Test Destination");
        rideData.put("destination", destination);
        
        rideData.put("status", "pending");
        rideData.put("people", 1);
        rideData.put("priceEach", 5.0);
        rideData.put("timestamp", System.currentTimeMillis());
        
        // Create in Firestore
        firestore.collection("rideRequests").document(rideId).set(rideData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Test ride request created in Firestore");
                
                // Create in Realtime Database
                realtimeDb.child("rideRequestsLive").child(rideId).setValue(rideData)
                    .addOnSuccessListener(aVoid1 -> Log.d(TAG, "Test ride request created in Realtime Database"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to create test ride request in Realtime Database", e));
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to create test ride request in Firestore", e));
    }
    
    /**
     * Clean up test data
     */
    public void cleanupTestData() {
        Log.d(TAG, "Cleaning up test data...");
        
        // Remove test ride requests
        realtimeDb.child("rideRequestsLive").orderByChild("rideId").startAt("test_ride_").endAt("test_ride_\uf8ff")
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();
                    }
                    Log.d(TAG, "Test ride requests cleaned up from Realtime Database");
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    Log.e(TAG, "Failed to clean up test ride requests", databaseError.toException());
                }
            });
        
        // Remove test ride requests from Firestore
        firestore.collection("rideRequests").whereGreaterThanOrEqualTo("rideId", "test_ride_")
            .whereLessThan("rideId", "test_ride_\uf8ff")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
                Log.d(TAG, "Test ride requests cleaned up from Firestore");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up test ride requests from Firestore", e));
    }
    
    /**
     * Get database statistics
     */
    public void getDatabaseStats() {
        Log.d(TAG, "Getting database statistics...");
        
        // Count drivers
        firestore.collection("drivers").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Total drivers in Firestore: " + querySnapshot.size()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to count drivers", e));
        
        // Count commuters
        firestore.collection("commuters").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Total commuters in Firestore: " + querySnapshot.size()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to count commuters", e));
        
        // Count ride requests
        firestore.collection("rideRequests").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Total ride requests in Firestore: " + querySnapshot.size()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to count ride requests", e));
        
        // Count live drivers
        realtimeDb.child("drivers_live").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Log.d(TAG, "Total live drivers in Realtime Database: " + dataSnapshot.getChildrenCount());
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to count live drivers", databaseError.toException());
            }
        });
        
        // Count live ride requests
        realtimeDb.child("rideRequestsLive").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Log.d(TAG, "Total live ride requests in Realtime Database: " + dataSnapshot.getChildrenCount());
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to count live ride requests", databaseError.toException());
            }
        });
    }
    
    /**
     * Verify database synchronization
     */
    public void verifyDatabaseSync() {
        Log.d(TAG, "Starting database synchronization verification...");
        new DatabaseSyncVerifier().verifySampleDataSync();
    }
    
    /**
     * Fix all synchronization issues
     */
    public void fixDatabaseSync() {
        Log.d(TAG, "Fixing database synchronization issues...");
        new DatabaseSyncVerifier().fixAllSyncIssues();
    }
}
