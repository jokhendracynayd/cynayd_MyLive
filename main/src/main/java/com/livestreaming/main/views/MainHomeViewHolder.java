package com.livestreaming.main.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.custom.MyViewPager3;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.dialog.GameDialogFragment;
import com.livestreaming.main.R;
import com.livestreaming.main.activity.LiveVoiceRoomListViewHolder;
import com.livestreaming.main.custom.MainHomePageTitleView;
import com.yariksoffice.lingver.Lingver;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * MainActivity 首页
 */

public class MainHomeViewHolder extends AbsMainHomeParentViewHolder {

    private MainHomeLiveViewHolder mLiveViewHolder;
    // private MainHomeVideoScrollVh mVideoViewHolder;
    private MainHomeNearViewHolder mNearViewHolder;
    private int mStatusBarHeight;
    private View mAppBarChildView;
    private View mCover;
    private int mChangeHeight;
    private ImageView mBtnSearch;
    private ImageView mBtnRank;
    private MyViewPager3 mMyViewPager;
    private List<MainHomePageTitleView> mPagerTitleList;
    private LinePagerIndicator mLinePagerIndicator;
    private boolean mIndicatorWhite;
    private int mGlobalColor;
    private boolean mAppBarLayoutScroll = true;
    private boolean mFirstShowed = true;
    private boolean mPaused;
    private LiveVoiceRoomListViewHolder mVoiceViewHolder;
    private ImageView games_btn;

