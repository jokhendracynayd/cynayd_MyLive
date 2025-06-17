package com.livestreaming.live.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.adapter.LiveGuestsApplyUpAdapter;
import com.livestreaming.live.bean.LiveGestBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.ArrayList;

/**
 * 语音聊天室 观众申请上麦
 */
public class LiveGuestsApplyUpFragment extends AbsDialogFragment implements LiveGuestsApplyUpAdapter.ActionListener {

    private TextView mTitle;
    private TextView mBtnApply;
    private String mStream;
    private boolean mIsAnchor;
    private boolean isAdmin = false;
    private boolean mApply;
    private ArrayList<LiveGestBean> gesttUserList = new ArrayList<>();
    private ActionListener actionListener;

    public void setActionListener(ActionListener listener){
        actionListener=listener;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_voice_apply;
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
        params.height = DpUtil.dp2px(340);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mStream = bundle.getString(Constants.LIVE_STREAM);
        mIsAnchor = bundle.getBoolean(Constants.ANCHOR);
        isAdmin = bundle.getBoolean("Admin", false);
        mTitle = findViewById(R.id.title);
        if (mIsAnchor) {
            findViewById(R.id.group_bottom).setVisibility(View.GONE);
        } else {
            mBtnApply = findViewById(R.id.btn_apply);
        }
        LiveHttpUtil.getGuestsRequests(CommonAppConfig.getInstance().getUid(), mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {

                if (code == 0 && info.length > 0) {
                    gesttUserList.clear();
                    JSONArray array = JSON.parseArray(info[0]);
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        LiveGestBean gestBean = new LiveGestBean();
                        gestBean.setType(i);
                        gestBean.setAvatar(jsonObject.getString("avatar"));
                        gestBean.setUser_id(Integer.parseInt(jsonObject.getString("user_id")));
                        gestBean.setStream(jsonObject.getString("stream"));
                        gestBean.setCam_type(jsonObject.getInteger("cam_type"));
                        gestBean.setMic_type(jsonObject.getInteger("mic_type"));
                        gestBean.setPosition(jsonObject.getInteger("position"));
                        gestBean.setUserName(jsonObject.getString("guest_name"));
                        gesttUserList.add(gestBean);
                    }
                    if (mTitle != null) {
                        mTitle.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.a_044), gesttUserList.size()));
                    }
                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    if (recyclerView != null) {
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                        LiveGuestsApplyUpAdapter adapter = new LiveGuestsApplyUpAdapter(mContext, gesttUserList);
                        adapter.setActionListener(LiveGuestsApplyUpFragment.this);
                        recyclerView.setAdapter(adapter);
                    }

                }
            }
        });


    }


    /**
     * 主播同意/拒绝用户上麦申请
     *
     * @param isAgree 0 拒绝 1 同意
     */
    @Override
    public void onAgreeUpMicClick(final LiveGestBean bean, int isAgree) {
        if (isAgree == 0) {
            LiveHttpUtil.deleteGest(CommonAppConfig.getInstance().getUid(), mStream, "" + bean.getUser_id(), new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        actionListener.onReject(bean);
                        dismiss();
                    }
                    ToastUtil.show(msg);
                }
            });

        } else {
            actionListener.onAccept(bean);
            dismiss();
        }
    }

    @Override
    public void onShowUserDialog(String uid) {
        showUserDialog(uid);
    }

    public void showUserDialog(String toUid) {
        if (!TextUtils.isEmpty(toUid)) {
            LiveUserDialogFragment fragment = new LiveUserDialogFragment();
            fragment.whenUserpClick = new LiveUserDialogFragment.WhenUserpClick() {
                @Override
                public void onClick(String uid) {
                    RouteUtil.forwardUserHome(mContext, toUid, true, CommonAppConfig.getInstance().getUid());
                }
            };
            Bundle bundle = new Bundle();
            bundle.putString(com.livestreaming.common.Constants.LIVE_UID, CommonAppConfig.getInstance().getUid());
            bundle.putString(com.livestreaming.common.Constants.TO_UID, toUid);
            fragment.setArguments(bundle);
            fragment.show(((LiveActivity) mContext).getSupportFragmentManager(), "LiveUserDialogFragment");
        }
    }

    @Override
    public void onDestroy() {
        mContext = null;
        LiveHttpUtil.cancel(LiveHttpConsts.APPLY_VOICE_LIVE_MIC);
        LiveHttpUtil.cancel(LiveHttpConsts.CANCEL_VOICE_LIVE_MIC);
        LiveHttpUtil.cancel(LiveHttpConsts.HANDLE_VOICE_MIC_APPLY);
        super.onDestroy();
    }

    public interface ActionListener{
        public void onReject(LiveGestBean liveGestBean);
        public void onAccept(LiveGestBean liveGestBean);
    }

}
