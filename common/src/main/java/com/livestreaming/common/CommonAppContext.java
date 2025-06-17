package com.livestreaming.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.livestreaming.common.event.AppLifecycleEvent;
import com.livestreaming.common.interfaces.AppLifecycleUtil;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;


/**
 * Created by cxf on 2017/8/3.
 */

public class CommonAppContext extends MultiDexApplication {

    private static CommonAppContext sInstance;
    private static Handler sMainThreadHandler;
    private int mCount;
    private boolean mFront;//是否前台


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        sMainThreadHandler=new Handler();
        registerActivityLifecycleCallbacks();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }

    public static CommonAppContext getInstance() {
        if (sInstance == null) {
            try {
                Class clazz = Class.forName("android.app.ActivityThread");
                Method method = clazz.getMethod("currentApplication", new Class[]{});
                Object obj = method.invoke(null, new Object[]{});
                if (obj != null && obj instanceof CommonAppContext) {
                    sInstance = (CommonAppContext) obj;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }



    public static void postDelayed(Runnable runnable, long delayMillis) {
        if (sMainThreadHandler != null) {
            sMainThreadHandler.postDelayed(runnable, delayMillis);
        }
    }

    public static void post(Runnable runnable) {
        if (sMainThreadHandler != null) {
            sMainThreadHandler.post(runnable);
        }
    }


    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mCount++;
                if (!mFront) {
                    mFront = true;
                    L.e("AppContext------->处于前台");
                    EventBus.getDefault().post(new AppLifecycleEvent(true));
                    CommonAppConfig.getInstance().setFrontGround(true);
                    FloatWindowHelper.setFloatWindowVisible(true);
                    AppLifecycleUtil.onAppFrontGround();

                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mCount--;
                if (mCount == 0) {
                    mFront = false;
                    L.e("AppContext------->处于后台");
                    EventBus.getDefault().post(new AppLifecycleEvent(false));
                    CommonAppConfig.getInstance().setFrontGround(false);
                    FloatWindowHelper.setFloatWindowVisible(false);
                    AppLifecycleUtil.onAppBackGround();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

 public boolean isFront() {
        return mFront;
    }

    public void startInitSdk(){

    }
}
