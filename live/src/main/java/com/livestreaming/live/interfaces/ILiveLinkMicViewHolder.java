package com.livestreaming.live.interfaces;

import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by cxf on 2018/12/14.
 */

public interface ILiveLinkMicViewHolder {

    LinearLayout getGestConitaner();

    ViewGroup getRightContainer();

    ViewGroup getPkContainer();

    void changeToLeft();

    void changeToBig();
}
