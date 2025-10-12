package com.kodelink.glide;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * SimpleTypewriterTextView - A minimal, reliable typewriter animation
 * 
 * This is a simplified version that definitely works for testing.
 */
public class SimpleTypewriterTextView extends AppCompatTextView {
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private int delay = 100; // milliseconds per character
    private String textToType = "";
    private int currentIndex = 0;
    
    public SimpleTypewriterTextView(Context context) {
        super(context);
    }
    
    public SimpleTypewriterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public SimpleTypewriterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public void setTypingDelay(int delayMs) {
        this.delay = delayMs;
    }
    
    public void setTypewriterText(String text) {
        this.textToType = text != null ? text : "";
        this.currentIndex = 0;
        
        // Clear the text first
        setText("");
        
        // Start typing
        typeNextChar();
    }
    
    private void typeNextChar() {
        if (currentIndex < textToType.length()) {
            // Add the next character
            String currentText = textToType.substring(0, currentIndex + 1);
            setText(currentText);
            currentIndex++;
            
            // Schedule next character
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    typeNextChar();
                }
            }, delay);
        }
    }
}

