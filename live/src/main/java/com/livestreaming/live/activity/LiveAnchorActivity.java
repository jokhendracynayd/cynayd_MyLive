package com.livestreaming.live.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.livestreaming.beauty.interfaces.IBeautyViewHolder;
import com.livestreaming.beauty.views.BeautyViewHolder;
import com.livestreaming.beauty.views.SimpleBeautyViewHolder;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.dialog.NotCancelableDialog;
import com.livestreaming.common.event.LoginInvalidEvent;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DateFormatUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.LogUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.game.bean.GameParam;
import com.livestreaming.game.event.GameWindowChangedEvent;
import com.livestreaming.game.util.GamePresenter;
import com.livestreaming.im.event.ImUnReadCountEvent;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.im.utils.ImUnReadCount;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.Mp3Adapter;
import com.livestreaming.live.bean.ChangeRoomBackBean;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.bean.LiveConfigBean;
import com.livestreaming.live.bean.LiveGestBean;
import com.livestreaming.live.bean.LiveGuardInfo;
import com.livestreaming.live.bean.LiveReceiveGiftBean;
import com.livestreaming.live.bean.LiveUserGiftBean;
import com.livestreaming.live.bean.LiveVoiceControlBean;
import com.livestreaming.live.bean.LiveVoiceMixUserBean;
import com.livestreaming.live.dialog.LiveChooseBackgroundDialogFragment;
import com.livestreaming.live.dialog.LiveFunctionDialogFragment;
import com.livestreaming.live.dialog.LiveGuestsApplyUpFragment;
import com.livestreaming.live.dialog.LiveLinkMicListDialogFragment;
import com.livestreaming.live.dialog.LiveShopDialogFragment;
import com.livestreaming.live.dialog.LiveVoiceControlFragment;
import com.livestreaming.live.dialog.SuperAdminTipDialogFragment;
import com.livestreaming.live.event.LinkMicTxMixStreamEvent;
import com.livestreaming.live.event.LiveVoiceMicStatusEvent;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.interfaces.LiveFunctionClickListener;
import com.livestreaming.live.interfaces.LivePushListener;
import com.livestreaming.live.interfaces.OnListChangedListener;
import com.livestreaming.live.interfaces.TxLinkMicPusher;
import com.livestreaming.live.music.LiveMusicDialogFragment;
import com.livestreaming.live.presenter.LiveLinkMicAnchorPresenter;
import com.livestreaming.live.presenter.LiveLinkMicPkPresenter;
import com.livestreaming.live.presenter.LiveLinkMicPresenter;
import com.livestreaming.live.socket.GameActionListenerImpl;
import com.livestreaming.live.socket.SocketChatUtil;
import com.livestreaming.live.socket.SocketClient;
import com.livestreaming.live.socket.SocketLinkMicUtil;
import com.livestreaming.live.socket.SocketVoiceRoomUtil;
import com.livestreaming.live.utils.LiveStorge;
import com.livestreaming.live.views.AbsLiveChatRoomPushViewHolder;
import com.livestreaming.live.views.AbsLivePushViewHolder;
import com.livestreaming.live.views.LiveAnchorViewHolder;
import com.livestreaming.live.views.LiveChatRoomLinkMicAgoraViewHolder;
import com.livestreaming.live.views.LiveChatRoomLinkMicTxViewHolder;
import com.livestreaming.live.views.LiveChatRoomPushAgoraViewHolder;
import com.livestreaming.live.views.LiveChatRoomPushTxViewHolder;
import com.livestreaming.live.views.LiveEndViewHolder;
import com.livestreaming.live.views.LiveGoodsAddViewHolder;
import com.livestreaming.live.views.LiveMusicViewHolder;
import com.livestreaming.live.views.LivePlatGoodsAddViewHolder;
import com.livestreaming.live.views.LivePushAgoraViewHolder;
import com.livestreaming.live.views.LivePushScreenTxViewHolder;
import com.livestreaming.live.views.LivePushTxViewHolder;
import com.livestreaming.live.views.LiveReadyChatRoomViewHolder;
import com.livestreaming.live.views.LiveReadyViewHolder;
import com.livestreaming.live.views.LiveRoomViewHolder;
import com.livestreaming.live.views.LiveVoiceAnchorViewHolder;
import com.opensource.svgaplayer.SVGAImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.agora.rtc2.RtcEngineEx;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/7.
 * 主播直播间
 */

public class LiveAnchorActivity extends LiveActivity implements LiveFunctionClickListener {

    private static final String TAG = "LiveAnchorActivity";
    private LiveGoodsAddViewHolder mLiveGoodsAddViewHolder;
    private LivePlatGoodsAddViewHolder mLivePlatGoodsAddViewHolder;
    private int isMicOpen = 1;
    private static final int REQUEST_CODE = 100;
    private boolean isShowBottomSheetRequested = false;
    private ArrayList<HashMap<String, String>> mp3List;
    private ArrayList<HashMap<String, String>> queueList = new ArrayList<>();
    private int currentPlayingIndex = -1;


    private Button joinBtn, bass, beautify,leaveRoom;
    // Change View to ConstraintLayout to match XML change
    private ConstraintLayout mediaControls;
    float dX, dY;
    int lastAction;

    private FrameLayout floating_media_wrapper;
    private ImageView discImage;
    private boolean isMediaWrapperVisible = true;



    private SeekBar seekBar;
    private ImageButton pauseBtn,btn_volume,next_SongsPlay;
    private String filePah = "";
    private String id = "";

    public static void forward(Context context, int liveSdk, LiveConfigBean bean, int haveStore, boolean isVoiceChatRoom, String forbidLiveTip, boolean isScreenRecord) {
        Intent intent = new Intent(context, LiveAnchorActivity.class);
        intent.putExtra(Constants.LIVE_SDK, liveSdk);
        if (bean != null) {
            intent.putExtra(Constants.LIVE_CONFIG, bean);
        }
        intent.putExtra(Constants.HAVE_STORE, haveStore);
        intent.putExtra(Constants.TIP, forbidLiveTip);
        intent.putExtra(Constants.IS_CHAT_ROOM, isVoiceChatRoom);
        intent.putExtra(Constants.SCREEN_RECORD, isScreenRecord);
        context.startActivity(intent);
    }

    private ViewGroup mRoot;
    private ViewGroup mContainerWrap;
    private AbsLivePushViewHolder mLivePushViewHolder;
    private LiveReadyViewHolder mLiveReadyViewHolder;
    private IBeautyViewHolder mLiveBeautyViewHolder;
    private LiveAnchorViewHolder mLiveAnchorViewHolder;
    private LiveVoiceAnchorViewHolder mLiveVoiceAnchorViewHolder;
    private LiveMusicViewHolder mLiveMusicViewHolder;
    private boolean mStartPreview;//是否开始预览
    private boolean mStartLive;//是否开始了直播
    private List<Integer> mGameList;//游戏开关
    private boolean mBgmPlaying;//是否在播放背景音乐
    private LiveConfigBean mLiveConfigBean;
    private HttpCallback mCheckLiveCallback;
    private File mLogFile;
    private int mReqCount;
    private boolean mPaused;
    private boolean mNeedCloseLive = true;
    private boolean mSuperCloseLive;//是否是超管关闭直播
    private boolean mLoginInvalid;//登录是否失效
    private boolean mEnd;
    private LiveLinkMicListDialogFragment mLiveLinkMicListDialogFragment;
    private AbsLiveChatRoomPushViewHolder mLiveChatRoomPushViewHolder;
    private boolean mIsScreenRecord;
    private Button startBtn,stopBtn;
    private RtcEngineEx mRtcEngine;

    private boolean isPaused = false;
    private int durationMs = 0;
    private android.os.Handler handler = new android.os.Handler();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_anchor;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void main() {
        super.main();
        if (LiveStorge.isSecure()){
            getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
            );
        }
        mp3List = new ArrayList<>();


        Intent intent = getIntent();
        mLiveSDK = intent.getIntExtra(Constants.LIVE_SDK, Constants.LIVE_SDK_TX);
        mLiveConfigBean = intent.getParcelableExtra(Constants.LIVE_CONFIG);
        int haveStore = intent.getIntExtra(Constants.HAVE_STORE, 0);
        String forbidLiveTip = intent.getStringExtra(Constants.TIP);
        mIsChatRoom = intent.getBooleanExtra(Constants.IS_CHAT_ROOM, false);
        mIsScreenRecord = intent.getBooleanExtra(Constants.SCREEN_RECORD, false);
        mRoot = (ViewGroup) findViewById(R.id.root);
        mSocketUserType = Constants.SOCKET_USER_TYPE_ANCHOR;
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        mLiveUid = u.getId();
        mLiveBean = new LiveBean();
        mLiveBean.setUid(mLiveUid);
        mLiveBean.setUserNiceName(u.getUserNiceName());
        mLiveBean.setAvatar(u.getAvatar());
        mLiveBean.setFrame(u.getFrame());
        mLiveBean.setAvatarThumb(u.getAvatarThumb());
        mLiveBean.setLevelAnchor(u.getLevelAnchor());
        mLiveBean.setGoodNum(u.getGoodName());
        mLiveBean.setCity(u.getCity());


