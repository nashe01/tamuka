package com.kodelink.glide;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RideHistoryActivity extends AppCompatActivity {
    private static final String TAG = "RideHistoryActivity";
    
    private RecyclerView rvRideHistory;
    private RideHistoryAdapter adapter;
    private List<RideHistoryItem> rideHistoryList;
    private FirebaseService firebaseService;
    private SharedPreferences prefs;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        rvRideHistory = findViewById(R.id.rvRideHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Set up back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize services
        firebaseService = FirebaseService.getInstance();
        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        // Initialize ride history list and adapter
        rideHistoryList = new ArrayList<>();
        adapter = new RideHistoryAdapter(rideHistoryList);
        rvRideHistory.setLayoutManager(new LinearLayoutManager(this));
        rvRideHistory.setAdapter(adapter);

        // Show loading state
        showLoadingState();

        // Debug: Check total ride count
        firebaseService.getTotalRideCount()
            .addOnSuccessListener(count -> {
                Log.d(TAG, "Total rides in database: " + count);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get total ride count", e);
            });

        // Load ride history
        loadRideHistory();
    }


    private void loadRideHistory() {
        String currentUserPhone = prefs.getString("current_user_phone", "");
        String role = prefs.getString(currentUserPhone + "_role", "commuter");

        Log.d(TAG, "Loading ride history for phone: " + currentUserPhone + ", role: " + role);

        // First try Firebase Auth
        firebaseService.getCurrentUserEntityId()
            .addOnSuccessListener(entityId -> {
                if (entityId != null) {
                    Log.d(TAG, "Got entity ID from Firebase Auth: " + entityId);
                    if (role.equals("driver")) {
                        loadDriverRideHistory(entityId);
                    } else {
                        loadCommuterRideHistory(entityId);
                    }
                } else {
                    // Fallback to mock authentication
                    Log.d(TAG, "Firebase Auth failed, trying mock authentication");
                    loadRideHistoryWithMockAuth(currentUserPhone, role);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firebase Auth failed, trying mock authentication", e);
                loadRideHistoryWithMockAuth(currentUserPhone, role);
            });
    }
    
    private void loadRideHistoryWithMockAuth(String phone, String role) {
        firebaseService.getCurrentUserEntityIdByPhone(phone, role)
            .addOnSuccessListener(entityId -> {
                if (entityId != null) {
                    Log.d(TAG, "Got entity ID from mock auth: " + entityId);
                    if (role.equals("driver")) {
                        loadDriverRideHistory(entityId);
                    } else {
                        loadCommuterRideHistory(entityId);
                    }
                } else {
                    Log.e(TAG, "No entity ID found for phone: " + phone + ", role: " + role);
                    showErrorState("User profile not found. Please register again.");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get entity ID from mock auth", e);
                showErrorState("Failed to load ride history");
            });
    }

    private void loadDriverRideHistory(String driverId) {
        Log.d(TAG, "Loading driver ride history for driverId: " + driverId);
        firebaseService.getEnhancedDriverRideHistory(driverId)
            .addOnSuccessListener(historyItems -> {
                Log.d(TAG, "Loaded " + historyItems.size() + " driver ride history items");
                rideHistoryList.clear();
                rideHistoryList.addAll(historyItems);
                
                // Load user names for each ride
                loadUserNamesForHistory();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load driver ride history", e);
                showErrorState("Failed to load ride history");
            });
    }

    private void loadCommuterRideHistory(String commuterId) {
        Log.d(TAG, "Loading commuter ride history for commuterId: " + commuterId);
        firebaseService.getEnhancedCommuterRideHistory(commuterId)
            .addOnSuccessListener(historyItems -> {
                Log.d(TAG, "Loaded " + historyItems.size() + " commuter ride history items");
                rideHistoryList.clear();
                rideHistoryList.addAll(historyItems);
                
                // Load user names for each ride
                loadUserNamesForHistory();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load commuter ride history", e);
                showErrorState("Failed to load ride history");
            });
    }
    
    private void loadUserNamesForHistory() {
        // Load names for all rides
        for (RideHistoryItem item : rideHistoryList) {
            if (item.userRole.equals("driver")) {
                // Load commuter name
                firebaseService.getUserName(item.commuterId, "commuter")
                    .addOnSuccessListener(name -> {
                        item.commuterName = name;
                        adapter.notifyDataSetChanged();
                    });
            } else {
                // Load driver name
                firebaseService.getUserName(item.driverId, "driver")
                    .addOnSuccessListener(name -> {
                        item.driverName = name;
                        adapter.notifyDataSetChanged();
                    });
            }
        }
        
        // Update UI
        updateUI();
    }
    
    private void updateUI() {
        if (rideHistoryList.isEmpty()) {
            showEmptyState();
        } else {
            showContentState();
        }
        adapter.notifyDataSetChanged();
    }
    
    private void showLoadingState() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        rvRideHistory.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
    }
    
    private void showContentState() {
        progressBar.setVisibility(android.view.View.GONE);
        rvRideHistory.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);
    }
    
    private void showEmptyState() {
        progressBar.setVisibility(android.view.View.GONE);
        rvRideHistory.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setText("No ride history found");
    }
    
    private void showErrorState(String message) {
        progressBar.setVisibility(android.view.View.GONE);
        rvRideHistory.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
