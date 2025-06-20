package com.livestreaming.live.activity;

import static com.livestreaming.common.Constants.CURRENT_IsNormalLive;
import static com.livestreaming.common.Constants.isMeInLinkMic;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.live.bean.LiveGestBean;
import com.livestreaming.live.bean.LiveVoiceControlBean;
import com.livestreaming.live.custom.LiveAudienceRecyclerView;
import com.livestreaming.live.interfaces.OnListChangedListener;
import com.livestreaming.live.socket.SocketLinkMicPkUtil;
import com.livestreaming.live.socket.SocketLinkMicUtil;
import com.opensource.svgaplayer.SVGAImageView;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.custom.MyViewPager;
import com.livestreaming.common.dialog.FirstChargeDialogFragment;
import com.livestreaming.common.dialog.NotLoginDialogFragment;
import com.livestreaming.common.event.LoginInvalidEvent;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.RandomUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.game.bean.GameParam;
import com.livestreaming.game.event.GameWindowChangedEvent;
import com.livestreaming.game.event.OpenGameChargeEvent;
import com.livestreaming.game.util.GamePresenter;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LiveRoomScrollAdapter;
import com.livestreaming.live.bean.LiveAudienceFloatWindowData;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.bean.LiveGuardInfo;
import com.livestreaming.live.bean.LiveUserGiftBean;
import com.livestreaming.live.bean.VoiceRoomAccPullBean;
import com.livestreaming.live.dialog.LiveFunctionDialogFragment;
import com.livestreaming.live.dialog.LiveGoodsDialogFragment;
import com.livestreaming.live.dialog.LiveVoiceFaceFragment;
import com.livestreaming.live.event.LinkMicTxAccEvent;
import com.livestreaming.live.event.LiveAudienceChatRoomExitEvent;
import com.livestreaming.live.event.LiveRoomChangeEvent;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.interfaces.LiveFunctionClickListener;
import com.livestreaming.live.interfaces.LivePushListener;
import com.livestreaming.live.livegame.luckpan.dialog.LiveGameLuckpanDialog;
import com.livestreaming.live.livegame.star.dialog.LiveGameStarDialog;
import com.livestreaming.live.presenter.LiveLinkMicAnchorPresenter;
import com.livestreaming.live.presenter.LiveLinkMicPkPresenter;
import com.livestreaming.live.presenter.LiveLinkMicPresenter;
import com.livestreaming.live.presenter.LiveRoomCheckLivePresenter;
import com.livestreaming.live.socket.GameActionListenerImpl;
import com.livestreaming.live.socket.SocketChatUtil;
import com.livestreaming.live.socket.SocketClient;
import com.livestreaming.live.socket.SocketVoiceRoomUtil;
import com.livestreaming.live.utils.LiveStorge;
import com.livestreaming.live.views.AbsLiveChatRoomPlayViewHolder;
import com.livestreaming.live.views.LiveAudienceViewHolder;
import com.livestreaming.live.views.LiveChatRoomLinkMicAgoraViewHolder;
import com.livestreaming.live.views.LiveChatRoomLinkMicTxViewHolder;
import com.livestreaming.live.views.LiveChatRoomPlayAgoraViewHolder;
import com.livestreaming.live.views.LiveChatRoomPlayTxViewHolder;
import com.livestreaming.live.views.LiveEndViewHolder;
import com.livestreaming.live.views.LivePlayAgoraViewHolder;
import com.livestreaming.live.views.LivePlayTxViewHolder;
import com.livestreaming.live.views.LiveRoomPlayViewHolder;
import com.livestreaming.live.views.LiveRoomViewHolder;
import com.livestreaming.live.views.LiveVoiceAudienceViewHolder;
import com.livestreaming.live.views.LiveChatRoomPlayUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/10.
 */

public class LiveAudienceActivity extends LiveActivity implements LiveFunctionClickListener, View.OnClickListener {

    private static final String TAG = "LiveAudienceActivity";
    public int camType = 1;
    private int liveCamStatus = 1;
    public static String liveThumb;
    public static LiveAudienceActivity instance;

    public ArrayList<LiveGestBean> mLiveGestList = new ArrayList<>();
    public boolean isRoomRequestEnabled = true;

    public static void forward(Context context, LiveBean liveBean, int liveType, int liveTypeVal, String key, int position, int liveSdk, boolean isChatRoom, int chatRoomType, int camStatus) {
        Intent intent = new Intent(context, LiveAudienceActivity.class);
        intent.putExtra(Constants.LIVE_BEAN, liveBean);
        intent.putExtra(Constants.LIVE_TYPE, liveType);
        intent.putExtra(Constants.LIVE_TYPE_VAL, liveTypeVal);
        intent.putExtra(Constants.LIVE_KEY, key);
        intent.putExtra(Constants.LIVE_POSITION, position);
        intent.putExtra(Constants.LIVE_SDK, liveSdk);
        intent.putExtra(Constants.IS_CHAT_ROOM, isChatRoom);
        intent.putExtra(Constants.CHAT_ROOM_TYPE, chatRoomType);
        intent.putExtra(Constants.Live_Cam_Status, camStatus);
        if (liveType == 0) {
            CURRENT_IsNormalLive = true;
        } else {
            CURRENT_IsNormalLive = false;
        }
        ((FragmentActivity) context).startActivityForResult(intent, 0);
    }

    public static void forward(Context context, LiveBean liveBean, int liveType, int liveTypeVal, String key, int position, int liveSdk, boolean isChatRoom, int chatRoomType, int camStatus, String liveGuests) {
        Intent intent = new Intent(context, LiveAudienceActivity.class);
        intent.putExtra(Constants.LIVE_BEAN, liveBean);
        intent.putExtra(Constants.LIVE_TYPE, liveType);
        intent.putExtra(Constants.LIVE_TYPE_VAL, liveTypeVal);
        intent.putExtra(Constants.LIVE_KEY, key);
        intent.putExtra(Constants.LIVE_POSITION, position);
        intent.putExtra(Constants.LIVE_SDK, liveSdk);
        intent.putExtra(Constants.IS_CHAT_ROOM, isChatRoom);
        intent.putExtra(Constants.CHAT_ROOM_TYPE, chatRoomType);
        intent.putExtra(Constants.Live_Cam_Status, camStatus);
        intent.putExtra(Constants.Live_Geusts, liveGuests);
        if (liveType == 0) {
            CURRENT_IsNormalLive = true;
        } else {
            CURRENT_IsNormalLive = false;
        }
        ((FragmentActivity) context).startActivityForResult(intent, 0);
    }

    private boolean mUseScroll = true;
    private String mKey;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private LiveRoomScrollAdapter mRoomScrollAdapter;
    private View mMainContentView;
    private MyViewPager mViewPager;
    private View mFirstPage;
    private ViewGroup mSecondPage;//默认显示第二页
    private FrameLayout mContainerWrap;
    private LiveRoomPlayViewHolder mLivePlayViewHolder;
    private LiveAudienceViewHolder mLiveAudienceViewHolder;
    private LiveVoiceAudienceViewHolder mLiveVoiceAudienceViewHolder;
    private boolean mEnd;
    private boolean mCoinNotEnough;//余额不足
    private LiveRoomCheckLivePresenter mCheckLivePresenter;
    private boolean mLighted;
    private AbsLiveChatRoomPlayViewHolder mLiveChatRoomPlayViewHolder;
    private TextView mNameFirst;
    private View mBtnFollowFirst;
    private View mGroupFirst;
    private int mLastViewPagerIndex;
    private View mBtnLandscape;
    private Handler mLandscapeHandler;
    private boolean mLuckPanSwitch;
    private boolean mGameStarEnable;//星球探宝游戏开关 0 关  1 开
    private boolean mGameLuckPanEnable;//幸运大转盘游戏开关  0 关  1  开
    private String mAgoraToken;

