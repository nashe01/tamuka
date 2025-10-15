package com.kodelink.glide;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * RideCompletionDialog - Shows completion notification for both driver and passenger
 * 
 * This dialog displays:
 * - Success message
 * - Ride summary (distance, duration, fare)
 * - OK button that navigates to respective screens
 */
public class RideCompletionDialog extends Dialog {

    private Context context;
    private RideRequest rideRequest;
    private String userType; // "driver" or "passenger"
    private String distance;
    private String duration;
    private String fare;

    public RideCompletionDialog(@NonNull Context context, RideRequest rideRequest, 
                               String userType, String distance, String duration, String fare) {
        super(context);
        this.context = context;
        this.rideRequest = rideRequest;
        this.userType = userType;
        this.distance = distance;
        this.duration = duration;
        this.fare = fare;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_ride_completion);
        
        // Set dialog properties
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(false);
        
        // Initialize views
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();
    }
    
    private void initializeViews() {
        // Update ride details
        TextView tvDistance = findViewById(R.id.tvCompletionDistance);
        TextView tvDuration = findViewById(R.id.tvCompletionDuration);
        TextView tvFare = findViewById(R.id.tvCompletionFare);
        
        if (tvDistance != null) tvDistance.setText(distance);
        if (tvDuration != null) tvDuration.setText(duration);
        if (tvFare != null) tvFare.setText(fare);
    }
    
    private void setupClickListeners() {
        Button btnOk = findViewById(R.id.btnRideCompletionOk);
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                dismiss();
                navigateToRespectiveScreen();
            });
        }
    }
    
    private void navigateToRespectiveScreen() {
        Intent intent;
        
        if ("driver".equals(userType)) {
            // Navigate to driver dashboard
            intent = new Intent(context, DashboardDriverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            // Navigate to passenger dashboard (HomeCommuterActivity)
            intent = new Intent(context, HomeCommuterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        
        context.startActivity(intent);
    }
}
