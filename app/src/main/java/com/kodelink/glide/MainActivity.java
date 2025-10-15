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
    private static final int SPLASH_DISPLAY_LENGTH = 1000; // 1000ms for all users
    private static final int SPLASH_DISPLAY_LENGTH_LOGGED_IN = 2000; // 1000ms for logged-in users

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

        // Show splash screen for the specified duration, then check authentication
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    prepareMainScreenForUser(user.getUid());
                } else {
                    startActivity(new Intent(MainActivity.this, OnboardingActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    /**
     * Prepares the main screen for a logged-in user by checking their role.
     * This method handles the transition directly in the Firestore callback to prevent black screens.
     */
    private void prepareMainScreenForUser(String userId) {
        mFirestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    Intent intent;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String role = task.getResult().getString("role");
                        if ("commuter".equals(role)) {
                            intent = new Intent(MainActivity.this, HomeCommuterActivity.class);
                        } else if ("driver".equals(role)) {
                            intent = new Intent(MainActivity.this, DashboardDriverActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, OnboardingActivity.class);
                        }
                    } else {
                        intent = new Intent(MainActivity.this, OnboardingActivity.class);
                    }

                    // Start the activity with smooth fade transition
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                });
    }

}