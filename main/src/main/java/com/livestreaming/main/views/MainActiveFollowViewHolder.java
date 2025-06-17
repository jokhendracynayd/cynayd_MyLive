package com.livestreaming.main.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.ActiveAdapter;
import com.livestreaming.main.bean.ActiveBean;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 首页 动态 关注
 */
public class MainActiveFollowViewHolder extends AbsMainActiveViewHolder {

    private List<ActiveBean> mEmptyList;
    private View mNoDataView;
    private View mGroupHasLogin;//已登录
    private View mGroupNotLogin;//未登录

    public MainActiveFollowViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_active_recommend;
    }

    @Override
    public void init() {
        super.init();
        mRefreshView = findViewById(R.id.refreshView);
        mNoDataView = LayoutInflater.from(mContext).inflate(R.layout.view_no_data_active_follow, mRefreshView.getmEmptyLayout(), false);
        mGroupHasLogin = mNoDataView.findViewById(R.id.group_has_login);
        mGroupNotLogin = mNoDataView.findViewById(R.id.group_not_login);
        mGroupNotLogin.findViewById(com.livestreaming.common.R.id.btn_to_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteUtil.forwardLogin(mContext);
            }
        });
        mRefreshView.setEmptyLayoutView(mNoDataView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<ActiveBean>() {
            @Override
            public RefreshAdapter<ActiveBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new ActiveAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (CommonAppConfig.getInstance().isLogin()) {
                    if (mGroupHasLogin != null && mGroupHasLogin.getVisibility() != View.VISIBLE) {
                        mGroupHasLogin.setVisibility(View.VISIBLE);
                    }
                    if (mGroupNotLogin != null && mGroupNotLogin.getVisibility() == View.VISIBLE) {
                        mGroupNotLogin.setVisibility(View.INVISIBLE);
                    }
                    MainHttpUtil.getActiveFollow(p, callback);
                } else {
                    if (mGroupNotLogin != null && mGroupNotLogin.getVisibility() != View.VISIBLE) {
                        mGroupNotLogin.setVisibility(View.VISIBLE);
                    }
                    if (mGroupHasLogin != null && mGroupHasLogin.getVisibility() == View.VISIBLE) {
                        mGroupHasLogin.setVisibility(View.INVISIBLE);
                    }
                    callback.onSuccess(Constants.ZERO, Constants.EMPTY_STRING, Constants.EMPTY_STRING_ARR);
                    callback.onFinish();
                }
            }

            @Override
            public List<ActiveBean> processData(String[] info) {
                if (CommonAppConfig.getInstance().isLogin()) {
                    return JSON.parseArray(Arrays.toString(info), ActiveBean.class);
                }
                if (mEmptyList == null) {
                    mEmptyList = new ArrayList<>();
                }
                return mEmptyList;
            }

            @Override
            public void onRefreshSuccess(List<ActiveBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<ActiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }


    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.release();
        }
        MainHttpUtil.cancel(MainHttpConsts.GET_ACTIVE_FOLLOW);
    }

}
