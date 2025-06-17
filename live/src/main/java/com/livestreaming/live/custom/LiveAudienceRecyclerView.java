package com.livestreaming.live.custom;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.livestreaming.live.R;

public class LiveAudienceRecyclerView extends RecyclerView {

    private float mScale;
    private int mLeft;
    private int mTop;
    private int mTop2;
    private int mRight;
    private int mBottom;
    private int mBottom2;
    private boolean isUserScrollEnabled=true;

    public LiveAudienceRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public LiveAudienceRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveAudienceRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScale = dm.density;
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        mLeft = dp2px(10);
        mBottom2=screenHeight-dp2px(400);
        mTop2=dp2px(100);
        mBottom = screenHeight - dp2px(50);
        mRight = screenWidth - dp2px(100);
        mTop = mBottom - dp2px(200);
    }

    private int dp2px(int dpVal) {
        return (int) (mScale * dpVal + 0.5f);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if(!isUserScrollEnabled){
            return false;
        }
        float x = e.getRawX();
        float y = e.getRawY();
        if (x >= mLeft && x <= mRight && y >= mTop && y <= mBottom) {
            return false;
        }
        if (x >= mLeft && x <= mRight && y >= mTop2 && y <= mBottom2) {
            return false;
        }
        return super.onInterceptTouchEvent(e);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Disable touch events based on the flag
        if(!isUserScrollEnabled){
            return false;
        }
        float x = e.getRawX();
        float y = e.getRawY();
        if (x >= mLeft && x <= mRight && y >= mTop&& y <= mBottom) {
            return false;
        }
        if (x >= mLeft && x <= mRight && y >= mTop2 && y <= mBottom2) {
            return false;
        }
        return  super.onTouchEvent(e);
    }

    // Setter to enable or disable user scroll
    public void setUserScrollEnabled(boolean enabled) {
        isUserScrollEnabled = enabled;
    }
}
