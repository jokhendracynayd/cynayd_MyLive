package com.livestreaming.main.views;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.adapter.ViewPagerAdapter;
import com.livestreaming.common.bean.LevelBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.FollowEvent;
import com.livestreaming.common.event.UpdateFieldEvent;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.CommonIconUtil;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.common.views.AbsCommonViewHolder;
import com.livestreaming.common.views.AbsLivePageViewHolder;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.activity.LiveReportActivity;
import com.livestreaming.live.activity.UserHomeBgActivity;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.bean.SearchUserBean;
import com.livestreaming.live.dialog.LiveShareDialogFragment;
import com.livestreaming.live.event.LiveRoomChangeEvent;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.presenter.UserHomeCheckLivePresenter;
import com.livestreaming.live.presenter.UserHomeSharePresenter;
import com.livestreaming.live.views.LiveRecordViewHolder;
import com.livestreaming.main.R;
import com.livestreaming.main.activity.EditProfileActivity;
import com.livestreaming.main.activity.FansActivity;
import com.livestreaming.main.activity.FollowActivity;
import com.livestreaming.main.activity.UserHomeActivity;
import com.livestreaming.main.dialog.UserAvatarPreviewDialog;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.mall.activity.ShopHomeActivity;
import com.livestreaming.mall.views.PayPubViewHolder;
import com.bumptech.glide.Glide;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cxf on 2018/10/18.
 * 用户个人主页
 */

public class UserHomeViewHolder extends AbsLivePageViewHolder implements LiveShareDialogFragment.ActionListener {

    private int mPageCount = 5;
    private UserHomeDetailViewHolder mDetailViewHolder;
    private VideoHomeViewHolder mVideoHomeViewHolder;
    private ActiveHomeViewHolder mActiveHomeViewHolder;
    private LiveRecordViewHolder mLiveRecordViewHolder;
    private AbsCommonViewHolder[] mViewHolders;
    private List<FrameLayout> mViewList;
    private ImageView mAvatarBg;
    private ImageView mAvatar;
    private TextView mName;
    private ImageView mSex;
    private ImageView mLevelAnchor;
    private ImageView mLevel;
    private TextView mID;
    private View mBtnLive;
    private TextView mFansNum;
    private TextView mFollowNum;
    private TextView mZanNum;
    private TextView mBtnOption;
    private TextView mBlackText;
    private Drawable mFollowDrawable;
    private Drawable mUnFollowDrawable;
    private ViewPager mViewPager;
    private MagicIndicator mIndicator;
    private String[] mIndicatorTitles;
    private TextView mVideoCountTextView;
    private TextView mActiveCountTextView;
    private TextView mLiveCountTextView;
    private TextView mPayCountTextView;


    private View mBtnShop;
    private TextView mShopName;
    private TextView mShopGoodsNum;

    private String mToUid;
    private boolean mFromLiveRoom;
    private String mFromLiveUid;
    private boolean mSelf;
    private UserHomeSharePresenter mUserHomeSharePresenter;
    private SearchUserBean mSearchUserBean;
    private String mVideoString;
    private String mLiveString;
    private String mActiveString;
    private String mPayContentString;
    private int mVideoCount;
    private int mActiveCount;
    private boolean mIsUpdateField;
    private boolean mPaused;
    private UserHomeCheckLivePresenter mCheckLivePresenter;
    private String mBgImgUrl;
    private String mAvatarUrl;
    private String mFrameUrl;
    private ImageView mFrame;

    public UserHomeViewHolder(Context context, ViewGroup parentView, String toUid, boolean fromLiveRoom, String fromLiveUid) {
        super(context, parentView, toUid, fromLiveRoom, fromLiveUid);
        mFromLiveRoom = fromLiveRoom;
    }

