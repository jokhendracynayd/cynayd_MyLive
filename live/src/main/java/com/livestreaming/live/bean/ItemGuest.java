package com.livestreaming.live.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ItemGuest {

    private String user_id;
    private String avatar;

    @JSONField(name = "user_id")
    public String getUser_id() {
        return user_id;
    }

    @JSONField(name = "user_id")
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @JSONField(name = "avatar")
    public String getAvatar() {
        return avatar;
    }

    @JSONField(name = "avatar")
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int roomIndex = -1;
}
