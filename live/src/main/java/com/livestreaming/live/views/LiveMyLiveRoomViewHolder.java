package com.livestreaming.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.views.AbsCommonViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAdminListActivity;
import com.livestreaming.live.activity.LiveBlackActivity;
import com.livestreaming.live.activity.LiveShutUpActivity;

/**
 * Created by cxf on 2019/4/23.
 * 我的直播间
 */

public class LiveMyLiveRoomViewHolder extends AbsCommonViewHolder implements View.OnClickListener {

    public LiveMyLiveRoomViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_my_live_room;
    }

    @Override
    public void init() {
        findViewById(R.id.btn_admin).setOnClickListener(this);
        findViewById(R.id.btn_user_shut_up).setOnClickListener(this);
        findViewById(R.id.btn_user_black).setOnClickListener(this);
    }

    @Override
    public void loadData() {
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_admin) {
            LiveAdminListActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
        } else if (i == R.id.btn_user_shut_up) {
            LiveShutUpActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
        } else if (i == R.id.btn_user_black) {
            LiveBlackActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
        }
    }
}
