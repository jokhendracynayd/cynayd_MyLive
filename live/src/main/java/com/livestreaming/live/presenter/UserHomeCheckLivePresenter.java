package com.livestreaming.live.presenter;

import static com.livestreaming.common.Constants.CHAT_ROOM_TYPE_VOICE;
import static com.livestreaming.common.Constants.LiveVoiceRoomUserCount;

import android.app.Dialog;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.dialog.LiveCheckDialogFragment;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

/**
 * Created by cxf on 2017/9/29.
 */

public class UserHomeCheckLivePresenter {

    private Context mContext;
    private int mLiveType;//直播间的类型  普通 密码 门票 计时等
    private int mLiveTypeVal;//收费价格等
    private String mLiveTypeMsg;//直播间提示信息或房间密码
    private LiveBean mLiveBean;
    private ActionListener mActionListener;
    private int mLiveSdk;
    private boolean mIsChatRoom;
    private int mChatRoomType;

    public UserHomeCheckLivePresenter(Context context, ActionListener actionListener) {
        mContext = context;
        mActionListener = actionListener;
    }

    /**
     * 观众 观看直播
     */
    public void checkLive(LiveBean bean) {
        mLiveBean = bean;
        LiveHttpUtil.checkLive(bean.getUid(), bean.getStream(), mCheckLiveCallback);
    }

    private HttpCallback mCheckLiveCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if (info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mIsChatRoom = obj.getIntValue("live_type") == 1;
                    mChatRoomType = obj.getIntValue("voice_type");
                    mLiveType = obj.getIntValue("type");
                    mLiveTypeVal = obj.getIntValue("type_val");
                    mLiveTypeMsg = obj.getString("type_msg");
                    mLiveSdk = obj.getIntValue("live_sdk");
                    if (mChatRoomType == CHAT_ROOM_TYPE_VOICE)
                        LiveVoiceRoomUserCount = obj.getIntValue("voice_wheats");
                    else LiveVoiceRoomUserCount = 9;
                    if (mLiveType == Constants.LIVE_TYPE_NORMAL) {
                        forwardNormalRoom();
                    } else {
                        if (CommonAppConfig.getInstance().isTeenagerType()) {
                            if (mLiveType == Constants.LIVE_TYPE_PAY || mLiveType == Constants.LIVE_TYPE_TIME) {
                                new DialogUitl.Builder(mContext)
                                        .setContent(WordUtil.getString(com.livestreaming.common.R.string.a_137))
                                        .setCancelString(WordUtil.getString(com.livestreaming.common.R.string.know))
                                        .setConfrimString(WordUtil.getString(com.livestreaming.common.R.string.a_118))
                                        .setCancelable(true)
                                        .setBackgroundDimEnabled(true)
                                        .setClickCallback(new DialogUitl.SimpleCallback() {


                                            @Override
                                            public void onConfirmClick(Dialog dialog, String content) {
                                                RouteUtil.forwardTeenager(mContext);
                                            }
                                        })
                                        .build()
                                        .show();
                                return;
                            }
                        }
                        LiveCheckDialogFragment fragment = new LiveCheckDialogFragment();
                        if (mLiveType == Constants.LIVE_TYPE_PWD) {
                            fragment.setLiveType(mLiveType, mLiveTypeMsg);
                        } else {
                            fragment.setLiveType(mLiveType, String.valueOf(mLiveTypeVal));
                        }
                        fragment.setActionListener(new LiveCheckDialogFragment.ActionListener() {
                            @Override
                            public void onConfirmClick() {
                                if (mLiveType == Constants.LIVE_TYPE_PWD) {
                                    forwardNormalRoom();
                                } else {
                                    if (((AbsActivity) mContext).checkLogin()) {
                                        roomCharge();
                                    }
                                }
                            }
                        });
                        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveRoomCheckDialogFragment2");
                    }
                }
            } else {
                ToastUtil.show(msg);
            }
        }

        @Override
        public boolean showLoadingDialog() {
            return true;
        }

        @Override
        public Dialog createLoadingDialog() {
            return DialogUitl.loadingDialog(mContext);
        }
    };

    /**
     * 前往普通房间
     */
    private void forwardNormalRoom() {
        enterLiveRoom();
    }


    public void roomCharge() {
        if (mLiveBean == null) {
            return;
        }
        LiveHttpUtil.roomCharge(mLiveBean.getUid(), mLiveBean.getStream(), mRoomChargeCallback);
    }

    private HttpCallback mRoomChargeCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                enterLiveRoom();
            } else {
                ToastUtil.show(msg);
            }
        }

        @Override
        public boolean showLoadingDialog() {
            return true;
        }

        @Override
        public Dialog createLoadingDialog() {
            return DialogUitl.loadingDialog(mContext);
        }
    };

    public void cancel() {
        mActionListener = null;
        LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
    }

    /**
     * 进入直播间
     */
    private void enterLiveRoom() {
        if (mActionListener != null) {
            mActionListener.onLiveRoomChanged(mLiveBean, mLiveType, mLiveTypeVal, mLiveSdk, mIsChatRoom, mChatRoomType);
        }
    }


    public interface ActionListener {
        void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal, int liveSdk, boolean isChatRoom, int chatRoomType);
    }
}
