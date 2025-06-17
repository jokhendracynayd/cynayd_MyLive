package com.livestreaming.live.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.im.interfaces.ChatRoomActionListener;
import com.livestreaming.im.views.ChatRoomDialogViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;

/**
 * Created by cxf on 2018/10/24.
 * 直播间私信聊天窗口
 */

public class LiveChatRoomDialogFragment extends AbsDialogFragment {

    private ChatRoomDialogViewHolder mChatRoomViewHolder;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_chat_room;
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
        window.setWindowAnimations(com.livestreaming.common.R.style.leftToRightAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((LiveActivity) mContext).setChatRoomOpened(this, true);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        UserBean userBean = bundle.getParcelable(Constants.USER_BEAN);
        if (userBean == null) {
            return;
        }
        boolean following = bundle.getBoolean(Constants.FOLLOW);
        mChatRoomViewHolder = new ChatRoomDialogViewHolder(mContext, (ViewGroup) mRootView, userBean, following);
        mChatRoomViewHolder.setActionListener(new ChatRoomActionListener() {
            @Override
            public void onCloseClick() {
                dismiss();
            }


            @Override
            public void onChooseImageClick() {
            }

            @Override
            public void onCameraClick() {
            }

            @Override
            public void onVoiceInputClick() {
            }

            @Override
            public void onLocationClick() {
            }


            @Override
            public ViewGroup getImageParentView() {
                return ((LiveActivity) mContext).getPageContainer();
            }

        });
        mChatRoomViewHolder.addToParent();
        mChatRoomViewHolder.loadData();
    }


    @Override
    public void onDestroy() {
        ((LiveActivity) mContext).setChatRoomOpened(null, false);
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.release();
        }
        super.onDestroy();
    }


    public void onKeyBoardHeightChanged(int keyboardHeight) {
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.onKeyBoardHeightChanged(keyboardHeight);
        }
    }




    @Override
    public void onPause() {
        super.onPause();
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.onResume();
        }
    }
}
