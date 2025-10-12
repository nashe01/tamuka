package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MainActivity - Entry point of the Swift Ride application
 * 
 * This activity serves as the splash screen and authentication gateway.
 * It checks if a user is already logged in and navigates them to the appropriate
 * screen based on their role (commuter or driver). If no user is logged in,
 * it redirects to the onboarding flow.
 * 
 * Key Features:
 * - Automatic authentication check on app launch
 * - Role-based navigation (commuter vs driver)
 * - Error handling for authentication failures
 * - Seamless user experience for returning users
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    // Firebase authentication instance for user management
    private FirebaseAuth mAuth;
    
    // Firestore database instance for user data retrieval
    private FirebaseFirestore mFirestore;
    
    // Splash screen display duration in milliseconds
    private static final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

    /**
     * Called when the activity is first created.
     * Sets up the splash screen layout and initializes Firebase services.
     * Shows splash screen for a fixed duration before checking authentication.
     * 
     * @param savedInstanceState Previously saved state data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase authentication and Firestore database
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Show splash screen for a fixed duration, then check authentication
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthenticationAndNavigate();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    /**
     * Checks authentication status and navigates to appropriate screen.
     * This method is called after the splash screen delay.
     */
    private void checkAuthenticationAndNavigate() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is already logged in - check role and navigate
                checkUserRoleAndNavigate(currentUser.getUid());
            } else {
                // Not logged in - go to onboarding
                navigateToOnboarding();
            }
        } catch (Exception e) {
            // If there's any error with authentication, go to onboarding
            Toast.makeText(this, "Authentication error, please login again", Toast.LENGTH_SHORT).show();
            navigateToOnboarding();
        }
    }

    /**
     * Checks the user's role in Firestore and navigates to the appropriate activity.
     * Handles role-based routing for commuters and drivers.
     * 
     * @param userId The Firebase user ID to check role for
     */
    private void checkUserRoleAndNavigate(String userId) {
        try {
            // Check user role in Firestore
            mFirestore.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String role = document.getString("role");
                                    if ("commuter".equals(role)) {
                                        // User is a commuter - navigate directly to commuter home
                                        Intent intent = new Intent(MainActivity.this, HomeCommuterActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else if ("driver".equals(role)) {
                                        // User is a driver - navigate directly to driver dashboard
                                        Intent intent = new Intent(MainActivity.this, DashboardDriverActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Invalid role - sign out and go to onboarding
                                        mAuth.signOut();
                                        navigateToOnboarding();
                                    }
                                } else {
                                    // User document not found - sign out and go to onboarding
                                    mAuth.signOut();
                                    navigateToOnboarding();
                                }
                            } else {
                                // Firestore error - sign out and go to onboarding
                                mAuth.signOut();
                                navigateToOnboarding();
                            }
                        } catch (Exception e) {
                            // If there's any error during navigation, go to onboarding
                            Toast.makeText(MainActivity.this, "Navigation error, please login again", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            navigateToOnboarding();
                        }
                    });
        } catch (Exception e) {
            // If there's any error with the role check, go to onboarding
            Toast.makeText(this, "Role check error, please login again", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            navigateToOnboarding();
        }
    }

    /**
     * Navigates to the onboarding activity for new or unauthenticated users.
     * This is the entry point for the user registration/login flow.
     */
    private void navigateToOnboarding() {
        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }
}