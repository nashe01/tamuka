package com.kodelink.glide;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to verify and ensure synchronization between Firestore and Realtime Database
 */
public class DatabaseSyncVerifier {
    private static final String TAG = "DatabaseSyncVerifier";
    
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;
    
    public DatabaseSyncVerifier() {
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/").getReference();
    }
    
    /**
     * Verify that all sample data is properly synchronized between databases
     */
    public void verifySampleDataSync() {
        Log.d(TAG, "Starting sample data synchronization verification...");
        
        verifyDriverDataSync();
        verifyRideRequestDataSync();
        verifyUserDataIntegrity();
    }
    
    /**
     * Verify driver data synchronization between Firestore and Realtime Database
     */
    private void verifyDriverDataSync() {
        Log.d(TAG, "Verifying driver data synchronization...");
        
        // Get all drivers from Firestore
        firestore.collection("drivers").get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> firestoreDriverIds = new ArrayList<>();
                Map<String, Map<String, Object>> firestoreDrivers = new HashMap<>();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String driverId = doc.getString("driverId");
                    if (driverId != null) {
                        firestoreDriverIds.add(driverId);
                        firestoreDrivers.put(driverId, doc.getData());
                    }
                }
                
                // Get all live drivers from Realtime Database
                realtimeDb.child("drivers_live").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> realtimeDriverIds = new ArrayList<>();
                        Map<String, Map<String, Object>> realtimeDrivers = new HashMap<>();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String driverId = snapshot.getKey();
                            if (driverId != null) {
                                realtimeDriverIds.add(driverId);
                                realtimeDrivers.put(driverId, (Map<String, Object>) snapshot.getValue());
                            }
                        }
                        
                        // Compare and report sync status
                        compareDriverData(firestoreDriverIds, realtimeDriverIds, firestoreDrivers, realtimeDrivers);
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to get live driver data", databaseError.toException());
                    }
                });
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to get Firestore driver data", e));
    }
    
    /**
     * Compare driver data between Firestore and Realtime Database
     */
    private void compareDriverData(List<String> firestoreIds, List<String> realtimeIds, 
                                 Map<String, Map<String, Object>> firestoreData, 
                                 Map<String, Map<String, Object>> realtimeData) {
        
        Log.d(TAG, "=== DRIVER DATA SYNC VERIFICATION ===");
        Log.d(TAG, "Firestore drivers: " + firestoreIds.size());
        Log.d(TAG, "Realtime drivers: " + realtimeIds.size());
        
        // Check for missing drivers in Realtime Database
        for (String driverId : firestoreIds) {
            if (!realtimeIds.contains(driverId)) {
                Log.w(TAG, "❌ Driver " + driverId + " missing in Realtime Database");
                // Fix: Add missing driver to Realtime Database
                addMissingDriverToRealtime(driverId, firestoreData.get(driverId));
            } else {
                // Verify data consistency
                verifyDriverDataConsistency(driverId, firestoreData.get(driverId), realtimeData.get(driverId));
            }
        }
        
        // Check for extra drivers in Realtime Database
        for (String driverId : realtimeIds) {
            if (!firestoreIds.contains(driverId)) {
                Log.w(TAG, "❌ Driver " + driverId + " exists in Realtime Database but not in Firestore");
            }
        }
        
        Log.d(TAG, "=== END DRIVER DATA SYNC VERIFICATION ===");
    }
    
    /**
     * Verify data consistency for a specific driver
     */
    private void verifyDriverDataConsistency(String driverId, Map<String, Object> firestoreData, 
                                           Map<String, Object> realtimeData) {
        if (firestoreData == null || realtimeData == null) {
            Log.w(TAG, "❌ Driver " + driverId + " has null data in one or both databases");
            return;
        }
        
        // Check status consistency
        String firestoreStatus = (String) firestoreData.get("status");
        String realtimeStatus = (String) realtimeData.get("status");
        
        if (!firestoreStatus.equals(realtimeStatus)) {
            Log.w(TAG, "❌ Driver " + driverId + " status mismatch: Firestore=" + firestoreStatus + 
                  ", Realtime=" + realtimeStatus);
            // Fix: Update Realtime Database to match Firestore
            realtimeDb.child("drivers_live").child(driverId).child("status").setValue(firestoreStatus);
        }
        
        // Check location consistency
        Map<String, Object> firestoreLocation = (Map<String, Object>) firestoreData.get("currentLocation");
        Map<String, Object> realtimeLocation = (Map<String, Object>) realtimeData.get("location");
        
        if (firestoreLocation != null && realtimeLocation != null) {
            Double firestoreLat = (Double) firestoreLocation.get("lat");
            Double firestoreLng = (Double) firestoreLocation.get("lng");
            Double realtimeLat = (Double) realtimeLocation.get("lat");
            Double realtimeLng = (Double) realtimeLocation.get("lng");
            
            if (firestoreLat != null && firestoreLng != null && realtimeLat != null && realtimeLng != null) {
                double latDiff = Math.abs(firestoreLat - realtimeLat);
                double lngDiff = Math.abs(firestoreLng - realtimeLng);
                
                if (latDiff > 0.001 || lngDiff > 0.001) { // Allow small differences
                    Log.w(TAG, "❌ Driver " + driverId + " location mismatch: Firestore=(" + 
                          firestoreLat + "," + firestoreLng + "), Realtime=(" + realtimeLat + "," + realtimeLng + ")");
                } else {
                    Log.d(TAG, "✅ Driver " + driverId + " data is synchronized");
                }
            }
        }
    }
    
    /**
     * Add missing driver to Realtime Database
     */
    private void addMissingDriverToRealtime(String driverId, Map<String, Object> firestoreData) {
        Log.d(TAG, "🔧 Adding missing driver " + driverId + " to Realtime Database");
        
        Map<String, Object> location = (Map<String, Object>) firestoreData.get("currentLocation");
        String status = (String) firestoreData.get("status");
        
        if (location != null && status != null) {
            DatabaseReference driverRef = realtimeDb.child("drivers_live").child(driverId);
            driverRef.child("status").setValue(status);
            driverRef.child("location").child("lat").setValue(location.get("lat"));
            driverRef.child("location").child("lng").setValue(location.get("lng"));
            driverRef.child("location").child("timestamp").setValue(System.currentTimeMillis());
            
            Log.d(TAG, "✅ Driver " + driverId + " added to Realtime Database");
        }
    }
    
    /**
     * Verify ride request data synchronization
     */
    private void verifyRideRequestDataSync() {
        Log.d(TAG, "Verifying ride request data synchronization...");
        
        // Get all ride requests from Firestore
        firestore.collection("rideRequests").get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> firestoreRideIds = new ArrayList<>();
                Map<String, Map<String, Object>> firestoreRides = new HashMap<>();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String rideId = doc.getString("rideId");
                    if (rideId != null) {
                        firestoreRideIds.add(rideId);
                        firestoreRides.put(rideId, doc.getData());
                    }
                }
                
                // Get all live ride requests from Realtime Database
                realtimeDb.child("rideRequestsLive").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> realtimeRideIds = new ArrayList<>();
                        Map<String, Map<String, Object>> realtimeRides = new HashMap<>();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String rideId = snapshot.getKey();
                            if (rideId != null) {
                                realtimeRideIds.add(rideId);
                                realtimeRides.put(rideId, (Map<String, Object>) snapshot.getValue());
                            }
                        }
                        
                        // Compare and report sync status
                        compareRideRequestData(firestoreRideIds, realtimeRideIds, firestoreRides, realtimeRides);
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to get live ride request data", databaseError.toException());
                    }
                });
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to get Firestore ride request data", e));
    }
    
    /**
     * Compare ride request data between databases
     */
    private void compareRideRequestData(List<String> firestoreIds, List<String> realtimeIds,
                                      Map<String, Map<String, Object>> firestoreData,
                                      Map<String, Map<String, Object>> realtimeData) {
        
        Log.d(TAG, "=== RIDE REQUEST DATA SYNC VERIFICATION ===");
        Log.d(TAG, "Firestore ride requests: " + firestoreIds.size());
        Log.d(TAG, "Realtime ride requests: " + realtimeIds.size());
        
        // Only pending/active rides should be in Realtime Database
        for (String rideId : firestoreIds) {
            Map<String, Object> rideData = firestoreData.get(rideId);
            String status = (String) rideData.get("status");
            
            if ("pending".equals(status) || "accepted".equals(status)) {
                if (!realtimeIds.contains(rideId)) {
                    Log.w(TAG, "❌ Active ride " + rideId + " missing in Realtime Database");
                    // Fix: Add missing ride to Realtime Database
                    addMissingRideToRealtime(rideId, rideData);
                } else {
                    Log.d(TAG, "✅ Active ride " + rideId + " is synchronized");
                }
            } else if ("completed".equals(status) || "declined".equals(status)) {
                if (realtimeIds.contains(rideId)) {
                    Log.w(TAG, "❌ Completed/declined ride " + rideId + " should not be in Realtime Database");
                    // Fix: Remove completed ride from Realtime Database
                    realtimeDb.child("rideRequestsLive").child(rideId).removeValue();
                    Log.d(TAG, "🔧 Removed completed ride " + rideId + " from Realtime Database");
                }
            }
        }
        
        Log.d(TAG, "=== END RIDE REQUEST DATA SYNC VERIFICATION ===");
    }
    
    /**
     * Add missing ride request to Realtime Database
     */
    private void addMissingRideToRealtime(String rideId, Map<String, Object> rideData) {
        Log.d(TAG, "🔧 Adding missing ride " + rideId + " to Realtime Database");
        
        realtimeDb.child("rideRequestsLive").child(rideId).setValue(rideData)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Ride " + rideId + " added to Realtime Database"))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to add ride " + rideId + " to Realtime Database", e));
    }
    
    /**
     * Verify user data integrity
     */
    private void verifyUserDataIntegrity() {
        Log.d(TAG, "Verifying user data integrity...");
        
        firestore.collection("users").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "=== USER DATA INTEGRITY VERIFICATION ===");
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String uid = doc.getId();
                    String role = doc.getString("role");
                    String entityId = doc.getString("entityId");
                    
                    if (role == null || entityId == null) {
                        Log.w(TAG, "❌ User " + uid + " has missing role or entityId - this appears to be an old/invalid user");
                        
                        // Check if this is a sample user (should have proper structure)
                        if (uid.startsWith("sample_")) {
                            Log.w(TAG, "🔧 Sample user " + uid + " has invalid structure - this shouldn't happen");
                        } else {
                            Log.d(TAG, "ℹ️ User " + uid + " appears to be from previous testing - ignoring for sample data verification");
                        }
                        continue;
                    }
                    
                    // Verify entity exists in appropriate collection
                    if ("driver".equals(role)) {
                        verifyDriverEntityExists(entityId);
                    } else if ("commuter".equals(role)) {
                        verifyCommuterEntityExists(entityId);
                    }
                }
                
                Log.d(TAG, "=== END USER DATA INTEGRITY VERIFICATION ===");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to verify user data integrity", e));
    }
    
    /**
     * Verify driver entity exists
     */
    private void verifyDriverEntityExists(String driverId) {
        firestore.collection("drivers").document(driverId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Log.d(TAG, "✅ Driver entity " + driverId + " exists");
                } else {
                    Log.w(TAG, "❌ Driver entity " + driverId + " does not exist");
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to verify driver entity " + driverId, e));
    }
    
    /**
     * Verify commuter entity exists
     */
    private void verifyCommuterEntityExists(String commuterId) {
        firestore.collection("commuters").document(commuterId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Log.d(TAG, "✅ Commuter entity " + commuterId + " exists");
                } else {
                    Log.w(TAG, "❌ Commuter entity " + commuterId + " does not exist");
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to verify commuter entity " + commuterId, e));
    }
    
    /**
     * Fix all synchronization issues found
     */
    public void fixAllSyncIssues() {
        Log.d(TAG, "Starting comprehensive sync fix...");
        
        // First verify and identify issues
        verifySampleDataSync();
        
        // Additional fixes for common issues
        fixDriverStatusSync();
        cleanupCompletedRides();
        cleanupInvalidUsers();
        
        Log.d(TAG, "Sync fix completed");
    }
    
    /**
     * Clean up invalid users from previous testing
     */
    private void cleanupInvalidUsers() {
        Log.d(TAG, "Cleaning up invalid users from previous testing...");
        
        firestore.collection("users").get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String uid = doc.getId();
                    String role = doc.getString("role");
                    String entityId = doc.getString("entityId");
                    
                    // Only clean up non-sample users that are invalid
                    if (!uid.startsWith("sample_") && (role == null || entityId == null)) {
                        Log.d(TAG, "🧹 Cleaning up invalid user: " + uid);
                        doc.getReference().delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Cleaned up invalid user: " + uid))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up user: " + uid, e));
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up invalid users", e));
    }
    
    /**
     * Fix driver status synchronization
     */
    private void fixDriverStatusSync() {
        Log.d(TAG, "Fixing driver status synchronization...");
        
        firestore.collection("drivers").get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String driverId = doc.getString("driverId");
                    String status = doc.getString("status");
                    
                    if (driverId != null && status != null) {
                        // Update Realtime Database to match Firestore
                        realtimeDb.child("drivers_live").child(driverId).child("status").setValue(status);
                    }
                }
                Log.d(TAG, "✅ Driver status synchronization fixed");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to fix driver status sync", e));
    }
    
    /**
     * Clean up completed rides from Realtime Database
     */
    private void cleanupCompletedRides() {
        Log.d(TAG, "Cleaning up completed rides from Realtime Database...");
        
        realtimeDb.child("rideRequestsLive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("completed".equals(status) || "declined".equals(status)) {
                        snapshot.getRef().removeValue();
                        Log.d(TAG, "🔧 Removed " + status + " ride from Realtime Database");
                    }
                }
                Log.d(TAG, "✅ Completed rides cleanup finished");
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to cleanup completed rides", databaseError.toException());
            }
        });
    }
}
