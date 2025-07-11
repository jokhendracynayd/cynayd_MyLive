package com.livestreaming.common.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.yariksoffice.lingver.Lingver;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.utils.ScreenDimenUtil;

/**
 * Created by cxf on 2018/10/15.
 */

public abstract class AbsLivePageViewHolder extends AbsViewHolder implements View.OnClickListener {

    protected ObjectAnimator mEnterAnimator;
    protected ObjectAnimator mOutAnimator;
    protected boolean mLoad;
    protected boolean mShowed;
    protected boolean mAnimating;

    public AbsLivePageViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
        Lingver.getInstance().setLocale(context, Constants.CUR_LANGUAGE);
    }

    public AbsLivePageViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
        Lingver.getInstance().setLocale(context, Constants.CUR_LANGUAGE);
    }


    @Override
    public void init() {

        setStatusHeight();
        int screenWidth = ScreenDimenUtil.getInstance().getScreenWidth();
        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        mEnterAnimator = ObjectAnimator.ofFloat(mContentView, "translationX", screenWidth, 0);
        mEnterAnimator.setDuration(200);
        mEnterAnimator.setInterpolator(interpolator);
        mEnterAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = true;
                onShow();
                loadData();
            }

        });
        mOutAnimator = ObjectAnimator.ofFloat(mContentView, "translationX", 0, screenWidth);
        mOutAnimator.setDuration(200);
        mOutAnimator.setInterpolator(interpolator);
        mOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = false;
                onHide();
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(this);
    }

    public abstract void loadData();

    public void show() {
        if (!mAnimating) {
            mAnimating = true;
            mEnterAnimator.start();
        }
    }

    public void hide() {
        if (!mAnimating) {
            mAnimating = true;
            mOutAnimator.start();
        }
    }

    public void onShow() {

    }

    public void onHide() {

    }

    public boolean isShowed() {
        return mShowed;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_back) {
            hide();
        }
    }

    public void release() {
        if (mEnterAnimator != null) {
            mEnterAnimator.cancel();
        }
        mEnterAnimator = null;
        if (mOutAnimator != null) {
            mOutAnimator.cancel();
        }
        mEnterAnimator = null;
    }

}
