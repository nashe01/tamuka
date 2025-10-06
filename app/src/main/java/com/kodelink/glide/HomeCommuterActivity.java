package com.kodelink.glide;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeCommuterActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMapLongClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MaterialButton btnMenu;
    private TextInputEditText etSearch;
    private TextInputLayout searchLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SharedPreferences prefs;
    
    // Places API variables
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    
    // Suggestions UI
    private RecyclerView rvSuggestions;
    private PlaceSuggestionAdapter suggestionAdapter;
    
    // Ride request workflow variables
    private LatLng currentLocation;
    private LatLng destinationLocation;
    private Marker destinationMarker;
    private List<Driver> availableDrivers = new ArrayList<>();
    private List<Marker> driverMarkers = new ArrayList<>();
    private FirebaseService firebaseService;
    private String currentUserId;
    private MaterialCardView driverInfoCard;
    private Driver selectedDriver;
    private CustomInfoWindowAdapter infoWindowAdapter;

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

        // Initialize preferences
        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        currentUserId = prefs.getString("current_user_phone", "");

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();
        
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
                // Show custom info window instead of automatically requesting ride
                marker.showInfoWindow();
            }
            return true;
        });
        
        // Set up map click listener to hide suggestions
        googleMap.setOnMapClickListener(latLng -> {
            hideSuggestions();
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
            Commuter.LocationData locationData = new Commuter.LocationData(
                    currentLocation.latitude, 
                    currentLocation.longitude, 
                    "Current Location"
            );
            
            firebaseService.updateCommuterLocation(currentUserId, locationData, new FirebaseService.DatabaseCallback() {
                @Override
                public void onSuccess(String message) {
                    // Location updated successfully
                }
                
                @Override
                public void onError(String error) {
                    // Handle error if needed
                }
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_notifications) {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
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
        
        // Show nearby drivers
        showNearbyDrivers();
        
        Toast.makeText(this, "Destination set! Looking for nearby drivers...", Toast.LENGTH_SHORT).show();
    }

    private void showNearbyDrivers() {
        // Clear existing driver markers
        for (Marker marker : driverMarkers) {
            marker.remove();
        }
        driverMarkers.clear();
        availableDrivers.clear();
        
        // Get available drivers from Firebase
        firebaseService.getAvailableDrivers(new FirebaseService.DriversListener() {
            @Override
            public void onDriversReceived(List<Driver> drivers) {
                availableDrivers = drivers;
                displayDriverMarkers();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(HomeCommuterActivity.this, "Error loading drivers: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDriverMarkers() {
        for (Driver driver : availableDrivers) {
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

    private void showDriverInfoCard(Driver driver) {
        // This method is now handled by the custom info window adapter
        // The info window will be shown when marker is clicked
    }
    
    /**
     * Handle Request Ride button click from custom info window
     */
    private void onRequestRide(Driver driver) {
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
        
        // Show immediate feedback
        Toast.makeText(this, "Sending request to " + driver.name + "...", Toast.LENGTH_SHORT).show();
        
        // Hide the info window
        for (Marker marker : driverMarkers) {
            if (marker.getTag() == driver) {
                marker.hideInfoWindow();
                break;
            }
        }
        
        // Send ride request
        requestRide(driver);
    }

    private void requestRide(Driver driver) {
        Log.d("RideRequest", "requestRide called for driver: " + driver.name);
        
        if (currentLocation == null || destinationLocation == null) {
            Toast.makeText(this, "Please set your destination first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create ride request
        String rideId = "ride_" + System.currentTimeMillis();
        Log.d("RideRequest", "Creating ride request with ID: " + rideId);
        
        RideRequest rideRequest = new RideRequest(
                rideId,
                currentUserId,
                driver.driverId,
                new RideRequest.LocationData(currentLocation.latitude, currentLocation.longitude, "Current Location"),
                new RideRequest.LocationData(destinationLocation.latitude, destinationLocation.longitude, "Destination"),
                "pending"
        );
        
        Log.d("RideRequest", "Ride request created: " + rideRequest.toString());
        Log.d("RideRequest", "Sending to Firebase...");
        
        // Send ride request to Firebase
        firebaseService.createRideRequest(rideRequest, new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("RideRequest", "Firebase success: " + message);
                // Show prominent success message
                showRideRequestSentMessage(driver.name);
                showWaitingScreen(rideId);
            }
            
            @Override
            public void onError(String error) {
                Log.e("RideRequest", "Firebase error: " + error);
                Toast.makeText(HomeCommuterActivity.this, "Failed to send ride request: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Show a prominent message that ride request has been sent
     */
    private void showRideRequestSentMessage(String driverName) {
        // Create a custom dialog to show the request sent message
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🚗 Ride Request Sent!")
                .setMessage("Your ride request has been sent to " + driverName + ".\n\nPlease wait for the driver to respond...")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showWaitingScreen(String rideId) {
        // Listen for ride request status updates
        firebaseService.listenToRideRequest(rideId, new FirebaseService.RideRequestListener() {
            @Override
            public void onRideRequestUpdated(RideRequest rideRequest) {
                switch (rideRequest.status) {
                    case "accepted":
                        Toast.makeText(HomeCommuterActivity.this, "Ride accepted! Driver is on the way.", Toast.LENGTH_LONG).show();
                        // Here you would navigate to ride tracking screen
                        break;
                    case "declined":
                        Toast.makeText(HomeCommuterActivity.this, "Ride declined. Looking for another driver...", Toast.LENGTH_LONG).show();
                        // Remove declined driver from available list and show others
                        showNearbyDrivers();
                        break;
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(HomeCommuterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
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
                        
                        Toast.makeText(this, "Found: " + place.getName(), Toast.LENGTH_SHORT).show();
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
        String commuterName = prefs.getString(currentUserId + "_name", "Commuter");
        Commuter commuter = new Commuter(
                currentUserId,
                commuterName,
                currentUserId,
                new Commuter.LocationData(0, 0, "Unknown Location")
        );
        
        firebaseService.createCommuter(commuter, new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("HomeCommuterActivity", "Commuter profile created: " + message);
            }
            
            @Override
            public void onError(String error) {
                Log.e("HomeCommuterActivity", "Failed to create commuter profile: " + error);
            }
        });
    }
}
