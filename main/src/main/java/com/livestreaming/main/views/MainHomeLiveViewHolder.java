package com.livestreaming.main.views;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.live.bean.ItemGuest;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.main.adapter.LiveHomeGuestsListAdapter;
import com.yariksoffice.lingver.Lingver;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.custom.ItemDecoration;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.utils.LiveStorge;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.MainHomeLiveAdapter;
import com.livestreaming.main.bean.BannerBean;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * MainActivity 首页 直播
 */

public class MainHomeLiveViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<LiveBean>, View.OnClickListener {

    private SwipeRefreshLayout mRefreshView;
    private MainHomeLiveAdapter mAdapter;
    private Banner mBanner;
    private boolean mBannerNeedUpdate;
    private List<BannerBean> mBannerList;
    private RecyclerView recyclerView;
    //    private RecyclerView mRecyclerViewVoice;
    private RecyclerView mRecycleGuestsVoice;
    public OnClickViewMoreListener onClickViewMoreListener;
    private LiveHomeGuestsListAdapter homeGuestsAdpater;
    private ArrayList<LiveBean> homeGuestsRooms;
    private ArrayList<ItemGuest> listGuests;
    private HttpCallback mGuestsCallBack;
    private int page = 1;
    private boolean isLoading;


    public MainHomeLiveViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);

        Lingver.getInstance().setLocale(parentView.getContext(), Constants.CUR_LANGUAGE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_live;
    }


    @Override
    public void init() {
        mRefreshView = findViewById(R.id.refreshView);
        recyclerView = findViewById(R.id.recyclerView_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
        mRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                isLoading = true;
                mAdapter.getList().clear();
                mAdapter.notifyDataSetChanged();
                homeGuestsRooms.clear();
                homeGuestsAdpater.notifyDataSetChanged();
                loadGuests();
                callApi();
            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 7, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        recyclerView.addItemDecoration(decoration);
        mAdapter = new MainHomeLiveAdapter(mContext, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int visibleThreshold = 5; // تحميل البيانات قبل 5 عناصر من النهاية

                if (dy > 0 && !isLoading && lastVisibleItem >= (totalItemCount - visibleThreshold)) {
                    isLoading = true;
                    page++;
                    callApi();
                }
            }
        });
        loadGuests();
        isLoading = true;
        callApi();

