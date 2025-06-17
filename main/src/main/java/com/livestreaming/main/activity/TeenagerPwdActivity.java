package com.livestreaming.main.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.custom.SplitEditText;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

/**
 * Created by 云豹科技 on 2022/6/6.
 * 青少年模式设置密码
 */
public class TeenagerPwdActivity extends AbsActivity {

    public static void forward(Context context, boolean isOpen, boolean hasSetPwd, int type) {
        Intent intent = new Intent(context, TeenagerPwdActivity.class);
        intent.putExtra("isOpen", isOpen);
        intent.putExtra("hasSetPwd", hasSetPwd);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    private InputMethodManager imm;
    private boolean mIsOpen;//是否开了青少年模式
    private boolean mHasSetPwd;//是否设置过密码
    private int mType;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_teenager_pwd;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mIsOpen = intent.getBooleanExtra("isOpen", false);
        mHasSetPwd = intent.getBooleanExtra("hasSetPwd", false);
        mType = intent.getIntExtra("type", 0);
        setTitle(WordUtil.getString(mHasSetPwd ? com.livestreaming.common.R.string.a_129 : com.livestreaming.common.R.string.a_127));
        TextView tip = findViewById(R.id.tip);
        tip.setText(mHasSetPwd ? com.livestreaming.common.R.string.login_input_pwd : com.livestreaming.common.R.string.a_128);
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        final SplitEditText editText = findViewById(R.id.edit);
        editText.setOnInputListener(new SplitEditText.OnInputListener() {

            @Override
            public void onInputFinished(String content) {
                if (mIsOpen) {
                    MainHttpUtil.closeTeenager(content, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                CommonAppConfig.getInstance().setTeenagerType(false);
                                restartApp();
                            }
                            ToastUtil.show(msg);
                        }
                    });
                } else {
                    MainHttpUtil.openTeenager(content, mType, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                CommonAppConfig.getInstance().setTeenagerType(true);
                                if (mHasSetPwd) {
                                    ToastUtil.show(msg);
                                    restartApp();
                                } else {
                                    new DialogUitl.Builder(mContext)
                                            .setContent(WordUtil.getString(com.livestreaming.common.R.string.a_130))
                                            .setCancelString(WordUtil.getString(com.livestreaming.common.R.string.a_139))
                                            .setCancelable(false)
                                            .setBackgroundDimEnabled(false)
                                            .setClickCallback(new DialogUitl.SimpleCallback2() {
                                                @Override
                                                public void onCancelClick() {
                                                    if (imm != null) {
                                                        imm.hideSoftInputFromWindow(findViewById(R.id.edit).getWindowToken(), 0);
                                                    }
                                                    TeenagerPwdActivity.forward(mContext, false, true, 0);
                                                    finish();
                                                }

                                                @Override
                                                public void onConfirmClick(Dialog dialog, String content) {
                                                    restartApp();
                                                }
                                            })
                                            .build().show();

                                }
                            } else {
                                ToastUtil.show(msg);
                            }

                        }
                    });
                }
            }

            @Override
            public void onInputChanged(String text) {

            }
        });
        View wrap = findViewById(R.id.wrap);
        wrap.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });

    }


    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.OPEN_TEENAGER);
        MainHttpUtil.cancel(MainHttpConsts.CLOSE_TEENAGER);
        super.onDestroy();
    }

    public void restartApp() {
        if (imm != null) {
            imm.hideSoftInputFromWindow(findViewById(R.id.edit).getWindowToken(), 0);
        }
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(Constants.EXIT, false);
        startActivity(intent);
    }


}
