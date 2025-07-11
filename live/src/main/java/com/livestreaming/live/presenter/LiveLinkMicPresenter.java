package com.livestreaming.live.presenter;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.LevelBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.utils.ClickUtil;
import com.livestreaming.common.utils.CommonIconUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.MyCountdown;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.bean.LiveGestBean;
import com.livestreaming.live.event.LinkMicTxAccEvent;
import com.livestreaming.live.event.LinkMicTxMixStreamEvent;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.interfaces.ILiveLinkMicViewHolder;
import com.livestreaming.live.interfaces.LivePushListener;
import com.livestreaming.live.socket.SocketClient;
import com.livestreaming.live.socket.SocketLinkMicUtil;
import com.livestreaming.live.views.AbsLiveLinkMicPlayViewHolder;
import com.livestreaming.live.views.AbsLiveLinkMicPushViewHolder;
import com.livestreaming.live.views.LivePushAgoraViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/25.
 * 观众和主播连麦的逻辑
 */

public class LiveLinkMicPresenter implements View.OnClickListener {

    private Context mContext;
    private View mRoot;
    private SocketClient mSocketClient;
    private boolean mIsAnchor;//是否是主播
    public ArrayList<UserBean> Guests = new ArrayList<>();
    private String mLiveUid;//主播的uid
    private LinearLayout gestCont;
    //    private TextView mLinkMicTip;
    private TextView mLinkMicWaitText;
    private String mApplyUid;//正在申请连麦的人的uid
    private String mLinkMicUid;//已经连麦的人的uid
    private String mLinkMicName;//已经连麦的人的昵称
    private long mLastApplyLinkMicTime;//观众上次申请连麦的时间
    private boolean mIsLinkMic;//是否已经连麦了
    private boolean mIsLinkMicDialogShow;//观众申请连麦的弹窗是否显示了
    private boolean mAcceptLinkMic;//是否接受连麦
    private String mLinkMicWaitString;
    private int mLinkMicWaitCount;//连麦弹窗等待倒计时
    private static final int LINK_MIC_COUNT_MAX = 10;
    private PopupWindow mLinkMicPopWindow;
    private MyCountdown mMyCountdown;
    private AbsLiveLinkMicPlayViewHolder mLiveLinkMicPlayViewHolder;//连麦播放小窗口
    private AbsLiveLinkMicPushViewHolder mLiveLinkMicPushViewHolder;//连麦推流小窗口
    private boolean mPaused;
    private ArrayList<String> mLinkMicUidList=new ArrayList<>();

    public LiveLinkMicPresenter(Context context, ILiveLinkMicViewHolder linkMicViewHolder, boolean isAnchor, int liveSdk, View root) {
        mContext = context;
        mRoot = root;
        mIsAnchor = isAnchor;
        gestCont = linkMicViewHolder.getGestConitaner();
//        if (!isAnchor && root != null) {
//            View btnLinkMic = root.findViewById(R.id.btn_link_mic);
//            if (btnLinkMic != null) {
//                if (!CommonAppConfig.getInstance().isTeenagerType()) {
//                    btnLinkMic.setVisibility(View.VISIBLE);
//                    btnLinkMic.setOnClickListener(this);
//                }
//                mLinkMicTip = btnLinkMic.findViewById(R.id.link_mic_tip);
//            }
//        }
        mLinkMicWaitString = WordUtil.getString(com.livestreaming.common.R.string.link_mic_wait);

    }

    public void setSocketClient(SocketClient socketClient) {
        mSocketClient = socketClient;
    }

    public void setLiveUid(String liveUid) {
        mLiveUid = liveUid;
    }


