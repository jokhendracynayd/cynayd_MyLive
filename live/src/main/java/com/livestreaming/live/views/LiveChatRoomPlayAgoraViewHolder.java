package com.livestreaming.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.livestreaming.common.utils.L;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;

/**
 * Created by cxf on 2018/10/10.
 * 声网播放 语音聊天室
 */

public class LiveChatRoomPlayAgoraViewHolder extends AbsLiveChatRoomPlayViewHolder {

    private static final String TAG = "LiveChatRoomPlayAgoraViewHolder";

    private ViewGroup mContainer;
    private boolean mChatRoomTypeVideo;
    private LiveChatRoomLinkMicAgoraViewHolder mLiveChatRoomLinkMicAgoraVh;

    public LiveChatRoomPlayAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_play_tx_chatroom;
    }

    @Override
    public void init() {
        mContainer = findViewById(R.id.voice_play_container);
        mChatRoomTypeVideo = ((LiveActivity) mContext).isChatRoomTypeVideo();
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LiveAudienceActivity) mContext).light();
            }
        });
    }

    public void setLiveChatRoomLinkMicAgoraVh(LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh) {
        mLiveChatRoomLinkMicAgoraVh = liveChatRoomLinkMicAgoraVh;
    }

    public ViewGroup getContainer() {
        return mContainer;
    }

    @Override
    public void hideCover() {
    }

    @Override
    public void setCover(String coverUrl) {
    }

    /**
     * 暂停播放
     */
    @Override
    public void pausePlay() {

    }

    /**
     * 暂停播放后恢复
     */
    @Override
    public void resumePlay() {

    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(String url) {

    }


    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void playAgora(String url, boolean isVideo, String agoraToken, String channelId, int liveUid) {
        L.e(TAG, "playAgora------channelId----->" + channelId);
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.audienceJoinChannel(agoraToken, channelId, liveUid);
        }
    }


    @Override
    public void stopPlay() {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.leaveChannel();
        }
    }


    @Override
    public void release() {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.release();
        }
        mLiveChatRoomLinkMicAgoraVh = null;
        L.e(TAG, "release------->");
    }

    @Override
    public LinearLayout getGestConitaner() {
        return null;
    }

    @Override
    public ViewGroup getRightContainer() {
        return null;
    }

    @Override
    public ViewGroup getPkContainer() {
        return null;
    }

    @Override
    public void changeToLeft() {

    }

    @Override
    public void changeToBig() {

    }

    @Override
    public void onDestroy() {
        release();
    }

    @Override
    public void onChangeRoomBack(String imgUrl) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            if (imgUrl != null) {
                mLiveChatRoomLinkMicAgoraVh.onChangeRoomBack(imgUrl);
            }
        }
    }

    public void isRoomRequestEnabled(boolean b) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.isRoomRequestEnabled(b);
        }
    }
}