    @Override
    protected void getIntentParams() {
        Intent intent = getIntent();
        mIsChatRoom = intent.getBooleanExtra(Constants.IS_CHAT_ROOM, false);
        setChatRoomType(intent.getIntExtra(Constants.CHAT_ROOM_TYPE, Constants.CHAT_ROOM_TYPE_VOICE));
        mLiveSDK = intent.getIntExtra(Constants.LIVE_SDK, Constants.LIVE_SDK_TX);
        mKey = intent.getStringExtra(Constants.LIVE_KEY);
        if (TextUtils.isEmpty(mKey)) {
            mUseScroll = false;
        }
        mPosition = intent.getIntExtra(Constants.LIVE_POSITION, 0);
        liveCamStatus = intent.getIntExtra(Constants.Live_Cam_Status, 1);
        mLiveType = intent.getIntExtra(Constants.LIVE_TYPE, Constants.LIVE_TYPE_NORMAL);
        mLiveTypeVal = intent.getIntExtra(Constants.LIVE_TYPE_VAL, 0);
        mLiveBean = intent.getParcelableExtra(Constants.LIVE_BEAN);
    }
    public void deleteMeGest() {
        isMeInLinkMic = false;
        ((LiveAudienceActivity) mContext).setUserScrollEnabled(true);
        changeLinkMicGuestIcon(false);
        LiveHttpUtil.deleteGest(mLiveBean.getUid(),
                mLiveBean.getStream(),
                CommonAppConfig.getInstance().getUid(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {

                    }
                });

    }
    private ArrayList<LiveGestBean> parseGests(String stringExtra) {
        if (stringExtra == null) {
            return new ArrayList<>();
        } else if (stringExtra.isBlank() || stringExtra.isEmpty()) {
            return new ArrayList<>();
        } else {
            try {
                ArrayList<LiveGestBean> temp = new ArrayList<>();
                JSONArray jsonArray = JSONArray.parseArray(stringExtra);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    LiveGestBean gestBean = new LiveGestBean();
                    gestBean.setAvatar(jsonObject.getString("avatar"));
                    gestBean.setUser_id(Integer.parseInt(jsonObject.getString("user_id")));
                    gestBean.setStream(jsonObject.getString("stream"));
                    gestBean.setCam_type(jsonObject.getInteger("cam_type"));
                    gestBean.setMic_type(jsonObject.getInteger("mic_type"));
                    gestBean.setPosition(jsonObject.getInteger("position"));
                    gestBean.setUserName(jsonObject.getString("guest_name"));
                    gestBean.setFrame(jsonObject.getString("frame_uri"));
                    gestBean.setIncome(jsonObject.getInteger("income"));
                    temp.add(gestBean);
                }
                return temp;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

    }

    private boolean isUseScroll() {
        return mUseScroll && CommonAppConfig.LIVE_ROOM_SCROLL;
    }

    @Override
    public <T extends View> T findViewById(@IdRes int id) {
        if (isUseScroll()) {
            if (mMainContentView != null) {
                return mMainContentView.findViewById(id);
            }
        }
        return super.findViewById(id);
    }

    @Override
    protected int getLayoutId() {
        if (isUseScroll()) {
            return R.layout.activity_live_audience_2;
        }
        return R.layout.activity_live_audience;
    }

    public void setScrollFrozen(boolean frozen) {
        if (isUseScroll() && mRecyclerView != null) {
            mRecyclerView.setLayoutFrozen(frozen);
        }
    }

    public void updateAddGest(int cam_type, int mic_type, LiveGestBean bean) {
        LiveHttpUtil.addUpdateGest(mLiveBean.getUid(),
                mLiveBean.getStream(),
                CommonAppConfig.getInstance().getUid(),
                mic_type, cam_type,
                CommonAppConfig.getInstance().getUserBean().getAvatar(), bean.getFrame(), bean.getUserName(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {

                    }
                });
    }



    @Override
    protected void main() {
        if (LiveStorge.isSecure()){
            getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
            );
        }
        instance = this;
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (isUseScroll()) {
            mRecyclerView = super.findViewById(R.id.recyclerView);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            mMainContentView = LayoutInflater.from(mContext).inflate(R.layout.activity_live_audience, null, false);
        }
        super.main();

        if (isChatRoom()) {
            if (isTxSdK()) {
                LiveChatRoomPlayTxViewHolder liveChatRoomPlayTxVh = new LiveChatRoomPlayTxViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
                final LiveChatRoomLinkMicTxViewHolder liveChatRoomLinkMicTxVh = new LiveChatRoomLinkMicTxViewHolder(mContext, liveChatRoomPlayTxVh.getContainer());
                UserBean anchorInfo = new UserBean();
                anchorInfo.setId(mLiveUid);
                anchorInfo.setUserNiceName(mLiveBean.getUserNiceName());
                anchorInfo.setAvatar(mLiveBean.getAvatar());
                liveChatRoomLinkMicTxVh.setChatRoomType(mChatRoomType, anchorInfo, true);
                liveChatRoomLinkMicTxVh.addToParent();
                liveChatRoomLinkMicTxVh.subscribeActivityLifeCycle();
                if (isChatRoomTypeVideo()) {
                    liveChatRoomPlayTxVh.setPlayViewProvider(new LiveChatRoomPlayTxViewHolder.PlayViewProvider() {
                        @Override
                        public TextureView getPlayView() {
                            return liveChatRoomLinkMicTxVh.getFirstPreview();
                        }
                    });
                }
                mLiveChatRoomPlayViewHolder = liveChatRoomPlayTxVh;
                mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicTxVh;
                mLivePlayViewHolder = liveChatRoomPlayTxVh;
            } else {
                setupLiveChatRoom();
            }
        } else {
            if (isTxSdK()) {
                //腾讯视频播放器
                mLivePlayViewHolder = new LivePlayTxViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
            } else {
                mLivePlayViewHolder = new LivePlayAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
                findViewById(R.id.fake_left_invest).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
                            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onClickInvestors(1);
                        }
                    }
                });
                findViewById(R.id.fake_right_invest).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
                            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onClickInvestors(2);
                        }
                    }
                });
            }
        }

        mLivePlayViewHolder.addToParent();
        mLivePlayViewHolder.subscribeActivityLifeCycle();
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        if (isChatRoom()) {

            mFirstPage = new View(mContext);
            mFirstPage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        mFirstPage = LayoutInflater.from(mContext).inflate(R.layout.view_audience_page_first, mViewPager, false);
        mNameFirst = mFirstPage.findViewById(R.id.name_first);
        mBtnFollowFirst = mFirstPage.findViewById(R.id.btn_follow_first);
        mBtnFollowFirst.setOnClickListener(this);
        mGroupFirst = mFirstPage.findViewById(R.id.group_first);
        mFirstPage.findViewById(R.id.btn_back_first).setOnClickListener(this);
        mFirstPage.findViewById(R.id.root_first_page).setOnClickListener(this);

        mSecondPage = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.view_audience_page, mViewPager, false);
        mContainerWrap = mSecondPage.findViewById(R.id.container_wrap);
        mContainer = mSecondPage.findViewById(R.id.container);
        mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) mSecondPage.findViewById(R.id.gift_gif), (SVGAImageView) mSecondPage.findViewById(R.id.gift_svga), mContainerWrap, false);
        mLiveRoomViewHolder.onLiveUserListChanged = new OnListChangedListener() {
            @Override
            public void onUserListChanged(List<LiveUserGiftBean> list, String pkObjString) {
                if (!mLiveBean.isVoiceRoom()) {
                    if (pkObjString != null && !pkObjString.isEmpty() && !pkObjString.isBlank()) {
                        showpkInvestRow(pkObjString);
                    }
                }
            }

            @Override
            public void onMicVotesChanged(String mic_list_votes) {
                if (isChatRoom()) {
                    JSONArray arr = JSONArray.parseArray(mic_list_votes);
                    Map<String, Integer> map = new HashMap<>();
                    if (arr != null && arr.size() > 0) {
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            map.put(obj.getString("uid"), obj.getInteger("votes"));
                        }
                    }
                    updateChatRoomMicContributionList(map);
                }
            }
        };
        mLiveRoomViewHolder.addToParent();
        mLiveRoomViewHolder.subscribeActivityLifeCycle();
        mBtnLandscape = findViewById(R.id.btn_landscape);
        mBtnLandscape.setOnClickListener(this);

        if (!isChatRoom()) {
            mLiveAudienceViewHolder = new LiveAudienceViewHolder(mContext, mContainer);
            mLiveAudienceViewHolder.addToParent();
//            mLiveAudienceViewHolder.setUnReadCount(getImUnReadCount());
            mLiveBottomViewHolder = mLiveAudienceViewHolder;

            mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePlayViewHolder, false, mLiveSDK, mLiveAudienceViewHolder.getContentView());
            mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePlayViewHolder, false, mLiveSDK, findViewById(R.id.root_live_audience));
            mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePlayViewHolder, false, null);
        } else {
            mViewPager.setCanScroll(true);
            mLiveVoiceAudienceViewHolder = new LiveVoiceAudienceViewHolder(mContext, mContainer);
            mLiveVoiceAudienceViewHolder.addToParent();
//            mLiveVoiceAudienceViewHolder.setUnReadCount(getImUnReadCount());
            mLiveBottomViewHolder = mLiveVoiceAudienceViewHolder;
        }
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    container.addView(mFirstPage);
                    return mFirstPage;
                } else {
                    container.addView(mSecondPage);
                    return mSecondPage;
                }

            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            }
        });
        mViewPager.setCurrentItem(1);

        if (isUseScroll()) {
            List<LiveBean> list = LiveStorge.getInstance().get(mKey);
            mRoomScrollAdapter = new LiveRoomScrollAdapter(mContext, list, mPosition);
            mRoomScrollAdapter.setActionListener(new LiveRoomScrollAdapter.ActionListener() {
                @Override
                public void onPageSelected(LiveBean liveBean, ViewGroup container, boolean first) {
//                    L.e(TAG, "onPageSelected----->" + liveBean);
                    if (mMainContentView != null && container != null) {
                        ViewParent parent = mMainContentView.getParent();
                        if (parent != null) {
                            ViewGroup viewGroup = (ViewGroup) parent;
                            if (viewGroup != container) {
                                viewGroup.removeView(mMainContentView);
                                container.addView(mMainContentView);
                            }
                        } else {
                            container.addView(mMainContentView);
                        }
                    }
                    if (!first) {
                        checkLive(liveBean);
                    }
                }

                @Override
                public void onPageOutWindow(String liveUid) {
//                    L.e(TAG, "onPageOutWindow----->" + liveUid);
                    if (TextUtils.isEmpty(mLiveUid) || mLiveUid.equals(liveUid)) {
                        LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
                        LiveHttpUtil.cancel(LiveHttpConsts.ENTER_ROOM);
                        LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
                        clearRoomData();
                    }
                }

                @Override
                public void onPageInWindow(String liveThumb) {
//                    L.e(TAG, "onPageInWindow----->");
                }
            });
            mRecyclerView.setAdapter(mRoomScrollAdapter);
        }
        setLiveRoomData(mLiveBean);
        enterRoom();
    }

    private void checkLive(LiveBean bean) {
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new LiveRoomCheckLivePresenter(mContext, new LiveRoomCheckLivePresenter.ActionListener() {
                @Override
                public void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal, int liveSdk, int camStatus, int chatRoomType) {
                    if (liveBean == null) {
                        return;
                    }
                    liveCamStatus = camStatus;

                    mLiveType = liveType;
                    mLiveTypeVal = liveTypeVal;
                    mChatRoomType = chatRoomType;
                    if (mRoomScrollAdapter != null) {
                        mRoomScrollAdapter.hideCover();
                    }
                    mIsChatRoom = liveBean.isVoiceRoom();
                    initt();
                    setLiveRoomData(liveBean);
                    enterRoom();
                }
            });
        }
        mCheckLivePresenter.checkLive(bean);
    }

    //fore reals
    private void initt() {
        // mSecondPage.rele();
        if (mLiveChatRoomLinkMicViewHolder != null) {
            mLiveChatRoomPlayViewHolder.removeFromParent();
            mLiveChatRoomPlayViewHolder.release();
//            mLiveChatRoomLinkMicViewHolder=null;
        }
        if (mLiveVoiceAudienceViewHolder != null) {
            mLiveVoiceAudienceViewHolder.removeFromParent();
            mLiveVoiceAudienceViewHolder.release();
//            mLiveVoiceAudienceViewHolder=null;
        }
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.removeFromParent();
            mLivePlayViewHolder.release();
//            mLivePlayViewHolder=null;
        }

        ((LinearLayout) findViewById(R.id.gest_container)).removeAllViews();

        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.removeFromParent();
            mLiveRoomViewHolder.release();
