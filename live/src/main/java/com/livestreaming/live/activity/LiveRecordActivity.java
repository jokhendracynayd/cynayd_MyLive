package com.livestreaming.live.activity;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.views.LiveRecordViewHolder;

/**
 * Created by cxf on 2018/9/30.
 */

public class LiveRecordActivity extends AbsActivity {

    public static void forward(Context context, UserBean userBean) {
        if (userBean == null) {
            return;
        }
        Intent intent = new Intent(context, LiveRecordActivity.class);
        intent.putExtra(Constants.USER_BEAN, userBean);
        context.startActivity(intent);
    }

    private UserBean mUserBean;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_record;
    }

    @Override
    protected void main() {
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
        );
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.live_record));
        mUserBean = getIntent().getParcelableExtra(Constants.USER_BEAN);
        if (mUserBean == null) {
            return;
        }
        LiveRecordViewHolder liveRecordViewHolder = new LiveRecordViewHolder(mContext, (ViewGroup) findViewById(R.id.container),mUserBean.getId());
        liveRecordViewHolder.setActionListener(new LiveRecordViewHolder.ActionListener() {
            @Override
            public UserBean getUserBean() {
                return mUserBean;
            }
        });
        liveRecordViewHolder.addToParent();
        liveRecordViewHolder.subscribeActivityLifeCycle();
        liveRecordViewHolder.loadData();
    }
}
