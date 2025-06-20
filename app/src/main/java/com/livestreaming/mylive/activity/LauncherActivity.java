package com.livestreaming.mylive.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.AdBean;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.custom.CircleProgress;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.FileDownloadCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.BranchHelper;
import com.livestreaming.common.utils.DownloadUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.live.views.LauncherAdViewHolder;
import com.livestreaming.main.activity.EditProfileActivity;
import com.livestreaming.main.activity.MainActivity;
import com.livestreaming.main.dialog.OnShelfLoginTipDialogFragment;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.mylive.AppContext;
import com.livestreaming.mylive.R;
import com.livestreaming.mylive.callback.BundleCallback;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yariksoffice.lingver.Lingver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;


public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LauncherActivity";
    private static final int WHAT_GET_CONFIG = 0;
    private static final int WHAT_COUNT_DOWN = 1;
    private Handler mHandler;
    protected Context mContext;
    private ViewGroup mRoot;
    private ImageView mCover;
    private ViewGroup mContainer;
    private CircleProgress mCircleProgress;
    private List<AdBean> mAdList;
    private List<ImageView> mImageViewList;
    private int mMaxProgressVal;
    private int mCurProgressVal;
    private int mAdIndex;
    private int mInterval = 2000;
    private View mBtnSkipImage;
    private View mBtnSkipVideo;
    private TXCloudVideoView mTXCloudVideoView;
    private TXVodPlayer mPlayer;
    private LauncherAdViewHolder mLauncherAdViewHolder;
    private boolean mPaused;
    private boolean mForward;
    private View mAdTip;
    private boolean mGetConfig;

    private AlertDialog dialog = null;
    private int isFirstLunch = 1;

    CompletableFuture<Boolean> future = new CompletableFuture<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setStatusBar();
        Lingver.getInstance().setLocale(this, Constants.CUR_LANGUAGE);
        setContentView(R.layout.activity_launcher);
