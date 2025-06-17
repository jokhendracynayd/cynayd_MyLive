package com.livestreaming.live.views;


import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_DISABLED;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;

import android.content.Context;
import android.view.TextureView;
import android.view.ViewGroup;

import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.ChangeRoomBackBean;
import com.livestreaming.live.bean.LiveConfigBean;

import io.agora.rtc2.Constants;

/**
 * Created by cxf on 2018/10/7.
 * 声网直播推流 语音聊天室
 */

public class LiveChatRoomPushAgoraViewHolder extends AbsLiveChatRoomPushViewHolder {

    private ViewGroup mContainer;
    //    private boolean mPaused;
    private boolean mPlayBgm;
    private LiveChatRoomLinkMicAgoraViewHolder mLiveChatRoomLinkMicAgoraVh;
    private boolean mMirror = true;
    private boolean mRealMirror = true;
    private boolean isMicOpen = false;

    public LiveChatRoomPushAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public LiveChatRoomPushAgoraViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    public void setLiveChatRoomLinkMicAgoraVh(LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh) {
        mLiveChatRoomLinkMicAgoraVh = liveChatRoomLinkMicAgoraVh;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_push_tx_voice;
    }

    @Override
    public void init() {
        mContainer = findViewById(R.id.voice_push_container);
    }

    @Override
    public void startMicrophone() {

    }

    @Override
    public void startPreview(LiveConfigBean liveConfigBean, TextureView textureView) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.startPreview(textureView);
        }
    }

    /**
     * 开始声网推流
     */
    @Override
    public void startPushAgora(String token, String channelId) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.anchorJoinChannel(token, channelId);
        }
        startCountDown();
    }

    @Override
    public void onPause() {
    }


    @Override
    public void onResume() {
        mLiveChatRoomLinkMicAgoraVh.mEngine.muteLocalAudioStream(isMicOpen);
    }

    @Override
    public ViewGroup getContainer() {
        return mContainer;
    }

    @Override
    public void changeRoomBack(ChangeRoomBackBean bean) {
        mLiveChatRoomLinkMicAgoraVh.onChangeRoomBack(bean);
    }

    @Override
    public void changeRoomBack(String bean) {
        mLiveChatRoomLinkMicAgoraVh.onChangeRoomBack(bean);
    }


    @Override
    public void changeToLeft() {
    }

    @Override
    public void changeToBig() {
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
        if (mLiveChatRoomLinkMicAgoraVh.mEngine != null) {
            mLiveChatRoomLinkMicAgoraVh.mEngine.setLocalRenderMode(RENDER_MODE_HIDDEN, mirror ? VIDEO_MIRROR_MODE_ENABLED : VIDEO_MIRROR_MODE_DISABLED);
        }
    }

    /**
     * 切换镜头
     */
    @Override
    public void toggleCamera() {
        if (mLiveChatRoomLinkMicAgoraVh.mEngine != null) {
            // Switch camera
            mCameraFront = !mCameraFront;
            mLiveChatRoomLinkMicAgoraVh.mEngine.switchCamera();  // This switches between front and rear cameras

            // Enable or disable the camera torch (flash) if available
            if (mFlashOpen) {
                mLiveChatRoomLinkMicAgoraVh.mEngine.setCameraTorchOn(false);  // Disable flash
                mFlashOpen = false;
            } else {
                mLiveChatRoomLinkMicAgoraVh.mEngine.setCameraTorchOn(true);   // Enable flash
                mFlashOpen = true;
            }

            // Handle encoder mirroring based on camera position
            if (!mCameraFront) {
                mLiveChatRoomLinkMicAgoraVh.mEngine.setLocalRenderMode(Constants.RENDER_MODE_HIDDEN, Constants.VIDEO_MIRROR_MODE_DISABLED);  // Disable mirroring for rear camera
            } else {
                int mirrorMode = mMirror ? Constants.VIDEO_MIRROR_MODE_ENABLED : Constants.VIDEO_MIRROR_MODE_DISABLED;
                mLiveChatRoomLinkMicAgoraVh.mEngine.setLocalRenderMode(Constants.RENDER_MODE_HIDDEN, mirrorMode);  // Set mirror based on the value of mMirror for front camera
            }
        }
    }

    @Override
    public void switchCamera(boolean isOn) {
        if (!isOn) {
            // Stop the camera
            mLiveChatRoomLinkMicAgoraVh.mEngine.enableLocalVideo(false);  // Disable video capture
            mLiveChatRoomLinkMicAgoraVh.mEngine.stopPreview();            // Stop the camera preview
        } else {
            // Start the camera
            mLiveChatRoomLinkMicAgoraVh.mEngine.enableLocalVideo(true);   // Enable video capture
            mLiveChatRoomLinkMicAgoraVh.mEngine.startPreview();           // Start the camera preview
        }
    }

    /**
     * 打开关闭闪光灯
     */
    @Override
    public void toggleFlash() {
    }

    /**
     * 开始推流
     *
     * @param pushUrl 推流地址
     */
    @Override
    public void startPush(String pushUrl) {
    }


    @Override
    public void startBgm(String path) {
        mPlayBgm = mLiveChatRoomLinkMicAgoraVh.startBgm(path);
    }

    @Override
    public void pauseBgm() {
//        if (mPlayBgm&&mLivePusher != null) {
//            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
//            if (audioEffectManager != null) {
//                audioEffectManager.pausePlayMusic(1);
//            }
//        }
    }

    @Override
    public void resumeBgm() {
//        if (mPlayBgm&&mLivePusher != null) {
//            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
//            if (audioEffectManager != null) {
//                audioEffectManager.resumePlayMusic(1);
//            }
//        }
    }

    @Override
    public void stopBgm() {
        if (mPlayBgm && mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.stopBgm();
        }
        mPlayBgm = false;
    }

    @Override
    public void controlMic(boolean b) {
        isMicOpen = b;
        mLiveChatRoomLinkMicAgoraVh.mEngine.muteLocalAudioStream(b);
        mLiveChatRoomLinkMicAgoraVh.setLocalMuted(b);
    }

    @Override
    public void switchLiveCam(boolean isCamOpen,String uid,String thumb) {

    }

    @Override
    protected void onCameraRestart() {
    }

    @Override
    public void release() {
        super.release();
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.anchorRelease();
        }
        mLiveChatRoomLinkMicAgoraVh = null;
    }


    /**
     * 设置静音
     */
    @Override
    public void setPushMute(boolean pushMute) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.setPushMute(pushMute);
        }
    }


}
