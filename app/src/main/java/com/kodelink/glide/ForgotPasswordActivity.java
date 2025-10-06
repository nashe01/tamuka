package com.kodelink.glide;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());

        View submit = findViewById(R.id.btnSubmitReset);
        if (submit != null) submit.setOnClickListener(v -> {
            EditText input = findViewById(R.id.etPhone);
            String phone = input != null ? input.getText().toString().trim() : "";
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            // Simulate network delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                String storedPassword = prefs.getString(phone + "_password", "");
                
                if (storedPassword.isEmpty()) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                } else {
                    // Mock: display the stored password directly
                    Toast.makeText(this, "Password reset link sent (mock). Your password is: " + storedPassword, Toast.LENGTH_LONG).show();
                    finish();
                }
            }, 1500);
        });
    }
}


