package com.livestreaming.main.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.ViewPagerAdapter;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.custom.TabButtonGroup;
import com.livestreaming.common.dialog.NotCancelableInputDialog;
import com.livestreaming.common.event.CloseFloatWindowEvent;
import com.livestreaming.common.event.LoginChangeEvent;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.LocationUtil;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.VersionUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.activity.ChatActivity;
import com.livestreaming.im.event.ImUnReadCountEvent;
import com.livestreaming.im.event.NotificiationClickEvent;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.im.utils.ImUnReadCount;
import com.livestreaming.im.utils.NotificationUtil;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.bean.LiveConfigBean;
import com.livestreaming.live.event.LiveAudienceChatRoomExitEvent;
import com.livestreaming.live.event.LiveFloatWindowEvent;
import com.livestreaming.live.floatwindow.FloatWindowUtil;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.utils.LiveStorge;
import com.livestreaming.live.utils.PrefsManager;
import com.livestreaming.live.views.LiveChatRoomPlayUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.bean.BonusBean;
import com.livestreaming.main.dialog.FullFunctionDialogFragment;
import com.livestreaming.main.dialog.MainStartDialogFragment;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.main.interfaces.MainAppBarLayoutListener;
import com.livestreaming.main.interfaces.MainStartChooseCallback;
import com.livestreaming.main.presenter.CheckLivePresenter;
import com.livestreaming.main.views.AbsMainViewHolder;
import com.livestreaming.main.views.BonusViewHolder;
import com.livestreaming.main.views.MainActiveViewHolder;
import com.livestreaming.main.views.MainHomeVideoScrollVh;
import com.livestreaming.main.views.MainHomeViewHolder;
import com.livestreaming.main.views.MainMeViewHolder;
import com.livestreaming.video.activity.AbsVideoPlayActivity;
import com.livestreaming.video.activity.VideoRecordActivity;
import com.livestreaming.video.http.VideoHttpUtil;
import com.livestreaming.video.utils.VideoStorge;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yariksoffice.lingver.Lingver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AbsVideoPlayActivity implements MainAppBarLayoutListener {

    private ViewGroup mRootView;
    private TabButtonGroup mTabButtonGroup;
    private ViewPager mViewPager;
    private List<FrameLayout> mViewList;
    private MainHomeViewHolder mHomeViewHolder;
    private MainActiveViewHolder mActiveViewHolder;
    //private MainMallViewHolder mMallViewHolder;
    private MainHomeVideoScrollVh mVideoViewHolder;
    private MainMeViewHolder mMeViewHolder;
    private AbsMainViewHolder[] mViewHolders;
    private View mBottom;
    private ObjectAnimator mUpAnimator;//向上动画
    private ObjectAnimator mDownAnimator;//向下动画
    private boolean mAnimating;
    private boolean mShowed = true;
    private boolean mHided;

    private String userCountryCode;
    private FusedLocationProviderClient fusedLocationClient;

    private CheckLivePresenter mCheckLivePresenter;
    private boolean mFristLoad;
    private long mLastClickBackTime;//上次点击back键的时间
    private AudioManager mAudioManager;
    private boolean mFirstLogin;//是否是第一次登录
    private Handler mHandler;
    private boolean mLoginChanged;
    private Runnable mTeengerTicker;
    private HttpCallback mTeenagerTimeCallback;
    private boolean mIsTeengerTick;
    private BonusViewHolder mBonusViewHolder;
    private View mBtnFullFunction;
    private int selectedType = 0;
    private boolean mPaused = false;
    public static int selectedHomeTab = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
//        String retrievedJson = PrefsManager.getNotificationObject();
//        Log.i("36545634", "main: "+retrievedJson);
//        if (retrievedJson != null) {
//            handleNotificationS(retrievedJson);
//        }

// Convert back from JSON to object (optional)
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String notificationObject = extras.getString("notification_object");

                if (notificationObject != null) {
                    handleNotificationS(notificationObject);
                } else {
                    String type = extras.getString("type");
                    if (type != null) {
                        handleNotifications(type, extras);
                    }
                }
            }
        }
        if (checkLogin()) {
            checkIfComingFromShareLive(getIntent());
        }
        Lingver.getInstance().setLocale(this, Constants.CUR_LANGUAGE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRootView = (ViewGroup) findViewById(R.id.root);
        mTabButtonGroup = (TabButtonGroup) findViewById(R.id.tab_group);
        mTabButtonGroup.setClickIntercepter(new TabButtonGroup.ClickIntercepter() {
            @Override
            public boolean needIntercept(int position) {
                if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
                    return true;
                }
                if (position == 3 && !CommonAppConfig.getInstance().isLogin()) {
                    RouteUtil.forwardLogin(mContext);
                    return true;
                }
                return false;
            }
        });
        mTabButtonGroup.setOnBtnClickListener(new TabButtonGroup.OnBtnClickListener() {
            @Override
            public void onClick(int position) {
                if (position == 0) {
                    if (mHomeViewHolder != null) {
                        mHomeViewHolder.changeToHomePage();
                    }
                }
            }
        });
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(4);
        mViewList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
                if (mViewHolders != null) {
                    for (int i = 0, length = mViewHolders.length; i < length; i++) {
                        AbsMainViewHolder vh = mViewHolders[i];
                        if (vh != null) {
                            vh.setShowed(position == i);
                        }
                    }
                }
                if (position == 0) {
                    boolean videoChecked = mHomeViewHolder != null && mHomeViewHolder.getCurrentItem() == 1;
                    changeStatusBarWhite(videoChecked);
                } else {
                    changeStatusBarWhite(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabButtonGroup.setViewPager(mViewPager);
        mViewHolders = new AbsMainViewHolder[4];
        int mDp70 = DpUtil.dp2px(70);
        mBottom = findViewById(R.id.bottom);
        mUpAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", mDp70, 0);
        mDownAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", 0, mDp70);
        mUpAnimator.setDuration(250);
        mDownAnimator.setDuration(250);
        mUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = true;
                mHided = false;
            }
        });
        mDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = false;
                mHided = true;
            }
        });
        mBtnFullFunction = findViewById(R.id.btn_full_function);
        if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
            mBtnFullFunction.setVisibility(View.VISIBLE);
            mBtnFullFunction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FullFunctionDialogFragment fragment = new FullFunctionDialogFragment();
                    fragment.setAgreeCallback(new CommonCallback<Boolean>() {
                        @Override
                        public void callback(Boolean argree) {
                            if (argree) {
                                CommonAppContext.getInstance().startInitSdk();
                                if (mBtnFullFunction != null && mBtnFullFunction.getVisibility() != View.INVISIBLE) {
                                    mBtnFullFunction.setVisibility(View.INVISIBLE);
                                }
                                CommonAppConfig.getInstance().setBaseFunctionMode(false);
                                if (mHomeViewHolder != null) {
                                    mHomeViewHolder.setViewPagerCanScroll(true);
                                }
                            }
                        }
                    });
                    fragment.show(getSupportFragmentManager(), "FullFunctionDialogFragment");
                }
            });
        }
        EventBus.getDefault().register(this);
        checkVersion();
        requestAfterLogin();
        CommonAppConfig.getInstance().setLaunched(true);
        mFristLoad = true;

        if (CommonAppConfig.getInstance().isLogin()) {
            sendFirebaseToken();
        }
    }

    private void handleNotifications(String type, Bundle extras) {
        if (type != null) {
            if (type.equals("10")) {
                Intent intent = new Intent(this, UserHomeActivity.class);
                intent.putExtra(Constants.TO_UID, extras.getString("uid"));
                startActivity(intent);
            }
        }
    }

    private void handleNotificationS(String notificationObject) {
        Log.d("6656+6+46", "handleNotificationS: ;ljddsdf");
        if(checkLogin()) {
            JSONObject object = JSONObject.parseObject(notificationObject);
            if (object.getString("type") != null) {
                if (object.getString("type").equals("10")) {
                    //follow notification
                    Intent intent = new Intent(this, UserHomeActivity.class);
                    intent.putExtra(Constants.TO_UID, object.getString("uid"));
                    startActivity(intent);
                } else if (object.getString("type").equals("1")) {
                    watchLive(new Gson().fromJson(object.getString("room"), LiveBean.class), "", 0);
                } else if (object.getString("type").equals("11")) {
                    watchLive(new Gson().fromJson(object.getString("room"), LiveBean.class), "", 0);
                }
            }
        }
    }

    private void sendFirebaseToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e("testnotf", s);
                MainHttpUtil.sendAndSaveFirebaseToken(s, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {

                    }
                });
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Subscribed to topic!");
                    }
                });
    }

    private void checkIfComingFromShareLive(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("stream_id")) {
                enterLiveRoomByPush(extras.getString("stream_id"));
            }
        }
    }

    private JSONObject startLiveInfoo;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (selectedType == 1) {
                    forwardLive(startLiveInfoo);
                } else if (selectedType == 2) {
                    startActivity(new Intent(mContext, VideoRecordActivity.class));
                } else if (selectedType == 3) {
                    forwardVoiceLive(startLiveInfoo);
                } else if (selectedType == 4) {
                    forwardScreenRecordLive(startLiveInfoo);
                }else if (selectedType == 5) {
                    getCurrentLocation();
                }
            });

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getCountryCode(CommonAppContext.getInstance(), location.getLatitude(), location.getLongitude());
                    } else {
                        Log.d("Location", "Current location is null, trying last known location...");
                        getLastKnownLocation();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Location", "Current location is null, trying last known location...");
                    }
                })
        ;
    }
    public void getCountryCode(Context context, double latitude, double longitude) {
        try {
            Log.e("LocationHelper3", "getUserLocation: ");
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Log.e("LocationHelper4", "getUserLocation: ");
                if (addresses != null && !addresses.isEmpty()) {
                    Log.e("LocationHelper5", "getUserLocation: ");
                    userCountryCode = addresses.get(0).getCountryCode();
                    updateCountryCode();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {

        }
    }
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        getCountryCode(CommonAppContext.getInstance(), location.getLatitude(), location.getLongitude());
                    } else {
                        Log.d("Location", "Failed to get last known location");
                    }
                });
    }
    private void updateCountryCode() {
        Log.e("LocationHelper8", "getUserLocation: ");
        if (userCountryCode == null) return;
        MainHttpUtil.updateUserCountry(userCountryCode.toLowerCase(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (info != null && (info.length > 0)) {
                    int isCountryUpdated = Integer.parseInt(info[0]);
                    SpUtil.getInstance().setIntValue("isCountryUpdated", isCountryUpdated);
                }
            }
        });
    }
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        sendFirebaseToken();
        if (mTabButtonGroup != null) {
            mTabButtonGroup.btnPerformClick(0);
        }
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            // startTeenagerCountdown();
            if (mBonusViewHolder != null) {
                mBonusViewHolder.dismiss();
                mBonusViewHolder = null;
            }
        } else {
            mIsTeengerTick = false;
            if (mHandler != null) {
                mHandler.removeCallbacks(mTeengerTicker);
            }
        }
        if (checkLogin()) {
            checkIfComingFromShareLive(getIntent());
        }
    }

    /**
     * 登录后请求接口
     */
    private void requestAfterLogin() {
        if (CommonAppConfig.getInstance().isLogin()) {
            getAgentCode();//邀请码弹窗
            loginIM();//登录IM
            getChatToken();
            //checkTeenager();
        }
    }


    public void mainClick(View v) {
        if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
            return;
        }
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_rank) {
            RankActivity.forward(mContext, 0);
            return;
        }
        if (!checkLogin()) {
            return;
        }
        if (i == R.id.btn_start) {
            showStartDialog();
        } else if (i == R.id.btn_search) {
            SearchActivity.forward(mContext);
        } else if (i == R.id.btn_msg) {
            ChatActivity.forward(mContext);
        } else if (i == R.id.btn_add_active) {
            startActivity(new Intent(mContext, ActivePubActivity.class));
        }
    }

    private void showStartDialog() {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            ToastUtil.show(com.livestreaming.common.R.string.a_137);
            return;
        }
        if (!FloatWindowHelper.checkVoice(false)) {
            return;
        }
        MainStartDialogFragment dialogFragment = new MainStartDialogFragment();
        dialogFragment.setMainStartChooseCallback(mMainStartChooseCallback);
        dialogFragment.show(getSupportFragmentManager(), "MainStartDialogFragment");
    }

    /* private MainStartChooseCallback mMainStartChooseCallback = new MainStartChooseCallback() {
         @Override
         public void onLiveClick(final JSONObject startLiveInfo) {
             PermissionUtil.request(MainActivity.this,
                     new PermissionCallback() {
                         @Override
                         public void onAllGranted() {
                             forwardLive(startLiveInfo);
                         }
                     },
                     Manifest.permission.CAMERA,
                     Manifest.permission.RECORD_AUDIO
             );
         }

         @Override
         public void onVideoClick() {
             PermissionUtil.request(MainActivity.this,
                     new PermissionCallback() {
                         @Override
                         public void onAllGranted() {
                             startActivity(new Intent(mContext, VideoRecordActivity.class));
                         }
                     },
                     Manifest.permission.READ_EXTERNAL_STORAGE,
                     Manifest.permission.WRITE_EXTERNAL_STORAGE,
                     Manifest.permission.CAMERA,
                     Manifest.permission.RECORD_AUDIO
             );
         }

         @Override
         public void onVoiceClick(final JSONObject startLiveInfo) {
             PermissionUtil.request(MainActivity.this,
                     new PermissionCallback() {
                         @Override
                         public void onAllGranted() {
                             forwardVoiceLive(startLiveInfo);
                         }
                     },
                     Manifest.permission.RECORD_AUDIO
             );
         }

         @Override
         public void onScreenRecordLive(final JSONObject startLiveInfo) {
             PermissionUtil.request(MainActivity.this,
                     new PermissionCallback() {
                         @Override
                         public void onAllGranted() {
                             forwardScreenRecordLive(startLiveInfo);
                         }
                     },
                     Manifest.permission.RECORD_AUDIO
             );
         }
     };*/
    private final MainStartChooseCallback mMainStartChooseCallback = new MainStartChooseCallback() {


        @Override
        public void onLiveClick(final JSONObject startLiveInfo) {
            selectedType = 1;
            MainActivity.this.startLiveInfoo = startLiveInfo;
            if (Build.VERSION.SDK_INT < 33) {
                PermissionUtil.request(MainActivity.this,
                        new PermissionCallback() {
                            @Override
                            public void onAllGranted() {
                                forwardLive(startLiveInfo);
                            }
                        },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                );
            } else if (Build.VERSION.SDK_INT == 33) {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };

                requestPermissionLauncher.launch(permissions);
            } else {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                        Manifest.permission.RECORD_AUDIO
                };
                requestPermissionLauncher.launch(permissions);
            }
        }

        @Override
        public void onVideoClick() {
            selectedType = 2;
            if (Build.VERSION.SDK_INT < 33) {
                PermissionUtil.request(MainActivity.this,
                        new PermissionCallback() {
                            @Override
                            public void onAllGranted() {
                                startActivity(new Intent(mContext, VideoRecordActivity.class));
                            }
                        },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                );
            } else if (Build.VERSION.SDK_INT == 33) {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };

                requestPermissionLauncher.launch(permissions);
            } else {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };
                requestPermissionLauncher.launch(permissions);
            }
        }

        @Override
        public void onVoiceClick(final JSONObject startLiveInfo) {
            MainActivity.this.startLiveInfoo = startLiveInfo;
            selectedType = 3;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                PermissionUtil.request(MainActivity.this,
                        new PermissionCallback() {
                            @Override
                            public void onAllGranted() {
                                forwardVoiceLive(startLiveInfo);
                            }
                        },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                );
            } else if (Build.VERSION.SDK_INT == 33) {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };
                requestPermissionLauncher.launch(permissions);

            } else {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };
                requestPermissionLauncher.launch(permissions);
            }
        }

        @Override
        public void onScreenRecordLive(final JSONObject startLiveInfo) {
            selectedType = 4;
            MainActivity.this.startLiveInfoo = startLiveInfo;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                PermissionUtil.request(MainActivity.this,
                        new PermissionCallback() {
                            @Override
                            public void onAllGranted() {
                                forwardScreenRecordLive(startLiveInfo);
                            }
                        },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                );
            } else if (Build.VERSION.SDK_INT == 33) {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };
                requestPermissionLauncher.launch(permissions);

            } else {
                String[] permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };
                requestPermissionLauncher.launch(permissions);
            }
        }
    };

    /**
     * 开启直播
     */
    private void forwardLive(JSONObject obj) {
        int haveStore = obj.getIntValue("isshop");
        String forbidLiveTip = obj.getString("liveban_title");
        int sdk = obj.getIntValue("live_sdk");
        LiveConfigBean configBean = null;
        if (sdk == Constants.LIVE_SDK_TX) {
            configBean = JSON.parseObject(obj.getString("android_tx"), LiveConfigBean.class);
        }
        LiveAnchorActivity.forward(mContext, sdk, configBean, haveStore, false, forbidLiveTip, false);

    }

    /**
     * 开启录屏直播
     */
    private void forwardScreenRecordLive(JSONObject obj) {
        String forbidLiveTip = obj.getString("liveban_title");
        LiveAnchorActivity.forward(mContext, obj.getIntValue("live_sdk"), null, 0, false, forbidLiveTip, true);
    }

    /**
     * 开启语音直播
     */
    private void forwardVoiceLive(JSONObject obj) {
        int haveStore = obj.getIntValue("isshop");
        String forbidLiveTip = obj.getString("liveban_title");
        int sdk = obj.getIntValue("live_sdk");
        LiveConfigBean configBean = null;
        if (sdk == Constants.LIVE_SDK_TX) {
            configBean = JSON.parseObject(obj.getString("android_tx"), LiveConfigBean.class);
        }
        LiveAnchorActivity.forward(mContext, sdk, configBean, haveStore, true, forbidLiveTip, false);
    }

    /**
     * 检查版本更新
     */
    private void checkVersion() {
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    if (configBean.getMaintainSwitch() == 1) {//开启维护
                        DialogUitl.showSimpleTipDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.main_maintain_notice), configBean.getMaintainTips());
                    }
                    if (!VersionUtil.isLatest(configBean.getVersion())) {
                        VersionUtil.showDialog(mContext, configBean, configBean.getDownloadApkUrl());
                    }
                }
            }
        });
    }

    /**
     * 检查codeInstall安装参数
     */
    private void getAgentCode() {
        checkAgent();
    }


    /**
     * 检查是否显示填写邀请码的弹窗
     */
    private void checkAgent() {
        MainHttpUtil.checkAgent(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    boolean isHasAgent = obj.getIntValue("has_agent") == 1;//IS THERE A SUPERIOR SUBORDINATE RELATIONSHIP
                    boolean agentSwitch = obj.getIntValue("agent_switch") == 1;
                    boolean isAgentMust = obj.getIntValue("agent_must") == 1;//是否必填
                    if (!isHasAgent && agentSwitch) {
                        if (mFirstLogin || isAgentMust) {
                            showInvitationCode(isAgentMust);
                        }
                    }
                }
            }
        });
    }

    /**
     * 填写邀请码的弹窗
     */
    private void showInvitationCode(boolean inviteMust) {
        if (inviteMust) {
            NotCancelableInputDialog dialog = new NotCancelableInputDialog();
            dialog.setTitle(WordUtil.getString(com.livestreaming.common.R.string.main_input_invatation_code));
            dialog.setActionListener(new NotCancelableInputDialog.ActionListener() {
                @Override
                public void onConfirmClick(String content, final DialogFragment dialog) {
                    if (TextUtils.isEmpty(content)) {
                        ToastUtil.show(com.livestreaming.common.R.string.main_input_invatation_code);
                        return;
                    }
                    MainHttpUtil.setDistribut(content, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                                dialog.dismissAllowingStateLoss();
                            } else {
                                ToastUtil.show(msg);
                            }
                        }
                    });
                }

            });
            dialog.show(getSupportFragmentManager(), "NotCancelableInputDialog");
        } else {
            new DialogUitl.Builder(mContext)
                    .setTitle(WordUtil.getString(com.livestreaming.common.R.string.main_input_invatation_code))
                    .setCancelable(true)
                    .setInput(true)
                    .setBackgroundDimEnabled(true)
                    .setClickCallback(new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(final Dialog dialog, final String content) {
                            if (TextUtils.isEmpty(content)) {
                                ToastUtil.show(com.livestreaming.common.R.string.main_input_invatation_code);
                                return;
                            }
                            MainHttpUtil.setDistribut(content, new HttpCallback() {
                                @Override
                                public void onSuccess(int code, String msg, String[] info) {
                                    if (code == 0 && info.length > 0) {
                                        ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                                        dialog.dismiss();
                                    } else {
                                        ToastUtil.show(msg);
                                    }
                                }
                            });
                        }
                    })
                    .build()
                    .show();
        }
    }

    /**
     * 签到奖励
     */
    private void requestBonus() {
        MainHttpUtil.requestBonus(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (obj.getIntValue("bonus_switch") == 0) {
                        return;
                    }
                    int day = obj.getIntValue("bonus_day");
                    if (day <= 0) {
                        return;
                    }
                    List<BonusBean> list = JSON.parseArray(obj.getString("bonus_list"), BonusBean.class);
                    BonusViewHolder bonusViewHolder = new BonusViewHolder(mContext, mRootView);
                    bonusViewHolder.setData(list, day, obj.getString("count_day"));
                    bonusViewHolder.show();
                    mBonusViewHolder = bonusViewHolder;
                }
            }
        });
    }

    private void getChatToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://chat.one.donalive.net/api/biklog/" + CommonAppConfig.getInstance().getUid());

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    // Get the response code
                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // If the request is successful, process the input stream
                        try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
                            readStream(in); // Process the stream
                        }
                    } else {
                        System.out.println("Error: " + responseCode);
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Handle the exception properly
                }

            }
        }).start();
    }

    public void readStream(InputStream inputStream) throws IOException {
        // Use InputStreamReader to convert InputStream to Reader
        InputStreamReader reader = new InputStreamReader(inputStream);

        // Parse the input stream to a JsonObject using Gson's JsonParser
        JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();


        // Example: Access a specific field in the JSON response
        if (jsonResponse.has("token")) {
            CommonAppConfig.getInstance().chatToken = jsonResponse.get("token").getAsString();
        }
    }

    /**
     * 登录IM
     */
    private void loginIM() {
        String uid = CommonAppConfig.getInstance().getUid();
        ImMessageUtil.getInstance().loginImClient(uid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFristLoad) {
            mFristLoad = false;
            loadPageData(0);
            if (mHomeViewHolder != null) {
                mHomeViewHolder.setShowed(true);
            }
            NotificationUtil.checkNotificationsEnabled(mContext);
            getLocation();
            getPushAction();
        }

        if (mLoginChanged) {
            mLoginChanged = false;
            requestAfterLogin();
//            if (mMallViewHolder != null) {
//                mMallViewHolder.showMyShopAvatar();
//            }
        }
        if (mPaused) {
            if (mVideoViewHolder != null && mVideoViewHolder.isShowed()) {
                mVideoViewHolder.resumePlay();
                VideoHttpUtil.startWatchVideo();
            }
        }
        mPaused = false;
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            if (mBonusViewHolder != null) {
                mBonusViewHolder.dismiss();
                mBonusViewHolder = null;
            }
        }
    }

    /**
     * 获取所在位置
     */
    private void getLocation() {
        if (hasLocationPermission()) {
            LocationUtil.getInstance().startLocation();
        }
    }


    @Override
    protected void onDestroy() {
        if (mTabButtonGroup != null) {
            mTabButtonGroup.cancelAnim();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        EventBus.getDefault().unregister(this);
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_SDK);
        MainHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        MainHttpUtil.cancel(MainHttpConsts.REQUEST_BONUS);
        MainHttpUtil.cancel(MainHttpConsts.GET_BONUS);
        MainHttpUtil.cancel(MainHttpConsts.CHECK_AGENT);
        MainHttpUtil.cancel(MainHttpConsts.SET_DISTRIBUT);
        MainHttpUtil.cancel(MainHttpConsts.CHANGE_TEENAGER_TIME);
        if (mCheckLivePresenter != null) {
            mCheckLivePresenter.cancel();
        }
        LocationUtil.getInstance().stopLocation();
        CommonAppConfig.getInstance().setGiftListJson(null);
        CommonAppConfig.getInstance().setLaunched(false);
        LiveStorge.getInstance().clear();
        VideoStorge.getInstance().clear();
        FloatWindowUtil.getInstance().dismiss();
        LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
        LiveChatRoomPlayUtil.getInstance().release();
        super.onDestroy();
    }

    public static void forward(Context context) {
        forward(context, null);
    }

    public static void forward(Context context, Bundle bundle) {
        Intent intent = new Intent(context, MainActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public void watchLive(LiveBean liveBean, String key, int position) {
        watchLive(liveBean, key, position, true);
    }

    /**
     * 观看直播
     */
    public void watchLive(LiveBean liveBean, String key, int position, boolean needShowDialog) {
        if (!FloatWindowHelper.checkVoice(true)) {
            return;
        }
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new CheckLivePresenter(mContext);
        }
        if (CommonAppConfig.LIVE_ROOM_SCROLL) {
            mCheckLivePresenter.watchLive(liveBean, key, position, needShowDialog);
        } else {
            mCheckLivePresenter.watchLive(liveBean, needShowDialog);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        if (!CommonAppConfig.getInstance().isPrivateMsgSwitchOpen()) {
            return;
        }
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        if (unReadCount != null) {
            String count = unReadCount.getTotalUnReadCount();
            if (!TextUtils.isEmpty(count)) {
//                if (mHomeViewHolder != null) {
//                    mHomeViewHolder.setUnReadCount(count);
//                }
                if (mActiveViewHolder != null) {
                    mActiveViewHolder.setUnReadCount(count);
                }
                if (mMeViewHolder != null) {
                    mMeViewHolder.setUnReadCount(count);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickBackTime > 2000) {
            mLastClickBackTime = curTime;
            ToastUtil.show(com.livestreaming.common.R.string.main_click_next_exit);
            return;
        }
        super.onBackPressed();
    }


    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsMainViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    try {
                        if (mHomeViewHolder == null) {
                            mHomeViewHolder = new MainHomeViewHolder(mContext, parent);

                            mHomeViewHolder.setAppBarLayoutListener(this);
                        }
                        vh = mHomeViewHolder;
                    } catch (Exception e) {

                    }
                } else if (position == 1) {

                    mVideoViewHolder = new MainHomeVideoScrollVh(mContext, parent);
                    //mMallViewHolder = new MainMallViewHolder(mContext, parent);
                    vh = mVideoViewHolder;
                } else if (position == 2) {
                    mActiveViewHolder = new MainActiveViewHolder(mContext, parent);
                    mActiveViewHolder.setAppBarLayoutListener(this);
                    vh = mActiveViewHolder;
                } else if (position == 3) {
                    mMeViewHolder = new MainMeViewHolder(mContext, parent);
                    // mMeViewHolder.loadData();
                    vh = mMeViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }

    @Override
    public void onOffsetChangedDirection(boolean up) {
        if (!mAnimating) {
            if (up) {
                if (mShowed && mDownAnimator != null) {
                    mDownAnimator.start();
                }
            } else {
                if (mHided && mUpAnimator != null) {
                    mUpAnimator.start();
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoViewHolder != null && mVideoViewHolder.isShowed()) {
            mVideoViewHolder.pausePlay();
            VideoHttpUtil.endWatchVideo();
            mPaused = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP -> {
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
                yield true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
                yield true;
            }
            default -> super.onKeyDown(keyCode, event);
        };
    }


    /**
     * 观众退出语音直播间，显示悬浮窗
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveAudienceExitEvent(LiveAudienceChatRoomExitEvent e) {
        LiveBean liveBean = e.getLiveBean();
        if (liveBean != null) {
            if (liveBean.isVoiceRoom()) {
                FloatWindowUtil.getInstance().setType(Constants.FLOAT_TYPE_DEFAULT)
                        .setLiveBean(liveBean)
                        .setLiveAudienceFloatWindowData(e.getLiveAudienceAgoraData())
                        .requestPermission();
            }
        }
    }


    /**
     * 点击悬浮窗，进入直播间
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveFloatWindowEvent(LiveFloatWindowEvent e) {
        if (e.getType() == Constants.FLOAT_TYPE_DEFAULT) {
            if (e.getLiveBean().isVoiceRoom()) {
                watchLive(e.getLiveBean(), "", 0, false);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloseFloatWindowEvent(CloseFloatWindowEvent e) {
        FloatWindowUtil.getInstance().dismiss();
    }


    /**
     * 登录状态改变
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginChangeEvent(LoginChangeEvent e) {
        mLoginChanged = true;
        mFirstLogin = e.isFirstLogin();
    }


    public void setCurrentPage(int position) {
        if (mTabButtonGroup != null) {
            mTabButtonGroup.setCurPosition(position);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mViewPager != null) {
            outState.putInt("CurrentItem", mViewPager.getCurrentItem());
        }
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int savedCurrentItem = savedInstanceState.getInt("CurrentItem", 0);
        if (mViewPager != null) {
            if (savedCurrentItem != mViewPager.getCurrentItem()) {
                mViewPager.setCurrentItem(savedCurrentItem, false);
            }
        }
        if (mTabButtonGroup != null) {
            mTabButtonGroup.setCurPosition(savedCurrentItem);
        }
    }

    /**
     * 是否开启青少年模式
     */
    private void checkTeenager() {
        MainHttpUtil.checkTeenager(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        boolean isOpen = obj.getIntValue("status") == 1;
                        CommonAppConfig.getInstance().setTeenagerType(isOpen);
                        if (!isOpen) {

                            requestBonus();//每日签到
                        } else {
                            int is_tip = obj.getIntValue("is_tip");
                            if (is_tip == 1) {
                                mIsTeengerTick = false;
                                RouteUtil.teenagerTip(obj.getString("tips"));
                            } else {
                                startTeenagerCountdown();
                            }
                        }
                    }

                }
            }
        });
    }

    /**
     * 启动青少年模式定时器
     */
    private void startTeenagerCountdown() {
        if (mIsTeengerTick) {
            return;
        }
        mIsTeengerTick = true;
        if (mTeengerTicker == null) {
            mTeengerTicker = new Runnable() {
                @Override
                public void run() {
                    if (mHandler == null) {
                        mHandler = new Handler();
                    }
                    long now = SystemClock.uptimeMillis();
                    if (mTeenagerTimeCallback == null) {
                        mTeenagerTimeCallback = new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code != 0) {
                                    mIsTeengerTick = false;
                                    if (mHandler != null) {
                                        mHandler.removeCallbacks(mTeengerTicker);
                                    }
                                    if (code == 10010 || code == 10011) {
                                        RouteUtil.teenagerTip(msg);
                                    }
                                }
                            }
                        };
                    }
                    MainHttpUtil.changeTeenagerTime(mTeenagerTimeCallback);
                    if (mIsTeengerTick) {
                        long next = now + (1000 - now % 1000) + 9000;
                        mHandler.postAtTime(mTeengerTicker, next);
                    }
                }
            };
        }
        mTeengerTicker.run();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificiationLiveEvent(NotificiationClickEvent e) {
        if (e == null) {
            return;
        }
        int type = e.getType();
        if (type == Constants.PUSH_TYPE_MESSAGE) {
            ChatActivity.forward(mContext);
        } else if (type == Constants.PUSH_TYPE_LIVE) {
//            enterLiveRoomByPush(e.getData());
        }
    }

    private void getPushAction() {
        Intent intent = getIntent();
        int pushType = intent.getIntExtra(Constants.PUSH_TYPE, 0);
        String pushData = intent.getStringExtra(Constants.PUSH_DATA);
        if (pushType == Constants.PUSH_TYPE_MESSAGE) {
            if (CommonAppConfig.getInstance().isLogin()) {
                ChatActivity.forward(mContext);
            }
        } else if (pushType == Constants.PUSH_TYPE_LIVE) {
            if (CommonAppConfig.getInstance().isLogin()) {
                enterLiveRoomByPush(pushData);
            }
        }
    }

    /**
     * 通过推送的数据进入直播间
     */
    private void enterLiveRoomByPush(String liveUserInfo) {
        if (TextUtils.isEmpty(liveUserInfo)) {
            return;
        }
        try {
            LiveBean liveBean = JSON.parseObject(liveUserInfo, LiveBean.class);
            watchLive(liveBean, "", 0);
        } catch (Exception e) {

        }
    }

    public void setBottomVisible(boolean visible) {
        if (mBottom != null) {
            mBottom.setTranslationX(visible ? 0 : 100000);
        }
    }

}
