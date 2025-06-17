package com.livestreaming.live.views;

import android.content.Context;
import android.view.ViewGroup;

import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.live.interfaces.ILiveLinkMicViewHolder;

/**
 * Created by cxf on 2018/10/25.
 */

public abstract class LiveRoomPlayViewHolder extends AbsViewHolder implements ILiveLinkMicViewHolder {

    public LiveRoomPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public abstract void play(String url);

    public abstract void stopPlay();

    public abstract void resumePlay();

    public abstract void pausePlay();

    public abstract void hideCover();

    public abstract void release();

    public abstract void setCover(String coverUrl);

    public void playAgora(String url,boolean isVideo,String agoraToken, String channelId,int liveUid) {

    }


    public void toggleLinkMicPushAgora(boolean isPush, String uid) {

    }

    /**
     * 设置主播连麦模式
     *
     * @param anchorLinkMic
     */
    public void setAnchorLinkMic(boolean anchorLinkMic, int delayTime) {

    }

    public void changeSize(boolean landscape) {

    }

    public void clearOldUsersOnSwitch() {
    }
}
