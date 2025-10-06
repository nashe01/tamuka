package com.kodelink.glide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

public class SignupActivity extends BaseActivity {

    private EditText etName;
    private EditText etPhone;
    private EditText etPassword;
    private Spinner spinnerRole;
    private View btnCreateAccount;
    private View btnBackToLogin;
    private ImageView keyboardToggle;
    private SharedPreferences prefs;
    private InputMethodManager inputMethodManager;
    private boolean isKeyboardVisible = false;

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
        keyboardToggle = findViewById(R.id.keyboardToggle);

        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (btnBackToLogin != null) btnBackToLogin.setOnClickListener(v -> finish());
        if (btnCreateAccount != null) btnCreateAccount.setOnClickListener(v -> signupUser());
        
        // Debug: Check if keyboardToggle is found
        if (keyboardToggle != null) {
            Toast.makeText(this, "Keyboard toggle found!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Keyboard toggle NOT found!", Toast.LENGTH_SHORT).show();
        }
        
        setupKeyboardToggle();
    }

    private void setupKeyboardToggle() {
        if (keyboardToggle == null) return;
        
        keyboardToggle.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        float endY = event.getY();
                        float deltaY = startY - endY;
                        
                        // Swipe up (or tap) - show keyboard
                        if (deltaY > 50 || Math.abs(deltaY) < 10) {
                            showKeyboard();
                        }
                        // Swipe down - hide keyboard
                        else if (deltaY < -50) {
                            hideKeyboardWithAnimation();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void showKeyboard() {
        if (etName != null) {
            etName.requestFocus();
            inputMethodManager.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT);
            animateArrow(180f);
            isKeyboardVisible = true;
        }
    }

    private void hideKeyboardWithAnimation() {
        if (keyboardToggle != null) {
            inputMethodManager.hideSoftInputFromWindow(keyboardToggle.getWindowToken(), 0);
            animateArrow(0f);
            isKeyboardVisible = false;
        }
    }

    private void animateArrow(float rotation) {
        if (keyboardToggle != null) {
            keyboardToggle.animate()
                    .rotation(rotation)
                    .setDuration(300)
                    .start();
        }
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


