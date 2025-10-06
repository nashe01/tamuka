package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
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


