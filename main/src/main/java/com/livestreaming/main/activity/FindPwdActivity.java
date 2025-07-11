package com.livestreaming.main.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.LoginChangeEvent;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.event.RegSuccessEvent;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2018/9/25.
 */

public class FindPwdActivity extends AbsActivity {

    private EditText mEditPhone;
    private EditText mEditCode;
    private EditText mEditPwd1;
    private EditText mEditPwd2;
    private TextView mBtnCode;
    private View mBtnRegister;
    private Handler mHandler;
    private static final int TOTAL = 60;
    private int mCount = TOTAL;
    private String mGetCode;
    private String mGetCodeAgain;
    private Dialog mDialog;
    private boolean mFirstLogin;//是否是第一次登录
    private TextView mCountryCode;
    private boolean mUseCountryCode;//是否能选择国家代号
    private boolean mFromLogin;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_find_pwd;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.find_pwd_forget));
        Intent intent=getIntent();
        mUseCountryCode =intent .getBooleanExtra(Constants.TIP, false);
        mFromLogin=intent.getBooleanExtra(Constants.FROM_LOGIN,false);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditCode = (EditText) findViewById(R.id.edit_code);
        mEditPwd1 = (EditText) findViewById(R.id.edit_pwd_1);
        mEditPwd2 = (EditText) findViewById(R.id.edit_pwd_2);
        mBtnCode = (TextView) findViewById(R.id.btn_code);
        mBtnRegister = findViewById(R.id.btn_register);
        mGetCode = WordUtil.getString(com.livestreaming.common.R.string.reg_get_code);
        mGetCodeAgain = WordUtil.getString(com.livestreaming.common.R.string.reg_get_code_again);
        mCountryCode = findViewById(R.id.country_code);
        mEditPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (!TextUtils.isEmpty(s) && s.length() == 11) {
//                    mBtnCode.setEnabled(true);
//                } else {
//                    mBtnCode.setEnabled(false);
//                }
                changeEnable();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changeEnable();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mEditCode.addTextChangedListener(textWatcher);
        mEditPwd1.addTextChangedListener(textWatcher);
        mEditPwd2.addTextChangedListener(textWatcher);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mCount--;
                if (mCount > 0) {
                    mBtnCode.setText(mGetCodeAgain + "(" + mCount + "s)");
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                } else {
                    mBtnCode.setText(mGetCode);
                    mCount = TOTAL;
                    if (mBtnCode != null) {
                        mBtnCode.setEnabled(true);
                    }
                }
            }
        };
        mDialog = DialogUitl.loadingDialog(mContext);
        EventBus.getDefault().register(this);
    }

    private void changeEnable() {
        String phone = mEditPhone.getText().toString();
        String code = mEditCode.getText().toString();
        String pwd1 = mEditPwd1.getText().toString();
        String pwd2 = mEditPwd2.getText().toString();
        mBtnRegister.setEnabled(!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(code) && !TextUtils.isEmpty(pwd1) && !TextUtils.isEmpty(pwd2));
    }

    public void registerClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_code) {
            getCode();
        } else if (i == R.id.btn_register) {
            register();
        } else if (i == R.id.btn_choose_country) {
            if (mUseCountryCode) {
                startActivityForResult(new Intent(mContext, ChooseCountryActivity.class), 101);
            }
        }
    }

    /**
     * 获取验证码
     */
    private void getCode() {
        String phoneNum = mEditPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.show(com.livestreaming.common.R.string.reg_input_phone);
            mEditPhone.requestFocus();
            return;
        }
