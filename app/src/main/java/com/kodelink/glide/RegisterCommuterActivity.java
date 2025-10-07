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
        Map<String, Object> commuterData = new HashMap<>();
        commuterData.put("name", name);
        commuterData.put("email", email);
        commuterData.put("gender", gender);
        commuterData.put("role", "commuter");
        commuterData.put("createdAt", System.currentTimeMillis());

        mFirestore.collection("users").document(userId).set(commuterData)
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
        
        if (errorCode.contains("email-already-in-use")) {
            return "This email address is already registered. Please use a different email or try logging in.";
        } else if (errorCode.contains("weak-password")) {
            return "Password is too weak. Please choose a stronger password.";
        } else if (errorCode.contains("invalid-email")) {
            return "Please enter a valid email address.";
        } else if (errorCode.contains("network-request-failed")) {
            return "Network error. Please check your internet connection and try again.";
        } else {
            return "Registration failed. Please try again.";
        }
    }
}
