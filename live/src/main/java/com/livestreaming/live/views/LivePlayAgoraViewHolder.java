package com.livestreaming.live.views;

import static io.agora.rtc2.Constants.AUDIO_PROFILE_MUSIC_STANDARD;
import static io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.toolbox.ImageLoader;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.MyCountdown;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.http.ImHttpUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.bean.LiveGestBean;
import com.livestreaming.live.bean.LiveUserGiftBean;
import com.livestreaming.live.custom.LiveAudienceRecyclerView;
import com.livestreaming.live.dialog.LiveUserDialogFragment;
import com.livestreaming.live.http.LiveHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.JsonObject;
import com.tencent.liteav.txcvodplayer.renderer.TextureRenderView;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;

import java.util.ArrayList;
import java.util.List;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class LivePlayAgoraViewHolder extends LiveRoomPlayViewHolder {

    private static final String TAG = "LiveTxPlayViewHolder";
    private ViewGroup mRoot;
    private LinearLayout gestCont;
    private ViewGroup mLeftContainer;
    private ViewGroup mRightContainer;
    private ViewGroup mPkContainer;
    private TextureView mLiveView;

    private PkBounsStartAfterViewHolder pkBounsStartAfterViewHolder;
    private PkBounsStartViewHolder pkBounsStartViewHolder;
    int myCamStatus = 0, myMicStatus = 0;

    private MyCountdown pkBounsCountDown;
    private MyCountdown pkBounsCountDown2;
    private FrameLayout pk_bouns_layout;

    private TextureRenderView mVideoView;
    private View mLoading;
    public ImageView mCover;
    private TXVodPlayer mVodPlayer;
    private boolean mPaused;//是否切后台了
    private boolean mPausedPlay;//是否被动暂停了播放
    private boolean mChangeToLeft;
    private boolean mChangeToAnchorLinkMic;

    private Handler mVideoHandler;
    public ArrayList<LiveGuestViewHolder> roomGests = new ArrayList<>();
    private Handler mLiveHandler;

    private float mVideoWidth;
    private float mVideoHeight;
    private int mRootHeight;
    private Boolean mIsLive;
    private boolean mShowVideoFirstFrame = false;
    private Runnable mVideoFirstFrameRunnable;
    private RtcEngineEx mEngine;
    private int mLiveUid;
    private int mUid;
    private LinearLayout investors_layout;
    private LinearLayout right_investors_layout;
    private LinearLayout left_investors_layout;
    private ImageView left_avatar1;
    private ImageView left_avatar2;
    private ImageView left_avatar3;

    private ImageView right_avatar1;
    private ImageView right_avatar2;
    private ImageView right_avatar3;
    private FrameLayout link_mic_live_img_layout;
    private int mLinkMicAudienceUid;//观众连麦，连麦观众的uid
    private int mLinkMicAudienceUid2;//观众连麦，连麦观众的uid
    private int mLinkMicAudienceUid3;//观众连麦，连麦观众的uid
    private int mLinkMicAnchorUid;//主播连麦，对方主播的uid
    private IRtcEngineEventHandler mIRtcEngineEventHandler;
    private MyCountdown pkBounsCountDown3;
//    private RtcConnection mOtherAnchorRtcConnection;//主播连麦，对方主播的RtcConnection

    public LivePlayAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_play_tx;
    }

    @Override
    public void init() {
        mUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
        mRoot = (ViewGroup) findViewById(R.id.root);
        mRoot.post(new Runnable() {
            @Override
            public void run() {
                mRootHeight = mRoot.getHeight();
            }
        });
        pkBounsCountDown = new MyCountdown();
        pkBounsCountDown2 = new MyCountdown();
        pk_bouns_layout = findViewById(R.id.pk_bouns_layout);
        link_mic_live_img_layout = findViewById(R.id.link_mic_live_img_layout);
        investors_layout = findViewById(R.id.investors_layout);
        right_investors_layout = findViewById(R.id.right_investors_layout);
        left_investors_layout = findViewById(R.id.left_investors_layout);
        left_avatar1 = findViewById(R.id.left_avatar1);
        left_avatar2 = findViewById(R.id.left_avatar2);
        left_avatar3 = findViewById(R.id.left_avatar3);
        right_avatar1 = findViewById(R.id.right_avatar1);
        right_avatar2 = findViewById(R.id.right_avatar2);
        right_avatar3 = findViewById(R.id.right_avatar3);

        gestCont = ((LiveAudienceActivity) mContext).getGestContainer();
        mLeftContainer = (ViewGroup) findViewById(R.id.left_container);
        mRightContainer = (ViewGroup) findViewById(R.id.right_container);
        mPkContainer = (ViewGroup) findViewById(R.id.pk_container);
        mLoading = findViewById(R.id.loading);
        mCover = (ImageView) findViewById(R.id.cover);
        mLiveView = (TextureView) findViewById(R.id.live_view);
        mVideoView = (TextureRenderView) findViewById(R.id.video_view);
        mLiveHandler = new Handler();
        initEngine();

        if (investors_layout != null) {
            investors_layout.bringToFront();
        }
    }

    public void initEngine() {
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
            public void onClientRoleChanged(int oldRole, int newRole, ClientRoleOptions newRoleOptions) {
                L.e(TAG, "IRtcEngineEventHandler---onClientRoleChanged--->oldRole: " + oldRole + " newRole: " + newRole);
            }

            @Override
            public void onClientRoleChangeFailed(int reason, int currentRole) {
                L.e(TAG, "IRtcEngineEventHandler---onClientRoleChangeFailed---->reason: " + reason + " currentRole: " + currentRole);
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                L.e(TAG, "IRtcEngineEventHandler---onUserJoined--->" + uid);
                if (mEngine == null || uid == mUid) {
                    return;
                }
                if (mLiveUid == uid) {
                    VideoCanvas remote = new VideoCanvas(mLiveView, RENDER_MODE_HIDDEN, uid);
                    mEngine.setupRemoteVideo(remote);
                } else {
                    LiveActivity liveActivity = (LiveActivity) mContext;
                    List<String> linkMicUids = liveActivity.getLinkMicUids(); // Now supports multiple users
                    int pkUid = liveActivity.getLinkMicAnchorUid();

                    if (linkMicUids.contains("" + uid) || pkUid == -1) {
                        handleGests(uid); // Audience connected to Link Mic
                    } else if (uid == pkUid) {
                        onAnchorLinkMic(pkUid, true); // Anchor connected to Link Mic
                    }
                }
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                removeGests(uid);
                ((LiveActivity) mContext).removeFromGuestList(uid);
                ((LiveAudienceActivity) mContext).deleteGuest(uid);
            }

            @Override
            public void onUserMuteAudio(int uid, boolean muted) {
                super.onUserMuteAudio(uid, muted);

                int pos;
                if (uid == 0) {
                    pos = getMyGuestPosition();
                } else {
                    pos = getGestPosition(uid);
                }
                if (pos != -1 && pos < roomGests.size()) {
                    roomGests.get(pos).handleMute(muted);
                }
            }


            @Override
            public void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width, int height, int rotation) {
                L.e(TAG, "IRtcEngineEventHandler---onVideoSizeChanged---source:" + source + " uid: " + uid + " width: " + width + " height: " + height + " rotation: " + rotation);
                if (mLiveUid == uid) {
                    if ((rotation / 90) % 2 == 0) {
                        mVideoWidth = width;
                        mVideoHeight = height;
                    } else {
                        mVideoWidth = height;
                        mVideoHeight = width;
                    }
                    if (mLiveHandler != null) {
                        mLiveHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                changeLiveSize(false);
                            }
                        });
                    }
                }
            }

            @Override
            public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
                // Handle volume indication for active speakers
                handleVolumeIndication(speakers);
            }
        };
        config.mEventHandler = engineEventHandler;
        mIRtcEngineEventHandler = engineEventHandler;
        try {
            mEngine = (RtcEngineEx) RtcEngine.create(config);
            mEngine.setClientRole(CLIENT_ROLE_AUDIENCE);
            VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_480x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            );
            configuration.orientationMode = ORIENTATION_MODE_FIXED_PORTRAIT;
            configuration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
            mEngine.setVideoEncoderConfiguration(configuration);
            mEngine.enableAudioVolumeIndication(1000, 3, true);
            mEngine.enableVideo();
        } catch (Exception e) {
            mEngine = null;
        }
    }

    Handler mainHandler = new Handler(Looper.getMainLooper());

    private void handleVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers) {

        for (IRtcEngineEventHandler.AudioVolumeInfo speaker : speakers) {
            int uid = speaker.uid != 0 ? speaker.uid : Integer.parseInt(CommonAppConfig.getInstance().getUid());
            int volume = speaker.volume;
            mainHandler.post(() -> {
                int pos;
                if (uid == 0) {
                    pos = getMyGuestPosition();
                } else {
                    pos = getGestPosition(uid);
                }
                if (pos != -1 && pos < roomGests.size()) {
                    roomGests.get(pos).handleSpeak(volume > 3);
                }
            });
        }
    }

    private void removeGests(int uid) {
        if (mLiveHandler != null) {

            mLiveHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mEngine != null) {
                        int x = getGestPosition(uid);
                        if (x != -1 && x < roomGests.size()) {
                            roomGests.get(x).removeFromParent();
                            roomGests.get(x).release();
                            roomGests.remove(x);
                        }
                    }
                }
            });
        }
    }

    public int getGestPosition(int uid) {
        int poistion = -1;
        if (!roomGests.isEmpty()) {
            for (int i = 0; i < roomGests.size(); i++) {
                if (uid == roomGests.get(i).bean.getUser_id()) {
                    poistion = i;
                    break;
                }
            }
        }
        return poistion;
    }

    private void handleGests(int uid) {
        boolean isFound = false;
        for (int i = 0; i < ((LiveAudienceActivity) mContext).mLiveGestList.size(); i++) {
            LiveGestBean bean = ((LiveAudienceActivity) mContext).mLiveGestList.get(i);
            if (bean.getUser_id() == uid) {
                isFound = true;
            }
        }
        if (isFound) {
            handleWhenEnterAllGests(uid);
        } else {
            if (roomGests.isEmpty()) {
                L.e(TAG, "link gest 1" + uid);
                mLinkMicAudienceUid = uid;
                handleAddGuestWhenRoomRunning(uid, 0);
            } else if (roomGests.size() == 1) {
                L.e(TAG, "link gest 2" + uid);
                mLinkMicAudienceUid2 = uid;
                handleAddGuestWhenRoomRunning(uid, 1);
            } else if (roomGests.size() == 2) {
                L.e(TAG, "link gest 3" + uid);
                mLinkMicAudienceUid3 = uid;
                handleAddGuestWhenRoomRunning(uid, 2);
            } else if (roomGests.size() == 3) {
                handleAddGuestWhenRoomRunning(uid, 3);
            }
        }

    }

    private String handleIncome(int income) {
        if (income == 0) {
            return "0";
        }
        if (income >= 1000 && income < 1000000) {
            return (income / 1000) + "K";
        } else if (income >= 1000000) {
            return (income / 1000000) + "M";
        } else {
            return "" + income;
        }
    }

    private void handleAddGuestWhenRoomRunning(int uid, int pos) {
        if (mLiveHandler == null)
            mLiveHandler = new Handler(Looper.getMainLooper());

        mLiveHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mEngine != null) {


                    LiveHttpUtil.getLiveGestId(uid, mLiveUid, ((LiveAudienceActivity) mContext).mLiveBean.getStream(), new HttpCallback() {
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
                                bean.setUser_id(uid);
                                LiveGuestViewHolder holder = new LiveGuestViewHolder(mContext, gestCont, LivePlayAgoraViewHolder.this::showUserDialog);
                                holder.bean = bean;
                                roomGests.add(holder);
                                holder.addToParent();
                                holder.subscribeActivityLifeCycle();
                                holder.setData();

                                VideoCanvas remote = new VideoCanvas(holder.textureView, RENDER_MODE_HIDDEN, uid);
                                mEngine.setupRemoteVideo(remote);
                            }
                        }
                    });

                }
            }
        });
    }

    AlertDialog dialog;

    @Override
    public void toggleLinkMicPushAgora(boolean isPush, String uid) {
        if (uid.equals(CommonAppConfig.getInstance().getUid())) {
            if (mEngine != null) {
                if (isPush) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    View view = LayoutInflater.from(mContext).inflate(R.layout.gest_cam_mic_settings, null);
                    SwitchCompat camEnable = view.findViewById(R.id.cam_switch);
                    SwitchCompat micEnable = view.findViewById(R.id.mic_switch);
                    AppCompatButton btnDone = view.findViewById(R.id.btn_done);
                    btnDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myCamStatus = camEnable.isChecked() ? 1 : 0;
                            myMicStatus = micEnable.isChecked() ? 1 : 0;
                            com.livestreaming.common.Constants.isMyCamOpen = camEnable.isChecked();
                            com.livestreaming.common.Constants.isMyMicOpen = micEnable.isChecked();
                            dialog.dismiss();
                            handleMeUpGuest();
                        }
                    });
                    dialog = builder.setView(view).setCancelable(false).show();
                } else {
                    try {
                        notifyBackendDeleteMeAsGest();
                        com.livestreaming.common.Constants.isMeInLinkMic = false;
                        int myPos = getMyGuestPosition();
                        if (myPos != -1 && myPos < roomGests.size()) {
                            roomGests.get(myPos).removeFromParent();
                            roomGests.get(myPos).release();
                            roomGests.remove(myPos);

                        }
                        mEngine.stopPreview();
                        mEngine.disableVideo();
                        mEngine.setClientRole(CLIENT_ROLE_AUDIENCE);
                        mEngine.setupLocalVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, mUid));

                        ((LiveAudienceActivity) mContext).setIsScrolling(true);

                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    private void handleMeUpGuest() {
        ((LiveAudienceActivity) mContext).setUserScrollEnabled(false);
        com.livestreaming.common.Constants.isMeInLinkMic = true;
        LiveGestBean bean = new LiveGestBean();
        bean.setFrame(CommonAppConfig.getInstance().getUserBean().getFrame());
        bean.setAvatar(CommonAppConfig.getInstance().getUserBean().getAvatar());
        bean.setUser_id(Integer.parseInt(CommonAppConfig.getInstance().getUid()));
        bean.setUserName(CommonAppConfig.getInstance().getUserBean().getUserNiceName());
        bean.setIncome(0);
        bean.setCam_type(myCamStatus);
        bean.setMic_type(myMicStatus);
        LiveGuestViewHolder holder = new LiveGuestViewHolder(mContext, gestCont, LivePlayAgoraViewHolder.this::showUserDialog);
        holder.bean = bean;
        roomGests.add(holder);
        holder.addToParent();
        holder.subscribeActivityLifeCycle();
        holder.setData();

        VideoCanvas videoCanvas = new VideoCanvas(holder.textureView, RENDER_MODE_HIDDEN, Integer.parseInt(CommonAppConfig.getInstance().getUid()));
        videoCanvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
        mEngine.setupLocalVideo(videoCanvas);
        mEngine.startPreview();
        mEngine.setClientRole(CLIENT_ROLE_BROADCASTER);
        ((LiveAudienceActivity) mContext).updateAddGest(com.livestreaming.common.Constants.isMyCamOpen ? 1 : 0, com.livestreaming.common.Constants.isMyMicOpen ? 1 : 0, bean);
        if (myMicStatus == 0) {
            openCloseMyMicAsGestLocal(false);
        }
        if (myCamStatus == 0) {
            closeMyCamAsGestLocal(false);
        }
        ((LiveAudienceActivity) mContext).setIsScrolling(false);
    }

    private void updateMeAsGest() {
        ((LiveAudienceActivity) mContext).updateAddGest(myCamStatus, myMicStatus, roomGests.get(getMyGuestPosition()).bean);
    }

    private int getMyGuestPosition() {
        int pos = -1;
        for (int i = 0; i < roomGests.size(); i++) {
            if (roomGests.get(i).getBean().getUser_id() == Integer.parseInt(CommonAppConfig.getInstance().getUid())) {
                pos = i;
            }
        }
        return pos;
    }

    private void notifyBackendDeleteMeAsGest() {
        ((LiveAudienceActivity) mContext).deleteMeGest();
    }


    private TXVodPlayer getVodPlayer() {
        if (mVodPlayer == null) {
            mVodPlayer = new TXVodPlayer(mContext);
            TXVodPlayConfig playConfig = new TXVodPlayConfig();
            playConfig.setProgressInterval(200);
            playConfig.setHeaders(CommonAppConfig.HEADER);
            mVodPlayer.setConfig(playConfig);
            mVodPlayer.setLoop(true);
            mVodPlayer.setAutoPlay(false);
            mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
            mVodPlayer.setVodListener(new ITXVodPlayListener() {
                @Override
                public void onPlayEvent(TXVodPlayer txVodPlayer, int e, Bundle bundle) {
//                    if (e != 2005) {
//                        L.e(TAG, "------onPlayEvent----->" + e);
//                    }
                    switch (e) {
                        case TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED:
                            if (mVodPlayer != null) {
                                mVodPlayer.resume();
                            }
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://播放开始
                            if (mLoading != null && mLoading.getVisibility() == View.VISIBLE) {
                                mLoading.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                            if (mLoading != null && mLoading.getVisibility() != View.VISIBLE) {
                                mLoading.setVisibility(View.VISIBLE);
                            }
                            break;
                        case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME://第一帧
                            mShowVideoFirstFrame = true;
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_END://播放结束
                            replay();
                            break;
                        case TXVodConstants.VOD_PLAY_EVT_CHANGE_RESOLUTION:
                            if (mChangeToLeft || mChangeToAnchorLinkMic) {
                                return;
                            }
                            mVideoWidth = bundle.getInt("EVT_PARAM1", 0);
                            mVideoHeight = bundle.getInt("EVT_PARAM2", 0);
                            changeVideoSize(false);
                            break;
                        case TXLiveConstants.PLAY_ERR_NET_DISCONNECT://播放失败
                        case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND:
                            ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.live_play_error));
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS:
                            if (mShowVideoFirstFrame) {
                                mShowVideoFirstFrame = false;
                                if (mVideoHandler == null) {
                                    mVideoHandler = new Handler();
                                }
                                if (mVideoFirstFrameRunnable == null) {
                                    mVideoFirstFrameRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mVideoView != null) {
                                                mVideoView.setTranslationX(0);
                                            }
                                            hideCover();
                                            mVodPlayer.setMute(false);
                                        }
                                    };
                                }
                                mVideoHandler.postDelayed(mVideoFirstFrameRunnable, 200);
                            }
                            break;
                    }
                }

                @Override
                public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

                }
            });
            mVodPlayer.setPlayerView(mVideoView);
        }
        return mVodPlayer;
    }

    public void play(String url) {

    }


    @Override
    public void playAgora(String url, boolean isVideo, String agoraToken, String channelId, int liveUid) {
        L.e(TAG, "playAgora------url----->" + url);
        mIsLive = !isVideo;
        mLiveUid = -1;
        if (isVideo) {
            if (TextUtils.isEmpty(url)) {
                return;
            }
            int playType = -1;
            if (url.startsWith("trtc://") || url.startsWith("rtmp://")) {
                playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
            } else if (url.contains(".flv") || url.contains(".FLV")) {
                playType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
            } else if (url.contains(".m3u8")) {
                playType = TXLivePlayer.PLAY_TYPE_VOD_HLS;
            } else if (url.contains(".mp4") || url.contains(".MP4")) {
                playType = TXLivePlayer.PLAY_TYPE_VOD_MP4;
            }
            if (playType == -1) {
                ToastUtil.show(com.livestreaming.common.R.string.live_play_error_2);
                return;
            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(true);
            }
            if (mVideoView != null) {
                mVideoView.setTranslationX(100000);
            }
            mShowVideoFirstFrame = false;
            getVodPlayer().startVodPlay(url);
        } else {
            mLiveUid = liveUid;
            if (mVideoView != null) {
                mVideoView.setTranslationX(100000);
            }
            if (mEngine != null) {
                ChannelMediaOptions option = new ChannelMediaOptions();
                option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
                option.clientRoleType = CLIENT_ROLE_AUDIENCE;
                option.autoSubscribeAudio = true;
                option.autoSubscribeVideo = true;
                option.publishMicrophoneTrack = true;
                option.publishCameraTrack = true;
                mEngine.joinChannel(agoraToken, channelId, mUid, option);
            }
        }

    }

    private void changeVideoSize(boolean landscape) {
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float videoRatio = mVideoWidth / mVideoHeight;
            float p1 = mParentView.getWidth();
            float p2 = mParentView.getHeight();
            float parentWidth = p1;
            float parentHeight = p2;
            if (landscape) {
                parentWidth = Math.max(p1, p2);
                parentHeight = Math.min(p1, p2);
            } else {
                parentWidth = Math.min(p1, p2);
                parentHeight = Math.max(p1, p2);
            }
//            L.e("changeVideoSize", "mVideoWidth----->" + mVideoWidth + "  mVideoHeight------>" + mVideoHeight);
//            L.e("changeVideoSize", "parentWidth----->" + parentWidth + "  parentHeight------>" + parentHeight);
            float parentRatio = parentWidth / parentHeight;
            if (videoRatio != parentRatio) {
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
                if (videoRatio > 10f / 16f && videoRatio > parentRatio) {
                    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.height = (int) (parentWidth / videoRatio);
                    p.gravity = Gravity.CENTER;
                } else {
                    p.width = (int) (parentHeight * videoRatio);
                    p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.gravity = Gravity.CENTER;
                }
                mVideoView.requestLayout();
//                View innerView = mVideoView.getVideoView();
//                if (innerView != null) {
//                    ViewGroup.LayoutParams innerLp = innerView.getLayoutParams();
//                    innerLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    innerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                    innerView.setLayoutParams(innerLp);
//                }
                ((LiveAudienceActivity) mContext).onVideoHeightChanged(p.height, mRootHeight);
            }
        }
    }


    private void changeLiveSize(boolean landscape) {
        if (mChangeToLeft || mChangeToAnchorLinkMic) {
            return;
        }
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float videoRatio = mVideoWidth / mVideoHeight;
            float p1 = mParentView.getWidth();
            float p2 = mParentView.getHeight();
            float parentWidth = p1;
            float parentHeight = p2;
            if (landscape) {
                parentWidth = Math.max(p1, p2);
                parentHeight = Math.min(p1, p2);
            } else {
                parentWidth = Math.min(p1, p2);
                parentHeight = Math.max(p1, p2);
            }
//            L.e("changeVideoSize", "mVideoWidth----->" + mVideoWidth + "  mVideoHeight------>" + mVideoHeight);
//            L.e("changeVideoSize", "parentWidth----->" + parentWidth + "  parentHeight------>" + parentHeight);
            float parentRatio = parentWidth / parentHeight;
            if (videoRatio != parentRatio) {
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
                if (videoRatio > 10f / 16f && videoRatio > parentRatio) {
                    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.height = (int) (parentWidth / videoRatio);
                    p.gravity = Gravity.CENTER;
                } else {
                    p.width = (int) (parentHeight * videoRatio);
//                    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.gravity = Gravity.CENTER;
                }
                mLiveView.requestLayout();
                if (!mChangeToAnchorLinkMic) {
                    ((LiveAudienceActivity) mContext).onVideoHeightChanged(p.height, mRootHeight);
                }
            }
        }
    }

    @Override
    public void changeSize(boolean landscape) {
        if (mIsLive) {
            changeLiveSize(landscape);
        } else {
            changeVideoSize(landscape);
        }
    }

    @Override
    public void hideCover() {
        if (mCover != null && mCover.getAlpha() != 0f) {
            L.e(TAG, "隐藏封面---hideCover--->");
            mCover.animate().alpha(0).setDuration(500).start();
        }
    }

    public void switchLiveCam(boolean isCamOpen, String uid, String thumb) {

        if (!isCamOpen) {
            // Stop the camera
            mEngine.enableLocalVideo(false);

            Glide.with(mContext)
                    .asBitmap()
                    .load(thumb)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            if (mPkContainer != null && mPkContainer.getVisibility() == View.VISIBLE) {

                                if (uid.equals("" + (((LiveAudienceActivity) mContext).getPkuid()))) {
                                    findViewById(R.id.right_cam_cover).setVisibility(View.VISIBLE);
                                    mRightContainer.setVisibility(View.INVISIBLE);
                                    ((ImageView) findViewById(R.id.right_cam_cover)).setImageBitmap(resource);
                                } else {
                                    mLeftContainer.setVisibility(View.INVISIBLE);
                                    findViewById(R.id.left_cam_cover).setVisibility(View.VISIBLE);
                                    ((ImageView) findViewById(R.id.left_cam_cover)).setImageBitmap(resource);
                                }
                            } else {
                                mLiveView.setVisibility(View.INVISIBLE);
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
            if (mPkContainer != null && mPkContainer.getVisibility() == View.VISIBLE) {
                if (uid.equals("" + (((LiveAudienceActivity) mContext).getPkuid()))) {
                    findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
                    mRightContainer.setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
                    mLeftContainer.setVisibility(View.VISIBLE);
                }
            } else {
                mLiveView.setVisibility(View.VISIBLE);
                mCover.setVisibility(View.GONE);
            }
            mEngine.enableLocalVideo(true);  // Re-enable the camera
            mEngine.startPreview();          // Start camera preview
        }
    }

    public void clearCams() {
        findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
        findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
    }

    @Override
    public void setCover(String coverUrl) {
        if (mCover != null) {
            mCover.setVisibility(View.VISIBLE);
            ImgLoader.displayBlur(mContext, coverUrl, mCover);
        }
    }

    /**
     * 循环播放
     */
    private void replay() {
//        if (mVodPlayer != null) {
//            mVodPlayer.seek(0);
//            mVodPlayer.resume();
//        }
    }


    /**
     * 暂停播放
     */
    @Override
    public void pausePlay() {
        if (!mPausedPlay) {
            mPausedPlay = true;
            if (!mPaused) {
//                if (mLivePlayer != null) {
//                    mLivePlayer.setPlayoutVolume(0);
//                }
                if (mVodPlayer != null) {
                    mVodPlayer.setMute(true);
                }
            }
            if (mCover != null) {
                mCover.setAlpha(1f);
                if (mCover.getVisibility() != View.VISIBLE) {
                    mCover.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    @Override
    public void resumePlay() {
        if (mPausedPlay) {
            mPausedPlay = false;
            if (!mPaused) {
//                if (mLivePlayer != null) {
//                    mLivePlayer.setPlayoutVolume(100);
//                }
                if (mVodPlayer != null) {
                    mVodPlayer.setMute(false);
                }
            }
            hideCover();
        }
    }

    @Override
    public void stopPlay() {
        mChangeToLeft = false;
        mChangeToAnchorLinkMic = false;
        mLinkMicAudienceUid = 0;
        mLinkMicAudienceUid2 = 0;
        mLinkMicAudienceUid3 = 0;
        mLinkMicAnchorUid = 0;
        if (mVideoHandler != null) {
            mVideoHandler.removeCallbacksAndMessages(null);
        }
        if (mCover != null) {
            mCover.setAlpha(1f);
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
        }
        L.e(TAG, "stopPlay-------->");
        if (mEngine != null) {
            mEngine.leaveChannel();
            /*if (mOtherAnchorRtcConnection != null) {
                mEngine.leaveChannelEx(mOtherAnchorRtcConnection);
                mOtherAnchorRtcConnection = null;
            }*/
            mEngine.stopPreview();
        }
        if (mVodPlayer != null) {
            mVodPlayer.pause();
        }
        if (mVideoView != null) {
            mVideoView.setTranslationX(100000);
        }
        mShowVideoFirstFrame = false;
    }

    @Override
    public void release() {
        gestCont.removeAllViews();
        roomGests.clear();
        ((LiveAudienceActivity) mContext).deleteMeGest();
        ((LiveAudienceActivity) mContext).mLiveGestList.clear();
        if (mEngine != null) {
            mEngine.leaveChannel();
            mEngine.stopPreview();

            // Wait for the handler to execute the destruction
            mLiveHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEngine = null;  // Ensure this happens after destruction is complete
                }
            });
        }
        if (mVideoHandler != null) {
            mVideoHandler.removeCallbacksAndMessages(null);
        }
        mVideoHandler = null;
        if (mVodPlayer != null) {
            mVodPlayer.stopPlay(false);
            mVodPlayer.setVodListener(null);
        }
        mVodPlayer = null;
        L.e(TAG, "release------->");
    }


    @Override
    public LinearLayout getGestConitaner() {
        return gestCont;
    }

    @Override
    public ViewGroup getRightContainer() {
        return mRightContainer;
    }

    @Override
    public ViewGroup getPkContainer() {
        return mPkContainer;
    }

    public void showUserDialog(String toUid) {
        if (!TextUtils.isEmpty(String.valueOf(mLiveUid)) && !TextUtils.isEmpty(toUid)) {
            LiveUserDialogFragment fragment = new LiveUserDialogFragment();
            fragment.whenUserpClick = new LiveUserDialogFragment.WhenUserpClick() {
                @Override
                public void onClick(String uid) {
                    RouteUtil.forwardUserHome(mContext, toUid, true, String.valueOf(mLiveUid));
                }
            };
            Bundle bundle = new Bundle();
            bundle.putString(com.livestreaming.common.Constants.LIVE_UID, String.valueOf(mLiveUid));
            Log.e("open_uid", " " + toUid);
            bundle.putString(com.livestreaming.common.Constants.TO_UID, toUid);
            fragment.setArguments(bundle);
            fragment.show(((LiveActivity) mContext).getSupportFragmentManager(), "LiveUserDialogFragment");
        }
    }


    @Override
    public void changeToLeft() {
        mChangeToLeft = true;
        if (mLiveView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
            params.width = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
            params.height = DpUtil.dp2px(360);
            params.topMargin = DpUtil.dp2px(130);
            params.gravity = Gravity.TOP;
            mLiveView.setLayoutParams(params);
        }
        if (mLoading != null && mLeftContainer != null) {
            ViewParent viewParent = mLoading.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mLoading);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DpUtil.dp2px(24), DpUtil.dp2px(24));
            params.gravity = Gravity.CENTER;
            mLoading.setLayoutParams(params);
            mLeftContainer.addView(mLoading);
        }
    }

    @Override
    public void changeToBig() {
        mChangeToLeft = false;
        if (mLiveView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.topMargin = 0;
            mLiveView.setLayoutParams(params);
        }
        if (mLoading != null && mRoot != null) {
            ViewParent viewParent = mLoading.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mLoading);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DpUtil.dp2px(24), DpUtil.dp2px(24));
            params.gravity = Gravity.CENTER;
            mLoading.setLayoutParams(params);
            mRoot.addView(mLoading);
        }
    }

    @Override
    public void onResume() {
        if (!mPausedPlay && mPaused) {
//            if (mLivePlayer != null) {
//                mLivePlayer.setPlayoutVolume(100);
//            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(false);
            }
        }
        mPaused = false;
    }

    @Override
    public void onPause() {
        if (!mPausedPlay) {
//            if (mLivePlayer != null) {
//                mLivePlayer.setPlayoutVolume(0);
//            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(true);
            }
        }
        mPaused = true;
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }


    @Override
    public void setAnchorLinkMic(final boolean anchorLinkMic, int delayTime) {
        if (mLiveView == null) {
            return;
        }
        if (delayTime < 0) {
            delayTime = 0;
        }
        if (delayTime > 0) {
            if (mVideoHandler == null) {
                mVideoHandler = new Handler();
            }
            mVideoHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    setLiveLinkMicImageVisible(true);
                    mChangeToAnchorLinkMic = anchorLinkMic;
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
                    if (anchorLinkMic) {
                        params.width = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
                        params.height = DpUtil.dp2px(360);
                        params.topMargin = DpUtil.dp2px(130);
                        params.gravity = Gravity.TOP;
                    } else {
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.topMargin = 0;
                        params.gravity = Gravity.CENTER;
                    }
                    mLiveView.setLayoutParams(params);
                }
            }, delayTime);
        } else {
            mChangeToAnchorLinkMic = anchorLinkMic;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
            if (anchorLinkMic) {
                params.width = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
                params.height = DpUtil.dp2px(360);
                params.topMargin = DpUtil.dp2px(130);
                params.gravity = Gravity.TOP;
            } else {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.topMargin = 0;
                params.gravity = Gravity.CENTER;
            }
            mLiveView.setLayoutParams(params);
        }
    }


    public void onAnchorLinkMicClose() {
        if (mLinkMicAnchorUid != 0) {
            L.e(TAG, "IRtcEngineEventHandler---主播连麦断开--->" + mLinkMicAnchorUid);
            if (mRightContainer != null) {
                mRightContainer.removeAllViews();
                clearCams();
                setLiveLinkMicImageVisible(false);
            }
            mLinkMicAnchorUid = 0;
        }
    }


    public void onAnchorLinkMicStart(String pkUid) {
        onAnchorLinkMic(Integer.parseInt(pkUid), false);
    }

    private void onAnchorLinkMic(int pkUid, boolean fromAgoraHandler) {
        if (mLinkMicAnchorUid == 0) {
            mLinkMicAnchorUid = pkUid;
            if (mLiveHandler != null) {
                mLiveHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEngine != null) {
                            TextureView textureView = new TextureView(mContext);
                            mRightContainer.addView(textureView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, pkUid);
                            mEngine.setupRemoteVideo(remote);
                            setLiveLinkMicImageVisible(true);
                        }
                    }
                });
            }
        }
    }

    public void onOpenCloseGestssMic(int b, String touid) {
        if (!touid.equals(CommonAppConfig.getInstance().getUid())) {
            int x = getGestPosition(Integer.parseInt(touid));
            if (x != -1 && x < roomGests.size()) {
                roomGests.get(x).handleMute(b == 1);
            }
        }
    }

    public void openCloseMyMicAsGest(boolean b) {
        myMicStatus = !b ? 0 : 1;
        int x = getMyGuestPosition();
        if (x != -1 && x < roomGests.size()) {
            roomGests.get(x).handleMute(b);
            ((LiveAudienceActivity) mContext).onGestsCloseOpenMic(x, b);
            mEngine.muteLocalAudioStream(!b);
            updateMeAsGest();
        }
    }

    public void openCloseMyMicAsGestLocal(boolean b) {
        myMicStatus = !b ? 0 : 1;
        int x = getMyGuestPosition();
        if (x != -1 && x < roomGests.size()) {
            roomGests.get(x).handleMute(b);
            ((LiveAudienceActivity) mContext).onGestsCloseOpenMic(x, b);
            mEngine.muteLocalAudioStream(!b);
        }
    }

    public void closeMyCamAsGest(boolean b) {
        try {
            int pos = getGestPosition(Integer.parseInt(CommonAppConfig.getInstance().getUid()));
            if (pos != -1) {
                roomGests.get(pos).handleGuestCam(b);
                myCamStatus = !b ? 0 : 1;
                if (!b) {
                    mEngine.muteLocalVideoStream(true);
                    mEngine.stopPreview();
                } else {
                    mEngine.muteLocalVideoStream(false);
                    mEngine.startPreview();
                }
                updateMeAsGest();
                ((LiveAudienceActivity) mContext).onMeAsGestChangeCam(getGestPosition(Integer.parseInt(CommonAppConfig.getInstance().getUid())), b);
            }
        } catch (Exception e) {

        }
    }

    public void closeMyCamAsGestLocal(boolean b) {
        try {
            int pos = getMyGuestPosition();
            if (pos != -1 && pos < roomGests.size()) {
                roomGests.get(pos).handleGuestCam(b);
                if (!b) {
                    mEngine.muteLocalVideoStream(true);
                    mEngine.stopPreview();
                } else {
                    mEngine.muteLocalVideoStream(false);
                    mEngine.startPreview();
                }
                myCamStatus = !b ? 0 : 1;
                ((LiveAudienceActivity) mContext).onMeAsGestChangeCam(getMyGuestPosition(), b);
            }
        } catch (Exception e) {

        }
    }

    public void handleGestCam(boolean b, String touid) {
        if (!touid.equals(CommonAppConfig.getInstance().getUid())) {
            try {
                int pos = getGestPosition(Integer.parseInt(touid));
                if (pos == -1) {
                    return;
                }
                roomGests.get(pos).handleGuestCam(b);
            } catch (Exception e) {

            }
        }
    }

    ArrayList<LiveGestBean> mGestsList = new ArrayList<>();

    public void handleWhenEnterAllGests(int id) {
        if (mGestsList.isEmpty()) {
            mGestsList = ((LiveAudienceActivity) mContext).mLiveGestList;
        }
        if (mGestsList.isEmpty()) {
            return;
        }

        mLiveHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mEngine != null) {
                    LiveHttpUtil.getLiveGestId(id, mLiveUid, ((LiveAudienceActivity) mContext).mLiveBean.getStream(), new HttpCallback() {
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
                                bean.setUser_id(id);
                                LiveGuestViewHolder holder = new LiveGuestViewHolder(mContext, gestCont, LivePlayAgoraViewHolder.this::showUserDialog);
                                holder.bean = bean;
                                roomGests.add(holder);
                                holder.addToParent();
                                holder.subscribeActivityLifeCycle();
                                holder.setData();
                                VideoCanvas remote = new VideoCanvas(holder.textureView, RENDER_MODE_HIDDEN, id);
                                mEngine.setupRemoteVideo(remote);
                            }
                        }
                    });

                }
            }
        });
        mGestsList.clear();
        ((LiveAudienceActivity) mContext).mLiveGestList.clear();

    }

    public void cleanGuests() {
        roomGests = new ArrayList<>();
        gestCont.removeAllViews();
    }

    public void updateGuestIncome(int uid, int income) {
        int x = getGestPosition(uid);
        if (x != -1) {
            roomGests.get(x).handleGuestIncome(income);
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

    public void setInvestRowVisible(boolean b) {
        if (b) {
            right_investors_layout.setVisibility(View.VISIBLE);
            left_investors_layout.setVisibility(View.VISIBLE);
        } else {
            right_investors_layout.setVisibility(View.GONE);
            left_investors_layout.setVisibility(View.GONE);
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
        pkBounsCountDown2 = new MyCountdown();
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
        pkBounsCountDown3 = new MyCountdown();
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
        if (pk_bouns_layout != null) {
            pk_bouns_layout.removeAllViews();
            pk_bouns_layout.setVisibility(View.GONE);
        }
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
        if (pkBounsCountDown3 != null) {
            pkBounsCountDown3.stop();
            pkBounsCountDown3.release();
            pkBounsCountDown3 = null;
        }


    }

    public void updatePkBouns(int value, int uid, int completed) {
        if (pkBounsStartViewHolder != null) {
            pkBounsStartViewHolder.handleTargetMessage(value);
            if (completed == 1) {
                if (uid == mLiveUid) {
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
                            ImgLoader.display(mContext, ((LiveActivity) mContext).getLiveBean().getAvatar(), ((ImageView) findViewById(R.id.left_cam_cover)));
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
            if (findViewById(R.id.left_cam_cover).getVisibility() == View.VISIBLE) {
                ((ImageView) findViewById(R.id.left_cam_cover)).setImageResource(0);
                findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
                mCover.setVisibility(View.VISIBLE);
                ImgLoader.display(mContext, ((LiveActivity) mContext).getLiveBean().getAvatar(), mCover);
            }
            if (findViewById(R.id.right_cam_cover).getVisibility() == View.VISIBLE) {
                ((ImageView) findViewById(R.id.right_cam_cover)).setImageResource(0);
                findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
            }
        }
    }

    public void handleEnterRoomCams(String thumb, String avatarPk) {
        if (((LiveAudienceActivity) mContext).isLinkMicAnchor()) {
            mCover.setVisibility(View.GONE);
            LiveHttpUtil.getLiveCamStatusByUserId(((LiveAudienceActivity) mContext).mLiveBean.getUid(), new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (info.length > 0) {
                        int cam = JSONObject.parseObject(info[0]).getIntValue("camera");
                        ((LiveAudienceActivity) mContext).camType = cam;
                        if (cam == 2) {
                            findViewById(R.id.left_cam_cover).setVisibility(View.VISIBLE);
                            Glide.with(mContext).load(thumb).into(((ImageView) findViewById(R.id.left_cam_cover)));
                        } else {
                            findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
                        }
                        LiveHttpUtil.getLiveCamStatusByUserId("" + ((LiveAudienceActivity) mContext).getPkuid(), new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (info.length > 0) {
                                    int cam = JSONObject.parseObject(info[0]).getIntValue("camera");
                                    if (cam == 2) {
                                        findViewById(R.id.right_cam_cover).setVisibility(View.VISIBLE);
                                        Glide.with(mContext).load(avatarPk).into(((ImageView) findViewById(R.id.right_cam_cover)));
                                    } else {
                                        findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else {
            findViewById(R.id.left_cam_cover).setVisibility(View.INVISIBLE);
            findViewById(R.id.right_cam_cover).setVisibility(View.INVISIBLE);
            if (mContext != null) {
                LiveHttpUtil.getLiveCamStatusByUserId(((LiveAudienceActivity) mContext).mLiveBean.getUid(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (info.length > 0 && mCover != null) {
                            try {
                                int cam = JSONObject.parseObject(info[0]).getIntValue("camera");
                                ((LiveAudienceActivity) mContext).camType = cam;
                                if (cam == 2) {
                                    mCover.setVisibility(View.VISIBLE);
                                    Glide.with(mContext).load(thumb).into(mCover);
                                } else {
                                    mCover.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                });
            }
        }
    }

    public void setLiveLinkMicImageVisible(boolean b) {
        link_mic_live_img_layout.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void handleDoubleTotalPoints(int totalPoints) {
        Log.e("test_double_total_points_5", "........................");
        pk_bouns_layout = findViewById(R.id.pk_bouns_layout);
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

    public void handleWhenEnterRoomDouble(int uid, int x, int missionType, int duration, int startAfter, int doubleSec, int currentTime, int state, int target_value, int targetValueDefault, int completed) {
        if (mLiveUid == uid) {
            this.duration = duration;
            this.startAfter = startAfter;
            this.x = x;
            this.mission_type = missionType;
            this.target_value = targetValueDefault;
            this.targetDuration = doubleSec;
            pk_bouns_layout = findViewById(R.id.pk_bouns_layout);
            pk_bouns_layout.removeAllViews();
            pk_bouns_layout.setVisibility(View.VISIBLE);
            pkBounsCountDown = new MyCountdown();
            pkBounsCountDown2 = new MyCountdown();
            pkBounsCountDown3 = new MyCountdown();
            if (state == 1) {
                this.startAfter -= currentTime;
                showPkBounsWillStartAfter();
                pkBounsCountDown.setTotalSecond(this.startAfter).setCallback(new MyCountdown.ActionListener() {
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
            } else if (state == 2) {

                this.duration -= currentTime;
                if (completed == 0) {
                    pk_bouns_layout.removeAllViews();
                    getPounsStartedView();
                    pkBounsStartViewHolder.handleTargetMessage(target_value);
                    pkBounsCountDown2.setTotalSecond(this.duration).setCallback(new MyCountdown.ActionListener() {
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
                } else if (completed == 1) {

                    this.targetDuration -= currentTime;
                    Log.e("test_double_enter_room_3_2_2", "........................");
                    pk_bouns_layout.removeAllViews();
                    getPounsStartedView();
                    pkBounsStartViewHolder.targetCompleted(0);
                    pkBounsStartViewHolder.handleTargetMessage(target_value);

                    pkBounsCountDown2.setTotalSecond(this.duration).setCallback(new MyCountdown.ActionListener() {
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
            } else if (state == 3) {
                Log.e("test_double_enter_room_3_0_0", "........................");
                getPounsStartedView();
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
        }
    }

    public void onClickInvestors(int i) {
        if (i == 1) {
            //left
            ((LiveActivity) mContext).showUserPKContributorListDialog("" + mLiveUid, 0);
        } else if (i == 2) {
            //right
            String pkUid = "" + ((LiveActivity) mContext).getLinkMicAnchorUid();
            ((LiveActivity) mContext).showUserPKContributorListDialog(pkUid, 1);
        }
    }


}
