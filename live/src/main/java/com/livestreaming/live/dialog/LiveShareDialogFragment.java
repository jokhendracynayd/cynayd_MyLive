package com.livestreaming.live.dialog;


import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.adapter.OnItemClickListener;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LiveShareAdapter;
import com.livestreaming.live.adapter.MobBean;

public class LiveShareDialogFragment extends AbsDialogFragment implements OnItemClickListener<MobBean> {

    private RecyclerView mRecyclerView;
    private ActionListener mActionListener;
    private boolean mNoLink;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_share;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
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
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        LiveShareAdapter adapter = new LiveShareAdapter(mContext, mNoLink);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void notifyItemClickCallback( int position,MobBean bean) {
        if (!canClick()) {
            return;
        }
        dismiss();
        mContext = null;
        if (mActionListener != null) {
            mActionListener.onItemClick(bean.getType());
        }
    }

    public interface ActionListener {
        void onItemClick(String type);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void setNoLink(boolean noLink) {
        mNoLink = noLink;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActionListener = null;
        mContext = null;
    }
}
