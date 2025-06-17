package com.livestreaming.live.presenter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.MyCountdown;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.custom.ProgressTextView;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.interfaces.ILiveLinkMicViewHolder;
import com.livestreaming.live.socket.SocketClient;
import com.livestreaming.live.socket.SocketLinkMicPkUtil;
import com.livestreaming.live.views.LiveLinkMicPkViewHolder;

/**
 * Created by cxf on 2018/11/16.
 * 主播与主播PK逻辑
 */

public class LiveLinkMicPkPresenter implements View.OnClickListener {

    private static final int WHAT_PK_WAIT_RECEIVE = 0;//收到pk申请等待 what
    private static final int WHAT_PK_WAIT_SEND = 1;//发送pk申请等待 what
    //    private static final int WHAT_PK_TIME = 2;//pk时间变化 what
    private static final int LINK_MIC_COUNT_MAX = 10;
    private static final int PK_TIME_MAX = 60 * 5;//pk时间 5分钟
    private static final int PK_TIME_MAX_2 = 60*3;//惩罚时间 1分钟
    private Context mContext;
    private View mRoot;
    private boolean mIsAnchor;//自己是否是主播
    private SocketClient mSocketClient;
    private ViewGroup mPkContainer;
    private boolean mIsApplyDialogShow;//是否显示了申请PK的弹窗
    private boolean mAcceptPk;//是否接受连麦
    private boolean mIsPk;//是否已经Pk了
    private String mApplyUid;//正在申请Pk的主播的uid
    private String mApplyStream;//正在申请Pk的主播的stream
    private String mLiveUid;//自己主播的uid
    private String mPkUid;//正在Pk的对方主播的uid
    private ProgressTextView mLinkMicWaitProgress;
    private int mPkWaitCount;//Pk弹窗等待倒计时Live
    private PopupWindow mPkPopWindow;
    private Handler mHandler;
    private MyCountdown mMyCountdown;
    private LiveLinkMicPkViewHolder mLiveLinkMicPkViewHolder;
    private String mPkTimeString1;
    private String mPkTimeString2;
    private boolean mIsPkEnd;//pk是否结束，进入惩罚时间
    private boolean mPkSend;//pk请求是否已经发送
    private int mPkSendWaitCount;//发送pk请求后的等待时间
    private String mSelfStream;
    private long mNextTime = -1;
    private boolean isInPunishTime = false;

