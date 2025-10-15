package com.kodelink.glide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

/**
 * OnboardingActivity - Handles the onboarding flow with ViewPager2 and typewriter animations
 * 
 * This activity manages the three-step onboarding process using ViewPager2 with
 * fragments. Each screen features typewriter animations for both title and
 * description text, with auto-advance functionality.
 * 
 * Features:
 * - ViewPager2 with fragments
 * - Typewriter animation for titles and descriptions
 * - Auto-advance functionality
 * - Page indicators
 * - Skip and navigation functionality
 * 
 * @author Swift Ride Development Team
 * @version 2.0
 */
public class OnboardingActivity extends AppCompatActivity implements View.OnClickListener, OnboardingFragment.OnAnimationCompleteListener {

    private ViewPager2 viewPager;
    private OnboardingPagerAdapter adapter;
    private Button btnSkip;
    private Button btnNext;
    private View indicator1, indicator2, indicator3;
    
    private int currentPage = 0;
    private static final int TOTAL_PAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        
        // Initialize views
        initializeViews();
        
        // Setup ViewPager2
        setupViewPager();
        
        // Setup page indicators
        setupPageIndicators();
        
        // Setup button listeners
        setupButtonListeners();
        
        // Start animation for first page
        startFirstPageAnimation();
    }

    /**
     * Initialize views
     */
    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);
    }
    
    /**
     * Setup ViewPager2
     */
    private void setupViewPager() {
        adapter = new OnboardingPagerAdapter(this);
        
        // Set up animation listener in the adapter
        adapter.setAnimationCompleteListener(this);
        
        viewPager.setAdapter(adapter);
        
        // Disable user swiping to control navigation programmatically
        viewPager.setUserInputEnabled(false);
        
        // Listen for page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updatePageIndicators();
                updateButtonText();
                
                // Animation will be triggered by fragment lifecycle methods
                // No need to manually trigger here
            }
        });
    }
    
    /**
     * Setup page indicators
     */
    private void setupPageIndicators() {
        updatePageIndicators();
    }
    
    /**
     * Update page indicators based on current page
     */
    private void updatePageIndicators() {
        // Reset all indicators to inactive
        indicator1.setBackgroundResource(R.drawable.indicator_inactive);
        indicator2.setBackgroundResource(R.drawable.indicator_inactive);
        indicator3.setBackgroundResource(R.drawable.indicator_inactive);
        
        // Set current page indicator to active
        switch (currentPage) {
            case 0:
                indicator1.setBackgroundResource(R.drawable.indicator_active);
                break;
            case 1:
                indicator2.setBackgroundResource(R.drawable.indicator_active);
                break;
            case 2:
                indicator3.setBackgroundResource(R.drawable.indicator_active);
                break;
        }
    }
    
    /**
     * Update button text based on current page
     */
    private void updateButtonText() {
        if (currentPage == TOTAL_PAGES - 1) {
            btnNext.setText("Get Started");
        } else {
            btnNext.setText("Next");
        }
    }
    
    /**
     * Setup button listeners
     */
    private void setupButtonListeners() {
        btnSkip.setOnClickListener(this);
        btnNext.setOnClickListener(this);
    }
    
    /**
     * Start animation for the first page
     */
    private void startFirstPageAnimation() {
        // Animation will be triggered by fragment lifecycle methods
        // No manual triggering needed
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSkip) {
            goToLogin();
        } else if (id == R.id.btnNext) {
            if (currentPage < TOTAL_PAGES - 1) {
                // Move to next page
                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                // Last page - go to login
                goToLogin();
            }
        }
    }
    
    @Override
    public void onAnimationComplete(int screenNumber) {
        // Auto-advance to next page after animation completes
        if (screenNumber < TOTAL_PAGES) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (screenNumber < TOTAL_PAGES) {
                        viewPager.setCurrentItem(screenNumber, true);
                    } else {
                        goToLogin();
                    }
                }
            }, 1000); // 1 second delay
        } else {
            // Last screen - go to login after delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToLogin();
                }
            }, 2000); // 2 second delay for final screen
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}


