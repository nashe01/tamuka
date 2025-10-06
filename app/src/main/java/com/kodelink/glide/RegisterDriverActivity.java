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

public class RegisterDriverActivity extends BaseActivity {

    private TextInputEditText etName, etSurname, etEmail, etAge, etVehicleName, etVehicleType, etVehiclePlate, etPassword;
    private Spinner spinnerGender;
    private MaterialButton btnRegister;
    private View tvBackToRoleSelection;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_driver);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etVehicleName = findViewById(R.id.etVehicleName);
        etVehicleType = findViewById(R.id.etVehicleType);
        etVehiclePlate = findViewById(R.id.etVehiclePlate);
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
        btnRegister.setOnClickListener(v -> registerDriver());

        // Back to role selection
        tvBackToRoleSelection.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterDriverActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerDriver() {
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String vehicleName = etVehicleName.getText().toString().trim();
        String vehicleType = etVehicleType.getText().toString().trim();
        String vehiclePlate = etVehiclePlate.getText().toString().trim();
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
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            return;
        }
        if (TextUtils.isEmpty(vehicleName)) {
            etVehicleName.setError("Vehicle name is required");
            return;
        }
        if (TextUtils.isEmpty(vehicleType)) {
            etVehicleType.setError("Vehicle type is required");
            return;
        }
        if (TextUtils.isEmpty(vehiclePlate)) {
            etVehiclePlate.setError("Vehicle plate is required");
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
                        saveDriverToDatabase(userId, name, surname, email, age, gender, 
                                          vehicleName, vehicleType, vehiclePlate);
                    } else {
                        // Registration failed
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");
                        Toast.makeText(RegisterDriverActivity.this, 
                                "Registration failed: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveDriverToDatabase(String userId, String name, String surname, 
                                   String email, String age, String gender,
                                   String vehicleName, String vehicleType, String vehiclePlate) {
        Map<String, Object> driverData = new HashMap<>();
        driverData.put("name", name);
        driverData.put("surname", surname);
        driverData.put("email", email);
        driverData.put("age", age);
        driverData.put("gender", gender);
        driverData.put("vehicleName", vehicleName);
        driverData.put("vehicleType", vehicleType);
        driverData.put("vehiclePlate", vehiclePlate);
        driverData.put("role", "driver");
        driverData.put("rating", 0);
        driverData.put("completedRides", 0);
        driverData.put("availability", "offline");

        mDatabase.child("drivers").child(userId).setValue(driverData)
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Register");
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterDriverActivity.this, 
                                "Registration successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to DashboardDriverActivity
                        Intent intent = new Intent(RegisterDriverActivity.this, DashboardDriverActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterDriverActivity.this, 
                                "Failed to save user data: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