//        if (!ValidatePhoneUtil.validateMobileNumber(phoneNum)) {
//            mEditPhone.setError(WordUtil.getString(R.string.login_phone_error));
//            mEditPhone.requestFocus();
//            return;
//        }
        mEditCode.requestFocus();
        String countryCode = mCountryCode.getText().toString();
        if (!TextUtils.isEmpty(countryCode) && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1);
        }
        MainHttpUtil.getFindPwdCode(phoneNum, countryCode, mGetCodeCallback);
    }

    private HttpCallback mGetCodeCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                mBtnCode.setEnabled(false);
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(0);
                }
                if (!TextUtils.isEmpty(msg) && msg.contains("123456")) {
                    ToastUtil.show(msg);
                }
            } else {
                ToastUtil.show(msg);
            }

        }
    };

    /**
     * 注册并登陆
     */
    private void register() {
        final String phoneNum = mEditPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.show(com.livestreaming.common.R.string.reg_input_phone);
            mEditPhone.requestFocus();
            return;
        }
//        if (!ValidatePhoneUtil.validateMobileNumber(phoneNum)) {
//            mEditPhone.setError(WordUtil.getString(R.string.login_phone_error));
//            mEditPhone.requestFocus();
//            return;
//        }
        String code = mEditCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            ToastUtil.show(com.livestreaming.common.R.string.reg_input_code);
            mEditCode.requestFocus();
            return;
        }
        final String pwd = mEditPwd1.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            ToastUtil.show(com.livestreaming.common.R.string.reg_input_pwd_1);
            mEditPwd1.requestFocus();
            return;
        }
        String pwd2 = mEditPwd2.getText().toString().trim();
        if (TextUtils.isEmpty(pwd2)) {
            ToastUtil.show(com.livestreaming.common.R.string.reg_input_pwd_2);
            mEditPwd2.requestFocus();
            return;
        }
//        if (!pwd.equals(pwd2)) {
//            mEditPwd2.setError(WordUtil.getString(R.string.reg_pwd_error));
//            mEditPwd2.requestFocus();
//            return;
//        }
        if (mDialog != null) {
            mDialog.show();
        }
        String countryCode = mCountryCode.getText().toString();
        if (!TextUtils.isEmpty(countryCode) && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1);
        }
        MainHttpUtil.findPwd(phoneNum, pwd, pwd2, code, countryCode, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if(mFromLogin){
                        login(phoneNum, pwd);
                    }else{
                        CommonAppConfig.getInstance().clearLoginInfo();
                        //退出IM
                        ImMessageUtil.getInstance().logoutImClient();
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        ToastUtil.show(msg);
                    }
                } else {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    ToastUtil.show(msg);
                }
            }

            @Override
            public void onError() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
    }

    private void login(String phoneNum, String pwd) {
        String countryCode = mCountryCode.getText().toString();
        if (!TextUtils.isEmpty(countryCode) && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1);
        }
        MainHttpUtil.login(phoneNum, pwd, countryCode, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        String uid = obj.getString("id");
                        String token = obj.getString("token");
                        mFirstLogin = obj.getIntValue("isreg") == 1;
                        SpUtil.getInstance().setStringValue(SpUtil.TX_IM_USER_SIGN, obj.getString("usersign"));
                        CommonAppConfig.getInstance().setLoginInfo(uid, token, true);
                        getBaseUserInfo();
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public void onError() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
    }

    /**
     * 获取用户信息
     */
    private void getBaseUserInfo() {
        MainHttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
            @Override
            public void callback(UserBean bean) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if (bean != null) {
                    if (mFirstLogin) {
                        RecommendActivity.forward(mContext, mFirstLogin);
                    } else {
                        EventBus.getDefault().post(new LoginChangeEvent(true,mFirstLogin));
                    }
                    EventBus.getDefault().post(new RegSuccessEvent());
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegSuccessEvent(RegSuccessEvent e) {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(MainHttpConsts.GET_FIND_PWD_CODE);
        MainHttpUtil.cancel(MainHttpConsts.FIND_PWD);
        MainHttpUtil.cancel(MainHttpConsts.LOGIN);
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra(Constants.TO_NAME);
            if (mCountryCode != null) {
                mCountryCode.setText(StringUtil.contact("+", code));
            }
        }
    }
}
