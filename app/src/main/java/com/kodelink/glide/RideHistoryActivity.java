package com.kodelink.glide;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class RideHistoryActivity extends AppCompatActivity {
    private static final String TAG = "RideHistoryActivity";
    
    private RecyclerView rvRideHistory;
    private RideHistoryAdapter adapter;
    private List<RideHistoryItem> rideHistoryList;
    private FirebaseService firebaseService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rvRideHistory = findViewById(R.id.rvRideHistory);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ride History");
        }

        // Initialize services
        firebaseService = FirebaseService.getInstance();
        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        // Initialize ride history list and adapter
        rideHistoryList = new ArrayList<>();
        adapter = new RideHistoryAdapter(rideHistoryList);
        rvRideHistory.setLayoutManager(new LinearLayoutManager(this));
        rvRideHistory.setAdapter(adapter);

        // Load ride history
        loadRideHistory();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRideHistory() {
        String currentUserPhone = prefs.getString("current_user_phone", "");
        String role = prefs.getString(currentUserPhone + "_role", "commuter");

        // Get current user's entity ID
        firebaseService.getCurrentUserEntityId()
            .addOnSuccessListener(entityId -> {
                if (entityId != null) {
                    if (role.equals("driver")) {
                        loadDriverRideHistory(entityId);
                    } else {
                        loadCommuterRideHistory(entityId);
                    }
                } else {
                    Toast.makeText(this, "Unable to load ride history", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get current user entity ID", e);
                Toast.makeText(this, "Failed to load ride history", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadDriverRideHistory(String driverId) {
        firebaseService.getDriverRideHistory(driverId)
            .addOnSuccessListener(rides -> {
                rideHistoryList.clear();
                for (RideRequest ride : rides) {
                    RideHistoryItem item = new RideHistoryItem();
                    item.rideId = ride.rideId;
                    item.pickupLocation = ride.pickupLocation.address;
                    item.destinationLocation = ride.destination.address;
                    item.status = ride.status;
                    item.timestamp = ride.timestamp;
                    item.people = ride.people;
                    item.priceEach = ride.priceEach;
                    item.commuterId = ride.commuterId;
                    item.driverId = ride.driverId;
                    item.userRole = "driver";
                    rideHistoryList.add(item);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load driver ride history", e);
                Toast.makeText(this, "Failed to load ride history", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadCommuterRideHistory(String commuterId) {
        firebaseService.getCommuterRideHistory(commuterId)
            .addOnSuccessListener(rides -> {
                rideHistoryList.clear();
                for (RideRequest ride : rides) {
                    RideHistoryItem item = new RideHistoryItem();
                    item.rideId = ride.rideId;
                    item.pickupLocation = ride.pickupLocation.address;
                    item.destinationLocation = ride.destination.address;
                    item.status = ride.status;
                    item.timestamp = ride.timestamp;
                    item.people = ride.people;
                    item.priceEach = ride.priceEach;
                    item.commuterId = ride.commuterId;
                    item.driverId = ride.driverId;
                    item.userRole = "commuter";
                    rideHistoryList.add(item);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load commuter ride history", e);
                Toast.makeText(this, "Failed to load ride history", Toast.LENGTH_SHORT).show();
            });
    }
}
