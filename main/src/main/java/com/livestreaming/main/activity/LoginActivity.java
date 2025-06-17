package com.livestreaming.main.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.LoginChangeEvent;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.dialog.LoginForbiddenDialogFragment;
import com.livestreaming.main.event.RegSuccessEvent;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by cxf on 2018/9/17.
 */
public class LoginActivity extends AbsActivity {

    private EditText mEditPhone;
    private EditText mEditPwd;
    private View mBtnLogin;
    private RecyclerView mRecyclerView;
    //private MobLoginUtil mLoginUtil;
    private boolean mFirstLogin;//是否是第一次登录
    private String mLoginType = Constants.MOB_PHONE;//登录方式
    private TextView mCountryCode;
    private boolean mUseCountryCode;//是否能选择国家代号
    private ImageView mLoginCheckBox;
    private boolean mChecked;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private ImageView mImgAppName;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return false;
    }





    @Override
    protected void main() {

        ImageView appIcon = findViewById(R.id.app_icon);
        appIcon.setImageResource(CommonAppConfig.getInstance().getAppIconRes());
//        TextView appName = findViewById(R.id.app_name);
//        appName.setText(CommonAppConfig.getInstance().getAppName());

        mImgAppName = findViewById(R.id.app_name);
        mCheckedDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_login_check_1);
        mUnCheckedDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_login_check_0);
        mLoginCheckBox = findViewById(R.id.btn_login_check);
        mLoginCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChecked = !mChecked;
                mLoginCheckBox.setImageDrawable(mChecked ? mCheckedDrawable : mUnCheckedDrawable);
            }
        });
        mCountryCode = findViewById(R.id.country_code);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditPwd = (EditText) findViewById(R.id.edit_pwd);
        mBtnLogin = findViewById(R.id.btn_login);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = mEditPhone.getText().toString();
                String pwd = mEditPwd.getText().toString();
                mBtnLogin.setEnabled(!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(pwd));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mEditPhone.addTextChangedListener(textWatcher);
        mEditPwd.addTextChangedListener(textWatcher);
        String tip = getIntent().getStringExtra(Constants.TIP);
        if (!TextUtils.isEmpty(tip)) {
            DialogUitl.showSimpleTipDialog(mContext, tip);
        }
        EventBus.getDefault().register(this);
        MainHttpUtil.getLoginInfo(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mImgAppName != null) {
                        ImgLoader.display(mContext, obj.getString("login_img"), mImgAppName);
                    }
                    mUseCountryCode = obj.getIntValue("sendcode_type") == 1;
                    String[] loginTypeArray = JSON.parseObject(obj.getString("login_type"), String[].class);
                    /*if (loginTypeArray != null && loginTypeArray.length > 0) {
                        //List<MobBean> list = MobBean.getLoginTypeList(loginTypeArray);
                        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 20, 0);
                        decoration.setOnlySetItemOffsetsButNoDraw(true);
                        mRecyclerView.addItemDecoration(decoration);
                        LoginTypeAdapter adapter = new LoginTypeAdapter(mContext, list);
                        adapter.setOnItemClickListener(LoginActivity.this);
                        mRecyclerView.setAdapter(adapter);
                        mLoginUtil = new MobLoginUtil();
                    }*/
                    TextView loginTipTextView = findViewById(R.id.login_tip);
                    if (loginTipTextView != null) {
                        JSONObject loginInfo = obj.getJSONObject("login_alert");
                        String loginTip = loginInfo.getString("login_title");
                        if (TextUtils.isEmpty(loginTip)) {
                            return;
                        }
                        SpannableString spannableString = new SpannableString(loginTip);
                        JSONArray msgArray = JSON.parseArray(loginInfo.getString("message"));
                        for (int i = 0, size = msgArray.size(); i < size; i++) {
                            final JSONObject msgItem = msgArray.getJSONObject(i);
                            String title = msgItem.getString("title");
                            int startIndex = loginTip.indexOf(title);
                            if (startIndex >= 0) {
                                ClickableSpan clickableSpan = new ClickableSpan() {

                                    @Override
                                    public void updateDrawState(TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setColor(0xff2197DA);
                                        ds.setUnderlineText(false);
                                    }

                                    @Override
                                    public void onClick(View widget) {
                                        WebViewActivity.forward(mContext, msgItem.getString("url"));
                                    }
                                };
                                int endIndex = startIndex + title.length();
                                spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        loginTipTextView.setText(spannableString);
                        loginTipTextView.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
                        loginTipTextView.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
                    }
                }
            }
        });
    }

    public void loginClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_login) {
            login();

        } else if (i == R.id.btn_register) {
            register();

        } else if (i == R.id.btn_forget_pwd) {
            forgetPwd();
        } else if (i == R.id.btn_choose_country) {
            chooseCountryCode();
        }
    }

    //注册
    private void register() {
        Intent intent = new Intent(mContext, RegisterActivity.class);
        intent.putExtra(Constants.TIP, mUseCountryCode);
        startActivity(intent);
    }

    //忘记密码
    private void forgetPwd() {
        Intent intent = new Intent(mContext, FindPwdActivity.class);
        intent.putExtra(Constants.FROM_LOGIN, true);
        intent.putExtra(Constants.TIP, mUseCountryCode);
        startActivity(intent);
    }

    //选择国家代号
    private void chooseCountryCode() {
        if (mUseCountryCode) {
            startActivityForResult(new Intent(mContext, ChooseCountryActivity.class), 101);
        }
    }


    //手机号密码登录
    private void login() {
        if (!mChecked) {
            ToastUtil.show(com.livestreaming.common.R.string.login_check_tip);
            return;
        }
        String phoneNum = mEditPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.show(com.livestreaming.common.R.string.login_input_phone);
            mEditPhone.requestFocus();
            return;
        }
        String pwd = mEditPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            ToastUtil.show(com.livestreaming.common.R.string.login_input_pwd);
            mEditPwd.requestFocus();
            return;
        }
        mLoginType = Constants.MOB_PHONE;
        String countryCode = mCountryCode.getText().toString();
        if (!TextUtils.isEmpty(countryCode) && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1);
        }
        MainHttpUtil.login(phoneNum, pwd, countryCode, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                onLoginSuccess(code, msg, info);
            }
        });
    }

    private void onLoginSuccess(int code, String msg, String[] info) {
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
        } else if (code == 1002) {
            if (info.length > 0) {
                LoginForbiddenDialogFragment fragment = new LoginForbiddenDialogFragment();
                JSONObject obj = JSON.parseObject(info[0]);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TIP, obj.getString("ban_reason"));
                bundle.putString(Constants.UID, obj.getString("ban_tip"));
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "LoginForbiddenDialogFragment");
            }
        } else {
            ToastUtil.show(msg);
        }
    }

    /**
     * 获取用户信息
     */
    private void getBaseUserInfo() {
        MainHttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
            @Override
            public void callback(UserBean bean) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                if (mFirstLogin) {
                    RecommendActivity.forward(mContext, mFirstLogin);
                } else {
                    EventBus.getDefault().post(new LoginChangeEvent(true, false));
                }
                finish();
            }
        });
    }



    /**
     * 三方登录
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegSuccessEvent(RegSuccessEvent e) {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(MainHttpConsts.LOGIN);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_QQ_LOGIN_UNION_ID);
        MainHttpUtil.cancel(MainHttpConsts.LOGIN_BY_THIRD);
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        MainHttpUtil.cancel(MainHttpConsts.GET_LOGIN_INFO);
//        if (mLoginUtil != null) {
//            mLoginUtil.release();
//        }
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
