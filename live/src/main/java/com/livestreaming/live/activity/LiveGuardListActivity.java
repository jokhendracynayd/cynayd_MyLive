package com.livestreaming.live.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.live.R;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.adapter.GuardAdapter;
import com.livestreaming.live.bean.GuardUserBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/15.
 */

public class LiveGuardListActivity extends AbsActivity {

    public static void forward(Context context, String toUid) {
        Intent intent = new Intent(context, LiveGuardListActivity.class);
        intent.putExtra(Constants.TO_UID, toUid);
        context.startActivity(intent);
    }

    private CommonRefreshView mRefreshView;
    private GuardAdapter mGuardAdapter;
    private String mToUid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_guard_list;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.guard_list));
        mToUid = getIntent().getStringExtra(Constants.TO_UID);
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        mRefreshView =  findViewById(R.id.refreshView);
        boolean self = mToUid.equals(CommonAppConfig.getInstance().getUid());
        mRefreshView.setEmptyLayoutId(self ? R.layout.view_no_data_guard_anc_2 : R.layout.view_no_data_guard_aud_2);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GuardUserBean>() {
            @Override
            public RefreshAdapter<GuardUserBean> getAdapter() {
                if (mGuardAdapter == null) {
                    mGuardAdapter = new GuardAdapter(mContext, false);
                }
                return mGuardAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getGuardList(mToUid, p, callback);
            }

            @Override
            public List<GuardUserBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), GuardUserBean.class);
            }

            @Override
            public void onRefreshSuccess(List<GuardUserBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<GuardUserBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_GUARD_LIST);
        super.onDestroy();
    }
}
