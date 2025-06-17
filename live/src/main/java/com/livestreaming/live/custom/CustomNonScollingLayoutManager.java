package com.livestreaming.live.custom;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

public class CustomNonScollingLayoutManager extends LinearLayoutManager {

   public  boolean isCanScroll=true;

    public CustomNonScollingLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean canScrollVertically() {
        return isCanScroll && super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        return isCanScroll && super.canScrollHorizontally();
    }
}
