package com.livestreaming.beauty.views;

import android.content.Context;
import android.view.ViewGroup;

import com.livestreaming.beauty.R;
import com.livestreaming.common.views.AbsCommonViewHolder;

public abstract class MhMeiYanChildViewHolder extends AbsCommonViewHolder {

    protected ActionListener mActionListener;

    public MhMeiYanChildViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_meiyan_child;
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void onProgressChanged(float rate, int progress) {

    }

    public void showSeekBar() {

    }


    public interface ActionListener {
        void changeProgress(boolean visible, int max, int progress,int text);
    }
}