//            mLiveRoomViewHolder=null;
        }
        if (mLiveAudienceViewHolder != null) {
            mLiveAudienceViewHolder.removeFromParent();
            mLiveAudienceViewHolder.release();
//            mLiveAudienceViewHolder=null;
        }


        if (mLiveBottomViewHolder != null) {
            mLiveBottomViewHolder.removeFromParent();
            mLiveBottomViewHolder.release();
//            mLiveBottomViewHolder=null;
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.release();
        }
        if (mLiveLinkMicAnchorPresenter != null) {

            mLiveLinkMicAnchorPresenter.release();
        }
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.release();
        }

        if (isChatRoom()) {

            releaseDouble();
            if (isTxSdK()) {
                LiveChatRoomPlayTxViewHolder liveChatRoomPlayTxVh = new LiveChatRoomPlayTxViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
                final LiveChatRoomLinkMicTxViewHolder liveChatRoomLinkMicTxVh = new LiveChatRoomLinkMicTxViewHolder(mContext, liveChatRoomPlayTxVh.getContainer());
                UserBean anchorInfo = new UserBean();
                anchorInfo.setId(mLiveUid);
                anchorInfo.setUserNiceName(mLiveBean.getUserNiceName());
                anchorInfo.setAvatar(mLiveBean.getAvatar());
                liveChatRoomLinkMicTxVh.setChatRoomType(mChatRoomType, anchorInfo, true);
                liveChatRoomLinkMicTxVh.addToParent();
                liveChatRoomLinkMicTxVh.subscribeActivityLifeCycle();
                if (isChatRoomTypeVideo()) {
                    liveChatRoomPlayTxVh.setPlayViewProvider(new LiveChatRoomPlayTxViewHolder.PlayViewProvider() {
                        @Override
                        public TextureView getPlayView() {
                            return liveChatRoomLinkMicTxVh.getFirstPreview();
                        }
                    });
                }
                mLiveChatRoomPlayViewHolder = liveChatRoomPlayTxVh;
                mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicTxVh;
                mLivePlayViewHolder = liveChatRoomPlayTxVh;
            } else {
                setupLiveChatRoom();
            }
        } else {
            if (isTxSdK()) {
                //腾讯视频播放器
                mLivePlayViewHolder = new LivePlayTxViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
            } else {
                // if (oldIsChatRoom) {
                //reinit when old is chat and new is live
                mLivePlayViewHolder = new LivePlayAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
                // ((LivePlayAgoraViewHolder)   mLivePlayViewHolder).initEngine();
                findViewById(R.id.fake_left_invest).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
                            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onClickInvestors(1);
                        }
                    }
                });
                findViewById(R.id.fake_right_invest).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
                            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onClickInvestors(2);
                        }
                    }
                });
                //   }
            }
        }
        mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) mSecondPage.findViewById(R.id.gift_gif), (SVGAImageView) mSecondPage.findViewById(R.id.gift_svga), mContainerWrap, false);
        mLiveRoomViewHolder.onLiveUserListChanged = new OnListChangedListener() {
            @Override
            public void onUserListChanged(List<LiveUserGiftBean> list, String pkObjString) {
                if (!mLiveBean.isVoiceRoom()) {
                    if (pkObjString != null && !pkObjString.isEmpty() && !pkObjString.isBlank()) {
                        showpkInvestRow(pkObjString);
                    }
                }
            }

            @Override
            public void onMicVotesChanged(String mic_list_votes) {
                if (isChatRoom()) {
                    JSONArray arr = JSONArray.parseArray(mic_list_votes);
                    Map<String, Integer> map = new HashMap<>();
                    if (arr != null && arr.size() > 0) {
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            map.put(obj.getString("uid"), obj.getInteger("votes"));
                        }
                    }
                    updateChatRoomMicContributionList(map);
                }
            }
        };

        mLivePlayViewHolder.addToParent();
        mLivePlayViewHolder.subscribeActivityLifeCycle();
        mLiveRoomViewHolder.addToParent();
        mLiveRoomViewHolder.subscribeActivityLifeCycle();
        mBtnLandscape = findViewById(R.id.btn_landscape);
        mBtnLandscape.setOnClickListener(this);

        if (!isChatRoom()) {
            //if (oldIsChatRoom) {
            mLiveAudienceViewHolder = new LiveAudienceViewHolder(mContext, mContainer);
            mLiveAudienceViewHolder.addToParent();
//            mLiveAudienceViewHolder.setUnReadCount(getImUnReadCount());
            mLiveBottomViewHolder = mLiveAudienceViewHolder;

            mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePlayViewHolder, false, mLiveSDK, mLiveAudienceViewHolder.getContentView());
            mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePlayViewHolder, false, mLiveSDK, findViewById(R.id.root_live_audience));
            mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePlayViewHolder, false, null);
            //   }
        } else {
            mViewPager.setCanScroll(true);
            mLiveVoiceAudienceViewHolder = new LiveVoiceAudienceViewHolder(mContext, mContainer);
            mLiveVoiceAudienceViewHolder.addToParent();
//            mLiveVoiceAudienceViewHolder.setUnReadCount(getImUnReadCount());
            mLiveBottomViewHolder = mLiveVoiceAudienceViewHolder;
        }
        //  oldIsChatRoom = isChatRoom();
    }


    private void showpkInvestRow(String pkObjString) {
        if (mLivePlayViewHolder != null && mLiveType == 0) {
            if (pkObjString != null && pkObjString != "") {
                JSONObject obj = JSONObject.parseObject(pkObjString);
                JSONObject pk1 = JSONObject.parseObject(obj.getString("pk1"));
                JSONObject pk2 = JSONObject.parseObject(obj.getString("pk2"));
                String uid1 = pk1.getString("id");
                List<LiveUserGiftBean> list1 = JSON.parseArray(pk1.getString("list"), LiveUserGiftBean.class);
                String uid2 = pk2.getString("id");
                List<LiveUserGiftBean> list2 = JSON.parseArray(pk2.getString("list"), LiveUserGiftBean.class);


                if (uid1.equals(CommonAppConfig.getInstance().getUid())) {
                    ((LivePlayAgoraViewHolder) mLivePlayViewHolder).setInvestUsers(list2, list1);
                } else {
                    ((LivePlayAgoraViewHolder) mLivePlayViewHolder).setInvestUsers(list1, list2);
                }
            }
        }
    }

    public void updateChatRoomMicContributionList(Map<String, Integer> map) {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            ((LiveChatRoomLinkMicAgoraViewHolder) mLiveChatRoomLinkMicViewHolder).handleMicListVotesChanged(map);
        }
    }

    @Override
    public void setInvestRowVisible(boolean b) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).setInvestRowVisible(b);
        }
    }

    private void setupLiveChatRoom() {
        LiveChatRoomPlayAgoraViewHolder liveChatRoomPlayAgoraVh = new LiveChatRoomPlayAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
        LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh = new LiveChatRoomLinkMicAgoraViewHolder(mContext, liveChatRoomPlayAgoraVh.getContainer());
        liveChatRoomPlayAgoraVh.setLiveChatRoomLinkMicAgoraVh(liveChatRoomLinkMicAgoraVh);
        UserBean anchorInfo = new UserBean();
        anchorInfo.setId(mLiveUid);
        anchorInfo.setUserNiceName(mLiveBean.getUserNiceName());
        anchorInfo.setAvatar(mLiveBean.getAvatar());
        anchorInfo.setFrame(mLiveBean.getFrame());
        liveChatRoomLinkMicAgoraVh.setChatRoomType(mChatRoomType, anchorInfo, true);
        liveChatRoomLinkMicAgoraVh.addToParent();
        liveChatRoomLinkMicAgoraVh.subscribeActivityLifeCycle();
        mLiveChatRoomPlayViewHolder = liveChatRoomPlayAgoraVh;
        mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicAgoraVh;
        mLivePlayViewHolder = liveChatRoomPlayAgoraVh;
    }


    public void scrollNextPosition() {
        if (mRoomScrollAdapter != null) {
            mRoomScrollAdapter.scrollNextPosition();
        }
    }


    private void setLiveRoomData(LiveBean liveBean) {
        mLiveBean = liveBean;
        mLiveUid = liveBean.getUid();
        mStream = liveBean.getStream();
        mLiveRoomViewHolder.setAvatar(liveBean.getAvatar());
        mLiveRoomViewHolder.setAnchorLevel(liveBean.getLevelAnchor());
        mLiveRoomViewHolder.setName(liveBean.getUserNiceName());
        if (mNameFirst != null) {
            mNameFirst.setText(liveBean.getUserNiceName());
        }
        mLiveRoomViewHolder.setRoomNum(liveBean.getLiangNameTip());
        mLiveRoomViewHolder.setTitle(liveBean.getTitle());
        if (!isChatRoom()) {
            mLivePlayViewHolder.setCover(liveBean.getThumb());
            if (mLiveLinkMicPkPresenter != null) {
                mLiveLinkMicPkPresenter.setLiveUid(mLiveUid);
            }
            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.setLiveUid(mLiveUid);
            }
            mLiveAudienceViewHolder.setLiveInfo(mLiveUid, mStream);
            mLiveAudienceViewHolder.setShopOpen(liveBean.getIsshop() == 1);
        }
    }

    private void clearRoomData() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        mSocketClient = null;
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.stopPlay();
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.clearData();
        }
        if (mGamePresenter != null) {
            mGamePresenter.clearGame();
        }
        if (mLiveEndViewHolder != null) {
            mLiveEndViewHolder.removeFromParent();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.clearData();
        }
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.clearData();
        }
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.clearData();
        }
        setPkBgVisible(false);
        mLighted = false;
        if (mLandscapeHandler != null) {
            mLandscapeHandler.removeCallbacksAndMessages(null);
        }
        if (mBtnLandscape != null && mBtnLandscape.getVisibility() == View.VISIBLE) {
            mBtnLandscape.setVisibility(View.INVISIBLE);
        }
    }




    private void enterRoom() {
        try {
            mLiveGestList = new ArrayList<>();
            mLiveGestList = parseGests(getIntent().getStringExtra(Constants.Live_Geusts));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLivePlayViewHolder != null & mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).cleanGuests();
        }
        LiveHttpUtil.enterRoom(mLiveUid, mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String pull = obj.getString("pull");
                    if (mLiveBean != null) {
                        mLiveBean.setPull(pull);
                    }
                    mDanmuPrice = obj.getString("barrage_fee");
                    mSocketUserType = obj.getIntValue("usertype");
                    if (mIsChatRoom) {
                        setManagerAdminActions();
                    }
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.updateLikesCount(obj.getIntValue("total_like"));
                    }
                    mChatLevel = obj.getIntValue("speak_limit");
                    mDanMuLevel = obj.getIntValue("barrage_limit");
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.setLiveInfo(mLiveUid, mStream, obj.getIntValue("userlist_time") * 1000);
                        mLiveRoomViewHolder.setVotes(obj.getString("votestotal"));
                        showFollow(obj.getIntValue("isattention"));
                        List<LiveUserGiftBean> list = JSON.parseArray(obj.getString("userlists"), LiveUserGiftBean.class);
                        mLiveRoomViewHolder.setUserList(list);
                        mLiveRoomViewHolder.setUserNum(obj.getIntValue("nums"));
                        mLiveRoomViewHolder.startRefreshUserList();
                        if (mLiveType == Constants.LIVE_TYPE_TIME) {//计时收费
                            mLiveRoomViewHolder.startRequestTimeCharge();
                        }
                    }

                    //连接socket
                    mSocketClient = new SocketClient(obj.getString("chatserver"), LiveAudienceActivity.this);
                    mSocketClient.connect(mLiveUid, mStream);
                    //守护相关
                    mLiveGuardInfo = new LiveGuardInfo();
                    int guardNum = obj.getIntValue("guard_nums");
                    mLiveGuardInfo.setGuardNum(guardNum);
                    JSONObject guardObj = obj.getJSONObject("guard");
                    if (guardObj != null) {
                        mLiveGuardInfo.setMyGuardType(guardObj.getIntValue("type"));
                        mLiveGuardInfo.setMyGuardEndTime(guardObj.getString("endtime"));
                    }
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.setGuardNum(guardNum);
                        //红包相关
                        mLiveRoomViewHolder.setRedPackBtnVisible(obj.getIntValue("isred") == 1);
                    }
                    int dailytask_switch = obj.getIntValue("dailytask_switch");
                    mTaskSwitch = dailytask_switch == 1;
                    //是否显示转盘
                    boolean showPan = obj.getIntValue("turntable_switch") == 1;
                    mLuckPanSwitch = showPan;
                    mGameStarEnable = obj.getIntValue("game_xqtb_switch") == 1;//星球探宝游戏开关 0 关  1 开;
                    mGameLuckPanEnable = obj.getIntValue("game_xydzp_switch") == 1;//幸运大转盘游戏开关  0 关  1  开

                    if (isChatRoom()) {
                        isRoomRequestEnabled = obj.getIntValue("guest_request") == 1;
                        if (mLiveVoiceAudienceViewHolder != null) {
                            mLiveVoiceAudienceViewHolder.setBtnGameVisible(mGameStarEnable || mGameLuckPanEnable);
                        }
                        if (mLiveChatRoomLinkMicViewHolder != null) {
                            mLiveChatRoomLinkMicViewHolder.showUserList(obj.getJSONArray("mic_list"));
                        }
                        JSONObject object = obj.getJSONObject("frame");
                        if (mLiveRoomViewHolder != null) {
                            if (object != null) {
                                mLiveBean.setFrame(object.getString("thumb"));
                                mLiveRoomViewHolder.setLiveOnerFrame(object.getString("thumb"));
                            } else {
                                mLiveRoomViewHolder.setLiveOnerFrame(null);
                            }
                        }
                        if (mLiveChatRoomPlayViewHolder != null) {
                            JSONObject objBack = obj.getJSONObject("background");
                            if (objBack != null) {
                                mLiveChatRoomPlayViewHolder.onChangeRoomBack(objBack.getString("thumb"));
                            }
                        }
                    } else {
                        releaseDouble();
                        String avatarPk = "";
                        if (mLiveAudienceViewHolder != null) {
                            mLiveAudienceViewHolder.setBtnGameVisible(mGameStarEnable || mGameLuckPanEnable);
                        }
                        if (mLiveLinkMicPresenter != null) {
                            mLiveLinkMicPresenter.setSocketClient(mSocketClient);
                        }
                        //判断是否有连麦，要显示连麦窗口
                        String linkMicUid = obj.getString("linkmic_uid");
                        if (!TextUtils.isEmpty(linkMicUid) && !"0".equals(linkMicUid)) {
                            if (mLiveLinkMicPresenter != null) {
                                mLiveLinkMicPresenter.onLinkMicPlay(linkMicUid);
                            }
                        }
                        //判断是否有主播连麦
                        JSONObject pkInfo = JSON.parseObject(obj.getString("pkinfo"));
                        if (pkInfo != null) {
                            String pkUid = pkInfo.getString("pkuid");
                            if (!TextUtils.isEmpty(pkUid) && !"0".equals(pkUid)) {
                                setLeftCountText(" x" + pkInfo.getIntValue("room_user_win"));
                                setRightCountText(" x" + pkInfo.getIntValue("pk_user_win"));
                                avatarPk = pkInfo.getString("avatar");
                                mLivePlayViewHolder.setAnchorLinkMic(true, 0);
                                if (mLiveLinkMicAnchorPresenter != null) {
                                    mLiveLinkMicAnchorPresenter.setPkUid(pkUid);
                                    mLiveLinkMicAnchorPresenter.setPkStream(pkInfo.getString("pkstream"));
                                    mLiveLinkMicAnchorPresenter.showPkUidFollow(pkUid);
                                }
                                setPkBgVisible(true);
                            } else {
                                if (mLiveLinkMicAnchorPresenter != null) {
                                    mLiveLinkMicAnchorPresenter.hidePkUidFollow();
                                }
                            }
                            if (pkInfo.getIntValue("ifpk") == 1 && mLiveLinkMicPkPresenter != null) {//pk开始了
                                findViewById(R.id.fake_investors_layout).setVisibility(View.VISIBLE);
                                int pk_time_punish = pkInfo.getIntValue("pk_time_punish");
                                if (pk_time_punish > 0) {
                                    mLiveLinkMicPkPresenter.onEnterRoomPkEnd(pkUid, pkInfo.getLongValue("pk_gift_liveuid"), pkInfo.getLongValue("pk_gift_pkuid"), pk_time_punish);
                                } else {
                                    mLiveLinkMicPkPresenter.onEnterRoomPkStart(pkUid, pkInfo.getLongValue("pk_gift_liveuid"), pkInfo.getLongValue("pk_gift_pkuid"), pkInfo.getIntValue("pk_time"));
                                    SocketLinkMicPkUtil.requestDublePk(mSocketClient, mLiveBean.getUid());
                                }
                            } else {
                                findViewById(R.id.fake_investors_layout).setVisibility(View.INVISIBLE);
                            }
                        }

                        //奖池等级
                        int giftPrizePoolLevel = obj.getIntValue("jackpot_level");

                        if (mLiveRoomViewHolder != null) {
                            mLiveRoomViewHolder.showBtn(showPan, giftPrizePoolLevel, dailytask_switch);
                        }

                        //直播间商品
                        JSONObject showGoodsInfo = obj.getJSONObject("show_goods");
                        String goodsId = showGoodsInfo.getString("goodsid");
                        if (!"0".equals(goodsId)) {
                            GoodsBean goodsBean = new GoodsBean();
                            goodsBean.setId(goodsId);
                            goodsBean.setThumb(showGoodsInfo.getString("goods_thumb"));
                            goodsBean.setName(showGoodsInfo.getString("goods_name"));
                            goodsBean.setPriceNow(showGoodsInfo.getString("goods_price"));
                            goodsBean.setType(showGoodsInfo.getIntValue("goods_type"));
                            if (mLiveRoomViewHolder != null) {
                                mLiveRoomViewHolder.setShowGoodsBean(goodsBean);
                            }
                        }

                        //游戏相关
                        if (CommonAppConfig.GAME_ENABLE && mLiveRoomViewHolder != null) {
                            GameParam param = new GameParam();
                            param.setContext(mContext);
                            param.setParentView(mContainerWrap);
                            param.setTopView(mContainer);
                            param.setInnerContainer(mLiveRoomViewHolder.getInnerContainer());
                            param.setGameActionListener(new GameActionListenerImpl(LiveAudienceActivity.this, mSocketClient));
                            param.setLiveUid(mLiveUid);
                            param.setStream(mStream);
                            param.setAnchor(false);
                            param.setCoinName(CommonAppConfig.getInstance().getScoreName());
                            param.setObj(obj);
                            if (mGamePresenter == null) {
                                mGamePresenter = new GamePresenter();
                            }
                            mGamePresenter.setGameParam(param);
                        }
                        handleEnterRoomCams(mLiveBean.getThumb(), avatarPk);
                    }
                    if (mLivePlayViewHolder != null) {
                        if (isTxSdK()) {
                            mLivePlayViewHolder.play(pull);
                        } else {
                            mAgoraToken = obj.getString("user_sw_token");
                            mLivePlayViewHolder.playAgora(pull,
                                    obj.getIntValue("isvideo") == 1, mAgoraToken, mStream, Integer.parseInt(mLiveUid));
                        }
                    }
                }
            }
        });
    }

    private void handleEnterRoomCams(String thumb, String avatarPk) {
        if (mLivePlayViewHolder != null) {

            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).handleEnterRoomCams(thumb, avatarPk);
        }
    }

    private void setManagerAdminActions() {
        if (mSocketUserType == Constants.SOCKET_USER_TYPE_NORMAL) {
            mLiveVoiceAudienceViewHolder.setAdminManagerFunctionsVisibilty(false);
            return;
        }
        mLiveVoiceAudienceViewHolder.setAdminManagerFunctionsVisibilty(true);
    }

    /**
     * 结束观看
     */
    private void endPlay() {
        if (mEnd) {
            return;
        }
        mEnd = true;
        //断开socket
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        mSocketClient = null;
        //结束播放
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.release();
        }
        mLivePlayViewHolder = null;
        release();
    }

    @Override
    protected void release() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.ENTER_ROOM);
        LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BALANCE);
        super.release();
        if (mRoomScrollAdapter != null) {
            mRoomScrollAdapter.release();
        }
        mRoomScrollAdapter = null;
        if (mLandscapeHandler != null) {
            mLandscapeHandler.removeCallbacksAndMessages(null);
        }
        mLandscapeHandler = null;
    }

    /**
     * 观众收到直播结束消息
     */
    @Override
    public void onLiveEnd() {
        super.onLiveEnd();
        endPlay();
        if (isLandscape()) {
            setPortrait();
        }
        if (mBtnLandscape != null && mBtnLandscape.getVisibility() == View.VISIBLE) {
            mBtnLandscape.setVisibility(View.INVISIBLE);
        }
        if (mViewPager != null) {
            if (mViewPager.getCurrentItem() != 1) {
                mViewPager.setCurrentItem(1, false);
            }
            mViewPager.setCanScroll(false);
        }
        if (mLiveEndViewHolder == null) {
            mLiveEndViewHolder = new LiveEndViewHolder(mContext, mSecondPage, false);
            mLiveEndViewHolder.subscribeActivityLifeCycle();
            mLiveEndViewHolder.addToParent();
        }
        mLiveEndViewHolder.showData(mLiveBean, mStream);
        setScrollFrozen(true);

    }


    /**
     * 观众收到踢人消息
     */
    @Override
    public void onKick(String touid) {
        if (!TextUtils.isEmpty(touid) && touid.equals(CommonAppConfig.getInstance().getUid())) {//被踢的是自己
            exitLiveRoom();
            ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.live_kicked_2));
        }
    }

    /**
     * 观众收到禁言消息
     */
    @Override
    public void onShutUp(String touid, String content) {
        if (!TextUtils.isEmpty(touid) && touid.equals(CommonAppConfig.getInstance().getUid())) {
            DialogUitl.showSimpleTipDialog(mContext, content);
        }
    }

    @Override
    public void onBackPressed() {
        if (isLandscape()) {
            setPortrait();
            return;
        }
        if (!mEnd && !canBackPressed()) {
            return;
        }
        if (isChatRoom() && !mEnd) {
            Integer[][] arr = null;
            if (isChatRoomTypeVideo() || isSelfChatRoomUpMic()) {
                arr = new Integer[][]{
                        {com.livestreaming.common.R.string.a_057, ContextCompat.getColor(mContext, com.livestreaming.common.R.color.red)}};
            } else {
                arr = new Integer[][]{
                        {com.livestreaming.common.R.string.a_058, ContextCompat.getColor(mContext, com.livestreaming.common.R.color.textColor)},
                        {com.livestreaming.common.R.string.a_057, ContextCompat.getColor(mContext, com.livestreaming.common.R.color.red)}};
            }
            DialogUitl.showStringArrayDialog(mContext, arr, new DialogUitl.StringArrayDialogCallback() {
                @Override
                public void onItemClick(String text, int tag) {
                    if (tag == com.livestreaming.common.R.string.a_058) {
                        if (mEnd) {
                            LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
                            exitLiveRoom();
                        } else {
                            LiveChatRoomPlayUtil.getInstance().setKeepAlive(true);
                            exitLiveRoom();
                            if (mLiveType != 0) {
                                EventBus.getDefault().post(new LiveAudienceChatRoomExitEvent(mLiveBean, null));
                            }
                        }
                    } else if (tag == com.livestreaming.common.R.string.a_057) {
                        LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
                        exitLiveRoom();
                    }
                }
            });
        } else {
            LiveAudienceFloatWindowData floatWindowData = getFloatWindowData();
            exitLiveRoom();
            if (CommonAppConfig.getInstance().isShowLiveFloatWindow() && mLiveType != Constants.LIVE_TYPE_TIME) {
                EventBus.getDefault().post(new LiveAudienceChatRoomExitEvent(mLiveBean, floatWindowData));
            }
        }
    }

    private LiveAudienceFloatWindowData getFloatWindowData() {
        LiveAudienceFloatWindowData floatWindowData = new LiveAudienceFloatWindowData();
        floatWindowData.setTxSDK(isTxSdK());
        floatWindowData.setAgoraToken(mAgoraToken);
        floatWindowData.setLiveUid(Integer.parseInt(mLiveUid));
        floatWindowData.setStream(mStream);
        floatWindowData.setLinkMicAudienceUid(getLinkMicUid());
        floatWindowData.setPkUid(getLinkMicAnchorUid());
        return floatWindowData;
    }

    /**
     * 退出直播间
     */
    public void exitLiveRoom() {
        endPlay();
        finish();
    }


    @Override
    protected void onDestroy() {
        Constants.isMeInLinkMic = false;
        if (mLiveAudienceViewHolder != null) {
            mLiveAudienceViewHolder.clearAnim();
        }
        super.onDestroy();
        L.e("LiveAudienceActivity-------onDestroy------->");
    }

    /**
     * 点亮
     */
    private int clickCount = 0;
    private Handler handler = new Handler();
    private Runnable actionRunnable = new Runnable() {
        @Override
        public void run() {
            SocketChatUtil.liveLike(mSocketClient, mLiveUid, mStream, clickCount);
            clickCount = 0;
//            LiveHttpUtil.sendLikeCount(mLiveUid, mStream, clickCount, new HttpCallback() {
//                @Override
//                public void onSuccess(int code, String msg, String[] info) {
//                    if (info != null) {
//                        if (info.length > 0) {
//                            JSONObject obj = JSONObject.parseObject(info[0]);
//                            sendClicksSockit(obj.getIntValue("ifpk"), obj.getIntValue("pktotal1"), obj.getIntValue("pktotal2"), obj.getIntValue("total"));
//                        }
//                    }
//                }
//            });
        }
    };

