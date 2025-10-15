package com.kodelink.glide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * OnboardingFragment - Reusable fragment for onboarding screens
 * 
 * This fragment handles individual onboarding screens with typewriter animations
 * for both title and description text. It provides a callback interface for
 * when the animation completes to enable auto-advance functionality.
 * 
 * Features:
 * - Typewriter animation for title and description
 * - Configurable content (title, description, image)
 * - Animation completion callback
 * - Fallback support for regular TextViews
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class OnboardingFragment extends Fragment {
    
    // Fragment arguments keys
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE_RESOURCE = "image_resource";
    private static final String ARG_SCREEN_NUMBER = "screen_number";
    
    // Typewriter animation speed (milliseconds per character)
    private static final int TYPING_SPEED = 50;
    
    // Views
    private SimpleTypewriterTextView tvTitle;
    private SimpleTypewriterTextView tvDescription;
    private ImageView ivIllustration;
    
    // Animation state
    private boolean hasAnimationStarted = false;
    private String pendingTitle = "";
    private String pendingDescription = "";
    private int pendingScreenNumber = 1;
    
    // Callback interface for animation completion
    public interface OnAnimationCompleteListener {
        void onAnimationComplete(int screenNumber);
    }
    
    private OnAnimationCompleteListener animationCompleteListener;
    
    /**
     * Create a new instance of OnboardingFragment
     */
    public static OnboardingFragment newInstance(String title, String description, int imageResource, int screenNumber) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_IMAGE_RESOURCE, imageResource);
        args.putInt(ARG_SCREEN_NUMBER, screenNumber);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivIllustration = view.findViewById(R.id.ivIllustration);
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            pendingTitle = args.getString(ARG_TITLE, "");
            pendingDescription = args.getString(ARG_DESCRIPTION, "");
            int imageResource = args.getInt(ARG_IMAGE_RESOURCE, 0);
            pendingScreenNumber = args.getInt(ARG_SCREEN_NUMBER, 1);
            
            // Set image immediately
            if (imageResource != 0) {
                ivIllustration.setImageResource(imageResource);
            }
            
            // Don't start animation yet - wait for fragment to become visible
        }
    }
    
    /**
     * Set the animation complete listener
     */
    public void setOnAnimationCompleteListener(OnAnimationCompleteListener listener) {
        this.animationCompleteListener = listener;
    }
    
    /**
     * Start the animation when fragment becomes visible
     */
    public void startAnimationIfVisible() {
        if (!hasAnimationStarted && !pendingTitle.isEmpty()) {
            hasAnimationStarted = true;
            startTypewriterAnimation(pendingTitle, pendingDescription, pendingScreenNumber);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Start animation when fragment resumes (becomes visible)
        startAnimationIfVisible();
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !hasAnimationStarted) {
            // Fragment is now visible, start animation
            startAnimationIfVisible();
        }
    }
    
    /**
     * Start the typewriter animation sequence
     */
    private void startTypewriterAnimation(String title, String description, int screenNumber) {
        // Animate title first
        if (tvTitle != null) {
            tvTitle.setTypingDelay(TYPING_SPEED);
            tvTitle.setTypewriterCallback(new SimpleTypewriterTextView.TypewriterCallback() {
                @Override
                public void onAnimationComplete() {
                    // Start description animation after title completes
                    startDescriptionAnimation(description, screenNumber);
                }
            });
            tvTitle.setTypewriterText(title);
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            TextView regularTitleView = getView().findViewById(R.id.tvTitle);
            if (regularTitleView != null) {
                regularTitleView.setText(title);
            }
            // Start description immediately if title fails
            startDescriptionAnimation(description, screenNumber);
        }
    }
    
    /**
     * Start description animation
     */
    private void startDescriptionAnimation(String description, int screenNumber) {
        if (tvDescription != null) {
            tvDescription.setTypingDelay(TYPING_SPEED);
            tvDescription.setTypewriterCallback(new SimpleTypewriterTextView.TypewriterCallback() {
                @Override
                public void onAnimationComplete() {
                    // Notify parent that animation is complete
                    if (animationCompleteListener != null) {
                        animationCompleteListener.onAnimationComplete(screenNumber);
                    }
                }
            });
            tvDescription.setTypewriterText(description);
        } else {
            // Fallback: if SimpleTypewriterTextView fails, use regular TextView
            TextView regularDescView = getView().findViewById(R.id.tvDescription);
            if (regularDescView != null) {
                regularDescView.setText(description);
            }
            // Notify completion immediately if animation fails
            if (animationCompleteListener != null) {
                animationCompleteListener.onAnimationComplete(screenNumber);
            }
        }
    }
}
