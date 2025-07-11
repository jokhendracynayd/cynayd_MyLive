package com.livestreaming.main.views;

import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.activity.ActiveAllTopicActivity;
import com.livestreaming.main.activity.ActiveSearchTopicActivity;
import com.livestreaming.main.adapter.ActiveHotTopicAdapter;
import com.livestreaming.main.bean.ActiveTopicBean;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.mall.http.MallHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 首页 动态
 */

public class MainActiveViewHolder extends AbsMainHomeParentViewHolder implements View.OnClickListener {

    private AbsMainActiveViewHolder[] mActiveViewHolders;
    private MainActiveRecommendViewHolder mRecommendViewHolder;
    private MainActiveFollowViewHolder mFollowViewHolder;
    private MainActiveNewestViewHolder mNewsetViewHolder;
    private RecyclerView mRecyclerViewTopic;//话题
    private ActiveHotTopicAdapter mHotTopicAdapter;

    public MainActiveViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public void init() {
        setStatusHeight();
        mActiveViewHolders = new AbsMainActiveViewHolder[3];
        super.init();
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.btn_all_topic).setOnClickListener(this);
        mRecyclerViewTopic = findViewById(R.id.recyclerView_topic);
        mRecyclerViewTopic.setHasFixedSize(true);
        mRecyclerViewTopic.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_active;
    }

    @Override
    protected void loadPageData(int position) {
        if (mActiveViewHolders == null) {
            return;
        }
        stopActiveVoice();
        AbsMainActiveViewHolder vh = mActiveViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {

                    mNewsetViewHolder = new MainActiveNewestViewHolder(mContext, parent);
                    vh = mNewsetViewHolder;
                } else if (position == 1) {
                    mFollowViewHolder = new MainActiveFollowViewHolder(mContext, parent);
                    vh = mFollowViewHolder;
                } else if (position == 2) {
                    mRecommendViewHolder = new MainActiveRecommendViewHolder(mContext, parent);
                    vh = mRecommendViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mActiveViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }

    @Override
    protected int getPageCount() {
        return 3;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{
                WordUtil.getString(com.livestreaming.common.R.string.newest),
                WordUtil.getString(com.livestreaming.common.R.string.recommend),
                WordUtil.getString(com.livestreaming.common.R.string.follow)
        };
    }

    @Override
    public void loadData() {
        getHotTopic();
        if (mViewPager != null) {
            loadPageData(mViewPager.getCurrentItem());
        }
    }


    @Override
    public void setShowed(boolean showed) {
        super.setShowed(showed);
//        L.e("MainActiveViewHolder----showed-----> "+showed);
        stopActiveVoice();
    }


    /**
     * 停止播放动态声音
     */
    private void stopActiveVoice() {
        if (mActiveViewHolders != null) {
            for (AbsMainActiveViewHolder vh : mActiveViewHolders) {
                if (vh != null) {
                    vh.stopActiveVoice();
                }
            }
        }
    }

    private void getHotTopic() {
        MainHttpUtil.getActiveHotTopic(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (mRecyclerViewTopic != null) {
                        List<ActiveTopicBean> list = JSON.parseArray(Arrays.toString(info), ActiveTopicBean.class);
                        if (mHotTopicAdapter == null) {
                            mHotTopicAdapter = new ActiveHotTopicAdapter(mContext, list);
                            mRecyclerViewTopic.setAdapter(mHotTopicAdapter);
                        } else {
                            mHotTopicAdapter.refreshData(list);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        MallHttpUtil.cancel(MainHttpConsts.GET_ACTIVE_HOT_TOPIC);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_search) {
            if (mContext != null) {
                mContext.startActivity(new Intent(mContext, ActiveSearchTopicActivity.class));
            }
        } else if (i == R.id.btn_all_topic) {
            if (mContext != null) {
                mContext.startActivity(new Intent(mContext, ActiveAllTopicActivity.class));
            }
        }
    }

}
