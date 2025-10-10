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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

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

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Check authentication immediately without delay for logged-in users
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is already logged in - check role and navigate directly
                checkUserRoleAndNavigate(currentUser.getUid());
            } else {
                // Not logged in - go to onboarding immediately
                navigateToOnboarding();
            }
        } catch (Exception e) {
            // If there's any error with authentication, go to onboarding immediately
            Toast.makeText(this, "Authentication error, please login again", Toast.LENGTH_SHORT).show();
            navigateToOnboarding();
        }
    }

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

    private void navigateToOnboarding() {
        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }
}