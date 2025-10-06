package com.kodelink.glide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPhone;
    private EditText etPassword;
    private Spinner spinnerRole;
    private View btnCreateAccount;
    private View btnBackToLogin;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etSignupPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        if (btnBackToLogin != null) btnBackToLogin.setOnClickListener(v -> finish());
        if (btnCreateAccount != null) btnCreateAccount.setOnClickListener(v -> signupUser());
    }

    private void signupUser() {
        String name = etName != null ? etName.getText().toString().trim() : "";
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString() : "";
        String role = spinnerRole != null && spinnerRole.getSelectedItem() != null ? spinnerRole.getSelectedItem().toString().toLowerCase() : "";

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreateAccount.setEnabled(false);
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if phone already exists
            if (prefs.contains(phone + "_password")) {
                btnCreateAccount.setEnabled(true);
                Toast.makeText(this, "Phone number already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user data to SharedPreferences using phone-based keys
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(phone + "_name", name);
            editor.putString(phone + "_password", password);
            editor.putString(phone + "_role", role);
            editor.putString("current_user_phone", phone);
            editor.apply();

            btnCreateAccount.setEnabled(true);
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, 1500);
    }
}


