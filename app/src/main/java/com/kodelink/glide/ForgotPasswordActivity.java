package com.kodelink.glide;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {
    
    private TextInputEditText etEmail;
    private MaterialButton btnSubmitReset;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar
        setupToolbar();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        btnSubmitReset = findViewById(R.id.btnSubmitReset);

        // Setup click listeners
        btnSubmitReset.setOnClickListener(v -> resetPassword());
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reset Password");
        }
        
        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

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

        // Disable button and show loading state
        btnSubmitReset.setEnabled(false);
        btnSubmitReset.setText("Sending...");

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Restore button state
                    btnSubmitReset.setEnabled(true);
                    btnSubmitReset.setText("Send Reset Email");
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, 
                                "Password reset email sent to " + email, 
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // Show user-friendly error message
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "An unknown error occurred";
        
        String errorCode = exception.getMessage();
        if (errorCode == null) return "An unknown error occurred";
        
        if (errorCode.contains("user-not-found")) {
            return "No account found with this email address. Please check your email or register.";
        } else if (errorCode.contains("invalid-email")) {
            return "Please enter a valid email address.";
        } else if (errorCode.contains("too-many-requests")) {
            return "Too many requests. Please try again later.";
        } else if (errorCode.contains("network-request-failed")) {
            return "Network error. Please check your internet connection and try again.";
        } else {
            return "Failed to send reset email. Please try again.";
        }
    }
}


