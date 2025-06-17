package com.livestreaming.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LiveShutUpAdapter;
import com.livestreaming.live.bean.LiveShutUpBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2019/4/27.
 * 直播间禁言用户列表
 */

public class LiveShutUpActivity extends AbsActivity implements OnItemClickListener<LiveShutUpBean> {


    public static void forward(Context context, String liveUid) {
        Intent intent = new Intent(context, LiveShutUpActivity.class);
        intent.putExtra(Constants.LIVE_UID, liveUid);
        context.startActivity(intent);
    }

    private CommonRefreshView mRefreshView;
    private LiveShutUpAdapter mAdapter;
    private String mLiveUid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shut_up;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.live_user_shut_up_list));
        mLiveUid = getIntent().getStringExtra(Constants.LIVE_UID);
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_shut_up);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveShutUpBean>() {
            @Override
            public RefreshAdapter<LiveShutUpBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LiveShutUpAdapter(mContext);
                    mAdapter.setOnItemClickListener(LiveShutUpActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getLiveShutUpList(mLiveUid, p, callback);
            }

            @Override
            public List<LiveShutUpBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), LiveShutUpBean.class);
            }

            @Override
            public void onRefreshSuccess(List<LiveShutUpBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LiveShutUpBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    public void onItemClick(final LiveShutUpBean bean, int position) {
        DialogUitl.showSimpleDialogDark(mContext, WordUtil.getString(com.livestreaming.common.R.string.live_setting_tip_2), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                LiveHttpUtil.liveCancelShutUp(mLiveUid, bean.getUid(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (mAdapter != null) {
                                mAdapter.removeItem(bean.getUid());
                            }
                        }
                        ToastUtil.show(msg);
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_SHUT_UP_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_CANCEL_SHUT_UP);
        super.onDestroy();
    }
}
