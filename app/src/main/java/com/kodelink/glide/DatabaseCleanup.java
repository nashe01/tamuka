package com.kodelink.glide;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * DatabaseCleanup - Utility class for cleaning up test data from the database
 * 
 * This class provides methods to remove test data from both Firestore and Realtime Database
 * to ensure a clean production environment.
 */
public class DatabaseCleanup {
    private static final String TAG = "DatabaseCleanup";
    
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private FirebaseFirestore firestore;
    
    public DatabaseCleanup() {
        database = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/");
        databaseRef = database.getReference();
        firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Clean up all test ride requests from both databases
     */
    public void cleanupTestRideRequests() {
        Log.d(TAG, "🧹 Starting test ride requests cleanup...");
        
        // Test ride IDs to remove
        String[] testRideIds = {
            "test_ride_in_progress_001",
            "test_ride_in_progress_002", 
            "test_ride_in_progress_003",
            "test_ride_in_progress_004",
            "test_ride_001",
            "test_ride_002",
            "test_ride_003",
            "test_ride_004",
            "ride_001",
            "ride_002"
        };
        
        int totalOperations = testRideIds.length * 2; // Realtime + Firestore
        final int[] completedOperations = {0};
        
        // Remove from Realtime Database
        for (String rideId : testRideIds) {
            databaseRef.child("rideRequestsLive").child(rideId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed test ride from Realtime Database: " + rideId);
                    completedOperations[0]++;
                    if (completedOperations[0] == totalOperations) {
                        Log.d(TAG, "🎉 Test ride requests cleanup completed successfully!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove test ride from Realtime Database: " + rideId, e);
                    completedOperations[0]++;
                    if (completedOperations[0] == totalOperations) {
                        Log.d(TAG, "🎉 Test ride requests cleanup completed with some errors!");
                    }
                });
        }
        
        // Remove from Firestore
        for (String rideId : testRideIds) {
            firestore.collection("rideRequests").document(rideId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed test ride from Firestore: " + rideId);
                    completedOperations[0]++;
                    if (completedOperations[0] == totalOperations) {
                        Log.d(TAG, "🎉 Test ride requests cleanup completed successfully!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove test ride from Firestore: " + rideId, e);
                    completedOperations[0]++;
                    if (completedOperations[0] == totalOperations) {
                        Log.d(TAG, "🎉 Test ride requests cleanup completed with some errors!");
                    }
                });
        }
    }
    
    /**
     * Clean up all test data (rides, users, drivers, etc.)
     */
    public void cleanupAllTestData() {
        Log.d(TAG, "🧹 Starting comprehensive test data cleanup...");
        
        // Clean up test ride requests
        cleanupTestRideRequests();
        
        // Clean up test users
        cleanupTestUsers();
        
        // Clean up test drivers
        cleanupTestDrivers();
        
        // Clean up test commuters
        cleanupTestCommuters();
        
        Log.d(TAG, "🧹 Comprehensive test data cleanup completed");
    }
    
    /**
     * Clean up test users
     */
    private void cleanupTestUsers() {
        Log.d(TAG, "🧹 Cleaning up test users...");
        
        String[] testUserIds = {
            "sample_driver_001",
            "sample_driver_002", 
            "sample_driver_003",
            "sample_commuter_001",
            "sample_commuter_002"
        };
        
        for (String userId : testUserIds) {
            firestore.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed test user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove test user: " + userId, e);
                });
        }
    }
    
    /**
     * Clean up test drivers
     */
    private void cleanupTestDrivers() {
        Log.d(TAG, "🧹 Cleaning up test drivers...");
        
        String[] testDriverIds = {
            "driver_001",
            "driver_002", 
            "driver_003"
        };
        
        for (String driverId : testDriverIds) {
            // Remove from Firestore
            firestore.collection("drivers").document(driverId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed test driver from Firestore: " + driverId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove test driver from Firestore: " + driverId, e);
                });
            
            // Remove from Realtime Database
            databaseRef.child("drivers_live").child(driverId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed test driver from Realtime Database: " + driverId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove test driver from Realtime Database: " + driverId, e);
                });
        }
    }
    
    /**
     * Clean up test commuters
     */
    private void cleanupTestCommuters() {
        Log.d(TAG, "🧹 Cleaning up test commuters...");
        
        String[] testCommuterIds = {
            "commuter_001",
            "commuter_002"
        };
        
        for (String commuterId : testCommuterIds) {
            firestore.collection("commuters").document(commuterId).delete()
                            .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed test commuter: " + commuterId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove test commuter: " + commuterId, e);
                });
        }
    }
    
    /**
     * Clean up completed/declined rides from Realtime Database
     * (These should only be in Firestore, not in live data)
     */
    public void cleanupCompletedRides() {
        Log.d(TAG, "🧹 Cleaning up completed/declined rides from Realtime Database...");
        
        databaseRef.child("rideRequestsLive").get()
            .addOnSuccessListener(dataSnapshot -> {
                int cleanedCount = 0;
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("completed".equals(status) || "declined".equals(status)) {
                        snapshot.getRef().removeValue();
                        cleanedCount++;
                    }
                }
                Log.d(TAG, "✅ Cleaned up " + cleanedCount + " completed/declined rides from Realtime Database");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to cleanup completed rides", e);
        });
    }
    
    /**
     * Clean up pending rides from both databases
     * This removes all pending ride requests
     */
    public void cleanupPendingRides() {
        Log.d(TAG, "🧹 Cleaning up pending rides from both databases...");
        
        // Clean up from Realtime Database
        databaseRef.child("rideRequestsLive").get()
            .addOnSuccessListener(dataSnapshot -> {
                int cleanedCount = 0;
                int totalPendingRides = 0;
                
                // Count total pending rides first
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equals(status)) {
                        totalPendingRides++;
                    }
                }
                
                if (totalPendingRides == 0) {
                    Log.d(TAG, "🎉 No pending rides found - cleanup completed!");
                    return;
                }
                
                final int[] completedFirestoreOperations = {0};
                
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equals(status)) {
                        String rideId = snapshot.getKey();
                        snapshot.getRef().removeValue();
                        cleanedCount++;
                        
                        // Also remove from Firestore
                        firestore.collection("rideRequests").document(rideId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Removed pending ride from Firestore: " + rideId);
                                completedFirestoreOperations[0]++;
                                if (completedFirestoreOperations[0] == totalPendingRides) {
                                    Log.d(TAG, "🎉 Pending rides cleanup completed successfully! Removed " + cleanedCount + " rides from both databases.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Failed to remove pending ride from Firestore: " + rideId, e);
                                completedFirestoreOperations[0]++;
                                if (completedFirestoreOperations[0] == totalPendingRides) {
                                    Log.d(TAG, "🎉 Pending rides cleanup completed with some errors! Removed " + cleanedCount + " rides from Realtime Database.");
                                }
                            });
                    }
                }
                Log.d(TAG, "✅ Cleaned up " + cleanedCount + " pending rides from Realtime Database");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to cleanup pending rides", e);
            });
    }
    
    /**
     * Clean up active rides (accepted and in_progress) from both databases
     * This removes all active ride requests
     */
    public void cleanupActiveRides() {
        Log.d(TAG, "🧹 Cleaning up active rides from both databases...");
        
        // Clean up from Realtime Database
        databaseRef.child("rideRequestsLive").get()
            .addOnSuccessListener(dataSnapshot -> {
                int cleanedCount = 0;
                int totalActiveRides = 0;
                
                // Count total active rides first
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("accepted".equals(status) || "in_progress".equals(status)) {
                        totalActiveRides++;
                    }
                }
                
                if (totalActiveRides == 0) {
                    Log.d(TAG, "🎉 No active rides found - cleanup completed!");
                    return;
                }
                
                final int[] completedFirestoreOperations = {0};
                
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("accepted".equals(status) || "in_progress".equals(status)) {
                        String rideId = snapshot.getKey();
                        snapshot.getRef().removeValue();
                        cleanedCount++;
                        
                        // Also remove from Firestore
                        firestore.collection("rideRequests").document(rideId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Removed active ride from Firestore: " + rideId);
                                completedFirestoreOperations[0]++;
                                if (completedFirestoreOperations[0] == totalActiveRides) {
                                    Log.d(TAG, "🎉 Active rides cleanup completed successfully! Removed " + cleanedCount + " rides from both databases.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Failed to remove active ride from Firestore: " + rideId, e);
                                completedFirestoreOperations[0]++;
                                if (completedFirestoreOperations[0] == totalActiveRides) {
                                    Log.d(TAG, "🎉 Active rides cleanup completed with some errors! Removed " + cleanedCount + " rides from Realtime Database.");
                                }
                            });
                    }
                }
                Log.d(TAG, "✅ Cleaned up " + cleanedCount + " active rides from Realtime Database");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to cleanup active rides", e);
            });
    }
    
    /**
     * Get database statistics
     */
    public void getDatabaseStats() {
        Log.d(TAG, "📊 Getting database statistics...");
        
        // Count Firestore documents
        firestore.collection("rideRequests").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "📊 Firestore ride requests: " + querySnapshot.size());
            });
        
        firestore.collection("drivers").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "📊 Firestore drivers: " + querySnapshot.size());
            });
        
        firestore.collection("commuters").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "📊 Firestore commuters: " + querySnapshot.size());
            });
        
        // Count Realtime Database nodes
        databaseRef.child("rideRequestsLive").get()
            .addOnSuccessListener(dataSnapshot -> {
                Log.d(TAG, "📊 Realtime Database ride requests: " + dataSnapshot.getChildrenCount());
            });
        
        databaseRef.child("drivers_live").get()
            .addOnSuccessListener(dataSnapshot -> {
                Log.d(TAG, "📊 Realtime Database drivers: " + dataSnapshot.getChildrenCount());
        });
    }
}