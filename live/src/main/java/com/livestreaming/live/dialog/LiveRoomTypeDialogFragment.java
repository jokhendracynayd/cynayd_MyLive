package com.livestreaming.live.dialog;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.livestreaming.common.Constants;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LiveRoomTypeAdapter;
import com.livestreaming.live.bean.LiveRoomTypeBean;

/**
 * Created by cxf on 2018/10/8.
 */

public class LiveRoomTypeDialogFragment extends AbsDialogFragment implements OnItemClickListener<LiveRoomTypeBean> {

    private RecyclerView mRecyclerView;
    private LiveRoomTypeAdapter mAdapter;
    private CommonCallback<LiveRoomTypeBean> mCallback;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_room_type;
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
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DpUtil.dp2px(80);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        int checkedId = bundle.getInt(Constants.CHECKED_ID, Constants.LIVE_TYPE_NORMAL);
        mAdapter = new LiveRoomTypeAdapter(mContext, checkedId);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(LiveRoomTypeBean bean, int position) {
        dismiss();
        if (mCallback != null) {
            mCallback.callback(bean);
        }
    }

    public void setCallback(CommonCallback<LiveRoomTypeBean> callback) {
        mCallback = callback;
    }

    @Override
    public void onDestroy() {
        mCallback = null;
        super.onDestroy();
    }
}
