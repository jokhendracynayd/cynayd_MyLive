package com.livestreaming.common.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by cxf on 2018/9/25.
 */

public class LiveClassBean {
    protected int id;
    protected String name;
    protected String thumb;
    protected int orderNo;
    private String des;
    private boolean checked;

    public LiveClassBean() {

    }
    public LiveClassBean(int id, String name, boolean checked) {
        this.id = id;
        this.name = name;
        this.checked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    @JSONField(name = "orderno")
    public int getOrderNo() {
        return orderNo;
    }

    @JSONField(name = "orderno")
    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