//        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        mContext = this;
        mRoot = findViewById(R.id.root);
        mCover = findViewById(R.id.cover);
        mCircleProgress = findViewById(R.id.progress);
        mContainer = findViewById(R.id.container);
        mBtnSkipImage = findViewById(R.id.btn_skip_img);
        mBtnSkipVideo = findViewById(R.id.btn_skip_video);
        mBtnSkipImage.setOnClickListener(this);
        mBtnSkipVideo.setOnClickListener(this);
        mAdTip = findViewById(R.id.ad_tip);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WHAT_GET_CONFIG:
                        try {
                            getConfig();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case WHAT_COUNT_DOWN:
                        updateCountDown();
                        break;
                }
            }
        };
        if (checkSharedPref_ForLangSet()) {
            boolean baseFunctionMode = CommonAppConfig.getInstance().isBaseFunctionMode();
            //已登录，显示引导页
            if (!baseFunctionMode) {
                try {
                    AppContext.initSdk();
                } catch (Exception e) {

                }
                mHandler.sendEmptyMessageDelayed(WHAT_GET_CONFIG, 1000);
            } else {
                //未登录，显示隐私条款
                OnShelfLoginTipDialogFragment fragment = new OnShelfLoginTipDialogFragment();
                fragment.setAgreeCallback(new CommonCallback<Boolean>() {
                    @Override
                    public void callback(Boolean argree) {
                        if (argree) {
                            try {
                                AppContext.initSdk();
                            } catch (Exception e) {

                            }
                            CommonAppConfig.getInstance().setBaseFunctionMode(false);
                        }
                        mHandler.sendEmptyMessageDelayed(WHAT_GET_CONFIG, 1000);
                    }
                });
                fragment.show(getSupportFragmentManager(), "LoginTipDialogFragment");
            }

        } else {

            SharedPreferences preferences = getSharedPreferences("myCurrentLang", MODE_PRIVATE);
            preferences.edit().putBoolean("isLang_set", true).apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(this, com.livestreaming.common.R.style.TransparentDialog);
            View view = getLayoutInflater().inflate(com.livestreaming.common.R.layout.view_first_lang_call, null);
            builder.setView(view);
            Button btnDone = view.findViewById(com.livestreaming.common.R.id.btnDonee);
            RadioGroup group = view.findViewById(com.livestreaming.common.R.id.group_lang);
            group.check(com.livestreaming.common.R.id.rbtnEnglish);
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String lang = "";
                    if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnArabic) {
                        lang = "ar";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnTurky) {
                        lang = "tr";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnkurdisch) {
                        lang = "ku";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnDeutsch) {
                        lang = "de";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnEnglish) {
                        lang = "en";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnItaliano) {
                        lang = "it";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnSpanish) {
                        lang = "es";
                    } else if (group.getCheckedRadioButtonId() == com.livestreaming.common.R.id.rbtnRomainion) {
                        lang = "ro";
                    } else {
                        lang = "fr";
                    }
                    preferences.edit().putString("current_lang", lang).apply();
                    Constants.CUR_LANGUAGE = lang;
                    Log.e("curLang", Constants.CUR_LANGUAGE);
                    Lingver.getInstance().setLocale(getApplicationContext(), lang);
                    LanguageUtil.getInstance().updateLanguage(lang);
                    Resources resources = getResources();
                    DisplayMetrics dm = resources.getDisplayMetrics();
                    Configuration config = resources.getConfiguration();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        config.setLocale(new Locale(lang));
                    } else {
                        config.locale = new Locale(lang);
                    }
                    resources.updateConfiguration(config, dm);
                    recreate();
                }
            });
            builder.setCancelable(false);
            dialog = builder.create();
            Window window = dialog.getWindow();
            Point size = new Point();
            if (window != null) {
                WindowManager manager = window.getWindowManager();
                if (manager != null) {
                    Display display = manager.getDefaultDisplay();
                    display.getSize(size);
                    window.setGravity(Gravity.CENTER);
                    dialog.show();
                    window.setLayout((int) (size.x * 0.859), WindowManager.LayoutParams.WRAP_CONTENT);
                }
            }
        }
    }

    private boolean checkSharedPref_ForLangSet() {
        SharedPreferences preferences = getSharedPreferences("myCurrentLang", MODE_PRIVATE);
        return preferences.getBoolean("isLang_set", false);
    }


    private void checkHasAdLink(int index) {
        if (index >= 0 && index < mAdList.size()) {
            AdBean adBean = mAdList.get(index);
            if (adBean != null && !TextUtils.isEmpty(adBean.getLink())) {
                if (mAdTip.getVisibility() != View.VISIBLE) {
                    mAdTip.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAdTip.getVisibility() == View.VISIBLE) {
                    mAdTip.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * 图片倒计时
     */
    private void updateCountDown() {
        mCurProgressVal += 100;
        if (mCurProgressVal > mMaxProgressVal) {
            return;
        }
        if (mCircleProgress != null) {
            mCircleProgress.setCurProgress(mCurProgressVal);
        }
        int index = mCurProgressVal / mInterval;
        if (index < mAdList.size() && mAdIndex != index) {
            View v = mImageViewList.get(mAdIndex);
            if (v != null && v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.INVISIBLE);
            }
            mAdIndex = index;
            checkHasAdLink(index);
        }
        if (mCurProgressVal < mMaxProgressVal) {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(WHAT_COUNT_DOWN, 100);
            }
        } else if (mCurProgressVal == mMaxProgressVal) {
            checkUidAndToken();
        }
    }


    /**
     * 获取Config信息
     */

    private void getConfig() {
        try {
            CommonHttpUtil.getConfig(new CommonCallback<ConfigBean>() {
                @Override
                public void callback(ConfigBean bean) {
                    if (mGetConfig) {
                        return;
                    }
                    mGetConfig = true;
                    if (bean != null) {
                        ((AppContext) CommonAppContext.getInstance()).initBeautySdk(bean.getBeautyAppId(), bean.getBeautyKey());
                        String adInfo = bean.getAdInfo();
                        if (!TextUtils.isEmpty(adInfo)) {
                            JSONObject obj = null;
                            try {
                                obj = JSON.parseObject(adInfo);
                            } catch (Exception e) {

                            }
                            if (obj != null) {
                                if (obj.getIntValue("switch") == 1) {
                                    List<AdBean> list = JSON.parseArray(obj.getString("list"), AdBean.class);
                                    if (list != null && !list.isEmpty()) {
                                        mAdList = list;
                                        mInterval = obj.getIntValue("time") * 1000;
                                        if (mContainer != null) {
                                            mContainer.setOnClickListener(LauncherActivity.this);
                                        }
                                        playAD(obj.getIntValue("type") == 0);
                                    } else {
                                        checkUidAndToken();
                                    }
                                } else {

                                    checkUidAndToken();
                                }
                            }
                        } else {
                            checkUidAndToken();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查uid和token是否存在
     */
    private void checkUidAndToken() {
        try {


            if (mForward) {
                return;
            }
            mForward = true;
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
            String[] uidAndToken = SpUtil.getInstance().getMultiStringValue(
                    new String[]{SpUtil.UID, SpUtil.TOKEN});
            final String uid = uidAndToken[0];
            final String token = uidAndToken[1];
            if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(token)) {
                if (CommonAppConfig.getInstance().isLogin()) {
                    MainHttpUtil.getBaseInfo(uid, token, new CommonCallback<UserBean>() {
                        @Override
                        public void callback(UserBean bean) {
                            if (bean != null) {
                                isFirstLunch = bean.getIsDataEditedFirst();
                                CommonAppConfig.getInstance().setLoginInfo(uid, token, false);
                                forwardMainActivity();
                            }
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            CommonAppConfig.getInstance().clearLoginInfo();
                            forwardMainActivity();
                        }
                    });
                } else {
                    forwardMainActivity();
                }
            } else {
                CommonAppConfig.getInstance().setLoginInfo(Constants.NOT_LOGIN_UID, Constants.NOT_LOGIN_TOKEN, false);
                forwardMainActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 跳转到首页
     */
    private void forwardMainActivity() {
        if (isFirstLunch == 0) {
            Intent intent = new Intent(LauncherActivity.this, EditProfileActivity.class);
            intent.putExtra("isFromFirst", true);
            startActivity(intent);
        } else {
            releaseVideo();
            future.complete(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        releaseVideo();
        if (mLauncherAdViewHolder != null) {
            mLauncherAdViewHolder.release();
        }
        mLauncherAdViewHolder = null;
        super.onDestroy();
        L.e(TAG, "----------> onDestroy");
    }

    /**
     * 设置透明状态栏
     */
    private void setStatusBar() {
        Window window = getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_skip_img || i == R.id.btn_skip_video) {
            if (mBtnSkipImage != null) {
                mBtnSkipImage.setClickable(false);
            }
            if (mBtnSkipVideo != null) {
                mBtnSkipVideo.setClickable(false);
            }
            checkUidAndToken();
        } else if (i == R.id.container) {
            clickAD();
        }
    }

    /**
     * 点击广告
     */
    private void clickAD() {
        if (mAdList != null && mAdList.size() > mAdIndex) {
            AdBean adBean = mAdList.get(mAdIndex);
            if (adBean != null) {
                String link = adBean.getLink();
                if (!TextUtils.isEmpty(link)) {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    if (mContainer != null) {
                        mContainer.setClickable(false);
                    }
                    releaseVideo();
                    if (mLauncherAdViewHolder == null) {
                        mLauncherAdViewHolder = new LauncherAdViewHolder(mContext, mRoot, link);
                        mLauncherAdViewHolder.addToParent();
                        mLauncherAdViewHolder.loadData();
                        mLauncherAdViewHolder.show();
                        mLauncherAdViewHolder.setActionListener(new LauncherAdViewHolder.ActionListener() {
                            @Override
                            public void onHideClick() {
                                checkUidAndToken();
                            }
                        });
                    }
                }
            }
        }
    }

    private void releaseVideo() {
        try {
            if (mPlayer != null) {
                mPlayer.stopPlay(false);
                mPlayer.setPlayListener(null);
            }
            mPlayer = null;
        } catch (Exception e) {

        }
    }


    /**
     * 播放广告
     */
    private void playAD(boolean isImage) {
        if (mContainer == null) {
            return;
        }
        if (isImage) {
            int imgSize = mAdList.size();
            if (imgSize > 0) {
                mImageViewList = new ArrayList<>();
                for (int i = 0; i < imgSize; i++) {
                    final ImageView imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setBackgroundColor(0xffffffff);
                    mImageViewList.add(imageView);
                    ImgLoader.displayDrawable(mContext, mAdList.get(i).getUrl(), new ImgLoader.DrawableCallback() {
                        @Override
                        public void onLoadSuccess(Drawable drawable) {
                            imageView.setImageDrawable(drawable);
                            if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
                                mCover.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onLoadFailed() {

                        }
                    });
                }
                for (int i = imgSize - 1; i >= 0; i--) {
                    mContainer.addView(mImageViewList.get(i));
                }
                if (mBtnSkipImage != null && mBtnSkipImage.getVisibility() != View.VISIBLE) {
                    mBtnSkipImage.setVisibility(View.VISIBLE);
                    mCover.setVisibility(TXCloudVideoView.GONE);
                }
                mMaxProgressVal = imgSize * mInterval;
                if (mCircleProgress != null) {
                    mCircleProgress.setMaxProgress(mMaxProgressVal);
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(WHAT_COUNT_DOWN, 100);
                }
                checkHasAdLink(0);
            } else {
                checkUidAndToken();
            }
        } else {
            if (mAdList == null || mAdList.size() == 0) {
                checkUidAndToken();
                return;
            }
            String videoUrl = mAdList.get(0).getUrl();
            if (TextUtils.isEmpty(videoUrl)) {
                checkUidAndToken();
                return;
            }
            String videoFileName = MD5Util.getMD5(videoUrl);
            if (TextUtils.isEmpty(videoFileName)) {
                checkUidAndToken();
                return;
            }
            File file = new File(getCacheDir(), videoFileName);
            if (file.exists()) {
                playAdVideo(file);
            } else {
                downloadAdFile(videoUrl, videoFileName);
            }
        }
    }

    @Override
    protected void onPause() {
        mPaused = true;
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.setMute(true);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.setMute(false);
            }
        }
        mPaused = false;
    }

    /**
     * 下载视频
     */
    private void downloadAdFile(String url, String fileName) {
        new DownloadUtil("ad_video").download(getCacheDir(), fileName, url, new FileDownloadCallback() {
            @Override
            public void onSuccess(File file) {
                playAdVideo(file);
            }

            @Override
            public void onError(Throwable e) {
                checkUidAndToken();
            }
        });
    }

    /**
     * 播放视频
     */
    private void playAdVideo(File videoFile) {
        if (mBtnSkipVideo != null && mBtnSkipVideo.getVisibility() != View.VISIBLE) {
            mBtnSkipVideo.setVisibility(View.VISIBLE);
            mCover.setVisibility(View.GONE);
        }
        mTXCloudVideoView = new TXCloudVideoView(mContext);
        mTXCloudVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        mTXCloudVideoView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mContainer.addView(mTXCloudVideoView);
        mPlayer = new TXVodPlayer(mContext);
        mPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mPlayer.setPlayerView(mTXCloudVideoView);
        mPlayer.setAutoPlay(true);
        mPlayer.setPlayListener(new ITXLivePlayListener() {
            @Override
            public void onPlayEvent(int e, Bundle bundle) {
                if (e == TXLiveConstants.PLAY_EVT_PLAY_END) {//获取到视频播放完毕的回调
                    checkUidAndToken();
                    L.e(TAG, "视频播放结束------>");
                } else if (e == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {////获取到视频宽高回调
                    float videoWidth = bundle.getInt("EVT_PARAM1", 0);
                    float videoHeight = bundle.getInt("EVT_PARAM2", 0);
                    if (mTXCloudVideoView != null && videoWidth > 0 && videoHeight > 0) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
                        int targetH = 0;
                        if (videoWidth >= videoHeight) {//横屏
                            params.gravity = Gravity.CENTER_VERTICAL;
                            targetH = (int) (mTXCloudVideoView.getWidth() / videoWidth * videoHeight);
                        } else {
                            targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                        }
                        if (targetH != params.height) {
                            params.height = targetH;
                            mTXCloudVideoView.requestLayout();
                        }
                    }
                } else if (e == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
                    if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
                        mCover.setVisibility(View.GONE);
                    }
                } else if (e == TXLiveConstants.PLAY_ERR_NET_DISCONNECT ||
                        e == TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND) {
                    checkUidAndToken();
                }
            }

            @Override
            public void onNetStatus(Bundle bundle) {

            }

        });
        mPlayer.startVodPlay(videoFile.getAbsolutePath());
        checkHasAdLink(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getBranchIoDeeplink(false, callback);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getBranchIoDeeplink(true, callback);
    }

    private void getBranchIoDeeplink(boolean newIntent, BundleCallback callback) {
        // Process Branch.io deep links
        BranchHelper.processBranchIoDeepLink(this,getIntent(), newIntent, getIntent().getData(), (referringParams, error) -> {
            if (error != null) {
                Log.d("ERRORR--", error.getMessage());
            }
            if (referringParams != null) {
                Log.d("referringParams", referringParams.toString());
            }
            if (error == null && referringParams != null) {
                String streamId = BranchHelper.extractStreamId(referringParams);
                if (streamId == null) {
                    callback.getBundle(null);
                }
                Bundle bundle = new Bundle();
                bundle.putString("stream_id", streamId);
                callback.getBundle(bundle);
            } else {
                callback.getBundle(null);
            }
        });
    }

    BundleCallback callback = new BundleCallback() {
        @Override
        public void getBundle(@Nullable Bundle bundle) {
            future.thenAccept(result->{
                if (result) {
                    if (bundle == null) {
                        MainActivity.forward(mContext, getIntent().getExtras());
                    } else {
                        Log.d("stream_id", bundle.getString("stream_id"));
                        MainActivity.forward(mContext, bundle);
                    }
                    finish();
                }
            });
        }
    };
}
