package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class RoleSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardCommuter;
    private MaterialCardView cardDriver;
    private View tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);

        cardCommuter = findViewById(R.id.cardCommuter);
        cardDriver = findViewById(R.id.cardDriver);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Commuter registration
        cardCommuter.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterCommuterActivity.class);
            startActivity(intent);
        });

        // Driver registration
        cardDriver.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterDriverActivity.class);
            startActivity(intent);
        });

        // Back to login
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
