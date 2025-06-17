package com.livestreaming.common.views;

import android.content.Context;
import android.view.ViewGroup;

import com.yariksoffice.lingver.Lingver;
import com.livestreaming.common.Constants;

/**
 * Created by cxf on 2018/10/26.
 */

public abstract class AbsCommonViewHolder extends AbsViewHolder {

    protected boolean mFirstLoadData = true;
    private boolean mShowed;

    public AbsCommonViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);

        Lingver.getInstance().setLocale(context, Constants.CUR_LANGUAGE);
    }

    public AbsCommonViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
        Lingver.getInstance().setLocale(context, Constants.CUR_LANGUAGE);
    }

    public void loadData() {
    }

    protected boolean isFirstLoadData() {
        if (mFirstLoadData) {
            mFirstLoadData = false;
            return true;
        }
        return false;
    }


    public void setShowed(boolean showed) {
        mShowed = showed;
    }

    public boolean isShowed() {
        return mShowed;
    }
}
