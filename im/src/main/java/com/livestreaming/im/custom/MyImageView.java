package com.livestreaming.im.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.makeramen.roundedimageview.RoundedImageView;
import com.livestreaming.im.bean.ImMessageBean;

/**
 * Created by cxf on 2018/6/7.
 */

public class MyImageView extends RoundedImageView {

    private ImMessageBean mImMessageBean;

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public ImMessageBean getImMessageBean() {
        return mImMessageBean;
    }

    public void setImMessageBean(ImMessageBean imMessageBean) {
        mImMessageBean = imMessageBean;
    }
}
