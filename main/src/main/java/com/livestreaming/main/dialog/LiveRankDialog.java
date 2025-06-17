package com.livestreaming.main.dialog;


import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.main.R;
import com.livestreaming.main.views.MainListViewHolder;


/**
 * 游戏
 */
public class LiveRankDialog extends AbsDialogFragment {

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_rank;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {

        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 1.0); // 宽度设置为屏幕的1.0
        lp.height = (int) (d.heightPixels * 0.7); // 高度设置为屏幕的1.0
        window.setAttributes(lp);
        window.setLayout(lp.width, lp.height);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainListViewHolder viewHolder = new MainListViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        viewHolder.addToParent();
        viewHolder.subscribeActivityLifeCycle();
        viewHolder.loadData(0);
    }


}
