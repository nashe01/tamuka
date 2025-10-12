package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * OnboardingActivity - Handles the onboarding flow with typewriter animations
 * 
 * This activity manages the three-step onboarding process with animated titles
 * using the TypewriterTextView component. Each screen features a typewriter
 * effect for the main title to create an engaging user experience.
 * 
 * Features:
 * - Three-step onboarding flow
 * - Typewriter animation for titles
 * - Configurable typing speed
 * - Skip and navigation functionality
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class OnboardingActivity extends AppCompatActivity implements View.OnClickListener {

    private int currentStep = 1;
    
    // Typewriter animation speed (milliseconds per character)
    private static final int TYPING_SPEED = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding1);
        bindButtonsForStep(R.id.btnSkip1, R.id.btnNext1, 0);
        
        // Start typewriter animation for the first screen
        initializeTypewriterAnimation();
    }

    /**
     * Initialize typewriter animation for the current screen
     */
    private void initializeTypewriterAnimation() {
        switch (currentStep) {
            case 1:
                initializeScreen1Animation();
                break;
            case 2:
                initializeScreen2Animation();
                break;
            case 3:
                initializeScreen3Animation();
                break;
        }
    }
    
    /**
     * Initialize typewriter animation for screen 1
     */
    private void initializeScreen1Animation() {
        SimpleTypewriterTextView tvTitle1 = findViewById(R.id.tvTitle1);
        if (tvTitle1 != null) {
            tvTitle1.setTypingDelay(TYPING_SPEED);
            tvTitle1.setTypewriterText("Welcome to Swift Ride");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvTitle1);
            if (regularTextView != null) {
                regularTextView.setText("Welcome to Swift Ride");
            }
        }
    }
    
    /**
     * Initialize typewriter animation for screen 2
     */
    private void initializeScreen2Animation() {
        SimpleTypewriterTextView tvTitle2 = findViewById(R.id.tvTitle2);
        if (tvTitle2 != null) {
            tvTitle2.setTypingDelay(TYPING_SPEED);
            tvTitle2.setTypewriterText("Live Location Tracking");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvTitle2);
            if (regularTextView != null) {
                regularTextView.setText("Live Location Tracking");
            }
        }
    }
    
    /**
     * Initialize typewriter animation for screen 3
     */
    private void initializeScreen3Animation() {
        SimpleTypewriterTextView tvTitle3 = findViewById(R.id.tvTitle3);
        if (tvTitle3 != null) {
            tvTitle3.setTypingDelay(TYPING_SPEED);
            tvTitle3.setTypewriterText("Join Swift Ride Today");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvTitle3);
            if (regularTextView != null) {
                regularTextView.setText("Join Swift Ride Today");
            }
        }
    }

    private void bindButtonsForStep(@IdRes int skipId, @IdRes int nextOrGetStartedId, int getStartedFlag) {
        View skip = findViewById(skipId);
        View next = findViewById(nextOrGetStartedId);
        if (skip != null) skip.setOnClickListener(this);
        if (next != null) next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSkip1 || id == R.id.btnSkip2 || id == R.id.btnSkip3) {
            goToLogin();
            return;
        }
        if (id == R.id.btnNext1) {
            currentStep = 2;
            setContentView(R.layout.activity_onboarding2);
            bindButtonsForStep(R.id.btnSkip2, R.id.btnNext2, 0);
            // Initialize typewriter animation for screen 2
            initializeTypewriterAnimation();
            return;
        }
        if (id == R.id.btnNext2) {
            currentStep = 3;
            setContentView(R.layout.activity_onboarding3);
            bindButtonsForStep(R.id.btnSkip3, R.id.btnGetStarted, 1);
            // Initialize typewriter animation for screen 3
            initializeTypewriterAnimation();
            return;
        }
        if (id == R.id.btnGetStarted) {
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}


