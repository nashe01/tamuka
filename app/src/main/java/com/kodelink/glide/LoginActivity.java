package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private View tvForgotPassword;
    private View tvRegisterLink;
    private ScrollView scrollView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Handler suggestionHandler;
    private boolean suggestionsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        scrollView = findViewById(R.id.scrollView);

        // Initialize suggestion handler
        suggestionHandler = new Handler(Looper.getMainLooper());

        // Setup long-press detection for input assistance
        setupInputAssistance();
        
        // Setup touch detection to disable suggestions when tapping outside
        setupTouchDetection();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup focus handling for keyboard visibility
        setupFocusHandling();
        
        // AuthStateListener removed to prevent double navigation
    }


    private void setupClickListeners() {
        // Login button click
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        }

        // Forgot password click
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            });
        }

        // Register link click
        if (tvRegisterLink != null) {
            tvRegisterLink.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class));
            });
        }
    }

    private void setupInputAssistance() {
        // Setup long-press detection for email field
        if (etEmail != null) {
            etEmail.setOnLongClickListener(v -> {
                enableSuggestionsTemporarily(etEmail);
                return true;
            });
        }

        // Setup long-press detection for password field
        if (etPassword != null) {
            etPassword.setOnLongClickListener(v -> {
                enableSuggestionsTemporarily(etPassword);
                return true;
            });
        }
    }

    private void setupTouchDetection() {
        // Get the root view to detect touches outside input fields
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnClickListener(v -> {
                // Disable suggestions when tapping outside input fields
                if (suggestionsEnabled) {
                    disableSuggestions();
                }
            });
        }
    }

    private void setupFocusHandling() {
        // Handle focus changes to ensure password field is visible when keyboard appears
        if (etPassword != null) {
            etPassword.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && scrollView != null) {
                    // Delay the scroll to ensure keyboard is fully shown
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        scrollView.smoothScrollTo(0, etPassword.getBottom());
                    }, 100);
                }
            });
        }

        // Also handle email field focus to ensure smooth scrolling
        if (etEmail != null) {
            etEmail.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && scrollView != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        scrollView.smoothScrollTo(0, etEmail.getBottom());
                    }, 100);
                }
            });
        }
    }

    private void enableSuggestionsTemporarily(TextInputEditText editText) {
        if (suggestionsEnabled) return;

        suggestionsEnabled = true;
        
        // Temporarily enable suggestions
        editText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        editText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        editText.setPrivateImeOptions(null);
        
        // Show toast to inform user
        Toast.makeText(this, "Input assistance enabled. Tap outside to disable.", Toast.LENGTH_SHORT).show();
        
        // Auto-disable after 10 seconds
        suggestionHandler.postDelayed(() -> {
            disableSuggestions();
        }, 10000);
    }

    private void disableSuggestions() {
        if (!suggestionsEnabled) return;

        suggestionsEnabled = false;
        
        // Disable suggestions for both fields
        if (etEmail != null) {
            etEmail.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            etEmail.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI | 
                                android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN);
            etEmail.setPrivateImeOptions("disablePersonalization=true");
        }
        
        if (etPassword != null) {
            etPassword.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            etPassword.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI | 
                                   android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN);
            etPassword.setPrivateImeOptions("disablePersonalization=true");
        }
        
        // Clear any pending handlers
        suggestionHandler.removeCallbacksAndMessages(null);
    }

    // Removed AuthStateListener to prevent double navigation after login

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suggestionHandler != null) {
            suggestionHandler.removeCallbacksAndMessages(null);
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Disable button and show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful, check user role
                        String userId = mAuth.getCurrentUser().getUid();
                        checkUserRole(userId);
                    } else {
                        // Login failed - restore button state
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        
                        // Show user-friendly error message
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        // Check user role in Firestore
        mFirestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("commuter".equals(role)) {
                                // User is a commuter
                                Intent intent = new Intent(LoginActivity.this, HomeCommuterActivity.class);
                                startActivity(intent);
                                finish();
                            } else if ("driver".equals(role)) {
                                // User is a driver
                                Intent intent = new Intent(LoginActivity.this, DashboardDriverActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Invalid role - restore button state and show error
                                btnLogin.setEnabled(true);
                                btnLogin.setText("Login");
                                Toast.makeText(LoginActivity.this, 
                                        "Invalid user role. Please contact support.", 
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            // User document not found - restore button state and show error
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");
                            Toast.makeText(LoginActivity.this, 
                                    "User data not found. Please register again.", 
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        // Firestore error - restore button state and show error
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        Toast.makeText(LoginActivity.this, 
                                "Failed to verify user data. Please try again.", 
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                });
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "An unknown error occurred";
        
        String errorCode = exception.getMessage();
        if (errorCode == null) return "An unknown error occurred";
        
        if (errorCode.contains("user-not-found")) {
            return "No account found with this email address. Please check your email or register.";
        } else if (errorCode.contains("wrong-password")) {
            return "Incorrect password. Please try again.";
        } else if (errorCode.contains("invalid-email")) {
            return "Please enter a valid email address.";
        } else if (errorCode.contains("user-disabled")) {
            return "This account has been disabled. Please contact support.";
        } else if (errorCode.contains("too-many-requests")) {
            return "Too many failed attempts. Please try again later.";
        } else if (errorCode.contains("network-request-failed")) {
            return "Network error. Please check your internet connection and try again.";
        } else {
            return "Login failed. Please try again.";
        }
    }
}


