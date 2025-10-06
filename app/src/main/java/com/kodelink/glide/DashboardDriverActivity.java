package com.kodelink.glide;

import android.Manifest;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

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
        
        // Get current driver ID
        SharedPreferences authPrefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        String currentUserPhone = authPrefs.getString("current_user_phone", "");
        currentDriverId = "driver_" + currentUserPhone.replaceAll("[^0-9]", "");

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();
        
        // Create driver profile in Firebase
        createDriverProfile();

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
            String status = isChecked ? "Available" : "Offline";
            int color = isChecked ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);
            
            tvAvailabilityStatus.setText(status);
            tvAvailabilityStatus.setTextColor(color);
            
            // Save status to SharedPreferences
            prefs.edit().putBoolean(KEY_DRIVER_STATUS, isChecked).apply();
            
            // Update availability in Firebase
            String availability = isChecked ? "available" : "offline";
            firebaseService.updateDriverAvailability(currentDriverId, availability, new FirebaseService.DatabaseCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(DashboardDriverActivity.this, "Status updated: " + status, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(DashboardDriverActivity.this, "Failed to update status: " + error, Toast.LENGTH_SHORT).show();
                }
            });
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

        // Set up ride request listening
        setupRideRequestListening();

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
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            
                            // Add marker for current location
                            googleMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Your Location"));
                            
                            // Move camera to current location
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                            
                            // Update driver location in Firebase
                            updateDriverLocationInFirebase();
                        }
                    });
        }
    }
    
    private void updateDriverLocationInFirebase() {
        if (currentLocation != null) {
            Driver.LocationData locationData = new Driver.LocationData(
                    currentLocation.latitude, 
                    currentLocation.longitude, 
                    "Current Location"
            );
            
            firebaseService.updateDriverLocation(currentDriverId, locationData, new FirebaseService.DatabaseCallback() {
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
        SharedPreferences authPrefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        String currentUserPhone = authPrefs.getString("current_user_phone", "");
        String role = authPrefs.getString(currentUserPhone + "_role", "driver");
        
        TextView tvUserRole = navigationView.getHeaderView(0).findViewById(R.id.tvUserRole);
        if (tvUserRole != null) {
            tvUserRole.setText(role.equals("driver") ? "Driver" : "Commuter");
        }
    }
    
    private void setupRideRequestListening() {
        // Listen for incoming ride requests
        firebaseService.listenToDriverRideRequests(currentDriverId, new FirebaseService.RideRequestListener() {
            @Override
            public void onRideRequestUpdated(RideRequest rideRequest) {
                if ("pending".equals(rideRequest.status)) {
                    currentRideRequest = rideRequest;
                    showIncomingRideRequestDialog(rideRequest);
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(DashboardDriverActivity.this, "Error listening to ride requests: " + error, Toast.LENGTH_SHORT).show();
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
        // Update ride request status to accepted
        firebaseService.updateRideRequestStatus(rideRequest.rideId, "accepted", new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(DashboardDriverActivity.this, "Ride accepted! Navigate to pickup location.", Toast.LENGTH_LONG).show();
                // Here you would navigate to ride tracking screen
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(DashboardDriverActivity.this, "Failed to accept ride: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void declineRideRequest(RideRequest rideRequest) {
        // Update ride request status to declined
        firebaseService.updateRideRequestStatus(rideRequest.rideId, "declined", new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(DashboardDriverActivity.this, "Ride declined", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(DashboardDriverActivity.this, "Failed to decline ride: " + error, Toast.LENGTH_SHORT).show();
            }
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
    
    private void createDriverProfile() {
        SharedPreferences authPrefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        String driverName = authPrefs.getString(currentDriverId.replace("driver_", "") + "_name", "Driver");
        
        Driver driver = new Driver(
                currentDriverId,
                driverName,
                currentDriverId.replace("driver_", ""),
                new Driver.LocationData(0, 0, "Unknown Location"),
                4.5,
                0,
                "offline"
        );
        
        firebaseService.createDriver(driver, new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("DashboardDriverActivity", "Driver profile created: " + message);
            }
            
            @Override
            public void onError(String error) {
                Log.e("DashboardDriverActivity", "Failed to create driver profile: " + error);
            }
        });
    }
}
