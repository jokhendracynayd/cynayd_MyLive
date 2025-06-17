package com.livestreaming.live.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.livestreaming.common.bean.UserBean;

/**
 * Created by cxf on 2018/10/27.
 * 直播间用户列表实体类
 */

public class LiveUserGiftBean extends UserBean {

    private String contribution;
    private int guardType;
    private int age;

    public String getContribution() {
        return contribution;
    }

    public void setContribution(String contribution) {
        this.contribution = contribution;
    }

    /**
     * 是否送过礼物
     */
    public boolean hasContribution() {
        return !TextUtils.isEmpty(this.contribution) && !"0".equals(this.contribution);
    }

    @JSONField(name = "guard_type")
    public int getGuardType() {
        return guardType;
    }

    @JSONField(name = "guard_type")
    public void setGuardType(int guardType) {
        this.guardType = guardType;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