    public MainHomeViewHolder(Context context, ViewGroup parentView) {

        super(context, parentView);
        Lingver.getInstance().setLocale(parentView.getContext(), Constants.CUR_LANGUAGE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home;
    }

    @Override
    public void init() {
        // Lingver.getInstance().setLocale(getContentView().getContext(), Constants.CUR_LANGUAGE);
        mGlobalColor = ContextCompat.getColor(mContext, com.livestreaming.common.R.color.global);
        mPagerTitleList = new ArrayList<>();
        setStatusHeight();
        super.init();

        games_btn = findViewById(R.id.btn_games);
        games_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showGamesDialog();
            }
        });
        Glide.with(mContext).load(com.livestreaming.live.R.mipmap.icon_live_func_game).dontAnimate().into(games_btn);
        mStatusBarHeight = ScreenDimenUtil.getInstance().getStatusBarHeight();
        mAppBarChildView = findViewById(R.id.app_bar_child_view);
        mCover = findViewById(R.id.cover);

        mAppBarLayout.post(new Runnable() {
            @Override
            public void run() {
                mChangeHeight = mAppBarLayout.getHeight() - mStatusBarHeight;
            }
        });
        mBtnSearch = findViewById(R.id.btn_rank);
        mBtnRank = findViewById(R.id.btn_search);
        onRankVisibleChanged();
        mMyViewPager = (MyViewPager3) mViewPager;
        if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
            mMyViewPager.setCanScroll(false);
        }
    }

    public void showGamesDialog() {
        if (!((AbsActivity) mContext).checkLogin()) {
            return;
        }
        GameDialogFragment fragment = new GameDialogFragment();
        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "GameDialogFragment");
    }

    public void onAppBarOffsetChanged(float verticalOffset) {
        if (mChangeHeight != 0 && mCover != null) {
            float rate = verticalOffset / mChangeHeight;
            mCover.setAlpha(rate);
        }
    }

    @Override
    protected void loadPageData(int position) {
        //setIndicatorWhite(position == 1);
        for (int i = 0; i < mPagerTitleList.size(); i++) {
            mPagerTitleList.get(i).updateColor(i == position);
        }
        //((MainActivity) mContext).changeStatusBarWhite(position == 1);
//        if (mAppBarChildView != null) {
//            mAppBarChildView.setMinimumHeight(mStatusBarHeight);
//        }
//        setAppBarLayoutScroll(position != 1);
//        ((MainActivity) mContext).setBottomVisible(position != 1);
        if (mViewHolders == null) {
            return;
        }
        AbsMainHomeChildViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mLiveViewHolder = new MainHomeLiveViewHolder(mContext, parent);
                    mLiveViewHolder.onClickViewMoreListener = new MainHomeLiveViewHolder.OnClickViewMoreListener() {
                        @Override
                        public void onClick() {
                            mMyViewPager.setCurrentItem(1, true);
                        }
                    };
                    vh = mLiveViewHolder;
                } else if (position == 1) {
                    mVoiceViewHolder = new LiveVoiceRoomListViewHolder(mContext, parent);
                    vh = mVoiceViewHolder;
                } else if (position == 2) {
                    mNearViewHolder = new MainHomeNearViewHolder(mContext, parent);
                    vh = mNearViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (mViewHolders != null) {
            for (int i = 0, length = mViewHolders.length; i < length; i++) {
                AbsMainViewHolder viewHolder = mViewHolders[i];
                if (viewHolder != null) {
                    viewHolder.setShowed(position == i);
                }
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
                WordUtil.getString(com.livestreaming.common.R.string.live),
                WordUtil.getString(R.string.party),
                WordUtil.getString(com.livestreaming.common.R.string.near)
        };
    }

    public void changeToHomePage() {
        if (mViewPager != null) {
            int index = mViewPager.getCurrentItem();
            if (index != 0) {
                mViewPager.setCurrentItem(0, false);
            }
        }
    }

    @Override
    public void loadData() {
        if (mPaused) {
            super.loadData();
        } else {
            if (mViewPager != null) {
                int index = mViewPager.getCurrentItem();
                if (index != 0) {
                    mViewPager.setCurrentItem(0, false);
                } else {
                    loadPageData(index);
                }
            }
        }
    }

    private void onRankVisibleChanged() {
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            if (configBean.getLeaderboard_switch() == 1) {
                if (CommonAppConfig.getInstance().isTeenagerType()) {
                    if (mBtnRank != null && mBtnRank.getVisibility() != View.GONE) {
                        mBtnRank.setVisibility(View.GONE);
                    }
                } else {
                    if (mBtnRank != null && mBtnRank.getVisibility() != View.VISIBLE) {
                        mBtnRank.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (mBtnRank != null && mBtnRank.getVisibility() != View.GONE) {
                    mBtnRank.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setViewPagerCanScroll(boolean canScroll) {
        if (mMyViewPager != null) {
            mMyViewPager.setCanScroll(canScroll);
        }
    }

    protected IPagerTitleView getIndicatorTitleView(Context context, String[] titles, final int index) {
        MainHomePageTitleView simplePagerTitleView = new MainHomePageTitleView(context);
        simplePagerTitleView.setPadding(DpUtil.dp2px(7), 0, DpUtil.dp2px(7), 0);
        simplePagerTitleView.setNormalColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.gray1));
        simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.textColor));
        simplePagerTitleView.setText(titles[index]);
        simplePagerTitleView.setTextSize(18);
        simplePagerTitleView.getPaint().setFakeBoldText(true);
        simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonAppConfig.getInstance().isBaseFunctionMode()) {
                    if (mViewPager != null) {
                        mViewPager.setCurrentItem(index);
                    }
                }
            }
        });
        mPagerTitleList.add(simplePagerTitleView);
        return simplePagerTitleView;
    }

    protected IPagerIndicator getIndicatorView(Context context) {
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        linePagerIndicator.setXOffset(DpUtil.dp2px(5));
        linePagerIndicator.setRoundRadius(DpUtil.dp2px(2));
        linePagerIndicator.setColors(mGlobalColor);
        mLinePagerIndicator = linePagerIndicator;
        return linePagerIndicator;
    }


    private void setIndicatorWhite(boolean white) {
        if (mIndicatorWhite != white) {
            mIndicatorWhite = white;
            SimplePagerTitleView pagerTitleView = null;
            for (int i = 0; i < mPagerTitleList.size(); i++) {
                pagerTitleView = mPagerTitleList.get(i);
                if (white) {
                    pagerTitleView.setNormalColor(0xb3ffffff);
                    pagerTitleView.setSelectedColor(0xffffffff);
                } else {
                    pagerTitleView.setNormalColor(0xff969696);
                    pagerTitleView.setSelectedColor(0xff282828);
                }
            }
            if (mLinePagerIndicator != null) {
                mLinePagerIndicator.setColors(white ? 0xffffffff : mGlobalColor);
            }
            if (mBtnSearch != null) {
                if (white) {
                    mBtnSearch.setColorFilter(0xffffffff);
                } else {
                    mBtnSearch.setColorFilter(0xff7d7d7d);
                }
            }
            if (mBtnRank != null) {
                if (white) {
                    mBtnRank.setColorFilter(0xffffffff);
                } else {
                    mBtnRank.setColorFilter(0xff7d7d7d);
                }
            }
        }
    }

    private void setAppBarLayoutScroll(boolean appBarLayoutScroll) {
        if (mAppBarLayoutScroll != appBarLayoutScroll) {
            mAppBarLayoutScroll = appBarLayoutScroll;
            if (mAppBarChildView != null) {
                AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mAppBarChildView.getLayoutParams();
                if (appBarLayoutScroll) {
                    lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                } else {
                    lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
                }
                mAppBarChildView.setLayoutParams(lp);
            }
            if (mViewPager != null) {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mViewPager.getLayoutParams();
                if (appBarLayoutScroll) {
                    lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());
                } else {
                    lp.setBehavior(null);
                }
                mViewPager.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void setShowed(boolean showed) {
        super.setShowed(showed);
        if (mViewHolders != null) {
            for (int i = 0, length = mViewHolders.length; i < length; i++) {
                AbsMainViewHolder viewHolder = mViewHolders[i];
                if (viewHolder != null) {
                    viewHolder.setShowed(0 == i);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (isShowed()) {
//            mPaused = true;
//
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (isShowed()) {
//            if (mPaused) {
//                if (mVideoViewHolder != null && mVideoViewHolder.isShowed()) {
//                    mVideoViewHolder.resumePlay();
//                    VideoHttpUtil.startWatchVideo();
//                }
//            }
//            mPaused = false;
//        }
    }

    public int getCurrentItem() {
        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        }
        return 0;
    }

    public void setSelectedTap(int selectedHomeTab) {
        mViewPager.setCurrentItem(selectedHomeTab);
    }
}
