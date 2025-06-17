package com.livestreaming.common.banner.indicator;

import android.view.View;

import androidx.annotation.NonNull;

import com.livestreaming.common.banner.config.IndicatorConfig;
import com.livestreaming.common.banner.listener.OnPageChangeListener;

public interface Indicator extends OnPageChangeListener {
    @NonNull
    View getIndicatorView();

    IndicatorConfig getIndicatorConfig();

    void onPageChanged(int count, int currentPosition);

}
