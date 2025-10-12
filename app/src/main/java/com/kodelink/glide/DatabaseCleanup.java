package com.kodelink.glide;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class for cleaning up database data
 * Use this to clean up test data or invalid entries
 */
public class DatabaseCleanup {
    private static final String TAG = "DatabaseCleanup";
    
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;
    
    public DatabaseCleanup() {
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/").getReference();
    }
    
    /**
     * Clean up all sample data (use with caution!)
     */
    public void cleanupAllSampleData() {
        Log.d(TAG, "🧹 Cleaning up all sample data...");
        
        // Clean up Firestore collections
        cleanupFirestoreSampleData();
        
        // Clean up Realtime Database
        cleanupRealtimeSampleData();
        
        Log.d(TAG, "✅ All sample data cleanup completed");
    }
    
    /**
     * Clean up only invalid users (safe to use)
     */
    public void cleanupInvalidUsers() {
        Log.d(TAG, "🧹 Cleaning up invalid users...");
        
        firestore.collection("users").get()
            .addOnSuccessListener(querySnapshot -> {
                java.util.concurrent.atomic.AtomicInteger cleanedCount = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger totalInvalidUsers = new java.util.concurrent.atomic.AtomicInteger(0);
                
                // First pass: count invalid users
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String role = doc.getString("role");
                    String entityId = doc.getString("entityId");
                    
                    if (role == null || entityId == null) {
                        totalInvalidUsers.incrementAndGet();
                    }
                }
                
                final int totalCount = totalInvalidUsers.get();
                
                if (totalCount == 0) {
                    Log.d(TAG, "✅ No invalid users found to clean up");
                    return;
                }
                
                // Second pass: delete invalid users
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String uid = doc.getId();
                    String role = doc.getString("role");
                    String entityId = doc.getString("entityId");
                    
                    // Clean up users without proper structure
                    if (role == null || entityId == null) {
                        Log.d(TAG, "🧹 Cleaning up invalid user: " + uid);
                        doc.getReference().delete()
                            .addOnSuccessListener(aVoid -> {
                                int count = cleanedCount.incrementAndGet();
                                Log.d(TAG, "✅ Cleaned up invalid user: " + uid + " (" + count + "/" + totalCount + ")");
                                
                                // Log final count when all deletions are complete
                                if (count == totalCount) {
                                    Log.d(TAG, "✅ Cleanup completed: " + count + " invalid users removed");
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up user: " + uid, e));
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up invalid users", e));
    }
    
    /**
     * Clean up completed rides from Realtime Database
     */
    public void cleanupCompletedRides() {
        Log.d(TAG, "🧹 Cleaning up completed rides from Realtime Database...");
        
        realtimeDb.child("rideRequestsLive").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                int cleanedCount = 0;
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("completed".equals(status) || "declined".equals(status)) {
                        Log.d(TAG, "🧹 Cleaning up " + status + " ride: " + snapshot.getKey());
                        snapshot.getRef().removeValue();
                        cleanedCount++;
                    }
                }
                Log.d(TAG, "✅ Cleaned up " + cleanedCount + " completed/declined rides from Realtime Database");
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to cleanup completed rides", databaseError.toException());
            }
        });
    }
    
    /**
     * Clean up Firestore sample data
     */
    private void cleanupFirestoreSampleData() {
        // Clean up sample users
        firestore.collection("users").whereEqualTo("email", "john.doe@glide.com").get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            });
        
        firestore.collection("users").whereEqualTo("email", "jane.smith@glide.com").get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            });
        
        firestore.collection("users").whereEqualTo("email", "mike.johnson@glide.com").get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            });
        
        firestore.collection("users").whereEqualTo("email", "blessing.moyo@glide.com").get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            });
        
        firestore.collection("users").whereEqualTo("email", "sarah.chikwava@glide.com").get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
            });
        
        // Clean up sample drivers
        firestore.collection("drivers").document("driver_001").delete();
        firestore.collection("drivers").document("driver_002").delete();
        firestore.collection("drivers").document("driver_003").delete();
        
        // Clean up sample vehicles
        firestore.collection("vehicles").document("vehicle_001").delete();
        firestore.collection("vehicles").document("vehicle_002").delete();
        firestore.collection("vehicles").document("vehicle_003").delete();
        
        // Clean up sample commuters
        firestore.collection("commuters").document("commuter_001").delete();
        firestore.collection("commuters").document("commuter_002").delete();
        
        // Clean up sample ride requests
        firestore.collection("rideRequests").document("ride_001").delete();
        firestore.collection("rideRequests").document("ride_002").delete();
    }
    
    /**
     * Clean up Realtime Database sample data
     */
    private void cleanupRealtimeSampleData() {
        // Clean up live drivers
        realtimeDb.child("drivers_live").child("driver_001").removeValue();
        realtimeDb.child("drivers_live").child("driver_002").removeValue();
        realtimeDb.child("drivers_live").child("driver_003").removeValue();
        
        // Clean up live ride requests
        realtimeDb.child("rideRequestsLive").child("ride_001").removeValue();
        realtimeDb.child("rideRequestsLive").child("ride_002").removeValue();
    }
    
    /**
     * Get database statistics after cleanup
     */
    public void getDatabaseStats() {
        Log.d(TAG, "📊 Getting database statistics...");
        
        // Count Firestore collections
        firestore.collection("users").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Users in Firestore: " + querySnapshot.size()));
        
        firestore.collection("drivers").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Drivers in Firestore: " + querySnapshot.size()));
        
        firestore.collection("commuters").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Commuters in Firestore: " + querySnapshot.size()));
        
        firestore.collection("rideRequests").get()
            .addOnSuccessListener(querySnapshot -> 
                Log.d(TAG, "Ride requests in Firestore: " + querySnapshot.size()));
        
        // Count Realtime Database
        realtimeDb.child("drivers_live").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Log.d(TAG, "Live drivers in Realtime Database: " + dataSnapshot.getChildrenCount());
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to count live drivers", databaseError.toException());
            }
        });
        
        realtimeDb.child("rideRequestsLive").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Log.d(TAG, "Live ride requests in Realtime Database: " + dataSnapshot.getChildrenCount());
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to count live ride requests", databaseError.toException());
            }
        });
    }
}