        btn_volume = findViewById(R.id.btn_volume);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        pauseBtn = findViewById(R.id.btn_play_pause);

        pauseBtn.setOnClickListener(v -> togglePauseResume());
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });
        findViewById(R.id.btn_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrevious();
            }
        });
        btn_volume.setOnClickListener(v -> muteMicrophone(v));

        discImage = findViewById(R.id.discImage);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(discImage, "rotation", 0f, 360f);
        rotate.setDuration(3000); // 3 seconds
        rotate.setRepeatCount(ValueAnimator.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.start();

        mediaControls = findViewById(R.id.floating_media); // This is now a ConstraintLayout
        floating_media_wrapper = findViewById(R.id.floating_media_wrapper);

        // Set the OnTouchListener directly on discImage for dragging
        discImage.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Ensure the discImage itself can be dragged, not just its parent (mediaControls)
                // We'll update the position of the parent (mediaControls) based on discImage's touch
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = mediaControls.getX() - event.getRawX();
                        dY = mediaControls.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mediaControls.animate() // Animate the parent layout
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        lastAction = MotionEvent.ACTION_MOVE;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            view.performClick(); // For the discImage's click listener
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        // Set the OnClickListener for discImage to toggle visibility
        discImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_media_wrapper.setElevation(50f);
                if (isMediaWrapperVisible) {
//                    floating_media_wrapper.animate()
//                            .translationX(floating_media_wrapper.getWidth()) // move right
//                            .alpha(0f)
//                            .setDuration(150)
//                            .withEndAction(() -> {
                                floating_media_wrapper.setVisibility(View.INVISIBLE); // Change to GONE
//                            })
//                            .start();
                } else {
                    floating_media_wrapper.setVisibility(View.VISIBLE);
//                    floating_media_wrapper.setAlpha(0f);
//                    floating_media_wrapper.setTranslationX(floating_media_wrapper.getWidth());
//                    floating_media_wrapper.animate()
//                            .translationX(0)
//                            .alpha(0.3f)
//                            .setDuration(150)
//                            .start();
                }
                isMediaWrapperVisible = !isMediaWrapperVisible;
            }
        });

        if (isChatRoom()) {

            Constants.CURRENT_IsNormalLive = false;
            if (isTxSdK()) {
                Log.i("1213213", "main: 6546546545645640000");

                mLiveChatRoomPushViewHolder = new LiveChatRoomPushTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container));
                mLiveChatRoomLinkMicViewHolder = new LiveChatRoomLinkMicTxViewHolder(mContext, mLiveChatRoomPushViewHolder.getContainer());
                mLiveChatRoomLinkMicViewHolder.addToParent();
                mLiveChatRoomLinkMicViewHolder.subscribeActivityLifeCycle();
                mLivePushViewHolder = mLiveChatRoomPushViewHolder;
            } else {
                Log.i("1213213", "main: 65465465456456411111");

                LiveChatRoomPushAgoraViewHolder liveChatRoomPushAgoraVh = new LiveChatRoomPushAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container));
                LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh = getLiveChatRoomLinkMicAgoraViewHolder(liveChatRoomPushAgoraVh);
                liveChatRoomPushAgoraVh.setLiveChatRoomLinkMicAgoraVh(liveChatRoomLinkMicAgoraVh);
                liveChatRoomLinkMicAgoraVh.addToParent();
                liveChatRoomLinkMicAgoraVh.subscribeActivityLifeCycle();
                mLiveChatRoomPushViewHolder = liveChatRoomPushAgoraVh;
                mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicAgoraVh;
                mLivePushViewHolder = liveChatRoomPushAgoraVh;
                mRtcEngine = liveChatRoomLinkMicAgoraVh.mEngine;
            }
        } else {
            Constants.CURRENT_IsNormalLive = true;
            if (isTxSdK()) {
                //添加推流预览控件
                if (mIsScreenRecord) {
                    Log.i("1213213", "main: 654654654564564222");
                    mLivePushViewHolder = new LivePushScreenTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container));
                } else {
                    Log.i("1213213", "main: 65465465456456433");

                    mLivePushViewHolder = new LivePushTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container), mLiveConfigBean);
                }
            } else {
                Log.i("1213213", "main: 654654654564564444");

                mLivePushViewHolder = new LivePushAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container), mIsScreenRecord, new LivePushAgoraViewHolder.Select() {
                    @Override
                    public void initRTC(RtcEngineEx mEngine1) {
                        Log.e(TAG, "initRTC: " );
                        mRtcEngine=mEngine1;

                    }
                });
            }
        }

        mLivePushViewHolder.setLivePushListener(new LivePushListener() {
            @Override
            public void onPreviewStart() {
                //开始预览回调
                mStartPreview = true;
            }

            @Override
            public void onPushStart() {
                //开始推流回调
                LiveHttpUtil.changeLive(mStream);
            }

            @Override
            public void onPushFailed() {
                //推流失败回调
                ToastUtil.show(com.livestreaming.common.R.string.live_push_failed);
            }
        });
        mLivePushViewHolder.addToParent();
        mLivePushViewHolder.subscribeActivityLifeCycle();
        mContainerWrap = (ViewGroup) findViewById(R.id.container_wrap);
        mContainer = (ViewGroup) findViewById(R.id.container);
        mStartPreview = true;
        //添加开播前设置控件
        if (!isChatRoom()) {
            mLiveReadyViewHolder = new LiveReadyViewHolder(mContext, mContainer, mLiveSDK, haveStore, forbidLiveTip, mIsScreenRecord);
        } else {
            mLiveReadyViewHolder = new LiveReadyChatRoomViewHolder(mContext, mContainer, mLiveSDK, haveStore, forbidLiveTip);
        }
        mLiveReadyViewHolder.addToParent();
        mLiveReadyViewHolder.subscribeActivityLifeCycle();
        if (!isChatRoom()) {
            mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePushViewHolder, true, mLiveSDK, mContainer);
            mLiveLinkMicPresenter.setLiveUid(mLiveUid);
            mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePushViewHolder, true, mLiveSDK, mRoot);
            mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePushViewHolder, true, mContainer);
        }
    }

    private void playNext() {
        if (queueList.isEmpty() || currentPlayingIndex == -1) return;
        currentPlayingIndex = (currentPlayingIndex + 1) % queueList.size();
        playFromQueueIndex(currentPlayingIndex);
    }

    private void playPrevious() {
        if (queueList.isEmpty() || currentPlayingIndex == -1) return;
        currentPlayingIndex = (currentPlayingIndex - 1 + queueList.size()) % queueList.size();
        playFromQueueIndex(currentPlayingIndex);
    }
    private void playFromQueueIndex(int index) {
        HashMap<String, String> song = queueList.get(index);
        stopAudio();
        playFromQueue(song.get("path"), song.get("id"));
    }
    public void playFromQueue(String filePath,String id) {
        if (!Objects.equals(filePath, "")) {
            mRtcEngine.startAudioMixing(filePath, false, 1, 0);

            mRtcEngine.adjustAudioMixingVolume(40);        // Local speaker volume
            mRtcEngine.adjustAudioMixingPublishVolume(40); // Remote volume

            mediaControls.setVisibility(View.VISIBLE);

            handler.postDelayed(() -> {
                durationMs = mRtcEngine.getAudioMixingDuration();
                if (durationMs > 0) {
                    seekBar.setMax(durationMs);
                    startUpdatingSeekBar();
                }
            }, 500);


        }
    }

    public void muteMicrophone(View view) {
        ImageView btn = (ImageView) view;
        boolean isMuted = !btn.isSelected(); // toggle state

        btn.setSelected(isMuted);
        btn.setImageResource(isMuted ? R.drawable.volume_mute : R.drawable.ic_volume);
        mRtcEngine.adjustAudioMixingVolume(isMuted ? 0 : 30);

    }

    public void stopAudio(View v) {
        mRtcEngine.stopAudioMixing();
        handler.removeCallbacksAndMessages(null);
        seekBar.setProgress(0);
        mediaControls.setVisibility(View.GONE);
    }

    private void showpkInvestRow(String pkObjString) {
        if (mLivePushViewHolder != null && mLiveType == 0) {
            if (pkObjString != null && pkObjString != "") {
                JSONObject obj = JSONObject.parseObject(pkObjString);
                JSONObject pk1 = JSONObject.parseObject(obj.getString("pk1"));
                JSONObject pk2 = JSONObject.parseObject(obj.getString("pk2"));
                String uid1 = pk1.getString("id");
                List<LiveUserGiftBean> list1 = JSON.parseArray(pk1.getString("list"), LiveUserGiftBean.class);
                String uid2 = pk2.getString("id");
                List<LiveUserGiftBean> list2 = JSON.parseArray(pk2.getString("list"), LiveUserGiftBean.class);

                if (uid1.equals(CommonAppConfig.getInstance().getUid())) {
                    ((LivePushAgoraViewHolder) mLivePushViewHolder).setInvestUsers(list1, list2);
                } else {
                    ((LivePushAgoraViewHolder) mLivePushViewHolder).setInvestUsers(list2, list1);
                }
            }
        }
    }

    @Override
    public void setInvestRowVisible(boolean b) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).setInvestRowVisible(b);
        }
    }

    @Override
    public void onGestCloseOpenCam(String touid, String avatar, Integer isOn) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).onLiveGestCamChanged(isOn == 1, touid, avatar);
        }
    }

    @Override
    public void handleMicGests(String touid, Integer isOn) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).onLiveGestMicChanged(isOn, touid);
        }
    }

    @Override
    public void onAnchorCloseLive() {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).releaseDouble();
        }
    }

    @Override
    public void onRoomBackgroundChanged(String backUrl) {

    }

    @NonNull
    private LiveChatRoomLinkMicAgoraViewHolder getLiveChatRoomLinkMicAgoraViewHolder(LiveChatRoomPushAgoraViewHolder liveChatRoomPushAgoraVh) {
        LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh = new LiveChatRoomLinkMicAgoraViewHolder(mContext, liveChatRoomPushAgoraVh.getContainer());
        liveChatRoomLinkMicAgoraVh.setLivePushListener(new LivePushListener() {
            @Override
            public void onPreviewStart() {
                //开始预览回调
                mStartPreview = true;
            }

            @Override
            public void onPushStart() {
                //开始推流回调
                LiveHttpUtil.changeLive(mStream);
            }

            @Override
            public void onPushFailed() {
                //推流失败回调
                ToastUtil.show(com.livestreaming.common.R.string.live_push_failed);
            }
        });
        return liveChatRoomLinkMicAgoraVh;
    }


    public boolean isStartPreview() {
        return mStartPreview || mIsScreenRecord;
    }

    /**
     * 主播直播间功能按钮点击事件
     *
     * @param functionID
     */
    @Override
    public void onClick(int functionID) {
        switch (functionID) {
            case Constants.LIVE_MP3_PLAYER:
                isShowBottomSheetRequested = true;
                checkAndRequestPermissions();
                break;
            case Constants.LIVE_FUNC_BEAUTY://美颜
                beauty();
                break;
            case Constants.LIVE_FUNC_CAMERA://切换镜头
                toggleCamera();
                break;
            case Constants.LIVE_FUNC_FLASH://切换闪光灯
                toggleFlash();
                break;
            case Constants.LIVE_FUNC_MUSIC://伴奏
                openMusicWindow();
                break;
            case Constants.LIVE_FUNC_SHARE://分享
                openShareWindow();
                break;
            case Constants.LIVE_FUNC_GAME://游戏
                // openGameWindow();
                break;
            case Constants.LIVE_FUNC_RED_PACK://红包
                openRedPackSendWindow();
                break;
            case Constants.LIVE_FUNC_LINK_MIC://连麦
                openLinkMicAnchorWindow();
                break;
            case Constants.LIVE_FUNC_MIRROR://镜像
                togglePushMirror();
                break;
            case Constants.LIVE_FUNC_TASK://每日任务
                openDailyTaskWindow();
                break;
            case Constants.ChangeRoomBack://每日任务
                openChangeRoomBackgroundDialog();
                break;
            case Constants.LIVE_FUNC_LUCK://幸运奖池
                openPrizePoolWindow();
                break;
            case Constants.LIVE_MIC_CON://幸运奖池
                openCloseMic();
                break;
            case Constants.LIVE_FUNC_LINK_MIC_AUD:
                if (mLiveAnchorViewHolder != null) {
                    mLiveAnchorViewHolder.changeLinkMicEnable();
                }
                break;
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(LiveAnchorActivity.this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LiveAnchorActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
            } else {
                fetchAllMp3FilesAndShowIfRequested();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(LiveAnchorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LiveAnchorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                fetchAllMp3FilesAndShowIfRequested();
            }
        } else {
            fetchAllMp3FilesAndShowIfRequested();
        }
    }

    private void fetchAllMp3FilesAndShowIfRequested() {
        mp3List.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                if (path != null && path.endsWith(".mp3")) {
                    // Normalize path for case-insensitive check
                    String lowerPath = path.toLowerCase();

                    if (path.endsWith(".mp3")) {
//                        if (path.contains("/emulated/0/music/") || path.contains("/emulated/0/Music/") ||
//                                path.contains("/emulated/0/download/") || path.contains("/emulated/0/Download/")) {

                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", id);
                            item.put("name", name);
                            item.put("path", path);
//                            item.put("duration", formattedDuration);
                            mp3List.add(item);
                        }
//                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (isShowBottomSheetRequested) {
            showBottomSheet();
            isShowBottomSheetRequested = false;
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatDuration(long durationMs) {
        long minutes = (durationMs / 1000) / 60;
        long seconds = (durationMs / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);

        RecyclerView recyclerView = view.findViewById(R.id.mp3RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        LinearLayout searchBarLayout = view.findViewById(R.id.searchBarLayout);
        EditText searchInput = view.findViewById(R.id.searchInput);
        ImageView clearIcon = view.findViewById(R.id.clearIcon);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        ImageView searchIcon = view.findViewById(R.id.searchIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView emptyView = view.findViewById(R.id.emptyView);

        Mp3Adapter adapter = new Mp3Adapter(mp3List, (path, id) -> {

            seekBar.setProgress(0);
            setFilePah(path);
            setId(id);
            queueList.clear();
            queueList.addAll(mp3List);
            currentPlayingIndex = getMp3IndexById(id);

            mediaControls.setVisibility(View.VISIBLE);
            playAudio(path);
            bottomSheetDialog.dismiss();

        });
        recyclerView.setAdapter(adapter);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBarLayout.setVisibility(View.GONE);
                tvTitle.setVisibility(View.VISIBLE);
                bottomSheetDialog.hide();
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBarLayout.getVisibility() == View.VISIBLE) {
                    // Hide search bar, show title
                    searchInput.setText("");
                    searchBarLayout.setVisibility(View.GONE);
                    tvTitle.setVisibility(View.VISIBLE);
                } else {
                    // Show search bar, hide title
                    searchInput.setText("");
                    searchBarLayout.setVisibility(View.VISIBLE);
                    tvTitle.setVisibility(View.GONE);
                }
            }
        });

        // Live Search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                // Show or hide empty view based on result
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
                clearIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear input
        clearIcon.setOnClickListener(v -> {
            searchInput.setText("");
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private int getMp3IndexById(String id) {
        for (int i = 0; i < mp3List.size(); i++) {
            if (mp3List.get(i).get("id").equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAllMp3FilesAndShowIfRequested();
        }
//        else if (requestCode == INITIAL_PERMISSIONS_CODE) {
//            initAgoraEngine();
//        }
    }

    public void playAudio(String path) {
            mRtcEngine.startAudioMixing(getFilePah(), false, 1, 0);
            mRtcEngine.adjustAudioMixingVolume(40);        // Local speaker volume
            mRtcEngine.adjustAudioMixingPublishVolume(40); // Remote volume

            mediaControls.setVisibility(View.VISIBLE);
            handler.postDelayed(() -> {
                durationMs = mRtcEngine.getAudioMixingDuration();
                if (durationMs > 0) {
                    seekBar.setMax(durationMs);
                    startUpdatingSeekBar();
                }
            }, 500);



    }
    private void startUpdatingSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused && mRtcEngine != null) {
                    int currentPos = mRtcEngine.getAudioMixingCurrentPosition();
                    seekBar.setProgress(currentPos);
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void togglePauseResume() {
        if (isPaused) {
            mRtcEngine.resumeAudioMixing();
            pauseBtn.setImageResource(R.drawable.ic_pause);
        } else {
            mRtcEngine.pauseAudioMixing();
            pauseBtn.setImageResource(R.drawable.play);
        }
        isPaused = !isPaused;
    }

    public void stopAudio() {
        mRtcEngine.stopAudioMixing();
        handler.removeCallbacksAndMessages(null);
        seekBar.setProgress(0);
        mediaControls.setVisibility(View.GONE);
    }

    private void openChangeRoomBackgroundDialog() {
        LiveChooseBackgroundDialogFragment fragment = new LiveChooseBackgroundDialogFragment();
        fragment.onBackChangedDone = new LiveChooseBackgroundDialogFragment.OnBackChangeDoneListener() {
            @Override
            public void onClick(ChangeRoomBackBean bean) {
                onChangeRoomBack(bean);
            }
        };
        fragment.show(getSupportFragmentManager(), "");
    }

    private void onChangeRoomBack(ChangeRoomBackBean bean) {
        mLiveChatRoomPushViewHolder.changeRoomBack(bean);
        SocketVoiceRoomUtil.changeRoomBack(mSocketClient, bean);
        LiveHttpUtil.addChangeRoomBackground(bean, new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {

            }
        });
    }

    private void openCloseMic() {
        if (isMicOpen == 1) {
            isMicOpen = 0;
            mLivePushViewHolder.controlMic(false);
        } else if (isMicOpen == 0) {
            isMicOpen = 1;
            mLivePushViewHolder.controlMic(true);
        } else {
            isMicOpen = -1;
        }

    }

    /**
     * 切换镜像
     */
    private void togglePushMirror() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.togglePushMirror();
        }
    }


    public void openShop(boolean isOpen) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setShopBtnVisible(isOpen);
        }
    }


    //打开相机前执行
    public void beforeCamera() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.setOpenCamera(true);
        }
    }


    /**
     * 切换镜头
     */
    public void toggleCamera() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.toggleCamera();
        }
    }

    /**
     * 切换闪光灯
     */
    public void toggleFlash() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.toggleFlash();
        }
    }

    /**
     * 设置美颜
     */

    public void beauty() {
        if (mLiveBeautyViewHolder == null) {
            if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
                mLiveBeautyViewHolder = new BeautyViewHolder(mContext, mRoot);
            } else {
                mLiveBeautyViewHolder = new SimpleBeautyViewHolder(mContext, mRoot, isTxSdK());
            }
            mLiveBeautyViewHolder.setVisibleListener(new IBeautyViewHolder.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    if (mLiveReadyViewHolder != null) {
                        if (visible) {
                            mLiveReadyViewHolder.hide();
                        } else {
                            mLiveReadyViewHolder.show();
                        }
                    }
                }
            });
        }
        mLiveBeautyViewHolder.show();
    }


    /**
     * 打开音乐窗口
     */
    private void openMusicWindow() {
        if (isLinkMic() || isLinkMicAnchor()) {
            ToastUtil.show(com.livestreaming.common.R.string.link_mic_not_bgm);
            return;
        }
        LiveMusicDialogFragment fragment = new LiveMusicDialogFragment();
        fragment.setActionListener(new LiveMusicDialogFragment.ActionListener() {
            @Override
            public void onChoose(String musicId) {
                if (mLivePushViewHolder != null) {
                    if (mLiveMusicViewHolder == null) {
                        mLiveMusicViewHolder = new LiveMusicViewHolder(mContext, mContainer, mLivePushViewHolder);
                        mLiveMusicViewHolder.subscribeActivityLifeCycle();
                        mLiveMusicViewHolder.addToParent();
                    }
                    mLiveMusicViewHolder.play(musicId);
                    mBgmPlaying = true;
                }
            }
        });
        fragment.show(getSupportFragmentManager(), "LiveMusicDialogFragment");
    }

    /**
     * 关闭背景音乐
     */
    public void stopBgm() {
        if (mLiveMusicViewHolder != null) {
            mLiveMusicViewHolder.release();
        }
        mLiveMusicViewHolder = null;
        mBgmPlaying = false;
    }

    public boolean isBgmPlaying() {
        return mBgmPlaying;
    }


    /**
     * 打开功能弹窗
     */
    public void showFunctionDialog() {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        Bundle bundle = new Bundle();
        boolean hasGame = false;
        if (CommonAppConfig.GAME_ENABLE && mGameList != null) {
            hasGame = mGameList.size() > 0;
        }
        bundle.putBoolean(Constants.HAS_GAME, hasGame);
        bundle.putBoolean(Constants.OPEN_FLASH, mLivePushViewHolder != null && mLivePushViewHolder.isFlashOpen());
        bundle.putBoolean("TASK", mTaskSwitch);
        bundle.putBoolean("screenRecord", mIsScreenRecord);
        bundle.putInt("isMicOpen", isMicOpen);
        boolean isLinkMic = false;
        if (mLiveAnchorViewHolder != null) {
            isLinkMic = mLiveAnchorViewHolder.isLinkMicEnable();
        }
        bundle.putBoolean(Constants.LINK_MIC, isLinkMic);
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    public void hideLinkMicAnchorWindow() {
        if (mLiveLinkMicListDialogFragment != null) {
            mLiveLinkMicListDialogFragment.dismissAllowingStateLoss();
        }
        mLiveLinkMicListDialogFragment = null;
    }

    public void hideLinkMicAnchorWindow2() {
        mLiveLinkMicListDialogFragment = null;
    }

    /**
     * 打开主播连麦窗口
     */
    private void openLinkMicAnchorWindow() {
        if (mLiveLinkMicAnchorPresenter != null && !mLiveLinkMicAnchorPresenter.canOpenLinkMicAnchor()) {
            return;
        }
        LiveLinkMicListDialogFragment fragment = new LiveLinkMicListDialogFragment();
        mLiveLinkMicListDialogFragment = fragment;
        fragment.show(getSupportFragmentManager(), "LiveLinkMicListDialogFragment");
    }


    /**
     * 打开选择游戏窗口
     */


    /**
     * 关闭游戏
     */
    public void closeGame() {
        if (mGamePresenter != null) {
            mGamePresenter.closeGame();
        }
    }

    /**
     * 开播成功
     *
     * @param data createRoom返回的数据
     */
    public void startLiveSuccess(String data, int liveType, int liveTypeVal, String title) {
        if (isChatRoom()) {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.setChatRoomType(mChatRoomType, CommonAppConfig.getInstance().getUserBean(), true);
                if (mLiveChatRoomPushViewHolder != null) {
                    if (isChatRoomTypeVideo()) {
                        mLiveChatRoomPushViewHolder.startPreview(mLiveConfigBean, mLiveChatRoomLinkMicViewHolder.getFirstPreview());
                    } else {
                        mLiveChatRoomPushViewHolder.startMicrophone();
                    }
                }
                JSONObject object = JSONObject.parseObject(data).getJSONObject("frame");
                if (object != null) {
                    mLiveBean.setFrame(object.getString("thumb"));
                }
                JSONObject objBack = JSONObject.parseObject(data).getJSONObject("background");
                if (objBack != null) {
                    mLiveChatRoomPushViewHolder.changeRoomBack(objBack.getString("thumb"));
                }
            }
        }
        mLiveType = liveType;
        mLiveTypeVal = liveTypeVal;
        if (mLiveBean != null) {
            mLiveBean.setTitle(title);
        }
        //处理createRoom返回的数据
        JSONObject obj = JSON.parseObject(data);
        mStream = obj.getString("stream");
        mDanmuPrice = obj.getString("barrage_fee");
        String playUrl = obj.getString("pull");
        L.e("createRoom----播放地址--->" + playUrl);
        mLiveBean.setPull(playUrl);
        mTxAppId = obj.getString("tx_appid");
        
        // Save beauty parameters when starting a live stream so audiences can see them
        if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
            com.livestreaming.beauty.utils.MhDataManager.getInstance().saveBeautyValue();
        } else {
            com.livestreaming.beauty.utils.SimpleDataManager.getInstance().saveBeautyValue();
        }
        
        //移除开播前的设置控件，添加直播间控件
        if (mLiveReadyViewHolder != null) {
            mLiveReadyViewHolder.removeFromParent();
            mLiveReadyViewHolder.release();
        }
        mLiveReadyViewHolder = null;
        if (mLiveRoomViewHolder == null) {
            mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) findViewById(R.id.gift_gif), (SVGAImageView) findViewById(R.id.gift_svga), mContainerWrap, true);
            mLiveRoomViewHolder.onLiveUserListChanged = new OnListChangedListener() {
                @Override
                public void onUserListChanged(List<LiveUserGiftBean> list, String pkObjString) {

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
            mLiveRoomViewHolder.setLiveInfo(mLiveUid, mStream, obj.getIntValue("userlist_time") * 1000);
            mLiveRoomViewHolder.setVotes(obj.getString("votestotal"));
            mLiveRoomViewHolder.setUserNum(0);
            if (!isChatRoom()) {
                if (mLiveRoomViewHolder != null) {
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
                }
            }
            if (mLiveBean.getFrame() != null) {
                mLiveRoomViewHolder.setLiveOnerFrame(mLiveBean.getFrame());
            } else {
                mLiveRoomViewHolder.setLiveOnerFrame(null);
            }
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null) {
                mLiveRoomViewHolder.setRoomNum(u.getLiangNameTip());
                mLiveRoomViewHolder.setName(u.getUserNiceName());
                mLiveRoomViewHolder.setAvatar(u.getAvatar());
                mLiveRoomViewHolder.setAnchorLevel(u.getLevelAnchor());
            }
            mLiveRoomViewHolder.startAnchorLight();
        }

        //start streaming
        if (mLivePushViewHolder != null) {
            if (isTxSdK()) {
                if (mIsScreenRecord) {
                    startService(new Intent(this, ScreenRecordService.class));
                }
                mLivePushViewHolder.startPush(obj.getString("push"));
            } else {
                mLivePushViewHolder.startPushAgora(obj.getString("user_sw_token"), mStream);
            }
        }
        //开始显示直播时长
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.startAnchorLiveTime();
            mLiveRoomViewHolder.startAnchorCheckLive();
        }
        mStartLive = true;
        mLiveRoomViewHolder.startRefreshUserList();

        //守护相关
        mLiveGuardInfo = new LiveGuardInfo();
        int guardNum = obj.getIntValue("guard_nums");
        mLiveGuardInfo.setGuardNum(guardNum);
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setGuardNum(guardNum);
        }

        //连接socket
        if (mSocketClient == null) {
            mSocketClient = new SocketClient(obj.getString("chatserver"), this);
        }
        mSocketClient.connect(mLiveUid, mStream);

        int dailytask_switch = obj.getIntValue("dailytask_switch");
        mTaskSwitch = dailytask_switch == 1;
        if (!isChatRoom()) {
            if (mLiveAnchorViewHolder == null) {
                mLiveAnchorViewHolder = new LiveAnchorViewHolder(mContext, mContainer);
                mLiveAnchorViewHolder.addToParent();
                //mLiveAnchorViewHolder.setUnReadCount(((LiveActivity) mContext).getImUnReadCount());
            }
            mLiveBottomViewHolder = mLiveAnchorViewHolder;

            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.setSocketClient(mSocketClient);
            }
            if (mLiveLinkMicAnchorPresenter != null) {
                mLiveLinkMicAnchorPresenter.setSocketClient(mSocketClient);
                mLiveLinkMicAnchorPresenter.setPlayUrl(playUrl);
                mLiveLinkMicAnchorPresenter.setSelfStream(mStream);
            }
            if (mLiveLinkMicPkPresenter != null) {
                mLiveLinkMicPkPresenter.setSocketClient(mSocketClient);
                mLiveLinkMicPkPresenter.setLiveUid(mLiveUid);
                mLiveLinkMicPkPresenter.setSelfStream(mStream);
            }

            //奖池等级
            int giftPrizePoolLevel = obj.getIntValue("jackpot_level");

            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.showBtn(false, giftPrizePoolLevel, dailytask_switch);
            }
            //游戏相关
            if (CommonAppConfig.GAME_ENABLE) {
                mGameList = JSON.parseArray(obj.getString("game_switch"), Integer.class);
                GameParam param = new GameParam();
                param.setContext(mContext);
                param.setParentView(mContainerWrap);
                param.setTopView(mContainer);
                param.setInnerContainer(mLiveRoomViewHolder.getInnerContainer());
                param.setGameActionListener(new GameActionListenerImpl(LiveAnchorActivity.this, mSocketClient));
                param.setLiveUid(mLiveUid);
                param.setStream(mStream);
                param.setAnchor(true);
                param.setCoinName(CommonAppConfig.getInstance().getScoreName());
                param.setObj(obj);
                mGamePresenter = new GamePresenter(param);
                mGamePresenter.setGameList(mGameList);
            }

        } else {
            if (mLiveVoiceAnchorViewHolder == null) {
                mLiveVoiceAnchorViewHolder = new LiveVoiceAnchorViewHolder(mContext, mContainer);
                mLiveVoiceAnchorViewHolder.addToParent();
                //  mLiveVoiceAnchorViewHolder.setUnReadCount(((LiveActivity) mContext).getImUnReadCount());
            }
            mLiveBottomViewHolder = mLiveVoiceAnchorViewHolder;
        }
    }


    public void updateChatRoomMicContributionList(Map<String, Integer> map) {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            ((LiveChatRoomLinkMicAgoraViewHolder) mLiveChatRoomLinkMicViewHolder).handleMicListVotesChanged(map);
        }
    }

    /**
     * 关闭直播
     */
    public void closeLive() {
        if (isChatRoom()) {
            DialogUitl.showStringArrayDialog(mContext,
                    new Integer[][]{
                            {com.livestreaming.common.R.string.a_057, ContextCompat.getColor(mContext, com.livestreaming.common.R.color.red)}},
                    new DialogUitl.StringArrayDialogCallback() {
                        @Override
                        public void onItemClick(String text, int tag) {
                            if (tag == com.livestreaming.common.R.string.a_057) {
                                endLive();
                            }
                        }
                    });
        } else {
            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.live_end_live), new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    endLive();
                }
            });
        }
    }


    /**
     * 结束直播
     */
    public void endLive() {
        if (mEnd) {
            return;
        }
        mEnd = true;
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.closeLinkMic();
        }
        //请求关播的接口
        LiveHttpUtil.stopLive(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    //断开socket
                    if (mSocketClient != null) {
                        SocketLinkMicUtil.onAnchoreCloseLive(mSocketClient);
                        mSocketClient.disConnect();
                        mSocketClient = null;
                    }

                    if (mLiveEndViewHolder == null) {
                        mLiveEndViewHolder = new LiveEndViewHolder(mContext, mRoot, true);
                        mLiveEndViewHolder.subscribeActivityLifeCycle();
                        mLiveEndViewHolder.addToParent();
                        mLiveEndViewHolder.showData(mLiveBean, mStream);
                        stopAudio();
                    }
                    release();
                    mStartLive = false;
                    if (mSuperCloseLive) {

                        LiveHttpUtil.getLiveBanInfo(new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0 && info.length > 0) {
                                    JSONObject obj = JSON.parseObject(info[0]);
                                    String banMsg = obj.getString("ban_msg");
                                    String banNum = obj.getString("ban_num");
                                    CharSequence tip = banMsg;
                                    int startIndex = banMsg.indexOf(banNum);
                                    if (startIndex >= 0) {
                                        SpannableString spannableString = new SpannableString(banMsg);
                                        spannableString.setSpan(new ForegroundColorSpan(0xffff0000), startIndex, startIndex + banNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        tip = spannableString;
                                    }
                                    showSuperAdminTipDialog(tip);

                                }
                            }
                        });


                    }
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.live_end_ing));
            }

            @Override
            public void onLoginInvalid() {
                mStartLive = false;
                release();
                finish();
                CommonAppConfig.getInstance().clearLoginInfo();
                //退出IM
                ImMessageUtil.getInstance().logoutImClient();
                if (mSuperCloseLive) {
                    RouteUtil.forwardLogin(mContext, WordUtil.getString(com.livestreaming.common.R.string.live_illegal));
                } else if (mLoginInvalid) {
                    RouteUtil.forwardLogin(mContext, WordUtil.getString(com.livestreaming.common.R.string.login_tip_0));
                } else {
                    RouteUtil.forwardLogin(mContext);
                }
            }

            @Override
            public boolean isUseLoginInvalid() {
                return true;
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (mLiveBeautyViewHolder != null && mLiveBeautyViewHolder.isShowed()) {
            mLiveBeautyViewHolder.hide();
            return;
        }
        if (mStartLive) {
            if (!canBackPressed()) {
                return;
            }
            closeLive();
        } else {
            if (mLivePushViewHolder != null) {
                mLivePushViewHolder.release();
            }
            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.release();
            }
            mLivePushViewHolder = null;
            mLiveLinkMicPresenter = null;
            superBackPressed();
        }
    }


    public void superBackPressed() {
        super.onBackPressed();
    }

    public void release() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
            mSocketClient = null;
        }
        LiveHttpUtil.cancel(LiveHttpConsts.CHANGE_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.STOP_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_LINK_MIC_ENABLE);
        CommonHttpUtil.cancel(CommonHttpConsts.CHECK_TOKEN_INVALID);
        if (mLiveReadyViewHolder != null) {
            mLiveReadyViewHolder.release();
        }
        if (mLiveMusicViewHolder != null) {
            mLiveMusicViewHolder.release();
        }
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.release();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.release();
        }
        if (mGamePresenter != null) {
            mGamePresenter.release();
        }
        mLiveMusicViewHolder = null;
        mLiveReadyViewHolder = null;
        mLivePushViewHolder = null;
        mLiveLinkMicPresenter = null;
        mLiveBeautyViewHolder = null;
        mGamePresenter = null;
        super.release();
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.ANCHOR_CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_BAN_INFO);
        super.onDestroy();
        RtcEngineEx.destroy();
        mRtcEngine = null;
        L.e("LiveAnchorActivity-------onDestroy------->");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (!isChatRoom() && !mIsScreenRecord) {
            if (mNeedCloseLive && mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.anchorPause();
            }
            sendSystemMessage("The anchor leaves for a while, the excitement will not be interrupted, don't go away",
                    "The anchor leaves for a while, the excitement will not be interrupted, don't go away");
        }
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isChatRoom() && !mIsScreenRecord && mPaused) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.anchorResume();
            }
            sendSystemMessage("The host is back!", "The host is back!");
            CommonHttpUtil.checkTokenInvalid();
        }
        mPaused = false;
        mNeedCloseLive = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        onAnchorInvalid();
    }

    /**
     * 直播间  主播登录失效
     */
    @Override
    public void onAnchorInvalid() {
        mLoginInvalid = true;
        super.onAnchorInvalid();
        endLive();
    }

    /**
     * 超管关闭直播间
     */
    @Override
    public void onSuperCloseLive() {
        mSuperCloseLive = true;
        super.onAnchorInvalid();
        endLive();
    }


    /**
     * 超管给直播间发过来的提示信息
     *
     * @param tip
     */
    private void showSuperAdminTipDialog(CharSequence tip) {
        SuperAdminTipDialogFragment fragment = new SuperAdminTipDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(Constants.TIP, tip);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "SuperAdminTipDialogFragment");
    }

    /**
     * 踢人
     */
    @Override
    public void onKick(String touid) {
        if (mLivePushViewHolder != null) {
            JSONObject obj = new JSONObject();
            obj.put("method", "kick");
            obj.put("touid", touid);
            mLivePushViewHolder.sendSeiMessage(obj.toJSONString());
        }
    }

    /**
     * 直播间警告
     *
     * @param warningTip 警告提示语
     */
    @Override
    public void onLiveRoomWarning(String warningTip) {
        showSuperAdminTipDialog(warningTip);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameWindowChangedEvent(GameWindowChangedEvent e) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setOffsetY(e.getGameViewHeight());
        }
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setGameBtnVisible(e.isOpen());
        }
    }


    /**
     * 主播与主播连麦  主播收到其他主播发过来的连麦申请
     */
    @Override
    public void onLinkMicAnchorApply(UserBean u, String stream) {
        if (mLivePushViewHolder.getIsInGestOrPk()) {
            if (mLiveLinkMicAnchorPresenter != null) {
                mLiveLinkMicAnchorPresenter.onLinkMicAnchorApply(u, stream);
            }
        } else {
            SocketLinkMicUtil.anchorBusy(mSocketClient, u.getId());
        }

    }

    /**
     * 主播与主播连麦  对方主播拒绝连麦的回调
     */
    @Override
    public void onLinkMicAnchorRefuse() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorRefuse();
            setStartPkBtnVisible(true);
        }
    }

    /**
     * 主播与主播连麦  对方主播无响应的回调
     */
    @Override
    public void onLinkMicAnchorNotResponse() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicNotResponse();
            setStartPkBtnVisible(true);
        }
    }

    /**
     * 主播与主播连麦  对方主播正在游戏
     */
    @Override
    public void onlinkMicPlayGaming() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onlinkMicPlayGaming();
        }
    }


    /**
     * 主播与主播连麦  对方主播正在忙的回调
     */
    @Override
    public void onLinkMicAnchorBusy() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorBusy();
        }
    }

    /**
     * 发起主播连麦申请
     *
     * @param pkUid  对方主播的uid
     * @param stream 对方主播的stream
     */
    public void linkMicAnchorApply(final String pkUid, final String stream) {
        if (isBgmPlaying()) {
            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.link_mic_close_bgm), new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    stopBgm();
                    checkLiveAnchorMic(pkUid, stream);
                }
            });
        } else {
            checkLiveAnchorMic(pkUid, stream);
        }
    }

    /**
     * 发起主播连麦申请
     *
     * @param pkUid  对方主播的uid
     * @param stream 对方主播的stream
     */
    private void checkLiveAnchorMic(final String pkUid, String stream) {
        if (mLivePushViewHolder.getIsInGestOrPk()) {
            LiveHttpUtil.livePkCheckLive(pkUid, stream, mStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        if (isTxSdK()) {
                            String playUrl = null;
                            JSONObject obj = JSON.parseObject(info[0]);
                            if (obj != null) {
                                String accUrl = obj.getString("pull");//自己主播的低延时流
                                if (!TextUtils.isEmpty(accUrl)) {
                                    playUrl = accUrl;
                                }
                            }
                            if (mLiveLinkMicAnchorPresenter != null) {
                                mLiveLinkMicAnchorPresenter.applyLinkMicAnchor(pkUid, playUrl, mStream);
                            }
                        } else {
                            if (mLiveLinkMicAnchorPresenter != null) {
                                mLiveLinkMicAnchorPresenter.applyLinkMicAnchor(pkUid, null, mStream);
                            }
                        }
                    } else {
                        ToastUtil.show(msg);
                    }
                }
            });
        }
    }

    /**
     * 设置连麦pk按钮是否可见
     */
    public void setPkBtnVisible(boolean visible) {
        if (mLiveAnchorViewHolder != null) {
            if (visible) {
                if (mLiveLinkMicAnchorPresenter.isLinkMic()) {
                    mLiveAnchorViewHolder.setPkCancelBtnVisible(false);
                    setCloseVisible(true);
                }
            } else {
                mLiveAnchorViewHolder.setPkCancelBtnVisible(true);
                ((LivePushAgoraViewHolder) mLivePushViewHolder).setLiveLinkMicImageVisible(false);
            }
        }
    }

    public void hidePkBtns() {
        mLiveAnchorViewHolder.hidePkBtns();
    }

    public void onClickCloseAnchorLinkMicBtn() {
        if (mLiveLinkMicAnchorPresenter != null) {
            if (mLiveLinkMicAnchorPresenter.isLinkMic()) {
                mLiveLinkMicAnchorPresenter.closeLinkMic();
                handleLiveLinkCameras(false);
                hidePkBtns();
            } else {
                openLinkMicAnchorWindow();
            }
        }
    }

    public void onClickCancelPK() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.closeLinkMicPk();
            setStartPkBtnVisible(true);
            setCloseVisible(true);
        }
    }

    public void setCloseVisible(boolean isVis) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setClosePkBtnVisible(isVis);
        }
    }

    /**
     * 发起主播连麦pk
     */
    public void applyLinkMicPk() {
        String pkUid = null;
        if (mLiveLinkMicAnchorPresenter != null) {
            pkUid = mLiveLinkMicAnchorPresenter.getPkUid();
        }
        if (!TextUtils.isEmpty(pkUid) && mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.applyLinkMicPk(pkUid, mStream);
        }
    }

    /**
     * 主播与主播PK  主播收到对方主播发过来的PK申请的回调
     *
     * @param u      对方主播的信息
     * @param stream 对方主播的stream
     */
    @Override
    public void onLinkMicPkApply(UserBean u, String stream) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkApply(u, stream);
        }
    }

    /**
     * 主播与主播PK  对方主播拒绝pk的回调
     */
    @Override
    public void onLinkMicPkRefuse() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkRefuse();
        }
    }

    /**
     * 主播与主播PK   对方主播正在忙的回调
     */
    @Override
    public void onLinkMicPkBusy() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkBusy();
        }
    }

    /**
     * 主播与主播PK   对方主播无响应的回调
     */
    @Override
    public void onLinkMicPkNotResponse() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkNotResponse();
        }
    }

    /**
     * 腾讯sdk连麦时候主播混流
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMicTxMixStreamEvent(LinkMicTxMixStreamEvent e) {
        if (mLivePushViewHolder != null && mLivePushViewHolder instanceof TxLinkMicPusher) {
            ((TxLinkMicPusher) mLivePushViewHolder).onLinkMicTxMixStreamEvent(e.getType(), e.getToUid(), e.getToStream());
        }
    }

    /**
     * 主播checkLive
     */
    public void checkLive() {
        if (mCheckLiveCallback == null) {
            mCheckLiveCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        int status = JSON.parseObject(info[0]).getIntValue("status");
                        printLog(DateFormatUtil.getCurTimeString2() + " <=== " + mReqCount + "----status=" + status + "\n");
                        if (status == 0) {
                            NotCancelableDialog dialog = new NotCancelableDialog();
                            dialog.setContent(WordUtil.getString(com.livestreaming.common.R.string.live_anchor_error));
                            dialog.setActionListener(new NotCancelableDialog.ActionListener() {
                                @Override
                                public void onConfirmClick(Context context, DialogFragment dialog) {
                                    dialog.dismiss();
                                    release();
                                    superBackPressed();
                                }
                            });
                            dialog.show(getSupportFragmentManager(), "VersionUpdateDialog");
                        }
                    }
                }

            };
        }
        mReqCount++;
        printLog(DateFormatUtil.getCurTimeString2() + " ===> " + mReqCount + "\n");
        LiveHttpUtil.anchorCheckLive(mLiveUid, mStream, mCheckLiveCallback);
    }


    private void printLog(String content) {
        if (mLogFile == null) {
            File dir = new File(CommonAppConfig.LOG_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mLogFile = new File(dir, DateFormatUtil.getCurTimeString2() + "_" + mLiveUid + "_" + mStream + ".txt");
        }
//        L.e(TAG, content);
        LogUtil.print(mLogFile, content);
    }

    /**
     * 打开商品窗口
     */
    public void openGoodsWindow() {
        LiveShopDialogFragment fragment = new LiveShopDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveShopDialogFragment");
    }


    public void forwardAddGoods() {
        if (mLiveGoodsAddViewHolder == null) {
            mLiveGoodsAddViewHolder = new LiveGoodsAddViewHolder(mContext, mPageContainer);
            mLiveGoodsAddViewHolder.subscribeActivityLifeCycle();
            mLiveGoodsAddViewHolder.addToParent();
        }
        mLiveGoodsAddViewHolder.show();
    }

    /**
     * 添加代卖的平台商品
     */
    public void forwardAddPlatGoods() {
        if (mLivePlatGoodsAddViewHolder == null) {
            mLivePlatGoodsAddViewHolder = new LivePlatGoodsAddViewHolder(mContext, mPageContainer);
            mLivePlatGoodsAddViewHolder.subscribeActivityLifeCycle();
            mLivePlatGoodsAddViewHolder.addToParent();
        }
        mLivePlatGoodsAddViewHolder.show();
    }


    @Override
    protected boolean canBackPressed() {
        if (mLiveGoodsAddViewHolder != null && mLiveGoodsAddViewHolder.isShowed()) {
            mLiveGoodsAddViewHolder.hide();
            return false;
        }
        if (mLivePlatGoodsAddViewHolder != null && mLivePlatGoodsAddViewHolder.isShowed()) {
            mLivePlatGoodsAddViewHolder.hide();
            return false;
        }
        return super.canBackPressed();
    }

    /**
     * 显示道具礼物
     */
    public void showStickerGift(LiveReceiveGiftBean bean) {
        if (CommonAppConfig.getInstance().isMhBeautyEnable() && mLivePushViewHolder != null) {
            String stickerGiftId = bean.getStickerId();
            float playTime = bean.getPlayTime();
            if (!TextUtils.isEmpty(stickerGiftId) && playTime > 0) {
                mLivePushViewHolder.setLiveStickerGift(stickerGiftId, (long) (playTime * 1000));
            }
        }
    }


    /**
     * 发送展示直播间商品的消息
     */
    public void sendLiveGoodsShow(GoodsBean goodsBean) {
        SocketChatUtil.sendLiveGoodsShow(mSocketClient, goodsBean);
    }


    /**
     * 语音聊天室--主播打开控麦窗口
     */
    public void controlMic() {
        LiveVoiceControlFragment fragment = new LiveVoiceControlFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_STREAM, mStream);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveVoiceControlFragment");
    }


    /**
     * 语音聊天室--主播收到观众申请上麦
     */
    @Override
    public void onVoiceRoomApplyUpMic() {
        if (mLiveVoiceAnchorViewHolder != null) {
            mLiveVoiceAnchorViewHolder.setApplyUpMicTipShow(true);
            mLiveVoiceAnchorViewHolder.changeCamBtnVisibility(true);
        }
    }

    /**
     * 语音聊天室--主播同意或拒绝用户的上麦申请
     *
     * @param position 上麦的位置 从0开始 -1表示拒绝
     */
    public void handleMicUpApply(UserBean bean, int position) {
        SocketVoiceRoomUtil.handleMicUpApply(mSocketClient, bean, position);
    }


    /**
     * 语音聊天室--所有人收到某人下麦的消息
     *
     * @param uid 下麦的人的uid
     */
    @Override
    public void onVoiceRoomDownMic(String uid, int type) {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            int position = mLiveChatRoomLinkMicViewHolder.getUserPosition(uid);
            if (position != -1) {
                mLiveChatRoomLinkMicViewHolder.onUserDownMic(position);
                if (isTxSdK()) {
                    mLiveChatRoomLinkMicViewHolder.stopPlay(position);//停止播放被下麦的人的流
                } else {
                    mLiveChatRoomLinkMicViewHolder.stopPlay(uid);//停止播放被下麦的人的流
                }
                EventBus.getDefault().post(new LiveVoiceMicStatusEvent(position, Constants.VOICE_CTRL_EMPTY));
                if (isTxSdK()) {
                    liveChatRoomAnchorMix();//重新混流
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
        EventBus.getDefault().post(new LiveVoiceMicStatusEvent(position, status));
        if (isChatRoomTypeVideo() && !TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {//被操作人是自己
            if (status == Constants.VOICE_CTRL_OPEN) {
//                ToastUtil.show(R.string.a_056);
                if (mLiveChatRoomPushViewHolder != null) {
                    mLiveChatRoomPushViewHolder.setPushMute(false);
                }
            } else if (status == Constants.VOICE_CTRL_CLOSE) {
//                ToastUtil.show(R.string.a_055);
                if (mLiveChatRoomPushViewHolder != null) {
                    mLiveChatRoomPushViewHolder.setPushMute(true);
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
        if (isTxSdK()) {
            LiveHttpUtil.getNewUpMicPullUrl(userStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        String playUrl = JSON.parseObject(info[0]).getString("play_url");
                        if (mLiveChatRoomLinkMicViewHolder != null) {
                            mLiveChatRoomLinkMicViewHolder.playAccStream(uid, playUrl, userStream);
                        }
                        liveChatRoomAnchorMix();//重新混流
                    }
                }
            });
        } else {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.playAccStream(uid, "", userStream);
            }
        }
    }

    @Override
    public void onVoiceRoomUserCam(String uid, int type, boolean isFromAudiance) {
        if (isFromAudiance) {
            closeOpenCam(false);
        } else {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.onCamChange(uid, type);
            }
        }
    }

    /**
     * 语音直播间主播混流
     */
    private void liveChatRoomAnchorMix() {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            List<LiveVoiceMixUserBean> userStreamList = mLiveChatRoomLinkMicViewHolder.getUserStreamForMix();
            if (mLiveChatRoomPushViewHolder != null) {
                if (isChatRoomTypeVideo()) {
                    mLiveChatRoomPushViewHolder.voiceRoomAnchorMixVideo(userStreamList);
                } else {
                    mLiveChatRoomPushViewHolder.voiceRoomAnchorMix(userStreamList);
                }
            }
        }
    }

    /**
     * 监听私信未读消息数变化事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        if (unReadCount != null) {
            String count = unReadCount.getLiveRoomUnReadCount();
            if (!TextUtils.isEmpty(count) && mLiveBottomViewHolder != null) {
                mLiveBottomViewHolder.setUnReadCount(count);
            }
        }
    }

    /**
     * 声网sdk--> 主播与其他主播开始连麦
     */
    public void startAgoraLinkMicAnchor(String pkUid, String pkStream) {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.startAgoraLinkMicAnchor(mStream, pkUid, pkStream);
            JSONObject obj = new JSONObject();
            obj.put("method", "LiveConnect");
            obj.put("pkuid", pkUid);
            mLivePushViewHolder.sendSeiMessage(obj.toJSONString());
        }
    }

    /**
     * 声网sdk--> 主播与其他主播断开连麦
     */
    public void closeAgoraLinkMicAnchor() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.closeAgoraLinkMicAnchor();
            JSONObject obj = new JSONObject();
            obj.put("method", "LiveConnect");
            obj.put("pkuid", 0);
            mLivePushViewHolder.sendSeiMessage(obj.toJSONString());
        }
    }

    boolean isCamOpen = true;

    public void closeOpenCam(boolean isLive) {

        isCamOpen = !isCamOpen;
        if (isLive) {
            String pUid = "";
            if (mLiveLinkMicAnchorPresenter != null) {
                pUid = mLiveLinkMicAnchorPresenter.getPkUid();
            }
            mLivePushViewHolder.switchLiveCam(isCamOpen, CommonAppConfig.getInstance().getUid(), CommonAppConfig.getInstance().getUserBean().getAvatarThumb());
            SocketVoiceRoomUtil.userCameraLive(mSocketClient, pUid, CommonAppConfig.getInstance().getUid(), isCamOpen ? 1 : 2, Constants.Live_Cam, CommonAppConfig.getInstance().getUserBean().getAvatarThumb());
            CommonHttpUtil.onChangeCam(mStream, isCamOpen ? "1" : "2", 2);
        } else {
            mLiveChatRoomLinkMicViewHolder.onCamChange(CommonAppConfig.getInstance().getUid(), isCamOpen ? 1 : 2);
            mLivePushViewHolder.switchCamera(isCamOpen);
            SocketVoiceRoomUtil.userCamera(mSocketClient, CommonAppConfig.getInstance().getUid(), isCamOpen ? 1 : 2, Constants.Voice_Cam);
            CommonHttpUtil.onChangeCam(mStream, isCamOpen ? "1" : "2", 1);
        }

    }

    public void clearCams() {
        ((LivePushAgoraViewHolder) mLivePushViewHolder).clearCams();
    }

    @Override
    public void onLiveHostSwitchCam(int type, String uid, String thumb) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).switchLiveCam(type != 2, uid, thumb);
        }
    }

    public void showRedDotOnGuestsIcon(boolean b) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.showRedDotOnGuestsIcon(b);
        }
    }

    public void micOnOff(boolean isMicOn) {
        mLivePushViewHolder.controlMic(!isMicOn);
    }

    @Override
    public void onAncorCancelPk(String winUid, int winCount) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkClose();
            setStartPkBtnVisible(true);
        }
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).releaseDouble();
        }
    }

    @Override
    public void onPkBouns(int duration, int startAfter, int x, int mission_type, int target_value, int targetDuration) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).onPkBouns(duration, startAfter, x, mission_type, target_value, targetDuration);
        }
    }

    public String getPkUid() {
        String pkuid = "";
        if (mLiveLinkMicAnchorPresenter != null) {
            pkuid = mLiveLinkMicAnchorPresenter.getPkUid();
        }
        return pkuid;
    }

    @Override
    public void onAdminAgreeUpLinkMicc(UserBean bean, int isAgree) {
        if (mLiveVoiceAnchorViewHolder != null) {
            mLiveVoiceAnchorViewHolder.setApplyUpMicTipShow(false);
        }
        LiveHttpUtil.getVoiceControlList(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<LiveVoiceControlBean> list = JSON.parseArray(Arrays.toString(info), LiveVoiceControlBean.class);
                    int count=0;
                    for (int i = 0; i < list.size(); i++) {
                        if(!list.get(i).isEmpty()){
                            count++;
                        }
                    }
                    if(count<Constants.LiveVoiceRoomUserCount){
                        LiveHttpUtil.handleVoiceMicApply(mStream, bean.getId(), isAgree, new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0 && info.length > 0) {
                                    if (mContext != null) {
                                        int position = JSON.parseObject(info[0]).getIntValue("position");
                                        handleMicUpApply(bean, position);
                                    }
                                }
                                ToastUtil.show(msg);
                            }

                            @Override
                            public void onError() {
                                Log.e("testawy", "terrrrrrr");
                                super.onError();
                            }
                        });
                    }else{
                        ToastUtil.show(getString(com.livestreaming.common.R.string.no_places));
                    }
                }
            }
        });
    }
    public void onAdminControlMicc(LiveVoiceControlBean bean) {
        if (bean.getStatus() == Constants.VOICE_CTRL_EMPTY) {//无人
            LiveHttpUtil.banEmptyMicPosition(mStream, bean.getPosition(), 0, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {

                        controlMicPosition(bean.getUid(), bean.getPosition(), Constants.VOICE_CTRL_BAN);
                    }
                    ToastUtil.show(msg);
                }
            });
        } else if (bean.getStatus() == Constants.VOICE_CTRL_BAN) {//禁麦
            LiveHttpUtil.banEmptyMicPosition(mStream, bean.getPosition(), 1, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        controlMicPosition(bean.getUid(), bean.getPosition(), Constants.VOICE_CTRL_EMPTY);
                    }
                    ToastUtil.show(msg);
                }
            });
        } else if (bean.getStatus() == Constants.VOICE_CTRL_OPEN || bean.getStatus() == Constants.VOICE_CTRL_CLOSE) {//开麦闭麦切换
            if (mContext != null) {
                ((LiveActivity) mContext).changeVoiceMicOpen(bean.getUid());
            }
        }
    }

    public void onAdminCloseMicc(LiveVoiceControlBean bean) {
        ((LiveActivity) mContext).closeUserVoiceMic(bean.getUid(), 1);
    }

    @Override
    public void onUpdateGuestIncome(int uid, int income) {
        if (mLivePushViewHolder != null && mLivePushViewHolder instanceof LivePushAgoraViewHolder) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).updateGuestIncome(uid, income);
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

    public void showGuestsRequestsDialog() {
        LiveGuestsApplyUpFragment fragment = new LiveGuestsApplyUpFragment();
        fragment.setActionListener(new LiveGuestsApplyUpFragment.ActionListener() {
            @Override
            public void onReject(LiveGestBean liveGestBean) {
                mLiveLinkMicPresenter.onRejectGuest(liveGestBean);
            }

            @Override
            public void onAccept(LiveGestBean liveGestBean) {
                UserBean bean = new UserBean();
                bean.setAvatar(liveGestBean.getAvatar());
                bean.setFrame(liveGestBean.getFrame());
                bean.setId("" + liveGestBean.getUser_id());
                bean.setUserNiceName(liveGestBean.getUserName());
                mLiveLinkMicPresenter.onAcceptGuest(bean);
            }
        });
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_STREAM, mStream);
        bundle.putBoolean(Constants.ANCHOR, mIsAnchor);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveGuestsApplyUpFragment");
    }

    public void releaseDouble() {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).releaseDouble();
        }
    }

    @Override
    public void updatePkBouns(int value, int uid, int completed) {

        if (mLivePushViewHolder != null) {
            if (("" + uid).equals(mLiveUid)) {
                ((LivePushAgoraViewHolder) mLivePushViewHolder).updatePkBouns(value, uid, completed);
            }
        }
    }

    @Override
    public void onSendGiftLiveResponse(String coin, int level, int type) {

    }

    public void handlePkStartCameras(String pkUid, int camStatus1, int camStatus2) {

    }

    public void handleLiveLinkCameras(boolean isUp) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).handleLiveLinkCams(getPkUid(), isUp);
        }
    }

    public void setLiveLinkMicImageVisible(boolean b) {
        if (mLivePushViewHolder != null) {
            ((LivePushAgoraViewHolder) mLivePushViewHolder).setLiveLinkMicImageVisible(b);
        }
    }

    public void setStartPkBtnVisible(boolean b) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setPkBtnVisible(b);
            mLiveAnchorViewHolder.setPkCancelBtnVisible(!b);
        }
    }

    @Override
    public void handleDoubleTotalPoints(int totalPoints, int uid) {
        Log.e("test_double_total_points_2", "........................");
        if (("" + uid).equals(CommonAppConfig.getInstance().getUid())) {
            Log.e("test_double_total_points_3", "........................");
            if (mLivePushViewHolder != null) {
                Log.e("test_double_total_points_4", "........................");
                ((LivePushAgoraViewHolder) mLivePushViewHolder).handleDoubleTotalPoints(totalPoints);
            }
        }
    }

    public void handleWhenEnterRoomDouble(int uid, int x, int missionType, int duration, int startAfter, int doubleSec, int currentTime, int state, int target_value, int targetValueDefault, int completed) {

    }

    @Override
    public void onChangeRoomGuetEnabled(int isEnabled) {

    }

    public void setPkStartBtnVisible(boolean b) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setPkBtnVisible(b);
            mLiveAnchorViewHolder.setPkCancelBtnVisible(!b);
        }
    }

    public void handleEnableDisableGuestRequest(boolean isEnabled) {
        SocketVoiceRoomUtil.enableDisableRequestGuest(mSocketClient, isEnabled);
        LiveHttpUtil.changeEnableDisableVoiceRoomsRequests(mStream, isEnabled, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {

            }
        });
    }

    public void onUserUpVoiceLinkMicDirect(UserBean bean) {
        LiveHttpUtil.getVoiceControlList(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<LiveVoiceControlBean> list = JSON.parseArray(Arrays.toString(info), LiveVoiceControlBean.class);
                    int count = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (!list.get(i).isEmpty()) {
                            count++;
                        }
                    }
                    if (count < Constants.LiveVoiceRoomUserCount) {
                        LiveHttpUtil.handleVoiceMicApply(mStream, bean.getId(), 1, new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0 && info.length > 0) {
                                    if (mContext != null) {
                                        int position = JSON.parseObject(info[0]).getIntValue("position");
                                        ((LiveAnchorActivity) mContext).handleMicUpApply(bean, position);
                                    }
                                }
                                ToastUtil.show(msg);
                            }

                            @Override
                            public void onError() {
                                Log.e("testawy", "terrrrrrr");
                                super.onError();
                            }
                        });
                    }
                }
            }
        });

    }
    public String getFilePah() {
        return filePah;
    }

    public void setFilePah(String filePah) {
        this.filePah = filePah;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
