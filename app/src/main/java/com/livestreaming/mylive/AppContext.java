package com.livestreaming.mylive;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.livestreaming.mylive.BuildConfig;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.utils.DecryptUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.im.tpns.TpnsUtil;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.mylive.activity.CustomErrorActivity;
import com.livestreaming.mylive.notification_service.AppLifecycleTracker;
import com.google.android.gms.ads.MobileAds;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.config.IToastInterceptor;
import com.meihu.beautylibrary.MHSDK;
import com.tencent.ugc.TXUGCBase;
import com.yariksoffice.lingver.Lingver;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import io.branch.referral.Branch;


public class AppContext extends CommonAppContext {

    private boolean mBeautyInited;
    public AppLifecycleTracker tracker;

    private static AppContext appInstance;

    private static boolean deBug = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();
        tracker=new AppLifecycleTracker();
        registerActivityLifecycleCallbacks(tracker);
        ToastUtils.init(this);
        initCrashActivity();
        ToastUtils.setInterceptor(new IToastInterceptor() {
            @Override
            public boolean intercept(CharSequence charSequence) {
                return !CommonAppContext.getInstance().isFront();
            }
        });
        L.setDeBug(deBug);
        Lingver.init(this);

//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);
        SharedPreferences preferences = getSharedPreferences("myCurrentLang", MODE_PRIVATE);

        Constants.CUR_LANGUAGE = preferences.getString("current_lang", "en");
        Log.e("curLang", Constants.CUR_LANGUAGE);
        LanguageUtil.getInstance().updateLanguage(Constants.CUR_LANGUAGE);
        Lingver.getInstance().setLocale(getApplicationContext(), Constants.CUR_LANGUAGE);

        // Enable Branch logging in debug mode
        if (BuildConfig.DEBUG) {
            Branch.enableLogging();
            Branch.enableTestMode();
            Log.d("Branch", "Branch test mode enabled");
        }

        // Initialize Branch.io SDK
        Branch.getAutoInstance(this);
    }

    public static void initSdk() {
        try {
            CommonAppContext context = CommonAppContext.getInstance();
            FirebaseApp.initializeApp(context);
            ImMessageUtil.getInstance().init();
            TpnsUtil.register(deBug);
           // String liveLicenceUrl = "https://license.vod2.myqcloud.com/license/v2/1304801090_1/v_cube.license";
          //  腾讯云直播鉴权key
           // String liveKey = "9e0983bc37b3e61aa894f4a85264376d";
            String ugcLicenceUrl = "https://license.vod2.myqcloud.com/license/v2/1323895393_1/v_cube.license";
            //腾讯云视频鉴权key
            String ugcKey = "4faa05720930d9a6eaa4ff39467b6288";
            //TXLiveBase.getInstance().setLicence(context, liveLicenceUrl, liveKey);
            TXUGCBase.getInstance().setLicence(context, ugcLicenceUrl, ugcKey);
//            //初始化腾讯bugly
////            CrashReport.initCrashReport(context);
//  //          CrashReport.setAppVersion(context, CommonAppConfig.getInstance().getVersion());
            MobileAds.initialize(context, initializationStatus -> {
                // SDK initialization complete
            });

        } catch (Exception e) {

        }
    }

    /**
     * 初始化美狐
     */
    public void initBeautySdk(String beautyAppId, String beautyKey) {
        if (!TextUtils.isEmpty(beautyAppId) && !TextUtils.isEmpty(beautyKey)) {
            if (!mBeautyInited) {
                mBeautyInited = true;
                if (CommonAppConfig.isYunBaoApp()) {
                    beautyAppId = DecryptUtil.decrypt(beautyAppId);
                    beautyKey = DecryptUtil.decrypt(beautyKey);
                }
                MHSDK.init(this, beautyAppId, beautyKey);
                CommonAppConfig.getInstance().setMhBeautyEnable(true);
            }
        } else {
            CommonAppConfig.getInstance().setMhBeautyEnable(false);
        }
    }

    @Override
    public void startInitSdk() {
        initSdk();
    }

    public void initCrashActivity() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .minTimeBetweenCrashesMs(2000)
                .restartActivity(CustomErrorActivity.class)
                .errorActivity(CustomErrorActivity.class)
                .apply();
    }
}
