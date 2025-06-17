package com.livestreaming.main.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.LoginChangeEvent;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.dialog.LoginForbiddenDialogFragment;
import com.livestreaming.main.event.RegSuccessEvent;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;

public class IntroActivity extends AbsActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_intro;
    }


    private static final String EMAIL = "email";
    private static final String PUBLIC_PROFILE = "public_profile";

//    LoginButton loginButton;
//
//    CallbackManager callbackManager;


    private boolean mFirstLogin;//是否是第一次登录


    private boolean mUseCountryCode;//是否能选择国家代号
    private ImageView mLoginCheckBox;
    private boolean mChecked;
    private boolean isShowMore = false;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;

    private GoogleSignInClient mGoogleSignInClient;


    SignInButton signInButton;

    private void initGoogleSignIn() {
        findViewById(R.id.g_container).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_google_signin).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_gmail).setVisibility(View.VISIBLE);
        try {
            signInButton = new SignInButton(this);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut();
            findViewById(R.id.g_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signInWithGoogle();
                }
            });
            findViewById(R.id.snap_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    singinWithSnapChat();
                }
            });
        } catch (Exception e) {

        }
    }

    private void singinWithSnapChat() {

    }

    //SnapLogin snapLogin;

    @Override
    protected void main() {
//        Uri uri = getIntent().getData();
//
//        if (uri != null) {
//            // Extract the state parameter from the URL
//            String returnedState = uri.getQueryParameter("state");
//
//            // Get the saved state value from SharedPreferences
//            String savedState = getSharedPreferences("SnapState", MODE_PRIVATE).getString("state", "");
//
//            // Compare the returned state with the saved state
//            if (returnedState != null && returnedState.equals(savedState)) {
//                // State matches, proceed with the OAuth process
//                String authCode = uri.getQueryParameter("code");
//                exchangeCodeForAccessToken(authCode);  // Call your method to exchange the code
//            } else {
//                // State doesn't match, handle error
//                Log.e("OAuth", "State mismatch! Possible CSRF attack");
//            }
//        }
        findViewById(R.id.show_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowMore) {
                    isShowMore = false;
                    ((ImageView) findViewById(R.id.show_more)).setImageDrawable(getDrawable(R.drawable.arrow_down));
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_translate_from_bottom_to_top);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            // Optional: Action to perform when the animation starts
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Action to perform after the animation ends
                            findViewById(R.id.show_more_layout).setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            // Optional: Action to perform if the animation repeats
                        }
                    });
                    findViewById(R.id.show_more_layout).startAnimation(animation);
                } else {
                    isShowMore = true;
                    ((ImageView) findViewById(R.id.show_more)).setImageDrawable(getDrawable(R.drawable.arrow_up));
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_translate_top_bottom);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            // Optional: Action to perform when the animation starts
                            findViewById(R.id.show_more_layout).setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Action to perform after the animation ends
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            // Optional: Action to perform if the animation repeats
                        }
                    });
                    findViewById(R.id.show_more_layout).startAnimation(animation);
                }
            }
        });
