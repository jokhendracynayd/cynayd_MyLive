package com.livestreaming.live.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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

public class LiveAudienceViewHolder extends AbsLiveViewHolder {

    private String mLiveUid;
    private String mStream;
    private View mBtnGoods;
    private View mGoodsIcon;
    private ScaleAnimation mAnimation;
    private View mBtnGame;
    private Drawable mMicUp;
    private Drawable mMicDown;
    private ImageView mBtnJoin;

    public LiveAudienceViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_audience;
    }

    @Override
    public void init() {
        super.init();
//        findViewById(R.id.btn_close).setOnClickListener(this);

        mMicUp = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_voice_join_1);
        mMicDown = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_voice_join_0);

        mBtnJoin = findViewById(R.id.btn_join);
        mBtnJoin.setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        findViewById(R.id.btn_function).setOnClickListener(this);
        findViewById(R.id.btn_request_guest).setOnClickListener(this);
        View btnRedPack = findViewById(R.id.btn_red_pack);
        ImageView btnGift = findViewById(R.id.btn_gift);
        Glide.with(mContext).load(R.mipmap.icon_live_gift).dontAnimate().into(btnGift);
        View btnFirstCharge = findViewById(R.id.btn_first_charge);
        mBtnGame = findViewById(R.id.btn_game);
        mBtnGame.setOnClickListener(this);
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            btnRedPack.setVisibility(View.GONE);
            btnGift.setVisibility(View.GONE);
            btnFirstCharge.setVisibility(View.GONE);
            findViewById(R.id.btn_link_mic).setVisibility(View.INVISIBLE);
        } else {
            btnRedPack.setOnClickListener(this);
            btnGift.setOnClickListener(this);
            btnFirstCharge.setOnClickListener(this);
        }
        mBtnGoods = findViewById(R.id.btn_goods);
        mBtnGoods.setOnClickListener(this);
        mGoodsIcon = findViewById(R.id.goods_icon);
    }

    public void setLiveInfo(String liveUid, String stream) {
        mLiveUid = liveUid;
        mStream = stream;
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        super.onClick(v);
        int i = v.getId();
        /*if (i == R.id.btn_close) {
            close();

        } else*/

        if (i == R.id.btn_share) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            openShareWindow();

        } else if (i == R.id.btn_red_pack) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openRedPackSendWindow();

        } else if (i == R.id.btn_gift) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openGiftWindow();

        } else if (i == R.id.btn_goods) {
            ((LiveAudienceActivity) mContext).openGoodsWindow();
        } else if (i == R.id.btn_first_charge) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).openFirstCharge();
        } else if (i == R.id.btn_function) {
            ((LiveAudienceActivity) mContext).showFunctionDialog();
        } else if (i == R.id.btn_game) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).openGame(mBtnGame);
        }else if (i == R.id.btn_request_guest) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).requestLinkMic();
        }else if(i==R.id.btn_join){
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveAudienceActivity) mContext).requestLinkMic();
        }
    }

    /**
     * 退出直播间
     */
    private void close() {
        ((LiveAudienceActivity) mContext).onBackPressed();
    }


    /**
     * 打开分享窗口
     */
    private void openShareWindow() {
        ((LiveActivity) mContext).openShareWindow();
    }

    /**
     * 动画停止
     */
    public void clearAnim() {
        if (mGoodsIcon != null) {
            mGoodsIcon.clearAnimation();
        }
    }

    public void setShopOpen(boolean isOpen) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            return;
        }
        if (isOpen) {
            if (mBtnGoods != null && mBtnGoods.getVisibility() != View.VISIBLE) {
                mBtnGoods.setVisibility(View.VISIBLE);
            }
            if (mGoodsIcon != null) {
                if (mAnimation == null) {
                    ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0.8f, 1, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation.setDuration(500);
                    scaleAnimation.setRepeatCount(-1);
                    scaleAnimation.setRepeatMode(Animation.REVERSE);
                    mAnimation = scaleAnimation;
                }
                mGoodsIcon.startAnimation(mAnimation);
            }
        } else {
            if (mGoodsIcon != null) {
                mGoodsIcon.clearAnimation();
            }
            if (mBtnGoods != null && mBtnGoods.getVisibility() != View.GONE) {
                mBtnGoods.setVisibility(View.GONE);
            }
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

    public void changeLinkMicIcon(boolean b) {
        ((ImageView)findViewById(R.id.btn_join)).setImageDrawable(b?mMicDown:mMicUp);
    }
}
