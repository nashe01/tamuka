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

public class ProfileCommuterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPhone;
    private View btnSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_commuter);

        prefs = getSharedPreferences("MockAuth", MODE_PRIVATE);

        etName = findViewById(R.id.etCommuterName);
        etPhone = findViewById(R.id.etCommuterPhone);
        btnSave = findViewById(R.id.btnSaveCommuter);

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
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(currentUserPhone + "_name", name);
            editor.putString(currentUserPhone + "_phone", phone);
            editor.apply();
            
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish();
        }, 1000);
    }
}