//    private void sendClicksSockit(int ifpk, int pktotal1, int pktotal2, int total) {
//        if (ifpk == 1) {
//            SocketChatUtil.sendCountLights(mSocketClient, ifpk, pktotal1, pktotal2, mLiveUid, getPkuid());
//        }
//        if (mSocketClient != null) {
//            SocketChatUtil.sendTotalLights(mSocketClient, total);
//            clickCount = 0;
//        }
//    }

    @Override
    public void onAncorCancelPk(String winUid, int winCount) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkClose();
        }
        ((LivePlayAgoraViewHolder) mLivePlayViewHolder).releaseDouble();
    }

    @Override
    public void onPkBouns(int duration, int startAfter, int x, int mission_type, int target_value, int targetDuration) {
        if (mLivePlayViewHolder != null) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onPkBouns(duration, startAfter, x, mission_type, target_value, targetDuration);
        }
    }

    @Override
    public void onLiveHostSwitchCam(int type, String uid, String thumb) {
        handleLiveCam(type, uid, thumb);
    }

    public void light() {
        if (mLiveType == 0) {
            clickCount++;
            handler.removeCallbacks(actionRunnable);
            handler.postDelayed(actionRunnable, 500);
        }
        if (!mLighted) {
            mLighted = true;
            int guardType = mLiveGuardInfo != null ? mLiveGuardInfo.getMyGuardType() : Constants.GUARD_TYPE_NONE;
            SocketChatUtil.sendLightMessage(mSocketClient, 1 + RandomUtil.nextInt(6), guardType);
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.playLightAnim();
        }
    }


    /**
     * 计时收费更新主播映票数
     */
    public void roomChargeUpdateVotes() {
        sendUpdateVotesMessage(mLiveTypeVal);
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.pausePlay();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlay() {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.resumePlay();
        }
    }

    /**
     * 充值成功
     */
    public void onChargeSuccess() {
        if (mLiveType == Constants.LIVE_TYPE_TIME) {
            if (mCoinNotEnough) {
                mCoinNotEnough = false;
                LiveHttpUtil.roomCharge(mLiveUid, mStream, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            roomChargeUpdateVotes();
                            if (mLiveRoomViewHolder != null) {
                                resumePlay();
                                mLiveRoomViewHolder.startRequestTimeCharge();
                            }
                        } else {
                            if (code == 1008) {//余额不足
                                mCoinNotEnough = true;
                                DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.live_coin_not_enough), false,
                                        new DialogUitl.SimpleCallback2() {
                                            @Override
                                            public void onConfirmClick(Dialog dialog, String content) {
                                                RouteUtil.forwardMyCoin(mContext);
                                            }

                                            @Override
                                            public void onCancelClick() {
                                                exitLiveRoom();
                                            }
                                        });
                            }
                        }
                    }
                });
            }
        }
    }

    public void setCoinNotEnough(boolean coinNotEnough) {
        mCoinNotEnough = coinNotEnough;
    }

    /**
     * 游戏窗口变化事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameWindowChangedEvent(GameWindowChangedEvent e) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setOffsetY(e.getGameViewHeight());
        }
    }

    /**
     * 游戏充值页面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOpenGameChargeEvent(OpenGameChargeEvent e) {
        openChargeWindow();
    }

    /**
     * 腾讯sdk连麦时候切换低延时流
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMicTxAccEvent(LinkMicTxAccEvent e) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayTxViewHolder) {
            ((LivePlayTxViewHolder) mLivePlayViewHolder).onLinkMicTxAccEvent(e.isLinkMic());
        }
    }

    /**
     * 腾讯sdk时候主播连麦回调
     *
     * @param linkMic true开始连麦 false断开连麦
     */
    public void onLinkMicTxAnchor(boolean linkMic) {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.setAnchorLinkMic(linkMic, 2000);
        }
    }


    //    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveRoomChangeEvent(LiveRoomChangeEvent e) {
        LiveBean liveBean = e.getLiveBean();
        if (liveBean != null) {
            releaseDouble();
            String liveUid = liveBean.getUid();
            if (!TextUtils.isEmpty(liveUid) && !liveUid.equals(mLiveUid)) {
                LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
                LiveHttpUtil.cancel(LiveHttpConsts.ENTER_ROOM);
                LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
                clearRoomData();

                setLiveRoomData(liveBean);
                mLiveType = e.getLiveType();
                mLiveTypeVal = e.getLiveTypeVal();
                enterRoom();
            }
        }
    }

    /**
     * 打开商品窗口
     */
    public void openGoodsWindow() {
        SocketChatUtil.liveGoodsFloat(mSocketClient);
        LiveGoodsDialogFragment fragment = new LiveGoodsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveGoodsDialogFragment");
    }

    /**
     * 打开首冲礼包窗口
     */
    public void openFirstCharge() {
        FirstChargeDialogFragment fragment = new FirstChargeDialogFragment();
        fragment.show(getSupportFragmentManager(), "FirstChargeDialogFragment");
    }

    public void liveGoodsFloat() {
        SocketChatUtil.liveGoodsFloat(mSocketClient);
    }


    /**
     * 打开功能弹窗
     */
    public void showFunctionDialogVoice(boolean hasFace) {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.HAS_GAME, false);
        bundle.putBoolean(Constants.OPEN_FLASH, false);
        bundle.putBoolean("TASK", mTaskSwitch);
        bundle.putBoolean("LUCK_PAN", mLuckPanSwitch);
        bundle.putBoolean("HAS_FACE", hasFace);
        bundle.putBoolean("HAS_MSG", true);
        boolean isLinkMic = false;
        if (mLiveLinkMicPresenter != null) {
            isLinkMic = mLiveLinkMicPresenter.isLinkMic();
        }
        bundle.putBoolean(Constants.LINK_MIC, isLinkMic);
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    /**
     * 打开功能弹窗
     */
    public void showFunctionDialog() {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("HAS_MSG", true);
        boolean isLinkMic = false;
        if (mLiveLinkMicPresenter != null) {
            isLinkMic = mLiveLinkMicPresenter.isLinkMic();
        }
        bundle.putBoolean(Constants.LINK_MIC, isLinkMic);
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    @Override
    public void onClick(int functionID) {
        switch (functionID) {
            case Constants.LIVE_FUNC_RED_PACK://红包
                openRedPackSendWindow();
                break;
            case Constants.LIVE_FUNC_TASK://每日任务
                openDailyTaskWindow();
                break;
            case Constants.LIVE_FUNC_LUCK://幸运奖池
                openPrizePoolWindow();
                break;
            case Constants.LIVE_FUNC_PAN://幸运转盘
                openLuckPanWindow();
                break;
            case Constants.LIVE_FUNC_SHARE://分享
                openShareWindow();
                break;
            case Constants.LIVE_FUNC_MSG://私信
                openChatListWindow();
                break;
            case Constants.LIVE_FUNC_FACE://表情
                openVoiceRoomFace();
                break;
            case Constants.LIVE_FUNC_LINK_MIC_AUD:
                if (mLiveLinkMicPresenter != null) {
                    mLiveLinkMicPresenter.onLinkMicBtnClick();
                }
                break;
            case Constants.CLOSE_MY_MIC:
                com.livestreaming.common.Constants.isMyMicOpen = false;
                ((LivePlayAgoraViewHolder) mLivePlayViewHolder).openCloseMyMicAsGest(false);
                break;

            case Constants.OPEN_MY_MIC:
                com.livestreaming.common.Constants.isMyMicOpen = true;
                ((LivePlayAgoraViewHolder) mLivePlayViewHolder).openCloseMyMicAsGest(true);
                break;

            case Constants.CLOSE_MY_CAM:
                com.livestreaming.common.Constants.isMyCamOpen = false;
                ((LivePlayAgoraViewHolder) mLivePlayViewHolder).closeMyCamAsGest(false);
                break;

            case Constants.OPEN_MY_CAM:
                com.livestreaming.common.Constants.isMyCamOpen = true;
                ((LivePlayAgoraViewHolder) mLivePlayViewHolder).closeMyCamAsGest(true);
                break;
            case Constants.Mall:
                forwardMall();
                break;
            case Constants.BackBag:
                forwardMyBackBag();
                break;
        }
    }

    public void forwardMall() {
        WebViewActivity.forward(mContext, "https://donalive.net/appapi/Mall/index");
    }

    public void forwardMyBackBag() {
        WebViewActivity.forward(mContext, "https://donalive.net/appapi/Equipment/index");
    }

    /**
     * 语音聊天室表情
     */
    public void openVoiceRoomFace() {
        LiveVoiceFaceFragment fragment = new LiveVoiceFaceFragment();
        fragment.show(getSupportFragmentManager(), "LiveVoiceFaceFragment");
    }

    /**
     * 点击上麦下麦按钮
     */
    public void clickVoiceUpMic() {
        if (isRoomRequestEnabled) {
            if (isSelfChatRoomUpMic()) {
                LiveHttpUtil.userDownVoiceMic(mStream, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            SocketVoiceRoomUtil.userDownMic(mSocketClient, CommonAppConfig.getInstance().getUid(), 0);
                        }
                        ToastUtil.show(msg);
                    }
                });
            } else {
                PermissionUtil.request(this, new PermissionCallback() {
                            @Override
                            public void onAllGranted() {
                                voiceApplyUpMic();
                            }
                        },
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA);
            }
        } else {
            onMeVoiceLinkMicDirect();
        }
    }


    /**
     * 语音聊天室--用户发起上麦申请
     */
    public void applyMicUp() {
        SocketVoiceRoomUtil.applyMicUp(mSocketClient);
    }

    AlertDialog dialog;

    @Override
    public void onVoiceRoomHandleApply(String toUid, String toName, String toAvatar, int position, String frame) {
        super.onVoiceRoomHandleApply(toUid, toName, toAvatar, position, frame);
        if (!TextUtils.isEmpty(toUid) && toUid.equals(CommonAppConfig.getInstance().getUid())) {////上麦的是自己
            boolean isUpMic = position >= 0;
            if (mLiveVoiceAudienceViewHolder != null) {
                mLiveVoiceAudienceViewHolder.changeMicUp(isUpMic);
            }
            ((LiveAudienceRecyclerView) mRecyclerView).setUserScrollEnabled(false);
            mViewPager.setCanScroll(false);
            ToastUtil.show(isUpMic ? com.livestreaming.common.R.string.a_046 : com.livestreaming.common.R.string.a_047);
            if (isUpMic) {
                if (isTxSdK()) {
                    //获取自己的推拉流地址开始推流
                    LiveHttpUtil.getVoiceMicStream(mStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                String push = obj.getString("push");
//                            final String pull = obj.getString("pull");
                                final String userStream = obj.getString("user_stream");
                                //L.e("语音聊天室----push----> " + push);
                                //L.e("语音聊天室----pull---> " + pull);
                                if (mLiveChatRoomLinkMicViewHolder != null) {
                                    mLiveChatRoomLinkMicViewHolder.startPush(0, 0, push, new LivePushListener() {
                                        @Override
                                        public void onPreviewStart() {

                                        }

                                        @Override
                                        public void onPushStart() {
                                            SocketVoiceRoomUtil.userPushSuccess(mSocketClient, "", userStream);
                                        }

                                        @Override
                                        public void onPushFailed() {

                                        }
                                    });
                                }
                            }
                        }
                    });

                    //获取主播和麦上其他用户的低延时流地址，开始播流
                    LiveHttpUtil.getVoiceLivePullStreams(mStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                List<VoiceRoomAccPullBean> list = JSON.parseArray(Arrays.toString(info), VoiceRoomAccPullBean.class);
                                for (VoiceRoomAccPullBean bean : list) {
                                    if (bean.getIsAnchor() == 1) {//主播
                                        if (mLiveChatRoomPlayViewHolder != null) {
                                            mLiveChatRoomPlayViewHolder.changeAccStream(bean.getPull());
                                        }
                                    } else {
                                        if (mLiveChatRoomLinkMicViewHolder != null) {
                                            mLiveChatRoomLinkMicViewHolder.playAccStream(bean.getUid(), bean.getPull(), null);
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else {
                    if (isChatRoomTypeVideo()) {
                        com.livestreaming.common.Constants.isMyCamOpen = true;
                        mLiveChatRoomLinkMicViewHolder.startPush(1, 0, "", new LivePushListener() {
                            @Override
                            public void onPreviewStart() {

                            }

                            @Override
                            public void onPushStart() {
                                SocketVoiceRoomUtil.userPushSuccess(mSocketClient, "", "");
                            }

                            @Override
                            public void onPushFailed() {

                            }
                        });

                    } else {
                        mLiveChatRoomLinkMicViewHolder.startPush(0, 0, "", new LivePushListener() {
                            @Override
                            public void onPreviewStart() {

                            }

                            @Override
                            public void onPushStart() {
                                SocketVoiceRoomUtil.userPushSuccess(mSocketClient, "", "");
                            }

                            @Override
                            public void onPushFailed() {

                            }
                        });
                    }

                }
            }
        }

    }

    /**
     * 语音聊天室--所有人收到某人下麦的消息
     *
     * @param uid 下麦的人的uid
     */
    @Override
    public void onVoiceRoomDownMic(String uid, int type) {
        if (!TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {//被下麦的是自己
            ((LiveAudienceRecyclerView) mRecyclerView).setUserScrollEnabled(true);
            mViewPager.setCanScroll(true);
            if (mLiveVoiceAudienceViewHolder != null) {
                mLiveVoiceAudienceViewHolder.changeMicUp(false);
            }
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.stopPush();//停止推流
                if (isTxSdK()) {
                    mLiveChatRoomLinkMicViewHolder.stopAllPlay();//停止所有播放
                }
                mLiveChatRoomLinkMicViewHolder.onUserDownMic(uid);
            }
            if (mLiveChatRoomPlayViewHolder != null) {
                if (isTxSdK()) {
                    mLiveChatRoomPlayViewHolder.changeAccStream(null);//切回到普通流播放
                }
            }
            if (type == 1 || type == 2) {//1被主播下麦  2被管理员下麦
                ToastUtil.show(com.livestreaming.common.R.string.a_054);
            }
        } else {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                int position = mLiveChatRoomLinkMicViewHolder.getUserPosition(uid);
                if (position != -1) {
                    if (isTxSdK()) {
                        mLiveChatRoomLinkMicViewHolder.stopPlay(position);//停止播放被下麦的人的流
                    } else {
                        mLiveChatRoomLinkMicViewHolder.stopPlay(uid);//停止播放被下麦的人的流
                    }
                    mLiveChatRoomLinkMicViewHolder.onUserDownMic(position);
                }
            }
        }
    }


    /**
     * 语音聊天室--主播控制麦位 闭麦开麦禁麦等
     *
     * @param uid      被操作人的uid
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    @Override
    public void onControlMicPosition(String uid, int position, int status) {
        super.onControlMicPosition(uid, position, status);
        if (!TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {//被操作人是自己
            if (status == Constants.VOICE_CTRL_OPEN) {
                ToastUtil.show(com.livestreaming.common.R.string.a_056);
                if (mLiveVoiceAudienceViewHolder != null) {
                    mLiveVoiceAudienceViewHolder.setVoiceMicClose(false);
                }
                if (mLiveChatRoomLinkMicViewHolder != null) {
                    mLiveChatRoomLinkMicViewHolder.setPushMute(false);
                }
            } else if (status == Constants.VOICE_CTRL_CLOSE) {
                ToastUtil.show(com.livestreaming.common.R.string.a_055);
                if (mLiveVoiceAudienceViewHolder != null) {
                    mLiveVoiceAudienceViewHolder.setVoiceMicClose(true);
                }
                if (mLiveChatRoomLinkMicViewHolder != null) {
                    mLiveChatRoomLinkMicViewHolder.setPushMute(true);
                }
            }
        }
    }


    /**
     * 语音聊天室--观众上麦后推流成功，把自己的播放地址广播给所有人
     *
     * @param uid        上麦观众的uid
     * @param pull       上麦观众的播流地址
     * @param userStream 上麦观众的流名，主播混流用
     */
    @Override
    public void onVoiceRoomPushSuccess(final String uid, String pull, final String userStream) {
        if (!TextUtils.isEmpty(uid) && !uid.equals(CommonAppConfig.getInstance().getUid())) {
            if (isTxSdK()) {
                if (isSelfChatRoomUpMic()) {
                    LiveHttpUtil.getNewUpMicPullUrl(userStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                String playUrl = JSON.parseObject(info[0]).getString("play_url");
                                if (mLiveChatRoomLinkMicViewHolder != null) {
                                    mLiveChatRoomLinkMicViewHolder.playAccStream(uid, playUrl, userStream);
                                }
                            }
                        }
                    });
                }
            } else {
                if (mLiveChatRoomLinkMicViewHolder != null) {
                    mLiveChatRoomLinkMicViewHolder.playAccStream(uid, pull, userStream);
                }
            }
        }
    }

    @Override
    public void onUserUpVoiceLinkMicDirect(UserBean bean) {

    }

    @Override
    public void onAnchorCloseLive() {
        ToastUtil.show(com.livestreaming.common.R.string.live_end);
        finish();
    }

    @Override
    public void onRoomBackgroundChanged(String imgUrl) {
        mLiveChatRoomPlayViewHolder.onChangeRoomBack(imgUrl);
    }

    @Override
    public void onAdminAgreeUpLinkMicc(UserBean bean, int isAgree) {

    }

    @Override
    public void onAdminControlMicc(LiveVoiceControlBean beann) {

    }

    @Override
    public void onAdminCloseMicc(LiveVoiceControlBean beann2) {

    }

    @Override
    public void onUpdateGuestIncome(int uid, int income) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).updateGuestIncome(uid, income);
        }
    }

    @Override
    public void onTotalLikesCount(int total) {
        if (mLiveType == 0) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.updateLikesCount(total);
            }
        }
    }

    /**
     * 语音聊天室--发送表情
     */
    public void voiceRoomSendFace(int index) {
        SocketVoiceRoomUtil.voiceRoomSendFace(mSocketClient, index);
    }

    /**
     * 聊天室 判断自己是否上麦了
     */
    private boolean isSelfChatRoomUpMic() {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            return mLiveChatRoomLinkMicViewHolder.getUserPosition(CommonAppConfig.getInstance().getUid()) >= 0;
        }
        return false;
    }


    /**
     * 未登录的弹窗
     */
    @Override
    public void showNotLoginDialog() {
        NotLoginDialogFragment fragment = new NotLoginDialogFragment();
        fragment.setActionListener(new NotLoginDialogFragment.ActionListener() {
            @Override
            public void beforeForwardLogin() {
                exitLiveRoom();
            }
        });
        fragment.show(getSupportFragmentManager(), "NotLoginDialogFragment");
    }


    /**
     * 设为横屏
     */
    public void setLandscape() {
        if (mViewPager != null) {
            mLastViewPagerIndex = mViewPager.getCurrentItem();
            if (mLastViewPagerIndex != 0) {
                mViewPager.setCurrentItem(0, false);
            }
            mViewPager.setCanScroll(false);
        }
        setScrollFrozen(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    /**
     * 设为竖屏
     */
    public void setPortrait() {
        if (mViewPager != null) {
            if (mLastViewPagerIndex != 0) {
                mViewPager.setCurrentItem(mLastViewPagerIndex, false);
            }
            mViewPager.setCanScroll(true);
        }
        setScrollFrozen(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 是否横屏
     */
    public boolean isLandscape() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (landscape) {
            //L.e("onConfigurationChanged-------->横屏");
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            if (mLandscapeHandler == null) {
                mLandscapeHandler = new Handler();
            } else {
                mLandscapeHandler.removeCallbacksAndMessages(null);
            }
            if (mGroupFirst != null && mGroupFirst.getVisibility() != View.VISIBLE) {
                mGroupFirst.setVisibility(View.VISIBLE);
            }
            mLandscapeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGroupFirst != null && mGroupFirst.getVisibility() == View.VISIBLE) {
                        mGroupFirst.setVisibility(View.INVISIBLE);
                    }
                }
            }, 5000);
        } else {
            //L.e("onConfigurationChanged-------->竖屏");
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            if (mLandscapeHandler != null) {
                mLandscapeHandler.removeCallbacksAndMessages(null);
            }
            if (mGroupFirst != null && mGroupFirst.getVisibility() == View.VISIBLE) {
                mGroupFirst.setVisibility(View.INVISIBLE);
            }
        }
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.changeSize(landscape);
        }
    }

    public void showFollow(int isAttention) {
        super.showFollow(isAttention);
        if (mBtnFollowFirst != null) {
            if (isAttention == 0) {
                if (mBtnFollowFirst.getVisibility() != View.VISIBLE) {
                    mBtnFollowFirst.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnFollowFirst.getVisibility() == View.VISIBLE) {
                    mBtnFollowFirst.setVisibility(View.GONE);
                }
            }
        }
    }


    public void onVideoHeightChanged(int videoHeight, int parentHeight) {
        if (mEnd) {
            return;
        }
        if (mLiveBean != null && mLiveBean.getAnyway() == 0) {
            return;
        }
        if (videoHeight > 0) {
            if (mBtnLandscape != null) {
                if (mBtnLandscape.getVisibility() != View.VISIBLE) {
                    mBtnLandscape.setVisibility(View.VISIBLE);
                }
                int y = (parentHeight - videoHeight) / 2 + videoHeight - DpUtil.dp2px(35);
                mBtnLandscape.setY(y);
            }
        } else {
            if (mBtnLandscape != null && mBtnLandscape.getVisibility() == View.VISIBLE) {
                mBtnLandscape.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_landscape) {
            if (checkLogin()) {
                setLandscape();
            }
        } else if (i == R.id.btn_back_first) {
            setPortrait();
        } else if (i == R.id.btn_follow_first) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.follow();
            }
        } else if (i == R.id.root_first_page) {
            clickFirstPage();
        }
    }

    private void clickFirstPage() {
        if (mGroupFirst != null && mGroupFirst.getVisibility() != View.VISIBLE) {
            if (mLandscapeHandler == null) {
                mLandscapeHandler = new Handler();
            } else {
                mLandscapeHandler.removeCallbacksAndMessages(null);
            }
            mGroupFirst.setVisibility(View.VISIBLE);
            mLandscapeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGroupFirst != null && mGroupFirst.getVisibility() == View.VISIBLE) {
                        mGroupFirst.setVisibility(View.INVISIBLE);
                    }
                }
            }, 5000);
        }
    }

    public void openGame(View btnGame) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.view_live_choose_game, null);
        View btnGameStar = v.findViewById(R.id.btn_live_game_star);
        View btnGameLuckpan = v.findViewById(R.id.btn_live_game_luckpan);
        if (!mGameStarEnable) {
            btnGameStar.setVisibility(View.GONE);
        }
        if (!mGameLuckPanEnable) {
            btnGameLuckpan.setVisibility(View.GONE);
        }
        v.measure(0, 0);
        int vw = v.getMeasuredWidth();
        int vh = v.getMeasuredHeight();
        final PopupWindow popupWindow = new PopupWindow(v, vw, vh, true);
        popupWindow.setAnimationStyle(com.livestreaming.common.R.style.animCenter);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        int[] arr = new int[2];
        btnGame.getLocationInWindow(arr);
        int offsetX = arr[0] + btnGame.getWidth() / 2 - vw / 2;
        int offsetY = arr[1] - vh;
        popupWindow.showAtLocation(btnGame, Gravity.LEFT | Gravity.TOP, offsetX, offsetY);
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_live_game_star) {
                    popupWindow.dismiss();
                    openStarGame();
                } else if (i == R.id.btn_live_game_luckpan) {
                    popupWindow.dismiss();
                    openLuckPanGame();
                }
            }
        };
        btnGameStar.setOnClickListener(onClick);
        btnGameLuckpan.setOnClickListener(onClick);
    }


    /**
     * 星球探宝
     */
    protected void openStarGame() {
        LiveGameStarDialog dialog = new LiveGameStarDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putString(Constants.STREAM, mStream);
        dialog.setArguments(bundle);
        dialog.setActionListener(new LiveGameStarDialog.ActionListener() {
            @Override
            public void onClose() {
                mLiveGameStarDialog = null;
            }

            @Override
            public void sendWinSocket(String json) {
                //发送星球探宝中奖消息
                SocketChatUtil.gameXqtbWin(mSocketClient, json);
            }

            @Override
            public void onPackClick() {
                openGiftWindow(true);
            }

            @Override
            public void onChargeClick() {
                openChargeWindow();
            }
        });
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameStarDialog");
        mLiveGameStarDialog = dialog;
    }

    /**
     * 幸运转盘
     */
    protected void openLuckPanGame() {
        LiveGameLuckpanDialog dialog = new LiveGameLuckpanDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putString(Constants.STREAM, mStream);
        dialog.setArguments(bundle);
        dialog.setActionListener(new LiveGameLuckpanDialog.ActionListener() {
            @Override
            public void onClose() {
                mLiveGameLuckpanDialog = null;
            }

            @Override
            public void sendWinSocket(String json) {
                //发送幸运大转盘中奖消息
                SocketChatUtil.gameLuckpanWin(mSocketClient, json);
            }

            @Override
            public void onPackClick() {
                openGiftWindow(true);
            }

            @Override
            public void onChargeClick() {
                openChargeWindow();
            }
        });
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameLuckPanDialog");
        mLiveGameLuckpanDialog = dialog;
    }

    /**
     * 声网sdk--> 开始/关闭 观众连麦推流
     *
     * @param isPush true开始推流 ，false结束推流
     */
    public void toggleLinkMicPushAgora(boolean isPush, String uid) {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.toggleLinkMicPushAgora(isPush, uid);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播开始连麦
     */
    public void startAgoraLinkMicAnchor(String pkUid) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onAnchorLinkMicStart(pkUid);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播断开连麦
     */
    public void closeAgoraLinkMicAnchor() {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onAnchorLinkMicClose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        hideDialogs();
        exitLiveRoom();
    }

    public void handleLiveCam(int type, String uid, String avatar) {
        if (mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {

            camType = type;
            if (type == 1) {
                if (mLiveLinkMicAnchorPresenter != null) {
                    if (mLiveLinkMicAnchorPresenter.getPkUid() != null && !mLiveLinkMicAnchorPresenter.getPkUid().isEmpty()) {
                        ((LivePlayAgoraViewHolder) mLivePlayViewHolder).switchLiveCam(true, uid, avatar);
                    } else {
                        mLivePlayViewHolder.hideCover();
                    }
                } else {
                    mLivePlayViewHolder.hideCover();
                }
            } else if (type == 2) {
                if (mLiveLinkMicAnchorPresenter != null) {
                    if (mLiveLinkMicAnchorPresenter.getPkUid() != null && !mLiveLinkMicAnchorPresenter.getPkUid().isEmpty()) {
                        ((LivePlayAgoraViewHolder) mLivePlayViewHolder).switchLiveCam(false, uid, avatar);
                    } else {
                        ((LivePlayAgoraViewHolder) mLivePlayViewHolder).mCover.setAlpha(1.0f);
                        ((LivePlayAgoraViewHolder) mLivePlayViewHolder).mCover.setVisibility(View.VISIBLE);
                        mLivePlayViewHolder.setCover(avatar);
                    }
                } else {
                    ((LivePlayAgoraViewHolder) mLivePlayViewHolder).mCover.setAlpha(1.0f);
                    ((LivePlayAgoraViewHolder) mLivePlayViewHolder).mCover.setVisibility(View.VISIBLE);
                    mLivePlayViewHolder.setCover(avatar);
                }
            }
        }
    }


    public void onMeAsGestChangeCam(int mPosition, boolean b) {
        SocketLinkMicUtil.onMeAsGestCloseOpenCam(mSocketClient, b, mPosition);
    }

    public void onGestsCloseOpenMic(int mPosition, boolean b) {
        SocketLinkMicUtil.onGestsCloseOpenMic(mSocketClient, b, mPosition);
    }

    public void handleLiveGestCam(boolean b, String touid, String avatar) {
        if (mLivePlayViewHolder != null) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).handleGestCam(b, touid);
        }
    }

    public void handleMicGestss(int b, String touid) {
        if (mLivePlayViewHolder != null) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onOpenCloseGestssMic(b, touid);
        }
    }

    public void setIsScrolling(boolean b) {
        mViewPager.setCanScroll(b);
    }

    public void onAdminAgreeUpLinkMic(UserBean bean, int isAgree) {
        SocketLinkMicUtil.onAdminAgreeUpLinkMic(mSocketClient, bean, isAgree);
    }

    public void onAdminControlClick(LiveVoiceControlBean bean) {
        SocketLinkMicUtil.onAdminControlClick(mSocketClient, bean);
    }

    public void onAdminControlCloseVoiceMic(LiveVoiceControlBean bean) {
        SocketLinkMicUtil.onAdminControlCloseVoiceMic(mSocketClient, bean);
    }

    public void requestLinkMic() {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onLinkMicBtnClick();
        }

    }

    public void changeLinkMicGuestIcon(boolean b) {
        if (mLiveAudienceViewHolder != null) {
            mLiveAudienceViewHolder.changeLinkMicIcon(b);
        }
    }

    public void releaseDouble() {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).releaseDouble();
        }
    }

    @Override
    public void updatePkBouns(int value, int uid, int completed) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            if (("" + uid).equals(mLiveUid)) {
                ((LivePlayAgoraViewHolder) mLivePlayViewHolder).updatePkBouns(value, uid, completed);
            }
        }
    }

    public String getPkuid() {
        String pkuid = "";
        if (mLiveLinkMicAnchorPresenter != null) {
            pkuid = mLiveLinkMicAnchorPresenter.getPkUid();
        }
        return pkuid;
    }

    @Override
    public void onSendGiftLiveResponse(String coin, int level, int type) {
        whenSendGiftResponse(coin, level, type);
    }

    public void handleLiveLinkCameras(boolean isUp) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).handleLiveLinkCams(getPkuid(), isUp);
        }
    }

    public void setLiveLinkMicImageVisible(boolean b) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).setLiveLinkMicImageVisible(b);
        }
    }

    @Override
    public void handleDoubleTotalPoints(int totalPoints, int uid) {
        Log.e("test_double_total_points_2", "........................");
        if (("" + uid).equals(mLiveBean.getUid())) {
            Log.e("test_double_total_points_3", "........................");
            if (mLivePlayViewHolder != null) {
                Log.e("test_double_total_points_4", "........................");
                ((LivePlayAgoraViewHolder) mLivePlayViewHolder).handleDoubleTotalPoints(totalPoints);
            }
        }
    }

    public void handleWhenEnterRoomDouble(int uid, int x, int missionType, int duration, int startAfter, int doubleSec, int currentTime, int state, int target_value, int targetValueDefault, int completed) {
        if (("" + uid).equals(mLiveBean.getUid())) {
            Log.e("test_double_enter_room_1", "........................");
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).handleWhenEnterRoomDouble(uid, x, missionType, duration, startAfter, doubleSec, currentTime, state, target_value, targetValueDefault, completed);
        }
    }

    public void onChangeRoomGuetEnabled(int isEnabled) {
        ToastUtil.show(getString(isEnabled == 1 ? R.string.request_guest_enabled : R.string.request_guest_disabled));
        this.isRoomRequestEnabled = isEnabled == 1;
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LiveChatRoomPlayAgoraViewHolder) {
            ((LiveChatRoomPlayAgoraViewHolder) mLivePlayViewHolder).isRoomRequestEnabled(isEnabled == 1);
        }
    }

    public void setUserScrollEnabled(boolean b) {
        if (mRecyclerView != null) {
            ((LiveAudienceRecyclerView) mRecyclerView).setUserScrollEnabled(b);
        }
    }

    public LinearLayout getGestContainer() {
        return (LinearLayout) findViewById(R.id.gest_container);
    }

    public void onMeVoiceLinkMicDirect() {
        if (isSelfChatRoomUpMic()) {
            LiveHttpUtil.userDownVoiceMic(mStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        SocketVoiceRoomUtil.userDownMic(mSocketClient, CommonAppConfig.getInstance().getUid(), 0);
                    }
                    ToastUtil.show(msg);
                }
            });
        } else {
            PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            handeWhenClickDirectVoiceApply();
                        }
                    },
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA);
        }
    }

    private void handeWhenClickDirectVoiceApply() {
        LiveHttpUtil.applyVoiceLiveMic(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    SocketVoiceRoomUtil.onUserUpMicDirect(mSocketClient);
                }
            }
        });
    }

    public void deleteGuest(int uid) {
        LiveHttpUtil.deleteGest(mLiveBean.getUid(),
                mLiveBean.getStream(),
                "" + uid, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {

                    }
                });
    }
}