    /**
     * 主播收到观众申请连麦的回调
     */
    public void onAudienceApplyLinkMic(UserBean u) {
        if (!mIsAnchor) {
            return;
        }
        if (u == null) {
            return;
        }

        if (isLinkMic() || ((LiveActivity) mContext).isLinkMicAnchor() || ((LiveActivity) mContext).isGamePlaying()) {
            SocketLinkMicUtil.anchorBusy(mSocketClient, u.getId());
            return;
        }
        mApplyUid = "";
        if (!TextUtils.isEmpty(mApplyUid) && mApplyUid.equals(u.getId())) {
            return;
        }
        if (!mIsLinkMic && !mIsLinkMicDialogShow) {
            mApplyUid = u.getId();
            LiveHttpUtil.addGuestRequest(mLiveUid, ((LiveActivity) mContext).mStream, u.getId(), 0, 0, u.getAvatar(), u.getFrame(), u.getUserNiceName(), new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {

                }
            });
            showRedDotOnGuestsIcon();
            //showLinkMicDialog(u);
        } else {
            SocketLinkMicUtil.anchorBusy(mSocketClient, u.getId());
        }
    }

    private void showRedDotOnGuestsIcon() {
        ((LiveAnchorActivity) mContext).showRedDotOnGuestsIcon(true);
    }

    /**
     * 观众收到主播同意连麦的回调
     */
    public void onAnchorAcceptLinkMic(String touid) {
        if (mIsAnchor || TextUtils.isEmpty(touid)) {
            return;
        }
        ((LiveAudienceActivity) mContext).changeLinkMicGuestIcon(true);
        mIsLinkMic = true;
        mLinkMicUid = touid;
        if (touid.equals(CommonAppConfig.getInstance().getUid())) {
            mLastApplyLinkMicTime = 0;
            ToastUtil.show(com.livestreaming.common.R.string.link_mic_anchor_accept);
            if (((LiveActivity) mContext).isTxSdK()) {
                LiveHttpUtil.getLinkMicStream(mLiveUid, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            JSONObject obj = JSON.parseObject(info[0]);
                            final String pushUrl = obj.getString("pushurl");
                            final String playUrl = obj.getString("playurl");
//                        L.e("getLinkMicStream", "pushurl--推流地址--->" + pushUrl);
//                        L.e("getLinkMicStream", "playurl--播放地址--->" + playUrl);
                            // mLiveLinkMicPushViewHolder = new LiveLinkMicPushTxViewHolder(mContext, mSmallContainer);
                            mLiveLinkMicPushViewHolder.setLivePushListener(new LivePushListener() {
                                @Override
                                public void onPreviewStart() {
                                    //预览成功的回调
                                }

                                @Override
                                public void onPushStart() {//推流成功的回调
                                    SocketLinkMicUtil.audienceSendLinkMicUrl(mSocketClient, playUrl);
                                }

                                @Override
                                public void onPushFailed() {//推流失败的回调
                                    DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.link_mic_failed_2), null);
                                    SocketLinkMicUtil.audienceCloseLinkMic(mSocketClient);
                                }
                            });
                            mLiveLinkMicPushViewHolder.addToParent();
                            mLiveLinkMicPushViewHolder.startPush(pushUrl);
                            EventBus.getDefault().post(new LinkMicTxAccEvent(true));
                        }
                    }
                });
            } else {
                ((LiveAudienceActivity) mContext).toggleLinkMicPushAgora(true, touid);
                mLinkMicUidList.add(touid);
            }
        }
    }

    /**
     * 观众收到主播拒绝连麦的回调
     */
    public void onAnchorRefuseLinkMic() {
        mLastApplyLinkMicTime = 0;
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_refuse);
    }

    /**
     * 所有人收到连麦观众发过来的播流地址的回调
     *
     * @param playUrl 连麦观众的播放地址
     */
    public void onAudienceSendLinkMicUrl(String uid, String uname, String playUrl) {
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        if (mIsAnchor) {
            if (!TextUtils.isEmpty(mApplyUid) && !uid.equals(mApplyUid)) {
                return;
            }
            LiveHttpUtil.linkMicShowVideo(uid, playUrl);
        }

        mApplyUid = null;
        mLinkMicName = uname;
        mLinkMicUid = uid;
        if (((LiveActivity) mContext).isTxSdK()) {
            if (mIsAnchor) {
//                mLiveLinkMicPlayViewHolder = new LiveLinkMicPlayTxViewHolder(mContext, mSmallContainer);
                mLiveLinkMicPlayViewHolder.setOnCloseListener(this);
                mLiveLinkMicPlayViewHolder.mBtnClose.setVisibility(View.VISIBLE);
                mLiveLinkMicPlayViewHolder.addToParent();
                mLiveLinkMicPlayViewHolder.play(playUrl);

                //主播混流
                int startIndex = playUrl.lastIndexOf("/");
                int endIndex = playUrl.indexOf("?", startIndex);
                if (startIndex >= 0 && startIndex < playUrl.length()
                        && endIndex >= 0 && endIndex < playUrl.length()
                        && startIndex < endIndex) {
                    String toStream = playUrl.substring(startIndex + 1, endIndex);
                    if (!TextUtils.isEmpty(toStream)) {
                        EventBus.getDefault().post(new LinkMicTxMixStreamEvent(Constants.LINK_MIC_TYPE_NORMAL, uid, toStream));
                    }
                }
            }
        } else {
            if (mIsAnchor) {
                //mLiveLinkMicPlayViewHolder = new LiveLinkMicPlayAgoraViewHolder(mContext, mSmallContainer);
                mLiveLinkMicPlayViewHolder.setOnCloseListener(this);
                mLiveLinkMicPlayViewHolder.addToParent();
            }
        }
    }

    /**
     * 显示连麦的播放窗口
     */
    public void onLinkMicPlay(String uid) {
        mLinkMicUid = uid;
    }

    public String getLinkMicUid() {
        return mLinkMicUid;
    }   public List<String> getLinkMicUids() {
        return mLinkMicUidList;
    }

    /**
     * 关闭连麦
     */
    String tempUName = "";

    private void closeLinkMic(String uid, String uname) {
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
        tempUName = uname;
        if (!TextUtils.isEmpty(uid) && uid.equals(mLinkMicUid)) {
//            if (!"undefined".equals(uname)) {
//                ToastUtil.show(uname + WordUtil.getString(com.livestreaming.common.R.string.link_mic_exit));
//            }
            if (((LiveActivity) mContext).isTxSdK()) {
                if (mIsAnchor) {
                    if (mLiveLinkMicPlayViewHolder != null) {
                        mLiveLinkMicPlayViewHolder.release();
                        mLiveLinkMicPlayViewHolder.removeFromParent();
                    }
                    mLiveLinkMicPlayViewHolder = null;
                    EventBus.getDefault().post(new LinkMicTxMixStreamEvent(Constants.LINK_MIC_TYPE_NORMAL, null, null));
                } else {
                    if (!TextUtils.isEmpty(mLinkMicUid) && mLinkMicUid.equals(CommonAppConfig.getInstance().getUid())) {//参与连麦的是自己
                        if (mLiveLinkMicPushViewHolder != null) {
                            mLiveLinkMicPushViewHolder.release();
                            mLiveLinkMicPushViewHolder.removeFromParent();
                        }
                        mLiveLinkMicPushViewHolder = null;
                        EventBus.getDefault().post(new LinkMicTxAccEvent(false));
                    }
                }
            } else {
                if (mIsAnchor) {
                    if (mContext != null) {
                        ((LiveAnchorActivity) mContext).releaseDouble();
                    }
                    if (mLiveLinkMicPlayViewHolder != null) {
                        mLiveLinkMicPlayViewHolder.release();
                        mLiveLinkMicPlayViewHolder.removeFromParent();
                    }
                    mLiveLinkMicPlayViewHolder = null;

                } else {
                    if (mContext != null) {
                        ((LiveAudienceActivity) mContext).releaseDouble();
                    }
                    if (mContext != null) {
                        ((LiveAudienceActivity) mContext).toggleLinkMicPushAgora(false, uid);
                        mLinkMicUidList.remove(uid);
                    }

                }

            }
        }
        mLinkMicUid = null;
        mLinkMicName = null;
        mApplyUid = null;
    }

    /**
     * 所有人收到主播关闭连麦的回调
     */
    public void onAnchorCloseLinkMic(String uid, String uname) {
        closeLinkMic(uid, uname);
    }

    /**
     * 所有人收到已连麦观众关闭连麦的回调
     */
    public void onAudienceCloseLinkMic(String uid, String uname) {
        ((LiveAudienceActivity) mContext).toggleLinkMicPushAgora(false, uid);
        mLinkMicUidList.remove(uid);
    }

    /**
     * 观众申请连麦时，收到主播无响应的回调
     */
    public void onAnchorNotResponse() {
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
        mLastApplyLinkMicTime = 0;
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_anchor_not_response);
    }

    /**
     * 观众申请连麦时，收到主播正在忙的回调
     */
    public void onAnchorBusy() {
        mLastApplyLinkMicTime = 0;
        ToastUtil.show(com.livestreaming.common.R.string.link_mic_anchor_busy);
    }

    /**
     * 已连麦用户退出直播间的回调
     */
    public void onAudienceLinkMicExitRoom(String touid) {

    }

    /**
     * 观众退出直播间回调
     */
    public void onAudienceLeaveRoom(UserBean bean) {
        if(mIsAnchor){
            ((LiveAnchorActivity)mContext).releaseDouble();
        }else{
            ((LiveAudienceActivity)mContext).releaseDouble();
        }
        if (bean != null) {
            String uid = bean.getId();
            if (!TextUtils.isEmpty(uid)) {
                if (uid.equals(mApplyUid)) {
                    if (mMyCountdown != null) {
                        mMyCountdown.release();
                    }
                    if (mLinkMicPopWindow != null) {
                        mLinkMicPopWindow.dismiss();
                    }
                    mIsLinkMic = false;
                    mApplyUid = null;
                }
                if (uid.equals(mLinkMicUid)) {
                    closeLinkMic(uid, bean.getUserNiceName());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtil.canClick()) {
            return;
        }
        int i = v.getId();
//        if (i == R.id.btn_link_mic) {
//            onLinkMicBtnClick();
//
//        } else

        if (i == R.id.btn_refuse) {
            anchorRefuseLinkMicApply();

        } else if (i == R.id.btn_accept) {
            anchorAcceptLinkMicApply();

        } else if (i == R.id.btn_close_link_mic) {
            anchorCloseLinkMic();
        }
    }

    public void onLinkMicBtnClick() {
        if (!((AbsActivity) mContext).checkLogin()) {
            return;
        }
        if (((LiveActivity) mContext).isGamePlaying()) {
            ToastUtil.show(com.livestreaming.common.R.string.live_game_cannot_link_mic);
            return;
        }
        if (((LiveActivity) mContext).isLinkMicAnchor()) {
            ToastUtil.show(com.livestreaming.common.R.string.live_link_mic_cannot_link);
            return;
        }
        if (Constants.isMeInLinkMic) {
            Constants.isMeInLinkMic = false;
            SocketLinkMicUtil.audienceCloseLinkMic(mSocketClient);
        } else {
            checkLinkMicEnable();
        }
    }

    /**
     * 观众检查主播是否允许连麦
     */
    private void checkLinkMicEnable() {

        audienceApplyLinkMic();

//        LiveHttpUtil.checkLinkMicEnable(mLiveUid, new HttpCallback() {
//            @Override
//            public void onSuccess(int code, String msg, String[] info) {
//                if (code == 0) {
//                    audienceApplyLinkMic();
//                } else {
//                    ToastUtil.show(msg);
//                }
//            }
//        });
    }

    /**
     * 观众发起连麦请求
     */
    private void audienceApplyLinkMic() {
        final long curTime = System.currentTimeMillis();
        if (curTime - mLastApplyLinkMicTime < 11000) {//时间间隔11秒
            ToastUtil.show(com.livestreaming.common.R.string.link_mic_apply_waiting);
        } else {
            //请求权限
            PermissionUtil.request((AbsActivity) mContext, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            mLastApplyLinkMicTime = curTime;
                            SocketLinkMicUtil.audienceApplyLinkMic(mSocketClient);
                            ToastUtil.show(com.livestreaming.common.R.string.link_mic_apply);
                        }
                    },
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );
        }
    }


    /**
     * 主播显示连麦的弹窗
     */
