package com.kodelink.glide;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static FirebaseService instance;
    private DatabaseReference database;

    private FirebaseService() {
        database = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/").getReference();
    }

    public static FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    // Ride Request Operations
    public void createRideRequest(RideRequest rideRequest, DatabaseCallback callback) {
        DatabaseReference rideRequestsRef = database.child("rideRequests").child(rideRequest.rideId);
        rideRequestsRef.setValue(rideRequest)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ride request created successfully");
                    callback.onSuccess("Ride request created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create ride request", e);
                    callback.onError(e.getMessage());
                });
    }

    public void updateRideRequestStatus(String rideId, String status, DatabaseCallback callback) {
        DatabaseReference rideRequestRef = database.child("rideRequests").child(rideId).child("status");
        rideRequestRef.setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ride request status updated to: " + status);
                    callback.onSuccess("Status updated");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update ride request status", e);
                    callback.onError(e.getMessage());
                });
    }

    public void listenToRideRequest(String rideId, RideRequestListener listener) {
        DatabaseReference rideRequestRef = database.child("rideRequests").child(rideId);
        rideRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    RideRequest rideRequest = dataSnapshot.getValue(RideRequest.class);
                    if (rideRequest != null) {
                        listener.onRideRequestUpdated(rideRequest);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to listen to ride request", databaseError.toException());
                listener.onError(databaseError.getMessage());
            }
        });
    }

    // Driver Operations
    public void createDriver(Driver driver, DatabaseCallback callback) {
        DatabaseReference driverRef = database.child("drivers").child(driver.driverId);
        driverRef.setValue(driver)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Driver created successfully");
                    callback.onSuccess("Driver created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create driver", e);
                    callback.onError(e.getMessage());
                });
    }

    public void updateDriverLocation(String driverId, Driver.LocationData location, DatabaseCallback callback) {
        DatabaseReference driverRef = database.child("drivers").child(driverId).child("currentLocation");
        driverRef.setValue(location)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Driver location updated");
                    callback.onSuccess("Location updated");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update driver location", e);
                    callback.onError(e.getMessage());
                });
    }

    public void updateDriverAvailability(String driverId, String availability, DatabaseCallback callback) {
        DatabaseReference driverRef = database.child("drivers").child(driverId).child("availability");
        driverRef.setValue(availability)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Driver availability updated to: " + availability);
                    callback.onSuccess("Availability updated");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update driver availability", e);
                    callback.onError(e.getMessage());
                });
    }

    public void getAvailableDrivers(DriversListener listener) {
        DatabaseReference driversRef = database.child("drivers");
        driversRef.orderByChild("availability").equalTo("available")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Driver> drivers = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Driver driver = snapshot.getValue(Driver.class);
                            if (driver != null) {
                                drivers.add(driver);
                            }
                        }
                        listener.onDriversReceived(drivers);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to get available drivers", databaseError.toException());
                        listener.onError(databaseError.getMessage());
                    }
                });
    }

    public void listenToDriverRideRequests(String driverId, RideRequestListener listener) {
        DatabaseReference rideRequestsRef = database.child("rideRequests");
        rideRequestsRef.orderByChild("driverId").equalTo(driverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            RideRequest rideRequest = snapshot.getValue(RideRequest.class);
                            if (rideRequest != null && "pending".equals(rideRequest.status)) {
                                listener.onRideRequestUpdated(rideRequest);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to listen to driver ride requests", databaseError.toException());
                        listener.onError(databaseError.getMessage());
                    }
                });
    }

    // Commuter Operations
    public void createCommuter(Commuter commuter, DatabaseCallback callback) {
        DatabaseReference commuterRef = database.child("commuters").child(commuter.commuterId);
        commuterRef.setValue(commuter)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Commuter created successfully");
                    callback.onSuccess("Commuter created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create commuter", e);
                    callback.onError(e.getMessage());
                });
    }

    public void updateCommuterLocation(String commuterId, Commuter.LocationData location, DatabaseCallback callback) {
        DatabaseReference commuterRef = database.child("commuters").child(commuterId).child("currentLocation");
        commuterRef.setValue(location)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Commuter location updated");
                    callback.onSuccess("Location updated");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update commuter location", e);
                    callback.onError(e.getMessage());
                });
    }

    // Callback interfaces
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


