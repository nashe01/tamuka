package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterCommuterActivity extends BaseActivity {

    private TextInputEditText etName, etSurname, etEmail, etPassword;
    private Spinner spinnerGender;
    private MaterialButton btnRegister;
    private View tvBackToRoleSelection;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_commuter);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToRoleSelection = findViewById(R.id.tvBackToRoleSelection);

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
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(surname)) {
            etSurname.setError("Surname is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        // Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully, now save to database
                        String userId = mAuth.getCurrentUser().getUid();
                        saveCommuterToDatabase(userId, name, surname, email, gender);
                    } else {
                        // Registration failed
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");
                        Toast.makeText(RegisterCommuterActivity.this, 
                                "Registration failed: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveCommuterToDatabase(String userId, String name, String surname, 
                                      String email, String gender) {
        Map<String, Object> commuterData = new HashMap<>();
        commuterData.put("name", name);
        commuterData.put("surname", surname);
        commuterData.put("email", email);
        commuterData.put("gender", gender);
        commuterData.put("role", "commuter");

        mDatabase.child("commuters").child(userId).setValue(commuterData)
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Register");
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterCommuterActivity.this, 
                                "Registration successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to HomeCommuterActivity
                        Intent intent = new Intent(RegisterCommuterActivity.this, HomeCommuterActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterCommuterActivity.this, 
                                "Failed to save user data: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
