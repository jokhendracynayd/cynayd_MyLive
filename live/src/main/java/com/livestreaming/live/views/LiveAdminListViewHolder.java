package com.livestreaming.live.views;

import android.app.Dialog;
import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.common.views.AbsLivePageViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAdminListActivity;
import com.livestreaming.live.adapter.LiveAdminListAdapter;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.List;

/**
 * Created by cxf on 2018/10/16.
 */

public class LiveAdminListViewHolder extends AbsLivePageViewHolder implements OnItemClickListener<UserBean> {

    private String mLiveUid;
    private TextView mTextView;
    private RecyclerView mRecyclerView;
    private LiveAdminListAdapter mLiveAdminListAdapter;
    private HttpCallback mHttpCallback;
    private String mTotalCount;


    public LiveAdminListViewHolder(Context context, ViewGroup parentView, String liveUid) {
        super(context, parentView);
        mLiveUid = liveUid;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_admin_list;
    }

    @Override
    public void init() {
        super.init();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mTextView = (TextView) findViewById(R.id.text);
        mHttpCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    List<UserBean> list = JSON.parseArray(obj.getString("list"), UserBean.class);
                    if (mLiveAdminListAdapter == null) {
                        mLiveAdminListAdapter = new LiveAdminListAdapter(mContext, list);
                        mLiveAdminListAdapter.setOnItemClickListener(LiveAdminListViewHolder.this);
                        mRecyclerView.setAdapter(mLiveAdminListAdapter);
                    } else {
                        mLiveAdminListAdapter.setList(list);
                    }
                    mTotalCount = obj.getString("total");
                    showTip();
                }
            }
        };
    }

    private void showTip() {
        mTextView.setText(WordUtil.getString(com.livestreaming.common.R.string.live_admin_count) + "(" + mLiveAdminListAdapter.getItemCount() + "/" + mTotalCount + ")");
    }


    @Override
    public void loadData() {
        LiveHttpUtil.getAdminList(mLiveUid, mHttpCallback);
    }

    @Override
    public void hide() {
        if (mContext instanceof LiveAdminListActivity) {
            ((LiveAdminListActivity) mContext).onBackPressed();
        } else {
            super.hide();
        }
    }

    @Override
    public void onHide() {
        if (mLiveAdminListAdapter != null) {
            mLiveAdminListAdapter.clear();
        }
    }

    @Override
    public void onItemClick(final UserBean bean, int position) {
        DialogUitl.showSimpleDialogDark(mContext, String.format(WordUtil.getString(com.livestreaming.common.R.string.live_setting_tip_1), bean.getUserNiceName()), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                LiveHttpUtil.setAdmin(mLiveUid, bean.getId(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            int res = JSON.parseObject(info[0]).getIntValue("isadmin");
                            if (res == 0) {//被取消管理员
                                if (mLiveAdminListAdapter != null) {
                                    mLiveAdminListAdapter.removeItem(bean.getId());
                                    showTip();
                                }
                                if (mContext instanceof LiveActivity) {
                                    ((LiveActivity) mContext).sendSetAdminMessage(0, bean.getId(), bean.getUserNiceName());
                                }
                            }
                        }
                    }
                });
            }
        });

    }

    @Override
    public void release() {
        if (mLiveAdminListAdapter != null) {
            mLiveAdminListAdapter.release();
        }
        mLiveAdminListAdapter = null;
        super.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveHttpUtil.cancel(LiveHttpConsts.GET_ADMIN_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_ADMIN);
    }
}
