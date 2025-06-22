package com.livestreaming.live.views;


import static io.agora.rtc2.Constants.AUDIO_PROFILE_MUSIC_STANDARD;
import static io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.Constants.LOCAL_VIDEO_STREAM_STATE_CAPTURING;
import static io.agora.rtc2.Constants.PUB_STATE_PUBLISHED;
import static io.agora.rtc2.Constants.RELAY_STATE_FAILURE;
import static io.agora.rtc2.Constants.RELAY_STATE_RUNNING;
import static io.agora.rtc2.Constants.ScreenScenarioType.SCREEN_SCENARIO_GAMING;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_DISABLED;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.beauty.bean.MeiYanValueBean;
import com.livestreaming.beauty.interfaces.IBeautyEffectListener;
import com.livestreaming.beauty.utils.MhDataManager;
import com.livestreaming.beauty.utils.SimpleDataManager;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.MyCountdown;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.im.http.ImHttpUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.bean.LiveGestBean;
import com.livestreaming.live.bean.LiveUserGiftBean;
import com.livestreaming.live.dialog.LiveUserDialogFragment;
import com.livestreaming.live.http.LiveHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import io.agora.base.TextureBuffer;
import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.DataStreamConfig;
import io.agora.rtc2.IAudioEffectManager;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.ScreenCaptureParameters;
import io.agora.rtc2.gl.EglBaseProvider;
import io.agora.rtc2.video.AgoraVideoFrame;
import io.agora.rtc2.video.BeautyOptions;
import io.agora.rtc2.video.ChannelMediaInfo;
import io.agora.rtc2.video.ChannelMediaRelayConfiguration;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoDenoiserOptions;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by cxf on 2018/10/7.
 * 声网直播推流
 */
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LivePushAgoraViewHolder extends AbsLivePushViewHolder {

    private boolean mMirror;
    private boolean mRealMirror;
    private boolean mPlayBgm;
    private boolean mPushSucceed;//是否推流成功
    private boolean mMhBeautyEnable;//是否使用美狐美颜
    private ArrayList<LiveGuestViewHolder> myGests = new ArrayList<>();
    private RtcEngineEx mEngine;
    private Handler mHandler;
    private boolean mCameraReady;
    private PkBounsStartAfterViewHolder pkBounsStartAfterViewHolder;
    private PkBounsStartViewHolder pkBounsStartViewHolder;

    private MyCountdown pkBounsCountDown;
    private MyCountdown pkBounsCountDown2;
    private FrameLayout pk_bouns_layout;
    private LinearLayout investors_layout;
    private LinearLayout right_investors_layout;
    private LinearLayout left_investors_layout;
    private ImageView left_avatar1;
    private ImageView left_avatar2;
    private ImageView left_avatar3;

    private ImageView right_avatar1;
    private ImageView right_avatar2;
    private ImageView right_avatar3;
    private int mUid;
    private VideoEncoderConfiguration mVideoEncoderConfiguration;
    private int mLinkMicAudienceUid;//观众连麦，连麦观众的uid
    private int mLinkMicAudienceUid2;//观众连麦，连麦观众的uid
    private int mLinkMicAudienceUid3;//观众连麦，连麦观众的uid
    private int mLinkMicAnchorUid;//主播连麦，对方主播的uid
    private boolean mMediaRelaying;//是否在进行跨频道媒体流转发
    private boolean mTryMediaRelayAgain;//跨频道媒体流转发 尝试重连
    private boolean mIsScreenRecord;
    public ImageView mCover;
    private int mDataStreamId;
    private TextureBufferHelper mTextureBufferHelper;
    private ChannelMediaRelayData mChannelMediaRelayData;

    //声网自带的美颜效果
    private BeautyOptions mBeautyOptions;
    private FrameLayout link_mic_live_img_layout;
    private boolean isOn;
    private String mApplyId;
    private String mApplyStream;
    private MyCountdown pkBounsCountDown3;

    Select select;
    public interface Select{
        void initRTC(RtcEngineEx mEngine);
    }

    public LivePushAgoraViewHolder(Context context, ViewGroup parentView, boolean isScreenRecord,Select select) {
        super(context, parentView, isScreenRecord);
        this.select = select;
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mIsScreenRecord = (boolean) args[0];
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_push_agora;
    }

    @Override
    public void init() {
        super.init();
        mMhBeautyEnable = CommonAppConfig.getInstance().isMhBeautyEnable();
        investors_layout = findViewById(R.id.investors_layout);
        right_investors_layout = findViewById(R.id.right_investors_layout);
        left_investors_layout = findViewById(R.id.left_investors_layout);

        link_mic_live_img_layout = findViewById(R.id.link_mic_live_img_layout);

        pkBounsCountDown = new MyCountdown();
        pkBounsCountDown2 = new MyCountdown();
        pk_bouns_layout = findViewById(R.id.pk_bouns_layout);


        right_investors_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pkUid = "" + ((LiveActivity) mContext).getLinkMicAnchorUid();
                ((LiveActivity) mContext).showUserPKContributorListDialog(pkUid, 1);
            }
        });
        left_investors_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LiveActivity) mContext).showUserPKContributorListDialog(CommonAppConfig.getInstance().getUid(), 0);
            }
        });
        left_avatar1 = findViewById(R.id.left_avatar1);
        left_avatar2 = findViewById(R.id.left_avatar2);
        left_avatar3 = findViewById(R.id.left_avatar3);

        right_avatar1 = findViewById(R.id.right_avatar1);
        right_avatar2 = findViewById(R.id.right_avatar2);
        right_avatar3 = findViewById(R.id.right_avatar3);
        if (mIsScreenRecord) {
            ImageView cover = findViewById(R.id.live_cover);
            if (cover != null) {
                cover.setVisibility(View.VISIBLE);
            }
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null) {
                ImgLoader.displayBlur(mContext, u.getAvatar(), cover);
            }
            mPreView = cover;
        } else {
            mPreView = findViewById(R.id.camera_preview);
        }
        mHandler = new Handler();
        mUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
        mMirror = true;
        mRealMirror = mMirror;
        createEngine(new Runnable() {
            @Override
            public void run() {
                if (mEngine != null) {

                    mEngine.setClientRole(CLIENT_ROLE_BROADCASTER);
                    mEngine.enableVideo();
                    VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
                            VideoEncoderConfiguration.VD_1280x720,
                            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                            VideoEncoderConfiguration.STANDARD_BITRATE,
                            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                    );
                    encoderConfiguration.orientationMode = ORIENTATION_MODE_FIXED_PORTRAIT;
                    mVideoEncoderConfiguration = encoderConfiguration;
                    if (!mIsScreenRecord) {
                        mEngine.setupLocalVideo(new VideoCanvas(mPreView, RENDER_MODE_HIDDEN, mUid));
                        mEngine.startPreview();
                        mEngine.setLocalRenderMode(RENDER_MODE_HIDDEN, mRealMirror ? VIDEO_MIRROR_MODE_ENABLED : VIDEO_MIRROR_MODE_DISABLED);
                        encoderConfiguration.mirrorMode = mRealMirror ? VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED
                                : VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
                    }
                    mEngine.setVideoEncoderConfiguration(encoderConfiguration);

                    mEngine.enableAudioVolumeIndication(1000, 3, true);
                }
            }
        });
        mCover = ((ImageView) findViewById(R.id.cover));
    }

    public void setLiveLinkMicImageVisible(boolean isv) {
        if (link_mic_live_img_layout != null) {
            if (isv) {
                link_mic_live_img_layout.setVisibility(View.VISIBLE);
            } else {
                link_mic_live_img_layout.setVisibility(View.GONE);
            }
        }
    }

    private void onPkBtnClicked() {
        ((LiveAnchorActivity) mContext).applyLinkMicPk();
    }

    private void createEngine(final Runnable onSuccess) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                RtcEngineConfig config = new RtcEngineConfig();
                config.mContext = CommonAppContext.getInstance();
                config.mAppId = CommonAppConfig.getInstance().getConfig().getAgoraAppId();
                config.mChannelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
                config.mAudioScenario = AUDIO_PROFILE_MUSIC_STANDARD;
                IRtcEngineEventHandler engineEventHandler = new IRtcEngineEventHandler() {
                    @Override
                    public void onError(int err) {
                        L.e(TAG, "IRtcEngineEventHandler---onError--->" + err);
                    }

                    @Override
                    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                        L.e(TAG, "IRtcEngineEventHandler---onJoinChannelSuccess--->channel: " + channel + " uid: " + uid);

                    }

                    @Override
                    public void onLeaveChannel(RtcStats stats) {
                        L.e(TAG, "IRtcEngineEventHandler---onLeaveChannel--->");
                    }

                    @Override
                    public void onUserMuteAudio(int uid, boolean muted) {
                        super.onUserMuteAudio(uid, muted);
                        int pos = getGestPosition(uid);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (pos != -1 && pos < myGests.size()) {
                                    myGests.get(pos).handleMute(muted);
                                }
                            }
                        });
                    }

                    @Override
                    public void onUserJoined(int uid, int elapsed) {
                        L.e(TAG, "IRtcEngineEventHandler---onUserJoined--->" + uid);
                        if (mHandler != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mEngine != null) {
                                        LiveActivity liveActivity = (LiveActivity) mContext;
                                        List<String> linkMicUids = liveActivity.getLinkMicUids(); // Now supports multiple users
                                        int pkUid = liveActivity.getLinkMicAnchorUid();
                                        if (linkMicUids.contains("" + uid) || pkUid == -1) {
                                            handleGests(uid); // Audience connected to Link Mic
                                        } else if (uid == pkUid) {
                                            onAnchorLinkMic(uid, true); // Anchor connected to Link Mic
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onUserOffline(int uid, int reason) {

                        if (String.valueOf(uid).equals(mApplyId)) {
                            ((LiveAnchorActivity) mContext).onClickCloseAnchorLinkMicBtn();

                        } else {
                            L.e(TAG, "IRtcEngineEventHandler---onUserOffline--->" + uid);
                            if (mHandler != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mEngine != null) {
                                            handleRemoveGests(uid);
                                            ((LiveActivity) mContext).removeFromGuestList(uid);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onLocalVideoStateChanged(io.agora.rtc2.Constants.VideoSourceType source, int state, int error) {
                        L.e(TAG, "IRtcEngineEventHandler---onLocalVideoStateChanged--state->" + state);
                        if (state == LOCAL_VIDEO_STREAM_STATE_CAPTURING) {
                            if (mHandler != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!mCameraReady) {
                                            mCameraReady = true;
                                            if (mLivePushListener != null) {
                                                mLivePushListener.onPreviewStart();
                                            }
                                            if (!mIsScreenRecord) {
                                                getBeautyValue();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onVideoPublishStateChanged(io.agora.rtc2.Constants.VideoSourceType source, String channel, int oldState, int newState, int elapseSinceLastState) {
                        if (newState == PUB_STATE_PUBLISHED) {
                            if (!mPushSucceed) {
                                mPushSucceed = true;
                                if (mLivePushListener != null) {
                                    mLivePushListener.onPushStart();
                                }
                                L.e(TAG, "IRtcEngineEventHandler---onVideoPublishStateChanged--->推流成功");
                            }
                        }
                    }

                    @Override
                    public void onChannelMediaRelayStateChanged(int state, int code) {
                        L.e(TAG, "IRtcEngineEventHandler---onChannelMediaRelayStateChanged--state-->" + state + " code: " + code);
                        if (state == RELAY_STATE_RUNNING) {
                            mMediaRelaying = true;
                            mTryMediaRelayAgain = false;
                        } else if (state == RELAY_STATE_FAILURE) {
                            if (code == 1 || code == 3 || code == 4 || code == 5 || code == 6 || code == 7 || code == 9) {
                                if (!mTryMediaRelayAgain) {
                                    mTryMediaRelayAgain = true;
                                    startChannelMediaRelay();  //跨频道媒体流转发 尝试重连
                                }
                            }
                        }
                    }

                    @Override
                    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
                        // Handle volume indication for active speakers
                        handleVolumeIndication(speakers);
                    }

                };
                config.mEventHandler = engineEventHandler;
                try {
                    mEngine = (RtcEngineEx) RtcEngine.create(config);

                    if (mHandler != null) {
                        mHandler.post(onSuccess);
                    }
                    select.initRTC(mEngine);
                } catch (Exception e) {
                    mEngine = null;
                }
            }
        }).start();

    }

    Handler mainHandler = new Handler(Looper.getMainLooper());

    private void handleVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers) {
        for (IRtcEngineEventHandler.AudioVolumeInfo speaker : speakers) {
            int uid = speaker.uid;
            int volume = speaker.volume;
            mainHandler.post(() -> {
                int pos = getGestPosition(uid);
                if (pos != -1 && pos <myGests.size()) {
                    myGests.get(pos).handleSpeak(volume>3);
                }
            });
        }
    }

    public void setInvestUsers(List<LiveUserGiftBean> list1, List<LiveUserGiftBean> list2) {
        if (!list1.isEmpty()) {
            left_investors_layout.setVisibility(View.VISIBLE);
            if (list1.size() == 1) {
                left_avatar1.setVisibility(View.VISIBLE);
                Glide.with(left_avatar1).load(list1.get(0).getAvatar()).into(left_avatar1);
                left_avatar2.setVisibility(View.INVISIBLE);
                left_avatar3.setVisibility(View.INVISIBLE);
            } else if (list1.size() == 2) {
                left_avatar1.setVisibility(View.VISIBLE);
                Glide.with(left_avatar1).load(list1.get(0).getAvatar()).into(left_avatar1);
                left_avatar2.setVisibility(View.VISIBLE);
                Glide.with(left_avatar2).load(list1.get(1).getAvatar()).into(left_avatar2);
                left_avatar3.setVisibility(View.INVISIBLE);
            } else {
                left_avatar1.setVisibility(View.VISIBLE);
                Glide.with(left_avatar1).load(list1.get(0).getAvatar()).into(left_avatar1);
                left_avatar2.setVisibility(View.VISIBLE);
                Glide.with(left_avatar2).load(list1.get(1).getAvatar()).into(left_avatar2);
                left_avatar3.setVisibility(View.VISIBLE);
                Glide.with(left_avatar3).load(list1.get(2).getAvatar()).into(left_avatar3);
            }
        } else {
            left_investors_layout.setVisibility(View.INVISIBLE);
        }
        if (!list2.isEmpty()) {
            right_investors_layout.setVisibility(View.VISIBLE);
            if (list2.size() == 1) {
                right_avatar1.setVisibility(View.VISIBLE);
                Glide.with(right_avatar1).load(list2.get(0).getAvatar()).into(right_avatar1);
                right_avatar2.setVisibility(View.INVISIBLE);
                right_avatar3.setVisibility(View.INVISIBLE);
            } else if (list2.size() == 2) {
                right_avatar1.setVisibility(View.VISIBLE);
                Glide.with(right_avatar1).load(list2.get(0).getAvatar()).into(right_avatar1);
                right_avatar2.setVisibility(View.VISIBLE);
                Glide.with(right_avatar2).load(list2.get(1).getAvatar()).into(right_avatar2);
                right_avatar3.setVisibility(View.INVISIBLE);
            } else {
                right_avatar1.setVisibility(View.VISIBLE);
                Glide.with(right_avatar1).load(list2.get(0).getAvatar()).into(right_avatar1);
                right_avatar2.setVisibility(View.VISIBLE);
                Glide.with(right_avatar2).load(list2.get(1).getAvatar()).into(right_avatar2);
                right_avatar3.setVisibility(View.VISIBLE);
                Glide.with(right_avatar3).load(list2.get(2).getAvatar()).into(right_avatar3);
            }
        } else {
            right_investors_layout.setVisibility(View.INVISIBLE);
        }
    }

    public int getGestPosition(int uid) {
        int poistion = -1;
        if (!myGests.isEmpty()) {
            for (int i = 0; i < myGests.size(); i++) {
                if (uid == myGests.get(i).bean.getUser_id()) {
                    poistion = i;
                    break;
                }
            }
        }
        return poistion;
    }

    private void handleAddGuest(int uid, int pos) {
        if (!((LiveAnchorActivity) mContext).isLinkMicAnchor()) {

            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEngine != null) {
                            addGuestAndData(uid);
                        }
                    }
                });
            }
        }
    }

    private void addGuestAndData(int uid) {
        LiveHttpUtil.getLiveGestId(uid, Integer.parseInt(CommonAppConfig.getInstance().getUid()), ((LiveAnchorActivity) mContext).mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    LiveGestBean bean = new LiveGestBean();
                    bean.setUserName(obj.getString("guest_name"));
                    bean.setAvatar(obj.getString("avatar"));
                    bean.setFrame(obj.getString("frame_uri"));
                    bean.setIncome(obj.getInteger("income"));
                    bean.setCam_type(obj.getInteger("cam_type"));
                    bean.setMic_type(obj.getInteger("mic_type"));
                    bean.setIncome(0);
                    bean.setUser_id(uid);
                    LiveGuestViewHolder holder = new LiveGuestViewHolder(mContext, gestCont, LivePushAgoraViewHolder.this::showUserDialog, toUid -> ((LiveAnchorActivity) mContext).onAnchorCloseAudeince(toUid, ""));
                    holder.bean = bean;
                    myGests.add(holder);
                    holder.addToParent();
                    holder.subscribeActivityLifeCycle();
                    holder.setData();
                    VideoCanvas remote = new VideoCanvas(holder.textureView, RENDER_MODE_HIDDEN, uid);
                    mEngine.setupRemoteVideo(remote);

                }
            }
        });
    }



    private void handleGests(int uid) {
        if (myGests.isEmpty()) {
            handleAddGuest(uid, 0);
        } else if (myGests.size() == 1) {
            mLinkMicAudienceUid2 = uid;
            handleAddGuest(uid, 1);
        } else if (myGests.size() == 2) {
            mLinkMicAudienceUid3 = uid;
            handleAddGuest(uid, 2);
        } else if (myGests.size() == 3) {
            mLinkMicAudienceUid3 = uid;
            handleAddGuest(uid, 3);
        }
    }

    public void showUserDialog(String toUid) {
        if (!TextUtils.isEmpty(toUid)) {
            LiveUserDialogFragment fragment = new LiveUserDialogFragment();
            fragment.isAnchor = true;
            fragment.whenUserpClick = new LiveUserDialogFragment.WhenUserpClick() {
                @Override
                public void onClick(String uid) {
                    RouteUtil.forwardUserHome(mContext, toUid, true, CommonAppConfig.getInstance().getUid());
                }
            };
            Bundle bundle = new Bundle();
            bundle.putString(com.livestreaming.common.Constants.LIVE_UID, CommonAppConfig.getInstance().getUid());
            bundle.putString(com.livestreaming.common.Constants.TO_UID, toUid);
            fragment.setArguments(bundle);
            fragment.show(((LiveActivity) mContext).getSupportFragmentManager(), "LiveUserDialogFragment");
        }
    }

    private void handleRemoveGests(int uid) {
        ((LiveAnchorActivity) mContext).getmLiveLinkMicPresenter().Guests.removeIf(userBean -> userBean.getId().equals(String.valueOf(uid)));
        int b = -1;
        for (int i = 0; i < myGests.size(); i++) {
            if (myGests.get(i).bean.getUser_id() == uid) {
                b = i;
            }
        }


        if (mEngine != null) {
            if (b != -1) {
                mEngine.setupRemoteVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, uid));
                myGests.get(b).removeFromParent();
                myGests.get(b).release();
                myGests.remove(b);
            }
            if (mLinkMicAudienceUid == uid) {
                mLinkMicAudienceUid = 0;
            } else if (mLinkMicAudienceUid2 == uid) {
                mLinkMicAudienceUid2 = 0;
            } else if (mLinkMicAudienceUid3 == uid) {
                mLinkMicAudienceUid3 = 0;
            }
        }
    }

    /**
     * 获取美颜参数
     */
    private void getBeautyValue() {
        CommonHttpUtil.getBeautyValue(new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mMhBeautyEnable) {
                        MhDataManager.getInstance().init().setMeiYanChangedListener(getMeiYanChangedListener());
                        MeiYanValueBean meiYanValueBean = JSON.parseObject(info[0], MeiYanValueBean.class);
                        MhDataManager.getInstance()
                                .setMeiYanValue(meiYanValueBean)
                                .useMeiYan().restoreBeautyValue();
                        if (mEngine != null) {

                            VideoDenoiserOptions videoDenoiserOptions = new VideoDenoiserOptions();// 视频降噪

                            videoDenoiserOptions.denoiserLevel =
                                    VideoDenoiserOptions.VIDEO_DENOISER_LEVEL_HIGH_QUALITY;
                            videoDenoiserOptions.denoiserMode = VideoDenoiserOptions.VIDEO_DENOISER_AUTO;
                            mEngine.setVideoDenoiserOptions(true, videoDenoiserOptions); // 视频降噪

                            mEngine.registerVideoFrameObserver(new IVideoFrameObserver() {
                                @Override
                                public boolean onCaptureVideoFrame(int sourceType, VideoFrame videoFrame) {
                                    VideoFrame.Buffer buffer = videoFrame.getBuffer();
                                    if (buffer instanceof TextureBuffer) {
                                        TextureBuffer textureBuffer = (TextureBuffer) buffer;
                                        int width = textureBuffer.getWidth();
                                        int height = textureBuffer.getHeight();
                                        int textureId = MhDataManager.getInstance().renderAgora(textureBuffer.getTextureId(), width, height, videoFrame.getRotation());
                                        if (mTextureBufferHelper == null) {
                                            mTextureBufferHelper = TextureBufferHelper.create("MHSDK",
                                                    EglBaseProvider.instance().getRootEglBase().getEglBaseContext());
                                        }
                                        if (mTextureBufferHelper != null) {
                                            VideoFrame.TextureBuffer processBuffer = mTextureBufferHelper.wrapTextureBuffer(
                                                    width, height, VideoFrame.TextureBuffer.Type.RGB, textureId, textureBuffer.getTransformMatrix()
                                            );
                                            videoFrame.replaceBuffer(processBuffer, videoFrame.getRotation(), videoFrame.getTimestampNs());
                                        }
                                    }
                                    return true;
                                }

                                @Override
                                public boolean onPreEncodeVideoFrame(int sourceType, VideoFrame videoFrame) {
                                    return true;
                                }

                                @Override
                                public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int mediaPlayerId) {
                                    return true;
                                }

                                @Override
                                public boolean onRenderVideoFrame(String channelId, int uid, VideoFrame videoFrame) {
                                    return true;
                                }

                                @Override
                                public int getVideoFrameProcessMode() {
                                    return IVideoFrameObserver.PROCESS_MODE_READ_WRITE;
                                }

                                @Override
                                public int getVideoFormatPreference() {
                                    return IVideoFrameObserver.VIDEO_PIXEL_DEFAULT;
                                }

                                @Override
                                public boolean getRotationApplied() {
                                    return false;
                                }

                                @Override
                                public boolean getMirrorApplied() {
                                    return false;
                                }

                                @Override
                                public int getObservedFramePosition() {
                                    return IVideoFrameObserver.POSITION_POST_CAPTURER;
                                }
                            });
                        }
                    } else {
                        SimpleDataManager.getInstance().setMeiYanChangedListener(getMeiYanChangedListener());
                        int meiBai = obj.getIntValue("skin_whiting");
                        int moPi = obj.getIntValue("skin_smooth");
                        int hongRun = obj.getIntValue("skin_tenderness");
                        
                        // Set individual beauty parameters
                        SimpleDataManager instance = SimpleDataManager.getInstance();
                        instance.setMeiBai(meiBai);
                        instance.setMoPi(moPi);
                        instance.setHongRun(hongRun);
                    }
                }
            }
        });
    }

    @Override
    public IBeautyEffectListener getMeiYanChangedListener() {
        return new IBeautyEffectListener() {
            @Override
            public void onMeiYanChanged(int meiBai, boolean meiBaiChanged, int moPi, boolean moPiChanged, int hongRun, boolean hongRunChanged) {
                if (meiBaiChanged || moPiChanged || hongRunChanged) {
                    if (mEngine != null) {
                        if (mBeautyOptions == null) {
                            mBeautyOptions = new BeautyOptions();
                        }
                        mBeautyOptions.lighteningLevel = meiBai / 10f;
                        mBeautyOptions.smoothnessLevel = moPi / 10f;
                        mBeautyOptions.rednessLevel = hongRun / 10f;
                        mEngine.setBeautyEffectOptions(true, mBeautyOptions);
                    }
                }
            }

            @Override
            public void onAdvancedBeautyChanged(String key, int value) {
                if (mEngine != null) {
                    // Check if MH beauty is enabled
                    if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
                        // Use MhDataManager for advanced beauty features
                        switch (key) {
                            case "brightness":
                                MhDataManager.getInstance().setLiangDu(value);
                                break;
                            case "sharpness":
                                MhDataManager.getInstance().setMoPi(value);
                                break;
                            case "face_slim":
                                MhDataManager.getInstance().setShouLian(value);
                                break;
                            case "big_eye":
                                MhDataManager.getInstance().setDaYan(value);
                                break;
                            case "jaw":
                                MhDataManager.getInstance().setXiaBa(value);
                                break;
                            case "eye_distance":
                                MhDataManager.getInstance().setYanJu(value);
                                break;
                            case "face_shape":
                                MhDataManager.getInstance().setETou(value);
                                break;
                            case "eye_brow":
                                MhDataManager.getInstance().setMeiMao(value);
                                break;
                            case "eye_corner":
                                MhDataManager.getInstance().setYanJiao(value);
                                break;
                            case "mouse_lift":
                                MhDataManager.getInstance().setZuiXing(value);
                                break;
                            case "nose_lift":
                                MhDataManager.getInstance().setShouBi(value);
                                break;
                            case "lengthen_noseLift":
                                MhDataManager.getInstance().setChangBi(value);
                                break;
                            case "face_shave":
                                MhDataManager.getInstance().setXueLian(value);
                                break;
                            case "eye_alat":
                                MhDataManager.getInstance().setKaiYanJiao(value);
                                break;
                        }
                        
                        // Save beauty values to server when they change
                        MhDataManager.getInstance().saveBeautyValue();
                    } else {
                        // Use Agora's built-in beauty options for the few parameters it supports
                        if (mBeautyOptions == null) {
                            mBeautyOptions = new BeautyOptions();
                        }
                        
                        // Map the advanced beauty parameters to available Agora beauty options
                        float normalizedValue = value / 10f; // Convert to 0-1 range for Agora SDK
                        
                        switch (key) {
                            case "brightness":
                                // For brightness, use lightening level in Agora
                                mBeautyOptions.lighteningLevel = normalizedValue;
                                break;
                            case "sharpness":
                                // For sharpness, could use smoothness as approximation
                                mBeautyOptions.smoothnessLevel = normalizedValue;
                                break;
                            // Agora's basic beauty options don't have these advanced features
                            // but we still need to handle them to avoid errors
                            case "face_slim":
                            case "big_eye":
                            case "jaw":
                            case "eye_distance":
                            case "face_shape":
                            case "eye_brow":
                            case "eye_corner":
                            case "mouse_lift":
                            case "nose_lift":
                            case "lengthen_noseLift":
                            case "face_shave":
                            case "eye_alat":
                                // These parameters are not supported by Agora's built-in beauty
                                // We'll just save them but not apply them
                                break;
                        }
                        
                        // Apply the beauty effect options
                        mEngine.setBeautyEffectOptions(true, mBeautyOptions);
                        
                        // Save beauty values to server when they change
                        SimpleDataManager.getInstance().saveBeautyValue();
                    }
                }
            }

            @Override
            public void onFilterChanged(int filterName) {

            }

            @Override
            public boolean isUseMhFilter() {
                return true;
            }

            @Override
            public boolean isTieZhiEnable() {
                return !mIsPlayGiftSticker;
            }
        };
    }


    @Override
    public void changeToLeft() {
        if (mPreView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPreView.getLayoutParams();
            params.width = mPreView.getWidth() / 2;
            params.height = DpUtil.dp2px(360);
            params.topMargin = DpUtil.dp2px(130);
            mPreView.setLayoutParams(params);
        }
    }

    @Override
    public void changeToBig() {
        if (mPreView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPreView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.topMargin = 0;
            mPreView.setLayoutParams(params);
        }
    }

    /**
     * 切换镜像
     */
    @Override
    public void togglePushMirror() {
        mMirror = !mMirror;
        setRealMirror(mMirror);
        if (!mMirror) {
            ToastUtil.show(com.livestreaming.common.R.string.live_mirror_1);
        } else {
            ToastUtil.show(com.livestreaming.common.R.string.live_mirror_0);
        }
    }

    private void setRealMirror(boolean mirror) {
        if (mRealMirror == mirror) {
            return;
        }
        mRealMirror = mirror;
        if (mEngine != null) {
            mEngine.setLocalRenderMode(RENDER_MODE_HIDDEN, mirror ? VIDEO_MIRROR_MODE_ENABLED : VIDEO_MIRROR_MODE_DISABLED);
            if (mVideoEncoderConfiguration != null) {
                mVideoEncoderConfiguration.mirrorMode = mirror ? VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED
                        : VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
                mEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration);
            }
        }
    }


    /**
     * 切换镜头
     */
    @Override
    public void toggleCamera() {
        if (!mCameraReady) {
            return;
        }
        if (mEngine != null) {
            if (mFlashOpen) {
                if (mCameraReady) {
                    mEngine.setCameraTorchOn(false);
                }
                mFlashOpen = false;
            }
            mCameraFront = !mCameraFront;
            mEngine.switchCamera();
            if (!mMhBeautyEnable) {
                if (!mCameraFront) {
                    setRealMirror(false);
                } else {
                    setRealMirror(mMirror);
                }
            }
        }

    }

    @Override
    public void switchCamera(boolean isOn) {

    }

    /**
     * 打开关闭闪光灯
     */
    @Override
    public void toggleFlash() {
        if (mCameraFront) {
            ToastUtil.show(com.livestreaming.common.R.string.live_open_flash);
            return;
        }
        if (mEngine != null) {
            boolean open = !mFlashOpen;
            if (mCameraReady && mEngine.setCameraTorchOn(open) == 0) {
                mFlashOpen = open;
            }
        }
    }

    /**
     * 开始推流
     *
     * @param pushUrl 推流地址
     */
    @Override
    public void startPush(String pushUrl) {

    }

    /**
     * 开始声网推流
     */
    @Override
    public void startPushAgora(String token, String channelId) {
        if (mIsScreenRecord) {
            mEngine.setScreenCaptureScenario(SCREEN_SCENARIO_GAMING);
            ScreenCaptureParameters screenCaptureParameters = new ScreenCaptureParameters();
            screenCaptureParameters.videoCaptureParameters.width = 720;
            screenCaptureParameters.videoCaptureParameters.height = (int) (ScreenDimenUtil.getInstance().getScreenRealHeight() * 720f / ScreenDimenUtil.getInstance().getScreenWidth());
            screenCaptureParameters.videoCaptureParameters.framerate = 15;
            screenCaptureParameters.captureVideo = true;
            screenCaptureParameters.captureAudio = true;
            mEngine.startScreenCapture(screenCaptureParameters);
        }
        mStartPush = true;
        if (mEngine != null) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
            option.autoSubscribeAudio = true;
            option.autoSubscribeVideo = true;
            option.publishMicrophoneTrack = true;
            option.publishCameraTrack = true;
            if (mIsScreenRecord) {
                option.publishScreenCaptureVideo = true;
                option.publishScreenCaptureAudio = true;
            }
            mEngine.joinChannel(token, channelId, mUid, option);
        }
        startCountDown();
    }


    @Override
    public void startBgm(String path) {
        if (mEngine != null) {
            int res = mEngine.startAudioMixing(path, false, 1, 0);
            if (res == 0) {
                //该方法调节混音音乐文件在本端和远端的播放音量大小。取值范围为 [0,100]，100 （默认值）为原始音量。
                mEngine.adjustAudioMixingVolume(80);
                mPlayBgm = true;
            }
        }
    }

    @Override
    public void pauseBgm() {
    }

    @Override
    public void resumeBgm() {
    }

    @Override
    public void stopBgm() {
        if (mPlayBgm && mEngine != null) {
            mEngine.stopAudioMixing();
        }
        mPlayBgm = false;
    }

    @Override
    protected void onCameraRestart() {
    }

    @Override
    public void controlMic(boolean b) {
        mEngine.muteLocalAudioStream(!b);  // Mute (stop) local audio capture
    }

    public void clearCams() {
        findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
        findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
    }

    @Override
    public void switchLiveCam(boolean isCamOpen, String uid, String thumb) {

        if (!isCamOpen) {
            // Stop the camera
            //mEngine.enableLocalVideo(false);

            Glide.with(mPreView.getContext())
                    .asBitmap()
                    .load(thumb)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            if (((LiveAnchorActivity) mContext).getPkUid() != null && !((LiveAnchorActivity) mContext).getPkUid().isEmpty()) {
                                if (uid.equals(CommonAppConfig.getInstance().getUid())) {
                                    findViewById(R.id.left_cam_cover).setVisibility(View.VISIBLE);
                                    ((ImageView) findViewById(R.id.left_cam_cover)).setImageBitmap(resource);
                                } else {
                                    findViewById(R.id.right_cam_cover).setVisibility(View.VISIBLE);
                                    ((ImageView) findViewById(R.id.right_cam_cover)).setImageBitmap(resource);
                                }
                            } else {
                                mCover.setVisibility(View.VISIBLE);
                                mCover.setImageBitmap(resource);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Handle the placeholder if needed
                        }
                    });
        } else {
            // Stop virtual camera and start regular camera
            if (((LiveAnchorActivity) mContext).getPkUid() != null && !((LiveAnchorActivity) mContext).getPkUid().isEmpty()) {
                if (uid.equals(CommonAppConfig.getInstance().getUid())) {
                    findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
                }
            } else {
                mCover.setVisibility(View.GONE);
            }
            // mEngine.enableLocalVideo(true);  // Re-enable the camera
            //mEngine.startPreview();          // Start camera preview
        }
    }


    // First, define a method to push a Bitmap as a video frame
    private void pushImageAsVirtualCamera(RtcEngine mRtcEngine, Bitmap bitmap) {
        AgoraVideoFrame videoFrame = new AgoraVideoFrame();
        videoFrame.format = AgoraVideoFrame.FORMAT_RGBA;
        videoFrame.timeStamp = System.currentTimeMillis();

        // You can set the frame dimensions and buffer based on the bitmap
        videoFrame.stride = bitmap.getWidth();
        videoFrame.height = bitmap.getHeight();
        videoFrame.buf = convertBitmapToByteArray(bitmap);  // You'll need to convert the Bitmap to byte array
        videoFrame.rotation = 0;

        mEngine.pushExternalVideoFrame(videoFrame);  // Push the frame to Agora's stream
    }

    // Helper method to convert Bitmap to byte array
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        return buffer.array();
    }


    @Override
    public void release() {
        super.release();
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BEAUTY_VALUE);
        releasePusher();
    }

    private void releasePusher() {
        if (mEngine != null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (mMediaRelaying) {
                        mMediaRelaying = false;
                        mEngine.stopChannelMediaRelay();
                    }
                    mEngine.leaveChannel();
                    if (mIsScreenRecord) {
                        mEngine.stopScreenCapture();
                    } else {
                        mEngine.stopPreview();
                    }
                    RtcEngine.destroy();
                }
            }).start();
            if (mMhBeautyEnable) {
                MhDataManager.getInstance().release();
            } else {
                SimpleDataManager.getInstance().saveBeautyValue();
            }
        }
       /* mEngine = null;
        mChannelMediaRelayData = null;
        mTryMediaRelayAgain = false;*/


    }

    @Override
    public void onDestroy() {
        releasePusher();
        super.onDestroy();
    }

    @Override
    public void sendSeiMessage(String msg) {
        L.e(TAG, "IRtcEngineEventHandler--sendSeiMessage--发送消息--->msg: " + msg);
        byte[] data = null;
        try {
            data = msg.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            data = null;
        }
        if (data != null && data.length > 0) {
            if (mEngine != null) {
                if (mDataStreamId == 0) {
                    DataStreamConfig dataStreamConfig = new DataStreamConfig();
                    dataStreamConfig.syncWithAudio = true;
                    dataStreamConfig.ordered = true;
                    mDataStreamId = mEngine.createDataStream(dataStreamConfig);
                }
                mEngine.sendStreamMessage(mDataStreamId, data);
            }
        }
    }

    /**
     * 声网sdk--> 主播与其他主播连麦
     *
     * @param uid 对方主播的uid
     */
    private void onAnchorLinkMic(int uid, boolean fromAgoraHandler) {
        if (mLinkMicAnchorUid == 0) {
            if (fromAgoraHandler) {
                L.e(TAG, "IRtcEngineEventHandler---主播连麦--声网OnUserJoined--->" + uid);
            } else {
                L.e(TAG, "IRtcEngineEventHandler---主播连麦---socket--->" + uid);
            }
            mLinkMicAnchorUid = uid;
            TextureView textureView = new TextureView(mContext);
            mRightContainer.removeAllViews();
            mRightContainer.addView(textureView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, uid);
            mEngine.setupRemoteVideo(remote);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播连麦，进行跨频道媒体流转发
     *
     * @param stream   自己主播的stream
     * @param pkUid    对方主播的uid
     * @param pkStream 对方主播的stream
     */
    public void startAgoraLinkMicAnchor(final String stream, String pkUid, final String pkStream) {
        mApplyId = pkUid;
        mApplyStream = pkStream;
        onAnchorLinkMic(Integer.parseInt(pkUid), false);
        LiveHttpUtil.getSwRtcPKToken(stream, pkStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String srcToken = obj.getString("src_token");
                    String destToken = obj.getString("dest_token");
                    L.e(TAG, "IRtcEngineEventHandler--getSwRtcPKToken--srcToken--->" + srcToken + "--destToken-->" + destToken);
                    mChannelMediaRelayData = new ChannelMediaRelayData(srcToken, stream, destToken, pkStream);
                    startChannelMediaRelay();
                }
            }
        });
        setLiveLinkMicImageVisible(true);
    }

    private void startChannelMediaRelay() {
        if (mChannelMediaRelayData != null && mEngine != null) {
            // 配置源频道信息
            ChannelMediaRelayConfiguration mediaRelayConfiguration = new ChannelMediaRelayConfiguration();
            ChannelMediaInfo srcChannelInfo = new ChannelMediaInfo(mChannelMediaRelayData.mSrcChannel, mChannelMediaRelayData.mSrcToken, 0);
            mediaRelayConfiguration.setSrcChannelInfo(srcChannelInfo);
            // 配置目标频道信息
            int myUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
            ChannelMediaInfo destChannelInfo = new ChannelMediaInfo(mChannelMediaRelayData.mDestChannel, mChannelMediaRelayData.mDestToken, myUid);
            mediaRelayConfiguration.setDestChannelInfo(mChannelMediaRelayData.mDestChannel, destChannelInfo);
            mEngine.startOrUpdateChannelMediaRelay(mediaRelayConfiguration);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播断开连麦
     */
    @Override
    public void closeAgoraLinkMicAnchor() {
        if (mEngine != null) {
            if (mMediaRelaying) {
                mMediaRelaying = false;
                mEngine.stopChannelMediaRelay();
            }
        }
        if (mLinkMicAnchorUid != 0) {
            L.e(TAG, "IRtcEngineEventHandler---主播连麦断开--->");
            if (mRightContainer != null) {
                mRightContainer.removeAllViews();
            }
            mLinkMicAnchorUid = 0;
        }
        mChannelMediaRelayData = null;
        mTryMediaRelayAgain = false;
    }

    public void onLiveGestCamChanged(boolean b, String touid, String avatar) {
        if (!touid.equals(CommonAppConfig.getInstance().getUid())) {
            try {
                int pos=getGestPosition(Integer.parseInt(touid));
                if(pos!=-1){
                    myGests.get(pos).handleGuestCam(b);
                }
            } catch (Exception e) {

            }
        }
    }

    public void onLiveGestMicChanged(int b, String touid) {
        if (!touid.equals(CommonAppConfig.getInstance().getUid())) {
            try {
                int pos=getGestPosition(Integer.parseInt(touid));
                if(pos!=-1){
                    myGests.get(pos).handleMute(b==1);
                }
            } catch (Exception e) {

            }
        }

    }

    public void updateGuestIncome(int uid, int income) {
        int x = getGestPosition(uid);
        if (x != -1&&myGests.size()>x) {
            myGests.get(x).handleGuestIncome(income);
        }
    }

    public void setInvestRowVisible(boolean b) {
        if (b) {
            right_investors_layout.setVisibility(View.VISIBLE);
            left_investors_layout.setVisibility(View.VISIBLE);
        } else {
            right_investors_layout.setVisibility(View.GONE);
            left_investors_layout.setVisibility(View.GONE);
        }
    }


    private class ChannelMediaRelayData {
        private final String mSrcToken;
        private final String mSrcChannel;
        private final String mDestToken;
        private final String mDestChannel;

        public ChannelMediaRelayData(String srcToken, String srcChannel, String destToken, String destChannel) {
            mSrcToken = srcToken;
            mSrcChannel = srcChannel;
            mDestToken = destToken;
            mDestChannel = destChannel;
        }
    }

    int duration, startAfter, x, mission_type, target_value, targetDuration;

    public void onPkBouns(int duration, int startAfter, int x, int mission_type, int target_value, int targetDuration) {
        this.duration = duration;
        this.startAfter = startAfter;
        this.x = x;
        this.mission_type = mission_type;
        this.target_value = target_value;
        this.targetDuration = targetDuration;
        showPkBounsWillStartAfter();
        pkBounsCountDown = new MyCountdown();
        pkBounsCountDown2 = new MyCountdown();
        pkBounsCountDown3 = new MyCountdown();
        pkBounsCountDown.setTotalSecond(startAfter).setCallback(new MyCountdown.ActionListener() {
            @Override
            public void onTimeChanged(String timeStr) {
                if (pkBounsStartAfterViewHolder != null) {
                    pkBounsStartAfterViewHolder.setTimeTic(timeStr);
                    pk_bouns_layout.invalidate();
                }
            }

            @Override
            public void onTimeEnd() {
                showPkBounsStarted();
            }
        }).start();
    }

    private void showPkBounsStarted() {
        pk_bouns_layout.removeAllViews();
        getPounsStartedView();
        pkBounsCountDown2.setTotalSecond(duration).setCallback(new MyCountdown.ActionListener() {
            @Override
            public void onTimeChanged(String timeStr) {
                if (pkBounsStartViewHolder != null) {
                    pkBounsStartViewHolder.setTimeTic(timeStr);
                    pk_bouns_layout.invalidate();
                }
            }

            @Override
            public void onTimeEnd() {
                if (pkBounsStartViewHolder.targetComplete) {
                    showWhenTargetCompletedStart();
                } else {
                    pkBounsStartViewHolder.targetNotCompleted();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pk_bouns_layout.removeAllViews();
                            pk_bouns_layout.setVisibility(View.GONE);
                        }
                    }, 3000);
                }
            }
        }).start();
    }

    private void showWhenTargetCompletedStart() {
        pkBounsStartViewHolder.handleWhenBounsBegin();
        pkBounsCountDown3.setTotalSecond(targetDuration).setCallback(new MyCountdown.ActionListener() {
            @Override
            public void onTimeChanged(String timeStr) {
                pkBounsStartViewHolder.whenTargetStartTicks(timeStr);
            }

            @Override
            public void onTimeEnd() {
                pk_bouns_layout.removeAllViews();
                pk_bouns_layout.setVisibility(View.GONE);
            }
        }).start();
    }

    private void getPounsStartedView() {
        pkBounsStartViewHolder = new PkBounsStartViewHolder(mContext, duration, x, mission_type, target_value, targetDuration);
        pk_bouns_layout.addView(pkBounsStartViewHolder.getView());
        pk_bouns_layout.invalidate();
    }


    private void showPkBounsWillStartAfter() {
        pk_bouns_layout.removeAllViews();
        pk_bouns_layout.setVisibility(View.VISIBLE);
        getPkBounsStartAfterView();
    }

    private void getPkBounsStartAfterView() {
        pkBounsStartAfterViewHolder = new PkBounsStartAfterViewHolder(mContext, startAfter, x, mission_type, target_value);
        pk_bouns_layout.addView(pkBounsStartAfterViewHolder.getView());
        pkBounsStartAfterViewHolder.init();
        pk_bouns_layout.invalidate();
    }

    public void releaseDouble() {
        if (pkBounsCountDown != null) {
            pkBounsCountDown.stop();
            pkBounsCountDown.release();
            pkBounsCountDown = null;
        }
        if (pkBounsCountDown2 != null) {
            pkBounsCountDown2.stop();
            pkBounsCountDown2.release();
            pkBounsCountDown2 = null;
        }
        if (pk_bouns_layout != null) {
            pk_bouns_layout.setVisibility(View.GONE);
        }
    }

    public void updatePkBouns(int value, int uid, int completed) {
        Log.e("targettttttttttttttttttttttttttttttt", "stepUpdate " + uid);
        if (pkBounsStartViewHolder != null) {
            pkBounsStartViewHolder.handleTargetMessage(value);
            if (completed == 1) {
                if (("" + uid).equals(CommonAppConfig.getInstance().getUid())) {
                    pkBounsStartViewHolder.targetCompleted(uid);
                }
            }
        }
    }

    public void handleLiveLinkCams(String pkUid, boolean isUp) {
        if (isUp) {
            LiveHttpUtil.getLiveCamStatusByUserId(pkUid, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (info.length > 0) {
                        JSONObject obj = JSONObject.parseObject(info[0]);
                        int pkCamStatus = obj.getIntValue("camera");
                        if (mCover.getVisibility() == View.VISIBLE) {
                            findViewById(R.id.left_cam_cover).setVisibility(View.VISIBLE);
                            ImgLoader.display(mContext, CommonAppConfig.getInstance().getUserBean().getAvatar(), ((ImageView) findViewById(R.id.left_cam_cover)));
                            ((ImageView) findViewById(R.id.left_cam_cover)).setImageDrawable(mCover.getDrawable());
                            mCover.setVisibility(View.INVISIBLE);
                        } else {
                            findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
                        }
                        if (pkCamStatus == 1) {
                            findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
                        } else if (pkCamStatus == 2) {
                            findViewById(R.id.right_cam_cover).setVisibility(View.VISIBLE);
                            ImHttpUtil.getImUserInfo(pkUid, new HttpCallback() {
                                @Override
                                public void onSuccess(int code, String msg, String[] info) {
                                    if (code == 0 && info.length > 0) {
                                        JSONObject obj = JSON.parseObject(info[0]);
                                        ImgLoader.display(mContext, obj.getString("avatar"), ((ImageView) findViewById(R.id.right_cam_cover)));
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } else {
            LiveHttpUtil.getLiveCamStatusByUserId(CommonAppConfig.getInstance().getUid(), new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    JSONObject obj = JSONObject.parseObject(info[0]);
                    int camStatus = obj.getIntValue("camera");
                    if (camStatus==2) {
                        ((ImageView) findViewById(R.id.left_cam_cover)).setImageResource(0);
                        findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
                        mCover.setVisibility(View.VISIBLE);
                        ImgLoader.display(mContext, CommonAppConfig.getInstance().getUserBean().getAvatar(), mCover);
                    }
                    if (findViewById(R.id.right_cam_cover).getVisibility() == View.VISIBLE) {
                        ((ImageView) findViewById(R.id.right_cam_cover)).setImageResource(0);
                        findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
                    }
                }});

        }

    }

    public void handleDoubleTotalPoints(int totalPoints) {
        Log.e("test_double_total_points_5", "........................");
        pk_bouns_layout = findViewById(R.id.pk_bouns_layout);
        pk_bouns_layout.setVisibility(View.VISIBLE);
        pkBounsStartViewHolder = new PkBounsStartViewHolder(mContext, duration, x, mission_type, target_value, targetDuration);
        pk_bouns_layout.removeAllViews();
        pkBounsStartViewHolder.showTotalPoints(totalPoints);
        pk_bouns_layout.addView(pkBounsStartViewHolder.getView());
        Log.e("test_double_total_points_6", "........................");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("test_double_total_points_7", "........................");
                pk_bouns_layout.removeAllViews();
                pk_bouns_layout.invalidate();
            }
        }, 4000);
    }

}
