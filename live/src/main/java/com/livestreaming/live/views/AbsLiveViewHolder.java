package com.livestreaming.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;

/**
 * Created by cxf on 2018/10/9.
 */

public abstract class AbsLiveViewHolder extends AbsViewHolder implements View.OnClickListener {

    private TextView mRedPoint;

    public AbsLiveViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public void init() {
        findViewById(R.id.btn_chat).setOnClickListener(this);
        findViewById(R.id.btn_msg).setOnClickListener(this);
        mRedPoint = (TextView) findViewById(R.id.red_point);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_msg) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openChatListWindow();

        } else if (i == R.id.btn_chat) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openChatWindow(null);
        }
    }

    public void setUnReadCount(String unReadCount) {
        if (mRedPoint != null) {
            if ("0".equals(unReadCount)) {
                if (mRedPoint.getVisibility() == View.VISIBLE) {
                    mRedPoint.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mRedPoint.getVisibility() != View.VISIBLE) {
                    mRedPoint.setVisibility(View.VISIBLE);
                }
            }
            mRedPoint.setText(unReadCount);
        }
    }


}
