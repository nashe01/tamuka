package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private View tvForgotPassword;
    private View tvRegisterLink;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Handler suggestionHandler;
    private boolean suggestionsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // Initialize suggestion handler
        suggestionHandler = new Handler(Looper.getMainLooper());

        // Setup long-press detection for input assistance
        setupInputAssistance();
        
        // Setup touch detection to disable suggestions when tapping outside
        setupTouchDetection();

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
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

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
                        // Login failed
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        Toast.makeText(LoginActivity.this, 
                                "Login failed: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        // Check if user is a commuter
        mDatabase.child("commuters").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User is a commuter
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Intent intent = new Intent(LoginActivity.this, HomeCommuterActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Check if user is a driver
                    mDatabase.child("drivers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                btnLogin.setEnabled(true);
                            btnLogin.setText("Login");
                            
                            if (dataSnapshot.exists()) {
                                // User is a driver
                                Intent intent = new Intent(LoginActivity.this, DashboardDriverActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // User not found in either role
                                Toast.makeText(LoginActivity.this, 
                                        "User data not found. Please register again.", 
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                btnLogin.setEnabled(true);
                            btnLogin.setText("Login");
                            Toast.makeText(LoginActivity.this, 
                                    "Database error: " + databaseError.getMessage(), 
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(LoginActivity.this, 
                        "Database error: " + databaseError.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}


