package com.kodelink.glide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private View btnLogin;
    private EditText etPhone;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        }

        View signUp = findViewById(R.id.btnGoToSignup);
        if (signUp != null) {
            signUp.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            });
        }

        View forgot = findViewById(R.id.tvForgotPassword);
        if (forgot != null) {
            forgot.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            });
        }
    }

    private void loginUser() {
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString() : "";
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter phone number and password", Toast.LENGTH_SHORT).show();
            return;
        }
        btnLogin.setEnabled(false);
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if phone number exists and get stored password
            String storedPassword = prefs.getString(phone + "_password", "");
            String role = prefs.getString(phone + "_role", "");
            
            if (storedPassword.isEmpty()) {
                btnLogin.setEnabled(true);
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!storedPassword.equals(password)) {
                btnLogin.setEnabled(true);
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get user role and navigate
            prefs.edit().putString("current_user_phone", phone).apply();
            
            btnLogin.setEnabled(true);
            if ("driver".equalsIgnoreCase(role)) {
                startActivity(new Intent(LoginActivity.this, DashboardDriverActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, HomeCommuterActivity.class));
            }
            finish();
        }, 1500);
    }
}


