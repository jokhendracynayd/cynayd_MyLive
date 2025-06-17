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
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.dialog.LiveRoomCheckDialogFragment;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

/**
 * Created by cxf on 2017/9/29.
 */

public class LiveRoomCheckLivePresenter {

    private Context mContext;
    private int mLiveType;//直播间的类型  普通 密码 门票 计时等
    private int mLiveTypeVal;//收费价格等
    private String mLiveTypeMsg;//直播间提示信息或房间密码
    private LiveBean mLiveBean;
    private ActionListener mActionListener;
    private int mLiveSdk;
    private int mCamStatus = 1;

    public LiveRoomCheckLivePresenter(Context context, ActionListener actionListener) {
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

    private int mLiveRoomType = Constants.CHAT_ROOM_TYPE_VOICE;
    private HttpCallback mCheckLiveCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if (info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mLiveType = obj.getIntValue("type");
                    mLiveTypeVal = obj.getIntValue("type_val");
                    mLiveTypeMsg = obj.getString("type_msg");
                    mLiveSdk = obj.getIntValue("live_sdk");
                    mLiveRoomType = obj.getIntValue("voice_type");
                    mCamStatus = obj.getIntValue("cam_status");

                    if (mLiveRoomType == CHAT_ROOM_TYPE_VOICE)
                        LiveVoiceRoomUserCount = obj.getIntValue("voice_wheats");
                    else LiveVoiceRoomUserCount = 9;
                    if (mLiveType == Constants.LIVE_TYPE_NORMAL) {
                        enterLiveRoom();
                    } else {
                        if (CommonAppConfig.getInstance().isTeenagerType()) {
                            if (mLiveType == Constants.LIVE_TYPE_PAY || mLiveType == Constants.LIVE_TYPE_TIME) {
                                new DialogUitl.Builder(mContext)
                                        .setContent(WordUtil.getString(com.livestreaming.common.R.string.a_137))
                                        .setCancelString(WordUtil.getString(com.livestreaming.common.R.string.know))
                                        .setConfrimString(WordUtil.getString(com.livestreaming.common.R.string.a_118))
                                        .setCancelable(true)
                                        .setBackgroundDimEnabled(true)
                                        .setClickCallback(new DialogUitl.SimpleCallback2() {


                                            @Override
                                            public void onCancelClick() {
                                                if (mContext != null && mContext instanceof LiveAudienceActivity) {
                                                    ((LiveAudienceActivity) mContext).exitLiveRoom();
                                                }
                                            }

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

                        LiveRoomCheckDialogFragment fragment = new LiveRoomCheckDialogFragment();
                        if (mLiveType == Constants.LIVE_TYPE_PWD) {
                            fragment.setLiveType(mLiveType, mLiveTypeMsg);
                        } else {
                            fragment.setLiveType(mLiveType, String.valueOf(mLiveTypeVal));
                        }
                        fragment.setActionListener(new LiveRoomCheckDialogFragment.ActionListener() {
                            @Override
                            public void onCloseClick() {
                                if (mContext != null && mContext instanceof LiveAudienceActivity) {
                                    ((LiveAudienceActivity) mContext).exitLiveRoom();
                                }
                            }

                            @Override
                            public void onConfirmClick() {
                                if (mLiveType == Constants.LIVE_TYPE_PWD) {
                                    enterLiveRoom();
                                } else {
                                    if (((AbsActivity) mContext).checkLogin()) {
                                        roomCharge();
                                    }
                                }
                            }

                            @Override
                            public void onNextClick() {
                                if (mContext != null && mContext instanceof LiveAudienceActivity) {
                                    ((LiveAudienceActivity) mContext).scrollNextPosition();
                                }
                            }
                        });
                        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveRoomCheckDialogFragment");
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
            mActionListener.onLiveRoomChanged(mLiveBean, mLiveType, mLiveTypeVal, mLiveSdk, mCamStatus, mLiveRoomType);
        }
    }


    public interface ActionListener {
        void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal, int liveSdk, int cam_status, int chatRoomType);
    }
}
