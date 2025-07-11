package com.livestreaming.beauty.interfaces;

public interface IBeautyEffectListener {

    void onMeiYanChanged(int meiBai, boolean meiBaiChanged, int moPi, boolean moPiChanged, int hongRun, boolean hongRunChanged);

    void onFilterChanged(int filterName);

    void onAdvancedBeautyChanged(String key, int value);

    boolean isUseMhFilter();

    boolean isTieZhiEnable();
}
