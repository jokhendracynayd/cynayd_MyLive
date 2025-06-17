package com.livestreaming.main.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.utils.LiveStorge;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.LiveVoiceListAdapter;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.main.presenter.CheckLivePresenter;
import com.livestreaming.main.views.AbsMainHomeChildViewHolder;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/10/27.
 * 语音直播间列表
 */

public class LiveVoiceRoomListViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<LiveBean> {
    ArrayList<LiveBean> topBeans=new ArrayList<>();
    private CommonRefreshView mRefreshView;
    private LiveVoiceListAdapter mAdapter;
    private CheckLivePresenter mCheckLivePresenter;
    private View mheadView;
    private View reRank;
    private View reShop;

    public LiveVoiceRoomListViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
        Lingver.getInstance().setLocale(parentView.getContext(), Constants.CUR_LANGUAGE);
        mContext=context;
    }
    private void forwardTopRank() {
        RankActivity.forward(mContext, 0);
    }

    private void forwardShop() {
        WebViewActivity.forward(mContext, "https://donalive.net/appapi/Mall/index");
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_voice;
    }

    @Override
    public void init() {
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveBean>() {
            @Override
            public RefreshAdapter<LiveBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LiveVoiceListAdapter(mContext);
                   mheadView= mAdapter.getHeadView();

                    reRank = mheadView.findViewById(R.id.re_rank);
                    reRank.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            forwardTopRank();
                        }
                    });
                    reShop = mheadView.findViewById(R.id.re_store);
                    reShop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            forwardShop();
                        }
                    });
                    mAdapter.setOnItemClickListener(LiveVoiceRoomListViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getVoiceRoomList(p, callback);
            }

            @Override
            public List<LiveBean> processData(String[] info) {
                ArrayList<LiveBean> beans = new ArrayList<>();
                topBeans = new ArrayList<>();
                List<LiveBean> all = JSON.parseArray(Arrays.toString(info), LiveBean.class);
                if (all.size() >= 3) {
                    topBeans.add(all.get(0));
                    topBeans.add(all.get(1));
                    topBeans.add(all.get(2));
                    if (all.size() > 3) {
                        LiveBean b=new LiveBean();
                        b.headItems=topBeans;
                        beans.add(b);
                        for (int i = 3; i < all.size(); i++) {
                            beans.add(all.get(i));
                        }
                    }
                }
                if (!beans.isEmpty()) {
                    return beans;
                } else {
                    return all;
                }
            }

            @Override
            public void onRefreshSuccess(List<LiveBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }




    @Override
    public void onItemClick(LiveBean bean, int position) {
        if (((AbsActivity) mContext).checkLogin()) {
            watchLive(bean, position);
        }
    }


    /**
     * 观看直播
     */
    public void watchLive(LiveBean liveBean, int position) {
        if (!FloatWindowHelper.checkVoice(true)) {
            return;
        }
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new CheckLivePresenter(mContext);
        }
        LiveStorge.getInstance().put("liveRecommend",mAdapter.getList());
        mCheckLivePresenter.watchLive(liveBean, "liveRecommend", position);
    }


}
