package com.livestreaming.im.bean;

import android.os.Parcelable;

import com.livestreaming.common.bean.UserBean;

/**
 * Created by cxf on 2017/8/14.
 * IM 聊天用户 实体类
 */

public class ImNotificationBean extends UserBean implements Parcelable {

    int id;
    int to_uid;

    public int getUId() {
        return id;
    }

    public void setUId(int id) {
        this.id = id;
    }

    public int getTo_uid() {
        return to_uid;
    }

    public void setTo_uid(int to_uid) {
        this.to_uid = to_uid;
    }

    public int getFrom_uid() {
        return from_uid;
    }

    public void setFrom_uid(int from_uid) {
        this.from_uid = from_uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    int from_uid;
    String title;
    String body;
    int type;
    int seen;
    String created_at;


    public ImNotificationBean() {

    }
}