//        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveBean>() {
//            @Override
//            public RefreshAdapter<LiveBean> getAdapter() {
//                return null;
//            }
//
//            @Override
//            public void loadData(int p, HttpCallback callback) {
//
//            }
//
//            @Override
//            public List<LiveBean> processData(String[] info) {
//
//                return liveList;
//            }
//
//            @Override
//            public void onRefreshSuccess(List<LiveBean> list, int count) {
//                if (CommonAppConfig.LIVE_ROOM_SCROLL) {
//                    LiveStorge.getInstance().put(Constants.LIVE_HOME, list);
//                }
//                showBanner();
//            }
//
//            @Override
//            public void onRefreshFailure() {
//
//            }
//
//            @Override
//            public void onLoadMoreSuccess(List<LiveBean> loadItemList, int loadItemCount) {
//
//            }
//
//            @Override
//            public void onLoadMoreFailure() {
//
//            }
//        });
//        View headView = mAdapter.getHeadView();
//        lottieAnim = headView.findViewById(R.id.view_all_anim);
//        lottieAnim.setAnimation("lottie_view_more.json");
//        lottieAnim.playAnimation();
//        lottieAnim.loop(true);
//        headView.findViewById(R.id.view_all).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onClickViewMoreListener.onClick();
//            }
//        });
//        mVoiceEmptyTip = headView.findViewById(R.id.voice_empty_tip);
//        mLiveEmptyTip = headView.findViewById(R.id.live_empty_tip);
//        mRecyclerViewVoice = headView.findViewById(R.id.recyclerView_voice);
        mRecycleGuestsVoice = findViewById(R.id.home_guests_recycle);
        //mRecyclerViewVoice.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        mRecycleGuestsVoice.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        homeGuestsAdpater = new LiveHomeGuestsListAdapter(mContext);
        homeGuestsAdpater.setOnItemClickListener(new OnItemClickListener<ItemGuest>() {
            @Override
            public void onItemClick(ItemGuest bean, int position) {
                LiveStorge.getInstance().put("liveRecommend", homeGuestsRooms);
                watchLive(homeGuestsRooms.get(bean.roomIndex), "liveRecommend", bean.roomIndex);
            }
        });
        mRecycleGuestsVoice.setAdapter(homeGuestsAdpater);
        ItemDecoration decoration2 = new ItemDecoration(mContext, 0x00000000, 7, 7);
        decoration.setOnlySetItemOffsetsButNoDraw(true);

        mBanner = (Banner) findViewById(R.id.banner);
        mBanner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                ImgLoader.display(mContext, ((BannerBean) path).getImageUrl(), imageView);
            }
        });
        mBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int p) {

                if (((AbsActivity) mContext).checkLogin()) {
                    if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
                        return;
                    }
                    if (mBannerList != null) {
                        if (p >= 0 && p < mBannerList.size()) {
                            BannerBean bean = mBannerList.get(p);
                            if (bean != null) {
                                String link = bean.getLink();
                                if (!TextUtils.isEmpty(link)) {
                                    try {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                        ((AbsActivity) mContext).startActivity(browserIntent);
                                    } catch (Exception e) {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        });


    }

    private void callApi() {
        MainHttpUtil.getHot2(page, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                isLoading = false;
                mRefreshView.setRefreshing(false);
                onHandleSuccess(info);
            }
        });
    }

    private void onHandleSuccess(String[] info) {
        JSONObject obj = JSON.parseObject(info[0]);
        List<LiveBean> liveList = JSON.parseArray(obj.getString("list"), LiveBean.class);
        LiveStorge.getInstance().put(Constants.LIVE_HOME, liveList);

        if (page == 1) {
            mBannerNeedUpdate = false;
            List<BannerBean> bannerList = JSON.parseArray(obj.getString("slide"), BannerBean.class);


            if (bannerList != null && bannerList.size() > 0) {
                if (mBannerList == null || mBannerList.size() != bannerList.size()) {
                    mBannerNeedUpdate = true;
                } else {
                    for (int i = 0; i < mBannerList.size(); i++) {
                        BannerBean bean = mBannerList.get(i);
                        if (bean == null || !bean.isEqual(bannerList.get(i))) {
                            mBannerNeedUpdate = true;
                            break;
                        }
                    }
                }
            }
            mBannerList = bannerList;
            showBanner();
            SpUtil.getInstance().setStringValue("baishun_game_config", obj.getString("baishun_game_config"));
            SpUtil.getInstance().setStringValue("game_baishun", obj.getString("game_baishun"));
            mAdapter.setList(liveList);
        } else {
            mAdapter.insertList(liveList);
        }
    }

    private void loadGuests() {
        mGuestsCallBack = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                homeGuestsRooms = new ArrayList<LiveBean>();
                int v = 0;
                listGuests = new ArrayList<>();
                for (int i = 0; i < info.length; i++) {
                    LiveBean obj = JSON.parseObject(info[i], LiveBean.class);
                    if (obj.getGuests() != null && !obj.getGuests().isEmpty()) {
                        homeGuestsRooms.add(obj);
                        for (ItemGuest item : obj.getGuests()
                        ) {
                            try {
                                if (Integer.parseInt(item.getUser_id()) > 0) {
                                    item.roomIndex = v;

                                    listGuests.add(item);
                                }
                            } catch (Exception e) {
                            }
                        }
                        v++;
                    }
                }
                LiveStorge.getInstance().put("bean.roomIndex", homeGuestsRooms);
                homeGuestsAdpater.setList(listGuests);
                homeGuestsAdpater.notifyDataSetChanged();
            }
        };
        LiveHttpUtil.getHomeGuests(mGuestsCallBack);
    }

    private void showBanner() {
        if (mBanner == null) {
            return;
        }
        if (mBannerList != null && mBannerList.size() > 0) {
            if (mBanner.getVisibility() != View.VISIBLE) {
                mBanner.setVisibility(View.VISIBLE);
            }
            if (mBannerNeedUpdate) {
                mBanner.update(mBannerList);
            }
        } else {
            if (mBanner.getVisibility() != View.GONE) {
                mBanner.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onItemClick(LiveBean bean, int position) {

        if (((AbsActivity) mContext).checkLogin()) {
            if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
                return;
            }
            LiveStorge.getInstance().put(Constants.LIVE_HOME, mAdapter.getList());
            watchLive(bean, Constants.LIVE_HOME, position);
        }
    }

    @Override
    public void loadData() {

    }

    @Override
    public void release() {
        MainHttpUtil.cancel(MainHttpConsts.GET_HOT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onClick(View v) {

    }

    public interface OnClickViewMoreListener {
        public void onClick();
    }
}
