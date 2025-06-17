package com.livestreaming.common.utils;
import android.os.Build;

public class DeviceInfoUtil {

    public static String getDeviceDetailsJson() {
        try {

            return Build.DEVICE+"_"+Build.MODEL;
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}

