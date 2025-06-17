package com.livestreaming.live.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;

public class LiveCheckDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private int mLiveType;
    private String mLiveTypeVal;
    private EditText mEditText;
    private ActionListener mActionListener;

    @Override
    protected int getLayoutId() {
        if (mLiveType == Constants.LIVE_TYPE_PWD) {
            return R.layout.dialog_live_check_pwd;
        }
        return R.layout.dialog_live_check_pay;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return false;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(250);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }


    public void setLiveType(int liveType, String liveTypeVal) {
        mLiveType = liveType;
        mLiveTypeVal = liveTypeVal;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });
        }
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        TextView tip = findViewById(R.id.tip);
        if (mLiveType == Constants.LIVE_TYPE_PWD) {
            mEditText = findViewById(R.id.edit);
            tip.setText(WordUtil.getString(com.livestreaming.common.R.string.live_room_type_pwd));
        } else {
            String coinName = CommonAppConfig.getInstance().getCoinName();
            if (mLiveType == Constants.LIVE_TYPE_PAY) {
                tip.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.live_room_type_pay), mLiveTypeVal, coinName));
            } else if (mLiveType == Constants.LIVE_TYPE_TIME) {
                tip.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.live_room_type_time), mLiveTypeVal, coinName));
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (mActionListener == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_close || i == R.id.btn_cancel) {
            dismiss();
        } else if (i == R.id.btn_confirm) {
            if (mLiveType == Constants.LIVE_TYPE_PWD) {
                if (mEditText != null) {
                    String content = mEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.live_input_password));
                        return;
                    }
                    String password = MD5Util.getMD5(content);
                    if (!TextUtils.isEmpty(password) && password.equalsIgnoreCase(mLiveTypeVal)) {
                        dismiss();
                        mActionListener.onConfirmClick();
                    } else {
                        ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.live_password_error));
                    }
                }
            } else {
                dismiss();
                mActionListener.onConfirmClick();
            }
        }

    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }


    public interface ActionListener {
        void onConfirmClick();
    }


}
