package com.livestreaming.live.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.bumptech.glide.Glide;

/**
 * Created by cxf on 2018/10/9.
 * 观众直播间逻辑
 */

public class LiveVoiceAudienceViewHolder extends AbsLiveViewHolder {

    public boolean isRoomRequestEnabled=true;
    private ImageView mBtnFunction;
    //    private Drawable mDrawable0;
//    private Drawable mDrawable1;
    private ImageView mBtnJoin;
    private ImageView mBtnMic;
    private ImageView btn_mic_apply;
    private ImageView btn_mic_control;
    private ImageView btn_games;
    private Drawable mMicUp;//上麦图标
    private Drawable mMicDown;//下麦图标
    private View mGroupMic;
    private Drawable mDrawableMicOpen;//开麦
    private Drawable mDrawableMicClose;//关麦
    private boolean mIsUpMic;
    private View mBtnGame;
    private boolean isCamOpen = true;
    private ImageView btnCam;

    public LiveVoiceAudienceViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_audience_voice;
    }

    @Override
    public void init() {
        super.init();
//        mDrawable0 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_func_0);
//        mDrawable1 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_func_1);
        mMicUp = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_voice_join_1);
        mMicDown = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_voice_join_0);
        mDrawableMicOpen = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_mic_open);
        mDrawableMicClose = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_mic_close);
        mBtnFunction = (ImageView) findViewById(R.id.btn_function);
//        mBtnFunction.setImageDrawable(mDrawable0);
        mBtnFunction.setOnClickListener(this);
        mBtnJoin = findViewById(R.id.btn_join);
        mBtnMic = findViewById(R.id.btn_mic);
        btn_mic_apply = findViewById(R.id.btn_mic_apply);
        btn_mic_apply.setOnClickListener(this);
        btn_mic_control = findViewById(R.id.btn_mic_control);
        btn_mic_control.setOnClickListener(this);
        btn_games = findViewById(R.id.btn_games);
       Glide.with(mContext).load(R.mipmap.icon_live_func_game).into(btn_games);
        mBtnMic.setOnClickListener(this);
        btn_games.setOnClickListener(this);
        btnCam = findViewById(R.id.btn_switch_cam);
        btnCam.setOnClickListener(this);
        mGroupMic = findViewById(R.id.group_mic);
        findViewById(R.id.btn_face).setOnClickListener(this);

        ImageView btnGift = findViewById(R.id.btn_gift);
        Glide.with(mContext).load(R.mipmap.icon_live_gift).into(btnGift);
        View btnFirstCharge = findViewById(R.id.btn_first_charge);
        mBtnGame = findViewById(R.id.btn_game);
        mBtnGame.setOnClickListener(this);
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            btnGift.setVisibility(View.GONE);
            btnFirstCharge.setVisibility(View.GONE);
            mBtnJoin.setVisibility(View.GONE);
        } else {
            btnGift.setOnClickListener(this);
            btnFirstCharge.setOnClickListener(this);
            mBtnJoin.setOnClickListener(this);
        }


    }


    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.btn_join) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).clickVoiceUpMic();
        } else if (i == R.id.btn_mic) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).changeVoiceMicOpen(CommonAppConfig.getInstance().getUid());
        } else if (i == R.id.btn_face) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).openVoiceRoomFace();
        } else if (i == R.id.btn_switch_cam) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            isCamOpen = !isCamOpen;
            ((LiveAudienceActivity) mContext).onVoiceRoomUserCam(CommonAppConfig.getInstance().getUid(), isCamOpen ? 1 : 2, true);

        } else if (i == R.id.btn_gift) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openGiftWindow();

        } else if (i == R.id.btn_function) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            showFunctionDialog();
        } else if (i == R.id.btn_first_charge) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).openFirstCharge();
        } else if (i == R.id.btn_game) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).openGame(mBtnGame);
        } else if (i == R.id.btn_mic_apply) {
            ((LiveActivity) mContext).voiceApplyUpMicAdminControl();
        } else if (i == R.id.btn_mic_control) {
            ((LiveActivity) mContext).adminControlMic();
        }else if(i==R.id.btn_games) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).showGamesDialog();
        }
    }

    /**
     * 显示功能弹窗
     */
    private void showFunctionDialog() {
//        if (mBtnFunction != null) {
//            mBtnFunction.setImageDrawable(mDrawable1);
//        }
        ((LiveAudienceActivity) mContext).showFunctionDialogVoice(mIsUpMic);
    }

    /**
     * 设置功能按钮变暗
     */
    public void setBtnFunctionDark() {
//        if (mBtnFunction != null) {
//            mBtnFunction.setImageDrawable(mDrawable0);
//        }
    }

    public void setAdminManagerFunctionsVisibilty(boolean isVisible) {
        btn_mic_apply.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        btn_mic_control.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 改变上麦下麦状态
     */
    public void changeMicUp(boolean isUpMic) {
        mIsUpMic = isUpMic;
        setVoiceMicClose(false);
        if (mBtnJoin != null) {
            mBtnJoin.setImageDrawable(isUpMic ? mMicDown : mMicUp);
        }
        if (mGroupMic != null) {
            if (isUpMic) {
                if (mGroupMic.getVisibility() != View.VISIBLE) {
                    mGroupMic.setVisibility(View.VISIBLE);
                }
                if (((LiveAudienceActivity) mContext).isChatRoomTypeVideo()) {
                    setSwitchCamToVisible(true);
                }
                setMicBtnToVisible(true);
            } else {
                if (((LiveAudienceActivity) mContext).isChatRoomTypeVideo()) {
                    setSwitchCamToVisible(false);
                }
                if (mGroupMic.getVisibility() != View.GONE) {
                    mGroupMic.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 设置是否被关麦
     *
     * @param micClose
     */
    public void setVoiceMicClose(boolean micClose) {
        if (mBtnMic != null) {
            mBtnMic.setImageDrawable(micClose ? mDrawableMicClose : mDrawableMicOpen);
        }
    }

    /**
     * 隐藏/显示 游戏按钮
     */
    public void setBtnGameVisible(boolean visible) {
        if (mBtnGame != null) {
            if (visible) {
                if (!CommonAppConfig.getInstance().isTeenagerType()) {
                    if (mBtnGame.getVisibility() != View.VISIBLE) {
                        mBtnGame.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (mBtnGame.getVisibility() != View.GONE) {
                    mBtnGame.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setMicBtnToVisible(boolean b) {
        mBtnMic.setVisibility(b ? View.VISIBLE : View.GONE);
    }


    public void setSwitchCamToVisible(boolean b) {
        if (b) {
            if (btnCam.getVisibility() != View.VISIBLE) {
                btnCam.setVisibility(View.VISIBLE);
            }
        } else {
            if (btnCam.getVisibility() != View.GONE) {
                btnCam.setVisibility(View.GONE);
            }
        }
        //((LiveAudienceActivity)mContext).updateLayout();
    }

}
