package com.kodelink.glide;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * DatabaseCleanupActivity - Manual database cleanup interface
 * 
 * This activity provides buttons to manually clean up test data from the database.
 * Use this for development and testing purposes.
 */
public class DatabaseCleanupActivity extends AppCompatActivity {
    
    private DatabaseCleanup cleanup;
    private TextView tvStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically
        createCleanupLayout();
        
        // Initialize cleanup manager
        cleanup = new DatabaseCleanup();
    }
    
    private void createCleanupLayout() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        
        // Title
        TextView title = new TextView(this);
        title.setText("Database Cleanup");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 32);
        mainLayout.addView(title);
        
        // Status text
        tvStatus = new TextView(this);
        tvStatus.setText("Ready to clean up database");
        tvStatus.setTextSize(16);
        tvStatus.setPadding(0, 0, 0, 32);
        mainLayout.addView(tvStatus);
        
        // Cleanup buttons
        Button btnCleanupRides = new Button(this);
        btnCleanupRides.setText("Clean Test Ride Requests");
        btnCleanupRides.setOnClickListener(v -> {
            cleanup.cleanupTestRideRequests();
            updateStatus("Cleaning up test ride requests...");
            Toast.makeText(this, "Test ride requests cleanup started", Toast.LENGTH_SHORT).show();
            
            // Show completion notification after a delay
            new android.os.Handler().postDelayed(() -> {
                updateStatus("Test ride requests cleanup completed!");
                Toast.makeText(this, "✅ Test ride requests cleanup completed!", Toast.LENGTH_LONG).show();
            }, 3000); // 3 second delay for completion
        });
        mainLayout.addView(btnCleanupRides);
        
        Button btnCleanupAll = new Button(this);
        btnCleanupAll.setText("Clean All Test Data");
        btnCleanupAll.setOnClickListener(v -> {
            cleanup.cleanupAllTestData();
            updateStatus("Cleaning up all test data...");
            Toast.makeText(this, "All test data cleanup started", Toast.LENGTH_SHORT).show();
            
            // Show completion notification after a delay
            new android.os.Handler().postDelayed(() -> {
                updateStatus("All test data cleanup completed!");
                Toast.makeText(this, "✅ All test data cleanup completed!", Toast.LENGTH_LONG).show();
            }, 4000); // 4 second delay for completion
        });
        mainLayout.addView(btnCleanupAll);
        
        Button btnCleanupCompleted = new Button(this);
        btnCleanupCompleted.setText("Clean Completed Rides");
        btnCleanupCompleted.setOnClickListener(v -> {
            cleanup.cleanupCompletedRides();
            updateStatus("Cleaning up completed rides...");
            Toast.makeText(this, "Completed rides cleanup started", Toast.LENGTH_SHORT).show();
            
            // Show completion notification after a delay
            new android.os.Handler().postDelayed(() -> {
                updateStatus("Completed rides cleanup completed!");
                Toast.makeText(this, "✅ Completed rides cleanup completed!", Toast.LENGTH_LONG).show();
            }, 2000); // 2 second delay for completion
        });
        mainLayout.addView(btnCleanupCompleted);
        
        Button btnCleanupPending = new Button(this);
        btnCleanupPending.setText("Clear Pending Rides");
        btnCleanupPending.setOnClickListener(v -> {
            cleanup.cleanupPendingRides();
            updateStatus("Clearing pending rides...");
            Toast.makeText(this, "Pending rides cleanup started", Toast.LENGTH_SHORT).show();
            
            // Show completion notification after a delay
            new android.os.Handler().postDelayed(() -> {
                updateStatus("Pending rides cleanup completed!");
                Toast.makeText(this, "✅ Pending rides cleanup completed!", Toast.LENGTH_LONG).show();
            }, 3000); // 3 second delay for completion
        });
        mainLayout.addView(btnCleanupPending);
        
        Button btnCleanupActive = new Button(this);
        btnCleanupActive.setText("Clear Active Rides");
        btnCleanupActive.setOnClickListener(v -> {
            cleanup.cleanupActiveRides();
            updateStatus("Clearing active rides...");
            Toast.makeText(this, "Active rides cleanup started", Toast.LENGTH_SHORT).show();
            
            // Show completion notification after a delay
            new android.os.Handler().postDelayed(() -> {
                updateStatus("Active rides cleanup completed!");
                Toast.makeText(this, "✅ Active rides cleanup completed!", Toast.LENGTH_LONG).show();
            }, 3000); // 3 second delay for completion
        });
        mainLayout.addView(btnCleanupActive);
        
        Button btnGetStats = new Button(this);
        btnGetStats.setText("Get Database Statistics");
        btnGetStats.setOnClickListener(v -> {
            cleanup.getDatabaseStats();
            updateStatus("Retrieving database statistics...");
            Toast.makeText(this, "Database statistics requested - check logs", Toast.LENGTH_SHORT).show();
        });
        mainLayout.addView(btnGetStats);
        
        setContentView(mainLayout);
    }
    
    private void updateStatus(String status) {
        tvStatus.setText("Status: " + status);
        Log.d("DatabaseCleanup", "Status: " + status);
    }
}
