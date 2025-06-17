package com.livestreaming.live.event;

import com.livestreaming.live.bean.LiveAudienceFloatWindowData;
import com.livestreaming.live.bean.LiveBean;

public class LiveAudienceChatRoomExitEvent {

    private final LiveBean mLiveBean;
    private final LiveAudienceFloatWindowData mLiveAudienceFloatWindowData;

    public LiveAudienceChatRoomExitEvent(LiveBean liveBean, LiveAudienceFloatWindowData liveAudienceFloatWindowData) {
        mLiveBean = liveBean;
        mLiveAudienceFloatWindowData = liveAudienceFloatWindowData;
    }

    public LiveBean getLiveBean() {
        return mLiveBean;
    }

    public LiveAudienceFloatWindowData getLiveAudienceAgoraData() {
        return mLiveAudienceFloatWindowData;
    }
}
