package com.livestreaming.main.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.ActiveAdapter;
import com.livestreaming.main.bean.ActiveBean;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 首页 动态 推荐
 */
public class MainActiveRecommendViewHolder extends AbsMainActiveViewHolder {

    public MainActiveRecommendViewHolder(Context context, ViewGroup parentView) {
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
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_active_home);
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
                MainHttpUtil.getActiveRecommend(p, callback);
            }

            @Override
            public List<ActiveBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), ActiveBean.class);
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
        MainHttpUtil.cancel(MainHttpConsts.GET_ACTIVE_RECOMMEND);

    }

}
