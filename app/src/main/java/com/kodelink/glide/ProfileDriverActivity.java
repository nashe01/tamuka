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

public class ProfileDriverActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPhone;
    private EditText etCarType;
    private EditText etCarColor;
    private EditText etCarPlate;
    private View btnSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_driver);

        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        etName = findViewById(R.id.etDriverName);
        etPhone = findViewById(R.id.etDriverPhone);
        etCarType = findViewById(R.id.etCarType);
        etCarColor = findViewById(R.id.etCarColor);
        etCarPlate = findViewById(R.id.etCarPlate);
        btnSave = findViewById(R.id.btnSaveDriver);

        if (btnSave != null) btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String currentUserPhone = prefs.getString("current_user_phone", "");
        if (currentUserPhone.isEmpty()) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = etName != null ? etName.getText().toString().trim() : "";
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        String carType = etCarType != null ? etCarType.getText().toString().trim() : "";
        String carColor = etCarColor != null ? etCarColor.getText().toString().trim() : "";
        String carPlate = etCarPlate != null ? etCarPlate.getText().toString().trim() : "";
        if (name.isEmpty() || phone.isEmpty() || carType.isEmpty() || carColor.isEmpty() || carPlate.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(currentUserPhone + "_name", name);
            editor.putString(currentUserPhone + "_phone", phone);
            editor.putString(currentUserPhone + "_carType", carType);
            editor.putString(currentUserPhone + "_carColor", carColor);
            editor.putString(currentUserPhone + "_carPlate", carPlate);
            editor.apply();
            
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish();
        }, 1000);
    }
}



