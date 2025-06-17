package com.livestreaming.im.views;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.FollowEvent;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.common.views.InputViewHolder;
import com.livestreaming.im.R;
import com.livestreaming.im.adapter.ImRoomAdapter;
import com.livestreaming.im.bean.ImMessageBean;
import com.livestreaming.im.custom.MyImageView;
import com.livestreaming.im.dialog.ChatImageDialog;
import com.livestreaming.im.event.ImMessageRevokeEvent;
import com.livestreaming.im.http.ImHttpUtil;
import com.livestreaming.im.interfaces.ChatRoomActionListener;
import com.livestreaming.im.utils.ImDateUtil;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.im.utils.VoiceMediaPlayerUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;


/**
 * Created by cxf on 2018/10/24.
 */

public class ChatRoomDialogViewHolder extends AbsViewHolder implements
        View.OnClickListener, ImRoomAdapter.ActionListener {

    private RecyclerView mRecyclerView;
    private ImRoomAdapter mAdapter;
    private TextView mTitleView;
    private UserBean mUserBean;
    private String mToUid;
    private ChatRoomActionListener mActionListener;
    private ChatImageDialog mChatImageDialog;//图片预览弹窗
    private boolean mFollowing;
    private View mFollowGroup;
    private VoiceMediaPlayerUtil mVoiceMediaPlayerUtil;
    private InputViewHolder mInputViewHolder;

    public ChatRoomDialogViewHolder(Context context, ViewGroup parentView, UserBean userBean, boolean following) {
        super(context, parentView, userBean, following);
    }

    @Override
    protected void processArguments(Object... args) {
        mUserBean = (UserBean) args[0];
        mFollowing = (boolean) args[1];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_chat_room_2;
    }

    @Override
    public void init() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mTitleView = (TextView) findViewById(R.id.titleView);
        mFollowGroup = findViewById(R.id.btn_follow_group);
        if (!mFollowing) {
            mFollowGroup.setVisibility(View.VISIBLE);
            mFollowGroup.findViewById(R.id.btn_close_follow).setOnClickListener(this);
            mFollowGroup.findViewById(R.id.btn_follow).setOnClickListener(this);
        }
        findViewById(R.id.btn_back).setOnClickListener(this);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mInputViewHolder != null) {
                        return mInputViewHolder.hideKeyBoardFaceMore();
                    }
                }
                return false;
            }
        });
        EventBus.getDefault().register(this);
        mInputViewHolder = new InputViewHolder(mContext,
                (ViewGroup) findViewById(R.id.input_container),
                true, com.livestreaming.common.R.layout.view_input_top_live, com.livestreaming.common.R.layout.view_input_face
        );
        mInputViewHolder.addToParent();
        mInputViewHolder.setActionListener(new InputViewHolder.ActionListener() {
            @Override
            public void onSendClick(String text) {
                sendText(text);
            }
        });
    }


    public void loadData() {
        if (mUserBean == null) {
            return;
        }
        mToUid = mUserBean.getId();
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        mTitleView.setText(mUserBean.getUserNiceName());
        mAdapter = new ImRoomAdapter(mContext, mToUid, mUserBean);
        mAdapter.setActionListener(this);
        mRecyclerView.setAdapter(mAdapter);
        ImMessageUtil.getInstance().getMessageList(mToUid, new CommonCallback<List<ImMessageBean>>() {
            @Override
            public void callback(List<ImMessageBean> list) {
                if (list.size() > 0) {
                    ImMessageBean bean;
                    for (int i = 0; i < list.size(); i++) {
                        bean = list.get(i);
                        long msgTime = bean.getTimestamp();
                        if (!ImDateUtil.isCloseEnough(msgTime, mAdapter.getLastMsgTime())) {
                            bean.setShowTimeString(ImDateUtil.getTimestampString(msgTime));
                            mAdapter.setLastMsgTime(msgTime);
                        }
                    }
                }
                mAdapter.setList(list);
                mAdapter.scrollToBottom();
            }
        });
    }


    public void setActionListener(ChatRoomActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void onKeyBoardHeightChanged(int keyboardHeight) {
        if (mAdapter != null) {
            mAdapter.scrollToBottom();
        }
        if (mInputViewHolder != null) {
            mInputViewHolder.onKeyBoardHeightChanged(keyboardHeight);
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_back) {
            back();

        } else if (i == com.livestreaming.common.R.id.btn_face) {

        } else if (i == R.id.btn_close_follow) {
            closeFollow();

        } else if (i == R.id.btn_follow) {
            follow();

        }
    }

    /**
     * 关闭关注提示
     */
    private void closeFollow() {
        if (mFollowGroup != null && mFollowGroup.getVisibility() == View.VISIBLE) {
            mFollowGroup.setVisibility(View.GONE);
        }
    }

    /**
     * 关注
     */
    private void follow() {
        CommonHttpUtil.setAttention(mToUid, null);
    }

    /**
     * 返回
     */
    public void back() {
        if (mInputViewHolder != null && mInputViewHolder.hideKeyBoardFaceMore()) {
            return;
        }
        if (mActionListener != null) {
            mActionListener.onCloseClick();
        }
    }


    /**
     * 释放资源
     */
    public void release() {
        if (mInputViewHolder != null) {
            mInputViewHolder.hideKeyBoard();
        }
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.destroy();
        }
        mVoiceMediaPlayerUtil = null;
        if (mAdapter != null) {
            mAdapter.release();
        }
        EventBus.getDefault().unregister(this);
        mActionListener = null;
        if (mChatImageDialog != null) {
            mChatImageDialog.dismiss();
        }
        mChatImageDialog = null;
    }

    /**
     * 点击图片的回调，显示图片
     */
    @Override
    public void onImageClick(MyImageView imageView, int x, int y) {
        if (mAdapter == null || imageView == null) {
            return;
        }
        ImMessageBean imMessageBean = imageView.getImMessageBean();
        if (imMessageBean != null) {
            mChatImageDialog = new ChatImageDialog();
            mChatImageDialog.setImageInfo(mAdapter.getChatImageBean(imMessageBean), x, y, imageView.getWidth(), imageView.getHeight(), imageView.getDrawable());
            mChatImageDialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "ChatImageDialog2");
        }
    }


    /**
     * 点击语音消息的回调，播放语音
     */
    @Override
    public void onVoiceStartPlay(File voiceFile) {
        if (mVoiceMediaPlayerUtil == null) {
            mVoiceMediaPlayerUtil = new VoiceMediaPlayerUtil(mContext);
            mVoiceMediaPlayerUtil.setActionListener(new VoiceMediaPlayerUtil.ActionListener() {
                @Override
                public void onPlayEnd() {
                    if (mAdapter != null) {
                        mAdapter.stopVoiceAnim();
                    }
                }
            });
        }
        mVoiceMediaPlayerUtil.startPlay(voiceFile.getAbsolutePath());
    }

    /**
     * 点击语音消息的回调，停止播放语音
     */
    @Override
    public void onVoiceStopPlay() {
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.stopPlay();
        }
    }

    /**************************************************************************************************/
    /*********************************以上是处理界面逻辑，以下是处理消息逻辑***********************************/
    /**************************************************************************************************/


    /**
     * 收到消息的回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImMessageBean(ImMessageBean bean) {
        if (!bean.getSenderId().equals(mToUid)) {
            return;
        }
        ImMessageUtil.getInstance().markConversationAsRead(mToUid);
        if (mAdapter != null) {
            mAdapter.insertItem(bean);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (e.getToUid().equals(mToUid)) {
            if (mFollowGroup != null) {
                if (e.getIsAttention() == 1) {
                    if (mFollowGroup.getVisibility() == View.VISIBLE) {
                        mFollowGroup.setVisibility(View.GONE);
                    }
                    ToastUtil.show(com.livestreaming.common.R.string.im_follow_tip_2);
                } else {
                    if (mFollowGroup.getVisibility() != View.VISIBLE) {
                        mFollowGroup.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    /**
     * 发消息前，检测是否能够发消息
     */
    private void checkSendMessage(final CommonCallback<CheckSendMsgResult> callback) {
        ImHttpUtil.checkBlack(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        int t2u = obj.getIntValue("t2u");
                        if (1 == t2u) {//被拉黑
                            ToastUtil.show(com.livestreaming.common.R.string.im_you_are_blacked);
                            if (callback != null) {
                                callback.callback(new CheckSendMsgResult(false));
                            }
                        } else {
                            if (callback != null) {
                                callback.callback(new CheckSendMsgResult(true));
                            }
                        }
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 发送文本信息
     */
    public void sendText(final String content) {
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(com.livestreaming.common.R.string.content_empty);
            return;
        }
        checkSendMessage(new CommonCallback<CheckSendMsgResult>() {
            @Override
            public void callback(CheckSendMsgResult checkResult) {
                if (checkResult.isCanSend()) {
                    ImMessageUtil.getInstance().sendC2CTextMessage(content, mToUid, new ImMessageUtil.SendMsgCallback() {
                        @Override
                        public void onStart(ImMessageBean imMsgBean) {
                            if (mInputViewHolder != null) {
                                mInputViewHolder.clearEditText();
                            }
                            if (mAdapter != null) {
                                mAdapter.insertItem(imMsgBean);
                            }
                        }

                        @Override
                        public void onProgress(ImMessageBean imMsgBean, int progress) {

                        }

                        @Override
                        public void onEnd(ImMessageBean imMsgBean, boolean success) {
                            if (success) {
                                if (mAdapter != null) {
                                    mAdapter.updateSendStatus(imMsgBean, ImMessageBean.STATUS_SEND_SUCC);
                                }
                            } else {
                                if (mAdapter != null) {
                                    mAdapter.updateSendStatus(imMsgBean, ImMessageBean.STATUS_SEND_FAIL);
                                }
                                ToastUtil.show(com.livestreaming.common.R.string.im_msg_send_failed);
                            }
                        }
                    });
                }
            }
        });
    }

    public void onPause() {
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.pausePlay();
        }
    }

    public void onResume() {
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.resumePlay();
        }
    }

    /**
     * 撤回消息的回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImMessagePromptEvent(ImMessageRevokeEvent e) {
        String toUid = e.getToUid();
        if (TextUtils.isEmpty(toUid) || !toUid.equals(mToUid)) {
            return;
        }
        if (mChatImageDialog != null) {
            mChatImageDialog.dismiss();
        }
        mChatImageDialog = null;
        if (mAdapter != null) {
            mAdapter.onPromptMessage(e.getMsgId());
        }
    }

}
