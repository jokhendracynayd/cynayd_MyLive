package com.livestreaming.live.views;

import static com.livestreaming.common.Constants.LiveVoiceRoomUserCount;
import static com.livestreaming.common.glide.ImgLoader.loadSvga;
import static io.agora.rtc2.Constants.AUDIO_PROFILE_MUSIC_STANDARD;
import static io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.Constants.PUB_STATE_PUBLISHED;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.custom.ItemDecoration;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.adapter.LiveVideoLinkMicAdapter;
import com.livestreaming.live.adapter.LiveVoiceLinkMicAdapter;
import com.livestreaming.live.bean.ChangeRoomBackBean;
import com.livestreaming.live.bean.LiveVoiceGiftBean;
import com.livestreaming.live.bean.LiveVoiceLinkMicBean;
import com.livestreaming.live.bean.LiveVoiceMixUserBean;
import com.livestreaming.live.floatwindow.FloatWindowUtil;
import com.livestreaming.live.interfaces.LivePushListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class LiveChatRoomLinkMicAgoraViewHolder extends AbsLiveChatRoomLinkMicViewHolder {

    private static final String TAG = "LiveChatRoomLinkMicAgoraViewHolder";
    private int mUserCount = LiveVoiceRoomUserCount;
    private List<LiveVoiceLinkMicBean> mList;
    private RefreshAdapter mAdapter;
    private ImageView bgImage;
    private LivePushListener mLivePushListener;
    private Handler mHandler;
    private boolean mPushMute;
    private boolean mPaused;
    private boolean mCameraReady;
    private boolean mPushSucceed;//是否推流成功
    private TextureView[] mTextureViewArr;
    private ImageView[] mCoverArr;
    private Drawable mBgDrawable;
    private View mRootContainer;
    private boolean mChatRoomTypeVideo;
    public RtcEngineEx mEngine;
    private int mLiveUid;
    private int mUid;
    private boolean mIsAnchor;
    private ArrayList<ImageView> speakers = new ArrayList<>();
    private int mPosition = 0;
    private boolean lastRoomType = false;


    public LiveChatRoomLinkMicAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_chatroom_link_mic_agora;
    }

    @Override
    public void init() {
        mUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
        mIsAnchor = mContext instanceof LiveAnchorActivity;

        Drawable bgDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_live_voice);
        bgImage = findViewById(R.id.bg_image);
        bgImage.setImageDrawable(bgDrawable);
        mBgDrawable = bgDrawable;
        mList = new ArrayList<>();
        mRootContainer = findViewById(R.id.voice_link_mic_container);
        if (mIsAnchor) {
            mLiveUid = mUid;
        } else {
            mRootContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((LiveAudienceActivity) mContext).light();
                }
            });
        }
        initEngine();
    }

    private void initEngine() {
        if (mEngine == null) {
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
                    if (mEngine == null || mUid == uid) {
                        return;
                    }
                    if (mChatRoomTypeVideo) {
                        TextureView textureView = getPlayView(String.valueOf(uid));
                        if (textureView != null) {
                            textureView.setVisibility(View.VISIBLE);
                            VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, uid);
                            mEngine.setupRemoteVideo(remote);
                        }
                    }

                }

                @Override
                public void onUserOffline(int uid, int reason) {
                    L.e(TAG, "IRtcEngineEventHandler---onUserOffline--->" + uid);
                    if (mLiveUid == uid) {
                        if (!mIsAnchor && LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
                            FloatWindowUtil.getInstance().release();
                        }
                    }
//                int pos = getUserPosition("" + uid);
//                if (mChatRoomTypeVideo) {
//                    if (pos < speakers.size() && pos != -1) {
//                        speakers.get(pos).setVisibility(View.GONE);
//                        mTextureViewArr[pos].setVisibility(View.INVISIBLE);
//                    }
//                } else {
//                    if (pos != -1) {
//                        if (mAdapter != null && !mAdapter.getList().isEmpty() && mAdapter instanceof LiveVoiceLinkMicAdapter) {
//                            ((LiveVoiceLinkMicBean) mAdapter.getList().get(pos)).speak = false;
//                            mAdapter.notifyItemChanged(pos);
//                        }
//                    }
//                    onUserDownMic(pos);
//                }
                }

                @Override
                public void onUserMuteAudio(int uid, boolean muted) {

//                if (muted) {
//                    int pos = getUserPosition("" + uid);
//                    if (mChatRoomTypeVideo) {
//                        if (pos < speakers.size() && pos != -1) {
//                            speakers.get(pos).setVisibility(View.GONE);
//                        }
//                    } else {
//                        if (pos != -1) {
//                            if (mAdapter != null && !mAdapter.getList().isEmpty()) {
//                                ((LiveVoiceLinkMicBean) mAdapter.getList().get(pos)).speak = false;
//                                mAdapter.notifyItemChanged(pos, "speak");
//                            }
//                        }
//                    }
//                }
                }

                @Override
                public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                    L.e(TAG, "IRtcEngineEventHandler---onFirstRemoteVideoFrame--->" + uid);
                }

                @Override
                public void onFirstRemoteAudioFrame(int uid, int elapsed) {
                    L.e(TAG, "IRtcEngineEventHandler---onFirstRemoteAudioFrame--->" + uid);
                }


                @Override
                public void onAudioPublishStateChanged(String channel, int oldState, int newState, int elapseSinceLastState) {
                    L.e(TAG, "IRtcEngineEventHandler---onAudioPublishStateChanged--->");
                    if (newState == PUB_STATE_PUBLISHED) {
                        if (!mPushSucceed) {
                            mPushSucceed = true;
                            if (mLivePushListener != null) {
                                mLivePushListener.onPushStart();
                            }
                            L.e(TAG, "IRtcEngineEventHandler--->推流成功");
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
            try {
                mEngine = (RtcEngineEx) RtcEngine.create(config);
                mEngine.enableAudioVolumeIndication(1500, 3, true);
            } catch (Exception e) {
                mEngine = null;
            }
            if (mEngine != null) {
                mEngine.setClientRole(mIsAnchor ? CLIENT_ROLE_BROADCASTER : CLIENT_ROLE_AUDIENCE);
            }
        }
    }

    Handler mainHandler = new Handler(Looper.getMainLooper());

    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 500;

    private void handleVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers) {
        long currentTime = System.currentTimeMillis();

        // Only update the UI if enough time has passed since the last update
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        mainHandler.post(() -> {
            if (mAdapter instanceof LiveVoiceLinkMicAdapter) {
                for (Object i : mAdapter.getList()
                ) {
                    ((LiveVoiceLinkMicBean) i).speak = false;
                }
            } else {
                for (ImageView i : this.speakers
                ) {
                    i.setVisibility(View.GONE);
                }
            }
            for (IRtcEngineEventHandler.AudioVolumeInfo speaker : speakers) {
                int uid = speaker.uid;
                int volume = speaker.volume;

                if (uid == 0) {
                    if (mPosition != -1 && mPosition < this.speakers.size()) {
                        this.speakers.get(mPosition).setVisibility(volume > 3 ? View.VISIBLE : View.GONE);
//                        if (!mIsAnchor && mAdapter instanceof LiveVoiceLinkMicAdapter && !mAdapter.getList().isEmpty()) {
//                            LiveVoiceLinkMicBean item = (LiveVoiceLinkMicBean) mAdapter.getList().get(mPosition);
//                            item.speak = volume > 3;
//                            itemsToUpdate.add(mPosition);
//                        }
                    }
                } else {
                    int position = getUserPosition(String.valueOf(uid));
                    if (position != -1) {
                        if (mChatRoomTypeVideo) {
                            if (position < this.speakers.size()) {
                                if (this.speakers.get(position).getVisibility() != (volume > 3 ? View.VISIBLE : View.GONE)) {
                                    this.speakers.get(position).setVisibility(volume > 3 ? View.VISIBLE : View.GONE);
                                }
                            }
                        }
                        if (mAdapter != null && mAdapter instanceof LiveVoiceLinkMicAdapter) {
                            ((LiveVoiceLinkMicBean) mAdapter.getList().get(position)).speak = volume > 3;

                        }
                    }
                }
            }

            // Update all items at once
            if (mAdapter != null && mAdapter instanceof LiveVoiceLinkMicAdapter) {
                for (int i = 0; i < mAdapter.getList().size(); i++) {
                    mAdapter.notifyItemChanged(i, "speak");
                }
            }
        });
    }


    public void setChatRoomType(int chatRoomType, UserBean anchorInfo, boolean isFirstTime) {

        initEngine();
        mChatRoomTypeVideo = chatRoomType == Constants.CHAT_ROOM_TYPE_VIDEO;
        if (mChatRoomTypeVideo) {
            mEngine.enableVideo();
            VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_360x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            );
            encoderConfiguration.orientationMode = ORIENTATION_MODE_FIXED_PORTRAIT;
            encoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
            mEngine.setVideoEncoderConfiguration(encoderConfiguration);
        } else {
            mEngine.disableVideo();
        }
        LiveChatRoomPlayUtil.getInstance().setAgoraEngine(!mIsAnchor && !mChatRoomTypeVideo ? mEngine : null);

        if (chatRoomType == Constants.CHAT_ROOM_TYPE_VOICE) {
            mUserCount = LiveVoiceRoomUserCount;
            mList.clear();
            for (int i = 0; i < mUserCount; i++) {
                mList.add(new LiveVoiceLinkMicBean());
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            if (isFirstTime || (!isFirstTime && lastRoomType)) {
                lp.topMargin = DpUtil.dp2px(150);
            }
            recyclerView.setLayoutParams(lp);
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, mUserCount == 4 ? 2 : 4, GridLayoutManager.VERTICAL, false));
            mAdapter = new LiveVoiceLinkMicAdapter(mContext, mList);
            recyclerView.setAdapter(mAdapter);
            findViewById(R.id.group_preview).setVisibility(View.GONE);
        } else {
            mUserCount = LiveVoiceRoomUserCount;
            mList.clear();
            for (int i = 0; i < mUserCount; i++) {
                mList.add(new LiveVoiceLinkMicBean());
            }
            mTextureViewArr = new TextureView[mUserCount];
            mTextureViewArr[0] = findViewById(R.id.camera_preview_0);
            mTextureViewArr[1] = findViewById(R.id.camera_preview_1);
            mTextureViewArr[2] = findViewById(R.id.camera_preview_2);
            mTextureViewArr[3] = findViewById(R.id.camera_preview_3);
            mTextureViewArr[4] = findViewById(R.id.camera_preview_4);
            mTextureViewArr[5] = findViewById(R.id.camera_preview_5);
            mTextureViewArr[6] = findViewById(R.id.camera_preview_6);
            mTextureViewArr[7] = findViewById(R.id.camera_preview_7);
            mTextureViewArr[8] = findViewById(R.id.camera_preview_8);
            mCoverArr = new ImageView[mUserCount];
            mCoverArr[1] = findViewById(R.id.cover_1);
            mCoverArr[2] = findViewById(R.id.cover_2);
            mCoverArr[3] = findViewById(R.id.cover_3);
            mCoverArr[4] = findViewById(R.id.cover_4);
            mCoverArr[5] = findViewById(R.id.cover_5);
            mCoverArr[6] = findViewById(R.id.cover_6);
            mCoverArr[7] = findViewById(R.id.cover_7);
            mCoverArr[8] = findViewById(R.id.cover_8);
            speakers = new ArrayList<>();
            speakers.add(((ImageView) findViewById(R.id.speak_1)));
            speakers.add(((ImageView) findViewById(R.id.speak_2)));
            speakers.add(((ImageView) findViewById(R.id.speak_3)));
            speakers.add(((ImageView) findViewById(R.id.speak_4)));
            speakers.add(((ImageView) findViewById(R.id.speak_5)));
            speakers.add(((ImageView) findViewById(R.id.speak_6)));
            speakers.add(((ImageView) findViewById(R.id.speak_7)));
            speakers.add(((ImageView) findViewById(R.id.speak_8)));
            speakers.add(((ImageView) findViewById(R.id.speak_9)));
            if (mRootContainer != null) {
                mRootContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        int w = mRootContainer.getWidth();
                        int h = mRootContainer.getHeight();
                        int[] location = new int[2];
                        for (int i = 0; i < mUserCount; i++) {
                            if (mCoverArr[i] != null) {
                                if ((!isFirstTime && !lastRoomType) || isFirstTime) {
                                    mCoverArr[i].getLocationInWindow(location);
                                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mCoverArr[i].getLayoutParams();
                                    lp.width = w;
                                    lp.height = h;
                                    lp.leftMargin = -location[0];
                                    lp.topMargin = -location[1];
                                    mCoverArr[i].requestLayout();
                                    mCoverArr[i].setImageDrawable(mBgDrawable);
                                }
                            }
                        }
                    }
                });
            }

            LiveVoiceLinkMicBean anchorBean = mList.get(0);
            if (anchorBean != null) {
                anchorBean.setUid(anchorInfo.getId());
                anchorBean.setAvatar(anchorInfo.getAvatar());
                anchorBean.setFrame(anchorInfo.getFrame());
                anchorBean.setUserName(anchorInfo.getUserNiceName());
                anchorBean.setStatus(Constants.VOICE_CTRL_OPEN);
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
            ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 1, 1);
            if (!lastRoomType && !isFirstTime) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                lp.topMargin = DpUtil.dp2px(160);
                recyclerView.setLayoutParams(lp);
            }
            decoration.setOnlySetItemOffsetsButNoDraw(true);
            recyclerView.addItemDecoration(decoration);
            mAdapter = new LiveVideoLinkMicAdapter(mContext, mList);
            recyclerView.setAdapter(mAdapter);
            findViewById(R.id.group_preview).setVisibility(View.VISIBLE);
        }
        lastRoomType = mChatRoomTypeVideo;
    }

    @Override
    public void setChatRoomType(int chatRoomType) {

    }


    public void setVideoCoversVisible() {
        if ((((LiveActivity) mContext).isChatRoomTypeVideo()) && mCoverArr != null && mList != null) {
            for (int i = 0; i < mCoverArr.length; i++) {
                if (mCoverArr[i] != null) {
                    LiveVoiceLinkMicBean bean = mList.get(i);
                    if (bean != null) {
                        if (bean.getStatus() == Constants.VOICE_CTRL_CLOSE || bean.getStatus() == Constants.VOICE_CTRL_OPEN) {
                            if (mCoverArr[i].getVisibility() != View.INVISIBLE) {
                                mCoverArr[i].setVisibility(View.INVISIBLE);
                            }
                        } else {
                            if (mCoverArr[i].getVisibility() != View.VISIBLE) {
                                mCoverArr[i].setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }
    }


    public TextureView getFirstPreview() {
        return mTextureViewArr[0];
    }


    public void onUserUpMic(String toUid, String toName, String toAvatar, int position, String frame) {
        if (TextUtils.isEmpty(toUid)) {
            return;
        }
        LiveVoiceLinkMicBean bean = mList.get(position);
        bean.setUid(toUid);
        bean.setUserName(toName);
        bean.setAvatar(toAvatar);
        bean.setStatus(Constants.VOICE_CTRL_OPEN);
        bean.setFaceIndex(-1);
        bean.setUserStream(null);
        bean.setFrame(frame);
        bean.setVotes("0");
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(position);
        }
        if (mHandler != null) {
            mHandler.removeMessages(position);
        }
        setVideoCoversVisible();
    }


    /**
     * 用户下麦
     *
     * @param uid 下麦人员的uid
     */
    public void onUserDownMic(String uid) {
        onUserDownMic(getUserPosition(uid));
    }

    /**
     * 用户下麦
     *
     * @param position 下麦人员的position
     */
    public void onUserDownMic(int position) {
        // Constants.currentVoiceListSize--;
        if (position >= 0 && position < mUserCount) {
            LiveVoiceLinkMicBean bean = mList.get(position);
            bean.setUid(null);
            bean.setUserName(null);
            bean.setAvatar(null);
            bean.setStatus(Constants.VOICE_CTRL_EMPTY);
            bean.setFaceIndex(-1);
            bean.setUserStream(null);
            bean.speak = false;
            bean.cam_status = 1;
            bean.setVotes(null);
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position);
            }
            if (mHandler != null) {
                mHandler.removeMessages(position);
            }
            setVideoCoversVisible();
            if(speakers.size()>position) {
                speakers.get(position).setVisibility(View.GONE);
            }
        }
    }


    /**
     * 语音聊天室--主播控制麦位 闭麦开麦禁麦等
     *
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    public void onControlMicPosition(int position, int status) {
        LiveVoiceLinkMicBean bean = mList.get(position);
        bean.setStatus(status);
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(position, Constants.PAYLOAD);
        }
    }

    /**
     * 语音聊天室--收到上麦观众发送表情的消息
     *
     * @param uid       上麦观众的uid
     * @param faceIndex 表情标识
     */
    public void onVoiceRoomFace(String uid, int faceIndex) {
        int position = getUserPosition(uid);
        if (position >= 0 && position < mUserCount) {
            LiveVoiceLinkMicBean bean = mList.get(position);
            bean.setFaceIndex(faceIndex);
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position, Constants.VOICE_FACE);
            }
            if (mHandler == null) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int pos = msg.what;
                        LiveVoiceLinkMicBean bean0 = mList.get(pos);
                        bean0.setFaceIndex(-1);
                        if (mAdapter != null) {
                            mAdapter.notifyItemChanged(pos, Constants.VOICE_FACE);
                        }
                    }
                };
            } else {
                mHandler.removeMessages(position);
            }
            mHandler.sendEmptyMessageDelayed(position, 5000);
        }
    }

    /**
     * 设置静音
     */
    public void setPushMute(boolean pushMute) {
        if (mPushMute != pushMute) {
            mPushMute = pushMute;
            setMute(pushMute);
        }
    }

    @Override
    public void onCamChange(String uid, int type) {
        int position = getUserPosition(uid);
        if (position != -1) {
            mList.get(position).cam_status = type;
            mAdapter.notifyItemChanged(position, "cam_change");
        }
    }

    @Override
    public void switchMyCam(int type) {
        if (mEngine != null) {
            if (type == 1) {
                // Start the camera and preview
                mEngine.enableLocalVideo(true);  // Enable video capture
                mEngine.startPreview();          // Start local video preview
            } else {
                // Stop the camera
                mEngine.enableLocalVideo(false); // Disable video capture
                mEngine.stopPreview();           // Stop local video preview
            }
        }
    }

    /**
     * 设置推流静音
     */
    private void setMute(boolean pushMute) {
        if (mEngine != null) {
            mEngine.muteLocalAudioStream(pushMute);
        }
    }


    /**
     * 开始推流
     */
    public void startPush(int myCamStatus, int micStatus, String pushUrl, LivePushListener pushListener) {
        mLivePushListener = pushListener;
        mPushSucceed = false;
        if (mEngine != null) {
            if (mChatRoomTypeVideo) {
                TextureView textureView = getPlayView(CommonAppConfig.getInstance().getUid());
                if (textureView != null) {
                    VideoCanvas videoCanvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, mUid);
                    videoCanvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
                    mEngine.setupLocalVideo(videoCanvas);
                    mEngine.startPreview();
                }
            }
            mEngine.setClientRole(CLIENT_ROLE_BROADCASTER);
            mPushMute = false;
            setMute(false);
        }
    }


    /**
     * 停止推流
     */
    public void stopPush() {
        if (mEngine != null) {
            mEngine.setClientRole(CLIENT_ROLE_AUDIENCE);
            if (mChatRoomTypeVideo) {
                mEngine.stopPreview();
                mEngine.setupLocalVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, mUid));
            }
        }
    }

    /**
     * 语音聊天室--观众下麦
     *
     * @param uid 下麦观众的uid
     */
    @Override
    public void stopPlay(String uid) {
        if (mEngine != null) {
            mEngine.setupRemoteVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, Integer.parseInt(uid)));
        }
    }

    @Override
    public void stopPlay(int position) {

    }

    /**
     * 语音聊天室--观众上麦
     *
     * @param uid 上麦观众的uid
     */
    public void playAccStream(String uid, String pull, String userStream) {
        /*if (mEngine != null) {
            TextureView textureView = getPlayView(uid);
            if (textureView != null) {
                VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, Integer.parseInt(uid));
                mEngine.setupRemoteVideo(remote);
            }
        }*/
    }


    /**
     * 获取用户在麦上的位置
     */
    public int getUserPosition(String uid) {
        if (!TextUtils.isEmpty(uid)) {
            for (int i = 0; i < mUserCount; i++) {
                LiveVoiceLinkMicBean bean = mList.get(i);
                if (uid.equals(bean.getUid())) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * 获取用户
     */
    public LiveVoiceLinkMicBean getUserBean(int position) {
        if (position >= 0 && position < mUserCount) {
            return mList.get(position);
        }
        return null;
    }


    /**
     * 获取用户
     */
    public LiveVoiceLinkMicBean getUserBean(String toUid) {
        return getUserBean(getUserPosition(toUid));
    }


    /**
     * 主播混流时候获取上麦用户的Stream
     */
    public List<LiveVoiceMixUserBean> getUserStreamForMix() {
        return null;
    }

    /**
     * 显示房间用户数据
     */
    public void showUserList(JSONArray arr) {
        if(arr!=null&& !arr.isEmpty()) {
            for (int i = 0; i < mUserCount; i++) {
                LiveVoiceLinkMicBean bean = mList.get(i);
                JSONObject obj = arr.getJSONObject(i);
                bean.setUid(obj.getString("id"));
                bean.setUserName(obj.getString("user_nickname"));
                bean.setAvatar(obj.getString("avatar"));
                bean.setStatus(obj.getIntValue("mic_status"));
                bean.cam_status = (obj.getIntValue("cam_status"));
                bean.setVotes(obj.getString("votes"));
                JSONObject object = obj.getJSONObject("frame");
                if (object != null) {
                    bean.setFrame(object.getString("thumb"));
                } else {
                    bean.setFrame(null);
                }
            }
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            setVideoCoversVisible();
        }
    }


    public List<LiveVoiceGiftBean> getVoiceGiftUserList() {
        List<LiveVoiceGiftBean> list = null;
        int startIndex = ((LiveActivity) mContext).isChatRoomTypeVideo() ? 1 : 0;
        for (int i = startIndex; i < mUserCount; i++) {
            LiveVoiceLinkMicBean bean = mList.get(i);
            if (!bean.isEmpty()) {
                LiveVoiceGiftBean giftUserBean = new LiveVoiceGiftBean();
                giftUserBean.setUid(bean.getUid());
                giftUserBean.setAvatar(bean.getAvatar());
                giftUserBean.setType(i);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(giftUserBean);
            }
        }
        return list;
    }


    @Override
    public void release() {
        mLivePushListener = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (!mIsAnchor) {
            if (mEngine != null) {
                if (mChatRoomTypeVideo) {
                    mEngine.stopPreview();
                }
                if (!LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
                    mEngine.leaveChannel();
                    if (mHandler != null) {
                        mHandler.post(RtcEngine::destroy);
                    }
                }
            }
            mEngine = null;
        }
        mHandler = null;
        super.release();
    }

    @Override
    public void onPause() {
        if (!mPushMute) {
            setMute(true);
        }
        if (mEngine != null) {
            mEngine.muteAllRemoteAudioStreams(true);
        }
        mPaused = true;
    }

    @Override
    public void onResume() {
        if (mPaused) {
            if (!mPushMute) {
                setMute(false);
            }
        }
        if (mEngine != null) {
            mEngine.muteAllRemoteAudioStreams(false);
        }
        mPaused = false;
    }

    private TextureView getPlayView(String uid) {
        int index = getUserPosition(uid);
        if (index >= 0 && index < mTextureViewArr.length) {
            return mTextureViewArr[index];
        }
        return null;
    }

    public void leaveChannel() {
        if (!mIsAnchor && LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
            return;
        }
        if (mEngine != null) {
            mEngine.leaveChannel();
            if (mChatRoomTypeVideo) {
                mEngine.stopPreview();
            }
        }
    }

    public void audienceJoinChannel(String agoraToken, String channelId, int liveUid) {
        if (!mIsAnchor && LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
            return;
        }
        mLiveUid = liveUid;
        if (mEngine != null) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
            option.clientRoleType = CLIENT_ROLE_AUDIENCE;
            option.autoSubscribeAudio = true;
            option.publishMicrophoneTrack = true;
            if (mChatRoomTypeVideo) {
                option.autoSubscribeVideo = true;
                option.publishCameraTrack = true;
            }
            mEngine.joinChannel(agoraToken, channelId, mUid, option);
        }
    }

    public void startPreview(TextureView textureView) {
        if (mIsAnchor && mEngine != null) {
            VideoCanvas videoCanvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, mUid);
            videoCanvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
            mEngine.setupLocalVideo(videoCanvas);
            mEngine.startPreview();
        }
    }


    public void anchorJoinChannel(String agoraToken, String channelId) {
        if (mIsAnchor && mEngine != null) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
            option.autoSubscribeAudio = true;
            option.publishMicrophoneTrack = true;
            if (mChatRoomTypeVideo) {
                option.autoSubscribeVideo = true;
                option.publishCameraTrack = true;
            }
            mEngine.joinChannel(agoraToken, channelId, mUid, option);
        }
    }

    private boolean isGuestsReuqestEnabled=true;
    public void isRoomRequestEnabled(boolean b) {
        this.isGuestsReuqestEnabled=b;
    }
    public boolean startBgm(String path) {
        if (mIsAnchor && mEngine != null) {
            int res = mEngine.startAudioMixing(path, false, 1, 0);
            if (res == 0) {
                //该方法调节混音音乐文件在本端和远端的播放音量大小。取值范围为 [0,100]，100 （默认值）为原始音量。
                mEngine.adjustAudioMixingVolume(80);
                return true;
            }
        }
        return false;
    }

    public void stopBgm() {
        if (mIsAnchor && mEngine != null) {
            mEngine.stopAudioMixing();
        }
    }

    public void anchorRelease() {
        if (mIsAnchor && mEngine != null) {
            mEngine.leaveChannel();
            if (mChatRoomTypeVideo) {
                mEngine.stopPreview();
            }
            if (mHandler != null) {
                mHandler.post(RtcEngine::destroy);
            }
            mEngine = null;
        }
    }

    public void setLivePushListener(LivePushListener livePushListener) {
        mLivePushListener = livePushListener;
    }

    public void setLocalMuted(boolean b) {
        int position = getUserPosition(String.valueOf(mLiveUid));
        if (position < speakers.size() && position != -1) {
            speakers.get(position).setVisibility(View.GONE);
        }
        if (position != -1) {
            ((LiveVoiceLinkMicBean) mAdapter.getList().get(position)).speak = false;
            ((LiveVoiceLinkMicBean) mAdapter.getList().get(position)).setStatus(b ? -1 : 1);
            if (mAdapter instanceof LiveVoiceLinkMicAdapter) {
                mAdapter.notifyItemChanged(position, "speak");
            } else {
                mAdapter.notifyItemChanged(position);
            }
        }
    }

    public void onChangeRoomBack(ChangeRoomBackBean bean) {
        loadSvga(mContext, bean.getThumb(), bgImage);
    }

    public void onChangeRoomBack(String imgUrl) {
        loadSvga(mContext, imgUrl, bgImage);
    }

    public void handleMicListVotesChanged(Map<String, Integer> map) {
        for (int i = 0; i < mList.size(); i++) {

            if(map.containsKey(mList.get(i).getUid())) {
                mList.get(i).setVotes(String.valueOf(map.get(mList.get(i).getUid())));
                if (mAdapter != null) {
                    mAdapter.notifyItemChanged(i,"room_votes");
                }
            }
        }
    }
}
