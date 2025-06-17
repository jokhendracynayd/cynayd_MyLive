package com.livestreaming.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.livestreaming.live.R;

/**
 * Created by cxf on 2018/10/25.
 * 连麦播放小窗口  使用声网sdk
 */

public class LiveLinkMicPlayAgoraViewHolder extends AbsLiveLinkMicPlayViewHolder {

//    private static final String TAG = "LiveLinkMicPlayAgoraViewHolder";

    public LiveLinkMicPlayAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_link_mic_play_agora;
    }

    @Override
    public void init() {

    }

    @Override
    public void setOnCloseListener(View.OnClickListener onClickListener) {
        if (onClickListener != null) {
        }
    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(final String url) {

    }

    @Override
    public void release() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }
}
