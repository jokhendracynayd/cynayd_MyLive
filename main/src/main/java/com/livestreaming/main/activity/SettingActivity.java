package com.livestreaming.main.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.event.LoginChangeEvent;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.VersionUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.SettingAdapter;
import com.livestreaming.main.bean.SettingBean;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/9/30.
 */

public class SettingActivity extends AbsActivity implements SettingAdapter.ActionListener {

    private static final String TAG = "";
    private RecyclerView mRecyclerView;
    private Handler mHandler;
    private SettingAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.setting));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        MainHttpUtil.getSettingList(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                List<SettingBean> list0 = JSON.parseArray(Arrays.toString(info), SettingBean.class);
                List<SettingBean> list = new ArrayList<>();
                SettingBean bean = new SettingBean();
                bean.setId(-1);
                bean.setName(WordUtil.getString(com.livestreaming.common.R.string.setting_brightness));
                bean.setChecked(SpUtil.getInstance().getBrightness() == 0.05f);
                list.add(bean);

                bean = new SettingBean();
                bean.setId(-3);
                bean.setName(WordUtil.getString(com.livestreaming.common.R.string.setting_msg_window));
                bean.setChecked(CommonAppConfig.getInstance().isShowLiveFloatWindow());
                list.add(bean);

                bean = new SettingBean();
                bean.setId(-2);
                bean.setName(WordUtil.getString(com.livestreaming.common.R.string.setting_msg_ring));
                bean.setChecked(SpUtil.getInstance().isImMsgRingOpen());
                list.add(bean);

                bean = new SettingBean();
                bean.setId(-4);
                bean.setName(WordUtil.getString(com.livestreaming.common.R.string.setting_msg_language));
                list.add(bean);

                list.addAll(list0);
                bean = new SettingBean();
                bean.setId(-6);
                bean.setName(WordUtil.getString(com.livestreaming.common.R.string.notification));
                list.add(bean);
                bean = new SettingBean();
                bean.setId(-5);
                bean.setName(WordUtil.getString(com.livestreaming.common.R.string.setting_exit));
                list.add(bean);
                mAdapter = new SettingAdapter(mContext, list, VersionUtil.getVersion(), getCacheSize());
                mAdapter.setActionListener(SettingActivity.this);
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }


    @Override
    public void onItemClick(SettingBean bean, int position) {
        String href = bean.getHref();
        if (TextUtils.isEmpty(href)) {
            if (bean.getId() == -6) {
                openNotificationsSettings();
            } else if (bean.getId() == -5) {//退出登录
                new DialogUitl.Builder(mContext)
                        .setContent(WordUtil.getString(com.livestreaming.common.R.string.logout_confirm))
                        .setConfrimString(WordUtil.getString(com.livestreaming.common.R.string.logout_confirm_2))
                        .setCancelable(true)
                        .setIsHideTitle(true)
                        .setBackgroundDimEnabled(true)
                        .setClickCallback(new DialogUitl.SimpleCallback() {
                            @Override
                            public void onConfirmClick(Dialog dialog, String content) {
                                logout();
                            }
                        })
                        .build()
                        .show();

            } else if (bean.getId() == Constants.SETTING_MODIFY_PWD) {//修改密码
                forwardModifyPwd();
            } else if (bean.getId() == Constants.SETTING_UPDATE_ID) {//检查更新
                checkVersion();
            } else if (bean.getId() == Constants.SETTING_CLEAR_CACHE) {//清除缓存
                clearCache(position);
            }
        } else {
            if (bean.getId() == 19) {//注销账号
                CancelConditionActivity.forward(mContext, href);
                return;
            }
            if (bean.getId() == 17) {//意见反馈要在url上加版本号和设备号
                if (!href.contains("?")) {
                    href = StringUtil.contact(href, "?");
                }
                href = StringUtil.contact(href, "&version=", Build.VERSION.RELEASE, "&model=", Build.MODEL);
            }
            WebViewActivity.forward(mContext, href);
        }
    }

    AlertDialog dialog = null;

    private void openNotificationsSettings() {

        SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("notification_settings", MODE_PRIVATE);
        int isBoxNotificationEnabled = prefs.getInt("box_notif", 1);
        int isBackNotificationEnabled = prefs.getInt("back_notif", 1);

        View v = getLayoutInflater().inflate(R.layout.notification_settings, null, false);

        ImageView close = (ImageView) v.findViewById(R.id.close_btn);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        SwitchCompat back_notif = v.findViewById(R.id.box_notif);
        back_notif.setChecked(isBackNotificationEnabled == 1);
        back_notif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateBackNotif(isChecked);
            }
        });
        SwitchCompat box_notif = v.findViewById(R.id.back_notif);
        box_notif.setChecked(isBoxNotificationEnabled == 1);
        box_notif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateBoxNotif(isChecked);
            }
        });
        dialog = new AlertDialog.Builder(this)
                .setView(v).setCancelable(false).create();
        dialog.show();
    }

    private void updateBackNotif(boolean isChecked) {
        SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("notification_settings", MODE_PRIVATE);
        prefs.edit().putInt("back_notif", isChecked ? 1 : 0).commit();
    }

    private void updateBoxNotif(boolean isChecked) {
        SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("notification_settings", MODE_PRIVATE);
        prefs.edit().putInt("box_notif", isChecked ? 1 : 0).commit();
    }

    @Override
    public void onCheckChanged(SettingBean bean) {
        int id = bean.getId();
        if (id == -1) {
            SpUtil.getInstance().setBrightness(bean.isChecked() ? 0.05f : -1f);
            updateBrightness();
        } else if (id == -2) {
            SpUtil.getInstance().setImMsgRingOpen(bean.isChecked());
        }
    }

    @Override
    public void onLanguageClick() {
        DialogUitl.showStringArrayDialog(mContext, new String[]{
                "English", "عربي", "Français", "Deutsch", "Türkçe", "Español", "Italiano", "română"
        }, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                String lang = null;
                if ("English" == (text)) {
                    lang = "en";
                } else if ("Français" == (text)) {
                    lang = "fr";
                } else if ("Deutsch" == (text)) {
                    lang = "de";
                } else if ("Türkçe" == (text)) {
                    lang = "tr";
                } else if ("Español" == (text)) {
                    lang = "es";
                } else if ("Italiano" == (text)) {
                    lang = "it";
                } else if ("română" == (text)) {
                    lang = "ro";
                } else {
                    lang = "ar";
                }
                Log.e("curLang", lang);
                changeLanguage(lang);
            }
        });
    }

    /**
     * 切换语言
     */
    private void changeLanguage(String lang) {
        saveToSharedPref(lang);
        doRestart();
    }

    private void saveToSharedPref(String lang) {
        LanguageUtil.getInstance().setLanguage(lang);
        SharedPreferences preferences = getSharedPreferences("myCurrentLang", MODE_PRIVATE);
        preferences.edit().putString("current_lang", lang).commit();
    }

    public void doRestart() {
        try {
            //fetch the packagemanager so we can get the default launch activity
            // (you can replace this intent with any other activity if you want
            PackageManager pm = getApplicationContext().getPackageManager();
            //check if we got the PackageManager
            if (pm != null) {
                //create the intent with the default start activity for your application
                Intent mStartActivity = pm.getLaunchIntentForPackage(
                        getApplicationContext().getPackageName()
                );
                if (mStartActivity != null) {
                    mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //create a pending intent so the application is restarted after System.exit(0) was called.
                    // We use an AlarmManager to call this intent in 100ms
                    int mPendingIntentId = 223344;
                    PendingIntent mPendingIntent = PendingIntent
                            .getActivity(getApplicationContext(), mPendingIntentId, mStartActivity,
                                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
                    //kill the application
                    System.exit(0);
                } else {
                    Log.e(TAG, "Was not able to restart application, mStartActivity null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, PM null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }

    }

    /**
     * 检查更新
     */
    private void checkVersion() {
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    if (VersionUtil.isLatest(configBean.getVersion())) {
                        ToastUtil.show(com.livestreaming.common.R.string.version_latest);
                    } else {
                        VersionUtil.showDialog(mContext, configBean, configBean.getDownloadApkUrl());
                    }
                }
            }
        });

    }

    /**
     * 退出登录
     */
    private void logout() {
        CommonAppConfig.getInstance().clearLoginInfo();
        //退出IM
        ImMessageUtil.getInstance().logoutImClient();
        EventBus.getDefault().post(new LoginChangeEvent(false, false));
        FirebaseMessaging.getInstance().deleteToken();
        finish();
    }

    /**
     * 修改密码
     */
    private void forwardModifyPwd() {
        startActivity(new Intent(mContext, ModifyPwdActivity.class));
    }

    /**
     * 获取缓存
     */
    private String getCacheSize() {
        return ImgLoader.getCacheSize();
    }

    /**
     * 清除缓存
     */
    private void clearCache(final int position) {
        final Dialog dialog = DialogUitl.loadingDialog(mContext, getString(com.livestreaming.common.R.string.setting_clear_cache_ing));
        dialog.show();
        ImgLoader.clearImageCache();
        File gifGiftDir = new File(CommonAppConfig.GIF_PATH);
        if (gifGiftDir.exists() && gifGiftDir.length() > 0) {
            gifGiftDir.delete();
        }
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (mAdapter != null) {
                    mAdapter.setCacheString(getCacheSize());
                    mAdapter.notifyItemChanged(position);
                }
                ToastUtil.show(com.livestreaming.common.R.string.setting_clear_cache);
            }
        }, 2000);
    }


    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        MainHttpUtil.cancel(MainHttpConsts.GET_SETTING_LIST);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        MainHttpUtil.cancel(MainHttpConsts.SET_LIVE_WINDOW);
        super.onDestroy();
    }


}
