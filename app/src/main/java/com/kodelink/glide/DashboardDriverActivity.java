package com.kodelink.glide;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DashboardDriverActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "DriverPrefs";
    private static final String KEY_DRIVER_STATUS = "driver_status";
    
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MaterialButton btnMenu;
    private SwitchMaterial switchAvailability;
    private TextView tvAvailabilityStatus;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SharedPreferences prefs;
    
    // Ride request handling variables
    private FirebaseService firebaseService;
    private String currentDriverId;
    private LatLng currentLocation;
    private RideRequest currentRideRequest;
    private Polyline routeToPickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_driver);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        btnMenu = findViewById(R.id.btnMenu);
        switchAvailability = findViewById(R.id.switchAvailability);
        tvAvailabilityStatus = findViewById(R.id.tvAvailabilityStatus);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Initialize preferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();
        
        // Get current user's entity ID (driverId)
        firebaseService.getCurrentUserEntityId()
            .addOnSuccessListener(entityId -> {
                if (entityId != null) {
                    currentDriverId = entityId;
                    Log.d("DriverActivity", "Current driver ID: " + currentDriverId);
                    
                    // Set up ride request listening now that we have the driver ID
                    setupRideRequestListening();
                } else {
                    Toast.makeText(this, "Driver profile not found. Please register again.", Toast.LENGTH_LONG).show();
                    logout();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("DriverActivity", "Failed to get current user entity ID", e);
                Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
                logout();
            });

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Set up navigation drawer
        navigationView.setNavigationItemSelectedListener(this);
        
        // Set up button listeners
        btnMenu.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Set up availability toggle
        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentDriverId == null) {
                Toast.makeText(this, "Driver ID not available. Please wait...", Toast.LENGTH_SHORT).show();
                switchAvailability.setChecked(!isChecked); // Revert the change
                return;
            }
            
            String status = isChecked ? "Available" : "Offline";
            int color = isChecked ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);
            
            tvAvailabilityStatus.setText(status);
            tvAvailabilityStatus.setTextColor(color);
            
            // Save status to SharedPreferences
            prefs.edit().putBoolean(KEY_DRIVER_STATUS, isChecked).apply();
            
            // Update availability in both Firestore and Realtime Database
            String availability = isChecked ? "available" : "unavailable";
            firebaseService.updateDriverStatus(currentDriverId, availability);
            
            Toast.makeText(DashboardDriverActivity.this, "Status updated: " + status, Toast.LENGTH_SHORT).show();
        });

        // Load saved availability status
        boolean isAvailable = prefs.getBoolean(KEY_DRIVER_STATUS, false);
        switchAvailability.setChecked(isAvailable);
        String status = isAvailable ? "Available" : "Offline";
        int color = isAvailable ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);
        tvAvailabilityStatus.setText(status);
        tvAvailabilityStatus.setTextColor(color);

        // Update header with user role
        updateNavigationHeader();

        // Request location permission
        requestLocationPermission();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        
        // Enable location button
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            
            Log.d("Location", "Getting current location...");
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Log.d("Location", "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            
                            // Add marker for current location
                            googleMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Your Location"));
                            
                            // Move camera to current location
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                            
                            // Update driver location in Firebase
                            updateDriverLocationInFirebase();
                        } else {
                            Log.w("Location", "Location is null - using default location");
                            // Use default location if GPS location is not available
                            currentLocation = new LatLng(-17.82486, 31.05343); // Harare CBD
                            updateDriverLocationInFirebase();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Location", "Failed to get location", e);
                        // Use default location if location service fails
                        currentLocation = new LatLng(-17.82486, 31.05343); // Harare CBD
                        updateDriverLocationInFirebase();
                    });
        } else {
            Log.w("Location", "Location permission not granted - using default location");
            // Use default location if permission not granted
            currentLocation = new LatLng(-17.82486, 31.05343); // Harare CBD
            updateDriverLocationInFirebase();
        }
    }
    
    private void updateDriverLocationInFirebase() {
        if (currentLocation != null && currentDriverId != null) {
            Log.d("LocationUpdate", "Updating driver location: " + currentLocation.latitude + ", " + currentLocation.longitude);
            
            // Update location in Realtime Database for live updates
            firebaseService.updateDriverLocationLive(currentDriverId, currentLocation.latitude, currentLocation.longitude);
            
            // Update location in Firestore periodically (every 10 seconds or when stopped)
            // For now, we'll update it every time, but in production you'd want to throttle this
            firebaseService.updateDriverLocationFirestore(currentDriverId, currentLocation.latitude, currentLocation.longitude)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LocationUpdate", "Driver location updated in Firestore: " + currentLocation.latitude + ", " + currentLocation.longitude);
                })
                .addOnFailureListener(e -> {
                    Log.e("LocationUpdate", "Failed to update driver location in Firestore", e);
                });
        } else {
            Log.w("LocationUpdate", "Cannot update location - currentLocation: " + currentLocation + ", currentDriverId: " + currentDriverId);
        }
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_home) {
            // Already on home screen, just close drawer
            Toast.makeText(this, "You're already on the home screen", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_ride_history) {
            Toast.makeText(this, "Ride History clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement ride history screen
        } else if (id == R.id.nav_notifications) {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logout();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateNavigationHeader() {
        SharedPreferences authPrefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        String currentUserPhone = authPrefs.getString("current_user_phone", "");
        String role = authPrefs.getString(currentUserPhone + "_role", "driver");
        
        TextView tvUserRole = navigationView.getHeaderView(0).findViewById(R.id.tvUserRole);
        if (tvUserRole != null) {
            tvUserRole.setText(role.equals("driver") ? "Driver" : "Commuter");
        }
    }

    private void logout() {
        // Clear SharedPreferences
        prefs.edit().clear().apply();
        SharedPreferences authPrefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        authPrefs.edit().clear().apply();
        
        // Sign out from Firebase Auth if using Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Navigate to login screen
        Intent intent = new Intent(DashboardDriverActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void setupRideRequestListening() {
        if (currentDriverId == null) {
            // Wait for driver ID to be available
            return;
        }
        
        // Listen for incoming ride requests from Realtime Database
        firebaseService.listenDriverRideRequests(currentDriverId, new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equals(status)) {
                        String rideId = snapshot.getKey();
                        if (rideId != null) {
                            // Get full ride request details from Firestore
                            firebaseService.getRideRequestDetails(rideId)
                                .addOnSuccessListener(rideRequest -> {
                                    if (rideRequest != null) {
                                        currentRideRequest = rideRequest;
                                        showIncomingRideRequestDialog(rideRequest);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("RideRequest", "Failed to get ride request details", e);
                                });
                        }
                    }
                }
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(DashboardDriverActivity.this, "Error listening to ride requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showIncomingRideRequestDialog(RideRequest rideRequest) {
        String message = "New ride request!\n\n" +
                "Pickup: " + rideRequest.pickupLocation.address + "\n" +
                "Destination: " + rideRequest.destination.address + "\n" +
                "Distance: " + calculateDistance(
                        new LatLng(rideRequest.pickupLocation.lat, rideRequest.pickupLocation.lng),
                        currentLocation
                ) + " km";
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Incoming Ride Request")
                .setMessage(message)
                .setPositiveButton("Accept", (dialog, which) -> {
                    acceptRideRequest(rideRequest);
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    declineRideRequest(rideRequest);
                })
                .setCancelable(false)
                .show();
    }
    
    private void acceptRideRequest(RideRequest rideRequest) {
        // Update ride request status to accepted in both databases
        firebaseService.updateRideRequestStatus(rideRequest.rideId, "accepted")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(DashboardDriverActivity.this, "Ride accepted! Showing route to pickup location.", Toast.LENGTH_LONG).show();
                
                // Draw route from driver location to pickup location
                if (currentLocation != null) {
                    LatLng pickupLocation = new LatLng(rideRequest.pickupLocation.lat, rideRequest.pickupLocation.lng);
                    drawRouteToPickup(currentLocation, pickupLocation);
                    
                    // Move camera to show both locations
                    moveCameraToShowRoute(currentLocation, pickupLocation);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(DashboardDriverActivity.this, "Failed to accept ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void declineRideRequest(RideRequest rideRequest) {
        // Update ride request status to declined in both databases
        firebaseService.updateRideRequestStatus(rideRequest.rideId, "declined")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(DashboardDriverActivity.this, "Ride declined", Toast.LENGTH_SHORT).show();
                // Clear any existing route
                clearRouteToPickup();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(DashboardDriverActivity.this, "Failed to decline ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
        double distance = R * c; // convert to kilometers
        
        return Math.round(distance * 100.0) / 100.0;
    }
    
    /**
     * Draw route from driver location to pickup location using Google Maps Directions API
     */
    private void drawRouteToPickup(LatLng origin, LatLng destination) {
        // Remove existing route polyline
        if (routeToPickup != null) {
            routeToPickup.remove();
        }
        
        // Get route from Google Maps Directions API
        getRouteFromDirectionsAPI(origin, destination);
    }
    
    /**
     * Get route data from Google Maps Directions API
     */
    private void getRouteFromDirectionsAPI(LatLng origin, LatLng destination) {
        String apiKey = "AIzaSyDc8_axTnQWPUiBWVgp1ifK0zV8Zy21Tqw";
        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;
        
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + originStr +
                "&destination=" + destinationStr +
                "&key=" + apiKey;
        
        Log.d("DriverDirectionsAPI", "Requesting route to pickup: " + url);
        
        // Execute API call in background thread
        new Thread(() -> {
            try {
                String response = makeHttpRequest(url);
                runOnUiThread(() -> parseDirectionsResponse(response, origin, destination));
            } catch (Exception e) {
                Log.e("DriverDirectionsAPI", "Error getting directions: " + e.getMessage());
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
                Log.e("DriverDirectionsAPI", "Directions API error: " + status);
                drawStraightLineRoute(origin, destination);
                return;
            }
            
            JSONArray routes = jsonResponse.getJSONArray("routes");
            if (routes.length() == 0) {
                Log.e("DriverDirectionsAPI", "No routes found");
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
            
            // Show route information
            showRouteInfo(distanceText, durationText);
            
            Log.d("DriverDirectionsAPI", "Route to pickup found: " + distanceText + ", " + durationText);
            
        } catch (JSONException e) {
            Log.e("DriverDirectionsAPI", "Error parsing directions response: " + e.getMessage());
            drawStraightLineRoute(origin, destination);
        }
    }
    
    /**
     * Decode Google's encoded polyline string
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
            
            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }
        
        return poly;
    }
    
    /**
     * Draw the route polyline on the map
     */
    private void drawRoutePolyline(List<LatLng> routePoints) {
        if (routePoints.isEmpty()) return;
        
        routeToPickup = googleMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(8)
                .color(0xFF6200EE) // Primary color (purple)
                .geodesic(true));
    }
    
    /**
     * Fallback method to draw straight line route
     */
    private void drawStraightLineRoute(LatLng origin, LatLng destination) {
        List<LatLng> routePoints = new ArrayList<>();
        routePoints.add(origin);
        routePoints.add(destination);
        drawRoutePolyline(routePoints);
    }
    
    /**
     * Show route information (distance and duration)
     */
    private void showRouteInfo(String distance, String duration) {
        String routeInfo = "Route to pickup: " + distance + " • " + duration;
        Toast.makeText(this, routeInfo, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Move camera to show both driver location and pickup location
     */
    private void moveCameraToShowRoute(LatLng driverLocation, LatLng pickupLocation) {
        if (driverLocation == null || pickupLocation == null) return;
        
        // Calculate bounds to show both locations
        LatLng southwest = new LatLng(
                Math.min(driverLocation.latitude, pickupLocation.latitude),
                Math.min(driverLocation.longitude, pickupLocation.longitude)
        );
        LatLng northeast = new LatLng(
                Math.max(driverLocation.latitude, pickupLocation.latitude),
                Math.max(driverLocation.longitude, pickupLocation.longitude)
        );
        
        // Move camera to show both locations
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                new com.google.android.gms.maps.model.LatLngBounds(southwest, northeast), 100));
    }
    
    /**
     * Clear the route to pickup location
     */
    private void clearRouteToPickup() {
        if (routeToPickup != null) {
            routeToPickup.remove();
            routeToPickup = null;
        }
    }
    
    // This method is no longer needed as driver profiles are created during registration
    // The driver profile should already exist in Firestore from the registration process
}