//    private void showLinkMicDialog(UserBean u) {
//        if (mIsAnchor) {
//            ((LiveAnchorActivity) mContext).hideLinkMicAnchorWindow();
//        }
//        mIsLinkMicDialogShow = true;
//        mAcceptLinkMic = false;
//        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_link_mic_wait, null);
//        ImageView avatar = (ImageView) v.findViewById(R.id.avatar);
//        TextView name = (TextView) v.findViewById(R.id.name);
//        ImageView sex = (ImageView) v.findViewById(R.id.sex);
//        ImageView level = (ImageView) v.findViewById(R.id.level);
//        mLinkMicWaitText = v.findViewById(R.id.wait_text);
//        v.findViewById(R.id.btn_refuse).setOnClickListener(this);
//        v.findViewById(R.id.btn_accept).setOnClickListener(this);
//        ImgLoader.display(mContext, u.getAvatar(), avatar);
//        name.setText(u.getUserNiceName());
//        sex.setImageResource(CommonIconUtil.getSexIcon(u.getSex()));
//        LevelBean levelBean = CommonAppConfig.getInstance().getLevel(u.getLevel());
//        if (levelBean != null) {
//            ImgLoader.display(mContext, levelBean.getThumb(), level);
//        }
//        mLinkMicWaitCount = LINK_MIC_COUNT_MAX;
//        mLinkMicPopWindow = new PopupWindow(v, DpUtil.dp2px(280), ViewGroup.LayoutParams.WRAP_CONTENT, true);
//        mLinkMicPopWindow.setBackgroundDrawable(new ColorDrawable());
//        mLinkMicPopWindow.setOutsideTouchable(false);
//        mLinkMicPopWindow.setFocusable(false);
//        mLinkMicPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                if (mMyCountdown != null) {
//                    mMyCountdown.stop();
//                }
//                if (mAcceptLinkMic) {
//                } else {
//                    if (mLinkMicWaitCount == 0) {
//                        SocketLinkMicUtil.anchorNotResponse(mSocketClient, mApplyUid);
//                    } else {
//                    }
//                    mApplyUid = null;
//                }
//                mIsLinkMicDialogShow = false;
//                mLinkMicWaitText = null;
//                mLinkMicPopWindow = null;
//            }
//        });
//        mLinkMicPopWindow.showAtLocation(mRoot, Gravity.CENTER, 0, 0);
//        if (mMyCountdown == null) {
//            mMyCountdown = new MyCountdown();
//            mMyCountdown.setCallback(new MyCountdown.ActionListener2() {
//                @Override
//                public void onTimeSecondChanged(int secondCount) {
//                    mLinkMicWaitCount = secondCount;
//                    if (mLinkMicWaitText != null) {
//                        mLinkMicWaitText.setText(mLinkMicWaitString + "(" + secondCount + "s)...");
//                    }
//                }
//
//                @Override
//                public void onTimeChanged(String timeStr) {
//
//                }
//
//                @Override
//                public void onTimeEnd() {
//                    if (mLinkMicPopWindow != null) {
//                        mLinkMicPopWindow.dismiss();
//                    }
//                }
//            });
//        }
//        mMyCountdown.setTotalSecond(LINK_MIC_COUNT_MAX).start();
//    }
    public void onAcceptGuest(UserBean u) {
        if(mIsAnchor) {
            if (((LiveActivity) mContext).isGamePlaying()) {
                ToastUtil.show(com.livestreaming.common.R.string.live_game_cannot_link_mic);
                SocketLinkMicUtil.anchorRefuseLinkMic(mSocketClient, mApplyUid);
                return;
            }
            if (((LiveActivity) mContext).isLinkMicAnchor()) {
                ToastUtil.show(com.livestreaming.common.R.string.live_link_mic_cannot_link_2);
                return;
            }
            LiveHttpUtil.getGuests(CommonAppConfig.getInstance().getUid(), ((LiveAnchorActivity)mContext).mStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (info.length > 0) {
                        JSONArray array = JSON.parseArray(info[0]);
                        if (array.size() < 4) {
                            mLinkMicUid = u.getId();
                            Guests.add(u);
                            SocketLinkMicUtil.anchorAcceptLinkMic(mSocketClient, u.getId());
                        } else {
                            mIsLinkMic = true;
                            ToastUtil.show(com.livestreaming.common.R.string.no_places);
                        }
                    }
                }
            });
        }
    }

    public void onRejectGuest(LiveGestBean bean) {
        SocketLinkMicUtil.anchorRefuseLinkMic(mSocketClient, "" + bean.getUser_id());
    }

    /**
     * 主播拒绝连麦申请
     */
    private void anchorRefuseLinkMicApply() {
        if (mLinkMicPopWindow != null) {
            mLinkMicPopWindow.dismiss();
        }
    }

    /**
     * 主播接受连麦申请
     */
    private void anchorAcceptLinkMicApply() {
        if (((LiveAnchorActivity) mContext).isBgmPlaying()) {
            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.link_mic_close_bgm), new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    ((LiveAnchorActivity) mContext).stopBgm();
                    mAcceptLinkMic = true;
                    if (mLinkMicPopWindow != null) {
                        mLinkMicPopWindow.dismiss();
                    }
                }
            });
        } else {
            mAcceptLinkMic = true;
            if (mLinkMicPopWindow != null) {
                mLinkMicPopWindow.dismiss();
            }
        }
    }

    /**
     * 主播断开 已连麦观众 的连麦
     */
    private void anchorCloseLinkMic() {
        SocketLinkMicUtil.anchorCloseLinkMic(mSocketClient, mLinkMicUid, mLinkMicName);
    }

    public void release() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_TX_LINK_MIC_ACC_URL);
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LINK_MIC_STREAM);
        LiveHttpUtil.cancel(LiveHttpConsts.LINK_MIC_SHOW_VIDEO);
        if(mLinkMicUidList!=null){
            mLinkMicUidList.clear();
        }
        if (mMyCountdown != null) {
            mMyCountdown.release();
        }
        mMyCountdown = null;
        mSocketClient = null;
        if (mLiveLinkMicPushViewHolder != null) {
            mLiveLinkMicPushViewHolder.release();
        }
        if (mLiveLinkMicPlayViewHolder != null) {
            mLiveLinkMicPlayViewHolder.release();
        }
        mLiveLinkMicPushViewHolder = null;
        mLiveLinkMicPlayViewHolder = null;

    }

    public void pause() {
        mPaused = true;
        if (mLiveLinkMicPushViewHolder != null) {
            mLiveLinkMicPushViewHolder.pause();
        }
        if (mLiveLinkMicPlayViewHolder != null) {
            mLiveLinkMicPlayViewHolder.pause();
        }
    }

    public void resume() {
        if (mPaused) {
            mPaused = false;
            if (mLiveLinkMicPushViewHolder != null) {
                mLiveLinkMicPushViewHolder.resume();
            }
            if (mLiveLinkMicPlayViewHolder != null) {
                mLiveLinkMicPlayViewHolder.resume();
            }
        }
    }

    public boolean isLinkMic() {
        return mIsLinkMic;
    }


    public void clearData() {
        LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LINK_MIC_ENABLE);
        mIsLinkMic = false;
        mIsLinkMicDialogShow = false;
        mAcceptLinkMic = false;
        mLastApplyLinkMicTime = 0;
        mApplyUid = null;
        mLinkMicUid = null;
        mLinkMicName = null;
        mLinkMicPopWindow = null;
        if (mMyCountdown != null) {
            mMyCountdown.release();
        }
        if (mLiveLinkMicPlayViewHolder != null) {
            mLiveLinkMicPlayViewHolder.release();
            mLiveLinkMicPlayViewHolder.removeFromParent();
        }
        mLiveLinkMicPlayViewHolder = null;
        if (mLiveLinkMicPushViewHolder != null) {
            mLiveLinkMicPushViewHolder.release();
            mLiveLinkMicPushViewHolder.removeFromParent();
        }
        mLiveLinkMicPushViewHolder = null;
    }

    public void onAnchorCloseAudiance(String touid, String name) {

        ((LiveAudienceActivity) mContext).toggleLinkMicPushAgora(false, touid);

    }

    public void removeFromGuestList(int uid) {
        int index=mLinkMicUidList.indexOf(""+uid);
        if(index>0&&index<mLinkMicUidList.size()) {
            mLinkMicUidList.remove(uid);
        }
    }
}
