package com.livestreaming.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import com.livestreaming.common.R;

/**
 * Created by cxf on 2018/6/9.
 * 可以禁止滑动的ViewPager
 */

public class MyViewPager3 extends ViewPager {

    private boolean mCanScroll;

    public MyViewPager3(Context context) {
        this(context, null);
    }

    public MyViewPager3(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyViewPager);
        mCanScroll = ta.getBoolean(R.styleable.MyViewPager_canScroll, true);
        ta.recycle();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (mCanScroll) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mCanScroll) {
            try {
                return super.onTouchEvent(ev);
            } catch (Exception e) {
                return false;
            }
        } else {

            return false;
        }
    }
    public void setCanScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }

}
