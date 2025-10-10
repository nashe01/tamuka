package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterCommuterActivity extends BaseActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Spinner spinnerGender;
    private MaterialButton btnRegister;
    private View tvBackToRoleSelection;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_commuter);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToRoleSelection = findViewById(R.id.tvBackToRoleSelection);
        progressBar = findViewById(R.id.progressBar);

        // Setup gender spinner
        String[] genderOptions = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Register button click
        btnRegister.setOnClickListener(v -> registerCommuter());

        // Back to role selection
        tvBackToRoleSelection.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCommuterActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void registerCommuter() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Disable button and show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");
        progressBar.setVisibility(View.VISIBLE);

        // Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully, now save to Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        saveCommuterToFirestore(userId, name, email, gender);
                    } else {
                        // Registration failed - restore button state
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");
                        progressBar.setVisibility(View.GONE);
                        
                        // Show user-friendly error message
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(RegisterCommuterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveCommuterToFirestore(String userId, String name, String email, String gender) {
        FirebaseService firebaseService = FirebaseService.getInstance();
        
        // Create commuter entity in Firestore
        firebaseService.createCommuter(userId, name)
                .addOnCompleteListener(task -> {
                    // Restore button state
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Register");
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterCommuterActivity.this, 
                                "Registration successful!", Toast.LENGTH_SHORT).show();
                        
                        // Sign out to prevent auto-login and navigate to Login screen
                        mAuth.signOut();
                        Intent intent = new Intent(RegisterCommuterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If Firestore save fails, delete the Firebase Auth user
                        mAuth.getCurrentUser().delete();
                        Toast.makeText(RegisterCommuterActivity.this, 
                                "Failed to save user data. Please try again.", 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "An unknown error occurred";
        
        String errorCode = exception.getMessage();
        if (errorCode == null) return "An unknown error occurred";
        
        // Handle specific Firebase Auth errors
        if (errorCode.contains("email-already-in-use")) {
            return "This email address is already registered. Please use a different email or try logging in.";
        } else if (errorCode.contains("weak-password")) {
            return "Password is too weak. Please choose a stronger password.";
        } else if (errorCode.contains("invalid-email")) {
            return "Please enter a valid email address.";
        } else if (errorCode.contains("network-request-failed")) {
            return "Network error. Please check your internet connection and try again.";
        } else if (errorCode.contains("too-many-requests")) {
            return "Too many failed attempts. Please try again later.";
        } else if (errorCode.contains("operation-not-allowed")) {
            return "Email/password authentication is not enabled. Please contact support.";
        } else if (errorCode.contains("user-disabled")) {
            return "This account has been disabled. Please contact support.";
        } else if (errorCode.contains("user-not-found")) {
            return "No account found with this email address.";
        } else if (errorCode.contains("wrong-password")) {
            return "Incorrect password. Please try again.";
        } else if (errorCode.contains("invalid-credential")) {
            return "Invalid credentials. Please check your email and password.";
        } else if (errorCode.contains("account-exists-with-different-credential")) {
            return "An account already exists with this email but different sign-in method.";
        } else if (errorCode.contains("requires-recent-login")) {
            return "This operation requires recent authentication. Please log in again.";
        } else if (errorCode.contains("provider-already-linked")) {
            return "This account is already linked to another provider.";
        } else if (errorCode.contains("no-such-provider")) {
            return "The specified provider is not available.";
        } else if (errorCode.contains("invalid-user-token")) {
            return "Invalid user token. Please log in again.";
        } else if (errorCode.contains("user-token-expired")) {
            return "Your session has expired. Please log in again.";
        } else if (errorCode.contains("null-user")) {
            return "No user is currently signed in.";
        } else if (errorCode.contains("app-not-authorized")) {
            return "This app is not authorized to use Firebase Authentication.";
        } else if (errorCode.contains("keychain-error")) {
            return "Keychain error. Please try again.";
        } else if (errorCode.contains("internal-error")) {
            return "Internal error. Please try again.";
        } else if (errorCode.contains("invalid-api-key")) {
            return "Invalid API key. Please contact support.";
        } else if (errorCode.contains("network-request-failed")) {
            return "Network error. Please check your internet connection and try again.";
        } else if (errorCode.contains("play-services-not-available") || 
                   errorCode.contains("play-services") ||
                   errorCode.contains("providerinstaller") ||
                   errorCode.contains("SecurityException") ||
                   errorCode.contains("Unknown calling package")) {
            return "Google Play Services issue detected. This may be due to:\n\n" +
                   "• Running on an emulator without Google Play\n" +
                   "• Outdated Google Play Services\n" +
                   "• Missing Google Play Store\n\n" +
                   "Please try on a real device with Google Play Services or update your emulator.";
        } else {
            // Log the full error for debugging
            android.util.Log.e("RegisterCommuter", "Unknown Firebase error: " + errorCode, exception);
            return "Registration failed: " + errorCode + "\n\nPlease try again or contact support if the issue persists.";
        }
    }
}
