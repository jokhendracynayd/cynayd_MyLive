package com.livestreaming.live.bean;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class LiveGestBean {
    String stream;
    int cam_type;
    int mic_type;
    String avatar;
    int user_id;
    int position;
    String  guest_name;
    private int income=0;

    public void setIncome(int income) {
        this.income = income;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    private String  frame;
    private int type=-2;

    private boolean mChecked;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private int mTextRes;

    public String getStream() {
        return stream;
    }


    public int getTextRes() {
        return mTextRes;
    }

    public void setTextRes(int textRes) {
        mTextRes = textRes;
    }
    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }


    public Drawable getCheckedDrawable() {
        return mCheckedDrawable;
    }

    public Drawable getUnCheckedDrawable() {
        return mUnCheckedDrawable;
    }


    public void setDrawable(Context context, int checkedRes, int unCheckedRes) {
        mCheckedDrawable = ContextCompat.getDrawable(context, checkedRes);
        mUnCheckedDrawable = ContextCompat.getDrawable(context, unCheckedRes);
    }
    public void setStream(String stream) {
        this.stream = stream;
    }

    public int getCam_type() {
        return cam_type;
    }

    public void setCam_type(int cam_type) {
        this.cam_type = cam_type;
    }

    public int getMic_type() {
        return mic_type;
    }

    public void setMic_type(int mic_type) {
        this.mic_type = mic_type;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }



    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return guest_name;
    }

    public void setUserName(String name) {
        this.guest_name = name;
    }


    public int getType() {
        return type;
    }
    public void setType(int i){
        type=i;
    }

    public int getIncome() {
        return income;
    }
}