//        loginButton = (LoginButton) findViewById(R.id.login_button);
//        loginButton.setPermissions(Arrays.asList(EMAIL, PUBLIC_PROFILE));
//
//        callbackManager = CallbackManager.Factory.create();
//
//        snapLogin = SnapLoginProvider.get(CommonAppContext.getInstance());
//        findViewById(R.id.snap_container).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Start token grant
//                signinWithSnap();
//
//            }
//
//        });
//        findViewById(R.id.tv_snap_signin).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Start token grant
//                signinWithSnap();
//
//            }
//
//
//        });
        findViewById(R.id.go_to_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        findViewById(R.id.go_to_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
//        LoginManager.getInstance().registerCallback(callbackManager,
//                new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//                        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
//                        if (isLoggedIn) {
//                            String userId = accessToken.getUserId();
//                            String token = accessToken.getToken();
//                            fetchUserProfile(accessToken);
//                        } else {
//                            ToastUtil.show(getString(com.livestreaming.common.R.string.login_auth_cancle));
//                        }
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        // App code
//                    }
//
//                    @Override
//                    public void onError(FacebookException exception) {
//                        // App code
//                    }
//                });
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mChecked) {
//                    ToastUtil.show(com.livestreaming.common.R.string.login_check_tip);
//                    return;
//                }
////                LoginManager.getInstance().logInWithReadPermissions(
////                        IntroActivity.this,
////                        Arrays.asList("email", "public_profile")
////                );
//            }
//        });
        initGoogleSignIn();
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

    private void exchangeCodeForAccessToken(String authCode) {

    }

//    private void signinWithSnap() {
//        if (!mChecked) {
//            ToastUtil.show(com.livestreaming.common.R.string.login_check_tip);
//            vibrateErrorTerms();
//            return;
//        }
//        snapLogin.startTokenGrant(new LoginResultCallback() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onSuccess(@NonNull String accessToken) {
//                if (snapLogin.isUserLoggedIn()) {
//
//                    final UserDataQuery userDataQuery = UserDataQuery.newBuilder()
//                            // optional: for 'displayName' resource
//                            .withDisplayName()
//                            // optional: for 'externalID' resource
//                            .withExternalId()
//                            .build();
//                    snapLogin.addLoginStateCallback(new LoginStateCallback() {
//                        @Override
//                        public void onLogout() {
//                            Log.w("snap","output");
//                        }
//
//                        @Override
//                        public void onStart() {
//
//                            Log.w("snap","start log");
//                        }
//
//                        @Override
//                        public void onSuccess(@NonNull String s) {
//
//                            Log.w("snap","success "+s);
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull LoginException e) {
//                            Log.w("snap","fail "+e.getLocalizedMessage());
//                        }
//                    });
//                    snapLogin.fetchUserData(userDataQuery, new UserDataResultCallback() {
//                        @Override
//                        public void onSuccess(@NonNull UserDataResult userDataResult) {
//                            // Handle Success
//                            if (userDataResult.getData() == null) {
//                                return;
//                            }
//
//                            MeData meData = userDataResult.getData().getMeData();
//                            if (meData == null) {
//                                return;
//                            }
//                            MainHttpUtil.loginWithGoogle(meData.getDisplayName(), meData.getDisplayName(), meData.getDisplayName(), meData.getDisplayName(), new HttpCallback() {
//                                @Override
//                                public void onSuccess(int code, String msg, String[] info) {
//                                    if (code == 0 && info.length > 0) {
//                                        JSONObject obj = JSON.parseObject(info[0]);
//                                        String uid = obj.getString("id");
//                                        String token = obj.getString("token");
//                                        if(info.length>1) {
//                                            JSONObject obj2 = JSON.parseObject(info[1]);
//                                            mFirstLogin = obj2.getIntValue("first_time") == 1;
//                                        }
//                                        String gameToken = obj.getString("game_login");
//                                        SpUtil.getInstance().setStringValue(SpUtil.TX_IM_USER_SIGN, obj.getString("usersign"));
//                                        CommonAppConfig.getInstance().setLoginInfo(uid, token, true);
//                                        CommonAppConfig.getInstance().setGameToken(gameToken);
//                                        getBaseUserInfo();
//                                    } else {
//                                        ToastUtil.show(msg);
//                                    }
//                                }
//
//                                @Override
//                                public void onError() {
//                                    super.onError();
//                                    Log.w("snap", "signInResult:failed code=");
//                                }
//                            });
//
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull UserDataException exception) {
//
//                            Log.e("snap", ""+exception.getMessage());
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull com.snap.loginkit.exceptions.LoginException e) {
//
//                Log.e("snap", "Failed to fetch user data", e);
//            }
//        });
//    }

//    private void fetchUserProfile(AccessToken accessToken) {
//        // Example of fetching user profile data (name, email, etc.)
//        GraphRequest request = GraphRequest.newMeRequest(accessToken, (object, response) -> {
//            try {
//                if (object == null) return;
//                String name = object.getString("name");
//                String email = object.getString("email");
//                Log.d("FacebookLogin", "User Name: " + name);
//                Log.d("FacebookLogin", "User Email: " + email);
//                loginBuyThird(name, email, accessToken.getUserId());
//            } catch (Exception e) {
//                Log.e("FacebookLogin", "Error fetching profile data: " + e.getMessage());
//            }
//        });
//
//        // Add fields to retrieve from the user's profile
//        Bundle parameters = new Bundle();
//        parameters.putString("fields", "id,name,email");
//        request.setParameters(parameters);
//        request.executeAsync();
//    }
//

    //注册
    private void register() {
        Intent intent = new Intent(mContext, RegisterActivity.class);
        intent.putExtra(Constants.TIP, mUseCountryCode);
        startActivity(intent);
    }

    private void login() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Constants.TIP, mUseCountryCode);
        startActivity(intent);
    }

    //private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 525;

    private void signInWithGoogle() {
        if (!mChecked) {
            ToastUtil.show(com.livestreaming.common.R.string.login_check_tip);
            vibrateErrorTerms();
            return;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void vibrateErrorTerms() {
        Animation animation=AnimationUtils.loadAnimation(mContext,R.anim.anim_vibrate_error);
        findViewById(R.id.group_login_tip).startAnimation(animation);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();
                MainHttpUtil.loginWithGoogle(personEmail, personId, personId, personId, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            JSONObject obj = JSONObject.parseObject(info[0]);
                            String uid = obj.getString("id");
                            String token = obj.getString("token");
                            if(info.length>1) {
                                JSONObject obj2 = JSONObject.parseObject(info[1]);
                                mFirstLogin = obj2.getIntValue("first_time") == 1;
                            }
                            String gameToken = obj.getString("game_login");
                            SpUtil.getInstance().setStringValue(SpUtil.TX_IM_USER_SIGN, obj.getString("usersign"));
                            CommonAppConfig.getInstance().setLoginInfo(uid, token, true);
                            CommonAppConfig.getInstance().setGameToken(gameToken);
                            getBaseUserInfo();
                        } else {
                            ToastUtil.show(msg);
                        }
                    }

                    @Override
                    public void onError() {
                        super.onError();
                        Log.w("TAG", "signInResult:failed code=");
                    }
                });
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }


    //忘记密码


    //手机号密码登录

    //登录即代表同意服务和隐私条款
    private void forwardTip() {
//        WebViewActivity.forward(mContext, HtmlConfig.LOGIN_PRIVCAY);
    }

    //登录成功！
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
                int is_data_edited_first= bean.getIsDataEditedFirst();
                if (is_data_edited_first==0) {
                    Intent intent = new Intent(IntroActivity.this, EditProfileActivity.class);
                    intent.putExtra("isFromFirst", true);
                    startActivity(intent);
                } else {
                    EventBus.getDefault().post(new LoginChangeEvent(true, false));
                }
                finish();
            }
        });
    }

    public String generateRandomState() {
        return UUID.randomUUID().toString();  // Generates a unique random string
    }
    /**
     * 三方登录
     */
    private void loginBuyThird(String name, String email, String id) {

        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        MainHttpUtil.loginByThird(id, email, name, "", "", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                onLoginSuccess(code, msg, info);
            }
        });
    }

    /*
        @Override
        public void onItemClick(MobBean bean, int position) {
            if (mLoginUtil == null) {
                return;
            }
            if (!mChecked) {
                ToastUtil.show(com.livestreaming.common.R.string.login_check_tip);
                return;
            }
            final Dialog dialog = DialogUitl.loginAuthDialog(mContext);
            dialog.show();
            mLoginUtil.execute(bean.getType(), new MobCallback() {
                @Override
                public void onSuccess(Object data) {
                    if (data != null) {
                        loginBuyThird((LoginData) data);
                    }
                }

                @Override
                public void onError() {

                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onFinish() {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        }*/
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
        //callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && data != null) {
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

}
