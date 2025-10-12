package com.kodelink.glide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * OnboardingPagerAdapter - ViewPager2 adapter for onboarding screens
 * 
 * This adapter manages the three onboarding fragments and provides
 * smooth transitions between screens. It creates fragments with
 * appropriate content for each onboarding step.
 * 
 * Features:
 * - Three onboarding screens
 * - Fragment-based architecture
 * - Smooth ViewPager2 transitions
 * - Configurable content per screen
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class OnboardingPagerAdapter extends FragmentStateAdapter {
    
    // Onboarding content data
    private static final String[] TITLES = {
        "Welcome to Swift Ride",
        "Live Location Tracking", 
        "Join Swift Ride Today"
    };
    
    private static final String[] DESCRIPTIONS = {
        "Experience seamless transportation with Swift Ride for all your travel needs.",
        "Stay connected with real-time location tracking and accurate arrival times.",
        "Ready to get started? Join thousands of users who trust Swift Ride!"
    };
    
    private static final int[] IMAGE_RESOURCES = {
        R.drawable.onboarding1,
        R.drawable.onboarding2,
        R.drawable.onboarding3
    };
    
    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create fragment with appropriate content based on position
        return OnboardingFragment.newInstance(
            TITLES[position],
            DESCRIPTIONS[position], 
            IMAGE_RESOURCES[position],
            position + 1 // Screen number (1-based)
        );
    }
    
    @Override
    public int getItemCount() {
        return TITLES.length;
    }
}
