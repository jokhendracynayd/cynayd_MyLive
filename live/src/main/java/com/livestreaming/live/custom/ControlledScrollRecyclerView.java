package com.livestreaming.live.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.recyclerview.widget.RecyclerView;

public class ControlledScrollRecyclerView extends RecyclerView {

    private boolean isUserScrollEnabled = true;

    public ControlledScrollRecyclerView(Context context) {
        super(context);
    }

    public ControlledScrollRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlledScrollRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Disable touch interception based on the flag
        return isUserScrollEnabled && super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Disable touch events based on the flag
        return isUserScrollEnabled && super.onTouchEvent(e);
    }

    // Setter to enable or disable user scroll
    public void setUserScrollEnabled(boolean enabled) {
        isUserScrollEnabled = enabled;
    }
}
