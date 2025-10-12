package com.kodelink.glide;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * TypewriterTextView - Custom TextView with typewriter animation effect
 * 
 * This class provides a typewriter animation effect where text appears
 * character by character with a configurable delay between each character.
 * 
 * Features:
 * - Configurable typing speed (delay per character)
 * - Smooth character-by-character animation
 * - Callback support for animation completion
 * - Pause and resume functionality
 * - Thread-safe implementation using Handler
 * 
 * Usage:
 * 1. Set the text using setText() or setTypewriterText()
 * 2. Call startTypewriterAnimation() to begin the animation
 * 3. Optionally set a callback to be notified when animation completes
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class TypewriterTextView extends AppCompatTextView {
    
    // Default typing speed (delay in milliseconds per character)
    private static final int DEFAULT_TYPING_DELAY = 50;
    
    // Handler for managing the animation timing
    private Handler handler;
    
    // Current typing delay in milliseconds
    private int typingDelay = DEFAULT_TYPING_DELAY;
    
    // The full text to be animated
    private String fullText = "";
    
    // Current position in the text
    private int currentIndex = 0;
    
    // Flag to control animation state
    private boolean isAnimating = false;
    private boolean isPaused = false;
    
    // Callback interface for animation events
    public interface TypewriterCallback {
        void onAnimationComplete();
        void onCharacterTyped(char character, int position);
    }
    
    private TypewriterCallback callback;
    
    /**
     * Constructor for programmatic creation
     */
    public TypewriterTextView(Context context) {
        super(context);
        init();
    }
    
    /**
     * Constructor for XML inflation
     */
    public TypewriterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * Constructor for XML inflation with style
     */
    public TypewriterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * Initialize the TypewriterTextView
     */
    private void init() {
        handler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Set the typing speed (delay per character in milliseconds)
     * 
     * @param delay Delay in milliseconds between each character
     */
    public void setTypingDelay(int delay) {
        this.typingDelay = Math.max(1, delay); // Ensure minimum delay of 1ms
    }
    
    /**
     * Get the current typing delay
     * 
     * @return Current delay in milliseconds
     */
    public int getTypingDelay() {
        return typingDelay;
    }
    
    /**
     * Set the text to be animated and start the typewriter effect
     * 
     * @param text The text to animate
     */
    public void setTypewriterText(String text) {
        this.fullText = text != null ? text : "";
        this.currentIndex = 0;
        this.isAnimating = false;
        this.isPaused = false;
        
        // Clear current text
        setText("");
        
        // Start the animation immediately
        if (!fullText.isEmpty()) {
            isAnimating = true;
            typeNextCharacter();
        }
    }
    
    /**
     * Start the typewriter animation with the current text
     */
    public void startTypewriterAnimation() {
        if (fullText.isEmpty()) {
            return;
        }
        
        // If already animating, stop current animation
        if (isAnimating) {
            stopTypewriterAnimation();
        }
        
        isAnimating = true;
        isPaused = false;
        currentIndex = 0;
        
        // Start typing the first character
        typeNextCharacter();
    }
    
    /**
     * Stop the typewriter animation and show full text immediately
     */
    public void stopTypewriterAnimation() {
        isAnimating = false;
        isPaused = false;
        handler.removeCallbacksAndMessages(null);
        
        // Show full text immediately
        setText(fullText);
        currentIndex = fullText.length();
        
        // Notify callback if animation was completed
        if (callback != null && currentIndex >= fullText.length()) {
            callback.onAnimationComplete();
        }
    }
    
    /**
     * Pause the typewriter animation
     */
    public void pauseTypewriterAnimation() {
        if (isAnimating && !isPaused) {
            isPaused = true;
            handler.removeCallbacksAndMessages(null);
        }
    }
    
    /**
     * Resume the typewriter animation
     */
    public void resumeTypewriterAnimation() {
        if (isAnimating && isPaused) {
            isPaused = false;
            typeNextCharacter();
        }
    }
    
    /**
     * Check if the animation is currently running
     * 
     * @return true if animating, false otherwise
     */
    public boolean isAnimating() {
        return isAnimating && !isPaused;
    }
    
    /**
     * Check if the animation is paused
     * 
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Set a callback to be notified of animation events
     * 
     * @param callback The callback interface
     */
    public void setTypewriterCallback(TypewriterCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Type the next character in the animation
     */
    private void typeNextCharacter() {
        if (!isAnimating || isPaused || currentIndex >= fullText.length()) {
            // Animation complete
            if (currentIndex >= fullText.length()) {
                isAnimating = false;
                if (callback != null) {
                    callback.onAnimationComplete();
                }
            }
            return;
        }
        
        // Get the next character
        char nextChar = fullText.charAt(currentIndex);
        
        // Update the displayed text
        String currentDisplayText = fullText.substring(0, currentIndex + 1);
        super.setText(currentDisplayText, BufferType.NORMAL);
        
        // Notify callback of character typed
        if (callback != null) {
            callback.onCharacterTyped(nextChar, currentIndex);
        }
        
        // Move to next character
        currentIndex++;
        
        // Schedule next character
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                typeNextCharacter();
            }
        }, typingDelay);
    }
    
    /**
     * Override setText to prevent external text changes during animation
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        // If we're animating, only allow internal text updates
        if (isAnimating) {
            super.setText(text, type);
        } else {
            // Store the text for potential animation
            this.fullText = text != null ? text.toString() : "";
            super.setText(text, type);
        }
    }
    
    /**
     * Clean up resources when view is detached
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
