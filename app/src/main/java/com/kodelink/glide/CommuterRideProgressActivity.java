package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CommuterRideProgressActivity - Handles the ride in progress screen for commuters
 * 
 * This activity shows:
 * - Current ride details
 * - Map with route
 * - Completion status for both driver and commuter
 * - Complete ride button (only enabled when both parties are ready)
 */
public class CommuterRideProgressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CommuterRideProgress";
    
    // UI Components
    private ImageButton btnBack;
    private TextView tvTitle;
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvDriverName;
    private TextView tvRideStatus;
    private TextView tvDistance;
    private TextView tvDuration;
    private TextView tvFare;
    private LinearLayout completionStatusLayout;
    private ImageView ivDriverStatus;
    private TextView tvDriverStatus;
    private ImageView ivCommuterStatus;
    private TextView tvCommuterStatus;
    private Button btnCompleteRide;
    
    // Data
    private FirebaseService firebaseService;
    private RideRequest currentRide;
    private String currentCommuterId;
    private String rideId;
    
    // Completion tracking
    private boolean driverReadyToComplete = false;
    private boolean commuterReadyToComplete = false;
    private ValueEventListener rideStatusListener;
    
    // Map loading tracking - once loaded, never reset
    private boolean mapLoaded = false;
    private long mapLoadTime = 0;
    
    // Map components
    private Marker driverMarker;
    private Marker pickupMarker;
    private Marker destinationMarker;
    private Polyline routePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commuter_ride_progress);
        
        // Get ride ID from intent
        rideId = getIntent().getStringExtra("rideId");
        if (rideId == null) {
            Toast.makeText(this, "No ride ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();
        
        // Initialize UI components
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();
        
        // Initialize map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        // Get current commuter ID and load ride details
        loadRideDetails();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Handle new intent - don't reload map if already loaded
        String newRideId = intent.getStringExtra("rideId");
        if (newRideId != null && !newRideId.equals(rideId)) {
            rideId = newRideId;
            // Only reload data if ride ID changed, but don't reload map
            loadRideDetails();
        } else if (mapLoaded) {
            Log.d(TAG, "Same activity brought to front, map already loaded - no reload needed");
        }
    }
    
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        mapView = findViewById(R.id.mapView);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvRideStatus = findViewById(R.id.tvRideStatus);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);
        tvFare = findViewById(R.id.tvFare);
        completionStatusLayout = findViewById(R.id.completionStatusLayout);
        ivDriverStatus = findViewById(R.id.ivDriverStatus);
        tvDriverStatus = findViewById(R.id.tvDriverStatus);
        ivCommuterStatus = findViewById(R.id.ivCommuterStatus);
        tvCommuterStatus = findViewById(R.id.tvCommuterStatus);
        btnCompleteRide = findViewById(R.id.btnCompleteRide);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            // Go back to commuter home
            Intent intent = new Intent(this, HomeCommuterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        btnCompleteRide.setOnClickListener(v -> {
            if (!commuterReadyToComplete) {
                // Mark commuter as ready to complete
                markCommuterReadyToComplete();
            } else {
                Toast.makeText(this, "You have already marked yourself as ready. Waiting for driver to confirm.", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void loadRideDetails() {
        // Get current commuter ID
        firebaseService.getCurrentUserEntityId()
            .addOnSuccessListener(commuterId -> {
                if (commuterId != null) {
                    currentCommuterId = commuterId;
                    loadRideData();
                } else {
                    Toast.makeText(this, "Commuter ID not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get commuter ID", e);
                Toast.makeText(this, "Failed to load commuter information", Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void loadRideData() {
        // Get ride details from Firestore
        firebaseService.getRideRequestDetails(rideId)
            .addOnSuccessListener(rideRequest -> {
                if (rideRequest != null) {
                    currentRide = rideRequest;
                    updateUI();
                    setupRideStatusListener();
                } else {
                    Toast.makeText(this, "Ride not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load ride details", e);
                Toast.makeText(this, "Failed to load ride details", Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void updateUI() {
        if (currentRide == null) return;
        
        // Update ride information
        tvDriverName.setText("Driver"); // You might want to get actual driver name
        tvRideStatus.setText("Ride in Progress");
        
        // Calculate and display stats
        if (currentRide.pickupLocation != null && currentRide.destination != null) {
            double distance = calculateDistance(
                new LatLng(currentRide.pickupLocation.lat, currentRide.pickupLocation.lng),
                new LatLng(currentRide.destination.lat, currentRide.destination.lng)
            );
            tvDistance.setText(String.format("%.1f km", distance));
        }
        
        // Set fare
        tvFare.setText("$" + String.format("%.2f", currentRide.priceEach));
        
        // Update map only once when it's ready and not already loaded
        // This ensures map is only loaded once when driver accepts the ride
        if (googleMap != null && !mapLoaded) {
            Log.d(TAG, "Loading map for the first time at " + System.currentTimeMillis());
            updateMap();
            mapLoaded = true;
            mapLoadTime = System.currentTimeMillis();
        } else if (mapLoaded) {
            Log.d(TAG, "Map already loaded at " + mapLoadTime + ", skipping reload");
        }
    }
    
    private void setupRideStatusListener() {
        // Listen for ride status changes
        rideStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    Boolean driverReady = dataSnapshot.child("driverReadyToComplete").getValue(Boolean.class);
                    Boolean commuterReady = dataSnapshot.child("commuterReadyToComplete").getValue(Boolean.class);
                    
                    updateCompletionStatus(driverReady != null ? driverReady : false, 
                                         commuterReady != null ? commuterReady : false);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to listen to ride status", databaseError.toException());
            }
        };
        
        firebaseService.listenRideRequest(rideId, rideStatusListener);
    }
    
    private void updateCompletionStatus(boolean driverReady, boolean commuterReady) {
        Log.d(TAG, "Status update - Driver ready: " + driverReady + ", Commuter ready: " + commuterReady + ", Map loaded: " + mapLoaded);
        
        driverReadyToComplete = driverReady;
        commuterReadyToComplete = commuterReady;
        
        // Update driver status
        if (driverReady) {
            ivDriverStatus.setImageResource(R.drawable.ic_check_circle);
            tvDriverStatus.setText("Driver Ready");
            tvDriverStatus.setTextColor(getResources().getColor(R.color.green));
        } else {
            ivDriverStatus.setImageResource(R.drawable.ic_pending);
            tvDriverStatus.setText("Waiting for Driver");
            tvDriverStatus.setTextColor(getResources().getColor(R.color.orange));
        }
        
        // Update commuter status
        if (commuterReady) {
            ivCommuterStatus.setImageResource(R.drawable.ic_check_circle);
            tvCommuterStatus.setText("Commuter Ready");
            tvCommuterStatus.setTextColor(getResources().getColor(R.color.green));
        } else {
            ivCommuterStatus.setImageResource(R.drawable.ic_pending);
            tvCommuterStatus.setText("Commuter Not Ready");
            tvCommuterStatus.setTextColor(getResources().getColor(R.color.orange));
        }
        
        // Update complete button based on commuter status
        if (commuterReady) {
            btnCompleteRide.setText("Complete Ride ✓");
            btnCompleteRide.setEnabled(false);
            btnCompleteRide.setBackgroundResource(R.drawable.button_confirm_background);
        } else {
            btnCompleteRide.setText("Complete Ride");
            btnCompleteRide.setEnabled(true);
            btnCompleteRide.setBackgroundResource(R.drawable.button_confirm_background);
        }
        
        // Check if both parties are ready and show loading animation
        if (driverReady && commuterReady) {
            showLoadingAndCompleteRide();
        }
    }
    
    private void showLoadingAndCompleteRide() {
        // Show loading animation
        btnCompleteRide.setText("Completing Ride...");
        btnCompleteRide.setEnabled(false);
        btnCompleteRide.setBackgroundResource(R.drawable.button_confirm_background);
        
        // Show toast message
        Toast.makeText(this, "Both parties ready! Completing ride...", Toast.LENGTH_LONG).show();
        
        // Add a small delay for loading animation effect
        new android.os.Handler().postDelayed(() -> {
            completeRide();
        }, 2000); // 2 second loading animation
    }
    
    private void completeRide() {
        // Complete the ride
        firebaseService.completeRideRequest(rideId)
            .addOnSuccessListener(aVoid -> {
                // Show completion dialog with ride details
                showRideCompletionDialog();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to complete ride", e);
                Toast.makeText(this, "Failed to complete ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void showRideCompletionDialog() {
        // Get ride details for the dialog
        String distance = tvDistance.getText().toString();
        String duration = tvDuration.getText().toString();
        String fare = tvFare.getText().toString();
        
        // Show completion dialog
        RideCompletionDialog dialog = new RideCompletionDialog(
            this, 
            currentRide, 
            "passenger", 
            distance, 
            duration, 
            fare
        );
        dialog.show();
    }
    
    private void markCommuterReadyToComplete() {
        // Use the FirebaseService method
        firebaseService.markCommuterReadyToComplete(rideId)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Commuter marked as ready to complete");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to mark commuter as ready", e);
            });
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        // Only update map if it hasn't been loaded yet
        if (!mapLoaded && currentRide != null) {
            Log.d(TAG, "Map ready, loading for the first time at " + System.currentTimeMillis());
            updateMap();
            mapLoaded = true;
            mapLoadTime = System.currentTimeMillis();
        } else if (mapLoaded) {
            Log.d(TAG, "Map ready but already loaded at " + mapLoadTime + ", skipping reload");
        }
    }
    
    private void updateMap() {
        if (googleMap == null || currentRide == null) return;
        
        // Double-check to prevent any accidental reloads
        if (mapLoaded) {
            Log.d(TAG, "Map already loaded, preventing reload");
            return;
        }
        
        Log.d(TAG, "Updating map with ride data");
        
        // Clear existing markers and polylines
        if (driverMarker != null) driverMarker.remove();
        if (pickupMarker != null) pickupMarker.remove();
        if (destinationMarker != null) destinationMarker.remove();
        if (routePolyline != null) routePolyline.remove();
        
        // Add pickup marker
        if (currentRide.pickupLocation != null) {
            LatLng pickupLatLng = new LatLng(currentRide.pickupLocation.lat, currentRide.pickupLocation.lng);
            pickupMarker = googleMap.addMarker(new MarkerOptions()
                .position(pickupLatLng)
                .title("Pickup Location"));
        }
        
        // Add destination marker
        if (currentRide.destination != null) {
            LatLng destLatLng = new LatLng(currentRide.destination.lat, currentRide.destination.lng);
            destinationMarker = googleMap.addMarker(new MarkerOptions()
                .position(destLatLng)
                .title("Destination"));
        }
        
        // Add route polyline if both locations exist
        if (currentRide.pickupLocation != null && currentRide.destination != null) {
            LatLng pickup = new LatLng(currentRide.pickupLocation.lat, currentRide.pickupLocation.lng);
            LatLng destination = new LatLng(currentRide.destination.lat, currentRide.destination.lng);
            
            // Get route from Google Directions API
            drawRoute(pickup, destination);
        }
        
        // Move camera to show both locations
        if (currentRide.pickupLocation != null && currentRide.destination != null) {
            LatLng pickup = new LatLng(currentRide.pickupLocation.lat, currentRide.pickupLocation.lng);
            LatLng destination = new LatLng(currentRide.destination.lat, currentRide.destination.lng);
            moveCameraToShowRoute(pickup, destination);
        }
    }
    
    private void moveCameraToShowRoute(LatLng pickup, LatLng destination) {
        if (googleMap == null) return;
        
        // Calculate bounds to show both locations
        double minLat = Math.min(pickup.latitude, destination.latitude);
        double maxLat = Math.max(pickup.latitude, destination.latitude);
        double minLng = Math.min(pickup.longitude, destination.longitude);
        double maxLng = Math.max(pickup.longitude, destination.longitude);
        
        // Add padding
        double latPadding = (maxLat - minLat) * 0.1;
        double lngPadding = (maxLng - minLng) * 0.1;
        
        com.google.android.gms.maps.model.LatLngBounds bounds = new com.google.android.gms.maps.model.LatLngBounds(
            new LatLng(minLat - latPadding, minLng - lngPadding),
            new LatLng(maxLat + latPadding, maxLng + lngPadding)
        );
        
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
    
    private double calculateDistance(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null) return 0;
        
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;
        
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        
        // Remove listener
        if (rideStatusListener != null) {
            firebaseService.removeRideRequestsListener(rideStatusListener);
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    
    /**
     * Draw route from origin to destination using Google Maps Directions API
     */
    private void drawRoute(LatLng origin, LatLng destination) {
        // Prevent route drawing if map is already loaded
        if (mapLoaded) {
            Log.d(TAG, "Map already loaded, skipping route drawing");
            return;
        }
        
        Log.d(TAG, "Drawing route for the first time");
        
        // Remove existing route polyline
        if (routePolyline != null) {
            routePolyline.remove();
        }
        
        // Get route from Google Maps Directions API
        getRouteFromDirectionsAPI(origin, destination);
    }
    
    /**
     * Get route data from Google Maps Directions API
     */
    private void getRouteFromDirectionsAPI(LatLng origin, LatLng destination) {
        // Prevent API calls if map is already loaded
        if (mapLoaded) {
            Log.d(TAG, "Map already loaded, skipping API call");
            return;
        }
        
        String apiKey = "AIzaSyDc8_axTnQWPUiBWVgp1ifK0zV8Zy21Tqw";
        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;
        
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + originStr +
                "&destination=" + destinationStr +
                "&key=" + apiKey;
        
        Log.d("CommuterRideProgressDirectionsAPI", "Requesting route: " + url);
        
        // Execute API call in background thread
        new Thread(() -> {
            try {
                String response = makeHttpRequest(url);
                runOnUiThread(() -> parseDirectionsResponse(response, origin, destination));
            } catch (Exception e) {
                Log.e("CommuterRideProgressDirectionsAPI", "Error getting directions: " + e.getMessage());
                runOnUiThread(() -> {
                    // Fallback to straight line if API fails
                    drawStraightLineRoute(origin, destination);
                    Toast.makeText(this, "Could not get route details. Showing direct path.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * Make HTTP request to Google Directions API
     */
    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        
        reader.close();
        inputStream.close();
        connection.disconnect();
        
        return response.toString();
    }
    
    /**
     * Parse the Directions API response and draw the route
     */
    private void parseDirectionsResponse(String response, LatLng origin, LatLng destination) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.getString("status");
            
            if (!status.equals("OK")) {
                Log.e("CommuterRideProgressDirectionsAPI", "Directions API error: " + status);
                drawStraightLineRoute(origin, destination);
                return;
            }
            
            JSONArray routes = jsonResponse.getJSONArray("routes");
            if (routes.length() == 0) {
                Log.e("CommuterRideProgressDirectionsAPI", "No routes found");
                drawStraightLineRoute(origin, destination);
                return;
            }
            
            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            
            // Extract distance and duration
            JSONObject distance = leg.getJSONObject("distance");
            JSONObject duration = leg.getJSONObject("duration");
            
            String distanceText = distance.getString("text");
            String durationText = duration.getString("text");
            
            // Extract route points
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPolyline = overviewPolyline.getString("points");
            
            // Decode polyline and draw route
            List<LatLng> routePoints = decodePolyline(encodedPolyline);
            drawRoutePolyline(routePoints);
            
            // Update route information in UI
            updateRouteInfo(distanceText, durationText);
            
            Log.d("CommuterRideProgressDirectionsAPI", "Route found: " + distanceText + ", " + durationText);
            
        } catch (JSONException e) {
            Log.e("CommuterRideProgressDirectionsAPI", "Error parsing directions response: " + e.getMessage());
            drawStraightLineRoute(origin, destination);
        }
    }
    
    /**
     * Draw straight line route as fallback
     */
    private void drawStraightLineRoute(LatLng origin, LatLng destination) {
        routePolyline = googleMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(5)
                .color(getResources().getColor(R.color.purple_500)));
    }
    
    /**
     * Draw route polyline from decoded points
     */
    private void drawRoutePolyline(List<LatLng> points) {
        if (points == null || points.isEmpty()) return;
        
        routePolyline = googleMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(5)
                .color(getResources().getColor(R.color.purple_500)));
    }
    
    /**
     * Decode polyline string to list of LatLng points
     */
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            
            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        
        return poly;
    }
    
    /**
     * Update route information in the UI
     */
    private void updateRouteInfo(String distance, String duration) {
        if (tvDistance != null) {
            tvDistance.setText(distance);
        }
        if (tvDuration != null) {
            tvDuration.setText(duration);
        }
    }
}