    public LiveLinkMicPkPresenter(Context context, ILiveLinkMicViewHolder linkMicViewHolder, boolean isAnchor, View root) {
        mContext = context;
        mIsAnchor = isAnchor;
        mRoot = root;
        mPkContainer = linkMicViewHolder.getPkContainer();
        mPkTimeString1 = WordUtil.getString(com.livestreaming.common.R.string.live_pk_time_1);
        mPkTimeString2 = WordUtil.getString(com.livestreaming.common.R.string.live_pk_time_2);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WHAT_PK_WAIT_RECEIVE:
                        onApplyPkWait();
                        break;
                    case WHAT_PK_WAIT_SEND:
                        onSendPkWait();
                        break;
//                    case WHAT_PK_TIME:
//                        changePkTime();
//                        break;
                }
            }
        };
    }

    public void setSocketClient(SocketClient socketClient) {
        mSocketClient = socketClient;
    }

    public void setLiveUid(String liveUid) {
        mLiveUid = liveUid;
    }

    /**
     * 申请pk弹窗倒计时
     */
    private void onApplyPkWait() {
        mPkWaitCount--;
        if (mPkWaitCount >= 0) {
            if (mLinkMicWaitProgress != null) {
                mLinkMicWaitProgress.setProgress(mPkWaitCount);
                if (mHandler != null) {
                    mHandler.sendEmptyMessageAtTime(WHAT_PK_WAIT_RECEIVE, getNextSecondTime());
                }
            }
        } else {
            if (mPkPopWindow != null) {
                mPkPopWindow.dismiss();
            }
        }
    }

    /**
     * 发送pk申请后等待倒计时
     */
    private void onSendPkWait() {
        mPkSendWaitCount--;
        if (mPkSendWaitCount >= 0) {
            nextSendPkWaitCountDown();
        } else {
            hideSendPkWait();
        }
    }


    private void nextSendPkWaitCountDown() {
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.setPkWaitProgress(mPkSendWaitCount);
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageAtTime(WHAT_PK_WAIT_SEND, getNextSecondTime());
        }
    }

    /**
     * 隐藏pk申请等待
     */
    private void hideSendPkWait() {
        mPkSend = false;
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_PK_WAIT_SEND);
        }
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.setPkWaitProgressVisible(false);
        }
    }


    /**
     * 获取下一秒钟的时间
     */
    private long getNextSecondTime() {
      /*  long now = SystemClock.uptimeMillis();
        return now + (1000 - now % 1000);*/
        if (mNextTime == -1) {
            mNextTime = SystemClock.uptimeMillis();
        }
        mNextTime += 1000;
        return mNextTime;
    }

    /**
     * 发起主播PK申请
     */
    public void applyLinkMicPk(String pkUid, String stream) {
        if (mIsPk && !isInPunishTime) {
            ToastUtil.show(com.livestreaming.common.R.string.live_link_mic_cannot_pk);
            return;
        }
        if (mIsPk) {
            isInPunishTime = false;
            onLinkMicPkClose();
        }
        if (isInPunishTime) {
            if (mMyCountdown != null) {
                mMyCountdown.release();
            }
        }
        if (mPkSend && !isInPunishTime) {
            ToastUtil.show(com.livestreaming.common.R.string.link_mic_apply_waiting);
            return;
        }
        mPkSend = true;
        SocketLinkMicPkUtil.linkMicPkApply(mSocketClient, pkUid, stream);
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_apply_pk);

        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.removeFromParent();
        }
        mLiveLinkMicPkViewHolder = new LiveLinkMicPkViewHolder(mContext, mPkContainer);
        mLiveLinkMicPkViewHolder.addToParent();
        mLiveLinkMicPkViewHolder.setPkWaitProgressVisible(true);
        mPkSendWaitCount = LINK_MIC_COUNT_MAX;
        mNextTime = -1;
        nextSendPkWaitCountDown();
        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).setStartPkBtnVisible(false);
        }
    }

    /**
     * 主播与主播PK  主播收到其他主播发过来的PK申请的回调
     */
    public void onLinkMicPkApply(UserBean u, String stream) {

        if (!mIsAnchor) {
            return;
        }
        if (u == null || TextUtils.isEmpty(stream)) {
            return;
        }
        if (!TextUtils.isEmpty(mApplyUid) && mApplyUid.equals(u.getId())) {
            return;
        }
        if (mPkSend && !isInPunishTime) {
            SocketLinkMicPkUtil.linkMicPkBusy(mSocketClient, u.getId());
            return;
        }
        if ((!mIsPk || !isInPunishTime) || !mIsApplyDialogShow) {
            if (mIsPk) {
                isInPunishTime = false;
                onLinkMicPkClose();
            }
            mApplyUid = u.getId();
            mApplyStream = stream;
            showApplyDialog(u);
        } else {
            SocketLinkMicPkUtil.linkMicPkBusy(mSocketClient, u.getId());
        }
    }

    /**
     * 显示申请PK的弹窗
     */
    private void showApplyDialog(UserBean u) {
        mIsApplyDialogShow = true;
        mAcceptPk = false;
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_link_mic_pk_wait, null);
        mLinkMicWaitProgress = v.findViewById(R.id.pk_wait_progress);
        v.findViewById(R.id.btn_refuse).setOnClickListener(this);
        v.findViewById(R.id.btn_accept).setOnClickListener(this);
        mPkWaitCount = LINK_MIC_COUNT_MAX;
        mNextTime = -1;
        mPkPopWindow = new PopupWindow(v, DpUtil.dp2px(280), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPkPopWindow.setBackgroundDrawable(new ColorDrawable());
        mPkPopWindow.setOutsideTouchable(false);
        mPkPopWindow.setFocusable(false);
        mPkPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mHandler != null) {
                    mHandler.removeMessages(WHAT_PK_WAIT_RECEIVE);
                }
                if (mAcceptPk) {
                    LiveHttpUtil.livePkCheckLive(mApplyUid, mApplyStream, mSelfStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                SocketLinkMicPkUtil.linkMicPkAccept(mSocketClient, mApplyUid);
                                mIsPk = true;
                            } else {
                                ToastUtil.show(msg);
                            }
                        }
                    });
                } else {
                    if (mPkWaitCount < 0) {
                        SocketLinkMicPkUtil.linkMicPkNotResponse(mSocketClient, mApplyUid);
                    } else {
                        SocketLinkMicPkUtil.linkMicPkRefuse(mSocketClient, mApplyUid);
                    }
                    mApplyUid = null;
                    mApplyStream = null;
                    if (mIsAnchor) {
                        ((LiveAnchorActivity) mContext).setPkBtnVisible(true);
                    }
                }
                mPkSend = false;
                mIsApplyDialogShow = false;
                mLinkMicWaitProgress = null;
                mPkPopWindow = null;
            }
        });
        mPkPopWindow.showAtLocation(mRoot, Gravity.CENTER, 0, 0);
        if (mHandler != null) {
            mHandler.sendEmptyMessageAtTime(WHAT_PK_WAIT_RECEIVE, getNextSecondTime());
        }
        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).setPkBtnVisible(false);
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_refuse) {
            refuseLinkMic();

        } else if (i == R.id.btn_accept) {
            acceptLinkMic();

        }
    }

    /**
     * 拒绝PK
     */
    private void refuseLinkMic() {
        if (mPkPopWindow != null) {
            mPkPopWindow.dismiss();
        }
    }

    /**
     * 接受PK
     */
    private void acceptLinkMic() {
        mAcceptPk = true;
        if (mPkPopWindow != null) {
            mPkPopWindow.dismiss();
        }
    }

    /**
     * pk 进度发送变化
     *
     * @param leftGift
     * @param rightGift
     */
    public void onPkProgressChanged(long leftGift, long rightGift) {
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.onProgressChanged(leftGift, rightGift);
        }
    }


    /**
     * 进房间的时候PK开始
     */
    public void onEnterRoomPkStart(String pkUid, long leftGift, long rightGift, int pkTime) {
        mIsPk = true;
        mPkSend = false;
        mIsPkEnd = false;
        mPkUid = pkUid;
        mApplyUid = null;
        mApplyStream = null;
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.removeFromParent();
        }
        mLiveLinkMicPkViewHolder = new LiveLinkMicPkViewHolder(mContext, mPkContainer);
        mLiveLinkMicPkViewHolder.addToParent();
        mLiveLinkMicPkViewHolder.showTime();
        mLiveLinkMicPkViewHolder.onEnterRoomPkStart();
        mLiveLinkMicPkViewHolder.onProgressChanged(leftGift, rightGift);
        startPkCountdown(pkTime);
    }
    public void onEnterRoomPkEnd(String pkUid, long leftGift, long rightGift, int pkTimeEnd) {
        mIsPk = false;
        mPkSend = true;
        mIsPkEnd = true;
        mPkUid = pkUid;
        Log.e("pk_enter_room","pkuid :"+pkUid+", left :"+leftGift+", right :"+rightGift+", time :"+pkTimeEnd);
        if (mLiveLinkMicPkViewHolder == null) {
            mLiveLinkMicPkViewHolder = new LiveLinkMicPkViewHolder(mContext, mPkContainer);

            Log.e("pk_enter_room1","pkuid :"+pkUid+", left :"+leftGift+", right :"+rightGift+", time :"+pkTimeEnd);
            mLiveLinkMicPkViewHolder.addToParent();
        }
        mLiveLinkMicPkViewHolder.showTime();

        Log.e("pk_enter_room2","pkuid :"+pkUid+", left :"+leftGift+", right :"+rightGift+", time :"+pkTimeEnd);
        mLiveLinkMicPkViewHolder.onProgressChanged(leftGift, rightGift);

        if (mMyCountdown == null) {
            mMyCountdown = new MyCountdown();
        }

        isInPunishTime = true;
        mMyCountdown.setTotalSecond(pkTimeEnd).setCallback(new MyCountdown.ActionListener() {
            @Override
            public void onTimeChanged(String timeStr) {
                if (mLiveLinkMicPkViewHolder != null) {
                    String s = mIsPkEnd ? mPkTimeString2 : mPkTimeString1;
                    mLiveLinkMicPkViewHolder.setTime(s + " " + timeStr);
                }
            }

            @Override
            public void onTimeEnd() {
                if (mIsPkEnd) {
                    isInPunishTime = false;
                    onLinkMicPkClose();

                    if (mIsAnchor) {
                        ((LiveAnchorActivity) mContext).setPkBtnVisible(true);
                    }
                }
            }
        }).start();
    }


    /**
     * 主播与主播PK 所有人收到PK开始的回调
     */
    public void onLinkMicPkStart(String pkUid) {
        mIsPk = true;
        mPkSend = false;
        if (mContext instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) mContext).setInvestRowVisible(false);
        } else if (mContext instanceof LiveAnchorActivity) {
            ((LiveAnchorActivity) mContext).setInvestRowVisible(false);
        }
        hideSendPkWait();
        mIsPkEnd = false;
        mPkUid = pkUid;
        mApplyUid = null;
        mApplyStream = null;
        if(mLiveLinkMicPkViewHolder!=null){
            mLiveLinkMicPkViewHolder.removeFromParent();
        }
        mLiveLinkMicPkViewHolder = new LiveLinkMicPkViewHolder(mContext, mPkContainer);
        mLiveLinkMicPkViewHolder.addToParent();
        mLiveLinkMicPkViewHolder.startAnim();
        mLiveLinkMicPkViewHolder.showTime();
        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).setPkStartBtnVisible(false);
        }
        startPkCountdown(PK_TIME_MAX);
    }


    private void startPkCountdown(int pkTime) {
        if (mMyCountdown == null) {
            mMyCountdown = new MyCountdown();
        }
        mMyCountdown.setTotalSecond(pkTime).setCallback(new MyCountdown.ActionListener() {
            @Override
            public void onTimeChanged(String timeStr) {
                if (mLiveLinkMicPkViewHolder != null) {
                    mLiveLinkMicPkViewHolder.setTime(timeStr);
                }
            }

            @Override
            public void onTimeEnd() {

            }
        }).start();
    }


    /**
     * 主播与主播PK PK结果的回调
     */
    public void onLinkMicPkEnd(String winUid, int win_count) {
//        if (mIsPkEnd) {
//            return;
//        }

        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
            ((LiveAnchorActivity)mContext).setStartPkBtnVisible(true);
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
        isInPunishTime = true;
        mIsPkEnd = true;
        if (mMyCountdown != null) {
            mMyCountdown.release();
        }
        if (mLiveLinkMicPkViewHolder != null) {
            if (!TextUtils.isEmpty(winUid)) {
                if ("0".equals(winUid)) {
                    mLiveLinkMicPkViewHolder.end(0);
                   // mLiveLinkMicPkViewHolder.hideTime();
                    if (mHandler != null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                               // onLinkMicPkClose();
                                if (mIsAnchor) {
                                    ((LiveAnchorActivity) mContext).setPkStartBtnVisible(true);
                                }
                            }
                        }, 3000);

                        mMyCountdown.setTotalSecond(PK_TIME_MAX_2).setCallback(new MyCountdown.ActionListener() {
                            @Override
                            public void onTimeChanged(String timeStr) {
                                if (mLiveLinkMicPkViewHolder != null) {
                                    String s = mIsPkEnd ? mPkTimeString2 : mPkTimeString1;
                                    mLiveLinkMicPkViewHolder.setTime(s + " " + timeStr);
                                }
                            }

                            @Override
                            public void onTimeEnd() {
                                if (mIsPkEnd) {
                                    isInPunishTime = false;
                                    onLinkMicPkClose();
                                    if (mIsAnchor) {
                                        ((LiveAnchorActivity) mContext).setPkStartBtnVisible(true);
                                    }
                                    if(mLiveUid.equals(CommonAppConfig.getInstance().getUid())){
                                        sendResetPk();
                                    }
                                }
                            }
                        }).start();
                    }
                } else {
                    if (mIsAnchor) {
                        ((LiveAnchorActivity) mContext).setStartPkBtnVisible(true);
                        if (winUid.equals(CommonAppConfig.getInstance().getUid())) {
                            ((LiveActivity) mContext).setLeftCountText(" x" + win_count);
                            ((LiveActivity) mContext).setRightCountText(" x" + 0);
                            mLiveLinkMicPkViewHolder.end(1);
                        } else {
                            ((LiveActivity) mContext).setRightCountText(" x" + win_count);
                            ((LiveActivity) mContext).setLeftCountText(" x" + 0);
                            mLiveLinkMicPkViewHolder.end(-1);
                        }
                    } else {
                        if (winUid.equals(mLiveUid)) {
                            ((LiveActivity) mContext).setLeftCountText(" x" + win_count);
                            ((LiveActivity) mContext).setRightCountText(" x" + 0);
                            mLiveLinkMicPkViewHolder.end(1);
                        } else {
                            ((LiveActivity) mContext).setRightCountText(" x" + win_count);
                            ((LiveActivity) mContext).setLeftCountText(" x" + 0);
                            mLiveLinkMicPkViewHolder.end(-1);
                        }
                    }
                    if (mMyCountdown == null) {
                        mMyCountdown = new MyCountdown();
                    }

                    mMyCountdown.setTotalSecond(PK_TIME_MAX_2).setCallback(new MyCountdown.ActionListener() {
                        @Override
                        public void onTimeChanged(String timeStr) {
                            if (mLiveLinkMicPkViewHolder != null) {
                                String s = mIsPkEnd ? mPkTimeString2 : mPkTimeString1;
                                mLiveLinkMicPkViewHolder.setTime(s + " " + timeStr);
                            }
                        }

                        @Override
                        public void onTimeEnd() {
                            if (mIsPkEnd) {
                                isInPunishTime = false;
                                onLinkMicPkClose();
                                if (mIsAnchor) {
                                    ((LiveAnchorActivity) mContext).setPkBtnVisible(true);
                                }
                                if(mLiveUid.equals(CommonAppConfig.getInstance().getUid())){
                                    sendResetPk();
                                }
                            }
                        }
                    }).start();
                }
            }

        }
    }

    private void sendResetPk() {
        LiveHttpUtil.resetPkData(mPkUid,new HttpCallback(){
            @Override
            public void onSuccess(int code, String msg, String[] info) {

            }
        });
    }

    /**
     * 主播与主播PK 断开连麦PK的回调
     */

    public void onLinkMicPkClose() {
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mMyCountdown != null) {
            mMyCountdown.release();
        }
        if (mPkPopWindow != null) {
            mPkPopWindow.dismiss();
        }
        mPkPopWindow = null;
        mIsPk = false;
        mIsPkEnd = false;
        mPkSend = false;
        hideSendPkWait();
        mPkUid = null;
        mApplyUid = null;
        mApplyStream = null;
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.removeFromParent();
            mLiveLinkMicPkViewHolder.release();
        }
        mLiveLinkMicPkViewHolder = null;
        ((LiveActivity) mContext).setInvestRowVisible(false);

    }
    public void closeLinkMicPk() {
        SocketLinkMicPkUtil.linkMicAnchorClose(mSocketClient,mPkUid);
        ((LiveAnchorActivity)mContext).releaseDouble();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mMyCountdown != null) {
            mMyCountdown.release();
        }
        if (mPkPopWindow != null) {
            mPkPopWindow.dismiss();
        }
        mPkPopWindow = null;
        mIsPk = false;
        mIsPkEnd = false;
        mPkSend = false;
        hideSendPkWait();
        mPkUid = null;
        mApplyUid = null;
        mApplyStream = null;
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.removeFromParent();
            mLiveLinkMicPkViewHolder.release();
        }
        mLiveLinkMicPkViewHolder = null;
        ((LiveActivity) mContext).setInvestRowVisible(false);

    }

    /**
     * 主播与主播Pk 对方主播拒绝Pk的回调
     */
    public void onLinkMicPkRefuse() {
        hideSendPkWait();
        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).setPkBtnVisible(true);
        }
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_refuse_pk);
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
    }

    /**
     * 主播与主播Pk  对方主播无响应的回调
     */
    public void onLinkMicPkNotResponse() {
        hideSendPkWait();
        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).setPkBtnVisible(true);
            ((LiveAnchorActivity) mContext).setStartPkBtnVisible(true);
        }
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_anchor_not_response_2);
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
    }

    /**
     * 主播与主播Pk  对方主播正在忙的回调
     */
    public void onLinkMicPkBusy() {
        hideSendPkWait();
        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).setPkBtnVisible(true);
            ((LiveAnchorActivity) mContext).setStartPkBtnVisible(true);

        }
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_anchor_busy_2);
    }


    public void release() {
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        mSocketClient = null;
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.release();
        }
        mLiveLinkMicPkViewHolder = null;
        if (mMyCountdown != null) {
            mMyCountdown.release();
            mMyCountdown = null;
        }
    }


    public void clearData() {
        if (mContext != null) {
            ((LiveActivity) mContext).setInvestRowVisible(false);
        }
        mIsApplyDialogShow = false;
        mAcceptPk = false;
        mIsPk = false;
        mApplyUid = null;
        mApplyStream = null;
        mLiveUid = null;
        mPkUid = null;
        mPkWaitCount = 0;
        mIsPkEnd = false;
        mPkSend = false;
        mPkSendWaitCount = 0;
        mNextTime = -1;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mLiveLinkMicPkViewHolder != null) {
            mLiveLinkMicPkViewHolder.release();
            mLiveLinkMicPkViewHolder.removeFromParent();
        }
        mLiveLinkMicPkViewHolder = null;
        if (mMyCountdown != null) {
            mMyCountdown.release();
        }
    }

    public void setSelfStream(String selfStream) {
        mSelfStream = selfStream;
    }

    public void updatePkBounses(int value, int uid, int completed) {

        if (mIsAnchor) {
            ((LiveAnchorActivity) mContext).updatePkBouns(value, uid, completed);
        } else {
            ((LiveAudienceActivity) mContext).updatePkBouns(value, uid, completed);
        }

    }
}
