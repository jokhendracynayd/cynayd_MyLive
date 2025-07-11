package com.livestreaming.mall.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.zyyoona7.wheel.IWheelEntity;

/**
 * 卖家拒绝退款 的原因
 */
public class RejectRefundBean implements IWheelEntity {

    private String mId;
    private String mName;

    @JSONField(name = "id")
    public String getId() {
        return mId;
    }

    @JSONField(name = "id")
    public void setId(String id) {
        mId = id;
    }

    @JSONField(name = "name")
    public String getName() {
        return mName;
    }

    @JSONField(name = "name")
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getWheelText() {
        return mName;
    }
}
