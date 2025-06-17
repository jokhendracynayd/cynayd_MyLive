package com.livestreaming.live.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.livestreaming.common.Constants;
import com.livestreaming.common.utils.LanguageUtil;

/**
 * Created by cxf on 2019/4/29.
 */

public class LiveLuckGiftWinBean {

    private String mUid;
    private String mUserNiceName;
    private String mAvatar;
    private String mGiftId;
    private String mGiftName;
    private String mGiftNameEn;
    private String mLuckTime;

    @JSONField(name = "uid")
    public String getUid() {
        return mUid;
    }

    @JSONField(name = "uid")
    public void setUid(String uid) {
        mUid = uid;
    }

    @JSONField(name = "uname")
    public String getUserNiceName() {
        return mUserNiceName;
    }

    @JSONField(name = "uname")
    public void setUserNiceName(String userNiceName) {
        mUserNiceName = userNiceName;
    }

    @JSONField(name = "uhead")
    public String getAvatar() {
        return mAvatar;
    }

    @JSONField(name = "uhead")
    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    @JSONField(name = "giftid")
    public String getGiftId() {
        return mGiftId;
    }

    @JSONField(name = "giftid")
    public void setGiftId(String giftId) {
        mGiftId = giftId;
    }

    @JSONField(name = "giftname")
    public String getGiftName() {
        String lang = LanguageUtil.getInstance().getLanguage();
        if (Constants.LANG_EN.equals(lang)) {
            return mGiftNameEn;
        }
        return mGiftName;
    }

    @JSONField(name = "giftname")
    public void setGiftName(String giftName) {
        mGiftName = giftName;
    }

    @JSONField(name = "giftname_en")
    public String getGiftNameEn() {
        return mGiftNameEn;
    }

    @JSONField(name = "giftname_en")
    public void setGiftNameEn(String giftNameEn) {
        mGiftNameEn = giftNameEn;
    }

    @JSONField(name = "lucktimes")
    public String getLuckTime() {
        return mLuckTime;
    }

    @JSONField(name = "lucktimes")
    public void setLuckTime(String luckTime) {
        mLuckTime = luckTime;
    }

}
