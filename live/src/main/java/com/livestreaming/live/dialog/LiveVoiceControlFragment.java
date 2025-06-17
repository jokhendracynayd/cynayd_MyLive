package com.livestreaming.live.dialog;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.Constants;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.adapter.LiveVoiceControlAdapter;
import com.livestreaming.live.bean.LiveVoiceControlBean;
import com.livestreaming.live.event.LiveVoiceMicStatusEvent;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

public class LiveVoiceControlFragment extends AbsDialogFragment implements LiveVoiceControlAdapter.ActionListener {

    private String mStream;
    private LiveVoiceControlAdapter mAdapter;
    private boolean isAdmin=false;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_voice_control;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mStream = bundle.getString(Constants.LIVE_STREAM);
        isAdmin=bundle.getBoolean("Admin",false);
        if(isAdmin){
            String uid=bundle.getString("uid");
            String token= bundle.getString("token");
            LiveHttpUtil.getVoiceControlListAdmin(mStream,uid,token, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        List<LiveVoiceControlBean> list = JSON.parseArray(Arrays.toString(info), LiveVoiceControlBean.class);
                        RecyclerView recyclerView = findViewById(R.id.recyclerView);
                        if (recyclerView != null) {
                            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                            mAdapter = new LiveVoiceControlAdapter(mContext, list);
                            mAdapter.setActionListener(LiveVoiceControlFragment.this);
                            recyclerView.setAdapter(mAdapter);
                        }
                    }
                }
            });
        }else {
            LiveHttpUtil.getVoiceControlList(mStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        List<LiveVoiceControlBean> list = JSON.parseArray(Arrays.toString(info), LiveVoiceControlBean.class);
                        RecyclerView recyclerView = findViewById(R.id.recyclerView);
                        if (recyclerView != null) {
                            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                            mAdapter = new LiveVoiceControlAdapter(mContext, list);
                            mAdapter.setActionListener(LiveVoiceControlFragment.this);
                            recyclerView.setAdapter(mAdapter);
                        }
                    }
                }
            });
        }

    }

    @Override
    public void onControlClick(final LiveVoiceControlBean bean) {
        if(isAdmin) {
            ((LiveAudienceActivity) mContext).onAdminControlClick(bean);
            ToastUtil.show(getString(com.livestreaming.common.R.string.done));
            dismiss();
        }else {
            if (bean.getStatus() == Constants.VOICE_CTRL_EMPTY) {//无人
                LiveHttpUtil.banEmptyMicPosition(mStream, bean.getPosition(), 0, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (mContext != null) {
                                ((LiveAnchorActivity) mContext).controlMicPosition(bean.getUid(), bean.getPosition(), Constants.VOICE_CTRL_BAN);
                            }
                        }
                        ToastUtil.show(msg);
                    }
                });
            } else if (bean.getStatus() == Constants.VOICE_CTRL_BAN) {//禁麦
                LiveHttpUtil.banEmptyMicPosition(mStream, bean.getPosition(), 1, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (mContext != null) {
                                ((LiveAnchorActivity) mContext).controlMicPosition(bean.getUid(), bean.getPosition(), Constants.VOICE_CTRL_EMPTY);
                            }
                        }
                        ToastUtil.show(msg);
                    }
                });
            } else if (bean.getStatus() == Constants.VOICE_CTRL_OPEN || bean.getStatus() == Constants.VOICE_CTRL_CLOSE) {//开麦闭麦切换
                if (mContext != null) {
                    ((LiveActivity) mContext).changeVoiceMicOpen(bean.getUid());
                }
            }
        }
    }

    @Override
    public void onDownMicClick(final LiveVoiceControlBean bean) {
        if (mContext != null) {
            if(isAdmin){
                ((LiveAudienceActivity) mContext).onAdminControlCloseVoiceMic(bean);
                ToastUtil.show(getString(com.livestreaming.common.R.string.done));
                dismiss();
            }else {
                ((LiveActivity) mContext).closeUserVoiceMic(bean.getUid(), 1);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveVoiceMicStatusEvent(LiveVoiceMicStatusEvent e) {
        if (mAdapter != null) {
            mAdapter.changeStatus(e.getPosition(), e.getStatus());
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mContext = null;
        LiveHttpUtil.cancel(LiveHttpConsts.GET_VOICE_CONTROL_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.BAN_EMPTY_MIC_POSITION);
        super.onDestroy();
    }
}
