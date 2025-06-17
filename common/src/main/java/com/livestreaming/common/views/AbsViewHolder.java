package com.livestreaming.common.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.yariksoffice.lingver.Lingver;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.interfaces.LifeCycleListener;
import com.livestreaming.common.utils.ClickUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.ScreenDimenUtil;

/**
 * Created by cxf on 2018/9/22.
 */

public abstract class AbsViewHolder implements LifeCycleListener {

    protected String mTag;
    protected Context mContext;
    protected ViewGroup mParentView;
    protected View mContentView;

    public AbsViewHolder(Context context, ViewGroup parentView) {
        mTag = getClass().getSimpleName();
        mContext = context;
        mParentView = parentView;

        Lingver.getInstance().setLocale(context, Constants.CUR_LANGUAGE);
        mContentView = LayoutInflater.from(context).inflate(getLayoutId(), mParentView, false);
        init();
    }

    public AbsViewHolder(Context context, ViewGroup parentView, Object... args) {
        mTag = getClass().getSimpleName();
        processArguments(args);
        mContext = context;
        mParentView = parentView;
        Lingver.getInstance().setLocale(context, Constants.CUR_LANGUAGE);
        mContentView = LayoutInflater.from(context).inflate(getLayoutId(), mParentView, false);
        init();
    }

    protected void processArguments(Object... args) {

    }

    protected abstract int getLayoutId();

    public abstract void init();

    protected <T extends View> T findViewById(int res) {
        return mContentView.findViewById(res);
    }

    public View getContentView() {
        return mContentView;
    }

    protected boolean canClick() {
        return ClickUtil.canClick();
    }

    public void addToParent() {
        if (mParentView != null && mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent != null) {
                if (parent != mParentView) {
                    ((ViewGroup) parent).removeView(mContentView);
                    mParentView.addView(mContentView);
                }
            } else {
                mParentView.addView(mContentView);
            }
        }
    }

    public void removeFromParent() {
        if (mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mContentView);
            }
        }
    }

    /**
     * 订阅Activity的生命周期
     */
    public void subscribeActivityLifeCycle() {
        if (mContext instanceof AbsActivity) {
            ((AbsActivity) mContext).addLifeCycleListener(this);
        }
    }

    /**
     * 取消订阅Activity的生命周期
     */
    public void unSubscribeActivityLifeCycle() {
        if (mContext instanceof AbsActivity) {
            ((AbsActivity) mContext).removeLifeCycleListener(this);
        }
    }


    public void finishAcitivty() {
        if (mContext != null && mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        L.e(mTag, "release-------->");
    }

    @Override
    public void onCreate() {
        L.e(mTag, "lifeCycle-----onCreate----->");
    }

    @Override
    public void onStart() {
        L.e(mTag, "lifeCycle-----onStart----->");
    }

    @Override
    public void onReStart() {
        L.e(mTag, "lifeCycle-----onReStart----->");
    }

    @Override
    public void onResume() {
        L.e(mTag, "lifeCycle-----onResume----->");
    }

    @Override
    public void onPause() {
        L.e(mTag, "lifeCycle-----onPause----->");
    }

    @Override
    public void onStop() {
        L.e(mTag, "lifeCycle-----onStop----->");
    }

    @Override
    public void onDestroy() {
        L.e(mTag, "lifeCycle-----onDestroy----->");
    }


    /**
     * 根据不同手机的状态栏设置高度
     */
    protected void setStatusHeight() {
        View flTop = findViewById(R.id.fl_top);
        if (flTop != null) {
            flTop.setPadding(0, ScreenDimenUtil.getInstance().getStatusBarHeight(), 0, 0);
        }
    }

}
