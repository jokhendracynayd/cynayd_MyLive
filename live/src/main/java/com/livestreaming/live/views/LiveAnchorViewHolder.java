package com.livestreaming.live.views;

import android.app.Dialog;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.http.LiveHttpUtil;

/**
 * Created by cxf on 2018/10/9.
 * 主播直播间逻辑
 */

public class LiveAnchorViewHolder extends AbsLiveViewHolder {

    private ImageView mBtnFunction;
    //private ImageView btn_games;
    private View mBtnGameClose;//关闭游戏的按钮
    private ImageView btn_close_pk;//关闭游戏的按钮
    private View btn_cancel_pk;//主播连麦pk按钮
    //    private Drawable mDrawable0;
//    private Drawable mDrawable1;
//    private Drawable mDrawableLinkMic0;//允许连麦
//    private Drawable mDrawableLinkMic1;//禁止连麦
//    private ImageView mLinkMicIcon;//是否允许连麦的标记
//    private TextView mLinkMicTip;//是否允许连麦的提示
    private boolean mLinkMicEnable;
    private View mBtnShop;
    private View btn_start_pk;


    public LiveAnchorViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_anchor;
    }

    @Override
    public void init() {
        super.init();
//        mDrawable0 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_func_0);
//        mDrawable1 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_func_1);
        mBtnFunction = (ImageView) findViewById(R.id.btn_function);
        btn_close_pk = (ImageView) findViewById(R.id.btn_close_pk);
        // btn_games = (ImageView) findViewById(R.id.btn_games);
//        mBtnFunction.setImageDrawable(mDrawable0);
        mBtnFunction.setOnClickListener(this);
        btn_close_pk.setOnClickListener(this);
        //btn_games.setOnClickListener(this);
        mBtnGameClose = findViewById(R.id.btn_close_game);
        mBtnGameClose.setOnClickListener(this);
//        findViewById(R.id.btn_close).setOnClickListener(this);
        btn_cancel_pk = findViewById(R.id.btn_cancel_pk);
        btn_start_pk = findViewById(R.id.btn_start_pk);

        btn_cancel_pk.setOnClickListener(this);
        btn_start_pk.setOnClickListener(this);
//        mDrawableLinkMic0 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_link_mic);
//        mDrawableLinkMic1 = ContextCompat.getDrawable(mContext, R.mipmap.icon_live_link_mic_1);
//        mLinkMicIcon = (ImageView) findViewById(R.id.link_mic_icon);
//        mLinkMicTip = (TextView) findViewById(R.id.link_mic_tip);
        findViewById(R.id.btn_mic_apply).setOnClickListener(this);
        findViewById(R.id.btn_switch_cam).setOnClickListener(this);
        mBtnShop = findViewById(R.id.btn_shop);
        mBtnShop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.btn_close) {
            close();

        } else if (i == R.id.btn_function) {
            showFunctionDialog();

        } else if (i == R.id.btn_close_game) {
            closeGame();

        } else if (i == R.id.btn_cancel_pk) {
            cancelPk();
        } else if (i == R.id.btn_start_pk) {
            startPkClicked();
        }
//        else if (i == R.id.btn_link_mic) {
//            changeLinkMicEnable();
//        }
        else if (i == R.id.btn_shop) {
            ((LiveAnchorActivity) mContext).openGoodsWindow();
        } else if (i == R.id.btn_switch_cam) {
           // ((LiveAnchorActivity) mContext).closeOpenCam(true);
        } else if (i == R.id.btn_close_pk) {
            setClosePkBtnVisible(false);
            ((LiveAnchorActivity) mContext).onClickCloseAnchorLinkMicBtn();
        } else if (i == R.id.btn_mic_apply) {
            showGuestsRequests();
        }
    }

    private void startPkClicked() {
        ((LiveAnchorActivity) mContext).applyLinkMicPk();
    }

    private void showGuestsRequests() {
        ((LiveAnchorActivity) mContext).showGuestsRequestsDialog();
        showRedDotOnGuestsIcon(false);

    }

    /**
     * 设置游戏按钮是否可见
     */
    public void setGameBtnVisible(boolean show) {
        if (mBtnGameClose != null) {
            if (show) {
                if (mBtnGameClose.getVisibility() != View.VISIBLE) {
                    mBtnGameClose.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnGameClose.getVisibility() == View.VISIBLE) {
                    mBtnGameClose.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setShopBtnVisible(boolean show) {
        if (mBtnShop != null) {
            if (show) {
                if (mBtnShop.getVisibility() != View.VISIBLE) {
                    mBtnShop.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnShop.getVisibility() == View.VISIBLE) {
                    mBtnShop.setVisibility(View.GONE);
                }
            }
        }
    }


    /**
     * 关闭游戏
     */
    private void closeGame() {
        ((LiveAnchorActivity) mContext).closeGame();
    }

    /**
     * 关闭直播
     */
    private void close() {
        ((LiveAnchorActivity) mContext).closeLive();
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

    //    /**
//     * 设置连麦pk按钮是否可见
//     */
    public void setPkCancelBtnVisible(boolean visible) {
        if (btn_cancel_pk != null) {
            if (visible) {
                if (btn_cancel_pk.getVisibility() != View.VISIBLE) {
                    btn_cancel_pk.setVisibility(View.VISIBLE);
                }
            } else {
                if (btn_cancel_pk.getVisibility() == View.VISIBLE) {
                    btn_cancel_pk.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 发起主播连麦pk
     */
    private void cancelPk() {
        new DialogUitl.Builder(mContext)
                .setContent(mContext.getString(com.livestreaming.common.R.string.cancel) + " pk ?")
                .setCancelable(true)
                .setBackgroundDimEnabled(true)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        setPkCancelBtnVisible(false);
                        ((LiveAnchorActivity) mContext).onClickCancelPK();
                    }
                })
                .build()
                .show();
    }

   /* public void setLinkMicEnable(boolean linkMicEnable) {
        mLinkMicEnable = linkMicEnable;
        showLinkMicEnable();
    }*/

   /* private void showLinkMicEnable() {
        if (mLinkMicEnable) {
            if (mLinkMicIcon != null) {
                mLinkMicIcon.setImageDrawable(mDrawableLinkMic1);
            }
            if (mLinkMicTip != null) {
                mLinkMicTip.setText(R.string.live_link_mic_5);
            }
        } else {
            if (mLinkMicIcon != null) {
                mLinkMicIcon.setImageDrawable(mDrawableLinkMic0);
            }
            if (mLinkMicTip != null) {
                mLinkMicTip.setText(R.string.live_link_mic_4);
            }
        }
    }*/


    public void changeLinkMicEnable() {
        final boolean enable = !mLinkMicEnable;
        LiveHttpUtil.setLinkMicEnable(enable, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    mLinkMicEnable = enable;
                }
                ToastUtil.show(msg);
            }
        });
    }

    public boolean isLinkMicEnable() {
        return mLinkMicEnable;
    }


    public void setClosePkBtnVisible(boolean b) {
        if (!b) {
            btn_close_pk.setImageResource(R.mipmap.icon_live_func_linkmic_aud);
        } else {
            btn_close_pk.setImageResource(R.drawable.image_stop);
        }
    }

    public void showRedDotOnGuestsIcon(boolean b) {
        findViewById(R.id.tip_mic_apply).setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setPkBtnVisible(boolean b) {
        btn_start_pk.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void hidePkBtns() {
        btn_start_pk.setVisibility(View.GONE);
        btn_cancel_pk.setVisibility(View.GONE);
    }
}
