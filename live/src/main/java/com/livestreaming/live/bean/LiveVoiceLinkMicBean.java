package com.livestreaming.live.bean;

import android.text.TextUtils;

public class LiveVoiceLinkMicBean {
    private String mUid;
    private String mUserName;
    private String mAvatar;
    private String mFrame;
    private int mStatus;//The status of the wheat position -1 Off wheat; 0 No one; 1 Open wheat; 2 Forbidden wheat;
    private int mFaceIndex = -1;
    private String mUserStream;//上麦观众的流名，主播混流用

    public int cam_status=1;
    public boolean speak=false;

    public LiveVoiceLinkMicBean() {
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getFaceIndex() {
        return mFaceIndex;
    }

    public void setFaceIndex(int faceIndex) {
        mFaceIndex = faceIndex;
    }


    public String getUserStream() {
        return mUserStream;
    }

    public void setUserStream(String userStream) {
        mUserStream = userStream;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(mUid) || "0".equals(mUid);
    }

    public String getFrame() {
        return mFrame;
    }
    public void setFrame(String f) {
         mFrame=f;
    }

    private String votes=null;

    public String getVotes() {
        return votes;
    }

    public void setVotes(String votes) {
        this.votes = votes;
    }
}
