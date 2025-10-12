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
    private static final int TYPING_SPEED = 50;

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
        // Animate title first
        SimpleTypewriterTextView tvTitle1 = findViewById(R.id.tvTitle1);
        if (tvTitle1 != null) {
            tvTitle1.setTypingDelay(TYPING_SPEED);
            tvTitle1.setTypewriterCallback(new SimpleTypewriterTextView.TypewriterCallback() {
                @Override
                public void onAnimationComplete() {
                    // Start description animation after title completes
                    startDescription1Animation();
                }
            });
            tvTitle1.setTypewriterText("Welcome to Swift Ride");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvTitle1);
            if (regularTextView != null) {
                regularTextView.setText("Welcome to Swift Ride");
            }
            // Start description immediately if title fails
            startDescription1Animation();
        }
    }
    
    /**
     * Start description animation for screen 1
     */
    private void startDescription1Animation() {
        SimpleTypewriterTextView tvDescription1 = findViewById(R.id.tvDescription1);
        if (tvDescription1 != null) {
            tvDescription1.setTypingDelay(TYPING_SPEED);
            tvDescription1.setTypewriterText("Experience seamless transportation with Swift Ride. Whether you're looking for a ride or want to earn by driving.");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvDescription1);
            if (regularTextView != null) {
                regularTextView.setText("Experience seamless transportation with Swift Ride. Whether you're looking for a ride or want to earn by driving.");
            }
        }
    }
    
    /**
     * Initialize typewriter animation for screen 2
     */
    private void initializeScreen2Animation() {
        // Animate title first
        SimpleTypewriterTextView tvTitle2 = findViewById(R.id.tvTitle2);
        if (tvTitle2 != null) {
            tvTitle2.setTypingDelay(TYPING_SPEED);
            tvTitle2.setTypewriterCallback(new SimpleTypewriterTextView.TypewriterCallback() {
                @Override
                public void onAnimationComplete() {
                    // Start description animation after title completes
                    startDescription2Animation();
                }
            });
            tvTitle2.setTypewriterText("Live Location Tracking");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvTitle2);
            if (regularTextView != null) {
                regularTextView.setText("Live Location Tracking");
            }
            // Start description immediately if title fails
            startDescription2Animation();
        }
    }
    
    /**
     * Start description animation for screen 2
     */
    private void startDescription2Animation() {
        SimpleTypewriterTextView tvDescription2 = findViewById(R.id.tvDescription2);
        if (tvDescription2 != null) {
            tvDescription2.setTypingDelay(TYPING_SPEED);
            tvDescription2.setTypewriterText("Stay connected with real-time location tracking. See exactly where your driver is and get accurate arrival times for peace of mind throughout your journey.");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvDescription2);
            if (regularTextView != null) {
                regularTextView.setText("Stay connected with real-time location tracking. See exactly where your driver is and get accurate arrival times for peace of mind throughout your journey.");
            }
        }
    }
    
    /**
     * Initialize typewriter animation for screen 3
     */
    private void initializeScreen3Animation() {
        // Animate title first
        SimpleTypewriterTextView tvTitle3 = findViewById(R.id.tvTitle3);
        if (tvTitle3 != null) {
            tvTitle3.setTypingDelay(TYPING_SPEED);
            tvTitle3.setTypewriterCallback(new SimpleTypewriterTextView.TypewriterCallback() {
                @Override
                public void onAnimationComplete() {
                    // Start description animation after title completes
                    startDescription3Animation();
                }
            });
            tvTitle3.setTypewriterText("Join Swift Ride Today");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvTitle3);
            if (regularTextView != null) {
                regularTextView.setText("Join Swift Ride Today");
            }
            // Start description immediately if title fails
            startDescription3Animation();
        }
    }
    
    /**
     * Start description animation for screen 3
     */
    private void startDescription3Animation() {
        SimpleTypewriterTextView tvDescription3 = findViewById(R.id.tvDescription3);
        if (tvDescription3 != null) {
            tvDescription3.setTypingDelay(TYPING_SPEED);
            tvDescription3.setTypewriterText("Ready to get started? Join thousands of users who trust Swift Ride for their daily transportation needs!");
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            android.widget.TextView regularTextView = findViewById(R.id.tvDescription3);
            if (regularTextView != null) {
                regularTextView.setText("Ready to get started? Join thousands of users who trust Swift Ride for their daily transportation needs!");
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


