package com.kodelink.glide;

// Android core imports for permissions, location, and UI components
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

// AndroidX support library imports
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

// Google Play Services imports for location and maps
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

// Google Places API imports for location autocomplete
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * HomeCommuterActivity - Main activity for commuters in the Swift Ride app
 * 
 * This is the primary interface for commuters to:
 * - View available drivers on an interactive map
 * - Search for destinations using Google Places API
 * - Select drivers and request rides
 * - View ride history and manage profile
 * - Navigate through the app using a side drawer menu
 * 
 * Key Features:
 * - Real-time driver location display on Google Maps
 * - Interactive driver selection with sliding cards
 * - Destination search with autocomplete suggestions
 * - Ride request management with input validation
 * - Navigation drawer with user profile and ride history
 * - Smooth animations for UI interactions
 * 
 * Technical Implementation:
 * - Uses Google Maps API for map display and interactions
 * - Integrates Google Places API for location search
 * - Firebase Realtime Database for live driver updates
 * - Firebase Firestore for persistent data storage
 * - Material Design components for modern UI
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class HomeCommuterActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMapLongClickListener {

    // Permission request code for location access
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    // Map-related components
    private MapView mapView;                    // Map view container
    private GoogleMap googleMap;                // Google Maps instance
    private FusedLocationProviderClient fusedLocationClient;  // Location services client
    // UI components for navigation and search
    private ImageButton btnMenu;                // Menu button to open navigation drawer
    private TextInputEditText etSearch;         // Search input field for destinations
    private TextInputLayout searchLayout;       // Search input layout container
    private DrawerLayout drawerLayout;          // Navigation drawer layout
    private NavigationView navigationView;      // Navigation drawer view
    // Shared preferences for storing user data
    private SharedPreferences prefs;
    
    // Google Places API components
    private PlacesClient placesClient;          // Places API client for location search
    private AutocompleteSessionToken sessionToken;  // Session token for autocomplete requests
    
    // Suggestions UI
    private RecyclerView rvSuggestions;
    private PlaceSuggestionAdapter suggestionAdapter;
    
    // Ride request workflow variables
    private LatLng currentLocation;
    private LatLng destinationLocation;
    private Marker destinationMarker;
    private Polyline routePolyline;
    private List<Driver> availableDrivers = new ArrayList<>();
    private List<Marker> driverMarkers = new ArrayList<>();
    private FirebaseService firebaseService;
    private String currentUserId;
    private MaterialCardView driverInfoCard;
    private Driver selectedDriver;
    private CustomInfoWindowAdapter infoWindowAdapter;
    
    // Active ride tracking
    private RideRequest activeRideRequest;
    private com.google.firebase.database.ValueEventListener activeRideListener;
    private boolean hasActiveRide = false;
    
    // Driver tracking for timeout handling
    private String currentRequestedDriverId;
    private List<String> timedOutDriverIds = new ArrayList<>();
    
    // Timeout handling
    private android.os.Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Runnable countdownRunnable;
    private long rideRequestStartTime;
    private static final long TIMEOUT_DURATION = 2 * 60 * 1000; // 2 minutes in milliseconds
    private int countdownSeconds = 120; // 2 minutes in seconds
    
    // Driver selection card variables
    private View driverSelectionCard;
    private TextView tvDriverName;
    private RatingBar ratingBar;
    private TextView tvRating;
    private TextView tvDistance;
    private TextView tvCompletedRides;
    private TextView tvDriverGender;
    private TextView tvVehicleInfo;
    private Button btnCancel;
    private Button btnRequestRide;
    private TextInputEditText etNumberOfPeople;
    private TextInputEditText etPricePerPerson;
    private Animation slideUpAnimation;
    private Animation slideDownAnimation;
    
    // Ride request sent card variables
    private View rideRequestSentCard;
    private TextView tvDriverNameSent;
    private TextView tvStatusMessage;
    private TextView tvCountdownTimer;
    private Button btnCancelRequest;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_commuter);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        btnMenu = findViewById(R.id.btnMenu);
        etSearch = findViewById(R.id.etSearch);
        searchLayout = findViewById(R.id.searchLayout);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        
        // Initialize driver selection card
        initializeDriverSelectionCard();
        
        // Initialize ride request sent card
        initializeRideRequestSentCard();

        // Initialize preferences
        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        currentUserId = prefs.getString("current_user_phone", "");

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();
        
        // Initialize timeout handler
        timeoutHandler = new android.os.Handler();
        
        // Initialize Places API
        initializePlacesAPI();
        
        // Initialize suggestions RecyclerView
        setupSuggestionsRecyclerView();
        
        // Initialize sample data (for testing)
        DataInitializer.initializeSampleData(this);
        
        // Create commuter profile in Firebase
        createCommuterProfile();

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

        // Set up search functionality
        setupSearchFunctionality();

        // Update header with user role
        updateNavigationHeader();

        // Check for active rides
        checkForActiveRides();

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
        
        // Set up custom info window adapter
        infoWindowAdapter = new CustomInfoWindowAdapter(this, this::onRequestRide);
        googleMap.setInfoWindowAdapter(infoWindowAdapter);
        
        // Set up map long click listener for destination selection
        googleMap.setOnMapLongClickListener(this);
        
        // Set up marker click listener for driver selection
        googleMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null && marker.getTag() instanceof Driver) {
                selectedDriver = (Driver) marker.getTag();
                // Show sliding card instead of info window
                showDriverSelectionCard(selectedDriver);
            }
            return true;
        });
        
        // Set up map click listener to hide suggestions and driver card
        googleMap.setOnMapClickListener(latLng -> {
            hideSuggestions();
            hideDriverSelectionCard();
        });
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
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            
                            // Add marker for current location (blue)
                            googleMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Your Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            
                            // Move camera to current location
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                            
                            // Update commuter location in Firebase
                            updateCommuterLocationInFirebase();
                            
                            // Update Places API session token with current location context
                            updatePlacesAPIContext();
                            
                            // Update info window adapter with current location
                            if (infoWindowAdapter != null) {
                                infoWindowAdapter.setCurrentLocation(currentLocation);
                            }
                        }
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("LocationError", "Failed to get current location: " + exception.getMessage());
                        Toast.makeText(this, "Could not get your current location. Search will work without location bias.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Update Places API context with current location for better search results
     */
    private void updatePlacesAPIContext() {
        if (currentLocation != null && placesClient != null) {
            // Create a new session token with current location context
            sessionToken = AutocompleteSessionToken.newInstance();
            Log.d("PlacesAPI", "Updated Places API context with current location: " + currentLocation.toString());
        }
    }
    
    private void updateCommuterLocationInFirebase() {
        if (currentLocation != null) {
            // Get current user's entity ID (commuterId) and update location
            firebaseService.getCurrentUserEntityId()
                .addOnSuccessListener(entityId -> {
                    if (entityId != null) {
                        firebaseService.updateCommuterLocation(entityId, currentLocation.latitude, currentLocation.longitude)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("LocationUpdate", "Commuter location updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("LocationUpdate", "Failed to update commuter location", e);
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LocationUpdate", "Failed to get current user entity ID", e);
                });
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
        // Remove active ride listener
        if (activeRideListener != null) {
            firebaseService.removeRideRequestsListener(activeRideListener);
        }
        // Stop timeout timer
        stopTimeoutTimer();
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
            Intent intent = new Intent(this, RideHistoryActivity.class);
            startActivity(intent);
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
        String currentUserPhone = prefs.getString("current_user_phone", "");
        String role = prefs.getString(currentUserPhone + "_role", "commuter");
        
        TextView tvUserRole = navigationView.getHeaderView(0).findViewById(R.id.tvUserRole);
        if (tvUserRole != null) {
            tvUserRole.setText(role.equals("driver") ? "Driver" : "Commuter");
        }
        
        // Update user name from database
        TextView tvUserName = navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        if (tvUserName != null) {
            // Get current user's entity ID from database
            firebaseService.getCurrentUserEntityId()
                .addOnSuccessListener(entityId -> {
                    if (entityId != null) {
                        if (role.equals("driver")) {
                            // Fetch driver name from database
                            firebaseService.getDriverDetails(entityId)
                                .addOnSuccessListener(driver -> {
                                    if (driver != null && driver.name != null) {
                                        tvUserName.setText(driver.name);
                                    } else {
                                        tvUserName.setText("Driver");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("NavigationHeader", "Failed to get driver details", e);
                                    tvUserName.setText("Driver");
                                });
                        } else {
                            // Fetch commuter name from database
                            firebaseService.getCommuterDetails(entityId)
                                .addOnSuccessListener(commuter -> {
                                    if (commuter != null && commuter.name != null) {
                                        tvUserName.setText(commuter.name);
                                    } else {
                                        tvUserName.setText("Commuter");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("NavigationHeader", "Failed to get commuter details", e);
                                    tvUserName.setText("Commuter");
                                });
                        }
                    } else {
                        tvUserName.setText("User");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NavigationHeader", "Failed to get current user entity ID", e);
                    tvUserName.setText("User");
                });
        }
    }

    private void logout() {
        // Clear SharedPreferences
        prefs.edit().clear().apply();
        
        // Sign out from Firebase Auth if using Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Navigate to login screen
        Intent intent = new Intent(HomeCommuterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Map long click listener for destination selection
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        setDestination(latLng);
    }

    private void setDestination(LatLng destination) {
        destinationLocation = destination;
        
        // Remove existing destination marker
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        
        // Add new destination marker (red)
        destinationMarker = googleMap.addMarker(new MarkerOptions()
                .position(destination)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        
        // Hide suggestions when destination is set
        hideSuggestions();
        
        // Move camera to show both current location and destination
        if (currentLocation != null) {
            // Calculate bounds to show both locations
            LatLng southwest = new LatLng(
                    Math.min(currentLocation.latitude, destination.latitude),
                    Math.min(currentLocation.longitude, destination.longitude)
            );
            LatLng northeast = new LatLng(
                    Math.max(currentLocation.latitude, destination.latitude),
                    Math.max(currentLocation.longitude, destination.longitude)
            );
            
            // Move camera to show both locations
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    new com.google.android.gms.maps.model.LatLngBounds(southwest, northeast), 100));
        }
        
        // Draw route from current location to destination
        if (currentLocation != null) {
            drawRoute(currentLocation, destination);
        }
        
        // Show nearby drivers
        showNearbyDrivers();
        
        Toast.makeText(this, "Destination set! Looking for nearby drivers...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Draw route from origin to destination using Google Maps Directions API
     */
    private void drawRoute(LatLng origin, LatLng destination) {
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
        String apiKey = "AIzaSyDc8_axTnQWPUiBWVgp1ifK0zV8Zy21Tqw";
        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;
        
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + originStr +
                "&destination=" + destinationStr +
                "&key=" + apiKey;
        
        Log.d("DirectionsAPI", "Requesting route: " + url);
        
        // Execute API call in background thread
        new Thread(() -> {
            try {
                String response = makeHttpRequest(url);
                runOnUiThread(() -> parseDirectionsResponse(response, origin, destination));
            } catch (Exception e) {
                Log.e("DirectionsAPI", "Error getting directions: " + e.getMessage());
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
                Log.e("DirectionsAPI", "Directions API error: " + status);
                drawStraightLineRoute(origin, destination);
                return;
            }
            
            JSONArray routes = jsonResponse.getJSONArray("routes");
            if (routes.length() == 0) {
                Log.e("DirectionsAPI", "No routes found");
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
            
            Log.d("DirectionsAPI", "Route found: " + distanceText + ", " + durationText);
            
        } catch (JSONException e) {
            Log.e("DirectionsAPI", "Error parsing directions response: " + e.getMessage());
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
        
        routePolyline = googleMap.addPolyline(new PolylineOptions()
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
        String routeInfo = "Route: " + distance + " • " + duration;
        Toast.makeText(this, routeInfo, Toast.LENGTH_LONG).show();
    }

    private void showNearbyDrivers() {
        // Clear existing driver markers
        for (Marker marker : driverMarkers) {
            marker.remove();
        }
        driverMarkers.clear();
        availableDrivers.clear();
        
        Log.d("DriverFilter", "Showing nearby drivers, excluding timed out drivers: " + timedOutDriverIds);
        
        // Listen for available drivers from Realtime Database
        firebaseService.listenAvailableDrivers(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                availableDrivers.clear();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String driverId = snapshot.getKey();
                    if (driverId != null) {
                        // Get driver details from Firestore
                        firebaseService.getDriverDetails(driverId)
                            .addOnSuccessListener(driver -> {
                                if (driver != null) {
                                    availableDrivers.add(driver);
                                    displayDriverMarkers();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DriverLoad", "Failed to load driver details for " + driverId, e);
                            });
                    }
                }
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(HomeCommuterActivity.this, "Error loading drivers: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDriverMarkers() {
        for (Driver driver : availableDrivers) {
            // Skip drivers that have timed out
            if (timedOutDriverIds.contains(driver.driverId)) {
                Log.d("DriverFilter", "Skipping timed out driver: " + driver.driverId);
                continue;
            }
            
            if (driver.currentLocation != null) {
                LatLng driverLocation = new LatLng(driver.currentLocation.lat, driver.currentLocation.lng);
                Marker driverMarker = googleMap.addMarker(new MarkerOptions()
                        .position(driverLocation)
                        .title(driver.name)
                        .snippet("Rating: " + driver.rating + " | Rides: " + driver.completedRides)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                
                // Set driver object as tag for click handling
                driverMarker.setTag(driver);
                driverMarkers.add(driverMarker);
            }
        }
    }

    /**
     * Remove a specific driver's marker from the map
     */
    private void removeDriverMarker(String driverId) {
        for (int i = driverMarkers.size() - 1; i >= 0; i--) {
            Marker marker = driverMarkers.get(i);
            if (marker.getTag() != null && marker.getTag() instanceof Driver) {
                Driver driver = (Driver) marker.getTag();
                if (driverId.equals(driver.driverId)) {
                    marker.remove();
                    driverMarkers.remove(i);
                    Log.d("DriverMarker", "Removed marker for driver: " + driverId);
                    break;
                }
            }
        }
    }

    private void showDriverInfoCard(Driver driver) {
        // This method is now handled by the custom info window adapter
        // The info window will be shown when marker is clicked
    }
    
    /**
     * Handle Request Ride button click from custom info window
     */
    private void onRequestRide(Driver driver) {
        // Check if commuter already has an active ride
        if (hasActiveRide) {
            String statusMessage = "You already have an active ride request";
            if (activeRideRequest != null) {
                switch (activeRideRequest.status) {
                    case "pending":
                        statusMessage = "You have a pending ride request. Please wait for driver response or cancel it first.";
                        break;
                    case "accepted":
                        statusMessage = "Your ride has been accepted and is in progress. Please complete this ride before requesting another.";
                        break;
                    case "in_progress":
                        statusMessage = "You have a ride in progress. Please complete this ride before requesting another.";
                        break;
                }
            }
            Toast.makeText(this, statusMessage, Toast.LENGTH_LONG).show();
            return;
        }
        
        // Clear timed out drivers list when starting a new request
        timedOutDriverIds.clear();
        Log.d("DriverFilter", "Cleared timed out drivers list for new request");
        
        // Show immediate feedback that button was pressed
        Toast.makeText(this, "Button pressed! Processing request...", Toast.LENGTH_SHORT).show();
        
        // Add debugging logs
        Log.d("RideRequest", "onRequestRide called for driver: " + driver.name);
        Log.d("RideRequest", "currentLocation: " + (currentLocation != null ? currentLocation.toString() : "null"));
        Log.d("RideRequest", "destinationLocation: " + (destinationLocation != null ? destinationLocation.toString() : "null"));
        
        if (currentLocation == null || destinationLocation == null) {
            String message = "Please set your destination first";
            if (currentLocation == null) message += " (current location missing)";
            if (destinationLocation == null) message += " (destination missing)";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }
        
        // Get and validate input values
        int numberOfPeople = 1;
        double pricePerPerson = 5.0;
        
        try {
            String peopleText = etNumberOfPeople.getText().toString().trim();
            if (!peopleText.isEmpty()) {
                numberOfPeople = Integer.parseInt(peopleText);
                if (numberOfPeople < 1 || numberOfPeople > 8) {
                    Toast.makeText(this, "Number of people must be between 1 and 8", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number of people", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String priceText = etPricePerPerson.getText().toString().trim();
            if (!priceText.isEmpty()) {
                pricePerPerson = Double.parseDouble(priceText);
                if (pricePerPerson < 0.01) {
                    Toast.makeText(this, "Price per person must be greater than $0.00", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price per person", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show immediate feedback
        Toast.makeText(this, "Sending request to " + driver.name + "...", Toast.LENGTH_SHORT).show();
        
        // Hide the driver selection card
        hideDriverSelectionCard();
        
        // Send ride request with people and price data
        requestRide(driver, numberOfPeople, pricePerPerson);
    }

    private void requestRide(Driver driver, int numberOfPeople, double pricePerPerson) {
        Log.d("RideRequest", "requestRide called for driver: " + driver.name + " with " + numberOfPeople + " people at $" + pricePerPerson + " each");
        
        if (currentLocation == null || destinationLocation == null) {
            Toast.makeText(this, "Please set your destination first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get current user's entity ID (commuterId)
        firebaseService.getCurrentUserEntityId()
            .addOnSuccessListener(commuterId -> {
                if (commuterId == null) {
                    Toast.makeText(HomeCommuterActivity.this, "User not found. Please login again.", Toast.LENGTH_LONG).show();
                    return;
                }
                
                Log.d("RideRequest", "Creating ride request for commuter: " + commuterId);
                
                // Create location data objects
                RideRequest.LocationData pickupLocation = new RideRequest.LocationData(
                    currentLocation.latitude, 
                    currentLocation.longitude, 
                    "Current Location"
                );
                
                RideRequest.LocationData destination = new RideRequest.LocationData(
                    destinationLocation.latitude, 
                    destinationLocation.longitude, 
                    "Destination"
                );
                
                // Create ride request in both Firestore and Realtime Database
                firebaseService.createRideRequest(
                    commuterId,
                    driver.driverId,
                    pickupLocation,
                    destination,
                    numberOfPeople,
                    pricePerPerson
                ).addOnSuccessListener(aVoid -> {
                    Log.d("RideRequest", "Ride request created successfully");
                    
                    // Create a temporary active ride request for the timer FIRST
                    activeRideRequest = new RideRequest();
                    activeRideRequest.rideId = "ride_" + System.currentTimeMillis();
                    activeRideRequest.status = "pending";
                    activeRideRequest.commuterId = commuterId;
                    activeRideRequest.driverId = driver.driverId;
                    
                    // Track the current requested driver
                    currentRequestedDriverId = driver.driverId;
                    
                    Log.d("RideRequest", "Active ride request created: " + activeRideRequest.rideId + ", status: " + activeRideRequest.status);
                    
                    // Set active ride state
                    hasActiveRide = true;
                    
                    // Start timeout timer
                    startTimeoutTimer();
                    
                    // Show UI
                    showRideRequestSentMessage(driver.name);
                    showWaitingScreen(activeRideRequest.rideId);
                }).addOnFailureListener(e -> {
                    Log.e("RideRequest", "Failed to create ride request", e);
                    Toast.makeText(HomeCommuterActivity.this, "Failed to send ride request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            })
            .addOnFailureListener(e -> {
                Log.e("RideRequest", "Failed to get current user entity ID", e);
                Toast.makeText(HomeCommuterActivity.this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
            });
    }

    /**
     * Show a prominent message that ride request has been sent
     */
    private void showRideRequestSentMessage(String driverName) {
        showRideRequestSentCard(driverName);
    }

    private void showWaitingScreen(String rideId) {
        // Listen for ride request status updates from Realtime Database
        firebaseService.listenRideRequest(rideId, new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null) {
                        switch (status) {
                            case "accepted":
                                Toast.makeText(HomeCommuterActivity.this, "Ride accepted! Driver is on the way.", Toast.LENGTH_LONG).show();
                                // Update active ride state
                                hasActiveRide = true;
                                // Here you would navigate to ride tracking screen
                                break;
                            case "declined":
                                Toast.makeText(HomeCommuterActivity.this, "Ride declined. Looking for another driver...", Toast.LENGTH_LONG).show();
                                // Reset active ride state
                                hasActiveRide = false;
                                activeRideRequest = null;
                                // Remove declined driver from available list and show others
                                showNearbyDrivers();
                                break;
                            case "completed":
                                Toast.makeText(HomeCommuterActivity.this, "Ride completed successfully!", Toast.LENGTH_LONG).show();
                                // Reset active ride state
                                hasActiveRide = false;
                                activeRideRequest = null;
                                break;
                            case "cancelled":
                                Toast.makeText(HomeCommuterActivity.this, "Ride cancelled.", Toast.LENGTH_LONG).show();
                                // Reset active ride state
                                hasActiveRide = false;
                                activeRideRequest = null;
                                // Stop timeout timer
                                stopTimeoutTimer();
                                break;
                            case "timeout":
                                Toast.makeText(HomeCommuterActivity.this, "Ride request timed out. No driver accepted within 2 minutes.", Toast.LENGTH_LONG).show();
                                // Reset active ride state
                                hasActiveRide = false;
                                activeRideRequest = null;
                                // Stop timeout timer
                                stopTimeoutTimer();
                                // Show nearby drivers again
                                showNearbyDrivers();
                                break;
                        }
                    }
                } else {
                    // Ride request no longer exists (completed/cancelled)
                    hasActiveRide = false;
                    activeRideRequest = null;
                }
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(HomeCommuterActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initialize Google Places API
     */
    private void initializePlacesAPI() {
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);
        sessionToken = AutocompleteSessionToken.newInstance();
    }

    /**
     * Set up comprehensive search functionality with Places Autocomplete
     */
    private void setupSearchFunctionality() {
        // Set up text change listener for autocomplete suggestions
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                // Don't show suggestions if destination is already selected
                if (destinationLocation != null) {
                    hideSuggestions();
                    return;
                }
                
                if (query.length() >= 2) {
                    getAutocompletePredictions(query);
                } else {
                    hideSuggestions();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Set up search field click listener to show autocomplete
        etSearch.setOnClickListener(v -> {
            // Don't show suggestions if destination is already selected
            if (destinationLocation != null) {
                hideSuggestions();
                return;
            }
            
            String currentText = etSearch.getText().toString().trim();
            if (currentText.length() >= 2) {
                getAutocompletePredictions(currentText);
            }
        });

        // Set up search on Enter key press
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String searchQuery = etSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                hideSuggestions();
                performLocationSearch(searchQuery);
                return true;
            }
            return false;
        });
    }

    /**
     * Get autocomplete predictions from Google Places API
     */
    private void getAutocompletePredictions(String query) {
        if (placesClient == null || query.length() < 2) {
            return;
        }

        // Create bounds based on current location if available
        RectangularBounds bounds = null;
        if (currentLocation != null) {
            // Create a 50km radius around current location
            double lat = currentLocation.latitude;
            double lng = currentLocation.longitude;
            double offset = 0.5; // Approximately 50km
            
            bounds = RectangularBounds.newInstance(
                new LatLng(lat - offset, lng - offset),
                new LatLng(lat + offset, lng + offset)
            );
        }

        // Build the autocomplete request
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(sessionToken)
                .setLocationBias(bounds)
                .build();

        // Execute the request
        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    if (response.getAutocompletePredictions() != null && 
                        !response.getAutocompletePredictions().isEmpty()) {
                        showAutocompleteSuggestions(response.getAutocompletePredictions());
                    }
                })
                .addOnFailureListener(exception -> {
                    handleNetworkError("Autocomplete predictions", exception);
                });
    }

    /**
     * Set up the suggestions RecyclerView
     */
    private void setupSuggestionsRecyclerView() {
        suggestionAdapter = new PlaceSuggestionAdapter(new PlaceSuggestionAdapter.OnSuggestionClickListener() {
            @Override
            public void onSuggestionClick(AutocompletePrediction prediction) {
                // Hide suggestions
                hideSuggestions();
                
                // Hide keyboard
                hideKeyboard();
                
                // Update search field with selected suggestion
                etSearch.setText(prediction.getPrimaryText(null).toString());
                
                // Fetch place details and move map
                fetchPlaceDetails(prediction.getPlaceId());
            }
        });
        
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionAdapter);
    }

    /**
     * Show autocomplete suggestions in the dropdown
     */
    private void showAutocompleteSuggestions(List<AutocompletePrediction> predictions) {
        if (predictions.isEmpty()) {
            hideSuggestions();
            return;
        }

        // Update adapter with new suggestions
        suggestionAdapter.updateSuggestions(predictions);
        
        // Show the suggestions RecyclerView
        rvSuggestions.setVisibility(View.VISIBLE);
        
        Log.d("PlacesAPI", "Showing " + predictions.size() + " suggestions");
    }

    /**
     * Hide the suggestions dropdown
     */
    private void hideSuggestions() {
        rvSuggestions.setVisibility(View.GONE);
        suggestionAdapter.clearSuggestions();
    }
    
    /**
     * Hide the soft keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }
    
    /**
     * Clear the current destination and reset search functionality
     */
    private void clearDestination() {
        destinationLocation = null;
        
        // Remove destination marker
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
        
        // Remove route polyline
        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }
        
        // Clear driver markers
        for (Marker marker : driverMarkers) {
            marker.remove();
        }
        driverMarkers.clear();
        availableDrivers.clear();
        
        // Hide driver info card
        if (driverInfoCard != null) {
            driverInfoCard.setVisibility(View.GONE);
        }
        
        // Clear search field
        etSearch.setText("");
        
        // Hide suggestions
        hideSuggestions();
        
        Toast.makeText(this, "Destination cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Perform location search and move map camera to the result
     */
    private void performLocationSearch(String query) {
        // Validate input
        if (!isValidSearchQuery(query)) {
            return;
        }

        // Check network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (placesClient == null) {
            Toast.makeText(this, "Places API not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state in search field
        etSearch.setEnabled(false);

        // For this implementation, we'll use the first prediction
        // In a full implementation, you'd let the user select from a dropdown
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(sessionToken)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    if (response.getAutocompletePredictions() != null && 
                        !response.getAutocompletePredictions().isEmpty()) {
                        
                        // Get the first prediction
                        AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                        String placeId = prediction.getPlaceId();
                        
                        // Fetch the full place details
                        fetchPlaceDetails(placeId);
                    } else {
                        Toast.makeText(this, "No results found for: " + query, Toast.LENGTH_SHORT).show();
                        resetSearchField();
                    }
                })
                .addOnFailureListener(exception -> {
                    handleNetworkError("Location search", exception);
                    resetSearchField();
                });
    }

    /**
     * Fetch detailed place information and move map camera
     */
    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = java.util.Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    LatLng placeLatLng = place.getLatLng();
                    
                    if (placeLatLng != null) {
                        // Move camera to the searched location
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15f));
                        
                        // Set this as the destination
                        setDestination(placeLatLng);
                        
                        // Update search field with the place name
                        etSearch.setText(place.getName());
                    } else {
                        Toast.makeText(this, "Could not get location details", Toast.LENGTH_SHORT).show();
                    }
                    resetSearchField();
                })
                .addOnFailureListener(exception -> {
                    handleNetworkError("Place details fetch", exception);
                    resetSearchField();
                });
    }

    /**
     * Reset search field to normal state
     */
    private void resetSearchField() {
        etSearch.setEnabled(true);
    }

    /**
     * Check if device has network connectivity
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Handle network-related errors gracefully
     */
    private void handleNetworkError(String operation, Exception exception) {
        Log.e("NetworkError", operation + " failed: " + exception.getMessage());
        
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
        } else {
            String errorMessage = "Search failed. Please try again.";
            if (exception.getMessage() != null) {
                if (exception.getMessage().contains("API key")) {
                    errorMessage = "API configuration error. Please contact support.";
                } else if (exception.getMessage().contains("quota")) {
                    errorMessage = "Search service temporarily unavailable. Please try again later.";
                }
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate search query input
     */
    private boolean isValidSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        // Check for minimum length
        if (query.trim().length() < 2) {
            Toast.makeText(this, "Please enter at least 2 characters to search", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Check for maximum length to prevent abuse
        if (query.length() > 100) {
            Toast.makeText(this, "Search query too long. Please shorten your search.", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
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
    
    private void createCommuterProfile() {
        // This method is no longer needed as commuter profiles are created during registration
        // The commuter profile should already exist in Firestore from the registration process
        Log.d("HomeCommuterActivity", "Commuter profile should already exist from registration");
    }
    
    /**
     * Initialize the driver selection card and its components
     */
    private void initializeDriverSelectionCard() {
        driverSelectionCard = findViewById(R.id.driverSelectionCard);
        
        // Initialize card views
        tvDriverName = driverSelectionCard.findViewById(R.id.tvDriverName);
        ratingBar = driverSelectionCard.findViewById(R.id.ratingBar);
        tvRating = driverSelectionCard.findViewById(R.id.tvRating);
        tvDistance = driverSelectionCard.findViewById(R.id.tvDistance);
        tvCompletedRides = driverSelectionCard.findViewById(R.id.tvCompletedRides);
        tvDriverGender = driverSelectionCard.findViewById(R.id.tvDriverGender);
        tvVehicleInfo = driverSelectionCard.findViewById(R.id.tvVehicleInfo);
        btnCancel = driverSelectionCard.findViewById(R.id.btnCancel);
        btnRequestRide = driverSelectionCard.findViewById(R.id.btnRequestRide);
        etNumberOfPeople = driverSelectionCard.findViewById(R.id.etNumberOfPeople);
        etPricePerPerson = driverSelectionCard.findViewById(R.id.etPricePerPerson);
        
        // Initialize animations
        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        
        // Set up button listeners
        btnCancel.setOnClickListener(v -> hideDriverSelectionCard());
        btnRequestRide.setOnClickListener(v -> {
            if (selectedDriver != null) {
                onRequestRide(selectedDriver);
            }
        });
        
        // Set up slide down animation listener
        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                driverSelectionCard.setVisibility(View.GONE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        // Initially hide the card
        driverSelectionCard.setVisibility(View.GONE);
    }
    
    /**
     * Show the driver selection card with smooth slide up animation
     */
    private void showDriverSelectionCard(Driver driver) {
        if (driver == null) return;
        
        // Populate card with driver information
        tvDriverName.setText(driver.name);
        ratingBar.setRating((float) driver.rating);
        tvRating.setText(String.format("%.1f", driver.rating));
        tvCompletedRides.setText(String.valueOf(driver.completedRides));
        tvDriverGender.setText(driver.gender != null ? driver.gender : "N/A");
        
        // Fetch vehicle information from database
        firebaseService.getVehicleDetails(driver.driverId)
            .addOnSuccessListener(vehicle -> {
                if (vehicle != null && vehicle.vehicleType != null) {
                    tvVehicleInfo.setText(vehicle.vehicleType);
                } else {
                    tvVehicleInfo.setText("N/A");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("DriverCard", "Failed to get vehicle details", e);
                tvVehicleInfo.setText("N/A");
            });
        
        // Calculate and display distance
        if (driver.currentLocation != null && currentLocation != null) {
            double distance = calculateDistance(currentLocation, 
                new LatLng(driver.currentLocation.lat, driver.currentLocation.lng));
            tvDistance.setText(String.format("%.1f km", distance));
        } else {
            tvDistance.setText("N/A");
        }
        
        // Reset input fields to default values
        etNumberOfPeople.setText("1");
        etPricePerPerson.setText("5.00");
        
        // Update button state based on active ride status
        if (hasActiveRide) {
            btnRequestRide.setEnabled(false);
            btnRequestRide.setText("Active Ride in Progress");
            btnRequestRide.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            btnRequestRide.setEnabled(true);
            btnRequestRide.setText("Request Ride");
            btnRequestRide.setBackgroundColor(getResources().getColor(R.color.purple_500));
        }
        
        // Show card with animation
        driverSelectionCard.setVisibility(View.VISIBLE);
        driverSelectionCard.startAnimation(slideUpAnimation);
    }
    
    /**
     * Hide the driver selection card with smooth slide down animation
     */
    private void hideDriverSelectionCard() {
        if (driverSelectionCard.getVisibility() == View.VISIBLE) {
            driverSelectionCard.startAnimation(slideDownAnimation);
        }
    }
    
    @Override
    public void onBackPressed() {
        // If driver selection card is visible, hide it instead of closing activity
        if (driverSelectionCard != null && driverSelectionCard.getVisibility() == View.VISIBLE) {
            hideDriverSelectionCard();
        } else if (rideRequestSentCard != null && rideRequestSentCard.getVisibility() == View.VISIBLE) {
            hideRideRequestSentCard();
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Initialize the ride request sent card and its components
     */
    private void initializeRideRequestSentCard() {
        rideRequestSentCard = findViewById(R.id.rideRequestSentCard);
        
        // Initialize card views
        tvDriverNameSent = rideRequestSentCard.findViewById(R.id.tvDriverNameSent);
        tvStatusMessage = rideRequestSentCard.findViewById(R.id.tvStatusMessage);
        tvCountdownTimer = rideRequestSentCard.findViewById(R.id.tvCountdownTimer);
        btnCancelRequest = rideRequestSentCard.findViewById(R.id.btnCancelRequest);
        
        // Initialize animations
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        
        // Set up button listener
        btnCancelRequest.setOnClickListener(v -> {
            String buttonText = btnCancelRequest.getText().toString();
            if ("OK".equals(buttonText)) {
                // OK button clicked - hide card and show drivers
                hideRideRequestSentCard();
                showNearbyDrivers();
            } else {
                // Cancel button clicked - cancel the ride request
                cancelActiveRideRequest();
                hideRideRequestSentCard();
            }
        });
        
        // Set up fade out animation listener
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                rideRequestSentCard.setVisibility(View.GONE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        // Initially hide the card
        rideRequestSentCard.setVisibility(View.GONE);
    }
    
    /**
     * Show the ride request sent card with fade in animation
     */
    private void showRideRequestSentCard(String driverName) {
        Log.d("RideRequestCard", "Showing ride request sent card for driver: " + driverName);
        if (driverName != null) {
            tvDriverNameSent.setText(driverName);
        }
        
        // Set initial status message
        if (tvStatusMessage != null) {
            tvStatusMessage.setText("Please wait for the driver to respond...");
            tvStatusMessage.setTextColor(getResources().getColor(R.color.gray_600));
        }
        
        // Reset button to Cancel Request
        if (btnCancelRequest != null) {
            btnCancelRequest.setText("Cancel Request");
            btnCancelRequest.setBackgroundColor(getResources().getColor(R.color.purple_500));
        }
        
        // Initialize countdown timer display
        if (tvCountdownTimer != null) {
            tvCountdownTimer.setText("2:00");
            tvCountdownTimer.setTextColor(getResources().getColor(R.color.purple_500));
            Log.d("RideRequestCard", "Countdown timer initialized");
        } else {
            Log.e("RideRequestCard", "Countdown timer TextView is null!");
        }
        
        // Show card with animation
        rideRequestSentCard.setVisibility(View.VISIBLE);
        rideRequestSentCard.startAnimation(fadeInAnimation);
        Log.d("RideRequestCard", "Card made visible and animation started");
    }
    
    /**
     * Hide the ride request sent card with fade out animation
     */
    private void hideRideRequestSentCard() {
        if (rideRequestSentCard.getVisibility() == View.VISIBLE) {
            rideRequestSentCard.startAnimation(fadeOutAnimation);
        }
    }

    /**
     * Check if commuter has any active ride requests
     */
    private void checkForActiveRides() {
        firebaseService.getCurrentUserEntityId()
            .addOnSuccessListener(commuterId -> {
                if (commuterId != null) {
                    // Check if commuter has active rides
                    firebaseService.hasActiveRideRequest(commuterId)
                        .addOnSuccessListener(hasActive -> {
                            if (hasActive) {
                                // Get the active ride details
                                firebaseService.getActiveRideRequest(commuterId)
                                    .addOnSuccessListener(activeRide -> {
                                        if (activeRide != null) {
                                            activeRideRequest = activeRide;
                                            hasActiveRide = true;
                                            
                                            // Show appropriate message based on status
                                            String message = "You have an active ride request";
                                            switch (activeRide.status) {
                                                case "pending":
                                                    message = "You have a pending ride request. Please wait for driver response.";
                                                    break;
                                                case "accepted":
                                                    message = "Your ride has been accepted and is in progress.";
                                                    break;
                                                case "in_progress":
                                                    message = "You have a ride in progress.";
                                                    break;
                                            }
                                            Toast.makeText(HomeCommuterActivity.this, message, Toast.LENGTH_LONG).show();
                                            
                                            // Set up listener for active ride updates
                                            setupActiveRideListener(commuterId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ActiveRide", "Failed to get active ride details", e);
                                    });
                            } else {
                                hasActiveRide = false;
                                activeRideRequest = null;
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ActiveRide", "Failed to check for active rides", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("ActiveRide", "Failed to get current user entity ID", e);
            });
    }

    /**
     * Set up listener for active ride updates
     */
    private void setupActiveRideListener(String commuterId) {
        activeRideListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Parse the active ride from snapshot
                    RideRequest ride = parseRideRequestFromSnapshot(dataSnapshot);
                    if (ride != null) {
                        activeRideRequest = ride;
                        hasActiveRide = true;
                        
                        // Update UI based on status
                        updateUIForActiveRide(ride);
                    }
                } else {
                    // No active ride
                    hasActiveRide = false;
                    activeRideRequest = null;
                }
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e("ActiveRide", "Error listening to active ride: " + databaseError.getMessage());
            }
        };
        
        firebaseService.listenActiveRideRequest(commuterId, activeRideListener);
    }

    /**
     * Parse RideRequest from Realtime Database snapshot
     */
    private RideRequest parseRideRequestFromSnapshot(com.google.firebase.database.DataSnapshot snapshot) {
        try {
            RideRequest ride = new RideRequest();
            ride.rideId = snapshot.child("rideId").getValue(String.class);
            ride.commuterId = snapshot.child("commuterId").getValue(String.class);
            ride.driverId = snapshot.child("driverId").getValue(String.class);
            ride.status = snapshot.child("status").getValue(String.class);
            ride.people = snapshot.child("people").getValue(Integer.class);
            ride.priceEach = snapshot.child("priceEach").getValue(Double.class);
            ride.timestamp = snapshot.child("timestamp").getValue(Long.class);
            
            // Parse location data
            com.google.firebase.database.DataSnapshot pickupSnapshot = snapshot.child("pickupLocation");
            if (pickupSnapshot.exists()) {
                ride.pickupLocation = new RideRequest.LocationData();
                ride.pickupLocation.lat = pickupSnapshot.child("lat").getValue(Double.class);
                ride.pickupLocation.lng = pickupSnapshot.child("lng").getValue(Double.class);
                ride.pickupLocation.address = pickupSnapshot.child("address").getValue(String.class);
            }
            
            com.google.firebase.database.DataSnapshot destSnapshot = snapshot.child("destination");
            if (destSnapshot.exists()) {
                ride.destination = new RideRequest.LocationData();
                ride.destination.lat = destSnapshot.child("lat").getValue(Double.class);
                ride.destination.lng = destSnapshot.child("lng").getValue(Double.class);
                ride.destination.address = destSnapshot.child("address").getValue(String.class);
            }
            
            return ride;
        } catch (Exception e) {
            Log.e("ParseRide", "Error parsing ride request from snapshot", e);
            return null;
        }
    }

    /**
     * Update UI based on active ride status
     */
    private void updateUIForActiveRide(RideRequest ride) {
        if (ride != null) {
            switch (ride.status) {
                case "pending":
                    // Show pending message and allow cancellation
                    Toast.makeText(this, "Ride request pending. You can cancel it if needed.", Toast.LENGTH_SHORT).show();
                    break;
                case "accepted":
                    // Show accepted message and disable new requests
                    Toast.makeText(this, "Ride accepted! Driver is on the way.", Toast.LENGTH_LONG).show();
                    break;
                case "in_progress":
                    // Show in progress message and disable new requests
                    Toast.makeText(this, "Ride in progress. Please complete this ride first.", Toast.LENGTH_LONG).show();
                    break;
                case "completed":
                    Toast.makeText(this, "Ride completed successfully!", Toast.LENGTH_SHORT).show();
                    // Reset active ride state
                    hasActiveRide = false;
                    activeRideRequest = null;
                    break;
                case "declined":
                    Toast.makeText(this, "Ride declined by driver. You can now request another ride.", Toast.LENGTH_LONG).show();
                    // Reset active ride state
                    hasActiveRide = false;
                    activeRideRequest = null;
                    break;
                case "cancelled":
                    Toast.makeText(this, "Ride cancelled successfully.", Toast.LENGTH_SHORT).show();
                    // Reset active ride state
                    hasActiveRide = false;
                    activeRideRequest = null;
                    stopTimeoutTimer();
                    break;
                case "timeout":
                    Toast.makeText(this, "Ride request timed out. You can now request another ride.", Toast.LENGTH_LONG).show();
                    // Reset active ride state
                    hasActiveRide = false;
                    activeRideRequest = null;
                    stopTimeoutTimer();
                    break;
            }
        }
    }

    /**
     * Cancel the active ride request
     */
    private void cancelActiveRideRequest() {
        if (activeRideRequest != null && activeRideRequest.rideId != null) {
            // Only allow cancellation if ride is still pending
            if ("pending".equals(activeRideRequest.status)) {
                firebaseService.cancelRideRequest(activeRideRequest.rideId)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Ride request cancelled successfully", Toast.LENGTH_SHORT).show();
                        hasActiveRide = false;
                        activeRideRequest = null;
                        stopTimeoutTimer();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to cancel ride request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            } else {
                Toast.makeText(this, "Cannot cancel ride request. Ride status: " + activeRideRequest.status, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No active ride request to cancel", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start the timeout timer for ride requests
     */
    private void startTimeoutTimer() {
        rideRequestStartTime = System.currentTimeMillis();
        Log.d("Timeout", "Starting timeout timer at: " + rideRequestStartTime);
        
        // Remove any existing timeout runnable
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (countdownRunnable != null) {
            timeoutHandler.removeCallbacks(countdownRunnable);
        }
        
        // Start countdown display
        startCountdownDisplay();
        
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                // Check if ride is still pending after 2 minutes
                if (activeRideRequest != null && "pending".equals(activeRideRequest.status)) {
                    Log.d("Timeout", "Ride request timed out after 2 minutes (backup timeout)");
                    triggerRideTimeout();
                }
            }
        };
        
        // Schedule timeout after 2 minutes
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
        Log.d("Timeout", "Timeout timer started for 2 minutes");
    }

    /**
     * Start countdown display
     */
    private void startCountdownDisplay() {
        Log.d("Countdown", "Starting countdown display");
        
        // Reset countdown seconds
        countdownSeconds = 120; // 2 minutes
        
        // Remove any existing countdown runnable first
        if (countdownRunnable != null) {
            timeoutHandler.removeCallbacks(countdownRunnable);
        }
        
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Countdown", "Countdown runnable executing - countdownSeconds: " + countdownSeconds + 
                      ", activeRideRequest: " + (activeRideRequest != null) + 
                      ", status: " + (activeRideRequest != null ? activeRideRequest.status : "null") + 
                      ", tvCountdownTimer: " + (tvCountdownTimer != null));
                
                Log.d("Countdown", "Checking conditions - activeRideRequest != null: " + (activeRideRequest != null) + 
                      ", status equals pending: " + (activeRideRequest != null && "pending".equals(activeRideRequest.status)) + 
                      ", tvCountdownTimer != null: " + (tvCountdownTimer != null) + 
                      ", countdownSeconds > 0: " + (countdownSeconds > 0));
                
                // Fallback: if activeRideRequest is null but we have an active ride, create it
                if (activeRideRequest == null && hasActiveRide) {
                    Log.d("Countdown", "Creating fallback activeRideRequest");
                    activeRideRequest = new RideRequest();
                    activeRideRequest.status = "pending";
                }
                
                if (tvCountdownTimer != null && countdownSeconds > 0) {
                    int minutes = countdownSeconds / 60;
                    int seconds = countdownSeconds % 60;
                    
                    String timeText = String.format("%d:%02d", minutes, seconds);
                    tvCountdownTimer.setText(timeText);
                    
                    Log.d("Countdown", "Updated timer text: " + timeText + " (countdownSeconds: " + countdownSeconds + ")");
                    
                    // Change color as time runs out
                    if (countdownSeconds <= 30) { // Less than 30 seconds
                        tvCountdownTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else if (countdownSeconds <= 60) { // Less than 1 minute
                        tvCountdownTimer.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    } else {
                        tvCountdownTimer.setTextColor(getResources().getColor(R.color.purple_500));
                    }
                    
                    // Decrement counter
                    countdownSeconds--;
                    
                    // Schedule next update in 1 second
                    timeoutHandler.postDelayed(countdownRunnable, 1000);
                    Log.d("Countdown", "Scheduled next update in 1 second, new countdownSeconds: " + countdownSeconds);
                } else {
                    if (countdownSeconds <= 0) {
                        tvCountdownTimer.setText("0:00");
                        tvCountdownTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        Log.d("Countdown", "Timer reached 0:00 - triggering timeout");
                        
                        // Trigger timeout when countdown reaches 0
                        triggerRideTimeout();
                    } else {
                        Log.d("Countdown", "Countdown stopped - conditions not met, countdownSeconds: " + countdownSeconds);
                    }
                }
            }
        };
        
        // Start countdown immediately
        timeoutHandler.post(countdownRunnable);
        Log.d("Countdown", "Countdown runnable posted to handler");
    }

    /**
     * Stop the timeout timer
     */
    private void stopTimeoutTimer() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        if (countdownRunnable != null) {
            timeoutHandler.removeCallbacks(countdownRunnable);
            countdownRunnable = null;
        }
        // Reset countdown seconds
        countdownSeconds = 120;
        Log.d("Timeout", "Timeout timer and countdown stopped");
    }

    /**
     * Trigger ride timeout when countdown reaches 0
     */
    private void triggerRideTimeout() {
        Log.d("Timeout", "Triggering ride timeout");
        
        if (activeRideRequest != null && activeRideRequest.rideId != null) {
            // Timeout the ride request
            firebaseService.timeoutRideRequest(activeRideRequest.rideId)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Timeout", "Ride request timed out successfully");
                    
                    // Show timeout message on the card
                    if (tvStatusMessage != null) {
                        tvStatusMessage.setText("Driver did not respond. Choose another driver below.");
                        tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                    
                    // Change cancel button to OK
                    if (btnCancelRequest != null) {
                        btnCancelRequest.setText("OK");
                        btnCancelRequest.setBackgroundColor(getResources().getColor(R.color.purple_500));
                    }
                    
                    // Show toast message
                    Toast.makeText(HomeCommuterActivity.this, "Driver did not respond. Please choose another driver.", Toast.LENGTH_LONG).show();
                    
                    // Add driver to timed out list and remove their marker
                    if (currentRequestedDriverId != null) {
                        timedOutDriverIds.add(currentRequestedDriverId);
                        removeDriverMarker(currentRequestedDriverId);
                        Log.d("Timeout", "Removed marker for timed out driver: " + currentRequestedDriverId);
                    }
                    
                    // Reset active ride state
                    hasActiveRide = false;
                    activeRideRequest = null;
                    currentRequestedDriverId = null;
                    
                    // Stop the timeout timer
                    stopTimeoutTimer();
                })
                .addOnFailureListener(e -> {
                    Log.e("Timeout", "Failed to timeout ride request", e);
                    Toast.makeText(HomeCommuterActivity.this, "Failed to timeout ride request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        } else {
            Log.w("Timeout", "No active ride request to timeout");
            // Add driver to timed out list and remove their marker
            if (currentRequestedDriverId != null) {
                timedOutDriverIds.add(currentRequestedDriverId);
                removeDriverMarker(currentRequestedDriverId);
                Log.d("Timeout", "Removed marker for timed out driver (fallback): " + currentRequestedDriverId);
            }
            
            // Still reset the state and show drivers
            hasActiveRide = false;
            activeRideRequest = null;
            currentRequestedDriverId = null;
            
            // Show timeout message on the card
            if (tvStatusMessage != null) {
                tvStatusMessage.setText("Driver did not respond. Choose another driver below.");
                tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            
            // Change cancel button to OK
            if (btnCancelRequest != null) {
                btnCancelRequest.setText("OK");
                btnCancelRequest.setBackgroundColor(getResources().getColor(R.color.purple_500));
            }
            
            Toast.makeText(HomeCommuterActivity.this, "Driver did not respond. Please choose another driver.", Toast.LENGTH_LONG).show();
            stopTimeoutTimer();
        }
    }

}