    @Override
    protected void processArguments(Object... args) {
        mToUid = (String) args[0];
        if (args.length > 2) {
            mFromLiveUid = (String) args[2];
        }
        if (!TextUtils.isEmpty(mToUid)) {
            mSelf = mToUid.equals(CommonAppConfig.getInstance().getUid());
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_user_home;
    }

    @Override
    public void init() {
        setStatusHeight();
        super.init();
        View bottom = findViewById(R.id.bottom);
        if (mSelf) {
            if (bottom.getVisibility() == View.VISIBLE) {
                bottom.setVisibility(View.GONE);
            }
        } else {
            if (bottom.getVisibility() != View.VISIBLE) {
                bottom.setVisibility(View.VISIBLE);
            }
            View btnReport = findViewById(R.id.btn_report);
            btnReport.setVisibility(View.VISIBLE);
            btnReport.setOnClickListener(this);
        }
        mAvatarBg = (ImageView) findViewById(R.id.bg_avatar);
        mAvatarBg.setOnClickListener(this);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mFrame = (ImageView) findViewById(R.id.frame);
        mAvatar.setOnClickListener(this);
        mName = (TextView) findViewById(R.id.name);
        mSex = (ImageView) findViewById(R.id.sex);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mLevel = (ImageView) findViewById(R.id.level);
        mID = (TextView) findViewById(R.id.id_val);
        mFansNum = (TextView) findViewById(R.id.fans_num);
        mFollowNum = (TextView) findViewById(R.id.follow_num);
        mZanNum = (TextView) findViewById(R.id.zan_num);
        mBtnOption = (TextView) findViewById(R.id.btn_option);

        mBtnLive = findViewById(R.id.btn_live);
        mBlackText = (TextView) findViewById(R.id.black_text);
        mBtnShop = findViewById(R.id.btn_shop);
        mShopName = findViewById(R.id.shop_name);
        mShopGoodsNum = findViewById(R.id.shop_goods_num);

        mFollowDrawable = ContextCompat.getDrawable(mContext, R.drawable.btn_user_option_0);
        mUnFollowDrawable = ContextCompat.getDrawable(mContext, R.drawable.btn_user_option_1);

        if (mSelf) {
            mBtnOption.setText(com.livestreaming.common.R.string.edit_profile);
            mBtnOption.setBackground(mUnFollowDrawable);
            mBtnOption.setTextColor(Color.WHITE);
        }

        if (CommonAppConfig.getInstance().isTeenagerType()) {
            mPageCount = 4;
        } else {
            mPageCount = 5;
        }

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        if (mPageCount > 1) {
            mViewPager.setOffscreenPageLimit(mPageCount - 1);
        }
        mViewHolders = new AbsCommonViewHolder[mPageCount];
        mViewList = new ArrayList<>();
        for (int i = 0; i < mPageCount; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mVideoString = WordUtil.getString(com.livestreaming.common.R.string.video);
        mLiveString = WordUtil.getString(com.livestreaming.common.R.string.live);
        mActiveString = WordUtil.getString(com.livestreaming.common.R.string.main_active);
        mPayContentString = WordUtil.getString(com.livestreaming.common.R.string.mall_355);
        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            mIndicatorTitles = new String[]{WordUtil.getString(com.livestreaming.common.R.string.live_user_home_detail), mVideoString, mActiveString, mLiveString};
        } else {
            mIndicatorTitles = new String[]{mVideoString, mActiveString, WordUtil.getString(com.livestreaming.common.R.string.live_user_home_detail), mLiveString, mPayContentString};
        }
        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return mIndicatorTitles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                if (LanguageUtil.isZh()) {
                    simplePagerTitleView.setPadding(0, 0, 0, 0);
                }
                simplePagerTitleView.setNormalColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.gray1));
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.textColor));
                simplePagerTitleView.setText(mIndicatorTitles[index]);
                simplePagerTitleView.setTextSize(13);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(index);
                        }
                    }
                });
                if (index == 0) {
                    mVideoCountTextView = simplePagerTitleView;
                } else if (index == 1) {
                    mActiveCountTextView = simplePagerTitleView;
                } else if (index == 3) {
                    mLiveCountTextView = simplePagerTitleView;
                } else if (index == 4) {
                    mPayCountTextView = simplePagerTitleView;
                }
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                linePagerIndicator.setLineWidth(DpUtil.dp2px(20));
                linePagerIndicator.setLineHeight(DpUtil.dp2px(2));
                linePagerIndicator.setRoundRadius(DpUtil.dp2px(1));
                linePagerIndicator.setColors(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.global));
                return linePagerIndicator;
            }

        });
        if (LanguageUtil.isZh()) {
            commonNavigator.setAdjustMode(true);
        }
        mIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mIndicator, mViewPager);
        findViewById(R.id.btn_fans).setOnClickListener(this);
        findViewById(R.id.btn_follow).setOnClickListener(this);
        mBtnLive.setOnClickListener(this);
        mBtnOption.setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        findViewById(R.id.btn_black).setOnClickListener(this);
        if (CommonAppConfig.getInstance().isPrivateMsgSwitchOpen()) {
            findViewById(R.id.btn_pri_msg).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_pri_msg).setVisibility(View.GONE);
        }
        mUserHomeSharePresenter = new UserHomeSharePresenter(mContext);
        EventBus.getDefault().register(this);
    }


    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsCommonViewHolder vh = mViewHolders[position];
        if (mDetailViewHolder == null) {
            FrameLayout parent = mViewList.get(2);
            mDetailViewHolder = new UserHomeDetailViewHolder(mContext, parent, mToUid, mSelf);
        }
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 2) {
                    vh = mDetailViewHolder;

                } else if (position == 0) {
                    mVideoHomeViewHolder = new VideoHomeViewHolder(mContext, parent, mToUid);
                    mVideoHomeViewHolder.setActionListener(new VideoHomeViewHolder.ActionListener() {
                        @Override
                        public void onVideoDelete(int deleteCount) {
                            mVideoCount -= deleteCount;
                            if (mVideoCount < 0) {
                                mVideoCount = 0;
                            }
                            if (mVideoCountTextView != null) {
                                mVideoCountTextView.setText(mVideoString + " " + mVideoCount);
                            }
                        }
                    });
                    vh = mVideoHomeViewHolder;
                } else if (position == 1) {
                    mActiveHomeViewHolder = new ActiveHomeViewHolder(mContext, parent, mToUid);
                    mActiveHomeViewHolder.setActionListener(new ActiveHomeViewHolder.ActionListener() {
                        @Override
                        public void onVideoDelete(int deleteCount) {
                            mActiveCount -= deleteCount;
                            if (mActiveCount < 0) {
                                mActiveCount = 0;
                            }
                            if (mActiveCountTextView != null) {
                                mActiveCountTextView.setText(mActiveString + " " + mActiveCount);
                            }
                        }
                    });
                    vh = mActiveHomeViewHolder;
                } else if (position == 3) {
                    mLiveRecordViewHolder = new LiveRecordViewHolder(mContext, parent, mToUid);
                    mLiveRecordViewHolder.setActionListener(new LiveRecordViewHolder.ActionListener() {
                        @Override
                        public UserBean getUserBean() {
                            return mSearchUserBean;
                        }
                    });
                    vh = mLiveRecordViewHolder;
                } else if (position == 4) {
                    vh = new PayPubViewHolder(mContext, parent, mToUid, false);
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }

    @Override
    public void loadData() {
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        loadPageData(0);
        MainHttpUtil.getUserHome(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    SearchUserBean userBean = JSON.toJavaObject(obj, SearchUserBean.class);
                    mSearchUserBean = userBean;
                    String avatar = userBean.getAvatar();
                    mBgImgUrl = obj.getString("bg_img");
                    if(mContext!=null&&mAvatar!=null) {
                        Glide.with(mContext).load(obj.getString("avatar")).into(mAvatar);
                    }
                    JSONObject f = obj.getJSONObject("frame");
                    if (f != null) {
                        mFrameUrl = f.getString("thumb");
                        if (mFrameUrl != null && !mFrameUrl.isEmpty()) {

                            Glide.with(CommonAppContext.getInstance()).load(f.getString("thumb")).into(mFrame);
                        }
                    }
                    ImgLoader.display(mContext, mBgImgUrl, mAvatarBg);
                    mAvatarUrl = avatar;
                    // ImgLoader.displayAvatar(mContext, avatar, mAvatar);
                    String toName = userBean.getUserNiceName();
                    mName.setText(toName);
                    mSex.setImageResource(CommonIconUtil.getSexIcon(userBean.getSex()));
                    CommonAppConfig appConfig = CommonAppConfig.getInstance();
                    LevelBean levelAnchor = appConfig.getAnchorLevel(userBean.getLevelAnchor());
                    ImgLoader.display(mContext, levelAnchor.getThumb(), mLevelAnchor);
                    LevelBean level = appConfig.getLevel(userBean.getLevel());
                    ImgLoader.display(mContext, level.getThumb(), mLevel);
                    mID.setText(userBean.getLiangNameTip());
                    String fansNum = StringUtil.toWan(userBean.getFans());
                    mFansNum.setText(fansNum);
                    mFollowNum.setText(StringUtil.toWan(userBean.getFollows()));
                    mZanNum.setText(obj.getString("praise_num"));

                    if (!mSelf) {
                        if (obj.getIntValue("isattention") == 1) {
                            if (mBtnOption != null) {
                                mBtnOption.setBackground(mFollowDrawable);
                                mBtnOption.setTextColor(0xff7d7d7d);
                                mBtnOption.setText(com.livestreaming.common.R.string.following);
                            }
                        } else {
                            if (mBtnOption != null) {
                                mBtnOption.setBackground(mUnFollowDrawable);
                                mBtnOption.setTextColor(0xffffffff);
                                mBtnOption.setText(com.livestreaming.common.R.string.follow);
                            }
                        }
                        if (mBlackText != null) {
                            mBlackText.setText(obj.getIntValue("isblack") == 1 ? com.livestreaming.common.R.string.black_ing : com.livestreaming.common.R.string.black);
                        }
                    }

                    mVideoCount = obj.getIntValue("videonums");
                    if (mVideoCountTextView != null) {
                        mVideoCountTextView.setText(mVideoString + " " + mVideoCount);
                    }
                    mActiveCount = obj.getIntValue("dynamicnums");
                    if (mActiveCountTextView != null) {
                        mActiveCountTextView.setText(mActiveString + " " + mActiveCount);
                    }
                    if (mLiveCountTextView != null) {
                        mLiveCountTextView.setText(mLiveString + " " + obj.getString("livenums"));
                    }
                    if (mPayCountTextView != null) {
                        mPayCountTextView.setText(mPayContentString + " " + obj.getString("paidprogram_nums"));
                    }
                    if (mUserHomeSharePresenter != null) {
                        mUserHomeSharePresenter.setToUid(mToUid).setToName(toName).setAvatarThumb(userBean.getAvatarThumb()).setFansNum(fansNum);
                    }
                    if (mDetailViewHolder != null) {
                        mDetailViewHolder.showData(userBean, obj);
                    }
                    if (mBtnLive != null) {
                        if (obj.getIntValue("islive") == 1) {
                            if (mBtnLive.getVisibility() != View.VISIBLE) {
                                mBtnLive.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (mBtnLive.getVisibility() == View.VISIBLE) {
                                mBtnLive.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    if (!CommonAppConfig.getInstance().isTeenagerType()) {
                        if (obj.getIntValue("isshop") == 1) {
                            if (mBtnShop != null) {
                                if (mBtnShop.getVisibility() != View.VISIBLE) {
                                    mBtnShop.setVisibility(View.VISIBLE);
                                }
                                mBtnShop.setOnClickListener(UserHomeViewHolder.this);
                            }
                            JSONObject shopInfo = obj.getJSONObject("shop");
                            if (mShopName != null) {
                                mShopName.setText(shopInfo.getString("name"));
                            }
                            if (mShopGoodsNum != null) {
                                mShopGoodsNum.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.mall_165), shopInfo.getString("goods_nums")));
                            }
                        }
                    }

                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }


    /**
     * 刷新印象
     */
    public void refreshImpress() {
        MainHttpUtil.getUserHome(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mDetailViewHolder != null) {
                        mDetailViewHolder.showImpress(obj.getString("label"));
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bg_avatar) {
            UserHomeBgActivity.forward(mContext, mBgImgUrl, mSelf);
            return;
        }
        if (i != R.id.btn_back && i != R.id.btn_live && i != R.id.btn_shop) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
        }
        if (i == R.id.btn_back) {
            back();

        } else if (i == R.id.btn_share) {
            share();

        } else if (i == R.id.btn_fans) {
            forwardFans();

        } else if (i == R.id.btn_follow) {
            forwardFollow();

        } else if (i == R.id.btn_pri_msg) {
            forwardMsg();

        } else if (i == R.id.btn_black) {
            setBlack();

        } else if (i == R.id.btn_live) {
            forwardLiveRoom();
        } else if (i == R.id.btn_shop) {
            forwardShopActivity();
        } else if (i == R.id.btn_report) {
            report();
        } else if (i == R.id.btn_option) {
            if (mSelf) {
                if (mContext != null) {
                    mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
                }
            } else {
                follow();
            }
        } else if (i == R.id.avatar) {
            showAvatarDialog();
        }
    }

    private void report() {
        LiveReportActivity.forward(mContext, mToUid);
    }


    private void forwardShopActivity() {
        ShopHomeActivity.forward(mContext, mToUid);
    }

    private void back() {
        if (mContext instanceof UserHomeActivity) {
            ((UserHomeActivity) mContext).onBackPressed();
        }
    }

    /**
     * 关注
     */
    private void follow() {
        CommonHttpUtil.setAttention(mToUid, null);
    }

    /**
     * 私信
     */
    private void forwardMsg() {
//        if (mSearchUserBean != null) {
//            ChatRoomActivity.forward(mContext, mSearchUserBean, mSearchUserBean.getAttention() == 1, true);
//        }
        String url = getPrivateMessageUrl();
        Log.e("test", url);
        WebViewActivity.forward(mContext, url);
    }

    private String getPrivateMessageUrl() {
        return "https://one.donalive.net/biklog?dona_id=" + CommonAppConfig.getInstance().getUid() +
                "&receiver_id=" + mSearchUserBean.getId();
    }

    private void onAttention(int isAttention) {

        if (isAttention == 1) {
            if (mBtnOption != null) {
                mBtnOption.setBackground(mFollowDrawable);
                mBtnOption.setTextColor(0xff7d7d7d);
                mBtnOption.setText(com.livestreaming.common.R.string.following);
            }
        } else {
            if (mBtnOption != null) {
                mBtnOption.setBackground(mUnFollowDrawable);
                mBtnOption.setTextColor(0xffffffff);
                mBtnOption.setText(com.livestreaming.common.R.string.follow);
            }
        }

        if (mBlackText != null) {
            if (isAttention == 1) {
                mBlackText.setText(com.livestreaming.common.R.string.black);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (e.getToUid().equals(mToUid)) {
            int isAttention = e.getIsAttention();
            if (mSearchUserBean != null) {
                mSearchUserBean.setAttention(isAttention);
            }
            onAttention(isAttention);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFieldEvent(UpdateFieldEvent e) {
        if (mSelf) {
            mIsUpdateField = true;
        }
    }


    /**
     * 前往TA的关注
     */
    private void forwardFollow() {
        FollowActivity.forward(mContext, mToUid);
    }

    /**
     * 前往TA的粉丝
     */
    private void forwardFans() {
        FansActivity.forward(mContext, mToUid);
    }

    /**
     * 拉黑，解除拉黑
     */
    private void setBlack() {
        MainHttpUtil.setBlack(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    boolean isblack = JSON.parseObject(info[0]).getIntValue("isblack") == 1;
                    if (mBlackText != null) {
                        mBlackText.setText(isblack ? com.livestreaming.common.R.string.black_ing : com.livestreaming.common.R.string.black);
                    }
                    if (isblack) {
                        ToastUtil.show(com.livestreaming.common.R.string.black_1);
                        if (mBtnOption != null) {
                            mBtnOption.setBackground(mUnFollowDrawable);
                            mBtnOption.setTextColor(0xffffffff);
                            mBtnOption.setText(com.livestreaming.common.R.string.follow);
                        }
                        EventBus.getDefault().post(new FollowEvent(mToUid, 0));
                    } else {
                        ToastUtil.show(com.livestreaming.common.R.string.black_ing_1);
                    }
                }
            }
        });
    }

    /**
     * 分享
     */
    private void share() {
        LiveShareDialogFragment fragment = new LiveShareDialogFragment();
        fragment.setActionListener(this);
        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveShareDialogFragment");
    }


    @Override
    public void onItemClick(String type) {
        if (Constants.LINK.equals(type)) {
            copyLink();
        } else {
            shareHomePage(type);
        }
    }

    /**
     * 复制页面链接
     */
    private void copyLink() {
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.copyLink();
        }
    }


    /**
     * 分享页面链接
     */
    private void shareHomePage(String type) {
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.shareHomePage(type);
        }
    }

    /**
     * 跳转到直播间
     */
    private void forwardLiveRoom() {
//        if (mFromLiveRoom && !TextUtils.isEmpty(mFromLiveUid) && mToUid.equals(mFromLiveUid)) {
//            ((UserHomeActivity) mContext).onBackPressed();
//            return;
//        }
        if (mSearchUserBean == null) {
            return;
        }
        LiveHttpUtil.getLiveInfo(mSearchUserBean.getId(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    LiveBean liveBean = JSON.parseObject(info[0], LiveBean.class);

                    if (mCheckLivePresenter == null) {
                        mCheckLivePresenter = new UserHomeCheckLivePresenter(mContext, new UserHomeCheckLivePresenter.ActionListener() {
                            @Override
                            public void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal, int liveSdk, boolean isChatRoom, int chatRoomType) {
                                if (liveBean == null) {
                                    return;
                                }
                                if (!FloatWindowHelper.checkVoice(true)) {
                                    return;
                                }
                                LiveAudienceActivity instance = LiveAudienceActivity.instance;
                                if (instance != null) {
                                    instance.onLiveRoomChangeEvent(new LiveRoomChangeEvent(liveBean, liveType, liveTypeVal));
                                    ((UserHomeActivity) mContext).onBackPressed();
                                } else {
                                    LiveAudienceActivity.forward(mContext, liveBean, liveType, liveTypeVal, "", 0, liveSdk, isChatRoom, chatRoomType, 1);
                                }
                            }
                        });
                    }
                    mCheckLivePresenter.checkLive(liveBean);
                }
            }
        });
    }


    @Override
    public void release() {
        super.release();
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_INFO);
        EventBus.getDefault().unregister(this);
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.release();
        }
        mUserHomeSharePresenter = null;
        if (mVideoHomeViewHolder != null) {
            mVideoHomeViewHolder.release();
        }
        mVideoHomeViewHolder = null;
        if (mActiveHomeViewHolder != null) {
            mActiveHomeViewHolder.release();
        }
        mActiveHomeViewHolder = null;
        if (mCheckLivePresenter != null) {
            mCheckLivePresenter.cancel();
        }
        mCheckLivePresenter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainHttpUtil.cancel(MainHttpConsts.GET_USER_HOME);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
        MainHttpUtil.cancel(MainHttpConsts.SET_BLACK);
    }

    @Override
    public void onPause() {
        mPaused = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSelf && mPaused) {
            if (mIsUpdateField) {
                mIsUpdateField = false;
                refreshUserInfo();
            }
        }
        mPaused = false;
    }

    /**
     * 刷新用户信息
     */
    private void refreshUserInfo() {
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        MainHttpUtil.getUserHome(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    SearchUserBean userBean = JSON.toJavaObject(obj, SearchUserBean.class);
                    mSearchUserBean = userBean;
                    String avatar = userBean.getAvatar();
                    mBgImgUrl = obj.getString("bg_img");
                    ImgLoader.display(mContext, mBgImgUrl, mAvatarBg);
                    mAvatarUrl = avatar;
                    ImgLoader.displayAvatar(mContext, avatar, mAvatar);
                    String toName = userBean.getUserNiceName();
                    mName.setText(toName);
                    mSex.setImageResource(CommonIconUtil.getSexIcon(userBean.getSex()));
                    mID.setText(userBean.getLiangNameTip());
                    String fansNum = StringUtil.toWan(userBean.getFans());
                    mFansNum.setText(fansNum);
                    mFollowNum.setText(StringUtil.toWan(userBean.getFollows()));
                    mZanNum.setText(obj.getString("praise_num"));
                    if (mDetailViewHolder != null) {
                        mDetailViewHolder.refreshData(userBean, obj);
                    }
                    if (mBtnLive != null) {
                        if (obj.getIntValue("islive") == 1) {
                            if (mBtnLive.getVisibility() != View.VISIBLE) {
                                mBtnLive.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (mBtnLive.getVisibility() == View.VISIBLE) {
                                mBtnLive.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    private void showAvatarDialog() {
        UserAvatarPreviewDialog dialog = new UserAvatarPreviewDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.URL, mAvatarUrl);
        dialog.setArguments(bundle);
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "UserAvatarPreviewDialog");
    }


}
