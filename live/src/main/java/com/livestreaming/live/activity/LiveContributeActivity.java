package com.livestreaming.live.activity;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.live.R;
import com.livestreaming.live.views.LiveContributeViewHolder;

/**
 * Created by cxf on 2018/10/19.
 */

public class LiveContributeActivity extends AbsActivity {

    private LiveContributeViewHolder mLiveContributeViewHolder;
    private String mLiveUid;

    @Override
    protected int getLayoutId() {
        return com.livestreaming.common.R.layout.activity_empty;
    }

    @Override
    protected void main() {
        mLiveUid = getIntent().getStringExtra(Constants.TO_UID);
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        mLiveContributeViewHolder = new LiveContributeViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        mLiveContributeViewHolder.addToParent();
        mLiveContributeViewHolder.loadData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mLiveContributeViewHolder != null) {
            mLiveContributeViewHolder.release();
        }
    }

    public String getLiveUid() {
        return mLiveUid;
    }
}
