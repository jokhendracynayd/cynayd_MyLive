package com.livestreaming.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;


public class LiveVoiceAnchorViewHolder extends AbsLiveViewHolder {

    private ImageView mBtnFunction;
//    private Drawable mDrawable0;
//    private Drawable mDrawable1;

    private ImageView btn_games;
    private View mApplyUpMicTip;
    private boolean isReuqestEnabled = true;


    public LiveVoiceAnchorViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_anchor_voice;
    }

    private boolean isMicOn = true;

    @Override
    public void init() {
        super.init();
//        mDrawable0 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_func_0);
//        mDrawable1 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_func_1);
        mBtnFunction = (ImageView) findViewById(R.id.btn_function);
        isReuqestEnabled = true;
//        mBtnFunction.setImageDrawable(mDrawable0);

        findViewById(R.id.btn_switch_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMicOn = !isMicOn;
                if (!isMicOn) {
                    ((ImageView) findViewById(R.id.btn_switch_mic)).setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.icon_live_mic_close));
                } else {
                    ((ImageView) findViewById(R.id.btn_switch_mic)).setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.icon_live_mic_open));
                }
                ((LiveAnchorActivity) mContext).micOnOff(isMicOn);
            }
        });

        btn_games = findViewById(R.id.btn_games);
        Glide.with(mContext).load(R.mipmap.icon_live_func_game).into(btn_games);
        btn_games.setOnClickListener(this);
        ImageView btnGift = findViewById(R.id.btn_gift);
        Glide.with(mContext).load(R.mipmap.icon_live_gift).into(btnGift);
        mBtnFunction.setOnClickListener(this);
        btnGift.setOnClickListener(this);
        findViewById(R.id.btn_mic_apply).setOnClickListener(this);
        findViewById(R.id.btn_mic_control).setOnClickListener(this);
        findViewById(R.id.btn_switch_cam).setOnClickListener(this);
        mApplyUpMicTip = findViewById(R.id.tip_mic_apply);
        findViewById(R.id.btn_enable_request).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.btn_function) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            showFunctionDialog();
        } else if (i == R.id.btn_gift) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openGiftWindow();
        } else if (i == R.id.btn_mic_apply) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).voiceApplyUpMic();
            setApplyUpMicTipShow(false);
        } else if(i == R.id.btn_games){
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).showGamesDialog();

        }else if (i == R.id.btn_mic_control) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAnchorActivity) mContext).controlMic();
        } else if (i == R.id.btn_switch_cam) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAnchorActivity) mContext).closeOpenCam(false);
        } else if (i == R.id.btn_enable_request) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }

            handleEnableDisableGuestRequest();

        }


    }

    private void handleEnableDisableGuestRequest() {
        isReuqestEnabled = !isReuqestEnabled;

        ((ImageView) findViewById(R.id.btn_enable_request)).setImageResource(isReuqestEnabled ? R.drawable.connection_svgrepo_com : R.drawable.selected_user);
        ((LiveAnchorActivity) mContext).handleEnableDisableGuestRequest(isReuqestEnabled);
        ToastUtil.show(mContext.getString(isReuqestEnabled ? R.string.request_guest_enabled : R.string.request_guest_disabled));
    }


    /**
     * 显示功能弹窗
     */
    private void showFunctionDialog() {
//        if (mBtnFunction != null) {
//            mBtnFunction.setImageDrawable(mDrawable1);
//        }
        ((LiveAnchorActivity) mContext).showFunctionDialog();
    }

    /**
     * 设置功能按钮变暗
     */
    public void setBtnFunctionDark() {
//        if (mBtnFunction != null) {
//            mBtnFunction.setImageDrawable(mDrawable0);
//        }
    }

    /**
     * 显示或隐藏 上麦申请的红点提示
     */
    public void setApplyUpMicTipShow(boolean show) {
        if (mApplyUpMicTip != null) {
            if (show) {
                if (mApplyUpMicTip.getVisibility() != View.VISIBLE) {
                    mApplyUpMicTip.setVisibility(View.VISIBLE);
                }
            } else {
                if (mApplyUpMicTip.getVisibility() == View.VISIBLE) {
                    mApplyUpMicTip.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void changeCamBtnVisibility(boolean b) {
        findViewById(R.id.btn_switch_cam).setVisibility(b ? View.VISIBLE : View.GONE);
    }
}
